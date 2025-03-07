#include <thread>

#include "utils/Logging.h"
#include "utils/Constants.h"
#include "Metronome.h"
#include "audio/AAssetDataSource.h"

Metronome::Metronome(AAssetManager &assetManager) : mAssetManager(assetManager) {
}

DataCallbackResult Metronome::onAudioReady(oboe::AudioStream *oboeStream, void *audioData,
                                           int32_t numFrames) {

    mMixer.renderAudio(static_cast<float *>(audioData), numFrames);

    return DataCallbackResult::Continue;
}

void Metronome::init() {

    openStream();

    setupAudioSources();

    Result result = mAudioStream->requestStart();
    if (result != Result::OK) {
        LOGE("Failed to start stream. Error: %s", convertToText(result));
        return;
    }
}

void Metronome::end() {

    if (mAudioStream) {
        mAudioStream->stop();
        mAudioStream->close();
        mAudioStream.reset();
    }
}

bool Metronome::openStream() {
    AudioStreamBuilder builder;
    builder.setFormat(AudioFormat::Float);
    builder.setFormatConversionAllowed(true);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setSharingMode(SharingMode::Exclusive);
    builder.setSampleRate(kSampleRate);
    builder.setSampleRateConversionQuality(SampleRateConversionQuality::Medium);
    builder.setChannelCount(kChannelCount);
    builder.setDataCallback(this);

    Result result = builder.openStream(mAudioStream);
    if (result != Result::OK) {
        LOGE("Failed to open stream. Error: %s", convertToText(result));
        return false;
    }
    return true;
}

bool Metronome::setupAudioSources() {
    if (!setupPlayerBeat(kNormalBeat, &mNormalBeatPlayer)) {
        LOGE("Could not load source data for normal beat sound");
        return false;
    }

    if (!setupPlayerBeat(kAccentBeat, &mAccentBeatPlayer)) {
        LOGE("Could not load source data for accent beat sound");
        return false;
    }

    if (!setupPlayerBeat(kMediumBeat, &mMediumBeatPlayer)) {
        LOGE("Could not load source data for medium beat sound");
        return false;
    }

    mMixer.addTrack(mNormalBeatPlayer.get());
    mMixer.addTrack(mMediumBeatPlayer.get());
    mMixer.addTrack(mAccentBeatPlayer.get());
    mMixer.setChannelCount(mAudioStream->getChannelCount());
    return true;
}

bool Metronome::setupPlayerBeat(const char beat[], std::unique_ptr<Player> *playerBeat) {

    AudioProperties targetProperties{
            .channelCount = kChannelCount,
            .sampleRate = kSampleRate
    };

    std::shared_ptr<AAssetDataSource> mBeatSource{
            AAssetDataSource::newFromCompressedAsset(mAssetManager,
                                                     beat,
                                                     targetProperties)
    };

    if (mBeatSource == nullptr) {
        return false;
    }

    *playerBeat = std::make_unique<Player>(mBeatSource);

    return true;
}

void Metronome::setBPM(int bpm) {
    mBPM = bpm;
    mIntervalMs = 60000 / mBPM;
}

void Metronome::setBeats(const std::vector<Beat> &beats) {
    mBeats = beats;
}

void Metronome::startPlaying() {
    if (mIsMetronomePlaying) return;
    if (mBeats.empty()) return;

    mIsMetronomePlaying = true;
    mCurrentBeatIndex = 0;

    mBeatThread = std::thread([this]() {
        std::unique_lock<std::mutex> lock(mMutex);
        int totalBeatsPerMeasure = 0;

        while (mIsMetronomePlaying) {

            totalBeatsPerMeasure = mBeats.size();
            mCurrentBeatIndex = mCurrentBeatIndex % totalBeatsPerMeasure;

            const Beat &currentBeat = mBeats[mCurrentBeatIndex];
            switch (currentBeat.stateDto) {
                case BeatState::Normal:
                    mNormalBeatPlayer->setPlaying(true);
                    break;

                case BeatState::Silence:
                    break;

                case BeatState::Accent:
                    mAccentBeatPlayer->setPlaying(true);
                    break;

                case BeatState::Medium:
                    mMediumBeatPlayer->setPlaying(true);
                    break;
            }

            mCondition.wait_for(lock, std::chrono::milliseconds(mIntervalMs),
                                [this] { return !mIsMetronomePlaying; });
            mCurrentBeatIndex = (mCurrentBeatIndex + 1) % totalBeatsPerMeasure;
        }
    });
}

void Metronome::stopPlaying() {
    {
        std::lock_guard<std::mutex> lock(mMutex);
        mIsMetronomePlaying = false;
    }
    mCondition.notify_all();

    if (mBeatThread.joinable()) {
        mBeatThread.join();
    }
}