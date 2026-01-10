/**
    Copyright (C) 2025 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.suntimeswidget;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.support.app.NotificationManagerCompat;

import java.lang.ref.WeakReference;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final WeakReference<Context> contextRef;

    public ExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultHandler)
    {
        this.contextRef = new WeakReference<>(context);
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e)
    {
        try {
            Log.e("CRASH", e.getClass().getSimpleName(), e);
            Context context = contextRef.get();
            if (context != null)
            {
                NotificationManagerCompat notifications = NotificationManagerCompat.from(context);
                if (notifications != null && notifications.areNotificationsEnabled()) {
                    showCrashReportNotification(context, e);
                } else {
                    launchCrashReportActivity(context, e);
                }
            }

        } finally {
            defaultHandler.uncaughtException(t, e);
        }
    }

    private static String createCrashReport(Throwable e)
    {
        return "Suntimes " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" + (BuildConfig.DEBUG ? " [debug] " : " ") + "[" + BuildConfig.GIT_HASH + "]" +  "\n"
                + "on Android " + Build.VERSION.RELEASE +  " (" + Build.VERSION.SDK_INT + ")" + " [" + Build.MANUFACTURER + "]"
                + "\n\n" + Log.getStackTraceString(e);
    }

    private void showCrashReportNotification(Context context, Throwable e)
    {
        if (context != null)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager.areNotificationsEnabled())
            {
                CrashReportNotification builder = new CrashReportNotification();
                builder.setNotificationMessage(e.toString());
                Notification notification = builder.createNotification(context, createCrashReport(e));
                notificationManager.notify("CrashReport", CrashReportNotification.NOTIFICATION_ID, notification);
            }
        }
    }

    private void launchCrashReportActivity(Context context, Throwable e)
    {
        if (context != null) {
            context.startActivity(new CrashReportNotification().getCrashReportActivityIntent(context, createCrashReport(e)));
        } else {
            Log.e("CrashReport", "launchCrashReportActivity: null context!");
        }
    }

    /**
     * CrashReportNotification
     */
    private static class CrashReportNotification extends ExceptionNotification
    {
        public static final int NOTIFICATION_ID = -1000000;

        @Override
        protected String getChannelID() {
            return AlarmNotifications.CHANNEL_ID_MISC;
        }

        @Override
        protected String getChannelTitle(Context context) {
            return context.getString(R.string.notificationChannel_misc_title);
        }

        @Override
        protected String getChannelDesc(Context context) {
            return context.getString(R.string.notificationChannel_misc_title);
        }

        @Override
        protected Intent getCrashReportActivityIntent(Context context, String report)
        {
            if (context != null)
            {
                Intent intent = new Intent(context, ExceptionActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ExceptionActivity.EXTRA_REPORT, report);
                return intent;

            } else {
                Log.e("CRASH", "launchCrashReportActivity: null context!");
                return null;
            }
        }

        @Override
        protected String getNotificationTitle(Context context) {
            return context.getString(R.string.crash_dialog_message, context.getString(R.string.app_name));
        }

        @Override
        protected String getNotificationMessage(Context context) {
            return message;
        }
        public void setNotificationMessage(String value) {
            message = value;
        }
        private String message = "";

        @Override
        protected String getNotificationActionText(Context context) {
            return context.getString(R.string.crash_dialog_view);
        }

        @Override
        protected int getNotificationIconResID() {
            return R.drawable.ic_action_error_light;
        }
    }
}
