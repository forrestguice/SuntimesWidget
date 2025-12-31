/**
    Copyright (C) 2019-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.cards;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;

/**
 * CardViewDecorator
 */
public class CardViewDecorator extends RecyclerView.ItemDecoration
{
    private final int marginPx;

    public CardViewDecorator(Context context ) {
        marginPx = (int)context.getResources().getDimension(R.dimen.activity_margin);
    }

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        outRect.left = outRect.right = marginPx;
        outRect.top = outRect.bottom = 0;
    }
}
