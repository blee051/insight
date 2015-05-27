package com.insight.insight.sensors;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.insight.insight.common.Setting;
import com.insight.insight.data.DataAcquisitor;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.data.SemanticTempCSVUtil;
import com.insight.insight.utils.NotificationParcel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by CM on 2/21/15.
 */

/* Only one wearableListener service is allowed
   per application
 */

    /* This is the Core Sensor Class for Insight.
       Notification, Activity, HeartRate, Bluetooth, Step are all processed here.

       This class receives updates through the DataLayer.API from the handheld. The
       received item is then evaluated based on it's Path
     */
public class NotificationSensor extends WearableListenerService {
    private final String NOTIF_KEY = "com.insight.notif";
    private final String HEART_KEY = "com.insight.heartrate";
    private final String ACTV_KEY = "com.insight.activity";
    private final String STEP_KEY = "com.insight.step";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yyyy HH:mm:ss");

    private static final String TAG = NotificationSensor.class.getSimpleName();
    private GoogleApiClient mClient;

    private DataAcquisitor mBTBuffer;
    private DataAcquisitor mNotifBuffer;
    private DataAcquisitor mHeartBuffer;
    private static DataAcquisitor mActivBuffer;
    private DataAcquisitor mStepBuffer;

    //TemporalGran Buffers
    private DataAcquisitor mSA_NotifBuffer;
    private DataAcquisitor mSA_BTBuffer;
    private DataAcquisitor mSA_HeartBuffer;
    private static DataAcquisitor mSA_ActivBuffer;
    private DataAcquisitor mSA_StepBuffer;

    private String lastPackageName;
    private String lastExtraText;
    private String lastTitle;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mClient != null) {
            mClient.connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBTBuffer = new DataAcquisitor(this, "Bluetooth");
        mNotifBuffer = new DataAcquisitor(this, "Notif");
        mHeartBuffer = new DataAcquisitor(this, "HeartRate");
        mActivBuffer = new DataAcquisitor(this, "ActivFit");
        mStepBuffer = new DataAcquisitor(this, "Activity");

        //TemporalGran buffers
        mSA_BTBuffer = new DataAcquisitor(this, "SA/Bluetooth");
        mSA_NotifBuffer = new DataAcquisitor(this, "SA/Notif");
        mSA_HeartBuffer = new DataAcquisitor(this, "SA/HeartRate");
        mSA_ActivBuffer = new DataAcquisitor(this, "SA/ActivFit");
        mSA_StepBuffer = new DataAcquisitor(this, "SA/Activity");


        mClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "Successful connect");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    @Override
    public void onDestroy() {
        mBTBuffer.flush(true);
        mNotifBuffer.flush(true);
        mHeartBuffer.flush(true);
        mActivBuffer.flush(true);

        //TempGran Buffers
        mSA_BTBuffer.flush(true);
        mSA_ActivBuffer.flush(true);
        mSA_HeartBuffer.flush(true);
        mSA_NotifBuffer.flush(true);

        super.onDestroy();
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "Bluetooth connected");
        String encoded = JSONUtil.encodeBT("Connected", new Date());
        Log.d(TAG, encoded);
        mBTBuffer.insert(encoded, true, Setting.bufferMaxSize);

        String encoded_SA = SemanticTempCSVUtil.encodedBT("Connected", new Date());
        mSA_BTBuffer.insert(encoded_SA, true, Setting.bufferMaxSize);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "Bluetooth Disconnected");
        String encoded = JSONUtil.encodeBT("Disconnected", new Date());
        Log.d(TAG, encoded);
        mBTBuffer.insert(encoded, true, Setting.bufferMaxSize);

        String encoded_SA = SemanticTempCSVUtil.encodedBT("Disconnected", new Date());
        mSA_BTBuffer.insert(encoded_SA, true, Setting.bufferMaxSize);


    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "On data changed");
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                //Data item changed
                DataItem item = event.getDataItem();

                //Log.d(TAG, "W Item Uri: " + item.getUri().getPath());

                if (item.getUri().getPath().compareTo("/notif") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final byte[] bytes = dataMap.getByteArray(NOTIF_KEY);

                    NotificationParcel np = unMarshall(bytes);
                    if (lastPackageName == null) {
                        lastPackageName = np.PACKAGE_NAME;
                        lastExtraText = np.EXTRA_TEXT;
                        lastTitle = np.EXTRA_TITLE;
                    } else if (lastExtraText.equals(np.EXTRA_TEXT) &&
                            lastPackageName.equals(np.PACKAGE_NAME) &&
                            lastTitle.equals(np.EXTRA_TITLE)) {
                        Log.d(TAG, "Same notif");
                        return;
                    }

                    lastExtraText = np.EXTRA_TEXT;
                    lastPackageName = np.PACKAGE_NAME;
                    lastTitle = np.EXTRA_TITLE;
                    String encoded = JSONUtil.encodeNotification(np);

                    Log.d(TAG, encoded);
                    mNotifBuffer.insert(encoded, true, Setting.bufferMaxSize);
                    //mNotifBuffer.flush(true);

                    //tempGran
                    String encoded_SA = SemanticTempCSVUtil.encodeNotification(np);
                    mSA_NotifBuffer.insert(encoded_SA, true, Setting.bufferMaxSize);
                    //mSA_NotifBuffer.flush(true);


                }
                if (item.getUri().getPath().compareTo("/heartrate") == 0) {
                    Log.d(TAG, "Heartrate");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final byte[] bytes = dataMap.getByteArray(HEART_KEY);
                    DataSet dataSet = unMarshallHeartData(bytes);
                    ArrayList<String> encodedDataSet = encodeDataSet(dataSet);
                    writeHeartRateValues(encodedDataSet);

                    writeSA_Heart_DataSet(dataSet);

                }
                if (item.getUri().getPath().compareTo("/actv") == 0) {
                    Log.d(TAG, "Activity Data");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final byte[] bytes = dataMap.getByteArray(ACTV_KEY);

                    DataReadResult result = unMarshallDataResult(bytes);
                    writeActivResults(result);

                    writeSA_ActivResults(result);

                }

                if (item.getUri().getPath().compareTo("/step") == 0) {
                    Log.d(TAG, "Step data");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final byte[] bytes = dataMap.getByteArray(STEP_KEY);

                    DataReadResult result = unMarshallDataResult(bytes);
                    writeStepResults(result);
                    writeSA_StepResult(result);

                }

                if (item.getUri().getPath().compareTo("/post/SA/notifFile") == 0) {
                    Log.d(TAG, "Received SA Complete");
                    // Overwrite previous SA File
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    overWritePreviousSAFile(dataMap);


                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }


    }

    private NotificationParcel unMarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);

        NotificationParcel np = NotificationParcel.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return np;
    }

    private DataSet unMarshallHeartData(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);

        DataSet dataSet = DataSet.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return dataSet;
    }

    private DataReadResult unMarshallDataResult(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);

        DataReadResult result = DataReadResult.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return result;

    }

    private static void dumpHeartDataPoints(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(TAG, "Data Returned of type:" + dp.getDataType().getName());
            Log.d(TAG, "Data Point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private ArrayList<String> encodeDataSet(DataSet dataSet) {
        ArrayList<String> encodedDp = new ArrayList<String>();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Date start = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));

            Value bpm;
            for (Field field : dp.getDataType().getFields()) {
                bpm = dp.getValue(field);
                Log.d(TAG, "BPM:" + bpm);
                String encoded = JSONUtil.encodeHeartRate(start, bpm.asFloat());
                Log.d(TAG, encoded);
                encodedDp.add(encoded);
            }

            Log.d(TAG, "---------");
        }
        return encodedDp;
    }

    /* We do not want to append since heartrate values will return same values if called
        during day more than once
     */
    private void writeHeartRateValues(ArrayList<String> encoded) {
        for (String s : encoded) {
            mHeartBuffer.insert(s, false, Setting.bufferMaxSize);
        }
        mHeartBuffer.flush(false);
    }

    private void writeSA_Heart_DataSet(DataSet dataSet) {

        for (DataPoint dp : dataSet.getDataPoints()) {
            Date start = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));

            Value bpm;
            for (Field field : dp.getDataType().getFields()) {
                bpm = dp.getValue(field);
                Log.d(TAG, "BPM:" + bpm);
                String encoded = SemanticTempCSVUtil.encodedHeartRate(bpm.asFloat(), start);
                Log.d(TAG, encoded);
                mSA_HeartBuffer.insert(encoded, false, Integer.MAX_VALUE);
            }

            Log.d(TAG, "---------");
        }
        mSA_HeartBuffer.flush(false);
    }

    private static void printReadResult(DataReadResult dataReadResult) {
        Log.d(TAG, "Printing results");
        Log.d(TAG, "Bucketsize: " + dataReadResult.getBuckets().size());

        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {
                processDataSet(dataSet);
                Log.d(TAG, "Data: " + encodeActvDataSet(dataSet));
            }
        }
    }

    private static void writeActivResults(DataReadResult dataReadResult) {
        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {

                String encoded = encodeActvDataSet(dataSet);
                mActivBuffer.insert(encoded, false, Integer.MAX_VALUE);
            }
        }
        mActivBuffer.flush(false);
    }

    private static void writeSA_ActivResults(DataReadResult dataReadResult) {
        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {

                String encoded = SA_encodeActvDataSet(dataSet);
                mSA_ActivBuffer.insert(encoded, false, Integer.MAX_VALUE);
            }
        }
        mSA_ActivBuffer.flush(false);
    }

    private void writeStepResults(DataReadResult dataReadResult) {
        DataSet dataSet = dataReadResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
        ArrayList<String> encoded = encodeStepDataSet(dataSet);
        for (String s : encoded) {
            mStepBuffer.insert(s, false, Integer.MAX_VALUE);
        }
        mStepBuffer.flush(false);
    }

    private void writeSA_StepResult(DataReadResult dataReadResult) {
        DataSet dataSet = dataReadResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
        ArrayList<String> sa_encoded = SA_encodeStepDataSet(dataSet);
        for (String s : sa_encoded) {
            mSA_StepBuffer.insert(s, false, Integer.MAX_VALUE);
        }
        mSA_StepBuffer.flush(false);
    }


    private static void processDataSet(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(TAG, "Data Returned of type:" + dp.getDataType().getName());
            Log.d(TAG, "Data Point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field)
                        + "Type: " + dp.getValue(field).asActivity());
            }
        }
        Log.d(TAG, "-----------------");
    }

    private static String encodeActvDataSet(DataSet dataSet) {
        Long startTime = null;
        Long endTime = null;
        String activity = null;
        Integer duration = null;

        String encoded;

        for (DataPoint dp : dataSet.getDataPoints()) {
            startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            for (Field field : dp.getDataType().getFields()) {
                if (field.getName().equals("activity")) {
                    activity = dp.getValue(field).asActivity();
                }
                if (field.getName().equals("duration")) {
                    duration = dp.getValue(field).asInt();
                }
            }

        }
        encoded = JSONUtil.encodeActivitySegments(new Date(startTime), new Date(endTime), activity, duration);
        return encoded;

    }

    /**
     * @param dataSet
     * @return [0] :
     */
    private static ArrayList<String> encodeStepDataSet(DataSet dataSet) {
        Long startTime = null;
        Long endTime = null;
        Integer steps = null;
        int stepCount = 0;
        ArrayList<String> encodedArray = new ArrayList<String>();
        String encoded;
        for (DataPoint dp : dataSet.getDataPoints()) {
            startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            for (Field field : dp.getDataType().getFields()) {
                if (field.getName().equals("steps")) {
                    steps = dp.getValue(field).asInt();
                    stepCount += steps;
                    encoded = JSONUtil.encodeStepActivity(new Date(startTime), new Date(endTime), stepCount, steps);
                    encodedArray.add(encoded);
                }

            }
        }
        return encodedArray;


    }

    private static ArrayList<String> SA_encodeStepDataSet(DataSet dataSet) {
        Long startTime = null;
        Long endTime = null;
        Integer steps = null;
        int stepCount = 0;
        ArrayList<String> encodedArray = new ArrayList<String>();
        String encoded;
        for (DataPoint dp : dataSet.getDataPoints()) {
            startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            for (Field field : dp.getDataType().getFields()) {
                if (field.getName().equals("steps")) {
                    steps = dp.getValue(field).asInt();
                    stepCount += steps;
                    encoded = SemanticTempCSVUtil.encodeStepActivity(new Date(startTime), stepCount, steps);
                    encodedArray.add(encoded);
                }

            }
        }
        return encodedArray;


    }


    private static String SA_encodeActvDataSet(DataSet dataSet) {
        Long startTime = null;
        Long endTime = null;
        String activity = null;
        Integer duration = null;

        String encoded;

        for (DataPoint dp : dataSet.getDataPoints()) {
            startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            for (Field field : dp.getDataType().getFields()) {
                if (field.getName().equals("activity")) {
                    activity = dp.getValue(field).asActivity();
                }
                if (field.getName().equals("duration")) {
                    duration = dp.getValue(field).asInt();
                }
            }

        }
        encoded = SemanticTempCSVUtil.encodedActivSegments(new Date(startTime), activity, duration);
        return encoded;

    }

    /**
     * Takes in the genre modified file. It will then overWrite the day's notif file on the watch
     *
     * @param dataMap
     */
    private void overWritePreviousSAFile(DataMap dataMap) {
        String filename = dataMap.getString("filename");
        Log.d(TAG, "Filename: " + filename);
        byte[] bytes = dataMap.getByteArray("SA_Notif_File");
        File file = new File(filename);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, false);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
