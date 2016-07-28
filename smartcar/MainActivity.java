package com.example.maor.smartcar;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Vibrator;

import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Bluetooth BT;
    private static String address;

    ImageButton driveForwardBtn,driveBackwardBtn,turnLeftBtn,turnRightBtn,driveLeftBtn,driveRightBtn,reverseLeftBtn,reverseRightBtn ;
    Button btnExecute,drawPathBtn;
    EditText customCommand;
    Vibrator vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BT = Bluetooth.getInstance(); //initial the bt
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (address != null)
        {
            BT.Connect(address);
            if(!BT.isConnected())
            {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        customCommand = (EditText)findViewById(R.id.customCommandeditText);

        drawPathBtn = (Button)findViewById(R.id.drawPathBtn);

        driveForwardBtn = (ImageButton)findViewById(R.id.driveForward_imgBtn);
        driveBackwardBtn = (ImageButton)findViewById(R.id.driveBackward_imgBtn);
        turnLeftBtn = (ImageButton)findViewById(R.id.turnLeft_imgBtn);
        turnRightBtn = (ImageButton)findViewById(R.id.turnRight_imgBtn);

        driveLeftBtn = (ImageButton)findViewById(R.id.driveLeft_imgBtn);
        driveRightBtn = (ImageButton)findViewById(R.id.driveRight_imgBtn);
        reverseLeftBtn = (ImageButton)findViewById(R.id.reverseLeft_imgBtn);
        reverseRightBtn = (ImageButton)findViewById(R.id.reverseRight_imgBtn);

        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        btnExecute = (Button) findViewById(R.id.btnExecute);

         btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BT.getAddress().isEmpty() || !BT.isConnected())
                {
                    //Toast.makeText(getBaseContext(),"BT not connected!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(coordinatorLayoutView, "Error! Couldn't Find Bluetooth Device!", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                BT.SendBT(customCommand.getText()+"~");
                vibe.vibrate(30);
                Snackbar.make(coordinatorLayoutView, "Executed: " + customCommand.getText(), Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        drawPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PathDrawActivity.class);
                startActivity(i);

            }
        });



        driveForwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        BT.SendBT("forward~");
                        vibe.vibrate(30);
                }

                else if(event.getAction() == MotionEvent.ACTION_UP){
                        BT.SendBT("stop~");
                }

                return true;
            }
        });
        driveBackwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("backwards~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        turnLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("tleft~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        turnRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("tright~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        driveLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("dleft~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        driveRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("dright~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        reverseLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("rleft~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });
        reverseRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    BT.SendBT("rright~");
                    vibe.vibrate(30);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BT.SendBT("stop~");
                }
                return true;
            }
        });


    }
}
