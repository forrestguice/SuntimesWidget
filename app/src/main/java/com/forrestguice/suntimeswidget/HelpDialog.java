/**
    Copyright (C) 2014 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class HelpDialog extends DialogFragment
{
    public static final String KEY_HELPTEXT = "helpText";
    public static final String KEY_NEUTRALTEXT = "neutralText";

    /**
     * The text content displayed by the help dialog.
     */
    private String rawContent = "";
    public String getContent()
    {
        return rawContent;
    }
    public void setContent( String content )
    {
        rawContent = content;
        if (txtView != null)
        {
            txtView.setText(SuntimesUtils.fromHtml(rawContent));
        }
    }

    /**
     * @param savedInstanceState a previously saved state (or null)
     * @return a Dialog object ready to be displayed
     */
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            neutralButtonMsg = savedInstanceState.getString(KEY_NEUTRALTEXT);
        }

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_help, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);

        if (neutralButtonMsg != null) {
            builder.setNeutralButton(neutralButtonMsg, null);
        }

        AlertDialog dialog = builder.create();
        initViews(dialogContent);
        if (savedInstanceState != null)
        {
            //Log.d("DEBUG", "HelpDialog onCreate (restoreState)");
            rawContent = savedInstanceState.getString(KEY_HELPTEXT);
        }
        setContent(rawContent);
        if (onShowListener != null) {
            dialog.setOnShowListener(onShowListener);
        }
        return dialog;
    }

    /**
     *
     */
    private TextView txtView;
    public void initViews(View dialogView)
    {
        txtView = (TextView) dialogView.findViewById(R.id.txt_help_content);
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //Log.d("DEBUG", "HelpDialog onSaveInstanceState");
        outState.putString(KEY_HELPTEXT, rawContent);
        //outState.putString(KEY_NEUTRALTEXT, neutralButtonMsg);
        super.onSaveInstanceState(outState);
    }

    /**
     * @param listener listener to be triggered when dialog is shown
     */
    public void setOnShowListener( DialogInterface.OnShowListener listener )
    {
        onShowListener = listener;
    }
    private DialogInterface.OnShowListener onShowListener;

    /**
     * Show/hide the neutral button; the click listener should be assigned in the dialogs onShowListener.
     * @param msg neutral button text (null hides button, default is null)
     */
    public void setShowNeutralButton( String msg )
    {
        neutralButtonMsg = msg;
    }
    private String neutralButtonMsg = null;

}
