/**
    Copyright (C) 2018-2020 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class AlarmTimeDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_TIME_24HR = "is24";
    public static final boolean PREF_DEF_ALARM_TIME_24HR = true;

    public static final String PREF_KEY_ALARM_TIME_DATE = "alarmdate";
    public static final long PREF_DEF_ALARM_TIME_DATE = -1L;

    public static final String PREF_KEY_ALARM_TIME_HOUR = "alarmhour";
    public static final int PREF_DEF_ALARM_TIME_HOUR = 0;

    public static final String PREF_KEY_ALARM_TIME_MINUTE = "alarmminute";
    public static final int PREF_DEF_ALARM_TIME_MINUTE = 0;

    public static final String PREF_KEY_ALARM_TIME_MODE = "alarmtimezonemode";
    public static final AlarmClockItem.AlarmTimeZone PREF_DEF_ALARM_TIME_MODE = AlarmClockItem.AlarmTimeZone.SYSTEM_TIME;

    public static final String PREF_KEY_ALARM_LOCATION = "alarmlocation";

    private TimePicker timePicker;
    private Spinner modePicker;
    private TextView locationPicker;
    private TextView datePicker;

    private AlarmTimeModeAdapter modeAdapter;
    private SuntimesUtils utils = new SuntimesUtils();

    public AlarmTimeDialog()
    {
        super();

        Bundle defaultArgs = new Bundle();
        defaultArgs.putLong(PREF_KEY_ALARM_TIME_DATE, PREF_DEF_ALARM_TIME_DATE);
        defaultArgs.putInt(PREF_KEY_ALARM_TIME_HOUR, PREF_DEF_ALARM_TIME_HOUR);
        defaultArgs.putInt(PREF_KEY_ALARM_TIME_MINUTE, PREF_DEF_ALARM_TIME_MINUTE);
        defaultArgs.putBoolean(PREF_KEY_ALARM_TIME_24HR, PREF_DEF_ALARM_TIME_24HR);
        defaultArgs.putString(PREF_KEY_ALARM_TIME_MODE, PREF_DEF_ALARM_TIME_MODE.timeZoneID());
        setArguments(defaultArgs);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        Bundle args = getArguments();
        if (getLocation() == null) {
            args.putParcelable(PREF_KEY_ALARM_LOCATION, WidgetSettings.loadLocationPref(getActivity(), 0));
        }
        super.onCreate(savedState);
    }

    public void setDate(long datetime) {
        getArguments().putLong(PREF_KEY_ALARM_TIME_DATE, datetime);
    }
    public void setTime(int hour, int minute) {
        getArguments().putInt(PREF_KEY_ALARM_TIME_HOUR, hour);
        getArguments().putInt(PREF_KEY_ALARM_TIME_MINUTE, minute);
        updateDate();
    }
    public void set24Hour(boolean value) {
        getArguments().putBoolean(PREF_KEY_ALARM_TIME_24HR, value);
    }
    public void setTimeZone(String value) {
        getArguments().putString(PREF_KEY_ALARM_TIME_MODE, value);
        updateDate();
    }
    public void setLocation(Location location) {
        getArguments().putParcelable(PREF_KEY_ALARM_LOCATION, location);
        updateDate();
    }

    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        super.onCreate(savedState);
        View dialogContent = inflater.inflate(R.layout.layout_dialog_alarmtime, null);
        initViews(getActivity(), dialogContent);
        updateViews(getContext());
        return dialogContent;
    }

    protected void initViews( final Context context, View dialogContent )
    {
        SuntimesUtils.initDisplayStrings(context);
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);
        modeAdapter = new AlarmTimeModeAdapter(context, R.layout.layout_listitem_alarmtz, AlarmClockItem.AlarmTimeZone.values());
        //modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modePicker = (Spinner)dialogContent.findViewById(R.id.modepicker);
        modePicker.setAdapter(modeAdapter);

        timePicker = (TimePicker)dialogContent.findViewById(R.id.timepicker);
        if (timePicker != null) {
            if (Build.VERSION.SDK_INT >= 11) {
                timePicker.setSaveFromParentEnabled(false);    // fixes crash #482 (https://issuetracker.google.com/issues/36936584)
            }
            timePicker.setSaveEnabled(true);
        }

        locationPicker = (TextView) dialogContent.findViewById(R.id.locationPicker);
        datePicker = (TextView) dialogContent.findViewById(R.id.datePicker);
        setTimeChangedListener();
    }

    private void setTimeChangedListener()
    {
        if (timePicker != null) {
            timePicker.setOnTimeChangedListener(onTimeChanged);
        }
        if (modePicker != null) {
            modePicker.setOnItemSelectedListener(onModeChanged);
        }
        if (locationPicker != null) {
            locationPicker.setOnClickListener(onLocationClicked);
        }
        if (datePicker != null) {
            datePicker.setOnClickListener(onDateClicked);
        }
    }
    private void clearTimeChangedListener()
    {
        if (timePicker != null) {
            timePicker.setOnTimeChangedListener(null);
        }
        if (modePicker != null) {
            modePicker.setOnItemSelectedListener(null);
        }
        if (locationPicker != null) {
            locationPicker.setOnClickListener(null);
        }
        if (datePicker != null) {
            datePicker.setOnClickListener(null);
        }
    }

    private TimePicker.OnTimeChangedListener onTimeChanged = new TimePicker.OnTimeChangedListener()
    {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
        {
            getArguments().putInt(PREF_KEY_ALARM_TIME_HOUR, hourOfDay);
            getArguments().putInt(PREF_KEY_ALARM_TIME_MINUTE, minute);
            updateDate();

            if (listener != null) {
                listener.onChanged(AlarmTimeDialog.this);
            }
        }
    };

    private void updateDate()
    {
        if (getDate() != -1L)
        {
            Calendar calendar = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(getTimeZone(), getLocation()));
            calendar.setTimeInMillis(getDate());
            calendar.set(Calendar.HOUR_OF_DAY, getHour());
            calendar.set(Calendar.MINUTE, getMinute());
            calendar.set(Calendar.SECOND, 0);
            getArguments().putLong(PREF_KEY_ALARM_TIME_DATE, calendar.getTimeInMillis());
        }
    }

    private AdapterView.OnItemSelectedListener onModeChanged = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            setTimeZone(((AlarmClockItem.AlarmTimeZone) parent.getItemAtPosition(position)).timeZoneID());
            updateViews(getActivity());
            if (listener != null) {
                listener.onChanged(AlarmTimeDialog.this);
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private View.OnClickListener onLocationClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onLocationClick(AlarmTimeDialog.this);
            }
        }
    };

    private View.OnClickListener onDateClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onDateClick(AlarmTimeDialog.this);
            }
        }
    };

    protected void updateViews(Context context)
    {
        clearTimeChangedListener();

        if (modePicker != null && modeAdapter != null) {
            AlarmClockItem.AlarmTimeZone mode = AlarmClockItem.AlarmTimeZone.valueOfID(getArguments().getString(PREF_KEY_ALARM_TIME_MODE));
            modePicker.setSelection(modeAdapter.getPosition(mode));
        }

        if (datePicker != null) {
            datePicker.setText(displayDate(getActivity(), getDate()));
        }

        if (timePicker != null)
        {
            timePicker.setIs24HourView(getArguments().getBoolean(PREF_KEY_ALARM_TIME_24HR));
            timePicker.setCurrentHour(getArguments().getInt(PREF_KEY_ALARM_TIME_HOUR));
            timePicker.setCurrentMinute(getArguments().getInt(PREF_KEY_ALARM_TIME_MINUTE));
        }

        if (locationPicker != null) {
            locationPicker.setText(displayLocation(getActivity(), getLocation()));
            locationPicker.setVisibility(getArguments().getString(PREF_KEY_ALARM_TIME_MODE) == null ? View.GONE : View.VISIBLE);
        }

        setTimeChangedListener();
    }

    public CharSequence displayDate(Context context, long datetime)
    {
        if (datetime != -1L)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(datetime);
            return utils.calendarDateDisplayString(context, calendar, true).toString();
        } else {
            return "";    //return context.getString(R.string.date_any);
        }
    }

    public static CharSequence displayLocation(Context context, Location location)
    {
        if (location == null) {
            return "";
        }
        String coordString = context.getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
        String labelString = location.getLabel();
        String displayString = labelString + "\n" + coordString;
        SpannableString displayText = SuntimesUtils.createBoldSpan(null, displayString, labelString);
        return SuntimesUtils.createRelativeSpan(displayText, displayString, coordString, 0.75f);
    }

    public long getDate() {
        return getArguments().getLong(PREF_KEY_ALARM_TIME_DATE, PREF_DEF_ALARM_TIME_DATE);
    }

    public int getHour() {
        return getArguments().getInt(PREF_KEY_ALARM_TIME_HOUR, PREF_DEF_ALARM_TIME_HOUR);
    }

    public int getMinute() {
        return getArguments().getInt(PREF_KEY_ALARM_TIME_MINUTE, PREF_DEF_ALARM_TIME_MINUTE);
    }

    public String getTimeZone() {
        return getArguments().getString(PREF_KEY_ALARM_TIME_MODE);
    }

    public Location getLocation() {
        return (Location)getArguments().getParcelable(PREF_KEY_ALARM_LOCATION);
    }

    public interface DialogListener
    {
        void onAccepted(AlarmTimeDialog dialog);
        void onCanceled(AlarmTimeDialog dialog);
        void onChanged(AlarmTimeDialog dialog);
        void onLocationClick(AlarmTimeDialog dialog);
        void onDateClick(AlarmTimeDialog dialog);
    }

    private DialogListener listener = null;
    public void setDialogListener( DialogListener listener ) {
        this.listener = listener;
    }

    /**
     * AlarmTimeModeAdapter
     */
    public static class AlarmTimeModeAdapter extends ArrayAdapter<AlarmClockItem.AlarmTimeZone>
    {
        private int layout;
        public AlarmTimeModeAdapter(@NonNull Context context, int resource, @NonNull AlarmClockItem.AlarmTimeZone[] objects)
        {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }
        @NonNull @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @SuppressLint("ResourceType")
        private View createView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(layout, parent, false);
            }

            int[] iconAttr = { R.attr.icActionTime };
            TypedArray typedArray = getContext().obtainStyledAttributes(iconAttr);
            int res_icon0 = typedArray.getResourceId(0, R.drawable.ic_action_time);
            typedArray.recycle();

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            TextView text = (TextView) view.findViewById(android.R.id.text1);

            AlarmClockItem.AlarmTimeZone item = getItem(position);

            if (text != null) {
                text.setText(item != null ? item.displayString() : "");
            }

            if (icon != null)
            {
                int resID = (item != null && item.timeZoneID() != null ? R.drawable.ic_sun : res_icon0);
                icon.setImageDrawable(null);
                icon.setBackgroundResource(item != null ? resID : 0);
            }

            return view;
        }
    }

}