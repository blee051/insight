package com.insight.insight.utils;

import android.app.Activity;

import com.insight.insight.R;
import com.insight.insight.ui.Activity_Actv;
import com.insight.insight.ui.AmbientLight_Actv;
import com.insight.insight.ui.AppUsage_Actv;
import com.insight.insight.ui.Battery_Actv;
import com.insight.insight.ui.Bluetooth_Actv;
import com.insight.insight.ui.HeartRate_Actv;
import com.insight.insight.ui.MainListItem;
import com.insight.insight.ui.Notifications_Actv;
import com.insight.insight.ui.Temp_Actv;

import java.util.ArrayList;

/**
 * Created by MN on 3/11/2015.
 */
public class MenuItems {
    Activity context = new Activity();

    public MenuItems(Activity context) {
        this.context = context;
    }

    // Create the list view items to be displayed in the homescreen Listview
    public MainListItem[] createMeListItems() {
        ArrayList<MainListItem> listArray = new ArrayList<MainListItem>();
        MainListItem meTitle = new MainListItem(context.getString(R.string.me_title),
                null, null);
       // listArray.add(meTitle);
        MainListItem activityItem = new MainListItem(
                context.getResources().getString(R.string.activity_title),
                R.drawable.ic_activity, Activity_Actv.class);//ActivitySensor.class
        listArray.add(activityItem);
        MainListItem heartrateItem = new MainListItem(
                context.getString(R.string.heart_rate_title),
                R.drawable.ic_heart, HeartRate_Actv.class);
        listArray.add(heartrateItem);
        MainListItem appUsageItem = new MainListItem(
                context.getString(R.string.app_usage_title),
                R.drawable.ic_appusage, AppUsage_Actv.class);

        listArray.add(appUsageItem);
        listArray.addAll(createSystemListItems());

        return listArray.toArray(new MainListItem[listArray.size()]);
    }

    public ArrayList<MainListItem> createSystemListItems() {
        ArrayList<MainListItem> systemItemList = new ArrayList<MainListItem>();
        MainListItem systemTitle = new MainListItem(
                context.getString(R.string.system_title),
                null, null);
        //systemItemList.add(systemTitle);

        MainListItem battItem = new MainListItem(
                context.getString(R.string.battery_title),
                R.drawable.ic_battery,
                Battery_Actv.class);//BatteryLevelActivity.class

        systemItemList.add(battItem);

        MainListItem notifItem = new MainListItem(
                context.getString(R.string.notification_title),
                R.drawable.ic_notif,
                Notifications_Actv.class);
        systemItemList.add(notifItem);

        if (FeatureCheck.hasBluetoothFeature(context)) {
            // create MainListItem bluetooth

            MainListItem bluetoothItem = new MainListItem(
                    context.getString(R.string.bluetooth_title),
                    R.drawable.ic_bluetooth, Bluetooth_Actv.class);
            systemItemList.add(bluetoothItem);
        }
        if (FeatureCheck.hasLightFeature(context)) {
            MainListItem ambLightItem = new MainListItem(
                    context.getString(R.string.amblight_title),
                    R.drawable.ic_light, AmbientLight_Actv.class);
            systemItemList.add(ambLightItem);
        }

        // Temp Debugger
            MainListItem ambLightItem = new MainListItem(
                    "Debugger",
                    R.drawable.ic_debugger, Temp_Actv.class);
            systemItemList.add(ambLightItem);


        return systemItemList;
    }
}
