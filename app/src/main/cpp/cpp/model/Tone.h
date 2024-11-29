#ifndef METRONOMEPLUS_TONE_H
#define METRONOMEPLUS_TONE_H

enum ToneState {
    Normal,
    Silence,
    Accent,
    Medium
};

struct Tone {
    ToneState state;
};

#endif //METRONOMEPLUS_TONE_H
