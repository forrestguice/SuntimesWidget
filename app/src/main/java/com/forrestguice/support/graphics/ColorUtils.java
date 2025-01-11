package com.forrestguice.support.graphics;

public class ColorUtils
{
    public static void colorToLAB(@android.support.annotation.ColorInt int color, @android.support.annotation.NonNull double[] outLab) {
        android.support.v4.graphics.ColorUtils.colorToLAB(color, outLab);
    }

    public static int compositeColors(@android.support.annotation.ColorInt int foreground, @android.support.annotation.ColorInt int background) {
        return android.support.v4.graphics.ColorUtils.compositeColors(foreground, background);
    }

    public static double distanceEuclidean(@android.support.annotation.NonNull double[] labX, @android.support.annotation.NonNull double[] labY) {
        return android.support.v4.graphics.ColorUtils.distanceEuclidean(labX, labY);
    }

    @android.support.annotation.ColorInt
    public static int setAlphaComponent(@android.support.annotation.ColorInt int color, @android.support.annotation.IntRange(from = 0L, to = 255L) int alpha) {
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(color, alpha);
    }
}
