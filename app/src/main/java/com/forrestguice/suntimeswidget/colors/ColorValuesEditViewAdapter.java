// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.support.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class ColorValuesEditViewAdapter extends RecyclerView.Adapter<ColorValuesEditViewHolder>
{
    public ColorValuesEditViewAdapter(Context context, ColorValues values)
    {
        contextRef = new WeakReference<>(context);
        colors = values;
    }

    protected WeakReference<Context> contextRef;
    public Context getContext() {
        return contextRef.get();
    }

    private ColorValues colors = null;
    public void setColorValues(ColorValues values)
    {
        colors = values;
        notifyDataSetChanged();
    }
    public ColorValues getColors() {
        return colors;
    }

    @Override
    public int getItemCount() {
        return getKeys().length;
    }

    public String[] getKeys() {
        return hasFilter() ? getFilter()
                : (colors != null ? colors.getColorKeys() : new String[0]);
    }

    public String getKey(int position)
    {
        String[] keys = getKeys();
        return ((position >= 0 && position < keys.length) ? keys[position] : null);
    }

    public int findPositionForKey(String key)
    {
        String[] keys = getKeys();
        for (int i=0; i<keys.length; i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    protected Integer itemLayoutResID = null;
    public void setItemLayoutResID(Integer value) {
        itemLayoutResID = value;
    }

    protected Set<String> filterValues = new TreeSet<>();
    public void setFilter(@Nullable String[] keys)
    {
        filterValues.clear();
        if (keys != null) {
            filterValues.addAll(Arrays.asList(keys));
        }
        notifyDataSetChanged();
    }
    public String[] getFilter() {
        return filterValues.toArray(new String[0]);
    }
    public boolean hasFilter() {
        return (!filterValues.isEmpty());
    }
    protected boolean passesFilter(String key) {
        return filterValues.isEmpty() || filterValues.contains(key);
    }

    @NonNull
    @Override
    public ColorValuesEditViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
        int layoutResID = itemLayoutResID != null ? itemLayoutResID : ColorValuesEditViewHolder.suggestedLayoutResID();
        View view = layout.inflate(layoutResID, viewGroup, false);
        return new ColorValuesEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorValuesEditViewHolder holder, int i)
    {
        String[] keys = getKeys();
        String key = (i >= 0 && i < keys.length) ? keys[i] : null;
        holder.bindColorToView(contextRef.get(), colors, key);
        holder.text1.setOnClickListener(key != null ? onClick(holder, key) : null);
    }

    public interface AdapterListener {
        void onItemClicked(String key);
    }

    protected AdapterListener adapterListener = null;
    public void setAdapterListener(AdapterListener listener) {
        adapterListener = listener;
    }

    private View.OnClickListener onClick(final ColorValuesEditViewHolder holder, final String key)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (adapterListener != null) {
                    adapterListener.onItemClicked(key);
                }
            }
        };
    }
}
