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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.forrestguice.suntimeswidget.R;

public class ColorDialog extends DialogFragment
{
    public ColorDialog() {}

    private int color = Color.WHITE;
    public int getColor()
    {
        return color;
    }
    public void setColor( int color )
    {
        this.color = color;
    }

    private boolean showAlpha = false;
    public void setShowAlpha(boolean value)
    {
        this.showAlpha = value;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState)
    {
        super.onCreate(savedState);
        if (savedState != null)
        {
            setColor(savedState.getInt("color", getColor()));
            showAlpha = savedState.getBoolean("showAlpha", showAlpha);
        }

        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            ColorPickerDialogBuilder builder = ColorPickerDialogBuilder.with(context)
                .setTitle(context.getString(R.string.color_dialog_msg))
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener()
                {
                    @Override
                    public void onColorSelected(int selectedColor)
                    {
                        setColor(selectedColor);
                    }
                })
                .setPositiveButton(context.getString(R.string.color_dialog_ok), new ColorPickerClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors)
                    {
                        setColor(selectedColor);
                        signalColorChange(getColor());
                    }
                })
                .setNegativeButton(context.getString(R.string.color_dialog_cancel), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });

            builder = (showAlpha ? builder.showLightnessSlider(true).showAlphaSlider(true)
                                 : builder.lightnessSliderOnly());

            return builder.build();

        }  else {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("STUB: TODO");
            alertDialog.setMessage("Not currently supported for api < 14");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            return alertDialog;
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putInt("color", getColor());
        outState.putBoolean("showAlpha", showAlpha);
    }

    /**
     * ColorChangeListener
     */
    public static abstract class ColorChangeListener
    {
        public void onColorChanged(int color) {}
    }
    public ColorChangeListener colorChangeListener = null;
    public void setColorChangeListener( ColorChangeListener listener )
    {
        this.colorChangeListener = listener;
    }

    private void signalColorChange(int color)
    {
        if (colorChangeListener != null)
        {
            colorChangeListener.onColorChanged(color);
        }
    }

}
