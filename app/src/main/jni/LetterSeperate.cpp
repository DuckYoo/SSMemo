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

typedef struct {
    int wSX;
    int wSY;
    int wEX;
    int wEY;
    int cutY[200]; //제한?
}letterInfo;


void* pixels;
uint8_t *data;

int wSX=5000,wSY=5000,wEX=0,wEY=0; //단어의 시작, 끝 저장 변수
int Vx[4000]={0,},Vy[3000]={0,}; //흑화소의 누적값 저장 변수
int upperLine,baseLine,centerY;

void getLetterBoundary(AndroidBitmapInfo* info, int H1){
    data = (uint8_t*)pixels;
    int x,y;
    //사용 전역변수: wSX,wEX,wSY,wEY,centerY



    //사각형 크기 안에서 문자를 나누기 시작.
    for(x=wSX+1; x<wEX; x++){
        for(y=wSY+1;y<wEY;y++){

            //백화소 다음에 흑화소가 발견됬을 경우!
            if(*(data+x+(y-1)*info->width) != 255 && *(data+x+y*info->width)==255){

                //수평획의 범위안에 포함될 경우
                if(y >= centerY ){
                    //가로의 길이를 재서
                    int curLen=0;
                    while(*(data+(x+curLen)+y*info->width) == 255) {
                        curLen++;
                    } // 길이가 일정 길이 이상이어야 수평획으로 인식하고 끝에서 자름
                    if(curLen >= H1*0.6){
                        int i=0;
                        for(i=wSY+1;i<wEY;i++){
                            *(data+x+i*info->width) = 255;
                        }
                        y = wEY -1;
                        x +=curLen;
                    }
                }

                //초성이 발견됬을 경우엔
                if( y < centerY){
                    int curXLen=0, curYLen=0, curCLen=0;
                    //가로를 먼저 검사해서
                    while( *(data+(x+curXLen)+y*info->width) == 255){
                        curXLen++;
                    }// 임계값 이상의 수평획이 존재하면 초성으로 인식
                    if(curXLen >= H1*0.3){
                        int i=0;
                        for(i=wSY;i<wEY;i++){
                            *(data+x+i*info->width) = 255;
                        }
                        y = wEY -1;
                        x += (curXLen*2);
                    }else{//수평획이 임계값 미만이면 임계값 미만의 수직획을 가지고 있는지 확인하고
                         while(*(data+x+(y+curYLen)*info->width) == 255){
                            curYLen++;
                        }//그 수직획의 끝에 임계값 이상의 긴 수평획이 연결되어 있는지 확인!
                        if(curYLen<=0.3*H1){
                            int conXLen = 0;
                            while(*(data+(x+conXLen)+(y+curYLen)*info->width)==255){
                                conXLen++;
                            }//연결되어있으면 초성으로 보고 건너뛰기!
                            if(conXLen >= 0.3*H1){
                                int i=0;
                                for(i=wSY+1; i<wEY; i++){
                                    *(data+x+i*info->width) = 255;
                                }
                                y=wEY-1;
                                x+=(curXLen);
                            }
                        }
                        //사선 획이 임계값 이상이면 사선 획을 가지고 있다고 하고
                        while(*(data+(x+curXLen+curCLen)+(y+curCLen)*info->width) == 255){
                            curCLen++;
                        }
                        if(curCLen >= 0.3*H1){
                            int i=0;
                            for(i=wSY+1;i<wEY;i++){
                                *(data+x+i*info->width) = 255;
                            }
                            y=wEY-1;
                            x+=(curCLen);
                        }
                    }
                }

                //.점일경우
                // ""따온표일 경우

            }else if( *(data+x+(y-1)*info->width)==255 && *(data+x+y*info->width)==0){
                y = wEY-1;
            }else{
                continue;
            }

        }
    }

}

void drawLineRange(AndroidBitmapInfo* info, int y){
    int i,j;
    data = (uint8_t *)pixels;


    //실제로 사각형을 그리는 부분. 한 행에 흑화소가 발견되기 시작하면 그리기 시작!
    if(Vy[y-1] != 0 && Vy[y] == 0){
        int maxY=0;

        //사각형의 크기만큼 그려
        for(j=wSY;j<=wEY;j++){
            for(i=wSX;i<=wEX;i++){
                Vx[i] = 0; //초기화 해주면
                if( (j == wSY ) && (i >= wSX && i <= wEX))
                    *(data + i + j * info->width) = 250;
                if( (j == wEY ) && (i >= wSX && i <= wEX))
                    *(data + i + j * info->width) = 250;
                if( (i == wSX ) && (j >= wSY && j <= wEY))
                    *(data + i + j * info->width) = 250;
                if( (i == wEX) && (j >= wSY && j<= wEY))
                    *(data + i + j * info->width) = 250;
                if(Vy[j]>maxY) {
                    maxY = Vy[j];
                    centerY = j;
                }
            }
        }

        //centerline
        for(i=wSX;i<=wEX;i++){
            //*(data + i + centerY * info->width) = 255;
        }
        //upperline
        int std = maxY*(0.2);
        for(j=wSY; j<=wEY; j++){
            if(Vy[j] >= std){
                upperLine = j;
                for(i=wSX;i<=wEX;i++){
                    //*(data + i + j * info->width) = 255;
                }
                break;
            }
        }
        //lowerline
        for(j=wEY; j>=wSY; j--){
            if(Vy[j] >= std){
                baseLine = j;
                for(i=wSX;i<=wEX;i++){
                    //*(data + i + j * info->width) = 255;
                }
                break;
            }
        }
        //H1
        int H1 = baseLine - upperLine + wSY; //실제 H1은 baseLine - upperY .
        for(i=wSX;i<=wEX;i++){
            //*(data + i + H1 * info->width) = 255;
        }
        getLetterBoundary(info,H1-wSY);
        wSX=5000,wSY=5000,wEX=0,wEY=0;
        Vx[i] = 0;
    }

}


void getLineInfo(AndroidBitmapInfo* info) {
    data = (uint8_t *)pixels;
    int x, y;
    int i,j;
    //사용하는 전역: Vx[x],Vy[y],wSX,wEX,wSY,wEY;

    //글자가 있는 한 행을 찾아냄.
    for(y=1; y<info->height-1;y++){
        for(x=1; x<info->width-1;x++) {
            if (*(data+x+y*info->width) == 255) {
                Vx[x]++;
                Vy[y]++;
                if (Vx[x] != 0) {
                    if (x < wSX)
                        wSX = x;
                    if (x > wEX)
                        wEX = x;
                    if (y < wSY)
                        wSY = y;
                    if (y > wEY)
                        wEY = y;
                }
            }
        }
        drawLineRange(info,y);
    }

}

JNIEXPORT void JNICALL Java_app_ssm_duck_duckapp_MainActivity_seperateLetter(JNIEnv *env, jobject obj, jobject bitmap) {
    AndroidBitmapInfo info;

    LOGE("Start to seperate Letter!");

    //get information for bitmap object
    if(0>AndroidBitmap_getInfo(env, bitmap,&info)){
        LOGE("AndroidBitmap_getInfo() failed!");
        return;
    }

    LOGI("imagesize(%d,%d)\n",info.width,info.height);

    if(info.format != ANDROID_BITMAP_FORMAT_A_8){
        LOGE("Bitmap format is not FORMAT_A_*:%d\n",info.format);
        return;
    }

    //attemp to lock the pixel address.
    if(0> AndroidBitmap_lockPixels(env,bitmap,&pixels)){
        LOGE("AndroidBitmap_lockPixels() failed!");
        return;
    }

    //함수
    getLineInfo(&info);

    AndroidBitmap_unlockPixels(env,bitmap);

}
