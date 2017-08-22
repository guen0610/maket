package com.example.guen.maket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";
    private Handler mHandler;
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
        // ... (Add other message types here as needed.)
    }
    ConnectedThread mConnectedThread;
    Button mButton;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if (isConnected) {
                    byte[] b = "led".getBytes();
                    mConnectedThread.write(b);
                }
            }
        });


        Log.d("hey", "jude");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            Context context = getApplicationContext();
            CharSequence text = "Bluetooth not supported on this Device!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            toastThread.start();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Utils.REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device: pairedDevices){
                String deviceName = device.getName();
                Log.d("maketa", deviceName);
                if(deviceName.equals("HC-06")){
                    mBluetoothDevice = device;
                    Log.d("maketa", "Detected");
                    ConnectThread connection = new ConnectThread(mBluetoothDevice);
                    connection.start();
                }
            }
        }



    }

    Thread toastThread = new Thread(){
        @Override
        public void run() {
            try{
                Thread.sleep(3000);
                MainActivity.this.finish();
                System.exit(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            Log.d("maketa", "Connecting...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                BluetoothSocket tmp = null;

            }
            catch(Exception e){
                Log.e("maketa", "Socket's create() method failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;



            Log.d("maketa", device.getUuids()[0].getUuid().toString());
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.

                tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                //tmp =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
            } catch (IOException e) {
                Log.d("maketa", "Socket's create() method failed");
                try {
                    tmp = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                }
                catch(Exception ex){
                    Log.d("maketa", "Socket's create() method failed2");
                }
            }
            mmSocket = tmp;
            Log.d("maketa", "Socket created");
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.d("maketa", "Fucking connected");
                isConnected = true;
                mConnectedThread = new ConnectedThread(mmSocket);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.d("maketa", "Cant connect");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("maketa", "Could not close the client socket", closeException);
                    //Log.e("","trying fallback...");

                    //mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    //mmSocket.connect();

                    Log.e("","Connected");
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("maketa", "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends  Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e)
            {
                Log.e("maketa", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
                Log.e("maketa", "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d("maketa", "Input stream was disconnected", e);
                    break;
                }
            }

        }

        //@Override
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                //Message writtenMsg = mHandler.obtainMessage(
                //        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                //writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.d("maketa", "Error occurred when sending data");

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }

        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("maketa", "Could not close the connect socket", e);

            }
        }

    }
}