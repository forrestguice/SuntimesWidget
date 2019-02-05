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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

public class MillisecondPickerPreference extends DialogPreference
{
    public static final int MODE_MINUTES = 0;
    public static final int MODE_HOURS = 10;
    public static final int MODE_SECONDS = 20;

    public static final int MAX_HOURS = 12;
    public static final int MAX_MINUTES = 59;
    public static final int MAX_SECONDS = 59;

    private int value;
    private int[] range;
    private NumberPicker picker;
    private TextView pickerLabel;
    private boolean wrap = false;
    private int mode = MODE_MINUTES;

    public MillisecondPickerPreference(Context context)
    {
        super(context);
    }

    public MillisecondPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setMode(context, attrs);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setMode(context, attrs);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setMode(context, attrs);
    }

    @Override
    protected View onCreateDialogView()
    {
        pickerLabel = new TextView(getContext());
        picker = new NumberPicker(getContext());
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pickerLabel.setText(createSummaryString(pickerValuesToMs(newVal)));
            }
        });

        LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params0.gravity = Gravity.CENTER;
        picker.setLayoutParams(params0);

        float marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.START;
        params1.setMargins((int)marginPx * 2, (int)marginPx, (int)marginPx * 2, (int)marginPx);
        pickerLabel.setLayoutParams(params1);

        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        dialogView.setLayoutParams(layoutParams);

        dialogView.addView(pickerLabel);
        dialogView.addView(picker);
        return dialogView;
    }

    private static int SECOND_TO_MS = 1000;
    private static int MINUTE_TO_MS = 1000 * 60;
    private static int HOUR_TO_MS = 1000 * 60 * 60;

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);

        int min, max, value;
        switch (mode) {
            case MODE_SECONDS:
                min = getMin() / 1000;
                max = getMax() / 1000;
                value = getValue() / 1000;
                break;
            case MODE_HOURS:
                min = getMin() / 60 / 60 / 1000;
                max = getMax() / 60 / 60 / 1000;
                value = getValue() / 60 / 60 / 1000;
                break;
            case MODE_MINUTES:
            default:
                min = getMin() / 60 / 1000;
                max = getMax() / 60 / 1000;
                value = getValue() / 60 / 1000;
                break;
        }

        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setWrapSelectorWheel(isWrapping());
        picker.setValue(value);
        pickerLabel.setText(createSummaryString(pickerValuesToMs(value)));
    }

    protected int pickerValuesToMs(int value)
    {
        switch (mode) {
            case MODE_SECONDS:
                return value * SECOND_TO_MS;
            case MODE_HOURS:
                return value * HOUR_TO_MS;
            case MODE_MINUTES:
            default:
                return value * MINUTE_TO_MS;
        }
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        if (result)
        {
            int changedValue = pickerValuesToMs(picker.getValue());
            if (callChangeListener(changedValue)) {
                setValue(changedValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int i)
    {
        return a.getInt(i, getMin());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        setValue(restoreValue ? getPersistedInt(getMin()) : (Integer)defaultValue);
    }

    private void initRange()
    {
        switch (mode)
        {
            case MODE_HOURS:
                range = new int[] {HOUR_TO_MS, MAX_HOURS * HOUR_TO_MS};   // [1, 12] hours
                break;
            case MODE_SECONDS:
                range = new int[] {SECOND_TO_MS, MAX_SECONDS * SECOND_TO_MS};   // [1, 59] seconds
                break;
            case MODE_MINUTES:
            default:
                range = new int[] {MINUTE_TO_MS, MAX_MINUTES * MINUTE_TO_MS};   // [1, 59] minutes
                break;
        }
    }

    public void setRange(int minValue, int maxValue)
    {
        initRange();
        range[0] = minValue;
        range[1] = maxValue;
    }
    public int getMin()
    {
        initRange();
        return range[0];
    }
    public int getMax()
    {
        initRange();
        return range[1];
    }

    public void setWrapping(boolean value)
    {
        this.wrap = value;
    }
    public boolean isWrapping()
    {
        return this.wrap;
    }

    public void setValue(int value)
    {
        this.value = value;
        persistInt(this.value);
        updateSummary();
    }
    public int getValue()
    {
        return this.value;
    }

    public void setMode(Context context, AttributeSet attrs )
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MillisecondPickerPreference, 0, 0);
        try {
            this.mode = a.getInt(R.styleable.MillisecondPickerPreference_mode, MODE_MINUTES);
        } finally {
            a.recycle();
        }
    }
    public int getMode()
    {
        return mode;
    }

    private String createSummaryString(int value)
    {
        Context context = getContext();
        int valueSeconds = (value / 1000);
        int valueMinutes = (valueSeconds / 60);
        int valueHours = (valueMinutes / 60);

        switch (mode)
        {
            case MODE_HOURS:
                if (context != null) {
                    return context.getResources().getQuantityString(R.plurals.units_hours, valueHours, valueHours);
                } else return valueHours + "";

            case MODE_SECONDS:
                if (context != null) {
                    return context.getResources().getQuantityString(R.plurals.units_seconds, valueSeconds, valueSeconds);
                } else return valueSeconds + "";

            case MODE_MINUTES:
            default:
                if (context != null) {
                    return context.getResources().getQuantityString(R.plurals.units_minutes, valueMinutes, valueMinutes);
                } else return valueMinutes + "";
        }
    }

    private void updateSummary()
    {
        setSummary(createSummaryString(getValue()));
    }
}
