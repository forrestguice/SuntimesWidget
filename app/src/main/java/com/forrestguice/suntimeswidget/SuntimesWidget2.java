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

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayoutEq;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1eq_0;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 *  Flippable widget
 */
public class SuntimesWidget2 extends SuntimesWidget
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

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    /**@Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected boolean handleClickAction(Context context, Intent intent)
    {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        int appWidgetId = (extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) : 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            if (action.equals(WidgetSettings.ActionMode.ONTAP_FLIPTO_NEXTITEM.name()))
            {
                RemoteViews views = getWidgetViews(context, null, appWidgetId);
                views.showNext(R.id.view_flip);
                AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views);
                return true;
            }
        }

        return super.handleClickAction(context, intent);
    }*/

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

        layout.themeViews(context, views, appWidgetId);

        SuntimesEquinoxSolsticeData data = new SuntimesEquinoxSolsticeData(context, appWidgetId); // constructor inits data from widget settings
        data.calculate();

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget.clickActionIntent(context, appWidgetId, SuntimesWidget2.class));
        layout.updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static SuntimesLayoutEq getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        int minWidth = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        int minHeight = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        int[] mustFitWithinDp = {minWidth, minHeight};
        Log.d("getWidgetLayout2", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int[]  sizePortrait = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),   // dp values
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) };
            int[]  sizeLandscape = { widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                    widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) };

            mustFitWithinDp[0] = Math.min( sizePortrait[0], sizeLandscape[0] );
            mustFitWithinDp[1] = Math.min( sizePortrait[1], sizeLandscape[1] );
            Log.d("getWidgetLayout2", "1: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");
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

        Log.d("getWidgetLayout2", "layout is: " + layout);
        return layout;
    }

}
