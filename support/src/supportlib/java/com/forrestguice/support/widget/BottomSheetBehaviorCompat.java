package com.forrestguice.support.widget;

import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.forrestguice.annotation.NonNull;

public class BottomSheetBehaviorCompat
{
    public static final int STATE_DRAGGING = BottomSheetBehavior.STATE_DRAGGING;
    public static final int STATE_SETTLING = BottomSheetBehavior.STATE_SETTLING;
    public static final int STATE_EXPANDED = BottomSheetBehavior.STATE_EXPANDED;
    public static final int STATE_COLLAPSED = BottomSheetBehavior.STATE_COLLAPSED;
    public static final int STATE_HIDDEN = BottomSheetBehavior.STATE_HIDDEN;
    public static final int STATE_HALF_EXPANDED = BottomSheetBehavior.STATE_HALF_EXPANDED;

    public BottomSheetBehaviorCompat(View view) {
        behavior = BottomSheetBehavior.from(view);
    }

    public static BottomSheetBehaviorCompat from(View view) {
        return new BottomSheetBehaviorCompat(view);
    }

    public void setState(int state) {
        behavior.setState(state);
    }
    public int getState() {
        return behavior.getState();
    }

    protected BottomSheetBehavior<?> behavior;
    public BottomSheetBehavior<?> getBehavior() {
        return behavior;
    }

    public void setBottomSheetCallback(final BottomSheetCallback callback)
    {
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@android.support.annotation.NonNull View view, int i) {
                callback.onStateChanged(view, i);
            }

            @Override
            public void onSlide(@android.support.annotation.NonNull View view, float v) {
                callback.onSlide(view, v);
            }
        });
    }

    public static abstract class BottomSheetCallback
    {
        public abstract void onStateChanged(@NonNull View view, int i);
        public abstract void onSlide(@NonNull View view, float v);
    }
}