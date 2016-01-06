package app.ssm.duck.duckapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class GroupingActivity extends AppCompatActivity {
    private Bitmap bm;
    private String bmPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //bmPath = getIntent().getStringExtra("convertedImagePath");
        //ImageView imgview = (ImageView)findViewById(R.id.croppedImageView);
        //bm = BitmapFactory.decodeFile(bmPath);
       // imgview.setImageBitmap(bm);
        setContentView(R.layout.activity_grouping);
    }
}
