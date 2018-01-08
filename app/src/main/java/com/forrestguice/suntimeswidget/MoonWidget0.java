package com.forrestguice.suntimeswidget;

/**
    Copyright (C) 2018 Forrest Guice
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

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.layouts.MoonLayout;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_0;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 *  Flippable widget
 */
public class MoonWidget0 extends SuntimesWidget0
{
    public static final String WIDGET_UPDATE = "MOON_WIDGET_UPDATE";
    private static final int UPDATEALARM_ID = 2;

    @Override
    protected Class getConfigClass()
    {
        return MoonWidget0ConfigActivity.class;
    }

    @Override
    protected String getUpdateIntentFilter()
    {
        return MoonWidget0.WIDGET_UPDATE;
    }

    @Override
    protected int getUpdateAlarmId()
    {
        return MoonWidget0.UPDATEALARM_ID;
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {

        MoonWidget0.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        MoonLayout layout = MoonWidget0.getWidgetLayout(context, appWidgetManager, appWidgetId);
        MoonWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, MoonLayout layout)
    {
        RemoteViews views = layout.getViews(context);
        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, MoonWidget0.class));

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        SuntimesMoonData data = new SuntimesMoonData(context, appWidgetId);
        data.calculate();
        layout.prepareForUpdate(data);
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, data);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static MoonLayout getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        return new MoonLayout_1x1_0();
    }

    @Override
    public void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.SolsticeEquinoxMode.initDisplayStrings(context);
    }

}
