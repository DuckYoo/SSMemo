package app.ssm.duck.duckapp;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class GroupingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bitmap bm = (Bitmap) getIntent().getExtras().get("croppedImage");
        ImageView imgview = (ImageView)findViewById(R.id.croppedImageView);
        imgview.setImageBitmap(bm);
        setContentView(R.layout.activity_grouping);
    }
}
