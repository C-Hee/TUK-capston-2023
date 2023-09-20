package com.example.gyroapplication;

import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean ThreadFlag = true;

    private String result = "";
    private String userId = "";

    TextView contextText;
    TextView indicatorText;

    Handler handler = new Handler();

    public ConnectedThread(BluetoothSocket socket, TextView contextText, TextView indicatorText, String id) {
        mmSocket = socket;
        this.contextText = contextText;
        this.indicatorText = indicatorText;
        userId = id;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getResult() {
        return result;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs

        while (ThreadFlag) {
            try {
                // Read from the InputStream
                bytes = mmInStream.available();
                if (bytes != 0) {
                    buffer = new byte[1024];
                    SystemClock.sleep(10); //pause and wait for rest of data. Adjust this depending on your sending speed.
                    bytes = mmInStream.available(); // how many bytes are ready to be read?
                    bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read


                    byte[] slice = Arrays.copyOfRange(buffer, 0, bytes);

                    result = new String(slice);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("Telechips", result);
                            String[] splitedResult = result.split(",");
                            if(splitedResult.length != 3)
                            {
                                return;
                            }
                            int angle = Integer.parseInt(splitedResult[0]);
                            int agv = Integer.parseInt(splitedResult[1]);
                            int status = Integer.parseInt(splitedResult[2]);

                            String stat;
                            switch(status){

                                case 1:
                                    stat="경고";
                                    break;
                                case 2:
                                    stat="위험";
                                    break;
                                default:
                                    stat="정상";
                                    break;
                            }

                            String Text = "탐지 각도 : " + angle + "\n" +
                                          "평균 탐지 각도 : " + agv + "\n" +
                                          "상태 : " + stat;
                            contextText.setText(Text);
                            if(status == 1)
                            {
                                indicatorText.setBackgroundColor(Color.YELLOW);
                            }
                            else if(status == 2)
                            {
                                indicatorText.setBackgroundColor(Color.RED);
                            }
                            else
                            {
                                indicatorText.setBackgroundColor(Color.GREEN);
                            }

                            new Thread(() -> {
                                JSONObject updateParam = new JSONObject();
                                JSONObject postParam = new JSONObject();
                                try {
                                    postParam.put("id", userId);
                                    postParam.put("data", result.split(",")[0]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                PostManager postManager = new PostManager("138.2.126.137");
                                        //("10.0.2.2:8080");
                                JSONObject postRet = postManager.sendPost(postParam.toString(), "update/gyro");
                            }).start();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();

                break;
            }
            if (Thread.currentThread().isInterrupted()) {
                Log.d("ThreadStatus", "Thread interrupted");
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte input) {
        byte bytes[] = {input, '\n'};        //converts entered String into bytes
        try {
            mmOutStream.write(bytes);
            Log.e("success","success write");
        } catch (IOException e) {
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            Log.e("Test","Socket close success");
            mmInStream.close();
            mmOutStream.close();
            mmSocket.close();
            ThreadFlag = false;
            this.interrupt();
        } catch (IOException e) {
        }
    }

    public boolean isOpen(){
        return mmSocket.isConnected();
    }
}