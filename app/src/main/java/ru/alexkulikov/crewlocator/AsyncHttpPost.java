package ru.alexkulikov.crewlocator;

/**
 * Created by BigWats on 02.07.2016.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;


public class AsyncHttpPost extends AsyncTask<String,String,String>
{
    private Activity source;

    public AsyncHttpPost(Activity source) {
        this.source = source;
    }

    @Override
    protected String doInBackground(String... params) {

        BufferedWriter writer = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL("https://dzzzr-bot.azurewebsites.net/api/message/gps/");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            outputStream = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            StringBuilder paramBuilder = new StringBuilder();

            paramBuilder.append("ClientID");
            paramBuilder.append("=");
            paramBuilder.append(params[0]).append("&");

            paramBuilder.append("Date");
            paramBuilder.append("=");
            paramBuilder.append(params[1]).append("&");

            paramBuilder.append("Latitude");
            paramBuilder.append("=");
            paramBuilder.append(params[2]).append("&");

            paramBuilder.append("Longitude");
            paramBuilder.append("=");
            paramBuilder.append(params[3]);

            writer.write(paramBuilder.toString());
            writer.flush();

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                Toast.makeText(source, "При отправке координат произошла ошибка", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {

                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {

                }
            }
        }

        return null;
    }
}
