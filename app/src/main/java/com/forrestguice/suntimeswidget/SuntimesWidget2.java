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

package com.forrestguice.suntimeswidget;

import android.app.AlarmManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.layouts.SunExtLayout;
import com.forrestguice.suntimeswidget.layouts.SunExtLayout_1x1_0;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class SuntimesWidget2 extends SuntimesWidget0
{
    public static final String SUNTIMES_WIDGET_UPDATE2 = "SUNTIMES_WIDGET_UPDATE2";
    public static final int UPDATEALARM_ID = 7;

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity2.class;
    }

    @Override
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget2.UPDATEALARM_ID;
    }

    @Override
    protected long getUpdateInterval()
    {
        return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    }

    @Override
    protected long getUpdateTimeMillis()
    {
        Calendar updateTime = Calendar.getInstance();
        updateTime.add(Calendar.SECOND, 1);
        return updateTime.getTimeInMillis();
    }


    @Override
    protected String getUpdateIntentFilter()
    {
        return SUNTIMES_WIDGET_UPDATE2;
    }

    @Override
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SunExtLayout layout = new SunExtLayout_1x1_0();
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SunExtLayout layout)
    {
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        SuntimesRiseSetDataset dataset = new SuntimesRiseSetDataset(context, appWidgetId);

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, SuntimesWidget2.class));
        layout.prepareForUpdate(dataset);
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, dataset);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}


