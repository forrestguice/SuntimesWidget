/**
    Copyright (C) 2017-2021 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class EquinoxDialog extends BottomSheetDialogFragment
{
    private EquinoxView equinoxView;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_equinox, parent, false);

        equinoxView = (EquinoxView) dialogContent.findViewById(R.id.info_time_equinox);
        equinoxView.setTrackingMode(WidgetSettings.loadTrackingModePref(getContext(), 0));
        if (savedState != null)
        {
            Log.d("DEBUG", "EquinoxDialog onCreate (restoreState)");
            overrideColumnWidthPx = savedState.getInt("overrideColumnWidthPx", overrideColumnWidthPx);
            equinoxView.loadState(savedState);
        }
        themeViews(getContext());

        if (overrideColumnWidthPx >= 0) {
            equinoxView.adjustColumnWidth(overrideColumnWidthPx);
        }

        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog != null)
        {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null)
            {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Context context = getContext();
            if (context != null) {
                equinoxView.updateViews(getContext());
            } else Log.w("EquinoxDialog.onShow", "null context! skipping update");
        }
    };

    private void themeViews(Context context)
    {
        if (themeOverride != null) {
            equinoxView.themeViews(context, themeOverride);
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (equinoxView != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        if (equinoxView != null) {
            equinoxView.updateViews(getContext());
            Log.d("DEBUG", "EquinoxDialog updated");
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        equinoxView.saveState(outState);
        outState.putInt("overrideColumnWidthPx", overrideColumnWidthPx);
        super.onSaveInstanceState(outState);
    }

    private int overrideColumnWidthPx = -1;
    public void adjustColumnWidth(int columnWidthPx) {
        overrideColumnWidthPx = columnWidthPx;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private EquinoxDialogListener dialogListener = null;
    public void setDialogListener( EquinoxDialogListener listener ) {
        dialogListener = listener;
    }

    /**
     * DialogListener
     */
    public static class EquinoxDialogListener
    {
        public void onSetAlarm( WidgetSettings.SolsticeEquinoxMode suggestedEvent ) {}
        public void onShowMap( long suggestedDate ) {}
        public void onShowPosition( long suggestedDate ) {}
    }
}
