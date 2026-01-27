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
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

@TargetApi(11)
public class MillisecondPickerHelper
{
    public static final int MODE_MINUTES = 0;
    public static final int MODE_HOURS = 10;
    public static final int MODE_SECONDS = 20;

    public static final int MAX_HOURS = 12;
    public static final int MAX_MINUTES = 59;
    public static final int MAX_SECONDS = 59;

    protected int value;
    protected int[] range;
    protected NumberPicker picker;
    protected TextView pickerLabel;
    protected boolean wrap = false;
    protected int mode = MODE_MINUTES;
    protected int param_min = 1;
    protected int param_max = MAX_MINUTES;
    protected String param_zeroText = null;

    public View createDialogView(final Context context)
    {
        float marginTopBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        float marginLeftRight;
        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.dialogPreferredPadding });
        marginLeftRight = context.getResources().getDimension(a.getResourceId(0, R.dimen.settingsGroup_margin));
        a.recycle();

        pickerLabel = new TextView(context);
        picker = new NumberPicker(context);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pickerLabel.setText(createSummaryString(context, pickerValuesToMs(newVal)));
            }
        });

        LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params0.gravity = Gravity.CENTER;
        picker.setLayoutParams(params0);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.START;
        params1.setMargins((int)marginLeftRight, (int)marginTopBottom, (int)marginLeftRight, (int)marginTopBottom);
        pickerLabel.setLayoutParams(params1);

        LinearLayout dialogView = new LinearLayout(context);
        dialogView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        dialogView.setLayoutParams(layoutParams);

        dialogView.addView(pickerLabel);
        dialogView.addView(picker);
        return dialogView;
    }

    public static final int SECOND_TO_MS = 1000;
    public static final int MINUTE_TO_MS = 1000 * 60;
    public static final int HOUR_TO_MS = 1000 * 60 * 60;

    public void onBindDialogView(View v)
    {
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

        pickerLabel.setText(createSummaryString(v.getContext(), pickerValuesToMs(value)));
    }

    public int pickerValuesToMs(int value)
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

    protected void initRange()
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

    public void setWrapping(boolean value) {
        this.wrap = value;
    }
    public boolean isWrapping() {
        return this.wrap;
    }

    public void setValue(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }
    public int getSelectedValue() {
        return pickerValuesToMs(picker.getValue());
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
    public int getMode() {
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

    public void setParamZeroText(String text) {
        param_zeroText = text;
    }
    public void setParamMinMax(int min, int max)
    {
        param_min = min;
        param_max = max;
        initRange();
    }
    public int getParamMin() {
        return param_min;
    }
    public int getParamMax() {
        return param_max;
    }

    public String createSummaryString(Context context, int value)
    {
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

}
