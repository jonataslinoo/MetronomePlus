// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("metronomeplus");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("metronomeplus")
//      }
//    }

#include <cstring>
#include <jni.h>

extern "C" {

JNIEXPORT jstring JNICALL
Java_br_com_jonatas_metronomeplus_MainActivity_helloC(JNIEnv *env, jobject instance) {
    return (*env).NewStringUTF("Hello C/C++");
}

}