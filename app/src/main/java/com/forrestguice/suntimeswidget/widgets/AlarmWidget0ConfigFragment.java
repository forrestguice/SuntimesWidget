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
package com.forrestguice.suntimeswidget.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.forrestguice.suntimeswidget.R;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_TYPES;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_TYPES;

public class AlarmWidget0ConfigFragment extends DialogFragment
{
    public AlarmWidget0ConfigFragment()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putStringArray(PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES);
        defaultArgs.putInt(PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER);
        defaultArgs.putBoolean(PREF_KEY_ALARMWIDGET_ENABLEDONLY, PREF_DEF_ALARMWIDGET_ENABLEDONLY);
        defaultArgs.putBoolean(PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
        setArguments(defaultArgs);
    }

    protected int getLayoutResID() {
        return R.layout.layout_settings_general_more_alarmwidget;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        super.onCreate(savedState);
        View dialogContent = inflater.inflate(getLayoutResID(), null);
        initViews(getActivity(), dialogContent);
        updateViews(getContext());
        return dialogContent;
    }

    protected CheckBox check_enabledOnly;
    protected CheckBox check_showIcons;

    protected void initViews( final Context context, View dialogContent )
    {
        check_enabledOnly = (CheckBox) dialogContent.findViewById(R.id.check_enabledOnly);
        if (check_enabledOnly != null) {
            check_enabledOnly.setOnCheckedChangeListener(onCheckedChangedListener(PREF_KEY_ALARMWIDGET_ENABLEDONLY));
        }

        check_showIcons = (CheckBox) dialogContent.findViewById(R.id.check_showIcons);
        if (check_showIcons != null) {
            check_showIcons.setOnCheckedChangeListener(onCheckedChangedListener(PREF_KEY_ALARMWIDGET_SHOWICONS));
        }

        // TODO
    }

    protected void updateViews(Context context)
    {
        if (!isAdded()) {
            return;
        }

        if (check_enabledOnly != null) {
            check_enabledOnly.setChecked(getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_ENABLEDONLY, check_enabledOnly.isChecked()));
        }
        if (check_showIcons != null) {
            check_showIcons.setChecked(getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_SHOWICONS, check_showIcons.isChecked()));
        }

        // TODO
    }

    /**
     * onCheckedChangedListener
     */
    protected CompoundButton.OnCheckedChangeListener onCheckedChangedListener(final String key)
    {
        return new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                setAlarmWidgetValue(key, isChecked);
                if (listener != null) {
                    listener.onChanged(AlarmWidget0ConfigFragment.this, key);
                }
            }
        };
    }

    /**
     * setAlarmWidgetValue
     */
    public void setAlarmWidgetValue(String key, String value) {
        getArguments().putString(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, boolean value) {
        getArguments().putBoolean(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, int value) {
        getArguments().putInt(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, String[] value) {
        getArguments().putStringArray(key, value);
        updateViews(getActivity());
    }

    /**
     * getAlarmWidgetValue
     */
    public String getAlarmWidgetString(String key, String defaultValue) {
        return getArguments().getString(key, defaultValue);
    }
    public int getAlarmWidgetInt(String key, int defaultValue) {
        return getArguments().getInt(key, defaultValue);
    }
    public boolean getAlarmWidgetBool(String key, boolean defaultValue) {
        return getArguments().getBoolean(key, defaultValue);
    }
    public String[] getAlarmWidgetStringSet(String key, String[] defaultValue) {
        String[] value = getArguments().getStringArray(key);
        return (value != null ? value : defaultValue);
    }

    /**
     * DialogListener
     */
    public interface DialogListener {
        void onChanged(AlarmWidget0ConfigFragment dialog, String key);
    }

    private DialogListener listener = null;
    public void setDialogListener( DialogListener listener ) {
        this.listener = listener;
    }

}