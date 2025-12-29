/**
    Copyright (C) 2019-2025 Forrest Guice
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
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;

/**
 * Clock widget config activity.
 */
@SuppressWarnings("Convert2Diamond")
public class ClockWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public ClockWidget0ConfigActivity()
    {
        super();
    }

    @Override
    protected Class getWidgetClass() {
        return ClockWidget0.class;
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_clockwidget0));

        showOptionShowDate(true);
        // TODO: date pattern config

        showOptionRiseSetOrder(false);
        hideOptionUseAltitude();
        hideOptionCompareAgainst();

        showOptionWeeks(false);
        showOptionHours(false);
        showOptionTimeDate(false);
        hideOptionShowSeconds();
        showOptionTrackingMode(false);
        showOptionTimeModeOverride(false);
        showDataSource(false);
        showTimeMode(false);
        showOptionShowNoon(false);

        //showTimeFormatMode(true);
        showOptionLabels(true);
        showOptionAllowResize(false);

        moveSectionToTop(R.id.appwidget_timezone_layout);
        moveSectionToTop(R.id.appwidget_general_layout);
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //ClockWidget0.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    protected int getAboutIconID()
    {
        return R.mipmap.ic_launcher_alarms;
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] {};

    public static final boolean DEF_SHOWTITLE = true;
    public static final String DEF_TITLETEXT = "%t";

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
    protected TimezoneMode getDefaultTimezoneMode()
    {
        return TimezoneMode.CURRENT_TIMEZONE;
    }

    @Override
    protected boolean getDefaultScaleText() {
        return true;
    }

    @Override
    protected Intent themeEditorIntent(Context context)
    {
        Intent intent = super.themeEditorIntent(context);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_CLOCK_1x1);
        return intent;
    }

    @Override
    protected TextView getPrimaryWidgetModeLabel() {
        return label_1x1mode;
    }

    @Override
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode };
    }

    @Override
    protected void initWidgetModeLayout(Context context)
    {
        super.initWidgetModeLayout(context);
        showOption2x2LayoutMode(false);
        showOption3x2LayoutMode(false);
        showOption2x1LayoutMode(false);
        showOption3x1LayoutMode(false);
    }

    /**
     * Mode 1x1
     */

    @Override
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null) {
            adapter_1x1mode = createAdapter_widgetMode1x1();
            spinner_1x1mode.setAdapter(adapter_1x1mode);
            addOnItemSelectedListener(spinner_1x1mode, null);
        }
    }
    private WidgetModeAdapter adapter_1x1mode;

    @Override
    protected void saveWidgetMode1x1(Context context, int appWidgetId)
    {
        if (spinner_1x1mode != null)
        {
            final ClockWidgetSettings.WidgetModeClock1x1[] modes = ClockWidgetSettings.WidgetModeClock1x1.values();
            ClockWidgetSettings.WidgetModeClock1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
            ClockWidgetSettings.saveClockModePref(context, appWidgetId, mode.name(), ClockWidgetSettings.MODE_1x1);
        }
    }
    @Override
    protected void loadWidgetMode1x1(Context context)
    {
        ClockWidgetSettings.WidgetModeClock1x1 mode1x1 = ClockWidgetSettings.loadClock1x1ModePref(context, appWidgetId);
        if (spinner_1x1mode != null)
        {
            int p = searchForIndex(spinner_1x1mode, mode1x1);
            if (p >= 0) {
                spinner_1x1mode.setSelection(mode1x1.ordinal());
            }
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode1x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, ClockWidgetSettings.WidgetModeClock1x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    protected static int searchForIndex(Spinner spinner, Object enumValue)
    {
        for (int i=0; i<spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).equals(enumValue)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected boolean supportsPreview() {
        return true;
    }

    @Override
    protected void updatePreview(final Context context)
    {
        super.updatePreview(context);
        if (spinner_1x1mode != null)
        {
            spinner_1x1mode.setOnItemSelectedListener(null);
            updateWidgetModeAdapter(spinner_1x1mode, null);
            addOnItemSelectedListener(spinner_1x1mode, null);
        }
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "ClockWidgetConfig: updatePreview");
        }
    }

    @Override
    protected View createPreview(Context context, int appWidgetId, SuntimesWidget0.AppWidgetManagerView appWidgetManager)
    {
        int[] defaultSizePx = getWidgetSizeConstraints(context, getPrimaryWidgetModeSize());
        ClockWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, defaultSizePx);
        return appWidgetManager.getView();
    }

    protected ClockLayout defaultClockLayout(Context context, int appWidgetId) {
        return ClockWidgetSettings.loadClock1x1ModePref_asLayout(context, appWidgetId);
    }

}
