#ifndef METRONOMEPLUS_IRENDERABLEAUDIO_H
#define METRONOMEPLUS_IRENDERABLEAUDIO_H

#include <cstdint>
#include <string>

class IRenderableAudio {

public:
    virtual ~IRenderableAudio() = default;
    virtual void renderAudio(float *audioData, int32_t numFrames) = 0;
};

#endif //METRONOMEPLUS_IRENDERABLEAUDIO_H
