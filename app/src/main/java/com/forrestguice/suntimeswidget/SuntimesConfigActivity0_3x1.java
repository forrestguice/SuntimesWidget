/**
    Copyright (C) 2022-2023 Forrest Guice
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
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * Widget config activity (for resizable widget that falls back to 3x1 layout).
 */
public class SuntimesConfigActivity0_3x1 extends SuntimesConfigActivity0_2x1
{
    public SuntimesConfigActivity0_3x1() {
        super();
    }

    @Override
    protected Class getWidgetClass() {
        return SuntimesWidget0_3x1.class;
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);
    }

    @Override
    protected int[] minWidgetSize(Context context)
    {
        int[] minSize = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        return minSize;
    }

    protected void initWidgetModeLayout(Context context)
    {
        super.initWidgetModeLayout(context);
        showOption3x1LayoutMode(true);
    }

    @Override
    protected TextView getPrimaryWidgetModeLabel() {
        return label_3x1mode;
    }

    @Override
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_3x1mode, spinner_3x1mode };
    }

    @Override
    protected View[] getSecondaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode, label_2x1mode, spinner_2x1mode, label_3x2mode, spinner_3x2mode, label_3x3mode, spinner_3x3mode };
    }

    @Override
    public boolean getDefaultShowSolarNoon() {
        return true;
    }

}
