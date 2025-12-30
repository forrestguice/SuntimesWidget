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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.dialog.DialogBase;

import static com.forrestguice.suntimeswidget.widgets.SolsticeWidgetSettings.PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER;
import static com.forrestguice.suntimeswidget.widgets.SolsticeWidgetSettings.PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER;

public class SolsticeWidget0ConfigFragment extends DialogBase
{
    protected CheckBox check_crossquarter;

    public SolsticeWidget0ConfigFragment()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putBoolean(PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER);
        setArguments(defaultArgs);
    }

    protected int getLayoutResID() {
        return R.layout.layout_settings_general_more_solsticewidget;
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

    protected void initViews( final Context context, View dialogContent )
    {
        check_crossquarter = (CheckBox) dialogContent.findViewById(R.id.check_show_crossquarter);
        if (check_crossquarter != null) {
            check_crossquarter.setOnCheckedChangeListener(onCheckedChangedListener(PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER));
        }
    }

    protected void updateViews(Context context)
    {
        if (!isAdded()) {
            return;
        }

        if (check_crossquarter != null) {
            check_crossquarter.setChecked(getWidgetBool(PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, check_crossquarter.isChecked()));
        }
    }

    /**
     * onCheckedChangedListener
     */
    protected CompoundButton.OnCheckedChangeListener onCheckedChangedListener(final String key)
    {
        return new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                setWidgetValue(key, isChecked);
                if (listener != null) {
                    listener.onChanged(SolsticeWidget0ConfigFragment.this, key);
                }
            }
        };
    }

    /**
     * setWidgetValue
     */
    public void setWidgetValue(String key, String value) {
        getArgs().putString(key, value);
        updateViews(getActivity());
    }
    public void setWidgetValue(String key, boolean value) {
        getArgs().putBoolean(key, value);
        updateViews(getActivity());
    }
    public void setWidgetValue(String key, int value) {
        getArgs().putInt(key, value);
        updateViews(getActivity());
    }
    public void setWidgetValue(String key, String[] value) {
        getArgs().putStringArray(key, value);
        updateViews(getActivity());
    }

    /**
     * getWidgetValue
     */
    public String getWidgetString(String key, String defaultValue) {
        return getArgs().getString(key, defaultValue);
    }
    public int getWidgetInt(String key, int defaultValue) {
        return getArgs().getInt(key, defaultValue);
    }
    public boolean getWidgetBool(String key, boolean defaultValue) {
        return getArgs().getBoolean(key, defaultValue);
    }
    public String[] getWidgetStringSet(String key, String[] defaultValue) {
        String[] value = getArgs().getStringArray(key);
        return (value != null ? value : defaultValue);
    }

    /**
     * DialogListener
     */
    public interface DialogListener {
        void onChanged(SolsticeWidget0ConfigFragment dialog, String key);
    }

    private DialogListener listener = null;
    public void setDialogListener( DialogListener listener ) {
        this.listener = listener;
    }

}