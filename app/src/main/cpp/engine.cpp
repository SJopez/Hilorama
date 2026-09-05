#include<iostream>
#include "engine.h"
#include<string>
#include <android/log.h>
#include<vector>
#include<atomic>
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


float Bresenham(int x0, int y0, int x1, int y1, int width, float* image, bool nailPicked, int channel){
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
    for (int i = 0; i <= dx; i++) {
        index = swapAxis ? (x0 + i) * width + y : y * width + (x0 + i);
        if (nailPicked) { image[index] -= 0.18; }
        else { sum += image[index]; }

        if (p >= 0) {
            y += dir;
            p -= 2 * dx;
        }
        p += 2 * dy;
    }

    return sum / float(dx + 1);
}

const int CHANNELS = 4;
atomic<bool> stop = false;
atomic<int> currNailSave[CHANNELS] = {0, 0, 0, 0};
atomic<int> prevNailSave[CHANNELS] = {-1, -1, -1, -1};
atomic<int> currLineSave[CHANNELS] = {0, 0, 0, 0};
atomic<int> generation = 0;

void saveStatus(int channel, int currNail, int prevNail, int currLine){
    currNailSave[channel].store(currNail);
    prevNailSave[channel].store(prevNail);
    currLineSave[channel].store(currLine);
}

void HiloramaMain::changeStatus(bool value){
    stop.store(value);

}

void HiloramaMain::reset(){
    generation.fetch_add(1);

    for (int c = 0; c < CHANNELS; c++){
        currNailSave[c].store(0);
        prevNailSave[c].store(-1);
        currLineSave[c].store(0);
    }
}

void HiloramaMain::drawImage(int channel, int count, float *image, vector<pair<int, int>> nails, int width,
                             function<void(int, int)> addThread) {
    int currNail = currNailSave[channel];
    int prevNail = prevNailSave[channel];
    int minDis = 30;
    int totalNail = int(nails.size());
    int currGen = generation.load();

    for (int currLines = currLineSave[channel]; currLines < count; currLines++){
        if (stop.load()) {
            if (generation.load() == currGen){
                saveStatus(channel, currNail, prevNail, currLines);
            }
            return;
        }

        int x0 = nails[currNail].first;
        int y0 = nails[currNail].second;
        int nailToPick = currNail;
        float average = -1e9+7;

        for (int j = 0; j < nails.size(); j++){
            if (stop.load()) {
                if (generation.load() == currGen){
                    saveStatus(channel, currNail, prevNail, currLines);
                }
                return;
            }

            int diff = abs(currNail - j);
            int dist = min(diff, totalNail - diff);

            if (dist <= minDis || j == prevNail || j == currNail) continue;

            int x1 = nails[j].first;
            int y1 = nails[j].second;
            float currAverage = Bresenham(x0, y0, x1, y1, width, image, false, channel);

            if (currAverage > average){
                average = currAverage;
                nailToPick = j;
            }
        }
        int x1 = nails[nailToPick].first;
        int y1 = nails[nailToPick].second;
        Bresenham(x0, y0, x1, y1, width, image, true, channel);
        addThread(currNail, nailToPick);
        prevNail = currNail;
        currNail = nailToPick;
    }

}

