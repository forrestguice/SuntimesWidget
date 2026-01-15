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

package com.forrestguice.suntimeswidget.graph;

import android.content.Context;
import android.util.DisplayMetrics;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;
import com.forrestguice.util.android.AndroidResources;

import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

/**
 * LineGraphOptions
 */
@SuppressWarnings("WeakerAccess")
public class LineGraphOptions
{
    public static final int DRAW_NONE = 0;
    public static final int DRAW_SUN1 = 1;    // solid stroke
    public static final int DRAW_SUN2 = 2;    // dashed stroke

    public double graph_width = LineGraphView.MINUTES_IN_DAY * 0.5d;    // minutes
    public double graph_x_offset = LineGraphView.MINUTES_IN_DAY / 4d;   // minutes

    public double graph_height = 55;                     // degrees
    public double graph_y_offset = 20;                   // degrees

    // X-Axis
    public boolean axisX_show = true;
    public double axisX_width = 5d * LineGraphView.MINUTES_IN_DAY_RATIO;   // minutes ratio

    public boolean axisX_labels_show = true;
    public float axisX_labels_textsize_ratio = 10;
    public float axisX_labels_interval = 60 * 3;  // minutes

    // Y-Axis
    public boolean axisY_show = true;
    public double axisY_width = 7.5d * LineGraphView.MINUTES_IN_DAY_RATIO;     // minutes ratio
    public int axisY_interval = 60 * 12;        // dp

    public boolean axisY_labels_show = true;
    public float axisY_labels_textsize_ratio = 10;
    public float axisY_labels_interval = 45;  // degrees

    // Grid-X
    public boolean gridX_major_show = true;
    public double gridX_major_width = 4d * LineGraphView.MINUTES_IN_DAY_RATIO;        // minutes ratio
    public float gridX_major_interval = axisY_labels_interval;    // degrees

    public boolean gridX_minor_show = true;
    public double gridX_minor_width = 2d * LineGraphView.MINUTES_IN_DAY_RATIO;        // minutes ratio
    public float gridX_minor_interval = 5;    // degrees

    // Grid-Y
    public boolean gridY_major_show = true;
    public double gridY_major_width = 4d * LineGraphView.MINUTES_IN_DAY_RATIO;       // minutes ratio
    public float gridY_major_interval = axisX_labels_interval;   // minutes

    public boolean gridY_minor_show = true;
    public double gridY_minor_width = 2d * LineGraphView.MINUTES_IN_DAY_RATIO;       // minutes ratio
    public float gridY_minor_interval = 60;   // minutes

    public boolean sunPath_show_line = true;
    public boolean sunPath_show_fill = true;
    public boolean sunPath_show_points = false;
    //public int sunPath_color_day_closed = Color.YELLOW;
    public int sunPath_color_day_closed_alpha = 200;
    //public int sunPath_color_night_closed = Color.BLUE;
    public int sunPath_color_night_closed_alpha = 200;
    public double sunPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
    public int sunPath_interval = 5;   // minutes

    public double[] sunPath_points_elevations = new double[] { 30, -50 };  // TODO
    //public int sunPath_points_color = Color.MAGENTA;    // TODO
    public float sunPath_points_width = 150;

    public boolean moonPath_show_line = true;
    public boolean moonPath_show_fill = true;
    //public int moonPath_color_day_closed = Color.LTGRAY;
    public int moonPath_color_day_closed_alpha = 200;
    //public int moonPath_color_night_closed = Color.CYAN;
    public int moonPath_color_night_closed_alpha = 200;
    public double moonPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
    public int moonPath_interval = 5;   // minutes

    public int option_drawNow = DRAW_SUN1;
    public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size

    public int densityDpi = DisplayMetrics.DENSITY_DEFAULT;

    public LineGraphColorValues colors;
    public int getColor(String key) {
        return colors.getColor(key);
    }

    public boolean is24 = false;
    public void setTimeFormat(Context context, TimeFormatMode timeFormat) {
        is24 = ((timeFormat == TimeFormatMode.MODE_24HR) || (timeFormat == TimeFormatMode.MODE_SYSTEM && android.text.format.DateFormat.is24HourFormat(context)));
    }

    public Location location = null;
    public TimeZone timezone = null;

    public long offsetMinutes = 0;
    public long now = -1L;
    public int anim_frameLengthMs = 100;         // frames shown for 200 ms
    public int anim_frameOffsetMinutes = 1;      // each frame 1 minute apart
    public Lock anim_lock = null;

    public LineGraphOptions() {
        colors = new LineGraphColorValues();
    }

    @SuppressWarnings("ResourceType")
    public LineGraphOptions(Context context) {
        init(context);
    }

    public void init(Context context)
    {
        colors = new LineGraphColorValues(AndroidResources.wrap(context));
        //gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
        //gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
        //axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
        //axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
        //sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
        //axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
        //axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());
    }

    public void initDefaultDark(Context context)
    {
        init(context);
        colors = new LineGraphColorValues(colors.getDefaultValues(AndroidResources.wrap(context), true));  // TODO: Resources
    }

    public void initDefaultLight(Context context)
    {
        init(context);
        colors = new LineGraphColorValues(colors.getDefaultValues(AndroidResources.wrap(context), false));  // TODO: Resources
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
