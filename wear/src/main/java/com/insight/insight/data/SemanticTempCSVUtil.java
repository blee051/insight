package com.insight.insight.data;

import com.insight.insight.utils.NotificationParcel;

import java.util.Date;

/**
 * Created by CM on 3/10/15.
 */
public class SemanticTempCSVUtil {

    public static String encodeBattery(int percent, boolean charging, Date timeStamp) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("battery," + percent + "% Charging:" + charging + "," + timeStamp);
        encoded.append("," + SemanticAbsUtil.getSemanticBattery(percent, charging) + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(timeStamp));

        return encoded.toString();

    }

    public static String encodeLight(float lux, Date timeStamp) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("light," + lux + "," + timeStamp + ",");
        encoded.append(SemanticAbsUtil.getSemanticAmbientLight(lux) + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(timeStamp));
        return encoded.toString();

    }

    public static String encodedBT(String state, Date timestamp) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("bt," + state + "," + timestamp + ",");
        encoded.append(state + "," + TempGranUtil.getTemporalTimeStamp(timestamp));
        return encoded.toString();

    }

    public static String encodeStepActivity(Date startTime, int culmStepAmt, int stepDiff) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("activity(step)," + culmStepAmt + "," + startTime + ",");
        encoded.append(SemanticAbsUtil.getSemanticSteps(culmStepAmt) + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(startTime));

        return encoded.toString();
    }

    public static String encodeNotification(NotificationParcel in) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("notification," + in.PACKAGE_NAME + "," + new Date(in.POST_TIME) + ",");
        encoded.append(SemanticAbsUtil.getSemanticNotificaiton(in.PACKAGE_NAME, in.EXTRA_TITLE) + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(new Date(in.POST_TIME)));

        return encoded.toString();
    }

    public static String encodedHeartRate(float bpm, Date timeStamp) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("heartRate," + bpm + "," + timeStamp + ",");
        encoded.append(SemanticAbsUtil.getSemanticHeartRate(bpm) + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(timeStamp));

        return encoded.toString();
    }

    public static String encodedActivSegments(Date startTime, String activity, Integer duration) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("activity," + activity + "," + startTime + ",");
        encoded.append(activity + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(startTime));

        return encoded.toString();
    }

    public static String encodedAppUsage(String appName, Date timeStamp) {
        StringBuilder encoded = new StringBuilder("");
        encoded.append("appUsage," + appName + "," + timeStamp + ",");
        encoded.append("N/A" + ",");
        encoded.append(TempGranUtil.getTemporalTimeStamp(timeStamp));

        return encoded.toString();
    }
}
