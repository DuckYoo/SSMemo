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

void convertB(AndroidBitmapInfo* info, AndroidBitmapInfo* binfo, void* pixels, void* bpixels,int num){

    LOGI("Convert to binary....");

    uint8_t *data;
    uint8_t *bdata;
    float hist[260] = {0,}; //histogram info
    float H[260] = {0,}; //sum of histo
    float iH[260] = {0,};
    float work1 = 0, work2 = 0;
    double work3 = 0.0, vari = 0.0, mvari = 0.0;
    float uT = 0, w = 0, u = 0, m = 0, fm = 0;
    int size = ((info->height) * (info->width));
    int sum = 0, idx = 0;
    int tvari = 0;
    int min = 255, max = 0;
    int pvalue[64];
    int x, y;
    int i, j;

    //Local Thresholding Test1 = mask 3x3
    LOGI("Local Thresholding....");

    data = (uint8_t *) pixels;
    bdata = (uint8_t *) bpixels;

    if(num == 1) {
        for (y = 0; y < info->height - 3; y++) {
            for (x = 0; x < info->width - 3; x++) {
                sum = 0;
                idx = 0;
                if (y < 3 || y >= (info->height - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else if (x < 3 || x >= (info->width - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else {
                    for (i = -3; i <= 3; i++) {
                        for (j = -3; j <= 3; j++) {
                            int tmp = *(data + x + i + (y + j) * (info->stride));
                            if (tmp < min)
                                min = tmp;
                            else if (tmp > max)
                                max = tmp;
                        }
                    }

                    if (*(data + x + y * info->width) < (min + max + 14) / 2)
                        *(bdata + x + y * binfo->width) = 0;
                    else
                        *(bdata + x + y * binfo->width) = 255;
                }
            }
        }

    }else if(num == 2){
        for (y = 0; y < info->height - 3; y++) {
            for (x = 0; x < info->width - 3; x++) {
                sum = 0;
                idx = 0;
                if (y < 3 || y >= (info->height - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else if (x < 3 || x >= (info->width - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else {
                    for (i = -3; i <= 3; i++) {
                        for (j = -3; j <= 3; j++) {
                            sum += *(data + x + i + (y + j) * (info->stride));
                        }
                    }

                    sum -= 130;
                    if (*(data + x + y * info->width) < (sum / 49))
                        *(bdata + x + y * binfo->width) = 255;
                    else
                        *(bdata + x + y * binfo->width) = 0;
                }
            }
        } // end of Case2

    }else if(num == 3){

        for (y = 0; y < info->height - 3; y++) {
            for (x = 0; x < info->width - 3; x++) {
                sum = 0;
                idx = 0;
                if (y < 3 || y >= (info->height - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else if (x < 3 || x >= (info->width - 3)) {
                    *(bdata + x + y * binfo->width) = 0;
                } else {
                    for(i=-3;i<=3;i++){
                        for(j=-3;j<=3;j++){
                            pvalue[idx++] = *(data + x + y * info->width);
                        }
                    }
                    for(i=0; i<48; i++){
                        for(j=i; j<48; j++){
                            if(pvalue[j] > pvalue[j+1]){
                                int tmp = pvalue[j];
                                pvalue[j] = pvalue[j+1];
                                pvalue[j+1] = tmp;
                            }
                        }
                    }
                    if(pvalue[49/2] < *(data + x + y * info->width))
                        *(bdata + x + y * binfo->width) = 255;
                    else
                        *(bdata + x + y * binfo->width) = 0;
                }
            }
        }


    }else { //Otsu
        for (y = 0; y < info->height; y++) {
            for (x = 0; x < info->width; x++) {
                hist[*(data + x + y * info->width)]++; //히스토그램 값 구하고
            }
        }

        for (i = 1; i <= 255; i++) {
            if (i == 1) {
                H[i - 1] = hist[i - 1];
                iH[i - 1] = i * hist[i - 1];
            }
            else {
                H[i - 1] = H[i - 2] + hist[i - 1];
                iH[i - 1] = iH[i - 2] + i * hist[i - 1];
            }
        }
        for (i = 1; i < 255; i++) {
            w = H[i - 1] / (float) size; //weight
            m = iH[i - 1] / H[i - 1]; //mean value
            fm = (iH[255] - iH[i - 1]) / (H[255] - H[i - 1]);
            vari = w * (1 - w) * m * fm;
            if (vari > mvari) {
                mvari = vari;
                tvari = i - 1;
            }
        }
        int threshold = tvari - 10;

        LOGI("threshold value is %d", threshold);

        for (y = 0; y < info->height; y++) {
            for (x = 0; x < info->width; x++) {
                if (*(data + x + y * info->width) < threshold)
                    *(bdata + x + y * binfo->width) = 0;
                else
                    *(bdata + x + y * binfo->width) = 255;
            }
        }
    } // end of Otsu
}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_MainActivity_convertToBin(JNIEnv *env, jobject obj, jobject original, jobject convertedimg) {
    AndroidBitmapInfo info;
    AndroidBitmapInfo binfo;
    void* pixels;
    void* bpixels;
    int num =1;

    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, original,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }
    if(0>AndroidBitmap_getInfo(env,convertedimg,&binfo)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("imagesize(%d,%d)\n",info.width,info.height);

    if(info.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not BITMAP_A_8:%d\n",info.format);
        //return;
    }
    if(binfo.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not BITMAP_A_8:%d\n",binfo.format);
        return;
    }

    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,original,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }
    if(0> AndroidBitmap_lockPixels(env,convertedimg,&bpixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    convertB(&info,&binfo,pixels,bpixels,num);

    AndroidBitmap_unlockPixels(env,original);
    AndroidBitmap_unlockPixels(env,convertedimg);
}