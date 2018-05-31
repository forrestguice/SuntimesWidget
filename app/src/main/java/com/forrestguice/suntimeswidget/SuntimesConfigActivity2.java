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
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_1X1_0;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * ConfigActivity for SunPosition widgets (SuntimesWidget2)
 */
public class SuntimesConfigActivity2 extends SuntimesConfigActivity0
{
    public SuntimesConfigActivity2()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_title2));
        hideOptionShowSeconds();
        hideOptionCompareAgainst();
        showTimeMode(false);
        showOptionShowNoon(false);
        showOptionLabels(true);
    }

    /**@Override
    protected void loadAppearanceSettings(Context context)
    {
        super.loadAppearanceSettings(context);
    }*/

    @Override
    protected WidgetSettings.ActionMode defaultActionMode()
    {
        return WidgetSettings.ActionMode.ONTAP_UPDATE;
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_POSITION };

    @Override
    protected void updateWidget(Context context)
    {
        SunPosLayout defLayout = new SunPosLayout_1X1_0();
        SuntimesWidget2.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, SuntimesWidget2.class, minWidgetSize(context), defLayout);
    }

    @Override
    protected void loadShowLabels(Context context)
    {
        checkbox_showLabels.setChecked(WidgetSettings.loadShowLabelsPref(context, appWidgetId, true));
    }

    @Override
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null)
        {
            spinner_1x1mode.setAdapter(createAdapter_widgetModeSunPos1x1());
        }
    }

    protected ArrayAdapter<WidgetSettings.WidgetModeSunPos1x1> createAdapter_widgetModeSunPos1x1()
    {
        ArrayAdapter<WidgetSettings.WidgetModeSunPos1x1> adapter = new ArrayAdapter<WidgetSettings.WidgetModeSunPos1x1>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSunPos1x1.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    @Override
    protected void saveWidgetMode1x1(Context context)
    {
        final WidgetSettings.WidgetModeSunPos1x1[] modes = WidgetSettings.WidgetModeSunPos1x1.values();
        WidgetSettings.WidgetModeSunPos1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
        WidgetSettings.saveSunPos1x1ModePref(context, appWidgetId, mode);
        //Log.d("DEBUG", "Saved mode: " + mode.name());
    }

    @Override
    protected void loadWidgetMode1x1(Context context)
    {
        WidgetSettings.WidgetModeSunPos1x1 mode1x1 = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());
    }

}
