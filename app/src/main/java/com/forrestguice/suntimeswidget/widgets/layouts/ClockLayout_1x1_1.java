/**
   Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets.layouts;

import com.forrestguice.suntimeswidget.R;

public class ClockLayout_1x1_1 extends ClockLayout_1x1_0
{
    public ClockLayout_1x1_1() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_clock_1x1_1;
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_clock_1x1_1_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_clock_1x1_1_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_clock_1x1_1_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_clock_1x1_1;         // center
        }
    }
}
