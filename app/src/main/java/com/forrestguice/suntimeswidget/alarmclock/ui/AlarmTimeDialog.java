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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;

public class AlarmTimeDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_TIME_24HR = "is24";
    public static final boolean PREF_DEF_ALARM_TIME_24HR = true;

    public static final String PREF_KEY_ALARM_TIME_HOUR = "alarmhour";
    public static final int PREF_DEF_ALARM_TIME_HOUR = 0;

    public static final String PREF_KEY_ALARM_TIME_MINUTE = "alarmminute";
    public static final int PREF_DEF_ALARM_TIME_MINUTE = 0;

    public static final String PREF_KEY_ALARM_TIME_MODE = "alarmtimezonemode";
    public static final AlarmClockItem.AlarmTimeZone PREF_DEF_ALARM_TIME_MODE = AlarmClockItem.AlarmTimeZone.SYSTEM_TIME;

    private TimePicker timePicker;
    private Spinner modePicker;

    public AlarmTimeDialog()
    {
        super();

        Bundle defaultArgs = new Bundle();
        defaultArgs.putInt(PREF_KEY_ALARM_TIME_HOUR, PREF_DEF_ALARM_TIME_HOUR);
        defaultArgs.putInt(PREF_KEY_ALARM_TIME_MINUTE, PREF_DEF_ALARM_TIME_MINUTE);
        defaultArgs.putBoolean(PREF_KEY_ALARM_TIME_24HR, PREF_DEF_ALARM_TIME_24HR);
        defaultArgs.putString(PREF_KEY_ALARM_TIME_MODE, PREF_DEF_ALARM_TIME_MODE.timeZoneID());
        setArguments(defaultArgs);
    }

    public void setTime(int hour, int minute) {
        getArguments().putInt(PREF_KEY_ALARM_TIME_HOUR, hour);
        getArguments().putInt(PREF_KEY_ALARM_TIME_MINUTE, minute);
    }
    public void set24Hour(boolean value) {
        getArguments().putBoolean(PREF_KEY_ALARM_TIME_24HR, value);
    }
    public void setTimeZone(String value) {
        getArguments().putString(PREF_KEY_ALARM_TIME_MODE, value);
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
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);
        ArrayAdapter<AlarmClockItem.AlarmTimeZone> modeAdapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, AlarmClockItem.AlarmTimeZone.values());
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modePicker = (Spinner)dialogContent.findViewById(R.id.modepicker);
        modePicker.setAdapter(modeAdapter);

        AlarmClockItem.AlarmTimeZone mode = AlarmClockItem.AlarmTimeZone.valueOfID(getArguments().getString(PREF_KEY_ALARM_TIME_MODE));
        modePicker.setSelection(modeAdapter.getPosition(mode));

        timePicker = (TimePicker)dialogContent.findViewById(R.id.timepicker);
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
    }
    private void clearTimeChangedListener()
    {
        if (timePicker != null) {
            timePicker.setOnTimeChangedListener(null);
        }
        if (modePicker != null) {
            modePicker.setOnItemSelectedListener(null);
        }
    }

    private TimePicker.OnTimeChangedListener onTimeChanged = new TimePicker.OnTimeChangedListener()
    {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
        {
            getArguments().putInt(PREF_KEY_ALARM_TIME_HOUR, hourOfDay);
            getArguments().putInt(PREF_KEY_ALARM_TIME_MINUTE, minute);
        }
    };

    private AdapterView.OnItemSelectedListener onModeChanged = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            getArguments().putString(PREF_KEY_ALARM_TIME_MODE, ((AlarmClockItem.AlarmTimeZone) parent.getItemAtPosition(position)).timeZoneID());
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void updateViews(Context context)
    {
        if (timePicker != null)
        {
            clearTimeChangedListener();
            timePicker.setIs24HourView(getArguments().getBoolean(PREF_KEY_ALARM_TIME_24HR));
            timePicker.setCurrentHour(getArguments().getInt(PREF_KEY_ALARM_TIME_HOUR));
            timePicker.setCurrentMinute(getArguments().getInt(PREF_KEY_ALARM_TIME_MINUTE));
            setTimeChangedListener();
        }
    }

    public int getHour() {
        return getArguments().getInt(PREF_KEY_ALARM_TIME_HOUR);
    }

    public int getMinute() {
        return getArguments().getInt(PREF_KEY_ALARM_TIME_MINUTE);
    }

    public String getTimeZone() {
        return getArguments().getString(PREF_KEY_ALARM_TIME_MODE);
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

}