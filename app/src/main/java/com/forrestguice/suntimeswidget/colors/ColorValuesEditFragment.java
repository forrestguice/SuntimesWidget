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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.colors.ColorActivity;
import com.forrestguice.suntimeswidget.settings.colors.ColorDialog;
import com.forrestguice.suntimeswidget.settings.colors.ColorUtils;
import com.forrestguice.suntimeswidget.settings.colors.pickers.ColorPickerFragment;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class ColorValuesEditFragment extends ColorValuesFragment
{
    public static final String ARG_ALLOW_DELETE = "allowDelete";
    public static final boolean DEF_ALLOW_DELETE = true;

    public static final String ARG_SHOW_ALPHA = "showAlpha";
    public static final boolean DEF_SHOW_ALPHA = false;

    protected EditText editID, editLabel;
    protected GridLayout panel;
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

        panel = (GridLayout) content.findViewById(R.id.colorPanel);
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

    private View.OnClickListener onSaveButtonClicked = new View.OnClickListener() {
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

    private View.OnClickListener onCancelButtonClicked = new View.OnClickListener() {
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

    private View.OnClickListener onOverflowButtonClicked = new View.OnClickListener() {
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
        out.putParcelable("colorValues", colorValues);
        out.putParcelable("defaultValues", defaultValues);
        out.putStringArray("filterValues", filterValues.toArray(new String[0]));
        out.putString("editID", editID.getText().toString());
        out.putString("editLabel", editLabel.getText().toString());
        super.onSaveInstanceState(out);
    }
    protected void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        colorValues = savedState.getParcelable("colorValues");
        defaultValues = savedState.getParcelable("defaultValues");
        String[] filter = savedState.getStringArray("filterValues");
        if (filter != null) {
            filterValues  = new TreeSet<>(Arrays.asList(filter));
        }
        setID(savedState.getString("editID"));
        setLabel(savedState.getString("editLabel"));
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

    //protected TextView[] colorEdits;
    protected void updateViews()
    {
        if (panel != null && colorValues != null)
        {
            String[] keys = colorValues.getColorKeys();

            int itemMargin = 0;
            TypedValue itemBackground = null;
            Context context = getActivity();
            if (context != null)
            {
                Resources.Theme theme = context.getTheme();
                itemBackground = new TypedValue();
                theme.resolveAttribute(android.R.attr.selectableItemBackground, itemBackground, true);
                itemMargin = (int)context.getResources().getDimension(R.dimen.colortext_margin);
            }

            int[] defaultColors = getDefaultColors(context);

            int c = 0;
            panel.removeAllViews();
            for (int i=0; i<keys.length; i++)
            {
                if (applyFilter()
                        && !passesFilter(keys[i])) {
                    continue;
                }

                boolean bold = true;
                Integer textColor;
                Integer backgroundColor;
                switch (colorValues.getRole(keys[i]))
                {
                    case ColorValues.ROLE_BACKGROUND_PRIMARY: case ColorValues.ROLE_BACKGROUND:
                    case ColorValues.ROLE_BACKGROUND_INVERSE: case ColorValues.ROLE_ACTION:
                        bold = false;
                        backgroundColor = colorValues.getColor(keys[i]);
                        textColor = getContrastingColor(keys[i], ColorUtils.isTextReadable(defaultColors[0], backgroundColor)
                                ? defaultColors[0] : defaultColors[2]);
                        break;

                    case ColorValues.ROLE_TEXT: case ColorValues.ROLE_TEXT_PRIMARY:
                    case ColorValues.ROLE_TEXT_PRIMARY_INVERSE: case ColorValues.ROLE_TEXT_INVERSE:
                    case ColorValues.ROLE_ACCENT: case ColorValues.ROLE_FOREGROUND:
                        textColor = colorValues.getColor(keys[i]);
                        backgroundColor = getContrastingColor(keys[i], defaultColors[1]);
                        break;

                    case ColorValues.ROLE_UNKNOWN:
                    default:
                        textColor = colorValues.getColor(keys[i]);
                        backgroundColor = null;
                        break;
                }

                SpannableString colorLabel = null;
                String labelText = " " + colorValues.getLabel(keys[i]) + " ";

                if (backgroundColor != null && textColor != null)
                {
                    float cornerRadiusPx = context.getResources().getDimension(R.dimen.chip_radius);
                    colorLabel = SuntimesUtils.createRoundedBackgroundColorSpan(colorLabel, " " + labelText + " ", labelText, textColor, bold, backgroundColor, cornerRadiusPx, cornerRadiusPx);

                } else if (textColor != null) {
                    colorLabel = (bold ? SuntimesUtils.createBoldColorSpan(colorLabel, labelText, labelText, textColor)
                                       : SuntimesUtils.createColorSpan(colorLabel, labelText, labelText, textColor));

                } else {
                    colorLabel = new SpannableString(" ");
                }

                TextView colorEdit = new TextView(getActivity());
                colorEdit.setText(colorLabel);
                colorEdit.setOnClickListener(onColorEditClick(keys[i]));
                colorEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSizePx_medium(context));
                colorEdit.setGravity(Gravity.CENTER);

                //LinearLayout.LayoutParams colorEdit_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //colorEdit_layoutParams.gravity = Gravity.CENTER;
                //colorEdit.setLayoutParams(colorEdit_layoutParams);

                if (itemBackground != null) {
                    colorEdit.setBackgroundResource(itemBackground.resourceId);
                }
                colorEdit.setPadding(itemMargin, itemMargin, itemMargin, itemMargin);

                panel.addView(colorEdit, getItemLayoutParams(c));
                c++;
            }

        } else if (panel != null) {
            TextView emptyMsg = new TextView(getActivity());
            emptyMsg.setText(" ");
            panel.removeAllViews();
            panel.addView(emptyMsg);
        }
    }

    private GridLayout.LayoutParams getItemLayoutParams(int i) {
        if (Build.VERSION.SDK_INT >= 21)
        {
            return new GridLayout.LayoutParams(GridLayout.spec(i/2, GridLayout.FILL, 1f),
                    GridLayout.spec(i%2, GridLayout.FILL, 1f));

        } else {
            return new GridLayout.LayoutParams(GridLayout.spec(i/2, GridLayout.CENTER),
                    GridLayout.spec(i%2, GridLayout.CENTER));
        }
    }

    public View.OnClickListener onColorEditClick(final String colorKey) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColor(colorKey);
            }
        };
    }

    protected ColorValues colorValues = null;
    public void setColorValues(ColorValues v)
    {
        colorValues = v;
        setID(null);
        setLabel(null);
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
        getArguments().putBoolean("applyFilter", value);
        if (isAdded()) {
            updateViews();
        }
    }
    public boolean applyFilter() {
        return getArguments().getBoolean("applyFilter", hasFilter());
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

    protected void setColor(String key, int color) {
        colorValues.setColor(key, color);
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

    protected float getTextSizePx_medium(Context context)
    {
        int[] attr = { R.attr.text_size_medium };
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        float textSize = typedArray.getDimension(0, context.getResources().getDimension(R.dimen.text_size_medium));
        typedArray.recycle();
        return textSize;
    }

    @SuppressLint("ResourceType")
    protected int[] getDefaultColors(Context context)
    {
        int[] attr = { R.attr.timeCardBackground, R.attr.text_primaryColor, R.attr.text_primaryInverseColor };
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        int backgroundColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.card_bg));
        int textColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_primary));
        int inverseTextColor = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.text_primary_inverse));
        typedArray.recycle();
        return new int[] { textColor, backgroundColor, inverseTextColor };
    }

    protected int[] getColorOverUnder(Context context, String key)
    {
        int[] defaultColors = getDefaultColors(context);
        int colorOver = defaultColors[0];
        int colorUnder = defaultColors[1];
        Integer contrastingColor = getContrastingColor(key, null);

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

    @Nullable
    protected Integer getContrastingColor(String key, Integer defaultValue)
    {
        String k;
        switch (colorValues.getRole(key))
        {
            case ColorValues.ROLE_ACTION:
            case ColorValues.ROLE_BACKGROUND: case ColorValues.ROLE_BACKGROUND_PRIMARY:
                k = colorValues.findColorWithRole(ColorValues.ROLE_TEXT_PRIMARY);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_BACKGROUND_INVERSE:
                k = colorValues.findColorWithRole(ColorValues.ROLE_TEXT_PRIMARY_INVERSE);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_TEXT: case ColorValues.ROLE_TEXT_PRIMARY:
            case ColorValues.ROLE_ACCENT: case ColorValues.ROLE_FOREGROUND:
                k = colorValues.findColorWithRole(ColorValues.ROLE_BACKGROUND_PRIMARY);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            case ColorValues.ROLE_TEXT_INVERSE:
            case ColorValues.ROLE_TEXT_PRIMARY_INVERSE:
                k = colorValues.findColorWithRole(ColorValues.ROLE_BACKGROUND_INVERSE);
                return (k != null) ? Integer.valueOf(colorValues.getColor(k)) : defaultValue;

            default:
                return null;
        }
    }

    protected Intent pickColorIntent(String key, int requestCode)
    {
        Intent intent = new Intent(getActivity(), ColorActivity.class);
        intent.putExtra(ColorDialog.KEY_SHOWALPHA, showAlpha());
        intent.setData(Uri.parse("color://" + String.format("#%08X", colorValues.getColor(key))));
        intent.putExtra(ColorDialog.KEY_RECENT, new ArrayList<>(new LinkedHashSet<>(colorValues.getColors())));
        intent.putExtra(ColorDialog.KEY_LABEL, colorValues.getLabel(key));

        int[] colorOverUnder = getColorOverUnder(getActivity(), key);
        intent.putExtra(ColorDialog.KEY_COLOR_OVER, colorOverUnder[0]);
        intent.putExtra(ColorDialog.KEY_COLOR_UNDER, colorOverUnder[1]);
        intent.putExtra(ColorDialog.KEY_PREVIEW_MODE, ColorPickerFragment.ColorPickerModel.PREVIEW_CONTRAST_RATIO);

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
        ColorValues values = new ColorValues(jsonInput) {
            @Override
            public String[] getColorKeys() {
                return new String[0];
            }
        };
        if (values != null)
        {

        }
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

    public boolean showAlpha() {
        return getBoolArg(ARG_SHOW_ALPHA, DEF_SHOW_ALPHA);
    }
    public void setShowAlpha(boolean value) {
        setBoolArg(ARG_SHOW_ALPHA, value);
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
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_coloredit, popup.getMenu());
        onPrepareOverflowMenu(context, popup.getMenu());
        popup.setOnMenuItemClickListener(onOverflowMenuItemSelected);
        popup.show();
    }

    protected void onPrepareOverflowMenu(Context context, Menu menu)
    {
        MenuItem deleteItem = menu.findItem(R.id.action_colors_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(allowDelete());
        }
    }

    private final PopupMenu.OnMenuItemClickListener onOverflowMenuItemSelected = new PopupMenu.OnMenuItemClickListener()
    {
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
