package app.ssm.duck.duckapp;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import app.ssm.duck.duckapp.File.JavaEditor;

public class MainActivity extends AppCompatActivity {

    //native method 부분
    static {
        System.loadLibrary("NDKTest");
    }

    public native void convertImage(Bitmap photo, Bitmap gbitmap, Bitmap tbitmap, Bitmap mbitmap); //이미지 전처리

    public native void convertForShow(Bitmap bitmap, Bitmap rbitmap); //보여주기 위해 format 변환

    public native void seperateLetter(Bitmap bitmap);
    //native를 위한 method 종료


    private static final int PICK_FROM_CAMERA = 0; //카메라
    private static final int PICK_FROM_GALLERY = 1; //갤러리
    private static final int MAKE_NEW_FOLDER = 2; //폴더 추가
    private static final int ERROR_MESSAGE = 3; //Error

    ImageView imgview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    //클릭이벤트
    public void mOnClick(View v) {
        final Intent intent;

        switch (v.getId()) {
            //카메라 선택
            case R.id.btn_take_camera:

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this)));

                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 150);

                try {
                    //intent.putExtra("return-data", true);
                    startActivityForResult(intent, PICK_FROM_CAMERA); //실제로 카메라 수행
                } catch (ActivityNotFoundException e) {
                    intent.putExtra(null, true);
                    startActivityForResult(intent, ERROR_MESSAGE);
                }

                break;

            //갤러리 선택
            case R.id.btn_select_gallery:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                /*
                // 잘라내기 셋팅
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1); //crop한 이미지의 x축 크기
                intent.putExtra("aspectY", 1); //crop한 이미지의 y축 크기
                //intent.putExtra("outputX", 256); //crop박스의 x축 비율
                //intent.putExtra("outputY", 256); //crop박스의 y축 비율
                //intent.putExtra("scale", "true");
                intent.putExtra("outputFormat", "JPEG");
                intent.putExtra("scale", "false");
*/
                try {
                    startActivityForResult(intent, PICK_FROM_GALLERY);

                } catch (ActivityNotFoundException e) {
                    startActivityForResult(Intent.createChooser(intent, "사진 불러오기"), ERROR_MESSAGE);
                }
                break;
            //폴더추가 선택
            case R.id.btn_add_folder:
                intent = new Intent(MainActivity.this, JavaEditor.class);
                startActivityForResult(intent, MAKE_NEW_FOLDER);
        }
    } //mOnclick 종

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent();
        imgview = (ImageView) findViewById(R.id.imageView1);
        Bitmap photo = null;
        Bitmap gbitmap, tbitmap, mbitmap, rbitmap;

        if (requestCode == ERROR_MESSAGE) {
            ;
        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case PICK_FROM_CAMERA:

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    final File file = getTempFile(this);
                    options.inSampleSize = 4;
                    photo = BitmapFactory.decodeFile(file.getAbsolutePath().toString(), options);
                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);
                    convertForShow(tbitmap, rbitmap);
                    SaveImage(rbitmap);
                    imgview.setImageBitmap(tbitmap);
                    break;

                case PICK_FROM_GALLERY:

                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = getRealPath(selectedImageUri);


                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inSampleSize = 4;
                    photo = BitmapFactory.decodeFile(selectedImagePath, options2);

                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);
                    convertForShow(tbitmap, rbitmap);
                    SaveImage(rbitmap);
                    imgview.setImageBitmap(tbitmap);
                    break;

                case MAKE_NEW_FOLDER:
                    break;
            }
        }

    } //onActivityResult 종료

    private void SaveImage(Bitmap bitmapimg) {

        File myDir = new File("/sdcard/SSMemo_folder");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapimg.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getTempFile(Context context) {
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, "image.tmp");
    }

    private String getRealPath(Uri uri) {
        String [] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = managedQuery(uri, proj, null, null, null);

        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(columnIdx);
    }
}
