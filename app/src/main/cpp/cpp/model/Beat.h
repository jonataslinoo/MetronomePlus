#ifndef METRONOMEPLUS_BEAT_H
#define METRONOMEPLUS_BEAT_H

enum BeatState {
    Normal,
    Silence,
    Accent,
    Medium
};

struct Beat {
    BeatState state;
};

#endif //METRONOMEPLUS_BEAT_H
