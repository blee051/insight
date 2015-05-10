package com.insight.insight.sensors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import com.insight.insight.common.Setting;
import com.insight.insight.core.DataAcquisitor;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.data.SemanticTempCSVUtil;

import java.util.Date;

/**
 * Created by CM on 2/22/15.
 */
public class BatterySensor extends Service {
    IntentFilter mIntentFilter;
    BatteryReceiver batteryReceiver;
    DataAcquisitor mDataBuffer;
    DataAcquisitor mSA_batteryBuffer;
    private final String TAG = this.getClass().getSimpleName();
    private int lastVal;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            this.registerReceiver(batteryReceiver, mIntentFilter);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        mDataBuffer = new DataAcquisitor(this, this.getClass().getSimpleName());
        mSA_batteryBuffer = new DataAcquisitor(this, "SA/BatterySensor");

    }

    @Override
    public void onDestroy() {
        mDataBuffer.flush(true);
        mSA_batteryBuffer.flush(true);
        unregisterReceiver(batteryReceiver);
        super.onDestroy();

    }

    /* This class receives updates from the System
       If the level is a multiple of 5, write to file
     */
    private class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent batteryStatus) {
            if (batteryStatus.getAction().equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (level == lastVal) {
                    return;
                }
                lastVal = level;

                boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                        (status == BatteryManager.BATTERY_STATUS_FULL);

                if (level % 5 == 0) {
                    //store in buff
                    Log.d(TAG, "Level:" + level);
                    Log.d(TAG, "Charging:" + isCharging);
                    String encoded = JSONUtil.encodeBattery(level, isCharging, new Date());
                    mDataBuffer.insert(encoded, true, Setting.bufferMaxSize);
                    mDataBuffer.flush(true);

                    String encoded_SA = SemanticTempCSVUtil.encodeBattery(level, isCharging, new Date());
                    mSA_batteryBuffer.insert(encoded_SA, true, Setting.bufferMaxSize);
                    mSA_batteryBuffer.flush(true);
                }

            }

        }
    }
}
