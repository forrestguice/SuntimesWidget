package com.forrestguice.support.drawerlayout.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;

public class DrawerLayout extends android.support.v4.widget.DrawerLayout
{
    public static final int START = GravityCompat.START;
    public static final int END = GravityCompat.END;

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
