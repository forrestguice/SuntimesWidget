package com.forrestguice.support.view;

import android.graphics.Rect;

public class GravityCompat
{
    public static final int RELATIVE_LAYOUT_DIRECTION = android.support.v4.view.GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
    public static final int START = android.support.v4.view.GravityCompat.START;
    public static final int END = android.support.v4.view.GravityCompat.END;
    public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = android.support.v4.view.GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;

    public static void apply(int gravity, int w, int h, Rect container, Rect outRect, int layoutDirection) {
        GravityCompat.apply(gravity, w, h, container, outRect, layoutDirection);
    }

    public static void apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect, int layoutDirection) {
        GravityCompat.apply(gravity, w, h, container, xAdj, yAdj, outRect, layoutDirection);
    }

    public static void applyDisplay(int gravity, Rect display, Rect inoutObj, int layoutDirection) {
        GravityCompat.applyDisplay(gravity, display, inoutObj, layoutDirection);
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        return GravityCompat.getAbsoluteGravity(gravity, layoutDirection);
    }
}