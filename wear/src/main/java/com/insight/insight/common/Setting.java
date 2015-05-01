package com.insight.insight.common;

import java.text.SimpleDateFormat;
/*
    Created by CM
    Modified by MN
*/

//TODO 4m Reza: static file names should be converted to CAPITAL letters
public final class Setting {

    public static final long SAVE_FILE_WAIT_INTERVAL = 10000L; // 10 seconds
    //Internal File Directory is appended to this
    public static final String LOG_FOLDER = "insight";
    public static final String APP_FOLDER = "insight";

    public static final int bufferMaxSize = 10; // default: 10

    public static final int linksButtonCount = 7;

    public static final String dataFolderName_Battery = "BatterySensor";
    public static final String dataFolderName_Bluetooth = "Bluetooth";
    public static final String dataFolderName_Notifications = "Notif";
    public static final String dataFolderName_HeartRate = "HeartRate";
    public static final String dataFolderName_LightSensor = "LightSensor";
    public static final String dataFolderName_ActivFit = "ActivFit";
    public static final String dataFolderName_Temp = "Temp";

    public static final SimpleDateFormat filenameFormat = new SimpleDateFormat("M-d-yyyy");
    public static final SimpleDateFormat timestampFormat = new SimpleDateFormat("E MMM d HH:mm:ss zzz yyyy"); // e.g. 'Wed Mar 04 00:03:56 GMT+01:00 2015'


    // Sensors Constants
    public static long ACCELEROMETER_LOG_INTERVAL = 300000L;  //5 minutes

    public static long GYROSCOPE_LOG_INTERVAL = 300000L;   //5 minutes

    public static long APP_LOG_INTERVAL = 10000L;
    public static long APP_LOG_INTERVAL_2 = 300000L;

    public static long BATTERY_LOG_INTERVAL = 60000L; // 1 min // 600000L;  //10 minutes

    public static long BT_LOG_INTERVAL = 600000L; //10 minutes

    public static long WALKDETECTION_INTERVAL = 30000L; //30 seconds

    public static long LIGHT_SENSOR_INTERVAL = 600000L; // 10 minutes
    public static long LIGHT_SENSOR_SAMPLE_INTERVAL = 30000L; // 30 Seconds
    public static int LIGHT_SAMPLE_AMNT = 3;
}