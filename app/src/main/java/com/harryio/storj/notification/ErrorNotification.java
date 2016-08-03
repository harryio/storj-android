package com.harryio.storj.notification;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.harryio.storj.R;

public class ErrorNotification extends AbstractNotification {
    private static final int ERROR_NOTIFICATION_ID = 3;

    private NotificationCompat.Builder builder;

    public ErrorNotification(Context context) {
        super(context);
        builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_error)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    @Override
    protected int getNotificationId() {
        return ERROR_NOTIFICATION_ID;
    }

    public ErrorNotification setError(String errorMessage) {
        builder.setContentText(errorMessage);
        notification = builder.build();

        return this;
    }
}
