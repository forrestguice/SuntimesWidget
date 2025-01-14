/**
    Copyright (C) 2022-2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.moon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.forrestguice.support.design.view.ViewCompat;

public class MoonRiseSetDivider extends RecyclerView.ItemDecoration
{
    protected Drawable divider;
    protected int centerPosition;
    protected int itemsPerDay;
    private final Rect bounds = new Rect();

    public MoonRiseSetDivider(Context context, int centerPosition, int itemsPerDay)
    {
        this.centerPosition = centerPosition;
        this.itemsPerDay = itemsPerDay;
        initDrawables(context);
    }

    protected void initDrawables(Context context)
    {
        TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.listDivider });
        divider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state)
    {
        if (parent.getLayoutManager() == null) {
            return;
        }

        c.save();
        int top, bottom;
        if (parent.getClipToPadding())
        {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            c.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        int n = parent.getChildCount();
        for (int i=0; i<n; i++)
        {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, bounds);

            int offset = (position - centerPosition) % itemsPerDay;
            if (offset < 0) {
                offset += itemsPerDay;
            }

            if (offset == 0) {
                int left = bounds.left + Math.round(ViewCompat.getTranslationX(child));
                drawHeader(c, position, left, top);
                drawFooter(c, position, left, bottom);

            } else if (offset == (itemsPerDay - 1)) {
                int right = bounds.right + Math.round(ViewCompat.getTranslationX(child));
                int left = right - divider.getIntrinsicWidth();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
        c.restore();
    }

    protected void drawFooter(Canvas c, int position, float x, float y) {
        /* EMPTY */
    }

    protected void drawHeader(Canvas c, int position, float x, float y) {
        /* EMPTY */
    }

    @Override
    public void getItemOffsets(Rect rect, View v, RecyclerView parent, RecyclerView.State state) {
        rect.set(0, 0, divider.getIntrinsicWidth(), 0);
    }
}
