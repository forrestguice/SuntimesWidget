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
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.forrestguice.suntimeswidget.R;

import java.util.Locale;

/**
 * TextSizeChooser
 */
public class TextSizeChooser implements TextWatcher, View.OnFocusChangeListener
{
    private Context context;
    private int textSize;
    private EditText edit = null;
    private float minSp, maxSp;

    public TextSizeChooser(Context context, EditText editField, float min, float max)
    {
        this.context = context;
        minSp = min;
        maxSp = max;
        edit = editField;
        if (edit != null)
        {
            edit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            edit.addTextChangedListener(this);
            edit.setOnFocusChangeListener(this);
        }
    }

    private boolean enabled = true;
    public void setEnabled(boolean value)
    {
        enabled = value;
        if (edit != null)
        {
            edit.setEnabled(enabled);
            if (!enabled)
                edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else edit.setPaintFlags(edit.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
    public boolean isEnabled()
    {
        return enabled;
    }

    public EditText getField()
    {
        return edit;
    }

    public int getTextSize()
    {
        return textSize;
    }

    public void setTextSize( int spValue )
    {
        textSize = spValue;
        updateViews();
    }

    public void updateViews()
    {
        if (edit != null)
        {
            edit.setText(String.format(Locale.US, "%d", textSize));
        }
    }

    public float getMinSp()
    {
        return minSp;
    }

    public float getMaxSp()
    {
        return maxSp;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable)
    {
        String spValue = editable.toString();
        try {
            textSize = Integer.parseInt(spValue);
        } catch (NumberFormatException e) {
            Log.w("setTextSize", "Invalid size! " + spValue + " ignoring...");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            if (edit != null)
            {
                afterTextChanged(edit.getText());
            }
            if (validateTextSize(context, edit, minSp, maxSp, false))
            {
                updatePreview();
            }
        }
    }

    public void updatePreview()
    {
    }

    public boolean validateTextSize(Context context)
    {
        return validateTextSize(context, edit, minSp, maxSp, true);
    }

    public boolean validateTextSize(Context context, EditText editTextSize, float min, float max, boolean grabFocus)
    {
        if (editTextSize == null)
            return true;

        boolean isValid = true;
        editTextSize.setError(null);

        try {
            int textSize = Integer.parseInt(editTextSize.getText().toString());
            if (textSize < min)
            {
                isValid = false;       // title too small
                editTextSize.setError(context.getString(R.string.edittheme_error_textsize_min, min+""));
                if (grabFocus)
                    editTextSize.requestFocus();
            }

            if (textSize > max)
            {
                isValid = false;       // title too large
                editTextSize.setError(context.getString(R.string.edittheme_error_textsize_max, max+""));
                if (grabFocus)
                    editTextSize.requestFocus();
            }

        } catch (NumberFormatException e) {
            isValid = false;          // title NaN (too small)
            editTextSize.setError(context.getString(R.string.edittheme_error_textsize_min, min+""));
            if (grabFocus)
                editTextSize.requestFocus();
        }
        return isValid;
    }
}
