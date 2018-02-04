/**
    Copyright (C) 2014-2018 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;


@SuppressWarnings("Convert2Diamond")
public class TimeDateDialog extends DialogFragment implements DatePicker.OnDateChangedListener
{
    public static final String KEY_TIMEDATE_MODE = "dateMode";
    public static final String KEY_TIMEDATE_YEAR = "year";
    public static final String KEY_TIMEDATE_MONTH = "month";
    public static final String KEY_TIMEDATE_DAY = "day";
    public static final String KEY_TIMEDATE_APPWIDGETID = "appwidgetid";

    private int year = -1, month = -1, day = -1;
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getday() { return day; }

    private Spinner spinner_dateMode;
    private ScrollView scroll;
    private DatePicker picker;

    public boolean isInitialized()
    {
        return (year != -1 && month != -1 && day == -1);
    }
    public void init()
    {
        init(Calendar.getInstance());
    }
    public void init(Calendar date)
    {
        init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    public void init(WidgetSettings.DateInfo info)
    {
        init(info.getYear(), info.getMonth(), info.getDay());
    }
    public void init(int year, int month, int day)
    {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     *
     */
    Spinner.OnItemSelectedListener dateModeSelectedListener = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            final WidgetSettings.DateMode[] dateModes = WidgetSettings.DateMode.values();
            WidgetSettings.DateMode dateMode = dateModes[parent.getSelectedItemPosition()];

            if (dateMode == WidgetSettings.DateMode.CURRENT_DATE)
            {
                picker.setEnabled(false);
                init();
                picker.init(year, month, day, TimeDateDialog.this);

            } else {
                picker.setEnabled(true);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    private void initViews(Context context, View dialogContent)
    {
        ArrayAdapter<WidgetSettings.DateMode> spinner_dateModeAdapter;
        spinner_dateModeAdapter = new ArrayAdapter<WidgetSettings.DateMode>(context, R.layout.layout_listitem_oneline, WidgetSettings.DateMode.values());
        spinner_dateModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_dateMode = (Spinner) dialogContent.findViewById(R.id.appwidget_date_mode);
        spinner_dateMode.setAdapter(spinner_dateModeAdapter);
        spinner_dateMode.setOnItemSelectedListener(dateModeSelectedListener);

        picker = (DatePicker) dialogContent.findViewById(R.id.appwidget_date_custom);

        scroll = (ScrollView) dialogContent.findViewById(R.id.appwidget_date_scroll);
        scroll.scrollTo(0, 0);
    }

    /**
     *
     * @param savedInstanceState a bundle containing previously saved dialog state
     * @return a dialog instance ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);
        final FragmentActivity myParent = getActivity();

        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_date, null);

        Resources r = getResources();
        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.timedate_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(false);

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, myParent.getString(R.string.timedate_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (onCanceled != null)
                        {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, myParent.getString(R.string.timedate_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        saveSettings(myParent);
                        dialog.dismiss();
                        if (onAccepted != null)
                        {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        initViews(myParent, dialogContent);
        if (savedInstanceState != null)
        {
            //Log.d("DEBUG", "TimeDateDialog onCreate (restoreState)");
            loadSettings(savedInstanceState);

        } else {
            // no saved dialog state; load from preferences
            //Log.d("DEBUG", "TimeDateDialog onCreate (newState)");
            loadSettings(myParent);
        }

        if (!isInitialized())
        {
            init();
        }

        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //Log.d("DEBUG", "TimeDateDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle state loaded from this Bundle
     */
    protected void loadSettings(Bundle bundle)
    {
        appWidgetId = bundle.getInt(KEY_TIMEDATE_APPWIDGETID, appWidgetId);

        String modeString = bundle.getString(KEY_TIMEDATE_MODE);
        if (modeString != null)
        {
            WidgetSettings.DateMode dateMode = WidgetSettings.DateMode.valueOf(modeString);
            spinner_dateMode.setSelection(dateMode.ordinal());
        }

        year = bundle.getInt(KEY_TIMEDATE_YEAR, year);
        month = bundle.getInt(KEY_TIMEDATE_MONTH, month);
        day = bundle.getInt(KEY_TIMEDATE_DAY, day);
        picker.init(year, month, day, this);
    }

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        WidgetSettings.DateMode dateMode = WidgetSettings.loadDateModePref(context, appWidgetId);
        spinner_dateMode.setSelection(dateMode.ordinal());

        if (dateMode == WidgetSettings.DateMode.CURRENT_DATE)
        {
            init();

        } else {
            WidgetSettings.DateInfo dateInfo = WidgetSettings.loadDatePref(context, appWidgetId);
            init(dateInfo);
        }
        picker.init(year, month, day, this);
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        final WidgetSettings.DateMode[] dateModes = WidgetSettings.DateMode.values();
        WidgetSettings.DateMode dateMode = dateModes[spinner_dateMode.getSelectedItemPosition()];
        WidgetSettings.saveDateModePref(context, appWidgetId, dateMode);

        WidgetSettings.DateInfo dateInfo = new WidgetSettings.DateInfo(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
        WidgetSettings.saveDatePref(context, appWidgetId, dateInfo);
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle state persisted to this Bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putInt(KEY_TIMEDATE_APPWIDGETID, appWidgetId);

        final WidgetSettings.DateMode[] dateModes = WidgetSettings.DateMode.values();
        WidgetSettings.DateMode dateMode = dateModes[spinner_dateMode.getSelectedItemPosition()];
        bundle.putString(KEY_TIMEDATE_MODE, dateMode.name());

        bundle.putInt(KEY_TIMEDATE_YEAR, year);
        bundle.putInt(KEY_TIMEDATE_MONTH, month);
        bundle.putInt(KEY_TIMEDATE_DAY, day);
    }

    /**
     * @return the appWidgetID used by this dialog when saving/loading prefs (use 0 for main app)
     */
    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
    }
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    /**
     * A listener that is triggered when the dialog is accepted.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * A listener that is triggered when the dialog is cancelled.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    /**
     * @param datePicker a reference to the DatePicker view
     * @param year the year (as int)
     * @param month the month (0-11)
     * @param day the day of the month
     */
    @Override
    public void onDateChanged(DatePicker datePicker, int year, int month, int day)
    {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}
