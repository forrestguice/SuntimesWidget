/**
    Copyright (C) 2019 Forrest Guice
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
import android.util.AttributeSet;

import com.forrestguice.support.widget.LinearLayoutManager;

public class CardLayoutManager extends LinearLayoutManager
{
     public CardLayoutManager(Context context) {
         super(context);
         init(context);
     }

    public CardLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init(context);
    }

    public CardLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context)
    {
        setOrientation(LinearLayoutManager.HORIZONTAL);
        setItemPrefetchEnabled(true);
    }

    /**@Override
    protected int getExtraLayoutSpace(RecyclerView.State state)
    {
        return super.getExtraLayoutSpace(state);
    }*/
}