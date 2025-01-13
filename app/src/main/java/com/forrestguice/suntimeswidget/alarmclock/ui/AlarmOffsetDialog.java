/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.design.app.DialogFragment;
import com.forrestguice.support.design.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

@TargetApi(11)
public class AlarmOffsetDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_TIME_OFFSET = "alarmoffset";
    public static final long PREF_DEF_ALARM_TIME_OFFSET = 0L;

    public static final String PREF_KEY_ALARM_TIME_OFFSET_DAYS = "showdays";
    public static final boolean PREF_DEF_ALARM_TIME_OFFSET_DAYS = false;

    private long offset = PREF_DEF_ALARM_TIME_OFFSET;

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an Dialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_alarmoffset, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.alarmoffset_dialog_title));

        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        AlertDialog.setButton(dialog, AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.alarmoffset_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (onCanceled != null) {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        AlertDialog.setButton(dialog, AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.alarmoffset_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (onAccepted != null) {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        AlertDialog.setButton(dialog, AlertDialog.BUTTON_NEUTRAL, getString(R.string.configAction_clearOffset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { /* EMPTY */ }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface d)
            {
                Button button = AlertDialog.getButton(dialog, DialogInterface.BUTTON_NEUTRAL);
                if (button == null) {
                    return;
                }
                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        if (offset == 0) {
                            dialog.dismiss();
                            if (onAccepted != null) {
                                onAccepted.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
                            }
                        } else setOffset(0);
                    }
                });
            }
        });

        initLocale(myParent);
        initViews(myParent, dialogContent);
        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        updateViews(getContext());
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    private NumberPicker pickerDirection;
    private NumberPicker pickerOffsetMinutes, pickerOffsetHours, pickerOffsetDays;

    private static String[] minuteStrings = new String[] {"  ", "1m", "5m", "10m", "15m", "20m", "25m", "30m", "35m", "40m", "45m", "50m", "55m"};
    private static final int[] minuteValues = new int[] {0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};

    private static String[] hourStrings = new String[] {"  ", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h", "22h"};
    private static final int[] hourValues = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22};

    private static String[] dayStrings = new String[] {"  ", "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "10d", "11d", "12d", "13d", "14d", "15d", "16d", "17d", "18d", "19d"};
    private static final int[] dayValues = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};

    protected void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        SuntimesUtils utils = new SuntimesUtils();

        minuteStrings = new String[minuteValues.length];
        minuteStrings[0] = " ";
        for (int i=1; i<minuteValues.length; i++) {
            minuteStrings[i] = utils.timeDeltaLongDisplayString(minuteValues[i] * 1000 * 60);
        }

        hourStrings = new String[hourValues.length];
        hourStrings[0] = " ";
        for (int i=1; i<hourValues.length; i++) {
            hourStrings[i] = utils.timeDeltaLongDisplayString(hourValues[i] * 1000 * 60 * 60);
        }

        dayStrings = new String[dayValues.length];
        dayStrings[0] = " ";
        for (int i=1; i<dayValues.length; i++) {
            dayStrings[i] = utils.timeDeltaLongDisplayString(dayValues[i] * 1000 * 60 * 60 * 24);
        }
    }

    protected void initViews( final Context context, View dialogContent )
    {
        pickerDirection = (NumberPicker) dialogContent.findViewById(R.id.alarmOption_offset_direction);
        pickerDirection.setMinValue(0);
        pickerDirection.setMaxValue(1);
        pickerDirection.setDisplayedValues( new String[] {context.getString(R.string.offset_button_before), context.getString(R.string.offset_button_after)} );
        pickerDirection.setOnValueChangedListener(onOffsetChanged);

        pickerOffsetMinutes = (NumberPicker) dialogContent.findViewById(R.id.alarmOption_offset_minute);
        pickerOffsetMinutes.setMinValue(0);
        pickerOffsetMinutes.setMaxValue(minuteStrings.length-1);
        pickerOffsetMinutes.setDisplayedValues(minuteStrings);
        pickerOffsetMinutes.setOnValueChangedListener(onOffsetChanged);

        pickerOffsetHours = (NumberPicker) dialogContent.findViewById(R.id.alarmOption_offset_hour);
        pickerOffsetHours.setMinValue(0);
        pickerOffsetHours.setMaxValue(hourStrings.length-1);
        pickerOffsetHours.setDisplayedValues(hourStrings);
        pickerOffsetHours.setOnValueChangedListener(onOffsetChanged);

        pickerOffsetDays = (NumberPicker) dialogContent.findViewById(R.id.alarmOption_offset_day);
        pickerOffsetDays.setMinValue(0);
        pickerOffsetDays.setMaxValue(dayStrings.length-1);
        pickerOffsetDays.setDisplayedValues(dayStrings);
        pickerOffsetDays.setOnValueChangedListener(onOffsetChanged);
    }

    private NumberPicker.OnValueChangeListener onOffsetChanged = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            offset = determineOffset();
        }
    };
    private long determineOffset()
    {
        if (pickerDirection != null && pickerOffsetHours != null && pickerOffsetMinutes != null && pickerOffsetDays != null)
        {
            int direction = (pickerDirection.getValue() == 0) ? -1 : 1;
            int minutes = minuteValues[pickerOffsetMinutes.getValue()];
            int hours = hourValues[pickerOffsetHours.getValue()];
            int days = dayValues[pickerOffsetDays.getValue()];
            return direction * ((minutes * 60 * 1000) + (hours * 60 * 60 * 1000) + (days * 24 * 60 * 60 * 1000));

        } else {
            return 0;
        }
    }

    private int findMinutesValue(int minuteValue)
    {
        int closestIndex = 0;
        int diff, smallestDiff = Integer.MAX_VALUE;
        for (int i=0; i<minuteValues.length; i++)
        {
            if ((diff = Math.abs(minuteValue - minuteValues[i])) < smallestDiff) {
                smallestDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private void updateViews(Context context)
    {
        boolean isBefore = (offset <= 0);
        long offset0 = Math.abs(offset);
        int d = (int)(offset0) / 1000 / 60 / 60 / 24;
        int h = (int)((offset0 / 1000) / 60 / 60) % 24;
        int m = (int)(offset0 / 1000 / 60) % 60;

        if (pickerDirection != null) {
            pickerDirection.setValue( isBefore ? 0 : 1 );
        }
        if (pickerOffsetDays != null)
        {
            pickerOffsetDays.setValue(showDays ? d : 0);
            pickerOffsetDays.setVisibility(showDays ? View.VISIBLE : View.GONE);
        }
        if (pickerOffsetHours != null) {
            pickerOffsetHours.setValue(h);
        }
        if (pickerOffsetMinutes != null)
        {
            pickerOffsetMinutes.setValue( (m == 0 || m == 1) ? m : findMinutesValue(m) );
            pickerOffsetMinutes.setVisibility(showDays ? View.GONE : View.VISIBLE);
        }
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
        updateViews(getContext());
    }

    public long getOffset()
    {
        determineOffset();
        return offset;
    }

    protected void loadSettings(Bundle bundle)
    {
        this.offset = bundle.getLong(PREF_KEY_ALARM_TIME_OFFSET, PREF_DEF_ALARM_TIME_OFFSET);
        this.showDays = bundle.getBoolean(PREF_KEY_ALARM_TIME_OFFSET_DAYS, PREF_DEF_ALARM_TIME_OFFSET_DAYS);
    }

    protected void saveSettings(Bundle bundle)
    {
        bundle.putLong(PREF_KEY_ALARM_TIME_OFFSET, offset);
        bundle.putBoolean(PREF_KEY_ALARM_TIME_OFFSET_DAYS, showDays);
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    private boolean showDays = false;
    public boolean showDays() {
        return showDays;
    }
    public void setShowDays(boolean value)
    {
        showDays = value;
    }

}