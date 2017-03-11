/**
    Copyright (C) 2014 Forrest Guice
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

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x3_0;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.util.Calendar;

/**
 * Main widget
 */
public class SuntimesWidget extends AppWidgetProvider
{
    public static String SUNTIMES_WIDGET_UPDATE = "SUNTIMES_WIDGET_UPDATE";
    public static final int UPDATEALARM_ID = 0;

    protected static SuntimesUtils utils = new SuntimesUtils();

    /**
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        initLocale(context);
        updateWidget(context, appWidgetManager, appWidgetId);
    }

    /**
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent)
    {
        super.onReceive(context, intent);
        initLocale(context);

        String action = intent.getAction();
        if (action.equals(SUNTIMES_WIDGET_UPDATE))
        {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            onUpdate(context, widgetManager, widgetIds);

        } else if (!action.equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
            handleClickAction(context, intent);
        }
    }

    /**
     * @param context
     * @param intent
     * @return
     */
    protected boolean handleClickAction(Context context, Intent intent)
    {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        int appWidgetId = (extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) : 0);

        // OnTap: Ignore
        if (action.equals(WidgetSettings.ActionMode.ONTAP_DONOTHING.name()))
        {
            return false;
        }

        // OnTap: Launch an Activity
        if (action.equals(WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY.name()))
        {
            String launchClassName = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
            Class<?> launchClass;
            try {
                launchClass = Class.forName(launchClassName);

            } catch (ClassNotFoundException e) {
                launchClass = getConfigClass();
                Log.e("SuntimesWidget", "LaunchApp :: " + launchClassName + " cannot be found! " + e.toString());
            }

            Intent launchIntent = new Intent(context, launchClass);
            launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return true;
        }

        // OnTap: Reconfigure the Widget
        if (action.equals(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name()))
        {
            Intent configIntent = new Intent(context, getConfigClass());
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            configIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);
            context.startActivity(configIntent);
            return true;
        }

        //Log.w("SuntimesWidget", "Unsupported click action: " + action + " (" + appWidgetId + ")");
        return false;
    }

    /**
     * @return the activity (class) to be used when configuring this widget
     */
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity.class;
    }

    /**
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        initLocale(context);
        WidgetThemes.initThemes(context);

        for (int appWidgetId : appWidgetIds)
        {
            updateWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.TimeMode.initDisplayStrings(context);
    }

    /**
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            WidgetSettings.deletePrefs(context, appWidgetId);
        }
    }

    /**
     * @param context
     */
    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        setUpdateAlarm(context);
    }

    /**
     * @param context
     */
    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        unsetUpdateAlarm(context);
    }

    /**
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected static SuntimesLayout getWidgetLayout( Context context, AppWidgetManager appWidgetManager, int appWidgetId )
    {
        int minWidth = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        int minHeight = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        int[] mustFitWithinDp = {minWidth, minHeight};
        Log.d("getWidgetLayout", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                                     widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            //Log.d("updateAppWidget", "portrait:  [" + sizePortrait[0] + ", " + sizePortrait[1] + "]");
            //Log.d("updateAppWidget", "landscape: [" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]");
            //Toast toast = Toast.makeText(context, "[" + sizePortrait[0] + ", " + sizePortrait[1] + "]; " + "[" + sizeLandscape[0] + ", " + sizeLandscape[1] + "]", Toast.LENGTH_SHORT);
            //toast.show();

            mustFitWithinDp[0] = Math.min( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.min( sizePortrait[1], sizeLandscape[1] );
            Log.d("getWidgetLayout", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
        }

        SuntimesLayout layout;
        if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        {
            int minWidth1x3 = context.getResources().getInteger(R.integer.widget_size_minWidthDp1x3);
            layout = ((mustFitWithinDp[0] >= minWidth1x3) ? new SuntimesLayout_1x3_0()
                                                          : WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId));
        } else {
            layout = WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId);
        }
        Log.d("getWidgetLayout", "layout is: " + layout);
        return layout;
    }

    /**
     * @param context the application context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of widget to be updated
     */
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesLayout layout = getWidgetLayout(context, appWidgetManager, appWidgetId);
        SuntimesWidget.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    /**
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param layout
     */
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SuntimesLayout layout)
    {
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        layout.themeViews(context, views, appWidgetId);

        SuntimesRiseSetData data = new SuntimesRiseSetData(context, appWidgetId); // constructor inits data from widget settings
        data.calculate();

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget.clickActionIntent(context, appWidgetId, SuntimesWidget.class));
        layout.updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * A static method for triggering an update of all widgets using ACTION_APPWIDGET_UPDATE intent;
     * triggers the onUpdate method.
     *
     * @param context
     * @param widgetClass the widget class (SuntimesWidget.class, SuntimesWidget1.class)
     */
    public static void triggerWidgetUpdate(Context context, Class widgetClass)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, widgetClass));

        Intent updateIntent = new Intent(context, widgetClass);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Start widget updates; register a daily alarm (inexactRepeating) that does not wake the device.
     * @param context
     */
    protected void setUpdateAlarm( Context context )
    {
        long updateInterval = getUpdateInterval();
        long updateTime = getUpdateTimeMillis();
        PendingIntent alarmIntent = getUpdateIntent(context);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, updateTime, updateInterval, alarmIntent);
        Log.d("DEBUG", "set update alarm: " + updateTime + "(" + updateInterval + ") --> " + alarmIntent);
    }

    /**
     * Stop widget updates; unregisters the update alarm.
     * @param context
     */
    protected void unsetUpdateAlarm( Context context )
    {
        PendingIntent alarmIntent = getUpdateIntent(context);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
        Log.d("DEBUG", "unset update alarm --> " + alarmIntent);
    }

    protected void resetUpdateAlarm( Context context )
    {
        unsetUpdateAlarm(context);
        setUpdateAlarm(context);
    }

    /**
     * @return time of update event (in millis); next midnight
     */
    protected long getUpdateTimeMillis()
    {
        Calendar updateTime = Calendar.getInstance();
        updateTime.set(Calendar.MILLISECOND, 0);
        updateTime.set(Calendar.MINUTE, 0);
        updateTime.set(Calendar.SECOND, 0);
        updateTime.set(Calendar.HOUR_OF_DAY, 0);
        updateTime.add(Calendar.DAY_OF_MONTH, 1);
        return updateTime.getTimeInMillis();
    }

    /**
     * @return an AlarmManager interval; e.g. AlarmManager.INTERVAL_DAY
     */
    protected long getUpdateInterval()
    {
        return AlarmManager.INTERVAL_DAY;
    }

    /**
     * @param context
     * @return a SUNTIMES_WIDGET_UPDATE broadcast intent for widget alarmId (@see getUpdateAlarmId)
     */
    protected PendingIntent getUpdateIntent(Context context)
    {
        return PendingIntent.getBroadcast(context, getUpdateAlarmId(), new Intent(SUNTIMES_WIDGET_UPDATE), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * @return an update alarm identifier for this class (SuntimesWidget: 0)
     */
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget.UPDATEALARM_ID;
    }

    /**
     * @param context
     * @param appWidgetId
     * @param widgetClass
     * @return
     */
    public static PendingIntent clickActionIntent(Context context, int appWidgetId, Class widgetClass)
    {
        WidgetSettings.ActionMode actionMode = WidgetSettings.loadActionModePref(context, appWidgetId);
        Intent actionIntent = new Intent(context, widgetClass);
        actionIntent.setAction(actionMode.name());
        actionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, actionIntent, 0);
    }

}


