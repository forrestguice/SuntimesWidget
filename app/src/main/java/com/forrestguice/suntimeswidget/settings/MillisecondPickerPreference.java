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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

@TargetApi(11)
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
    private int param_min = 1;
    private int param_max = MAX_MINUTES;
    private String param_zeroText = null;

    @TargetApi(21)
    public MillisecondPickerPreference(Context context)
    {
        super(context);
    }

    public MillisecondPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setMode(context, attrs);
        setParamMinMax(context, attrs);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setMode(context, attrs);
        setParamMinMax(context, attrs);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setMode(context, attrs);
        setParamMinMax(context, attrs);
    }

    @Override
    protected View onCreateDialogView()
    {
        Context context = getContext();

        float marginTopBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        float marginLeftRight;
        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.dialogPreferredPadding });
        marginLeftRight = context.getResources().getDimension(a.getResourceId(0, R.dimen.settingsGroup_margin));
        a.recycle();

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

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.START;
        params1.setMargins((int)marginLeftRight, (int)marginTopBottom, (int)marginLeftRight, (int)marginTopBottom);
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

        String[] pickerDisplayValues = picker.getDisplayedValues();
        if (param_min == 0 && param_zeroText != null)
        {
            if (pickerDisplayValues != null) {
                pickerDisplayValues[0] = param_zeroText;
            } else {
                pickerDisplayValues = new String[param_max - param_min + 1];
                pickerDisplayValues[0] = param_zeroText;
                for (int i=1; i<pickerDisplayValues.length; i++) {
                    pickerDisplayValues[i] = (param_min + i) + "";
                }
            }
            picker.setDisplayedValues(pickerDisplayValues);
        }

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

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        if (param_zeroText != null)
        {
            builder.setNeutralButton(param_zeroText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setValue(0);
                }
            });
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
                range = new int[] {(param_min * HOUR_TO_MS), param_max * HOUR_TO_MS};   // [1, 12] hours
                break;
            case MODE_SECONDS:
                range = new int[] {(param_min * SECOND_TO_MS), param_max * SECOND_TO_MS};   // [1, 59] seconds
                break;
            case MODE_MINUTES:
            default:
                range = new int[] {(param_min * MINUTE_TO_MS), param_max * MINUTE_TO_MS};   // [1, 59] minutes
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

    public static int getMaxForMode( int mode )
    {
        switch (mode)
        {
            case MODE_HOURS:
                return MAX_HOURS;
            case MODE_SECONDS:
                return MAX_SECONDS;
            case MODE_MINUTES:
            default:
                return MAX_MINUTES;
        }
    }

    public void setParamMinMax(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MillisecondPickerPreference, 0, 0);
        try {
            param_min = a.getInt(R.styleable.MillisecondPickerPreference_minValue, param_min);
            param_max = a.getInt(R.styleable.MillisecondPickerPreference_maxValue, getMaxForMode(mode));
            param_zeroText = a.getString(R.styleable.MillisecondPickerPreference_zeroValueText);
        } finally {
            a.recycle();
        }
    }
    public void setParamMinMax(int min, int max)
    {
        param_min = min;
        param_max = max;
        initRange();
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
                    if (valueHours == 0 && param_zeroText != null)
                        return param_zeroText;
                    else return context.getResources().getQuantityString(R.plurals.units_hours, valueHours, valueHours);
                } else return valueHours + "";

            case MODE_SECONDS:
                if (context != null) {
                    if (valueSeconds == 0 && param_zeroText != null)
                        return param_zeroText;
                    else return context.getResources().getQuantityString(R.plurals.units_seconds, valueSeconds, valueSeconds);
                } else return valueSeconds + "";

            case MODE_MINUTES:
            default:
                if (context != null) {
                    if (valueMinutes == 0 && param_zeroText != null)
                        return param_zeroText;
                    else return context.getResources().getQuantityString(R.plurals.units_minutes, valueMinutes, valueMinutes);
                } else return valueMinutes + "";
        }
    }

    private void updateSummary()
    {
        setSummary(createSummaryString(getValue()));
    }
}
