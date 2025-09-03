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

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.Toast;

import java.util.Locale;

public class ColorValuesSheetFragment extends ColorValuesFragment
{
    public static final String ARG_HIDE_AFTER_SAVE = "hideAfterSave";    // sheet is hidden/dismissed after saving from edit dialog
    public static final boolean DEF_HIDE_AFTER_SAVE = true;

    public static final String ARG_PERSIST_SELECTION = "persistSelection";    // selection is automatically persisted from list dialog
    public static final boolean DEF_PERSIST_SELECTION = true;

    public static final String DIALOG_LIST = "listDialog";
    public static final String DIALOG_EDIT = "editDialog";
    
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

    protected ColorValuesEditFragment.ColorValuesEditViewModel editViewModel;
    public ColorValuesEditFragment.ColorValuesEditViewModel getEditViewModel() {
        return editViewModel;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState)
    {
        //android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View content = inflater.cloneInContext(getActivity()).inflate(R.layout.fragment_colorsheet, container, false);
        editViewModel = ViewModelProviders.of(getActivity()).get(ColorValuesEditFragment.ColorValuesEditViewModel .class);
        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }
        initViews();
        return content;
    }

    protected void initViews()
    {
        FragmentManager fragments = getChildFragmentManager();
        listDialog = (ColorValuesSelectFragment) fragments.findFragmentByTag(DIALOG_LIST);
        editDialog = (ColorValuesEditFragment) fragments.findFragmentByTag(DIALOG_EDIT);

        if (listDialog == null)
        {
            listDialog = new ColorValuesSelectFragment(); //(ColorValuesSelectFragment) fragments.findFragmentById(R.id.colorsCollectionFragment);
            listDialog.setAppWidgetID(getAppWidgetID());
            listDialog.setColorTag(getColorTag());
            listDialog.setTheme(getThemeResID());
            listDialog.setShowBack(getShowBack());
            listDialog.setShowMenu(getShowMenu());
            listDialog.setPreviewKeys(previewKeys());

            FragmentTransaction transaction = fragments.beginTransaction();
            transaction.add(R.id.layout_color_sheet, listDialog, DIALOG_LIST);
            transaction.addToBackStack(DIALOG_LIST);
            transaction.commit();
        }
        if (editDialog == null)
        {
            editDialog = new ColorValuesEditFragment();  // (ClockColorValuesEditFragment) fragments.findFragmentById(R.id.colorsFragment);
            editDialog.setTheme(getThemeResID());
            editDialog.setFilter(getFilter());
            editDialog.setApplyFilter(applyFilter());

            FragmentTransaction transaction = fragments.beginTransaction();
            transaction.add(R.id.layout_color_sheet, editDialog, DIALOG_EDIT);
            transaction.addToBackStack(DIALOG_EDIT);
            transaction.commit();
        }
        fragments.executePendingTransactions();
    }

    @Override
    public void onResume()
    {
        super.onResume();

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
                            onSelectColors(editDialog.getColorValues(), "onResume");
                        }
                    }
                }, 500);
            }
        }
    }

    public void requestFocus()
    {
        if (mode == MODE_EDIT) {
            editDialog.requestFocus();
        } else {
            listDialog.requestFocus();
        }
    }

    protected ColorValuesSelectFragment listDialog;
    protected ColorValuesEditFragment editDialog;

    protected void onRestoreInstanceState(@NonNull Bundle savedState) {
        mode = savedState.getInt("mode");
        colorCollection = (ColorValuesCollection<ColorValues>) savedState.getSerializable("colorCollection");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("mode", mode);
        outState.putSerializable("colorCollection", colorCollection);
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

    public void setPreviewKeys(String... keys) {
        getArguments().putStringArray("previewKeys", keys);
    }
    public String[] previewKeys() {
        return getArguments().getStringArray("previewKeys");
    }

    protected String suggestColorValuesID(Context context)
    {
        String base = context.getString(R.string.suggest_colorid).toLowerCase(Locale.ROOT);
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

    private final ColorValuesSelectFragment.FragmentListener listDialogListener = new ColorValuesSelectFragment.FragmentListener()
    {
        @Override
        public void onBackClicked() {
            requestHideSheet();
        }

        @Override
        public void onImportClicked()
        {
            final Context context = getActivity();
            if (context != null)
            {
                AlertDialog.Builder dialog = createImportColorsDialog(context, new ImportColorsDialogInterface()
                {
                    @Override
                    public void onImportClicked(String input) {
                        importColors(context, input);
                    }
                });
                dialog.show();
            }
        }

        @Override
        public void onAddClicked(@Nullable String colorsID)
        {
            //Log.d("DEBUG", "onAddClicked " + colorsID);
            Context context = getActivity();
            if (context != null)
            {
                String suggestedID = suggestColorValuesID(context);
                ColorValues values = (colorsID == null)
                        ? colorCollection.getDefaultColors(context)
                        : colorCollection.getColors(context, colorsID);
                editDialog.setColorValues(values);
                editDialog.setID(suggestedID);
                editDialog.setLabel(suggestedID);
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
        public void onDeleteClicked(@Nullable String colorsID)
        {
            Context context = getActivity();
            if (context != null && !colorCollection.isDefaultColorID(colorsID))
            {
                String title = context.getString(R.string.colorsdelete_dialog_title);
                String message = context.getString(R.string.colorsdelete_dialog_message, colorsID);
                AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                        .setTitle(title).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(context.getString(R.string.colorsdelete_dialog_ok), onConfirmDelete(context, colorsID))
                        .setNegativeButton(context.getString(R.string.colorsdelete_dialog_cancel), null);
                confirm.show();
            }
        }
        protected DialogInterface.OnClickListener onConfirmDelete(final Context context, @NonNull final String colorsID)
        {
            return new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String selectedID = colorCollection.getSelectedColorsID(context, getAppWidgetID(), getColorTag());
                    colorCollection.removeColors(context, colorsID);
                    if (colorsID.equals(selectedID)) {
                        colorCollection.setSelectedColorsID(context, null, getAppWidgetID(), getColorTag());
                    }
                    Toast.makeText(context, getString(R.string.msg_colors_deleted, colorsID), Toast.LENGTH_SHORT).show();
                    updateViews();
                }
            };
        }

        @Override
        public void onItemSelected(ColorValuesSelectFragment.ColorValuesItem item)
        {
            //Log.d("DEBUG", "onItemSelected " + item.colorsID);
            Context context = getActivity();
            if (context != null)
            {
                if (persistSelection()) {
                    colorCollection.setSelectedColorsID(context, item.colorsID, getAppWidgetID(), getColorTag());
                }
                ColorValues selectedColors = colorCollection.getColors(context, item.colorsID);
                onSelectColors(selectedColors, "onItemSelected");
            }
        }
    };

    protected void importColors(@NonNull Context context, String jsonString)
    {
        ColorValues values = listener.getDefaultValues();
        if (values != null)
        {
            if (values.loadColorValues(jsonString))
            {
                String id = values.getID();
                if (id != null)
                {
                    int c = 0;
                    String base = id;
                    while (colorCollection.hasColors(id)) {
                        id = base + "_" + c;
                        c++;
                    }
                    if (!base.equals(id)) {
                        values.setID(id);
                    }

                    colorCollection.setColors(context, id, values);
                    //colorCollection.setSelectedColorsID(context, id, getAppWidgetID(), getColorTag());
                    Toast.makeText(getActivity(), context.getString(R.string.msg_colors_imported, id), Toast.LENGTH_SHORT).show();
                    updateViews();
                    return;
                }
            }
        }
        Toast.makeText(getActivity(), context.getString(R.string.msg_colors_import_failed), Toast.LENGTH_SHORT).show();
    }

    private final ColorValuesEditFragment.FragmentListener editDialogListener = new ColorValuesEditFragment.FragmentListener()
    {
        @Override
        public void onCancelClicked() {
            cancelEdit(getActivity());
        }

        @Override
        public void onSaveClicked(@NonNull String colorsID, ColorValues values)
        {
            Context context = getActivity();
            if (context != null)
            {
                colorCollection.clearCache();
                colorCollection.setColors(context, colorsID, values);
                colorCollection.setSelectedColorsID(context, colorsID, getAppWidgetID(), getColorTag());
                onSelectColors(colorCollection.getColors(context, colorsID), "onSaveClicked");

                listDialog.updateViews();
                setMode(MODE_SELECT);
                toggleFragmentVisibility(getMode());
                Toast.makeText(context, getString(R.string.msg_colors_saved, colorsID), Toast.LENGTH_SHORT).show();

                if (getHideAfterSave()) {
                    requestHideSheet();
                }
            }
        }

        @Override
        public void onDeleteClicked(String colorsID)
        {
            Context context = getActivity();
            if (context != null)
            {
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
        if (AppSettings.isTelevision(getActivity())) {
            requestFocus();
        }
    }

    public void cancelEdit(Context context)
    {
        if (context != null)
        {
            colorCollection.clearCache();    // cached instance may have been modified
            setMode(MODE_SELECT);
            toggleFragmentVisibility(getMode());
            onSelectColors(colorCollection.getSelectedColors(context, getAppWidgetID(), getColorTag()), "cancelEdit");
        }
    }

    public boolean persistSelection() {
        return getArguments().getBoolean(ARG_PERSIST_SELECTION, DEF_PERSIST_SELECTION);
    }
    public void setPersistSelection(boolean value) {
        getArguments().putBoolean(ARG_PERSIST_SELECTION, value);
    }

    public void setHideAfterSave(boolean showBack) {
        getArguments().putBoolean(ARG_HIDE_AFTER_SAVE, showBack);
    }
    public boolean getHideAfterSave() {
        return getArguments().getBoolean(ARG_HIDE_AFTER_SAVE, DEF_HIDE_AFTER_SAVE);
    }

    public void setShowBack(boolean showBack) {
        getArguments().putBoolean(ColorValuesSelectFragment.ARG_SHOW_BACK, showBack);
    }
    public boolean getShowBack() {
        return getArguments().getBoolean(ColorValuesSelectFragment.ARG_SHOW_BACK, ColorValuesSelectFragment.DEF_SHOW_BACK);
    }

    public void setShowMenu(boolean showMenu) {
        getArguments().putBoolean(ColorValuesSelectFragment.ARG_SHOW_MENU, showMenu);
    }
    public boolean getShowMenu() {
        return getArguments().getBoolean(ColorValuesSelectFragment.ARG_SHOW_MENU, ColorValuesSelectFragment.DEF_SHOW_MENU);
    }

    public String getSelectedID() {
        return (listDialog != null ? listDialog.getSelectedID() : null);
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

    protected ColorValuesCollection<ColorValues> colorCollection = null;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colorCollection = collection;
    }
    public ColorValuesCollection<?> getColorCollection() {
        return colorCollection;
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
    protected void onSelectColors(ColorValues values, String tag) {
        if (listener != null) {
            listener.onColorValuesSelected(values);
        }
    }
    protected void onModeChanged()
    {
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