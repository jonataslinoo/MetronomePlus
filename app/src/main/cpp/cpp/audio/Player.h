#ifndef METRONOMEPLUS_SOUNDRECORDING_H
#define METRONOMEPLUS_SOUNDRECORDING_H

#include <cstdint>
#include <array>

#include <chrono>
#include <memory>
#include <atomic>

#include <android/asset_manager.h>

#include "DataSource.h"
#include "IRenderableAudio.h"

class Player : public IRenderableAudio{

public:
    /**
     * Construct a new Player from the given DataSource. Players can share the same data source.
     * For example, you could play two identical sounds concurrently by creating 2 Players with the
     * same data source.
     *
     * @param source
     */
    Player(std::shared_ptr<DataSource> source)
        : mSource(source)
    {};

    void renderAudio(float *targetData, int32_t numFrames);
    void resetPlayHead() { mReadFrameIndex = 0; };
    void setPlaying(bool isPlaying) { mIsPlaying = isPlaying; resetPlayHead(); };
    void setLooping(bool isLooping) { mIsLooping = isLooping; };

private:
    int32_t mReadFrameIndex = 0;
    std::atomic<bool> mIsPlaying { false };
    std::atomic<bool> mIsLooping { false };
    std::shared_ptr<DataSource> mSource;

    void renderSilence(float*, int32_t);
};

#endif //METRONOMEPLUS_SOUNDRECORDING_H
