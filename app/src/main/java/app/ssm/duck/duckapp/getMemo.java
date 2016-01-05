package app.ssm.duck.duckapp;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by broDuck on 16. 1. 5.
 */
public class getMemo extends AsyncTask<String, Integer, String> {
    @Override
    protected String doInBackground(String... params) {
        return "hello";
    }

    public void getResult(String memo_id) {

        try {
            URL url = new URL("http://210.118.64.177/android/getMemo.php?memo_id=" + memo_id);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();


        } catch (Exception e) {

        }
    }
}
