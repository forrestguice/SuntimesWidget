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

    public void notify(@Nullable String tag, int id, @NonNull Notification notification) {
        notifications.notify(tag, id, notification);
    }
}