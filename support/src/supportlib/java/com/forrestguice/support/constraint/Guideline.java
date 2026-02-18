package com.forrestguice.support.constraint;

import android.content.Context;
import android.util.AttributeSet;

public class Guideline extends android.support.constraint.Guideline
{
    public Guideline(Context context) {
        super(context);
    }

    public Guideline(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Guideline(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Guideline(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}