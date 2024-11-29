#ifndef METRONOMEPLUS_METRONOME_H
#define METRONOMEPLUS_METRONOME_H

#include <vector>
#include <thread>
#include <android/asset_manager.h>
#include <oboe/Oboe.h>

#include "model/Tone.h"
#include "audio/Player.h"
#include "audio/Mixer.h"

using namespace oboe;

class Metronome : public AudioStreamDataCallback {
public:
    explicit Metronome(AAssetManager &);

    DataCallbackResult onAudioReady(
            AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    void init();
    void end();
    void setBPM(int bpm);
    void setTones(const std::vector<Tone> &notes);
    void startPlaying();
    void stopPlaying();

private:
    Mixer mMixer;
    std::shared_ptr<AudioStream> mAudioStream;
    std::unique_ptr<Player> mNormalTonePlayer;
    std::unique_ptr<Player> mAccentTonePlayer;
    std::unique_ptr<Player> mMediumTonePlayer;

    std::thread mClapThread;                       // Thread para tocar as palmas
    std::atomic<bool> mIsMetronomePlaying{};         // Controle se as palmas estão tocando
    int mBPM{60};                                  // BPM padrão
    int mIntervalMs{1000};                         // Intervalo entre os toques em milissegundos

    std::vector<Tone> mNotes;                      // Notas do compasso
    int mCurrentNoteIndex{0};                      // Índice da nota atual
    std::atomic<bool> mIsPlayingNotes{false};   // Controle se as notas estão sendo tocadas

    bool openStream();
    bool setupAudioSources();
    bool setupPlayerTone(const char tone[], std::unique_ptr<Player> *playerTone);

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-private-field"
    AAssetManager &mAssetManager;
#pragma clang diagnostic pop
};

#endif //METRONOMEPLUS_METRONOME_H