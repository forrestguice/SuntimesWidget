/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.TimeZoneDialog;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.FragmentManagerCompat;
import com.forrestguice.support.content.ContextCompat;

import java.util.Calendar;
import java.util.TimeZone;

public class WelcomeTimeZoneView extends WelcomeView
{
    private static final TimeDeltaDisplay utils = new TimeDeltaDisplay();

    private Spinner timeFormatSpinner;
    private TextView timeZoneWarning, timeZoneWarningNote;
    private Button timeZoneSuggestButton;

    public WelcomeTimeZoneView(Context context) {
        super(context, R.layout.layout_welcome_timezone);
    }
    public WelcomeTimeZoneView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_timezone);
    }
    public WelcomeTimeZoneView(AppCompatActivity activity)
    {
        super(activity, R.layout.layout_welcome_timezone);
        setLongitude(Double.parseDouble(WidgetSettings.PREF_DEF_LOCATION_LONGITUDE));
    }

    public static WelcomeTimeZoneView newInstance(AppCompatActivity activity)
    {
        WelcomeTimeZoneView fragment = new WelcomeTimeZoneView(activity);
        Location location = WidgetSettings.loadLocationPref(activity, 0);
        fragment.setLongitude(location.getLongitudeAsDouble());
        fragment.setLongitudeLabel(location.getLabel());
        return fragment;
    }

    public double getLongitude() {
        //return getArgs().getDouble(TimeZoneDialog.KEY_LONGITUDE);
        return longitude;
    }
    public void setLongitude(double value) {
        //getArgs().putDouble(TimeZoneDialog.KEY_LONGITUDE, value);
        longitude = value;
    }
    protected double longitude;

    public String getLongitudeLabel() {
        //return getArgs().getString(LocationConfigView.KEY_LOCATION_LABEL);
        return label_longitude;
    }
    public void setLongitudeLabel( String value ) {
        label_longitude = value;
        //getArgs().putString(LocationConfigView.KEY_LOCATION_LABEL, value);
    }
    protected String label_longitude;

    public void toggleWarning(boolean visible)
    {
        if (timeZoneWarning != null) {
            timeZoneWarning.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        //if (timeZoneWarningNote != null) {
        //    timeZoneWarningNote.setVisibility(visible ? View.VISIBLE : View.GONE);
        //}
        if (timeZoneSuggestButton != null) {
            timeZoneSuggestButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected TimeZoneDialog getTimeZoneDialog()
    {
        FragmentManagerCompat fragments = getFragmentManager();
        return (fragments != null ? (TimeZoneDialog) fragments.findFragmentByTag("TimeZoneDialog") : null);
    }

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);

        TimeZoneDialog tzConfig = getTimeZoneDialog();
        if (tzConfig != null) {
            tzConfig.setTimeFormatMode(WidgetSettings.loadTimeFormatModePref(context, 0));
            tzConfig.setDialogListener(timeZoneDialogListener());
        }

        timeZoneWarning = (TextView) view.findViewById(R.id.warning_timezone);
        timeZoneWarningNote = (TextView) view.findViewById(R.id.warning_timezone_note);

        if (timeZoneWarning != null)
        {
            ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.warningIcon_size));
            timeZoneWarning.setText(SuntimesUtils.createSpan(context, timeZoneWarning.getText().toString(), SuntimesUtils.SPANTAG_WARNING, warningIcon));
        }

        timeZoneSuggestButton = (Button) view.findViewById(R.id.button_suggest_timezone);
        if (timeZoneSuggestButton != null) {
            timeZoneSuggestButton.setOnClickListener(timeZoneSuggestButtonListener());
        }

        timeFormatSpinner = (Spinner) view.findViewById(R.id.appwidget_general_timeformatmode);
        if (timeFormatSpinner != null)
        {
            final TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, 0);
            final ArrayAdapter<TimeFormatMode> adapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline,
                    new TimeFormatMode[] {TimeFormatMode.MODE_SYSTEM, TimeFormatMode.MODE_12HR, TimeFormatMode.MODE_24HR});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeFormatSpinner.setAdapter(adapter);
            timeFormatSpinner.setOnItemSelectedListener(onTimeFormatSelected());
            timeFormatSpinner.post(new Runnable() {
                @Override
                public void run() {
                    timeFormatSpinner.setSelection(adapter.getPosition(timeFormat), false);
                }
            });
        }
    }

    @Override
    public void updateViews(Context context)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        setLongitude(location.getLongitudeAsDouble());
        setLongitudeLabel(location.getLabel());

        TimeZoneDialog tzConfig = getTimeZoneDialog();
        if (tzConfig != null) {
            tzConfig.setLongitude(getLongitudeLabel(), getLongitude());
            tzConfig.updatePreview(context);
        }
    }

    protected void updateWarningNote(Context context, TimeZone tz)
    {
        if (timeZoneWarningNote != null)
        {
            long zoneOffsetMillis = tz.getOffset(System.currentTimeMillis());
            long lonOffsetMillis = Math.round(getLongitude() * (24 * 60 * 60 * 1000) / 360d);
            long offset = zoneOffsetMillis - lonOffsetMillis;
            String offsetDisplay = (offset < 0 ? "-" : "+") + utils.timeDeltaLongDisplayString(offset);

            TypedArray typedArray = context.obtainStyledAttributes(new int[] { R.attr.tagColor_warning, R.attr.text_primaryColor });
            int warningColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.warningTag_dark));
            int normalColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_primary_dark));
            typedArray.recycle();

            int highlightColor = normalColor;
            if (Math.abs(offset / 1000 / 60 / 60) >= WidgetTimezones.WARNING_TOLERANCE_HOURS) {
                highlightColor = warningColor;
            }

            String location = getLongitudeLabel();
            String note = context.getString(R.string.timezoneWarningNote, tz.getID(), offsetDisplay, location);
            SpannableString noteDisplay = SuntimesUtils.createBoldColorSpan(null, note, offsetDisplay, highlightColor);
            noteDisplay = SuntimesUtils.createBoldColorSpan(noteDisplay, note, location, normalColor);
            timeZoneWarningNote.setText(noteDisplay);
        }
    }

    private TimeZoneDialog.TimeZoneDialogListener timeZoneDialogListener()
    {
        return new TimeZoneDialog.TimeZoneDialogListener()
        {
            @Override
            public void onSelectionChanged( TimeZone tz ) {
                toggleWarning(WidgetTimezones.isProbablyNotLocal(tz, getLongitude(), Calendar.getInstance(tz).getTime()));
                updateWarningNote(getContext(), tz);
            }
        };
    }

    private AdapterView.OnItemSelectedListener onTimeFormatSelected()
    {
        return new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Activity context = getActivity();
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null && context != null) {
                    tzConfig.setTimeFormatMode((TimeFormatMode) parent.getAdapter().getItem(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    private OnClickListener timeZoneSuggestButtonListener()
    {
        return new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TimeZoneDialog tzConfig = getTimeZoneDialog();
                if (tzConfig != null) {
                    tzConfig.setCustomTimeZone(tzConfig.timeZoneRecommendation(getLongitudeLabel(), getLongitude()));
                }
            }
        };
    }

    @Override
    public boolean saveSettings(Context context)
    {
        if (isAdded())
        {
            TimeZoneDialog tzConfig = getTimeZoneDialog();
            if (tzConfig != null) {
                tzConfig.saveSettings(context);
            }

            TimeFormatMode timeFormat = (TimeFormatMode) timeFormatSpinner.getSelectedItem();
            WidgetSettings.saveTimeFormatModePref(context, 0, timeFormat);
            //Log.d("DEBUG", "saveSettings: timezone");
            return true;
        }
        return false;
    }
}
