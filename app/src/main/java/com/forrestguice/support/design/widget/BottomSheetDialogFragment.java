package com.forrestguice.support.design.widget;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.FrameLayout;

import com.forrestguice.support.annotation.Nullable;

public class BottomSheetDialogFragment extends android.support.design.widget.BottomSheetDialogFragment
{
    public static int getBottomSheetResID() {
        //return com.google.android.material.R.id.design_bottom_sheet;    // for androidx
        return android.support.design.R.id.design_bottom_sheet;           // for support libs
    }

    @Nullable
    public static FrameLayout getBottomSheetLayout(@Nullable DialogInterface dialog)
    {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        if (bottomSheet != null) {
            return (FrameLayout) bottomSheet.findViewById(getBottomSheetResID());
        }
        return null;
    }

    @Nullable
    public static BottomSheetBehavior<?> getBottomSheetBehavior(@Nullable DialogInterface dialog)
    {
        FrameLayout layout = getBottomSheetLayout(dialog);
        if (layout != null) {
            return BottomSheetBehavior.from(layout);
        }
        return null;
    }

    @Nullable
    protected BottomSheetBehaviorCompat initBottomSheetBehavior() {
        return initBottomSheetBehavior(getDialog());
    }

    @Nullable
    protected BottomSheetBehaviorCompat initBottomSheetBehavior(DialogInterface dialog)
    {
        final BottomSheetBehavior<?> behavior = getBottomSheetBehavior(dialog);
        if (behavior != null)
        {
            return new BottomSheetBehaviorCompat()
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
            };
        }
        return null;
    }

    public static void initPeekHeight(DialogInterface dialog, int bottomViewResId) {
        initPeekHeight(dialog, bottomViewResId, true);
    }
    public static void initPeekHeight(DialogInterface dialog, int bottomViewResId, boolean toBottom)
    {
        if (dialog != null)
        {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(getBottomSheetResID());
            if (layout != null)
            {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
                View divider1 = bottomSheet.findViewById(bottomViewResId);
                if (divider1 != null)
                {
                    Rect headerBounds = new Rect();
                    divider1.getDrawingRect(headerBounds);
                    layout.offsetDescendantRectToMyCoords(divider1, headerBounds);
                    behavior.setPeekHeight(toBottom ? headerBounds.bottom : headerBounds.top); // + (int)getResources().getDimension(R.dimen.dialog_margin));

                } else {
                    behavior.setPeekHeight(-1);
                }
            }
        }
    }

    /**
     * BottomSheetBehaviorCompat
     */
    public interface BottomSheetBehaviorCompat
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
    }
}