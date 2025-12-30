/**
    Copyright (C) 2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.dialog.DialogBase;

public class BedtimeSleepDialog extends DialogBase
{
    protected static final String DIALOGTAG_HELP = "sleepcyclehelp";

    protected NumberPicker sleepCyclePicker;
    protected TextView sleepCycleLabel;

    protected SuntimesUtils utils = new SuntimesUtils();

    public BedtimeSleepDialog() {
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
        return (title != null ? title : context.getString(R.string.configLabel_sleepCycles));
    }

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return a Dialog ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SuntimesUtils.initDisplayStrings(getActivity());

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_sleepcycle, null);   // TODO

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(getDialogTitle(myParent));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.dialog_cancel),
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

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.dialog_ok),
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
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, myParent.getString(R.string.configAction_help), (DialogInterface.OnClickListener) null);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {    // AlertDialog.neutralButton calls dismiss unless the listener is initially null
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    button.setOnClickListener(onHelpButtonClicked);
                }
            });
        }

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);
        }
        initViews(myParent, dialogContent);
        updateViews(getContext());
        return dialog;
    }

    protected void showHelpDialog()
    {
        CharSequence helpContent = helpContent();
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpContent != null ? helpContent : "");
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl())));
            }
        }, helpTag());
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    private final View.OnClickListener onHelpButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelpDialog();
        }
    };

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void initViews( final Context context, View dialogContent )
    {
        sleepCycleLabel = (TextView) dialogContent.findViewById(R.id.text_numCycles);
        sleepCyclePicker = (NumberPicker) dialogContent.findViewById(R.id.pick_numCycles);
        if (sleepCyclePicker != null)
        {
            sleepCyclePicker.setMinValue(1);
            sleepCyclePicker.setMaxValue(10);
            sleepCyclePicker.setOnValueChangedListener(onValueChangeListener);
        }
    }

    private final NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener()
    {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal)
        {
            setNumCycles(newVal);
            updateViews(getActivity());
        }
    };

    private void updateViews(Context context)
    {
        float numCycles = getNumCycles();
        if (sleepCyclePicker != null)
        {
            sleepCyclePicker.setOnValueChangedListener(null);
            sleepCyclePicker.setValue((int) numCycles);
            sleepCyclePicker.setOnValueChangedListener(onValueChangeListener);
        }
        if (sleepCycleLabel != null)
        {
            long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(context);
            long totalSleepMs = (long) (sleepCycleMs * numCycles);

            String sleepCycleMsText = utils.timeDeltaLongDisplayString(sleepCycleMs);
            String totalSleepMsText = utils.timeDeltaLongDisplayString(totalSleepMs);
            String numCyclesText = getResources().getQuantityString(R.plurals.cyclePlural, (int)numCycles, (int)numCycles);

            String sleepCycleText = getString(R.string.configLabel_numSleepCycles, numCyclesText, sleepCycleMsText, totalSleepMsText);
            SpannableString sleepCycleDisplay = SuntimesUtils.createBoldSpan(null, sleepCycleText, sleepCycleMsText);
            sleepCycleDisplay = SuntimesUtils.createBoldSpan(sleepCycleDisplay, sleepCycleText, sleepCycleMsText);
            sleepCycleDisplay = SuntimesUtils.createBoldSpan(sleepCycleDisplay, sleepCycleText, totalSleepMsText);
            sleepCycleLabel.setText(sleepCycleDisplay);
        }
    }

    public void setNumCycles(float value)
    {
        getArgs().putFloat("numCycles", value);
        updateViews(getContext());
    }
    public float getNumCycles() {
        return getArgs().getFloat("numCycles", BedtimeSettings.PREF_DEF_SLEEPCYCLE_COUNT);
    }

    protected void loadSettings(Bundle bundle) {
    }

    protected void saveSettings(Bundle bundle) {
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

}