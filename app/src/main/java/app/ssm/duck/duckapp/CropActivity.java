package app.ssm.duck.duckapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

public class CropActivity extends AppCompatActivity {
    int rotateFlag = 0;  int id = 0;
    int nwidth=0, nheight=0; float srtHeight = 0 , endHeight = 0;
    private cropView cview;
    private LinearLayout.LayoutParams params;
    private android.widget.Button btn;
    boolean dragcoordi = false, croppedflg = false; int refIdx = 0;
    String image_path;
    Bitmap bitmapimg,resizedbitmap;
    Matrix matrix;

    float x1 = 0, x2 = 0, x3 = 0, x4 = 0;
    float y1 = 0, y2 = 0, y3 = 0, y4 = 0;

//    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //cropView의 화면구성.
        LinearLayout vlayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vlayout.setLayoutParams(params);
        vlayout.setOrientation(LinearLayout.VERTICAL);
        vlayout.setBackgroundColor(Color.BLACK);

        final LinearLayout hlayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlayout.setLayoutParams(params);
        hlayout.setOrientation(LinearLayout.HORIZONTAL);

        Button cropBtn = new Button(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cropBtn.setLayoutParams(params);
        cropBtn.setGravity(Gravity.CENTER);
        cropBtn.setText("CROP");
        cropBtn.setId(id + 0);

        Button skipBtn = new Button(this);
        skipBtn.setLayoutParams(params);
        skipBtn.setGravity(Gravity.CENTER);
        skipBtn.setText("OKAY");
        skipBtn.setId(id + 1);

        cview = new cropView(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cview.setLayoutParams(params);
        cview.setDrawingCacheEnabled(true);

        hlayout.addView(cropBtn);
        hlayout.addView(skipBtn);
        vlayout.addView(hlayout);
        vlayout.addView(cview);

        setContentView(vlayout);


        //버튼 클릭 이벤트.
        cropBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast toast = Toast.makeText(v.getContext(), "cropClicked", Toast.LENGTH_LONG);
                toast.show();
                cropping(cview);
                hlayout.removeView(v);
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast toast = Toast.makeText(v.getContext(), "skipClicked", Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }


    public void cropping(View v){
        //화면에 더이상 그만 그림.
        v.destroyDrawingCache();
        v.bringToFront();

        //crop한 영역의 width와 height를 결정
        int cw = (int)getWidth();
        int ch = (int)getHeight();

        //crop된 이미지를 보여준다.

        float[] src = new float[] {x1,y1,x4,y4,x3,y3,x2,y2};
        //float[] dst = new float[] {0,0,resizedbitmap.getWidth(),0,resizedbitmap.getWidth(),resizedbitmap.getHeight(),0,resizedbitmap.getHeight()};
        float[] dst = new float[] {x1,y1,x1+cw,y1,x1+cw,y1+ch,x1,y1+ch};

        matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);


        try{
            if(resizedbitmap == null)
                Log.d("TAG","resizedbitmap is null!");
            else {
                Bitmap croppedbitmap = Bitmap.createBitmap(resizedbitmap, 0, 0, resizedbitmap.getWidth(), resizedbitmap.getHeight(), matrix, true);
                //Bitmap croppedbitmap = Bitmap.createBitmap(bitmapimg,0,0,bitmapimg.getWidth(),bitmapimg.getHeight(),matrix,true);
                if (bitmapimg != croppedbitmap) {
                    bitmapimg.recycle();
                    //bitmapimg = Bitmap.createBitmap(croppedbitmap, 0, 0, resizedbitmap.getWidth(), resizedbitmap.getHeight());
                    //bitmapimg = Bitmap.createBitmap(croppedbitmap,(int)x1,(int)y1,cw,ch);
                    bitmapimg = croppedbitmap;
                }
            }
        }catch (OutOfMemoryError e){

        }

        //crop된 이미지를 canvas에 그린다.
        //canvas.drawBitmap(bitmapimg, 0, srtHeight, null);
        croppedflg = true;
        v.invalidate();

    }

    public float getWidth(){
        float maxX = getMax(x1,x2,x3,x4);
        float minX = getMin(x1,x2,x3,x4);
        return (maxX-minX);
    }
    public float getHeight(){
        float maxY = getMax(y1,y2,y3,y4);
        float minY = getMin(y1,y2,y3,y4);
        return (maxY-minY);
    }

    public float getMax(float a,float b, float c,float d){
        float x = a>b?a:b;
        float y = c>d?c:d;
        return x>y?x:y;
    }

    public float getMin(float a,float b,float c,float d){
        float x = a<b?a:b;
        float y = c<d?c:d;
        return x<y?x:y;
    }


    public class cropView extends View{
        Paint paint = new Paint();
        Bitmap backbitmap;

        public cropView(Context context){
            super(context);

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);
            rotateBitmapImage(getImageRotatedDegree());
        }

        public cropView(Context context, AttributeSet attrs){
            super(context,attrs);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);
            rotateBitmapImage(getImageRotatedDegree());
        }


        public boolean onTouchEvent(MotionEvent event){
            super.onTouchEvent(event);
            //터치된 좌표를 받아오는 변수
            float pressedX,pressedY;
            //화면이 터치됬을 경우엔 좌표를 확인한다.
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                pressedX = event.getX();
                pressedY = event.getY();
                compareToCoordi(pressedX,pressedY);
             }
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                if(dragcoordi == true){
                    dragCoordi(event.getX(),event.getY());
                }
            }
            if(event.getAction() == MotionEvent.ACTION_UP){
                dragcoordi = false;
                refIdx = 0;
            }
            invalidate();
            return true;
        }

        public void compareToCoordi(float x,float y){

            if(x<=0 || x>=nwidth || y<= srtHeight || y>= endHeight){
                dragcoordi = false;
                return;
            }
            //좌표가 사각형의 코너와 일치하면.
            if( x>= x1-50 && x <= x1+50 && y>= y1-50 && y <= y1+50 ){
                dragcoordi = true;
                x1 = x;
                y1 = y;
                refIdx = 1;
            }else if( x>= x2-50 && x <= x2+50 && y>= y2-50 && y <= y2+50 ){
                dragcoordi = true;
                x2 = x;
                y2 = y;
                refIdx = 2;
            }else if( x>= x3-50 && x <= x3+50 && y>= y3-50 && y <= y3+50 ){
                dragcoordi = true;
                x3 = x;
                y3 = y;
                refIdx = 3;
            }else if( x>= x4-50 && x <= x4+50 && y>= y4-50 && y <= y4+50 ){
                dragcoordi = true;
                x4 = x;
                y4 = y;
                refIdx =4;
            }
        }
        public void dragCoordi(float x, float y){
            //범위를 벗어날 경우엔 자신 그대로.
            if(x<=0 || x>=nwidth || y<= srtHeight || y>= endHeight){
                dragcoordi = false;
                return;
            }
            if(refIdx == 1){
                x1 = x;
                y1 = y;
            }else if(refIdx == 2){
                x2 = x;
                y2 = y;
            }else if(refIdx == 3){
                x3 = x;
                y3 = y;
            }else if(refIdx == 4){
                x4 = x;
                y4 = y;
            }
        }

        public void onDraw(Canvas canvas){
            super.onDraw(canvas);
            canvas.drawColor(Color.BLACK);

            //Tmp
            Path path = new Path();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            //TMP

                //캔버스에 배경 비트맵 먼저 그려주고
                if (rotateFlag == 1 || rotateFlag == 3) {
                    nwidth = canvas.getWidth();
                    nheight = canvas.getHeight();
                    backbitmap = Bitmap.createScaledBitmap(bitmapimg, nwidth, nheight, false);
                    srtHeight = 0;
                    endHeight = nheight;
                    canvas.drawBitmap(backbitmap, 0, srtHeight, null);
                } else {
                    //중간부터 canvas에 채우도록 코드 추가해야함!.
                    nwidth = canvas.getWidth();
                    nheight = (canvas.getHeight() * canvas.getWidth()) / bitmapimg.getWidth();
                    backbitmap = Bitmap.createScaledBitmap(bitmapimg, nwidth, nheight, false);
                    srtHeight = (canvas.getHeight() - nheight) / 2;
                    endHeight = srtHeight + nheight;
                    canvas.drawBitmap(backbitmap, 0, srtHeight, null);
                }
                resizedbitmap = backbitmap;

            //crop할 사각형 영역을 그려준다
            if(croppedflg == false){
                setCropRange(canvas);
            }else{
            }
        }
    }

    void setCropRange(Canvas canvas){
        Paint paint = new Paint();
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        if(x1 == 0 && x2 == 0 && x3 == 0 && x4 == 0){
            x1 = canvas.getWidth()/2 - 200;
            x2 = canvas.getWidth()/2 - 200;
            x3 = canvas.getWidth()/2 + 200;
            x4 = canvas.getWidth()/2 + 200;

            y1 = canvas.getHeight()/2 - 200;
            y2 = canvas.getHeight()/2 + 200;
            y3 = canvas.getHeight()/2 + 200;
            y4 = canvas.getHeight()/2 - 200;
        }

        canvas.drawCircle(x1, y1, 10, paint);
        canvas.drawCircle(x2,y2,10,paint);
        canvas.drawCircle(x3,y3,10,paint);
        canvas.drawCircle(x4, y4, 10, paint);

        path.moveTo(x1, y1);
        path.lineTo(x1,y1);
        path.lineTo(x2,y2);
        path.lineTo(x3,y3);
        path.lineTo(x4,y4);
        path.lineTo(x1,y1);
        path.close();
        canvas.drawPath(path,paint);
    }


    //비트맵 이미지를 회전해주는 함수.
    private void rotateBitmapImage(int degree){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        bitmapimg = BitmapFactory.decodeFile(image_path,options);

        if(degree != 0 && bitmapimg != null){
            Matrix m = new Matrix();
            m.setRotate(degree,(float)bitmapimg.getWidth(),(float)bitmapimg.getHeight());
            try{
                Bitmap rotateimg = Bitmap.createBitmap(bitmapimg,0,0,bitmapimg.getWidth(),bitmapimg.getHeight(),m,true);
                if(bitmapimg != rotateimg){
                    bitmapimg.recycle();
                    bitmapimg = rotateimg;
                }
            }catch (OutOfMemoryError e){

            }
        }
    }

    //이미지의 회전된 정도를 계산.
    public int getImageRotatedDegree(){
        int degree = 0;
        image_path = getIntent().getStringExtra("imagePath");
        ExifInterface exif = null;

        try{
            exif = new ExifInterface(image_path);

        }catch (IOException e){
            Log.e("TAG","cannot read exif");
            e.printStackTrace();
        }

        if(exif != null){
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if(orientation != -1){
                switch (orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        rotateFlag = 1;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        rotateFlag = 2;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        rotateFlag = 3;
                        break;
                }
            }
        }
        return degree;
    }
}

