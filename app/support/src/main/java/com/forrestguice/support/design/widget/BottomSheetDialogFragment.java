package com.forrestguice.support.design.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.app.FragmentManagerCompat;
import com.forrestguice.support.design.app.FragmentManagerInterface;

public class BottomSheetDialogFragment extends android.support.design.widget.BottomSheetDialogFragment
{
    public FragmentManagerInterface getFragmentManagerCompat() {
        return FragmentManagerCompat.create(getFragmentManager());
    }

    public FragmentManagerInterface getChildFragmentManagerCompat() {
        return FragmentManagerCompat.create(getChildFragmentManager());
    }

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
    public static BottomSheetBehavior<?> getBottomSheetBehavior(@Nullable DialogInterface dialog) {
        return BottomSheetBehaviorCompat.getBottomSheetBehavior(getBottomSheetLayout(dialog));
    }

    @Nullable
    protected BottomSheetBehaviorInterface initBottomSheetBehavior() {
        return initBottomSheetBehavior(getDialog());
    }

    @Nullable
    protected BottomSheetBehaviorInterface initBottomSheetBehavior(DialogInterface dialog) {
        return BottomSheetBehaviorCompat.create(getBottomSheetBehavior(dialog));
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
     * OnBackPressed
     */
    public interface OnBackPressed
    {
        /**
         * @return true handled (return), false unhandled (call super)
         */
        boolean onBackPressed();
    }

    public BottomSheetDialog createBottomSheetDialog(Context context, final OnBackPressed onBackPressed) {
        return createBottomSheetDialog(context, getTheme(), onBackPressed);
    }
    public static BottomSheetDialog createBottomSheetDialog(Context context, final int themeResId, final OnBackPressed onBackPressed)
    {
        return new BottomSheetDialog(context, themeResId)
        {
            @Override
            public void onBackPressed() {
                if (!onBackPressed.onBackPressed()) {
                    super.onBackPressed();
                }
            }
        };
    }

    public static void setCancelable(DialogInterface dialog, boolean value)
    {
        try {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            bottomSheet.setCancelable(value);

        } catch (Exception e) {
            Log.e("setCancelable", "failed to set cancelable: " + e);
        }
    }

}