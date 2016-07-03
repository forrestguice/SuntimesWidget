/**
    Copyright (C) 2014 Forrest Guice
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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;

import java.util.TimeZone;

/**
 * Main widget config activity.
 */
public class SuntimesConfigActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_ABOUT = "about";
    private static final String DIALOGTAG_HELP = "help";

    protected int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean reconfigure = false;

    private Spinner spinner_calculatorMode;
    private Spinner spinner_timeMode;
    private Spinner spinner_compareMode;

    protected Spinner spinner_onTap;
    protected EditText text_launchActivity;

    protected Spinner spinner_1x1mode;
    private Spinner spinner_theme;
    private CheckBox checkbox_showTitle;

    private TextView label_titleText;
    private EditText text_titleText;

    private LocationConfigView locationConfig;

    private Spinner spinner_timezoneMode;
    private TextView label_timezone;
    private Spinner spinner_timezone;
    private String customTimezoneID;

    public SuntimesConfigActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        GetFixUI.themeIcons(this);

        super.onCreate(icicle);
        setResult(RESULT_CANCELED);  // causes widget host to cancel if user presses back
        setContentView(R.layout.layout_settings);

        Context context = SuntimesConfigActivity.this;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            appWidgetId = extras.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID,
                                         AppWidgetManager.INVALID_APPWIDGET_ID );
            reconfigure = extras.getBoolean(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), false);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            Log.w("CONFIG", "Invalid widget ID! returning early.");
            finish();
            return;
        }

        WidgetThemes.initThemes(context);

        initDisplayStrings(context);
        initViews(context);
        loadSettings(context);
    }

    @Override
    public void onDestroy()
    {
        locationConfig.cancelGetFix();
        super.onDestroy();
    }

    protected void initDisplayStrings( Context context )
    {
        WidgetSettings.initDisplayStrings(context);
    }

    /**
     * Save settings (as represented by the state of the config UI).
     * @param context the android application context
     */
    protected void saveSettings( Context context )
    {
        saveGeneralSettings(context);
        locationConfig.saveSettings(context);
        saveTimezoneSettings(context);
        saveAppearanceSettings(context);
        saveActionSettings(context);
    }

    /**
     * Load settings (update the state of the config UI).
     * @param context
     */
    protected void loadSettings( Context context )
    {
        loadGeneralSettings(context);
        loadAppearanceSettings(context);
        locationConfig.loadSettings(context);
        loadTimezoneSettings(context);
        loadActionSettings(context);
    }


    protected ArrayAdapter<WidgetSettings.ActionMode> createAdapter_actionMode()
    {
        ArrayAdapter<WidgetSettings.ActionMode> adapter = new ArrayAdapter<WidgetSettings.ActionMode>(this, R.layout.layout_listitem_oneline, supportedActionModes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
    protected WidgetSettings.ActionMode[] supportedActionModes()
    {
        WidgetSettings.ActionMode[] allModes = WidgetSettings.ActionMode.values();
        WidgetSettings.ActionMode[] supportedModes = new WidgetSettings.ActionMode[allModes.length - 1];
        for (int i=0; i<supportedModes.length; i++)
        {
            supportedModes[i] = allModes[i];
        }
        return supportedModes;
    }


    protected void initViews( Context context )
    {
        //
        // widget: onTap
        //
        spinner_onTap = (Spinner)findViewById(R.id.appwidget_action_onTap);
        spinner_onTap.setAdapter(createAdapter_actionMode());
        spinner_onTap.setOnItemSelectedListener(onActionModeListener);

        //
        // widget: onTap launchActivity
        //
        text_launchActivity = (EditText)findViewById(R.id.appwidget_action_launch);

        ImageButton button_launchAppHelp = (ImageButton)findViewById(R.id.appwidget_action_launch_helpButton);
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

        //
        // widget: theme
        //
        ArrayAdapter<ThemeDescriptor> spinner_themeAdapter;
        spinner_themeAdapter = new ArrayAdapter<ThemeDescriptor>(this, R.layout.layout_listitem_oneline, WidgetThemes.values());
        spinner_themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_theme = (Spinner)findViewById(R.id.appwidget_appearance_theme);
        spinner_theme.setAdapter(spinner_themeAdapter);

        //
        // widget: source
        //
        ArrayAdapter<SuntimesCalculatorDescriptor> spinner_calculatorModeAdapter;
        spinner_calculatorModeAdapter = new ArrayAdapter<SuntimesCalculatorDescriptor>(this, R.layout.layout_listitem_oneline, SuntimesCalculatorDescriptor.values());
        spinner_calculatorModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_calculatorMode = (Spinner)findViewById(R.id.appwidget_general_calculator);
        spinner_calculatorMode.setAdapter(spinner_calculatorModeAdapter);

        //
        // widget: time mode
        //
        ArrayAdapter<WidgetSettings.TimeMode> spinner_timeModeAdapter;
        spinner_timeModeAdapter = new ArrayAdapter<WidgetSettings.TimeMode>(this,
                R.layout.layout_listitem_oneline,
                WidgetSettings.TimeMode.values());
        spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timeMode = (Spinner)findViewById(R.id.appwidget_general_timeMode);
        spinner_timeMode.setAdapter(spinner_timeModeAdapter);

        ImageButton button_timeModeHelp = (ImageButton)findViewById(R.id.appwidget_generale_timeMode_helpButton);
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

        //
        // widget: timezone mode
        //
        ArrayAdapter<WidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<WidgetSettings.TimezoneMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner)findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
        spinner_timezoneMode.setOnItemSelectedListener(onTimezoneModeListener);

        //
        // widget: timezone
        //
        label_timezone = (TextView)findViewById(R.id.appwidget_timezone_custom_label);

        WidgetTimezones.TimeZoneItemAdapter spinner_timezoneAdapter;
        spinner_timezoneAdapter = new WidgetTimezones.TimeZoneItemAdapter(this,
                R.layout.layout_listitem_twoline, WidgetTimezones.getValues() );

        spinner_timezone = (Spinner)findViewById(R.id.appwidget_timezone_custom);
        spinner_timezone.setAdapter(spinner_timezoneAdapter);

        //
        // widget: location
        //
        locationConfig = (LocationConfigView)findViewById(R.id.appwidget_location_config);
        locationConfig.setAutoAllowed(false);
        locationConfig.init(this, false);
        locationConfig.setAppWidgetId(this.appWidgetId);

        //
        // widget: 1x1 widget mode
        //
        ArrayAdapter<WidgetSettings.WidgetMode1x1> spinner_1x1ModeAdapter;
        spinner_1x1ModeAdapter = new ArrayAdapter<WidgetSettings.WidgetMode1x1>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetMode1x1.values());
        spinner_1x1ModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_1x1mode = (Spinner)findViewById(R.id.appwidget_appearance_1x1mode);
        spinner_1x1mode.setAdapter(spinner_1x1ModeAdapter);

        //
        // widget: title text
        //
        label_titleText = (TextView)findViewById(R.id.appwidget_appearance_titleText_label);
        text_titleText = (EditText)findViewById(R.id.appwidget_appearance_titleText);

        ImageButton button_titleText = (ImageButton)findViewById(R.id.appwidget_appearance_titleText_helpButton);
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

        //
        // widget: show title
        //
        checkbox_showTitle = (CheckBox)findViewById(R.id.appwidget_appearance_showTitle);
        checkbox_showTitle.setOnCheckedChangeListener(onShowTitleListener);

        //
        // widget: compare mode
        //
        ArrayAdapter<WidgetSettings.CompareMode> spinner_compareModeAdapter;
        spinner_compareModeAdapter = new ArrayAdapter<WidgetSettings.CompareMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.CompareMode.values());
        spinner_compareModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_compareMode = (Spinner)findViewById(R.id.appwidget_general_compareMode);
        spinner_compareMode.setAdapter(spinner_compareModeAdapter);

        //
        // widget: add button
        //
        Button button_addWidget = (Button)findViewById(R.id.add_button);
        button_addWidget.setOnClickListener(onAddButtonClickListener);

        if (reconfigure)
        {
            button_addWidget.setText(getString(R.string.configAction_reconfigWidget_short));

            TextView activityTitle = (TextView) findViewById(R.id.activity_title);
            activityTitle.setText(getString(R.string.configAction_reconfigWidget));
        }

        //
        // widget: about button
        //
        Button button_aboutWidget = (Button)findViewById(R.id.about_button);
        button_aboutWidget.setOnClickListener(onAboutButtonClickListener);
    }

    private void setTitleTextEnabled( boolean value )
    {
        label_titleText.setEnabled(value);
        text_titleText.setEnabled(value);
    }

    private void setCustomTimezoneEnabled( boolean value )
    {
        String timezoneID = (value ? customTimezoneID : TimeZone.getDefault().getID());
        spinner_timezone.setSelection(WidgetTimezones.ordinal(timezoneID), true);

        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
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
            WidgetSettings.TimezoneMode timezoneMode = timezoneModes[ parent.getSelectedItemPosition() ];
            setCustomTimezoneEnabled( (timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE) );
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
            WidgetSettings.ActionMode actionMode = actionModes[ parent.getSelectedItemPosition() ];

            switch (actionMode)
            {
                case ONTAP_LAUNCH_ACTIVITY:
                    findViewById(R.id.applayout_action_launch).setVisibility(View.VISIBLE);
                    break;

                case ONTAP_DONOTHING:
                default:
                    findViewById(R.id.applayout_action_launch).setVisibility(View.GONE);
                    break;
            }
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };

    /**
     * Save UI state to settings (appearance group).
     * @param context the android application context
     */
    protected void saveAppearanceSettings(Context context)
    {
        // save: widgetmode_1x1
        final WidgetSettings.WidgetMode1x1[] modes = WidgetSettings.WidgetMode1x1.values();
        WidgetSettings.WidgetMode1x1 mode = modes[ spinner_1x1mode.getSelectedItemPosition() ];
        WidgetSettings.save1x1ModePref(context, appWidgetId, mode);
        Log.d("DEBUG", "Saved mode: " + mode.name());

        // save: theme
        final ThemeDescriptor[] themes = WidgetThemes.values();
        ThemeDescriptor theme = themes[ spinner_theme.getSelectedItemPosition() ];
        WidgetSettings.saveThemePref(context, appWidgetId, theme.name());
        Log.d("DEBUG", "Saved theme: " + theme.name());

        // save: show title
        boolean showTitle = checkbox_showTitle.isChecked();
        WidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

        // save:: title text
        String titleText = text_titleText.getText().toString().trim();
        WidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);
    }

    /**
     * Load settings into UI state (appearance group).
     * @param context the android application context
     */
    protected void loadAppearanceSettings(Context context)
    {
        // load: widgetmode_1x1
        WidgetSettings.WidgetMode1x1 mode1x1 = WidgetSettings.load1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());

        // load: theme
        SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetId);
        ThemeDescriptor themeDescriptor = WidgetThemes.valueOf(theme.themeName());
        spinner_theme.setSelection(themeDescriptor.ordinal(WidgetThemes.values()) );

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
     * @param context the android application context
     */
    protected void saveGeneralSettings(Context context)
    {
        // save: calculator mode
        final SuntimesCalculatorDescriptor[] calculators = SuntimesCalculatorDescriptor.values();
        SuntimesCalculatorDescriptor calculator = calculators[ spinner_calculatorMode.getSelectedItemPosition() ];
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, calculator);

        // save: time mode
        final WidgetSettings.TimeMode[] timeModes = WidgetSettings.TimeMode.values();
        WidgetSettings.TimeMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition()];
        WidgetSettings.saveTimeModePref(context, appWidgetId, timeMode);

        // save: compare mode
        final WidgetSettings.CompareMode[] compareModes = WidgetSettings.CompareMode.values();
        WidgetSettings.CompareMode compareMode = compareModes[ spinner_compareMode.getSelectedItemPosition()];
        WidgetSettings.saveCompareModePref(context, appWidgetId, compareMode);
    }

    /**
     * Load settings into UI state (general group).
     * @param context the android application context
     */
    protected void loadGeneralSettings(Context context)
    {
        // load: calculator mode
        SuntimesCalculatorDescriptor calculatorMode = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        spinner_calculatorMode.setSelection(calculatorMode.ordinal());

        // load: time mode
        WidgetSettings.CompareMode compareMode = WidgetSettings.loadCompareModePref(context, appWidgetId);
        spinner_compareMode.setSelection(compareMode.ordinal());

        // load: compare mode
        WidgetSettings.TimeMode timeMode = WidgetSettings.loadTimeModePref(context, appWidgetId);
        spinner_timeMode.setSelection(timeMode.ordinal());
    }

    /**
     * Save UI state to settings (timezone group).
     * @param context the android application context
     */
    protected void saveTimezoneSettings(Context context)
    {
        // save: timezone mode
        final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[ spinner_timezoneMode.getSelectedItemPosition() ];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem)spinner_timezone.getSelectedItem();
        WidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());
    }

    /**
     * Load settings into UI state (timezone group).
     * @param context the android application context
     */
    protected void loadTimezoneSettings(Context context)
    {
        WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        customTimezoneID = WidgetSettings.loadTimezonePref(context, appWidgetId);
        int timezonePos = WidgetTimezones.ordinal(customTimezoneID);
        int numTimeZones = WidgetTimezones.values().length;

        if (timezonePos >= 0 && timezonePos < numTimeZones)
        {
            spinner_timezone.setSelection(timezonePos);
        } else {
            spinner_timezone.setSelection(0);
            Log.w("loadTimezoneSettings", "unable to find timezone " + customTimezoneID + " in the list! Setting selection to 0." );
        }
    }

    /**
     * Save UI state to settings (action group).
     * @param context the android application context
     */
    protected void saveActionSettings(Context context)
    {
        // save: action mode
        WidgetSettings.ActionMode actionMode = (WidgetSettings.ActionMode)spinner_onTap.getSelectedItem();
        WidgetSettings.saveActionModePref(context, appWidgetId, actionMode);

        // save: launch activity
        String launchString = text_launchActivity.getText().toString();
        WidgetSettings.saveActionLaunchPref(context, appWidgetId, launchString);
    }

    /**
     * Load settings into UI state (action group).
     * @param context the android application context
     */
    protected void loadActionSettings(Context context)
    {
        // load: action mode
        WidgetSettings.ActionMode actionMode = WidgetSettings.loadActionModePref(context, appWidgetId);
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
        final Context context = SuntimesConfigActivity.this;
        saveSettings(context);
        updateWidget(context);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    protected void updateWidget( Context context )
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesLayout layout = WidgetSettings.load1x1ModePref_asLayout(context, appWidgetId);
        SuntimesWidget.updateAppWidget(context, appWidgetManager, appWidgetId, layout);
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
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        locationConfig.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
