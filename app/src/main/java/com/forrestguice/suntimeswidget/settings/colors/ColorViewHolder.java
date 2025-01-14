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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.forrestguice.support.design.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.forrestguice.suntimeswidget.R;

/**
 * ColorViewHolder
 */
public class ColorViewHolder extends RecyclerView.ViewHolder
{
    public Integer color;
    public ImageButton colorButton;
    public View colorButtonFrame;

    public ColorViewHolder(View itemView) {
        super(itemView);
        colorButton = (ImageButton)itemView.findViewById(R.id.colorButton);
        colorButtonFrame = itemView.findViewById(R.id.colorButtonFrame);
    }

    public static int suggestedLayoutResID() {
        return R.layout.layout_listitem_color;
    }

    public void bindColorToView(Integer color, boolean isSelected)
    {
        this.color = color;
        if (color != null)
        {
            Drawable d = colorButton.getDrawable();
            if (d != null) {
                GradientDrawable g = (GradientDrawable) d.mutate();
                g.setColor(color);
                g.invalidateSelf();
            }
        }
        colorButton.setVisibility(color != null ? View.VISIBLE : View.GONE);
        setSelected(isSelected);
    }

    public void setSelected(boolean isSelected) {
        colorButtonFrame.setBackgroundColor(isSelected ? Color.WHITE : Color.TRANSPARENT);
    }
}
