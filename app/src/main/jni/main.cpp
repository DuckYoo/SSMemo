#include "app_ssm_duck_duckapp_MainActivity.h"
#include <jni.h>
#include <android/log.h>
#include "math.h"
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


void reverse(AndroidBitmapInfo* info,AndroidBitmapInfo* ginfo, void* pixels, void* gpixels){

    //current values
    //AndroidBitmapInfo* info
    //AndroidBitmapInfo* ginfo
    //void* pixels
    //void* gpixels
    int x, y;

    //convert to grayscale
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
}

void threshold(AndroidBitmapInfo* ginfo,AndroidBitmapInfo* tinfo, void* gpixels, void* tpixels) {
    uint8_t *gdata;
    uint8_t *tdata;
    float hist[260] = {0,}; //histogram info
    float H[260] = {0,}; //sum of histo
    float iH[260] = {0,};
    float work1 = 0, work2 = 0;
    double work3 = 0.0, vari = 0.0, mvari = 0.0;
    float uT = 0, w = 0, u = 0, m = 0, fm = 0;
    int size = ((ginfo->height) * (ginfo->width));
    int sum = 0, idx = 0;
    int tvari = 0;
    int min = 255, max = 0;
    int pvalue[64];
    int x, y;
    int i, j;

    //Local Thresholding Test1 = mask 3x3
    LOGI("Local Thresholding....");

    gdata = (uint8_t *) gpixels;
    tdata = (uint8_t *) tpixels;

    for (y = 0; y < ginfo->height - 3; y++) {
        for (x = 0; x < ginfo->width - 3; x++) {
            min = 255;
            max = 0;
            sum = 0;
            idx = 0;
            if (y < 3 || y >= (ginfo->height - 3)) {
                *(tdata + x + y * tinfo->width) = 0;
            } else if (x < 3 || x >= (ginfo->width - 3)) {
                *(tdata + x + y * tinfo->width) = 0;
            } else {
                for (i = -3; i <= 3; i++) {
                    for (j = -3; j <= 3; j++) {
                        int tmp = *(gdata + x + i + (y + j) * (ginfo->stride));
                        if (tmp < min)
                            min = tmp;
                        else if (tmp > max)
                            max = tmp;
                    }
                }
                if ((min == max) && (min == 0))
                    *(tdata + x + y * tinfo->width) = 0;
                if (*(gdata + x + y * ginfo->width) < (min + max + 14) / 2)
                    *(tdata + x + y * tinfo->width) = 0;
                else
                    *(tdata + x + y * tinfo->width) = 255;
            }
        }
    }
}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_MainActivity_convertImage(JNIEnv *env, jobject obj, jobject bitmap, jobject graybitmap, jobject tbitmap,jobject mbitmap) {


    AndroidBitmapInfo info;
    AndroidBitmapInfo grayinfo;
    AndroidBitmapInfo tinfo;
    AndroidBitmapInfo minfo;
    void* pixels;
    void* graypixels;
    void* tpixels;
    void* mpixels;


    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, bitmap,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env, graybitmap,&grayinfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env, tbitmap, &tinfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env, mbitmap, &minfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("imagesize(%d,%d)\n",info.width,info.height);


    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888){
        LOGE("Bitmap format is not RGBA_8888:%d\n",info.format);
        //return;
    }
    if(grayinfo.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not BITMAP_A_8:%d\n",grayinfo.format);
        //return;
    }


    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,bitmap,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,graybitmap,&graypixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,tbitmap,&tpixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,mbitmap,&mpixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    //converting...
    reverse(&info,&grayinfo,pixels,graypixels);
    threshold(&grayinfo,&tinfo,graypixels,tpixels);
    //morphology(&tinfo,&minfo,tpixels,mpixels);

    AndroidBitmap_unlockPixels(env,bitmap);
    AndroidBitmap_unlockPixels(env,graybitmap);
    AndroidBitmap_unlockPixels(env,tbitmap);
    AndroidBitmap_unlockPixels(env,mbitmap);
}