/**
    Copyright (C) 2019-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.design.widget.Snackbar;

public class SnackbarUtils
{
    @SuppressLint("ResourceType")
    public static Snackbar.SnackbarInterface themeSnackbar(Context context, Snackbar.SnackbarInterface snackbar) {
        return themeSnackbar(context, snackbar, null);
    }

    @SuppressLint("ResourceType")
    public static Snackbar.SnackbarInterface themeSnackbar(Context context, Snackbar.SnackbarInterface snackbar, Integer[] colorOverrides)
    {
        Integer[] colors = new Integer[] {null, null, null};
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, R.drawable.button_fab_dark));
        int buttonPadding = (int)context.getResources().getDimension(R.dimen.snackbar_button_padding);
        a.recycle();

        if (colorOverrides != null && colorOverrides.length == colors.length) {
            for (int i=0; i<colors.length; i++) {
                if (colorOverrides[i] != null) {
                    colors[i] = colorOverrides[i];
                }
            }
        }
        return com.forrestguice.support.design.widget.Snackbar.themeSnackbar(snackbar, colors, buttonDrawable, buttonPadding);
    }
}
