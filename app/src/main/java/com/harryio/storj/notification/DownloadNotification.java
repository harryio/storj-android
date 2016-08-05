package com.harryio.storj.notification;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.harryio.storj.R;

public class DownloadNotification extends AbstractNotification {
    public static final int DOWNLOAD_NOTIFICATION_ID = 3;

    public DownloadNotification(Context context) {
        super(context);
        update(0);
    }

    @Override
    protected int getNotificationId() {
        return DOWNLOAD_NOTIFICATION_ID;
    }

    public DownloadNotification update(int numOfFiles) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_cloud_download)
                .setContentTitle(numOfFiles > 1 ? "Downloading files" : "Downloading file")
                .setProgress(0, 0, true);

        if (numOfFiles > 1) {
            builder.setContentText(String.valueOf(numOfFiles - 1) + " queued");
        }

        notification = builder.build();
        return this;
    }
}
