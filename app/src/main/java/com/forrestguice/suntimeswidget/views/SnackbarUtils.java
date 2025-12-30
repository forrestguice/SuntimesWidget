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
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.content.ContextCompat;

import android.support.design.widget.Snackbar;

public class SnackbarUtils
{
    public static final int LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE;
    public static final int LENGTH_SHORT = Snackbar.LENGTH_SHORT;
    public static final int LENGTH_LONG = Snackbar.LENGTH_LONG;

    public static Snackbar make(@NonNull Context context, @NonNull View view, @NonNull CharSequence text, int duration)
    {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        themeSnackbar(context, snackbar, null);
        return snackbar;
    }

    @SuppressLint("ResourceType")
    public static void themeSnackbar(Context context, Snackbar snackbar, Integer[] colorOverrides)
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

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(getSnackbarTextResourceID());
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }

        View snackbarAction = snackbarView.findViewById(getSnackbarActionResourceID());
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16)
            {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            }
        }
    }

    public static int getSnackbarTextResourceID() {
        return android.support.design.R.id.snackbar_text;    // support libraries
        //return com.google.android.material.R.id.snackbar_text;   // androidx
    }

    public static int getSnackbarActionResourceID() {
        return android.support.design.R.id.snackbar_action;    // support libraries
        //return com.google.android.material.R.id.snackbar_action;   // androidx
    }

}
