/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import com.forrestguice.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import java.lang.ref.WeakReference;

/**
 * SuntimesCalculatorDescriptorListAdapter
 */
public class SuntimesCalculatorDescriptorListAdapter extends ArrayAdapter<SuntimesCalculatorDescriptor>
{
    private int layoutID, dropDownLayoutID;
    private String pluginTag = "[plugin]", defaultTag = "[default]";
    private int pluginColor = Color.WHITE, defaultColor = Color.WHITE;
    private String defaultValue = null;
    private WeakReference<Context> contextRef;

    @SuppressLint("ResourceType")
    public SuntimesCalculatorDescriptorListAdapter(@NonNull Context context, @LayoutRes int resource, @LayoutRes int dropDownResource, @NonNull SuntimesCalculatorDescriptor[] entries)
    {
        super(context, resource, entries);
        this.contextRef = new WeakReference<>(context);
        this.layoutID = resource;
        this.dropDownLayoutID = dropDownResource;
        initDisplayStrings(context);
    }

    public void setDefaultValue(String value)
    {
        defaultValue = value;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(this.dropDownLayoutID, parent, false);
        }
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        TextView summaryText = (TextView) view.findViewById(android.R.id.text2);

        SuntimesCalculatorDescriptor descriptor = getItem(position);
        if (descriptor != null)
        {
            text.setText(descriptor.getName());
            if (summaryText != null)
            {
                Context context = contextRef.get();
                if (context != null)
                {
                    String displayString = (descriptor.getName().equalsIgnoreCase(defaultValue))
                                         ? context.getString(R.string.configLabel_prefSummaryTagged, descriptor.getDisplayString(), defaultTag)
                                         : descriptor.getDisplayString();

                    if (descriptor.isPlugin()) {
                        displayString = context.getString(R.string.configLabel_prefSummaryTagged, displayString, pluginTag);
                    }

                    SpannableString styledSummary = SuntimesUtils.createBoldColorSpan(null, displayString, defaultTag, defaultColor);
                    styledSummary = SuntimesUtils.createRelativeSpan(styledSummary, displayString, defaultTag, 1.15f);
                    styledSummary = SuntimesUtils.createBoldColorSpan(styledSummary, displayString, pluginTag, pluginColor);
                    styledSummary = SuntimesUtils.createRelativeSpan(styledSummary, displayString, pluginTag, 1.15f);
                    summaryText.setText(styledSummary);

                } else {
                    summaryText.setText(descriptor.getDisplayString());
                }
            }

        } else {
            text.setText("");
            if (summaryText != null)
            {
                summaryText.setText("");
            }
        }
        return view;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(this.layoutID, parent, false);
        }

        SuntimesCalculatorDescriptor descriptor = getItem(position);
        TextView text = (TextView)view.findViewById(android.R.id.text1);
        text.setText(descriptor != null ? descriptor.getName() : "");
        return view;
    }

    @SuppressLint("ResourceType")
    public void initDisplayStrings(Context context)
    {
        for (SuntimesCalculatorDescriptor value : SuntimesCalculatorDescriptor.values(context))
        {
            value.initDisplayStrings(context);
        }

        defaultTag = context.getString(R.string.configLabel_tagDefault);
        pluginTag = context.getString(R.string.configLabel_tagPlugin);

        int[] colorAttrs = { R.attr.text_accentColor, R.attr.tagColor_warning };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        defaultColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.text_accent_dark));
        pluginColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.warningTag_dark));
        typedArray.recycle();
    }
}
