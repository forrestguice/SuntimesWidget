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

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.layouts.MoonLayout;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_0;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class MoonWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public MoonWidget0ConfigActivity()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_moonwidget0));
        showTimeMode(false);
        showOptionShowNoon(false);
        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        showOptionTrackingMode(false);
        showOptionTimeModeOverride(false);
        //showDataSource(false);
    }

    @Override
    protected void updateWidget(Context context)
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);

        MoonLayout defLayout = new MoonLayout_1x1_0();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        MoonWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, minSize, defLayout);
    }

    @Override
    protected void initTimeMode( Context context )
    {
        // EMPTY
    }
    @Override
    protected void loadTimeMode(Context context)
    {
        // EMPTY
    }
    @Override
    protected void saveTimeMode(Context context)
    {
        // EMPTY
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_MOON };

    public static final boolean DEF_SHOWTITLE = false;
    public static final String DEF_TITLETEXT = "";

    @Override
    protected void loadTitleSettings(Context context)
    {
        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId, DEF_SHOWTITLE);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

        String titleText = WidgetSettings.loadTitleTextPref(context, appWidgetId, DEF_TITLETEXT);
        text_titleText.setText(titleText);
    }

}
