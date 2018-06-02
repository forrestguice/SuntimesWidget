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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.map.WorldMapView;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * A 3x2 layout with world map.
 */
public class SunPosLayout_3X2_0 extends SunPosLayout
{
    public SunPosLayout_3X2_0()
    {
        super();
    }

    /**public SunPosLayout_3X1_0(int layoutID )
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_3x2_0;
    }

    @Override
    public void prepareForUpdate(SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(dataset, widgetSize);
        if (Build.VERSION.SDK_INT >= 16)
        {
            this.dpWidth = widgetSize[0];
            this.dpHeight = widgetSize[1];
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);

        //boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        //int labelVisibility = (showLabels ? View.VISIBLE : View.GONE);
        //views.setViewVisibility(R.id.info_time_worldmap_labels, visibility);   // TODO

        WorldMapView.WorldMapTask drawTask = new WorldMapView.WorldMapTask();
        Bitmap bitmap = drawTask.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), options);
        views.setImageViewBitmap(R.id.info_time_worldmap, bitmap);

        Log.d("DEBUG", "map is " + bitmap.getWidth() + " x " + bitmap.getHeight());
        //if (Build.VERSION.SDK_INT >= 15) {
            //views.setContentDescription(R.id.info_time_worldmap, buildContentDescription(context, now, sunPosition));
            // TODO
        //}
    }

    private WorldMapView.WorldMapOptions options;
    private int dpWidth = 512, dpHeight = 285;

    @SuppressLint("ResourceType")
    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        options = new WorldMapView.WorldMapOptions();     // TODO: themable

        //options.map = ContextCompat.getDrawable(context, R.drawable.land_shallow_topo_1024);
        options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
        options.backgroundColor = ContextCompat.getColor(context, R.color.map_background);
        options.foregroundColor = ContextCompat.getColor(context, R.color.map_foreground);
        options.sunShadowColor = ContextCompat.getColor(context, R.color.map_sunshadow);
        options.moonLightColor = ContextCompat.getColor(context, R.color.map_moonlight);
        options.sunFillColor = ContextCompat.getColor(context, R.color.sunIcon_color_rising_dark);
        options.sunStrokeColor = ContextCompat.getColor(context, R.color.sunIcon_color_risingBorder_dark);
        options.moonFillColor = ContextCompat.getColor(context, R.color.moonIcon_color_full_dark);
        options.moonStrokeColor = ContextCompat.getColor(context, R.color.moonIcon_color_full_border_dark);
        options.showMoonLight = false;
        options.showMajorLatitudes = false;
    }

}
