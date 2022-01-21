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

package com.forrestguice.suntimeswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_3X1_0;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_3X2_0;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_3X3_0;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class SuntimesWidget2 extends SuntimesWidget0
{
    public static final String SUNTIMES_WIDGET_UPDATE2 = "suntimes.SUNTIMES_WIDGET_UPDATE2";

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity2.class;
    }

    @Override
    protected long getUpdateInterval()
    {
        return 5 * 60 * 1000;  // 5 min
    }

    @Override
    protected long getUpdateTimeMillis(Context context, int appWidgetId)
    {
        Calendar updateTime = Calendar.getInstance();
        updateTime.add(Calendar.SECOND, (int)(getUpdateInterval() / 1000));
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
        SunPosLayout deflayout = WidgetSettings.loadSunPos1x1ModePref_asLayout(context, appWidgetId);
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget2.class, getMinSize(context), deflayout);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Class widgetClass, int[] defSize, SunPosLayout defLayout)
    {
        SunPosLayout layout = SuntimesWidget2.getWidgetLayout(context, appWidgetManager, appWidgetId, defSize, defLayout);
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId, layout, widgetClass);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SunPosLayout layout, Class widgetClass)
    {
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        SuntimesRiseSetDataset dataset = new SuntimesRiseSetDataset(context, appWidgetId);

        views.setOnClickPendingIntent(R.id.widgetframe_inner, SuntimesWidget0.clickActionIntent(context, appWidgetId, widgetClass));
        layout.prepareForUpdate(dataset, widgetMaxSizeDp(context, appWidgetManager, appWidgetId, new int[] {40, 40}));
        layout.themeViews(context, views, appWidgetId);
        layout.updateViews(context, appWidgetId, views, dataset);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    protected SuntimesData getData(Context context, int appWidgetId) {
        return new SuntimesRiseSetDataset(context, appWidgetId).dataActual;
    }

    protected static SunPosLayout getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize, SunPosLayout defLayout)
    {
        int[] mustFitWithinDp = widgetSizeDp(context, appWidgetManager, appWidgetId, defSize);
        SunPosLayout layout;
        if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
        {
            int minDimen_x3 = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
            int minDimen_x2 = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
            if (mustFitWithinDp[0] >= minDimen_x3)
            {
                if (mustFitWithinDp[1] >= minDimen_x3)
                    layout = ((mustFitWithinDp[0] / (float)mustFitWithinDp[1]) < 1.1) ? new SunPosLayout_3X3_0()
                                                                                      : new SunPosLayout_3X2_0();
                else if (mustFitWithinDp[1] >= minDimen_x2)
                    layout = new SunPosLayout_3X2_0();
                else layout = new SunPosLayout_3X1_0();

            } else {
                layout = WidgetSettings.loadSunPos1x1ModePref_asLayout(context, appWidgetId);
            }
        } else {
            layout = defLayout;
        }
        //Log.d("getWidgetLayout", "layout is: " + layout);
        return layout;
    }

}


