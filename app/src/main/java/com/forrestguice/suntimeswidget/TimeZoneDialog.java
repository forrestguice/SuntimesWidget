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
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.TimeZone;

public class TimeZoneDialog extends DialogFragment
{
    public static final String KEY_TIMEZONE_MODE = "timezoneMode";
    public static final String KEY_TIMEZONE_ID = "timezoneID";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String customTimezoneID;

    private Spinner spinner_timezoneMode;
    private TextView label_timezone;
    private Spinner spinner_timezone;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        View dialogContent = inflater.inflate(R.layout.layout_dialog_timezone, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        builder.setTitle(myParent.getString(R.string.timezone_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.timezone_dialog_cancel),
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.timezone_dialog_ok),
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
        /**dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface) {}
        });*/
        /**dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {}
        });*/

        initViews(myParent, dialogContent);
        if (savedInstanceState != null)
        {
            // saved dialog state; restore it
            Log.d("DEBUG", "TimeZoneDialog onCreate (restoreState)");
            loadSettings(savedInstanceState);

        } else {
            // no saved dialog state; load from preferences
            Log.d("DEBUG", "TimeZoneDialog onCreate (newState)");
            loadSettings(myParent);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        Log.d("DEBUG", "TimeZoneDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void initViews( Context context, View dialogContent )
    {
        WidgetSettings.initDisplayStrings(context);

        TextView groupTitle = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_grouptitle);
        groupTitle.setVisibility(View.GONE);

        label_timezone = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_custom_label);

        ArrayAdapter<WidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, WidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner) dialogContent.findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
        spinner_timezoneMode.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
                                                       {
                                                           public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                                                           {
                                                               final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
                                                               WidgetSettings.TimezoneMode timezoneMode = timezoneModes[parent.getSelectedItemPosition()];
                                                               setUseCustomTimezone((timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE));
                                                           }

                                                           public void onNothingSelected(AdapterView<?> parent)
                                                           {
                                                           }
                                                       }
        );

        WidgetTimezones.TimeZoneItemAdapter spinner_timezoneAdapter;
        spinner_timezoneAdapter = new WidgetTimezones.TimeZoneItemAdapter(context, R.layout.layout_listitem_twoline, WidgetTimezones.getValues() );

        spinner_timezone = (Spinner) dialogContent.findViewById(R.id.appwidget_timezone_custom);
        spinner_timezone.setAdapter(spinner_timezoneAdapter);
    }

    private void setUseCustomTimezone( boolean value )
    {
        String timezoneID = (value ? customTimezoneID : TimeZone.getDefault().getID());
        spinner_timezone.setSelection(WidgetTimezones.ordinal(timezoneID), true);

        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
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

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context
     */
    protected void loadSettings(Context context)
    {
        WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        customTimezoneID = WidgetSettings.loadTimezonePref(context, appWidgetId);
        int timezonePos = WidgetTimezones.ordinal(customTimezoneID);
        int numTimeZones = WidgetTimezones.values().length;

        if (timezonePos >= 0 && timezonePos < numTimeZones)
        {
            spinner_timezone.setSelection(timezonePos);
        } else {
            spinner_timezone.setSelection(0);
            Log.w("loadTimezoneSettings", "unable to find timezone " + customTimezoneID + " in the list! Setting selection to 0.");
        }
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle
     */
    protected void loadSettings(Bundle bundle)
    {
        String modeString = bundle.getString(KEY_TIMEZONE_MODE);
        if (modeString != null)
        {
            WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.TimezoneMode.valueOf(modeString);
            spinner_timezoneMode.setSelection(timezoneMode.ordinal());
        }

        customTimezoneID = bundle.getString(KEY_TIMEZONE_ID);
        if (customTimezoneID != null)
        {
            int timezonePos = WidgetTimezones.ordinal(customTimezoneID);
            int numTimeZones = WidgetTimezones.values().length;
            if (timezonePos >= 0 && timezonePos < numTimeZones)
            {
                spinner_timezone.setSelection(timezonePos);
            } else {
                spinner_timezone.setSelection(0);
                Log.w("loadTimezoneSettings", "unable to find timezone " + customTimezoneID + " in the list! Setting selection to 0.");
            }
        }
    }

    /**
     * Save the dialog state to preferences to be used by the app (occurs on dialog accept).
     * @param context
     */
    protected void saveSettings(Context context)
    {
        final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        WidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        // save: timezone mode
        WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        bundle.putString(KEY_TIMEZONE_MODE, timezoneMode.name());

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        bundle.putString(KEY_TIMEZONE_ID, customTimezone.getID());
    }

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

}
