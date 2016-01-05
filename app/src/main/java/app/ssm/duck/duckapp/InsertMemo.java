package app.ssm.duck.duckapp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by broDuck on 16. 1. 4.
 */
public class InsertMemo extends AsyncTask<String, Void, Long> {
    String memoName;
    String userId;

    //constructor
    public InsertMemo(String memoName, String userId) {
        this.memoName = memoName;
        this.userId = userId;
    }

    @Override
    protected Long doInBackground(String... urls) {
        insertDB();

        return (long) 1;
    }

    public String insertDB() {
        try {
            String addr = "http://210.118.64.177/android/insert.php";

            String data = URLEncoder.encode("memo_name", "UTF-8") + "="
                        + URLEncoder.encode(memoName, "UTF-8");
            data += "&" + URLEncoder.encode("user_id", "UTF-8") + "="
                        + URLEncoder.encode(userId, "UTF-8");

            URL url = new URL(addr);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(data);
            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                break;
            }

            return sb.toString();


        } catch (Exception e) {
            Log.d("DB", "insertDB Error!");

            return "Error";
        }
    }
}
