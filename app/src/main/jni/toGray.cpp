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

void convertG(AndroidBitmapInfo* info, AndroidBitmapInfo* ginfo, void* pixels, void* gpixels){
    int x, y;

    LOGI("Converting to grayscale....");

    for(y=0; y<info->height; y++){
        argb* line = (argb *)pixels;
        uint8_t* grayline = (uint8_t*)gpixels;
        for(x=0; x<info->width; x++){
            grayline[x] = 255 - ( 0.3 * line[x].red + 0.59 * line[x].green + 0.11*line[x].blue);
        }
        pixels = (char *)pixels + info->stride;
        gpixels = (char *)gpixels + ginfo->stride;
    }
    LOGI("Converting to grayscale is finished!");
}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_CropActivity_convertToGray(JNIEnv *env, jobject obj, jobject original, jobject convertedimg) {
    AndroidBitmapInfo info;
    AndroidBitmapInfo ginfo;
    void* pixels;
    void* gpixels;

    LOGE("HaHaHa");

    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, original,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env,convertedimg,&ginfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("imagesize(%d,%d)\n",info.width,info.height);

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888){
        LOGE("Bitmap format is not RGBA_8888:%d\n",info.format);
        //return;
    }
    if(ginfo.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not BITMAP_A_8:%d\n",ginfo.format);
        return;
    }

    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,original,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,convertedimg,&gpixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    convertG(&info,&ginfo,pixels,gpixels);

    AndroidBitmap_unlockPixels(env,original);
    AndroidBitmap_unlockPixels(env,convertedimg);
}