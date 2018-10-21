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
package com.forrestguice.suntimeswidget.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class WorldMapWidgetSettings
{

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP = "widgetmode_sunposmap";
    public static final WorldMapWidgetMode PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP = WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE;

    /**
     * WorldMapWidgetMode
     */
    public static enum WorldMapWidgetMode
    {
        EQUIRECTANGULAR_SIMPLE("Simple", R.layout.layout_widget_sunpos_3x2_0),
        EQUIRECTANGULAR_BLUEMARBLE("Blue Marble", R.layout.layout_widget_sunpos_3x2_0),
        EQUIAZIMUTHAL_SIMPLE("Polar", R.layout.layout_widget_sunpos_3x2_0);

        private final int layoutID;
        private String displayString;

        private WorldMapWidgetMode(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public String toString()
        {
            return displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            EQUIAZIMUTHAL_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal));
            EQUIRECTANGULAR_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simplerectangular));
            EQUIRECTANGULAR_BLUEMARBLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_bluemarble));
        }
    }


    public static void saveSunPosMapModePref(Context context, int appWidgetId, WorldMapWidgetMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP, mode.name());
        prefs.apply();
    }
    public static WorldMapWidgetMode loadSunPosMapModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP, PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP.name());

        WorldMapWidgetMode widgetMode;
        try {
            widgetMode = WorldMapWidgetMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP;
            Log.w("loadSunPosMapModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP.name() + "'.");
        }
        return widgetMode;
    }
    public static void deleteSunPosMapModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP);
        prefs.apply();
    }


    /**
     * @param context
     */
    public static void initDisplayStrings( Context context )
    {
        WorldMapWidgetMode.initDisplayStrings(context);
    }

    /**
     * @param context
     * @param appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteSunPosMapModePref(context, appWidgetId);
    }

}
