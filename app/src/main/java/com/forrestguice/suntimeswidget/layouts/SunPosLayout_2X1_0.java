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

package com.forrestguice.suntimeswidget.layouts;

import com.forrestguice.suntimeswidget.R;

/**
 * A 2x1 layout that displays azimuth and elevation.
 */
public class SunPosLayout_2X1_0 extends SunPosLayout_1X1_0
{
    public SunPosLayout_2X1_0()
    {
        super();
    }

    /**public SunPosLayout_2X1_0(int layoutID )
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_2x1_5;
    }

}
