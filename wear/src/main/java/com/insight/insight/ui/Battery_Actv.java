package com.insight.insight.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
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

import com.insight.insight.R;
import com.insight.insight.common.Setting;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.utils.IOManager;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
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

/**
 * Created by MN on 2/13/2015.
 */
public class Battery_Actv extends Activity {
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
        tvTitle.setText(R.string.title_activity_wbattery);


        lastDataFilesList = ioManager.getLastFilesInDir(Setting.dataFolderName_Battery, Setting.linksButtonCount);
        if (lastDataFilesList != null && lastDataFilesList.length > 0) {
            Date date = ioManager.parseDataFilename2Date(lastDataFilesList[0].getName());//
            displayData(date);
        } else {
            tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
            tvLastSync.setText("\n" + getResources().getString(R.string.message_nodata));
            tvLastSync.setTextSize(getResources().getDimension(R.dimen.textsize_m1));
            linksCursor.setVisibility(View.INVISIBLE);
        }
    }

    private void displayData(Date date) {
        tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(date));
        tvLastSync.setHeight(0);

        // remove all added views before except linksbox and tvLastSync label
        frameBox.removeViewsInLayout(1, frameBox.getChildCount() - 2);

        FrameLayout chart = new FrameLayout(this);
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(getSizeInDP(190), getSizeInDP(170));
        cParams.gravity = (Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        chart.setLayoutParams(cParams);
        chart.setPadding(10, 0, 15, 10);
        chart.addView(createGraph(date));
        frameBox.addView(chart, 1);


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

    private View createGraph(Date date) {
        Log.i("Battery", "In Create Chart");
        // We start creating the XYSeries to plot the temperature
        XYSeries series1 = new XYSeries("Battery");


        // start filling the series
        try {
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ioManager.getDataFolderFullPath(Setting.dataFolderName_Battery) + Setting.filenameFormat.format(date) + ".txt")));
            try {
                ArrayList<BatteryDataRecord> dataRecords = new ArrayList<>();
                while ((sCurrentLine = br.readLine()) != null) {
                    Object[] decodedRow = jsonUtil.decodeBattery(sCurrentLine);// [0]:Date, [1]:Percent, [2]:isCharging
                    if (decodedRow != null) {
                        BatteryDataRecord dataRecord = new BatteryDataRecord();

                        SimpleDateFormat timeFormat = new SimpleDateFormat("H"); // return just hours of timestamp

                        dataRecord.timeStamp = (Date) decodedRow[0];
                        dataRecord.timeStampHour = Integer.valueOf(timeFormat.format(dataRecord.timeStamp));
                        dataRecord.percent = (int) decodedRow[1];
                        dataRecord.isCharging = (boolean) decodedRow[2];
                        dataRecord.density = 1; // density of records in same hours

                        //Log.d(">>", "ts:" + dataRecord.timeStamp.toString() + ", " + "tsh:" + dataRecord.timeStampHour + ", " + dataRecord.percent + "%, " + "chrg:" + dataRecord.isCharging + ", " + "dns:" + dataRecord.density);

                        //check if previous record's hour is the same with current record,
                        //calculate the average 'percent' and update previous record
                        if (dataRecords.size() > 0 && dataRecords.get(dataRecords.size() - 1).timeStampHour == dataRecord.timeStampHour) {
                            BatteryDataRecord lastDataRecord = dataRecords.get(dataRecords.size() - 1);
                            lastDataRecord.density += 1;
                            lastDataRecord.percent += dataRecord.percent;
                        } else {
                            dataRecords.add(dataRecord);
                        }
                    }
                }


                for (BatteryDataRecord record : dataRecords) {
                    series1.add(record.timeStampHour, record.percent / record.density);
                    // Log.d(">>>", "ts:" + record.timeStamp.toString() + ", tsh:" + record.timeStampHour + ", " + record.percent / record.density + "%, chrg:" + record.isCharging + ", dns:" + record.density);
                }


            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        renderer1.setLineWidth(getResources().getInteger(R.integer.chart_line_width));
        renderer1.setColor(getResources().getColor(R.color.chart_line_color));
        //renderer1.setDisplayBoundingPoints(true);
        //renderer1.setPointStyle(PointStyle.CIRCLE);
        //renderer1.setPointStrokeWidth(2);


        // Now we add our series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series1);

        // Finaly we create the multiple series renderer to control the graph
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer1);
        mRenderer.setYAxisMin(0);
        mRenderer.setYAxisMax(101);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setYLabelsPadding(5.0f);
        mRenderer.setXAxisMin(0);
        mRenderer.setXAxisMax(23);

        mRenderer.addXTextLabel(0, "00:00");
        mRenderer.addXTextLabel(12, "12:00");
        mRenderer.addXTextLabel(23, "23:59");

        mRenderer.setXLabelsAlign(Paint.Align.CENTER);
        mRenderer.setXLabels(0);

        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setShowGrid(false);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setMargins(new int[]{10, 20, 5, 20});  //setMargins(top, left, bottom, right) defaults(20,30,10,20) ** more space in left and right for labels
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setShowLegend(false);//hide info label
        mRenderer.setLabelsColor(getResources().getColor(R.color.chart_labels_color));
        mRenderer.setXLabelsColor(getResources().getColor(R.color.chart_labels_color));
        mRenderer.setYLabelsColor(0, getResources().getColor(R.color.chart_labels_color));
        mRenderer.setShowTickMarks(false);
        //mRenderer.setChartTitle(new SimpleDateFormat("MM/dd/yyyy").format(date));

        GraphicalView chartView = ChartFactory.getLineChartView(this, dataset, mRenderer);
        return chartView;
    }

    private int getSizeInDP(int x) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }

    private class BatteryDataRecord {
        public Date timeStamp;
        public int timeStampHour;
        public int percent;
        public boolean isCharging;
        public int density; // density of records in same hour
    }
}
