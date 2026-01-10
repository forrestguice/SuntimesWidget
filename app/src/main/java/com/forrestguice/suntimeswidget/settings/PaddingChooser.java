/**
    Copyright (C) 2017 Forrest Guice
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

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.forrestguice.annotation.NonNull;

public class PaddingChooser implements TextWatcher, View.OnFocusChangeListener
{
    private final int[] padding = new int[4];
    private final EditText edit;

    private boolean isRunning = false, isRemoving = false;
    private final char[] brackets = {'[',']'};
    private final char separator = ',';

    public PaddingChooser(EditText editField )
    {
        edit = editField;
        edit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        edit.addTextChangedListener(this);
        edit.setOnFocusChangeListener(this);
    }

    /**
     * @return EditText wrapped by chooser
     */
    public EditText getField()
    {
        return edit;
    }

    /**
     * @return padding values [left, top, right, bottom]
     */
    public int[] getPadding()
    {
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
        for (int i=0; i<padding.length; i++)
        {
            paddingPixels[i] = (int)((metrics.density * this.padding[i]) + 0.5f);
        }
        return paddingPixels;
    }

    /**
     * @param padding values [left, top, right, bottom]
     */
    public void setPadding( int[] padding )
    {
        for (int i=0; i<padding.length && i<this.padding.length; i++)
        {
            this.padding[i] = padding[i];
        }
        updateViews();
    }

    private void setPadding(int i, int value)
    {
        if (i >= 0 && i < padding.length)
        {
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
        if (edit != null)
        {
            edit.setEnabled(value);
            if (!enabled)
                edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else edit.setPaintFlags(edit.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
    public boolean isEnabled()
    {
        return enabled;
    }

    private void updateViews()
    {
        edit.setText(toString());
    }

    @NonNull
    public String toString() {
        return "" + brackets[0] + padding[0] + separator + padding[1] + separator + padding[2] + separator + padding[3] + brackets[1];
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
    {
        isRemoving = count > after;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int after) {}

    @Override
    public void afterTextChanged(Editable editable)
    {
        if (isRunning || isRemoving)
            return;
        isRunning = true;

        insertStartBracket(editable);
        int length = editable.length();
        String text = editable.toString();

        //noinspection UnusedAssignment
        int i0 = -1, i1 = -1, i2 = -1, i3 = -1;
        if ((i0 = text.indexOf(separator, 0)) != -1)
        {
            if ((i1 = text.indexOf(separator, i0 + 1)) != -1)
            {
                if ((i2 = text.indexOf(separator, i1 + 1)) != -1)
                {
                    if ((i3 = text.indexOf(separator, i2 + 1)) != -1)
                    {
                        // has four commas (one too many)
                        editable.delete(i3, length);
                    }

                    // has 3 commas (the right amount)
                    appendEndBracket(editable);
                    length = editable.length();
                    text = editable.toString();

                    setPadding(0, text.substring(1, i0));
                    setPadding(1, text.substring(i0+1, i1));
                    setPadding(2, text.substring(i1+1, i2));
                    setPadding(3, text.substring(i2+1, length-1));
                    onPaddingChanged(padding);

                } else {
                    // has two commas
                    setPadding(0, text.substring(1, i0));
                    setPadding(1, text.substring(i0+1, i1));
                    onPaddingChanged(padding);
                    appendSeparator(editable);
                }

            } else {
                // has one comma
                setPadding(0, text.substring(1, i0));
                onPaddingChanged(padding);
                appendSeparator(editable);
            }
        } else {
            // has no commas
            if (length > 1)
            {
                setPadding(0, text.substring(1, length));
                onPaddingChanged(padding);
                appendSeparator(editable);
            }
        }
        isRunning = false;
    }

    private void insertStartBracket(Editable editable)
    {
        if (editable.charAt(0) != brackets[0])
        {
            editable.insert(0, brackets[0]+"");
        }
    }

    private void appendEndBracket(Editable editable)
    {
        int i;
        if ((i = editable.toString().indexOf(brackets[1])) != -1)
        {
            editable.delete(i, i+1);
        }
        editable.append(brackets[1]);
    }

    private void appendSeparator(Editable editable)
    {
        if (editable.charAt(editable.length() - 1) != separator)
        {
            editable.append(separator);
        }
    }

    protected void onPaddingChanged( int[] newPadding ) {}

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            afterTextChanged(edit.getText());
            updateViews();
        }
    }
}
