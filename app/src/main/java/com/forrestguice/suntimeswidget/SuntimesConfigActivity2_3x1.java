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

import com.forrestguice.suntimeswidget.layouts.SunPosLayout_3X1_0;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity.PICK_THEME_REQUEST;

public class SuntimesConfigActivity2_3x1 extends SuntimesConfigActivity2
{
    public SuntimesConfigActivity2_3x1()
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
        SuntimesWidget2_3x1.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, SuntimesWidget2_3x1.class, minWidgetSize(context), new SunPosLayout_3X1_0());
    }

    @Override
    protected int[] minWidgetSize( Context context )
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp3x1);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        return minSize;
    }

    @Override
    protected void launchThemeEditor(Context context)
    {
        Intent configThemesIntent = themeEditorIntent(context);
        configThemesIntent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_SUNPOS_3x1);
        startActivityForResult(configThemesIntent, PICK_THEME_REQUEST);
    }

}
