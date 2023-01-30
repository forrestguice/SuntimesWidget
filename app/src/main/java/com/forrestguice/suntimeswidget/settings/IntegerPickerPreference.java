/**
    Copyright (C) 2022 Forrest Guice
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
public class IntegerPickerPreference extends DialogPreference
{
    public static final int MAX_VALUE = Integer.MAX_VALUE;

    private int value;
    private int[] range;
    private NumberPicker picker;
    private TextView pickerLabel;
    private boolean wrap = false;

    private int param_min = 1;
    private int param_max = MAX_VALUE;
    private String param_zeroText = null;

    @TargetApi(21)
    public IntegerPickerPreference(Context context)
    {
        super(context);
    }

    public IntegerPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setParamMinMax(context, attrs);
    }

    @TargetApi(21)
    public IntegerPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setParamMinMax(context, attrs);
    }

    @TargetApi(21)
    public IntegerPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                pickerLabel.setText(createSummaryString(newVal));
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

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);

        picker.setMinValue(getMin());
        picker.setMaxValue(getMax());
        picker.setWrapSelectorWheel(isWrapping());
        picker.setValue(getValue());

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

        pickerLabel.setText(createSummaryString(value));
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
            int changedValue = picker.getValue();
            if (callChangeListener(changedValue)) {
                setValue(changedValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int i) {
        return a.getInt(i, getMin());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(getMin()) : (Integer)defaultValue);
    }

    private void initRange() {
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

    public void setValue(int value)
    {
        this.value = value;
        persistInt(this.value);
        updateSummary();
    }
    public int getValue() {
        return this.value;
    }

    public void setParamMinMax(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IntegerPickerPreference, 0, 0);
        try {
            param_min = a.getInt(R.styleable.IntegerPickerPreference_minValue, param_min);
            param_max = a.getInt(R.styleable.IntegerPickerPreference_maxValue, getMax());
            param_zeroText = a.getString(R.styleable.IntegerPickerPreference_zeroValueText);
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
        if (context != null) {
            if (value == 0 && param_zeroText != null)
                return param_zeroText;
            else return value + "";
        } else return value + "";
    }

    private void updateSummary()
    {
        setSummary(createSummaryString(getValue()));
    }
}
