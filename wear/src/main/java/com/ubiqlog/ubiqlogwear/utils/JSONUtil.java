package com.ubiqlog.ubiqlogwear.utils;

import com.ubiqlog.ubiqlogwear.common.NotificationParcel;
import com.ubiqlog.ubiqlogwear.common.Setting;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by User on 2/23/15.
 */
public class JSONUtil {
    public static String encodeBattery(int percent, boolean charging, Date timeStamp) {
        JSONObject jsonObject = new JSONObject();
        JSONObject sensorDataObj = new JSONObject();
        try {
            jsonObject.put("sensor_name", "Battery");
            jsonObject.put("timestamp", timeStamp);

            sensorDataObj.put("percent", percent);
            sensorDataObj.put("charging", charging);

            jsonObject.put("sensor_data", sensorDataObj);

            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String encodeLight(float lux, Date timeStamp) {
        JSONObject jsonObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        try {
            jsonObject.put("sensor_name", "Light");
            jsonObject.put("timestamp", timeStamp);

            sensorData.put("lux", lux);

            jsonObject.put("sensor_data", sensorData);

            return jsonObject.toString();


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeBT(String state, Date timeStamp) {
        JSONObject jsonObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        try {
            jsonObject.put("sensor_name", "BT");
            jsonObject.put("timestamp", timeStamp);

            sensorData.put("state", state);

            jsonObject.put("sensor_data", sensorData);

            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeStepActivity(Date startTime, Date endTime,
                                            int culmStepAmt, int stepDiff) {

        JSONObject jsonObject = new JSONObject();
        JSONObject timeObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        try {
            jsonObject.put("sensor_name", "Activity");

            timeObject.put("start_time", startTime);
            timeObject.put("end_time", endTime);

            jsonObject.put("timestamp", timeObject);


            sensorData.put("step_counts", culmStepAmt);
            sensorData.put("step_delta", stepDiff);

            jsonObject.put("sensor_data", sensorData);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeNotification(NotificationParcel in) {
        JSONObject jsonObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        try {
            jsonObject.put("sensor_name", "Notification");
            jsonObject.put("timestamp", new Date(in.POST_TIME));

            sensorData.put("package_name", in.PACKAGE_NAME);
            sensorData.put("title", in.EXTRA_TITLE);
            //filter sms and gmail and twitter
            if (in.PACKAGE_NAME.equals("com.android.mms") || in.PACKAGE_NAME.equals("com.google.android.gm")
                    || in.PACKAGE_NAME.equals("com.twitter.android") || in.PACKAGE_NAME.equals("com.facebook.orca")) {
                sensorData.put("text", "");
            } else {
                sensorData.put("text", in.EXTRA_TEXT);
            }
            sensorData.put("flags", in.flags);
            sensorData.put("category", in.category);

            jsonObject.put("sensor_data", sensorData);

            return jsonObject.toString();


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeHeartRate(Date timestamp, float bpm) {

        JSONObject jsonObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        try {
            jsonObject.put("sensor_name", "HeartRate");
            jsonObject.put("timestamp", timestamp);

            sensorData.put("bpm", bpm);

            jsonObject.put("sensor_data", sensorData);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeActivitySegments (Date startTime, Date endTime, String activity,
                                                 Integer duration){
        JSONObject jsonObject = new JSONObject();
        JSONObject sensorData = new JSONObject();
        JSONObject timeData = new JSONObject();

        try{
            jsonObject.put("sensor_name","Activity");

            timeData.put("start_time", startTime);
            timeData.put("end_time",endTime);

            jsonObject.put("timestamp", timeData);

            sensorData.put("activity",activity);
            sensorData.put("duration",duration);

            jsonObject.put("sensor_data",sensorData);

            return jsonObject.toString();

        } catch(JSONException e){
            e.printStackTrace();
        }
        return null;

    }


    /* Decode Methods*/


    /**
     * @param encoded
     * @return obj[0] : Date timestamp
     * obj[1] : int percent
     * obj[2] : boolean charging
     */
    public Object[] decodeBattery(String encoded) {
        try {
            JSONObject jObj = new JSONObject(encoded);
            Date date = Setting.timestampFormat.parse(jObj.get("timestamp").toString());

            JSONObject sensorData = jObj.getJSONObject("sensor_data");

            int percent = sensorData.getInt("percent");
            boolean charging = sensorData.getBoolean("charging");

            return new Object[]{date, percent, charging};
            
        } catch (JSONException e) {
            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * obj[0] : date
     * obj[1] : lux
     *
     * @param encoded
     * @return
     */
    public Object[] decodeLight(String encoded) {
        try {
            JSONObject jObj = new JSONObject(encoded);
            Date date = (Date) jObj.get("timestamp");

            JSONObject sensorData = jObj.getJSONObject("sensor_data");

            float lux = (float) sensorData.get("lux");

            return new Object[]{date, lux};

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * obj[0]:date
     * obj[1] :pkg name
     * obj[2]: title
     * obj[3] : text
     * obj[4] : flags
     * obj[5] : category
     *
     * @param encoded
     * @return
     */
    public Object[] decodeNotification(String encoded) {
        try {
            JSONObject jObj = new JSONObject(encoded);
            Date date = (Date) jObj.get("timestamp");

            JSONObject sensorData = jObj.getJSONObject("sensor_data");
            String packageName = sensorData.getString("package_name");
            String title = sensorData.getString("title");
            String text = sensorData.getString("text");
            Integer flags = sensorData.getInt("flags");
            String category = sensorData.getString("category");


            return new Object[]{date, packageName, title, text, flags, category};

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * obj[0] :date
     * obj[1]: startTime
     * obj[2] : endTime
     * obj[3] : culmStep
     * obj[4] : step_delta
     *
     * @param encoded
     * @return
     */
    public Object[] decodeStepActivity(String encoded) {
        try {
            JSONObject jObj = new JSONObject(encoded);
            Date date = (Date) jObj.get("timestamp");
            JSONObject timeObj = jObj.getJSONObject("timestamp");
            Date startDate = (Date) timeObj.get("start_time");
            Date endDate = (Date) timeObj.get("end_time");

            JSONObject sensorData = jObj.getJSONObject("sensor_data");

            Integer culmAmt = sensorData.getInt("step_counts");
            Integer stepDelta = sensorData.getInt("step_delta");

            return new Object[]{date, startDate, endDate, culmAmt, stepDelta};


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
