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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.forrestguice.suntimeswidget.R;

public class MillisecondPickerPreference extends DialogPreference
{
    private int value;
    private int[] range;
    private NumberPicker picker;
    private boolean wrap = false;

    public MillisecondPickerPreference(Context context)
    {
        super(context);
    }

    public MillisecondPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MillisecondPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected View onCreateDialogView()
    {
        picker = new NumberPicker(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        picker.setLayoutParams(params);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);
        return dialogView;
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);

        int minMinutes = getMin() / 60 / 1000;
        int maxMinutes = getMax() / 60 / 1000;
        picker.setMinValue(minMinutes);
        picker.setMaxValue(maxMinutes);

        int valueMinutes = getValue() / 60 / 1000;
        picker.setWrapSelectorWheel(isWrapping());
        picker.setValue(valueMinutes);
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        if (result)
        {
            int changedValue = picker.getValue() * 60 * 1000;
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
        if (range == null) {
            range = new int[] {1000 * 60, 1000 * 60 * 59};   // [1, 59] minute
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

    private void updateSummary()
    {
        int valueMinutes = (getValue() / 60 / 1000);
        Context context = getContext();
        if (context != null) {
            setSummary(context.getResources().getQuantityString(R.plurals.units_minutes, valueMinutes, valueMinutes));
        } else setSummary(valueMinutes + "");
    }
}
