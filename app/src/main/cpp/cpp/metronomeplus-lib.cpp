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
Java_br_com_jonatas_metronomeplus_presenter_ui_MainActivity_helloC(JNIEnv *env, jobject instance) {
    return (*env).NewStringUTF("Hello C/C++");
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1onInit(JNIEnv *env, jobject instance,
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
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1onEnd(JNIEnv *env, jobject instance) {

    metronome->end();
}

//***********************************************

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1SetBPM(JNIEnv *env, jobject instance,
                                                                                 jint bpm) {
    if (metronome) {
        metronome->setBPM(bpm); // Atualiza o BPM e o intervalo
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1SetBeats(JNIEnv *env, jobject instance,
                                                                                   jobjectArray jBeats) {

    if (!metronome) {
        LOGE("Game não inicializado");
        return;
    }

    LOGI("Iniciando configuração de notas...");
    std::vector<Beat> beats;

    jsize beatsCount = env->GetArrayLength(jBeats);

    for (jsize i = 0; i < beatsCount; ++i) {
        jobject jBeat = env->GetObjectArrayElement(jBeats, i);
        if (jBeat == nullptr) continue; // Ignora objetos nulos

        // Obtenha a classe Beat
        jclass beatClass = env->GetObjectClass(jBeat);
        if (beatClass == nullptr) continue;

        // Obtenha o campo `state`
        jfieldID stateField = env->GetFieldID(beatClass, "state",
                                              "Lbr/com/jonatas/metronomeplus/domain/model/BeatState;");
        jobject jState = env->GetObjectField(jBeat, stateField);
        if (jState == nullptr) continue;

        // Obtenha a classe BeatState
        jclass beatStateClass = env->GetObjectClass(jState);
        if (beatStateClass == nullptr) continue;

        // Converta o estado para um enum inteiro
        jmethodID ordinalMethod = env->GetMethodID(beatStateClass, "ordinal", "()I");
        jint stateOrdinal = env->CallIntMethod(jState, ordinalMethod);

        beats.push_back({static_cast<BeatState>(stateOrdinal)});

        // Libere referências locais
        env->DeleteLocalRef(jBeat);
        env->DeleteLocalRef(jState);
        env->DeleteLocalRef(beatClass);
        env->DeleteLocalRef(beatStateClass);
    }

    metronome->setBeats(beats);
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1onStartPlaying(JNIEnv *env,
                                                                                         jobject instance) {
    if (metronome) {
        metronome->startPlaying();
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1onStopPlaying(JNIEnv *env,
                                                                                        jobject instance) {
    if (metronome) {
        metronome->stopPlaying();
    }
}

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_MetronomeEngineImpl_native_1setDefaultStreamValues(JNIEnv *env,
                                                                                                 jobject instance,
                                                                                                 jint sampleRate,
                                                                                                 jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

JavaVM* gJvm = nullptr;
jobject gListener = nullptr;

JNIEXPORT void JNICALL
Java_br_com_jonatas_metronomeplus_data_engine_NativeMetronomeEngine_native_1setListener(JNIEnv *env, jobject thiz,
                                                                     jobject listener) {
    env->GetJavaVM(&gJvm);
    gListener = env->NewGlobalRef(listener);
}

void notifyUiChangeBeat(int beatIndex) {
    if (!gJvm || !gListener) return;

    JNIEnv *env;
    bool didAttach = false;

    if (gJvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        gJvm->AttachCurrentThread(&env, nullptr);
        didAttach = true;
    }

    jclass listenerClass = env->GetObjectClass(gListener);
    jmethodID methodId = env->GetMethodID(listenerClass, "onBeat", "(I)V");
    if (methodId) {
        env->CallVoidMethod(gListener, methodId, beatIndex);
    }

    if (didAttach) {
        gJvm->DetachCurrentThread();
    }
}

}