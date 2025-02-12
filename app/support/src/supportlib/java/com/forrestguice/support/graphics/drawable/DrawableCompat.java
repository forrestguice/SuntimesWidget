package com.forrestguice.support.graphics.drawable;

public class DrawableCompat
{
    public static void setTint(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable, @android.support.annotation.ColorInt int tint) {
        android.support.v4.graphics.drawable.DrawableCompat.setTint(drawable, tint);
    }

    public static void setTintMode(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable, @android.support.annotation.NonNull android.graphics.PorterDuff.Mode tintMode) {
        android.support.v4.graphics.drawable.DrawableCompat.setTintMode(drawable, tintMode);
    }

    public static android.graphics.drawable.Drawable wrap(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable) {
        return android.support.v4.graphics.drawable.DrawableCompat.wrap(drawable);
    }
}
