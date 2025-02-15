package com.forrestguice.support.design.widget;

import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.forrestguice.support.annotation.Nullable;

public class BottomSheetBehaviorCompat
{
    @Nullable
    public static BottomSheetBehavior<?> getBottomSheetBehavior(@Nullable View view)
    {
        if (view != null) {
            return BottomSheetBehavior.from(view);
        }
        return null;
    }

    @Nullable
    public static BottomSheetBehaviorInterface create(final View view) {
        return create(getBottomSheetBehavior(view));
    }

    @Nullable
    public static BottomSheetBehaviorInterface create(final BottomSheetBehavior<?> behavior)
    {
        if (behavior != null)
        {
            return new BottomSheetBehaviorInterface()
            {
                @Override
                public void setState(int state) {
                    behavior.setState(state);
                }

                @Override
                public int getState() {
                    return behavior.getState();
                }

                @Override
                public void setHideable(boolean hideable) {
                    behavior.setHideable(hideable);
                }

                @Override
                public boolean isHideable() {
                    return behavior.isHideable();
                }

                @Override
                public void setSkipCollapsed(boolean value) {
                    behavior.setSkipCollapsed(value);
                }

                @Override
                public boolean getSkipCollapsed() {
                    return behavior.getSkipCollapsed();
                }

                @Override
                public void setPeekHeight(int peekHeight) {
                    behavior.setPeekHeight(peekHeight);
                }

                @Override
                public int getPeekHeight() {
                    return behavior.getPeekHeight();
                }

                @Override
                public void setBottomSheetCallback(BottomSheetCallbackCompat callback) {
                    behavior.setBottomSheetCallback(callback.get());
                }
            };
        }
        return null;
    }


}
