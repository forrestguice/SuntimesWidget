package com.forrestguice.support.view;

import android.content.res.ColorStateList;
import android.support.v4.widget.CompoundButtonCompat;
import android.widget.CompoundButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public class ViewCompat extends android.support.v4.view.ViewCompat
{
    public static void setButtonTintList(@NonNull CompoundButton button, @Nullable ColorStateList tint) {
        CompoundButtonCompat.setButtonTintList(button, tint);
    }
}