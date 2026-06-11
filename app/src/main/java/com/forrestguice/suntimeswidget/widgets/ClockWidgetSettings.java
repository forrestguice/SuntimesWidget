/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimeswidget.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.Color;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_1;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_2;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_3;
import com.forrestguice.suntimeswidget.widgets.layouts.SuntimesLayout;

import java.util.Map;
import java.util.TreeMap;

import static com.forrestguice.suntimeswidget.settings.WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;

public class ClockWidgetSettings
{
    public static final String PREF_KEY_APPEARANCE_TYPEFACE = "typeface";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_BOLD = "bold";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_ITALIC = "italic";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE = "outline";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_COLOR = "color";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_GLOW = "glow";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_GLOW_COLOR = "glow_color";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT = "cutout";
    public static final String PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT_RADIUS = "cutout_radius";

    public static final String MODE_1x1 = "1x1";
    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK = "widgetmode_clock";
    public static final WidgetModeClock1x1 PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1 = WidgetModeClock1x1.CLOCK2;

    public static final String PREF_DEF_APPEARANCE_TYPEFACE = "serif-monospace";
    public static final boolean PREF_DEF_APPEARANCE_BOLD = true;
    public static final boolean PREF_DEF_APPEARANCE_ITALIC = false;
    public static final boolean PREF_DEF_APPEARANCE_OUTLINE = false;
    public static final int PREF_DEF_APPEARANCE_COLOR = Color.WHITE;

    public static final boolean PREF_DEF_APPEARANCE_GLOW = false;
    public static final int PREF_DEF_APPEARANCE_GLOW_COLOR = Color.WHITE;

    public static final boolean PREF_DEF_APPEARANCE_CUTOUT = true;
    public static final float PREF_DEF_APPEARANCE_CUTOUT_RADIUS = 4;  // 8dp

    public static final String[] FONT_FAMILIES = new String[] {
            "casual", "cursive", "monospace",
            "sans-serif", "sans-serif-black",
            "sans-serif-condensed", "sans-serif-condensed-light", "sans-serif-condensed-medium",
            "sans-serif-light", "sans-serif-medium", "sans-serif-smallcaps", "sans-serif-thin",
            "serif", "serif-monospace",
            "source-sans-pro", "source-sans-pro-semi-bold",
            "roboto-flex"
    };  // https://android.googlesource.com/platform/frameworks/base/+/master/data/fonts/fonts.xml

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + MODE_1x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_BOLD,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_ITALIC,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_COLOR,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_GLOW,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_GLOW_COLOR,
    };
    public static final String[] BOOL_KEYS = new String[] {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_BOLD,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_ITALIC,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_CUTOUT,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_GLOW,
    };
    public static final String[] INT_KEYS = new String[] {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_COLOR,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TYPEFACE + "_" + PREF_KEY_APPEARANCE_TYPEFACE_GLOW_COLOR,
    };

    private static Map<String,Class<?>> types = null;
    public static Map<String,Class<?>> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            WidgetSettings.putType(types, Integer.class, INT_KEYS);
            WidgetSettings.putType(types, Boolean.class, BOOL_KEYS);

            for (String key : ALL_KEYS) {
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static void saveClockTypefacePref(Context context, int appWidgetId, String value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE, value);
        prefs.apply();
    }
    public static void deleteClockTypefacePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE);
        prefs.apply();
    }
    public static String loadClockTypefacePref(Context context, int appWidgetId, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String s = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE, defaultValue);
        return (s != null ? s : defaultValue);
    }

    public static void saveClockTypefaceFlag(Context context, int appWidgetId, String key, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE + "_" + key, value);
        prefs.apply();
    }
    public static boolean loadClockTypefaceFlag(Context context, int appWidgetId, String key, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE + "_" + key, defaultValue);
    }

    public static void saveClockTypefaceValue(Context context, int appWidgetId, String key, int value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putInt(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE + "_" + key, value);
        prefs.apply();
    }
    public static void deleteClockTypefaceValue(Context context, int appWidgetId, String key)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE + "_" + key);
        prefs.apply();
    }
    public static int loadClockTypefaceValue(Context context, int appWidgetId, String key, int defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getInt(prefs_prefix + PREF_KEY_APPEARANCE_TYPEFACE + "_" + key, defaultValue);
    }

    //////////////////////////////////////////////////

    public static void saveClockModePref(Context context, int appWidgetId, String value, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix, value);
        prefs.apply();
    }
    public static void deleteClockModePref(Context context, int appWidgetId, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix);
        prefs.apply();
    }
    public static String loadClockModePref(Context context, int appWidgetId, String suffix, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String s = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix, defaultValue);
        return (s != null ? s : defaultValue);
    }
    public static String getClockModeDefault(Context context, int appWidgetId) {
        return (context != null ? context.getString(R.string.def_appwidget_0_appearance_widgetmode_clock) : PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1.name());
    }

    //////////////////////////////////////////////////

    public static WidgetModeClock1x1 loadClock1x1ModePref(Context context, int appWidgetId)
    {
        String modeString = loadClockModePref(context, appWidgetId, MODE_1x1, getClockModeDefault(context, appWidgetId));
        WidgetModeClock1x1 widgetMode;
        try {
            widgetMode = WidgetModeClock1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1;
            Log.w("loadClock1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static ClockLayout loadClock1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        ClockLayout layout;
        WidgetModeClock1x1 mode = loadClock1x1ModePref(context, appWidgetId);
        switch (mode)
        {
            case CLOCK3: layout = new ClockLayout_1x1_3(); break;
            case CLOCK2: layout = new ClockLayout_1x1_2(); break;
            case CLOCK1: layout = new ClockLayout_1x1_1(); break;
            case CLOCK0:
            default: layout = new ClockLayout_1x1_0(); break;
        }
        return layout;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * WidgetModeClock1x1
     */
    public static enum WidgetModeClock1x1 implements WidgetSettings.WidgetModeDisplay
    {
        CLOCK0("Clock 0", R.layout.layout_widget_clock_1x1_0, new ClockLayout_1x1_0()),
        CLOCK1("Clock 1", R.layout.layout_widget_clock_1x1_1, new ClockLayout_1x1_1()),
        CLOCK2("Clock 2", R.layout.layout_widget_clock_1x1_2, new ClockLayout_1x1_2()),
        CLOCK3("Clock 3", R.layout.layout_widget_clock_1x1_3, new ClockLayout_1x1_3()),
        ;

        private final SuntimesLayout layout;
        private final int layoutID;
        private String displayString;

        private WidgetModeClock1x1(@NonNull String displayString, int layoutID, ClockLayout layout)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
            this.layout = layout;
        }

        @Override
        public String getWidgetSize() {
            return WidgetSettings.SIZE_3x1;    // prefer 3x1 mode for preview
        }

        @Override
        public Class<?> getWidgetClass() {
            return ClockWidget0_3x1.class;    // prefer 3x1 mode for preview
        }

        @Override
        public SuntimesLayout getWidgetLayout() {
            return layout;
        }

        public int getLayoutID() {
            return layoutID;
        }

        @NonNull
        public String toString() {
            return displayString;
        }

        @NonNull
        public String getDisplayString() {
            return displayString;
        }

        public void setDisplayString( @NonNull String displayString ) {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context ) {
            //CLOCK0.setDisplayString(context.getString(R.string.configAction_clock));
            //CLOCK1.setDisplayString(context.getString(R.string.configAction_clock));
        }

        @Nullable
        public static ClockWidgetSettings.WidgetModeClock1x1 findMode(int layoutID)
        {
            for (WidgetModeClock1x1 mode : values()) {
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

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * @param context context
     */
    public static void initDisplayStrings( Context context ) {
        WidgetModeClock1x1.initDisplayStrings(context);
    }

    /**
     * @param context context
     * @param appWidgetId appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteClockModePref(context, appWidgetId, MODE_1x1);
        deleteClockTypefacePref(context, appWidgetId);
        deleteClockTypefaceValue(context, appWidgetId, PREF_KEY_APPEARANCE_TYPEFACE_BOLD);
        deleteClockTypefaceValue(context, appWidgetId, PREF_KEY_APPEARANCE_TYPEFACE_ITALIC);
        deleteClockTypefaceValue(context, appWidgetId, PREF_KEY_APPEARANCE_TYPEFACE_OUTLINE);
        deleteClockTypefaceValue(context, appWidgetId, PREF_KEY_APPEARANCE_TYPEFACE_GLOW);
        deleteClockTypefaceValue(context, appWidgetId, PREF_KEY_APPEARANCE_TYPEFACE_COLOR);
    }

}
