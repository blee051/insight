package com.insight.insight.sensors;

import android.content.Context;
import android.util.Log;

import com.insight.insight.common.Setting;
import com.insight.insight.core.DataAcquisitor;
import com.insight.insight.data.SemanticTempCSVUtil;
import com.insight.insight.data.JSONUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by CM on 2/9/15.
 */

/* This class is no longer being used. It was previously used to differentiate when the user
   has started and stopped walking
 */
public class ActivityDataHelper {
    private static final String LOG_TAG = ActivityDataHelper.class.getSimpleName();


    public static class Step {
        private int culmStepAmt;
        private Date date;

        public Step(int culmStepAmt, Date date) {
            this.culmStepAmt = culmStepAmt;
            this.date = date;
        }

        public int getCulmStepAmt() {
            return this.culmStepAmt;
        }

        public Date getDate() {
            return this.date;
        }

    }

    /* This StepList will be used to determine when the user has stopped walking
       If the time between the last Inserted Element and the element being inserted
       differs by SensorConstants.StepListInterval, add to DataAcquistor buff,
       Otherwise, Update last inserted
     */
    public static class StepList {
        ArrayList<Step> stepBuff;
        private DataAcquisitor mDataBuffer;
        private DataAcquisitor mSA_stepBuffer;


        public StepList(Context context) {
            stepBuff = new ArrayList<Step>();
            mDataBuffer = new DataAcquisitor(context, "Activity");
            mSA_stepBuffer = new DataAcquisitor(context, "SA/Activity");
        }
        public DataAcquisitor getmDataBuffer(){
            return mDataBuffer;
        }

        public synchronized void insert(Step s) {
            if (stepBuff.isEmpty()) {
                stepBuff.add(s);
            } else {
                if (stepBuff.size() == 1) {
                    //Start time has been inserted
                    stepBuff.add(s);
                } else {
                    if (isStillWalking(s)) {
                        //Update buffer
                        stepBuff.add(1,s);
                    } else {
                        //form Encoded String
                        Date startTime = stepBuff.get(0).getDate();
                        Date endTime = stepBuff.get(1).getDate();
                        int culmStepAmnt = stepBuff.get(1).getCulmStepAmt();
                        int diffInStep = stepBuff.get(1).getCulmStepAmt() - stepBuff.get(0).getCulmStepAmt();

                        String encoded = JSONUtil.encodeStepActivity(startTime,
                                endTime, culmStepAmnt, diffInStep);
                        Log.d(LOG_TAG,encoded);

                        //Send to DataAcquisitor
                        mDataBuffer.insert(encoded,true, Setting.bufferMaxSize);
                        mDataBuffer.flush(true);

                        String encoded_SA = SemanticTempCSVUtil.encodeStepActivity(startTime, culmStepAmnt, diffInStep);
                        mSA_stepBuffer.insert(encoded_SA,true,Setting.bufferMaxSize);
                        mSA_stepBuffer.flush(true);



                        stepBuff.clear();
                    }

                }
            }
        }


        /*This method should only be called if there is a startTime and lastUpdated */
        private boolean isStillWalking(Step s) {
            if (stepBuff.size() < 2){
                try {
                    throw new IllegalAccessException();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            Date lastUpdateDate = stepBuff.get(1).getDate();
            long diffInMillis = lastUpdateDate.getTime() - s.getDate().getTime();
            long absDiff = Math.abs(diffInMillis);
            Log.d(LOG_TAG, "Diff in millis:" + absDiff);
            if (absDiff > Setting.WALKDETECTION_INTERVAL) {
                return false;
            } else
                return true;

        }
    }
}
