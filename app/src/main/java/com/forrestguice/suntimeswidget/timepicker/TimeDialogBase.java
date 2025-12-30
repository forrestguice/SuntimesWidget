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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.dialog.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Calendar;

public abstract class TimeDialogBase extends BottomSheetDialogBase
{
    public static final String KEY_DIALOG_TITLE = "dialog_title";
    public static final String KEY_DIALOG_IS24 = "dialog_is24";
    private static final String KEY_DIALOG_NEUTRAL_LABEL = "dialog_neutral_label";

    protected ImageButton acceptButton;

    public TimeDialogBase() {
        super();
    }

    protected abstract int getDialogLayoutResID();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(getDialogLayoutResID(), parent, false);
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }
        return dialogContent;
    }

    protected void initViews(Context context, View dialogContent)
    {
        ImageButton cancelButton = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (cancelButton != null) {
            TooltipCompat.setTooltipText(cancelButton, cancelButton.getContentDescription());
            cancelButton.setOnClickListener(onDialogCancelClick);
            if (AppSettings.isTelevision(context)) {
                cancelButton.setFocusableInTouchMode(true);
            }
        }

        acceptButton = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        if (acceptButton != null) {
            TooltipCompat.setTooltipText(acceptButton, acceptButton.getContentDescription());
            acceptButton.setOnClickListener(onDialogAcceptClick);
        }

        Button neutralButton = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        if (neutralButton != null)
        {
            if (getNeutralButtonLabel() != null) {
                neutralButton.setText(getNeutralButtonLabel());
                neutralButton.setOnClickListener(onDialogNeutralClick);
            } else {
                neutralButton.setVisibility(View.GONE);
            }
        }

        View dialogFooter = dialogContent.findViewById(R.id.dialog_footer);
        if (dialogFooter != null) {
            dialogFooter.setVisibility(getNeutralButtonLabel() != null ? View.VISIBLE : View.GONE);
        }

        String title = getDialogTitle();
        TextView text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);
        if (text_title != null && title != null) {
            text_title.setText(title);
        }
    }

    @Nullable
    protected CharSequence getNeutralButtonLabel() {
        return getArgs().getCharSequence(KEY_DIALOG_NEUTRAL_LABEL);
    }
    public void setNeutralButtonLabel(CharSequence value) {
        getArgs().putCharSequence(KEY_DIALOG_NEUTRAL_LABEL, value);
    }

    @SuppressWarnings({"RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle) {}
    protected void saveSettings(Bundle bundle) {}

    public void loadSettings(@Nullable Activity activity) {}
    public void saveSettings(@Nullable Activity activity) {}

    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    private DialogInterface.OnClickListener onNeutral = null;
    public void setOnNeutralListener( DialogInterface.OnClickListener listener ) {
        onNeutral = listener;
    }

    private DialogInterface.OnShowListener onShowListener;
    public void setOnShowListener( DialogInterface.OnShowListener listener ) {
        onShowListener = listener;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    protected DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            ViewUtils.initPeekHeight(dialog, getPeekViewId());
            if (onShowListener != null) {
                onShowListener.onShow(dialog);
            }

            if (AppSettings.isTelevision(getActivity())) {
                acceptButton.requestFocus();
            }
        }
    };

    protected View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            onDialogNeutralClick(v);
        }
    };
    protected void onDialogNeutralClick(View v) {
        if (onNeutral != null) {
            onNeutral.onClick(getDialog(), DialogInterface.BUTTON_NEUTRAL);
        }
    }

    protected View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    protected View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            onDialogAccept();
        }
    };
    protected void onDialogAccept()
    {
        saveSettings(getActivity());
        dismiss();
        if (onAccepted != null) {
            onAccepted.onClick(getDialog(), 0);
        }
    }

    @Override
    protected int getPeekViewId() {
        return R.id.dialog_footer;
    }

    public void setTimeIs24(boolean is24) {
        getArgs().putBoolean(KEY_DIALOG_IS24, is24);
    }
    public boolean timeIs24() {
        return getArgs().getBoolean(KEY_DIALOG_IS24, true);
    }

    @Nullable
    public String getDialogTitle() {
        return getArgs().getString(KEY_DIALOG_TITLE);
    }
    public void setDialogTitle(@Nullable String title) {
        getArgs().putString(KEY_DIALOG_TITLE, title);
    }

    public abstract TimeDialogResult getSelected();
    public interface TimeDialogResult
    {
        Integer getYear();
        Integer getMonth();
        Integer getDay();
        Integer getHour();
        Integer getMinute();
        Integer getSecond();
    }

    @Nullable
    public static Calendar getCalendar(TimeDialogResult dateTime, Calendar now) {
        return getCalendar(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), dateTime.getHour(), dateTime.getMinute(), now);
    }
    @Nullable
    public static Calendar getCalendar(Integer year, Integer month, Integer day, Integer hour, Integer minute, Calendar now )
    {
        Calendar calendar = Calendar.getInstance(now.getTimeZone());
        calendar.setTimeInMillis(now.getTimeInMillis());
        if (year != null) {
            calendar.set(Calendar.YEAR, year);
        }
        if (month != null) {
            calendar.set(Calendar.MONTH, month);
        }
        if (day != null) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
        }
        if (hour != null) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            calendar.set(Calendar.MINUTE, minute);
        }
        return calendar;
    }
}