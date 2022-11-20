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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class SunLayout_3x1_0 extends SunLayout_2x1_0
{
    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_3x1_0;
    }

    @Override
    protected int chooseLayout(int position, SuntimesRiseSetData data)
    {
        switch (position) {
            case 0: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_fill, R.layout.layout_widget_3x1_01_align_fill, data, order);
            case 1: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_1, R.layout.layout_widget_3x1_01_align_float_1, data, order);
            case 2: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_2, R.layout.layout_widget_3x1_01_align_float_2, data, order);
            case 3: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_3, R.layout.layout_widget_3x1_01_align_float_3, data, order);
            case 4: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_4, R.layout.layout_widget_3x1_01_align_float_4, data, order);
            case 6: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_6, R.layout.layout_widget_3x1_01_align_float_6, data, order);
            case 7: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_7, R.layout.layout_widget_3x1_01_align_float_7, data, order);
            case 8: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_8, R.layout.layout_widget_3x1_01_align_float_8, data, order);
            case 9: return chooseSunLayout(R.layout.layout_widget_3x1_0_align_float_9, R.layout.layout_widget_3x1_01_align_float_9, data, order);
            case 5: default: return chooseSunLayout(R.layout.layout_widget_3x1_0, R.layout.layout_widget_3x1_01, data, order);
        }
    }

    @Override
    protected float[] findAdjustedSize(Context context, int appWidgetId)
    {
        boolean showSolarNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showDayDelta = WidgetSettings.loadShowComparePref(context, appWidgetId);
        int numRows = 1, numCols = 2;
        numRows += showDayDelta ? 1 : 0;
        numRows += showSolarNoon ? 1 : 0;
        numCols += showSolarNoon ? 1 : 0;
        int[] maxDp = new int[] {(maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2] + 32)) / numCols,
                                 ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3])) / numRows)};
        float maxSp = ClockLayout.CLOCKFACE_MAX_SP;
        return adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, (showSeconds ? "00:00:00" : "00:00"), timeSizeSp, maxSp, "MM", suffixSizeSp, iconSizeDp);
    }
}
