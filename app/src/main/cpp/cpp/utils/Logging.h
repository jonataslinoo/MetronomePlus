#ifndef METRONOMEPLUS_LOGGING_H
#define METRONOMEPLUS_LOGGING_H

#include <cstdio>
#include <android/log.h>

#define APP_NAME "MetronomePlus-C"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, APP_NAME, __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, APP_NAME, __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, APP_NAME, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, APP_NAME, __VA_ARGS__))

#endif //METRONOMEPLUS_LOGGING_H
