/**
    Copyright (C) 2018 Forrest Guice
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
import android.widget.Button;
import android.widget.DatePicker;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class TimeDateDialogEasy extends DialogFragment
{
    public static final String KEY_TIMEDATE_APPWIDGETID = "appwidgetid";

    private DatePicker picker;

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
        picker.init(year, month, day, null);
    }

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    private void initViews(Context context, View dialogContent)
    {
        picker = (DatePicker) dialogContent.findViewById(R.id.appwidget_date_custom);
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
        View dialogContent = inflater.inflate(R.layout.layout_dialog_date1, null);

        Resources r = getResources();
        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.timedate_dialog_title));
        builder.setNeutralButton(getString(R.string.today), null);

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

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(final DialogInterface dialog)
            {
                Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        boolean alreadyToday = isToday();
                        init(Calendar.getInstance());
                        if (alreadyToday) {
                            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                        }
                    }
                });
            }
        });

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
    }

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        WidgetSettings.DateMode mode = WidgetSettings.loadDateModePref(context, appWidgetId);
        if (mode == WidgetSettings.DateMode.CURRENT_DATE)
        {
            init(Calendar.getInstance());

        } else {
            WidgetSettings.DateInfo dateInfo = WidgetSettings.loadDatePref(context, appWidgetId);
            init(dateInfo);
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        WidgetSettings.DateMode dateMode = (isToday() ? WidgetSettings.DateMode.CURRENT_DATE : WidgetSettings.DateMode.CUSTOM_DATE);
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

    public boolean isToday()
    {
        Calendar date = Calendar.getInstance();
        return isToday(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }

    public boolean isToday(int year, int month, int day)
    {
        return (year == picker.getYear() && month == picker.getMonth() && day == picker.getDayOfMonth());
    }
}
