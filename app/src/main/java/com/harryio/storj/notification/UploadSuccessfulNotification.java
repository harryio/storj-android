package com.harryio.storj.notification;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.harryio.storj.R;

public class UploadSuccessfulNotification extends AbstractNotification {
    private static final int UPLOAD_SUCCESSFUL_NOTIFICATION_ID = 2;

    public UploadSuccessfulNotification(Context context) {
        super(context);
    }

    @Override
    protected int getNotificationId() {
        return UPLOAD_SUCCESSFUL_NOTIFICATION_ID;
    }

    public UploadSuccessfulNotification update(int numberOfItems) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_cloud_done)
                .setContentTitle(numberOfItems > 1 ? "Upload files" : "Upload file")
                .setContentText(numberOfItems > 1 ?
                        numberOfItems + " files successfully uploaded" :
                        "File successfully uploaded");

        notification = builder.build();
        return this;
    }
}
