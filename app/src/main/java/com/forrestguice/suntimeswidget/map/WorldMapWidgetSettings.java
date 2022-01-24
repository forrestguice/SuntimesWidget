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
    public static final WorldMapWidgetMode PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP1 = WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE1;

    public static final String PREF_KEY_WORLDMAP = "worldmap_";
    public static final String PREF_KEY_WORLDMAP_MAJORLATITUDES = "majorlatitudes";
    public static final String PREF_KEY_WORLDMAP_MINORGRID = "minorgrid";
    public static final String PREF_KEY_WORLDMAP_SUNSHADOW = "sunshadow";
    public static final String PREF_KEY_WORLDMAP_MOONLIGHT = "moonlight";
    public static final String PREF_KEY_WORLDMAP_LOCATION = "showlocation";
    public static final String PREF_KEY_WORLDMAP_SPEED1D = "speed_1d";

    public static final String[][] PREF_DEF_WORLDMAP = new String[][] {
            new String[] {PREF_KEY_WORLDMAP_MAJORLATITUDES, "false"},
            new String[] {PREF_KEY_WORLDMAP_MINORGRID, "false"},
            new String[] {PREF_KEY_WORLDMAP_SUNSHADOW, "true"},
            new String[] {PREF_KEY_WORLDMAP_MOONLIGHT, "true"},
            new String[] {PREF_KEY_WORLDMAP_LOCATION, "false"},
            new String[] {PREF_KEY_WORLDMAP_SPEED1D, "false"}
    };

    public static final String PREF_KEY_WORLDMAP_BACKGROUND = "background";

    public static final String PREF_KEY_WORLDMAP_CENTER_LATITUDE = "center_latitude";
    public static final String PREF_DEF_WORLDMAP_CENTER_LATITUDE = "33.45";

    public static final String PREF_KEY_WORLDMAP_CENTER_LONGITUDE = "center_longitude";
    public static final String PREF_DEF_WORLDMAP_CENTER_LONGITUDE = "-111.94";

    public static final String MAPTAG_3x2 = "";    // EMPTY
    public static final String MAPTAG_3x3 = "1";
    public static final String MAPTAG_DEF = MAPTAG_3x2;

    /**
     * WorldMapWidgetMode
     */
    public static enum WorldMapWidgetMode
    {
        EQUIRECTANGULAR_SIMPLE("Simple", "Equidistant Rectangular", R.layout.layout_widget_sunpos_3x2_0),
        EQUIRECTANGULAR_BLUEMARBLE("Blue Marble", "Equidistant Rectangular", R.layout.layout_widget_sunpos_3x2_0),
        EQUIAZIMUTHAL_SIMPLE("Polar [north]", "Equidistant Azimuthal", R.layout.layout_widget_sunpos_3x3_0),
        EQUIAZIMUTHAL_SIMPLE1("Polar [south]", "Equidistant Azimuthal", R.layout.layout_widget_sunpos_3x3_0),
        EQUIAZIMUTHAL_SIMPLE2("Polar [location]", "Equidistant Azimuthal", R.layout.layout_widget_sunpos_3x3_0);

        private final int layoutID;
        private String displayString;
        private String projectionString;

        private WorldMapWidgetMode(String displayString, String projectionString, int layoutID)
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

        public String getProjectionString() {
            return projectionString;
        }
        public void setProjectionString(String value) {
            projectionString = value;
        }

        public static void initDisplayStrings( Context context )
        {
            EQUIAZIMUTHAL_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal));
            EQUIAZIMUTHAL_SIMPLE1.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal_south));
            EQUIAZIMUTHAL_SIMPLE2.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal_location));
            EQUIRECTANGULAR_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simplerectangular));
            EQUIRECTANGULAR_BLUEMARBLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_bluemarble));

            EQUIAZIMUTHAL_SIMPLE.setProjectionString(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIAZIMUTHAL_SIMPLE1.setProjectionString(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIAZIMUTHAL_SIMPLE2.setProjectionString(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIRECTANGULAR_SIMPLE.setProjectionString(context.getString(R.string.worldmap_projection_equirectangular));
            EQUIRECTANGULAR_BLUEMARBLE.setProjectionString(context.getString(R.string.worldmap_projection_equirectangular));
        }
    }


    public static void saveSunPosMapModePref(Context context, int appWidgetId, WorldMapWidgetMode mode, String mapTag)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP + mapTag, mode.name());
        prefs.apply();
    }
    public static WorldMapWidgetMode loadSunPosMapModePref(Context context, int appWidgetId, String mapTag)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP + mapTag, defaultSunPosMapMode(mapTag).name());

        WorldMapWidgetMode widgetMode;
        try {
            widgetMode = WorldMapWidgetMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = defaultSunPosMapMode(mapTag);
            Log.w("loadSunPosMapModePref", "Failed to load value '" + modeString + "'; using default '" + widgetMode.name() + "'.");
        }
        return widgetMode;
    }
    public static void deleteSunPosMapModePref(Context context, int appWidgetId, String mapTag)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP + mapTag);
        prefs.apply();
    }
    public static WorldMapWidgetMode defaultSunPosMapMode(String mapTag)
    {
        if (mapTag.equals(MAPTAG_3x3)) {
            return PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP1;
        } else return PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP;
    }

    public static void saveWorldMapPref(Context context, int appWidgetId, String key, String mapTag, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        prefs.putBoolean(prefs_prefix + key + mapTag, value);
        prefs.apply();
    }
    public static boolean loadWorldMapPref(Context context, int appWidgetId, String key, String mapTag)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        return prefs.getBoolean(prefs_prefix + key + mapTag, defaultWorldMapFlag(key));
    }
    public static void deleteWorldMapPref(Context context, int appWidgetId, String key, String mapTag)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        prefs.remove(prefs_prefix + key + mapTag);
        prefs.apply();
    }

    public static void saveWorldMapString(Context context, int appWidgetId, String key, String mapTag, String value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        prefs.putString(prefs_prefix + key + mapTag, value);
        prefs.apply();
    }
    public static String loadWorldMapString(Context context, int appWidgetId, String key, String mapTag) {
        return loadWorldMapString(context, appWidgetId, key, mapTag, null);
    }
    public static String loadWorldMapString(Context context, int appWidgetId, String key, String mapTag, String defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        return prefs.getString(prefs_prefix + key + mapTag, defValue);
    }

    public static double[] loadWorldMapCenter(Context context, int appWidgetId, String mapTag) {
        return new double[] {
                Double.parseDouble(loadWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LATITUDE, mapTag, PREF_DEF_WORLDMAP_CENTER_LATITUDE)),
                Double.parseDouble(loadWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LONGITUDE, mapTag, PREF_DEF_WORLDMAP_CENTER_LONGITUDE))
        };
    }
    public static void saveWorldMapCenter(Context context, int appWidgetId, String mapTag, double[] value) {
        saveWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LATITUDE, mapTag, Double.toString(value[0]));
        saveWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LONGITUDE, mapTag, Double.toString(value[1]));
    }

    public static boolean defaultWorldMapFlag(String key)
    {
        for (String[] defaultValue : PREF_DEF_WORLDMAP)
        {
            if (defaultValue == null) {
                Log.e("defaultWorldMapPref", "Bad default mapping! null. skipping...");
                continue;

            } else if (defaultValue.length != 2) {
                Log.e("defaultWorldMapPref", "Bad default mapping! incorrect length " + defaultValue.length + ". skipping...");
                continue;
            }

            if (defaultValue[0].equals(key)) {
                return Boolean.parseBoolean(defaultValue[1]);
            }
        }
        Log.e("defaultWorldMapPref", "Bad default mapping! not found, returning false...");
        return false;
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
        deleteSunPosMapModePref(context, appWidgetId, MAPTAG_3x2);
        deleteSunPosMapModePref(context, appWidgetId, MAPTAG_3x3);
        deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, MAPTAG_3x2);
        deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, MAPTAG_3x3);
    }

}
