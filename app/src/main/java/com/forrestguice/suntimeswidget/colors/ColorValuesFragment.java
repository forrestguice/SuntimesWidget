// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2024 Forrest Guice
    This file is part of Natural Hour.

    Natural Hour is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Natural Hour is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Natural Hour.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.colors;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.forrestguice.suntimeswidget.R;

public class ColorValuesFragment extends Fragment
{
    public static final String KEY_DIALOGTHEME = "themeResID";
    protected static final int DEF_DIALOGTHEME = R.style.AppTheme_Dark;

    public void setTheme(int themeResID)
    {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putInt(KEY_DIALOGTHEME, themeResID);
        setArguments(args);
    }
    public int getThemeResID() {
        return (getArguments() != null) ? getArguments().getInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME) : DEF_DIALOGTHEME;
    }

    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////

    public interface ImportColorsDialogInterface {
        void onImportClicked(String input);
    }
    public static AlertDialog.Builder createImportColorsDialog(final Context context, final ImportColorsDialogInterface dialogInterface)
    {
        final EditText editText = new EditText(context);
        editText.setSingleLine(false);
        editText.setLines(3);
        editText.setMaxLines(10);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setHint(context.getString(R.string.colorsimport_dialog_hint));

        int margin = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(margin, margin, margin, 0);
        layout.addView(editText, layoutParams);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getString(R.string.colorsimport_dialog_title));
        dialog.setNegativeButton(context.getString(R.string.colorsimport_dialog_cancel), null);
        dialog.setPositiveButton(context.getString(R.string.colorsimport_dialog_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogInterface.onImportClicked(editText.getText().toString());
            }
        });
        dialog.setView(layout);
        return dialog;
    }
}