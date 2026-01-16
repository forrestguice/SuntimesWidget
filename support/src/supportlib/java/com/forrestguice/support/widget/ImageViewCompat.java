package com.forrestguice.support.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public class ImageViewCompat
{
    @Nullable
    public static ColorStateList getImageTintList(@NonNull android.widget.ImageView view) {
        return android.support.v4.widget.ImageViewCompat.getImageTintList(view);
    }

    public static void setImageTintList(@NonNull android.widget.ImageView view, @Nullable ColorStateList tintList) {
        android.support.v4.widget.ImageViewCompat.setImageTintList(view, tintList);
    }

    @Nullable
    public static PorterDuff.Mode getImageTintMode(@NonNull android.widget.ImageView view) {
        return android.support.v4.widget.ImageViewCompat.getImageTintMode(view);
    }

    public static void setImageTintMode(@NonNull android.widget.ImageView view, @Nullable PorterDuff.Mode mode) {
        android.support.v4.widget.ImageViewCompat.setImageTintMode(view, mode);
    }
}