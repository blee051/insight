package com.insight.insight.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.insight.insight.R;
import com.insight.insight.common.Setting;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.utils.IOManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MN on 2/13/2015.
 */
public class Temp_Actv extends Activity {
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
        tvTitle.setText("Temp Debugger");


        lastDataFilesList = ioManager.getLastFilesInDir(Setting.dataFolderName_Temp, Setting.linksButtonCount);
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

        TextView tvLog = new TextView(this);
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(getSizeInDP(200), ViewGroup.LayoutParams.WRAP_CONTENT);
        cParams.gravity = (Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        tvLog.setLayoutParams(cParams);
        tvLog.setText(getFileContent(date));
        tvLog.setTextSize(7.5f);
        frameBox.addView(tvLog, 1);


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

    private String getFileContent(Date date) {
        String result = "";
        try {
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ioManager.getDataFolderFullPath(Setting.dataFolderName_Temp) + Setting.filenameFormat.format(date) + ".txt")));
            try {
                while ((sCurrentLine = br.readLine()) != null) {
                    Object[] decodedRow = jsonUtil.decodeTempDebuggerLog(sCurrentLine);// [0]:Date, [1]:Tag, [2]:Log
                    if (decodedRow != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss"); // return just hours of timestamp
                        result += timeFormat.format((Date) decodedRow[0]);
                        result += " " + decodedRow[1];
                        result += " " + decodedRow[2];
                        result += "\n";
                    }
                }

            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int getSizeInDP(int x) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, getResources().getDisplayMetrics());
    }
}
