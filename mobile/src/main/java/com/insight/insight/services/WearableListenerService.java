package com.insight.insight.services;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.insight.insight.listeners.WearableDataLayer;
import com.insight.insight.util.NotifLookupUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by CM on 2/25/15.
 */

/* This class receives sync requests from the handheld

*/
public class WearableListenerService extends com.google.android.gms.wearable.WearableListenerService {
    private final String TAG = this.getClass().getSimpleName();
    private static GoogleApiClient mGoogleApiClient;
    private static final String HEART_SYNC_KEY = "/start/HeartSync";
    private static final String ACTV_SYNC_KEY = "/start/ActvSync";
    private static final String NOTIF_FILE_KEY = "/get/notifFile";


    @Override
    public void onDataChanged(DataEventBuffer events) {
        Log.d(TAG, "On data Changed");
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();

                //Log.d(TAG, "M Item Uri: " + item.getUri().getPath());

                if (item.getUri().getPath().compareTo(HEART_SYNC_KEY) == 0) {
                    Log.d(TAG, "HEART SYNC REQ");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Date date = new Date(dataMap.getLong("time"));
                    buildGoogleAPIClient();
                    mGoogleApiClient.connect();
                    fetchHeartDataSet(this, date);

                }
                if (item.getUri().getPath().compareTo(ACTV_SYNC_KEY) == 0) {
                    Log.d(TAG, "ACTIVITY SYNC REQ");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Date date = new Date(dataMap.getLong("time"));
                    buildGoogleAPIClient();
                    mGoogleApiClient.connect();

                    Handler actvHandler = buildActivityHandler();
                    actvHandler.post(new ActivitySensor.ActivityInformationRunnable(mGoogleApiClient, this, date));
                }
                if (item.getUri().getPath().compareTo(NOTIF_FILE_KEY) == 0) {
                    Log.d(TAG, "NOTIF FILE");

                    synchronized (this) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        handleNotifFile(dataMap); // Creates file with SA completed and mapped
                        WearableDataLayer.sendSACompleteToWear(this, mGoogleApiClient, getFileName(dataMap));
                        cleanUpFiles(this);
                    }


                }
            }
        }

    }

    //Send the dataSet to the wearable
    public static void sendToWearable(DataSet dataSet) {
        WearableDataLayer.sendHeartData(mGoogleApiClient, dataSet, WearableDataLayer.HEART_HIST_KEY);

    }

    /*This function builds a fitClient after receiving a sync Request from the wearable
        It starts a thread to fetch the heartDataSet and calls to sendToWearable, sending
        the dataset to the wearable
     */
    public static void fetchHeartDataSet(Context context, Date date) {
        GoogleApiClient fitClient = HeartRateSensor.buildFitClient(context);
        fitClient.connect();

        Handler heartHandler = buildHandler();

        HeartRateSensor.getDataPoints(heartHandler, fitClient, new HeartRateSensor.SyncRequestInterface() {
            @Override
            public void setDataSet(DataSet dataSet) {
                sendToWearable(dataSet);
            }
        }, date);
    }

    private static Handler buildHandler() {
        HandlerThread heartThread = new HandlerThread("Heartthread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        heartThread.start();
        Handler heartHandler = new Handler(heartThread.getLooper());
        return heartHandler;
    }

    private static Handler buildActivityHandler() {
        HandlerThread actvThread = new HandlerThread("Actvthread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        actvThread.start();
        Handler actvHandler = new Handler(actvThread.getLooper());
        return actvHandler;
    }


    private void buildGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "Connected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        //Try reconnect
                        mGoogleApiClient.connect();
                    }
                })
                .build();
    }

    /* tmp.txt will be overwritten with each time */
    private void handleNotifFile(DataMap dataMap) {
        byte[] bytes = dataMap.getByteArray("NOTIF_FILE");
        File dirs = new File(this.getFilesDir() + "/notif");
        dirs.mkdirs();
        File tmp = new File(dirs, "tmp.txt");

        try {
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(bytes);
            fos.close();

            NotifLookupUtil.handleNotifLookup(this, tmp.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getFileName(DataMap dataMap) {
        String fileName = dataMap.getString("filename");
        return fileName;
    }

    private void cleanUpFiles(Context context) {
        // File: /data/data/com.insight.insight/files/SAComplete/TMP_SACompleted.txt
        File saCompleteNotifFile = new File(context.getFilesDir() + NotifLookupUtil.sa_completeDir + "/" + NotifLookupUtil.completeFileName);
        saCompleteNotifFile.delete();

    }
}
