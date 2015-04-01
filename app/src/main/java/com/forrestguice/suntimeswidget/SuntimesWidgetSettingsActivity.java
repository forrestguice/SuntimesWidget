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
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetSettings;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetTheme;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetThemes;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetTimeZones;

import java.util.TimeZone;


/**
 * The configuration screen for the {@link SuntimesWidget SuntimesWidget} AppWidget.
 */
public class SuntimesWidgetSettingsActivity extends Activity
{
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_calculatorMode;
    private Spinner spinner_timeMode;
    private Spinner spinner_compareMode;

    private Spinner spinner_1x1mode;
    private Spinner spinner_theme;
    private CheckBox checkbox_showTitle;
    private EditText text_titleText;
    private TextView label_titleText;

    private Spinner spinner_locationMode;
    private EditText text_locationLon;
    private EditText text_locationLat;
    private TextView label_locationLon;
    private TextView label_locationLat;

    private Spinner spinner_timezoneMode;
    private Spinner spinner_timezone;
    private TextView label_timezone;
    private String customTimezoneID;

    public SuntimesWidgetSettingsActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);  // causes widget host to cancel if user presses back
        setContentView(R.layout.layout_settings);

        Context context = SuntimesWidgetSettingsActivity.this;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            appWidgetId = extras.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID,
                                         AppWidgetManager.INVALID_APPWIDGET_ID );
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
            return;
        }

        SuntimesWidgetThemes.initThemes(context);
        SuntimesWidgetSettingsActivity.initDisplayStrings(context);

        initViews(context);

        loadGeneralSettings(context);
        loadAppearanceSettings(context);
        loadLocationSettings(context);
        loadTimezoneSettings(context);
    }

    private static void initDisplayStrings( Context context )
    {
        SuntimesWidgetSettings.WidgetMode1x1.initDisplayStrings(context);
        SuntimesWidgetSettings.CompareMode.initDisplayStrings(context);
        SuntimesWidgetSettings.TimeMode.initDisplayStrings(context);
        SuntimesWidgetSettings.LocationMode.initDisplayStrings(context);
        SuntimesWidgetSettings.TimezoneMode.initDisplayStrings(context);
    }

    private void initViews( Context context )
    {
        //
        // widget: source
        //
        ArrayAdapter<SuntimesWidgetThemes.ThemeDescriptor> spinner_themeAdapter;
        spinner_themeAdapter = new ArrayAdapter<>(this,
                R.layout.layout_listitem_oneline,
                SuntimesWidgetThemes.values());
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
        ArrayAdapter<SuntimesWidgetSettings.TimeMode> spinner_timeModeAdapter;
        spinner_timeModeAdapter = new ArrayAdapter<SuntimesWidgetSettings.TimeMode>(this,
                R.layout.layout_listitem_oneline,
                SuntimesWidgetSettings.TimeMode.values());
        spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timeMode = (Spinner)findViewById(R.id.appwidget_general_timeMode);
        spinner_timeMode.setAdapter(spinner_timeModeAdapter);

        ImageButton button_timeModeHelp = (ImageButton)findViewById(R.id.appwidget_generale_timeMode_helpButton);
        button_timeModeHelp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SettingsHelpDialog helpDialog = new SettingsHelpDialog(SuntimesWidgetSettingsActivity.this);
                String helpContent = getString(R.string.help_general_timeMode);
                helpDialog.onPrepareDialog(helpContent);
                helpDialog.show();
            }
        });

        //
        // widget: timezone mode
        //
        ArrayAdapter<SuntimesWidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesWidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner)findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
        spinner_timezoneMode.setOnItemSelectedListener(onTimezoneModeListener);

        //
        // widget: timezone
        //
        label_timezone = (TextView)findViewById(R.id.appwidget_timezone_custom_label);

        SuntimesWidgetTimeZones.TimeZoneItemAdapter spinner_timezoneAdapter;
        spinner_timezoneAdapter = new SuntimesWidgetTimeZones.TimeZoneItemAdapter(this,
                R.layout.layout_listitem_twoline, SuntimesWidgetTimeZones.getValues() );

        spinner_timezone = (Spinner)findViewById(R.id.app_widget_timezone_custom);
        spinner_timezone.setAdapter(spinner_timezoneAdapter);

        //
        // widget: location mode
        //
        ArrayAdapter<SuntimesWidgetSettings.LocationMode> spinner_locationModeAdapter;
        spinner_locationModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesWidgetSettings.LocationMode.values());
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

        //
        // widget: 1x1 widget mode
        //
        ArrayAdapter<SuntimesWidgetSettings.WidgetMode1x1> spinner_1x1ModeAdapter;
        spinner_1x1ModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesWidgetSettings.WidgetMode1x1.values());
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
                SettingsHelpDialog helpDialog = new SettingsHelpDialog(SuntimesWidgetSettingsActivity.this);
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
        ArrayAdapter<SuntimesWidgetSettings.CompareMode> spinner_compareModeAdapter;
        spinner_compareModeAdapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesWidgetSettings.CompareMode.values());
        spinner_compareModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_compareMode = (Spinner)findViewById(R.id.appwidget_general_compareMode);
        spinner_compareMode.setAdapter(spinner_compareModeAdapter);

        //
        // widget: add button
        //
        Button button_addWidget = (Button)findViewById(R.id.add_button);
        button_addWidget.setOnClickListener(onAddButtonClickListener);

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
        spinner_timezone.setSelection(SuntimesWidgetTimeZones.ordinal(timezoneID), true);

        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
    }

    private void setCustomLocationEnabled( boolean value )
    {
        label_locationLon.setEnabled(value);
        text_locationLon.setEnabled(value);

        label_locationLat.setEnabled(value);
        text_locationLat.setEnabled(value);
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
     *
     */
    Spinner.OnItemSelectedListener onTimezoneModeListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final SuntimesWidgetSettings.TimezoneMode[] timezoneModes = SuntimesWidgetSettings.TimezoneMode.values();
            SuntimesWidgetSettings.TimezoneMode timezoneMode = timezoneModes[ parent.getSelectedItemPosition() ];
            setCustomTimezoneEnabled( (timezoneMode == SuntimesWidgetSettings.TimezoneMode.CUSTOM_TIMEZONE) );
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };

    /**
     *
     */
    Spinner.OnItemSelectedListener onLocationModeListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final SuntimesWidgetSettings.LocationMode[] locationModes = SuntimesWidgetSettings.LocationMode.values();
            SuntimesWidgetSettings.LocationMode locationMode = locationModes[ parent.getSelectedItemPosition() ];
            setCustomLocationEnabled( (locationMode == SuntimesWidgetSettings.LocationMode.CUSTOM_LOCATION) );
        }

        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };


    private void saveAppearanceSettings(Context context)
    {
        // save: widgetmode_1x1
        final SuntimesWidgetSettings.WidgetMode1x1[] modes = SuntimesWidgetSettings.WidgetMode1x1.values();
        SuntimesWidgetSettings.WidgetMode1x1 mode = modes[ spinner_1x1mode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.save1x1ModePref(context, appWidgetId, mode);

        // save: theme
        final SuntimesWidgetThemes.ThemeDescriptor[] themes = SuntimesWidgetThemes.values();
        SuntimesWidgetThemes.ThemeDescriptor theme = themes[ spinner_theme.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveThemePref(context, appWidgetId, theme.name());
        Log.d("DEBUG", "Saved theme: " + theme.name());

        // save: show title
        boolean showTitle = checkbox_showTitle.isChecked();
        SuntimesWidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

        // save:: title text
        String titleText = text_titleText.getText().toString().trim();
        SuntimesWidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);
    }

    private void loadAppearanceSettings(Context context)
    {
        // load: widgetmode_1x1
        SuntimesWidgetSettings.WidgetMode1x1 mode1x1 = SuntimesWidgetSettings.load1x1ModePref(context, appWidgetId);
        spinner_1x1mode.setSelection(mode1x1.ordinal());

        // load: theme
        SuntimesWidgetTheme theme = SuntimesWidgetSettings.loadThemePref(context, appWidgetId);
        SuntimesWidgetThemes.ThemeDescriptor themeDescriptor = SuntimesWidgetThemes.valueOf(theme.getThemeName());
        spinner_theme.setSelection(themeDescriptor.ordinal());

        // load: show title
        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

        // load: title text
        String titleText = SuntimesWidgetSettings.loadTitleTextPref(context, appWidgetId);
        text_titleText.setText(titleText);
    }


    private void saveGeneralSettings(Context context)
    {
        // save: calculator mode
        final SuntimesCalculatorDescriptor[] calculators = SuntimesCalculatorDescriptor.values();
        SuntimesCalculatorDescriptor calculator = calculators[ spinner_calculatorMode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveCalculatorModePref(context, appWidgetId, calculator);

        // save: time mode
        final SuntimesWidgetSettings.TimeMode[] timeModes = SuntimesWidgetSettings.TimeMode.values();
        SuntimesWidgetSettings.TimeMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveTimeModePref(context, appWidgetId, timeMode);

        // save: compare mode
        final SuntimesWidgetSettings.CompareMode[] compareModes = SuntimesWidgetSettings.CompareMode.values();
        SuntimesWidgetSettings.CompareMode compareMode = compareModes[ spinner_compareMode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveCompareModePref(context, appWidgetId, compareMode);
    }

    private void loadGeneralSettings(Context context)
    {
        // load: calculator mode
        SuntimesCalculatorDescriptor calculatorMode = SuntimesWidgetSettings.loadCalculatorModePref(context, appWidgetId);
        spinner_calculatorMode.setSelection(calculatorMode.ordinal());

        // load: time mode
        SuntimesWidgetSettings.CompareMode compareMode = SuntimesWidgetSettings.loadCompareModePref(context, appWidgetId);
        spinner_compareMode.setSelection(compareMode.ordinal());

        // load: compare mode
        SuntimesWidgetSettings.TimeMode timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);
        spinner_timeMode.setSelection(timeMode.ordinal());
    }


    private void saveLocationSettings(Context context)
    {
        // save: location mode
        final SuntimesWidgetSettings.LocationMode[] locationModes = SuntimesWidgetSettings.LocationMode.values();
        SuntimesWidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

        // save: lat / lon
        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();
        SuntimesWidgetSettings.Location location = new SuntimesWidgetSettings.Location(latitude, longitude);
        SuntimesWidgetSettings.saveLocationPref(context, appWidgetId, location);
    }

    private void loadLocationSettings(Context context)
    {
        SuntimesWidgetSettings.LocationMode locationMode = SuntimesWidgetSettings.loadLocationModePref(context, appWidgetId);
        spinner_locationMode.setSelection(locationMode.ordinal());

        SuntimesWidgetSettings.Location location = SuntimesWidgetSettings.loadLocationPref(context, appWidgetId);
        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
    }


    private void saveTimezoneSettings(Context context)
    {
        // save: timezone mode
        final SuntimesWidgetSettings.TimezoneMode[] timezoneModes = SuntimesWidgetSettings.TimezoneMode.values();
        SuntimesWidgetSettings.TimezoneMode timezoneMode = timezoneModes[ spinner_timezoneMode.getSelectedItemPosition() ];
        SuntimesWidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        SuntimesWidgetTimeZones.TimeZoneItem customTimezone = (SuntimesWidgetTimeZones.TimeZoneItem)spinner_timezone.getSelectedItem();
        SuntimesWidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());
    }

    private void loadTimezoneSettings(Context context)
    {
        SuntimesWidgetSettings.TimezoneMode timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        customTimezoneID = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);
        int timezonePos = SuntimesWidgetTimeZones.ordinal(customTimezoneID);
        int numTimeZones = SuntimesWidgetTimeZones.values().length;

        if (timezonePos >= 0 && timezonePos < numTimeZones)
        {
            spinner_timezone.setSelection(timezonePos);
        } else {
            spinner_timezone.setSelection(0);
            Log.w("loadTimezoneSettings", "unable to find timezone " + customTimezoneID + " in the list! Setting selection to 0." );
        }
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

    private void addWidget()
    {
        final Context context = SuntimesWidgetSettingsActivity.this;

        saveGeneralSettings(context);
        saveLocationSettings(context);
        saveTimezoneSettings(context);
        saveAppearanceSettings(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget.updateAppWidget(context, appWidgetManager, appWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    /**
     * Click handler executed when the "About" button is pressed.
     */
    View.OnClickListener onAboutButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            SettingsAboutDialog aboutDialog = new SettingsAboutDialog(SuntimesWidgetSettingsActivity.this);
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

}
