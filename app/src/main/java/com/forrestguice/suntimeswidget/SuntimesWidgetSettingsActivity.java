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
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;


/**
 * The configuration screen for the {@link SuntimesWidget SuntimesWidget} AppWidget.
 */
public class SuntimesWidgetSettingsActivity extends Activity
{
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_calculatorMode;
    private Spinner spinner_timeMode;
    private Spinner spinner_compareMode;

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

    public SuntimesWidgetSettingsActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);  // causes widget host to cancel if user presses back
        setContentView(R.layout.sunrise_and_set_widget_configure);

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

        initViews(context);

        loadGeneralSettings(context);
        loadAppearanceSettings(context);
        loadLocationSettings(context);
        loadTimezoneSettings(context);
    }

    View.OnClickListener onAddButtonClickListener = new View.OnClickListener()
    {
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


    private void initViews( Context context )
    {
        //
        // widget: source
        //
        ArrayAdapter<SuntimesCalculatorDescriptor> spinner_calculatorModeAdapter;
        spinner_calculatorModeAdapter = new ArrayAdapter<SuntimesCalculatorDescriptor>(this,
                android.R.layout.simple_spinner_item,
                SuntimesCalculatorDescriptor.values());  // TODO: source of values
        spinner_calculatorModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_calculatorMode = (Spinner)findViewById(R.id.appwidget_general_calculator);
        spinner_calculatorMode.setAdapter(spinner_calculatorModeAdapter);


        //
        // widget: time mode
        //
        ArrayAdapter<SuntimesWidgetSettings.TimeMode> spinner_timeModeAdapter;
        spinner_timeModeAdapter = new ArrayAdapter<SuntimesWidgetSettings.TimeMode>(this,
                android.R.layout.simple_spinner_item,
                SuntimesWidgetSettings.TimeMode.values());
        spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timeMode = (Spinner)findViewById(R.id.appwidget_general_timeMode);
        spinner_timeMode.setAdapter(spinner_timeModeAdapter);


        //
        // widget: timezone mode
        //
        ArrayAdapter<SuntimesWidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<SuntimesWidgetSettings.TimezoneMode>(this,
                android.R.layout.simple_spinner_item,
                SuntimesWidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner)findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
        spinner_timezoneMode.setOnItemSelectedListener(onTimezoneModeListener);

        //
        // widget: timezone
        //
        label_timezone = (TextView)findViewById(R.id.appwidget_timezone_custom_label);
        spinner_timezone = (Spinner)findViewById(R.id.app_widget_timezone_custom);

        //
        // widget: location mode
        //
        ArrayAdapter<SuntimesWidgetSettings.LocationMode> spinner_locationModeAdapter;
        spinner_locationModeAdapter = new ArrayAdapter<SuntimesWidgetSettings.LocationMode>(this,
                android.R.layout.simple_spinner_item,
                SuntimesWidgetSettings.LocationMode.values());
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
        // widget: title text
        //
        label_titleText = (TextView)findViewById(R.id.appwidget_appearance_titleText_label);
        text_titleText = (EditText)findViewById(R.id.appwidget_appearance_titleText);

        //
        // widget: show title
        //
        checkbox_showTitle = (CheckBox)findViewById(R.id.appwidget_appearance_showTitle);
        checkbox_showTitle.setOnCheckedChangeListener(onShowTitleListener);

        //
        // widget: compare mode
        //
        ArrayAdapter<SuntimesWidgetSettings.CompareMode> spinner_compareModeAdapter;
        spinner_compareModeAdapter = new ArrayAdapter<SuntimesWidgetSettings.CompareMode>(this,
                android.R.layout.simple_spinner_item,
                SuntimesWidgetSettings.CompareMode.values());
        spinner_compareModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_compareMode = (Spinner)findViewById(R.id.appwidget_general_compareMode);
        spinner_compareMode.setAdapter(spinner_compareModeAdapter);

        //
        // widget: add button
        //
        Button button_addWidget = (Button)findViewById(R.id.add_button);
        button_addWidget.setOnClickListener(onAddButtonClickListener);
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
        // save: appearance (show title)
        boolean showTitle = checkbox_showTitle.isChecked();
        SuntimesWidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

        // save:: appearance (title text)
        String titleText = text_titleText.getText().toString().trim();
        SuntimesWidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);
    }

    private void loadAppearanceSettings(Context context)
    {
        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

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
        SuntimesWidgetSettings.CompareMode.initDisplayStrings(context);
        SuntimesWidgetSettings.CompareMode compareMode = SuntimesWidgetSettings.loadCompareModePref(context, appWidgetId);
        spinner_compareMode.setSelection(compareMode.ordinal());

        // load: compare mode
        SuntimesWidgetSettings.TimeMode.initDisplayStrings(context);
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
        SuntimesWidgetSettings.LocationMode.initDisplayStrings(context);
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
        String customTimezone = SuntimesWidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;  // TODO
        SuntimesWidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone);
    }

    private void loadTimezoneSettings(Context context)
    {
        SuntimesWidgetSettings.TimezoneMode.initDisplayStrings(context);
        SuntimesWidgetSettings.TimezoneMode timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        String timezone = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);    // TODO: finish custom timezone feature
        // TODO
    }

}
