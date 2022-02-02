/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class TimeDateDialog extends BottomSheetDialogFragment
{
    public static final String KEY_TIMEDATE_APPWIDGETID = "appwidgetid";

    protected DatePicker picker;

    protected TimeZone timezone = Calendar.getInstance().getTimeZone();
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }
    public TimeZone getTimeZone() {
        return timezone;
    }

    public void init(Calendar date)
    {
        init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    public void init(WidgetSettings.DateInfo info)
    {
        init(info.getYear(), info.getMonth(), info.getDay());
    }
    public void init(int year, int month, int day)
    {
        picker.init(year, month, day, null);
    }

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    protected void initViews(Context context, View dialogContent)
    {
        picker = (DatePicker) dialogContent.findViewById(R.id.appwidget_date_custom);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(onDialogCancelClick);

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(onDialogAcceptClick);

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        btn_neutral.setOnClickListener(onDialogNeutralClick);
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
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //Log.d("DEBUG", "TimeDateDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle state loaded from this Bundle
     */
    protected void loadSettings(Bundle bundle)
    {
        appWidgetId = bundle.getInt(KEY_TIMEDATE_APPWIDGETID, appWidgetId);
    }

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        WidgetSettings.DateMode mode = WidgetSettings.loadDateModePref(context, appWidgetId);
        if (mode == WidgetSettings.DateMode.CURRENT_DATE)
        {
            init(Calendar.getInstance(timezone));

        } else {
            WidgetSettings.DateInfo dateInfo = WidgetSettings.loadDatePref(context, appWidgetId);
            init(dateInfo);
        }
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        WidgetSettings.DateMode dateMode = (isToday() ? WidgetSettings.DateMode.CURRENT_DATE : WidgetSettings.DateMode.CUSTOM_DATE);
        WidgetSettings.saveDateModePref(context, appWidgetId, dateMode);

        WidgetSettings.DateInfo dateInfo = getDateInfo();
        WidgetSettings.saveDatePref(context, appWidgetId, dateInfo);
    }

    public WidgetSettings.DateInfo getDateInfo() {
        return new WidgetSettings.DateInfo(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle state persisted to this Bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putInt(KEY_TIMEDATE_APPWIDGETID, appWidgetId);
    }

    /**
     * @return the appWidgetID used by this dialog when saving/loading prefs (use 0 for main app)
     */
    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
    }
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

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
        public void onShow(DialogInterface dialog) {
            // EMPTY; placeholder
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

    protected void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
