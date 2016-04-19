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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AlarmDialog extends Dialog
{
    public static final String PREF_KEY_ALARM_LASTCHOICE = "alarmdialog_lastchoice";
    public static final AlarmChoice PREF_DEF_ALARM_LASTCHOICE = AlarmChoice.SUNRISE;

    protected static SuntimesUtils utils = new SuntimesUtils();

    private Activity myParent;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_scheduleMode;
    private TextView txt_note;

    private AlarmChoice choice;
    private SuntimesData dataActual, dataCivil, dataNautical, dataAstro;

    public AlarmDialog(Activity c, SuntimesData dataActual, SuntimesData dataCivil, SuntimesData dataNautical, SuntimesData dataAstro)
    {
        super(c);
        myParent = c;
        this.dataActual = dataActual;
        this.dataCivil = dataCivil;
        this.dataNautical = dataNautical;
        this.dataAstro = dataAstro;

        setContentView(R.layout.layout_dialog_schedalarm);
        setTitle(myParent.getString(R.string.schedalarm_dialog_title));
        setCancelable(true);

        initViews(myParent);
        loadSettings(myParent);
    }

    protected void initViews( Context context )
    {
        WidgetSettings.initDisplayStrings(context);
        AlarmChoice.initDisplayStrings(myParent);

        txt_note = (TextView) findViewById(R.id.appwidget_schedalarm_note);
        txt_note.setText("");

        spinner_scheduleMode = (Spinner) findViewById(R.id.appwidget_schedalarm_mode);
        spinner_scheduleMode.setAdapter(AlarmChoice.createAlarmChoiceAdapter(context));

        spinner_scheduleMode.setOnItemSelectedListener(
                new Spinner.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        final AlarmChoice[] choices = AlarmChoice.values();
                        choice = choices[spinner_scheduleMode.getSelectedItemPosition()];

                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(dataActual.timezone()));
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

    public AlarmChoice getChoice()
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
            choice = AlarmChoice.valueOf(choiceString);
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

    public static enum AlarmChoice
    {
        MORNING_ASTRONOMICAL("astronomical twilight", "morning astronomical twilight", R.drawable.ic_sunrise_large),
        MORNING_NAUTICAL("nautical twilight", "morning nautical twilight", R.drawable.ic_sunrise_large),
        MORNING_CIVIL("civil twilight", "morning civil twilight", R.drawable.ic_sunrise_large),
        SUNRISE("sunrise", "sunrise", R.drawable.ic_sunrise_large),
        SUNSET("sunset", "sunset", R.drawable.ic_sunset_large),
        EVENING_CIVIL("civil twilight", "evening civil twilight", R.drawable.ic_sunset_large),
        EVENING_NAUTICAL("nautical twilight", "evening nautical twilight", R.drawable.ic_sunset_large),
        EVENING_ASTRONOMICAL("astronomical twilight", "evening astronomical twilight", R.drawable.ic_sunset_large);

        private int iconResource;
        private String shortDisplayString, longDisplayString;

        private AlarmChoice(String shortDisplayString, String longDisplayString, int iconResource)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
            this.iconResource = iconResource;
        }

        public String toString()
        {
            return longDisplayString;
        }

        public int getIcon()
        {
            return iconResource;
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayString(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            String[] modes_short = context.getResources().getStringArray(R.array.schedalarm_dialog_modes_short);
            String[] modes_long = context.getResources().getStringArray(R.array.schedalarm_dialog_modes_long);

            MORNING_ASTRONOMICAL.setDisplayString(modes_short[0], modes_long[0]);
            MORNING_NAUTICAL.setDisplayString(modes_short[1], modes_long[1]);
            MORNING_CIVIL.setDisplayString(modes_short[2], modes_long[2]);
            SUNRISE.setDisplayString(modes_short[3], modes_long[3]);
            SUNSET.setDisplayString(modes_short[4], modes_long[4]);
            EVENING_CIVIL.setDisplayString(modes_short[5], modes_long[5]);
            EVENING_NAUTICAL.setDisplayString(modes_short[6], modes_long[6]);
            EVENING_ASTRONOMICAL.setDisplayString(modes_short[7], modes_long[7]);
        }

        public static AlarmChoiceAdapter createAlarmChoiceAdapter(Context context)
        {
            ArrayList<AlarmChoice> choices = new ArrayList<>();
            for (AlarmChoice choice : AlarmChoice.values())
            {
                choices.add(choice);
            }
            return new AlarmChoiceAdapter(context, choices);
        }
    }

    public Calendar getCalendarForAlarmChoice( AlarmDialog.AlarmChoice choice, Calendar now )
    {
        Date time = now.getTime();
        Calendar calendar;
        switch (choice)
        {
            case MORNING_ASTRONOMICAL:
                calendar = dataAstro.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataAstro.sunriseCalendarOther();
                }
                break;
            case MORNING_NAUTICAL:
                calendar = dataNautical.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataNautical.sunriseCalendarOther();
                }
                break;
            case MORNING_CIVIL:
                calendar = dataCivil.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataCivil.sunriseCalendarOther();
                }
                break;
            case SUNSET:
                calendar = dataActual.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataActual.sunsetCalendarOther();
                }
                break;
            case EVENING_CIVIL:
                calendar = dataCivil.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataCivil.sunsetCalendarOther();
                }
                break;
            case EVENING_NAUTICAL:
                calendar = dataNautical.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataNautical.sunsetCalendarOther();
                }
                break;
            case EVENING_ASTRONOMICAL:
                calendar = dataAstro.sunsetCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataAstro.sunsetCalendarOther();
                }
                break;
            case SUNRISE:
            default:
                calendar = dataActual.sunriseCalendarToday();
                if (time.after(calendar.getTime()))
                {
                    calendar = dataActual.sunriseCalendarOther();
                }
                break;
        }
        return calendar;
    }

    public static class AlarmChoiceAdapter extends ArrayAdapter<AlarmChoice>
    {
        private final Context context;
        private final ArrayList<AlarmChoice> choices;

        public AlarmChoiceAdapter(Context context, ArrayList<AlarmChoice> choices)
        {
            super(context, R.layout.layout_listitem_alarmchoice, choices);
            this.context = context;
            this.choices = choices;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return alarmItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return alarmItemView(position, convertView, parent);
        }

        private View alarmItemView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.layout_listitem_alarmchoice, parent, false);

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            icon.setImageResource(choices.get(position).getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(choices.get(position).getLongDisplayString());

            return view;
        }
    }

}
