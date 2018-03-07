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

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.layouts.MoonLayout;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity.PICK_THEME_REQUEST;

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
        showOptionLabels(true);
        hideOptionCompareAgainst();
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

        MoonLayout defLayout = WidgetSettings.loadMoon1x1ModePref_asLayout(context, appWidgetId);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        MoonWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, MoonWidget0.class, minSize, defLayout);
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
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null)
        {
            spinner_1x1mode.setAdapter(createAdapter_widgetModeMoon1x1());
        }
    }

    protected ArrayAdapter<WidgetSettings.WidgetModeMoon1x1> createAdapter_widgetModeMoon1x1()
    {
        ArrayAdapter<WidgetSettings.WidgetModeMoon1x1> adapter = new ArrayAdapter<WidgetSettings.WidgetModeMoon1x1>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeMoon1x1.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    @Override
    protected void saveWidgetMode1x1(Context context)
    {
        final WidgetSettings.WidgetModeMoon1x1[] modes = WidgetSettings.WidgetModeMoon1x1.values();
        WidgetSettings.WidgetModeMoon1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
        WidgetSettings.saveMoon1x1ModePref(context, appWidgetId, mode);
        //Log.d("DEBUG", "Saved mode: " + mode.name());
    }

    @Override
    protected void loadWidgetMode1x1(Context context)
    {
        WidgetSettings.WidgetModeMoon1x1 mode1x1 = WidgetSettings.loadMoon1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());
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

    @Override
    protected void launchThemeEditor(Context context)
    {
        Intent configThemesIntent = themeEditorIntent(context);
        configThemesIntent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_MOON_2x1);
        startActivityForResult(configThemesIntent, PICK_THEME_REQUEST);
    }

}
