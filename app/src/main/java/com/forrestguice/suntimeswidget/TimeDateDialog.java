/**
    Copyright (C) 2018-2022 Forrest Guice
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

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.DateInfo;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeDateDialog extends BottomSheetDialogBase
{
    public static final String KEY_TIMEDATE_APPWIDGETID = "appwidgetid";
    public static final String KEY_DIALOG_TITLE = "dialog_title";

    protected DatePicker picker;

    protected TimeZone timezone = Calendar.getInstance().getTimeZone();
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }
    public TimeZone getTimeZone() {
        return timezone;
    }

    public TimeDateDialog() {
        setArguments(new Bundle());
    }

    public void init(Calendar date)
    {
        init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    public void init(DateInfo info)
    {
        init(info.getYear(), info.getMonth(), info.getDay());
    }
    public void init(int year, int month, int day)
    {
        picker.init(year, month, day, null);
    }

    protected ImageButton btn_accept;

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    protected void initViews(Context context, View dialogContent)
    {
        picker = (DatePicker) dialogContent.findViewById(R.id.appwidget_date_custom);

        if (Build.VERSION.SDK_INT >= 11)
        {
            if (getArgs().containsKey(KEY_MIN_DATETIME)) {
                picker.setMinDate(getArgs().getLong(KEY_MIN_DATETIME));
            }
            if (getArgs().containsKey(KEY_MAX_DATETIME)) {
                picker.setMaxDate(getArgs().getLong(KEY_MAX_DATETIME));
            }
        }

        ImageButton btn_cancel = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
        TooltipCompat.setTooltipText(btn_cancel, btn_cancel.getContentDescription());
        btn_cancel.setOnClickListener(onDialogCancelClick);
        if (AppSettings.isTelevision(getActivity())) {
            btn_cancel.setFocusableInTouchMode(true);
        }

        btn_accept = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        TooltipCompat.setTooltipText(btn_accept, btn_accept.getContentDescription());
        btn_accept.setOnClickListener(onDialogAcceptClick);

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        btn_neutral.setOnClickListener(onDialogNeutralClick);

        String title = getDialogTitle();
        TextView text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);
        if (text_title != null && title != null) {
            text_title.setText(title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_date1, parent, false);

        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        } else {
            loadSettings(getActivity());
        }

        return dialogContent;
    }

    /**
     *
     * @param savedInstanceState a bundle containing previously saved dialog state
     * @return a dialog instance ready to be shown
     */
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
        //Log.d("DEBUG", "TimeDateDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle state loaded from this Bundle
     */
    protected void loadSettings(Bundle bundle) {
        //getArgs().putInt(KEY_TIMEDATE_APPWIDGETID, bundle.getInt(KEY_TIMEDATE_APPWIDGETID, getAppWidgetId()));
        //getArgs().putString(KEY_DIALOG_TITLE, bundle.getString(KEY_DIALOG_TITLE, null));
    }

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        int appWidgetId = getAppWidgetId();
        DateMode mode = WidgetSettings.loadDateModePref(context, appWidgetId);
        if (mode == DateMode.CURRENT_DATE)
        {
            init(getInitialDateTime());

        } else {
            DateInfo dateInfo = WidgetSettings.loadDatePref(context, appWidgetId);
            init(dateInfo);
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context) {
        /* EMPTY */
    }

    public DateInfo getDateInfo() {
        return new DateInfo(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle state persisted to this Bundle
     */
    protected void saveSettings(Bundle bundle) {
    }

    /**
     * @return the appWidgetID used by this dialog when saving/loading prefs (use 0 for main app)
     */
    public int getAppWidgetId() {
        return getArgs().getInt(KEY_TIMEDATE_APPWIDGETID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    public void setAppWidgetId(int value) {
        getArgs().putInt(KEY_TIMEDATE_APPWIDGETID, value);
    }

    @Nullable
    public String getDialogTitle() {
        return getArgs().getString(KEY_DIALOG_TITLE);
    }
    public void setDialogTitle(@Nullable String title) {
        getArgs().putString(KEY_DIALOG_TITLE, title);
    }

    /**
     * A listener that is triggered when the dialog is accepted.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * A listener that is triggered when the dialog is cancelled.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    private DialogInterface.OnShowListener onShowListener;
    public void setOnShowListener( DialogInterface.OnShowListener listener ) {
        onShowListener = listener;
    }

    public boolean isToday()
    {
        Calendar date = Calendar.getInstance(timezone);
        return isToday(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }

    public boolean isToday(int year, int month, int day)
    {
        return (year == picker.getYear() && month == picker.getMonth() && day == picker.getDayOfMonth());
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
            BottomSheetDialogBase.initPeekHeight(dialog, R.id.dialog_footer);
            if (onShowListener != null) {
                onShowListener.onShow(dialog);
            }

            if (AppSettings.isTelevision(getActivity())) {
                btn_accept.requestFocus();
            }
        }
    };

    protected View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            boolean alreadyToday = isToday();
            init(Calendar.getInstance(timezone));
            if (alreadyToday) {
                onDialogAcceptClick.onClick(v);
            }
        }
    };

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
        public void onClick(View v)
        {
            saveSettings(getContext());
            dismiss();
            if (onAccepted != null) {
                onAccepted.onClick(getDialog(), 0);
            }
        }
    };

    public static final String KEY_MIN_DATETIME = "min_datetime";
    public void setMinDate(long datetime) {
        getArgs().putLong(KEY_MIN_DATETIME, datetime);
    }

    public static final String KEY_MAX_DATETIME = "max_datetime";
    public void setMaxDate(long datetime) {
        getArgs().putLong(KEY_MAX_DATETIME, datetime);
    }

    public static final String KEY_INITIAL_DATETIME = "initial_datetime";
    public void setInitialDateTime(long datetime) {
        getArgs().putLong(KEY_INITIAL_DATETIME, datetime);
    }
    public Calendar getInitialDateTime()
    {
        Calendar calendar = Calendar.getInstance(timezone);
        long datetime = getArgs().getLong(KEY_INITIAL_DATETIME, calendar.getTimeInMillis());
        calendar.setTimeInMillis(datetime);
        return calendar;
    }

}
