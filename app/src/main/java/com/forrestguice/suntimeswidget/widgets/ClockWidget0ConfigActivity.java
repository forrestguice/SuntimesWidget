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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooser;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooserView;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Clock widget config activity.
 */
public class ClockWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public ClockWidget0ConfigActivity()
    {
        super();
    }

    @Override
    protected Class<?> getWidgetClass() {
        return ClockWidget0.class;
    }

    protected ArrayAdapter<String> spin_typeface_adapter;
    protected Spinner spin_typeface;
    protected ColorChooser choose_textColor;
    protected CheckBox check_bold;
    protected CheckBox check_italic;
    protected CheckBox check_outline;
    protected CheckBox check_cutout;

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.widgetConfig_clockwidget0));

        showOptionTypeface(true);
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

        spin_typeface = (Spinner) findViewById(R.id.appwidget_appearance_typeface);
        if (spin_typeface != null)
        {
            spin_typeface_adapter = new ArrayAdapter<String>(this, R.layout.layout_listitem_oneline, ClockWidgetSettings.FONT_FAMILIES);
            spin_typeface_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_typeface.setAdapter(spin_typeface_adapter);
            spin_typeface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    updatePreview("onTypefaceChanged", ClockWidget0ConfigActivity.this);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        ColorChooserView textColorView = (ColorChooserView) findViewById(R.id.appwidget_appearance_typeface_color);
        if (textColorView != null)
        {
            choose_textColor = new ColorChooser(context, textColorView.getLabel(), textColorView.getEdit(), textColorView.getButton(), "textColor");
            choose_textColor.setFragmentManager(this);
            choose_textColor.setCollapsed(true);
            choose_textColor.setColorChangeListener(new ColorChangeListener()
            {
                @Override
                public void onColorChanged(int color)
                {
                    updatePreview("onColorChanged", ClockWidget0ConfigActivity.this);
                    choose_textColor.addRecentColor(color);
                }
            });
        }

        check_bold = (CheckBox) findViewById(R.id.appwidget_appearance_typeface_bold);
        if (check_bold != null) {
            addOnCheckedChangeListener(check_bold, null);
        }

        check_italic = (CheckBox) findViewById(R.id.appwidget_appearance_typeface_italic);
        if (check_italic != null) {
            addOnCheckedChangeListener(check_italic, null);
        }

        check_outline = (CheckBox) findViewById(R.id.appwidget_appearance_typeface_outline);
        if (check_outline != null) {
            addOnCheckedChangeListener(check_outline, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b && check_cutout != null) {
                        check_cutout.setChecked(false);
                    }
                }
            });
        }

        check_cutout = (CheckBox) findViewById(R.id.appwidget_appearance_typeface_cutout);
        if (check_cutout != null) {
            addOnCheckedChangeListener(check_cutout, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b && check_outline != null) {
                        check_outline.setChecked(false);
                    }
                }
            });
        }

        Button restoreDefaults = (Button) findViewById(R.id.appwidget_appearance_typeface_clear);
        if (restoreDefaults != null) {
            restoreDefaults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restoreTypefaceDefaults(view.getContext());
                }
            });
        }
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
    private static final int[] requiredFeatures = new int[] {};

    public static final boolean DEF_SHOWTITLE = false;
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
    protected void loadAppearanceSettings(Context context)
    {
        super.loadAppearanceSettings(context);
        loadTypefaceSettings(context);
    }

    @Override
    protected void saveAppearanceSettings(Context context, int appWidgetId)
    {
        super.saveAppearanceSettings(context, appWidgetId);
        saveTypefaceSettings(context, appWidgetId);
    }

    protected void loadTypefaceSettings(Context context)
    {
        SuntimesTheme.ThemeDescriptor themeDescriptor = (SuntimesTheme.ThemeDescriptor) spinner_theme.getSelectedItem();
        SuntimesTheme theme = WidgetThemes.loadTheme(context, themeDescriptor.name());

        int defaultColor = theme.getTimeColor();
        boolean defaultBold = theme.getTimeBold();
        boolean defaultItalic = ClockWidgetSettings.PREF_DEF_APPEARANCE_ITALIC;
        boolean defaultOutline = ClockWidgetSettings.PREF_DEF_APPEARANCE_OUTLINE;
        boolean defaultCutout = ClockWidgetSettings.PREF_DEF_APPEARANCE_CUTOUT;

        if (spin_typeface != null) {
            String fontfamily = ClockWidgetSettings.loadClockTypefacePref(context, appWidgetId, ClockWidgetSettings.PREF_DEF_APPEARANCE_TYPEFACE);
            spin_typeface.setSelection(spin_typeface_adapter.getPosition(fontfamily));
        }
        if (choose_textColor != null)
        {
            int color = ClockWidgetSettings.loadClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR, defaultColor);
            choose_textColor.setColor(color);

            ArrayList<Integer> recentColors = new ArrayList<>();
            recentColors.add(color);
            choose_textColor.setRecentColors(recentColors);
        }
        if (check_bold != null) {
            check_bold.setChecked(ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_BOLD, defaultBold));
        }
        if (check_italic != null) {
            check_italic.setChecked(ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_ITALIC, defaultItalic));
        }
        if (check_outline != null) {
            check_outline.setChecked(ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE, defaultOutline));
        }
        if (check_cutout != null) {
            check_cutout.setChecked(ClockWidgetSettings.loadClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT, defaultCutout));
        }
    }

    protected void saveTypefaceSettings(Context context, int appWidgetId)
    {
        SuntimesTheme.ThemeDescriptor themeDescriptor = (SuntimesTheme.ThemeDescriptor) spinner_theme.getSelectedItem();
        SuntimesTheme theme = WidgetThemes.loadTheme(context, themeDescriptor.name());

        if (spin_typeface != null) {
            ClockWidgetSettings.saveClockTypefacePref(context, appWidgetId, spin_typeface.getSelectedItem().toString());
        }
        if (choose_textColor != null)
        {
            int color0 = theme.getTimeColor();
            int color1 = choose_textColor.getColor();
            if (color1 != color0) {
                ClockWidgetSettings.saveClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR, color1);
            } else {
                ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR);
                Log.d("DEBUG", "clearing settings that match default");
            }
        }
        if (check_bold != null)
        {
            boolean bold0 = theme.getTimeBold();
            boolean bold1 = check_bold.isChecked();
            if (bold1 != bold0) {
                ClockWidgetSettings.saveClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_BOLD, bold1);
            } else {
                ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_BOLD);
                Log.d("DEBUG", "clearing settings that match default1");
            }
        }
        if (check_italic != null) {
            ClockWidgetSettings.saveClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_ITALIC, check_italic.isChecked());
        }
        if (check_outline != null) {
            ClockWidgetSettings.saveClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE, check_outline.isChecked());
        }
        if (check_cutout != null) {
            ClockWidgetSettings.saveClockTypefaceFlag(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT, check_cutout.isChecked());
        }
    }

    protected void restoreTypefaceDefaults(Context context)
    {
        ClockWidgetSettings.deleteClockTypefacePref(context, appWidgetId);
        ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_COLOR);
        ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_BOLD);
        ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_ITALIC);
        ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE);
        ClockWidgetSettings.deleteClockTypefaceValue(context, appWidgetId, ClockWidgetSettings.PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT);
        loadTypefaceSettings(context);
        updatePreview("onRestoreDefaults", context);
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
            addOnItemSelectedListener("WidgetMode1x1", spinner_1x1mode, null);
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
    protected void updatePreview(final String tag, final Context context)
    {
        super.updatePreview(tag, context);
        if (supportsPreview())
        {
            if (spinner_1x1mode != null) {
                spinner_1x1mode.setOnItemSelectedListener(null);
                updateWidgetModeAdapter(spinner_1x1mode, null);
                addOnItemSelectedListener("WidgetMode1x1", spinner_1x1mode, null);
            }
            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "ClockWidgetConfig: updatePreview");
            }
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
