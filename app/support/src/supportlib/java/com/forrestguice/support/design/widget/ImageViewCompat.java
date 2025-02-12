package com.forrestguice.support.design.widget;

public class ImageViewCompat
{
    public static void setImageTintList(@android.support.annotation.NonNull android.widget.ImageView view, @android.support.annotation.Nullable android.content.res.ColorStateList tintList) {
        android.support.v4.widget.ImageViewCompat.setImageTintList(view, tintList);
    }
}
