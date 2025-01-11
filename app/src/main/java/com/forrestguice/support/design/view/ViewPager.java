package com.forrestguice.support.design.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class ViewPager extends android.support.v4.view.ViewPager
{
    public ViewPager(@NonNull Context context) {
        super(context);
    }

    public ViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}