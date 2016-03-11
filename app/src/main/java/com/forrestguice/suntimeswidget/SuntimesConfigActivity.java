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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;

import java.util.TimeZone;

/**
 * Main widget config activity.
 */
public class SuntimesConfigActivity extends Activity
{
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

    private Spinner spinner_locationMode;

    private TextView label_locationLon;
    private EditText text_locationLon;

    private TextView label_locationLat;
    private EditText text_locationLat;

    private TextView label_locationName;
    private EditText text_locationName;
    private ImageButton button_getfix;
    private ProgressBar progress_getfix;

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
        cancelGetFix();

        if (gpsPrompt != null)
        {
            gpsPrompt.dismiss();
        }

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
        saveLocationSettings(context);
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
        loadLocationSettings(context);
        loadTimezoneSettings(context);
        loadActionSettings(context);
    }


    protected ArrayAdapter<WidgetSettings.ActionMode> createAdapter_actionMode()
    {
        ArrayAdapter<WidgetSettings.ActionMode> adapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, supportedActionModes());
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
                SettingsHelpDialog helpDialog = new SettingsHelpDialog(SuntimesConfigActivity.this);
                String helpContent = getString(R.string.help_action_launch);
                helpDialog.onPrepareDialog(helpContent);
                helpDialog.show();
            }
        });

        //
        // widget: theme
        //
        ArrayAdapter<ThemeDescriptor> spinner_themeAdapter;
        spinner_themeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, WidgetThemes.values());
        spinner_themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_theme = (Spinner)findViewById(R.id.appwidget_appearance_theme);
        spinner_theme.setAdapter(spinner_themeAdapter);

        //
        // widget: source
        //
        ArrayAdapter<SuntimesCalculatorDescriptor> spinner_calculatorModeAdapter;
        spinner_calculatorModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesCalculatorDescriptor.values());
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
                SettingsHelpDialog helpDialog = new SettingsHelpDialog(SuntimesConfigActivity.this);
                String helpContent = getString(R.string.help_general_timeMode);
                helpDialog.onPrepareDialog(helpContent);
                helpDialog.show();
            }
        });

        //
        // widget: timezone mode
        //
        ArrayAdapter<WidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, WidgetSettings.TimezoneMode.values());
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

        spinner_timezone = (Spinner)findViewById(R.id.app_widget_timezone_custom);
        spinner_timezone.setAdapter(spinner_timezoneAdapter);

        //
        // widget: location mode
        //
        ArrayAdapter<WidgetSettings.LocationMode> spinner_locationModeAdapter;
        spinner_locationModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, WidgetSettings.LocationMode.values());
        spinner_locationModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_locationMode = (Spinner)findViewById(R.id.appwidget_location_mode);
        spinner_locationMode.setAdapter(spinner_locationModeAdapter);
        spinner_locationMode.setOnItemSelectedListener(onLocationModeListener);

        //
        // widget: custom lon / lat
        //
        label_locationLon = (TextView)findViewById(R.id.appwidget_location_lon_label);
        text_locationLon = (EditText)findViewById(R.id.appwidget_location_lon);

        label_locationLat = (TextView)findViewById(R.id.appwidget_location_lat_label);
        text_locationLat = (EditText)findViewById(R.id.appwidget_location_lat);

        label_locationName = (TextView)findViewById(R.id.appwidget_location_name_label);
        text_locationName = (EditText)findViewById(R.id.appwidget_location_name);

        progress_getfix = (ProgressBar)findViewById(R.id.appwidget_location_getfixprogress);
        progress_getfix.setVisibility(View.GONE);

        button_getfix = (ImageButton)findViewById(R.id.appwidget_location_getfix);
        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFix();
            }
        });

        if (!isGPSEnabled())
        {
            button_getfix.setImageResource(GetFixTask.ICON_DISABLED);
        }

        //
        // widget: 1x1 widget mode
        //
        ArrayAdapter<WidgetSettings.WidgetMode1x1> spinner_1x1ModeAdapter;
        spinner_1x1ModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, WidgetSettings.WidgetMode1x1.values());
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
                SettingsHelpDialog helpDialog = new SettingsHelpDialog(SuntimesConfigActivity.this);
                String helpContent = getString(R.string.help_appearance_title);
                helpDialog.onPrepareDialog(helpContent);
                helpDialog.show();
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
        spinner_compareModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, WidgetSettings.CompareMode.values());
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
            button_addWidget.setText(getString(R.string.configAction_reconfigWidget));
        }

        //
        // widget: about button
        //
        Button button_aboutWidget = (Button)findViewById(R.id.about_button);
        button_aboutWidget.setOnClickListener(onAboutButtonClickListener);
    }


    /**private Location getLocationFromAddress(String address, Context context)
    {
        Geocoder geocoder = new Geocoder(context);
        List<Address> locations;
        try
        {
            locations = geocoder.getFromLocationName(address, 1);

        } catch (IOException e) {
            locations = new ArrayList<Address>();
        }

        Location location = null;
        if (!locations.isEmpty())
        {
            double lat = locations.get(0).getLatitude();
            double lon = locations.get(0).getLongitude();
            location = new Location(lat, lon);
        } else {

        }

        return location;
    }*/


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

    private void setCustomLocationEnabled( boolean value )
    {
        label_locationLon.setEnabled(value);
        text_locationLon.setEnabled(value);

        label_locationLat.setEnabled(value);
        text_locationLat.setEnabled(value);

        label_locationName.setEnabled(value);
        text_locationName.setEnabled(value);
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
     * OnItemSelected (Location Mode)
     */
    Spinner.OnItemSelectedListener onLocationModeListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
            WidgetSettings.LocationMode locationMode = locationModes[ parent.getSelectedItemPosition() ];
            setCustomLocationEnabled( (locationMode == WidgetSettings.LocationMode.CUSTOM_LOCATION) );
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
        spinner_theme.setSelection( themeDescriptor.ordinal(WidgetThemes.values()) );

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
        WidgetSettings.TimeMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition() ];
        WidgetSettings.saveTimeModePref(context, appWidgetId, timeMode);

        // save: compare mode
        final WidgetSettings.CompareMode[] compareModes = WidgetSettings.CompareMode.values();
        WidgetSettings.CompareMode compareMode = compareModes[ spinner_compareMode.getSelectedItemPosition() ];
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
     * Save UI state to settings (location group).
     * @param context the android application context
     */
    protected void saveLocationSettings(Context context)
    {
        // save: location mode
        final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
        WidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];
        WidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

        // save: lat / lon
        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();
        String name = text_locationName.getText().toString();
        WidgetSettings.Location location = new WidgetSettings.Location(name, latitude, longitude);
        WidgetSettings.saveLocationPref(context, appWidgetId, location);
    }

    /**
     * Load settings into UI state (location group).
     * @param context the android application context
     */
    protected void loadLocationSettings(Context context)
    {
        WidgetSettings.LocationMode locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);
        spinner_locationMode.setSelection(locationMode.ordinal());

        WidgetSettings.Location location = WidgetSettings.loadLocationPref(context, appWidgetId);
        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
        text_locationName.setText(location.getLabel());
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
            SettingsAboutDialog aboutDialog = new SettingsAboutDialog(SuntimesConfigActivity.this);
            aboutDialog.onPrepareDialog();
            aboutDialog.show();
        }
    };

    /**
     * SettingsAboutDialog : Dialog
     */
    public class SettingsAboutDialog extends Dialog
    {
        private Activity myParent;

        public SettingsAboutDialog( Activity c )
        {
            super(c);
            myParent = c;
            setContentView(R.layout.layout_dialog_about);
            setCancelable(true);
        }

        public void onPrepareDialog()
        {
            setTitle(myParent.getString(R.string.about_dialog_title));

            TextView urlView = (TextView)findViewById(R.id.txt_about_url);
            urlView.setMovementMethod(LinkMovementMethod.getInstance());
            urlView.setText(Html.fromHtml(myParent.getString(R.string.app_url)));

            TextView supportView = (TextView)findViewById(R.id.txt_about_support);
            supportView.setMovementMethod(LinkMovementMethod.getInstance());
            supportView.setText(Html.fromHtml(myParent.getString(R.string.app_support_url)));

            TextView legalView = (TextView)findViewById(R.id.txt_about_legal);
            legalView.setMovementMethod(LinkMovementMethod.getInstance());
            legalView.setText(Html.fromHtml(myParent.getString(R.string.app_legal)));
        }
    }

    /**
     * SettingsHelpDialog : Dialog
     */
    public class SettingsHelpDialog extends Dialog
    {
        private Activity myParent;

        public SettingsHelpDialog( Activity c )
        {
            super(c);
            myParent = c;
            setContentView(R.layout.layout_dialog_help);
            setTitle(myParent.getString(R.string.help_dialog_title));
            setCancelable(true);
        }

        public void onPrepareDialog(String content)
        {
            TextView txt = (TextView)findViewById(R.id.txt_help_content);
            txt.setText(Html.fromHtml(content));
        }
    }

    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////

    private boolean gettingFix = false;
    private GetFixTask getFixTask = null;

    /**
     * Get a fix; main entry point for GPS "get fix" button in location settings.
     * Spins up a GetFixTask; allows only one such task to execute at a time.
     */
    public void getFix()
    {
        if (!gettingFix)
        {
            if (isGPSEnabled())
            {
                getFixTask = new GetFixTask();
                getFixTask.execute();

            } else {
                showGPSEnabledPrompt();
            }
        }
    }

    public boolean isGPSEnabled()
    {
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private AlertDialog gpsPrompt = null;
    private void showGPSEnabledPrompt()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps_dialog_msg))
               .setCancelable(false)
               .setPositiveButton(getString(R.string.gps_dialog_ok), new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                   {
                       startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                       gpsPrompt = null;
                   }
               })
               .setNegativeButton(getString(R.string.gps_dialog_cancel), new DialogInterface.OnClickListener()
               {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                   {
                       dialog.cancel();
                       gpsPrompt = null;
                   }
               });

        gpsPrompt = builder.create();
        gpsPrompt.show();
    }

    /**
     * Cancel acquiring a location fix (cancels running task(s)).
     */
    public void cancelGetFix()
    {
        if (gettingFix && getFixTask != null)
        {
            getFixTask.cancel(true);
        }
    }

    /**
     * An AsyncTask that registers a LocationListener, starts listening for
     * gps updates, and then waits a predetermined amount of time for a
     * good location fix to be acquired; updates progress in settings activity.
     */
    public class GetFixTask extends AsyncTask<String, Location, Location>
    {
        private static final int ICON_DISABLED = R.drawable.ic_action_location_off;
        private static final int ICON_SEARCHING = R.drawable.ic_action_location_searching;
        private static final int ICON_FOUND = R.drawable.ic_action_location_found;

        private static final int MIN_ELAPSED = 1000 * 5;        // wait at least 5s before settling on a fix
        private static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
        private static final int MAX_AGE = 1000 * 60 * 5;       // consider fixes over 5min be "too old"

        private long startTime, stopTime, elapsedTime;
        private Location bestFix, lastFix;
        private LocationManager locationManager;
        private LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                lastFix = location;
                if (isBetterFix(lastFix, bestFix))
                {
                    bestFix = lastFix;
                    onProgressUpdate(bestFix);
                }
            }

            private boolean isBetterFix(Location location, Location location2)
            {
                if (location2 == null)
                {
                    return true;

                } else if (location != null) {
                    if ((location.getTime() - location2.getTime()) > MAX_AGE)
                    {
                        return true;  // more than 5min since last fix; assume the latest fix is better

                    } else if (location.getAccuracy() < location2.getAccuracy()) {
                        return true;  // accuracy is a measure of radius of certainty; smaller values are more accurate
                    }
                }
                return false;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        @Override
        protected void onPreExecute()
        {
            button_getfix.setVisibility(View.GONE);
            progress_getfix.setVisibility(View.VISIBLE);
            enableLocationUI(false);

            bestFix = null;
            gettingFix = true;
            elapsedTime = 0;
            startTime = stopTime = System.currentTimeMillis();
        }

        private void enableLocationUI(boolean value)
        {
            text_locationName.requestFocus();
            text_locationLat.setEnabled(value);
            text_locationLon.setEnabled(value);
            text_locationName.setEnabled(value);
        }


        @Override
        protected Location doInBackground(String... params)
        {
            locationManager = (LocationManager)SuntimesConfigActivity.this.getSystemService(Context.LOCATION_SERVICE);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                public void run()
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            });


            while (elapsedTime < MAX_ELAPSED && !isCancelled())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                stopTime = System.currentTimeMillis();
                elapsedTime = stopTime - startTime;

                if (bestFix != null && elapsedTime > MIN_ELAPSED)
                {
                    break;
                }
            }
            return bestFix;
        }

        @Override
        protected void onProgressUpdate(Location... locations)
        {
            text_locationLat.setText(locations[0].getLatitude() + "");
            text_locationLon.setText(locations[0].getLongitude() + "");
        }

        @Override
        protected void onPostExecute(Location result)
        {
            locationManager.removeUpdates(locationListener);
            gettingFix = false;

            progress_getfix.setVisibility(View.GONE);
            enableLocationUI(true);

            button_getfix.setImageResource((result == null) ? ICON_SEARCHING : ICON_FOUND);
            button_getfix.setVisibility(View.VISIBLE);
            button_getfix.setEnabled(true);
        }

        @Override
        protected void onCancelled(Location result)
        {
            locationManager.removeUpdates(locationListener);
            gettingFix = false;

            progress_getfix.setVisibility(View.GONE);
            enableLocationUI(true);

            button_getfix.setImageResource( (result == null) ? ICON_SEARCHING : ICON_FOUND );
            button_getfix.setVisibility(View.VISIBLE);
            button_getfix.setEnabled(true);
        }
    }
}
