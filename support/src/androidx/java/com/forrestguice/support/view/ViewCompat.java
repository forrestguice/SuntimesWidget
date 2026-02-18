package com.forrestguice.support.view;

import android.content.res.ColorStateList;

import androidx.core.widget.CompoundButtonCompat;

import android.view.View;
import android.widget.CompoundButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public class ViewCompat // extends androidx.core.view.ViewCompat  // ... default constructor decrecated!
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

    public static void setBackgroundTintList(@NonNull View view, ColorStateList tintList) {
        androidx.core.view.ViewCompat.setBackgroundTintList(view, tintList);
    }

    @Nullable
    public static String getTransitionName(@NonNull View view) {
        return androidx.core.view.ViewCompat.getTransitionName(view);
    }

    public static void setTransitionName(@NonNull View view, String transitionName) {
        androidx.core.view.ViewCompat.setTransitionName(view, transitionName);
    }

}