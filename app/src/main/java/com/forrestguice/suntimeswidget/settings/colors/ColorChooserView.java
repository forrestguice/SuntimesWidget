/**
    Copyright (C) 2018 Forrest Guice
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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

public class ColorChooserView extends LinearLayout
{
    private ImageButton button;
    private EditText edit;
    private TextView label;

    public ColorChooserView(Context context)
    {
        super(context);
        init(context, null);
    }

    public ColorChooserView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorChooserView, 0, 0);
        try
        {
            String labelText = a.getString(R.styleable.ColorChooserView_labelText);
            if (label != null)
            {
                label.setText(labelText);
                button.setContentDescription(labelText);
            }

            String hintText = a.getString(R.styleable.ColorChooserView_hintText);
            if (hintText != null) {
                edit.setHint(hintText);
            }

        } finally {
            a.recycle();
        }
    }

    private int getLayoutID(Context context,AttributeSet attrs)
    {
        int layoutID = R.layout.layout_view_colorchooser;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorChooserView, 0, 0);
        try
        {
            if (a.getBoolean(R.styleable.ColorChooserView_reverse, false)) {
                layoutID = R.layout.layout_view_colorchooser_rev;
            }
        } finally {
            a.recycle();
        }
        return layoutID;
    }

    private void init(Context context, AttributeSet attrs)
    {

        LayoutInflater.from(context).inflate(getLayoutID(context, attrs), this, true);
        label = (TextView) findViewById(R.id.editLabel_color);
        button = (ImageButton) findViewById(R.id.editButton_color);
        edit = (EditText) findViewById(R.id.edit_color);
        applyAttributes(context, attrs);
    }

    public ImageButton getButton() {
         return button;
    }

    public EditText getEdit() {
        return edit;
    }

    public TextView getLabel() {
        return label;
    }
}
