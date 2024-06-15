/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.colors;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class ColorValuesSheetDialog extends BottomSheetDialogFragment
{
    public ColorValuesSheetDialog() {
        setArguments(new Bundle());
    }

    public void setAppWidgetID(int id) {
        getArguments().putInt("appWidgetID", id);
        if (colorSheet != null) {
            colorSheet.setAppWidgetID(id);
        }
    }
    public int getAppWidgetID() {
        return getArguments().getInt("appWidgetID", 0);
    }

    public void setColorTag(String tag) {
        getArguments().putString("colorTag", tag);
        if (colorSheet != null) {
            colorSheet.setColorTag(tag);
        }
    }
    @Nullable
    public String getColorTag() {
        return getArguments().getString("colorTag", null);
    }

    public void setFilter(String[]... keys)
    {
        Set<String> filterSet = new TreeSet<>();
        for (String[] array : keys) {
            filterSet.addAll(Arrays.asList(array));
        }

        String[] filter = filterSet.toArray(new String[0]);
        getArguments().putStringArray("filterValues", filter);
        if (colorSheet != null) {
            colorSheet.setFilter(filter);
        }
    }
    public String[] getFilter() {
        return getArguments().getStringArray("filterValues");
    }
    public void clearFilter() {
        getArguments().remove("filterValues");
        if (colorSheet != null) {
            colorSheet.clearFilter();
        }
    }

    public void setDialogTitle(String title)
    {
        getArguments().putString("dialogTitle", title);
        if (isAdded()) {
            updateViews();
        }
    }
    @Nullable
    public String getDialogTitle() {
        return getArguments().getString("dialogTitle", null);
    }

    protected ColorValuesCollection<ColorValues> colorCollection = null;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colorCollection = collection;
    }
    public ColorValuesCollection<ColorValues> getColorCollection() {
        return colorCollection;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        Window w = getDialog().getWindow();
        if (w != null) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        //ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.layout_dialog_colorsheet, parent, false);

        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }

        initViews(dialogContent);
        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateViews();

        expandSheet(getDialog());
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(false);
                behavior.setPeekHeight(200);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private TextView titleText;
    private ColorValuesSheetFragment colorSheet;

    public void initViews(View dialogView)
    {
        titleText = (TextView) dialogView.findViewById(R.id.dialog_title);

        FragmentManager fragments = getChildFragmentManager();
        FragmentTransaction transaction = fragments.beginTransaction();

        colorSheet = new ColorValuesSheetFragment();
        colorSheet.setAppWidgetID(getAppWidgetID());
        colorSheet.setColorTag(getColorTag());
        colorSheet.setFilter(getFilter());
        colorSheet.setColorCollection(getColorCollection());
        colorSheet.setMode(ColorValuesSheetFragment.MODE_SELECT);
        colorSheet.setFragmentListener(fragmentListener);

        transaction.replace(R.id.fragmentContainer2, colorSheet, "ColorValuesSheet");
        transaction.commit();
    }

    public void updateViews()
    {
        String title = getDialogTitle();
        if (titleText != null && title != null) {
            titleText.setText(title);
        }

        colorSheet.updateViews();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        outState.putParcelable("colorCollection", colorCollection);
        colorSheet.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
    protected void onRestoreInstanceState( Bundle savedState ) {
        colorCollection = savedState.getParcelable("colorCollection");
    }

    private final ColorValuesSheetFragment.FragmentListener fragmentListener = new ColorValuesSheetFragment.FragmentListener()
    {
        @Override
        public void requestPeekHeight(int height)
        {
            if (dialogListener != null) {
                dialogListener.requestPeekHeight(height);
            }
        }

        @Override
        public void requestHideSheet() {
            dismiss();
        }

        @Override
        public void requestExpandSheet() {
            expandSheet(getDialog());
        }

        @Override
        public void onColorValuesSelected(ColorValues values)
        {
            if (dialogListener != null) {
                dialogListener.onColorValuesSelected(values);
            }
        }

        @Override
        public void onModeChanged(int mode)
        {
            if (dialogListener != null) {
                dialogListener.onModeChanged(mode);
            }
        }
    };

    /**
     * DialogListener
     */
    public interface DialogListener extends ColorValuesSheetFragment.FragmentListener
    {
    }

    protected DialogListener dialogListener = null;
    public void setDialogListener(DialogListener l) {
        dialogListener = l;
    }

}
