package com.forrestguice.suntimeswidget;

/**
    Copyright (C) 2017 Forrest Guice
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

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayoutEq;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1eq_0;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

/**
 *  Flippable widget
 */
public class SuntimesWidget2 extends SuntimesWidget0
{
    private static final int UPDATEALARM_ID = 2;

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity2.class;
    }

    /**
     * @return an update alarm identifier for this class (SuntimesWidget2: 2)
     */
    @Override
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget2.UPDATEALARM_ID;
    }

    /**
     * @return AlarmManager.INTERVAL_HOUR;
     */
    @Override
    protected long getUpdateInterval()
    {
        return UpdateInterval.INTERVAL_NORMAL.interval;
    }

    /**
     * @return
     */
    @Override
    protected long getUpdateTimeMillis()
    {
        Calendar updateTime = Calendar.getInstance();
        updateTime.add(Calendar.SECOND, 1);
        return updateTime.getTimeInMillis();
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {

        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesLayoutEq layout = SuntimesWidget2.getWidgetLayout(context, appWidgetManager, appWidgetId);
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SuntimesLayoutEq layout)
    {
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        SuntimesEquinoxSolsticeData data;
        boolean overrideMode = WidgetSettings.loadTimeMode2OverridePref(context, appWidgetId);
        if (overrideMode)
        {
            SuntimesEquinoxSolsticeDataset dataset = new SuntimesEquinoxSolsticeDataset(context, appWidgetId);
            dataset.calculateData();

            SuntimesEquinoxSolsticeData nextEvent = dataset.findSoonest(dataset.now());
            data = (nextEvent != null ? nextEvent : dataset.dataEquinoxVernal);

        } else {
            data = new SuntimesEquinoxSolsticeData(context, appWidgetId);
            data.calculate();
        }

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, SuntimesWidget2.class));
        layout.updateViews(context, appWidgetId, views, data);
        layout.themeViews(context, views, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static SuntimesLayoutEq getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        int minWidth = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        int minHeight = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        int[] mustFitWithinDp = {minWidth, minHeight};
        //Log.d("getWidgetLayout2", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            mustFitWithinDp[0] = Math.min( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.min( sizePortrait[1], sizeLandscape[1] );
            //Log.d("getWidgetLayout2", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
        }

        SuntimesLayoutEq layout;
        //if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        //{
            /**int minWidth1x3 = context.getResources().getInteger(R.integer.widget_size_minWidthDp1x3);
            layout = ((mustFitWithinDp[0] >= minWidth1x3) ? new SuntimesLayout_1x3_0()
                    : WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId));
            } else {
            layout = WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId); */
            //layout = new SuntimesLayout_1x1eq_0();  // TODO

        //} else {
            layout = new SuntimesLayout_1x1eq_0();
        //}

        //Log.d("getWidgetLayout2", "layout is: " + layout);
        return layout;
    }

    @Override
    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.SolsticeEquinoxMode.initDisplayStrings(context);
    }

    /**
     * UpdateInterval
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    public static enum UpdateInterval
    {
        INTERVAL_NORMAL(AlarmManager.INTERVAL_HOUR, -1),  // 1h
        INTERVAL_FAST(AlarmManager.INTERVAL_FIFTEEN_MINUTES, 24 * 60 * 60 * 1000),  // 15m, 24h
        INTERVAL_RAPID(5 * 60 * 1000, 1 * 60 * 60 * 1000),  // 5m, 1h
        INTERVAL_VERYRAPID(60 * 1000, 15 * 60 * 1000);      // 1m, 15m

        public long interval, threshold;

        private UpdateInterval(long interval, long threshold)
        {
            this.interval = interval;
            this.threshold = threshold;
        }

        public static UpdateInterval getInterval(long timeDelta)
        {
            if (timeDelta < INTERVAL_VERYRAPID.threshold)
            {
                return INTERVAL_VERYRAPID;

            } else if (timeDelta < INTERVAL_RAPID.threshold) {
                return INTERVAL_RAPID;

            } else if (timeDelta < INTERVAL_FAST.threshold) {
                return INTERVAL_FAST;

            } else {
                return INTERVAL_NORMAL;
            }
        }
    }


}
