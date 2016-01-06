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

    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    private String convertedFilePath;
    UserInfo account;

    ImageView imgview; //이미지를 저장할 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        //Intent successIntent = getIntent();

        //if (successIntent.hasExtra("data")) {
        //    account = (UserInfo) successIntent.getSerializableExtra("data");
        //}

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

        mAdapter.addItem(getResources().getDrawable(R.drawable.cameraimg), "메모 1", "2016-01-05");
        mAdapter.addItem(getResources().getDrawable(R.drawable.cameraimg), "메모 1", "2016-01-05");
        mAdapter.addItem(getResources().getDrawable(R.drawable.cameraimg), "메모 1", "2016-01-05");
        mAdapter.addItem(getResources().getDrawable(R.drawable.cameraimg), "메모 1", "2016-01-05");
        mAdapter.addItem(getResources().getDrawable(R.drawable.cameraimg), "메모 1", "2016-01-05");

//        GetMemo getMemo = new GetMemo(account.getId());
//        getMemo.execute();
    }

    /**
     * 리스트뷰 만들어주는 클래스들
     */
    private class ViewHolder {
        public ImageView mImage;
        public TextView mName;
        public TextView mUpdate;
    }

    public class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cardview, null);

                holder.mImage = (ImageView) convertView.findViewById(R.id.memo_image);
                holder.mName = (TextView) convertView.findViewById(R.id.memo_name);
                holder.mUpdate = (TextView) convertView.findViewById(R.id.update_date);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);

            if (mData.mImage != null) {
                holder.mImage.setVisibility(View.VISIBLE);
                holder.mImage.setImageDrawable(mData.mImage);
            } else {
                holder.mImage.setVisibility(View.GONE);
            }

            holder.mName.setText(mData.mName);
            holder.mUpdate.setText(mData.mUpdate);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TAG","list click!!");
                }
            });

            return convertView;
        }

        public void addItem(Drawable image, String mName, String mUpdate) {
            ListData addInfo = new ListData();
            addInfo.mImage = image;
            addInfo.mName = mName;
            addInfo.mUpdate = mUpdate;

            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 카메라 또는 갤러리를 선택했을때의 동작을 결정하는 함수.
     */
    public void mOnClick(View v) {
        final Intent intent;

        switch (v.getId()) {
            //카메라 선택
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
        }
    }

    /**
     * 카메라와 갤러리를 선택한 뒤 다시 메인 엑티비티로 돌아온 뒤 수행하는 작업.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //crop을 수행하기 위해 새로운 intent 생성
        Intent intent = new Intent(this, CropActivity.class);
        //카메라 또는 갤러리로부터 가져온 이미지를 담을 bitmap
        Bitmap photo = null;
        //전처리한 비트맵 정보를 담을 bitmap
        Bitmap gbitmap, tbitmap, mbitmap, rbitmap;

        if (requestCode == ERROR_MESSAGE) {

        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {

                //카메라를 선택했을 경우
                case PICK_FROM_CAMERA:

                    //사진의 화질을 연산하기 좋도록 1/4로 조정
                   BitmapFactory.Options options = new BitmapFactory.Options();
                    final File file = getTempFile(this);
                    options.inSampleSize = 4;
                    photo = BitmapFactory.decodeFile(file.getAbsolutePath().toString(), options);

                    //crop activity로 이
                    intent.putExtra("imagePath", file.getAbsolutePath().toString());
                    startActivity(intent);

                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);

                    seperateLetter(tbitmap);
                    convertForShow(tbitmap, rbitmap);
                    SaveImage(rbitmap);

                    break;

                //갤러리를 선택했을 경우
                case PICK_FROM_GALLERY:

                    //갤러리 이미지의  Uri를 가지고 경로를 찾음
                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = getRealPath(selectedImageUri);

                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inSampleSize = 4;
                    photo = BitmapFactory.decodeFile(selectedImagePath, options2);

                    intent.putExtra("imagePath", selectedImagePath);
                    startActivity(intent);

                    gbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //grayscaled
                    tbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //thresholeded
                    mbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ALPHA_8); //mopology
                    rbitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888); //showimage

                    convertImage(photo, gbitmap, tbitmap, mbitmap);
                    seperateLetter(tbitmap);
                    convertForShow(tbitmap, rbitmap);
                    SaveImage(rbitmap);

                    break;

                case MAKE_NEW_FOLDER:
                    break;
            }
        }

    }

    /**
     * 비트맵 이미지를 받아 SDcard에 저장하는 함수.
     */
    private void SaveImage(Bitmap bitmapimg) {
        Intent gintent = new Intent(this,GroupingActivity.class);

        File myDir = new File("/sdcard/SSMemo_folder");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);


        /* 이미지 서버 전송 시작
         * @Usage
         * 파일 확인 방법
         * 브라우저를 열고
         * ftp://210.118.64.177 접속
         * images 디렉터리에 들어가면
         * userId 디렉터리에 fname 으로 저장
         * 거기 들어가서 이미지 파일 클릭하면 볼 수 있음!
         */
//        SaveToServer server = new SaveToServer(myDir, fname, account.getId());
//        try {
//            server.execute(new URL("http://210.118.64.177"));
//        } catch (MalformedURLException e) {
//            Log.d("FTP", "SaveImage.server.execute : Execute Error!");
//        }
//        //이미지 서버 전송 끝

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
        convertedFilePath = file.getAbsolutePath().toString();
    }

    /**
     * 임시 (이미지) 파일을 생성하는 함수
     */
    private File getTempFile(Context context) {
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, "image.tmp");
    }

    /**
     * 갤러리에서 선택한 이미지의 절대 경로를 반환하는 함수.
     */
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

//        TextView user_disPlayname = (TextView) findViewById(R.id.user_displayName);
//        user_disPlayname.setText(account.getDisplayName());

//        TextView user_email = (TextView) findViewById(R.id.user_email);
//        user_email.setText(account.getEmail());
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
