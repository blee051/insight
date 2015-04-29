package com.insight.insight.sensors;

/* Created by CM
* Modified by MN
*/

//TODO 4m Reza: Move this to Common.Settings class 
public final class SensorConstants {

    public static long ACCELEROMETER_LOG_INTERVAL = 300000L;  //5 minutes

    public static long GYROSCOPE_LOG_INTERVAL = 300000L;   //5 minutes

    public static long APP_LOG_INTERVAL = 10000L;
    public static long APP_LOG_INTERVAL_2 = 300000L;

    public static long BATTERY_LOG_INTERVAL =  60000L; // 1 min // 600000L;  //10 minutes

    public static long BT_LOG_INTERVAL = 600000L; //10 minutes

    public static long WALKDETECTION_INTERVAL = 30000L; //30 seconds

    public static long LIGHT_SENSOR_INTERVAL = 600000L; // 10 minutes

    public static long LIGHT_SENSOR_SAMPLE_INTERVAL = 30000L; // 30 Seconds

    public static int LIGHT_SAMPLE_AMNT = 3;
}
