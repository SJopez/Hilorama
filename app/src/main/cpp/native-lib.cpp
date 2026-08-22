#include<jni.h>
#include<iostream>
#include "engine.h"

using namespace std;

static HiloramaMain engine;

extern "C" [[maybe_unused]] JNIEXPORT void JNICALL
Java_com_example_hilorama_HiloramaEngine_drawImage (
        JNIEnv* env, jobject thiz, jfloatArray image, jfloatArray nails){

    jfloat* castedImage = env->GetFloatArrayElements(image, nullptr);
    jsize imageSize = env->GetArrayLength(image);

    jfloat* castedNails = env->GetFloatArrayElements(nails, nullptr);
    jsize nailsSize = env->GetArrayLength(nails);

    engine.drawImage(castedImage, imageSize, castedNails, nailsSize);
}