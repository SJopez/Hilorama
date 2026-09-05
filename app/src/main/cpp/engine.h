#pragma once
#include<iostream>

using namespace std;

class HiloramaMain {
public:
    void drawImage(int channel, int count, float* image, vector<pair<int, int>> nails, int width, function<void(int, int)> addThread);
    void changeStatus(bool value);
    void reset();
};
