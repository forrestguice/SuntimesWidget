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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimes.support.app.DialogBase;
import com.forrestguice.support.app.AlertDialog;

@TargetApi(11)
public class TimeOffsetPickerDialog extends DialogBase
{
    protected TextView label;
    protected TimeOffsetPicker pickMillis;

    public TimeOffsetPickerDialog() {
        setArguments(new Bundle());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        SuntimesUtils.initDisplayStrings(getContext());
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialogListener != null) {
                    dialogListener.onDialogAccepted(pickMillis.getSelectedValue());
                }
            }
        })
        .setNegativeButton(getString(R.string.dialog_cancel), null);
        dialog.setCancelable(false);

        String dialogTitle = getDialogTitle();
        if (dialogTitle != null) {
            dialog.setTitle(dialogTitle);
        }

        String neutralButtonText = getZeroText();
        int neutralButtonValue = 0;

        if (neutralButtonText == null) {
            neutralButtonText = getRestoreDefaultText();
            neutralButtonValue = getRestoreDefaultValue();
        }

        if (neutralButtonText != null)
        {
            final int neutralButtonValue0 = neutralButtonValue;
            dialog.setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (dialogListener != null) {
                        dialogListener.onDialogAccepted(neutralButtonValue0);
                    }
                }
            });
        }

        SuntimesUtils.initDisplayStrings(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.layout_dialog_timeoffset, null, false);

        label = (TextView) dialogView.findViewById(R.id.text_label);
        if (label != null) {
            label.setText(createSummaryString(getValue()));
            label.setVisibility(getShowLabel() ? View.VISIBLE : View.GONE);
        }

        pickMillis = (TimeOffsetPicker) dialogView.findViewById(R.id.pick_offset_millis);
        if (pickMillis != null) {
            pickMillis.setParams(getContext(), getMin(), getMax(), allowSeconds(), allowMinutes(), allowHours(), allowDays(), allowDirection());
            pickMillis.setSelectedValue(getArgs().getInt("value", getMin()));
            pickMillis.addViewListener(onValueChanged);
        }

        dialog.setView(dialogView);
        Dialog d = dialog.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        return d;
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

    public void setRange(int minValue, int maxValue) {
        getArgs().putInt("min", minValue);
        getArgs().putInt("max", maxValue);
    }
    public int getMin() {
        return getArgs().getInt("min");
    }
    public int getMax() {
        return getArgs().getInt("max");
    }

    public void setFlags(boolean allowSeconds, boolean allowMinutes, boolean allowHours, boolean allowDays, boolean allowDirection) {
        getArgs().putBoolean("allowSeconds", allowSeconds);
        getArgs().putBoolean("allowMinutes", allowMinutes);
        getArgs().putBoolean("allowHours", allowHours);
        getArgs().putBoolean("allowDays", allowDays);
        getArgs().putBoolean("allowDirection", allowDirection);
    }
    public boolean allowSeconds() {
        return getArgs().getBoolean("allowSeconds", true);
    }
    public boolean allowMinutes() {
        return getArgs().getBoolean("allowMinutes", true);
    }
    public boolean allowHours() {
        return getArgs().getBoolean("allowHours", true);
    }
    public boolean allowDays() {
        return getArgs().getBoolean("allowDays", true);
    }
    public boolean allowDirection() {
        return getArgs().getBoolean("allowDirection", true);
    }

    public void setShowLabel(boolean value) {
        getArgs().putBoolean("showlabel", value);
        if (label != null) {
            label.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }
    public boolean getShowLabel() {
        return getArgs().getBoolean("showlabel", true);
    }

    public void setZeroText(String text) {
        getArgs().putString("zerotext", text);
    }
    public String getZeroText() {
        return getArgs().getString("zerotext", null);
    }

    public void setRestoreDefault(String text, int value) {
        getArgs().putString("defaulttext", text);
        getArgs().putInt("defaultvalue", value);
    }
    public String getRestoreDefaultText() {
        return getArgs().getString("defaulttext", null);
    }
    public int getRestoreDefaultValue() {
        return getArgs().getInt("defaultvalue", getMin());
    }

    public void setValue(int value)
    {
        getArgs().putInt("value", value);
        if (pickMillis != null) {
            pickMillis.setSelectedValue(value);
        }
        if (label != null) {
            label.setText(createSummaryString(getValue()));
        }
    }
    public int getValue() {
        return (int)(pickMillis != null ? pickMillis.getSelectedValue()
                : getArgs().getInt("value", getMin()));
    }

    public void setDialogTitle(String title) {
        getArgs().putString("title", title);
    }
    public String getDialogTitle() {
        return getArgs().getString("title", null);
    }

    private String createSummaryString(int value) {
        return (value == 0 && getZeroText() != null) ? getZeroText()
                : new SuntimesUtils().timeDeltaLongDisplayString(0, value, true).getValue();
    }

    /**
     * DialogListener
     */
    public interface DialogListener
    {
        void onDialogAccepted(long value);
    }

    protected DialogListener dialogListener = null;
    public void setDialogListener(DialogListener listener) {
        dialogListener = listener;
    }

}