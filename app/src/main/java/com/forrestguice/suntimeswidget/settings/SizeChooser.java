/**
    Copyright (C) 2017-2021 Forrest Guice
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
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.Locale;

/**
 * TextSizeChooser
 */
public class SizeChooser implements TextWatcher, View.OnFocusChangeListener
{
    private String chooserID = "0";
    private final Context context;
    private float value;
    private final EditText edit;
    private final float min, max;

    public SizeChooser(Context context, EditText editField, float min, float max, String id)
    {
        this.chooserID = id;
        this.context = context;
        this.min = min;
        this.max = max;
        edit = editField;
        if (edit != null)
        {
            edit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            edit.addTextChangedListener(this);
            edit.setOnFocusChangeListener(this);

            edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        changeValue();
                        return true;
                    }
                    return false;
                }
            });

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

    public String getID()
    {
        return chooserID;
    }

    public EditText getField()
    {
        return edit;
    }

    public float getValue()
    {
        return value;
    }

    public void setValue( float value )
    {
        this.value = value;
        updateViews();
    }

    public void setValue( Bundle savedState )
    {
        setValue(savedState.getFloat(chooserID, getValue()));
    }

    public void updateViews()
    {
        if (edit != null)
        {
            edit.setText(String.format(Locale.US, "%.1f", value));
        }
    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable)
    {
        String rawValue = editable.toString();
        try {
            value = Float.parseFloat(rawValue);
        } catch (NumberFormatException e) {
            Log.w("setTextSize", "Invalid size! " + rawValue + " ignoring...");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            changeValue();
        }
    }

    private void changeValue()
    {
        if (edit != null)
        {
            afterTextChanged(edit.getText());
        }
        if (validateValue(context, edit, min, max, false))
        {
            updatePreview();
        }
    }

    public void updatePreview()
    {
    }

    public boolean validateValue(Context context)
    {
        return validateValue(context, edit, min, max, true);
    }

    public boolean validateValue(Context context, EditText editValue, float min, float max, boolean grabFocus)
    {
        if (editValue == null)
            return true;

        boolean isValid = true;
        editValue.setError(null);

        try {
            float textSize = Float.parseFloat(editValue.getText().toString());
            if (textSize < min)
            {
                isValid = false;       // too small
                editValue.setError(context.getString(R.string.edittheme_error_textsize_min, min+""));
                if (grabFocus)
                    editValue.requestFocus();
            }

            if (textSize > max)
            {
                isValid = false;       // too large
                editValue.setError(context.getString(R.string.edittheme_error_textsize_max, max+""));
                if (grabFocus)
                    editValue.requestFocus();
            }

        } catch (NumberFormatException e) {
            isValid = false;          // NaN (too small)
            editValue.setError(context.getString(R.string.edittheme_error_textsize_min, min+""));
            if (grabFocus)
                editValue.requestFocus();
        }
        return isValid;
    }
}
