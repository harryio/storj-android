package com.harryio.storj.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

/**
 * Base class to create and handle notifications
 */
public abstract class AbstractNotification {
    protected final Context context;
    protected final NotificationManager notificationManager;
    protected Notification notification;

    public AbstractNotification(Context context) {
        this.context = context.getApplicationContext();
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    protected abstract int getNotificationId();

    public Notification getNotification() {
        return notification;
    }

    public void show() {
        notificationManager.notify(getNotificationId(), notification);
    }

    public void hide() {
        notificationManager.cancel(getNotificationId());
    }
}
