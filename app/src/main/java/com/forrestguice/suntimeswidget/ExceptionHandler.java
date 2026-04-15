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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.app.NotificationManagerCompat;

import java.lang.ref.WeakReference;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    @Nullable
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final WeakReference<Context> contextRef;

    public ExceptionHandler(Context context, @Nullable Thread.UncaughtExceptionHandler defaultHandler)
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
                String crashReport = createCrashReport(e);
                NotificationManagerCompat notifications = NotificationManagerCompat.from(context);
                //noinspection ConstantConditions
                if (notifications != null && notifications.areNotificationsEnabled()) {
                    showCrashReportNotification(context, e, crashReport);
                } else {
                    launchCrashReportActivity(context, e, crashReport);
                }
                saveLastCrashReport(context, crashReport);
            }

        } finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(t, e);
            }
        }
    }

    private static String createCrashReport(Throwable e)
    {
        return "Suntimes " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" + (BuildConfig.DEBUG ? " [debug] " : " ") + "[" + BuildConfig.GIT_HASH + "]" +  "\n"
                + "on Android " + Build.VERSION.RELEASE +  " (" + Build.VERSION.SDK_INT + ")" + " [" + Build.MANUFACTURER + "]"
                + "\n\n" + Log.getStackTraceString(e);
    }

    private void showCrashReportNotification(Context context, Throwable e, String crashReport)
    {
        if (context != null)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager.areNotificationsEnabled())
            {
                CrashReportNotification builder = new CrashReportNotification();
                builder.setNotificationMessage(e.toString());
                Notification notification = builder.createNotification(context, crashReport);
                notificationManager.notify("CrashReport", CrashReportNotification.NOTIFICATION_ID, notification);
            }
        }
    }

    private void launchCrashReportActivity(Context context, Throwable e, String crashReport)
    {
        if (context != null) {
            context.startActivity(getCrashReportActivityIntent(context, crashReport));

        } else {
            Log.e("CrashReport", "launchCrashReportActivity: null context!");
        }
    }

    @Nullable
    public static Intent getCrashReportActivityIntent(Context context, String report) {
        return new CrashReportNotification().getCrashReportActivityIntent(context, report);
    }

    /**
     * Last Crash Report
     */

    private static final String PREF_KEY_REPORT = "crash_report";
    private static final String PREF_KEY_DATE = "crash_report_date";

    @SuppressLint("ApplySharedPref")
    public static void saveLastCrashReport(Context context, String report)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_REPORT, report);
        prefs.putLong(PREF_KEY_DATE, System.currentTimeMillis());
        prefs.commit();   // wait for completion or report may not be saved
    }
    public static void clearLastCrashReport(Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PREF_KEY_REPORT);
        prefs.remove(PREF_KEY_DATE);
        prefs.apply();
    }
    @Nullable
    public static String getLastCrashReport(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_REPORT, null);
    }
    public static long getLastCrashReportDate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_DATE, -1);
    }
    public static boolean hasLastCrashReport(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains(PREF_KEY_REPORT);
    }

    /**
     * CrashReportNotification
     */
    private static class CrashReportNotification extends ExceptionNotification
    {
        public static final String CHANNEL_ID = "suntimes.channel.crashreport";
        public static final int NOTIFICATION_ID = -1000000;

        @Override
        protected String getChannelID() {
            return CHANNEL_ID;
        }

        @Override
        protected String getChannelTitle(Context context) {
            return context.getString(R.string.notificationChannel_crashreport_title);
        }

        @Override
        protected String getChannelDesc(Context context) {
            return context.getString(R.string.notificationChannel_crashreport_desc);
        }

        @Nullable
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
