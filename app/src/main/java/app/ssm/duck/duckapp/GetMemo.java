package app.ssm.duck.duckapp;

import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by broDuck on 16. 1. 5.
 */
public class GetMemo extends AsyncTask<String, Integer, String> {
    String memo_id;

    public GetMemo(String memo_id) {
        this.memo_id = memo_id;
    }

    @Override
    protected String doInBackground(String... params) {
        getResult();

        return "hello";
    }

    public void getResult() {

        try {
            URL url = new URL("http://210.118.64.177/android/getMemo.php?memo_id=" + memo_id);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();

            readStream(conn.getInputStream());

        } catch (Exception e) {

        }
    }

    private void readStream(InputStream in) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                Log.d("DB", line);
            }

        } catch (IOException e) {
            Log.d("DB", "GetMemo.readStram Error!");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d("DB", "GetMemo.readStream reader.close() Error!");
                }
            }
        }
    }
}
