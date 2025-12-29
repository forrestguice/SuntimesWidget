package com.forrestguice.suntimeswidget.widgets;

/**
    Copyright (C) 2024 Forrest Guice
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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;

import java.util.Calendar;

/**
 *  Alarm widget
 */
public class AlarmWidget0 extends SuntimesWidget0
{
    public static final String ALARM_WIDGET_UPDATE = "suntimes.ALARM_WIDGET_UPDATE";

    @Override
    protected Class getConfigClass() {
        return AlarmWidget0ConfigActivity.class;
    }

    @Override
    protected String getUpdateIntentFilter() {
        return AlarmWidget0.ALARM_WIDGET_UPDATE;
    }

    @Override
    protected long getUpdateInterval() {
        return 1000 * 60 * 60;  // every hour
    }

    @Override
    protected void onAlarmUpdateUIBroadcast(Context context) {
        updateWidgets(context);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        AlarmLayout defLayout = AlarmWidgetSettings.loadAlarm1x1ModePref_asLayout(context, appWidgetId);
        AlarmWidget0.updateAppWidget(context, new AppWidgetManagerWrapper(appWidgetManager), appWidgetId, AlarmWidget0.class, getMinSize(context), defLayout);
    }

    protected static void updateAppWidget(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, Class widgetClass, int[] defSize, AlarmLayout defLayout)
    {
        AlarmLayout layout = AlarmWidget0.getWidgetLayout(context, appWidgetManager, appWidgetId, defSize, defLayout);
        AlarmWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, layout, widgetClass);
    }

    protected static void updateAppWidget(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, AlarmLayout layout, Class widgetClass)
    {
        SuntimesClockData data = new SuntimesClockData(context, appWidgetId);
        data.calculate(context);
        layout.prepareForUpdate(context, appWidgetId, data);
        RemoteViews views = layout.getViews(context);

        Intent intentTemplate = AlarmNotifications.getAlarmListIntent(context, null);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.list_alarms, pendingIntent);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);
        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, widgetClass));
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(context, appWidgetId, views);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_alarms);

        Calendar nextUpdate = Calendar.getInstance();
        nextUpdate.setTimeInMillis(data.calendar().getTimeInMillis());
        nextUpdate.add(Calendar.HOUR, 1);   // up to an hour from now
        nextUpdate.set(Calendar.SECOND, 1);
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate.getTimeInMillis());
    }

    @Override
    protected SuntimesData getData(Context context, int appWidgetId) {
        return new SuntimesClockData(context, appWidgetId);
    }

    @Override
    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
    }

    protected static AlarmLayout getWidgetLayout(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, int[] defSize, AlarmLayout defLayout)
    {
        int[] mustFitWithinDp = widgetSizeDp(context, appWidgetManager, appWidgetId, defSize);
        AlarmLayout layout;
        if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        {
            int minWidth3x1 = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
            int minWidth2x1 = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
            layout = (mustFitWithinDp[0] >= minWidth3x1) ? AlarmWidgetSettings.loadAlarm3x2ModePref_asLayout(context, appWidgetId)
                    : ((mustFitWithinDp[0] >= minWidth2x1) ? AlarmWidgetSettings.loadAlarm2x2ModePref_asLayout(context, appWidgetId)
                        : AlarmWidgetSettings.loadAlarm1x1ModePref_asLayout(context, appWidgetId));
        } else {
            layout = defLayout;
        }
        layout.setMaxDimensionsDp(widgetSizeDp(context, appWidgetManager, appWidgetId, defSize));
        layout.setCategory(widgetCategory(appWidgetManager, appWidgetId));
        //Log.d("getWidgetLayout", "layout is: " + layout);
        return layout;
   }

    public static AlarmClockItem loadAlarmClockItem(Context context, long rowID)
    {
        long bench_start = System.nanoTime();
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
        db.open();
        AlarmClockItem item = AlarmDatabaseAdapter.AlarmItemTask.loadAlarmClockItem(context, db, rowID);
        db.close();
        long bench_end = System.nanoTime();
        //Log.d("DEBUG", "load single alarm item takes " + ((bench_end - bench_start) / 1000000.0) + " ms");
        return item;
    }

    public static Long findUpcomingAlarmId(Context context, long now, String[] types)
    {
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
        db.open();
        Long rowId = db.findUpcomingAlarmId(now, types);
        db.close();
        return rowId;
    }

}
