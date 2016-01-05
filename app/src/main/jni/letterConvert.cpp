#include "app_ssm_duck_duckapp_MainActivity.h"
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

void getPixelInfo(AndroidBitmapInfo* binfo,AndroidBitmapInfo* ainfo, void* bpixels, void* apixels) {

    int x, y;

    //흑화소의 위치 검출

}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_MainActivity_seperateLetter(JNIEnv *env, jobject obj, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;

    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, bitmap,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("imagesize(%d,%d)\n",info.width,info.height);

    if(info.format != ANDROID_BITMAP_FORMAT_RGB_565){
        LOGE("Bitmap format is not RGB_565:%d\n",info.format);
        //return;
    }

    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,bitmap,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    //함수

    AndroidBitmap_unlockPixels(env,bitmap);

}
