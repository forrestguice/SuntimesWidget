package com.forrestguice.support.app;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.Nullable;

import com.forrestguice.annotation.NonNull;

public class NotificationManagerCompat
{
    protected androidx.core.app.NotificationManagerCompat notifications;

    public NotificationManagerCompat(Context context) {
        notifications = androidx.core.app.NotificationManagerCompat.from(context);
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

    public static final int IMPORTANCE_UNSPECIFIED = androidx.core.app.NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
    public static final int IMPORTANCE_NONE = androidx.core.app.NotificationManagerCompat.IMPORTANCE_NONE;
    public static final int IMPORTANCE_MIN = androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN;
    public static final int IMPORTANCE_LOW = androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW;
    public static final int IMPORTANCE_DEFAULT = androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
    public static final int IMPORTANCE_HIGH = androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;
    public static final int IMPORTANCE_MAX = androidx.core.app.NotificationManagerCompat.IMPORTANCE_MAX;
}