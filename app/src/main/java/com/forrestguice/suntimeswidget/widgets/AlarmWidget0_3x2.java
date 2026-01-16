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

package com.forrestguice.suntimeswidget.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;

/**
 *  Alarm widget 3x2
 */
public class AlarmWidget0_3x2 extends AlarmWidget0
{
    @Override
    protected Class<?> getConfigClass() {
        return AlarmWidget0ConfigActivity_3x2.class;
    }

    @Override
    protected void initMinSize(Context context)
    {
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        AlarmLayout defLayout = AlarmWidgetSettings.loadAlarm3x2ModePref_asLayout(context, appWidgetId);
        AlarmWidget0.updateAppWidget(context, new AppWidgetManagerWrapper(appWidgetManager), appWidgetId, AlarmWidget0_3x2.class, getMinSize(context), defLayout);
    }

}
