package com.forrestguice.support.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class Toolbar extends androidx.appcompat.widget.Toolbar
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