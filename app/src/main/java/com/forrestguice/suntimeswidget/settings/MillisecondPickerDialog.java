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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.app.DialogBase;

@TargetApi(11)
public class MillisecondPickerDialog extends DialogBase
{
    public MillisecondPickerDialog() {
        setArguments(new Bundle());
    }

    protected MillisecondPickerHelper helper = new MillisecondPickerHelper();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        initHelper();

        AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity());
        dialog.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialogListener != null) {
                    dialogListener.onDialogAccepted(helper.getSelectedValue());
                }
            }
        })
        .setNegativeButton(getString(R.string.dialog_cancel), null);
        dialog.setCancelable(false);

        String dialogTitle = getDialogTitle();
        if (dialogTitle != null) {
            dialog.setTitle(dialogTitle);
        }

        if (helper.param_zeroText != null)
        {
            dialog.setNeutralButton(helper.param_zeroText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (dialogListener != null) {
                        dialogListener.onDialogAccepted(0);
                    }
                }
            });
        }

        View dialogView = helper.createDialogView(requireActivity());
        dialog.setView(dialogView);
        helper.onBindDialogView(dialogView);

        Dialog d = dialog.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        return d;
    }

    public void setRange(int minValue, int maxValue)
    {
        getArgs().putInt("min", minValue);
        getArgs().putInt("max", maxValue);
        helper.setRange(minValue, maxValue);
    }
    public int getMin() {
        return helper.getMin();
    }
    public int getMax() {
        return helper.getMax();
    }

    public void setWrapping(boolean value) {
        getArgs().putBoolean("wrapping", value);
        helper.setWrapping(value);
    }
    public boolean isWrapping() {
        return helper.isWrapping();
    }

    public void setValue(int value)
    {
        getArgs().putInt("value", value);
        helper.setValue(value);
    }
    public int getValue() {
        return helper.getValue();
    }

    public void setMode(int mode)
    {
        getArgs().putInt("mode", mode);
        helper.setMode(mode);
    }
    public int getMode() {
        return helper.getMode();
    }

    public void setParamZeroText(String text)
    {
        getArgs().putString("zerotext", text);
        helper.setParamZeroText(text);
    }
    public void setParamMinMax(int min, int max) {
        getArgs().putInt("param_min", min);
        getArgs().putInt("param_max", max);
        helper.setParamMinMax(min, max);
    }

    public void setDialogTitle(String title) {
        getArgs().putString("title", title);
    }
    public String getDialogTitle() {
        return getArgs().getString("title", null);
    }

    public void initHelper()
    {
        Bundle args = getArgs();
        helper.setMode(args.getInt("mode", helper.getMode()));
        helper.setParamZeroText(args.getString("zerotext"));
        helper.setRange(args.getInt("min", helper.getMin()), getArgs().getInt("max", helper.getMax()));
        helper.setParamMinMax(args.getInt("param_min", helper.getParamMin()), getArgs().getInt("param_max", helper.getParamMax()));
        helper.setValue(args.getInt("value", helper.getValue()));
        helper.setWrapping(args.getBoolean("wrapping", helper.isWrapping()));
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
