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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.support.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ColorValuesSelectFragment extends ColorValuesFragment
{
    public static final String ARG_APPWIDGETID = "appWidgetID";
    public static final int DEF_APPWIDGETID = 0;
    
    public static final String ARG_ALLOW_EDIT = "allowEdit";
    public static final boolean DEF_ALLOW_EDIT = true;

    public static final String ARG_SHOW_LABEL = "showLabel";
    public static final boolean DEF_SHOW_LABEL = false;

    public static final String ARG_SHOW_BACK = "showBack";
    public static final boolean DEF_SHOW_BACK = true;

    public static final String ARG_SHOW_MENU = "showMenu";
    public static final boolean DEF_SHOW_MENU = true;

    public static final String ARG_COLOR_TAG = "colorTag";
    public static final String DEF_COLOR_TAG = null;

    public static final String ARG_COLOR_SELECTED_ID = "selectedID";
    public static final String ARG_COLOR_SELECTED_DEFAULT = "defaultIsSelected";

    protected TextView label;
    protected Spinner selector;
    protected ImageButton addButton, editButton, backButton, menuButton;

    public ColorValuesSelectFragment()
    {
        setHasOptionsMenu(false);

        Bundle args = new Bundle();
        args.putBoolean(ARG_ALLOW_EDIT, DEF_ALLOW_EDIT);
        args.putBoolean(ARG_SHOW_LABEL, DEF_SHOW_LABEL);
        args.putBoolean(ARG_SHOW_BACK, DEF_SHOW_BACK);
        args.putBoolean(ARG_SHOW_MENU, DEF_SHOW_MENU);
        args.putInt(ARG_APPWIDGETID, DEF_APPWIDGETID);
        args.putString(ARG_COLOR_TAG, DEF_COLOR_TAG);
        setArguments(args);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState)
    {
        //android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View content = inflater.cloneInContext(getActivity()).inflate(R.layout.fragment_colorselector, container, false);
        if (savedState != null) {
            onRestoreInstanceState(savedState);
        }

        label = (TextView) content.findViewById(R.id.color_values_selector_label);
        selector = (Spinner) content.findViewById(R.id.colorvalues_selector);
        editButton = (ImageButton) content.findViewById(R.id.editButton);
        addButton = (ImageButton) content.findViewById(R.id.addButton);
        backButton = (ImageButton) content.findViewById(R.id.backButton);
        menuButton = (ImageButton) content.findViewById(R.id.menuButton);

        updateViews();
        return content;
    }

    protected void attachListeners()
    {
        if (selector != null) {
            selector.setOnItemSelectedListener(onItemSelected);
        }
        if (editButton != null) {
            editButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onEditButtonClicked));
        }
        if (addButton != null) {
            addButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onAddButtonClicked));
        }
        if (backButton != null) {
            backButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onBackButtonClicked));
        }
        if (menuButton != null) {
            menuButton.setOnClickListener(new ViewUtils.ThrottledClickListener(onMenuButtonClicked));
        }
    }
    protected void detachListeners()
    {
        if (selector != null) {
            selector.setOnItemSelectedListener(null);
        }
        if (editButton != null) {
            editButton.setOnClickListener(null);
        }
        if (addButton != null) {
            addButton.setOnClickListener(null);
        }
        if (backButton != null) {
            backButton.setOnClickListener(null);
        }
        if (menuButton != null) {
            menuButton.setOnClickListener(null);
        }
    }


    public void requestFocus()
    {
        if (backButton != null) {
            backButton.requestFocus();
        }
    }

    private final AdapterView.OnItemSelectedListener onItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            onColorValuesSelected(position);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    protected void onColorValuesSelected(int position)
    {
        ColorValuesItem item = (ColorValuesItem) selector.getItemAtPosition(position);
        getArgs().putString(ARG_COLOR_SELECTED_ID, (item != null ? item.colorsID : null));
        getArgs().putBoolean(ARG_COLOR_SELECTED_DEFAULT, (item != null && item.colorsID == null));
        if (listener != null) {
            listener.onItemSelected(item);
        }
        updateControls();
    }
    public String getSelectedID() {
        if (selector != null) {
            ColorValuesItem item = (ColorValuesItem) selector.getSelectedItem();
            return item.colorsID;
        } else return null;
    }

    private final View.OnClickListener onEditButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onEditSelectedItem();
        }
    });
    protected void onEditSelectedItem()
    {
        if (listener != null) {
            ColorValuesItem item = (ColorValuesItem) selector.getSelectedItem();
            listener.onEditClicked(item != null ? item.colorsID : null);
        }
    }

    private final View.OnClickListener onAddButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onAddItem();
        }
    };
    protected void onAddItem()
    {
        if (listener != null) {
            ColorValuesItem item = (ColorValuesItem) selector.getSelectedItem();
            listener.onAddClicked(item != null ? item.colorsID : null);
        }
    }

    protected void onDeleteItem()
    {
        if (listener != null) {
            ColorValuesItem item = (ColorValuesItem) selector.getSelectedItem();
            listener.onDeleteClicked(item != null ? item.colorsID : null);
        }
    }

    private final View.OnClickListener onBackButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBack();
        }
    };
    protected void onBack() {
        if (listener != null) {
            listener.onBackClicked();
        }
    }

    private final View.OnClickListener onMenuButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getActivity(), v);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (onOverflowMenuItemSelected.onMenuItemClick(item)) {
            return true;
        } else { return super.onOptionsItemSelected(item); }
    }

    protected void showOverflowMenu(Context context, View v)
    {
        PopupMenuCompat.createMenu(context, v, R.menu.menu_colorlist, onOverflowMenuItemSelected).show();
    }
    protected void onPrepareOverflowMenu(Context context, Menu menu)
    {
        MenuItem deleteItem = menu.findItem(R.id.action_colors_delete);
        if (deleteItem != null) {
            deleteItem.setEnabled(!colorCollection.isDefaultColorID(getSelectedID()));
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
            int itemId = item.getItemId();
            if (itemId == R.id.action_colors_add) {
                onAddItem();
                return true;

            } else if (itemId == R.id.action_colors_delete) {
                onDeleteItem();
                return true;

            } else if (itemId == R.id.action_colors_share) {
                onShareColors();
                return true;

            } else if (itemId == R.id.action_colors_import) {
                onImportColors();
                return true;
            }
            return false;
        }
    };

    protected void onShareColors()
    {
        Context context = getActivity();
        if (colorCollection != null && context != null)
        {
            ColorValues colors = colorCollection.getColors(context, getSelectedID());
            if (colors == null) {
                colors = colorCollection.getDefaultColors(context);
            }

            if (colors != null)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, new ColorValuesJSON().toJSON(colors, false));
                startActivity(Intent.createChooser(intent, null));
            }
        }
    }

    protected void onImportColors()
    {
        if (listener != null) {
            listener.onImportClicked();
        }
    }

    protected ArrayAdapter<ColorValuesItem> initAdapter(Context context)
    {
        ColorValuesItem[] items = (colorCollection == null ? new ColorValuesItem[0] : ColorValuesItem.createItems(context, colorCollection, previewKeys()));
        return new ColorValuesArrayAdapter(context, R.layout.layout_listitem_colors, items);
    }

    public void setPreviewKeys(String... keys) {
        getArgs().putStringArray("previewKeys", keys);
    }
    public String[] previewKeys() {
        return getArgs().getStringArray("previewKeys");
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedState) { /* EMPTY */ }

    protected void updateViews()
    {
        detachListeners();
        boolean allowEdit = allowEdit();

        if (selector != null)
        {
            selector.setAdapter(initAdapter(getActivity()));

            if (colorCollection != null)
            {
                int selectedIndex = 0;
                String selectedColorsID0 = colorCollection.getSelectedColorsID(getActivity(), getAppWidgetID(), getColorTag());
                boolean defaultIsSelected = getArgs().getBoolean(ARG_COLOR_SELECTED_DEFAULT, false);
                String selectedColorsID = getArgs().getString(ARG_COLOR_SELECTED_ID, (defaultIsSelected ? null : selectedColorsID0));

                for (int i=0; i<selector.getCount(); i++)
                {
                    ColorValuesItem item = (ColorValuesItem) selector.getItemAtPosition(i);
                    if (item.colorsID != null && item.colorsID.equals(selectedColorsID))
                    {
                        selectedIndex = i;
                        break;
                    }
                }
                selector.setSelection(selectedIndex, false);
            }
        }

        if (label != null) {
            label.setVisibility(getShowLabel() ? View.VISIBLE : View.GONE);
        }
        if (backButton != null) {
            backButton.setVisibility(getShowBack() ? View.VISIBLE : View.GONE);
        }
        if (addButton != null) {
            addButton.setVisibility(allowEdit ? View.VISIBLE : View.GONE);
        }
        if (editButton != null) {
            editButton.setVisibility(allowEdit ? View.VISIBLE : View.GONE);
        }
        if (menuButton != null)
        {
            boolean showMenu = getShowMenu();
            menuButton.setVisibility(showMenu ? View.VISIBLE : View.GONE);
            //if (addButton != null) {    // shown as part of menu
            //    addButton.setVisibility(showMenu || !allowEdit() ? View.GONE : View.VISIBLE);
            //}
        }
        updateControls();
        attachListeners();
    }

    protected void updateControls()
    {
        //String selectedColorsID = (colorCollection != null) ? colorCollection.getSelectedColorsID(getActivity(), getAppWidgetID(), getColorTag()) : null;
        String selectedColorsID;
        if (selector != null) {
            ColorValuesItem selectedColors = (ColorValuesItem) selector.getSelectedItem();
            selectedColorsID = (selectedColors != null ? selectedColors.colorsID : null);
        } else {
            selectedColorsID = (colorCollection != null) ? colorCollection.getSelectedColorsID(getActivity(), getAppWidgetID(), getColorTag()) : null;
        }

        boolean isDefault = ((colorCollection != null) ? colorCollection.isDefaultColorID(selectedColorsID) : (selectedColorsID == null));

        if (editButton != null) {
            editButton.setVisibility(isDefault ? View.GONE : View.VISIBLE);
        }
        if (addButton != null) {
            addButton.setVisibility(isDefault || !getShowMenu() ? View.VISIBLE : View.GONE);
        }
    }

    protected ColorValuesCollection<ColorValues> colorCollection = null;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colorCollection = collection;
        updateViews();
    }

    public void setAllowEdit(boolean allowEdit) {
        setBoolArg(ARG_ALLOW_EDIT, allowEdit);
    }
    public boolean allowEdit() {
        return getBoolArg(ARG_ALLOW_EDIT, DEF_ALLOW_EDIT);
    }

    public void setShowLabel(boolean showLabel) {
        setBoolArg(ARG_SHOW_LABEL, showLabel);
    }
    public boolean getShowLabel() {
        return getBoolArg(ARG_SHOW_LABEL, DEF_SHOW_LABEL);
    }

    public void setShowBack(boolean showBack) {
        setBoolArg(ARG_SHOW_BACK, showBack);
    }
    public boolean getShowBack() {
        return getBoolArg(ARG_SHOW_BACK, DEF_SHOW_BACK);
    }

    public void setShowMenu(boolean showMenu) {
        setBoolArg(ARG_SHOW_MENU, showMenu);
    }
    public boolean getShowMenu() {
        return getBoolArg(ARG_SHOW_MENU, DEF_SHOW_MENU);
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

    public void setAppWidgetID(int appWidgetID)
    {
        getArgs().putInt(ARG_APPWIDGETID, appWidgetID);
        updateViews();
    }
    public int getAppWidgetID() {
        return getArgs().getInt(ARG_APPWIDGETID, DEF_APPWIDGETID);
    }

    public void setColorTag(@Nullable String tag)
    {
        getArgs().putString(ARG_COLOR_TAG, tag);
        updateViews();
    }
    @Nullable
    public String getColorTag() {
        return getArgs().getString(ARG_COLOR_TAG, DEF_COLOR_TAG);
    }

    /**
     * ColorValuesItem
     */
    public static class ColorValuesItem
    {
        public String displayString;
        public String colorsID;
        public int[] previewColors;

        public ColorValuesItem(@Nullable String displayString, @Nullable String colorsID, int... previewColors)
        {
            this.displayString = (displayString != null ? displayString : colorsID);
            this.colorsID = colorsID;
            this.previewColors = previewColors;
        }

        public String toString() {
            return displayString;
        }

        public static ColorValuesItem[] createItems(Context context, ColorValuesCollection<ColorValues> collection, String[] previewKeys)
        {
            String[] colorIDs = collection != null ? collection.getCollection() : new String[0];
            ColorValuesItem[] items = new ColorValuesItem[colorIDs.length+1];

            items[0] = new ColorValuesItem(context.getString(R.string.configLabel_tagDefault), null, getPreviewColors(context, collection, null, previewKeys));
            for (int i=0; i<colorIDs.length; i++) {
                items[i+1] = new ColorValuesItem(collection.getColorsLabel(context, colorIDs[i]), colorIDs[i], getPreviewColors(context, collection, colorIDs[i], previewKeys));
            }
            return items;
        }

        public static int[] getPreviewColors(Context context, @Nullable ColorValuesCollection<ColorValues> collection, @Nullable String colorsID, @Nullable String[] previewKeys)
        {
            if (colorsID == null)
            {
                int[] colors = ((previewKeys != null) ? new int[previewKeys.length] : new int[0]);
                if (previewKeys != null)
                {
                    ColorValues values = collection.getDefaultColors(context);
                    for (int i=0; i<previewKeys.length; i++) {
                        colors[i] = values.getColor(previewKeys[i]);
                    }
                }
                return colors;

            } else {
                return collection.getColors(context, colorsID, ContextCompat.getColor(context, R.color.def_app_alarms_bright_color_end), previewKeys);
            }
        }
    }

    /**
     * ColorValuesArrayAdapter
     */
    public static class ColorValuesArrayAdapter extends ArrayAdapter<ColorValuesItem>
    {
        private int resourceID, dropDownResourceID;
        private ColorValuesItem selectedItem;

        public ColorValuesArrayAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            init(context, resource);
        }

        public ColorValuesArrayAdapter(@NonNull Context context, int resource, @NonNull ColorValuesItem[] objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        public ColorValuesArrayAdapter(@NonNull Context context, int resource, @NonNull List<ColorValuesItem> objects) {
            super(context, resource, objects);
            init(context, resource);
        }

        private void init(@NonNull Context context, int resource) {
            resourceID = dropDownResourceID = resource;
        }

        public void setSelected( ColorValuesItem item ) {
            selectedItem = item;
            notifyDataSetChanged();
        }

        public ColorValuesItem getSelected() {
            return selectedItem;
        }

        public List<ColorValuesItem> getItems()
        {
            ArrayList<ColorValuesItem> items = new ArrayList<>();
            for (int i=0; i<getCount(); i++)
            {
                ColorValuesItem item = getItem(i);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        }

        @Override
        public void setDropDownViewResource(int resID) {
            super.setDropDownViewResource(resID);
            dropDownResourceID = resID;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, dropDownResourceID);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getItemView(position, convertView, parent, resourceID);
        }

        private View getItemView(int position, View convertView, @NonNull ViewGroup parent, int resID)
        {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(resID, parent, false);
            }

            ColorValuesItem item = getItem(position);
            if (item == null) {
                Log.w("getItemView", "item at position " + position + " is null.");
                return view;
            }

            TextView primaryText = (TextView) view.findViewById(android.R.id.text1);
            if (primaryText != null) {
                primaryText.setText(item.displayString);
            }

            int[] previewColors = item.previewColors;
            View[] previews = new View[] { view.findViewById(R.id.colorPreview0), view.findViewById(R.id.colorPreview1), view.findViewById(R.id.colorPreview2) };
            if (previews[0] != null && previewColors != null && previewColors.length > 0)
            {
                for (int i=0; i<previews.length; i++)
                {
                    View preview = previews[i];
                    if (preview != null)
                    {
                        if (i < previewColors.length) {
                            preview.setBackgroundColor(previewColors[i]);
                            preview.setVisibility(View.VISIBLE);
                        } else {
                            preview.setVisibility(View.GONE);
                        }
                    }
                }

            } else {
                for (View v : previews) {
                    if (v != null) {
                        v.setVisibility(View.GONE);
                    }
                }
            }

            /*if (selectedItem != null && item.colorsID.equals(selectedItem.colorsID)) {
                Log.d("DEBUG", "getItemView: " + selectedItem.colorsID);
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.text_accent_dark));
            } else view.setBackgroundColor(Color.TRANSPARENT);
            TextView secondaryText = (TextView)view.findViewById(android.R.id.text2);
            if (secondaryText != null) {
                secondaryText.setText(item.getSummary(getContext()));
            }*/

            return view;
        }
    }

    /**
     * FragmentListener
     */
    public interface FragmentListener
    {
        void onBackClicked();
        void onImportClicked();
        void onAddClicked(@Nullable String colorsID);
        void onEditClicked(@Nullable String colorsID);
        void onDeleteClicked(@Nullable String colorsID);
        void onItemSelected(ColorValuesItem item);
    }

    protected FragmentListener listener = null;
    public void setFragmentListener(FragmentListener l) {
        listener = l;
    }
}