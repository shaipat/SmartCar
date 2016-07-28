package com.example.maor.smartcar;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.os.Vibrator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.RunnableFuture;



public class PathDrawActivity extends Activity implements OnTouchListener {
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    float lastX = -10 , lastY = -10;
    float newX = 0 , newY = 0 ;
    int lastYdir = 0 , lastXdir = 0;
    float dotsToSecFactor = 0.0025f;
    Button resetBtn,goPathBtn;
    Thread muThread;
    ArrayList<String> commandsList;
    ArrayList<Line> lines;
    Bluetooth BT;
    TextView tvInstructions,distanceMeterText;
    boolean paint_initialized_Flag = false;
    float downx = 0, downy = 0, upx = 0, upy = 0;
    PathStore storage;
    Vibrator vibe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_draw);
        storage = PathStore.getInstance();

        imageView = (ImageView)findViewById(R.id.PathimageView);
     //   Display currentDisplay = getWindowManager().getDefaultDisplay();

        imageView.setOnTouchListener(this);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        distanceMeterText = (TextView) findViewById(R.id.distanceMeterTview);
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        resetBtn = (Button)findViewById(R.id.resetPathBtn);
        goPathBtn = (Button)findViewById(R.id.goPathBtn);
        commandsList = new ArrayList<String>();
        lines = storage.lines;
        commandsList = storage.commandslist;
        BT = Bluetooth.getInstance(); //initial the bt
        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);


        BT.Set_BluetoothHandle_Function(new Runnable() {
            @Override
            public void run() {
                String msg = BT.GetLastMessage();
                if(msg.contains("ok"))
                {
                  //  Toast.makeText(getBaseContext(),msg,Toast.LENGTH_SHORT).show();
                    if(msg.length()>2)
                    {
                        int idx = Integer.parseInt(msg.substring(2));
                        Path_PartCompleted(idx);
                    }

                }

                else if(msg.contains("fail"))
                {
                   // Toast.makeText(getBaseContext(),msg,Toast.LENGTH_SHORT).show();
                    if(msg.length()>4)
                    {
                        vibe.vibrate(100);
                        int idx = Integer.parseInt(msg.substring(4));
                        Path_ObstacleFound(idx);
                    }

                }

            }
        });


        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandsList.clear();
                lines.clear();
                canvas.drawColor(Color.WHITE);
                imageView.invalidate();
                restPaintVar();
                tvInstructions.setText("");
                Snackbar.make(coordinatorLayoutView, "Canvas Reset!", Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        goPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    vibe.vibrate(70);
                    if(BT.getAddress().isEmpty() || !BT.isConnected())
                    {
                        //Toast.makeText(getBaseContext(),"BT not connected!",Toast.LENGTH_SHORT).show();
                        Snackbar.make(coordinatorLayoutView, "Error! Couldn't Find Bluetooth Device!", Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                    Path_PaintBlack();
                    int autoDriveIndex = commandsList.indexOf("autodrive~");
                    if(autoDriveIndex != -1)
                        commandsList.remove(autoDriveIndex);
                    commandsList.add("autodrive~");

                    if(commandsList.size()>0) //commit first command immediately
                    {
                        BT.SendBT(commandsList.get(0));
                        try {Thread.sleep(500);}
                        catch (InterruptedException e) {e.printStackTrace();}
                   // todo: think about how to solve the counter reset at mbed when "autodrive" send first time
                    //    BT.SendBT("autodrive~");
                   //     try {Thread.sleep(400);}
                    //    catch (InterruptedException e) {e.printStackTrace();}
                    }
                    for (int i = 1; i < commandsList.size(); i++) {
                        String x = commandsList.get(i);
                        BT.SendBT(x);
                        //  Log.d("DrawLine",x);
                        try {Thread.sleep(400);}
                        catch (InterruptedException e) {e.printStackTrace();}
                    }

                    Snackbar.make(coordinatorLayoutView, "Path was sent successfully!", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });

    }

    void restPaintVar()
    {
        lastX = -10;
        lastY = -10;
        newX = 0 ;
        newY = 0 ;
        lastYdir = 0;
        lastXdir = 0;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!paint_initialized_Flag)
        {
            PreparePaint();
            lines = PathStore.getInstance().lines;
            commandsList = PathStore.getInstance().commandslist;
            if(lines.size()>0)
            {
                lastX = lines.get(lines.size()-1).getEnd().x();
                lastY = lines.get(lines.size()-1).getEnd().y();
                Print_Path_To_Screen();
            }
            Path_PaintBlack();
        }

    }

    private void PreparePaint()
    {

        float dw = imageView.getMeasuredWidth();
        float dh = imageView.getHeight();

        bitmap = Bitmap.createBitmap((int) dw, (int) dh,
                Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        imageView.setImageBitmap(bitmap);
        paint_initialized_Flag = true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        paint.setColor(Color.BLACK);
        float devX ;
        float devY ;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(lastX == -10 && lastY == -10)
                {
                    lastX = event.getX();
                    lastY = downy = event.getY();
                }
                //  downx = event.getX();
                //  downy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                newX = event.getX();
                newY = event.getY();
                devX = Math.abs(lastX - newX);
                devY = Math.abs(lastY - newY);
                if(devX > devY)
                    distanceMeterText.setText("Drive "+String.format("%.2f",devX*dotsToSecFactor)+" sec");
                else
                    distanceMeterText.setText("Drive "+String.format("%.2f",devY*dotsToSecFactor)+" sec");

                break;
            case MotionEvent.ACTION_UP:
                newX = event.getX();
                newY = event.getY();
                distanceMeterText.setText("");

                // check which direction to draw straight line
                devX = Math.abs(lastX - newX);
                devY = Math.abs(lastY - newY);

                Log.d("DrawLine","up X="+newX);
                Log.d("DrawLine","up Y="+newY);


                if(devX < devY) // paint on Y axis
                {
                    if(lastY - newY >0 ) // Y go up
                    {
                        lastYdir = 1;
                        if(lastXdir ==1)
                            Left(devY);
                        else if (lastXdir == -1)
                            Right(devY);
                        else
                            Straight(devY);

                        lastXdir = 0;


                    }
                    else // Y go down
                    {
                        lastYdir = -1;
                        if(lastXdir ==1)
                            Right(devY);
                        else if (lastXdir == -1)
                            Left(devY);
                        else
                            Straight(devY);
                        lastXdir = 0;

                    }
                    canvas.drawLine(lastX, lastY, lastX, newY, paint);
                  //  Line line = new Line(lastX, lastY, lastX, newY);
                    lines.add(new Line(lastX, lastY, lastX, newY));
                    lastY = newY;
                }
                else // paint on X axis
                {
                    if(lastX - newX >0 ) // X go left
                    {
                        lastXdir = -1;
                        if(lastYdir ==1)
                            Left(devX);
                        else if (lastYdir == -1)
                            Right(devX);
                        else
                            Straight(devX);
                        lastYdir = 0;

                    }
                    else // X go Right
                    {
                        lastXdir = 1; // X go Right
                        if(lastYdir == -1)
                            Left(devX);
                        else if (lastYdir == 1)
                            Right(devX);
                        else
                            Straight(devX);
                        lastYdir = 0;
                    }

                    canvas.drawLine(lastX, lastY, newX, lastY, paint);
                    lines.add(new Line(lastX,lastY,newX,lastY));
                    lastX = newX;
                }
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    void Left(float dots)
    {
        dots *= dotsToSecFactor;
       // Toast.makeText(getBaseContext(),"Left " + dots + "dots",Toast.LENGTH_SHORT).show();
        commandsList.add("left#"+ String.format("%.2f",dots) +"~");
        tvInstructions.setText(tvInstructions.getText() + "\n" + "Left "+ String.format("%.2f",dots) + " sec"); // show real instruction


    }

    void Right(float dots)
    {
        dots *= dotsToSecFactor;
       // Toast.makeText(getBaseContext(),"Right " + dots + "dots",Toast.LENGTH_SHORT).show();
        commandsList.add("right#"+String.format("%.2f",dots)+"~");
        tvInstructions.setText(tvInstructions.getText() + "\n" + "Right "+ String.format("%.2f",dots) + " sec"); // show real instruction

    }

    void Straight(float dots)
    {
        dots *= dotsToSecFactor;
      //  Toast.makeText(getBaseContext(),"Straight " + dots + "dots",Toast.LENGTH_SHORT).show();
        commandsList.add("forward#"+String.format("%.2f",dots)+"~");
        tvInstructions.setText(tvInstructions.getText() + "\n" + "Forward "+ String.format("%.2f",dots) + " sec"); // show real instruction
    }


    void DrawRedLine(float x1,float y1,float x2,float y2)
    {
        paint.setColor(Color.RED);
        canvas.drawLine(x1, y1, x2, y2, paint);
        imageView.invalidate();
    }

    void DrawGreenLine(float x1,float y1,float x2,float y2)
    {
        paint.setColor(Color.GREEN);
        canvas.drawLine(x1, y1, x2, y2, paint);
        imageView.invalidate();
    }
    void DrawBlackLine(float x1,float y1,float x2,float y2)
    {
        paint.setColor(Color.BLACK);
        canvas.drawLine(x1, y1, x2, y2, paint);

    }

    void Path_ObstacleFound(int partIndex)
    {
        if(lines.size()< partIndex+1)
            return;

        Line l = lines.get(partIndex);
        DrawRedLine(l.getStart().x(),l.getStart().y(),l.getEnd().x(),l.getEnd().y());
    }

    void Path_PartCompleted(int partIndex)
    {
        if(lines.size()< partIndex+1)
            return;

        Line l = lines.get(partIndex);
        DrawGreenLine(l.getStart().x(),l.getStart().y(),l.getEnd().x(),l.getEnd().y());
    }

    void Path_PaintBlack() // used for re-paint the path in black color (when using "Go" for second time)
    {
        for(int i=0 ; i<lines.size() ; i++)
        {
            Line l = lines.get(i);
            DrawBlackLine(l.getStart().x(),l.getStart().y(),l.getEnd().x(),l.getEnd().y());
        }
        imageView.invalidate();
    }

    void Print_Path_To_Screen()
    {
        tvInstructions = (TextView)findViewById(R.id.tvInstructions);

        // remove "autodrive" command in case we wanna add commands and press "go" again
        int autoDriveIndex = commandsList.indexOf("autodrive~");
        if(autoDriveIndex != -1)
            commandsList.remove(autoDriveIndex);

        for(int i=0 ; i<commandsList.size() ; i++)
        {
            tvInstructions.setText(tvInstructions.getText() + "\n" +commandsList.get(i));
        }



    }


}