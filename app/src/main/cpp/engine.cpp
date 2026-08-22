#include<iostream>
#include "engine.h"
#include<string>
#include <android/log.h>
#define LOG_TAG "StringArt"

#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


using namespace  std;

string debugArray(float* array, int len){
    string ans = "[ ";

    for (int i = 0; i < len; i++){
        ans += to_string(array[i]);
        ans += ", ";
    }

    ans.append(" ]");

    return ans;
}

void HiloramaMain::drawImage(float *image, int imageLen, float *nails, int nailsLen) {
    string debug = debugArray(image, imageLen);
    LOG("%s", debug.c_str());
}