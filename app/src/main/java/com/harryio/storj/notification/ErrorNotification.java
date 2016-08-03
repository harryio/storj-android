package com.harryio.storj.notification;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.harryio.storj.R;

public class ErrorNotification extends AbstractNotification {
    private NotificationCompat.Builder builder;
    private int notificationId;

    public ErrorNotification(Context context, int notificationId) {
        super(context);
        builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_error)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        this.notificationId = notificationId;
    }

    @Override
    protected int getNotificationId() {
        return notificationId;
    }

    public ErrorNotification setError(String errorMessage) {
        builder.setContentText(errorMessage);
        notification = builder.build();

        return this;
    }
}
