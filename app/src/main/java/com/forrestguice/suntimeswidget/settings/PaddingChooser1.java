/**
    Copyright (C) 2017-2026 Forrest Guice
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.forrestguice.annotation.NonNull;

import java.util.Locale;

public class PaddingChooser1 implements View.OnFocusChangeListener
{
    private final int[] padding = new int[4];
    private final EditText[] pickers;

    public PaddingChooser1(EditText left, EditText top, EditText right, EditText bottom)
    {
        pickers = new EditText[] { left, top, right, bottom };
        for (int i=0; i<pickers.length; i++)
        {
            EditText picker = pickers[i];
            if (picker != null)
            {
                picker.setTag(i);
                picker.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                picker.setOnFocusChangeListener(this);
                picker.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable)
                    {
                        int p = (Integer) picker.getTag();
                        setPadding(p, editable.toString());
                        onPaddingChanged(padding);
                    }
                });
            }
        }
    }

    /**
     * @return EditText wrapped by chooser
     */
    public EditText getField(int i) {
        if (i >= 0 && i < pickers.length) {
            return pickers[i];
        } else return null;
    }

    /**
     * @return padding values [left, top, right, bottom]
     */
    public int[] getPadding() {
        return padding;
    }

    /**
     * @param context Context obj used to access resources
     * @return padding pixel values [left, top, right, bottom]
     */
    public int[] getPaddingPixels(Context context)
    {
        int[] paddingPixels = new int[padding.length];
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        for (int i=0; i<padding.length; i++) {
            paddingPixels[i] = (int)((metrics.density * this.padding[i]) + 0.5f);
        }
        return paddingPixels;
    }

    /**
     * @param padding values [left, top, right, bottom]
     */
    public void setPadding( int[] padding ) {
        for (int i=0; i<padding.length && i<this.padding.length; i++) {
            this.padding[i] = padding[i];
        }
        updateViews();
    }

    private void setPadding(int i, int value) {
        if (i >= 0 && i < padding.length) {
            padding[i] = value;
        }
    }
    private void setPadding(int i, String value)
    {
        try {
            setPadding(i, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            setPadding(i, 0);
        }
    }

    private boolean enabled = true;
    public void setEnabled(boolean value)
    {
        enabled = value;
        for (EditText edit : pickers)
        {
            if (edit != null) {
                edit.setEnabled(value);
                if (!enabled)
                    edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                else edit.setPaintFlags(edit.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }
    public boolean isEnabled() {
        return enabled;
    }

    @SuppressLint("SetTextI18n")
    private void updateViews()
    {
        for (EditText edit : pickers) {
            if (edit != null) {
                int i = (Integer) edit.getTag();
                edit.setText(Integer.toString(padding[i]));
            }
        }
    }

    @NonNull
    public String toString() {
        return "" + brackets[0] + padding[0] + separator + padding[1] + separator + padding[2] + separator + padding[3] + brackets[1];
    }
    private final char[] brackets = {'[',']'};
    private final char separator = ',';

    protected void onPaddingChanged( int[] newPadding ) {}

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus) {
            updateViews();
        }
    }
}
