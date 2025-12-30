/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.SwitchCompat;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.dialog.DialogBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class AlarmRepeatDialog extends DialogBase
{
    public static final String PREF_KEY_ALARM_REPEAT = "alarmrepeat_repeat";
    public static final boolean PREF_DEF_ALARM_REPEAT = false;

    public static final String PREF_KEY_ALARM_REPEATDAYS = "alarmrepeat_days";
    public static final ArrayList<Integer> PREF_DEF_ALARM_REPEATDAYS = AlarmClockItem.everyday();

    public static final String KEY_COLORS = "alarmrepeat_colors";

    protected static final SuntimesUtils utils = new SuntimesUtils();

    private SwitchCompat switchRepeat;
    private CheckBox checkRepeat;

    private boolean repeat = PREF_DEF_ALARM_REPEAT;
    private ArrayList<Integer> repeatDays = PREF_DEF_ALARM_REPEATDAYS;
    private SparseArray<ToggleButton> btnDays;

    private int[] colorOverrides = new int[] {-1, -1, -1, -1};
    public void setColorOverrides(int onColor, int offColor, int disabledColor, int pressedColor)
    {
        colorOverrides[0] = onColor;
        colorOverrides[1] = offColor;
        colorOverrides[2] = disabledColor;
        colorOverrides[3] = pressedColor;
    }

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

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        initViews(myParent, dialogContent);
        updateViews(getContext());
        return dialog;
    }

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
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
        SuntimesUtils.initDisplayStrings(context);

        if (Build.VERSION.SDK_INT >= 14)
        {
            switchRepeat = (SwitchCompat) dialogContent.findViewById(R.id.alarmOption_repeat);
            if (switchRepeat != null)
            {
                switchRepeat.setOnCheckedChangeListener(onRepeatChanged);
                if (colorOverrides[0] != -1) {
                    switchRepeat.setThumbTintList(SuntimesUtils.colorStateList(
                            colorOverrides[0],
                            colorOverrides[1],
                            colorOverrides[2],
                            colorOverrides[3]));
                    switchRepeat.setTrackTintList(SuntimesUtils.colorStateList(
                            ColorUtils.setAlphaComponent(colorOverrides[0], 85),
                            ColorUtils.setAlphaComponent(colorOverrides[1], 85),
                            ColorUtils.setAlphaComponent(colorOverrides[2], 85),
                            ColorUtils.setAlphaComponent(colorOverrides[3], 85)));  // 33% alpha (85 / 255)
                }
            }

        } else {
            checkRepeat = (CheckBox) dialogContent.findViewById(R.id.alarmOption_repeat);
            if (checkRepeat != null) {
                checkRepeat.setOnCheckedChangeListener(onRepeatChanged);
                CompoundButtonCompat.setButtonTintList(checkRepeat, SuntimesUtils.colorStateList(colorOverrides[0], colorOverrides[1], colorOverrides[2], colorOverrides[3]));
            }
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
    private final CompoundButton.OnCheckedChangeListener onRepeatChanged = new CompoundButton.OnCheckedChangeListener()
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
    private final CompoundButton.OnCheckedChangeListener onRepeatDayChanged = new CompoundButton.OnCheckedChangeListener()
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

    @Nullable
    protected static Integer tagToDay(@Nullable Object tag)
    {
        if (tag != null)
        {
            try {
                return Integer.parseInt(tag.toString());

            } catch (NumberFormatException e) {
                return null;
            }
        } else return null;
    }

    /**
     * @param value true alarm should repeat, false one-time alarm
     * @param days list of repeat days, or null (everyday)
     */
    public void setRepetition(boolean value, @Nullable ArrayList<Integer> days)
    {
        this.repeat = value;
        repeatDays = (days != null ? new ArrayList<Integer>(days) : AlarmClockItem.everyday());
        updateViews(getContext());
    }

    private void updateViews(Context context)
    {
        if (Build.VERSION.SDK_INT >= 14)
        {
            if (switchRepeat != null)
            {
                if (!repeat || repeatDays == null || repeatDays.isEmpty())
                    switchRepeat.setText(context.getString(R.string.alarmOption_repeat_none));
                else switchRepeat.setText(AlarmClockItem.repeatsEveryDay(repeatDays) ? context.getString(R.string.alarmOption_repeat_all) : context.getString(R.string.alarmOption_repeat));

                switchRepeat.setOnCheckedChangeListener(null);
                switchRepeat.setChecked(this.repeat);
                switchRepeat.setOnCheckedChangeListener(onRepeatChanged);
            }

        } else {
            if (checkRepeat != null)
            {
                if (!repeat || repeatDays == null || repeatDays.isEmpty())
                    checkRepeat.setText(context.getString(R.string.alarmOption_repeat_none));
                else checkRepeat.setText(AlarmClockItem.repeatsEveryDay(repeatDays) ? context.getString(R.string.alarmOption_repeat_all) : context.getString(R.string.alarmOption_repeat));

                checkRepeat.setOnCheckedChangeListener(null);
                checkRepeat.setChecked(this.repeat);
                checkRepeat.setOnCheckedChangeListener(onRepeatChanged);
            }
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

                    } //else Log.d("DEBUG", "updateViews: missing button tag " + i);
                } //else Log.d("DEBUG", "updateViews: missing button " + i);
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
        int[] colors = bundle.getIntArray(KEY_COLORS);
        if (colors != null) {
            colorOverrides = colors;
        }
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
        bundle.putIntArray(KEY_COLORS, colorOverrides);
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