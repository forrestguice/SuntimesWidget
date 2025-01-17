package com.forrestguice.support.design.app;

import android.content.Context;
import android.support.annotation.NonNull;

public class NotificationManagerCompat
{
    public static final int IMPORTANCE_NONE = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_NONE;
    public static final int IMPORTANCE_MIN = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MIN;
    public static final int IMPORTANCE_LOW = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_LOW;
    public static final int IMPORTANCE_DEFAULT = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
    public static final int IMPORTANCE_HIGH = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
    public static final int IMPORTANCE_MAX = android.support.v4.app.NotificationManagerCompat.IMPORTANCE_MAX;

    @android.support.annotation.NonNull
    public static android.support.v4.app.NotificationManagerCompat from(@NonNull Context context) {
        return android.support.v4.app.NotificationManagerCompat.from(context);
    }
}