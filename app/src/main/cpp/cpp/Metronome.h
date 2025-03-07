#ifndef METRONOMEPLUS_METRONOME_H
#define METRONOMEPLUS_METRONOME_H

#include <vector>
#include <thread>
#include <android/asset_manager.h>
#include <oboe/Oboe.h>

#include "model/Beat.h"
#include "audio/Player.h"
#include "audio/Mixer.h"

using namespace oboe;

#ifdef __cplusplus
extern "C" {
#endif
void notifyUiChangeBeat(int beatIndex);
#ifdef __cplusplus
}
#endif

class Metronome : public AudioStreamDataCallback {
public:
    explicit Metronome(AAssetManager &);

    DataCallbackResult onAudioReady(
            AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    void init();
    void end();
    void setBPM(int bpm);
    void setBeats(const std::vector<Beat> &beats);
    void startPlaying();
    void stopPlaying();

private:
    Mixer mMixer;
    std::shared_ptr<AudioStream> mAudioStream;
    std::unique_ptr<Player> mNormalBeatPlayer;
    std::unique_ptr<Player> mAccentBeatPlayer;
    std::unique_ptr<Player> mMediumBeatPlayer;

    std::thread mBeatThread;
    std::atomic<bool> mIsMetronomePlaying{false};

    std::mutex mMutex;
    std::condition_variable mCondition;

    std::vector<Beat> mBeats;
    int mBPM{60};
    int mIntervalMs{1000};
    int mCurrentBeatIndex{0};

    bool openStream();
    bool setupAudioSources();
    bool setupPlayerBeat(const char beat[], std::unique_ptr<Player> *playerBeat);

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-private-field"
    AAssetManager &mAssetManager;
#pragma clang diagnostic pop
};

#endif //METRONOMEPLUS_METRONOME_H