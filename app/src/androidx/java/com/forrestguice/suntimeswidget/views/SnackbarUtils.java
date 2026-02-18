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

import android.content.Context;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.widget.SnackbarCompat;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils extends SnackbarCompat
{
    public SnackbarUtils(Snackbar snackbar) {
        super(snackbar);
    }

    public static Snackbar make(@NonNull Context context, @NonNull View view, @NonNull CharSequence text, int duration) {
        return SnackbarCompat.make(context, view, text, duration, new BasicSnackbarTheme());
    }

    public static class BasicSnackbarTheme implements SnackbarTheme
    {
        public int[] colorAttrs() {
            return new int[] { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        }
        public int[] colorAttrs_defaults() {
            return new int[] { android.R.color.primary_text_dark, R.color.text_accent_dark, R.color.card_bg_dark, R.drawable.button_fab_dark };
        }
        public int resId_buttonPadding() {
            return R.dimen.snackbar_button_padding;
        }
    }
}
