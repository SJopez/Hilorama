#include<jni.h>
#include<iostream>
#include <android/log.h>
#include "engine.h"
#define LOG_TAG "StringArt"

#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


using namespace std;

static HiloramaMain engine;

vector<pair<int, int>> getNails(JNIEnv* env, jfloatArray nails){
    jsize nailsSize = env->GetArrayLength(nails);
    jfloat* capNails = env->GetFloatArrayElements(nails, nullptr);

    vector<pair<int, int>> castedNails;
    castedNails.reserve(nailsSize);

    for (int i = 0; i < nailsSize / 2; i++){
        float x = capNails[2 * i];
        float y = capNails[2 * i + 1];
        int castedX = static_cast<int>(lround(x));
        int castedY = static_cast<int>(lround(y));

        castedNails.emplace_back(castedX, castedY);
    }

    env->ReleaseFloatArrayElements(nails, capNails, JNI_ABORT);

    return castedNails;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_hilorama_HiloramaEngine_drawImage(
        JNIEnv* env, jobject thiz, jint channel, jint count, jfloatArray image, jfloatArray nails, jint width, jobject threadAdding){

    jfloat* capImage = env->GetFloatArrayElements(image, nullptr);
    jsize imageSize = env->GetArrayLength(image);

    vector<pair<int, int>> castedNails = getNails(env, nails);

    jclass addingClass = env->GetObjectClass(threadAdding);
    jmethodID addingMethod = env->GetMethodID(addingClass, "addThread", "(II)V");

    engine.drawImage(channel, count, capImage, castedNails, width,
                                              [env, addingMethod, threadAdding](int nail0, int nail1){
                                                              env->CallVoidMethod(threadAdding, addingMethod, nail0, nail1);
                                                         });

    env->ReleaseFloatArrayElements(image, capImage, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_hilorama_HiloramaEngine_changeStatus (
        JNIEnv* env, jobject thiz, jboolean value){
    engine.changeStatus(value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_hilorama_HiloramaEngine_reset (
        JNIEnv* env, jobject thiz){
    engine.reset();
}