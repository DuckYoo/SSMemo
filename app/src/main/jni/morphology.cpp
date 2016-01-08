#include "app_ssm_duck_duckapp_CropActivity.h"
#include "app_ssm_duck_duckapp_CropActivity_cropView.h"
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG ("NDKTest")
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct {
    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} argb;

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_CropActivity_morphology(JNIEnv *env, jobject obj, jobject original, jobject convertedimg) {

}