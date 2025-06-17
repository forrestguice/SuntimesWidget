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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.os.Build;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

public class SunPosLayout_3X1_1 extends SunPosLayout_3X1_0
{
    public SunPosLayout_3X1_1() {
        super();
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize) {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);
        if (Build.VERSION.SDK_INT >= 16) {
            this.dpHeight = HEIGHT_MEDIUM;
        }
    }
}
