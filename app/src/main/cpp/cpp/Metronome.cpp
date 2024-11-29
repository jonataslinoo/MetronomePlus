#include <thread>

#include "utils/Logging.h"
#include "utils/Constants.h"
#include "Metronome.h"
#include "audio/AAssetDataSource.h"

Metronome::Metronome(AAssetManager &assetManager) : mAssetManager(assetManager) {
}

DataCallbackResult Metronome::onAudioReady(oboe::AudioStream *oboeStream, void *audioData,
                                           int32_t numFrames) {

//    auto *outputBuffer = static_cast<float *>(audioData);
//
//    int64_t nextClapEventMs;
//
//    for (int i = 0; i < numFrames; i++) {

//        mSongPositionsMs = convertFramesToMillis(
//                mCurrentFrame,
//                mAudioStream->getSampleRate());
//
//        if (mClapEvents.peek(nextClapEventMs) && mSongPositionsMs >= nextClapEventMs) {
//            mBaseNote->setPlaying(true);
//            mClapEvents.pop(nextClapEventMs);
//        }

//        mMixer.renderAudio(outputBuffer + (oboeStream->getChannelCount() * i), 1);
//        mCurrentFrame++;
//    }

//    mLastUpdateTime = nowUptimeMillis();


    mMixer.renderAudio(static_cast<float *>(audioData), numFrames);

    return DataCallbackResult::Continue;
}

void Metronome::init() {

    openStream();

    setupAudioSources();

    Result result = mAudioStream->requestStart();
    if (result != Result::OK){
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
//    AudioProperties targetProperties{
//            .channelCount = kChannelCount,
//            .sampleRate = kSampleRate
//    };
//
//    std::shared_ptr<AAssetDataSource> mNormalToneSource{
//            AAssetDataSource::newFromCompressedAsset(mAssetManager,
//                                                     kNormalTone,
//                                                     targetProperties)
//    };
////    std::shared_ptr<AAssetDataSource> mNormalToneSource{
////            AAssetDataSource::newFromCompressedAsset(mAssetManager,
////                                                     "CLAP.mp3",
////                                                     targetProperties)
////    };
//
//    if (mNormalToneSource == nullptr) {
//        LOGE("Could not load source data for normal tone sound");
//        return false;
//    }
//
//    mNormalTonePlayer = std::make_unique<Player>(mNormalToneSource);
//
//    std::shared_ptr<AAssetDataSource> mAccentToneSource{
//            AAssetDataSource::newFromCompressedAsset(mAssetManager,
//                                                     kAccentTone,
//                                                     targetProperties)
//    };
//
//    if (mAccentToneSource == nullptr) {
//        LOGE("Could not load source data for accent tone sound");
//        return false;
//    }
//
//    mAccentTonePlayer = std::make_unique<Player>(mAccentToneSource);
//
//
//    std::shared_ptr<AAssetDataSource> mMediumToneSource{
//            AAssetDataSource::newFromCompressedAsset(mAssetManager,
//                                                     kMediumTone,
//                                                     targetProperties)
//    };
//
//    if (mMediumToneSource == nullptr) {
//        LOGE("Could not load source data for medium tone sound");
//        return false;
//    }
//
//    mMediumTonePlayer = std::make_unique<Player>(mMediumToneSource);

    if(!setupPlayerTone(kNormalTone, &mNormalTonePlayer))
    {
        LOGE("Could not load source data for normal tone sound");
        return false;
    }

    if(!setupPlayerTone(kAccentTone, &mAccentTonePlayer))
    {
        LOGE("Could not load source data for accent tone sound");
        return false;
    }

    if(!setupPlayerTone(kMediumTone, &mMediumTonePlayer))
    {
        LOGE("Could not load source data for medium tone sound");
        return false;
    }

    mMixer.addTrack(mNormalTonePlayer.get());
    mMixer.addTrack(mMediumTonePlayer.get());
    mMixer.addTrack(mAccentTonePlayer.get());
    mMixer.setChannelCount(mAudioStream->getChannelCount());
    return true;
}

bool Metronome::setupPlayerTone(const char tone[], std::unique_ptr<Player> *playerTone) {

    AudioProperties targetProperties{
            .channelCount = kChannelCount,
            .sampleRate = kSampleRate
    };

    std::shared_ptr<AAssetDataSource> mToneSource{
            AAssetDataSource::newFromCompressedAsset(mAssetManager,
                                                     tone,
                                                     targetProperties)
    };

    if (mToneSource == nullptr) {
        return false;
    }

    *playerTone = std::make_unique<Player>(mToneSource);

    return true;
}


//configure tone variables to play the desired sound

//end configure tone variables to play the desired sound

void Metronome::setBPM(int bpm) {
    mBPM = bpm;
    mIntervalMs = 60000 / mBPM; // Calcula o intervalo em milissegundos
    LOGI("BPM atualizado para: %d, Intervalo: %d ms", mBPM, mIntervalMs);
}

void Metronome::setTones(const std::vector<Tone> &notes) {
    mNotes = notes;
    mCurrentNoteIndex = 0; // Reinicia o índice

    LOGI("Notas configuradas:");
    for (const auto &note: notes) {
        LOGI("Estado da nota: %d", note.state);
    }
}

void Metronome::startPlaying() {
    LOGI("iniciando");
    if (mIsPlayingNotes) return;
    mIsPlayingNotes = true;
    LOGI("iniciou");
    mClapThread = std::thread([this]() {
        while (mIsPlayingNotes) {
            if (mNotes.empty()) continue;
            const Tone &currentNote = mNotes[mCurrentNoteIndex];
            switch (currentNote.state) {
                case ToneState::Normal:
                    mNormalTonePlayer->setPlaying(true);
                    LOGI("normal tone");
                    break;

                case ToneState::Silence:
                    break;

                case ToneState::Accent:
                    mAccentTonePlayer->setPlaying(true);
                    LOGI("accent tone");

                    break;

                case ToneState::Medium:
                    mMediumTonePlayer->setPlaying(true);
                    LOGI("medium tone");

                    break;
            }

            // Intervalo baseado no BPM
            std::this_thread::sleep_for(std::chrono::milliseconds(mIntervalMs));
            // Volta ao início do compasso
            mCurrentNoteIndex = (mCurrentNoteIndex + 1) % mNotes.size();
        }
    });
}

void Metronome::stopPlaying() {
    mIsPlayingNotes = false;
    if (mClapThread.joinable()) {
        mClapThread.join();
    }
}

