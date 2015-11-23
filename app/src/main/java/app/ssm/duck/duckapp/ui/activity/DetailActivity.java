package app.ssm.duck.duckapp.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import app.ssm.duck.duckapp.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailActivity extends ActionBarActivity {

    public static final String EXTRA_IMAGE = "DetailActivity.EXTRA_IMAGE";
    public static final String EXTRA_CAPTION = "DetailActivity.EXTRA_CAPTION";

    @InjectView(R.id.image)
    ImageView mImage;
    @InjectView(R.id.text)
    TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);

        ViewCompat.setTransitionName(mImage, EXTRA_IMAGE);

        Picasso.with(this).load(getIntent().getStringExtra(EXTRA_IMAGE)).into(mImage);
        mText.setText("Hello");
        //mText.setText(getIntent().getStringExtra(EXTRA_CAPTION));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
