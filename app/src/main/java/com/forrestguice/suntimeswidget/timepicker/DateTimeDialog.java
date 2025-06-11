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
 * A "date selection" bottom sheet dialog that offers a "Time" button (that then shows an additional dialog).
 */
public class DateTimeDialog extends DateDialog
{
    public static final String DIALOGTAG_TIME = "dialog_time";

    public DateTimeDialog() {
        super();
    }

    @Nullable
    protected CharSequence getNeutralButtonLabel() {
        return getString(R.string.configAction_time);
    }

    @Override
    protected void onDialogNeutralClick(View v) {
        showTimeDialog(getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FragmentManager fragments = getChildFragmentManager();
        TimeDialog timeDialog = (TimeDialog) fragments.findFragmentByTag(DIALOGTAG_TIME);
        if (timeDialog != null) {
            timeDialog.setOnAcceptedListener(onTimeDialogAccepted(timeDialog));
        }
    }

    protected void showTimeDialog(Context context)
    {
        TimeDialog dialog = new TimeDialog();
        dialog.loadSettings(getActivity());
        dialog.setOnAcceptedListener(onTimeDialogAccepted(dialog));
        dialog.show(getChildFragmentManager(), DIALOGTAG_TIME);
    }

    private DialogInterface.OnClickListener onTimeDialogAccepted(final TimeDialog dialog) {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which) {
                getArgs().putInt(TimeDialog.KEY_DIALOG_HOUR, dialog.getSelectedHour());
                getArgs().putInt(TimeDialog.KEY_DIALOG_MINUTE, dialog.getSelectedMinute());
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
                return getSelectedYear();
            }

            @Override
            public Integer getMonth() {
                return getSelectedMonth();
            }

            @Override
            public Integer getDay() {
                return getSelectedDay();
            }

            @Override
            public Integer getHour() {
                int hour = getArgs().getInt(TimeDialog.KEY_DIALOG_HOUR, -1);
                return (hour != -1 ? hour : null);
            }

            @Override
            public Integer getMinute() {
                int minute = getArgs().getInt(TimeDialog.KEY_DIALOG_MINUTE, -1);
                return (minute != -1 ? minute : null);
            }

            @Override
            public Integer getSecond() {
                return 0;
            }
        };
    }

}