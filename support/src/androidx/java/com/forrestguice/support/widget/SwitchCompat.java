package com.forrestguice.support.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SwitchCompat extends androidx.appcompat.widget.SwitchCompat
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