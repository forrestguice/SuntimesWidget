/**
    Copyright (C) 2022 Forrest Guice
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

import com.forrestguice.suntimeswidget.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_1X3_0;

public class SuntimesWidget2_1x3 extends SuntimesWidget2
{
    @Override
    protected Class getConfigClass() {
        return SuntimesConfigActivity2_1x3.class;
    }

    @Override
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SunPosLayout layout = new SunPosLayout_1X3_0();   // WidgetSettings.loadSunPos1x3ModePref_asLayout(context, appWidgetId);    // TODO
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget2_1x3.class, getMinSize(context), layout);
    }

}


