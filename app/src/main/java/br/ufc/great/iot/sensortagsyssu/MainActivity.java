package br.ufc.great.iot.sensortagsyssu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import br.ufc.great.sensortag.SensorTag;
import br.ufc.great.sensortag.callbacks.SensorTagListener;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.Tuple;

public class MainActivity extends AppCompatActivity implements SensorTagListener{

    private SensorTag mTag;

    private SyssuManager mSyssu;

    private static final String ID = "sensortagapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSyssu = SyssuManager.getInstance(this);
        mSyssu.start();

        mTag = new SensorTag();
        mTag.setPeriod(1000);
        mTag.start(this, this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTag.stop();
    }

    @Override
    public void onSensorTagUpdate(final String s) {
        Log.d("Sensor Tag", s);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Tuple t = new Tuple();
                t.addField("id", ID);
                t.addField("value", "hello");

                mSyssu.put(t, Provider.ADHOC);
            }
        }).start();

    }
}
