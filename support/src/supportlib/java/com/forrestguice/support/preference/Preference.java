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
}
