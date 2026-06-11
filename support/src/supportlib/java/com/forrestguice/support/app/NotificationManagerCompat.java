package com.forrestguice.support.app;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.Nullable;

import com.forrestguice.annotation.NonNull;

public class NotificationManagerCompat
{
    protected android.support.v4.app.NotificationManagerCompat notifications;

    public NotificationManagerCompat(Context context) {
        notifications = android.support.v4.app.NotificationManagerCompat.from(context);
    }

    public static NotificationManagerCompat from(Context context) {
        return new NotificationManagerCompat(context);
    }

    public boolean areNotificationsEnabled() {
        return notifications.areNotificationsEnabled();
    }

    public void notify(int id, @NonNull android.app.Notification notification) {
        notifications.notify(id, notification);
    }
    public void notify(@Nullable String tag, int id, @NonNull Notification notification) {
        notifications.notify(tag, id, notification);
    }

    public void cancel(int id) {
        notifications.cancel(id);
    }
    public void cancel(@Nullable String tag, int id) {
        notifications.cancel(tag, id);
    }
    public void cancelAll() {
        notifications.cancelAll();
    }

    public int getImportance() {
        return notifications.getImportance();
    }

    public static final int IMPORTANCE_UNSPECIFIED = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
    public static final int IMPORTANCE_NONE = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_NONE;
    public static final int IMPORTANCE_MIN = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MIN;
    public static final int IMPORTANCE_LOW = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_LOW;
    public static final int IMPORTANCE_DEFAULT = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
    public static final int IMPORTANCE_HIGH = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
    public static final int IMPORTANCE_MAX = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MAX;
}