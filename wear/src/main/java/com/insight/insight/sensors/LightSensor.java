package com.insight.insight.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.insight.insight.common.Setting;
import com.insight.insight.core.DataAcquisitor;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.data.SemanticTempCSVUtil;

import java.util.Date;

/**
 * Created by CM
 * Modified by MN
 */

/**
 * Class will take 3 lux readings every SensorConstant.LIGHT_SENSOR_INTERVAL
 * and write to file
 */

public class LightSensor extends Service implements SensorEventListener {
    private static final String LOG_TAG = LightSensor.class.getSimpleName();
    private Sensor mLight;
    private SensorManager mSensorManager;
    int count; // store number of samples
    float totalSum; // store sum of 3 sampling to get avg value by davide to 3 after 3rd sample
    private DataAcquisitor mDataBuffer;
    private DataAcquisitor mSA_lightBuffer;

    public LightSensor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        count = 0;
        totalSum = 0f;
        mDataBuffer = new DataAcquisitor(this, Setting.dataFolderName_LightSensor);
        mSA_lightBuffer = new DataAcquisitor(this, "SA/" + Setting.dataFolderName_LightSensor);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Log.d(LOG_TAG, "Light sensor started");
        Log.d(LOG_TAG, "Light sensor started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //SensorDelayNormal is 200,000 ms
        mSensorManager.registerListener(LightSensor.this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        new SensorEventLoggerTask().execute(event);
        mSensorManager.unregisterListener(this);
    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            float lux = event.values[0];
            totalSum += lux;
            count++;
            Log.d(LOG_TAG, "Sample count:" + count + ", lux:" + lux + ", total:" + totalSum);

            if (count >= Setting.LIGHT_SAMPLE_AMNT) {
                Date date = new Date();
                float avg = totalSum / count;

                //Encode the lux value and date
                String encoded = JSONUtil.encodeLight(avg, date);
                Log.d(LOG_TAG, encoded);

                //add encoded string to buffer
                mDataBuffer.insert(encoded, true, 1); // 1 for BufferMaxSize causes to flush Buffer automatically after inserting value

                String encoded_SA = SemanticTempCSVUtil.encodeLight(avg, date);
                mSA_lightBuffer.insert(encoded_SA, true, 1); // 1 for BufferMaxSize causes to flush Buffer automatically after inserting value

                totalSum = 0;
                count = 0;

                // stop the service. The service will run after 15min by ServiceMonitor class(AlarmService)
                stopSelf();

            } else {
                try {
                    //sleep current thread for about 30sec to get a new sample
                    Thread.sleep(Setting.LIGHT_SENSOR_SAMPLE_INTERVAL);

                    // register a listener to waiting for onSensorChanged() event
                    mSensorManager.registerListener(LightSensor.this, mLight, SensorManager.SENSOR_DELAY_FASTEST);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        //Unregister the listener
        mSensorManager.unregisterListener(this);
        Log.d(LOG_TAG, "Light sensor stopped");
        super.onDestroy();
    }
}