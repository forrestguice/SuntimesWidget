package com.forrestguice.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

public class Preference extends android.preference.Preference
{
    public Preference(Context context) {
        super(context);
    }
    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @TargetApi(21)
    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }
    public void setOnPreferenceClickListener( OnPreferenceClickListener listener )
    {
        setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                return listener.onPreferenceClick(Preference.this);
            }
        });
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object value);
    }
    public void setOnPreferenceChangeListener( OnPreferenceChangeListener listener )
    {
        setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object o) {
                return listener.onPreferenceChange(Preference.this, o);
            }
        });
    }
}
