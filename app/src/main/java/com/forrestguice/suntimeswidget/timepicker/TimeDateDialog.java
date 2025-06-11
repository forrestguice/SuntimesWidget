/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimeswidget.timepicker;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.forrestguice.suntimeswidget.R;

/**
 * A "time selection" bottom sheet dialog that offers a "Date" button (that then shows an additional dialog).
 */
public class TimeDateDialog extends TimeDialog
{
    public static final String DIALOGTAG_DATE = "dialog_date";

    public TimeDateDialog() {
        super();
    }

    @Nullable
    protected CharSequence getNeutralButtonLabel() {
        return getString(R.string.configAction_date);
    }

    @Override
    protected void onDialogNeutralClick(View v) {
        showDateDialog(getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FragmentManager fragments = getChildFragmentManager();
        DateDialog dateDialog = (DateDialog) fragments.findFragmentByTag(DIALOGTAG_DATE);
        if (dateDialog != null) {
            dateDialog.setOnAcceptedListener(onDateDialogAccepted(dateDialog));
        }
    }

    protected void showDateDialog(Context context)
    {
        DateDialog dialog = new DateDialog();
        dialog.loadSettings(getActivity());
        dialog.setTimeIs24(timeIs24()); 
        dialog.setOnAcceptedListener(onDateDialogAccepted(dialog));
        dialog.show(getChildFragmentManager(), DIALOGTAG_DATE);
    }

    private DialogInterface.OnClickListener onDateDialogAccepted(final DateDialog dialog) {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which) {
                getArgs().putInt(DateDialog.KEY_DIALOG_YEAR, dialog.getSelectedYear());
                getArgs().putInt(DateDialog.KEY_DIALOG_MONTH, dialog.getSelectedMonth());
                getArgs().putInt(DateDialog.KEY_DIALOG_DAY, dialog.getSelectedDay());
                onDialogAccept();
            }
        };
    }

    @Override
    public TimeDialogResult getSelected()
    {
        return new TimeDialogResult()
        {
            @Override
            public Integer getYear() {
                int value = getArgs().getInt(DateDialog.KEY_DIALOG_YEAR, -1);
                return (value != -1 ? value : null);
            }

            @Override
            public Integer getMonth() {
                int value = getArgs().getInt(DateDialog.KEY_DIALOG_MONTH, -1);
                return (value != -1 ? value : null);
            }

            @Override
            public Integer getDay() {
                int value = getArgs().getInt(DateDialog.KEY_DIALOG_DAY, -1);
                return (value != -1 ? value : null);
            }

            @Override
            public Integer getHour() {
                return getSelectedHour();
            }

            @Override
            public Integer getMinute() {
                return getSelectedMinute();
            }

            @Override
            public Integer getSecond() {
                return 0;
            }
        };
    }

}