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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal1;
import com.forrestguice.suntimeswidget.map.WorldMapEquiazimuthal2;
import com.forrestguice.suntimeswidget.map.WorldMapEquirectangular;
import com.forrestguice.suntimeswidget.map.WorldMapMercator;
import com.forrestguice.suntimeswidget.map.WorldMapSinusoidal;
import com.forrestguice.suntimeswidget.map.WorldMapTask;
import com.forrestguice.suntimeswidget.map.WorldMapView;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
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
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);

        if (Build.VERSION.SDK_INT >= 16)
        {
            this.dpWidth = widgetSize[0];
            this.dpHeight = widgetSize[1];
        }
    }

    public static WorldMapTask.WorldMapProjection createProjectionForMode(Context context, WorldMapWidgetSettings.WorldMapWidgetMode mapMode, WorldMapTask.WorldMapOptions options)
    {
        options.tintForeground = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapMode.getMapTag());
        if (!options.tintForeground) {
            options.foregroundColor = Color.TRANSPARENT;    // override color assigned by themeViews
        }

        options.center = WorldMapWidgetSettings.loadWorldMapCenter(context, 0, mapMode.getMapTag(), mapMode.getProjectionCenter());
        Drawable background = WorldMapView.loadBackgroundDrawable(context, mapMode.getMapTag(), options.center);

        WorldMapTask.WorldMapProjection projection;
        switch (mapMode)
        {
            case MERCATOR_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap_mercator);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapMercator();
                break;

            case VANDERGRINTEN_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap_van_der_grinten);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapMercator();
                break;

            case SINUSOIDAL_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap_sinusoidal);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapSinusoidal();
                break;

            case EQUIAZIMUTHAL_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap2);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal();
                break;

            case EQUIAZIMUTHAL_SIMPLE1:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap3);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal1();
                break;

            case EQUIAZIMUTHAL_SIMPLE2:
                options.map = background;  // ContextCompat.getDrawable(context, R.drawable.worldmap4);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquiazimuthal2();
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.world_topo_bathy_1024x512);
                options.map_night = ContextCompat.getDrawable(context, R.drawable.earth_lights_lrg_1024);
                options.hasTransparentBaseMap = false;
                options.foregroundColor = Color.TRANSPARENT;
                projection = new WorldMapEquirectangular();
                break;

            case EQUIRECTANGULAR_SIMPLE:
            default:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap);
                options.map_night = null;
                options.hasTransparentBaseMap = true;
                projection = new WorldMapEquirectangular();
                break;
        }
        return projection;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);
        WorldMapWidgetSettings.WorldMapWidgetMode mapMode = getMapMode(context, appWidgetId);
        WorldMapTask.WorldMapProjection projection = createProjectionForMode(context, mapMode, options);

        boolean showLocation = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2);
        if (showLocation) {
            Location location = dataset.location();
            options.locations = new double[][] {{location.getLatitudeAsDouble(), location.getLongitudeAsDouble()}};
        }

        Bitmap bitmap = projection.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), options);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.info_time_worldmap, bitmap);
            //Log.d("DEBUG", "map is " + bitmap.getWidth() + " x " + bitmap.getHeight());
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

        options.colors.setColor(WorldMapColorValues.COLOR_BACKGROUND, theme.getMapBackgroundColor());
        options.colors.setColor(WorldMapColorValues.COLOR_FOREGROUND, theme.getMapForegroundColor());
        options.foregroundColor = theme.getMapForegroundColor();

        options.colors.setColor(WorldMapColorValues.COLOR_SUN_SHADOW, theme.getMapShadowColor());
        options.colors.setColor(WorldMapColorValues.COLOR_MOON_LIGHT, theme.getMapHighlightColor());

        options.colors.setColor(WorldMapColorValues.COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(WorldMapColorValues.COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());

        options.colors.setColor(WorldMapColorValues.COLOR_MOON_FILL, theme.getMoonFullColor());
        options.colors.setColor(WorldMapColorValues.COLOR_MOON_STROKE, theme.getMoonWaningColor());

        options.colors.setColor(WorldMapColorValues.COLOR_GRID_MAJOR, options.colors.getColor(WorldMapColorValues.COLOR_MOON_LIGHT));
        options.colors.setColor(WorldMapColorValues.COLOR_GRID_MINOR, options.colors.getColor(WorldMapColorValues.COLOR_MOON_LIGHT));
        options.colors.setColor(WorldMapColorValues.COLOR_POINT_FILL, theme.getActionColor());

        options.latitudeColors[0] = options.colors.getColor(WorldMapColorValues.COLOR_AXIS);
        options.latitudeColors[1] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);
        options.latitudeColors[2] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);

        options.showSunShadow = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2);             // uses app setting // TODO: from widget settings
        options.showMoonLight = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2);             // uses app setting // TODO: from widget settings
        options.showMajorLatitudes = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);   // uses app setting // TODO: from widget settings
        options.showGrid = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2);                  // uses app setting // TODO: from widget settings
        options.showDebugLines = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_DEBUGLINES, WorldMapWidgetSettings.MAPTAG_3x2);           // uses app setting // TODO: from widget settings
    }

    public WorldMapWidgetSettings.WorldMapWidgetMode getMapMode(Context context, int appWidgetId) {
        return WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, getMapTag());
    }

    public String getMapTag()
    {
        return WorldMapWidgetSettings.MAPTAG_3x2;
    }

}
