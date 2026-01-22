package com.forrestguice.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

public class CheckBoxPreference extends android.preference.CheckBoxPreference
{
    public CheckBoxPreference(Context context) {
        super(context);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(CheckBoxPreference preference);
    }
    public void setOnPreferenceClickListener( CheckBoxPreference.OnPreferenceClickListener listener )
    {
        setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                return listener.onPreferenceClick(CheckBoxPreference.this);
            }
        });
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(CheckBoxPreference preference, Object value);
    }
    public void setOnPreferenceChangeListener( CheckBoxPreference.OnPreferenceChangeListener listener )
    {
        setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object o) {
                return listener.onPreferenceChange(CheckBoxPreference.this, o);
            }
        });
    }
}
