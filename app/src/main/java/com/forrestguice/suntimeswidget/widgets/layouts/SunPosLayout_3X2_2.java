/**
   Copyright (C) 2024 Forrest Guice
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
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.graph.LightGraphOptions;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.suntimeswidget.graph.LightGraphView;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.util.android.AndroidResources;

import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_WORLDMAP_MINORGRID;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWASTRO;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWCIVIL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWNAUTICAL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWSEASONS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWASTRO;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWCIVIL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWNAUTICAL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWSEASONS;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_STROKE;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_ASTRONOMICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_CIVIL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_DAY;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NAUTICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NIGHT;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_STROKE;
import static com.forrestguice.suntimeswidget.graph.LightGraphDialog.MAPTAG_LIGHTGRAPH;

/**
 * A 3x2 sunlight graph
 */
public class SunPosLayout_3X2_2 extends SunPosLayout
{
    public SunPosLayout_3X2_2() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_sunpos_3x2_2;
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_sunpos_3x2_2_align_fill;                         // fill
            case 1: case 2: case 3: return R.layout.layout_widget_sunpos_3x2_2_align_float_2;      // top
            case 7: case 8: case 9: return R.layout.layout_widget_sunpos_3x2_2_align_float_8;      // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_sunpos_3x2_2;           // center
        }
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
            this.dpHeight = (int)(1.5 * widgetSize[1]);
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId)) {
                scaleLabels(context, views);
            }
        }
        updateLabels(context, views, dataset);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.info_time_lightmap_labels, (showLabels ? View.VISIBLE : View.GONE));

        String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID);
        options.timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? dataset.timezone()
                : WidgetTimezones.getTimeZone(tzId, dataset.location().getLongitudeAsDouble(), dataset.calculator());

        LightGraphView.LightGraphTask drawTask = new LightGraphView.LightGraphTask();

        SuntimesRiseSetDataset[] yearData = LightGraphView.LightGraphTask.createYearData(context, dataset);
        drawTask.setData(yearData);

        options.densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        options.setTimeFormat(context, WidgetSettings.loadTimeFormatModePref(context, 0));
        Bitmap bitmap = drawTask.makeBitmap( yearData, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), options );
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.info_time_graph, bitmap);
            //Log.d("DEBUG", "graph is " + bitmap.getWidth() + " x " + bitmap.getHeight());
        }
    }

    protected LightGraphOptions options;
    protected int dpWidth = 512, dpHeight = 512;

    @Override
    public void themeViews(Context context, RemoteViews views, int appWidgetId)
    {
        super.themeViews(context, views, appWidgetId);
        options.axisX_labels_show = options.axisY_labels_show = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        options.showSeasons = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWSEASONS);
    }

    @SuppressLint("ResourceType")
    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        options = new LightGraphOptions(context);

        options.colors = LightGraphColorValues.getColorDefaults(AndroidResources.wrap(context), (theme.getBackground() == SuntimesTheme.ThemeBackground.DARK));
        options.colors.setColor(COLOR_DAY, theme.getDayColor());
        options.colors.setColor(COLOR_CIVIL, theme.getCivilColor());
        options.colors.setColor(COLOR_NAUTICAL, theme.getNauticalColor());
        options.colors.setColor(COLOR_ASTRONOMICAL, theme.getAstroColor());
        options.colors.setColor(COLOR_NIGHT, theme.getNightColor());
        options.colors.setColor(COLOR_POINT_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        options.colors.setColor(COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());

        options.graph_width = 365;    // days
        options.graph_height = 24;    // hours
        options.graph_x_offset = options.graph_y_offset = 0;

        options.axisX_show = options.axisY_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWAXIS);
        options.gridX_minor_show = options.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, DEF_KEY_WORLDMAP_MINORGRID);

        options.showSeasons = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWSEASONS);
        options.showCivil = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCIVIL);
        options.showNautical = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWNAUTICAL);
        options.showAstro = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWASTRO);

        options.gridX_major_show = options.gridY_major_show = false;
        options.axisX_width = options.axisY_width = 365;

        options.sunPath_show_points = true;
        options.sunPath_show_fill = true; //WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH);

        themeViewsAzimuthElevationText(context, views, theme);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_rising, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_atnoon, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_azimuth_setting, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }


}
