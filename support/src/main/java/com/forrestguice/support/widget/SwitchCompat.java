package com.forrestguice.support.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SwitchCompat extends android.support.v7.widget.SwitchCompat
{
    public SwitchCompat(Context context) {
        super(context);
    }

    public SwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}