#include "../utils/Logging.h"
#include <oboe/Oboe.h>

#include "AAssetDataSource.h"

#include "NDKExtractor.h"


constexpr int kMaxCompressionRatio { 12 };

AAssetDataSource* AAssetDataSource::newFromCompressedAsset(
        AAssetManager &assetManager,
        const char *filename,
        const AudioProperties targetProperties) {

    AAsset *asset = AAssetManager_open(&assetManager, filename, AASSET_MODE_UNKNOWN);
    if (!asset) {
        LOGE("Failed to open asset %s", filename);
        return nullptr;
    }

    off_t assetSize = AAsset_getLength(asset);
    LOGD("Opened %s, size %ld", filename, assetSize);

    // Allocate memory to store the decompressed audio. We don't know the exact
    // size of the decoded data until after decoding so we make an assumption about the
    // maximum compression ratio and the decoded sample format (float for FFmpeg, int16 for NDK).
    const long maximumDataSizeInBytes = kMaxCompressionRatio * assetSize * sizeof(int16_t);
    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int64_t bytesDecoded = NDKExtractor::decode(asset, decodedData, targetProperties);
    auto numSamples = bytesDecoded / sizeof(int16_t);

    // Now we know the exact number of samples we can create a float array to hold the audio data
    auto outputBuffer = std::make_unique<float[]>(numSamples);

    // The NDK decoder can only decode to int16, we need to convert to floats
    oboe::convertPcm16ToFloat(
            reinterpret_cast<int16_t*>(decodedData),
            outputBuffer.get(),
            bytesDecoded / sizeof(int16_t));

    delete[] decodedData;
    AAsset_close(asset);

    return new AAssetDataSource(std::move(outputBuffer),
            numSamples,
            targetProperties);
}