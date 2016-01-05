package app.ssm.duck.duckapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

/**
 * Created by hyejung on 15. 12. 23.
 */
public class cropView extends View {
    int rotateFlag = 0;
    String image_path;
    private cropView cview;
    private Button btn;

    float x1 = 300, x2 = 300, x3 = 500, x4 = 500;
    float y1 = 300, y2 = 500, y3 = 500, y4 = 300;

    //드래그 여부를 결정하는 플래그 값.
    boolean dragcoordi = false;
    int refIdx = 0;
    Bitmap bitmapimg;
    Paint paint = new Paint();
    Bitmap backbitmap;
    int cwidth=0, cheight=0;

    public cropView(Context context){
        super(context);

        ///////image_path = xxx;;

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
        //화면이 터치됬을 경우엔 좌표를 확인한
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            pressedX = event.getX();
            pressedY = event.getY();
            compareToCoordi(pressedX,pressedY);
            //avertex.add(new vertex(event.getX(),event.getY(),false));
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            Log.i("MotionEvent", "ACTION_MOVE");
            if(dragcoordi == true){
                dragCoordi(event.getX(),event.getY());
            }
            //avertex.add(new vertex(event.getX(), event.getY(), true));
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            dragcoordi = false;
            refIdx = 0;
        }
        invalidate();
        return true;
    }

    public void compareToCoordi(float x,float y){
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

        //캔버스에 배경 비트맵 먼저 그려주고
        if(rotateFlag == 1 || rotateFlag == 3){
            cwidth = canvas.getWidth();
            cheight = canvas.getHeight();
            backbitmap = Bitmap.createScaledBitmap(bitmapimg, cwidth, cheight, false);
            canvas.drawBitmap(backbitmap, 0, 0, null);
        }else {
            //중간부터 canvas에 채우도록 코드 추가해야함!.
            cwidth = canvas.getWidth();
            cheight = (bitmapimg.getHeight()*canvas.getWidth())/canvas.getHeight();
            backbitmap = Bitmap.createScaledBitmap(bitmapimg,cwidth,cheight,false);
            canvas.drawBitmap(backbitmap, 0, (canvas.getHeight() - backbitmap.getWidth())/2, null);
        }

        //crop할 사각형 영역을 그려준다
        setCropRange(canvas);
    }

    void setCropRange(Canvas canvas){
        Paint paint = new Paint();
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

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

        //imageView = (ImageView)findViewById(R.id.cropimgView);

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
        //image_path = getIntent().getStringExtra("imagePath");
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
