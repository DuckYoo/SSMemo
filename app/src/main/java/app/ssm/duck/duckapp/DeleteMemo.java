package app.ssm.duck.duckapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by hjson on 16. 1. 9.
 */
public class DeleteMemo extends AsyncTask<String, String, String> {

    String memo_hash;

    public DeleteMemo(String memo_hash) {
        this.memo_hash = memo_hash;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String addr = "http://www.broduck.com/memo/removePage";

            String data = URLEncoder.encode("memo_hash", "UTF-8") + "="
                    + URLEncoder.encode(memo_hash, "UTF-8");

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
            Log.d("DB", "DeleteMemo Error!");

            return "Error";
        }
    }


}
