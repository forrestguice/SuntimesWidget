package com.forrestguice.support.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class DrawerLayout extends androidx.drawerlayout.widget.DrawerLayout
{
    public DrawerLayout(@NonNull Context context) {
        super(context);
    }

    public DrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}