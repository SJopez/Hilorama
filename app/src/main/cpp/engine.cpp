#include<iostream>
#include "engine.h"
#include<string>
#include <android/log.h>
#include<vector>
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

void addPixel(int x, int y){

}

float Bresinham(int x0, int y0, int x1, int y1, int width, float* image){
    bool swapAxis = abs(y0 - y1) > abs(x0 - x1);

    if (swapAxis){
        swap(x0, y0);
        swap(x1, y1);
    }
    if (x0 > x1){
        swap(x0, x1);
        swap(y0, y1);
    }

    float sum = 0;
    int dx = x1 - x0;
    int dy = abs(y1 - y0);
    int dir = (y1 - y0 > 0) ? 1 : -1;

    int index;
    int y = y0;
    int p = 2 * dy - dx;
    for (int i = 0; i <= dx; i++){
        index = swapAxis ? (x0 + i) * width + y : y * width + (x0 + i);
        sum += image[index];

        if (p >= 0){
            y += dir;
            p -= 2 * dx;
        }
        p += 2 * dy;
    }

    return sum / float(dx + 1);
}

void HiloramaMain::drawImage(float* image, int imageLen, float* nails, int nailsLen) {

}