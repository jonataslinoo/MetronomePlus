#ifndef METRONOMEPLUS_CONSTANTS_H
#define METRONOMEPLUS_CONSTANTS_H

#include <cstdint>

struct AudioProperties {
    int32_t channelCount;
    int32_t sampleRate;
};

constexpr int32_t kSampleRate = 48000;
constexpr int kChannelCount = 2;

//Tones
constexpr char kNormalTone[] { "" } ;
constexpr char kSilenceTone[] { } ;
constexpr char kAccentTone[] { "" } ;
constexpr char kMediumTone[] { "" } ;

#endif //METRONOMEPLUS_CONSTANTS_H
