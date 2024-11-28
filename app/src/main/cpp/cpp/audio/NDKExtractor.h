#ifndef METRONOMEPLUS_NDKMEDIAEXTRACTOR_H
#define METRONOMEPLUS_NDKMEDIAEXTRACTOR_H


#include <cstdint>
#include <android/asset_manager.h>
#include "../utils/Constants.h"


class NDKExtractor {

public:
    static int32_t decode(AAsset *asset, uint8_t *targetData, AudioProperties targetProperties);
};


#endif //METRONOMEPLUS_NDKMEDIAEXTRACTOR_H
