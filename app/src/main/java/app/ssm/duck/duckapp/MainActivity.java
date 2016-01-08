package app.ssm.duck.duckapp;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_FROM_CAMERA = 0; //카메라
    private static final int PICK_FROM_GALLERY = 1; //갤러리
    private static final int MAKE_NEW_FOLDER = 2; //폴더 추가
    private static final int ERROR_MESSAGE = 3; //Error

    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    public UserInfo account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        Intent successIntent = getIntent();

        if (successIntent.hasExtra("data")) {
            account = (UserInfo) successIntent.getSerializableExtra("data");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /**
         * 슬라이드 메뉴 만들어줌.
         */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
         * 리스트뷰 만들어줌
         */
        mListView = (ListView) findViewById(R.id.mList);

        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);

        GetMemo getMemo = new GetMemo(account.getId(), mAdapter, mListView);
        getMemo.execute();
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

            // 임시로 추가해놓은 메모 만들기
            case R.id.btn_add_folder:
                View dialog = View.inflate(getApplicationContext(), R.layout.input_memo_name, null);
                final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).setView(dialog).create();
                final EditText folderName = (EditText) dialog.findViewById(R.id.folderName);
                dialog.findViewById(R.id.completeButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (folderName.getText().toString().length() == 0) {

                        } else {
                            InsertMemo insertMemo = new InsertMemo(folderName.getText().toString(), account.getId());

                            insertMemo.execute("http://210.118.64.177/android/insert.php");
                            ad.hide();

                            Toast.makeText(MainActivity.this, "메모가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ad.show();
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this, CropActivity.class);
        Bitmap photo = null;

        if (requestCode == ERROR_MESSAGE) {
            ;
        } else if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case PICK_FROM_CAMERA:
                    //크기가 일정 이상일 경우에만 option을 지정해준다
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    final File file = getTempFile(this);
                    options.inSampleSize = 2;
                    photo = BitmapFactory.decodeFile(file.getAbsolutePath().toString(), options);

                    intent.putExtra("imagePath", file.getAbsolutePath().toString());
                    intent.putExtra("data", account);
                    startActivity(intent);

                    break;

                case PICK_FROM_GALLERY:

                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = getRealPath(selectedImageUri);


                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inSampleSize = 2;
                    photo = BitmapFactory.decodeFile(selectedImagePath, options2);

                    intent.putExtra("imagePath", selectedImagePath);
                    startActivity(intent);

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

        //전처리된 이미지의 경로.
        //convertedFilePath = file.getAbsolutePath().toString();
    }

    private File getTempFile(Context context) {
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, "image.tmp");
    }

    private String getRealPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = managedQuery(uri, proj, null, null, null);

        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(columnIdx);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navi, menu);

        TextView user_disPlayname = (TextView) findViewById(R.id.user_displayName);
        user_disPlayname.setText(account.getDisplayName());

        TextView user_email = (TextView) findViewById(R.id.user_email);
        user_email.setText(account.getEmail());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            // setting
            // 지금 뭐 딱히 할 건 없음
        } else if (id == R.id.nav_logout) {
            // sign out!
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("data", 1);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
