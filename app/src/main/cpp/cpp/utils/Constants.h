#ifndef METRONOMEPLUS_CONSTANTS_H
#define METRONOMEPLUS_CONSTANTS_H

#include <cstdint>

struct AudioProperties {
    int32_t channelCount;
    int32_t sampleRate;
};

constexpr int32_t kSampleRate = 48000;
constexpr int kChannelCount = 2;

//Beats
constexpr char kNormalBeat[] {"beat_4.wav" } ;
constexpr char kSilenceBeat[] { } ;
constexpr char kAccentBeat[] {"beat_1.wav" } ;
constexpr char kMediumBeat[] {"beat_3.wav" } ;

#endif //METRONOMEPLUS_CONSTANTS_H
