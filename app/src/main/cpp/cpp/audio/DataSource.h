#ifndef METRONOMEPLUS_AUDIOSOURCE_H
#define METRONOMEPLUS_AUDIOSOURCE_H

#include <cstdint>
#include "../utils/Constants.h"

class DataSource {
public:
    virtual ~DataSource(){};
    virtual int64_t getSize() const = 0;
    virtual AudioProperties getProperties() const  = 0;
    virtual const float* getData() const = 0;
};

#endif //METRONOMEPLUS_AUDIOSOURCE_H
