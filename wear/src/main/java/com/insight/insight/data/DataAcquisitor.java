package com.insight.insight.data;

import android.content.Context;

import com.insight.insight.utils.IOManager;

import java.util.ArrayList;

/* Created by CM*/
public class DataAcquisitor {
    private static final String LOG_TAG = DataAcquisitor.class.getSimpleName();
    private String folderName;
    private Context context;
    private ArrayList<String> dataBuffer;

    public DataAcquisitor(Context context, String folderName) {
        this.context = context;
        this.folderName = folderName;
        dataBuffer = new ArrayList<String>();
    }

    public String getFolderName() {
        return folderName;
    }

    public ArrayList<String> getDataBuffer() {
        return dataBuffer;
    }

    public Context getContext() {
        return context;
    }

    public void insert(String s, boolean append, int maxBuffSize) {
        //Log.d(LOG_TAG, "Inserting into dBuff");
        dataBuffer.add(s);
        if (dataBuffer.size() >= maxBuffSize) {
            flush(append);
        }
    }

    public void flush(boolean append) {
        //Log.d(LOG_TAG, "Flushing buffer" + this.getFolderName());
        IOManager dataLogger = new IOManager();
        dataLogger.logData(this, append);
        getDataBuffer().clear();
    }

}
