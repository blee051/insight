package com.insight.insight.utils;

import android.app.Notification;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;

/**
 * Created by CM on 2/21/15.
 */
public class NotificationParcel implements Parcelable {
    public String EXTRA_TITLE;
    public String EXTRA_TEXT;
    public Integer flags;
    public String category;
    public String PACKAGE_NAME;
    public Long POST_TIME;
    public String PACKAGE_GENER;


    public NotificationParcel(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();

        this.EXTRA_TITLE = n.extras.getCharSequence(Notification.EXTRA_TITLE).toString();
        if (n.extras.getCharSequence(Notification.EXTRA_TEXT) == null) {
            this.EXTRA_TEXT = "";
        } else {
            this.EXTRA_TEXT = n.extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        }

        this.flags = n.flags;
        /*Category was added in API Level 21 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.category = n.category;

        } else {
            this.category = "NA";
        }

        this.PACKAGE_NAME = sbn.getPackageName();
        this.POST_TIME = sbn.getPostTime();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(EXTRA_TITLE);
        dest.writeString(EXTRA_TEXT);
        dest.writeInt(flags);
        dest.writeString(category);
        dest.writeString(PACKAGE_NAME);
        dest.writeLong(POST_TIME);


    }

    public NotificationParcel(Parcel in) {
        this.EXTRA_TITLE = in.readString();
        this.EXTRA_TEXT = in.readString();
        this.flags = in.readInt();
        this.category = in.readString();
        this.PACKAGE_NAME = in.readString();
        this.POST_TIME = in.readLong();
    }

    public static final Creator<NotificationParcel> CREATOR = new Creator<NotificationParcel>() {
        @Override
        public NotificationParcel createFromParcel(Parcel source) {
            return new NotificationParcel(source);
        }

        @Override
        public NotificationParcel[] newArray(int size) {
            return new NotificationParcel[size];
        }
    };
}

