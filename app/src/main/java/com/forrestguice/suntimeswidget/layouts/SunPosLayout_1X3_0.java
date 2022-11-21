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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.LightMapView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * A 1x3 layout with the lightmap graph.
 */
public class SunPosLayout_1X3_0 extends SunPosLayout_3X1_0
{
    public SunPosLayout_1X3_0()
    {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_sunpos_1x3_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);
        int position = (scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId));
        this.layoutID = chooseLayout(position);
        if (Build.VERSION.SDK_INT >= 16)
        {
            this.dpWidth = widgetSize[0];
            this.dpHeight = widgetSize[1];
        }
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_sunpos_1x3_0_align_fill;                       // fill
            case 1: case 7: case 4: return R.layout.layout_widget_sunpos_1x3_0_align_float_4;    // left
            case 3: case 9: case 6: return R.layout.layout_widget_sunpos_1x3_0_align_float_6;    // right
            case 2: case 8: case 5: default: return R.layout.layout_widget_sunpos_1x3_0;         // center
        }
    }

    @Override
    public void updateBitmap(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        LightMapView.LightMapTask drawTask = new LightMapView.LightMapTask();
        Bitmap bitmap = drawTask.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, 1.5f * dpHeight), SuntimesUtils.dpToPixels(context, dpWidth), colors);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        views.setImageViewBitmap(R.id.info_time_lightmap, rotatedBitmap);
        Log.d("DEBUG", "updateBitmap: " + dpWidth + "," + dpHeight);
    }
}
