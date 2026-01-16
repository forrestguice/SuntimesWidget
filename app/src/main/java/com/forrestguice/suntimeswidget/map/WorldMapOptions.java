/**
    Copyright (C) 2018-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.util.android.AndroidResources;

public class WorldMapOptions
{
    public boolean modified = false;

    public WorldMapOptions() {
        colors = new WorldMapColorValues();
    }
    public WorldMapOptions(Context context) {
        init(context);
    }

    public void init(Context context) {
        colors = new WorldMapColorValues(AndroidResources.wrap(context), true);
    }
    public WorldMapColorValues colors;
    public int foregroundColor = Color.TRANSPARENT;

    @Nullable
    public Drawable map = null;                  // BitmapDrawable
    @Nullable
    public Drawable map_night = null;            // BitmapDrawable
    public boolean tintForeground = true;
    public boolean hasTransparentBaseMap = true;
    public boolean showDebugLines = false;

    public boolean showGrid = false;

    public boolean showMajorLatitudes = false;
    public int[] latitudeColors = { Color.DKGRAY, Color.WHITE, Color.DKGRAY };    // equator, tropics, polar circle
    float[][] latitudeLinePatterns = new float[][] {{ 0, 0 }, {5, 10}, {10, 5}};    // {dash-on, dash-off} .. for equator, tropics, and polar circle .. dash-on 0 for a solid line
    public float latitudeLineScale = 0.5f;

    public boolean showSunPosition = true;
    public boolean showSunShadow = true;
    public boolean showMoonPosition = true;
    public boolean showMoonLight = true;

    public int sunScale = 48;                     // 48; default 48 suns fit within the width of the image (which is 24 hr wide meaning the sun has diameter of a half-hour)
    public int sunStrokeScale = 3;                // 3; default 3 strokes fit within the radius of the sun (i.e. the stroke is 1/3 the width)

    public int moonScale = 72;                    // 72; default moonscale is 3/4 the size of the sun (48)
    public int moonStrokeScale = 3;               // 3; default 3 strokes fit within the radius of the moon

    public boolean translateToLocation = false;

    public double[] center = null;
    @Nullable
    public double[][] locations = null;  // a list of locations {{lat, lon}, {lat, lon}, ...} or null
    public double locationScale = 1 / 192d;

    public long offsetMinutes = 0;    // minutes offset from "now" (default 0)
    public long now = -1;            // -1 (current)

    public int anim_frameLengthMs = 100;         // frames shown for 100 ms
    public int anim_frameOffsetMinutes = 3;      // each frame 3 minutes apart
}
