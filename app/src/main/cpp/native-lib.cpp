#include<jni.h>
#include "engine.h"

static HiloramaMain engine;

extern "C" JNIEXPORT jint JNICALL
Java_com_example_hilorama_HiloramaEngine_sumita(
        JNIEnv* env, jobject thiz, jint a, jint b){
    return engine.sumita(a, b);
}