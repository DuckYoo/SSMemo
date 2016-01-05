package app.ssm.duck.duckapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Collection;

public class CropActivity extends AppCompatActivity {
    String image_path;
    Bitmap bitmapimg;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_crop);
        //rotateBitmapImage(getImageRotatedDegree());
        //drawMask();
        cropView cview = new cropView(this);
        setContentView(cview);
    }

    protected class cropView extends View{

        public cropView(Context context){
            super(context);
        }

        public void onDraw(Canvas canvas){
            rotateBitmapImage(getImageRotatedDegree());
            canvas.drawBitmap(bitmapimg, 0, 0, null);

           // Paint paint = new Paint();
           // paint.setColor(Color.WHITE);
           // paint.setStyle(Paint.Style.STROKE);
           // paint.setStrokeWidth(10);
           // canvas.drawRect(300,300,500,500,paint);
        }
    }


    //비트맵 회전을 위한 함수
    private void rotateBitmapImage(int degree){
        imageView = (ImageView)findViewById(R.id.cropimgView);

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
        //imageView.setImageBitmap(bitmapimg);
    }

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
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
}

