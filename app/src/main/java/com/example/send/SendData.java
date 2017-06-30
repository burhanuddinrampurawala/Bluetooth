package com.example.send;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SendData extends AppCompatActivity {
    private static final String TAG = "MAINACTIVITY";
    private String name;
    private EditText text;
    private Button send;
    private BluetoothDevice myDevice = null;
    private BluetoothSocket mySocket = null;
    private OutputStream myOutputStream = null;
    private InputStream myInputStream = null;
    private BluetoothAdapter bluetoothAdapter;
    private View mProgressView;
    private View formView;
    private  View textView;
    private TextView receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        name = getIntent().getStringExtra("name");
        setTitle(name);
        text = (EditText) findViewById(R.id.text);
        send = (Button) findViewById(R.id.send);
        mProgressView = findViewById(R.id.progress);
        formView = findViewById(R.id.form);
        textView = findViewById(R.id.textView);
        receive = (TextView)findViewById(R.id.receive);

        showProgress(true);
        receiver();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });

    }



    private void receiver() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i(TAG,"name "+ name);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();

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
                if (myDevice == null){
                    Toast.makeText(getApplicationContext(),"Couldn't establish Bluetooth connection! Please try again.",Toast.LENGTH_LONG).show();
                    finish();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().equalsIgnoreCase(name)) {
                    myDevice = device;
                    bluetoothAdapter.cancelDiscovery();
                    Log.i(TAG,"device assigned " + device.getName());
                    bluetoothAdapter.cancelDiscovery();

                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                            try {
                                mySocket = myDevice.createRfcommSocketToServiceRecord(uuid);
                            } catch (IOException e) {
                                Log.e(TAG, e.toString());
                            }
                            try {
                                mySocket.connect();
                                Log.d(TAG, "socket connected");
                                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                try {
                                    Log.d(TAG, "trying fallback...");

                                    mySocket = (BluetoothSocket) myDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(myDevice, 1);
                                    mySocket.connect();

                                    Log.d(TAG, " socket Connected");
                                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                                } catch (Exception e2) {
                                    if(!mySocket.isConnected()){
                                        Log.e(TAG, "Couldn't establish Bluetooth connection!");
                                        finish();
                                    }
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            showProgress(false);
                        }
                    }.execute();
//                    try {
//                        myInputStream = mySocket.getInputStream();
//                        Log.d(TAG,"got input stream");
//                    }
//                    catch (IOException e){
//                        Log.d(TAG,e.toString());
                    //}

                    input();

                }
                Log.e(TAG,device.getName());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try {
//            myInputStream.close();
//        } catch (IOException e) {
//            Log.i(TAG,"Input stream was closed");
//        }
        if(myOutputStream != null){
            try {
                myOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG,e.toString());
            }
        }
    }

    private void send() {
        try {
            createSocket(text.getText().toString());
        } catch (IOException ioe) {
            Log.d("MAINACTIVITY", ioe.toString());
        }
        text.setText("");
    }

    private void createSocket(final String s )throws IOException {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    myOutputStream = mySocket.getOutputStream();
                    Log.d("TAG", "got output stream");
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }

                try {
                    // sending data to bluetooth
                    myOutputStream.write(s.getBytes());
                    Log.d(TAG, "wrote value " + s + " on serial out");
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }

                return null;
            }

        }.execute();
    }

    private void input() {
//        if(myInputStream != null){
//
//            new AsyncTask<Void,Void,Void>(){
//                String tmp;
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    int numBytes; // bytes returned from read()
//                    byte[] myBuffer;//buffer for received data
//
//                    // Keep listening to the InputStream until an exception occurs.
//                    while (true) {
//                        try {
//
//                            myBuffer = new byte[256];
//
//                            if (myInputStream.available() != 0) {
//                                // Read from the InputStream.
//                                numBytes = myInputStream.read(myBuffer);
//                                // Send the obtained bytes to the UI activity.
//                                tmp = new String(myBuffer, 0, numBytes);
//
//                                Log.d("input", tmp + " " + numBytes);
//                            }
//
//                        } catch (IOException e) {
//                            Log.d(TAG, e.toString());
//                            break;
//                        }
//                    }
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(Void aVoid) {
//                    if(tmp.length() != 0)
//                        receive.setText(tmp);
//                }
//            }.execute();
//        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formView.setVisibility(show ? View.GONE : View.VISIBLE);
            formView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
            textView.setVisibility(show ? View.VISIBLE : View.GONE);
            textView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    textView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            textView.setVisibility(show ? View.VISIBLE : View.GONE);
            formView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
