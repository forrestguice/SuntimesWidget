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
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

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
        showOptionRiseSetOrder(false);
        hideOptionCompareAgainst();
        showTimeMode(false);
        showOptionShowNoon(false);
        showOptionLabels(true);
        showOption2x1LayoutMode(false);
        showOption3x1LayoutMode(true);
        showOption3x2LayoutMode(true);
        showOption3x3LayoutMode(true);
        showTimeFormatMode(false);
    }

    @Override
    protected void initLocale(Context context)
    {
        super.initLocale(context);
        WorldMapWidgetSettings.initDisplayStrings(context);
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
        return SuntimesCalculatorDescriptor.values(this, requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_POSITION };

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, SuntimesWidget2.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //SunPosLayout defLayout = new SunPosLayout_1X1_0();
        //SuntimesWidget2.updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, SuntimesWidget2.class, minWidgetSize(context), defLayout);
    }

    @Override
    protected void loadShowLabels(Context context)
    {
        checkbox_showLabels.setChecked(WidgetSettings.loadShowLabelsPref(context, appWidgetId, true));
    }

    @Override
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null) {
            spinner_1x1mode.setAdapter(createAdapter_widgetMode1x1());
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode1x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSunPos1x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
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
        WidgetSettings.WidgetModeSunPos1x1 mode = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        int pos = searchForIndex(spinner_1x1mode, mode);
        if (pos >= 0) {
            spinner_1x1mode.setSelection(pos);
        }
    }

    @Override
    protected void initWidgetMode3x1(Context context)
    {
        if (spinner_3x1mode != null) {
            spinner_3x1mode.setAdapter(createAdapter_widgetMode3x1());
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode3x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSunPos3x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    @Override
    protected void saveWidgetMode3x1(Context context)
    {
        if (spinner_3x1mode != null) {
            WidgetSettings.WidgetModeSunPos3x1 mode = (WidgetSettings.WidgetModeSunPos3x1) spinner_3x1mode.getSelectedItem();
            WidgetSettings.saveSunPos3x1ModePref(context, appWidgetId, mode);
        }
    }

    @Override
    protected void loadWidgetMode3x1(Context context)
    {
        if (spinner_3x1mode != null)
        {
            WidgetSettings.WidgetModeSunPos3x1 mode = WidgetSettings.loadSunPos3x1ModePref(context, appWidgetId);
            int pos = searchForIndex(spinner_3x1mode, mode);
            if (pos >= 0) {
                spinner_3x1mode.setSelection(pos);
            }
        }
    }

    @Override
    protected void initWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null) {
            spinner_3x2mode.setAdapter(createAdapter_widgetMode3x2());
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode3x2()
    {
        ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>();
        modes.add(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE);
        modes.add(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_BLUEMARBLE);
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, modes.toArray(new WidgetSettings.WidgetModeDisplay[0]));
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    @Override
    protected void saveWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = (WorldMapWidgetSettings.WorldMapWidgetMode) spinner_3x2mode.getSelectedItem();
            WorldMapWidgetSettings.saveSunPosMapModePref(context, appWidgetId, mode, WorldMapWidgetSettings.MAPTAG_3x2);
        }
    }

    @Override
    protected void loadWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x2);
            int pos = searchForIndex(spinner_3x2mode, mode);
            if (pos >= 0) {
                spinner_3x2mode.setSelection(pos);
            }
        }
    }

    private static int searchForIndex(Spinner spinner, Object enumValue)
    {
        for (int i=0; i<spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i) == enumValue) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void initWidgetMode3x3(Context context)
    {
        if (spinner_3x3mode != null) {
            spinner_3x3mode.setAdapter(createAdapter_widgetMode3x3());
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode3x3()
    {
        ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>();
        modes.add(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE);
        modes.add(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE1);
        modes.add(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE2);
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, modes.toArray(new WidgetSettings.WidgetModeDisplay[0]));
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    @Override
    protected void saveWidgetMode3x3(Context context)
    {
        if (spinner_3x3mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = (WorldMapWidgetSettings.WorldMapWidgetMode) spinner_3x3mode.getSelectedItem();
            WorldMapWidgetSettings.saveSunPosMapModePref(context, appWidgetId, mode, WorldMapWidgetSettings.MAPTAG_3x3);
        }
    }

    @Override
    protected void loadWidgetMode3x3(Context context)
    {
        if (spinner_3x3mode != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x3);
            int pos = searchForIndex(spinner_3x3mode, mode);
            if (pos >= 0) {
                spinner_3x3mode.setSelection(pos);
            }
        }
    }
}
