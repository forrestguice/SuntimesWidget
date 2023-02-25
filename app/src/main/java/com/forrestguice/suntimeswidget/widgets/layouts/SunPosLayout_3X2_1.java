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
import com.forrestguice.suntimeswidget.graph.LineGraphView;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import static com.forrestguice.suntimeswidget.LightMapDialog.DEF_KEY_GRAPH_FILLPATH;
import static com.forrestguice.suntimeswidget.LightMapDialog.DEF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.LightMapDialog.DEF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.LightMapDialog.DEF_KEY_GRAPH_SHOWMOON;
import static com.forrestguice.suntimeswidget.LightMapDialog.DEF_KEY_WORLDMAP_MINORGRID;
import static com.forrestguice.suntimeswidget.LightMapDialog.MAPTAG_LIGHTMAP;
import static com.forrestguice.suntimeswidget.LightMapDialog.PREF_KEY_GRAPH_FILLPATH;
import static com.forrestguice.suntimeswidget.LightMapDialog.PREF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.LightMapDialog.PREF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.LightMapDialog.PREF_KEY_GRAPH_SHOWMOON;

/**
 * A 3x2 line graph
 */
public class SunPosLayout_3X2_1 extends SunPosLayout
{
    public SunPosLayout_3X2_1() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_sunpos_3x2_1;
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_sunpos_3x2_1_align_fill;                         // fill
            case 1: case 2: case 3: return R.layout.layout_widget_sunpos_3x2_1_align_float_2;      // top
            case 7: case 8: case 9: return R.layout.layout_widget_sunpos_3x2_1_align_float_8;      // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_sunpos_3x2_1;           // center
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
            this.dpHeight = widgetSize[1];
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

        LineGraphView.LineGraphTask drawTask = new LineGraphView.LineGraphTask();
        options.densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        options.setTimeFormat(context, WidgetSettings.loadTimeFormatModePref(context, 0));
        Bitmap bitmap = drawTask.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), options);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.info_time_graph, bitmap);
            //Log.d("DEBUG", "graph is " + bitmap.getWidth() + " x " + bitmap.getHeight());
        }
    }

    protected LineGraphView.LineGraphOptions options;
    protected int dpWidth = 512, dpHeight = 256;

    @Override
    public void themeViews(Context context, RemoteViews views, int appWidgetId)
    {
        super.themeViews(context, views, appWidgetId);
        //options.axisX_labels_show = options.axisY_labels_show = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
    }

    @SuppressLint("ResourceType")
    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        options = new LineGraphView.LineGraphOptions();
        if (theme.getBackground() == SuntimesTheme.ThemeBackground.LIGHT)
            options.initDefaultLight(context);
        else options.initDefaultDark(context);

        options.colorDay = theme.getDayColor();
        options.colorCivil = theme.getCivilColor();
        options.colorNautical = theme.getNauticalColor();
        options.colorAstro = theme.getAstroColor();
        options.colorNight = theme.getNightColor();
        options.colorPointFill = theme.getGraphPointFillColor();
        options.colorPointStroke = theme.getGraphPointStrokeColor();

        options.colorBackground = options.colorNight;
        options.sunPath_color_day = options.sunPath_color_day_closed = options.colorDay;
        options.sunPath_color_night = options.sunPath_color_night_closed = options.colorNautical;
        options.moonPath_color_day = options.moonPath_color_day_closed = theme.getMoonriseTextColor();
        options.moonPath_color_night = options.moonPath_color_night_closed = theme.getMoonsetTextColor();

        options.graph_width = LineGraphView.MINUTES_IN_DAY;
        options.graph_height = 180;
        options.graph_x_offset = options.graph_y_offset = 0;
        options.gridX_minor_show = options.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, DEF_KEY_WORLDMAP_MINORGRID);
        options.axisX_labels_show = options.axisY_labels_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWLABELS);
        options.axisX_show = options.axisY_show = options.gridY_major_show = options.gridX_major_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWAXIS);
        options.sunPath_show_line = true;
        options.sunPath_show_fill = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH);
        options.moonPath_show_line = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWMOON);
        options.moonPath_show_fill = options.sunPath_show_fill;

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
