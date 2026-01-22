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
}
