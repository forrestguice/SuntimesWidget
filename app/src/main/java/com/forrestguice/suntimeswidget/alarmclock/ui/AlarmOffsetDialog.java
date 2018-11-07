/**
    Copyright (C) 2018 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.forrestguice.suntimeswidget.R;

public class AlarmOffsetDialog extends DialogFragment
{
    public static final String PREF_KEY_ALARM_TIME_OFFSET = "alarmoffset";
    public static final long PREF_DEF_ALARM_TIME_OFFSET = 0L;

    private long offset = PREF_DEF_ALARM_TIME_OFFSET;

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an Dialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_alarmoffset, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.alarmoffset_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.alarmoffset_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (onCanceled != null) {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.alarmoffset_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (onAccepted != null) {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        initViews(myParent, dialogContent);
        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        updateViews(getContext());
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void initViews( final Context context, View dialogContent )
    {
    }

    private void updateViews(Context context)
    {
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
    }

    public long getOffset()
    {
        return offset;
    }

    protected void loadSettings(Bundle bundle)
    {
        this.offset = bundle.getLong(PREF_KEY_ALARM_TIME_OFFSET, PREF_DEF_ALARM_TIME_OFFSET);
    }

    protected void saveSettings(Bundle bundle)
    {
        bundle.putLong(PREF_KEY_ALARM_TIME_OFFSET, offset);
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

}