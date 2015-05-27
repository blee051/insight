package com.insight.insight.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.insight.insight.R;
import com.insight.insight.common.Setting;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.utils.IOManager;
import com.insight.insight.utils.WearableSendSync;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by MN on 2/13/2015.
 */
public class Activity_Actv extends Activity {
    private GoogleApiClient mGoogleApiClient;
    private final String TAG = this.getClass().getSimpleName();

    JSONUtil jsonUtil = new JSONUtil();
    IOManager ioManager = new IOManager();
    File[] lastDataFilesList;

    TextView tvDate = null;
    TextView tvLastSync = null;
    ScrollView scrollView = null;
    LinearLayout frameBox = null;
    ImageView linksCursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chart);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvLastSync = (TextView) findViewById(R.id.tvLastSync);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        frameBox = (LinearLayout) findViewById(R.id.frameBox);
        linksCursor = (ImageView) findViewById(R.id.linksCursor);


        //set Title of activity
        TextView tvTitle = (TextView) findViewById(R.id.tvTitleChart);
        tvTitle.setText(R.string.title_activity_wactivity);


        lastDataFilesList = ioManager.getLastFilesInDir(Setting.dataFolderName_ActivFit, Setting.linksButtonCount);
        //lastDataFilesList = new File[]{new File("sdcard/2-9-2015.txt"), new File("sdcard/2-8-2015.txt")}; // reading from temp file
        if (lastDataFilesList != null && lastDataFilesList.length > 0) {
            String filename = lastDataFilesList[0].getName();
            Date date = ioManager.parseDataFilename2Date(filename);//
            displayData(date);
        } else {
            showNoData(new Date());
        }
    }

    private void showNoData(Date date) {
        tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(date));
        tvLastSync.setText("\n" + getResources().getString(R.string.message_nodata) + "\n");
        tvLastSync.setTextSize(getResources().getDimension(R.dimen.textsize_m1));
        linksCursor.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new SyncFitActivityData().execute();

    }

    private HashMap<String, ArrayList<ActivityDataRecord>> fetchData(String filename) {
        HashMap<String, ArrayList<ActivityDataRecord>> dataMapList = new HashMap<>();
        try {
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            try {
                while ((sCurrentLine = br.readLine()) != null) {
                    Object[] decodedRow = jsonUtil.decodeActivityFit(sCurrentLine);// [0]:startTime, [1]:endTime, [2]:activityType, [3]:duration
                    if (decodedRow != null) {
                        ActivityDataRecord dataRecord = new ActivityDataRecord();

                        SimpleDateFormat timeFormat = new SimpleDateFormat("H"); // return just hours of timestamp

                        dataRecord.startTime = (Date) decodedRow[0];
                        dataRecord.startTimeHour = Integer.valueOf(timeFormat.format(dataRecord.startTime));
                        dataRecord.activityType = decodedRow[2].toString();
                        dataRecord.duration = (int) decodedRow[3];
                        dataRecord.density = 1; // density of records in same hours

                        //ignore 'unknown' type activities
                        if (dataRecord.activityType.toLowerCase().contains("unknown"))
                            continue;

                        if (!dataMapList.containsKey(dataRecord.activityType))
                            dataMapList.put(dataRecord.activityType, new ArrayList<ActivityDataRecord>());

                        ArrayList<ActivityDataRecord> dataRecords = dataMapList.get(dataRecord.activityType);

                        //check if previous record's hour is the same with current record,
                        //calculate the average 'bpm' values and update previous record
                        if (dataRecords.size() > 0 && dataRecords.get(dataRecords.size() - 1).startTimeHour == dataRecord.startTimeHour) {
                            ActivityDataRecord lastDataRecord = dataRecords.get(dataRecords.size() - 1);
                            lastDataRecord.density += 1;
                            lastDataRecord.duration += dataRecord.duration;
                            dataRecords.set(dataRecords.size() - 1, lastDataRecord);
                            dataMapList.put(dataRecord.activityType, dataRecords);//update records array
                        } else {
                            dataMapList.get(dataRecord.activityType).add(dataRecord);
                            //dataRecords.add(dataRecord);
                        }
                    }
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMapList;
    }

    private void displayData(Date date) {
        tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(date));
        String dataFilename = ioManager.getDataFolderFullPath(Setting.dataFolderName_ActivFit) + Setting.filenameFormat.format(date) + ".txt";
        tvLastSync.setTextSize(getResources().getDimension(R.dimen.textsize_s1));
        tvLastSync.setText("Last Sync: " + new SimpleDateFormat("MM/dd/yyyy hh:mm").format(ioManager.getDataFileLastModifiedDate(dataFilename)));

        HashMap<String, ArrayList<ActivityDataRecord>> dataMapList = fetchData(dataFilename);
        if (dataMapList.size() <= 0)
            showNoData(date);

        // remove all added views before except linksbox and tvLastSync label
        frameBox.removeViewsInLayout(1, frameBox.getChildCount() - 2);

        FrameLayout chart;
        LinearLayout.LayoutParams cParams;
        int i = 0;
        for (String activityType : dataMapList.keySet()) {
            chart = new FrameLayout(this);
            chart.removeAllViews();
            cParams = new LinearLayout.LayoutParams(getSizeInDP(190), getSizeInDP(60));
            cParams.gravity = (Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            boolean showFooter = false;

            if (i == 0) {
                //first item
                cParams.setMargins(0, 0, 0, -10);// setMargins(left, top, right, bottom)

            } else if (i == dataMapList.size() - 1) {
                //last item
                showFooter = true;
                cParams.setMargins(0, -10, 0, 0);

            } else {
                //middle items
                cParams.setMargins(0, -10, 0, -10);

            }
            chart.setLayoutParams(cParams);
            chart.setPadding(10, 0, 0, 0); // setPadding(left, top, right, bottom)
            chart.addView(createGraph(dataMapList.get(activityType), activityType, showFooter));
            frameBox.addView(chart, i + 1);
            i += 1;
        }

        // add a cursor point to show the user the scroll feature ////////////////////////////////////////////////////////
        final AnimationSet aniSetCursor = new AnimationSet(true);
        final AlphaAnimation aniAlpha = new AlphaAnimation(1.0f, 0.0f);
        aniAlpha.setDuration(1500);
        aniAlpha.setRepeatCount(2);
        aniAlpha.setFillAfter(true);
        aniSetCursor.addAnimation(aniAlpha);

        TranslateAnimation aniMove = new TranslateAnimation(0.0f, 0.0f, -10.0f, 20.0f);          //  TranslateAnimation(xFrom, xTo, yFrom, yTo)
        aniMove.setDuration(1500);
        aniMove.setRepeatCount(2);
        aniSetCursor.addAnimation(aniMove);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics); // get screen properties ex. size
        RelativeLayout.LayoutParams linksCursorParams = (RelativeLayout.LayoutParams) linksCursor.getLayoutParams();
        linksCursorParams.setMargins(0, displayMetrics.heightPixels - 35, 0, 0); // set position of cursor in bottom of screen
        linksCursor.setTag(null);
        linksCursor.startAnimation(aniSetCursor);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                if (scrollY > 10 && linksCursor.getTag() == null) {
                    aniAlpha.setRepeatCount(0);
                    linksCursor.startAnimation(aniAlpha);
                    linksCursor.setTag("displayed");
                }
            }
        });


        //render Links box //////////////////////////////////////////////////////////////////////////////////////////
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 0, 0, 0);

        final LinearLayout linksBox = (LinearLayout) findViewById(R.id.linksBox);
        linksBox.removeAllViews();
        linksBox.setLayoutParams(params);

        // create links to datefiles
        for (File file : lastDataFilesList) {
            final Button btn1 = new Button(this);
            final Date tmpDate = ioManager.parseDataFilename2Date(file.getName());
            btn1.setText(new SimpleDateFormat("MM/dd/yyyy").format(tmpDate));
            btn1.setBackgroundColor(getResources().getColor(R.color.chart_button_bgcolor));
            btn1.setBackground(getResources().getDrawable(R.drawable.listview_bg_title));
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayData(tmpDate);
                    scrollView.scrollTo(0, 0);
                }
            });
            linksBox.addView(btn1, params);
        }

    }

    private View createGraph(ArrayList<ActivityDataRecord> dataRecords, String title, boolean isFooterVisible) {
        Log.i("Activity " + title, "In Create Chart");
        XYSeries series1 = new XYSeries("Activity " + title);

        // add a small value(5) to all values to visualize a horizontal line
        for (int i = 0; i <= 23; i++) {
            series1.add(i, 5);
        }

        for (ActivityDataRecord record : dataRecords) {
            series1.add(record.startTimeHour, record.duration + 5);
            //Log.d(">>", "act:" + record.activityType + ",ts:" + record.startTime.toString() + ", tsh:" + record.startTimeHour + ", sumdur:" + record.duration + ", dns:" + record.density);
        }

        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        renderer1.setLineWidth(getResources().getInteger(R.integer.chart_line_width));
        renderer1.setColor(getResources().getColor(R.color.chart_line_color));
        //renderer1.setDisplayBoundingPoints(true);
        //renderer1.setPointStyle(PointStyle.CIRCLE);
        //renderer1.setPointStrokeWidth(2);


        // add series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series1);

        // create the multiple series renderer to control the graph
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer1);
        mRenderer.setYAxisMin(0);
        mRenderer.setYLabels(0);
        mRenderer.addYTextLabel(0, title); // set Title in the middle of chart
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setYLabelsPadding(8.0f);

        mRenderer.setXAxisMin(0);
        mRenderer.setXAxisMax(23);

        mRenderer.setXLabels(0);
        if (isFooterVisible) {
            mRenderer.addXTextLabel(0, "00:00");
            mRenderer.addXTextLabel(12, "12:00");
            mRenderer.addXTextLabel(23, "23:59");
        }

        mRenderer.setBarSpacing(0.25);

        mRenderer.setXLabelsAlign(Paint.Align.CENTER);

        mRenderer.setShowAxes(false);
        mRenderer.setLabelsTextSize(getResources().getDimension(R.dimen.textsize_s3));

        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        mRenderer.setShowGridX(true);
        mRenderer.setShowGridY(false);
        mRenderer.setGridColor(Color.WHITE);
        mRenderer.setPanEnabled(false, false);// Disable Pan on two axis
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setBackgroundColor(Color.TRANSPARENT);
        mRenderer.setMargins(new int[]{10, 70, 10, 30}); //setMargins(top, left, bottom, right) defaults(20,30,10,20)
        mRenderer.setAxesColor(Color.WHITE);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setShowLegend(false);//hide info label
        mRenderer.setLabelsColor(getResources().getColor(R.color.chart_labels_color));
        mRenderer.setXLabelsColor(getResources().getColor(R.color.chart_labels_color));
        mRenderer.setYLabelsColor(0, getResources().getColor(R.color.chart_labels_color));
        mRenderer.setShowTickMarks(false);
        GraphicalView chartView = ChartFactory.getBarChartView(this, dataset, mRenderer, BarChart.Type.DEFAULT);
        return chartView;
    }

    private int getSizeInDP(int x) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }

    private class ActivityDataRecord {
        public Date startTime;
        public Date endTime;
        public int startTimeHour;
        //public int endTimeHour; // we calc just startTime
        public String activityType;
        public int duration;
        public int density; // density of records in same hour
    }


    private class SyncFitActivityData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mGoogleApiClient = new GoogleApiClient.Builder(Activity_Actv.this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(TAG, "Connected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
            if (mGoogleApiClient.isConnected()) {
                WearableSendSync.sendSyncToDevice(mGoogleApiClient,
                        WearableSendSync.START_ACTV_SYNC, new Date());
            }
            return null;

        }
    }
}
