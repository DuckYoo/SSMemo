package app.ssm.duck.duckapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
    String user_id;
    ListViewAdapter list;
    ListView listView;

    /**
     * constructor
     * @param user_id   user's id
     * @param list      adapter object
     * @param listView  ListView object
     */
    public GetMemo(String user_id, ListViewAdapter list, ListView listView) {
        this.user_id = user_id;
        this.list = list;
        this.listView = listView;
    }

    /**
     * Async DB select from server
     * @param params    nothing
     * @return          nothing
     */
    @Override
    protected String doInBackground(String... params) {
        getResult();

        return "hello";
    }

    /**
     * Http Connection Method
     */
    public void getResult() {

        try {
            URL url = new URL("http://210.118.64.177/android/getMemo.php?user_id=" + user_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            readStream(conn.getInputStream());

        } catch (Exception e) {

        }
    }

    /**
     * result for selected data add ListView
     * @param in get InputStream to getResult.conn
     */
    private void readStream(InputStream in) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            Object obj = new Object();

            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                obj = JSONValue.parse(line);
            }

            JSONObject object = (JSONObject) obj;

            JSONArray array = (JSONArray) object.get("list");

            for (int i = 0; i < array.size(); i++) {
                object = (JSONObject) array.get(i);

                list.addItem(R.drawable.cameraimg, object.get("memo_name").toString(), object.get("update_date").toString(), object.get("memo_hash").toString());
            }
        } catch (Exception e) {
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
