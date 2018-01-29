/**
    Copyright (C) 2014-2018 Forrest Guice
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
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;

import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;

import java.security.InvalidParameterException;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity.PICK_THEME_REQUEST;

/**
 * Widget config activity for resizable widget.
 */
@SuppressWarnings("Convert2Diamond")
public class SuntimesConfigActivity0 extends AppCompatActivity
{
    protected static final String DIALOGTAG_ABOUT = "about";
    protected static final String DIALOGTAG_HELP = "help";

    protected int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    protected boolean reconfigure = false;

    protected Spinner spinner_calculatorMode;
    protected Spinner spinner_timeMode;
    protected CheckBox checkbox_timeModeOverride;
    protected ImageButton button_timeModeHelp;
    protected Spinner spinner_trackingMode;
    protected Spinner spinner_compareMode;
    protected CheckBox checkbox_showNoon;
    protected CheckBox checkbox_showCompare;
    protected CheckBox checkbox_showSeconds;

    protected Spinner spinner_onTap;
    protected EditText text_launchActivity;

    protected TextView button_themeConfig;
    private WidgetThemes.ThemeListAdapter spinner_themeAdapter;
    protected Spinner spinner_theme;

    protected Spinner spinner_1x1mode;
    protected CheckBox checkbox_allowResize;
    protected CheckBox checkbox_showTitle;
    protected TextView label_titleText;
    protected EditText text_titleText;

    protected LocationConfigView locationConfig;

    protected Spinner spinner_timezoneMode;

    protected LinearLayout layout_timezone;
    protected TextView label_timezone;
    protected Spinner spinner_timezone;

    protected LinearLayout layout_solartime;
    protected TextView label_solartime;
    protected Spinner spinner_solartime;

    protected String customTimezoneID;
    protected ActionMode.Callback spinner_timezone_actionMode;
    protected WidgetTimezones.TimeZoneItemAdapter spinner_timezone_adapter;

    protected ActionMode actionMode = null;

    public SuntimesConfigActivity0()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        GetFixUI.themeIcons(this);

        super.onCreate(icicle);
        initLocale();
        setResult(RESULT_CANCELED);  // causes widget host to cancel if user presses back
        setContentView(R.layout.layout_settings);

        Context context = SuntimesConfigActivity0.this;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            reconfigure = extras.getBoolean(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), false);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            Log.w("CONFIG", "Invalid widget ID! returning early.");
            finish();
            return;
        }

        WidgetThemes.initThemes(context);

        initViews(context);
        loadSettings(context);
    }

    private void initLocale()
    {
        AppSettings.initLocale(this);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
        WidgetTimezones.TimeZoneSort.initDisplayStrings(this);
    }

    @Override
    public void onDestroy()
    {
        locationConfig.cancelGetFix();
        super.onDestroy();
    }

    /**
     * Save settings (as represented by the state of the config UI).
     *
     * @param context the android application context
     */
    protected void saveSettings(Context context)
    {
        saveGeneralSettings(context);
        locationConfig.saveSettings(context);
        saveTimezoneSettings(context);
        saveAppearanceSettings(context);
        saveActionSettings(context);
    }

    /**
     * Load settings (update the state of the config UI).
     *
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        loadGeneralSettings(context);
        loadAppearanceSettings(context);
        locationConfig.loadSettings(context);
        loadTimezoneSettings(context);
        loadActionSettings(context);
    }

    protected ArrayAdapter<SuntimesCalculatorDescriptor> createAdapter_calculators()
    {
        SuntimesCalculatorDescriptor[] calculators = supportingCalculators();
        return new SuntimesCalculatorDescriptor.SuntimesCalculatorDescriptorListAdapter(this, R.layout.layout_listitem_oneline, R.layout.layout_listitem_twoline, calculators);
    }

    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values();
    }

    protected ArrayAdapter<WidgetSettings.TimezoneMode> createAdapter_timezoneMode()
    {
        WidgetSettings.TimezoneMode[] modes = WidgetSettings.TimezoneMode.values();
        ArrayAdapter<WidgetSettings.TimezoneMode> adapter = new ArrayAdapter<WidgetSettings.TimezoneMode>(this, R.layout.layout_listitem_oneline, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.SolarTimeMode> createAdapter_solarTimeMode()
    {
        WidgetSettings.SolarTimeMode[] modes = WidgetSettings.SolarTimeMode.values();
        ArrayAdapter<WidgetSettings.SolarTimeMode> adapter = new ArrayAdapter<WidgetSettings.SolarTimeMode>(this, R.layout.layout_listitem_oneline, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.TrackingMode> createAdapter_trackingMode()
    {
        ArrayAdapter<WidgetSettings.TrackingMode> adapter = new ArrayAdapter<WidgetSettings.TrackingMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.TrackingMode.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.CompareMode> createAdapter_compareMode()
    {
        ArrayAdapter<WidgetSettings.CompareMode> adapter = new ArrayAdapter<WidgetSettings.CompareMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.CompareMode.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.WidgetMode1x1> createAdapter_widgetMode1x1()
    {
        ArrayAdapter<WidgetSettings.WidgetMode1x1> adapter = new ArrayAdapter<WidgetSettings.WidgetMode1x1>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetMode1x1.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.ActionMode> createAdapter_actionMode()
    {
        ArrayAdapter<WidgetSettings.ActionMode> adapter = new ArrayAdapter<WidgetSettings.ActionMode>(this, R.layout.layout_listitem_oneline, supportedActionModes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected WidgetSettings.ActionMode defaultActionMode()
    {
        return WidgetSettings.PREF_DEF_ACTION_MODE;
    }

    protected WidgetSettings.ActionMode[] supportedActionModes()
    {
        WidgetSettings.ActionMode[] allModes = WidgetSettings.ActionMode.values();
        WidgetSettings.ActionMode[] supportedModes = new WidgetSettings.ActionMode[allModes.length - 1];
        System.arraycopy(allModes, 0, supportedModes, 0, supportedModes.length);
        return supportedModes;
    }

    protected void initViews(final Context context)
    {
        //
        // widget: add button
        //
        button_addWidget = (Button) findViewById(R.id.add_button);
        if (button_addWidget != null)
        {
            button_addWidget.setEnabled(false);   // enabled later after timezones fully loaded
            button_addWidget.setOnClickListener(onAddButtonClickListener);
        }

        if (reconfigure)
        {
            setActionButtonText(getString(R.string.configAction_reconfigWidget_short));
            setConfigActivityTitle(getString(R.string.configAction_reconfigWidget));
        }

        //
        // widget: onTap
        //
        spinner_onTap = (Spinner) findViewById(R.id.appwidget_action_onTap);
        if (spinner_onTap != null)
        {
            spinner_onTap.setAdapter(createAdapter_actionMode());
            spinner_onTap.setOnItemSelectedListener(onActionModeListener);
        }

        //
        // widget: onTap launchActivity
        //
        text_launchActivity = (EditText) findViewById(R.id.appwidget_action_launch);

        ImageButton button_launchAppHelp = (ImageButton) findViewById(R.id.appwidget_action_launch_helpButton);
        if (button_launchAppHelp != null)
        {
            button_launchAppHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_action_launch));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        //
        // widget: theme
        //
        spinner_theme = (Spinner) findViewById(R.id.appwidget_appearance_theme);
        if (spinner_theme != null)
        {
            initThemeAdapter(context);
        }

        button_themeConfig = (TextView) findViewById(R.id.appwidget_appearance_theme_label);
        if (button_themeConfig != null)
        {
            button_themeConfig.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent configThemesIntent = new Intent(context, WidgetThemeListActivity.class);
                    startActivityForResult(configThemesIntent, PICK_THEME_REQUEST);
                }
            });
        }

        //
        // widget: source
        //
        spinner_calculatorMode = (Spinner) findViewById(R.id.appwidget_general_calculator);
        if (spinner_calculatorMode != null)
        {
            spinner_calculatorMode.setAdapter(createAdapter_calculators());
        }

        //
        // widget: time mode
        //
        spinner_timeMode = (Spinner) findViewById(R.id.appwidget_general_timeMode);
        checkbox_timeModeOverride = (CheckBox) findViewById(R.id.appwidget_general_timeMode_override);
        button_timeModeHelp = (ImageButton) findViewById(R.id.appwidget_general_timeMode_helpButton);
        initTimeMode(context);

        //
        // widget: timezone mode
        //
        spinner_timezoneMode = (Spinner) findViewById(R.id.appwidget_timezone_mode);
        if (spinner_timezoneMode != null)
        {
            spinner_timezoneMode.setAdapter(createAdapter_timezoneMode());
            spinner_timezoneMode.setOnItemSelectedListener(onTimezoneModeListener);
        }

        //
        // widget: timezone / solartime
        //
        layout_timezone = (LinearLayout) findViewById(R.id.appwidget_timezone_custom_layout);
        label_timezone = (TextView) findViewById(R.id.appwidget_timezone_custom_label);
        spinner_timezone = (Spinner) findViewById(R.id.appwidget_timezone_custom);

        if (label_timezone != null)
        {
            label_timezone.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    triggerTimeZoneActionMode(view);
                }
            });
            label_timezone.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    return triggerTimeZoneActionMode(view);
                }
            });
        }

        if (spinner_timezone != null)
        {
            View spinner_timezone_empty = findViewById(R.id.appwidget_timezone_custom_empty);
            spinner_timezone.setEmptyView(spinner_timezone_empty);

            WidgetTimezones.TimeZoneSort sortZonesBy = AppSettings.loadTimeZoneSortPref(context);
            WidgetTimezones.TimeZonesLoadTask loadTask = new WidgetTimezones.TimeZonesLoadTask(context);
            loadTask.setListener(new WidgetTimezones.TimeZonesLoadTaskListener()
            {
                @Override
                public void onStart()
                {
                    super.onStart();
                    spinner_timezone.setAdapter(new WidgetTimezones.TimeZoneItemAdapter(SuntimesConfigActivity0.this, R.layout.layout_listitem_timezone));
                    button_addWidget.setEnabled(false);
                }

                @Override
                public void onFinished(WidgetTimezones.TimeZoneItemAdapter result)
                {
                    super.onFinished(result);
                    spinner_timezone_adapter = result;
                    spinner_timezone.setAdapter(spinner_timezone_adapter);
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                    button_addWidget.setEnabled(true);
                }
            });
            loadTask.execute(sortZonesBy);
        }

        layout_solartime = (LinearLayout) findViewById(R.id.appwidget_solartime_layout);
        label_solartime = (TextView) findViewById(R.id.appwidget_solartime_label);
        spinner_solartime = (Spinner) findViewById(R.id.appwidget_solartime);
        if (spinner_solartime != null)
        {
            spinner_solartime.setAdapter(createAdapter_solarTimeMode());
        }

        spinner_timezone_actionMode = new WidgetTimezones.TimeZoneSpinnerSortActionCompat(context, spinner_timezone)
        {
            @Override
            public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
            {
                super.onSortTimeZones(result, sortMode);
                spinner_timezone_adapter = result;
                WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
            }

            @Override
            public void onSaveSortMode(WidgetTimezones.TimeZoneSort sortMode)
            {
                super.onSaveSortMode(sortMode);
                AppSettings.setTimeZoneSortPref(SuntimesConfigActivity0.this, sortMode);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                super.onDestroyActionMode(mode);
                actionMode = null;
            }
        };

        //
        // widget: location
        //
        locationConfig = (LocationConfigView) findViewById(R.id.appwidget_location_config);
        if (locationConfig != null)
        {
            locationConfig.setAutoAllowed(false);
            locationConfig.init(this, false, this.appWidgetId);
        }

        //
        // widget: 1x1 widget mode
        //
        spinner_1x1mode = (Spinner) findViewById(R.id.appwidget_appearance_1x1mode);
        if (spinner_1x1mode != null)
        {
            spinner_1x1mode.setAdapter(createAdapter_widgetMode1x1());
        }

        //
        // widget: title text
        //
        label_titleText = (TextView) findViewById(R.id.appwidget_appearance_titleText_label);
        text_titleText = (EditText) findViewById(R.id.appwidget_appearance_titleText);

        ImageButton button_titleText = (ImageButton) findViewById(R.id.appwidget_appearance_titleText_helpButton);
        if (button_titleText != null)
        {
            button_titleText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_appearance_title));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        //
        // widget: show title
        //
        checkbox_showTitle = (CheckBox) findViewById(R.id.appwidget_appearance_showTitle);
        if (checkbox_showTitle != null)
        {
            checkbox_showTitle.setOnCheckedChangeListener(onShowTitleListener);
        }

        //
        // widget: allow resize
        //
        checkbox_allowResize = (CheckBox) findViewById(R.id.appwidget_appearance_allowResize);
        if (checkbox_allowResize != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            disableOptionAllowResize();  // resizable widgets require api14+
        }

        //
        // widget: tracking mode
        //
        spinner_trackingMode = (Spinner) findViewById(R.id.appwidget_general_trackingMode);
        if (spinner_trackingMode != null)
        {
            spinner_trackingMode.setAdapter(createAdapter_trackingMode());
        }

        //
        // widget: compare mode
        //
        spinner_compareMode = (Spinner) findViewById(R.id.appwidget_general_compareMode);
        if (spinner_compareMode != null)
        {
            spinner_compareMode.setAdapter(createAdapter_compareMode());
        }

        //
        // widget: showNoon
        //
        checkbox_showNoon = (CheckBox) findViewById(R.id.appwidget_general_showNoon);

        //
        // widget: showCompare
        //
        checkbox_showCompare = (CheckBox) findViewById(R.id.appwidget_general_showCompare);
        checkbox_showCompare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                showCompareUI(isChecked);
            }
        });

        //
        // widget: showSeconds
        //
        checkbox_showSeconds = (CheckBox)findViewById(R.id.appwidget_general_showSeconds);

        //
        // widget: about button
        //
        Button button_aboutWidget = (Button) findViewById(R.id.about_button);
        if (button_aboutWidget != null)
        {
            button_aboutWidget.setOnClickListener(onAboutButtonClickListener);
        }
    }

    /**
     * @param context a context used to access resources
     */
    protected void initThemeAdapter(final Context context)
    {
        spinner_themeAdapter = new WidgetThemes.ThemeListAdapter(this, R.layout.layout_listitem_oneline, android.R.layout.simple_spinner_dropdown_item, WidgetThemes.values());
        spinner_theme.setAdapter(spinner_themeAdapter);
    }

    /**
     * @param context a context used to access resources
     */
    protected void initTimeMode(Context context)
    {
        if (spinner_timeMode != null)
        {
            final ArrayAdapter<WidgetSettings.TimeMode> spinner_timeModeAdapter;
            spinner_timeModeAdapter = new ArrayAdapter<WidgetSettings.TimeMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.TimeMode.values());
            spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_timeMode.setAdapter(spinner_timeModeAdapter);

            spinner_timeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    showOptionShowNoon(spinner_timeModeAdapter.getItem(i) != WidgetSettings.TimeMode.NOON);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView)
                {
                }
            });
        }

        if (button_timeModeHelp != null)
        {
            button_timeModeHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_general_timeMode));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        showOptionTimeModeOverride(false);
        showOptionTrackingMode(false);
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadTimeMode(Context context)
    {
        WidgetSettings.TimeMode timeMode = WidgetSettings.loadTimeModePref(context, appWidgetId);
        spinner_timeMode.setSelection(timeMode.ordinal());
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveTimeMode(Context context)
    {
        final WidgetSettings.TimeMode[] timeModes = WidgetSettings.TimeMode.values();
        WidgetSettings.TimeMode timeMode = timeModes[spinner_timeMode.getSelectedItemPosition()];
        WidgetSettings.saveTimeModePref(context, appWidgetId, timeMode);
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadTimeModeOverride(Context context)
    {
        boolean value = WidgetSettings.loadTimeMode2OverridePref(context, appWidgetId);
        checkbox_timeModeOverride.setChecked(value);
    }

    /**
     * @param context a context used to access shared prefs
     */
    public void saveTimeModeOverride(Context context)
    {
        WidgetSettings.saveTimeMode2OverridePref(context, appWidgetId, checkbox_timeModeOverride.isChecked());
    }

    private Button button_addWidget;

    protected void setActionButtonText(String text)
    {
        if (button_addWidget != null)
        {
            button_addWidget.setText(text);
        }
    }

    protected void setTitleTextEnabled(boolean value)
    {
        label_titleText.setEnabled(value);
        text_titleText.setEnabled(value);
    }

    protected void setUseSolarTime(boolean value)
    {
        label_solartime.setEnabled(value);
        spinner_solartime.setEnabled(value);
        layout_solartime.setVisibility((value ? View.VISIBLE : View.GONE));
        layout_timezone.setVisibility((value ? View.GONE : View.VISIBLE));
    }

    protected void setCustomTimezoneEnabled(boolean value)
    {
        String timezoneID = (value ? customTimezoneID : TimeZone.getDefault().getID());

        if (spinner_timezone_adapter != null)
        {
            spinner_timezone.setSelection(spinner_timezone_adapter.ordinal(timezoneID), true);
        }

        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
    }

    private boolean triggerTimeZoneActionMode(View view)
    {
        if (actionMode == null)
        {
            actionMode = startSupportActionMode(spinner_timezone_actionMode);
            actionMode.setTitle(getString(R.string.timezone_sort_contextAction));
            return true;
        }
        return false;
    }

    /**
     *
     */
    CheckBox.OnCheckedChangeListener onShowTitleListener = new CheckBox.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            setTitleTextEnabled(isChecked);
        }
    };

    /**
     * OnItemSelected (TimeZone Mode)
     */
    Spinner.OnItemSelectedListener onTimezoneModeListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
            WidgetSettings.TimezoneMode timezoneMode = timezoneModes[parent.getSelectedItemPosition()];
            setCustomTimezoneEnabled((timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE));
            setUseSolarTime((timezoneMode == WidgetSettings.TimezoneMode.SOLAR_TIME));
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };

    /**
     * OnItemSelected (Action Mode)
     */
    Spinner.OnItemSelectedListener onActionModeListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final WidgetSettings.ActionMode[] actionModes = WidgetSettings.ActionMode.values();
            WidgetSettings.ActionMode actionMode = actionModes[parent.getSelectedItemPosition()];

            View launchActionView = findViewById(R.id.applayout_action_launch);
            if (launchActionView != null)
            {
                switch (actionMode)
                {
                    case ONTAP_LAUNCH_ACTIVITY:
                        launchActionView.setVisibility(View.VISIBLE);
                        break;

                    case ONTAP_DONOTHING:
                    default:
                        launchActionView.setVisibility(View.GONE);
                        break;
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };

    /**
     * Save UI state to settings (appearance group).
     *
     * @param context the android application context
     */
    protected void saveAppearanceSettings(Context context)
    {
        // save: widgetmode_1x1
        final WidgetSettings.WidgetMode1x1[] modes = WidgetSettings.WidgetMode1x1.values();
        WidgetSettings.WidgetMode1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
        WidgetSettings.save1x1ModePref(context, appWidgetId, mode);
        //Log.d("DEBUG", "Saved mode: " + mode.name());

        // save: theme
        final ThemeDescriptor[] themes = WidgetThemes.values();
        ThemeDescriptor theme = themes[spinner_theme.getSelectedItemPosition()];
        WidgetSettings.saveThemePref(context, appWidgetId, theme.name());
        //Log.d("DEBUG", "Saved theme: " + theme.name());

        // save: allow resize
        boolean allowResize = checkbox_allowResize.isChecked();
        WidgetSettings.saveAllowResizePref(context, appWidgetId, allowResize);

        // save: show title
        boolean showTitle = checkbox_showTitle.isChecked();
        WidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

        // save:: title text
        String titleText = text_titleText.getText().toString().trim();
        WidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);
    }

    /**
     * Load settings into UI state (appearance group).
     *
     * @param context the android application context
     */
    protected void loadAppearanceSettings(Context context)
    {
        // load: widgetmode_1x1
        WidgetSettings.WidgetMode1x1 mode1x1 = WidgetSettings.load1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());

        // load: theme
        SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetId);
        ThemeDescriptor themeDescriptor;
        try
        {
            themeDescriptor = WidgetThemes.valueOf(theme.themeName());
        } catch (InvalidParameterException e)
        {
            Log.e("loadAppearanceSettings", "Failed to load theme " + theme.themeName());
            themeDescriptor = DarkTheme.THEMEDEF_DESCRIPTOR;
        }
        if (themeDescriptor != null)
        {
            spinner_theme.setSelection(themeDescriptor.ordinal(WidgetThemes.values()));
        } else
        {
            Log.e("loadAppearanceSettings", "theme is not installed! " + theme.themeName());
        }

        // load: allow resize
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            boolean allowResize = WidgetSettings.loadAllowResizePref(context, appWidgetId);
            checkbox_allowResize.setChecked(allowResize);
        } else
        {
            disableOptionAllowResize();
        }

        loadTitleSettings(context);
    }

    protected void loadTitleSettings(Context context)
    {
        // load: show title
        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

        // load: title text
        String titleText = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        text_titleText.setText(titleText);
    }

    /**
     * Save UI state to settings (general group).
     *
     * @param context the android application context
     */
    protected void saveGeneralSettings(Context context)
    {
        // save: calculator mode
        final SuntimesCalculatorDescriptor[] calculators = supportingCalculators();
        SuntimesCalculatorDescriptor calculator = calculators[spinner_calculatorMode.getSelectedItemPosition()];
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, calculator);

        // save: tracking mode
        final WidgetSettings.TrackingMode[] trackingModes = WidgetSettings.TrackingMode.values();
        WidgetSettings.TrackingMode trackingMode = trackingModes[spinner_trackingMode.getSelectedItemPosition()];
        WidgetSettings.saveTrackingModePref(context, appWidgetId, trackingMode);

        // save: compare mode
        final WidgetSettings.CompareMode[] compareModes = WidgetSettings.CompareMode.values();
        WidgetSettings.CompareMode compareMode = compareModes[spinner_compareMode.getSelectedItemPosition()];
        WidgetSettings.saveCompareModePref(context, appWidgetId, compareMode);

        // save: showNoon
        boolean showNoon = checkbox_showNoon.isChecked();
        WidgetSettings.saveShowNoonPref(context, appWidgetId, showNoon);

        // save: showCompare
        boolean showCompare = checkbox_showCompare.isChecked();
        WidgetSettings.saveShowComparePref(context, appWidgetId, showCompare);

        // save: showSeconds
        boolean showSeconds = checkbox_showSeconds.isChecked();
        WidgetSettings.saveShowSecondsPref(context, appWidgetId, showSeconds);

        // save: time mode
        saveTimeMode(context);
        saveTimeModeOverride(context);
    }

    /**
     * Load settings into UI state (general group).
     *
     * @param context the android application context
     */
    protected void loadGeneralSettings(Context context)
    {
        // load: calculator mode
        SuntimesCalculatorDescriptor[] calculators = supportingCalculators();
        SuntimesCalculatorDescriptor calculatorMode = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        spinner_calculatorMode.setSelection((calculatorMode != null ? calculatorMode.ordinal(calculators) : 0));

        // load: tracking mode
        WidgetSettings.TrackingMode trackingMode = WidgetSettings.loadTrackingModePref(context, appWidgetId);
        spinner_trackingMode.setSelection(trackingMode.ordinal());

        // load: compare mode
        WidgetSettings.CompareMode compareMode = WidgetSettings.loadCompareModePref(context, appWidgetId);
        spinner_compareMode.setSelection(compareMode.ordinal());

        // load: showCompare
        boolean showCompare = WidgetSettings.loadShowComparePref(context, appWidgetId);
        checkbox_showCompare.setChecked(showCompare);
        showCompareUI(showCompare);

        // load: showNoon
        boolean showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        checkbox_showNoon.setChecked(showNoon);

        // load: showSeconds
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        checkbox_showSeconds.setChecked(showSeconds);

        // load: time mode
        loadTimeMode(context);
        loadTimeModeOverride(context);
    }

    /**
     * Save UI state to settings (timezone group).
     *
     * @param context the android application context
     */
    protected void saveTimezoneSettings(Context context)
    {
        // save: timezone mode
        final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        if (customTimezone != null)
        {
            WidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());
        } else
        {
            Log.e("saveTimezoneSettings", "Failed to save timezone; none selected (was null). The timezone selector may not have been fully loaded..");
        }

        // save: solar timemode
        WidgetSettings.SolarTimeMode[] solarTimeModes = WidgetSettings.SolarTimeMode.values();
        WidgetSettings.SolarTimeMode solarTimeMode = solarTimeModes[spinner_solartime.getSelectedItemPosition()];
        WidgetSettings.saveSolarTimeModePref(context, appWidgetId, solarTimeMode);
    }

    /**
     * Load settings into UI state (timezone group).
     *
     * @param context the android application context
     */
    protected void loadTimezoneSettings(Context context)
    {
        WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        WidgetSettings.SolarTimeMode solartimeMode = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
        spinner_solartime.setSelection(solartimeMode.ordinal());

        setCustomTimezoneEnabled(timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        setUseSolarTime(timezoneMode == WidgetSettings.TimezoneMode.SOLAR_TIME);

        customTimezoneID = WidgetSettings.loadTimezonePref(context, appWidgetId);
        WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
    }

    /**
     * Save UI state to settings (action group).
     *
     * @param context the android application context
     */
    protected void saveActionSettings(Context context)
    {
        // save: action mode
        WidgetSettings.ActionMode actionMode = (WidgetSettings.ActionMode) spinner_onTap.getSelectedItem();
        WidgetSettings.saveActionModePref(context, appWidgetId, actionMode);

        // save: launch activity
        String launchString = text_launchActivity.getText().toString();
        WidgetSettings.saveActionLaunchPref(context, appWidgetId, launchString);
    }

    /**
     * Load settings into UI state (action group).
     *
     * @param context the android application context
     */
    protected void loadActionSettings(Context context)
    {
        // load: action mode
        WidgetSettings.ActionMode actionMode = WidgetSettings.loadActionModePref(context, appWidgetId, defaultActionMode());
        spinner_onTap.setSelection(actionMode.ordinal(supportedActionModes()));

        // load: launch activity
        String launchString = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
        text_launchActivity.setText(launchString);
    }

    /**
     * Click handler executed when the "Add Widget" button is pressed.
     */
    View.OnClickListener onAddButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            addWidget();
        }
    };

    protected void addWidget()
    {
        boolean hasValidInput = locationConfig.validateInput();  // todo: && validate other potentially troublesome input values
        if (hasValidInput)
        {
            locationConfig.setMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
            locationConfig.populateLocationList();  // triggers 'add place'

            final Context context = SuntimesConfigActivity0.this;
            saveSettings(context);
            updateWidget(context);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }

    protected void updateWidget(Context context)
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);

        SuntimesLayout defLayout = WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget0.class, minSize, defLayout);
    }

    /**
     * Click handler executed when the "About" button is pressed.
     */
    View.OnClickListener onAboutButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            AboutDialog aboutDialog = new AboutDialog();
            aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
        }
    };

    /**
     * @param requestCode  the request code that was passed to requestPermissions
     * @param permissions  the requested permissions
     * @param grantResults either PERMISSION_GRANTED or PERMISSION_DENIED for each of the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        locationConfig.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     *
     */
    protected void disableOptionAllowResize()
    {
        if (checkbox_allowResize != null)
        {
            checkbox_allowResize.setChecked(false);
            checkbox_allowResize.setEnabled(false);
        }
    }

    protected void showDataSource(boolean showDataSourceUI)
    {
        View dataSourceLayout = findViewById(R.id.appwidget_general_calculator_layout);
        if (dataSourceLayout != null)
        {
            dataSourceLayout.setVisibility((showDataSourceUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showCompareUI true: show comparison ui, false: hide comparison ui
     */
    protected void showCompareUI(boolean showCompareUI)
    {
        View compareModeLayout = findViewById(R.id.appwidget_general_compareMode_layout);
        if (compareModeLayout != null)
        {
            compareModeLayout.setVisibility((showCompareUI && !hideCompareAgainst ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showOption true: show noon ui, false: hide noon ui
     */
    protected void showOptionShowNoon(boolean showOption)
    {
        View layout_showNoon = findViewById(R.id.appwidget_general_showNoon_layout);
        if (layout_showNoon != null)
        {
            layout_showNoon.setVisibility((showOption ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showOptionTimeModeOverride(boolean showUI)
    {
        View layout_timeModeOverride = findViewById(R.id.appwidget_general_timeMode_override_layout);
        if (layout_timeModeOverride != null)
        {
            layout_timeModeOverride.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showOptionTrackingMode(boolean showUI)
    {
        View layout_trackingMode = findViewById(R.id.appwidget_general_trackingMode_layout);
        if (layout_trackingMode != null)
        {
            layout_trackingMode.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     *
     */
    protected void hideOptionCompareAgainst()
    {
        hideCompareAgainst = true;
        View layout_showCompare = findViewById(R.id.appwidget_general_showCompare_layout);
        if (layout_showCompare != null)
        {
            layout_showCompare.setVisibility(View.GONE);
        }
        showCompareUI(false);
    }
    private boolean hideCompareAgainst = false;

    /**
     *
     */
    protected void hideOption1x1LayoutMode()
    {
        View layout_1x1mode = findViewById(R.id.appwidget_appearance_1x1mode_layout);
        if (layout_1x1mode != null)
        {
            layout_1x1mode.setVisibility(View.GONE);
        }
    }

    /**
     * @param text activity title text
     */
    protected void setConfigActivityTitle(String text)
    {
        TextView activityTitle = (TextView) findViewById(R.id.activity_title);
        if (activityTitle != null)
        {
            activityTitle.setText(text);
        }
    }

    /**
     * @param requestCode anticipates PICK_THEME_REQUEST
     * @param resultCode RESULT_OK, RESULT_CANCELED
     * @param data an Intent with extra string data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case PICK_THEME_REQUEST:
                onPickThemeResult(resultCode, data);
                break;
        }
    }

    /**
     * @param resultCode RESULT_OK a theme was selected, a theme was added, or a theme was removed, and RESULT_CANCELED otherwise.
     * @param data an Intent with data; "name" extra contains selected themeName if a selection was made, "isModified" is true if list of themes was changed.
     */
    protected void onPickThemeResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String paramSelection = data.getStringExtra(SuntimesTheme.THEME_NAME);
            String themeName = (paramSelection != null) ? paramSelection
                                                        : ((ThemeDescriptor)spinner_theme.getSelectedItem()).name();

            boolean paramReloadAdapter = data.getBooleanExtra(WidgetThemeListActivity.ADAPTER_MODIFIED, false);
            if (paramReloadAdapter)
            {
                Log.d("selectTheme", "reloading list of themes...");
                initThemeAdapter(this);
            }

            if (themeName != null)
            {
                selectTheme(themeName);
            }
        }
    }

    private void selectTheme(String themeName)
    {
        ThemeDescriptor themeDescriptor = WidgetThemes.valueOf(themeName);
        if (themeDescriptor == null)
        {
            Log.w("selectTheme", "unable to find " + themeName + " (null descriptor); reverting to default.");
            themeDescriptor = WidgetThemes.valueOf(WidgetSettings.PREF_DEF_APPEARANCE_THEME);
            if (themeDescriptor == null)
            {
                Log.e("selectTheme", "failed to revert to default! " + WidgetSettings.PREF_DEF_APPEARANCE_THEME + " not found.");
                return;
            }
        }

        int position = themeDescriptor.ordinal(spinner_themeAdapter.values());
        if (position >= 0)
        {
            spinner_theme.setSelection(position, true);
            Log.d("selectTheme", "selected theme: " + themeDescriptor.name());

        } else {
            Log.w("selectTheme", "unable to find " + themeDescriptor.name() + " (bad position).");
        }
    }
}
