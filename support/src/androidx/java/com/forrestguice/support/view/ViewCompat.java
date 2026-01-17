package com.forrestguice.support.view;

import android.content.res.ColorStateList;
import androidx.core.widget.CompoundButtonCompat;

import android.view.View;
import android.widget.CompoundButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public class ViewCompat extends androidx.core.view.ViewCompat
{
    public static void setButtonTintList(@NonNull CompoundButton button, @Nullable ColorStateList tint) {
        CompoundButtonCompat.setButtonTintList(button, tint);
    }

    @SuppressWarnings("deprecation")
    public static float getTranslationX(View view) {
        return view.getTranslationX();
    }

    @SuppressWarnings("deprecation")
    public static float getTranslationY(View view) {
        return view.getTranslationY();
    }
}