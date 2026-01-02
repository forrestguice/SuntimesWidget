package com.forrestguice.support.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class CoordinatorLayout extends android.support.design.widget.CoordinatorLayout
{
    public CoordinatorLayout(@NonNull Context context) {
        super(context);
    }

    public CoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}