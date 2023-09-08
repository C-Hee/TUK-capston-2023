package com.example.gyroapplication;


import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PostManager
{
        private String ip;

        public PostManager(String ip) {
                this.ip = ip;
        }

        public JSONObject sendPost(String params, String path)
        {
                try {
                        URL url = new URL("http://" + ip + ":3004/" + path);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        connection.setRequestProperty("Accept", "application/json; charset=utf-8");
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setReadTimeout(15000);
                        connection.setConnectTimeout(1500);

                        // Send Data
                        byte[] outputInBytes = params.getBytes(StandardCharsets.UTF_8);
                        OutputStream os = connection.getOutputStream();
                        os.write(outputInBytes);
                        os.close();

                        // Recv Data
                        InputStream is = connection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line;
                        StringBuffer response = new StringBuffer();
                        while ((line = br.readLine()) != null) {
                                response.append(line);
                                response.append('\r');
                        }
                        br.close();

                        String res = response.toString();
                        res = res.trim();
                        Log.i("Telechips", res);

                        JSONObject jsonObject = new JSONObject(res);
                        return jsonObject;
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                        return null;
                }
        }
}