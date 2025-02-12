package com.forrestguice.support.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;

public class AppCompatTextView extends android.support.v7.widget.AppCompatTextView
{
    public AppCompatTextView(Context context) {
        super(context);
    }

    public AppCompatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCompatTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
