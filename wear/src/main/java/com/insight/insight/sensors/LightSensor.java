package com.insight.insight.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.insight.insight.common.Setting;
import com.insight.insight.common.TempDebugger;
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
    private Handler mHandler;
    int count;
    float totalSum;

    private DataAcquisitor mDataBuffer;
    private DataAcquisitor mSA_lightBuffer;

    TempDebugger TempDebugger;

    public LightSensor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        TempDebugger = new TempDebugger(this);
        count = 0;
        totalSum = 0f;
        mDataBuffer = new DataAcquisitor(this, Setting.dataFolderName_LightSensor);
        mSA_lightBuffer = new DataAcquisitor(this, "SA/" + Setting.dataFolderName_LightSensor);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        HandlerThread ht = new HandlerThread("LightThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        mHandler = new Handler(ht.getLooper());
        mHandler.post(activateLightListener);
        Log.d(LOG_TAG, "Light sensor started");
        TempDebugger.Log("LightSensor", "Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Light sensor returns one value as LUX
        //taking 3 sample on every <SensorConstants.LIGHT_SENSOR_SAMPLE_INTERVAL> Milliseconds
        //and then calculate the average and insert to buffer
        //and sleep for <SensorConstants.LIGHT_SENSOR_INTERVAL> Milliseconds
        try {
            mSensorManager.unregisterListener(this);
            float lux = event.values[0];
            totalSum += lux;
            count++;
            Log.d(LOG_TAG, "Sample count:" + count + ", lux:" + lux + ", total:" + totalSum);
            TempDebugger.Log("LightSensor", "Sampled " + count);
            if (count >= Setting.LIGHT_SAMPLE_AMNT) {
                Date date = new Date();
                float avg = totalSum / count;

                //Encode the lux value and date
                String encoded = JSONUtil.encodeLight(avg, date);
                Log.d(LOG_TAG, encoded);

                //add encoded string to buffer
                mDataBuffer.insert(encoded, true, Setting.bufferMaxSize);
                mDataBuffer.flush(true);

                String encoded_SA = SemanticTempCSVUtil.encodeLight(avg, date);
                mSA_lightBuffer.insert(encoded_SA, true, Setting.bufferMaxSize);
                mSA_lightBuffer.flush(true);
                totalSum = 0;
                count = 0;

                mHandler.postDelayed(activateLightListener, Setting.LIGHT_SENSOR_INTERVAL);

            } else {
                mHandler.postDelayed(activateLightListener, Setting.LIGHT_SENSOR_SAMPLE_INTERVAL);

            }
        } catch (Exception e) {
            TempDebugger.Log("LightSensor", "Sampling ERROR " + e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        try {
            mDataBuffer.flush(true);
            mSA_lightBuffer.flush(true);
            mSensorManager.unregisterListener(this);
            Log.d(LOG_TAG, "Light sensor stopped");
            TempDebugger.Log("LightSensor", "Destroyed");
            TempDebugger.Vibrate(1);
            super.onDestroy();
            startService(new Intent(this, this.getClass()));
            TempDebugger.Log("LightSensor", "Trying to Start Service...");
        } catch (Exception e) {
            TempDebugger.Log("LightSensor", "Sampling ERROR " + e.getMessage());
        }
    }

    private Runnable activateLightListener = new Runnable() {
        @Override
        public void run() {
            try {
                mSensorManager.registerListener(LightSensor.this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
            } catch (Exception e) {
                TempDebugger.Log("LightSensor", "registerListener ERROR " + e.getMessage());
            }
        }
    };
}