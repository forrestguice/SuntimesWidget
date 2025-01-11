package com.forrestguice.support.design.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class Toolbar extends android.support.v7.widget.Toolbar
{
    public Toolbar(Context context) {
        super(context);
    }

    public Toolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Toolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
