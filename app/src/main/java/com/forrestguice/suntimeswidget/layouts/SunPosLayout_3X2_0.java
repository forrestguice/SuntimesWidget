/**
   Copyright (C) 2018-2019 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal1;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal2;
import com.forrestguice.suntimeswidget.map.WorldMapEquirectangular;
import com.forrestguice.suntimeswidget.map.WorldMapTask;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
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
        WorldMapWidgetSettings.WorldMapWidgetMode mapMode = getMapMode(context, appWidgetId);
        WorldMapTask.WorldMapProjection projection;
        switch (mapMode)
        {
            case EQUIAZIMUTHAL_SIMPLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap2);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal();
                break;

            case EQUIAZIMUTHAL_SIMPLE1:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap3);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal1();
                break;

            case EQUIAZIMUTHAL_SIMPLE2:
                options.map = null;    // TODO
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal2();
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.land_shallow_topo_1024);
                options.map_night = ContextCompat.getDrawable(context, R.drawable.earth_lights_lrg_1024);
                options.hasTransparentBaseMap = false;
                options.foregroundColor = Color.TRANSPARENT;
                projection = new WorldMapEquirectangular();
                break;

            case EQUIRECTANGULAR_SIMPLE:
            default:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquirectangular();
                break;
        }

        boolean showLocation = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, getMapTag());
        if (showLocation) {
            Location location = dataset.location();
            options.locations = new double[][] {{location.getLatitudeAsDouble(), location.getLongitudeAsDouble()}};
        }

        Bitmap bitmap = projection.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), options);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.info_time_worldmap, bitmap);
            Log.d("DEBUG", "map is " + bitmap.getWidth() + " x " + bitmap.getHeight());
        }
    }

    protected WorldMapTask.WorldMapOptions options;
    protected int dpWidth = 512, dpHeight = 256;

    @SuppressLint("ResourceType")
    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        options = new WorldMapTask.WorldMapOptions();

        options.backgroundColor = theme.getMapBackgroundColor();
        options.foregroundColor = theme.getMapForegroundColor();

        options.sunShadowColor = theme.getMapShadowColor();
        options.moonLightColor = theme.getMapHighlightColor();

        options.sunFillColor = theme.getNoonIconColor();
        options.sunStrokeColor = theme.getNoonIconStrokeColor();

        options.moonFillColor = theme.getMoonFullColor();
        options.moonStrokeColor = theme.getMoonWaningColor();

        options.showMoonLight = true;
        options.showMajorLatitudes = false;
    }

    public WorldMapWidgetSettings.WorldMapWidgetMode getMapMode(Context context, int appWidgetId) {
        return WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, getMapTag());
    }

    public String getMapTag()
    {
        return WorldMapWidgetSettings.MAPTAG_3x2;
    }

}
