// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020 Forrest Guice
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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.views.Toast;

public class ColorValuesSheetFragment extends ColorValuesFragment
{
    public static final int MODE_SELECT = 0;
    public static final int MODE_EDIT = 1;

    public ColorValuesSheetFragment() {
        setArguments(new Bundle());
        setHasOptionsMenu(false);
    }

    private int mode = MODE_SELECT;
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
        onModeChanged();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState)
    {
        //android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View content = inflater.cloneInContext(getActivity()).inflate(R.layout.fragment_colorsheet, container, false);

        listDialog = new ColorValuesSelectFragment(); //(ColorValuesSelectFragment) fragments.findFragmentById(R.id.colorsCollectionFragment);
        listDialog.setAppWidgetID(getAppWidgetID());
        listDialog.setColorTag(getColorTag());
        listDialog.setTheme(getThemeResID());

        editDialog = new ColorValuesEditFragment();  // (ClockColorValuesEditFragment) fragments.findFragmentById(R.id.colorsFragment);
        editDialog.setTheme(getThemeResID());
        editDialog.setFilter(getFilter());
        editDialog.setApplyFilter(applyFilter());

        getChildFragmentManager().beginTransaction().add(R.id.layout_color_sheet, listDialog).add(R.id.layout_color_sheet, editDialog).commit();

        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }
        return content;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        FragmentManager fragments = getChildFragmentManager();
        //listDialog = (ColorValuesSelectFragment) fragments.findFragmentById(R.id.colorsCollectionFragment);
        if (listDialog != null) {
            listDialog.setColorCollection(colorCollection);
            listDialog.setFragmentListener(listDialogListener);
            listDialog.setAppWidgetID(getAppWidgetID());
            listDialog.setColorTag(getColorTag());
        }

        //editDialog = (ClockColorValuesEditFragment) fragments.findFragmentById(R.id.colorsFragment);
        if (editDialog != null) {
            editDialog.setFragmentListener(editDialogListener);
            View v = getView();
            if (v != null)
            {
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mode == MODE_EDIT) {
                            onSelectColors(editDialog.getColorValues());
                        }
                    }
                }, 500);
            }
        }
    }

    protected ColorValuesSelectFragment listDialog;
    protected ColorValuesEditFragment editDialog;

    protected void onRestoreInstanceState(@NonNull Bundle savedState) {
        mode = savedState.getInt("mode");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("mode", mode);
        super.onSaveInstanceState(outState);
    }

    public void updateViews()
    {
        if (listDialog != null && editDialog != null)
        {
            toggleFragmentVisibility(mode);
            if (listDialog.getView() != null) {
                requestPeekHeight(listDialog.getView().getHeight());
            }

            listDialog.setColorCollection(colorCollection);
            listDialog.setFragmentListener(listDialogListener);
            editDialog.setFragmentListener(editDialogListener);
        }
    }

    public void updateViews(ColorValues values)
    {
        if (editDialog != null)
        {
            editDialog.setColorValues(values);
            if (listener != null) {
                editDialog.setDefaultValues(listener.getDefaultValues());
            }
        }
        updateViews();
    }

    protected String suggestColorValuesID(Context context)
    {
        String base = context.getString(R.string.suggest_colorid).toLowerCase();
        String suggestion = base;
        if (colorCollection != null)
        {
            int c = 1;
            while (colorCollection.hasColors(suggestion))
            {
                suggestion = base + c;
                c++;
            }
        }
        return suggestion;
    }

    public ColorValues getColors() {
        if (editDialog != null) {
            return editDialog.getColorValues();
        } else return null;
    }

    private ColorValuesSelectFragment.FragmentListener listDialogListener = new ColorValuesSelectFragment.FragmentListener()
    {
        @Override
        public void onBackClicked() {
            requestHideSheet();
        }

        @Override
        public void onAddClicked(@Nullable String colorsID)
        {
            //Log.d("DEBUG", "onAddClicked " + colorsID);
            Context context = getActivity();
            if (context != null)
            {
                ColorValues values = (colorsID == null)
                        ? colorCollection.getDefaultColors(context)
                        : colorCollection.getColors(context, colorsID);
                editDialog.setColorValues(values);
                editDialog.setID(suggestColorValuesID(context));
                editDialog.setAllowDelete(false);
                if (listener != null) {
                    editDialog.setDefaultValues(listener.getDefaultValues());
                }
                setMode(MODE_EDIT);
                toggleFragmentVisibility(getMode());
                requestExpandSheet();
            }
        }

        @Override
        public void onEditClicked(@Nullable String colorsID)
        {
            //Log.d("DEBUG", "onEditClicked " + colorsID);
            Context context = getActivity();
            if (context != null && colorsID != null) {
                editDialog.setColorValues(colorCollection.getColors(context, colorsID));
                editDialog.setAllowDelete(true);
                if (listener != null) {
                    editDialog.setDefaultValues(listener.getDefaultValues());
                }
                setMode(MODE_EDIT);
                toggleFragmentVisibility(getMode());
                requestExpandSheet();
            }
        }

        @Override
        public void onItemSelected(ColorValuesSelectFragment.ColorValuesItem item)
        {
            //Log.d("DEBUG", "onItemSelected " + item.colorsID);
            Context context = getActivity();
            if (context != null) {
                colorCollection.setSelectedColorsID(context, item.colorsID, getAppWidgetID(), getColorTag());
                ColorValues selectedColors = colorCollection.getColors(context, item.colorsID);
                onSelectColors(selectedColors);
            }
        }
    };

    private ColorValuesEditFragment.FragmentListener editDialogListener = new ColorValuesEditFragment.FragmentListener()
    {
        @Override
        public void onCancelClicked() {
            cancelEdit(getActivity());
        }

        @Override
        public void onSaveClicked(String colorsID, ColorValues values)
        {
            Context context = getActivity();
            if (context != null)
            {
                colorCollection.clearCache();
                colorCollection.setColors(context, colorsID, values);
                colorCollection.setSelectedColorsID(context, colorsID, getAppWidgetID(), getColorTag());
                onSelectColors(colorCollection.getColors(context, colorsID));

                setMode(MODE_SELECT);
                toggleFragmentVisibility(getMode());
                requestHideSheet();
                Toast.makeText(context, getString(R.string.msg_colors_saved, colorsID), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDeleteClicked(String colorsID) {
            Context context = getActivity();
            if (context != null) {
                colorCollection.removeColors(context, colorsID);
                setMode(MODE_SELECT);
                toggleFragmentVisibility(getMode());
                requestHideSheet();
                Toast.makeText(context, getString(R.string.msg_colors_deleted, colorsID), Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void toggleFragmentVisibility(int mode)
    {
        if (listDialog.getView() != null) {
            listDialog.getView().setVisibility(mode == MODE_EDIT ? View.GONE : View.VISIBLE);
        }
        if (editDialog.getView() != null) {
            editDialog.getView().setVisibility(mode == MODE_EDIT ? View.VISIBLE : View.GONE);
        }
    }

    public void cancelEdit(Context context)
    {
        if (context != null)
        {
            colorCollection.clearCache();    // cached instance may have been modified
            setMode(MODE_SELECT);
            toggleFragmentVisibility(getMode());
            onSelectColors(colorCollection.getSelectedColors(context, getAppWidgetID(), getColorTag()));
        }
    }

    public void setAppWidgetID(int id)
    {
        getArguments().putInt("appWidgetID", id);
        if (listDialog != null) {
            listDialog.setAppWidgetID(id);
        }
    }
    public int getAppWidgetID() {
        return getArguments().getInt("appWidgetID", 0);
    }

    public void setColorTag(@Nullable String tag)
    {
        getArguments().putString("colorTag", tag);
        if (listDialog != null) {
            listDialog.setColorTag(tag);
        }
    }
    @Nullable
    public String getColorTag() {
        return getArguments().getString("colorTag", null);
    }

    public void setApplyFilter(boolean value) {
        getArguments().putBoolean("applyFilter", value);
        if (editDialog != null) {
            editDialog.setApplyFilter(value);
        }
    }
    public boolean applyFilter() {
        return getArguments().getBoolean("applyFilter", hasFilter());
    }
    public boolean hasFilter() {
        return (getFilter() != null && getFilter().length > 0);
    }

    public void setFilter(String[] keys) {
        getArguments().putStringArray("filterValues", keys);
        if (editDialog != null) {
            editDialog.setFilter(keys);
        }
    }
    @Nullable
    public String[] getFilter() {
        return getArguments().getStringArray("filterValues");
    }
    public void clearFilter() {
        getArguments().remove("filterValues");
        if (editDialog != null) {
            editDialog.clearFilter();
        }
    }

    protected ColorValuesCollection colorCollection = null;
    public void setColorCollection(ColorValuesCollection collection) {
        colorCollection = collection;
    }

    protected void requestPeekHeight(int height) {
        if (listener != null) {
            listener.requestPeekHeight(height);
        }
    }
    protected void requestHideSheet() {
        if (listener != null) {
            listener.requestHideSheet();
        }
    }
    protected void requestExpandSheet() {
        if (listener != null) {
            listener.requestExpandSheet();
        }
    }
    protected void onSelectColors(ColorValues values) {
        if (listener != null) {
            listener.onColorValuesSelected(values);
        }
    }
    protected void onModeChanged() {
        if (listener != null) {
            listener.onModeChanged(mode);
        }
    }

    /**
     * FragmentListener
     */
    public interface FragmentListener
    {
        void requestPeekHeight(int height);
        void requestHideSheet();
        void requestExpandSheet();
        void onColorValuesSelected(ColorValues values);
        void onModeChanged(int mode);
        @Nullable ColorValues getDefaultValues();
    }

    protected FragmentListener listener = null;
    public void setFragmentListener(FragmentListener l) {
        listener = l;
    }
}