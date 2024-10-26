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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.PrefTypeInfo;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Map;
import java.util.TreeMap;

public class WorldMapWidgetSettings
{

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP = "widgetmode_sunposmap";
    public static final WorldMapWidgetMode PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP = WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE;
    public static final WorldMapWidgetMode PREF_DEF_APPEARANCE_WIDGETMODE_WORLDMAP1 = WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE1;

    public static final String PREF_KEY_WORLDMAP = "worldmap_";
    public static final String PREF_KEY_WORLDMAP_MAJORLATITUDES = "majorlatitudes";
    public static final String PREF_KEY_WORLDMAP_MINORGRID = "minorgrid";
    public static final String PREF_KEY_WORLDMAP_DEBUGLINES = "debuglines";
    public static final String PREF_KEY_WORLDMAP_TINTMAP = "tintmap";
    public static final String PREF_KEY_WORLDMAP_SUNSHADOW = "sunshadow";
    public static final String PREF_KEY_WORLDMAP_MOONLIGHT = "moonlight";
    public static final String PREF_KEY_WORLDMAP_LOCATION = "showlocation";
    public static final String PREF_KEY_WORLDMAP_SPEED1D = "speed_1d";

    public static final String[][] PREF_DEF_WORLDMAP = new String[][] {
            new String[] {PREF_KEY_WORLDMAP_MAJORLATITUDES, "false"},
            new String[] {PREF_KEY_WORLDMAP_MINORGRID, "false"},
            new String[] {PREF_KEY_WORLDMAP_DEBUGLINES, "false"},
            new String[] {PREF_KEY_WORLDMAP_TINTMAP, "true"},
            new String[] {PREF_KEY_WORLDMAP_SUNSHADOW, "true"},
            new String[] {PREF_KEY_WORLDMAP_MOONLIGHT, "true"},
            new String[] {PREF_KEY_WORLDMAP_LOCATION, "false"},
            new String[] {PREF_KEY_WORLDMAP_SPEED1D, "false"}
    };

    public static final String PREF_KEY_WORLDMAP_BACKGROUND = "background";

    public static final double[] PREF_DEF_WORLDMAP_CENTER = new double[] { 33.45, -111.94 };
    public static final String PREF_KEY_WORLDMAP_CENTER_LABEL = "center_label";
    public static final String PREF_KEY_WORLDMAP_CENTER_LATITUDE = "center_latitude";
    public static final String PREF_KEY_WORLDMAP_CENTER_LONGITUDE = "center_longitude";

    public static final String PREF_KEY_WORLDMAP_TIMEZONE = "timezone";
    public static final String PREF_DEF_WORLDMAP_TIMEZONE = "UTC";

    public static final String MAPTAG_3x2 = "";    // EMPTY
    public static final String MAPTAG_3x3 = "1";
    public static final String MAPTAG_DEF = MAPTAG_3x2;
    public static final String[] MAPTAGS = new String[] { MAPTAG_3x2, MAPTAG_3x3 };

    public static final String PROJ4_EQD = "+proj=eqc +lat_ts=0 +lat_0=%1$s +lon_0=%2$s +x_0=0 +y_0=0 +a=6371007 +b=6371007 +units=m +no_defs";
    public static final String PROJ4_AEQD = "+proj=aeqd +lat_0=%1$s +lon_0=%2$s +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs";
    public static final String PROJ4_AEQD1 = "+proj=aeqd +lat_0=%1$s +lon_0=%2$s +x_0=0 +y_0=0 +a=6371000 +b=6371000 +units=m +no_defs";
    public static final String PROJ4_MERC = "+proj=merc +lat_ts=0 +lat_0=%1$s +lon_0=%2$s +x_0=0 +y_0=0 +a=6371007 +b=6371007 +units=m +no_defs";   // TODO: verify

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_WORLDMAP    // TODO: preserve other map settings related keys?
    };

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo() {
            public String[] allKeys() {
                return ALL_KEYS;
            }
            public String[] intKeys() {
                return new String[0];
            }
            public String[] longKeys() {
                return new String[0];
            }
            public String[] floatKeys() {
                return new String[0];
            }
            public String[] boolKeys() {
                return new String[0];
            }
        };
    }

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            for (String key : ALL_KEYS) {                // all others are type String
                for (String tag : MAPTAGS) {
                    if (!types.containsKey(key + tag)) {
                        types.put(key + tag, String.class);
                    }
                }
            }
        }
        return types;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * WorldMapWidgetMode
     */
    public static enum WorldMapWidgetMode implements WidgetSettings.WidgetModeDisplay
    {
        EQUIRECTANGULAR_SIMPLE("Simple", MAPTAG_3x2, R.layout.layout_widget_sunpos_3x2_0, false, 0, 0, "Equidistant Rectangular", PROJ4_EQD),
        EQUIRECTANGULAR_BLUEMARBLE("Blue Marble", MAPTAG_3x2, R.layout.layout_widget_sunpos_3x2_01, false, 0, 0, "Equidistant Rectangular", PROJ4_EQD),
        EQUIAZIMUTHAL_SIMPLE("Polar [north]", MAPTAG_3x3, R.layout.layout_widget_sunpos_3x3_0, false, 90, 0, "Equidistant Azimuthal", PROJ4_AEQD),
        EQUIAZIMUTHAL_SIMPLE1("Polar [south]", MAPTAG_3x3, R.layout.layout_widget_sunpos_3x3_1, false, -90, 0, "Equidistant Azimuthal", PROJ4_AEQD),
        EQUIAZIMUTHAL_SIMPLE2("Equidistant Azimuthal", MAPTAG_3x3, R.layout.layout_widget_sunpos_3x3_2, true, 33.45, -111.94, "Equidistant Azimuthal", PROJ4_AEQD1),
        MERCATOR_SIMPLE("Mercator", MAPTAG_3x3, R.layout.layout_widget_sunpos_3x3_2, false, 0, 0, "Mercator", PROJ4_MERC),
        ;

        private final int layoutID;
        private String displayString;
        private String tag;
        private boolean supportsCenter;
        private double[] center;
        private String projectionTitle;
        private String proj4String;

        private WorldMapWidgetMode(String displayString, String tag, int layoutID, boolean supportsCenter, double centerLat, double centerLon, String projectionTitle, String proj4String)
        {
            this.displayString = displayString;
            this.projectionTitle = projectionTitle;
            this.proj4String = proj4String;
            this.layoutID = layoutID;
            this.tag = tag;
            this.supportsCenter = supportsCenter;
            this.center = new double[] {centerLat, centerLon};
        }

        public String toString()
        {
            return displayString;
        }

        @Override
        public int getLayoutID() {
            return layoutID;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        public String getProjectionTitle() {
            return projectionTitle;
        }
        public void setProjectionTitle(String value) {
            projectionTitle = value;
        }

        public String getProj4() {
            return String.format(proj4String, center);
        }
        public String getProj4(double[] center) {
            return String.format(proj4String, center[0], center[1]);
        }

        public double[] getProjectionCenter() {
            return center;
        }

        public String getMapTag() {
            return tag + ":" + name();
        }

        public boolean supportsCenter() {
            return supportsCenter;
        }

        public static void initDisplayStrings( Context context )
        {
            MERCATOR_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simplemercator));
            EQUIAZIMUTHAL_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal));
            EQUIAZIMUTHAL_SIMPLE1.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal_south));
            EQUIAZIMUTHAL_SIMPLE2.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simpleazimuthal_location));
            EQUIRECTANGULAR_SIMPLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_simplerectangular));
            EQUIRECTANGULAR_BLUEMARBLE.setDisplayString(context.getString(R.string.widgetMode_sunPosMap_bluemarble));

            MERCATOR_SIMPLE.setProjectionTitle(context.getString(R.string.worldmap_projection_mercator));
            EQUIAZIMUTHAL_SIMPLE.setProjectionTitle(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIAZIMUTHAL_SIMPLE1.setProjectionTitle(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIAZIMUTHAL_SIMPLE2.setProjectionTitle(context.getString(R.string.worldmap_projection_equiazimuthal));
            EQUIRECTANGULAR_SIMPLE.setProjectionTitle(context.getString(R.string.worldmap_projection_equirectangular));
            EQUIRECTANGULAR_BLUEMARBLE.setProjectionTitle(context.getString(R.string.worldmap_projection_equirectangular));
        }

        public static WorldMapWidgetMode findMode(int layoutID)
        {
            for (WorldMapWidgetMode mode : values()) {
                if (mode.layoutID == layoutID) {
                    return mode;
                }
            }
            return null;
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetSettings.WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
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
        if (mapTag.startsWith(MAPTAG_3x3)) {
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
    public static boolean loadWorldMapPref(Context context, int appWidgetId, String key, String mapTag) {
        return loadWorldMapPref(context, appWidgetId, key, mapTag, null);
    }
    public static boolean loadWorldMapPref(Context context, int appWidgetId, String key, String mapTag, Boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_WORLDMAP;
        return prefs.getBoolean(prefs_prefix + key + mapTag, (defaultValue != null ? defaultValue : defaultWorldMapFlag(key)));
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

    public static double[] loadWorldMapCenter(Context context, int appWidgetId, String mapTag, double[] defCenter) {
        return new double[] {
                Double.parseDouble(loadWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LATITUDE, mapTag, defCenter[0]+"")),
                Double.parseDouble(loadWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LONGITUDE, mapTag, defCenter[1]+""))
        };
    }
    public static void saveWorldMapCenter(Context context, int appWidgetId, String mapTag, double[] value)
    {
        saveWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LATITUDE, mapTag, Double.toString(value[0]));
        saveWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_CENTER_LONGITUDE, mapTag, Double.toString(value[1]));
    }
    public static void deleteWorldMapCenter(Context context, int appWidgetId, String mapTag)
    {
        WorldMapWidgetSettings.deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_CENTER_LABEL, mapTag);
        WorldMapWidgetSettings.deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_CENTER_LATITUDE, mapTag);
        WorldMapWidgetSettings.deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_CENTER_LONGITUDE, mapTag);
    }
    public static String getCenterTag(@Nullable double[] center) {
        return (center == null ? "0:0" : (int)center[0] + ":" + (int)center[1]);
    }

    public static String loadWorldMapBackground(Context context, int appWidgetId, String mapTag, @Nullable double[] center) {
        return loadWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_BACKGROUND, mapTag + ":" + getCenterTag(center));
    }
    public static void saveWorldMapBackground(Context context, int appWidgetId, String mapTag, @Nullable double[] center, String backgroundUri) {
        saveWorldMapString(context, appWidgetId, PREF_KEY_WORLDMAP_BACKGROUND, mapTag + ":" + getCenterTag(center), backgroundUri);
    }
    public static void deleteWorldMapBackground(Context context, int appWidgetId, String mapTag, @Nullable double[] center) {
        deleteWorldMapPref(context, appWidgetId, PREF_KEY_WORLDMAP_BACKGROUND, mapTag + ":" + getCenterTag(center));
    }
    public static void initWorldMapBackgroundDefaults(Context context)
    {
        if (null == loadWorldMapBackground(context, 0, WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE2.getMapTag(), PREF_DEF_WORLDMAP_CENTER)) {
            saveWorldMapBackground(context, 0, WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE2.getMapTag(), PREF_DEF_WORLDMAP_CENTER, getDrawableUri(context, R.drawable.worldmap4).toString());
        }
    }

    private static Uri getDrawableUri(Context context, int resId)
    {
        Resources resources = context.getResources();
        return (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resId))
                .appendPath(resources.getResourceTypeName(resId))
                .appendPath(resources.getResourceEntryName(resId))
                .build();
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
        initWorldMapBackgroundDefaults(context);
    }

    /**
     * @param context
     * @param appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        for (String tag : MAPTAGS)
        {
            for (String[] flags : PREF_DEF_WORLDMAP) {
                deleteWorldMapPref(context, appWidgetId, flags[0], tag);
            }
            deleteSunPosMapModePref(context, appWidgetId, tag);
            deleteWorldMapCenter(context, appWidgetId, tag);
            // deleteWorldMapBackground(context, appWidgetId, tag, center);    // TODO
            deleteWorldMapPref(context, appWidgetId, PREF_KEY_WORLDMAP_TIMEZONE, tag);
        }
    }

}
