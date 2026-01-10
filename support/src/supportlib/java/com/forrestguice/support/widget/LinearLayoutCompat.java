package com.forrestguice.support.widget;

import android.content.Context;
import android.util.AttributeSet;

public class LinearLayoutCompat extends android.support.v7.widget.LinearLayoutCompat
{
    public LinearLayoutCompat(Context context) {
        super(context);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}