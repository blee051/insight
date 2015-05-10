package com.insight.insight.utils;

/**
 * Created by MN
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.insight.insight.alarm.AlarmReceiver;
import com.insight.insight.common.Setting;
import com.insight.insight.sensors.BatterySensor;
import com.insight.insight.sensors.LightSensor;

public class ServicesMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Send Data to Remote Server
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.setMidnightAlarmManager(context.getApplicationContext());

        // Battery Sensor
        context.startService(new Intent(context, BatterySensor.class));

        // AmbientLight Service
        if (FeatureCheck.hasLightFeature(context)) {
            AlarmManager scheduler = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent myIntent = new Intent(context.getApplicationContext(), LightSensor.class);
            PendingIntent scheduledIntent = PendingIntent.getService(context.getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            scheduler.cancel(scheduledIntent);
            scheduler.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Setting.LIGHT_SENSOR_INTERVAL, scheduledIntent);
        }
    }
}