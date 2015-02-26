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

import com.luckycatlabs.sunrisesunset.dto.Location;


/**
 * The configuration screen for the {@link SuntimesWidget SuntimesWidget} AppWidget.
 */
public class SuntimesWidgetSettingsActivity extends Activity
{
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_locationMode;

    private EditText text_locationLon;
    private EditText text_locationLat;
    private TextView label_locationLon;
    private TextView label_locationLat;

    private Spinner spinner_timezoneMode;
    private Spinner spinner_timezone;
    private TextView label_timezone;

    private Spinner spinner_timeMode;
    private Spinner spinner_compareMode;

    private CheckBox checkbox_showtitle;
    private EditText text_titletext;
    private TextView label_titletext;

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

        SuntimesWidgetSettings.LocationMode.initDisplayStrings(context);
        SuntimesWidgetSettings.TimezoneMode.initDisplayStrings(context);
        SuntimesWidgetSettings.CompareMode.initDisplayStrings(context);

        SuntimesWidgetSettings.TimeMode timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);
        SuntimesWidgetSettings.LocationMode locationMode = SuntimesWidgetSettings.loadLocationModePref(context, appWidgetId);
        SuntimesWidgetSettings.TimezoneMode timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        SuntimesWidgetSettings.CompareMode compareMode = SuntimesWidgetSettings.loadCompareModePref(context, appWidgetId);

        Location location = SuntimesWidgetSettings.loadLocationPref(context, appWidgetId);
        String timezone = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);    // TODO: finish custom timezone feature

        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        String titleText = SuntimesWidgetSettings.loadTitleTextPref(context, appWidgetId);

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
        spinner_timeMode.setSelection(timeMode.ordinal());

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
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

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
        spinner_locationMode.setSelection(locationMode.ordinal());

        //
        // widget: custom lon / lat
        //
        label_locationLon = (TextView)findViewById(R.id.appwidget_location_lon_label);
        text_locationLon = (EditText)findViewById(R.id.appwidget_location_lon);
        text_locationLon.setText(location.getLongitude().toPlainString());

        label_locationLat = (TextView)findViewById(R.id.appwidget_location_lat_label);
        text_locationLat = (EditText)findViewById(R.id.appwidget_location_lat);
        text_locationLat.setText(location.getLatitude().toPlainString());

        //
        // widget: title text
        //
        label_titletext = (TextView)findViewById(R.id.appwidget_appearance_titleText_label);
        text_titletext = (EditText)findViewById(R.id.appwidget_appearance_titleText);
        text_titletext.setText(titleText);

        //
        // widget: show title
        //
        checkbox_showtitle = (CheckBox)findViewById(R.id.appwidget_appearance_showTitle);
        checkbox_showtitle.setOnCheckedChangeListener(onShowTitleListener);
        checkbox_showtitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

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
        spinner_compareMode.setSelection(compareMode.ordinal());

        //
        // widget: add button
        //
        Button button_addWidget = (Button)findViewById(R.id.add_button);
        button_addWidget.setOnClickListener(onAddButtonClickListener);
    }

    private void setTitleTextEnabled( boolean value )
    {
        label_titletext.setEnabled(value);
        text_titletext.setEnabled(value);
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

    /**
     * OnClickListener called when the add button is clicked.
     */
    View.OnClickListener onAddButtonClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = SuntimesWidgetSettingsActivity.this;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // save: appearance (show title)
            boolean showTitle = checkbox_showtitle.isChecked();
            SuntimesWidgetSettings.saveShowTitlePref(context, appWidgetId, showTitle);

            // save:: appearance (title text)
            String titleText = text_titletext.getText().toString().trim();
            SuntimesWidgetSettings.saveTitleTextPref(context, appWidgetId, titleText);

            // save: time mode
            final SuntimesWidgetSettings.TimeMode[] timeModes = SuntimesWidgetSettings.TimeMode.values();
            SuntimesWidgetSettings.TimeMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition() ];
            SuntimesWidgetSettings.saveTimeModePref(context, appWidgetId, timeMode);

            // save: location mode
            final SuntimesWidgetSettings.LocationMode[] locationModes = SuntimesWidgetSettings.LocationMode.values();
            SuntimesWidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];
            SuntimesWidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

            // save: lat / lon
            String latitude = text_locationLat.getText().toString();
            String longitude = text_locationLon.getText().toString();
            Location location = new Location(latitude, longitude);
            SuntimesWidgetSettings.saveLocationPref(context, appWidgetId, location);

            // save: timezone mode
            final SuntimesWidgetSettings.TimezoneMode[] timezoneModes = SuntimesWidgetSettings.TimezoneMode.values();
            SuntimesWidgetSettings.TimezoneMode timezoneMode = timezoneModes[ spinner_timezoneMode.getSelectedItemPosition() ];
            SuntimesWidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

            // save: custom timezone
            String customTimezone = SuntimesWidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;  // TODO
            SuntimesWidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone);

            // save: compare mode
            final SuntimesWidgetSettings.CompareMode[] compareModes = SuntimesWidgetSettings.CompareMode.values();
            SuntimesWidgetSettings.CompareMode compareMode = compareModes[ spinner_compareMode.getSelectedItemPosition() ];
            SuntimesWidgetSettings.saveCompareModePref(context, appWidgetId, compareMode);

            // update and return
            SuntimesWidget.updateAppWidget(context, appWidgetManager, appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

}



