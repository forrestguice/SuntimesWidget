package com.forrestguice.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

public class DialogPreference extends android.preference.DialogPreference
{
    public DialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public DialogPreference(Context context) {
        super(context);
    }

    @TargetApi(21)
    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
