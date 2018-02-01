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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AlarmDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_LASTCHOICE = "alarmdialog_lastchoice";
    public static final SolarEvents PREF_DEF_ALARM_LASTCHOICE = SolarEvents.SUNRISE;

    protected static final SuntimesUtils utils = new SuntimesUtils();

    /**
     * The appWidgetID used when saving/loading choice to prefs (main app uses 0).
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public int getAppWidgetId() { return appWidgetId; }
    public void setAppWidgetId(int value) { appWidgetId = value; }

    /**
     * The supporting dataset.
     */
    private SuntimesRiseSetDataset dataset;
    public SuntimesRiseSetDataset getData() { return dataset; }
    public void setData( SuntimesRiseSetDataset dataset) { this.dataset = dataset; }

    /**
     * The user's alarm choice.
     */
    private SolarEvents choice = null;
    public void setChoice( SolarEvents choice )
    {
        if (choice != null)
        {
            this.choice = choice;
            if (spinner_scheduleMode != null)
            {
                spinner_scheduleMode.setSelection(choice.ordinal());
            }
        }
    }
    public SolarEvents getChoice() { return choice; }

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an AlarmDialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_schedalarm, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.schedalarm_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.schedalarm_dialog_cancel),
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.schedalarm_dialog_ok),
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
            //Log.d("DEBUG", "AlarmDialog onCreate (restoreState)");
            loadSettings(savedInstanceState);

        } else {
            //Log.d("DEBUG", "AlarmDialog onCreate (newState)");
            loadSettings(myParent);
        }
        return dialog;
    }

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //Log.d("DEBUG", "AlarmDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     *
     */
    private Spinner spinner_scheduleMode;
    private TextView txt_note;
    private ImageView icon_note;

    protected void initViews( final Context context, View dialogContent )
    {
        initColors(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);

        icon_note = (ImageView) dialogContent.findViewById(R.id.appwidget_schedalarm_note_icon);
        icon_note.setVisibility(View.GONE);

        txt_note = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_note);
        txt_note.setText("");

        spinner_scheduleMode = (Spinner) dialogContent.findViewById(R.id.appwidget_schedalarm_mode);
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

                        if (alarmCalendar != null)
                        {
                            String timeString =" " + utils.timeDeltaDisplayString(now.getTime(), alarmCalendar.getTime()).getValue() + " ";
                            String noteString = context.getString(R.string.schedalarm_dialog_note, timeString);
                            txt_note.setText(SuntimesUtils.createBoldColorSpan(noteString, timeString, color_textTimeDelta));
                            icon_note.setVisibility(View.GONE);
                            SuntimesUtils.announceForAccessibility(txt_note, context.getString(R.string.configLabel_schedalarm_mode) + " " + choice.getLongDisplayString() + ", " + txt_note.getText());

                        } else {
                            String timeString = " " + choice.getLongDisplayString() + " ";
                            String noteString = context.getString(R.string.schedalarm_dialog_note2, timeString);
                            txt_note.setText(SuntimesUtils.createBoldColorSpan(noteString, timeString, color_textTimeDelta));
                            icon_note.setVisibility(View.VISIBLE);
                            SuntimesUtils.announceForAccessibility(txt_note, choice.getLongDisplayString() + ", " + txt_note.getText());
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                }
        );
    }

    private int color_textTimeDelta;
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = Color.WHITE;

        color_textTimeDelta = ContextCompat.getColor(context, typedArray.getResourceId(0, def));

        typedArray.recycle();
    }

    protected void loadSettings(Context context)
    {
        loadSettings(context, false);
    }
    protected void loadSettings(Context context, boolean overwriteCurrent)
    {
        if (overwriteCurrent || choice == null)
        {
            SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
            String choiceString = prefs.getString(PREF_KEY_ALARM_LASTCHOICE, PREF_DEF_ALARM_LASTCHOICE.name());
            try
            {
                choice = SolarEvents.valueOf(choiceString);
            } catch (IllegalArgumentException e) {
                choice = PREF_DEF_ALARM_LASTCHOICE;
            }
        }
        setChoice(choice);
    }
    protected void loadSettings(Bundle bundle)
    {
        String choiceString = bundle.getString(PREF_KEY_ALARM_LASTCHOICE);
        if (choiceString != null)
        {
            try {
                choice = SolarEvents.valueOf(choiceString);
            } catch (IllegalArgumentException e) {
                choice = PREF_DEF_ALARM_LASTCHOICE;
            }
        } else {
            choice = PREF_DEF_ALARM_LASTCHOICE;
        }
        setChoice(choice);
    }

    /**
     * Save alarm choice to prefs.
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        prefs.putString(PREF_KEY_ALARM_LASTCHOICE, choice.name());
        prefs.apply();
    }

    /**
     * Save alarm choice to bundle.
     * @param bundle state persisted to this bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putString(PREF_KEY_ALARM_LASTCHOICE, choice.name());
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

    /**
     * @param choice a SolarEvent "alarm choice"
     * @param now a Calendar representing "right now"
     * @return a Calendar representing the alarm selection
     */
    public Calendar getCalendarForAlarmChoice( SolarEvents choice, Calendar now )
    {
        Date time = now.getTime();
        Calendar calendar;
        switch (choice)
        {
            case MORNING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunriseCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunriseCalendarOther();
                }
                break;
            case MORNING_NAUTICAL:
                calendar = dataset.dataNautical.sunriseCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunriseCalendarOther();
                }
                break;
            case MORNING_CIVIL:
                calendar = dataset.dataCivil.sunriseCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunriseCalendarOther();
                }
                break;
            case NOON:
                calendar = dataset.dataNoon.sunriseCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNoon.sunriseCalendarOther();
                }
                break;
            case SUNSET:
                calendar = dataset.dataActual.sunsetCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunsetCalendarOther();
                }
                break;
            case EVENING_CIVIL:
                calendar = dataset.dataCivil.sunsetCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataCivil.sunsetCalendarOther();
                }
                break;
            case EVENING_NAUTICAL:
                calendar = dataset.dataNautical.sunsetCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataNautical.sunsetCalendarOther();
                }
                break;
            case EVENING_ASTRONOMICAL:
                calendar = dataset.dataAstro.sunsetCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataAstro.sunsetCalendarOther();
                }
                break;
            case SUNRISE:
            default:
                calendar = dataset.dataActual.sunriseCalendarToday();
                if (calendar != null && time.after(calendar.getTime()))
                {
                    calendar = dataset.dataActual.sunriseCalendarOther();
                }
                break;
        }
        return calendar;
    }

    /**
     * Schedule the selected alarm on click.
     */
    public DialogInterface.OnClickListener scheduleAlarmClickListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            SolarEvents choice = getChoice();
            String alarmLabel = choice.getShortDisplayString();
            Calendar now = dataset.now();
            Calendar calendar = getCalendarForAlarmChoice(choice, now);
            if (calendar != null)
            {
                AlarmDialog.scheduleAlarm(getActivity(), alarmLabel, calendar);

            } else {
                String alarmErrorTxt = getString(R.string.schedalarm_dialog_error) + "\n" + getString(R.string.schedalarm_dialog_note2, choice.getLongDisplayString());
                Toast alarmError = Toast.makeText(getActivity(), alarmErrorTxt, Toast.LENGTH_LONG);
                alarmError.show();
            }
        }
    };

    public static void scheduleAlarm(Activity context, String label, Calendar calendar)
    {
        if (calendar == null)
            return;

        Calendar alarm = new GregorianCalendar(TimeZone.getDefault());
        alarm.setTimeInMillis(calendar.getTimeInMillis());
        int hour = alarm.get(Calendar.HOUR_OF_DAY);
        int minutes = alarm.get(Calendar.MINUTE);

        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, label);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);

        if (alarmIntent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(alarmIntent);
        }
    }

    /**
     * @param context a context used to start the "show alarm" intent
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void showAlarms(Activity context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Intent alarmsIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            alarmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (alarmsIntent.resolveActivity(context.getPackageManager()) != null)
            {
                context.startActivity(alarmsIntent);
            }
        }
    }

}
