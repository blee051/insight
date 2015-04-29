package com.insight.insight.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.insight.insight.R;
import com.insight.insight.common.Setting;
import com.insight.insight.data.JSONUtil;
import com.insight.insight.ui.adapters.MultipleListItemsAdapter;
import com.insight.insight.utils.IOManager;
import com.insight.insight.utils.RowData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Notifications_Actv extends Activity {
    private static final String LOG_TAG = Notifications_Actv.class.getSimpleName();
    private MultipleListItemsAdapter mAdapter;
    TextView tvDate = null;
    JSONUtil jsonUtil = new JSONUtil();
    IOManager ioManager = new IOManager();
    File[] lastDataFilesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalist);
        tvDate = (TextView) findViewById(R.id.tvDate);

        //set Title of activity
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.title_activity_wnotifications);

        lastDataFilesList = ioManager.getLastFilesInDir(Setting.dataFolderName_Notifications, Setting.linksButtonCount);
        if (lastDataFilesList != null && lastDataFilesList.length > 0) {
            Date date = ioManager.parseDataFilename2Date(lastDataFilesList[0].getName());
            displayDataList(date);
        } else {
            tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
            TextView tvMessage = new TextView(this);
            tvMessage.setGravity(Gravity.CENTER_HORIZONTAL);
            tvMessage.setText(getResources().getString(R.string.message_nodata));
            RelativeLayout.LayoutParams tvMessageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvMessageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tvMessageParams.topMargin = 60;
            tvMessage.setLayoutParams(tvMessageParams);
            RelativeLayout frameBox = (RelativeLayout) tvTitle.getParent().getParent();
            frameBox.removeViewsInLayout(1, frameBox.getChildCount() - 1); // remove all views except title
            frameBox.addView(tvMessage, 1);
        }

        //-------------------------------- pop up a prediction on watch screen
        /*
        PredictionNotification predictionNotification = new PredictionNotification();
        predictionNotification.show(this, "Caution", "Your battery is going to drain faster than average!");
        Toast.makeText(this, "Prediction Created!", Toast.LENGTH_SHORT).show();
        */
    }

    public void displayDataList(Date date) {
        tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(date));
        mAdapter = new MultipleListItemsAdapter(this);

        //generate sample data
        for (RowData rData : getDataFromFile(date)) {
            mAdapter.addItem(rData);
        }

        // create links to datefiles
        for (File file : lastDataFilesList) {
            final Date tmpDate = ioManager.parseDataFilename2Date(file.getName());
            mAdapter.addLinkButtonItem(new SimpleDateFormat("MM/dd/yyyy").format(tmpDate));
        }

        //set adapter to listView
        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(mAdapter);

        // list item's 'clickable' property on activity is disabled. User can only click on 'LinkButtons'
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedDate = view.findViewById(R.id.tvLinkText).getTag().toString();
                try {
                    Date date = new SimpleDateFormat("MM/dd/yyyy").parse(selectedDate);
                    displayDataList(date);

                } catch (ParseException e) {
                }
            }
        });
    }

    public List<RowData> getDataFromFile(Date date) {
        List<RowData> rDatas = new ArrayList<RowData>();
        try {
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ioManager.getDataFolderFullPath(Setting.dataFolderName_Notifications) + Setting.filenameFormat.format(date) + ".txt")));
            try {
                while ((sCurrentLine = br.readLine()) != null) {
                    Object[] decodedRow = jsonUtil.decodeNotification(sCurrentLine); // [0]:date, [1]:pkg name, [2]:title, [3]:flags, [4]:category
                    if (decodedRow != null) {
                        Date rowDate = (Date) decodedRow[0];
                        String rowTitle = decodedRow[2].toString();
                        RowData rData = new RowData(rowDate, rowTitle, R.drawable.ic_bar_bullet);
                        rDatas.add(rData);
                        // Log.d(">>", "ts:" + rowDate.toString() + ", st:" + rowState);
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rDatas;
    }
}