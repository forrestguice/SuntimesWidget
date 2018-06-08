/**
    Copyright (C) 2014-2018 Forrest Guice
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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.layouts.MoonLayout;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_0;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_1;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_2;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_3;
import com.forrestguice.suntimeswidget.layouts.MoonLayout_1x1_4;
import com.forrestguice.suntimeswidget.layouts.SunLayout;
import com.forrestguice.suntimeswidget.layouts.SunLayout_1x1_0;
import com.forrestguice.suntimeswidget.layouts.SunLayout_1x1_1;
import com.forrestguice.suntimeswidget.layouts.SunLayout_1x1_2;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_1X1_0;
import com.forrestguice.suntimeswidget.layouts.SunPosLayout_1X1_1;
import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

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
    public static final String PREF_DEF_GENERAL_CALCULATOR = "time4a-noaa";
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

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_MOON1x1 = "widgetmode_moon1x1";
    public static final WidgetModeMoon1x1 PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1 = WidgetModeMoon1x1.MODE1x1_RISESET;

    public static final String PREF_KEY_APPEARANCE_ALLOWRESIZE = "allowresize";
    public static final boolean PREF_DEF_APPEARANCE_ALLOWRESIZE = true;

    public static final String PREF_KEY_APPEARANCE_TIMEFORMATMODE = "timeformatmode";
    public static final TimeFormatMode PREF_DEF_APPEARANCE_TIMEFORMATMODE = TimeFormatMode.MODE_SYSTEM;

    public static final String PREF_KEY_GENERAL_TIMEMODE = "timemode";
    public static final TimeMode PREF_DEF_GENERAL_TIMEMODE = TimeMode.OFFICIAL;

    public static final String PREF_KEY_GENERAL_TIMEMODE2 = "timemode2";
    public static final SolsticeEquinoxMode PREF_DEF_GENERAL_TIMEMODE2 = SolsticeEquinoxMode.EQUINOX_VERNAL;

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

    public static final String PREF_KEY_GENERAL_OBSERVERHEIGHT = "observerheight";
    public static final float PREF_DEF_GENERAL_OBSERVERHEIGHT = 1.8288f; // meters (6ft)

    public static final String PREF_KEY_ACTION_MODE = "action";
    public static final ActionMode PREF_DEF_ACTION_MODE = ActionMode.ONTAP_LAUNCH_CONFIG;

    public static final String PREF_KEY_ACTION_LAUNCH = "launch";
    public static final String PREF_DEF_ACTION_LAUNCH = "com.forrestguice.suntimeswidget.SuntimesActivity";

    public static final String PREF_KEY_LOCATION_MODE = "locationMode";
    public static final LocationMode PREF_DEF_LOCATION_MODE = LocationMode.CUSTOM_LOCATION;

    public static final String PREF_KEY_LOCATION_LONGITUDE = "longitude";
    public static String PREF_DEF_LOCATION_LONGITUDE = "-112.4691";      // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_LATITUDE = "latitude";
    public static String PREF_DEF_LOCATION_LATITUDE = "34.5409";         // reassigned later by initDefaults

    public static final String PREF_KEY_LOCATION_ALTITUDE = "altitude";
    public static String PREF_DEF_LOCATION_ALTITUDE = "";

    public static final String PREF_KEY_LOCATION_LABEL = "label";
    public static String PREF_DEF_LOCATION_LABEL = "Prescott, AZ";       // reassigned later by initDefaults

    public static final String PREF_KEY_TIMEZONE_MODE = "timezoneMode";
    public static final TimezoneMode PREF_DEF_TIMEZONE_MODE = TimezoneMode.CURRENT_TIMEZONE;

    public static final String PREF_KEY_TIMEZONE_CUSTOM = "timezone";
    public static final String PREF_DEF_TIMEZONE_CUSTOM = "US/Arizona";

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

        public static void initDisplayStrings( Context context )
        {
            ONTAP_DONOTHING.setDisplayString(context.getString(R.string.actionMode_doNothing));
            ONTAP_UPDATE.setDisplayString(context.getString(R.string.actionMode_update));
            ONTAP_LAUNCH_CONFIG.setDisplayString(context.getString(R.string.actionMode_config));
            ONTAP_LAUNCH_ACTIVITY.setDisplayString(context.getString(R.string.actionMode_launchActivity));
            ONTAP_FLIPTO_NEXTITEM.setDisplayString(context.getString(R.string.actionMode_flipToNextItem));
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

    /**
     * WidgetModeSun1x1
     */
    public static enum WidgetModeSun1x1
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
    }


    /**
     * WidgetModeSunPos1x1
     */
    public static enum WidgetModeSunPos1x1
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
    }


    /**
     * WidgetModeMoon1x1
     */
    public static enum WidgetModeMoon1x1
    {
        MODE1x1_RISESET("Moonrise & moonset", R.layout.layout_widget_moon_1x1_0),
        MODE1x1_PHASEILLUM("Moon phase & illumination", R.layout.layout_widget_moon_1x1_1),
        MODE1x1_PHASE("Moon phase only", R.layout.layout_widget_moon_1x1_2),
        MODE1x1_ILLUM("Moon illumination only", R.layout.layout_widget_moon_1x1_3),
        MODE1x1_PHASENEXT("Next major phase", R.layout.layout_widget_moon_1x1_4);

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
        }
    }


    /**
     * DateMode
     */
    public static enum DateMode
    {
        CURRENT_DATE("Today"),
        CUSTOM_DATE("User Defined");

        private String displayString;

        private DateMode(String displayString)
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

        public static void initDisplayStrings( Context context )
        {
            CURRENT_DATE.setDisplayString(context.getString(R.string.dateMode_current));
            CUSTOM_DATE.setDisplayString(context.getString(R.string.dateMode_custom));
        }
    }

    /**
     * DateInfo
     */
    public static class DateInfo
    {
        private int year = -1, month = -1, day = -1;

        public DateInfo(Calendar date)
        {
            this(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        }
        public DateInfo( int year, int month, int day )
        {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getDay() { return day; }

        public boolean isSet()
        {
            return (year != -1 && month != -1 && day != -1);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof DateInfo))
            {
                return false;
            } else {
                DateInfo that = (DateInfo)obj;
                return (this.getYear() == that.getYear()) && (this.getMonth() == that.getMonth()) && (this.getDay() == that.getDay());
            }
        }

        @Override
        public int hashCode()
        {
            int hash = Integer.valueOf(year).hashCode();
            hash = hash * 37 + (Integer.valueOf(month).hashCode());
            hash = hash * 37 + (Integer.valueOf(day).hashCode());
            return hash;
        }
    }

    /**
     * TimezoneMode
     */
    public static enum TimezoneMode
    {
        SOLAR_TIME("Solar"),
        CURRENT_TIMEZONE("Current"),
        CUSTOM_TIMEZONE("Custom");

        private String displayString;

        private TimezoneMode(String displayString)
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

        public static void initDisplayStrings( Context context )
        {
            SOLAR_TIME.setDisplayString(context.getString(R.string.timezoneMode_solar));
            CURRENT_TIMEZONE.setDisplayString(context.getString(R.string.timezoneMode_current));
            CUSTOM_TIMEZONE.setDisplayString(context.getString(R.string.timezoneMode_custom));
        }
    }

    /**
     * SolarTimeMode
     */
    public static enum SolarTimeMode
    {
        APPARENT_SOLAR_TIME("Current"),
        LOCAL_MEAN_TIME("Solar");

        private String displayString;

        private SolarTimeMode(String displayString)
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

        public static void initDisplayStrings( Context context )
        {
            LOCAL_MEAN_TIME.setDisplayString(context.getString(R.string.solartime_localMean));
            APPARENT_SOLAR_TIME.setDisplayString(context.getString(R.string.solartime_apparent));
        }
    }

    /**
     * TimeFormatMode
     */
    public static enum TimeFormatMode
    {
        MODE_SYSTEM("System"),
        MODE_12HR("12 hr"),
        MODE_24HR("24 hr");

        private String displayString;

        private TimeFormatMode( String displayString )
        {
            this.displayString = displayString;
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
            MODE_SYSTEM.setDisplayString(context.getString(R.string.timeFormatMode_system));
            MODE_12HR.setDisplayString(context.getString(R.string.timeFormatMode_12hr));
            MODE_24HR.setDisplayString(context.getString(R.string.timeFormatMode_24hr));

        }
    }

    /**
     * LocationMode
     */
    public static enum LocationMode
    {
        CURRENT_LOCATION("Current Location"),
        CUSTOM_LOCATION("Custom Location");

        private String displayString;

        private LocationMode(String displayString)
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

        public static void initDisplayStrings( Context context )
        {
            CURRENT_LOCATION.setDisplayString(context.getString(R.string.locationMode_current));
            CUSTOM_LOCATION.setDisplayString(context.getString(R.string.locationMode_custom));
        }
    }

    /**
     * Location
     */
    public static class Location
    {
        public static String pattern_latLon = "#.#####";

        private String label;
        private String latitude;   // decimal degrees (DD)
        private String longitude;  // decimal degrees (DD)
        private String altitude;   // meters above the WGS 84 reference ellipsoid

        /**
         * @param latitude decimal degrees (DD) string
         * @param longitude decimal degrees (DD) string
         */
        public Location( String latitude, String longitude )
        {
            this(null, latitude, longitude, null);
        }

        /**
         * @param label display name
         * @param latitude decimal degrees (DD) string
         * @param longitude decimal degrees (DD) string
         */
        public Location( String label, String latitude, String longitude )
        {
            this(label, latitude, longitude, null);
        }

        /**
         * @param label display name
         * @param latitude decimal degrees (DD) string
         * @param longitude decimal degrees (DD) string
         * @param altitude meters string
         */
        public Location( String label, String latitude, String longitude, String altitude )
        {
            this.label = (label == null) ? "" : label;
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = (altitude == null) ? "" : altitude;
        }

        /**
         * @param label display name
         * @param location an android.location.Location object (that might be obtained via GPS or otherwise)
         */
        public Location( String label, @NonNull android.location.Location location )
        {
            double rawLatitude = location.getLatitude();
            double rawLongitude = location.getLongitude();
            double rawAltitude = location.getAltitude();

            DecimalFormat formatter = decimalDegreesFormatter();

            this.label = label;
            this.latitude = formatter.format(rawLatitude);
            this.longitude = formatter.format(rawLongitude);
            this.altitude = rawAltitude + "";
        }

        /**
         * @return a user-defined display label / location name
         */
        public String getLabel()
        {
            return label;
        }

        /**
         * @return latitude in decimal degrees (DD)
         */
        public String getLatitude()
        {
            return latitude;
        }

        public Double getLatitudeAsDouble()
        {
            double latitudeDouble = Double.parseDouble(latitude);
            if (latitudeDouble > 90 || latitudeDouble < -90)
            {
                double s = Math.signum(latitudeDouble);
                double adjusted = (s * 90) - (latitudeDouble % (s * 90));
                Log.w("Location", "latitude is out of range! adjusting.. " + latitudeDouble + " -> " + adjusted);
                latitudeDouble = adjusted;
            }
            return latitudeDouble;
        }

        /**
         * @return longitude in decimal degrees (DD)
         */
        public String getLongitude()
        {
            return longitude;
        }

        public Double getLongitudeAsDouble()
        {
            Double longitudeDouble = Double.parseDouble(longitude);
            if (longitudeDouble > 180 || longitudeDouble < -180)
            {
                double s = Math.signum(longitudeDouble);
                double adjusted = (longitudeDouble % (s * 180)) - (s * 180);
                Log.w("Location", "longitude is out of range! adjusting.. " + longitudeDouble + " -> " + adjusted);
                longitudeDouble = adjusted;
            }
            if (longitudeDouble == 180d) {
                longitudeDouble = -180d;
            }
            return longitudeDouble;
        }

        /**
         * @return altitude in meters
         */
        public String getAltitude() { return altitude; }

        public Double getAltitudeAsDouble()
        {
            if (altitude.isEmpty())
                return 0.0;
            else return Double.parseDouble(altitude);
        }
        public Integer getAltitudeAsInteger()
        {
            return getAltitudeAsDouble().intValue();
        }

        /**
         * @return a "geo" URI describing this Location
         */
        public Uri getUri()
        {
            String uriString = "geo:" + latitude + "," + longitude;
            if (!altitude.isEmpty())
            {
                uriString += "," + altitude;
            }
            return Uri.parse(uriString);
        }

        /**
         * @return a decimal degrees string "latitude, longitude" describing this location
         */
        public String toString()
        {
            return latitude + ", " + longitude;
        }

        /**
         * @param obj another Location object
         * @return true the locations are the same (label, lat, lon, and alt), false they are different somehow
         */
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Location))
            {
                return false;
            } else {
                Location that = (Location)obj;
                return (this.getLabel().equals(that.getLabel()))
                        && (this.getLatitude().equals(that.getLatitude()))
                        && (this.getLongitude().equals(that.getLongitude()))
                        && (this.getAltitude().equals(that.getAltitude()));
            }
        }

        public static DecimalFormat decimalDegreesFormatter()
        {
            DecimalFormat formatter = (DecimalFormat)(NumberFormat.getNumberInstance(Locale.US));
            formatter.applyLocalizedPattern(pattern_latLon);
            return formatter;
        }
    }

    /**
     * CompareMode
     */
    public static enum CompareMode
    {
        YESTERDAY("Yesterday"),
        TOMORROW("Tomorrow");

        private String displayString;

        private CompareMode( String displayString )
        {
            this.displayString = displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        public String toString()
        {
            return displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            YESTERDAY.setDisplayString( context.getString(R.string.compareMode_yesterday) );
            TOMORROW.setDisplayString(context.getString(R.string.compareMode_tomorrow));
        }
    }

    /**
     * TrackingMode
     */
    public static enum TrackingMode
    {
        CLOSEST("Closest Event"),
        SOONEST("Upcoming Event");

        private String displayString;

        private TrackingMode( String displayString )
        {
            this.displayString = displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        public String toString()
        {
            return displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            CLOSEST.setDisplayString( context.getString(R.string.trackingMode_closest) );
            SOONEST.setDisplayString( context.getString(R.string.trackingMode_soonest) );
        }
    }

    /**
     * SolsticeEquinoxMode
     */
    public static enum SolsticeEquinoxMode
    {
        EQUINOX_VERNAL("Equinox", "Vernal Equinox"),
        SOLSTICE_SUMMER("Solstice", "Summer Solstice"),
        EQUINOX_AUTUMNAL("Equinox", "Autumnal Equinox"),
        SOLSTICE_WINTER("Solstice", "Winter Solstice");

        public static boolean shortDisplayStrings = false;

        private String shortDisplayString;
        private String longDisplayString;

        private SolsticeEquinoxMode(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public String toString()
        {
            if (shortDisplayStrings)
                return shortDisplayString;
            else return longDisplayString;
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayStrings(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            EQUINOX_VERNAL.setDisplayStrings(context.getString(R.string.timeMode_equinox_vernal_short),
                    context.getString(R.string.timeMode_equinox_vernal));

            SOLSTICE_SUMMER.setDisplayStrings( context.getString(R.string.timeMode_solstice_summer_short),
                    context.getString(R.string.timeMode_solstice_summer));

            EQUINOX_AUTUMNAL.setDisplayStrings( context.getString(R.string.timeMode_equinox_autumnal_short),
                    context.getString(R.string.timeMode_equinox_autumnal) );

            SOLSTICE_WINTER.setDisplayStrings(context.getString(R.string.timeMode_solstice_winter_short),
                    context.getString(R.string.timeMode_solstice_winter));
        }
    }

    /**
     * MoonPhaseMode
     */
    public static enum MoonPhaseMode
    {
        NEW_MOON("New", "New Moon"),
        FIRST_QUARTER("First Quarter", "First Quarter Moon"),
        FULL_MOON("Full", "Full Moon"),
        THIRD_QUARTER("Third Quarter", "Third Quarter Moon");

        private String shortDisplayString;
        private String longDisplayString;

        public static boolean shortDisplayStrings = false;

        private MoonPhaseMode( String shortDisplayString, String longDisplayString )
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public String toString()
        {
            if (shortDisplayStrings)
                return shortDisplayString;
            else return longDisplayString;
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayStrings(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            NEW_MOON.setDisplayStrings(context.getString(R.string.timeMode_moon_new_short),
                    context.getString(R.string.timeMode_moon_new));

            FIRST_QUARTER.setDisplayStrings( context.getString(R.string.timeMode_moon_firstquarter_short),
                    context.getString(R.string.timeMode_moon_firstquarter));

            FULL_MOON.setDisplayStrings( context.getString(R.string.timeMode_moon_full_short),
                    context.getString(R.string.timeMode_moon_full) );

            THIRD_QUARTER.setDisplayStrings(context.getString(R.string.timeMode_moon_thirdquarter_short),
                    context.getString(R.string.timeMode_moon_thirdquarter));
        }
    }

    /**
     * TimeMode
     */
    public static enum TimeMode
    {
        OFFICIAL("Actual", "Actual Time"),
        CIVIL("Civil", "Civil Twilight"),
        NAUTICAL("Nautical", "Nautical Twilight"),
        ASTRONOMICAL("Astronomical", "Astronomical Twilight"),
        NOON("Noon", "Solar Noon"),
        GOLD("Golden", "Golden Hour"),
        BLUE8("Blue", "Blue Hour"),      // 8 deg; morning start, evening end
        BLUE4("Blue", "Blue Hour");      // 4 deg; morning end, evening start

        public static boolean shortDisplayStrings = false;
        private String longDisplayString;
        private String shortDisplayString;

        private TimeMode(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public String toString()
        {
            if (shortDisplayStrings)
            {
                return shortDisplayString;

            } else {
                return longDisplayString;
            }
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayStrings(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            OFFICIAL.setDisplayStrings( context.getString(R.string.timeMode_official_short),
                    context.getString(R.string.timeMode_official) );

            NAUTICAL.setDisplayStrings( context.getString(R.string.timeMode_nautical_short),
                    context.getString(R.string.timeMode_nautical));

            CIVIL.setDisplayStrings( context.getString(R.string.timeMode_civil_short),
                    context.getString(R.string.timeMode_civil) );

            ASTRONOMICAL.setDisplayStrings( context.getString(R.string.timeMode_astronomical_short),
                    context.getString(R.string.timeMode_astronomical) );

            NOON.setDisplayStrings( context.getString(R.string.timeMode_noon_short),
                    context.getString(R.string.timeMode_noon) );

            GOLD.setDisplayStrings( context.getString(R.string.timeMode_golden_short),
                    context.getString(R.string.timeMode_golden) );

            BLUE8.setDisplayStrings( context.getString(R.string.timeMode_blue8_short),
                    context.getString(R.string.timeMode_blue8) );

            BLUE4.setDisplayStrings( context.getString(R.string.timeMode_blue4_short),
                    context.getString(R.string.timeMode_blue4) );
        }
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
        calculatorName = calculatorName.toLowerCase().trim();
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
        prefs.putString(key, mode.name());
        prefs.apply();
    }

    public static String defaultCalculatorModePref(Context context, int appWidgetId, @NonNull String calculatorName)
    {
        calculatorName = calculatorName.toLowerCase().trim();
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

    public static void saveTimeModePref(Context context, int appWidgetId, WidgetSettings.TimeMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.TimeMode loadTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE, PREF_DEF_GENERAL_TIMEMODE.name());

        TimeMode timeMode;
        try
        {
            timeMode = WidgetSettings.TimeMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timeMode = PREF_DEF_GENERAL_TIMEMODE;
        }
        return timeMode;
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
    public static WidgetSettings.SolsticeEquinoxMode loadTimeMode2Pref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2, PREF_DEF_GENERAL_TIMEMODE2.name());

        SolsticeEquinoxMode timeMode;
        try
        {
            timeMode = WidgetSettings.SolsticeEquinoxMode.valueOf(modeString);

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
    public static WidgetSettings.MoonPhaseMode loadTimeMode3Pref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMEMODE2, PREF_DEF_GENERAL_TIMEMODE3.name());

        MoonPhaseMode timeMode;
        try
        {
            timeMode = WidgetSettings.MoonPhaseMode.valueOf(modeString);

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

    public static void saveSolarTimeModePref(Context context, int appWidgetId, WidgetSettings.SolarTimeMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_SOLARMODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.SolarTimeMode loadSolarTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_SOLARMODE, PREF_DEF_TIMEZONE_SOLARMODE.name());

        SolarTimeMode timeMode;
        try
        {
            timeMode = WidgetSettings.SolarTimeMode.valueOf(modeString);

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

    public static void saveTimeFormatModePref(Context context, int appWidgetId, WidgetSettings.TimeFormatMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_TIMEFORMATMODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.TimeFormatMode loadTimeFormatModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_TIMEFORMATMODE, PREF_DEF_APPEARANCE_TIMEFORMATMODE.name());

        TimeFormatMode formatMode;
        try
        {
            formatMode = WidgetSettings.TimeFormatMode.valueOf(modeString);

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

    public static void saveActionLaunchPref(Context context, int appWidgetId, String launchString)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        prefs.putString(prefs_prefix + PREF_KEY_ACTION_LAUNCH, launchString);
        prefs.apply();
    }
    public static String loadActionLaunchPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        //noinspection UnnecessaryLocalVariable
        String launchString = prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH, PREF_DEF_ACTION_LAUNCH);
        return launchString;

    }
    public static void deleteActionLaunchPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION;
        prefs.remove(prefs_prefix + PREF_KEY_ACTION_LAUNCH);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveLocationModePref(Context context, int appWidgetId, WidgetSettings.LocationMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_MODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.LocationMode loadLocationModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_MODE, PREF_DEF_LOCATION_MODE.name());

        LocationMode locationMode;
        try
        {
            locationMode = WidgetSettings.LocationMode.valueOf(modeString);

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

    public static void saveDateModePref(Context context, int appWidgetId, WidgetSettings.DateMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        prefs.putString(prefs_prefix + PREF_KEY_DATE_MODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.DateMode loadDateModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_DATE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_DATE_MODE, PREF_DEF_DATE_MODE.name());

        DateMode dateMode;
        try
        {
            dateMode = WidgetSettings.DateMode.valueOf(modeString);

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
    public static WidgetSettings.DateInfo loadDatePref(Context context, int appWidgetId)
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

    public static void saveTimezoneModePref(Context context, int appWidgetId, WidgetSettings.TimezoneMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.TimezoneMode loadTimezoneModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, PREF_DEF_TIMEZONE_MODE.name());

        TimezoneMode timezoneMode;
        try
        {
            timezoneMode = WidgetSettings.TimezoneMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timezoneMode = PREF_DEF_TIMEZONE_MODE;
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
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        String altString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_ALTITUDE, PREF_DEF_LOCATION_ALTITUDE);
        String lonString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE, PREF_DEF_LOCATION_LONGITUDE);
        String latString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LATITUDE, PREF_DEF_LOCATION_LATITUDE);
        String nameString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LABEL, PREF_DEF_LOCATION_LABEL);
        return new Location(nameString, latString, lonString, altString);

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

    public static void saveTimezonePref(Context context, int appWidgetId, String timezone)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM, timezone);
        prefs.apply();
    }
    public static String loadTimezonePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        return prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM, PREF_DEF_TIMEZONE_CUSTOM);
    }
    public static void deleteTimezonePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM);
        prefs.apply();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveTrackingModePref(Context context, int appWidgetId, WidgetSettings.TrackingMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TRACKINGMODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.TrackingMode loadTrackingModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TRACKINGMODE, PREF_DEF_GENERAL_TRACKINGMODE.name());

        TrackingMode trackingMode;
        try
        {
            trackingMode = WidgetSettings.TrackingMode.valueOf(modeString);

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

    public static void saveCompareModePref(Context context, int appWidgetId, WidgetSettings.CompareMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_COMPAREMODE, mode.name());
        prefs.apply();
    }
    public static WidgetSettings.CompareMode loadCompareModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_COMPAREMODE, PREF_DEF_GENERAL_COMPAREMODE.name());

        CompareMode compareMode;
        try
        {
            compareMode = WidgetSettings.CompareMode.valueOf(modeString);

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
    public static boolean loadShowNoonPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_GENERAL_SHOWNOON, PREF_DEF_GENERAL_SHOWNOON);
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


    public static void saveTimeNoteRisePref(Context context, int appWidgetId, SolarEvents riseChoice)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_RISE, riseChoice.name());
        prefs.apply();
    }
    public static SolarEvents loadTimeNoteRisePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_RISE, PREF_DEF_GENERAL_TIMENOTE_RISE.name());

        SolarEvents riseMode;
        try {
            riseMode = SolarEvents.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            riseMode = PREF_DEF_GENERAL_TIMENOTE_RISE;
        }
        return riseMode;
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

    public static void saveTimeNoteSetPref(Context context, int appWidgetId, SolarEvents setChoice)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_SET, setChoice.name());
        prefs.apply();
    }
    public static SolarEvents loadTimeNoteSetPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_TIMENOTE_SET, PREF_DEF_GENERAL_TIMENOTE_SET.name());

        SolarEvents setMode;
        try {
            setMode = SolarEvents.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            setMode = PREF_DEF_GENERAL_TIMENOTE_SET;
        }
        return setMode;
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
        deleteActionModePref(context, appWidgetId);
        deleteActionLaunchPref(context, appWidgetId);

        deleteSun1x1ModePref(context, appWidgetId);
        deleteSunPos1x1ModePref(context, appWidgetId);
        deleteMoon1x1ModePref(context, appWidgetId);
        deleteAllowResizePref(context, appWidgetId);

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

        deleteCompareModePref(context, appWidgetId);
        deleteShowComparePref(context, appWidgetId);
        deleteShowNoonPref(context, appWidgetId);
        deleteShowWeeksPref(context, appWidgetId);
        deleteShowHoursPref(context, appWidgetId);
        deleteShowSecondsPref(context, appWidgetId);
        deleteShowTimeDatePref(context, appWidgetId);

        deleteObserverHeightPref(context, appWidgetId);

        deleteLocationModePref(context, appWidgetId);
        deleteLocationPref(context, appWidgetId);

        deleteTimezoneModePref(context, appWidgetId);
        deleteSolarTimeModePref(context, appWidgetId);
        deleteTimezonePref(context, appWidgetId);

        deleteDateModePref(context, appWidgetId);
        deleteDatePref(context, appWidgetId);

        deleteTimeNoteRisePref(context, appWidgetId);
        deleteTimeNoteSetPref(context, appWidgetId);
    }

    public static void initDefaults( Context context )
    {
        PREF_DEF_LOCATION_LABEL = context.getString(R.string.default_location_label);
        PREF_DEF_LOCATION_LATITUDE = context.getString(R.string.default_location_latitude);
        PREF_DEF_LOCATION_LONGITUDE = context.getString(R.string.default_location_longitude);
    }

    public static void initDisplayStrings( Context context )
    {
        ActionMode.initDisplayStrings(context);
        WidgetModeSun1x1.initDisplayStrings(context);
        WidgetModeSunPos1x1.initDisplayStrings(context);
        WidgetModeMoon1x1.initDisplayStrings(context);
        TrackingMode.initDisplayStrings(context);
        CompareMode.initDisplayStrings(context);
        TimeMode.initDisplayStrings(context);
        MoonPhaseMode.initDisplayStrings(context);
        SolsticeEquinoxMode.initDisplayStrings(context);
        LocationMode.initDisplayStrings(context);
        TimezoneMode.initDisplayStrings(context);
        SolarTimeMode.initDisplayStrings(context);
        DateMode.initDisplayStrings(context);
        TimeFormatMode.initDisplayStrings(context);
    }
}
