package com.example.send;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAINACTIVITY";
    BluetoothAdapter bluetoothAdapter;

    List<String> devices = new ArrayList<>();
    ListView list;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.list,devices);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter==null){
            Log.e(TAG,"bluetooth not available");
            finish();
        }
        else {

            if (!bluetoothAdapter.isEnabled()) {
                Intent enablebt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enablebt, 1);
            }

            send();

        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,SendData.class);
                intent.putExtra("name",devices.get(i));
                startActivity(intent);
            }
        });
    }

    private void send() {

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        send();
    }

    @Override
    protected void onStart() {
        super.onStart();
        send();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode==RESULT_OK) {
        }
        else if (resultCode==RESULT_CANCELED){
            finish();
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Log.d(TAG,"ON");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                Log.d(TAG,"OFF");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(!devices.contains(device.getName()))
                    adapter.add(device.getName());

                Log.e(TAG,devices.toString());
                Log.w(TAG,"Device Found : " + device.getName() );
            }
        }
    };





}

