package com.insight.insight.data;

/**
 * Created by CM on 3/8/15.
 */
public class SemanticAbsUtil {

    public static String getSemanticActivity(String activity) {
        return activity;
    }

    public static String getSemanticSteps(int steps) {
        if (steps < 1000) {
            return "very inactive";
        }
        if (steps >= 1000 && steps < 5000) {
            return "inactive";
        }
        if (steps >= 5000 && steps < 10000) {
            return "active";
        } else {  //(steps > 10000){
            return "very active";
        }

    }

    public static String getSemanticHeartRate(float heartRate) {
        if (heartRate < 60) {
            return "low";

        }
        if (heartRate >= 60 && heartRate < 90) {
            return "average";
        } else {
            return "high";
        }
    }

    public static String getSemanticAppUsage(String appName) {
        //TODO Lookup app genre
        String appGenre = "";
        return appGenre;
    }

    public static String getSemanticBattery(int percent, boolean status) {
        int rounded = ((percent + 5) / 10) * 10;

        return rounded + "% Charging:" + status;
    }

    public static String getSemanticNotificaiton(String appName, String notificationTitle) {
        //TODO Lookup app genre

        String appGenre = "NA";
        return appGenre;
    }

    public static String getSemanticBluetooth(String status) {
        return status;
    }

    public static String getSemanticAmbientLight(float number) {
        if (number < 5) {
            return "dark";
        }
        if (number < 10000) {
            return "less bright";
        }
        if (number >= 10000 && number < 50000) {
            return "bright";
        } else {
            return "very bright";
        }
    }
}
