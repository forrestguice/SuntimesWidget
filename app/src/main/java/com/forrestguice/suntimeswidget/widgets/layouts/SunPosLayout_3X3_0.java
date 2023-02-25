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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.os.Build;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;

/**
 * A 3x3 layout with world map.
 */
public class SunPosLayout_3X3_0 extends SunPosLayout_3X2_0
{
    public SunPosLayout_3X3_0()
    {
        super();
        dpWidth = dpHeight = 512;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_3x3_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);

        if (Build.VERSION.SDK_INT >= 16) {
            this.dpWidth = this.dpHeight = Math.min(widgetSize[0], widgetSize[1]);
        }
    }

    @Override
    public String getMapTag() {
        return WorldMapWidgetSettings.MAPTAG_3x3;
    }

}
