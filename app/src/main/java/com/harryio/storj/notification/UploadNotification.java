package com.harryio.storj.notification;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.harryio.storj.R;

public class UploadNotification extends AbstractNotification {
    public static final int UPLOAD_NOTIFICATION_ID = 1;

    public UploadNotification(Context context) {
        super(context);
        update(1);
    }

    @Override
    protected int getNotificationId() {
        return UPLOAD_NOTIFICATION_ID;
    }

    public UploadNotification update(int numberOfItems) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setContentTitle(numberOfItems > 1 ? "Uploading files" : "Uploading file")
                .setProgress(0, 0, true);

        if (numberOfItems > 1) {
            builder.setContentText(numberOfItems + " queued");
        }

        notification = builder.build();
        return this;
    }
}
