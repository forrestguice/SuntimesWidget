/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimeswidget.timepicker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;

/**
 * A "time selection" bottom sheet dialog.
 */
public class TimeDialog extends TimeDialogBase
{
    public static final String KEY_DIALOG_HOUR = "dialog_hour";
    public static final String KEY_DIALOG_MINUTE = "dialog_minute";

    protected TimePicker timePicker;

    public TimeDialog() {
        super();
    }

    @Override
    protected int getDialogLayoutResID() {
        return R.layout.layout_dialog_time;
    }
    protected int getTimePickerResID() {
        return R.id.pick_time;
    }

    protected void initViews(Context context, View dialogContent)
    {
        super.initViews(context, dialogContent);
        timePicker = (TimePicker) dialogContent.findViewById(getTimePickerResID());
        if (timePicker != null)
        {
            timePicker.setIs24HourView(timeIs24());
            setSelectedTime(getInitialHour(), getInitialMinute());
        }
    }

    @Override
    public void loadSettings(@Nullable Activity activity)
    {
        if (activity != null)
        {
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            int hour = prefs.getInt(KEY_DIALOG_HOUR, -1);
            int minute = prefs.getInt(KEY_DIALOG_MINUTE, -1);

            if (hour != -1) {
                getArgs().putInt(KEY_DIALOG_HOUR, hour);
            }
            if (minute != -1) {
                getArgs().putInt(KEY_DIALOG_MINUTE, minute);
            }

            if (timePicker != null && hour != -1 && minute != -1) {
                setSelectedTime(hour, minute);
            }
        }
    }

    @Override
    public void saveSettings(@Nullable Activity activity)
    {
        if (activity != null) {
            SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
            prefs.putInt(KEY_DIALOG_HOUR, getSelectedHour());
            prefs.putInt(KEY_DIALOG_MINUTE, getSelectedMinute());
            prefs.apply();
        }
    }

    public void setSelectedTime(int hour, int minute)
    {
        if (timePicker != null)
        {
            if (Build.VERSION.SDK_INT >= 23) {
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            } else {
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);
            }
        }
    }

    public int getSelectedHour() {
        if (Build.VERSION.SDK_INT >= 23) {
            return (timePicker != null ? timePicker.getHour() : getInitialHour());
        } else {
            //noinspection deprecation
            return (timePicker != null ? timePicker.getCurrentHour() : getInitialHour());
        }
    }

    public int getSelectedMinute() {
        if (Build.VERSION.SDK_INT >= 23) {
            return (timePicker != null ? timePicker.getMinute() : getInitialMinute());
        } else {
            //noinspection deprecation
            return (timePicker != null ? timePicker.getCurrentMinute() : getInitialMinute());
        }
    }

    public void setInitialTime(String hour, String minute) {
        try {
            setInitialTime(Integer.parseInt(hour), Integer.parseInt(minute));
        } catch (NumberFormatException e) {
            Log.e("TimeDialog", "setInitialTime: invalid input; " + e);
        }
    }
    public void setInitialTime(int hour, int minute) {
        getArgs().putInt(KEY_DIALOG_HOUR, hour);
        getArgs().putInt(KEY_DIALOG_MINUTE, minute);
    }
    public int getInitialHour() {
        return getArgs().getInt(KEY_DIALOG_HOUR, 12);
    }
    public int getInitialMinute() {
        return getArgs().getInt(KEY_DIALOG_MINUTE, 0);
    }

    @Override
    public TimeDialogResult getSelected()
    {
        return new TimeDialogResult()
        {
            @Override
            public Integer getYear() {
                return null;
            }
            @Override
            public Integer getMonth() {
                return null;
            }
            @Override
            public Integer getDay() {
                return null;
            }

            @Override
            public Integer getHour() {
                return getSelectedHour();
            }
            @Override
            public Integer getMinute() {
                return getSelectedMinute();
            }
            @Override
            public Integer getSecond() {
                return 0;
            }
        };
    }

}