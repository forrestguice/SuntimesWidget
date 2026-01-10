/**
    Copyright (C) 2024 Forrest Guice
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
public class IntegerPickerHelper
{
    protected int value;
    protected int[] range;
    protected NumberPicker picker;
    protected TextView pickerLabel;
    protected boolean wrap = false;
    protected int param_min = 1;
    protected int param_max = 10;
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
                pickerLabel.setText(createSummaryString(context, newVal));
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

    public void onBindDialogView(View v)
    {
        int value = getValue();
        picker.setMinValue(getMin());
        picker.setMaxValue(getMax());
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

        pickerLabel.setText(createSummaryString(v.getContext(), value));
    }

    protected void initRange() {
        range = new int[] {param_min, param_max};
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
        return picker.getValue();
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
        if (context != null) {
            if (value == 0 && param_zeroText != null)
                return param_zeroText;
            else return value + "";
        } else return value + "";
    }

}
