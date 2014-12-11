package com.ubiqlog.ubiqlogwear.ui.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONObject;

import com.ubiqlog.ubiqlogwear.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by prajnashetty on 10/30/14.
 */
public class BluetoothChartFragment extends Fragment {

    private LinearLayout chartLyt;
    private Animation fadeAnim;
    private int greater;

    ArrayList<Integer> processList = new ArrayList<Integer>();
    ArrayList<Integer> timeList = new ArrayList<Integer>();

    public static final SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    public static final SimpleDateFormat filedateformat = new SimpleDateFormat("MM-dd-yyyy");


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //fadeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_anim);
        getData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("BluetoothChartFragment", "in onCreateView");
        View v = inflater.inflate(R.layout.fragment_chart, container, false);
        chartLyt = (LinearLayout) v.findViewById(R.id.chart);
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        getData();
    }

    private void getData()
    {

        try {
            Calendar cal = Calendar.getInstance();
            //cal.add(Calendar.DATE, -1);

            File dir = Environment.getExternalStorageDirectory();
            File logFile = new File(dir,"/ubiqlog/log_" + filedateformat.format(cal.getTime()) + ".txt");
            Log.i("BluetoothChartFragment", "logFile =" + logFile);
            if (logFile != null) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
                String read;
                StringBuilder builder = new StringBuilder("");
                while ((read = bufferedReader.readLine()) != null) {
                    JSONObject jsonObject1 = new JSONObject(read);
                    if (jsonObject1.optJSONObject("Bluetooth") != null) {

                        JSONObject batteryObject = jsonObject1.optJSONObject("Bluetooth");

                        String process = batteryObject.getString("name");
                        processList.add(1);
                        Date date = dateformat.parse(batteryObject.getString("time"));
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(date);
                        int minutes = cal1.get(Calendar.MINUTE);
                        if (minutes > 30)
                            timeList.add (cal1.get(Calendar.HOUR_OF_DAY) + 1);
                        else
                            timeList.add (cal1.get(Calendar.HOUR_OF_DAY));
                        Log.i("BluetoothChartFragment", "Process =" +process);
                        Log.i("BluetoothChartFragment", "Date =" + date);
                    }
                }
                Log.d("Output", builder.toString());
                bufferedReader.close();
            }
        } catch (Exception e) {

            Log.e("ChartFragment", "--------Failed to read file-----"+ e.getMessage() + "; Stack: " +  Log.getStackTraceString(e));

        }
        Toast.makeText(getActivity(), "Data retrieved", Toast.LENGTH_LONG).show();
        chartLyt.addView(createGraph(), 0);
    }

    private View createGraph() {
        Log.i("BluetoothChartFragment", "In Create Chart");
        // We start creating the XYSeries to plot the temperature
        XYSeries series1 = new XYSeries("  Bluetooth Detected");


        // We start filling the series
        int hour = 0;

        for (int i = 0; i < timeList.size(); i++) {
            series1.add(timeList.get(i), processList.get(i));
        }

        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        renderer1.setLineWidth(2);
        renderer1.setColor(Color.BLUE);
        renderer1.setDisplayBoundingPoints(true);
        renderer1.setPointStyle(PointStyle.CIRCLE);
        renderer1.setPointStrokeWidth(3);


        // Now we add our series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series1);

        // Finaly we create the multiple series renderer to control the graph
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer1);
        mRenderer.setYAxisMin(0);
        mRenderer.setYAxisMax(2);

        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setShowGrid(true);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0,Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);


        mRenderer.setChartTitle("BluetoothData");
        mRenderer.setLabelsColor(Color.BLACK);
        GraphicalView chartView = ChartFactory.getScatterChartView(getActivity(), dataset, mRenderer);
        return chartView;
    }

    private void applyAnim(final View v, final View nextView) {

        Animation.AnimationListener list = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                chartLyt.removeViewAt(0);
                chartLyt.addView(nextView,0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        fadeAnim.setAnimationListener(list);
        v.startAnimation(fadeAnim);
    }
}