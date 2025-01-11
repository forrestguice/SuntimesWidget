package com.forrestguice.support.design.widget;

import android.support.annotation.Nullable;
import android.support.v7.widget.SimpleItemAnimator;

public class RecyclerViewUtils
{
    public static void setChangeDuration(@Nullable android.support.v7.widget.RecyclerView view, long changeDuration)
    {
        if (view != null) {
            SimpleItemAnimator animator = (SimpleItemAnimator) view.getItemAnimator();
            if (animator != null) {
                animator.setChangeDuration(changeDuration);
            }
        }
    }
}
