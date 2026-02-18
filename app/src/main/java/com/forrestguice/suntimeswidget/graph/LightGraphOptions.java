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

package com.forrestguice.suntimeswidget.graph;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.util.Resources;
import com.forrestguice.util.SystemTimeFormat;

import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

@SuppressWarnings("WeakerAccess")
public class LightGraphOptions
{
    public static final int DRAW_NONE = 0;
    public static final int DRAW_NOW1 = 1;    // solid
    public static final int DRAW_NOW2 = 2;    // dashed

    public double graph_width = 365;    // days
    public double graph_x_offset = 0;   // days

    public double graph_height = 24;                     // hours
    public double graph_y_offset = 0;                    // hours

    // X-Axis
    public boolean axisX_show = true;
    public double axisX_width = 365;   // days

    public boolean axisX_labels_show = true;
    public float axisX_labels_textsize_ratio = 20;
    public float axisX_labels_interval = 30;  // days

    // Y-Axis
    public boolean axisY_show = true;
    public double axisY_width = 300;    // ~5m minutes
    public int axisY_interval = 60 * 12;        // dp

    public boolean axisY_labels_show = true;
    public float axisY_labels_textsize_ratio = 20;
    public float axisY_labels_interval = 3;  // hours

    // Grid-X
    public boolean gridX_major_show = true;
    public double gridX_major_width = 300;        // minutes
    public float gridX_major_interval = axisY_labels_interval;    // hours

    public boolean gridX_minor_show = true;
    public double gridX_minor_width = 400;        // minutes
    public float gridX_minor_interval = 1;    // hours

    // Grid-Y
    public boolean gridY_major_show = true;
    public double gridY_major_width = 300;       // minutes
    public float gridY_major_interval = axisX_labels_interval;   // days

    public boolean gridY_minor_show = true;
    public double gridY_minor_width = 400;       // minutes
    public float gridY_minor_interval = 5;       // days

    public boolean sunPath_show_line = false;
    public boolean sunPath_show_fill = true;
    public boolean sunPath_show_points = LightGraphView.DEF_KEY_GRAPH_SHOWPOINTS;

    public double sunPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
    public float sunPath_points_width = 150;

    public boolean localizeToHemisphere = true;
    public boolean showSeasons = true;
    public boolean showCivil = true, showNautical = true, showAstro = true;
    public int option_drawNow = DRAW_NOW1;
    public int option_drawNow_pointSizePx = -1;    // when set, use a fixed point size

    public boolean option_drawNow_crosshair = LightGraphView.DEF_KEY_GRAPH_SHOWCROSSHAIR;

    public int densityDpi = 160;  // DisplayMetrics.DENSITY_DEFAULT;

    public boolean is24 = false;
    public void setTimeFormat(TimeFormatMode timeFormat) {
        is24 = ((timeFormat == TimeFormatMode.MODE_24HR) || (timeFormat == TimeFormatMode.MODE_SYSTEM && SystemTimeFormat.is24HourFormat()));
    }

    public void setLocation(Location value) {
        location = value;
        longitude = location.getLongitudeAsDouble();
    }
    public Location location = null;
    public double longitude;

    public long offsetDays = 0;
    public long now = -1L;
    public int anim_frameLengthMs = 100;         // frames shown for 200 ms
    public int anim_frameOffsetDays = 1;         // each frame 1 day apart
    public Lock anim_lock = null;

    public TimeZone timezone = null;
    public LightGraphColorValues colors;

    @Nullable
    public EarliestLatestSunriseSunsetData earliestLatestData;

    public LightGraphOptions() {}

    @SuppressWarnings("ResourceType")
    public LightGraphOptions(Resources context) {
        init(context);
    }

    protected void init(Resources context)
    {
        colors = new LightGraphColorValues(context);
        //gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
        //gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
        //axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
        //axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
        //sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
        //axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
        //axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());
    }

    public void initDefaultDark(Resources context) {
        init(context);
    }

    public void initDefaultLight(Resources context) {
        init(context);
    }

    public void acquireDrawLock()
    {
        if (anim_lock != null) {
            anim_lock.lock();
            //Log.d("DEBUG", "GraphView :: acquire " + anim_lock);
        }
    }
    public void releaseDrawLock()
    {
        if (anim_lock != null) {
            //Log.d("DEBUG", "GraphView :: release " + anim_lock);
            anim_lock.unlock();
        }
    }

}
