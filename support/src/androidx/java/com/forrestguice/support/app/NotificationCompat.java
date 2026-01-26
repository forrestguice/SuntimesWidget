package com.forrestguice.support.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;

public class NotificationCompat //extends androidx.core.app.NotificationCompat
{
    public static final int BADGE_ICON_NONE = androidx.core.app.NotificationCompat.BADGE_ICON_NONE;
    public static final int BADGE_ICON_SMALL = androidx.core.app.NotificationCompat.BADGE_ICON_SMALL;
    public static final int BADGE_ICON_LARGE = androidx.core.app.NotificationCompat.BADGE_ICON_LARGE;

    public static final String CATEGORY_CALL = androidx.core.app.NotificationCompat.CATEGORY_CALL;
    public static final String CATEGORY_NAVIGATION = androidx.core.app.NotificationCompat.CATEGORY_NAVIGATION;
    public static final String CATEGORY_MESSAGE = androidx.core.app.NotificationCompat.CATEGORY_MESSAGE;
    public static final String CATEGORY_EMAIL = androidx.core.app.NotificationCompat.CATEGORY_EMAIL;
    public static final String CATEGORY_EVENT = androidx.core.app.NotificationCompat.CATEGORY_EVENT;
    public static final String CATEGORY_PROMO = androidx.core.app.NotificationCompat.CATEGORY_PROMO;
    public static final String CATEGORY_ALARM = androidx.core.app.NotificationCompat.CATEGORY_ALARM;
    public static final String CATEGORY_PROGRESS =androidx.core.app.NotificationCompat.CATEGORY_PROGRESS;
    public static final String CATEGORY_SOCIAL = androidx.core.app.NotificationCompat.CATEGORY_SOCIAL;
    public static final String CATEGORY_ERROR = androidx.core.app.NotificationCompat.CATEGORY_ERROR;
    public static final String CATEGORY_TRANSPORT = androidx.core.app.NotificationCompat.CATEGORY_TRANSPORT;
    public static final String CATEGORY_SYSTEM = androidx.core.app.NotificationCompat.CATEGORY_SYSTEM;
    public static final String CATEGORY_SERVICE = androidx.core.app.NotificationCompat.CATEGORY_SERVICE;
    public static final String CATEGORY_REMINDER = androidx.core.app.NotificationCompat.CATEGORY_REMINDER;
    public static final String CATEGORY_RECOMMENDATION = androidx.core.app.NotificationCompat.CATEGORY_RECOMMENDATION;
    public static final String CATEGORY_STATUS = androidx.core.app.NotificationCompat.CATEGORY_STATUS;
    public static final String CATEGORY_WORKOUT = androidx.core.app.NotificationCompat.CATEGORY_WORKOUT;
    public static final String CATEGORY_LOCATION_SHARING = androidx.core.app.NotificationCompat.CATEGORY_LOCATION_SHARING;
    public static final String CATEGORY_STOPWATCH = androidx.core.app.NotificationCompat.CATEGORY_STOPWATCH;
    public static final String CATEGORY_MISSED_CALL = androidx.core.app.NotificationCompat.CATEGORY_MISSED_CALL;

    public static final int COLOR_DEFAULT = androidx.core.app.NotificationCompat.COLOR_DEFAULT;

    public static final int PRIORITY_DEFAULT = androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;
    public static final int PRIORITY_LOW = androidx.core.app.NotificationCompat.PRIORITY_LOW;
    public static final int PRIORITY_MIN = androidx.core.app.NotificationCompat.PRIORITY_MIN;
    public static final int PRIORITY_HIGH = androidx.core.app.NotificationCompat.PRIORITY_HIGH;
    public static final int PRIORITY_MAX = androidx.core.app.NotificationCompat.PRIORITY_MAX;

    public static final int VISIBILITY_PUBLIC = androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
    public static final int VISIBILITY_PRIVATE = androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE;
    public static final int VISIBILITY_SECRET = androidx.core.app.NotificationCompat.VISIBILITY_SECRET;

    public static class Builder extends androidx.core.app.NotificationCompat.Builder
    {
        @TargetApi(19)
        public Builder(@NonNull Context context, @NonNull Notification notification) {
            super(context, notification);
        }
        public Builder(@NonNull Context context, @NonNull String channelId) {
            super(context, channelId);
        }
        @Deprecated
        public Builder(@NonNull Context context) {
            //noinspection deprecation
            super(context);
        }
    }

    public static class BigTextStyle extends androidx.core.app.NotificationCompat.BigTextStyle {}
}