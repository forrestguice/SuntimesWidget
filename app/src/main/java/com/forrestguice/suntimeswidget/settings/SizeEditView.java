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
package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

public class SizeEditView extends LinearLayout
{
    private EditText edit;
    private TextView label;

    public SizeEditView(Context context)
    {
        super(context);
        init(context, null);
    }

    public SizeEditView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SizeEditView, 0, 0);
        try {
            String labelText = a.getString(R.styleable.SizeEditView_labelText);
            if (label != null) {
                label.setText(labelText);
            }

            String hintText = a.getString(R.styleable.SizeEditView_hintText);
            if (edit != null) {
                edit.setHint(hintText);
            }
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet attrs)
    {
        LayoutInflater.from(context).inflate(R.layout.layout_view_sizechooser0, this, true);
        label = (TextView) findViewById(R.id.editLabel_size);
        edit = (EditText) findViewById(R.id.edit_size);
        applyAttributes(context, attrs);
    }

    public EditText getEdit() {
        return edit;
    }

    public TextView getLabel() {
        return label;
    }
}
