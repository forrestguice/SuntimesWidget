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
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity.PICK_THEME_REQUEST;

public class SuntimesConfigActivity2_3x2 extends SuntimesConfigActivity2
{
    public SuntimesConfigActivity2_3x2()
    {
        super();
    }

    @Override
    protected Class getWidgetClass() {
        return SuntimesWidget2_3x2.class;
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
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //SuntimesWidget2_3x2.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, SuntimesWidget2_3x2.class, minWidgetSize, new SunPosLayout_3X2_0());
    }

    @Override
    protected int[] minWidgetSize( Context context )
    {
        int[] minSize = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minWidthDp2x1);
        return minSize;
    }

    @Override
    protected Intent themeEditorIntent(Context context)
    {
        Intent intent = super.themeEditorIntent(context);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_SUNPOS_3x2);
        return intent;
    }

    @Override
    protected SunPosLayout defaultSunPosLayout(Context context, int appWidgetId) {
        return WidgetSettings.loadSunPos3x2ModePref_asLayout(context, appWidgetId);
    }

    @Override
    protected String getPrimaryWidgetModeSize() {
        return SIZE_3x2;
    }

    @Override
    protected TextView getPrimaryWidgetModeLabel() {
        return label_3x2mode;
    }

    @Override
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_3x2mode, spinner_3x2mode };
    }

    @Override
    protected View[] getSecondaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode, label_2x1mode, spinner_2x1mode, label_3x1mode, spinner_3x1mode, label_3x3mode, spinner_3x3mode };
    }

}
