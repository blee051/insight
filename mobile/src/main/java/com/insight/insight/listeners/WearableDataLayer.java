package com.insight.insight.listeners;

/**
 * Created by CM on 2/16/15.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.insight.insight.util.NotificationParcel;
import com.insight.insight.util.FileUtils;
import com.insight.insight.util.NotifLookupUtil;

import java.io.File;
import java.util.Date;


/*
    This Class handles the sending of data to the wearable
 */
public class WearableDataLayer {
    private static final String TAG = WearableDataLayer.class.getSimpleName();

    public static final String HEART_HIST_KEY = "com.insight.heartrate";
    public static final String ACTV_HIST_KEY = "com.insight.activity";
    private static final String SYNC_KEY = "/start/HistorySYNC";
    public static final String STEP_HIST_KEY = "com.insight.step";
    private static Context mContext;

    public static GoogleApiClient buildDataApiClient(Context context) {
        mContext = context;
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API

                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        return mGoogleApiClient;

    }


    public static void sendHeartData(GoogleApiClient mClient, DataSet dataSet, final String KEY_NAME) {
        Parcel p = Parcel.obtain();
        dataSet.writeToParcel(p, 0);
        byte[] bytes = p.marshall();
        Log.d(TAG, "Sending data");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/heartrate");
        putDataMapReq.getDataMap().putLong("time", new Date().getTime());
        putDataMapReq.getDataMap().putByteArray(KEY_NAME, bytes);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //Send Data To wearable
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mClient, putDataReq);
        p.recycle();

    }

    public static void sendActivityData(GoogleApiClient mClient, DataReadResult dr, final String KEY_NAME) {
        Parcel p = Parcel.obtain();
        dr.writeToParcel(p, 0);
        byte[] bytes = p.marshall();
        Log.d(TAG, "Sending activity data");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/actv");
        putDataMapReq.getDataMap().putLong("time", new Date().getTime());
        putDataMapReq.getDataMap().putByteArray(KEY_NAME, bytes);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //Send Data To wearable
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mClient, putDataReq);
        p.recycle();
    }

    public static void sendStepResult(GoogleApiClient mClient, DataReadResult dr, final String KEY_NAME) {
        Parcel p = Parcel.obtain();
        dr.writeToParcel(p, 0);
        byte[] bytes = p.marshall();
        Log.d(TAG, "Sending activity data");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/step");
        putDataMapReq.getDataMap().putLong("time", new Date().getTime());
        putDataMapReq.getDataMap().putByteArray(KEY_NAME, bytes);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //Send Data To wearable
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mClient, putDataReq);
        p.recycle();
    }

    public static void sendNotificationtoWear(GoogleApiClient mClient, StatusBarNotification sbn,
                                              final String KEY_NAME) {
        NotificationParcel notificationParcel = new NotificationParcel(sbn);
        Parcel p = Parcel.obtain();
        notificationParcel.writeToParcel(p, 0);
        byte[] bytes = p.marshall();
        Log.d(TAG, "Bytes:" + bytes.length);
        Log.d(TAG, "Sending..");

       /* Parcel p = Parcel.obtain();
        sbn.writeToParcel(p,0);
        byte[] bytes = p.marshall();
        Log.d(TAG, "Sending data");
        */

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/notif");
        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putByteArray(KEY_NAME, bytes);
        PutDataRequest pdq = putDataMapReq.asPutDataRequest();

        //Send to wearable
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mClient, pdq);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error:" + dataItemResult.getStatus().getStatusCode());
                }
            }
        });
        p.recycle();

    }

    /* This will send the newly updated genre complete notification file to the handheld

     */
    public static void sendSACompleteToWear(Context context, GoogleApiClient mClient, String filename) {
        File file = new File(context.getFilesDir() + NotifLookupUtil.sa_completeDir + "/" + NotifLookupUtil.completeFileName);


        //Convert to byte arr
        byte[] bytes = FileUtils.convertFileToBytes(file);

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/post/SA/notifFile");
        putDataMapReq.getDataMap().putLong("time", new Date().getTime());
        putDataMapReq.getDataMap().putString("filename", filename);
        putDataMapReq.getDataMap().putByteArray("SA_Notif_File", bytes);

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //Send Data To wearable
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mClient, putDataReq);
    }


}