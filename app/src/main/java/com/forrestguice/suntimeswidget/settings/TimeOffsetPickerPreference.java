/**
    Copyright (C) 2025 Forrest Guice
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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.support.preference.DialogPreference;
import com.forrestguice.util.android.AndroidResources;

/**
 * A version of MillisecondPickerPreference that allows selecting a millisecond value as a
 * combination of hours, minutes, and seconds.
 */
@TargetApi(11)
public class TimeOffsetPickerPreference extends DialogPreference
{
    private int value;
    private int param_minMs = 1, param_maxMs = 10000;

    private String param_zeroText = null;
    private String param_resetText = null;
    private Integer param_resetValue = null;

    private boolean param_showDirection = false;
    private boolean param_showSeconds = true;
    private boolean param_showMinutes = true;
    private boolean param_showHours = true;
    private boolean param_showDays = false;

    @TargetApi(21)
    public TimeOffsetPickerPreference(Context context) {
        super(context);
    }

    public TimeOffsetPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    @TargetApi(21)
    public TimeOffsetPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }

    @TargetApi(21)
    public TimeOffsetPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initParams(context, attrs);
    }

    private TextView label;
    private TimeOffsetPicker pickMillis;

    @Override
    protected View onCreateDialogView()
    {
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.layout_dialog_timeoffset, null, false);

        label = (TextView) dialogView.findViewById(R.id.text_label);
        pickMillis = (TimeOffsetPicker) dialogView.findViewById(R.id.pick_offset_millis);
        pickMillis.setParams(getContext(), param_minMs, param_maxMs, param_showSeconds, param_showMinutes, param_showHours, param_showDays, param_showDirection);
        pickMillis.addViewListener(onValueChanged);

        return dialogView;
    }

    private final TimeOffsetPicker.MillisecondPickerViewListener onValueChanged = new TimeOffsetPicker.MillisecondPickerViewListener()
    {
        @Override
        public void onValueChanged() {
            if (label != null && pickMillis != null) {
                label.setText(createSummaryString((int) pickMillis.getSelectedValue()));
            }
        }
    };

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        if (label != null) {
            label.setText(createSummaryString(getValue()));
        }
        if (pickMillis != null) {
            pickMillis.setSelectedValue(getValue());
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

        } else if (param_resetText != null) {
            builder.setNeutralButton(param_resetText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setValue(param_resetValue);
                }
            });
        }
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        if (result)
        {
            int changedValue = (int) pickMillis.getSelectedValue();
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

    public int getMin() {
        return param_minMs;
    }
    public int getMax() {
        return param_maxMs;
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

    public void initParams(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeOffsetPickerPreference, 0, 0);
        try {
            param_minMs = a.getInt(R.styleable.TimeOffsetPickerPreference_minValue, param_minMs);
            param_maxMs = a.getInt(R.styleable.TimeOffsetPickerPreference_maxValue, param_maxMs);
            param_zeroText = a.getString(R.styleable.TimeOffsetPickerPreference_zeroValueText);
            param_resetText = a.getString(R.styleable.TimeOffsetPickerPreference_resetDefaultsText);
            param_resetValue = a.getInt(R.styleable.TimeOffsetPickerPreference_resetDefaultsValue, param_minMs);
            param_showSeconds = a.getBoolean(R.styleable.TimeOffsetPickerPreference_allowPickSeconds, param_showSeconds);
            param_showMinutes = a.getBoolean(R.styleable.TimeOffsetPickerPreference_allowPickMinutes, param_showMinutes);
            param_showHours = a.getBoolean(R.styleable.TimeOffsetPickerPreference_allowPickHours, param_showHours);
            param_showDays = a.getBoolean(R.styleable.TimeOffsetPickerPreference_allowPickDays, param_showDays);
            param_showDirection = a.getBoolean(R.styleable.TimeOffsetPickerPreference_allowPickBeforeAfter, param_showDirection);

        } finally {
            a.recycle();
        }
    }
    
    private String createSummaryString(int value)
    {
        if (value == 0 && param_zeroText != null) {
            return param_zeroText;
        } else {
            return new TimeDeltaDisplay().timeDeltaLongDisplayString(0, value, true).getValue();
        }
    }

    private void updateSummary() {
        setSummary(createSummaryString(getValue()));
    }
}
