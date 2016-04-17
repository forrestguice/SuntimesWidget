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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.TimeZone;

public class TimeZoneDialog extends Dialog
{
    private Activity myParent;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_timezoneMode;
    private TextView label_timezone;
    private Spinner spinner_timezone;
    private String customTimezoneID;

    public TimeZoneDialog(Activity c)
    {
        super(c);
        myParent = c;
        setContentView(R.layout.layout_dialog_timezone);
        setTitle(myParent.getString(R.string.timezone_dialog_title));
        setCancelable(true);

        initViews(myParent);
        loadSettings(myParent);
    }

    protected void initViews( Context context )
    {
        WidgetSettings.initDisplayStrings(context);

        TextView groupTitle = (TextView)findViewById(R.id.appwidget_timezone_grouptitle);
        groupTitle.setVisibility(View.GONE);

        label_timezone = (TextView) findViewById(R.id.appwidget_timezone_custom_label);

        ArrayAdapter<WidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, WidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner) findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
        spinner_timezoneMode.setOnItemSelectedListener( new Spinner.OnItemSelectedListener()
        {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
                    WidgetSettings.TimezoneMode timezoneMode = timezoneModes[ parent.getSelectedItemPosition() ];
                    setUseCustomTimezone( (timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE) );
                }

                public void onNothingSelected(AdapterView<?> parent) {}
            }
        );

        WidgetTimezones.TimeZoneItemAdapter spinner_timezoneAdapter;
        spinner_timezoneAdapter = new WidgetTimezones.TimeZoneItemAdapter(context, R.layout.layout_listitem_twoline, WidgetTimezones.getValues() );

        spinner_timezone = (Spinner) findViewById(R.id.appwidget_timezone_custom);
        spinner_timezone.setAdapter(spinner_timezoneAdapter);
    }

    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
        loadSettings(myParent);
    }

    private void setUseCustomTimezone( boolean value )
    {
        String timezoneID = (value ? customTimezoneID : TimeZone.getDefault().getID());
        spinner_timezone.setSelection(WidgetTimezones.ordinal(timezoneID), true);

        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
    }

    public void onPrepareDialog()
    {
    }

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

    protected void saveSettings(Context context)
    {
        // save: timezone mode
        final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[ spinner_timezoneMode.getSelectedItemPosition() ];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem)spinner_timezone.getSelectedItem();
        WidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());
    }

    private OnClickListener onAccepted = null;
    public void setOnAcceptedListener( OnClickListener listener )
    {
        onAccepted = listener;
    }

    private OnClickListener onCanceled = null;
    public void setOnCanceledListener( OnClickListener listener )
    {
        onCanceled = listener;
    }

    public AlertDialog toAlertDialog()
    {
        ViewGroup dialogFrame = (ViewGroup)this.getWindow().getDecorView();
        View dialogContent = dialogFrame.getChildAt(0);
        dialogFrame.removeView(dialogContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.timezone_dialog_cancel),
                new OnClickListener()
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
                new OnClickListener()
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

        dialog.setOnShowListener(new OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                loadSettings(myParent);
            }
        });

        dialog.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface) { }
        });

        return dialog;
    }

}
