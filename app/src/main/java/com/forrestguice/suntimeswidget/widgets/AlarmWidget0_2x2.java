package com.forrestguice.suntimeswidget.widgets;

/**
    Copyright (C) 2024 Forrest Guice
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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_2x2_0;

/**
 *  Alarm widget 2x2
 */
public class AlarmWidget0_2x2 extends AlarmWidget0
{
    @Override
    protected Class getConfigClass() {
        return AlarmWidget0ConfigActivity_2x2.class;
    }

    @Override
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        AlarmWidget0_2x2.updateAppWidget(context, appWidgetManager, appWidgetId, getMinSize(context));
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize)
    {
        AlarmLayout layout = AlarmWidget0_2x2.getWidgetLayout(context, appWidgetManager, appWidgetId, defSize);
        AlarmWidget0_2x2.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
    }

    protected static AlarmLayout getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] defSize)
    {
        AlarmLayout layout = new AlarmLayout_2x2_0();
        layout.setMaxDimensionsDp(widgetSizeDp(context, appWidgetManager, appWidgetId, defSize));
        layout.setCategory(widgetCategory(appWidgetManager, appWidgetId));
        return layout;
    }


}
