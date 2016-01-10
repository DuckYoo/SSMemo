package app.ssm.duck.duckapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CropActivity extends AppCompatActivity {
    int rotateFlag = 0;  int id = 0;
    int nwidth=0, nheight=0; float srtHeight = 0 , endHeight = 0; int cwidth=0, cheight=0;
    private cropView cview;
    private LinearLayout.LayoutParams params;
    boolean dragcoordi = false, croppedflg = false, initrange = true, skipflg = false;
    int refIdx = 0;
    int btnPressed = 0;
    String image_path; String buttonTxt;
    Bitmap bitmapimg,resizedbitmap;
    Matrix matrix;
    UserInfo account;

    //crop영역 좌표.
    float x1 = 0, x2 = 0, x3 = 0, x4 = 0;
    float y1 = 0, y2 = 0, y3 = 0, y4 = 0;

    //native 함수 부
    static {
        System.loadLibrary("NDKTest");
    }

    public native void convertImage(Bitmap photo, Bitmap gbitmap, Bitmap tbitmap);
    public native void convertForShow(Bitmap bitmap, Bitmap rbitmap);
    public native void seperateLetter(Bitmap bitmap);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setLayoutView(this);
    }

    void setLayoutView(Context context){
        //cropView의 화면구성.
        LinearLayout vlayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vlayout.setLayoutParams(params);
        vlayout.setOrientation(LinearLayout.VERTICAL);
        vlayout.setBackgroundColor(Color.BLACK);

        final LinearLayout hlayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        hlayout.setOrientation(LinearLayout.HORIZONTAL);

        final Button cropBtn = new Button(this);
        cropBtn.setLayoutParams(params);
        buttonTxt = getString(R.string.diversion_string);
        cropBtn.setText(buttonTxt);
        cropBtn.setId(id + 0);

        final Button skipBtn = new Button(this);
        skipBtn.setLayoutParams(params);
        buttonTxt = getString(R.string.skip_string);
        skipBtn.setText(buttonTxt);
        skipBtn.setId(id + 1);

        //레이아웃에 custom View를 추가.
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
                if(btnPressed == 0){
                    Toast toast = Toast.makeText(v.getContext(),R.string.diversion_string, Toast.LENGTH_LONG);
                    toast.show();
                    //크롭버튼으로 변환.
                    buttonTxt = getString(R.string.crop_string);
                    cropBtn.setText(buttonTxt);

                    //이미지 왜곡
                    cropping(cview);
                    btnPressed++;

                }else if(btnPressed == 1 ){
                    Toast toast = Toast.makeText(v.getContext(),R.string.crop_string, Toast.LENGTH_LONG);
                    toast.show();
                    //확인버튼으로 전환
                    buttonTxt = getString(R.string.confirm_string);
                    skipBtn.setText(buttonTxt);

                    //버튼 제거
                    hlayout.removeView(v);

                    //customView를 채워준다.
                    cutting(cview);
                    convertBitmapWithJni(bitmapimg);
                    btnPressed++;

                }else{
                    Intent intent = new Intent();
                    intent.putExtra("SUCCESS", 4321);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String str;
                if(btnPressed == 0){
                    Toast toast = Toast.makeText(v.getContext(),R.string.skip_string, Toast.LENGTH_LONG);
                    toast.show();

                    //diversion에서 crop버튼으로 text를 바꿔준다.
                    buttonTxt = getString(R.string.crop_string);
                    cropBtn.setText(buttonTxt);

                    //이미지 왜곡
                    skipflg = true;
                    cropping(cview);
                    btnPressed++;

                }else if(btnPressed == 1){
                    Toast toast = Toast.makeText(v.getContext(),R.string.skip_string, Toast.LENGTH_LONG);
                    toast.show();

                    buttonTxt = getString(R.string.confirm_string);
                    cropBtn.setText(buttonTxt);

                    //버튼 제거
                    hlayout.removeView(v);

                    //cutting할 좌표만 set해주면 될듯!.
                    skipflg = true;
                    cutting(cview);
                    convertBitmapWithJni(bitmapimg);
                    btnPressed++;

                }else{
                    Intent intent = new Intent();
                    intent.putExtra("SUCCESS", 4321);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    /**
     * 사용자가 지정한 영역을 실제로 자르는 함수
     */
    public void cutting(View v){
        v.destroyDrawingCache();
        v.bringToFront();

        if(skipflg == true){
            skipflg = false;
        }else {
            int srtx = (int) x1;
            int srty = (int) y1;

            int cw = (int) getWidth();
            int ch = (int) getHeight();

            try {
                if (rotateFlag == 2 || rotateFlag == 0) {
                    srty = srty - (int) srtHeight;
                }
                Bitmap cuttedbitmap = Bitmap.createBitmap(resizedbitmap, srtx, srty, cw, ch);
                if (bitmapimg != cuttedbitmap) {
                    bitmapimg.recycle();
                    bitmapimg = cuttedbitmap;
                }
            } catch (OutOfMemoryError e) {

            }
        }
        croppedflg = true;
        v.invalidate();
    }


    /**
     * crop을 위해 Bitmap을 왜곡하는 함수
     */
    public void cropping(View v){
        //화면에 더이상 그만 그림.
        v.destroyDrawingCache();
        v.bringToFront();

        int cw = (int)getWidth();
        int ch = (int)getHeight();

        if(skipflg == true){
            x1=0;x2=0;x3=0;x4=0;
            skipflg = false;
        }

        float[] src = new float[] {x1,y1,x4,y4,x3,y3,x2,y2};
        float[] dst = new float[] {x1,y1,x1+cw,y1,x1+cw,y1+ch,x1,y1+ch};
        //float[] dst = new float[] {0,0,resizedbitmap.getWidth(),0,resizedbitmap.getWidth(),resizedbitmap.getHeight(),0,resizedbitmap.getHeight()};

        matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);

        try{
            if(resizedbitmap == null)
                Log.d("TAG","resizedbitmap is null!");
            else {
                //선택한 영역을 펴서 새로운 bitmap 생성
                Bitmap croppedbitmap = Bitmap.createBitmap(resizedbitmap, 0, 0, resizedbitmap.getWidth(), resizedbitmap.getHeight(), matrix, true);
                if (bitmapimg != croppedbitmap) {
                    bitmapimg.recycle();
                    bitmapimg = croppedbitmap;
                }
            }
        }catch (OutOfMemoryError e){

        }
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

    /**
     * crop작업을 할 customView 클래스
     */
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
            super(context, attrs);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);
            rotateBitmapImage(getImageRotatedDegree());
        }


        public boolean onTouchEvent(MotionEvent event){
            super.onTouchEvent(event);
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
            //화면 업데이트
            invalidate();
            return true;
        }

        /**
         * 사용자가 터치한 영역이 범위내인지를 확인하는 함수.
         */
        public void compareToCoordi(float x,float y){

            if(btnPressed == 1){
                dragcoordi = true;
            }else{
                if(x<=0 || x>=nwidth || y<= srtHeight || y>= endHeight){
                    dragcoordi = false;
                    return;
                }
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

        /**
         * 사용자가 드래그할때 crop영역의 각 꼭지점의 위치를 새로 setting해주는 함수.
         */
        public void dragCoordi(float x, float y){
            //범위를 벗어날 경우엔 자신 그대로.
            if(btnPressed == 1){
                dragcoordi = true;

                if(refIdx == 1){
                    x1 = x;
                    y1 = y;
                    x2 = x;
                    y4 = y;
                }else if(refIdx == 2){
                    x2 = x;
                    y2 = y;
                    x1 = x;
                    y3 = y;
                }else if(refIdx == 3){
                    x3 = x;
                    y3 = y;
                    x4 = x;
                    y2 = y;
                }else if(refIdx == 4){
                    x4 = x;
                    y4 = y;
                    x3 = x;
                    y1 = y;
                }

            }else{
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
        }


        /**
         * cropView의 Canvas에 Bitmap과 crop영역을 그려주는 함수.
         */
        public void onDraw(Canvas canvas){
            super.onDraw(canvas);
            canvas.drawColor(Color.BLACK);

            cwidth = canvas.getWidth();
            cheight = canvas.getHeight();

            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);


            //비트맵의 회전 정도에 따라 캔버스에 비율을 맞춰서 그려준다.
            if (rotateFlag == 1 || rotateFlag == 3) {
                nwidth = cwidth;
                nheight = cheight;
                backbitmap = Bitmap.createScaledBitmap(bitmapimg, nwidth, nheight, false);
                srtHeight = 0;
                endHeight = nheight;
                canvas.drawBitmap(backbitmap, 0, srtHeight, null);
            } else {
                nwidth = cwidth;
                nheight = (cheight * cwidth) / bitmapimg.getWidth();
                backbitmap = Bitmap.createScaledBitmap(bitmapimg, nwidth, nheight, false);
                srtHeight = (canvas.getHeight() - nheight) / 2;
                endHeight = srtHeight + nheight;
                canvas.drawBitmap(backbitmap, 0, srtHeight, null);
            }
            resizedbitmap = backbitmap;

            //crop할 사각형 영역을 그려준다
            if(btnPressed == 0 && croppedflg == false){
                setCropRange(canvas);
            }else if(btnPressed == 1 && croppedflg == false){
                setCropRecRange(canvas);
            }else{
                ;
            }
        }
    }

    /**
     * crop영역을 지정하고 그려주는 함수.
     */
    void setCropRange(Canvas canvas){

        Paint paint = new Paint();
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        //crop영역의 초기화
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


    void setCropRecRange(Canvas canvas){
        Paint paint = new Paint();
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        //crop영역의 초기화
        if(initrange == true){
            x1 = canvas.getWidth()/2 - 200;
            x2 = canvas.getWidth()/2 - 200;
            x3 = canvas.getWidth()/2 + 200;
            x4 = canvas.getWidth()/2 + 200;

            y1 = canvas.getHeight()/2 - 200;
            y2 = canvas.getHeight()/2 + 200;
            y3 = canvas.getHeight()/2 + 200;
            y4 = canvas.getHeight()/2 - 200;

            initrange = false;
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



    /**
     * 회전정도를 인자로 받아 비트맵이미지를 회전해주는 함수
     */
    private void rotateBitmapImage(int degree){

        // Bitmap bm = BitmapFactory.decodeFile(image_path);

        //if(bm.getWidth() > 3000 || bm.getHeight() > 3000) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        bitmapimg = BitmapFactory.decodeFile(image_path, options);
        // }else{
        //  bitmapimg = bm;
        //}

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

    /**
     * 이미지의 회전 정도를 계산하고 반환해주는 함수.
     */
    public int getImageRotatedDegree(){
        int degree = 0;
        image_path = getIntent().getStringExtra("imagePath");
        account = (UserInfo) getIntent().getSerializableExtra("data");
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

    private void SaveImage(Bitmap bitmap) {
        //String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        //File myDir = new File(ex_storage);
        File myDir = new File("/sdcard/SSMemo_folder");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        final String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);

        /**
         * 이미지 서버 전송 시작
         */
        SaveToServer server = new SaveToServer(myDir, fname, account.getId());
        try {
            server.execute(new URL("http://210.118.64.177:8080"));
        } catch (MalformedURLException e) {
            Log.d("FTP", "SaveImage.server.execute : Execute Error!");
        }
        /**
         * 끝
         */

        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        //여기서 경로를 받아서 List에 추가해주기!
        image_path = file.getAbsolutePath().toString();

        //여기서 경로를 받아서 List에 추가해주기!
        final String str = file.getAbsolutePath().toString();

        View dialog = View.inflate(getApplicationContext(), R.layout.input_memo_name, null);
        final AlertDialog ad = new AlertDialog.Builder(CropActivity.this).setView(dialog).create();
        final EditText folderName = (EditText) dialog.findViewById(R.id.folderName);
        dialog.findViewById(R.id.completeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderName.getText().toString().length() == 0) {
                    Toast.makeText(CropActivity.this, "아무것도 입력하지 않으셨는데...", Toast.LENGTH_SHORT).show();

                } else {
                    InsertMemo insertMemo = new InsertMemo(folderName.getText().toString(), account.getId(), fname);

                    insertMemo.execute("http://210.118.64.177/android/insert.php");
                    ad.hide();

                    Toast.makeText(CropActivity.this, "메모가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(4321);
                }
            }
        });
        ad.show();
    }

    protected void convertBitmapWithJni(Bitmap bitmap){
        Bitmap gbitmap, tbitmap, rbitmap;

        gbitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
        tbitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
        rbitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888); //showimage

        convertImage(bitmap, gbitmap, tbitmap);
        seperateLetter(tbitmap);
        convertForShow(tbitmap, rbitmap);
        SaveImage(rbitmap);
    }


}

