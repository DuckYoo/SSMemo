#include "app_ssm_duck_duckapp_CropActivity.h"
#include "app_ssm_duck_duckapp_CropActivity_cropView.h"
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


    //Otsu modified!
    //referenced by www.DanDiggins.co.uk
    /*
    for (y = 0; y < ginfo->height; y++) {
        for (x = 0; x < ginfo->width; x++) {
            hist[*(gdata + x + y * ginfo->width)]++; //히스토그램 값 구하고
        }
    }

    for(i=1;i<=255;i++){
        H[i-1]=hist[i-1]/(float)size;
    }

    for(i=1;i<=255;i++){
        uT += (i*H[i-1]);
    }

    for(i=1;i<255;i++){
        w+=H[i-1];
        u+=(i*H[i-1]);
        work1 = (uT*w-u);
        work2 = (work1*work1)/(w*(1.0f-w));
        if(work2>work3)work3 = work2;
    }

    int threshold = (int)work3;
    LOGE("%d",threshold);

    for(y=0;y<ginfo->height;y++){
        for(x=0;x<ginfo->width;x++){
            if(*(gdata + x + y * ginfo->width) <= threshold)
                *(tdata + x + y * tinfo->width) = 0;
            else
                *(tdata + x + y * tinfo->width)= 255;
        }
    }
*/
/******************************************************** //Otsu Algorithm
    for (y = 0; y < ginfo->height; y++) {
        for (x = 0; x < ginfo->width; x++) {
            hist[*(gdata + x + y * ginfo->width)]++; //히스토그램 값 구하고
        }
    }

    for(i=1;i<=255;i++){
        if(i==1){
            H[i - 1] = hist[i - 1];
            iH[i-1] = i*hist[i-1];
        }
        else {
            H[i - 1] = H[i-2] + hist[i - 1];
            iH[i - 1] = iH[i-2] + i*hist[i - 1];
        }
    }

    for(i=1;i<255;i++){
        w = H[i-1]/(float)size; //weight
        m = iH[i-1]/H[i-1]; //mean value
        fm = (iH[255]-iH[i-1])/(H[255]-H[i-1]);
       // work1 = (hist[i-1] - m)*(hist[i-1] - m);
       // work2 += work1*hist[i-1]/H[i-1];

        //LOGI("H[i-1]: %lf",H[i-1]);
        //LOGI("iH[i-1]: %lf",iH[i-1]);
        //LOGI("w: %lf",w);
        //LOGI("m: %lf",m);

        vari=w*(1-w)*m*fm;
        if(vari > mvari){
            mvari = vari;
            tvari = i-1;
        }
    }
    int threshold = tvari - 10;

    LOGI("threshold value is %d",threshold);

    for(y=0;y<ginfo->height;y++){
        for(x=0;x<ginfo->width;x++){
            if(*(gdata + x + y * tinfo->width) < threshold)
                *(tdata + x + y * tinfo->width) = 0;
            else
                *(tdata + x + y * tinfo->width)= 255;
        }
    }
*********************************************************************///End of Otsu!

//*********************************************************This is for median:Cas1,Case2
    for (y = 0; y < ginfo->height - 3; y++) {
        for (x = 0; x < ginfo->width - 3; x++) {
            min = 255; max = 0;
            sum = 0;
            idx = 0;
            if (y < 3 || y >= (ginfo->height - 3)) {
                *(tdata + x + y * tinfo->width) = 0;
            } else if (x < 3 || x >= (ginfo->width - 3)) {
                *(tdata + x + y * tinfo->width) = 0;
            } else {
                //********************medianCase1
                for (i = -3; i <= 3; i++) {
                    for (j = -3; j <= 3; j++) {
                        int tmp = *(gdata + x + i + (y + j) * (ginfo->stride));
                        if (tmp < min)
                            min = tmp;
                        else if (tmp > max)
                            max = tmp;
                    }
                }
                if((min==max) && (min == 0))
                    *(tdata + x + y * tinfo->width) = 0;
                if (*(gdata + x + y * ginfo->width) < (min + max + 10)/2 )
                    *(tdata + x + y * tinfo->width) = 0;
                else
                    *(tdata + x + y * tinfo->width) = 255;
                //*///end of Case1

    /***************************this is for loacl thresholding median filter: Case2
    for (i = -3; i <= 3; i++) {
        for (j = -3; j <= 3; j++) {
            sum += *(gdata + x + i + (y + j) * (ginfo->stride));
        }
    }

sum -= 130;
if (*(gdata + x + y * ginfo->width) < (sum / 49))
    *(tdata + x + y * ginfo->width) = 255;
else
    *(tdata + x + y * ginfo->width) = 0;
***********************************************///end of Case2

    /*this is for mean filter. but it takes too long time.
    for(i=-3;i<=3;i++){
        for(j=-3;j<=3;j++){
            pvalue[idx++] = *(gdata + x + y * ginfo->width);
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
    if(pvalue[49/2] < *(gdata + x + y * ginfo->width))
        *(tdata + x + y * ginfo->width) = 255;
    else
        *(tdata + x + y * ginfo->width) = 0;*/


           }
      }
     }

/* global threshloding
    for (y = 0; y < ginfo->height -1; y++) {
        for (x = 0; x < ginfo->width - 1; x++) {
            if(*(gdata+x+y*ginfo->width) >= 150)
                *(tdata+x+y*tinfo->width) = 255;
            else *(tdata+x+y*tinfo->width) = 0;
        }
    }
*/
}

void morphology(AndroidBitmapInfo* tinfo,AndroidBitmapInfo* minfo, void* tpixels, void* mpixels){
    //current values
    //AndroidBitmapInfo* tinfo
    //AndroidBitmapInfo* minfo
    //void* tixels
    //void* mpixels


    uint8_t *tdata;
    uint8_t *mdata;
    tdata = (uint8_t *)tpixels;
    mdata = (uint8_t *)mpixels;
    int x, y;
    int Em[3][3]; //Erosion mask
    int Dm[3][3]; // Dilation mask
    int Msk[3][3]; //

    Msk[0][0]=255; Msk[0][1]=0; Msk[0][2]=255;
    Msk[1][0]=0; Msk[1][1]=0; Msk[1][2]=0;
    Msk[2][0]=255; Msk[2][1]=0; Msk[2][2]=255;

    //close operation
    //1.Erosion
    LOGI("Mopoly....");


    int i,j;
    bool flag= true;
    for(y=0; y<tinfo->height-1; y++){
        for(x=0; x<tinfo->width-1; x++){

            if(y==0 || y==tinfo->height-1){
                *(mdata + x + y*(minfo->stride)) = *(tdata + x + y*(tinfo->stride)); //테두리는 값을 그대로 저장
            }else if(x==0 || x==(tinfo->width-1)){
                *(mdata + x + y*(minfo->stride)) = *(tdata + x + y*(tinfo->stride));
            }else{
                for(i=-1;i<=1;i++){
                    for(j=-1; j<=1; j++){
                        if( (Msk[i][j] == 0) && (*(tdata+x+i + (y+j)*(tinfo->stride)) != Msk[i+1][j+1])) { //마스크와 1의 값이 모두 같
                            flag = false;
                        }
                    }
                }
                if(flag == true) {
                    //*(mdata + x + y*(minfo->stride)) = *(tdata + x + y * (tinfo->stride));
                    *(mdata + x + y * (minfo->stride)) = 255;
                }else {
                    *(mdata + x + y * (minfo->stride)) = *(tdata + x + y * (tinfo->stride));
                    //*(mdata + x + y * (minfo->stride)) = 255;
                }
                flag = true;
            }

        }
    }
    //2.Dilation

    //만약 모든 픽셀값들이 마스크와 일치하면
    //결과값으로 1을 갖고있고
    //아니면 0의 값을 반환
}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_CropActivity_convertImage(JNIEnv *env, jobject obj, jobject bitmap, jobject graybitmap, jobject tbitmap,jobject mbitmap) {


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
    morphology(&tinfo,&minfo,tpixels,mpixels);

    AndroidBitmap_unlockPixels(env,bitmap);
    AndroidBitmap_unlockPixels(env,graybitmap);
    AndroidBitmap_unlockPixels(env,tbitmap);
    AndroidBitmap_unlockPixels(env,mbitmap);
}