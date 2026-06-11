package com.forrestguice.support.content;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class ContextCompat extends androidx.core.content.ContextCompat
{
    /**
     * ResourcesCompat
     */
    @androidx.annotation.Nullable
    public static android.graphics.drawable.Drawable getDrawable(@androidx.annotation.NonNull android.content.res.Resources res, @androidx.annotation.DrawableRes int id, @androidx.annotation.Nullable android.content.res.Resources.Theme theme) throws android.content.res.Resources.NotFoundException {
        return ResourcesCompat.getDrawable(res, id, theme);
    }

    /**
     * DrawableCompat
     */
    public static void setTint(@androidx.annotation.NonNull android.graphics.drawable.Drawable drawable, @androidx.annotation.ColorInt int tint) {
        DrawableCompat.setTint(drawable, tint);
    }
    public static void setTintMode(@androidx.annotation.NonNull android.graphics.drawable.Drawable drawable, @androidx.annotation.NonNull android.graphics.PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(drawable, tintMode);
    }
    public static android.graphics.drawable.Drawable wrap(@androidx.annotation.NonNull android.graphics.drawable.Drawable drawable) {
        return DrawableCompat.wrap(drawable);
    }
}