package com.forrestguice.support.design.widget;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

public interface BottomSheetBehaviorInterface
{
    int STATE_DRAGGING = BottomSheetBehavior.STATE_DRAGGING;
    int STATE_SETTLING = BottomSheetBehavior.STATE_SETTLING;
    int STATE_EXPANDED = BottomSheetBehavior.STATE_EXPANDED;
    int STATE_COLLAPSED = BottomSheetBehavior.STATE_COLLAPSED;
    int STATE_HIDDEN = BottomSheetBehavior.STATE_HIDDEN;
    int STATE_HALF_EXPANDED = BottomSheetBehavior.STATE_HALF_EXPANDED;

    void setState(int state);
    int getState();

    void setHideable(boolean hideable);
    boolean isHideable();

    void setSkipCollapsed(boolean value);
    boolean getSkipCollapsed();

    void setPeekHeight(int peekHeight);
    int getPeekHeight();

    void setBottomSheetCallback(BottomSheetCallbackCompat callback);

    /**
     * BottomSheetCallbackCompat
     */
    abstract class BottomSheetCallbackCompat
    {
        public abstract void onStateChanged(View view, int i);
        public abstract void onSlide(View view, float v);

        public BottomSheetBehavior.BottomSheetCallback get()
        {
            return new BottomSheetBehavior.BottomSheetCallback()
            {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    BottomSheetCallbackCompat.this.onStateChanged(view, i);
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                    BottomSheetCallbackCompat.this.onSlide(view, v);
                }
            };
        }
    }
}
