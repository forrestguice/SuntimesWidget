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

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.forrestguice.suntimeswidget.layouts.SunLayout_2x1_0;

/**
 * Widget receiver for resizable widget (that falls back to 2x1 layout).
 */
public class SuntimesWidget0_2x1 extends SuntimesWidget0
{
    public static final int UPDATEALARM_ID = 2;

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity0_2x1.class;
    }

    @Override
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget0_2x1.UPDATEALARM_ID;
    }

    @Override
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget0_2x1.class, getMinSize(context), new SunLayout_2x1_0());
    }
}


