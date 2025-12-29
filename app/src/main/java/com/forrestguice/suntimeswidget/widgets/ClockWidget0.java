package com.forrestguice.suntimeswidget.widgets;

/**
    Copyright (C) 2019 Forrest Guice
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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

/**
 *  Clock widget
 */
public class ClockWidget0 extends SuntimesWidget0
{
    public static final String CLOCK_WIDGET_UPDATE = "suntimes.CLOCK_WIDGET_UPDATE";

    @Override
    protected Class getConfigClass()
    {
        return ClockWidget0ConfigActivity.class;
    }

    @Override
    protected String getUpdateIntentFilter()
    {
        return ClockWidget0.CLOCK_WIDGET_UPDATE;
    }

    @Override
    protected long getUpdateInterval()
    {
        return 1000 * 60;  // every minute
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        ClockWidget0.updateAppWidget(context, new AppWidgetManagerWrapper(appWidgetManager), appWidgetId, getMinSize(context));
    }

    protected static void updateAppWidget(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, int[] defSize)
    {
        ClockLayout layout = ClockWidget0.getWidgetLayout(context, appWidgetManager, appWidgetId, defSize);
        ClockWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static void updateAppWidget(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, ClockLayout layout)
    {
        SuntimesClockData data = new SuntimesClockData(context, appWidgetId);
        data.calculate(context);
        layout.prepareForUpdate(context, appWidgetId, data, widgetMaxSizeDp(context, appWidgetManager, appWidgetId, new int[] {40, 40}));
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);
        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, ClockWidget0.class));
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(context, appWidgetId, views);

        Calendar nextUpdate = Calendar.getInstance();
        nextUpdate.setTimeInMillis(data.calendar().getTimeInMillis());
        nextUpdate.add(Calendar.MINUTE, 1);   // up to a minute from now
        nextUpdate.set(Calendar.SECOND, 1);
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate.getTimeInMillis());
    }

    @Override
    protected SuntimesData getData(Context context, int appWidgetId) {
        return new SuntimesClockData(context, appWidgetId);
    }

    protected static ClockLayout getWidgetLayout(Context context, WidgetManagerInterface appWidgetManager, int appWidgetId, int[] defSize)
    {
        ClockLayout layout = ClockWidgetSettings.loadClock1x1ModePref_asLayout(context, appWidgetId);
        layout.setMaxDimensionsDp(widgetSizeDp(context, appWidgetManager, appWidgetId, defSize));
        layout.setCategory(widgetCategory(appWidgetManager, appWidgetId));
        return layout;
    }

    @Override
    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
    }

}
