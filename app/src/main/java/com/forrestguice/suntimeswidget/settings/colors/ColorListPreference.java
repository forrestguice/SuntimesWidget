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

package com.forrestguice.suntimeswidget.settings.colors;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;

/**
 * A dialog preference that allows choosing from a list of pre-defined colors.
 */
@TargetApi(11)
public class ColorListPreference extends DialogPreference
{
    private final int FALLBACK_DEFAULT_VALUE = Color.WHITE;

    private int value;
    private final ArrayList<Integer> colors = new ArrayList<>();

    private TextView label;
    private LinearLayout preview;
    private RecyclerView picker;
    private ColorsAdapter adapter;

    @TargetApi(21)
    public ColorListPreference(Context context) {
        super(context);
    }

    public ColorListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setParams(context, attrs);
    }

    public void setParams(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorListPreference, 0, 0);
        try {
            int arrayID = a.getResourceId(R.styleable.ColorListPreference_colorValues, 0);
            if (arrayID != 0)
            {
                int[] array = context.getResources().getIntArray(arrayID);
                if (array != null)
                {
                    colors.clear();
                    for (int i=0; i<array.length; i++) {
                        colors.add(array[i]);
                    }
                }
            }
            numColumns = a.getInt(R.styleable.ColorListPreference_android_numColumns, numColumns);

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        View colorView = view.findViewById(R.id.colorPreview);
        if (colorView != null) {
            colorView.setBackgroundColor(getValue());
        }
    }

    @Override
    protected View onCreateDialogView()
    {
        Context context = getContext();

        float marginTopBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        float marginLeftRight;
        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.dialogPreferredPadding });
        marginLeftRight = context.getResources().getDimension(a.getResourceId(0, R.dimen.settingsGroup_margin));
        a.recycle();

        // label
        label = new TextView(getContext());
        LinearLayout.LayoutParams label_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        label_layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        label_layoutParams.setMargins((int)marginLeftRight, (int)marginTopBottom, (int)marginLeftRight, (int)marginTopBottom);
        label.setVisibility(View.INVISIBLE);
        label.setLayoutParams(label_layoutParams);

        // preview
        preview = new LinearLayout(getContext());
        LinearLayout.LayoutParams preview_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        preview_layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        preview_layoutParams.setMargins((int)marginLeftRight, (int)marginTopBottom, (int)marginLeftRight, (int)marginTopBottom);
        preview.setLayoutParams(preview_layoutParams);
        preview.addView(label);

        // color picker
        adapter = new ColorsAdapter(colors);
        adapter.setSelectedColor(getValue());
        adapter.setItemLayoutResID(R.layout.layout_listitem_color2);
        adapter.setOnColorButtonClickListener(new ColorChangeListener()
        {
            @Override
            public void onColorChanged(int color) {
                updatePreview(color);
            }
        });

        picker = new RecyclerView(getContext());
        picker.setHasFixedSize(true);
        picker.setLayoutManager(new GridLayoutManager(getContext(), getNumColumns(), GridLayoutManager.VERTICAL, false));

        LinearLayout.LayoutParams picker_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        picker_layoutParams.gravity = Gravity.CENTER;
        picker.setLayoutParams(picker_layoutParams);

        // dialog layout
        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams dialog_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_layoutParams.gravity = Gravity.CENTER;
        dialogView.setLayoutParams(dialog_layoutParams);

        dialogView.addView(preview);
        dialogView.addView(picker);
        return dialogView;
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        updatePreview(getValue());
        adapter.setSelectedColor(getValue());
        picker.setAdapter(adapter);
        picker.scrollToPosition(0);
    }

    protected void updatePreview(int color)
    {
        label.setText(createSummaryString(color));
        preview.setBackgroundColor(color);
        preview.setContentDescription(createSummaryString(color));
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        if (result)
        {
            Integer changedValue = adapter.getSelectedColor();
            if (changedValue != null && callChangeListener(changedValue)) {
                setValue(changedValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int i) {
        return a.getInt(i, FALLBACK_DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue0)
    {
        super.onSetInitialValue(restoreValue, defaultValue0);
        int defValue = ((defaultValue0 != null) ? (Integer) defaultValue0 : FALLBACK_DEFAULT_VALUE);
        setValue(restoreValue ? getPersistedInt(defValue) : defValue);
    }

    public int getNumColumns() {
        return numColumns;
    }
    private int numColumns = 6;

    public void setValue(int value)
    {
        this.value = value;
        persistInt(this.value);
        updateSummary();
    }
    public int getValue() {
        return this.value;
    }

    private String createSummaryString(int value) {
        return String.format("#%08X", value);
    }

    private void updateSummary() {
        setSummary(createSummaryString(getValue()));
    }

}
