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
constexpr char kNormalBeat[] {"a_m_wood_light_tip.wav" } ;
constexpr char kSilenceBeat[] { } ;
constexpr char kAccentBeat[] {"a_m_wet_wood_vinyl_hit_to.wav" } ;
constexpr char kMediumBeat[] {"a_m_wood_hit_pi.wav" } ;

#endif //METRONOMEPLUS_CONSTANTS_H
