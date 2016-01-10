package app.ssm.duck.duckapp;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by broDuck on 15. 12. 23.
 */
public class SaveToServer extends AsyncTask<URL, Void, Long> {
    File file;
    String dir;
    String dName;

    public SaveToServer(File file, String dir, String dName) {
        this.file = file;
        this.dir = dir;
        this.dName = dName;
    }

    @Override
    protected Long doInBackground(URL... urls) {
        SaveImageToServer(file, dir, dName);

        return (long) 1;
    }

    private void SaveImageToServer(File dir, String name, String dName) {
        final String FTP_HOST= "210.118.64.177";

        /*********  FTP USERNAME ***********/
        final String FTP_USER = "ssmemo";

        /*********  FTP PASSWORD ***********/
        final String FTP_PASS = "123000";

        FTPClient client = new FTPClient();

        try {
            Log.d("FTP", "FTP Start");

            File file = new File(dir, name);

            client.connect(FTP_HOST);
            client.login(FTP_USER, FTP_PASS);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d("FTP", "login success");

            InputStream inputStream = new FileInputStream(file);

            client.changeWorkingDirectory("./tomcat/webapps/ssmemo/resources/images");

            if (!client.changeWorkingDirectory("./" + dName)) {
                client.makeDirectory("./" + dName);
                client.changeWorkingDirectory("./" + dName);
            }

            client.storeFile(name, inputStream);
            Log.d("FTP", "FTP Success : " + name);

        } catch (Exception e) {
            Log.d("FTP", "SaveToServer.SaveImageToServer() : Connect Error!");
        } finally {
            try {
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                Log.d("FTP", "SaveToServer.SaveImageToServer() : Disconnect Error!");
            }

        }
    }
}
