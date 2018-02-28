/**
    Copyright (C) 2014 Forrest Guice
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
import android.os.Build;

import com.forrestguice.suntimeswidget.layouts.SunLayout_2x1_0;

/**
 * Widget config activity (for resizable widget that falls back to 2x1 layout).
 */
public class SuntimesConfigActivity0_2x1 extends SuntimesConfigActivity0
{
    public SuntimesConfigActivity0_2x1()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            hideOption1x1LayoutMode();
        }
    }

    @Override
    protected void updateWidget(Context context)
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget0_2x1.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget0_2x1.class, minSize, new SunLayout_2x1_0());
    }

}
