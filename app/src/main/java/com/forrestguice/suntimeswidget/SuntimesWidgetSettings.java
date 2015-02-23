package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.SharedPreferences;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class SuntimesWidgetSettings
{
    /**
     * TimezoneMode
     */
    public static enum TimezoneMode
    {
        CURRENT_TIMEZONE,
        CUSTOM_TIMEZONE;
    }

    /**
     * LocationMode
     */
    public static enum LocationMode
    {
        CURRENT_LOCATION,
        CUSTOM_LOCATION;
    }

    /**
     * TimeMode
     */
    public static enum TimeMode
    {
        OFFICIAL("Actual Time"),
        NAUTICAL("Nautical Twilight"),
        CIVIL("Civil Twilight"),
        ASTRONOMICAL("Astronomical Twilight");

        private String displayString;

        private TimeMode(String displayString)
        {
            this.displayString = displayString;
        }

        public String getDisplayString()
        {
            return displayString;
        }

    }

    /**
     *
     */
    private static final String PREFS_NAME = "com.forrestguice.suntimeswidget.SuntimesWidget";

    public static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_PREFIX_KEY_APPEARANCE = "_appearance_";
    public static final String PREF_PREFIX_KEY_GENERAL = "_general_";
    public static final String PREF_PREFIX_KEY_LOCATION = "_location_";
    public static final String PREF_PREFIX_KEY_TIMEZONE = "_timezone_";

    public static final String PREF_KEY_APPEARANCE_SHOWTITLE = "showtitle";
    public static final boolean PREF_DEF_APPEARANCE_SHOWTITLE = false;

    public static final String PREF_KEY_GENERAL_MODE = "timemode";
    public static final TimeMode PREF_DEF_GENERAL_MODE = TimeMode.OFFICIAL;

    public static final String PREF_KEY_LOCATION_MODE = "locationMode";
    public static final LocationMode PREF_DEF_LOCATION_MODE = LocationMode.CUSTOM_LOCATION;

    public static final String PREF_KEY_LOCATION_LONGITUDE = "longitude";
    public static final String PREF_DEF_LOCATION_LONGITUDE = "-112.4677778";

    public static final String PREF_KEY_LOCATION_LATITUDE = "latitude";
    public static final String PREF_DEF_LOCATION_LATITUDE = "34.54";

    public static final String PREF_KEY_TIMEZONE_MODE = "timezoneMode";
    public static final TimezoneMode PREF_DEF_TIMEZONE_MODE = TimezoneMode.CUSTOM_TIMEZONE;

    public static final String PREF_KEY_TIMEZONE_CUSTOM = "timezone";
    public static final String PREF_DEF_TIMEZONE_CUSTOM = "US/Arizona";


    static void saveShowTitlePref(Context context, int appWidgetId, boolean showTitle)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE, showTitle);
        prefs.commit();
    }
    static boolean loadShowTitlePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getBoolean(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE, PREF_DEF_APPEARANCE_SHOWTITLE);
    }
    static void deleteShowTitlePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_SHOWTITLE);
        prefs.commit();
    }


    static void saveTimeModePref(Context context, int appWidgetId, SuntimesWidgetSettings.TimeMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.putString(prefs_prefix + PREF_KEY_GENERAL_MODE, mode.name());
        prefs.commit();
    }
    static SuntimesWidgetSettings.TimeMode loadTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_GENERAL_MODE, PREF_DEF_GENERAL_MODE.name());

        TimeMode timeMode;
        try
        {
            timeMode = SuntimesWidgetSettings.TimeMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timeMode = PREF_DEF_GENERAL_MODE;
        }
        return timeMode;
    }
    static void deleteTimeModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_GENERAL;
        prefs.remove(prefs_prefix + PREF_KEY_GENERAL_MODE);
        prefs.commit();
    }


    static void saveLocationModePref(Context context, int appWidgetId, SuntimesWidgetSettings.LocationMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_MODE, mode.name());
        prefs.commit();
    }
    static SuntimesWidgetSettings.LocationMode loadLocationModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_MODE, PREF_DEF_LOCATION_MODE.name());

        LocationMode locationMode;
        try
        {
            locationMode = SuntimesWidgetSettings.LocationMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            locationMode = PREF_DEF_LOCATION_MODE;
        }
        return locationMode;
    }
    static void deleteLocationModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_MODE);
        prefs.commit();
    }


    static void saveTimezoneModePref(Context context, int appWidgetId, SuntimesWidgetSettings.TimezoneMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, mode.name());
        prefs.commit();
    }
    static SuntimesWidgetSettings.TimezoneMode loadTimezoneModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_MODE, PREF_DEF_TIMEZONE_MODE.name());

        TimezoneMode timezoneMode;
        try
        {
            timezoneMode = SuntimesWidgetSettings.TimezoneMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            timezoneMode = PREF_DEF_TIMEZONE_MODE;
        }
        return timezoneMode;
    }
    static void deleteTimezoneModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_MODE);
        prefs.commit();
    }


    static void saveLocationPref(Context context, int appWidgetId, Location location)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE, location.getLongitude().toPlainString());
        prefs.putString(prefs_prefix + PREF_KEY_LOCATION_LATITUDE, location.getLatitude().toPlainString());
        prefs.commit();
    }
    static Location loadLocationPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        String lonString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE, PREF_DEF_LOCATION_LONGITUDE);
        String latString = prefs.getString(prefs_prefix + PREF_KEY_LOCATION_LATITUDE, PREF_DEF_LOCATION_LATITUDE);
        return new Location(latString, lonString);
    }
    static void deleteLocationPref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_LOCATION;
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_LONGITUDE);
        prefs.remove(prefs_prefix + PREF_KEY_LOCATION_LATITUDE);
        prefs.commit();
    }


    static void saveTimezonePref(Context context, int appWidgetId, String timezone)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.putString(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM, timezone);
        prefs.commit();
    }
    static String loadTimezonePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        return prefs.getString(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM, PREF_DEF_TIMEZONE_CUSTOM);
    }
    static void deleteTimezonePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_TIMEZONE;
        prefs.remove(prefs_prefix + PREF_KEY_TIMEZONE_CUSTOM);
        prefs.commit();
    }


    static void deletePrefs(Context context, int appWidgetId)
    {
        deleteShowTitlePref(context, appWidgetId);

        deleteTimeModePref(context, appWidgetId);

        deleteLocationModePref(context, appWidgetId);
        deleteLocationPref(context, appWidgetId);

        deleteTimezoneModePref(context, appWidgetId);
        deleteTimezonePref(context, appWidgetId);
    }

}
