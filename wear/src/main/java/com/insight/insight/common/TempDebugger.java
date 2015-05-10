package com.insight.insight.common;

/*
    Created by MN
*/

import android.content.Context;
import android.os.Vibrator;

import com.insight.insight.core.DataAcquisitor;
import com.insight.insight.data.JSONUtil;

import java.util.Date;

public class TempDebugger {
    private Context context;

    public TempDebugger(Context con) {
        this.context = con;
    }

    public void Log(String strTag, String strLog) {
        DataAcquisitor mDataBuffer = new DataAcquisitor(context, Setting.dataFolderName_Temp);
        mDataBuffer.insert(JSONUtil.encodeTempDebuggerLog(new Date(), strTag, android.os.Process.myPid() + " " + strLog), true, Setting.bufferMaxSize);
        mDataBuffer.flush(true);
    }

    public void Vibrate(int lng) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // first delay, vibrate, sleep, vibrate, sleep... in milliseconds
        long[] vibrationPattern = {0, lng * 100, lng * 500, lng * 100};

        //-1 - don't repeat
        vibrator.vibrate(vibrationPattern, -1);
    }

}