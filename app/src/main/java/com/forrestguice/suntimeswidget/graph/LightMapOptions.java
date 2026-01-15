/**
    Copyright (C) 2014-2022 Forrest Guice
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

import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.util.android.AndroidResources;

import java.util.concurrent.locks.Lock;

/**
 * LightMapColors
 */
@SuppressWarnings("WeakerAccess")
public class LightMapOptions
{
    public static final String MAPTAG_LIGHTMAP = "_lightmap";

    public void setOption_drawNow(SunSymbol symbol) {
        option_drawNow = SunSymbolBitmap.fromSunSymbol(symbol);
    }

    public int option_drawNow = SunSymbolBitmap.DRAW_SUN_CIRCLEDOT_SOLID;
    public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size
    public boolean option_lmt = false;

    public boolean option_drawNoon = false;

    public long offsetMinutes = 0;
    public long now = -1L;
    public int anim_frameLengthMs = 100;         // frames shown for 200 ms
    public int anim_frameOffsetMinutes = 1;      // each frame 1 minute apart
    public Lock anim_lock = null;

    public LightMapColorValues values;

    public LightMapOptions() {
        values = new LightMapColorValues();
    }

    @SuppressWarnings("ResourceType")
    public LightMapOptions(Context context) {
        init(context);
    }

    public void initDefaultDark(Context context) {
        values = new LightMapColorValues(values.getDefaultValues(AndroidResources.wrap(context), true));      // TODO: Resources
    }

    public void initDefaultLight(Context context) {
        values = new LightMapColorValues(values.getDefaultValues(AndroidResources.wrap(context), false));       // TODO: Resources
    }

    public void init(Context context) {
        values = new LightMapColorValues(AndroidResources.wrap(context));
    }

    public void acquireDrawLock()
    {
        if (anim_lock != null) {
            anim_lock.lock();
            //Log.d("DEBUG", "MapView :: acquire " + anim_lock);
        }
    }
    public void releaseDrawLock()
    {
        if (anim_lock != null) {
            //Log.d("DEBUG", "MapView :: release " + anim_lock);
            anim_lock.unlock();
        }
    }
}
