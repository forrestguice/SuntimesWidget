/**
    Copyright (C) 2014-2024 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;

import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptorListAdapter;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.events.EventListActivity;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;

import com.forrestguice.suntimeswidget.getfix.LocationConfigDialog;
import com.forrestguice.suntimeswidget.getfix.LocationConfigView;
import com.forrestguice.suntimeswidget.getfix.PlacesActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.actions.EditActionView;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsExportTask;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsMetadata;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesThemeContract;
import com.forrestguice.suntimeswidget.themes.WidgetThemePreview;
import com.forrestguice.suntimeswidget.themes.defaults.DarkTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.events.EventListActivity.PICK_EVENT_REQUEST;
import static com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity.PICK_THEME_REQUEST;

/**
 * Widget config activity for resizable widget.
 */
@SuppressWarnings("Convert2Diamond")
public class SuntimesConfigActivity0 extends AppCompatActivity
{
    public static final String EXTRA_RECONFIGURE = "ONTAP_LAUNCH_CONFIG";

    protected static final String DIALOGTAG_ABOUT = "about";
    protected static final String DIALOGTAG_HELP = "help";

    protected static final String HELPTAG_SUBSTITUTIONS = "help_substitutions";

    public static final int IMPORT_REQUEST = 100;
    public static final int EXPORT_REQUEST = 200;

    protected int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    protected boolean reconfigure = false;
    protected ContentValues themeValues;

    private ActionBar actionBar;
    protected TextView text_appWidgetID;
    protected View progressView;

    protected ScrollView scrollView;

    protected Spinner spinner_calculatorMode;
    protected Spinner spinner_timeFormatMode;
    protected CheckBox checkbox_timeModeOverride;
    protected ImageButton button_timeModeHelp;
    protected Spinner spinner_trackingMode;
    protected Spinner spinner_compareMode;
    protected CheckBox checkbox_showNoon;
    protected CheckBox checkbox_showCompare;
    protected CheckBox checkbox_showSeconds;
    protected CheckBox checkbox_showDate;
    protected CheckBox checkbox_showTimeDate;
    protected CheckBox checkbox_showAbbrMonth;
    protected CheckBox checkbox_showWeeks;
    protected CheckBox checkbox_showHours;
    protected CheckBox checkbox_useAltitude;
    protected CheckBox checkbox_locationFromApp;
    protected CheckBox checkbox_tzFromApp;
    protected CheckBox checkbox_localizeHemisphere;
    protected EditText edit_dayOffset;

    protected Spinner spinner_timeMode;
    protected ImageButton button_timeModeMenu;

    protected Spinner spinner_riseSetOrder;
    protected ImageButton button_riseSetOrderHelp;

    protected Spinner spinner_onTap;
    protected EditActionView edit_launchIntent;

    protected TextView button_themeConfig;
    private WidgetThemes.ThemeListAdapter spinner_themeAdapter;
    protected Spinner spinner_theme;

    protected TextView label_1x1mode, label_2x1mode, label_2x2mode, label_3x1mode, label_3x2mode, label_3x3mode;
    protected Spinner spinner_1x1mode, spinner_2x1mode, spinner_2x2mode, spinner_3x1mode, spinner_3x2mode, spinner_3x3mode;
    protected CheckBox checkbox_allowResize;
    protected CheckBox checkbox_scaleText;
    protected CheckBox checkbox_scaleBase;
    protected Spinner spinner_gravity;

    protected CheckBox checkbox_showTitle;
    protected TextView label_titleText;
    protected EditText text_titleText;
    protected CheckBox checkbox_showLabels;

    protected LocationConfigView locationConfig;

    protected TextView label_timezoneMode;
    protected Spinner spinner_timezoneMode;

    protected LinearLayout layout_timezone;
    protected TextView label_timezone;
    protected ImageButton button_timezone_sort;
    protected Spinner spinner_timezone;
    protected ProgressBar progress_timezone;

    protected LinearLayout layout_solartime;
    protected TextView label_solartime;
    protected Spinner spinner_solartime;
    protected ImageButton button_solartime_help;

    protected String customTimezoneID;
    protected ActionMode.Callback spinner_timezone_actionMode;
    protected WidgetTimezones.TimeZoneItemAdapter spinner_timezone_adapter;

    protected ActionMode actionMode = null;

    public SuntimesConfigActivity0()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        GetFixUI.themeIcons(this);

        super.onCreate(icicle);
        initLocale(this);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_settings);

        Context context = SuntimesConfigActivity0.this;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            reconfigure = extras.getBoolean(EXTRA_RECONFIGURE, false);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            Log.w("onCreate", "Invalid widget ID! returning early.");
            finish();
            overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
            return;
        }

        Intent cancelIntent = new Intent();
        cancelIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, cancelIntent);

        WidgetThemes.initThemes(context);
        SuntimesTheme theme = WidgetSettings.loadThemePref(this, appWidgetId);
        int[] padding = theme.getPaddingPixels(context);    // caches pixel values
        themeValues = theme.toContentValues();

        initViews(context);
        loadSettings(context);
    }

    protected void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        WidgetTimezones.TimeZoneSort.initDisplayStrings(context);
    }

    @Override
    public void onDestroy()
    {
        if (locationConfig != null) {           // null if onCreate finishes early
            locationConfig.cancelGetFix();
        }
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        edit_launchIntent.setOnExpandedChangedListener(onEditLaunchIntentExpanded);
        edit_launchIntent.onResume(getSupportFragmentManager(), getData(this, appWidgetId));
    }

    public SuntimesData getData(Context context, int appWidgetId) {
        return new SuntimesRiseSetData(context, appWidgetId);
    }

    /**
     * Save settings (as represented by the state of the config UI).
     *
     * @param context the android application context
     */
    protected void saveSettings(Context context)
    {
        saveLayoutSettings(context);
        saveGeneralSettings(context);
        saveCalendarSettings(context);
        locationConfig.saveSettings(context);
        saveTimezoneSettings(context);
        saveAppearanceSettings(context);
        saveActionSettings(context);
        saveMoreGeneralSettings(context);
        saveMetadata(context);
    }

    /**
     * Load settings (update the state of the config UI).
     *
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        locationConfig.loadSettings(context);
        loadLayoutSettings(context);
        loadGeneralSettings(context);
        loadMoreGeneralSettings(context);
        loadCalendarSettings(context);
        loadAppearanceSettings(context);
        loadTimezoneSettings(context);
        loadActionSettings(context);
    }

    protected ArrayAdapter<SuntimesCalculatorDescriptor> createAdapter_calculators()
    {
        SuntimesCalculatorDescriptor[] calculators = supportingCalculators();
        SuntimesCalculatorDescriptorListAdapter adapter= new SuntimesCalculatorDescriptorListAdapter(this, R.layout.layout_listitem_oneline, R.layout.layout_listitem_twoline, calculators);
        adapter.setDefaultValue(defaultCalculator());
        return adapter;
    }

    protected String defaultCalculator()
    {
        return WidgetSettings.PREF_DEF_GENERAL_CALCULATOR;
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

    protected WidgetTimezones.TimeZoneItemAdapter createAdapter_solarTimeMode()
    {
        int c = 0;
        ArrayList<WidgetTimezones.TimeZoneItem> items = new ArrayList<>();
        for (WidgetSettings.SolarTimeMode value : WidgetSettings.SolarTimeMode.values()) {
            items.add(new WidgetTimezones.TimeZoneItem(value.getID(), value.getDisplayString(), c++));
        }
        return new WidgetTimezones.TimeZoneItemAdapter(this, R.layout.layout_listitem_timezone, items, R.string.timezoneCustom_line1, R.string.timezoneCustom_line2b);
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

    // TODO: enhanced adapter
    protected WidgetModeAdapter createAdapter_widgetModeSun1x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSun1x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    protected WidgetModeAdapter createAdapter_widgetModeSun2x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSun2x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    protected WidgetModeAdapter createAdapter_widgetModeSun3x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetModeSun3x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.ActionMode> createAdapter_actionMode()
    {
        ArrayAdapter<WidgetSettings.ActionMode> adapter = new ArrayAdapter<WidgetSettings.ActionMode>(this, R.layout.layout_listitem_oneline, supportedActionModes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected ArrayAdapter<WidgetSettings.RiseSetOrder> createAdapter_riseSetOrder()
    {
        ArrayAdapter<WidgetSettings.RiseSetOrder> adapter = new ArrayAdapter<WidgetSettings.RiseSetOrder>(this, R.layout.layout_listitem_oneline, WidgetSettings.RiseSetOrder.values());
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

    @SuppressWarnings("ResourceType")
    protected void initViews(final Context context)
    {
        initToolbar(context);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        progressView = findViewById(R.id.progress);

        text_appWidgetID = (TextView) findViewById(R.id.text_appwidgetid);
        if (text_appWidgetID != null)
        {
            text_appWidgetID.setText(String.format("%s", appWidgetId));
        }

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
            //setConfigActivityTitle(getString(R.string.configAction_reconfigWidget));
        }

        initMoreGeneralSettings(context);

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
        edit_launchIntent = (EditActionView) findViewById(R.id.appwidget_action_launch_edit);
        edit_launchIntent.setFragmentManager(getSupportFragmentManager());
        edit_launchIntent.setData(getData(this, appWidgetId));

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
                    launchThemeEditor(context);
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
            spinner_calculatorMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    SuntimesCalculatorDescriptor descriptor = (SuntimesCalculatorDescriptor)adapterView.getItemAtPosition(i);
                    checkbox_useAltitude.setEnabled(descriptor.hasRequestedFeature(SuntimesCalculator.FEATURE_ALTITUDE));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        // widget: show date, calendar mode
        checkbox_showDate = (CheckBox) findViewById(R.id.appwidget_general_showDate);
        initCalendarMode(context);

        //
        // widget: time format mode
        //
        spinner_timeFormatMode = (Spinner) findViewById(R.id.appwidget_general_timeformatmode);
        initTimeFormatMode(context);

        //
        // widget: time mode
        //
        spinner_timeMode = (Spinner) findViewById(R.id.appwidget_general_timeMode);
        checkbox_timeModeOverride = (CheckBox) findViewById(R.id.appwidget_general_timeMode_override);
        button_timeModeHelp = (ImageButton) findViewById(R.id.appwidget_general_timeMode_helpButton);
        button_timeModeMenu = (ImageButton) findViewById(R.id.appwidget_general_timeMode_moreButton);
        initTimeMode(context);

        //
        // widget: timezone mode
        //
        label_timezoneMode = (TextView) findViewById(R.id.appwidget_timezone_mode_label);
        spinner_timezoneMode = (Spinner) findViewById(R.id.appwidget_timezone_mode);
        if (spinner_timezoneMode != null)
        {
            spinner_timezoneMode.setAdapter(createAdapter_timezoneMode());
            spinner_timezoneMode.setOnItemSelectedListener(onTimezoneModeListener);
        }

        //
        // widget: date offset
        //
        edit_dayOffset = (EditText) findViewById(R.id.appwidget_general_dayOffset);

        //
        // widget: riseSetOrder
        //
        spinner_riseSetOrder = (Spinner) findViewById(R.id.appwidget_general_riseSetOrder);
        if (spinner_riseSetOrder != null)
        {
            spinner_riseSetOrder.setAdapter(createAdapter_riseSetOrder());
        }

        button_riseSetOrderHelp = (ImageButton) findViewById(R.id.appwidget_general_riseSetOrder_helpButton);
        if (button_riseSetOrderHelp != null)
        {
            button_riseSetOrderHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_general_riseSetOrder));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        //
        // widget: timezone / solartime
        //
        layout_timezone = (LinearLayout) findViewById(R.id.appwidget_timezone_custom_layout);
        button_timezone_sort = (ImageButton) findViewById(R.id.sort_timezones);
        label_timezone = (TextView) findViewById(R.id.appwidget_timezone_custom_label);
        spinner_timezone = (Spinner) findViewById(R.id.appwidget_timezone_custom);
        progress_timezone = (ProgressBar) findViewById(R.id.appwidget_timezone_progress);

        if (button_timezone_sort != null)
        {
            TooltipCompat.setTooltipText(button_timezone_sort, button_timezone_sort.getContentDescription());
            button_timezone_sort.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //triggerTimeZoneActionMode(view);
                    showTimeZoneSortMenu(context, view);
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
                    progress_timezone.setVisibility(View.VISIBLE);
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
                    progress_timezone.setVisibility(View.GONE);
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

        button_solartime_help = (ImageButton) findViewById(R.id.appwidget_solartime_help);
        button_solartime_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getString(R.string.help_general_solartime));
                helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
            }
        });

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
                AppSettings.saveTimeZoneSortPref(SuntimesConfigActivity0.this, sortMode);
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
            locationConfig.setAutoAllowed(true);
            locationConfig.setHideMode(false);
            locationConfig.init(this, false, this.appWidgetId);
            locationConfig.setOnListButtonClicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuntimesConfigActivity0.this, PlacesActivity.class);
                    intent.putExtra(PlacesActivity.EXTRA_ALLOW_PICK, true);
                    startActivityForResult(intent, LocationConfigDialog.REQUEST_LOCATION);
                }
            });
        }

        //
        // widget: 1x1, 2x1, 2x2, 3x1, 3x2, 3x3 widget modes
        //
        label_1x1mode = (TextView) findViewById(R.id.appwidget_appearance_1x1mode_label);
        spinner_1x1mode = (Spinner) findViewById(R.id.appwidget_appearance_1x1mode);
        initWidgetMode1x1(context);

        label_2x1mode = (TextView) findViewById(R.id.appwidget_appearance_2x1mode_label);
        spinner_2x1mode = (Spinner) findViewById(R.id.appwidget_appearance_2x1mode);
        initWidgetMode2x1(context);

        label_2x2mode = (TextView) findViewById(R.id.appwidget_appearance_2x2mode_label);
        spinner_2x2mode = (Spinner) findViewById(R.id.appwidget_appearance_2x2mode);
        initWidgetMode2x2(context);

        label_3x1mode = (TextView) findViewById(R.id.appwidget_appearance_3x1mode_label);
        spinner_3x1mode = (Spinner) findViewById(R.id.appwidget_appearance_3x1mode);
        initWidgetMode3x1(context);

        label_3x2mode = (TextView) findViewById(R.id.appwidget_appearance_3x2mode_label);
        spinner_3x2mode = (Spinner) findViewById(R.id.appwidget_appearance_3x2mode);
        initWidgetMode3x2(context);

        label_3x3mode = (TextView) findViewById(R.id.appwidget_appearance_3x3mode_label);
        spinner_3x3mode = (Spinner) findViewById(R.id.appwidget_appearance_3x3mode);
        initWidgetMode3x3(context);

        final TextView primaryWidgetModeLabel = (TextView) getPrimaryWidgetModeLabel();
        if (primaryWidgetModeLabel != null)
        {
            int[] colorAttrs = { R.attr.text_accentColor, R.attr.text_disabledColor, R.attr.buttonPressColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int accentColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.text_accent_light));
            int disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_disabled_dark));
            int pressedColor = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.btn_tint_pressed_dark));
            typedArray.recycle();
            primaryWidgetModeLabel.setTextColor(SuntimesUtils.colorStateList(accentColor, disabledColor, pressedColor));

            primaryWidgetModeLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    String modeDesc = primaryWidgetModeLabel.getText().toString().replace(":", "");
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_appearance_widgetlayout, modeDesc));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        //
        // widget: allow resize
        //
        checkbox_allowResize = (CheckBox) findViewById(R.id.appwidget_appearance_allowResize);
        if (checkbox_allowResize != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            disableOptionAllowResize();  // resizable widgets require api14+
        }

        ImageButton button_allowResizeHelp = (ImageButton) findViewById(R.id.appwidget_appearance_allowResize_helpButton);
        if (button_allowResizeHelp != null)
        {
            button_allowResizeHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_appearance_allowresize));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }

        initWidgetModeLayout(context);

        //
        // widget: gravity/alignment
        //
        spinner_gravity = (Spinner) findViewById(R.id.appwidget_appearance_gravity);
        if (spinner_gravity != null) {
            initGravityAdapter(context);
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
                    helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
                    helpDialog.setNeutralButtonListener(helpDialogListener_substitutions, HELPTAG_SUBSTITUTIONS);
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
        // widget: show labels
        //
        checkbox_showLabels = (CheckBox) findViewById(R.id.appwidget_appearance_showLabels);
        showOptionLabels(false);


        //
        // widget: scale text
        //
        checkbox_scaleText = (CheckBox) findViewById(R.id.appwidget_appearance_scaleText);
        if (checkbox_scaleText != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            disableOptionScaleText();  // scalable text require api14+
        }

        //
        // widget: scale base
        //
        checkbox_scaleBase = (CheckBox) findViewById(R.id.appwidget_appearance_scaleBase);
        if (checkbox_scaleBase != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            disableOptionScaleBase();  // scalable text require api14+
        }
        checkbox_scaleBase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spinner_gravity.setEnabled(!isChecked);
            }
        });

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
        // widget: showTimeDate
        //
        checkbox_showTimeDate = (CheckBox)findViewById(R.id.appwidget_general_showTimeDate);
        showOptionTimeDate(false);

        //
        // widget: showAbbrMonth
        //
        checkbox_showAbbrMonth = (CheckBox)findViewById(R.id.appwidget_general_showAbbrMonth);
        showOptionAbbrvMonth(false);

        //
        // widget: showWeeks
        //
        checkbox_showWeeks = (CheckBox)findViewById(R.id.appwidget_general_showWeeks);
        showOptionWeeks(false);

        //
        // widget: showHours
        //
        checkbox_showHours = (CheckBox)findViewById(R.id.appwidget_general_showHours);
        showOptionHours(false);

        //
        // widget: useAltitude
        //
        checkbox_useAltitude = (CheckBox)findViewById(R.id.appwidget_general_useAltitude);

        // widget: locationFromApp
        checkbox_locationFromApp = (CheckBox)findViewById(R.id.appwidget_location_fromapp);
        if (checkbox_locationFromApp != null)
        {
            checkbox_locationFromApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    locationConfig.setMode(isChecked ? LocationConfigView.LocationViewMode.MODE_DISABLED : LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
                    locationConfig.setAutoAllowed(!isChecked);
                    locationConfig.setHideMode(isChecked, true);
                    if (isChecked) {
                        locationConfig.updateViews(WidgetSettings.loadLocationPref(context, 0));
                    } else {
                        locationConfig.updateViews();
                    }
                }
            });
        }

        // widget: timezoneFromApp
        checkbox_tzFromApp = (CheckBox)findViewById(R.id.appwidget_timezone_fromapp);
        if (checkbox_tzFromApp != null)
        {
            checkbox_tzFromApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (label_timezoneMode != null) {
                        label_timezoneMode.setEnabled(!isChecked);
                    }
                    if (spinner_timezoneMode != null) {
                        spinner_timezoneMode.setEnabled(!isChecked);
                    }
                    loadTimezoneSettings(context, isChecked ? 0 : appWidgetId);
                }
            });
        }

        checkbox_localizeHemisphere = (CheckBox)findViewById(R.id.appwidget_general_localize_hemisphere);

        //
        // widget: about button
        //
        Button button_aboutWidget = (Button) findViewById(R.id.about_button);
        if (button_aboutWidget != null)
        {
            button_aboutWidget.setOnClickListener(onAboutButtonClickListener);
        }
    }

    protected void initToolbar(final Context context)
    {
        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(reconfigure ? R.string.configAction_reconfigWidget_short : R.string.configAction_addWidget));
        }
    }

    /**
     * @param context a context used to access resources
     */
    protected void initThemeAdapter(final Context context)
    {
        spinner_themeAdapter = new WidgetThemes.ThemeListAdapter(this, R.layout.layout_listitem_oneline, R.layout.layout_listitem_themes, WidgetThemes.sortedValues(false));
        spinner_theme.setAdapter(spinner_themeAdapter);
        spinner_theme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onThemeSelectionChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    protected void initGravityAdapter(final Context context)
    {
        ArrayAdapter<WidgetSettings.WidgetGravity> adapter = new ArrayAdapter<WidgetSettings.WidgetGravity>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetGravity.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_gravity.setAdapter(adapter);
    }

    /**
     * @param context a context used to access resources
     */
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null)
        {
            spinner_1x1mode.setAdapter(createAdapter_widgetModeSun1x1());
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveWidgetMode1x1(Context context)
    {
        final WidgetSettings.WidgetModeSun1x1[] modes = WidgetSettings.WidgetModeSun1x1.values();
        WidgetSettings.WidgetModeSun1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
        WidgetSettings.saveSun1x1ModePref(context, appWidgetId, mode);
        //Log.d("DEBUG", "Saved mode: " + mode.name());
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadWidgetMode1x1(Context context)
    {
        WidgetSettings.WidgetModeSun1x1 mode1x1 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());
    }

    protected void initWidgetMode2x1(Context context)
    {
        if (spinner_2x1mode != null) {
            spinner_2x1mode.setAdapter(createAdapter_widgetModeSun2x1());
        }
    }
    protected void loadWidgetMode2x1(Context context)
    {
        //WidgetSettings.WidgetModeSun1x1 mode1x1 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        //spinner_1x1mode.setSelection(mode1x1.ordinal());
    }
    protected void saveWidgetMode2x1(Context context)
    {
        // EMPTY
    }

    protected void initWidgetMode2x2(Context context) {
        // EMPTY
    }
    protected void loadWidgetMode2x2(Context context) {
        // EMPTY
    }
    protected void saveWidgetMode2x2(Context context) {
        // EMPTY
    }

    protected void initWidgetMode3x1(Context context)
    {
        if (spinner_3x1mode != null) {
            spinner_3x1mode.setAdapter(createAdapter_widgetModeSun3x1());
        }
    }
    protected void loadWidgetMode3x1(Context context)
    {
        //WidgetSettings.WidgetModeSun1x1 mode1x1 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        //spinner_1x1mode.setSelection(mode1x1.ordinal());
    }
    protected void saveWidgetMode3x1(Context context)
    {
        // EMPTY
    }

    /**
     * @param context a context used to access resources
     */
    protected void initWidgetMode3x2(Context context)
    {
        // EMPTY
    }
    protected void saveWidgetMode3x2(Context context)
    {
        // EMPTY
    }
    protected void loadWidgetMode3x2(Context context)
    {
        // EMPTY
    }

    protected void initWidgetMode3x3(Context context)
    {
        // EMPTY
    }
    protected void saveWidgetMode3x3(Context context)
    {
        // EMPTY
    }
    protected void loadWidgetMode3x3(Context context)
    {
        // EMPTY
    }

    protected void initWidgetModeLayout(Context context)
    {
        showOption2x1LayoutMode(true);
        showOption3x1LayoutMode(true);
        if (checkbox_allowResize != null)
        {
            checkbox_allowResize.setOnCheckedChangeListener(onAllowResizeChecked);
            onAllowResizeChecked.onCheckedChanged(null, checkbox_allowResize.isChecked());
        }
    }

    protected CompoundButton.OnCheckedChangeListener onAllowResizeChecked = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            TextView label = getPrimaryWidgetModeLabel();
            if (label != null) {
                label.setTypeface(label.getTypeface(), Typeface.BOLD);
                label.setPaintFlags(label.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
            for (View v : getPrimaryWidgetModeViews()) {
                if (v != null) {
                    v.setEnabled(true);
                }
            }
            for (View v : getSecondaryWidgetModeViews()) {
                if (v != null) {
                    v.setEnabled(isChecked);
                }
            }
        }
    };

    protected TextView getPrimaryWidgetModeLabel() {
        return label_1x1mode;
    }
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode };
    }
    protected View[] getSecondaryWidgetModeViews() {
        return new View[] { label_2x1mode, spinner_2x1mode, label_3x1mode, spinner_3x1mode, label_3x2mode, spinner_3x2mode, label_3x3mode, spinner_3x3mode };
    }

    protected void initMoreGeneralSettings(final Context context) { /* EMPTY */ }
    protected void saveMoreGeneralSettings(final Context context) { /* EMPTY */ }
    protected void loadMoreGeneralSettings(final Context context) { /* EMPTY */ }

    protected void initCalendarMode(final Context context) { /* EMPTY */ }
    protected void saveCalendarSettings(Context context) {
        CalendarSettings.saveCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, checkbox_showDate.isChecked());
    }
    protected void loadCalendarSettings(Context context) {
        checkbox_showDate.setChecked(CalendarSettings.loadCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE));
    }

    protected void dismissHelpDialog()
    {
        FragmentManager fragments = getSupportFragmentManager();
        HelpDialog dialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * @param context a context used to access resources
     */
    protected void initTimeFormatMode(Context context)
    {
        if (spinner_timeFormatMode != null)
        {
            final ArrayAdapter<WidgetSettings.TimeFormatMode> spinner_timeFormatModeAdapter;
            spinner_timeFormatModeAdapter = new ArrayAdapter<WidgetSettings.TimeFormatMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.TimeFormatMode.values());
            spinner_timeFormatModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_timeFormatMode.setAdapter(spinner_timeFormatModeAdapter);
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadTimeFormatMode(Context context)
    {
        if (spinner_timeFormatMode != null)
        {
            WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
            spinner_timeFormatMode.setSelection(mode.ordinal());
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveTimeFormatMode(Context context)
    {
        if (spinner_timeFormatMode != null)
        {
            final WidgetSettings.TimeFormatMode[] modes = WidgetSettings.TimeFormatMode.values();
            WidgetSettings.TimeFormatMode mode = modes[spinner_timeFormatMode.getSelectedItemPosition()];
            WidgetSettings.saveTimeFormatModePref(context, appWidgetId, mode);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void loadDateOffset(Context context)
    {
        if (edit_dayOffset != null) {
            edit_dayOffset.setText(WidgetSettings.loadDateOffsetPref(context, appWidgetId) + "");
        }
    }
    protected void saveDateOffset(Context context)
    {
        if (edit_dayOffset != null)
        {
            int offset = 0;
            try {
                offset = Integer.parseInt(edit_dayOffset.getText().toString());
            } catch (NumberFormatException e) {
                Log.e("saveSettings", "saveDateOffset: " + e);
            }
            WidgetSettings.saveDateOffsetPref(context, appWidgetId, offset);
        }
    }

    /**
     * @param context a context used to access resources
     */
    protected void initTimeMode(Context context)
    {
        if (spinner_timeMode != null)
        {
            ArrayList<WidgetSettings.RiseSetDataMode> items = new ArrayList<WidgetSettings.RiseSetDataMode>(Arrays.asList(WidgetSettings.TimeMode.values()));
            final TimeModeAdapter adapter = new TimeModeAdapter(this, R.layout.layout_listitem_events, items);
            adapter.setDropDownViewResource(R.layout.layout_listitem_one_line_colortab);
            adapter.setThemeValues(themeValues);
            spinner_timeMode.setAdapter(adapter);

            spinner_timeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    showOptionShowNoon(adapter.getItem(i) != WidgetSettings.TimeMode.NOON);
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
                public void onClick(View v) {
                    showTimeModeHelp();
                }
            });
            button_timeModeHelp.setVisibility(View.GONE);
        }

        if (button_timeModeMenu != null) {
            button_timeModeMenu.setOnClickListener(onTimeModeMenuButtonClicked);
            button_timeModeMenu.setVisibility(View.VISIBLE);
        }

        showOptionTimeModeOverride(false);
        showOptionTrackingMode(false);
    }

    private View.OnClickListener onTimeModeMenuButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showTimeModeMenu(SuntimesConfigActivity0.this, v);
        }
    };

    protected void showTimeModeMenu(Context context, View v)
    {
        PopupMenu menu = new PopupMenu(context, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.timemode_overflow, menu.getMenu());
        menu.setOnMenuItemClickListener(onTimeModeMenuClicked);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        prepareTimeModeMenu(context, menu.getMenu());
        menu.show();
    }

    protected void prepareTimeModeMenu(Context context, Menu menu) {
    }

    protected PopupMenu.OnMenuItemClickListener onTimeModeMenuClicked = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.action_manage_events:
                    launchEventEditor(SuntimesConfigActivity0.this);
                    return true;

                case R.id.action_help:
                    showTimeModeHelp();
                    return true;

                default:
                    return false;
            }
        }
    });

    protected void showTimeModeHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        String help0 = getString(R.string.help_general_actualTime);
        String help1 = getString(R.string.help_general_twilight);
        String help2 = getString(R.string.help_general_bluehour);
        String help3 = getString(R.string.help_general_goldhour);
        helpDialog.setContent(getString(R.string.help_general4, help0, help1, help2, help3));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    protected Intent eventEditorIntent(Context context)
    {
        Intent intent = new Intent(context, EventListActivity.class);
        WidgetSettings.RiseSetDataMode item = (WidgetSettings.RiseSetDataMode) spinner_timeMode.getSelectedItem();
        intent.putExtra(EventListActivity.EXTRA_SELECTED, item.name());
        return intent;
    }

    protected void launchEventEditor(Context context)
    {
        Intent configEventsIntent = eventEditorIntent(context);
        startActivityForResult(configEventsIntent, PICK_EVENT_REQUEST);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    protected int insertEventAliasIntoTimeModeAdapter(@NonNull Context context, String eventID)
    {
        TimeModeAdapter adapter = (TimeModeAdapter) spinner_timeMode.getAdapter();
        if (adapter != null)
        {
            for (int i=0; i<adapter.getCount(); i++)
            {
                WidgetSettings.RiseSetDataMode item = adapter.getItem(i);
                if (item != null && item.name().equals(eventID)) {
                    return i;
                }
            }

            if (EventSettings.hasEvent(context, eventID))
            {
                EventSettings.EventAlias event = EventSettings.loadEvent(SuntimesConfigActivity0.this, eventID);
                if (event.getType() == AlarmEventProvider.EventType.SUN_ELEVATION
                        || event.getType() == AlarmEventProvider.EventType.SHADOWLENGTH)
                {
                    WidgetSettings.EventAliasTimeMode item = new WidgetSettings.EventAliasTimeMode(event);
                    adapter.insert(item, 0);
                    return 0;
                } else {
                    Log.w("onPickEvent", "event has unexpected type! " + event.getType());
                }
            }
        }
        return -1;
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadTimeMode(Context context)
    {
        if (spinner_timeMode != null) {
            TimeModeAdapter adapter = (TimeModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null) {
                WidgetSettings.RiseSetDataMode timeMode = WidgetSettings.loadTimeModePref(context, appWidgetId);
                int position = adapter.getPosition(timeMode);
                if (position < 0) {
                    position = insertEventAliasIntoTimeModeAdapter(context, timeMode.name());
                }
                spinner_timeMode.setSelection(position);
            }
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveTimeMode(Context context)
    {
        if (spinner_timeMode != null) {
            TimeModeAdapter adapter = (TimeModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null) {
                WidgetSettings.RiseSetDataMode item = adapter.getItem(spinner_timeMode.getSelectedItemPosition());
                WidgetSettings.saveTimeModePref(context, appWidgetId, ((item!= null) ? item : WidgetSettings.PREF_DEF_GENERAL_TIMEMODE));
            }
        }
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
        boolean useAppTz = (checkbox_tzFromApp != null && checkbox_tzFromApp.isChecked());
        label_solartime.setEnabled(value && !useAppTz);
        spinner_solartime.setEnabled(value && !useAppTz);
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

        boolean useAppTz = (checkbox_tzFromApp != null && checkbox_tzFromApp.isChecked());
        button_timezone_sort.setEnabled(value && !useAppTz);
        label_timezone.setEnabled(value && !useAppTz);
        spinner_timezone.setEnabled(value && !useAppTz);
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

    protected boolean showTimeZoneSortMenu(Context context, View view)
    {
        PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.timezonesort, onTimeZoneSortMenuClick);
        TimeZoneDialog.updateTimeZoneSortMenu(context, menu);
        menu.show();
        return true;
    }
    private final PopupMenu.OnMenuItemClickListener onTimeZoneSortMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = SuntimesConfigActivity0.this;
            WidgetTimezones.TimeZoneSpinnerSortActionBase sortActionBase = new WidgetTimezones.TimeZoneSpinnerSortActionBase()
            {
                @Override
                public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
                {
                    super.onSortTimeZones(result, sortMode);
                    spinner_timezone_adapter = result;
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                    progress_timezone.setVisibility(View.GONE);
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.saveTimeZoneSortPref(context, sortMode);
                }
            };
            sortActionBase.init(context, spinner_timezone);
            return sortActionBase.onActionItemClicked(item.getItemId());
        }
    });

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
            final WidgetSettings.ActionMode[] actionModes = supportedActionModes();
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

    protected void saveLayoutSettings(Context context)
    {
        // save: widgetmode_1x1, 2x2, 3x1, 3x2, 3x3
        saveWidgetMode1x1(context);
        saveWidgetMode2x1(context);
        saveWidgetMode2x2(context);
        saveWidgetMode3x1(context);
        saveWidgetMode3x2(context);
        saveWidgetMode3x3(context);

        // save: allow resize
        boolean allowResize = checkbox_allowResize.isChecked();
        WidgetSettings.saveAllowResizePref(context, appWidgetId, allowResize);
    }

    protected void loadLayoutSettings(Context context)
    {
        // load: widgetmode_1x1, 2x2, 3x1, 3x2, 3x3
        loadWidgetMode1x1(context);
        loadWidgetMode2x1(context);
        loadWidgetMode2x2(context);
        loadWidgetMode3x1(context);
        loadWidgetMode3x2(context);
        loadWidgetMode3x3(context);

        // load: allow resize
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            boolean allowResize = WidgetSettings.loadAllowResizePref(context, appWidgetId);
            checkbox_allowResize.setChecked(allowResize);
        } else {
            disableOptionAllowResize();
        }
    }

    /**
     * Save UI state to settings (appearance group).
     *
     * @param context the android application context
     */
    protected void saveAppearanceSettings(Context context)
    {
        // save: theme
        ThemeDescriptor theme = (ThemeDescriptor)spinner_theme.getSelectedItem();
        WidgetSettings.saveThemePref(context, appWidgetId, theme.name());
        //Log.d("DEBUG", "Saved theme: " + theme.name());

        // save: scale text
        boolean scaleText = checkbox_scaleText.isChecked();
        WidgetSettings.saveScaleTextPref(context, appWidgetId, scaleText);

        // save: scale base
        boolean scaleBase = checkbox_scaleBase.isChecked();
        WidgetSettings.saveScaleBasePref(context, appWidgetId, scaleBase);

        // save: gravity
        WidgetSettings.WidgetGravity gravity = (WidgetSettings.WidgetGravity)spinner_gravity.getSelectedItem();
        WidgetSettings.saveWidgetGravityPref(context, appWidgetId, gravity.getPosition());

        // save: show title
        boolean showTitle = checkbox_showTitle.isChecked();
        WidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

        // save:: title text
        String titleText = text_titleText.getText().toString().trim();
        WidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);

        // save: show labels
        boolean showLabels = checkbox_showLabels.isChecked();
        WidgetSettings.saveShowLabelsPref(context, appWidgetId, showLabels);
    }

    /**
     * Load settings into UI state (appearance group).
     *
     * @param context the android application context
     */
    protected void loadAppearanceSettings(Context context)
    {
        // load: theme
        SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetId);
        ThemeDescriptor themeDescriptor;
        try
        {
            themeDescriptor = WidgetThemes.valueOf(theme.themeName());
        } catch (InvalidParameterException e) {
            Log.e("loadAppearanceSettings", "Failed to load theme " + theme.themeName());
            themeDescriptor = DarkTheme.themeDescriptor(context);
        }
        if (themeDescriptor != null)
        {
            spinner_theme.setSelection(themeDescriptor.ordinal(spinner_themeAdapter.values()), false);
        } else {
            Log.e("loadAppearanceSettings", "theme is not installed! " + theme.themeName());
        }

        // load: scale text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            boolean scaleText = WidgetSettings.loadScaleTextPref(context, appWidgetId, getDefaultScaleText());
            checkbox_scaleText.setChecked(scaleText);

            boolean scaleBase = WidgetSettings.loadScaleBasePref(context, appWidgetId, getDefaultScaleBase());
            checkbox_scaleBase.setChecked(scaleBase);

        } else {
            disableOptionScaleText();
            disableOptionScaleBase();
        }

        // load: gravity
        WidgetSettings.WidgetGravity gravity = WidgetSettings.WidgetGravity.findPosition(WidgetSettings.loadWidgetGravityPref(context, appWidgetId));
        spinner_gravity.setSelection(gravity != null ? gravity.ordinal() : WidgetSettings.PREF_DEF_APPEARANCE_GRAVITY.ordinal());

        loadTitleSettings(context);
        loadShowLabels(context);
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

    protected void loadShowLabels(Context context)
    {
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        checkbox_showLabels.setChecked(showLabels);
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

        // save: riseSetOrder
        final WidgetSettings.RiseSetOrder[] riseSetOrders = WidgetSettings.RiseSetOrder.values();
        WidgetSettings.RiseSetOrder riseSetOrder = (WidgetSettings.RiseSetOrder)spinner_riseSetOrder.getSelectedItem();
        WidgetSettings.saveRiseSetOrderPref(context, appWidgetId, riseSetOrder);

        // save: showNoon
        boolean showNoon = checkbox_showNoon.isChecked();
        WidgetSettings.saveShowNoonPref(context, appWidgetId, showNoon);

        // save: showCompare
        boolean showCompare = checkbox_showCompare.isChecked();
        WidgetSettings.saveShowComparePref(context, appWidgetId, showCompare);

        // save: showSeconds
        boolean showSeconds = checkbox_showSeconds.isChecked();
        WidgetSettings.saveShowSecondsPref(context, appWidgetId, showSeconds);

        // save: showTimeDate
        boolean showTimeDate = checkbox_showTimeDate.isChecked();
        WidgetSettings.saveShowTimeDatePref(context, appWidgetId, showTimeDate);

        // save: showAbbrMonth
        boolean showAbbrMonth = checkbox_showAbbrMonth.isChecked();
        WidgetSettings.saveShowAbbrMonthPref(context, appWidgetId, showAbbrMonth);

        // save: showWeeks
        boolean showWeeks = checkbox_showWeeks.isChecked();
        WidgetSettings.saveShowWeeksPref(context, appWidgetId, showWeeks);

        // save: showHours
        boolean showHours = checkbox_showHours.isChecked();
        WidgetSettings.saveShowHoursPref(context, appWidgetId, showHours);

        // save: useAltitude
        boolean useAltitude = checkbox_useAltitude.isChecked();
        WidgetSettings.saveLocationAltitudeEnabledPref(context, appWidgetId, useAltitude);

        // save: locationFromApp
        boolean locationFromApp = checkbox_locationFromApp.isChecked();
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId, locationFromApp);

        // save: tzFromApp
        boolean tzFromApp = checkbox_tzFromApp.isChecked();
        WidgetSettings.saveTimeZoneFromAppPref(context, appWidgetId, tzFromApp);

        // save: localize hemisphere
        WidgetSettings.saveLocalizeHemispherePref(context, appWidgetId, checkbox_localizeHemisphere.isChecked());

        // save: time mode
        saveTimeMode(context);
        saveTimeModeOverride(context);

        // save: time format
        saveTimeFormatMode(context);

        // save: date offset
        saveDateOffset(context);
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

        // load: riseSetOrder
        WidgetSettings.RiseSetOrder riseSetOrder = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        spinner_riseSetOrder.setSelection(riseSetOrder.ordinal());

        // load: showNoon
        boolean showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId, getDefaultShowSolarNoon());
        checkbox_showNoon.setChecked(showNoon);

        // load: showSeconds
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        checkbox_showSeconds.setChecked(showSeconds);

        // load showTimeDate
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        checkbox_showTimeDate.setChecked(showTimeDate);

        // load showAbbrMonth
        boolean showAbbrMonth = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        checkbox_showAbbrMonth.setChecked(showAbbrMonth);

        // load: showWeeks
        boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        checkbox_showWeeks.setChecked(showWeeks);

        // load: showHours
        boolean showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        checkbox_showHours.setChecked(showHours);

        // load: useAltitude
        boolean useAltitude = WidgetSettings.loadLocationAltitudeEnabledPref(context, appWidgetId);
        checkbox_useAltitude.setChecked(useAltitude);

        // load: locationFromApp
        boolean locationFromApp = WidgetSettings.loadLocationFromAppPref(context, appWidgetId, getDefaultLocationFromApp());
        checkbox_locationFromApp.setChecked(locationFromApp);

        // load: tzFromApp
        boolean tzFromApp = WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetId);
        checkbox_tzFromApp.setChecked(tzFromApp);

        // load: localize hemisphere
        checkbox_localizeHemisphere.setChecked(WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId));

        // load: time mode
        loadTimeMode(context);
        loadTimeModeOverride(context);

        // load: time format
        loadTimeFormatMode(context);

        // load: date offset
        loadDateOffset(context);
    }

    public boolean getDefaultShowSolarNoon() {
        return WidgetSettings.PREF_DEF_GENERAL_SHOWNOON;
    }

    public boolean getDefaultLocationFromApp() {
        return WidgetSettings.PREF_DEF_LOCATION_FROMAPP;
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
        int widgetId = (WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetId) ? 0 : appWidgetId);
        loadTimezoneSettings(context, widgetId);
    }
    protected void loadTimezoneSettings(Context context, int widgetId)
    {
        WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, widgetId, getDefaultTimezoneMode());
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        WidgetSettings.SolarTimeMode solartimeMode = WidgetSettings.loadSolarTimeModePref(context, widgetId);
        spinner_solartime.setSelection(solartimeMode.ordinal());

        setCustomTimezoneEnabled(timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        setUseSolarTime(timezoneMode == WidgetSettings.TimezoneMode.SOLAR_TIME);

        customTimezoneID = WidgetSettings.loadTimezonePref(context, widgetId);
        WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
    }

    protected WidgetSettings.TimezoneMode getDefaultTimezoneMode()
    {
        return WidgetSettings.PREF_DEF_TIMEZONE_MODE;
    }

    protected void saveMetadata(Context context)
    {
        WidgetSettingsMetadata.WidgetMetadata metadata = new WidgetSettingsMetadata.WidgetMetadata(
                getWidgetClass().getSimpleName(), BuildConfig.VERSION_CODE,
                WidgetSettingsMetadata.loadMetaData(context, appWidgetId)
        );
        WidgetSettingsMetadata.saveMetaData(context, appWidgetId, metadata);
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
        edit_launchIntent.saveIntent(context, appWidgetId, null, edit_launchIntent.getIntentTitle(), edit_launchIntent.getIntentDesc());
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
        edit_launchIntent.loadIntent(context, appWidgetId, null, launchActionIntentDefaults());
    }

    protected ContentValues launchActionIntentDefaults() {
        return WidgetActions.defaultLaunchPrefValues();
    }

    /**
     * OnEditLaunchIntentExpanded
     */
    private CompoundButton.OnCheckedChangeListener onEditLaunchIntentExpanded = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            buttonView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 250);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void showProgress( Context context, CharSequence title, CharSequence message )
    {
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
    }
    public void dismissProgress()
    {
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
    }

    /**
     * exportSettings
     */
    protected void exportSettings(Context context)
    {
        saveSettings(context);

        String exportTarget = getWidgetClass().getSimpleName() + "_" + appWidgetId;
        if (Build.VERSION.SDK_INT >= 19)
        {
            String filename = exportTarget + WidgetSettingsExportTask.FILEEXT;
            Intent intent = ExportTask.getCreateFileIntent(filename, WidgetSettingsExportTask.MIMETYPE);
            try {
                startActivityForResult(intent, EXPORT_REQUEST);
                return;

            } catch (ActivityNotFoundException e) {
                Log.e("ExportSettings", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
            }
        }

        WidgetSettingsExportTask task = new WidgetSettingsExportTask(context, exportTarget, true, true);  // export to external cache
        task.setTaskListener(exportSettingsListener);
        task.setAppWidgetId(appWidgetId);
        task.execute();
    }
    public void exportSettings(Context context, @NonNull Uri uri)
    {
        Log.i("ExportSettings", "Starting export task: " + uri);
        saveSettings(context);
        WidgetSettingsExportTask task = new WidgetSettingsExportTask(context, uri);
        task.setTaskListener(exportSettingsListener);
        task.setAppWidgetId(appWidgetId);
        task.execute();
    }

    private final WidgetSettingsExportTask.TaskListener exportSettingsListener = new WidgetSettingsExportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            //setRetainInstance(true);
            Context context = SuntimesConfigActivity0.this;
            showProgress(context, context.getString(R.string.exportwidget_dialog_title), context.getString(R.string.exportwidget_dialog_message));
        }

        @Override
        public void onFinished(WidgetSettingsExportTask.ExportResult results)
        {
            //setRetainInstance(false);
            dismissProgress();

            Context context = SuntimesConfigActivity0.this;
            if (context != null)
            {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath()
                        : ExportTask.getFileName(context.getContentResolver(), results.getExportUri()));

                if (results.getResult())
                {
                    //if (isAdded()) {
                    String successMessage = context.getString(R.string.msg_export_success, path);
                    Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show();
                    //}

                    if (Build.VERSION.SDK_INT >= 19) {
                        if (results.getExportUri() == null) {
                            ExportTask.shareResult(context, file, results.getMimeType());
                        }
                    } else {
                        ExportTask.shareResult(context, file, results.getMimeType());
                    }
                    return;
                }

                //if (isAdded()) {
                String failureMessage = context.getString(R.string.msg_export_failure, path);
                Toast.makeText(context, failureMessage, Toast.LENGTH_LONG).show();
                //}
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * importSettings
     */
    protected boolean importSettings(Context context)
    {
        if (context != null) {
            startActivityForResult(ExportTask.getOpenFileIntent("text/*"), IMPORT_REQUEST);
            return true;
        }
        return false;
    }
    public boolean importSettings(final Context context, @NonNull Uri uri)
    {
        Log.i("ImportSettings", "Starting import task: " + uri);
        WidgetSettingsImportTask task = new WidgetSettingsImportTask(context);
        task.setTaskListener(new WidgetSettingsImportTask.TaskListener()
        {
            @Override
            public void onStarted() {
                showProgress(context, context.getString(R.string.importwidget_dialog_title), context.getString(R.string.importwidget_dialog_message));
            }

            @Override
            public void onFinished(WidgetSettingsImportTask.TaskResult result)
            {
                dismissProgress();
                if (result.getResult() && result.numResults() > 0)
                {
                    ArrayList<ContentValues> values = new ArrayList<>();
                    CharSequence[] labels = new CharSequence[result.numResults()];

                    for (int i=0; i<result.numResults(); i++)
                    {
                        ContentValues v = result.getItems()[i];
                        WidgetSettingsMetadata.WidgetMetadata metadata = WidgetSettingsMetadata.WidgetMetadata.getMetaDataFromValues(v);
                        String values_widgetClassName = ((metadata != null) ? metadata.getWidgetClassName() : null);
                        labels[i] = context.getString(R.string.importwidget_dialog_item, (values_widgetClassName != null)
                                ? values_widgetClassName : context.getString(R.string.importwidget_dialog_item_unknown));

                        if (getWidgetClass().getSimpleName().equals(values_widgetClassName))
                        {
                            //Log.d("ImportSettings", "found settings for widget type " + values_widgetClassName + " at index " + i);
                            values.add(v);
                        }
                    }

                    if (values.size() == 1) {    // one match
                        importSettings(context, values.get(0));

                    } else if (values.size() > 1) {    // multiple matches; choose one
                        chooseImportValuesOfSameType(context, values);

                    } else {    // no matches; choose any
                        chooseImportValuesOfDifferentType(context, result.getItems(), labels);
                    }

                } else {
                    Toast.makeText(context, context.getString(R.string.msg_import_failure, context.getString(R.string.msg_import_label_file)), Toast.LENGTH_SHORT).show();
                }
            }
        });
        task.execute(uri);
        return true;
    }

    protected void chooseImportValuesOfSameType(final Context context, ArrayList<ContentValues> values)
    {
        final ContentValues[] matchingValues = values.toArray(new ContentValues[0]);
        CharSequence[] labels = new CharSequence[matchingValues.length];
        for (int i=0; i<matchingValues.length; i++) {
            labels[i] = (i + 1) + "";
        }
        String title = context.getString(R.string.importwidget_dialog_title2);
        AlertDialog.Builder confirm = new AlertDialog.Builder(context).setTitle(title).setIcon(android.R.drawable.ic_dialog_info)
                .setSingleChoiceItems(labels, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { /* EMPTY */ }
                })
                .setPositiveButton(context.getString(R.string.configAction_import), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        int p = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if ((p >= 0 && p < matchingValues.length)) {
                            //Log.d("ImportSettings", "user selected " + p + " of " + (matchingValues.length-1));
                            importSettings(context, matchingValues[p]);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }

    protected void chooseImportValuesOfDifferentType(final Context context, final ContentValues[] values, final CharSequence[] labels)
    {
        String title = context.getString(R.string.importwidget_dialog_title1);
        AlertDialog.Builder confirm = new AlertDialog.Builder(context).setTitle(title).setIcon(android.R.drawable.ic_dialog_alert)
                .setSingleChoiceItems(labels, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { /* EMPTY */ }
                })
                .setPositiveButton(context.getString(R.string.configAction_import), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        int p = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if ((p >= 0 && p < values.length)) {
                            //Log.d("ImportSettings", "user selected " + p + " of " + (values.length-1) + " (" + labels[p] + ")");
                            importSettings(context, values[p]);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }

    protected void importSettings(Context context, ContentValues values) {
        importSettings(context, values, true);
    }
    protected void importSettings(Context context, ContentValues values, boolean offerUndo)
    {
        SharedPreferences prefs0 = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        ContentValues previousValues = null;
        if (offerUndo) {
            previousValues = WidgetSettingsExportTask.toContentValues(prefs0, appWidgetId);
        }

        SharedPreferences.Editor prefs = prefs0.edit();
        WidgetSettingsImportTask.importValues(prefs, values, WidgetSettings.PREF_PREFIX_KEY, (long) appWidgetId);
        loadSettings(context);   // reload ui

        if (offerUndo) {
            //Toast.makeText(context, context.getString(R.string.msg_import_success, context.getString(R.string.configAction_settings)), Toast.LENGTH_SHORT).show();
            offerUndoImport(context, previousValues);
        }
    }

    protected  void offerUndoImport(final Context context, final ContentValues previous)
    {
        View view = getWindow().getDecorView();
        if (context != null && view != null)
        {
            CharSequence message = context.getString(R.string.msg_import_success, context.getString(R.string.configAction_settings));
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.configAction_undo), new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    importSettings(context, previous, false);
                }
            });
            ViewUtils.themeSnackbar(context, snackbar, null);
            snackbar.setDuration(UNDO_IMPORT_MILLIS);
            snackbar.show();
        }
    }
    public static final int UNDO_IMPORT_MILLIS = 12000;

    /**
     * clear settings (restore defaults)
     */
    protected void confirmResetWidget(final Context context)
    {
        String title = context.getString(R.string.configAction_restoreDefaults);
        AlertDialog.Builder confirm = new AlertDialog.Builder(context).setTitle(title).setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(context.getString(R.string.resetwidget_dialog_message))
                .setPositiveButton(context.getString(R.string.configAction_restoreDefaults), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        resetWidget();
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }
    protected void resetWidget()
    {
        WidgetSettings.deletePrefs(this, appWidgetId);
        onResetWidget();
        loadSettings(this);
    }
    protected void onResetWidget() { /* EMPTY */ }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
            locationConfig.setMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
            locationConfig.populateLocationList();  // triggers 'add place'

            final Context context = SuntimesConfigActivity0.this;
            saveSettings(context);
            updateWidgets(context,  new int[] {appWidgetId});
            CalculatorProvider.clearCachedConfig(appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
            overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
        }
    }

    protected Class getWidgetClass() {
        return SuntimesWidget0.class;
    }

    /**
     * Update all widgets of this type (direct update, no broadcast).
     * @param context a context used to access resources
     */
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //SunLayout defLayout = WidgetSettings.loadSun1x1ModePref_asLayout(context, appWidgetId);
        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //SuntimesWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, SuntimesWidget0.class, minWidgetSize(context), defLayout);
    }

    /**
     * @param context a context used to access resources
     * @return [w,h] minSize array; minimum size required by this type of widget
     */
    protected int[] minWidgetSize(Context context)
    {
        int minSize[] = new int[2];
        minSize[0] = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        minSize[1] = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        return minSize;
    }

    protected int getAboutIconID()
    {
        return R.mipmap.ic_launcher;
    }

    /**
     * Click handler executed when the "About" button is pressed.
     */
    View.OnClickListener onAboutButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            showAbout();
        }
    };

    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
        aboutDialog.setIconID(getAboutIconID());
    }

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

    protected void disableOptionScaleText()
    {
        if (checkbox_scaleText != null)
        {
            checkbox_scaleText.setChecked(false);
            checkbox_scaleText.setEnabled(false);
        }
    }

    protected void disableOptionScaleBase()
    {
        if (checkbox_scaleBase != null)
        {
            checkbox_scaleBase.setChecked(false);
            checkbox_scaleBase.setEnabled(false);
        }
    }

    protected boolean getDefaultScaleText() {
        return WidgetSettings.PREF_DEF_APPEARANCE_SCALETEXT;
    }

    protected boolean getDefaultScaleBase() {
        return WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE;
    }

    protected void hideLayoutSettings()
    {
        View layoutSettings = findViewById(R.id.appwidget_widget_layout);
        if (layoutSettings != null) {
            layoutSettings.setVisibility(View.GONE);
        }
    }

    protected void hideGeneralSettings()
    {
        View generalSettings = findViewById(R.id.appwidget_general_layout);
        if (generalSettings != null) {
            generalSettings.setVisibility(View.GONE);
        }
    }

    protected void hideLocationSettings()
    {
        View settings = findViewById(R.id.appwidget_location_layout);
        if (settings != null) {
            settings.setVisibility(View.GONE);
        }
    }
    
    protected void hideTimeZoneSettings()
    {
        View settings = findViewById(R.id.appwidget_timezone_layout);
        if (settings != null) {
            settings.setVisibility(View.GONE);
        }
    }

    protected void hideAppearanceSettings()
    {
        View settings = findViewById(R.id.appwidget_appearance_layout);
        if (settings != null) {
            settings.setVisibility(View.GONE);
        }
    }

    protected void hideActionSettings()
    {
        View settings = findViewById(R.id.appwidget_action_layout);
        if (settings != null) {
            settings.setVisibility(View.GONE);
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
    
    protected void showTimeMode(boolean showTimeModeUI)
    {
        View timeModeLayout = findViewById(R.id.appwidget_general_timeMode_layout);
        if (timeModeLayout != null)
        {
            timeModeLayout.setVisibility((showTimeModeUI ? View.VISIBLE : View.GONE));
        }
    }

    protected void showTimeFormatMode(boolean show)
    {
        View layout = findViewById(R.id.appwidget_general_timeformatmode_layout);
        if (layout != null) {
            layout.setVisibility((show ? View.VISIBLE : View.GONE));
        }
    }

    protected void showOptionWeeks( boolean showOption )
    {
        View weeksOptionLayout = findViewById(R.id.appwidget_general_showWeeks_layout);
        if (weeksOptionLayout != null)
        {
            weeksOptionLayout.setVisibility((showOption ? View.VISIBLE : View.GONE));
        }
    }

    protected void showOptionHours( boolean showOption )
    {
        View hoursOptionLayout = findViewById(R.id.appwidget_general_showHours_layout);
        if (hoursOptionLayout != null)
        {
            hoursOptionLayout.setVisibility((showOption ? View.VISIBLE : View.GONE));
        }
    }

    protected void showOptionTimeDate( boolean showOption )
    {
        View optionLayout = findViewById(R.id.appwidget_general_showTimeDate_layout);
        if (optionLayout != null)
        {
            optionLayout.setVisibility((showOption ? View.VISIBLE : View.GONE));
        }
    }

    protected void showOptionAbbrvMonth( boolean showOption )
    {
        View optionLayout = findViewById(R.id.appwidget_general_showAbbrMonth_layout);
        if (optionLayout != null) {
            optionLayout.setVisibility((showOption ? View.VISIBLE : View.GONE));
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
     * @param showOption true; show labels option, false hide option
     */
    protected void showOptionLabels(boolean showOption)
    {
        if (checkbox_showLabels != null)
        {
            checkbox_showLabels.setVisibility((showOption) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * @param showOption true; show title option, false hide option
     */
    protected void showOptionTitle(boolean showOption) {
        if (checkbox_showTitle != null) {
            checkbox_showTitle.setVisibility((showOption) ? View.VISIBLE : View.GONE);
        }
        View titleLayout = findViewById(R.id.appwidget_appearance_titleText_layout);
        if (titleLayout != null) {
            titleLayout.setVisibility((showOption) ? View.VISIBLE : View.GONE);
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
     * @param showUI true show option, false hide option
     */
    protected void showOptionRiseSetOrder(boolean showUI)
    {
        View layout_riseSetOrder = findViewById(R.id.appwidget_general_riseSetOrder_layout);
        if (layout_riseSetOrder != null)
        {
            layout_riseSetOrder.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showOptionShowDate(boolean showUI)
    {
        View layout = findViewById(R.id.appwidget_general_showDate_layout);
        if (layout != null) {
            layout.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showCalendarMode(boolean showUI)
    {
        View layout = findViewById(R.id.appwidget_general_calendarMode_layout);
        if (layout != null) {
            layout.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showCalendarFormat(boolean showUI)
    {
        View layout = findViewById(R.id.appwidget_general_calendarFormat_layout);
        if (layout != null) {
            layout.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
        layout = findViewById(R.id.appwidget_general_calendarPattern_layout);
        if (layout != null) {
            layout.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     * @param showUI true show option, false hide option
     */
    protected void showMoreGeneralSettings(boolean showUI) {
        View layout = findViewById(R.id.appwidget_general_moreOptions_layout);
        if (layout != null) {
            layout.setVisibility((showUI ? View.VISIBLE : View.GONE));
        }
    }

    /**
     */
    protected void showOptionLocalizeHemisphere(boolean showUI) {
        View layout = findViewById(R.id.appwidget_general_localize_hemisphere_layout);
        if (layout != null) {
            layout.setVisibility(showUI ? View.VISIBLE : View.GONE);
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
    protected void hideOptionUseAltitude()
    {
        View layout_useAltitude = findViewById(R.id.appwidget_general_useAltitude_layout);
        if (layout_useAltitude != null)
        {
            layout_useAltitude.setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    protected void hideOptionShowSeconds()
    {
        View layout_showSeconds = findViewById(R.id.appwidget_general_showSeconds_layout);
        if (layout_showSeconds != null)
        {
            layout_showSeconds.setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    protected void hideOption1x1LayoutMode()
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_1x1mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(View.GONE);
        }
    }

    protected void showOption2x1LayoutMode(boolean show)
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_2x1mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showOption2x2LayoutMode(boolean show)
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_2x2mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showOption3x1LayoutMode(boolean show)
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_3x1mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     *
     * @param show true show option, false hide option (default hidden)
     */
    protected void showOption3x2LayoutMode(boolean show)
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_3x2mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showOption3x3LayoutMode(boolean show)
    {
        View layout_mode = findViewById(R.id.appwidget_appearance_3x3mode_layout);
        if (layout_mode != null) {
            layout_mode.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showOptionDateOffset(boolean show)
    {
        View layout = findViewById(R.id.appwidget_general_dayOffset_layout);
        if (layout != null) {
            layout.setVisibility(show ? View.VISIBLE : View.GONE);
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

    public void moveSectionToTop(int sectionLayoutID)
    {
        View sectionLayout = findViewById(sectionLayoutID);
        LinearLayout settingsLayout = (LinearLayout)findViewById(R.id.appwidget_settings_layout);
        if (sectionLayout != null && settingsLayout != null)
        {
            settingsLayout.removeView(sectionLayout);
            settingsLayout.addView(sectionLayout, 0);
        }
    }

    public void moveViewToTop(int sectionLayoutID, int viewID) {
        moveViewToPosition(sectionLayoutID, viewID, 1);    // 1; 0 is group title view
    }
    public void moveViewToBeforeOther(int sectionLayoutID, int viewID, int otherViewID)
    {
        int i = getViewPosition(sectionLayoutID, otherViewID);
        if (i >= 0) {
            moveViewToPosition(sectionLayoutID, viewID, Math.max(i - 1, 0));
        }
    }
    public void moveViewToAfterOther(int sectionLayoutID, int viewID, int otherViewID)
    {
        int i = getViewPosition(sectionLayoutID, otherViewID);
        if (i >= 0) {
            moveViewToPosition(sectionLayoutID, viewID, i + 1);
        }
    }
    protected void moveViewToPosition(int sectionLayoutID, int viewID, int position)
    {
        LinearLayout layout = (LinearLayout) findViewById(sectionLayoutID);
        if (layout != null && position >= 0)
        {
            View view = findViewById(viewID);
            if (view != null)
            {
                LinearLayout parent = (LinearLayout) view.getParent();
                parent.removeView(view);
                layout.addView(view, position);
            }
        }
    }
    protected int getViewPosition(int sectionLayoutID, int viewID)
    {
        LinearLayout layout = (LinearLayout)findViewById(sectionLayoutID);
        if (layout != null)
        {
            View view = layout.findViewById(viewID);
            if (view != null) {
                return layout.indexOfChild(view);
            }
        }
        return -1;
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
            case EXPORT_REQUEST:
                onExportResult(resultCode, data);
                break;

            case IMPORT_REQUEST:
                onImportResult(resultCode, data);
                break;

            case LocationConfigDialog.REQUEST_LOCATION:
                onLocationResult(resultCode, data);
                break;

            case PICK_EVENT_REQUEST:
                onPickEventResult(resultCode, data);
                break;

            case PICK_THEME_REQUEST:
                onPickThemeResult(resultCode, data);
                break;
        }
    }

    protected void onExportResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = (data != null ? data.getData() : null);
            if (uri != null) {
                exportSettings(this, uri);
            }
        }
    }

    protected void onImportResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = (data != null ? data.getData() : null);
            if (uri != null) {
                importSettings(this, uri);
            }
        }
    }

    protected void onLocationResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            Location location = data.getParcelableExtra(PlacesActivity.EXTRA_LOCATION);
            if (location != null) {
                locationConfig.loadSettings(SuntimesConfigActivity0.this, LocationConfigView.bundleData(Uri.parse(location.getUri()), location.getLabel(), LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT));
            }
        }
    }

    protected void onPickEventResult(int resultCode, Intent data)
    {
        Context context = SuntimesConfigActivity0.this;
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            String eventID = data.getStringExtra(EventListActivity.SELECTED_EVENTID);
            if (eventID != null) {
                int position = insertEventAliasIntoTimeModeAdapter(context, eventID);
                spinner_timeMode.setSelection(position);
            }
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
            String paramSelection = data.getStringExtra(SuntimesThemeContract.THEME_NAME);
            String themeName = (paramSelection != null) ? paramSelection
                                                        : ((ThemeDescriptor)spinner_theme.getSelectedItem()).name();

            boolean paramReloadAdapter = data.getBooleanExtra(WidgetThemeListActivity.ADAPTER_MODIFIED, false);
            if (paramReloadAdapter)
            {
                //Log.d("selectTheme", "reloading list of themes...");
                initThemeAdapter(this);
            }

            if (themeName != null)
            {
                selectTheme(themeName);
            }
        }
    }

    protected void onThemeSelectionChanged()
    {
        //Log.d("DEBUG", "onThemeSelectionChanged");
        ThemeDescriptor theme = (ThemeDescriptor) spinner_theme.getSelectedItem();

        SuntimesTheme t = WidgetThemes.loadTheme(this, theme.name());
        t.getPaddingPixels(this);
        this.themeValues = t.toContentValues();
        themeViews(themeValues);

        updateWidgetModeAdapter(spinner_1x1mode, themeValues);    // refresh widget previews
        updateWidgetModeAdapter(spinner_2x1mode, themeValues);
        updateWidgetModeAdapter(spinner_3x1mode, themeValues);
        updateWidgetModeAdapter(spinner_3x2mode, themeValues);
        updateWidgetModeAdapter(spinner_3x3mode, themeValues);
        updateTimeModeAdapter(themeValues);
    }

    protected void themeViews(ContentValues themeValues)
    {
        int backgroundColor = themeValues.getAsInteger(SuntimesThemeContract.THEME_BACKGROUND_COLOR);
        String backgroundName = themeValues.getAsString(SuntimesThemeContract.THEME_BACKGROUND);
        SuntimesTheme.ThemeBackground background = null;
        if (backgroundName != null) {
            background = SuntimesTheme.ThemeBackground.valueOf(backgroundName);
        }
        int[] paddingPixels = new int[] {
                themeValues.getAsInteger(SuntimesThemeContract.THEME_PADDING_LEFT + SuntimesThemeContract.THEME_PADDING_PIXELS),
                themeValues.getAsInteger(SuntimesThemeContract.THEME_PADDING_TOP + SuntimesThemeContract.THEME_PADDING_PIXELS),
                themeValues.getAsInteger(SuntimesThemeContract.THEME_PADDING_RIGHT + SuntimesThemeContract.THEME_PADDING_PIXELS),
                themeValues.getAsInteger(SuntimesThemeContract.THEME_PADDING_BOTTOM + SuntimesThemeContract.THEME_PADDING_PIXELS),
        };

        // findViewById(R.id.appwidget_widget_layout),
        View[] groups = new View[] { null };
        //View[] groups = new View[] { findViewById(R.id.root_layout) };
        //View[] groups = new View[] {  findViewById(R.id.appwidget_appearance_layout),
        //        findViewById(R.id.appwidget_general_layout), findViewById(R.id.appwidget_timezone_layout),
        //        findViewById(R.id.appwidget_location_layout), findViewById(R.id.appwidget_action_layout)};   // TODO: all groups
        for (View group : groups)
        {
            if (group != null) {
                if (background != null)
                {
                    if (background.supportsCustomColors())
                        group.setBackgroundColor(backgroundColor);
                    else group.setBackgroundResource(background.getResID());

                    group.setPadding(paddingPixels[0], paddingPixels[1], paddingPixels[2], paddingPixels[3]);
                }
            }
        }
    }

    protected void updateTimeModeAdapter(ContentValues themeValues)
    {
        if (spinner_timeMode != null)
        {
            TimeModeAdapter adapter = (TimeModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null)
            {
                WidgetSettings.RiseSetDataMode selected = (WidgetSettings.RiseSetDataMode) spinner_timeMode.getSelectedItem();
                adapter.setThemeValues(themeValues);
                spinner_timeMode.setAdapter(adapter);
                spinner_timeMode.setSelection(adapter.getPosition(selected));
            }
        }
    }

    private void updateWidgetModeAdapter(@Nullable Spinner spinner, ContentValues themeValues)
    {
        if (spinner != null) {
            try {
                WidgetModeAdapter adapter = (WidgetModeAdapter) spinner.getAdapter();
                if (adapter != null) {
                    WidgetSettings.WidgetModeDisplay selected = (WidgetSettings.WidgetModeDisplay) spinner.getSelectedItem();
                    adapter.setThemeValues(themeValues);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(adapter.getPosition(selected));
                }
            } catch (ClassCastException e) {
                Log.w("updateWidgetMode", "Failed to update adapter! " + e);
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
            //Log.d("selectTheme", "selected theme: " + themeDescriptor.name());

        } else {
            Log.w("selectTheme", "unable to find " + themeDescriptor.name() + " (bad position).");
        }
    }

    protected Intent themeEditorIntent(Context context)
    {
        Intent intent = new Intent(context, WidgetThemeListActivity.class);
        if (spinner_theme != null)
        {
            ThemeDescriptor theme = (ThemeDescriptor) spinner_theme.getSelectedItem();
            if (theme != null)
            {
                intent.putExtra(WidgetThemeListActivity.PARAM_SELECTED, theme.name());
            }
        }
        return intent;
    }

    protected void launchThemeEditor(Context context)
    {
        Intent configThemesIntent = themeEditorIntent(context);
        startActivityForResult(configThemesIntent, PICK_THEME_REQUEST);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widgetconfig, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_about:
                showAbout();
                return true;

            case R.id.action_import:
                importSettings(SuntimesConfigActivity0.this);
                return true;

            case R.id.action_export:
                exportSettings(SuntimesConfigActivity0.this);
                return true;

            case R.id.action_save:
                addWidget();
                return true;

            case R.id.action_reset:
                confirmResetWidget(this);
                return true;

            case android.R.id.home:
                if (reconfigure) {
                    onBackPressed();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    /**
     * HelpDialog (title substitutions)
     */
    private final View.OnClickListener helpDialogListener_substitutions = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url) + getString(R.string.help_substitutions_path))));
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WidgetLayoutAdapter
     */
    public static class WidgetModeAdapter extends ArrayAdapter<WidgetSettings.WidgetModeDisplay>
    {
        protected int resourceID, dropDownResourceID;
        protected WidgetSettings.WidgetModeDisplay[] objects;
        protected WidgetThemePreview preview;
        protected ContentValues themeValues = null;

        public WidgetModeAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            init(context, resource);
        }

        public WidgetModeAdapter(@NonNull Context context, int resource, @NonNull WidgetSettings.WidgetModeDisplay[] objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        public WidgetModeAdapter(@NonNull Context context, int resource, @NonNull List<WidgetSettings.WidgetModeDisplay> objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        private void init(@NonNull Context context, int resource) {
            resourceID = dropDownResourceID = resource;
            preview = new WidgetThemePreview(context, 0);
            preview.setShowTitle(false);
        }

        public void setThemeValues(ContentValues values) {
            themeValues = values;
        }

        @Override
        public void setDropDownViewResource(int resID) {
            super.setDropDownViewResource(resID);
            dropDownResourceID = resID;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, true, dropDownResourceID);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, true, resourceID);
        }

        protected void modifyThemeValues(int position, ContentValues values) {
            /* EMPTY */
        }

        protected View getItemView(int position, View convertView, @NonNull ViewGroup parent, boolean colorize, int resID)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = (convertView == null) ? layoutInflater.inflate(resID, parent, false) : convertView;

            WidgetSettings.WidgetModeDisplay item = getItem(position);
            if (item == null) {
                Log.w("getItemView", "item at position " + position + " is null.");
                return view;
            }

            Object tag = view.getTag();
            if (tag == null || !tag.equals(item.name()))
            {
                TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
                primaryText.setText(item.toString());

                LinearLayout previewArea = (LinearLayout) view.findViewById(R.id.preview_area);
                if (previewArea != null && colorize)
                {
                    previewArea.removeAllViewsInLayout();
                    View previewView = layoutInflater.inflate(item.getLayoutID(), previewArea, true);
                    if (themeValues != null) {
                        modifyThemeValues(position, themeValues);
                        preview.updatePreview(item.getLayoutID(), previewView, themeValues);
                    }
                }
                view.setTag(item.name());
            }
            return view;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * TimeModeAdapter
     */
    public static class TimeModeAdapter extends ModeAdapterBase<WidgetSettings.RiseSetDataMode>
    {
        private final SuntimesUtils utils = new SuntimesUtils();
        private WeakReference<Context> contextRef;

        public TimeModeAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            contextRef = new WeakReference<>(context);
        }
        public TimeModeAdapter(@NonNull Context context, int resource, @NonNull WidgetSettings.RiseSetDataMode[] objects) {
            super(context, resource, objects);
            contextRef = new WeakReference<>(context);
        }
        public TimeModeAdapter(@NonNull Context context, int resource, @NonNull List<WidgetSettings.RiseSetDataMode> objects) {
            super(context, resource, objects);
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected String getNameForMode(WidgetSettings.RiseSetDataMode mode) {
            return mode.name();
        }

        @Override
        protected String getSummaryForMode(WidgetSettings.RiseSetDataMode item)
        {
            Context context = contextRef.get();
            WidgetSettings.TimeMode timeMode = item.getTimeMode();
            if (timeMode != null)
            {
                Double angle = timeMode.angle();
                return angle != null ? utils.formatAsDegrees(angle, 1) : null;

            } else if (context != null && EventSettings.hasEvent(context, item.name())) {
                String eventID = EventSettings.getEventUriLastPathSegment(context, item.name());
                AlarmEventProvider.SunElevationEvent event = AlarmEventProvider.SunElevationEvent.valueOf(eventID);
                return ((event != null) ? utils.formatAsDegrees(event.getAngle(), 1) : null);

            } else return null;
        }

        @Override
        protected int getColorForMode(WidgetSettings.RiseSetDataMode item)
        {
            if (themeValues == null || item == null) {
                return Color.TRANSPARENT;
            }
            if (item.getTimeMode() == null) {
                return EventSettings.getColor(contextRef.get(), item.name());
            }
            switch (item.getTimeMode())
            {
                case ASTRONOMICAL: return themeValues.getAsInteger(SuntimesThemeContract.THEME_ASTROCOLOR);
                case NAUTICAL: case BLUE8: return themeValues.getAsInteger(SuntimesThemeContract.THEME_NAUTICALCOLOR);
                case CIVIL: case BLUE4: return themeValues.getAsInteger(SuntimesThemeContract.THEME_CIVILCOLOR);
                case GOLD: return themeValues.getAsInteger(SuntimesThemeContract.THEME_SUNRISECOLOR);
                case NOON: return themeValues.getAsInteger(SuntimesThemeContract.THEME_SUNSETCOLOR);
                case MIDNIGHT: return themeValues.getAsInteger(SuntimesThemeContract.THEME_NIGHTCOLOR);
                case OFFICIAL: default: return themeValues.getAsInteger(SuntimesThemeContract.THEME_DAYCOLOR);
            }
        }
    }

    public static class ModeAdapterBase<T> extends ArrayAdapter<T>
    {
        protected int resourceID, dropDownResourceID;
        protected ContentValues themeValues = null;

        public ModeAdapterBase(@NonNull Context context, int resource) {
            super(context, resource);
            init(context, resource);
        }

        public ModeAdapterBase(@NonNull Context context, int resource, @NonNull T[] objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        public ModeAdapterBase(@NonNull Context context, int resource, @NonNull List<T> objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        private void init(@NonNull Context context, int resource) {
            resourceID = dropDownResourceID = resource;
        }

        public void setThemeValues(ContentValues values) {
            themeValues = values;
        }

        @Override
        public void setDropDownViewResource(int resID) {
            super.setDropDownViewResource(resID);
            dropDownResourceID = resID;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, dropDownResourceID);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, resourceID);
        }

        private View getItemView(int position, View convertView, @NonNull ViewGroup parent, int resID)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = (convertView == null) ? layoutInflater.inflate(resID, parent, false) : convertView;

            T item = getItem(position);
            if (item == null) {
                Log.w("getItemView", "item at position " + position + " is null.");
                return view;
            }

            Object tag = view.getTag();
            if (tag == null || !tag.equals(getNameForMode(item)))
            {
                TextView primaryText = (TextView)view.findViewById(android.R.id.text1);
                primaryText.setText(getLabelForMode(item));

                TextView text2 = (TextView)view.findViewById(android.R.id.text2);
                if (text2 != null)
                {
                    String summary = getSummaryForMode(item);
                    text2.setText(summary);
                    text2.setVisibility(summary != null ? View.VISIBLE : View.GONE);
                }

                View colorTab = view.findViewById(android.R.id.icon1);
                colorTab = (colorTab != null ? colorTab : view.findViewById(R.id.icon1));
                if (colorTab != null && themeValues != null) {
                    colorTab.setBackgroundColor(getColorForMode(item));
                }
                view.setTag(getNameForMode(item));
            }
            return view;
        }

        protected String getNameForMode(T mode) {
            return null;
        }

        protected String getSummaryForMode(T mode) {
            return null;
        }

        protected String getLabelForMode(T mode) {
            return mode.toString();
        }

        protected int getColorForMode(T mode) {
            return Color.WHITE;
        }

    }
    
}
