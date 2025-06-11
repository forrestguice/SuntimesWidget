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
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import com.forrestguice.suntimeswidget.R;

import java.util.Calendar;

/**
 * A "date selection" bottom sheet dialog.
 */
public class DateDialog extends TimeDialogBase
{
    public static final String KEY_DIALOG_YEAR = "dialog_year";
    public static final String KEY_DIALOG_MONTH = "dialog_month";
    public static final String KEY_DIALOG_DAY = "dialog_day";

    protected DatePicker datePicker;

    public DateDialog() {
        super();
    }

    @Override
    protected int getDialogLayoutResID() {
        return R.layout.layout_dialog_date1;
    }
    protected int getDatePickerResID() {
        return R.id.appwidget_date_custom;
    }

    protected void initViews(Context context, View dialogContent)
    {
        super.initViews(context, dialogContent);
        datePicker = (DatePicker) dialogContent.findViewById(getDatePickerResID());
        if (datePicker != null) {
            datePicker.updateDate(getInitialYear(), getInitialMonth(), getInitialDay());
        }
    }

    @Override
    public void loadSettings(@Nullable Activity activity)
    {
        if (activity != null)
        {
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            int year = prefs.getInt(KEY_DIALOG_YEAR, -1);
            int month = prefs.getInt(KEY_DIALOG_MONTH, -1);
            int day = prefs.getInt(KEY_DIALOG_DAY, -1);

            if (year != -1) {
                getArgs().putInt(KEY_DIALOG_YEAR, year);
            }
            if (month != -1) {
                getArgs().putInt(KEY_DIALOG_MONTH, month);
            }
            if (day != -1) {
                getArgs().putInt(KEY_DIALOG_DAY, day);
            }
            if (datePicker != null && year != -1 && month != -1 && day != -1) {
                datePicker.updateDate(year, month, day);
            }
        }
    }

    @Override
    public void saveSettings(@Nullable Activity activity)
    {
        if (activity != null) {
            SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
            prefs.putInt(KEY_DIALOG_YEAR, getSelectedYear());
            prefs.putInt(KEY_DIALOG_MONTH, getSelectedMonth());
            prefs.putInt(KEY_DIALOG_DAY, getSelectedDay());
            prefs.apply();
        }
    }

    public int getSelectedYear() {
        return (datePicker != null ? datePicker.getYear() : getInitialYear());
    }

    public int getSelectedMonth() {
        return (datePicker != null ? datePicker.getMonth() : getInitialMonth());
    }

    public int getSelectedDay() {
        return (datePicker != null ? datePicker.getDayOfMonth() : getInitialDay());
    }

    public void setInitialDate(String year, String month, String day) {
        try {
            setInitialDate(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (NumberFormatException e) {
            Log.e("DateDialog", "setInitialDate: invalid input; " + e);
        }
    }
    public void setInitialDate(int year, int month, int day) {
        getArgs().putInt(KEY_DIALOG_YEAR, year);
        getArgs().putInt(KEY_DIALOG_MONTH, month);
        getArgs().putInt(KEY_DIALOG_DAY, day);
    }
    public int getInitialYear() {
        return getArgs().getInt(KEY_DIALOG_YEAR, Calendar.getInstance().get(Calendar.YEAR));
    }
    public int getInitialMonth() {
        return getArgs().getInt(KEY_DIALOG_MONTH, Calendar.getInstance().get(Calendar.MONTH));
    }
    public int getInitialDay() {
        return getArgs().getInt(KEY_DIALOG_DAY, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public TimeDialogResult getSelected()
    {
        return new TimeDialogResult()
        {
            @Override
            public Integer getYear() {
                return getSelectedYear();
            }
            @Override
            public Integer getMonth() {
                return getSelectedMonth();
            }
            @Override
            public Integer getDay() {
                return getSelectedDay();
            }

            @Override
            public Integer getHour() {
                return null;
            }
            @Override
            public Integer getMinute() {
                return null;
            }
            @Override
            public Integer getSecond() {
                return null;
            }
        };
    }
}