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
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.layouts.SolsticeLayout;
import com.forrestguice.suntimeswidget.layouts.SolsticeLayout_1x1_0;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

/**
 *  Flippable widget
 */
public class SolsticeWidget0 extends SuntimesWidget0
{
    public static final String SOLSTICE_WIDGET_UPDATE = "suntimes.SOLSTICE_WIDGET_UPDATE";
    private static final int UPDATEALARM_ID = 3;

    @Override
    protected Class getConfigClass()
    {
        return SolsticeWidget0ConfigActivity.class;
    }

    @Override
    protected String getUpdateIntentFilter()
    {
        return SolsticeWidget0.SOLSTICE_WIDGET_UPDATE;
    }

    /**
     * @return an update alarm identifier for this class (SolsticeWidget0: 3)
     */
    @Override
    protected int getUpdateAlarmId()
    {
        return SolsticeWidget0.UPDATEALARM_ID;
    }

    @Override
    protected long getUpdateInterval()
    {
        return AlarmManager.INTERVAL_HOUR * 3;
    }

    @Override
    protected long getUpdateTimeMillis()
    {
        Calendar updateTime = Calendar.getInstance();
        updateTime.add(Calendar.SECOND, (int)(getUpdateInterval() / 1000));
        return updateTime.getTimeInMillis();
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {

        SolsticeWidget0.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SolsticeLayout layout = SolsticeWidget0.getWidgetLayout(context, appWidgetManager, appWidgetId);
        SolsticeWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SolsticeLayout layout)
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

            WidgetSettings.TrackingMode trackingMode = WidgetSettings.loadTrackingModePref(context, appWidgetId);
            SuntimesEquinoxSolsticeData nextEvent = (trackingMode == WidgetSettings.TrackingMode.SOONEST ? dataset.findSoonest(dataset.now())
                                                                                                         : dataset.findClosest(dataset.now()));
            data = (nextEvent != null ? nextEvent : dataset.dataEquinoxVernal);

        } else {
            data = new SuntimesEquinoxSolsticeData(context, appWidgetId);
            data.calculate();
        }

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, SolsticeWidget0.class));
        layout.prepareForUpdate(data);
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static SolsticeLayout getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        //int minWidth = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        //int minHeight = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        //int[] mustFitWithinDp = {minWidth, minHeight};
        //Log.d("getWidgetLayout2", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        /**if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            mustFitWithinDp[0] = Math.min( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.min( sizePortrait[1], sizeLandscape[1] );
            //Log.d("getWidgetLayout2", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
        }*/

        SolsticeLayout layout;
        //if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        //{
            /**int minWidth1x3 = context.getResources().getInteger(R.integer.widget_size_minWidthDp1x3);
            layout = ((mustFitWithinDp[0] >= minWidth1x3) ? new SuntimesLayout_1x3_0()
                    : WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId));
            } else {
            layout = WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId); */
            //layout = new SolsticeLayout_1x1_0();  // TODO

        //} else {
            layout = new SolsticeLayout_1x1_0();
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
     * enum               interval  intended for                                example
     *----------------------------------------------------------------------------------------------
     * INTERVAL_SLOW      .. 1d  .. time shown is in years or weeks          .. e.g. 1y 2w from now
     * INTERVAL_NORMAL    .. 12h .. time shown is less than a year           .. e.g. 14w 5d from now
     * INTERVAL_FAST      .. 15m .. time shown is less than a week           .. e.g. 4d 16h from now
     * INTERVAL_VERYFAST  .. 5m  .. time shown is less than a day            .. e.g. 16h 48m from now
     * INTERVAL_RAPID     .. 1m  .. time shown is less than an hour          .. e.g. 45m from now
     * INTERVAL_VERYRAPID .. 5s  .. time shown is less than a minute         .. e.g. 30s from now
     */
    /**public static enum UpdateInterval
    {
        INTERVAL_SLOW( ONE_DAY, -1 ),
        INTERVAL_NORMAL( HALF_DAY, ONE_YEAR ),
        INTERVAL_FAST( FIFTEEN_MINUTES, ONE_WEEK ),
        INTERVAL_VERYFAST( FIVE_MINUTES, ONE_DAY ),
        INTERVAL_RAPID( ONE_MINUTE, ONE_HOUR ),
        INTERVAL_VERYRAPID( FIVE_SECONDS, ONE_MINUTE );

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
            } else if (timeDelta < INTERVAL_VERYFAST.threshold) {
                return INTERVAL_VERYFAST;
            } else if (timeDelta < INTERVAL_FAST.threshold) {
                return INTERVAL_FAST;
            } else if (timeDelta < INTERVAL_NORMAL.threshold) {
                return INTERVAL_NORMAL;
            } else {
                return INTERVAL_SLOW;
            }
        }
    }

    private static final long ONE_YEAR = 365 * AlarmManager.INTERVAL_DAY;
    private static final long ONE_WEEK = 7 * AlarmManager.INTERVAL_DAY;
    private static final long ONE_DAY = AlarmManager.INTERVAL_DAY;
    private static final long HALF_DAY = AlarmManager.INTERVAL_HALF_DAY;
    private static final long ONE_HOUR = AlarmManager.INTERVAL_HOUR;
    private static final long FIFTEEN_MINUTES = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final long ONE_MINUTE = 1 * 60 * 1000;
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final long FIVE_SECONDS = 1 * 5 * 1000;*/

}
