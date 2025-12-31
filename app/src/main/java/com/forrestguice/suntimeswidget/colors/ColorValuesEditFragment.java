// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2024 Forrest Guice
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

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.ColorActivity;
import com.forrestguice.suntimeswidget.settings.colors.ColorDialog;
import com.forrestguice.suntimeswidget.settings.colors.pickers.ColorPickerFragment;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.widget.GridLayoutManager;
import com.forrestguice.support.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class ColorValuesEditFragment extends ColorValuesFragment
{
    public static final String ARG_ALLOW_DELETE = "allowDelete";
    public static final boolean DEF_ALLOW_DELETE = true;

    protected ColorValuesEditViewModel viewModel;
    protected EditText editID, editLabel;
    protected RecyclerView panel;
    protected ColorValuesEditViewAdapter adapter;
    protected ImageButton cancelButton;

    public ColorValuesEditFragment() {
        super();
        setHasOptionsMenu(true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_ALLOW_DELETE, DEF_ALLOW_DELETE);
        setArguments(bundle);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState)
    {
        //android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View content = inflater.cloneInContext(getActivity()).inflate(R.layout.fragment_colorvalues, container, false);

        viewModel = ViewModelProviders.of(getActivity()).get(ColorValuesEditViewModel.class);

        ImageButton overflow = (ImageButton) content.findViewById(R.id.overflow);
        if (overflow != null) {
            overflow.setOnClickListener(new ViewUtils.ThrottledClickListener(onOverflowButtonClicked));
        }

        ImageButton saveButton = (ImageButton) content.findViewById(R.id.saveButton);
        if (saveButton != null) {
            saveButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onSaveButtonClicked));
        }

        cancelButton = (ImageButton) content.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onCancelButtonClicked));
        }

        adapter = new ColorValuesEditViewAdapter(getActivity(), colorValues);
        adapter.setFilter(getFilter());
        adapter.setAdapterListener(new ColorValuesEditViewAdapter.AdapterListener()
        {
            @Override
            public void onItemClicked(String key) {
                pickColor(key);
            }
        });

        panel = (RecyclerView) content.findViewById(R.id.colorPanel);
        panel.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false));
        panel.setAdapter(adapter);
        panel.scrollToPosition(0);

        editID = (EditText) content.findViewById(R.id.editTextID);
        editLabel = (EditText) content.findViewById(R.id.editTextLabel);
        setID(null);
        setLabel(null);

        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }
        updateViews();
        return content;
    }

    public void requestFocus()
    {
        if (cancelButton != null) {
            cancelButton.requestFocus();
        }
    }

    /**
     * @param colorsID value to set on edittext; use null to get the id from ColorValues
     */
    protected void setID(@Nullable String colorsID)
    {
        if (editID != null)
        {
            if (colorValues != null && colorsID == null) {
                colorsID =  colorValues.getID();
            }
            editID.setText(colorsID != null ? colorsID : "");
        }
    }

    /**
     * @param colorsLabel value to set on edittext; use null to get the label from ColorValues
     */
    protected void setLabel(@Nullable String colorsLabel)
    {
        if (editLabel != null)
        {
            if (colorValues != null && colorsLabel == null) {
                colorsLabel = colorValues.getLabel();
            }
            editLabel.setText(colorsLabel != null ? colorsLabel : "");
        }
    }

    protected boolean validateInput()
    {
        String colorsID = editID.getText().toString();
        if (colorsID.trim().isEmpty()) {    // must not be empty
            editID.setError(getString(R.string.error_colorid_empty));
            return false;

        } else if (colorsID.contains(" ")) {    // must not contain spaces
            editID.setError(getString(R.string.error_colorid_spaces));
            editID.setSelection(colorsID.indexOf(" "), colorsID.indexOf(" ") + 1);
            return false;
        }

        String colorsLabel = editLabel.getText().toString();
        if (colorsLabel.trim().isEmpty()) {
            editLabel.setError(getString(R.string.error_colorlabel_empty));
            return false;
        }

        return true;
    }

    private final View.OnClickListener onSaveButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onSaveColorValues();
        }
    };
    protected boolean onSaveColorValues()
    {
        if (validateInput())
        {
            String colorsID = editID.getText().toString();
            String colorsLabel = editLabel.getText().toString();
            colorValues.setID(colorsID);
            colorValues.setLabel(colorsLabel);

            if (listener != null) {
                listener.onSaveClicked(colorsID, colorValues);
            }
            return true;
        }
        return false;
    }

    private final View.OnClickListener onCancelButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCancelled();
        }
    };
    protected void onCancelled() {
        if (listener != null) {
            listener.onCancelClicked();
        }
    }

    private final View.OnClickListener onOverflowButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getActivity(), v);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out)
    {
        out.putSerializable("colorValues", colorValues);
        out.putSerializable("defaultValues", defaultValues);
        out.putStringArray("filterValues", filterValues.toArray(new String[0]));
        out.putString("editID", editID.getText().toString());
        out.putString("editLabel", editLabel.getText().toString());
        super.onSaveInstanceState(out);
    }
    protected void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        colorValues = (ColorValues) savedState.getSerializable("colorValues");
        defaultValues = (ColorValues) savedState.getSerializable("defaultValues");
        String[] filter = savedState.getStringArray("filterValues");
        if (filter != null) {
            filterValues  = new TreeSet<>(Arrays.asList(filter));
        }
        setID(savedState.getString("editID"));
        setLabel(savedState.getString("editLabel"));

        if (adapter != null) {
            adapter.setColorValues(getColorValues());
            adapter.setFilter(getFilter());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode >= 0 && requestCode < REQUEST_IMPORT_THEME) {
            onPickColorResult(requestCode, resultCode, data);

        } else if (requestCode == REQUEST_IMPORT_THEME) {
            onPickThemeResult(data);
        }
    }

    protected void updateViews() {
    }

    protected ColorValues colorValues = null;
    public void setColorValues(ColorValues v)
    {
        colorValues = v;
        setID(null);
        setLabel(null);

        if (adapter != null) {
            adapter.setColorValues(colorValues);
        }
        updateViews();
    }
    public ColorValues getColorValues() {
         return colorValues;
    }

    protected ColorValues defaultValues = null;
    public void setDefaultValues(ColorValues v) {
        defaultValues = v;
    }
    public ColorValues getDefaultValues() {
        return defaultValues;
    }

    public void setApplyFilter(boolean value) {
        getArgs().putBoolean("applyFilter", value);
        if (isAdded())
        {
            if (adapter != null) {
                adapter.setFilter(applyFilter() ? getFilter() : null);
            }
            updateViews();
        }
    }
    public boolean applyFilter() {
        return getArgs().getBoolean("applyFilter", hasFilter());
    }
    public boolean hasFilter() {
        return (!filterValues.isEmpty());
    }

    protected Set<String> filterValues = new TreeSet<>();
    public void setFilter(@Nullable String[] keys)
    {
        filterValues.clear();
        if (keys != null) {
            filterValues.addAll(Arrays.asList(keys));
        }
        updateViews();
    }
    public String[] getFilter() {
        return filterValues.toArray(new String[0]);
    }
    public void clearFilter() {
        filterValues.clear();
        updateViews();
    }
    protected boolean passesFilter(String key) {
        return filterValues.isEmpty() || filterValues.contains(key);
    }

    protected void setColor(String key, int color)
    {
        colorValues.setColor(key, color);
        int position = adapter.findPositionForKey(key);
        if (position >= 0) {
            adapter.notifyItemChanged(position);
        }
        updateViews();
    }

    public void pickColor(String key)
    {
        int requestCode = colorValues.colorKeyIndex(key);
        if (requestCode >= 0)
        {
            Intent intent = pickColorIntent(key, requestCode);
            if (intent != null) {
                startActivityForResult(pickColorIntent(key, requestCode), requestCode);
            }
        }
    }

    protected int[] getColorOverUnder(Context context, String key)
    {
        int[] defaultColors = ColorValuesEditViewHolder.getDefaultColors(context);
        int colorOver = defaultColors[0];
        int colorUnder = defaultColors[1];
        Integer contrastingColor = ColorValuesEditViewHolder.getContrastingColor(colorValues, key, null);

        switch (colorValues.getRole(key))
        {
            case ColorValues.ROLE_BACKGROUND_PRIMARY:
                colorOver = (contrastingColor != null) ? contrastingColor : defaultColors[0];
                break;

            case ColorValues.ROLE_FOREGROUND:
            case ColorValues.ROLE_TEXT: case ColorValues.ROLE_TEXT_INVERSE:
            case ColorValues.ROLE_TEXT_PRIMARY: case ColorValues.ROLE_TEXT_PRIMARY_INVERSE:
                colorUnder = (contrastingColor != null) ? contrastingColor : defaultColors[1];
                break;
        }
        return new int[] { colorOver, colorUnder };
    }

    protected Intent pickColorIntent(String key, int requestCode)
    {
        int color = colorValues.getColor(key);
        viewModel.setColor(color);

        ArrayList<Integer> recentColors = new ArrayList<>(new LinkedHashSet<>(colorValues.getColors()));
        recentColors.add(0, color);

        int[] colorOverUnder = getColorOverUnder(getActivity(), key);
        viewModel.setColorOver(colorOverUnder[0]);
        viewModel.setColorUnder(colorOverUnder[1]);

        Intent intent = new Intent(getActivity(), ColorActivity.class);
        intent.putExtra(ColorDialog.KEY_SHOWALPHA, viewModel.showAlpha());
        intent.setData(Uri.parse("color://" + String.format("#%08X", color)));
        intent.putExtra(ColorDialog.KEY_RECENT, recentColors);
        intent.putExtra(ColorDialog.KEY_LABEL, colorValues.getLabel(key));
        intent.putExtra(ColorDialog.KEY_COLOR_OVER, viewModel.getColorOver());
        intent.putExtra(ColorDialog.KEY_COLOR_UNDER, viewModel.getColorUnder());
        intent.putExtra(ColorDialog.KEY_PREVIEW_MODE, viewModel.getPreviewMode());

        if (defaultValues != null) {
            intent.putExtra(ColorDialog.KEY_SUGGESTED, defaultValues.getColor(key));
        }
        return intent;
    }

    protected void onPickColorResult(int requestCode, int resultCode, Intent data)
    {
        String[] keys = colorValues.getColorKeys();
        if (resultCode == Activity.RESULT_OK && requestCode >= 0 && requestCode <keys.length) {
            onPickColorResult(keys[requestCode],data);
        }
    }

    protected void onPickColorResult(String key, Intent data)
    {
        Uri uri = data.getData();
        if (uri != null)
        {
            try {
                setColor(key, Color.parseColor("#" + uri.getFragment()));
            } catch (IllegalArgumentException e) {
                Log.e("onActivityResult", "bad color uri; " + e);
            }
        }
    }

    protected void importColors(final Context context)
    {
        if (context != null)
        {
            AlertDialog.Builder dialog = createImportColorsDialog(context, new ImportColorsDialogInterface()
            {
                public void onImportClicked(String input) {
                    importColors(context, input);
                }
            });
            dialog.show();
        }
    }
    protected void importColors(final Context context, String jsonInput)
    {
        // TODO
    }

    protected void shareColors(Context context)
    {
        if (colorValues != null)
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, colorValues.toString());
            startActivity(Intent.createChooser(intent, null));
        }
    }

    protected void deleteColors(Context context)
    {
        if (listener != null && allowDelete() && colorValues != null) {
            listener.onDeleteClicked(colorValues.getID());
        }
    }
    public boolean allowDelete() {
        return getBoolArg(ARG_ALLOW_DELETE, DEF_ALLOW_DELETE);
    }
    public void setAllowDelete(boolean allowDelete) {
        setBoolArg(ARG_ALLOW_DELETE, allowDelete);
    }

    public static final int REQUEST_IMPORT_THEME = 1000;
    protected void importFromTheme(Context context) {
        startActivityForResult(pickThemeIntent(), REQUEST_IMPORT_THEME);
    }
    protected Intent pickThemeIntent() {
        return null;
    }
    protected void onPickThemeResult(Intent data) { /* EMPTY */ }

    public void showOverflowMenu(Context context, View v)
    {
        PopupMenuCompat.createMenu(context, v, R.menu.menu_coloredit, onOverflowMenuItemSelected).show();
    }

    protected void onPrepareOverflowMenu(Context context, Menu menu)
    {
        MenuItem deleteItem = menu.findItem(R.id.action_colors_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(allowDelete());
        }
    }

    private final PopupMenuCompat.PopupMenuListener onOverflowMenuItemSelected = new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            onPrepareOverflowMenu(context, menu);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                //case R.id.action_colors_copytheme:
                //    importFromTheme(getActivity());
                //    return true;

                case R.id.action_colors_import:
                    importColors(getActivity());
                    return true;

                case R.id.action_colors_delete:
                    deleteColors(getActivity());
                    return true;

                case R.id.action_colors_share:
                    shareColors(getActivity());
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //if (onOverflowMenuItemSelected.onMenuItemClick(item)) {
        //    return true;
        //} else {
            return super.onOptionsItemSelected(item);
        //}
    }

    protected void setBoolArg(String key, boolean value) {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(key, value);
            updateViews();
        }
    }
    protected boolean getBoolArg(String key, boolean defValue) {
        Bundle args = getArguments();
        return args != null ? args.getBoolean(key, defValue) : defValue;
    }

    /**
     * ColorValuesEditViewModel
     */
    public static class ColorValuesEditViewModel extends ColorPickerFragment.ColorPickerModel {}

    /**
     * FragmentListener
     */
    public interface FragmentListener
    {
        void onCancelClicked();
        void onSaveClicked(String colorsID, ColorValues values);
        void onDeleteClicked(String colorsID);
    }

    protected FragmentListener listener = null;
    public void setFragmentListener(FragmentListener l) {
        listener = l;
    }

}
