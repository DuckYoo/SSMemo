package app.ssm.duck.duckapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    //native method 부분
    static {
        System.loadLibrary("NDKTest");
    }
    //비트맵 이미지 전처리 함수
    public native void convertImage(Bitmap photo, Bitmap gbitmap, Bitmap tbitmap, Bitmap mbitmap);
    //비트맵 format을 맞춰주는 함수
    public native void convertForShow(Bitmap bitmap, Bitmap rbitmap);
    //문자영역 추출 함수
    public native void seperateLetter(Bitmap bitmap);


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
            //카메라 선
            case R.id.btn_take_camera:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this)));
                try {
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    intent.putExtra(null, true);
                    startActivityForResult(intent, ERROR_MESSAGE);
                }
                break;

            //갤러리 선택
            case R.id.btn_select_gallery:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                try {
                    startActivityForResult(intent, PICK_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                    startActivityForResult(Intent.createChooser(intent, "사진 불러오기"), ERROR_MESSAGE);
                }
                break;

            //폴더추가 선택
            case R.id.btn_add_folder:
                break;
        }
    }

    //
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this,CropActivity.class);
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

                    intent.putExtra("imagePath",file.getAbsolutePath().toString());
                    startActivity(intent);

                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);

                    /////////서버로 gbitmap

                    seperateLetter(tbitmap);
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

                    intent.putExtra("imagePath",selectedImagePath);
                    startActivity(intent);

                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);
                    seperateLetter(tbitmap);
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
