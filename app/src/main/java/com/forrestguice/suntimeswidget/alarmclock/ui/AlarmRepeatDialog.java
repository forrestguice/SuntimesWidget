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
package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class AlarmRepeatDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_REPEAT = "alarmrepeat_repeat";
    public static final boolean PREF_DEF_ALARM_REPEAT = false;

    public static final String PREF_KEY_ALARM_REPEATDAYS = "alarmrepeat_days";
    public static final ArrayList<Integer> PREF_DEF_ALARM_REPEATDAYS = new ArrayList<>(Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY));

    protected static final SuntimesUtils utils = new SuntimesUtils();

    private Switch switchRepeat;
    private boolean repeat = PREF_DEF_ALARM_REPEAT;
    private ArrayList<Integer> repeatDays = PREF_DEF_ALARM_REPEATDAYS;
    private SparseArray<ToggleButton> btnDays;

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
        View dialogContent = inflater.inflate(R.layout.layout_dialog_alarmrepeat, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        //builder.setTitle(myParent.getString(R.string.alarmrepeat_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.alarmrepeat_dialog_cancel),
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.alarmrepeat_dialog_ok),
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

        initViews(myParent, dialogContent);
        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        updateViews(getContext());
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
    protected void initViews( final Context context, View dialogContent )
    {
        initColors(context);
        SuntimesUtils.initDisplayStrings(context);

        switchRepeat = (Switch) dialogContent.findViewById(R.id.alarmOption_repeat);
        if (switchRepeat != null) {
            switchRepeat.setOnCheckedChangeListener(onRepeatChanged);
        }

        btnDays = new SparseArray<>();
        btnDays.put(Calendar.SUNDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_sun));
        btnDays.put(Calendar.MONDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_mon));
        btnDays.put(Calendar.TUESDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_tue));
        btnDays.put(Calendar.WEDNESDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_wed));
        btnDays.put(Calendar.THURSDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_thu));
        btnDays.put(Calendar.FRIDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_fri));
        btnDays.put(Calendar.SATURDAY, (ToggleButton)dialogContent.findViewById(R.id.alarmOption_repeat_sat));

        int n = btnDays.size();
        for (int i=0; i<n; i++)
        {
            int day = btnDays.keyAt(i);
            ToggleButton button = btnDays.get(day);
            if (button != null)
            {
                button.setOnCheckedChangeListener(onRepeatDayChanged);
                String dayName = utils.getShortDayString(context, day);
                button.setTextOn(dayName);
                button.setTextOff(dayName);
            }
        }
    }

    public static String getDisplayString(Context context, ArrayList<Integer> days)
    {
        StringBuilder retString = new StringBuilder();
        if (days != null && days.size() > 0)
        {
            if (days.size() == 1)
            {
                retString.append(utils.getDayString(context, days.get(0)));

            } else {
                String[] dayStrings = utils.getShortDayStrings(context);
                Collections.sort(days);
                int n = days.size();
                for (int i=0; i<n; i++)
                {
                    int day = days.get(i);
                    retString.append(dayStrings[day]);

                    boolean isLast = (i == (n-1));
                    if (!isLast) {
                        retString.append(", ");
                    }
                }
            }
        }
        return retString.toString();
    }

    /**
     * onRepeatChanged
     */
    private CompoundButton.OnCheckedChangeListener onRepeatChanged = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            repeat = isChecked;
            repeatDays = (repeat ? new ArrayList<Integer>(PREF_DEF_ALARM_REPEATDAYS) : new ArrayList<Integer>());
            updateViews(getContext());
        }
    };

    /**
     * onRepeatDayChanged
     */
    private CompoundButton.OnCheckedChangeListener onRepeatDayChanged = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            Integer day = tagToDay(buttonView.getTag());
            if (repeatDays != null && day != null)
            {
                if (isChecked) {
                    if (!repeatDays.contains(day)) {
                        repeatDays.add(day);
                        Collections.sort(repeatDays);
                    }
                } else {
                    if (repeatDays.contains(day)) {
                        repeatDays.remove(day);
                    }
                }

                if (repeatDays.isEmpty()) {
                    repeat = false;
                }
                updateViews(getContext());
            }
        }
    };

    private Integer tagToDay(Object tag)
    {
        try {
            return Integer.parseInt(tag.toString());

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void initColors(Context context)
    {
        /**int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = Color.WHITE;
        color_textTimeDelta = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();*/
    }

    /**
     * @param value true alarm should repeat, false one-time alarm
     */
    public void setRepetition(boolean value, ArrayList<Integer> days)
    {
        this.repeat = value;
        repeatDays = (days != null ? new ArrayList<Integer>(days) : new ArrayList<Integer>());
        updateViews(getContext());
    }

    private void updateViews(Context context)
    {
        if (switchRepeat != null)
        {
            if (repeatDays == null || repeatDays.isEmpty())
                switchRepeat.setText(context.getString(R.string.alarmOption_repeat_none));
            else switchRepeat.setText(AlarmClockItem.repeatsEveryDay(repeatDays) ? context.getString(R.string.alarmOption_repeat_all) : context.getString(R.string.alarmOption_repeat));

            switchRepeat.setOnCheckedChangeListener(null);
            switchRepeat.setChecked(this.repeat);
            switchRepeat.setOnCheckedChangeListener(onRepeatChanged);
        }

        if (btnDays != null)
        {
            int n = btnDays.size();
            for (int i=0; i<n; i++)
            {
                ToggleButton button = btnDays.valueAt(i);
                if (button != null)
                {
                    Integer day = tagToDay(button.getTag());
                    if (day != null)
                    {
                        button.setChecked(repeatDays.contains(day));
                        button.setEnabled(repeat);

                    } else Log.d("DEBUG", "updateViews: missing button tag " + i);
                } else Log.d("DEBUG", "updateViews: missing button " + i);
            }
        }
    }

    /**
     * @return true alarm should repeat, false one-time alarm
     */
    public boolean getRepetition()
    {
        return repeat;
    }

    /**
     * @return a list of days (e.g. [Calendar.SUNDAY, Calendar.MONDAY, ...]); null or empty means no-repeat
     */
    public ArrayList<Integer> getRepetitionDays()
    {
        return repeatDays;
    }

    /**
     * Load alarm repetition from bundle.
     * @param bundle saved state
     */
    protected void loadSettings(Bundle bundle)
    {
        setRepetition( bundle.getBoolean(PREF_KEY_ALARM_REPEAT, PREF_DEF_ALARM_REPEAT),
                       bundle.getIntegerArrayList(PREF_KEY_ALARM_REPEATDAYS) );
    }

    /**
     * Save alarm repetition to bundle.
     * @param bundle state persisted to this bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putBoolean(PREF_KEY_ALARM_REPEAT, repeat);
        bundle.putIntegerArrayList(PREF_KEY_ALARM_REPEATDAYS, repeatDays);
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

}