package com.forrestguice.support.content;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

public class ContextCompat extends android.support.v4.content.ContextCompat
{
    /**
     * ResourcesCompat
     */
    @android.support.annotation.Nullable
    public static android.graphics.drawable.Drawable getDrawable(@android.support.annotation.NonNull android.content.res.Resources res, @android.support.annotation.DrawableRes int id, @android.support.annotation.Nullable android.content.res.Resources.Theme theme) throws android.content.res.Resources.NotFoundException {
        return ResourcesCompat.getDrawable(res, id, theme);
    }

    /**
     * DrawableCompat
     */
    public static void setTint(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable, @android.support.annotation.ColorInt int tint) {
        DrawableCompat.setTint(drawable, tint);
    }
    public static void setTintMode(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable, @android.support.annotation.NonNull android.graphics.PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(drawable, tintMode);
    }
    public static android.graphics.drawable.Drawable wrap(@android.support.annotation.NonNull android.graphics.drawable.Drawable drawable) {
        return DrawableCompat.wrap(drawable);
    }
}