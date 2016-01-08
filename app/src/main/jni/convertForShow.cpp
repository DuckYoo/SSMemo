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

void convert(AndroidBitmapInfo* info, AndroidBitmapInfo* rinfo, void* pixels, void* rpixels){
    //info, rinfo, pixels, rpixles
    int x,y;
    uint8_t *grayline;
    argb *showline;

    LOGI("init Converting For Saving");

    for(y=0; y<info->height; y++){
        grayline = (uint8_t *)pixels;
        showline = (argb *)rpixels;
        for(x=0; x<info->width; x++){
            showline[x].alpha = (255 - grayline[x]);
            showline[x].red = (255 - grayline[x]);
            showline[x].green = (255 - grayline[x]);
            showline[x].blue = (255 - grayline[x]);
        }
        pixels = (char *)pixels + info->stride;
        rpixels = (char *)rpixels + rinfo->stride;
    }
}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_CropActivity_convertForShow(JNIEnv *env, jobject obj, jobject bitmap,jobject rbitmap) {
    AndroidBitmapInfo info;
    AndroidBitmapInfo rinfo;
    void* pixels;
    void* rpixels;

    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, bitmap,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env, rbitmap, &rinfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("convert imagesize(%d,%d)\n",info.width,info.height);

    if(info.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not RGB_A_8:%d\n",info.format);
        //return;
    }
    if(rinfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888){
        LOGE("This Bitmap format is not RGB_8888:%d\n",rinfo.format);
        //return;
    }

    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,bitmap,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,rbitmap,&rpixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    //함수
    convert(&info,&rinfo,pixels,rpixels);

    AndroidBitmap_unlockPixels(env,bitmap);
    AndroidBitmap_unlockPixels(env,rbitmap);

}
