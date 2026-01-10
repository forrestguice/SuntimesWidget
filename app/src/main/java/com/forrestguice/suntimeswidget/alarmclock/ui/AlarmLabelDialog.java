/**
    Copyright (C) 2018-2023 Forrest Guice
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
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.app.DialogBase;
import com.forrestguice.support.view.ViewCompat;

public class AlarmLabelDialog extends DialogBase
{
    public static final String PREF_KEY_ALARM_LABEL = "alarmlabel";
    public static final String PREF_DEF_ALARM_LABEL = "";

    public static final String KEY_COLORS = "alarmlabel_colors";

    private static final String DIALOGTAG_HELP = "alarmlabelhelp";

    private EditText edit;
    private String label = PREF_DEF_ALARM_LABEL;

    public AlarmLabelDialog() {
        setArguments(new Bundle());
    }

    public void setShowHelp(boolean showHelp, CharSequence helpContent, String helpUrl, String helpTag) {
        getArgs().putBoolean("showHelp", showHelp);
        getArgs().putCharSequence("helpContent", helpContent);
        getArgs().putString("helpUrl", helpUrl);
        getArgs().putString("helpTag", helpTag);
    }
    public CharSequence helpContent() {
        return getArgs().getCharSequence("helpContent");
    }
    public String helpUrl() {
        return getArgs().getString("helpUrl");
    }
    public String helpTag() {
        return getArgs().getString("helpTag");
    }
    public boolean showHelp() {
        return getArgs().getBoolean("showHelp", false);
    }

    public void setDialogTitle(String value) {
        getArgs().putString("dialogTitle", value);
    }
    public String getDialogTitle(Context context) {
        String title = getArgs().getString("dialogTitle");
        return (title != null ? title : context.getString(R.string.alarmlabel_dialog_title));
    }

    public void setMultiLine(boolean value) {
        getArgs().putBoolean("multiLine", value);
    }
    public boolean isMultiLine() {
        return getArgs().getBoolean("multiLine", false);
    }

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an Dialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = requireActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_alarmlabel, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(getDialogTitle(myParent));

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        AlertDialog.setButton(dialog, AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.alarmlabel_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (edit != null) {
                            label = edit.getText().toString();
                        }
                        dialog.dismiss();
                        if (onCanceled != null) {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        AlertDialog.setButton(dialog, AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.alarmlabel_dialog_ok),
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

        if (showHelp())
        {
            AlertDialog.setButton(dialog, AlertDialog.BUTTON_NEUTRAL, myParent.getString(R.string.configAction_help), (DialogInterface.OnClickListener)null);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {    // AlertDialog.neutralButton calls dismiss unless the listener is initially null
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = AlertDialog.getButton(dialog, AlertDialog.BUTTON_NEUTRAL);
                    if (button != null) {
                        button.setOnClickListener(onHelpButtonClicked);
                    }
                }
            });
        }

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        initViews(myParent, dialogContent);
        updateViews(getContext());

        Window w = dialog.getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        selectAll();
        return dialog;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        HelpDialog helpDialog = (HelpDialog) getChildFragmentManager().findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(onlineHelpClickListener, helpTag());
        }
    }

    protected void showHelpDialog()
    {
        CharSequence helpContent = helpContent();
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpContent != null ? helpContent : "");
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(onlineHelpClickListener, helpTag());
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    protected View.OnClickListener onlineHelpClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl())));
        }
    };

    private final View.OnClickListener onHelpButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelpDialog();
        }
    };

    private int accentColor = -1;
    public void setAccentColor( int color ) {
        accentColor = color;
    }

    public void selectAll()
    {
        if (edit != null)
        {
            edit.selectAll();
            edit.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void initViews( final Context context, View dialogContent )
    {
        edit = (EditText) dialogContent.findViewById(R.id.edit_alarmLabel);
        if (edit != null)
        {
            if (isMultiLine()) {
                edit.setSingleLine(false);
                edit.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            }

            if (accentColor != -1) {
                ViewCompat.setBackgroundTintList(edit, SuntimesUtils.colorStateList(accentColor, accentColor, accentColor));
            }
            edit.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s)
                {
                    label = s.toString();
                }
            });
        }
    }

    private void updateViews(Context context)
    {
        if (edit != null) {
            edit.setText(label != null ? label : PREF_DEF_ALARM_LABEL);
        }
    }

    public void setLabel(String value)
    {
        this.label = value;
        updateViews(getContext());
    }
    public String getLabel()
    {
        return label;
    }

    protected void loadSettings(Bundle bundle)
    {
        this.label =  bundle.getString(PREF_KEY_ALARM_LABEL);
        if (this.label == null) {
            this.label = PREF_DEF_ALARM_LABEL;
        }
        this.accentColor = bundle.getInt(KEY_COLORS, accentColor);
    }

    protected void saveSettings(Bundle bundle)
    {
        bundle.putString(PREF_KEY_ALARM_LABEL, label);
        bundle.putInt(KEY_COLORS, accentColor);
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