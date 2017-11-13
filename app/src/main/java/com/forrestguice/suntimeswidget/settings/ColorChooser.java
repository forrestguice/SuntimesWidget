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
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.forrestguice.suntimeswidget.R;

import java.util.HashSet;
import java.util.Locale;

public class ColorChooser implements TextWatcher, View.OnFocusChangeListener
{
    final private ImageButton button;
    final private EditText edit;

    private int color;
    private boolean isRunning = false, isRemoving = false;

    public static final char[] alphabet = {'#', '0', '1', '2', '3', '4', '5', '6', '7','8', '9', 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F'};
    HashSet<Character> inputSet;

    public ColorChooser(final Context context, EditText editField, ImageButton button)
    {
        edit = editField;
        edit.addTextChangedListener(this);
        edit.setOnFocusChangeListener(this);

        inputSet = new HashSet<>();
        for (char c : alphabet)
        {
            inputSet.add(c);
        }

        this.button = button;
        this.button.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showColorPicker(context, getColor());
            }
        });
    }

    /**
     * @return EditText wrapped by chooser
     */
    public EditText getField()
    {
        return edit;
    }

    /**
     * @return Button wrapped by chooser
     */
    public ImageButton getButton()
    {
        return button;
    }

    /**
     * Set the color.
     * @param color color as integer
     */
    public void setColor(int color)
    {
        this.color = color;
        updateViews();
    }

    /**
     * Set the color.
     * @param hexCode hex color code #aarrggbb
     */
    public void setColor(String hexCode)
    {
        this.color = Color.parseColor(hexCode);
        updateViews();
    }

    /**
     * @return color value
     */
    public int getColor()
    {
        return color;
    }

    private void updateViews()
    {
        edit.setText( String.format("#%08X", color) );
        Drawable d = button.getDrawable();
        if (d != null)
        {
            GradientDrawable g = (GradientDrawable)d.mutate();
            g.setColor(color);
            g.invalidateSelf();
        }
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

        String text = editable.toString();             // should consist of [#][0-9][a-f]
        for (int j=text.length()-1; j>=0; j--)
        {
            if (!inputSet.contains(text.charAt(j)))
            {
                editable.delete(j, j+1);
            }
        }

        text = editable.toString();                   // should start with a #
        int i = text.indexOf('#');
        if (i != -1)
        {
            editable.delete(i, i + 1);
        }
        editable.insert(0, "#");

        if (editable.length() > 8)                   // should be no longer than 8
        {
            editable.delete(9, editable.length());
        }

        text = editable.toString();
        String toCaps = text.toUpperCase(Locale.US);
        editable.clear();
        editable.append(toCaps);

        isRunning = false;
    }

    protected void onColorChanged( int newColor ) {}

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            Editable editable = edit.getText();
            int i = editable.toString().indexOf('#');
            if (i != -1)                    // should start with a #
            {
                editable.delete(i, i + 1);
            }
            editable.insert(0, "#");

            while (editable.length() < 3)   // supply an alpha value (FF)
            {
                editable.insert(1, "F");
            }
            if (editable.length() == 7)
            {
                editable.insert(1, "FF");
            }

            while (editable.length() < 9)   // fill rest with "0"
            {
                editable.append("0");
            }

            //Log.d("DEBUG", "color is " + editable.toString());
            edit.setText(editable);
            setColor(editable.toString());
            onColorChanged(getColor());
        }
    }

    private void showColorPicker(Context context, int selectedColor)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            AlertDialog colorDialog = ColorPickerDialogBuilder.with(context)
                    .setTitle(context.getString(R.string.color_dialog_msg))
                    .initialColor(selectedColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(10)
                    .lightnessSliderOnly()
                    .setOnColorSelectedListener(new OnColorSelectedListener()
                    {
                        @Override
                        public void onColorSelected(int selectedColor) {}
                    })
                    .setPositiveButton(context.getString(R.string.color_dialog_ok), new ColorPickerClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors)
                        {
                            setColor(selectedColor);
                            onColorChanged(getColor());
                        }
                    })
                    .setNegativeButton(context.getString(R.string.color_dialog_cancel), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .build();
            colorDialog.show();
        } //else { // TODO: implement colorpicker for pre v14 }
    }

}
