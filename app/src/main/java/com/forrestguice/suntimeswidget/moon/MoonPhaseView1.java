/**
    Copyright (C) 2022 Forrest Guice
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
import android.util.AttributeSet;

import com.forrestguice.suntimeswidget.MoonPhaseView;
import com.forrestguice.suntimeswidget.R;

@SuppressWarnings("Convert2Diamond")
public class MoonPhaseView1 extends MoonPhaseView
{
    public MoonPhaseView1(Context context) {
        super(context);
    }

    public MoonPhaseView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.layout_view_moonphase1;
    }

    @Override
    protected void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonPhaseView1, 0, 0);
        try {
            this.illumAtNoon = a.getBoolean(R.styleable.MoonPhaseView1_illuminationAtLunarNoon, illumAtNoon);
            this.showPosition = a.getBoolean(R.styleable.MoonPhaseView1_showPosition, false);
        } finally {
            a.recycle();
        }
    }
}