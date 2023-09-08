package com.example.gyroapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class ReceiveActivity extends AppCompatActivity {

    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    TextView contextText;
    TextView indicatorText;
    Button btnSearch;
    Button btnGraph;
    ListView listView;
    BluetoothAdapter btAdapter;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread = null;

    Intent receiveActivityIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        receiveActivityIntent = getIntent();

        // Get permission
        String[] permission_list = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(ReceiveActivity.this, permission_list, 1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        contextText = findViewById(R.id.contextText);
        indicatorText = findViewById(R.id.indicatorText);
        btnSearch = findViewById(R.id.btn_search);
        btnGraph = findViewById(R.id.btn_graph);
        listView = findViewById(R.id.listview);

        // Show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        listView.setOnItemClickListener(new myOnItemClickListener());

        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                intent.putExtra("ID", receiveActivityIntent.getStringExtra("ID"));
                startActivity(intent);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void onClickButtonSearch(View view) {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                btArrayAdapter.clear();
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                if(deviceName != null)
                {
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if(!deviceAddressArray.contains(deviceHardwareAddress))
                    {
                        deviceAddressArray.add(deviceHardwareAddress);
                        btArrayAdapter.add(deviceName);
                        btArrayAdapter.notifyDataSetChanged();
                    }
                }

                Log.e("Telechips","change");
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(connectedThread != null){
            if(connectedThread.isOpen()){
                connectedThread.cancel();
                try {
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        finish();
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), "try to connect: " +btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

//            textStatus.setText("try...");

            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            // create & connect socket
            try {
                if(connectedThread != null) {
                    if (connectedThread.isOpen()) {
                        connectedThread.cancel();
//                        textStatus.setText("disconnected!");
                    }
                }
                if (btSocket != null) {
                    if (btSocket.isConnected()) {
                        btSocket.close();
//                        textStatus.setText("closed!");
                    }
                }

                btSocket = createBluetoothSocket(device);
                if (ActivityCompat.checkSelfPermission(ReceiveActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    /*ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            33);*/
                    return;
                }
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
//                textStatus.setText("connection failed!");
                e.printStackTrace();
            }

            // start bluetooth communication
            if (flag) {
//                textStatus.setText("connected to " + name);
                connectedThread = new ConnectedThread(btSocket, contextText, indicatorText,receiveActivityIntent.getStringExtra("ID"));
                connectedThread.start();
            }
            else{
                Toast.makeText(getApplicationContext(), "????", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e("TAG", "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        else return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}