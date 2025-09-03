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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.CompareMode;
import com.forrestguice.suntimeswidget.calculator.settings.DateInfo;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.suntimeswidget.calculator.settings.EventAliasTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.calculator.settings.MoonPhaseMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetDataMode;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetOrder;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.LengthUnitDisplay;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_1;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_2;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_3;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_4;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_5;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_6;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_7;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_8;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_9;
import com.forrestguice.suntimeswidget.widgets.layouts.SunLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.SunLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SunLayout_1x1_1;
import com.forrestguice.suntimeswidget.widgets.layouts.SunLayout_1x1_2;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_1X1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_1X1_1;

import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X1_1;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X1_2;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X2_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X2_1;
import com.forrestguice.suntimeswidget.themes.defaults.DarkTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X2_2;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.prefs.PrefTypeInfo;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shared preferences used by individual widgets; uses getSharedPreferences (stored in com.forrestguice.suntimeswidget.xml).
 * Each pref takes an appWidgetId; the app uses these prefs by supplying 0 (AppWidgetManager.INVALID_APPWIDGET_ID).
 */
public class WidgetSettings
{
    public static final String PREFS_WIDGET = "com.forrestguice.suntimeswidget";

    public static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_PREFIX_KEY_APPEARANCE = "_appearance_";
    public static final String PREF_PREFIX_KEY_GENERAL = "_general_";
    public static final String PREF_PREFIX_KEY_LOCATION = "_location_";
    public static final String PREF_PREFIX_KEY_TIMEZONE = "_timezone_";
    public static final String PREF_PREFIX_KEY_DATE = "_date_";
    public static final String PREF_PREFIX_KEY_ACTION = "_action_";

    public static final String PREF_KEY_GENERAL_CALCULATOR = "calculator";
    public static final String PREF_DEF_GENERAL_CALCULATOR = "time4a-time4j";
    public static final String PREF_DEF_GENERAL_CALCULATOR_MOON = "time4a-time4j";
    public static final String[][] PREF_DEF_GENERAL_CALCULATORS = new String[][] { new String[] {"",     PREF_DEF_GENERAL_CALCULATOR},
                                                                                   new String[] {"moon", PREF_DEF_GENERAL_CALCULATOR_MOON} };

    public static final String PREF_KEY_APPEARANCE_THEME = "theme";
    public static final String PREF_DEF_APPEARANCE_THEME = DarkTheme.THEMEDEF_NAME;

    public static final String PREF_KEY_APPEARANCE_SHOWTITLE = "showtitle";
    public static final boolean PREF_DEF_APPEARANCE_SHOWTITLE = false;

    public static final String PREF_KEY_APPEARANCE_TITLETEXT = "titletext";
    public static final String PREF_DEF_APPEARANCE_TITLETEXT = "";

    public static final String PREF_KEY_APPEARANCE_SHOWLABELS = "showlabels";
    public static final boolean PREF_DEF_APPEARANCE_SHOWLABELS = true;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_SUN1x1 = "widgetmode_1x1";
    public static final WidgetModeSun1x1 PREF_DEF_APPEARANCE_WIDGETMODE_SUN1x1 = WidgetModeSun1x1.WIDGETMODE1x1_BOTH_1;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS1x1 = "widgetmode_sunpos1x1";
    public static final WidgetModeSunPos1x1 PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS1x1 = WidgetModeSunPos1x1.MODE1x1_ALTAZ;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1 = "widgetmode_sunpos3x1";
    public static final WidgetModeSunPos3x1 PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x1 = WidgetModeSunPos3x1.MODE3x1_LIGHTMAP;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2 = "widgetmode_sunpos3x2";
    public static final WidgetModeSunPos3x2 PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x2 = WidgetModeSunPos3x2.MODE3x2_WORLDMAP;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1 = "widgetmode_moon1x1";
    public static final WidgetModeMoon1x1 PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1 = WidgetModeMoon1x1.MODE1x1_RISESET;

    public static final String PREF_KEY_APPEARANCE_ALLOWRESIZE = "allowresize";
    public static final boolean PREF_DEF_APPEARANCE_ALLOWRESIZE = false;

    public static final String PREF_KEY_APPEARANCE_SCALETEXT = "scaletext";
    public static final boolean PREF_DEF_APPEARANCE_SCALETEXT = false;

    public static final String PREF_KEY_APPEARANCE_SCALEBASE = "scalebase";
    public static final boolean PREF_DEF_APPEARANCE_SCALEBASE =  false;

    public static final String PREF_KEY_APPEARANCE_GRAVITY = "gravity";
    public static final WidgetGravity PREF_DEF_APPEARANCE_GRAVITY = WidgetGravity.CENTER;

    public static final String PREF_KEY_APPEARANCE_TIMEFORMATMODE = "timeformatmode";
    public static final TimeFormatMode PREF_DEF_APPEARANCE_TIMEFORMATMODE = TimeFormatMode.MODE_SYSTEM;

    public static final String PREF_KEY_GENERAL_RISESETORDER = "risesetorder";
    public static final RiseSetOrder PREF_DEF_GENERAL_RISESETORDER = RiseSetOrder.TODAY;

    public static final String PREF_KEY_GENERAL_TIMEMODE = "timemode";
    public static final TimeMode PREF_DEF_GENERAL_TIMEMODE = TimeMode.OFFICIAL;

    public static final String PREF_KEY_GENERAL_TIMEMODE2 = "timemode2";
    public static final SolsticeEquinoxMode PREF_DEF_GENERAL_TIMEMODE2 = SolsticeEquinoxMode.EQUINOX_SPRING;

    public static final String PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE = "timemode2override";
    public static final boolean PREF_DEF_GENERAL_TIMEMODE2_OVERRIDE = true;

    public static final String PREF_KEY_GENERAL_TIMEMODE3 = "timemode3";
    public static final MoonPhaseMode PREF_DEF_GENERAL_TIMEMODE3 = MoonPhaseMode.FULL_MOON;

    public static final String PREF_KEY_GENERAL_TIMENOTE_RISE = "timenoterise";
    public static final SolarEvents PREF_DEF_GENERAL_TIMENOTE_RISE = SolarEvents.SUNRISE;

    public static final String PREF_KEY_GENERAL_TIMENOTE_SET = "timenoteset";
    public static final SolarEvents PREF_DEF_GENERAL_TIMENOTE_SET = SolarEvents.SUNSET;

    public static final String PREF_KEY_GENERAL_TRACKINGMODE = "trackingmode";
    public static final TrackingMode PREF_DEF_GENERAL_TRACKINGMODE = TrackingMode.SOONEST;

    public static final int TRACKINGLEVEL_MIN = 0;
    public static final int TRACKINGLEVEL_MAX = 10;
    public static final String PREF_KEY_GENERAL_TRACKINGLEVEL = "trackinglevel";
    public static final int PREF_DEF_GENERAL_TRACKINGLEVEL = TRACKINGLEVEL_MAX;

    public static final String PREF_KEY_GENERAL_COMPAREMODE = "comparemode";
    public static final CompareMode PREF_DEF_GENERAL_COMPAREMODE = CompareMode.TOMORROW;

    public static final String PREF_KEY_GENERAL_SHOWCOMPARE = "showcompare";
    public static final boolean PREF_DEF_GENERAL_SHOWCOMPARE = true;

    public static final String PREF_KEY_GENERAL_SHOWNOON = "shownoon";
    public static final boolean PREF_DEF_GENERAL_SHOWNOON = false;

    public static final String PREF_KEY_GENERAL_SHOWWEEKS = "showweeks";
    public static final boolean PREF_DEF_GENERAL_SHOWWEEKS = false;

    public static final String PREF_KEY_GENERAL_SHOWHOURS = "showhours";
    public static final boolean PREF_DEF_GENERAL_SHOWHOURS = true;

    public static final String PREF_KEY_GENERAL_SHOWSECONDS = "showseconds";
    public static final boolean PREF_DEF_GENERAL_SHOWSECONDS = false;

    public static final String PREF_KEY_GENERAL_SHOWTIMEDATE = "showtimedate";
    public static final boolean PREF_DEF_GENERAL_SHOWTIMEDATE = true;

    public static final String PREF_KEY_GENERAL_SHOWABBRMONTH = "showabbrmonth";
    public static final boolean PREF_DEF_GENERAL_SHOWABBRMONTH = true;

    public static final String PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE = "localize_hemisphere";
    public static final boolean PREF_DEF_GENERAL_LOCALIZE_HEMISPHERE = true;

    public static final String PREF_KEY_GENERAL_OBSERVERHEIGHT = "observerheight";
    public static final float PREF_DEF_GENERAL_OBSERVERHEIGHT = 1.8288f; // meters (6ft)

    public static final String PREF_KEY_GENERAL_UNITS_LENGTH = "lengthunits";
    public static LengthUnit PREF_DEF_GENERAL_UNITS_LENGTH = LengthUnit.METRIC;  // reassigned later by initDefaults

    public static final String PREF_KEY_ACTION_MODE = "action";
    public static final ActionMode PREF_DEF_ACTION_MODE = ActionMode.ONTAP_LAUNCH_CONFIG;

    public static final String PREF_KEY_LOCATION_MODE = "locationMode";
    public static final LocationMode PREF_DEF_LOCATION_MODE = LocationMode.CUSTOM_LOCATION;

    public static final String PREF_KEY_LOCATION_LONGITUDE = "longitude";
    public static String PREF_DEF_LOCATION_LONGITUDE = "-111.4557";      // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_LATITUDE = "latitude";
    public static String PREF_DEF_LOCATION_LATITUDE = "33.4557";         // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_ALTITUDE = "altitude";
    public static String PREF_DEF_LOCATION_ALTITUDE = "0";               // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_ALTITUDE_ENABLED = "altitude_enabled";
    public static final boolean PREF_DEF_LOCATION_ALTITUDE_ENABLED = true;

    public static final String PREF_KEY_LOCATION_LABEL = "label";
    public static String PREF_DEF_LOCATION_LABEL = "Phoenix";       // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_FROMAPP = "fromapp";
    public static final boolean PREF_DEF_LOCATION_FROMAPP = false;

    public static final String PREF_KEY_TIMEZONE_MODE = "timezoneMode";
    public static final TimezoneMode PREF_DEF_TIMEZONE_MODE = TimezoneMode.CURRENT_TIMEZONE;

    public static final String PREF_KEY_TIMEZONE_FROMAPP = "fromapp";
    public static final boolean PREF_DEF_TIMEZONE_FROMAPP = false;

    public static final String PREF_KEY_TIMEZONE_CUSTOM = "timezone";
    public static String PREF_DEF_TIMEZONE_CUSTOM = "MST";    // reassigned later by initDefaults
    public static final String[][] PREF_DEF_TIMEZONES = new String[][] { };  // e.g. new String[] {"", PREF_DEF_TIMEZONE_CUSTOM} };

    public static final String PREF_KEY_TIMEZONE_SOLARMODE = "solarmode";
    public static final SolarTimeMode PREF_DEF_TIMEZONE_SOLARMODE = SolarTimeMode.LOCAL_MEAN_TIME;

    public static final String PREF_KEY_DATE_MODE = "dateMode";
    public static final DateMode PREF_DEF_DATE_MODE = DateMode.CURRENT_DATE;

    public static final String PREF_KEY_DATE_YEAR = "dateYear";
    public static final int PREF_DEF_DATE_YEAR = -1;

    public static final String PREF_KEY_DATE_MONTH = "dateMonth";
    public static final int PREF_DEF_DATE_MONTH = -1;

    public static final String PREF_KEY_DATE_DAY = "dateDay";
    public static final int PREF_DEF_DATE_DAY = -1;

    public static final String PREF_KEY_DATE_OFFSET = "dateOffset";    // offset in days
    public static final int PREF_DEF_DATE_OFFSET = 0;

    public static final String PREF_KEY_NEXTUPDATE = "nextUpdate";
    public static final long PREF_DEF_NEXTUPDATE = -1L;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static String[] ALL_KEYS = new String[]
    {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_THEME,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SHOWTITLE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TITLETEXT,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SHOWLABELS,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_SUN1x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS1x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_ALLOWRESIZE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SCALETEXT,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SCALEBASE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_GRAVITY,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_TIMEFORMATMODE,

            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_CALCULATOR,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_RISESETORDER,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMEMODE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMEMODE2,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMEMODE3,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMENOTE_RISE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMENOTE_SET,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TRACKINGMODE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TRACKINGLEVEL,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_COMPAREMODE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWCOMPARE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWNOON,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWWEEKS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWHOURS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWSECONDS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWTIMEDATE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWABBRMONTH,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_OBSERVERHEIGHT,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_UNITS_LENGTH,

            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_MODE,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_LONGITUDE,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_LATITUDE,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_ALTITUDE,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_ALTITUDE_ENABLED,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_LABEL,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_FROMAPP,
            PREF_PREFIX_KEY_TIMEZONE + PREF_KEY_TIMEZONE_MODE,
            PREF_PREFIX_KEY_TIMEZONE + PREF_KEY_TIMEZONE_FROMAPP,
            PREF_PREFIX_KEY_TIMEZONE + PREF_KEY_TIMEZONE_CUSTOM,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_TIMEZONE_SOLARMODE,    // in _general

            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_MODE,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_YEAR,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_MONTH,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_DAY,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_OFFSET,
            PREF_PREFIX_KEY_DATE + PREF_KEY_NEXTUPDATE,

            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_MODE,

            PREF_KEY_NEXTUPDATE
    };
    public static String[] BOOL_KEYS = new String[]
    {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SHOWTITLE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SHOWLABELS,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_ALLOWRESIZE,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SCALETEXT,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_SCALEBASE,

            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWCOMPARE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWNOON,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWWEEKS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWHOURS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWSECONDS,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWTIMEDATE,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_SHOWABBRMONTH,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE,

            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_ALTITUDE_ENABLED,
            PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_FROMAPP,
            PREF_PREFIX_KEY_TIMEZONE + PREF_KEY_TIMEZONE_FROMAPP
    };
    public static String[] FLOAT_KEYS = new String[] { PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_OBSERVERHEIGHT };
    public static String[] LONG_KEYS = new String[] { PREF_KEY_NEXTUPDATE };
    public static String[] INT_KEYS = new String[]
    {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_GRAVITY,   // enum as ordinal
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_YEAR,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_MONTH,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_DAY,
            PREF_PREFIX_KEY_DATE + PREF_KEY_DATE_OFFSET,
            PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_TRACKINGLEVEL,
    };

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo() {
            public String[] allKeys() {
                return ALL_KEYS;
            }
            public String[] intKeys() {
                return INT_KEYS;
            }
            public String[] longKeys() {
                return LONG_KEYS;
            }
            public String[] floatKeys() {
                return FLOAT_KEYS;
            }
            public String[] boolKeys() {
                return BOOL_KEYS;
            }
        };
    }

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            putType(types, Long.class, LONG_KEYS);
            putType(types, Float.class, FLOAT_KEYS);
            putType(types, Integer.class, INT_KEYS);
            putType(types, Boolean.class, BOOL_KEYS);

            types.put(PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_LATITUDE, String.class);    // double as String
            types.put(PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_LONGITUDE, String.class);   // double as String
            types.put(PREF_PREFIX_KEY_LOCATION + PREF_KEY_LOCATION_ALTITUDE, String.class);    // double as String

            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }
    public static void putType(Map<String,Class> map, Class type, String... keys) {
        for (String key : keys) {
            map.put(key, type);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * WidgetOnTap
     */
    public static enum ActionMode
    {
        ONTAP_DONOTHING("Ignore"),
        ONTAP_UPDATE("Update Widget"),
        ONTAP_LAUNCH_CONFIG("Reconfigure Widget"),
        ONTAP_LAUNCH_ACTIVITY("Launch Activity"),
        ONTAP_UPDATE_ALL("Update All Widgets"),
        ONTAP_FLIPTO_NEXTITEM("Flip Views");

        private String displayString;

        private ActionMode(String displayString)
        {
            this.displayString = displayString;
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

        public int ordinal( ActionMode[] array )
        {
            for (int i=0; i<array.length; i++)
            {
                if (array[i].name().equals(this.name()))
                {
                    return i;
                }
            }
            return -1;
        }
    }
    public static void initDisplayStrings_ActionMode( Context context )
    {
        ActionMode.ONTAP_DONOTHING.setDisplayString(context.getString(R.string.actionMode_doNothing));
        ActionMode.ONTAP_UPDATE.setDisplayString(context.getString(R.string.actionMode_update));
        ActionMode.ONTAP_UPDATE_ALL.setDisplayString(context.getString(R.string.actionMode_update_all));
        ActionMode.ONTAP_LAUNCH_CONFIG.setDisplayString(context.getString(R.string.actionMode_config));
        ActionMode.ONTAP_LAUNCH_ACTIVITY.setDisplayString(context.getString(R.string.actionMode_launchActivity));
        ActionMode.ONTAP_FLIPTO_NEXTITEM.setDisplayString(context.getString(R.string.actionMode_flipToNextItem));
    }

    public interface WidgetModeDisplay
    {
        int getLayoutID();
        String getDisplayString();
        String name();
    }

    /**
     * WidgetModeSun1x1
     */
    public static enum WidgetModeSun1x1 implements WidgetModeDisplay
    {
        WIDGETMODE1x1_SUNRISE("Sunrise only", R.layout.layout_widget_1x1_1),
        WIDGETMODE1x1_SUNSET("Sunset only", R.layout.layout_widget_1x1_2),
        WIDGETMODE1x1_BOTH_1("Sunrise & Sunset (1)", R.layout.layout_widget_1x1_0),
        WIDGETMODE1x1_BOTH_2("Sunrise & Sunset (2)", R.layout.layout_widget_1x1_3);

        private final int layoutID;
        private String displayString;

        private WidgetModeSun1x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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
            WIDGETMODE1x1_SUNRISE.setDisplayString(context.getString(R.string.widgetMode1x1_sunrise));
            WIDGETMODE1x1_SUNSET.setDisplayString(context.getString(R.string.widgetMode1x1_sunset));
            WIDGETMODE1x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_both_1));
            WIDGETMODE1x1_BOTH_2.setDisplayString(context.getString(R.string.widgetMode1x1_both_2));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeSun2x1
     */
    public static enum WidgetModeSun2x1 implements WidgetModeDisplay
    {
        WIDGETMODE2x1_BOTH_1("Sunrise & Sunset", R.layout.layout_widget_2x1_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeSun2x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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

        public static void initDisplayStrings( Context context ) {
            WIDGETMODE2x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_both_1));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeSun3x1
     */
    public static enum WidgetModeSun3x1 implements WidgetModeDisplay
    {
        WIDGETMODE3x1_BOTH_1("Sunrise, Noon, and Sunset", R.layout.layout_widget_3x1_0);

        private WidgetModeSun3x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        private final int layoutID;
        public int getLayoutID() {
            return layoutID;
        }

        private String displayString;
        public String getDisplayString() {
            return displayString;
        }
        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }
        public static void initDisplayStrings( Context context ) {
            WIDGETMODE3x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode3x1_sunrise_sunset_noon));
        }
        public String toString() {
            return displayString;
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeSunPos1x1
     */
    public static enum WidgetModeSunPos1x1 implements WidgetModeDisplay
    {
        MODE1x1_ALTAZ("Altitude & Azimuth", R.layout.layout_widget_sunpos_1x1_5),
        MODE1x1_DECRIGHT("Declination & Right Ascension", R.layout.layout_widget_sunpos_1x1_6);

        private final int layoutID;
        private String displayString;

        private WidgetModeSunPos1x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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
            MODE1x1_ALTAZ.setDisplayString(context.getString(R.string.widgetMode1x1_altaz));
            MODE1x1_DECRIGHT.setDisplayString(context.getString(R.string.widgetMode1x1_decright));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeSunPos3x1
     */
    public static enum WidgetModeSunPos3x1 implements WidgetModeDisplay
    {
        MODE3x1_LIGHTMAP("Lightmap", R.layout.layout_widget_sunpos_3x1_0),
        MODE3x1_LIGHTMAP_MEDIUM("Lightmap (medium)", R.layout.layout_widget_sunpos_3x1_0),
        MODE3x1_LIGHTMAP_SMALL("Lightmap (small)", R.layout.layout_widget_sunpos_3x1_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeSunPos3x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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
            MODE3x1_LIGHTMAP.setDisplayString(context.getString(R.string.widgetMode3x1_lightmap_large));
            MODE3x1_LIGHTMAP_MEDIUM.setDisplayString(context.getString(R.string.widgetMode3x1_lightmap_medium));
            MODE3x1_LIGHTMAP_SMALL.setDisplayString(context.getString(R.string.widgetMode3x1_lightmap_small));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeSunPos3x2
     */
    public static enum WidgetModeSunPos3x2 implements WidgetModeDisplay
    {
        MODE3x2_LIGHTGRAPH("Light Graph", R.layout.layout_widget_sunpos_3x2_2),
        MODE3x2_LINEGRAPH("Altitude Graph", R.layout.layout_widget_sunpos_3x2_1),
        MODE3x2_WORLDMAP("World Map", R.layout.layout_widget_sunpos_3x2_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeSunPos3x2(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID() {
            return layoutID;
        }

        public String toString() {
            return displayString;
        }

        public String getDisplayString() {
            return displayString;
        }

        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            MODE3x2_LIGHTGRAPH.setDisplayString(context.getString(R.string.widgetMode3x2_lightgraph));
            MODE3x2_LINEGRAPH.setDisplayString(context.getString(R.string.widgetMode3x2_linegraph));
            MODE3x2_WORLDMAP.setDisplayString(context.getString(R.string.widgetMode3x2_worldmap));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeMoon1x1
     */
    public static enum WidgetModeMoon1x1 implements WidgetModeDisplay
    {
        MODE1x1_RISESET("Moonrise & moonset", R.layout.layout_widget_moon_1x1_0),
        MODE1x1_PHASEILLUM("Moon phase & illumination", R.layout.layout_widget_moon_1x1_1),
        MODE1x1_PHASE("Moon phase only", R.layout.layout_widget_moon_1x1_2),
        MODE1x1_ILLUM("Moon illumination only", R.layout.layout_widget_moon_1x1_3),
        MODE1x1_PHASENEXT("Next major phase", R.layout.layout_widget_moon_1x1_4),
        MODE1x1_ALTAZ("Altitude & Azimuth", R.layout.layout_widget_moon_1x1_5),
        MODE1x1_DECRIGHT("Declination & Right Ascension", R.layout.layout_widget_moon_1x1_6),
        MODE1x1_DISTANCE("Current distance", R.layout.layout_widget_moon_1x1_7),
        MODE1x1_APSIS("Next apogee / perigee", R.layout.layout_widget_moon_1x1_8),
        MODE1x1_MOONDAY("Moon Day", R.layout.layout_widget_moon_1x1_9);

        private final int layoutID;
        private String displayString;

        private WidgetModeMoon1x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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
            MODE1x1_RISESET.setDisplayString(context.getString(R.string.widgetMode1x1_moonriseset));
            MODE1x1_PHASEILLUM.setDisplayString(context.getString(R.string.widgetMode1x1_moonphaseillum));
            MODE1x1_PHASE.setDisplayString(context.getString(R.string.widgetMode1x1_moonphase));
            MODE1x1_ILLUM.setDisplayString(context.getString(R.string.widgetMode1x1_moonillum));
            MODE1x1_PHASENEXT.setDisplayString(context.getString(R.string.widgetMode1x1_moonphasenext));
            MODE1x1_ALTAZ.setDisplayString(context.getString(R.string.widgetMode1x1_altaz));
            MODE1x1_DECRIGHT.setDisplayString(context.getString(R.string.widgetMode1x1_decright));
            MODE1x1_DISTANCE.setDisplayString(context.getString(R.string.widgetMode1x1_distance));
            MODE1x1_APSIS.setDisplayString(context.getString(R.string.widgetMode1x1_apsis));
            MODE1x1_MOONDAY.setDisplayString(context.getString(R.string.widgetMode1x1_moonday));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeMoon2x1
     */
    public static enum WidgetModeMoon2x1 implements WidgetModeDisplay
    {
        WIDGETMODE2x1_BOTH_1("Moonrise, moonset, phase & illumination", R.layout.layout_widget_moon_2x1_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeMoon2x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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

        public static void initDisplayStrings( Context context ) {
            //WIDGETMODE2x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_moonriseset));   // TODO
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetModeMoon3x1
     */
    public static enum WidgetModeMoon3x1 implements WidgetModeDisplay
    {
        WIDGETMODE3x1_BOTH_1("Major Phases", R.layout.layout_widget_moon_3x1_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeMoon3x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID()
        {
            return layoutID;
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

        public static void initDisplayStrings( Context context ) {
            //WIDGETMODE3x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_moonphase));   // TODO
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * WidgetGravity
     */
    public static enum WidgetGravity
    {
        //FILL("fill", 0),
        TOP_LEFT("top-left", 1),
        TOP("top", 2),
        TOP_RIGHT("top-right", 3),
        LEFT("left", 4),
        CENTER("center", 5),
        RIGHT("right", 6),
        BOTTOM_LEFT("bottom-left", 7),
        BOTTOM("bottom", 8),
        BOTTOM_RIGHT("bottom-right", 9);

        private String displayString;
        private int position;

        private WidgetGravity(String displayString, int position)
        {
            this.displayString = displayString;
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public String toString() {
            return displayString;
        }
        public String getDisplayString() {
            return displayString;
        }
        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }

        @Nullable
        public static WidgetGravity findPosition( int position )
        {
            WidgetGravity[] values = WidgetGravity.values();
            for (int i=0; i<values.length; i++) {
                if (values[i].getPosition() == position) {
                     return values[i];
                }
            }
            return null;
        }
    }
    public static void initDisplayStrings_WidgetGravity( Context context )
    {
        WidgetGravity[] values = WidgetGravity.values();
        String[] display = context.getResources().getStringArray(R.array.widgetgravity);
        for (int i=0; i<values.length; i++) {
            if (i < display.length) {
                values[i].setDisplayString(display[i]);
            } else break;
        }
    }

    public static void initDisplayStrings_DateMode( Context context )
    {
        DateMode.CURRENT_DATE.setDisplayString(context.getString(R.string.dateMode_current));
        DateMode.CUSTOM_DATE.setDisplayString(context.getString(R.string.dateMode_custom));
    }

    public static void initDisplayStrings_TimezoneMode( Context context )
    {
        TimezoneMode.SOLAR_TIME.setDisplayString(context.getString(R.string.timezoneMode_standard));
        TimezoneMode.CURRENT_TIMEZONE.setDisplayString(context.getString(R.string.timezoneMode_current));
        TimezoneMode.CUSTOM_TIMEZONE.setDisplayString(context.getString(R.string.timezoneMode_custom));
    }


    public static void initDisplayStrings_SolarTimeMode( Context context )
    {
        SolarTimeMode.LOCAL_MEAN_TIME.setDisplayString(context.getString(R.string.time_localMean));
        SolarTimeMode.APPARENT_SOLAR_TIME.setDisplayString(context.getString(R.string.time_apparent));
        SolarTimeMode.LMST.setDisplayString(context.getString(R.string.time_lmst));
        SolarTimeMode.GMST.setDisplayString(context.getString(R.string.time_gmst));
        SolarTimeMode.UTC.setDisplayString(context.getString(R.string.time_utc));
    }

    public static void initDisplayStrings_TimeFormatMode( Context context )
    {
        TimeFormatMode.MODE_SYSTEM.setDisplayString(context.getString(R.string.timeFormatMode_system));
        TimeFormatMode.MODE_12HR.setDisplayString(context.getString(R.string.timeFormatMode_12hr));
        TimeFormatMode.MODE_24HR.setDisplayString(context.getString(R.string.timeFormatMode_24hr));
        TimeFormatMode.MODE_SUNTIMES.setDisplayString(context.getString(R.string.timeFormatMode_suntimes));
    }

    public static void initDisplayStrings_LocationMode( Context context )
    {
        LocationMode.CURRENT_LOCATION.setDisplayString(context.getString(R.string.locationMode_current));
        LocationMode.CUSTOM_LOCATION.setDisplayString(context.getString(R.string.locationMode_custom));
    }

    public static void initDisplayStrings_CompareMode( Context context )
    {
        CompareMode.YESTERDAY.setDisplayString( context.getString(R.string.compareMode_yesterday) );
        CompareMode.TOMORROW.setDisplayString(context.getString(R.string.compareMode_tomorrow));
    }

    public static void initDisplayStrings_TrackingMode( Context context )
    {
        TrackingMode.RECENT.setDisplayString( context.getString(R.string.trackingMode_recent) );
        TrackingMode.CLOSEST.setDisplayString( context.getString(R.string.trackingMode_closest) );
        TrackingMode.SOONEST.setDisplayString( context.getString(R.string.trackingMode_soonest) );
    }

    public static void initDisplayStrings_SolsticeEquinoxMode( Context context )
    {
        SolsticeEquinoxMode.CROSS_SPRING.setDisplayStrings(context.getString(R.string.timeMode_cross_midwinter_short),
                context.getString(R.string.timeMode_cross_midwinter));
        SolsticeEquinoxMode.EQUINOX_SPRING.setDisplayStrings(context.getString(R.string.timeMode_equinox_vernal_short),
                context.getString(R.string.timeMode_equinox_vernal));

        SolsticeEquinoxMode.CROSS_SUMMER.setDisplayStrings( context.getString(R.string.timeMode_cross_midspring_short),
                context.getString(R.string.timeMode_cross_midspring));
        SolsticeEquinoxMode.SOLSTICE_SUMMER.setDisplayStrings( context.getString(R.string.timeMode_solstice_summer_short),
                context.getString(R.string.timeMode_solstice_summer));

        SolsticeEquinoxMode.CROSS_AUTUMN.setDisplayStrings( context.getString(R.string.timeMode_cross_midsummer_short),
                context.getString(R.string.timeMode_cross_midsummer) );
        SolsticeEquinoxMode.EQUINOX_AUTUMNAL.setDisplayStrings( context.getString(R.string.timeMode_equinox_autumnal_short),
                context.getString(R.string.timeMode_equinox_autumnal) );

        SolsticeEquinoxMode.CROSS_WINTER.setDisplayStrings(context.getString(R.string.timeMode_cross_midautumnal_short),
                context.getString(R.string.timeMode_cross_midautumnal));
        SolsticeEquinoxMode.SOLSTICE_WINTER.setDisplayStrings(context.getString(R.string.timeMode_solstice_winter_short),
                context.getString(R.string.timeMode_solstice_winter));
    }

    public static void initDisplayStrings_MoonPhaseMode( Context context )
    {
        MoonPhaseMode.NEW_MOON.setDisplayStrings(context.getString(R.string.timeMode_moon_new_short),
                context.getString(R.string.timeMode_moon_new));

        MoonPhaseMode.FIRST_QUARTER.setDisplayStrings( context.getString(R.string.timeMode_moon_firstquarter_short),
                context.getString(R.string.timeMode_moon_firstquarter));

        MoonPhaseMode.FULL_MOON.setDisplayStrings( context.getString(R.string.timeMode_moon_full_short),
                context.getString(R.string.timeMode_moon_full) );

        MoonPhaseMode.THIRD_QUARTER.setDisplayStrings(context.getString(R.string.timeMode_moon_thirdquarter_short),
                context.getString(R.string.timeMode_moon_thirdquarter));
    }

    public static void initDisplayStrings_TimeMode( Context context )
    {
        TimeMode.OFFICIAL.setDisplayStrings( context.getString(R.string.timeMode_official_short),
                context.getString(R.string.timeMode_official) );

        TimeMode.NAUTICAL.setDisplayStrings( context.getString(R.string.timeMode_nautical_short),
                context.getString(R.string.timeMode_nautical));

        TimeMode.CIVIL.setDisplayStrings( context.getString(R.string.timeMode_civil_short),
                context.getString(R.string.timeMode_civil) );

        TimeMode.ASTRONOMICAL.setDisplayStrings( context.getString(R.string.timeMode_astronomical_short),
                context.getString(R.string.timeMode_astronomical) );

        TimeMode.NOON.setDisplayStrings( context.getString(R.string.timeMode_noon_short),
                context.getString(R.string.timeMode_noon) );

        TimeMode.MIDNIGHT.setDisplayStrings( context.getString(R.string.timeMode_midnight_short),
                context.getString(R.string.timeMode_midnight) );

        TimeMode.GOLD.setDisplayStrings( context.getString(R.string.timeMode_golden_short),
                context.getString(R.string.timeMode_golden) );

        TimeMode.BLUE8.setDisplayStrings( context.getString(R.string.timeMode_blue8_short),
                context.getString(R.string.timeMode_blue8) );

        TimeMode.BLUE4.setDisplayStrings( context.getString(R.string.timeMode_blue4_short),
                context.getString(R.string.timeMode_blue4) );
    }

    public static void initDisplayStrings_RiseSetOrder( Context context )
    {
        RiseSetOrder.TODAY.setDisplayString( context.getString(R.string.risesetorder_today) );
        RiseSetOrder.LASTNEXT.setDisplayString( context.getString(R.string.risesetorder_lastnext) );
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static long getNextSuggestedUpdate(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId;
        return prefs.getLong(prefs_prefix + PREF_KEY_NEXTUPDATE, -1);
    }
    public static void saveNextSuggestedUpdate(Context context, int appWidgetId, long updateTime)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId;
        prefs.putLong(prefs_prefix + PREF_KEY_NEXTUPDATE, updateTime);
        prefs.apply();
    }
    public static void deleteNextSuggestedUpdate(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId;
        prefs.remove(prefs_prefix + PREF_KEY_NEXTUPDATE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveRiseSetOrderPref(Context context, int appWidgetId, RiseSetOrder mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_RISESETORDER, mode.name());
        prefs.apply();
    }
    public static RiseSetOrder loadRiseSetOrderPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_RISESETORDER, PREF_DEF_GENERAL_RISESETORDER.name());

        RiseSetOrder mode;
        try
        {
            mode = RiseSetOrder.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            mode = PREF_DEF_GENERAL_RISESETORDER;
            Log.w("loadRiseSetOrder", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_GENERAL_RISESETORDER.name() + "'.");
        }
        return mode;
    }
    public static void deleteRiseSetOrderPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_RISESETORDER);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveAllowResizePref(Context context, int appWidgetId, boolean allowResize)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_ALLOWRESIZE, allowResize);
        prefs.apply();
    }
    public static boolean loadAllowResizePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_ALLOWRESIZE, PREF_DEF_APPEARANCE_ALLOWRESIZE);
    }
    public static void deleteAllowResizePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_ALLOWRESIZE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveScaleTextPref(Context context, int appWidgetId, boolean scaleText)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SCALETEXT, scaleText);
        prefs.apply();
    }
    public static boolean loadScaleTextPref(Context context, int appWidgetId) {
        return loadScaleTextPref(context, appWidgetId, PREF_DEF_APPEARANCE_SCALETEXT);
    }
    public static boolean loadScaleTextPref(Context context, int appWidgetId, boolean defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SCALETEXT, defValue);
    }
    public static void deleteScaleTextPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_SCALETEXT);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveScaleBasePref(Context context, int appWidgetId, boolean scaleBase)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SCALEBASE, scaleBase);
        prefs.apply();
    }
    public static boolean loadScaleBasePref(Context context, int appWidgetId) {
        return loadScaleBasePref(context, appWidgetId, PREF_DEF_APPEARANCE_SCALEBASE);
    }
    public static boolean loadScaleBasePref(Context context, int appWidgetId, boolean defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SCALEBASE, defValue);
    }
    public static void deleteScaleBasePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_SCALEBASE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveWidgetGravityPref(Context context, int appWidgetId, int gravity)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putInt(prefs_prefix + PREF_KEY_APPEARANCE_GRAVITY, gravity);
        prefs.apply();
    }

    public static int loadWidgetGravityPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getInt(prefs_prefix + PREF_KEY_APPEARANCE_GRAVITY, PREF_DEF_APPEARANCE_GRAVITY.getPosition());
    }
    public static void deleteWidgetGravityPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_GRAVITY);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveSun1x1ModePref(Context context, int appWidgetId, WidgetModeSun1x1 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUN1x1, mode.name());
        prefs.apply();
    }
    public static WidgetModeSun1x1 loadSun1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUN1x1, PREF_DEF_APPEARANCE_WIDGETMODE_SUN1x1.name());

        WidgetModeSun1x1 widgetMode;
        try
        {
            widgetMode = WidgetModeSun1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_SUN1x1;
            Log.w("loadSun1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_SUN1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static SunLayout loadSun1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        SunLayout layout;
        WidgetModeSun1x1 mode = loadSun1x1ModePref(context, appWidgetId);
        switch (mode.getLayoutID())
        {
            case R.layout.layout_widget_1x1_1:
                layout = new SunLayout_1x1_1();
                break;

            case R.layout.layout_widget_1x1_2:
                layout = new SunLayout_1x1_2();
                break;

            case R.layout.layout_widget_1x1_0:
            default:
                layout = new SunLayout_1x1_0(mode.getLayoutID());
                break;
        }
        return layout;
    }
    public static void deleteSun1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUN1x1);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveSunPos1x1ModePref(Context context, int appWidgetId, WidgetModeSunPos1x1 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS1x1, mode.name());
        prefs.apply();
    }
    public static WidgetModeSunPos1x1 loadSunPos1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS1x1, PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS1x1.name());

        WidgetModeSunPos1x1 widgetMode;
        try {
            widgetMode = WidgetModeSunPos1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS1x1;
            Log.w("loadSunPos1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static SunPosLayout loadSunPos1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        SunPosLayout layout;
        WidgetModeSunPos1x1 mode = loadSunPos1x1ModePref(context, appWidgetId);
        switch (mode)
        {
            case MODE1x1_DECRIGHT:
                layout = new SunPosLayout_1X1_1();
                break;

            case MODE1x1_ALTAZ:
            default:
                layout = new SunPosLayout_1X1_0();
                break;
        }
        return layout;
    }
    public static void deleteSunPos1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS1x1);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveSunPos3x1ModePref(Context context, int appWidgetId, WidgetModeSunPos3x1 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1, mode.name());
        prefs.apply();
    }
    public static WidgetModeSunPos3x1 loadSunPos3x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1, PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x1.name());
        try {
            return WidgetModeSunPos3x1.valueOf(modeString);
        } catch (IllegalArgumentException e) {
            Log.w("loadSunPos3x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x1.name() + "'.");
            return PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x1;
        }
    }
    public static SunPosLayout loadSunPos3x1ModePref_asLayout(Context context, int appWidgetId)
    {
        WidgetModeSunPos3x1 mode = loadSunPos3x1ModePref(context, appWidgetId);
        switch (mode) {
            case MODE3x1_LIGHTMAP_SMALL: return new SunPosLayout_3X1_2();
            case MODE3x1_LIGHTMAP_MEDIUM: return new SunPosLayout_3X1_1();
            case MODE3x1_LIGHTMAP: default: return new SunPosLayout_3X1_0();
        }
    }
    public static void deleteSunPos3x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1);
        prefs.apply();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveSunPos3x2ModePref(Context context, int appWidgetId, WidgetModeSunPos3x2 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2, mode.name());
        prefs.apply();
    }
    public static WidgetModeSunPos3x2 loadSunPos3x2ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2, PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x2.name());
        try {
            return WidgetModeSunPos3x2.valueOf(modeString);
        } catch (IllegalArgumentException e) {
            Log.w("loadSunPos3x2ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x2.name() + "'.");
            return PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x2;
        }
    }
    public static SunPosLayout loadSunPos3x2ModePref_asLayout(Context context, int appWidgetId)
    {
        WidgetModeSunPos3x2 mode = loadSunPos3x2ModePref(context, appWidgetId);
        switch (mode) {
            case MODE3x2_LIGHTGRAPH: return new SunPosLayout_3X2_2();
            case MODE3x2_LINEGRAPH: return new SunPosLayout_3X2_1();
            case MODE3x2_WORLDMAP: default: return new SunPosLayout_3X2_0();
        }
    }
    public static void deleteSunPos3x2ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveMoon1x1ModePref(Context context, int appWidgetId, WidgetModeMoon1x1 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1, mode.name());
        prefs.apply();
    }
    public static WidgetModeMoon1x1 loadMoon1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1, PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1.name());

        WidgetModeMoon1x1 widgetMode;
        try
        {
            widgetMode = WidgetModeMoon1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1;
            Log.w("loadMoon1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static MoonLayout loadMoon1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        MoonLayout layout;
        WidgetModeMoon1x1 mode = loadMoon1x1ModePref(context, appWidgetId);
        switch (mode)
        {
            case MODE1x1_MOONDAY:
                layout = new MoonLayout_1x1_9();
                break;

            case MODE1x1_APSIS:
                layout = new MoonLayout_1x1_8();
                break;

            case MODE1x1_DISTANCE:
                layout = new MoonLayout_1x1_7();
                break;

            case MODE1x1_DECRIGHT:
                layout = new MoonLayout_1x1_6();
                break;

            case MODE1x1_ALTAZ:
                layout = new MoonLayout_1x1_5();
                break;

            case MODE1x1_PHASENEXT:
                layout = new MoonLayout_1x1_4();
                break;

            case MODE1x1_ILLUM:
                layout = new MoonLayout_1x1_3();
                break;

            case MODE1x1_PHASE:
                layout = new MoonLayout_1x1_2();
                break;

            case MODE1x1_PHASEILLUM:
                layout = new MoonLayout_1x1_1();
                break;

            case MODE1x1_RISESET:
            default:
                layout = new MoonLayout_1x1_0();
                break;
        }
        return layout;
    }
    public static void deleteMoon1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveDateOffsetPref(Context context, int appWidgetId, int offset)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.putInt(prefs_prefix + PREF_KEY_DATE_OFFSET, offset);
        prefs.apply();
    }

    public static int loadDateOffsetPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        return prefs.getInt(prefs_prefix + PREF_KEY_DATE_OFFSET, PREF_DEF_DATE_OFFSET);
    }

    public static void deleteDateOffsetPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.remove(prefs_prefix + PREF_KEY_DATE_OFFSET);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveThemePref(Context context, int appWidgetId, String themeName)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_THEME, themeName);
        prefs.apply();
    }
    public static String loadThemeName(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_THEME, PREF_DEF_APPEARANCE_THEME);
    }
    public static SuntimesTheme loadThemePref(Context context, int appWidgetId)
    {
        String themeName = loadThemeName(context, appWidgetId);
        //noinspection UnnecessaryLocalVariable
        SuntimesTheme theme = WidgetThemes.loadTheme(context, themeName);
        //Log.d("loadThemePref", "theme is " + theme.themeName());
        return theme;
    }
    public static void deleteThemePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_THEME);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static String keyCalculatorModePref(int appWidgetId)
    {
        return keyCalculatorModePref(appWidgetId, "");
    }
    public static String keyCalculatorModePref(int appWidgetId, @NonNull String calculatorName)
    {
        calculatorName = calculatorName.toLowerCase(Locale.US).trim();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        if (calculatorName.isEmpty())
            return prefs_prefix + PREF_KEY_GENERAL_CALCULATOR;
        else return prefs_prefix + PREF_KEY_GENERAL_CALCULATOR + "_" + calculatorName;
    }

    public static void saveCalculatorModePref(Context context, int appWidgetId, SuntimesCalculatorDescriptor mode)
    {
        saveCalculatorModePref(context, appWidgetId, "", mode);
    }
    public static void saveCalculatorModePref(Context context, int appWidgetId, @NonNull String calculatorName, SuntimesCalculatorDescriptor mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String key = keyCalculatorModePref(appWidgetId, calculatorName);
        prefs.putString(key, mode.getName());
        prefs.apply();
    }

    public static String defaultCalculatorModePref(Context context, int appWidgetId, @NonNull String calculatorName)
    {
        calculatorName = calculatorName.toLowerCase(Locale.US).trim();
        if (!calculatorName.isEmpty())
        {
            for (String[] defaultCalculator : PREF_DEF_GENERAL_CALCULATORS)
            {
                if (defaultCalculator == null)
                {
                    Log.e("loadCalculatorModePref", "Bad default mapping! null. skipping...");
                    continue;

                } else if (defaultCalculator.length != 2) {
                    Log.e("loadCalculatorModePref", "Bad default mapping! incorrect length " + defaultCalculator.length + ". skipping...");
                    continue;
                }

                if (defaultCalculator[0].equals(calculatorName))
                    return defaultCalculator[1];
            }
            Log.w("defaultCalculator", "default for :: " + calculatorName + " :: was not found!");
        }
        return PREF_DEF_GENERAL_CALCULATOR;
    }

    public static SuntimesCalculatorDescriptor loadCalculatorModePref(Context context, int appWidgetId)
    {
        return loadCalculatorModePref(context, appWidgetId, "");
    }
    public static SuntimesCalculatorDescriptor loadCalculatorModePref(Context context, int appWidgetId, @NonNull String calculatorName)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String key = keyCalculatorModePref(appWidgetId, calculatorName);
        String defaultValue = defaultCalculatorModePref(context, appWidgetId, calculatorName);
        String modeString = prefs.getString(key, defaultValue);

        //noinspection UnusedAssignment
        SuntimesCalculatorDescriptor calculatorMode = null;
        try {
            calculatorMode = SuntimesCalculatorDescriptor.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            Log.e("loadCalculatorModePref", e.toString() + " ... It looks like " + modeString + " isn't in our list of calculators.");
            return null;
        }
        return calculatorMode;
    }

    public static void deleteCalculatorModePref(Context context, int appWidgetId)
    {
        deleteCalculatorModePref(context, appWidgetId, "");
    }
    public static void deleteCalculatorModePref(Context context, int appWidgetId, @NonNull String calculatorName)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String key = keyCalculatorModePref(appWidgetId, calculatorName);
        prefs.remove(key);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowLabelsPref(Context context, int appWidgetId, boolean showLabels)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWLABELS, showLabels);
        prefs.apply();
    }
    public static boolean loadShowLabelsPref(Context context, int appWidgetId)
    {
        return loadShowLabelsPref(context, appWidgetId, PREF_DEF_APPEARANCE_SHOWLABELS);
    }
    public static boolean loadShowLabelsPref(Context context, int appWidgetId, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWLABELS, defaultValue);
    }
    public static void deleteShowLabelsPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_SHOWLABELS);
        prefs.apply();
    }


    public static void saveShowTitlePref(Context context, int appWidgetId, boolean showTitle)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE, showTitle);
        prefs.apply();
    }
    public static boolean loadShowTitlePref(Context context, int appWidgetId)
    {
        return loadShowTitlePref(context, appWidgetId, PREF_DEF_APPEARANCE_SHOWTITLE);
    }
    public static boolean loadShowTitlePref(Context context, int appWidgetId, boolean defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE, defValue);
    }
    public static void deleteShowTitlePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTitleTextPref(Context context, int appWidgetId, String titleText)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_TITLETEXT, titleText);
        prefs.apply();
    }
    public static String loadTitleTextPref(Context context, int appWidgetId)
    {
        return loadTitleTextPref(context, appWidgetId, PREF_DEF_APPEARANCE_TITLETEXT);
    }
    public static String loadTitleTextPref(Context context, int appWidgetId, String defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_TITLETEXT, defValue);
    }
    public static void deleteTitleTextPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_TITLETEXT);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeModePref(Context context, int appWidgetId, RiseSetDataMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE, mode.name());
        //Log.d("DEBUG", "save time mode: " + mode.name());
        prefs.apply();
    }
    public static RiseSetDataMode loadTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE, PREF_DEF_GENERAL_TIMEMODE.name());

        RiseSetDataMode mode;
        try {
            mode = TimeMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            if (EventSettings.hasEvent(context, modeString)) {
                mode = new EventAliasTimeMode(EventSettings.loadEvent(context, modeString));
            } else {
                mode = PREF_DEF_GENERAL_TIMEMODE;
            }
        }
        //Log.d("DEBUG", "load time mode: " + mode.name());
        return mode;
    }
    public static void deleteTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeMode2OverridePref(Context context, int appWidgetId, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE, value);
        prefs.apply();
    }
    public static boolean loadTimeMode2OverridePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE, PREF_DEF_GENERAL_TIMEMODE2_OVERRIDE);
    }
    public static void deleteTimeMode2OverridePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2_OVERRIDE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeMode2Pref(Context context, int appWidgetId, SolsticeEquinoxMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2, mode.name());
        prefs.apply();
    }
    public static SolsticeEquinoxMode loadTimeMode2Pref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2, PREF_DEF_GENERAL_TIMEMODE2.name());

        SolsticeEquinoxMode timeMode;
        try
        {
            timeMode = SolsticeEquinoxMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timeMode = PREF_DEF_GENERAL_TIMEMODE2;
        }
        return timeMode;
    }
    public static void deleteTimeMode2Pref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeMode3Pref(Context context, int appWidgetId, MoonPhaseMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE3, mode.name());
        prefs.apply();
    }
    public static MoonPhaseMode loadTimeMode3Pref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE3, PREF_DEF_GENERAL_TIMEMODE3.name());

        MoonPhaseMode timeMode;
        try
        {
            timeMode = MoonPhaseMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timeMode = PREF_DEF_GENERAL_TIMEMODE3;
        }
        return timeMode;
    }
    public static void deleteTimeMode3Pref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE3);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveSolarTimeModePref(Context context, int appWidgetId, SolarTimeMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_SOLARMODE, mode.name());
        prefs.apply();
    }
    public static SolarTimeMode loadSolarTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_SOLARMODE, PREF_DEF_TIMEZONE_SOLARMODE.name());

        SolarTimeMode timeMode;
        try
        {
            timeMode = SolarTimeMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timeMode = PREF_DEF_TIMEZONE_SOLARMODE;
        }
        return timeMode;
    }
    public static void deleteSolarTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_SOLARMODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeFormatModePref(Context context, int appWidgetId, TimeFormatMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_TIMEFORMATMODE, mode.name());
        prefs.apply();
    }
    public static TimeFormatMode loadTimeFormatModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_TIMEFORMATMODE, PREF_DEF_APPEARANCE_TIMEFORMATMODE.name());

        TimeFormatMode formatMode;
        try
        {
            formatMode = TimeFormatMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            formatMode = PREF_DEF_APPEARANCE_TIMEFORMATMODE;
        }
        return formatMode;
    }
    public static void deleteTimeFormatModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_TIMEFORMATMODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveActionModePref(Context context, int appWidgetId, @NonNull WidgetSettings.ActionMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        prefs.putString(prefs_prefix + PREF_KEY_ACTION_MODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.ActionMode loadActionModePref(Context context, int appWidgetId)
    {
        return loadActionModePref(context, appWidgetId, PREF_DEF_ACTION_MODE);
    }
    public static WidgetSettings.ActionMode loadActionModePref(Context context, int appWidgetId, @NonNull WidgetSettings.ActionMode defMode)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_ACTION_MODE, defMode.name());

        ActionMode actionMode;
        try
        {
            actionMode = WidgetSettings.ActionMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            actionMode = PREF_DEF_ACTION_MODE;
        }
        return actionMode;
    }
    public static void deleteActionModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        prefs.remove(prefs_prefix + PREF_KEY_ACTION_MODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocationModePref(Context context, int appWidgetId, LocationMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_MODE, mode.name());
        prefs.apply();
    }
    public static LocationMode loadLocationModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_MODE, PREF_DEF_LOCATION_MODE.name());

        LocationMode locationMode;
        try
        {
            locationMode = LocationMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            locationMode = PREF_DEF_LOCATION_MODE;
        }
        return locationMode;
    }
    public static void deleteLocationModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_MODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveDateModePref(Context context, int appWidgetId, DateMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.putString(prefs_prefix + PREF_KEY_DATE_MODE, mode.name());
        prefs.apply();
    }
    public static DateMode loadDateModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_DATE_MODE, PREF_DEF_DATE_MODE.name());

        DateMode dateMode;
        try
        {
            dateMode = DateMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            dateMode = PREF_DEF_DATE_MODE;
        }
        return dateMode;
    }
    public static void deleteDateModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.remove(prefs_prefix + PREF_KEY_DATE_MODE);
        prefs.apply();
    }

    public static void saveDatePref(Context context, int appWidgetId, DateInfo info )
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.putInt(prefs_prefix + PREF_KEY_DATE_YEAR, info.getYear());
        prefs.putInt(prefs_prefix + PREF_KEY_DATE_MONTH, info.getMonth());
        prefs.putInt(prefs_prefix + PREF_KEY_DATE_DAY, info.getDay());
        prefs.apply();
    }
    public static DateInfo loadDatePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        int year = prefs.getInt(prefs_prefix + PREF_KEY_DATE_YEAR, PREF_DEF_DATE_YEAR);
        int month = prefs.getInt(prefs_prefix + PREF_KEY_DATE_MONTH, PREF_DEF_DATE_MONTH);
        int day = prefs.getInt(prefs_prefix + PREF_KEY_DATE_DAY, PREF_DEF_DATE_DAY);
        return new DateInfo(year, month, day);
    }
    public static void deleteDatePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.remove(prefs_prefix + PREF_KEY_DATE_YEAR);
        prefs.remove(prefs_prefix + PREF_KEY_DATE_MONTH);
        prefs.remove(prefs_prefix + PREF_KEY_DATE_DAY);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimezoneModePref(Context context, int appWidgetId, TimezoneMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, mode.name());
        prefs.apply();
    }
    public static TimezoneMode loadTimezoneModePref(Context context, int appWidgetId)
    {
        return loadTimezoneModePref(context, appWidgetId, PREF_DEF_TIMEZONE_MODE);
    }
    public static TimezoneMode loadTimezoneModePref(Context context, int appWidgetId, TimezoneMode defaultMode)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, defaultMode.name());

        TimezoneMode timezoneMode;
        try
        {
            timezoneMode = TimezoneMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timezoneMode = defaultMode;
        }
        return timezoneMode;
    }
    public static void deleteTimezoneModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_MODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocationPref(Context context, int appWidgetId, Location location)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE, location.getAltitude());
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE, location.getLongitude());
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_LATITUDE, location.getLatitude());
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_LABEL, location.getLabel());
        prefs.apply();
    }
    public static Location loadLocationPref(Context context, int appWidgetId)
    {
        if (loadLocationFromAppPref(context, appWidgetId)) {
            appWidgetId = 0;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;

        String defaultAlt = PREF_DEF_LOCATION_ALTITUDE;   // locale defaults
        String defaultLon = PREF_DEF_LOCATION_LONGITUDE;
        String defaultLat = PREF_DEF_LOCATION_LATITUDE;
        String defaultName = PREF_DEF_LOCATION_LABEL;
        boolean defaultUseAltitude = PREF_DEF_LOCATION_ALTITUDE_ENABLED;

        String nameString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LABEL, null);
        if (nameString == null)
        {
            String prefs_prefix0 = PREF_PREFIX_KEY + "0" + PREF_PREFIX_KEY_LOCATION;    // prefer app configuration (if it exists) over locale default
            defaultAlt = prefs.getString(prefs_prefix0 + PREF_KEY_LOCATION_ALTITUDE, PREF_DEF_LOCATION_ALTITUDE);
            defaultLon = prefs.getString(prefs_prefix0 + PREF_KEY_LOCATION_LONGITUDE, PREF_DEF_LOCATION_LONGITUDE);
            defaultLat = prefs.getString(prefs_prefix0 + PREF_KEY_LOCATION_LATITUDE, PREF_DEF_LOCATION_LATITUDE);
            defaultName = prefs.getString(prefs_prefix0 + PREF_KEY_LOCATION_LABEL, PREF_DEF_LOCATION_LABEL);
            defaultUseAltitude = prefs.getBoolean(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE_ENABLED, PREF_DEF_LOCATION_ALTITUDE_ENABLED);
        }

        String altString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE, defaultAlt);
        String lonString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE, defaultLon);
        String latString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LATITUDE, defaultLat);
        nameString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LABEL, defaultName);

        Location location = new Location(nameString, latString, lonString, altString);
        location.setUseAltitude(prefs.getBoolean(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE_ENABLED, defaultUseAltitude));
        return location;
    }
    public static Location loadLocationDefault()
    {
        Location location = new Location(PREF_DEF_LOCATION_LABEL, PREF_DEF_LOCATION_LATITUDE, PREF_DEF_LOCATION_LONGITUDE, PREF_DEF_LOCATION_ALTITUDE);
        location.setUseAltitude(PREF_DEF_LOCATION_ALTITUDE_ENABLED);
        return location;
    }
    public static void deleteLocationPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE);
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE);
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_LATITUDE);
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_LABEL);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocationAltitudeEnabledPref(Context context, int appWidgetId, boolean enabled)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putBoolean(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE_ENABLED, enabled);
        prefs.apply();
    }
    public static boolean loadLocationAltitudeEnabledPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        boolean enabled = prefs.getBoolean(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE_ENABLED, PREF_DEF_LOCATION_ALTITUDE_ENABLED);
        return enabled;
    }
    public static void deleteLocationAltitudeEnabledPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE_ENABLED);
        prefs.apply();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocationFromAppPref(Context context, int appWidgetId, boolean enabled)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putBoolean(prefs_prefix + PREF_KEY_LOCATION_FROMAPP, enabled);
        prefs.apply();
    }
    public static boolean loadLocationFromAppPref(Context context, int appWidgetId) {
        return loadLocationFromAppPref(context, appWidgetId, PREF_DEF_LOCATION_FROMAPP);
    }
    public static boolean loadLocationFromAppPref(Context context, int appWidgetId, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        boolean enabled = prefs.getBoolean(prefs_prefix + PREF_KEY_LOCATION_FROMAPP, defaultValue);
        return enabled;
    }
    public static void deleteLocationFromAppPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_FROMAPP);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeZoneFromAppPref(Context context, int appWidgetId, boolean enabled)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_TIMEZONE_FROMAPP, enabled);
        prefs.apply();
    }
    public static boolean loadTimeZoneFromAppPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        boolean enabled = prefs.getBoolean(prefs_prefix + PREF_KEY_TIMEZONE_FROMAPP, PREF_DEF_TIMEZONE_FROMAPP);
        return enabled;
    }
    public static void deleteTimeZoneFromAppPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_FROMAPP);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimezonePref(Context context, int appWidgetId, String timezone) {
        saveTimezonePref(context, appWidgetId, timezone, "");
    }
    public static void saveTimezonePref(Context context, int appWidgetId, String timezone, String slotName)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String key = keyTimezonePref(appWidgetId, slotName);
        prefs.putString(key, timezone);
        prefs.apply();
    }

    public static String loadTimezonePref(Context context, int appWidgetId) {
        return loadTimezonePref(context, appWidgetId, "");
    }
    public static String loadTimezonePref(Context context, int appWidgetId, @NonNull String slotName)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String key = keyTimezonePref(appWidgetId, slotName);
        String defaultValue = defaultTimezonePref(context, appWidgetId, slotName);
        return prefs.getString(key, defaultValue);
    }

    public static void deleteTimezonePref(Context context, int appWidgetId) {
        deleteTimezonePref(context, appWidgetId, "");
    }
    public static void deleteTimezonePref(Context context, int appWidgetId, @NonNull String slotName)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String key = keyTimezonePref(appWidgetId, slotName);
        prefs.remove(key);
        prefs.apply();
    }

    public static String keyTimezonePref(int appWidgetId, @NonNull String slotName)
    {
        slotName = slotName.toLowerCase(Locale.US).trim();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        if (slotName.isEmpty())
            return prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM;
        else return prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM + "_" + slotName;
    }
    public static String defaultTimezonePref(Context context, int appWidgetId, @NonNull String slotName)
    {
        slotName = slotName.toLowerCase(Locale.US).trim();
        if (!slotName.isEmpty())
        {
            for (String[] defaultTimezone : PREF_DEF_TIMEZONES)
            {
                if (defaultTimezone == null) {
                    Log.e("defaultTimezonePref", "Bad default mapping! null. skipping...");
                    continue;
                } else if (defaultTimezone.length != 2) {
                    Log.e("defaultTimezonePref", "Bad default mapping! incorrect length " + defaultTimezone.length + ". skipping...");
                    continue;
                } else if (defaultTimezone[0].equals(slotName)) {
                    return defaultTimezone[1];
                }
            }
            Log.w("defaultTimezone", "default for :: " + slotName + " :: was not found!");
        }
        return PREF_DEF_TIMEZONE_CUSTOM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTrackingModePref(Context context, int appWidgetId, TrackingMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TRACKINGMODE, mode.name());
        prefs.apply();
    }
    public static TrackingMode loadTrackingModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TRACKINGMODE, PREF_DEF_GENERAL_TRACKINGMODE.name());

        TrackingMode trackingMode;
        try
        {
            trackingMode = TrackingMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            trackingMode = PREF_DEF_GENERAL_TRACKINGMODE;
        }
        return trackingMode;
    }
    public static void deleteTrackingModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TRACKINGMODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTrackingLevelPref(Context context, int appWidgetId, int level)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putInt(prefs_prefix + PREF_KEY_GENERAL_TRACKINGLEVEL, level);
        prefs.apply();
    }
    public static int loadTrackingLevelPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getInt(prefs_prefix + PREF_KEY_GENERAL_TRACKINGLEVEL, PREF_DEF_GENERAL_TRACKINGLEVEL);
    }
    public static void deleteTrackingLevelPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TRACKINGLEVEL);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveCompareModePref(Context context, int appWidgetId, CompareMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_COMPAREMODE, mode.name());
        prefs.apply();
    }
    public static CompareMode loadCompareModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_COMPAREMODE, PREF_DEF_GENERAL_COMPAREMODE.name());

        CompareMode compareMode;
        try
        {
            compareMode = CompareMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            compareMode = PREF_DEF_GENERAL_COMPAREMODE;
        }
        return compareMode;
    }
    public static void deleteCompareModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_COMPAREMODE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowComparePref(Context context, int appWidgetId, boolean showCompare)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWCOMPARE, showCompare);
        prefs.apply();
    }
    public static boolean loadShowComparePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWCOMPARE, PREF_DEF_GENERAL_SHOWCOMPARE);
    }
    public static void deleteShowComparePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWCOMPARE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowNoonPref(Context context, int appWidgetId, boolean showNoon)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWNOON, showNoon);
        prefs.apply();
    }
    public static boolean loadShowNoonPref(Context context, int appWidgetId) {
        return loadShowNoonPref(context, appWidgetId, PREF_DEF_GENERAL_SHOWNOON);
    }
    public static boolean loadShowNoonPref(Context context, int appWidgetId, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWNOON, defaultValue);
    }
    public static void deleteShowNoonPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWNOON);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowWeeksPref(Context context, int appWidgetId, boolean showWeeks)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWWEEKS, showWeeks);
        prefs.apply();
    }
    public static boolean loadShowWeeksPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWWEEKS, PREF_DEF_GENERAL_SHOWWEEKS);
    }
    public static void deleteShowWeeksPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWWEEKS);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowHoursPref(Context context, int appWidgetId, boolean showHours)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWHOURS, showHours);
        prefs.apply();
    }
    public static boolean loadShowHoursPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWHOURS, PREF_DEF_GENERAL_SHOWHOURS);
    }
    public static void deleteShowHoursPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWHOURS);
        prefs.apply();
    }


    public static void saveShowSecondsPref(Context context, int appWidgetId, boolean showSeconds)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWSECONDS, showSeconds);
        prefs.apply();
    }
    public static boolean loadShowSecondsPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWSECONDS, PREF_DEF_GENERAL_SHOWSECONDS);
    }
    public static void deleteShowSecondsPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWSECONDS);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowTimeDatePref(Context context, int appWidgetId, boolean showTimeWithDates)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWTIMEDATE, showTimeWithDates);
        prefs.apply();
    }
    public static boolean loadShowTimeDatePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWTIMEDATE, PREF_DEF_GENERAL_SHOWTIMEDATE);
    }
    public static void deleteShowTimeDatePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWTIMEDATE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveShowAbbrMonthPref(Context context, int appWidgetId, boolean abbreviate)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWABBRMONTH, abbreviate);
        prefs.apply();
    }
    public static boolean loadShowAbbrMonthPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWABBRMONTH, PREF_DEF_GENERAL_SHOWABBRMONTH);
    }
    public static void deleteShowAbbrMonthPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_SHOWABBRMONTH);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocalizeHemispherePref(Context context, int appWidgetId, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putBoolean(prefs_prefix + PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE, value);
        prefs.apply();
    }
    public static boolean loadLocalizeHemispherePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE, PREF_DEF_GENERAL_LOCALIZE_HEMISPHERE);
    }
    public static void deleteLocalizeHemispherePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveObserverHeightPref(Context context, int appWidgetId, float meters)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putFloat(prefs_prefix + PREF_KEY_GENERAL_OBSERVERHEIGHT, meters);
        prefs.apply();
    }
    public static float loadObserverHeightPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getFloat(prefs_prefix + PREF_KEY_GENERAL_OBSERVERHEIGHT, PREF_DEF_GENERAL_OBSERVERHEIGHT);
    }
    public static void deleteObserverHeightPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_OBSERVERHEIGHT);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLengthUnitsPref(Context context, int appWidgetId, LengthUnit value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_UNITS_LENGTH, value.name());
        prefs.apply();
    }

    public static LengthUnit loadLengthUnitsPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String defaultValue = (appWidgetId == 0) ? PREF_DEF_GENERAL_UNITS_LENGTH.name()             // prefer the current app setting [0] as the default value
                : prefs.getString(PREF_PREFIX_KEY + 0 + PREF_PREFIX_KEY_GENERAL + PREF_KEY_GENERAL_UNITS_LENGTH, PREF_DEF_GENERAL_UNITS_LENGTH.name());
        return getLengthUnit(prefs.getString(prefs_prefix + PREF_KEY_GENERAL_UNITS_LENGTH, defaultValue));
    }

    public static LengthUnit getLengthUnit(String unitName)
    {
        LengthUnit retValue;
        try {
            retValue = LengthUnit.valueOf(unitName);
        } catch (IllegalArgumentException e) {
            retValue = PREF_DEF_GENERAL_UNITS_LENGTH;
        }
        return retValue;
    }

    public static void deleteLengthUnitsPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_UNITS_LENGTH);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeNoteRisePref(Context context, int appWidgetId, String riseChoice)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_RISE, riseChoice);
        prefs.apply();
    }
    public static String loadTimeNoteRisePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_RISE, PREF_DEF_GENERAL_TIMENOTE_RISE.name());
    }
    public static void deleteTimeNoteRisePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_RISE);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTimeNoteSetPref(Context context, int appWidgetId, String setChoice)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_SET, setChoice);
        prefs.apply();
    }
    public static String loadTimeNoteSetPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_SET, PREF_DEF_GENERAL_TIMENOTE_SET.name());
    }
    public static void deleteTimeNoteSetPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_SET);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteNextSuggestedUpdate(context, appWidgetId);
        deleteActionModePref(context, appWidgetId);

        deleteSun1x1ModePref(context, appWidgetId);
        deleteSunPos1x1ModePref(context, appWidgetId);
        deleteSunPos3x1ModePref(context, appWidgetId);
        deleteSunPos3x2ModePref(context, appWidgetId);
        deleteMoon1x1ModePref(context, appWidgetId);
        deleteAllowResizePref(context, appWidgetId);
        deleteScaleTextPref(context, appWidgetId);
        deleteScaleBasePref(context, appWidgetId);
        deleteWidgetGravityPref(context, appWidgetId);

        deleteThemePref(context, appWidgetId);
        deleteShowLabelsPref(context, appWidgetId);
        deleteShowTitlePref(context, appWidgetId);
        deleteTitleTextPref(context, appWidgetId);
        deleteTimeFormatModePref(context, appWidgetId);

        deleteCalculatorModePref(context, appWidgetId);
        deleteCalculatorModePref(context, appWidgetId, "moon");

        deleteTimeModePref(context, appWidgetId);
        deleteTimeMode2Pref(context, appWidgetId);
        deleteTimeMode3Pref(context, appWidgetId);

        deleteRiseSetOrderPref(context, appWidgetId);
        deleteTrackingModePref(context, appWidgetId);
        deleteTrackingLevelPref(context, appWidgetId);
        deleteCompareModePref(context, appWidgetId);
        deleteShowComparePref(context, appWidgetId);
        deleteShowNoonPref(context, appWidgetId);
        deleteShowWeeksPref(context, appWidgetId);
        deleteShowHoursPref(context, appWidgetId);
        deleteShowSecondsPref(context, appWidgetId);
        deleteShowTimeDatePref(context, appWidgetId);
        deleteShowAbbrMonthPref(context, appWidgetId);

        deleteLocalizeHemispherePref(context, appWidgetId);

        deleteObserverHeightPref(context, appWidgetId);
        deleteLengthUnitsPref(context, appWidgetId);

        deleteLocationModePref(context, appWidgetId);
        deleteLocationAltitudeEnabledPref(context, appWidgetId);
        deleteLocationFromAppPref(context, appWidgetId);
        deleteTimeZoneFromAppPref(context, appWidgetId);
        deleteLocationPref(context, appWidgetId);

        deleteTimezoneModePref(context, appWidgetId);
        deleteSolarTimeModePref(context, appWidgetId);
        deleteTimezonePref(context, appWidgetId);

        CalendarSettings.deletePrefs(context, appWidgetId);

        deleteDateModePref(context, appWidgetId);
        deleteDatePref(context, appWidgetId);
        deleteDateOffsetPref(context, appWidgetId);

        deleteTimeNoteRisePref(context, appWidgetId);
        deleteTimeNoteSetPref(context, appWidgetId);

        WidgetActions.deletePrefs(context, appWidgetId);
        WidgetSettingsMetadata.deleteMetaData(context, appWidgetId);
        AlarmWidgetSettings.deletePrefs(context, appWidgetId);
    }

    public static void initDefaults( Context context )
    {
        PREF_DEF_LOCATION_LABEL = context.getString(R.string.default_location_label);
        PREF_DEF_LOCATION_LATITUDE = context.getString(R.string.default_location_latitude);
        PREF_DEF_LOCATION_LONGITUDE = context.getString(R.string.default_location_longitude);
        PREF_DEF_LOCATION_ALTITUDE = context.getString(R.string.default_location_altitude);
        PREF_DEF_TIMEZONE_CUSTOM = context.getString(R.string.default_timezone);
        PREF_DEF_GENERAL_UNITS_LENGTH = getLengthUnit(context.getString(R.string.default_units_length));

        WidgetActions.initDefaults(context);
    }

    public static void initDisplayStrings( Context context )
    {
        LengthUnitDisplay.initDisplayStrings_LengthUnit(AndroidResources.wrap(context));
        initDisplayStrings_ActionMode(context);
        WidgetModeSun1x1.initDisplayStrings(context);
        WidgetModeSun2x1.initDisplayStrings(context);
        WidgetModeSun3x1.initDisplayStrings(context);
        WidgetModeSunPos1x1.initDisplayStrings(context);
        WidgetModeSunPos3x1.initDisplayStrings(context);
        WidgetModeSunPos3x2.initDisplayStrings(context);
        WidgetModeMoon1x1.initDisplayStrings(context);
        WidgetModeMoon2x1.initDisplayStrings(context);
        WidgetModeMoon3x1.initDisplayStrings(context);
        initDisplayStrings_WidgetGravity(context);
        initDisplayStrings_TrackingMode(context);
        initDisplayStrings_CompareMode(context);
        initDisplayStrings_TimeMode(context);
        initDisplayStrings_MoonPhaseMode(context);
        initDisplayStrings_SolsticeEquinoxMode(context);
        initDisplayStrings_LocationMode(context);
        initDisplayStrings_TimezoneMode(context);
        initDisplayStrings_SolarTimeMode(context);
        initDisplayStrings_DateMode(context);
        initDisplayStrings_TimeFormatMode(context);
        initDisplayStrings_RiseSetOrder(context);
        CalendarSettings.initDisplayStrings(context);
        WidgetActions.initDisplayStrings(context);
        AlarmWidgetSettings.initDisplayStrings(context);
    }

}
