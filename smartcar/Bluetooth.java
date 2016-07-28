package com.example.maor.smartcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Bluetooth extends Activity {


    Handler bluetoothIn;
    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private Runnable BT_Message_Handle_Func;
    private ConnectedThread mConnectedThread;
    private String BT_Last_Message;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "";


    public static void setAddress(String address) {
        Bluetooth.address = address;
    }

    public static String getAddress() {
        return address;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    public String GetLastMessage()
    {
        return BT_Last_Message;
    }


    public void SendBT(String str)
    {
        if(mConnectedThread != null && address.length()!=0)
            mConnectedThread.write(str);
    }
    // Private constructor suppresses generation of a (public) default constructor
    private Bluetooth() {}

    private static class BluetoothHolder {
        private final static Bluetooth INSTANCE = new Bluetooth();
    }

    public static Bluetooth getInstance() {
        return BluetoothHolder.INSTANCE;
    }


    public void Connect(String newAddress)
    {
        address = newAddress;
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        //send data every interval
        final Handler handler = new Handler();

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        BT_Last_Message = recDataString.substring(0, endOfLineIndex);    // extract string
                        incomingBT_handle(); // call the right function that handle new msg
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                    }
                }
            }
        };
    }


    public void Disconnect()
    {
        try
        {
            if(btSocket != null)
                btSocket.close();
        } catch (IOException e) {
        }
    }

    public boolean isConnected()
    {
        return btSocket.isConnected();
    }

    private void incomingBT_handle()
    {
        // handle in the right function
        if(BT_Message_Handle_Func != null)
            BT_Message_Handle_Func.run();

    }

    public void Set_BluetoothHandle_Function(Runnable r)
    {
        BT_Message_Handle_Func = r;
    }


    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                //    Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                //  finish();

            }
        }
    }




}