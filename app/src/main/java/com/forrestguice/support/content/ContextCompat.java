package com.forrestguice.support.content;

import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

public class ContextCompat extends android.support.v4.content.ContextCompat
{
    /**
     * ResourcesCompat
     */
    @Nullable
    public static android.graphics.drawable.Drawable getDrawable(@android.support.annotation.NonNull android.content.res.Resources res, @android.support.annotation.DrawableRes int id, @android.support.annotation.Nullable android.content.res.Resources.Theme theme) throws android.content.res.Resources.NotFoundException {
        return ResourcesCompat.getDrawable(res, id, theme);
    }
}