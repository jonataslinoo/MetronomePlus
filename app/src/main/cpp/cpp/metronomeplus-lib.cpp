// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("metronomeplus-lib");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("metronomeplus-lib")
//      }
//    }

#include <cstring>
#include <jni.h>
#include <android/asset_manager_jni.h>
#include "utils/Logging.h"
#include "Metronome.h"

extern "C" {

std::unique_ptr<Metronome> metronome;

JNIEXPORT jstring JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_helloC(JNIEnv *env, jobject instance) {
    return (*env).NewStringUTF("Hello C/C++");
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1onInit(JNIEnv *env, jobject instance,
                                                                 jobject asset_manager) {

    AAssetManager *assetManager = AAssetManager_fromJava(env, asset_manager);
    if (assetManager == nullptr) {
        LOGE("Could not obtain the AAssetManager");
        return;
    }

    metronome = std::make_unique<Metronome>(*assetManager);
    metronome->init();
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1onEnd(JNIEnv *env, jobject instance) {

    metronome->end();
}

//***********************************************

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1SetBPM(JNIEnv *env, jobject instance, jint bpm) {
    if (metronome) {
        metronome->setBPM(bpm); // Atualiza o BPM e o intervalo
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1SetTones(JNIEnv *env, jobject instance, jobjectArray jTones) {

    if (!metronome) {
        LOGE("Game não inicializado");
        return;
    }

    LOGI("Iniciando configuração de notas...");
    std::vector<Tone> tones;

    jsize tonesCount = env->GetArrayLength(jTones);

    for (jsize i = 0; i < tonesCount; ++i) {
        jobject jTone = env->GetObjectArrayElement(jTones, i);
        if (jTone == nullptr) continue; // Ignora objetos nulos

        // Obtenha a classe Tone
        jclass toneClass = env->GetObjectClass(jTone);
        if (toneClass == nullptr) continue;

        // Obtenha o campo `state`
        jfieldID stateField = env->GetFieldID(toneClass, "state", "Lbr/com/jonatas/metronomeplus/model/ToneState;");
        jobject jState = env->GetObjectField(jTone, stateField);
        if (jState == nullptr) continue;

        // Obtenha a classe ToneState
        jclass toneStateClass = env->GetObjectClass(jState);
        if (toneStateClass == nullptr) continue;

        // Converta o estado para um enum inteiro
        jmethodID ordinalMethod = env->GetMethodID(toneStateClass, "ordinal", "()I");
        jint stateOrdinal = env->CallIntMethod(jState, ordinalMethod);

        tones.push_back({static_cast<ToneState>(stateOrdinal)});

        // Libere referências locais
        env->DeleteLocalRef(jTone);
        env->DeleteLocalRef(jState);
        env->DeleteLocalRef(toneClass);
        env->DeleteLocalRef(toneStateClass);
    }

    metronome->setTones(tones);
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1onStartPlaying(JNIEnv *env, jobject instance) {
    if (metronome) {
        metronome->startPlaying();
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1onStopPlaying(JNIEnv *env, jobject instance) {
    if (metronome) {
        metronome->stopPlaying();
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_ui_MainActivity_native_1setDefaultStreamValues(JNIEnv *env,
                                                                                    jobject instance,
                                                                                    jint sampleRate,
                                                                                    jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}
}