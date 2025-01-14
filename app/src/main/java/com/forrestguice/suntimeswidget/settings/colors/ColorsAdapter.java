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

import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * ColorsAdapter
 */
public class ColorsAdapter extends RecyclerView.Adapter<ColorViewHolder>
{
    private final ArrayList<Integer> colors = new ArrayList<>();

    public ColorsAdapter(List<Integer> colors) {
        this.colors.addAll(colors);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected Integer itemLayoutResID = null;
    public void setItemLayoutResID(Integer value) {
        itemLayoutResID = value;
    }

    public void setColors(List<Integer> colors)
    {
        this.colors.clear();
        this.colors.addAll(colors);
        notifyDataSetChanged();
    }

    @Nullable
    public Integer getSelectedColor() {
        return selectedColor;
    }
    public void setSelectedColor(int color)
    {
        int newPosition = colors.indexOf(color);
        int oldPosition = ((selectedColor != null) ? colors.indexOf(selectedColor) : -1);
        selectedColor = color;

        notifyItemChanged(newPosition);
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
    }
    public void clearSelectedColor()
    {
        int oldPosition = ((selectedColor != null) ? colors.indexOf(selectedColor) : -1);
        selectedColor = null;

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
    }
    protected Integer selectedColor = null;

    @Override
    public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        int layoutResID = itemLayoutResID != null ? itemLayoutResID : ColorViewHolder.suggestedLayoutResID();
        View view = layout.inflate(layoutResID, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ColorViewHolder holder, int position)
    {
        Integer color = (position >= 0 && position < colors.size()) ? colors.get(position) : null;
        holder.bindColorToView(color, selectedColor != null && selectedColor.equals(color));
        holder.colorButton.setOnClickListener(color != null ? onColorButtonClick(holder, color) : null);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }


    private View.OnClickListener onColorButtonClick(final ColorViewHolder holder, final int color)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                holder.setSelected(true);
                setSelectedColor(color);
                if (onColorChangeListener != null) {
                    onColorChangeListener.onColorChanged(color);
                }
            }
        };
    }

    private ColorChangeListener onColorChangeListener;
    public void setOnColorButtonClickListener( ColorChangeListener listener ) {
        onColorChangeListener = listener;
    }
}
