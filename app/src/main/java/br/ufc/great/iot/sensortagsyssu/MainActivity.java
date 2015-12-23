package br.ufc.great.iot.sensortagsyssu;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import br.ufc.great.sensortag.SensorTag;
import br.ufc.great.sensortag.callbacks.SensorTagListener;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.Tuple;

public class MainActivity extends AppCompatActivity implements SensorTagListener{

    private SensorTag mTag;

    private SyssuManager mSyssu;

    private int messageCount = 0;

    private double caliX;
    private double caliY;
    private double caliZ;

    private double lastX;
    private double lastY;
    private double lastZ;

    private double threshold = 1;

    private boolean presence;

    private TextView tvAccelX;
    private TextView tvAccelY;
    private TextView tvAccelZ;
    private TextView tvPresence;
    private TextView tvIndicator;

    private EditText etThreshold;
    private Button btThreshold;

    private Button btCalibrate;

    private PresenceTimer mPresenceTimer;

    private static final String ID = "sensortagapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            createFileOnDevice(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvAccelX = (TextView) findViewById(R.id.tv_accel_x);
        tvAccelY = (TextView) findViewById(R.id.tv_accel_y);
        tvAccelZ = (TextView) findViewById(R.id.tv_accel_z);
        tvIndicator = (TextView) findViewById(R.id.tv_indicator);
        tvPresence = (TextView) findViewById(R.id.tv_presence);

        etThreshold = (EditText) findViewById(R.id.et_threshold);
        btThreshold = (Button) findViewById(R.id.bt_threshold);
        btThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threshold = Double.valueOf(etThreshold.getText().toString());
            }
        });


        btCalibrate = (Button) findViewById(R.id.bt_calibrate);
        btCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caliX = lastX;
                caliY = lastY;
                caliZ = lastZ;
            }
        });

        mPresenceTimer = new PresenceTimer();
        mPresenceTimer.start();

        mSyssu = SyssuManager.getInstance(this);
        mSyssu.start();

        mTag = new SensorTag();
        mTag.setPeriod(1000);
        mTag.start(this, this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenceTimer.stopThread();
        mTag.stop();
    }

    @Override
    public void onSensorTagUpdate(final String s) {
        Log.d("Sensor Tag", s);

        try {
            JSONObject accel = new JSONObject(s).getJSONObject("AccelData");
            lastX = accel.getDouble("X");
            lastY = accel.getDouble("Y");
            lastZ = accel.getDouble("Z");
            calculePresence();
            updateUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Tuple t = new Tuple();
        t.addField("id", ID);
        t.addField("presence", "" + presence);
        t.addField("messageCount", "" + messageCount);

        log("id=" + ID + ",presence="+presence + ",timestamp=" + System.currentTimeMillis() + ",messagecount=" + messageCount);

        mSyssu.put(t, Provider.ADHOC);
        messageCount++;
    }

    private void log(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeToFile(s);
            }
        }).start();
    }

    private void calculePresence() {
        double x = lastX - caliX;
        double y = lastY - caliY;
        double z = lastZ - caliZ;

        final double sum = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvIndicator.setText("" + sum);
            }
        });

        if(sum > threshold){
            mPresenceTimer.reset();
        }
    }

    private void updateUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAccelX.setText("Accel X: " + (lastX - caliX));
                tvAccelY.setText("Accel Y: " + (lastY - caliY));
                tvAccelZ.setText("Accel Z: " + (lastZ - caliZ));

                if(presence){
                    tvPresence.setText("Presence Detected: TRUE");
                }
                else{
                    tvPresence.setText("Presence Detected: FALSE");
                }
            }
        });
    }

    private class PresenceTimer extends Thread{

        private static final int MAX_TIME = 30000;
        private int elapsedTime = MAX_TIME;

        private boolean running = false;

        @Override
        public void run() {

            running = true;

            while(running){
                while(elapsedTime < MAX_TIME){
                    if(!presence){
                        presence = true;
                        updateUI();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    elapsedTime += 1000;
                }

                if(presence){
                    presence = false;
                    updateUI();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        public void reset() {
            elapsedTime = 0;
        }

        public void stopThread(){
            running = false;
        }
    }

    public static BufferedWriter out;

    private void createFileOnDevice(Boolean append) throws IOException {
                /*
                 * Function to initially create the log file and it also writes the time of creation to file.
                 */
        File Root = Environment.getExternalStorageDirectory();
        if(Root.canWrite()){
            File  LogFile = new File(Root, "Log.txt");
            FileWriter LogWriter = new FileWriter(LogFile, append);
            out = new BufferedWriter(LogWriter);
            Date date = new Date();
            out.write("Logged at" + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            out.flush();

        }
    }

    public void writeToFile(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
