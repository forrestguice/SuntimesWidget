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
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;

public class AlarmDialog extends Dialog
{
    public static final String PREF_KEY_ALARM_LASTCHOICE = "alarmdialog_lastchoice";
    public static final SolarEvents PREF_DEF_ALARM_LASTCHOICE = SolarEvents.SUNRISE;

    protected static SuntimesUtils utils = new SuntimesUtils();

    private Activity myParent;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_scheduleMode;
    private TextView txt_note;

    private SolarEvents choice;
    private SuntimesDataset dataset;

    public AlarmDialog(Activity c, SuntimesDataset dataset)
    {
        super(c);
        myParent = c;
        this.dataset = dataset;

        setContentView(R.layout.layout_dialog_schedalarm);
        setTitle(myParent.getString(R.string.schedalarm_dialog_title));
        setCancelable(true);

        initViews(myParent);
        loadSettings(myParent);
    }

    protected void initViews( Context context )
    {
        WidgetSettings.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(myParent);

        txt_note = (TextView) findViewById(R.id.appwidget_schedalarm_note);
        txt_note.setText("");

        spinner_scheduleMode = (Spinner) findViewById(R.id.appwidget_schedalarm_mode);
        spinner_scheduleMode.setAdapter(SolarEvents.createAdapter(context));

        spinner_scheduleMode.setOnItemSelectedListener(
                new Spinner.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        final SolarEvents[] choices = SolarEvents.values();
                        choice = choices[spinner_scheduleMode.getSelectedItemPosition()];

                        Calendar now = dataset.now();
                        Calendar alarmCalendar = getCalendarForAlarmChoice(choice, now);
                        SuntimesUtils.TimeDisplayText timeString = utils.timeDeltaDisplayString(now.getTime(), alarmCalendar.getTime());
                        txt_note.setText( myParent.getString(R.string.schedalarm_dialog_note, timeString.getValue()) );
                    }

                    public void onNothingSelected(AdapterView<?> parent) { }
                }
        );
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

    public SolarEvents getChoice()
    {
        return choice;
    }

    public void onPrepareDialog()
    {
    }

    protected void loadSettings(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String choiceString = prefs.getString(PREF_KEY_ALARM_LASTCHOICE, PREF_DEF_ALARM_LASTCHOICE.name());
        try {
            choice = SolarEvents.valueOf(choiceString);
        } catch (IllegalArgumentException e) {
            choice = PREF_DEF_ALARM_LASTCHOICE;
        }
        spinner_scheduleMode.setSelection(choice.ordinal());
    }

    protected void saveSettings(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        prefs.putString(PREF_KEY_ALARM_LASTCHOICE, choice.name());
        prefs.apply();
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

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.schedalarm_dialog_cancel),
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.schedalarm_dialog_ok),
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
            public void onDismiss(DialogInterface dialogInterface)
            {
            }
        });

        return dialog;
    }

    public Calendar getCalendarForAlarmChoice( SolarEvents choice, Calendar now )
    {
        Date time = now.getTime();
        Calendar calendar;
        switch (choice)
        {
            case MORNING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunriseCalendarOther();
                }
                break;
            case MORNING_NAUTICAL:
                calendar = dataset.dataNautical.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunriseCalendarOther();
                }
                break;
            case MORNING_CIVIL:
                calendar = dataset.dataCivil.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunriseCalendarOther();
                }
                break;
            case NOON:
                calendar = dataset.dataNoon.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNoon.sunriseCalendarOther();
                }
                break;
            case SUNSET:
                calendar = dataset.dataActual.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunsetCalendarOther();
                }
                break;
            case EVENING_CIVIL:
                calendar = dataset.dataCivil.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunsetCalendarOther();
                }
                break;
            case EVENING_NAUTICAL:
                calendar = dataset.dataNautical.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunsetCalendarOther();
                }
                break;
            case EVENING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunsetCalendarOther();
                }
                break;
            case SUNRISE:
            default:
                calendar = dataset.dataActual.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunriseCalendarOther();
                }
                break;
        }
        return calendar;
    }

}
