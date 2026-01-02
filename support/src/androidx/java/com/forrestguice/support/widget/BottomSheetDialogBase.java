/**
    Copyright (C) 2025 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.support.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public abstract class BottomSheetDialogBase extends BottomSheetDialogFragment
{
    public static int getBottomSheetResourceID() {
        return android.support.design.R.id.design_bottom_sheet;    // support libraries
        //return com.google.android.material.R.id.design_bottom_sheet;   // androidx
    }

    public BottomSheetDialogBase() {
        setArguments(new Bundle());
    }

    public static void initPeekHeight(DialogInterface dialog, int bottomViewResId)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(getBottomSheetResourceID());
            if (layout != null)
            {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
                View divider1 = bottomSheet.findViewById(bottomViewResId);
                if (divider1 != null)
                {
                    Rect headerBounds = new Rect();
                    divider1.getDrawingRect(headerBounds);
                    layout.offsetDescendantRectToMyCoords(divider1, headerBounds);
                    behavior.setPeekHeight(headerBounds.bottom); // + (int)getResources().getDimension(R.dimen.dialog_margin));

                } else {
                    behavior.setPeekHeight(-1);
                }
            }
        }
    }

    @NonNull
    public Bundle getArgs()
    {
        Bundle args = getArguments();
        Bundle retValue = args;
        if (args == null) {
            setArguments(retValue = new Bundle());
        }
        return retValue;
    }

    protected FrameLayout getBottomSheetFrameLayout(DialogInterface dialog) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        return (FrameLayout) bottomSheet.findViewById(getSheetFrameId());
    }

    protected boolean getBottomSheetBehavior_skipCollapsed() {
        return true;
    }
    protected boolean getBottomSheetBehavior_hideable() {
        return false;
    }
    protected int getPeekViewId() {
        return 0;
    }
    protected int getPeekHeight() {
        return -1;
    }
    protected int getSheetFrameId() {
        return getBottomSheetResourceID();
    }

    protected void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior<?> bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    protected void expandSheet(final DialogInterface dialog, long afterDelay)
    {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(getSheetFrameId());
        if (layout != null)
        {
            final BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
            if (getPeekViewId() != 0) {
                initPeekHeight(dialog, getPeekViewId());
            } else if (getPeekHeight() >= 0) {
                behavior.setPeekHeight(getPeekHeight());
            }

            layout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    expandSheet(dialog);
                }
            }, afterDelay);
        }
    }

    protected void collapseSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior<?> bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    public boolean isCollapsed()
    {
        BottomSheetBehavior<?> bottomSheet = initSheet(getDialog());
        if (bottomSheet != null) {
            return (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED);
        }
        return false;
    }

    @Nullable
    protected BottomSheetBehavior<?> initSheet(DialogInterface dialog)
    {
        if (dialog != null)
        {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(getSheetFrameId());
            if (layout != null)
            {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(getBottomSheetBehavior_hideable());
                behavior.setSkipCollapsed(getBottomSheetBehavior_skipCollapsed());

                if (getPeekViewId() != 0) {
                    initPeekHeight(dialog, getPeekViewId());

                } else if (getPeekHeight() >= 0) {
                    behavior.setPeekHeight(getPeekHeight());
                }
                return behavior;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (!BottomSheetDialogBase.this.onBackPressed()) {
                    super.onBackPressed();
                }
            }
        };
    }

    /**
     * @return true handled, false should call to super
     */
    protected boolean onBackPressed() {
        return false;
    }

}