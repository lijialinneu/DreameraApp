//
// Created by 10405 on 2016/7/5.
//

#include "neu_dreamerajni_utils_OpenCVSmooth.h"
#include <stdio.h>
#include <stdlib.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui.hpp>

using namespace cv;

extern  "C" {

JNIEXPORT jintArray JNICALL Java_neu_dreamerajni_utils_OpenCVSmooth_smooth
        (JNIEnv *env, jclass obj, jintArray buf, int w, int h);

JNIEXPORT jintArray JNICALL Java_neu_dreamerajni_utils_OpenCVSmooth_smooth
        (JNIEnv *env, jclass obj, jintArray buf, int w, int h) {

    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, false);
    if (cbuf == NULL) {
        return 0;
    }

    return NULL;

}
}




