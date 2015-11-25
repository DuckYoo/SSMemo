package app.ssm.duck.duckapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import app.ssm.duck.duckapp.File.JavaEditor;

public class MainActivity extends AppCompatActivity {

    //native method 부분
    static{
        System.loadLibrary("NDKTest");
    }
    public native String getStringFromNative();
    //native method 종료


    private static final int PICK_FROM_CAMERA = 0; //카메라
    private static final int PICK_FROM_GALLERY = 1; //갤러리
    private static final int MAKE_NEW_FOLDER = 2; //폴더 추가
    private static final int ERROR_MESSAGE = 3; //Error

    ImageView imgview; //이미지를 저장할 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView v = (TextView)findViewById(R.id.tV);
        v.setText(getStringFromNative());

    } //onCreate 종료


    //클릭이벤트
    public void mOnClick(View v){
        Intent intent;

        switch(v.getId()){
            //카메라 선택
            case R.id.btn_take_camera:

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 100);
                intent.putExtra("outputY", 250);

                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, PICK_FROM_CAMERA); //실제로 카메라 수행
                    //startActivityForResult(intent, 1); //실제로 카메라 수행
                } catch (ActivityNotFoundException e) {
                    intent.putExtra(null, true);
                    startActivityForResult(intent, ERROR_MESSAGE);
                }

                break;

            //갤러리 선택
            case R.id.btn_select_gallery:
                intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                // 잘라내기 셋팅
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1); //crop한 이미지의 x축 크기
                intent.putExtra("aspectY", 1); //crop한 이미지의 y축 크기
                intent.putExtra("outputX", 256); //crop박스의 x축 비율
                intent.putExtra("outputY", 256); //crop박스의 y축 비율
                intent.putExtra("scale","true");

                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent,"사진 불러오기"), PICK_FROM_GALLERY); //실제로 갤러리 불러오
                } catch (ActivityNotFoundException e) {
                    intent.putExtra(null, true);
                    startActivityForResult(Intent.createChooser(intent, "사진 불러오기"), ERROR_MESSAGE);
                }
                break;
            //폴더추가 선택
            case R.id.btn_add_folder:
                intent = new Intent(MainActivity.this, JavaEditor.class);
                startActivityForResult(intent,MAKE_NEW_FOLDER);
        }
    } //mOnclick 종



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent();
        imgview = (ImageView) findViewById(R.id.imageView1);

        if (requestCode == ERROR_MESSAGE) {
            ;
        }else if(resultCode == RESULT_OK){
            switch(requestCode){

                case PICK_FROM_CAMERA:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data"); //받은 이미지 Bitmap

                        //intent.setAction("c.ImageCropActivity");
                        //intent.putExtra("img",photo);
                        //intent = new Intent(MainActivity.this, ImageCropActivity.class);
                        //startActivity(intent);
                        imgview.setImageBitmap(photo);
                    }
                    break;

                case PICK_FROM_GALLERY:
                    Bundle extras2 = data.getExtras();
                    if (extras2 != null) {
                        Bitmap photo = extras2.getParcelable("data"); //가져온 이미지 Bitmap
                        imgview.setImageBitmap(photo);
                    }
                    break;

                case MAKE_NEW_FOLDER:
                    break;
            }
        }

    } //onActivityResult 종료


}
