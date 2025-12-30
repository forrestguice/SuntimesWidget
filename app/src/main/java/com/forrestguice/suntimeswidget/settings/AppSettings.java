/**
    Copyright (C) 2014-2024 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.getfix.LocationHelperSettings;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.util.prefs.PrefTypeInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared preferences used by the app; uses getDefaultSharedPreferences (stored in com.forrestguice.suntimeswidget_preferences.xml).
 */
public class AppSettings
{
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DAYNIGHT = "daynight";
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_SYSTEM1 = "contrast_system";
    public static final String THEME_DARK1 = "contrast_dark";
    public static final String THEME_LIGHT1 = "contrast_light";

    public static final String THEME_DEFAULT = "default";
    public static final String[] THEMES = new String[] { THEME_DEFAULT, THEME_DARK, THEME_LIGHT, THEME_DAYNIGHT, THEME_SYSTEM, THEME_SYSTEM1, THEME_DARK1, THEME_LIGHT1 };

    public static final String PREF_KEY_APPEARANCE_THEME = "app_appearance_theme";
    // public static final String PREF_DEF_APPEARANCE_THEME = THEME_SYSTEM;    // @see R.string.def_app_appearance_theme

    public static final String PREF_KEY_APPEARANCE_THEME_LIGHT = "app_appearance_theme_light";
    public static final String PREF_DEF_APPEARANCE_THEME_LIGHT = THEME_DEFAULT;

    public static final String PREF_KEY_APPEARANCE_THEME_DARK = "app_appearance_theme_dark";
    public static final String PREF_DEF_APPEARANCE_THEME_DARK = THEME_DEFAULT;

    public static final String PREF_KEY_APPEARANCE_TEXTSIZE = "app_appearance_textsize";
    public static final TextSize PREF_DEF_APPEARANCE_TEXTSIZE = TextSize.NORMAL;

    public static final String PREF_KEY_LOCALE_MODE = "app_locale_mode";
    public static final LocaleMode PREF_DEF_LOCALE_MODE = LocaleMode.SYSTEM_LOCALE;

    public static final String PREF_KEY_LOCALE = "app_locale";
    public static final String PREF_DEF_LOCALE = "en";

    public static final String PREF_KEY_LAUNCHER_MODE = "app_launcher_mode";
    public static final String PREF_DEF_LAUNCHER_MODE = "default";

    public static final String NAVIGATION_SIMPLE = "SIMPLE";
    public static final String NAVIGATION_SIDEBAR = "SIDEBAR";
    public static final String PREF_KEY_NAVIGATION_MODE = "app_navigation_mode";

    public static final String PREF_KEY_UI_DATETAPACTION = "app_ui_datetapaction";
    public static final String PREF_DEF_UI_DATETAPACTION = WidgetActions.SuntimesAction.SWAP_CARD.name();

    public static final String PREF_KEY_UI_DATETAPACTION1 = "app_ui_datetapaction1";
    public static final String PREF_DEF_UI_DATETAPACTION1 = WidgetActions.SuntimesAction.SHOW_CALENDAR.name();

    public static final String PREF_KEY_UI_CLOCKTAPACTION = "app_ui_clocktapaction";
    public static final String PREF_DEF_UI_CLOCKTAPACTION = WidgetActions.SuntimesAction.RESET_NOTE.name();

    public static final String PREF_KEY_UI_NOTETAPACTION = "app_ui_notetapaction";
    public static final String PREF_DEF_UI_NOTETAPACTION = WidgetActions.SuntimesAction.NEXT_NOTE.name();

    public static final String PREF_KEY_UI_SHOWWARNINGS = "app_ui_showwarnings";
    public static final boolean PREF_DEF_UI_SHOWWARNINGS = true;

    public static final String PREF_KEY_UI_SHOWLIGHTMAP = "app_ui_showlightmap";
    public static final boolean PREF_DEF_UI_SHOWLIGHTMAP = true;

    public static final String PREF_KEY_UI_SHOWEQUINOX = "app_ui_showequinox";
    public static final boolean PREF_DEF_UI_SHOWEQUINOX = true;

    public static final String PREF_KEY_UI_SHOWEQUINOXDATE = "app_ui_showequinox_date";

    public static final String PREF_KEY_UI_SHOWCROSSQUARTER = "app_ui_showcrossquarter";
    public static final boolean PREF_DEF_UI_SHOWCROSSQUARTER = true;

    public static final String PREF_KEY_UI_SHOWMOON = "app_ui_showmoon";
    public static final boolean PREF_DEF_UI_SHOWMOON = true;

    public static final String PREF_KEY_UI_SHOWLUNARNOON = "app_ui_showmoon_noon";
    public static final boolean PREF_DEF_UI_SHOWLUNARNOON = false;

    public static final String PREF_KEY_UI_MOONPHASECOLUMNS = "app_ui_showmoon_phases_columns";
    public static final int PREF_DEF_UI_MOONPHASECOLUMNS = 4;

    public static final String PREF_KEY_UI_MOONPHASE_SHOWDATE = "app_ui_showmoon_phases_showdate";

    public static final String PREF_KEY_UI_SHOWMAPBUTTON = "app_ui_showmapbutton";
    public static final boolean PREF_DEF_UI_SHOWMAPBUTTON = true;

    public static final String PREF_KEY_UI_SHOWCOORDINATES = "app_ui_showcoordinates";

    public static final String PREF_KEY_UI_SHOWDATASOURCE = "app_ui_showdatasource";
    public static final boolean PREF_DEF_UI_SHOWDATASOURCE = true;

    public static final String PREF_KEY_UI_SHOWHEADER_ICON = "app_ui_showheader_icon";
    public static final boolean PREF_DEF_UI_SHOWHEADER_ICON = true;

    public static final int HEADER_TEXT_NONE = 0;
    public static final int HEADER_TEXT_LABEL = 1;         // sunrise/sunset
    public static final int HEADER_TEXT_LABEL_ALT = 10;    // dawn/dusk
    public static final int HEADER_TEXT_AZIMUTH = 2;

    public static final String PREF_KEY_UI_SHOWHEADER_TEXT = "app_ui_showheader_text1";
    public static final int PREF_DEF_UI_SHOWHEADER_TEXT = HEADER_TEXT_LABEL;

    public static final String PREF_KEY_UI_EMPHASIZEFIELD = "app_ui_emphasizefield";

    public static final String PREF_KEY_UI_SHOWFIELDS = "app_ui_showfields";
    public static final byte PREF_DEF_UI_SHOWFIELDS = 0b000010011;
    public static final int FIELD_ACTUAL = 0;  // bit positions
    public static final int FIELD_CIVIL = 1;
    public static final int FIELD_NAUTICAL = 2;
    public static final int FIELD_ASTRO = 3;
    public static final int FIELD_NOON = 4;
    public static final int FIELD_GOLD = 5;
    public static final int FIELD_BLUE = 6;
    public static final int FIELD_MIDNIGHT = 7;
    public static final int NUM_FIELDS = 8;

    public static final String PREF_KEY_ACCESSIBILITY_VERBOSE = "app_accessibility_verbose";
    public static final boolean PREF_DEF_ACCESSIBILITY_VERBOSE = false;

    public static final String PREF_KEY_UI_TIMEZONESORT = "app_ui_timezonesort";
    public static final WidgetTimezones.TimeZoneSort PREF_DEF_UI_TIMEZONESORT = WidgetTimezones.TimeZoneSort.SORT_BY_ID;

    public static final String PREF_KEY_GETFIX_MINELAPSED = LocationHelperSettings.PREF_KEY_LOCATION_MIN_ELAPSED;
    public static final String PREF_KEY_GETFIX_MAXELAPSED = LocationHelperSettings.PREF_KEY_LOCATION_MAX_ELAPSED;
    public static final String PREF_KEY_GETFIX_MAXAGE = LocationHelperSettings.PREF_KEY_LOCATION_MAX_AGE;
    public static final String PREF_KEY_GETFIX_TIME = LocationHelperSettings.PREF_KEY_LOCATION_LAST_TIME;
    public static final String PREF_KEY_GETFIX_PASSIVE = LocationHelperSettings.PREF_KEY_LOCATION_PASSIVE;

    public static final String PREF_KEY_PLUGINS_ENABLESCAN = "app_plugins_enabled";
    public static final boolean PREF_DEF_PLUGINS_ENABLESCAN = false;

    public static final String PREF_KEY_FIRST_LAUNCH = "app_first_launch";
    public static boolean isFirstLaunch( Context context ) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_KEY_FIRST_LAUNCH, true);
    }
    public static void setFirstLaunch( Context context, boolean value ) {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_FIRST_LAUNCH, value).apply();
    }

    public static final String PREF_KEY_DIALOG = "dialog";
    public static final String PREF_KEY_DIALOG_DONOTSHOWAGAIN = "donotshowagain";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[]
    {
            PREF_KEY_APPEARANCE_THEME, PREF_KEY_APPEARANCE_THEME_LIGHT, PREF_KEY_APPEARANCE_THEME_DARK, PREF_KEY_APPEARANCE_TEXTSIZE,
            PREF_KEY_LOCALE_MODE, PREF_KEY_LOCALE, PREF_KEY_UI_SHOWWARNINGS,
            PREF_KEY_UI_DATETAPACTION, PREF_KEY_UI_DATETAPACTION1, PREF_KEY_UI_CLOCKTAPACTION, PREF_KEY_UI_NOTETAPACTION,
            PREF_KEY_UI_SHOWLIGHTMAP, PREF_KEY_UI_SHOWEQUINOX, PREF_KEY_UI_SHOWEQUINOXDATE, PREF_KEY_UI_SHOWCROSSQUARTER, PREF_KEY_UI_SHOWMOON, PREF_KEY_UI_SHOWLUNARNOON,
            PREF_KEY_UI_SHOWMAPBUTTON, PREF_KEY_UI_SHOWDATASOURCE, PREF_KEY_UI_SHOWHEADER_ICON, PREF_KEY_UI_SHOWHEADER_TEXT, PREF_KEY_UI_SHOWCOORDINATES,
            PREF_KEY_UI_EMPHASIZEFIELD, PREF_KEY_UI_SHOWFIELDS, PREF_KEY_ACCESSIBILITY_VERBOSE, PREF_KEY_UI_TIMEZONESORT,
            PREF_KEY_UI_MOONPHASECOLUMNS, PREF_KEY_UI_MOONPHASE_SHOWDATE,
            PREF_KEY_GETFIX_MINELAPSED, PREF_KEY_GETFIX_MAXELAPSED, PREF_KEY_GETFIX_MAXAGE, PREF_KEY_GETFIX_PASSIVE,
            PREF_KEY_PLUGINS_ENABLESCAN, PREF_KEY_FIRST_LAUNCH, PREF_KEY_DIALOG, PREF_KEY_DIALOG_DONOTSHOWAGAIN,
            //PREF_KEY_GETFIX_TIME,
            PREF_KEY_NAVIGATION_MODE, PREF_KEY_LAUNCHER_MODE
    };
    public static final String[] INT_KEYS = new String[] {
            PREF_KEY_UI_SHOWFIELDS,
            PREF_KEY_UI_MOONPHASECOLUMNS
    };
    public static final String[] LONG_KEYS = new String[] {
            //PREF_KEY_GETFIX_TIME    // commented; TODO: does it actually make sense to preserve this value across installations? #783
    };
    public static final String[] BOOL_KEYS = new String[]
    {
            PREF_KEY_UI_SHOWWARNINGS, PREF_KEY_UI_SHOWMAPBUTTON, PREF_KEY_UI_SHOWDATASOURCE, PREF_KEY_UI_SHOWHEADER_ICON, PREF_KEY_UI_SHOWCOORDINATES,
            PREF_KEY_UI_SHOWLIGHTMAP, PREF_KEY_UI_SHOWEQUINOX, PREF_KEY_UI_SHOWEQUINOXDATE, PREF_KEY_UI_SHOWCROSSQUARTER,
            PREF_KEY_UI_SHOWMOON, PREF_KEY_UI_SHOWLUNARNOON, PREF_KEY_UI_MOONPHASE_SHOWDATE,
            PREF_KEY_ACCESSIBILITY_VERBOSE, PREF_KEY_GETFIX_PASSIVE, PREF_KEY_PLUGINS_ENABLESCAN, PREF_KEY_FIRST_LAUNCH, PREF_KEY_DIALOG_DONOTSHOWAGAIN
    };

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo()
        {
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
                return new String[0];
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
            for (String key : INT_KEYS) {
                types.put(key, Integer.class);
            }
            for (String key : LONG_KEYS) {
                types.put(key, Long.class);
            }
            for (String key : BOOL_KEYS) {
                types.put(key, Boolean.class);
            }

            types.put(PREF_KEY_UI_SHOWHEADER_TEXT, String.class);   // int as String
            types.put(PREF_KEY_GETFIX_MINELAPSED, String.class);    // int as String
            types.put(PREF_KEY_GETFIX_MAXELAPSED, String.class);    // int as String
            types.put(PREF_KEY_GETFIX_MAXAGE, String.class);        // int as String

            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Text sizes
     */
    public static enum TextSize
    {
        SMALL("Small"),
        NORMAL("Normal"),
        LARGE("Large"),
        XLARGE("Extra Large");

        private TextSize( String displayString ) {
            this.displayString = displayString;
        }

        public String getDisplayString() {
            return displayString;
        }
        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }
        private String displayString;

        public static void initDisplayStrings( Context context )
        {
            SMALL.setDisplayString(context.getString(R.string.textSize_small));
            NORMAL.setDisplayString(context.getString(R.string.textSize_normal));
            LARGE.setDisplayString(context.getString(R.string.textSize_large));
            XLARGE.setDisplayString(context.getString(R.string.textSize_xlarge));
        }

        public static TextSize valueOf(String value, TextSize defaultValue)
        {
            try {
                return TextSize.valueOf(value);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        }
    }

    /**
     * Language modes (system, user defined)
     */
    public static enum LocaleMode
    {
        SYSTEM_LOCALE("System Locale"),
        CUSTOM_LOCALE("Custom Locale");

        private String displayString;

        private LocaleMode( String displayString )
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
            String[] labels = context.getResources().getStringArray(R.array.localeMode_display);
            SYSTEM_LOCALE.setDisplayString(labels[0]);
            CUSTOM_LOCALE.setDisplayString(labels[1]);
        }
    }

    /**
     * Preference: locale mode
     */
    public static LocaleMode loadLocaleModePref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return loadLocaleModePref(pref);
    }

    public static LocaleMode loadLocaleModePref( SharedPreferences pref )
    {
        String modeString = pref.getString(PREF_KEY_LOCALE_MODE, PREF_DEF_LOCALE_MODE.name());

        LocaleMode localeMode;
        try {
            localeMode = LocaleMode.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            localeMode = PREF_DEF_LOCALE_MODE;
        }
        return localeMode;
    }

    /**
     * Preference: custom locale
     */
    public static String loadLocalePref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_LOCALE, PREF_DEF_LOCALE);
    }

    /**
     * @return true if locale was changed by init, false otherwise
     */
    public static Context initLocale( Context context)
    {
        return initLocale(context, new LocaleInfo());
    }
    public static Context initLocale( Context context, LocaleInfo resultInfo )
    {
        resultInfo.localeMode = AppSettings.loadLocaleModePref(context);
        if (resultInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE)
        {
            resultInfo.customLocale = AppSettings.loadLocalePref(context);
            return AppSettings.loadLocale(context, resultInfo.customLocale);

        } else {
            return resetLocale(context);
        }
    }
    public static class LocaleInfo
    {
        public LocaleMode localeMode;
        public String customLocale;
    }

    /**
     * @return true if the locale was changed by reset, false otherwise
     */
    public static Context resetLocale( Context context )
    {
        if (systemLocale != null)
        {
            //Log.d("resetLocale", "locale reset to " + systemLocale);
            return loadLocale(context, systemLocale);
        }
        return context;
    }

    private static String systemLocale = null;  // null until locale is overridden w/ loadLocale
    public static String getSystemLocale()
    {
        if (systemLocale == null)
        {
            systemLocale = Locale.getDefault().getLanguage();
        }
        return systemLocale;
    }
    public static Locale getLocale()
    {
        return Locale.getDefault();
    }

    public static Context loadLocale( Context context, String languageTag )
    {
        if (systemLocale == null) {
            systemLocale = Locale.getDefault().getLanguage();
        }

        Locale customLocale = localeForLanguageTag(languageTag);
        Locale.setDefault(customLocale);
        Log.i("loadLocale", languageTag);

        Resources resources = context.getApplicationContext().getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= 17)
            config.setLocale(customLocale);
        else config.locale = customLocale;

        if (Build.VERSION.SDK_INT >= 25) {
            return new ContextWrapper(context.createConfigurationContext(config));

        } else {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            //noinspection deprecation
            resources.updateConfiguration(config, metrics);
            return new ContextWrapper(context);
        }
    }

    private static @NonNull
    Locale localeForLanguageTag(@NonNull String languageTag)
    {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            locale = Locale.forLanguageTag(languageTag.replaceAll("_", "-"));

        } else {
            String[] parts = languageTag.split("[_]");
            String language = parts[0];
            String country = (parts.length >= 2) ? parts[1] : null;
            locale = (country != null) ? new Locale(language, country) : new Locale(language);
        }
        Log.d("localeForLanguageTag", "tag: " + languageTag + " :: locale: " + locale.toString());
        return locale;
    }

    /**
     * Is the current locale right-to-left?
     * @param context a context used to access resources
     * @return true the locale is right-to-left, false the locale is left-to-right
     */
    public static boolean isLocaleRtl(Context context)
    {
        return context.getResources().getBoolean(R.bool.is_rtl);
    }

    /**
     * @param context Context
     * @return launcher class name
     */
    @NonNull
    public static String loadLauncherModePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_LAUNCHER_MODE, PREF_DEF_LAUNCHER_MODE);
    }

    @NonNull
    public static String loadNavModePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_NAVIGATION_MODE, context.getString(R.string.def_app_navigation_mode));
    }

    /**
     * Is the current device a television? This implies limited features.
     */
    public static boolean isTelevision(@Nullable Context context)
    {
        if (context != null)
        {
            if (Build.VERSION.SDK_INT >= 21) {
                return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK);

            } else if (Build.VERSION.SDK_INT >= 13) {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
                return (uiModeManager != null && (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION));

            } else return false;
        } else return false;
    }

    public static void saveTimeZoneSortPref(Context context, WidgetTimezones.TimeZoneSort sortMode )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putString(PREF_KEY_UI_TIMEZONESORT, sortMode.name());
        pref.apply();
    }

    public static WidgetTimezones.TimeZoneSort loadTimeZoneSortPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String modeString = pref.getString(PREF_KEY_UI_TIMEZONESORT, PREF_DEF_UI_TIMEZONESORT.name());

        WidgetTimezones.TimeZoneSort sortMode;
        try {
            sortMode = WidgetTimezones.TimeZoneSort.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            sortMode = PREF_DEF_UI_TIMEZONESORT;
        }
        return sortMode;
    }

    public static boolean loadShowWarningsPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWWARNINGS, PREF_DEF_UI_SHOWWARNINGS);
    }

    public static boolean loadShowLightmapPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWLIGHTMAP, PREF_DEF_UI_SHOWLIGHTMAP);
    }

    public static boolean loadShowEquinoxPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWEQUINOX, PREF_DEF_UI_SHOWEQUINOX);
    }
    public static void saveShowEquinoxPref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_SHOWEQUINOX, value);
        pref.apply();
    }

    public static boolean loadShowEquinoxDatePref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWEQUINOXDATE, context.getResources().getBoolean(R.bool.def_app_ui_showequinox_date));
    }
    public static void saveShowEquinoxDatePref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_SHOWEQUINOXDATE, value);
        pref.apply();
    }

    public static boolean loadShowCrossQuarterPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWCROSSQUARTER, PREF_DEF_UI_SHOWCROSSQUARTER);
    }
    public static void saveShowCrossQuarterPref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_SHOWCROSSQUARTER, value);
        pref.apply();
    }

    public static boolean loadShowMoonPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWMOON, PREF_DEF_UI_SHOWMOON);
    }
    public static void saveShowMoonPref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_SHOWMOON, value);
        pref.apply();
    }

    public static boolean loadShowLunarNoonPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWLUNARNOON, PREF_DEF_UI_SHOWLUNARNOON);
    }
    public static void saveShowLunarNoonPref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_SHOWLUNARNOON, value);
        pref.apply();
    }

    public static int loadMoonPhaseColumnsPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(PREF_KEY_UI_MOONPHASECOLUMNS, PREF_DEF_UI_MOONPHASECOLUMNS);
    }
    public static void saveMoonPhaseColumnsPref( Context context, int value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putInt(PREF_KEY_UI_MOONPHASECOLUMNS, value);
        pref.apply();
    }

    public static boolean loadShowMoonPhaseDatePref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_MOONPHASE_SHOWDATE, context.getResources().getBoolean(R.bool.def_app_ui_showmoon_phases_showdate));
    }
    public static void saveShowMoonPhaseDatePref( Context context, boolean value )
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_UI_MOONPHASE_SHOWDATE, value);
        pref.apply();
    }

    public static boolean loadShowHeaderIconPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWHEADER_ICON, PREF_DEF_UI_SHOWHEADER_ICON);
    }

    public static int loadShowHeaderTextPref( Context context )
    {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            return Integer.parseInt(pref.getString(PREF_KEY_UI_SHOWHEADER_TEXT, "" + PREF_DEF_UI_SHOWHEADER_TEXT));
        } catch (NumberFormatException | ClassCastException e) {
            return PREF_DEF_UI_SHOWHEADER_TEXT;
        }
    }

    public static String loadEmphasizeFieldPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_UI_EMPHASIZEFIELD, context.getString(R.string.def_app_ui_emphasizefield));
    }

    public static boolean loadDatasourceUIPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWDATASOURCE, PREF_DEF_UI_SHOWDATASOURCE);
    }

    public static boolean loadShowMapButtonPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWMAPBUTTON, PREF_DEF_UI_SHOWMAPBUTTON);
    }

    public static boolean loadShowCoordinatesPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWCOORDINATES, context.getResources().getBoolean(R.bool.def_app_ui_showcoordinates));
    }

    public static boolean[] loadShowFieldsPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return loadShowFields(pref.getInt(PREF_KEY_UI_SHOWFIELDS, PREF_DEF_UI_SHOWFIELDS));
    }
    public static boolean[] loadShowFields( int showFields )
    {
        boolean[] retValue = new boolean[8];
        for (int i=0; i<retValue.length; i++)
        {
            retValue[i] = (((showFields >> i) & 1) == 1);
        }
        return retValue;
    }

    public static void saveShowFieldsPref( Context context, int k, boolean value )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int showFields = pref.getInt(PREF_KEY_UI_SHOWFIELDS, PREF_DEF_UI_SHOWFIELDS);

        if (value)
            showFields |= (1 << k);  // true; OR position k to 1
        else showFields &= ~(1 << k);  // false; AND position k to 0

        SharedPreferences.Editor prefs = pref.edit();
        prefs.putInt(PREF_KEY_UI_SHOWFIELDS, showFields);
        prefs.apply();
    }

    public static boolean loadVerboseAccessibilityPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_ACCESSIBILITY_VERBOSE, PREF_DEF_ACCESSIBILITY_VERBOSE);
    }

    public static boolean loadScanForPluginsPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_PLUGINS_ENABLESCAN, PREF_DEF_PLUGINS_ENABLESCAN);
    }

    /**
     * Preference: the action that is performed when the clock ui is clicked/tapped
     */
    public static String loadClockTapActionPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_UI_CLOCKTAPACTION, PREF_DEF_UI_CLOCKTAPACTION);
    }

    /**
     * Preference: the action that is performed when the date field is clicked/tapped
     */
    public static String loadDateTapActionPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_UI_DATETAPACTION, PREF_DEF_UI_DATETAPACTION);
    }

    /**
     * Preference: the action that is performed when the date field is long-clicked
     */
    public static String loadDateTapAction1Pref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_UI_DATETAPACTION1, PREF_DEF_UI_DATETAPACTION1);
    }

    /**
     * Preference: the action that is performed when the note ui is clicked/tapped
     */
    public static String loadNoteTapActionPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_UI_NOTETAPACTION, PREF_DEF_UI_NOTETAPACTION);
    }

    /**
     * @param context an application context
     * @return an extended theme identifier; themeName_textSize
     */
    public static String loadThemePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AppThemeInfo.getExtendedThemeName(pref.getString(PREF_KEY_APPEARANCE_THEME, context.getString(R.string.def_app_appearance_theme)), loadTextSizePref(context));
    }

    public static void saveTextSizePref(Context context, TextSize value)
    {
        Log.d("DEBUG", "saveTextSizePref: " + value);
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putString(PREF_KEY_APPEARANCE_TEXTSIZE, value.name());
        pref.apply();
    }
    public static String loadTextSizePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_TEXTSIZE, PREF_DEF_APPEARANCE_TEXTSIZE.name());
    }

    public static void setThemePref(Context context, String themeID) {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putString(PREF_KEY_APPEARANCE_THEME, themeID);
        pref.apply();
    }

    public static String loadThemeLightPref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME_LIGHT, PREF_DEF_APPEARANCE_THEME_LIGHT);
    }
    public static void saveThemeLightPref(Context context, String themeID)
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putString(PREF_KEY_APPEARANCE_THEME_LIGHT, themeID);
        pref.apply();
    }

    public static String loadThemeDarkPref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME_DARK, PREF_DEF_APPEARANCE_THEME_DARK);
    }
    public static void saveThemeDarkPref(Context context, String themeID)
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putString(PREF_KEY_APPEARANCE_THEME_DARK, themeID);
        pref.apply();
    }

    public static int setTheme(Activity activity, String appTheme)
    {
        int themeResID = AppSettings.themePrefToStyleId(activity, appTheme, null);
        activity.setTheme(themeResID);
        AppCompatDelegate.setDefaultNightMode(loadThemeInfo(appTheme).getDefaultNightMode());
        return themeResID;
    }

    public static int loadTheme(Context context)
    {
        return themePrefToStyleId(context, loadThemePref(context), null);
    }
    public static int loadTheme(Context context, SuntimesRiseSetData data)
    {
        return themePrefToStyleId(context, loadThemePref(context), data);
    }

    public static int themePrefToStyleId( Context context, String themeName )
    {
        return themePrefToStyleId(context, themeName, null);
    }
    public static int themePrefToStyleId( Context context, String themeName, SuntimesRiseSetData data )
    {
        if (themeName != null) {
            AppThemeInfo themeInfo = loadThemeInfo(themeName);
            TextSize textSize = AppThemeInfo.getTextSize(themeName);

            String themeName1 = getThemeOverride(context, themeInfo);
            if (themeName1 != null) {
                AppThemeInfo themeInfo1 = loadThemeInfo(themeName1);
                return themeInfo1.getStyleId(context, textSize, data);

            } else return themeInfo.getStyleId(context, textSize, data);
        } else return R.style.AppTheme;
    }

    public static boolean systemInNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            return (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
        } else return false;
    }

    public static String getThemeOverride(Context context, String appTheme) {
        return getThemeOverride(context, loadThemeInfo(appTheme));
    }
    public static String getThemeOverride(Context context, AppThemeInfo themeInfo)
    {
        int nightMode = themeInfo.getDefaultNightMode();
        String override = (nightMode == AppCompatDelegate.MODE_NIGHT_NO) ? AppSettings.loadThemeLightPref(context)
                : (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ? AppSettings.loadThemeDarkPref(context)
                : (systemInNightMode(context) ? AppSettings.loadThemeDarkPref(context) : AppSettings.loadThemeLightPref(context));
        return ((override != null && !override.equals(THEME_DEFAULT)) ? override : null);
    }

    /**
     * @return true; dialog should not be shown (user has check 'do not show again')
     */
    public static boolean checkDialogDoNotShowAgain( Context context, String dialogKey ) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_DIALOG + "_" + dialogKey + "_" + PREF_KEY_DIALOG_DONOTSHOWAGAIN, false);
    }
    public static void setDialogDoNotShowAgain(Context context, String dialogKey, boolean value)
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putBoolean(PREF_KEY_DIALOG + "_" + dialogKey + "_" + PREF_KEY_DIALOG_DONOTSHOWAGAIN, value);
        pref.apply();
    }
    public static AlertDialog.Builder buildAlertDialog(final String key, @NonNull LayoutInflater inflater,
                                                       int iconResId, @Nullable String title, @NonNull String message, @Nullable final DialogInterface.OnClickListener onOkClicked)
    {
        final Context context = inflater.getContext();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.layout_dialog_alert, null);
        final CheckBox check_notagain = (CheckBox) dialogView.findViewById(R.id.check_donotshowagain);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setMessage(message)
                .setView(dialogView)
                .setIcon(iconResId)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (check_notagain != null) {
                            AppSettings.setDialogDoNotShowAgain(context, key, check_notagain.isChecked());
                        }
                        if (onOkClicked != null) {
                            onOkClicked.onClick(dialog, which);
                        }
                    }
                });
        return dialog;
    }

    /**
     * @param context a context used to access resources
     */
    public static void initDisplayStrings( Context context )
    {
        LocaleMode.initDisplayStrings(context);
        WidgetActions.SuntimesAction.initDisplayStrings(context);
    }

    /**
     * Verify that our custom permissions are not being held by some other app. Displays a dialog
     * warning the user of potential malicious behavior when duplicate permissions are found.
     *
     * This security issue is fixed in api21; apps with differing signatures are not allowed to
     * (re)define the same permission. However lower apis are still vulnerable to "permission squatting"
     * by potentially malicious apps (that may attempt to redefine a permission's definition by exploiting
     * the "first come first served" nature of custom permissions).
     *
     * @param context a Context
     */
    public static void checkCustomPermissions(@NonNull Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            long bench_start = System.nanoTime();

            int[] attrs = new int[] { R.attr.icActionWarning };
            TypedArray a = context.obtainStyledAttributes(attrs);
            int warningIcon = a.getResourceId(0, R.drawable.ic_action_warning);
            a.recycle();

            PackageManager packageManager = context.getPackageManager();
            String myPackageName = context.getPackageName();

            try {
                PackageInfo myPackageInfo = packageManager.getPackageInfo(myPackageName, PackageManager.GET_PERMISSIONS);
                HashMap<String, PermissionInfo> myPermissions = new HashMap<>();
                for (PermissionInfo permission : myPackageInfo.permissions) {
                    myPermissions.put(permission.name, permission);
                }

                for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS))
                {
                    if (packageInfo.packageName.equals(myPackageName) || packageInfo.permissions == null) {
                        continue;      // skip our entry.. and skip entries without any permissions
                    }

                    for (PermissionInfo permission : packageInfo.permissions)                           // for each package that defines permissions..
                    {                                                                                     // and for each of those permissions..
                        if (myPermissions.containsKey(permission.name))                                      // check against our permissions..
                        {
                            // !!! some other app has claimed our permission!
                            // On api21+ this security risk is prevented (but is still possible for lower apis).
                            // Warn the user that the other package might be malicious!

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setIcon(warningIcon);
                            alertDialog.setTitle(context.getString(R.string.security_dialog_title));
                            alertDialog.setMessage(context.getString(R.string.security_duplicate_permissions, permission.name, packageInfo.packageName));
                            alertDialog.setNeutralButton( context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            Log.e("checkCustomPermissions", "Duplicate permissions! " + packageInfo.packageName + " also defines " + permission.name + "!");
                            alertDialog.show();
                        }
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                Log.e("checkCustomPermissions", "Unable to get package " + myPackageName);
            }

            long bench_end = System.nanoTime();
            Log.d("checkCustomPermissions", "permission check took :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        }
    }

    /**
     * @param context context
     * @param permissionName permission
     * @return package that owns this permission, or null of permission dne
     */
    @Nullable
    public static String findPermission(@NonNull Context context, String permissionName)
    {
        String packageName = null;
        PackageManager packageManager = context.getPackageManager();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (packageInfo.permissions != null) {
                for (PermissionInfo permission : packageInfo.permissions) {
                    if (permission != null && permission.name.equals(permissionName)) {
                        packageName = packageInfo.packageName;
                        break;
                    }
                }
            }
        }
        return packageName;
    }

    public static void openAppDetails(Activity activity)
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void saveLocationPref(final Context context, Location location) {
        saveLocationPref(context, location, true);
    }
    public static void saveLocationPref(final Context context, Location location, boolean withSideEffects)
    {
        WidgetSettings.saveLocationPref(context, 0, location);

        if (withSideEffects)
        {
            ExecutorService executor = Executors.newScheduledThreadPool(2);
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    long bench_start = System.nanoTime();
                    WidgetListAdapter.updateAllWidgetAlarms(context);
                    long bench_end = System.nanoTime();
                    Log.d("DEBUG", "update all widgets :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
                }
            });
            executor.execute(new Runnable()
            {
                @Override
                public void run() {
                    context.sendBroadcast(new Intent(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_LOCATION_CHANGED, null)));
                }
            });
            executor.shutdown();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @NonNull
    public static AppThemeInfo loadThemeInfo(Context context) {
        return AppSettings.loadThemeInfo(AppSettings.loadThemePref(context));
    }

    @NonNull
    public static AppThemeInfo loadThemeInfo(@Nullable String extendedThemeName)
    {
        if (extendedThemeName == null) {
            return info_defaultTheme;

        } else if (extendedThemeName.startsWith(THEME_LIGHT)) {
            return info_lightTheme;

        } else if (extendedThemeName.startsWith(THEME_LIGHT1)) {
            return info_light1Theme;

        } else if (extendedThemeName.startsWith(THEME_DARK)) {
            return info_darkTheme;

        } else if (extendedThemeName.startsWith(THEME_DARK1)) {
            return info_dark1Theme;

        } else if (extendedThemeName.startsWith(THEME_SYSTEM)) {
            return info_systemTheme;

        } else if (extendedThemeName.startsWith(THEME_DAYNIGHT)) {
            return info_dayNightTheme;

        } else if (extendedThemeName.startsWith(System1ThemeInfo.THEMENAME)) {
            return info_system1Theme;

        } // else if (extendedThemeName.startsWith(SOME_THEME_NAME)) { /* TODO: additional themes here */ }
        else {
            return info_defaultTheme;
        }
    }
    private static final AppThemeInfo info_darkTheme = new DarkThemeInfo();
    private static final AppThemeInfo info_lightTheme = new LightThemeInfo();
    private static final AppThemeInfo info_dayNightTheme = new DayNightThemeInfo();
    private static final AppThemeInfo info_systemTheme = new SystemThemeInfo();
    private static final AppThemeInfo info_system1Theme = new System1ThemeInfo();
    private static final AppThemeInfo info_dark1Theme = new DarkTheme1Info();
    private static final AppThemeInfo info_light1Theme = new LightTheme1Info();
    private static final AppThemeInfo info_defaultTheme = info_systemTheme;

    public static AppThemeInfo[] appThemeInfo()
    {
        return new AppThemeInfo[] {
                info_systemTheme, info_darkTheme, info_lightTheme,
                info_system1Theme, info_dark1Theme, info_light1Theme
        };
    }

    /**
     * AppThemeInfo
     */
    public abstract static class AppThemeInfo
    {
        public abstract int getStyleId(Context context, TextSize textSize, SuntimesRiseSetData data);
        public abstract String getThemeName();

        /**
         * @return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_NO;
         */
        public abstract int getDefaultNightMode();

        public String getExtendedThemeName(TextSize textSize) {
            return getExtendedThemeName(getThemeName(), textSize.name());
        }
        public String getExtendedThemeName(String textSize) {
            return getExtendedThemeName(getThemeName(), textSize);
        }

        public String getDisplayString(Context context) {
            return getThemeName();
        }
        public String toString() {
            return getThemeName();
        }

        public static String getExtendedThemeName(String themeName, String textSize) {
            return themeName + "_" + textSize;
        }
        public static TextSize getTextSize(String extendedThemeName) {
            String[] parts = extendedThemeName.split("_");
            return TextSize.valueOf((parts.length > 0 ? parts[1] : TextSize.NORMAL.name()), TextSize.NORMAL);
        }
    }

    public static class SystemThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_SYSTEM;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System_Small;
                case LARGE: return R.style.AppTheme_System_Large;
                case XLARGE: return R.style.AppTheme_System_XLarge;
                case NORMAL: default: return R.style.AppTheme_System;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_systemDefault);
        }
    }

    public static class LightThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light_Small;
                case LARGE: return R.style.AppTheme_Light_Large;
                case XLARGE: return R.style.AppTheme_Light_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_lightTheme);
        }
    }

    public static class DarkThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark_Small;
                case LARGE: return R.style.AppTheme_Dark_Large;
                case XLARGE: return R.style.AppTheme_Dark_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_darkTheme);
        }
    }

    public static class DayNightThemeInfo extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_DAYNIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return (isDay == null) ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    : (isDay ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            if (data == null)
            {
                data = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                data.initCalculator();
            }
            isDay = data.isDay();
            switch (size) {
                case SMALL: return (isDay ? R.style.AppTheme_Light_Small : R.style.AppTheme_Dark_Small);
                case LARGE: return (isDay ? R.style.AppTheme_Light_Large : R.style.AppTheme_Dark_Large);
                case NORMAL: default: return (isDay ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
            }
        }
        private Boolean isDay = null;
        public void setIsDay(boolean value) {
            isDay = value;
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_nightMode);
        }
    }

    public static class LightTheme1Info extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_LIGHT1;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light1_Small;
                case LARGE: return R.style.AppTheme_Light1_Large;
                case XLARGE: return R.style.AppTheme_Light1_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_lightTheme1);
        }
    }

    public static class DarkTheme1Info extends AppThemeInfo
    {
        @Override
        public String getThemeName() {
            return THEME_DARK1;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark1_Small;
                case LARGE: return R.style.AppTheme_Dark1_Large;
                case XLARGE: return R.style.AppTheme_Dark1_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_darkTheme1);
        }
    }

    public static class System1ThemeInfo extends AppThemeInfo
    {
        public static String THEMENAME = THEME_SYSTEM1;

        @Override
        public String getThemeName() {
            return THEMENAME;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System1_Small;
                case LARGE: return R.style.AppTheme_System1_Large;
                case XLARGE: return R.style.AppTheme_System1_XLarge;
                case NORMAL: default: return R.style.AppTheme_System1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.appThemes_systemDefault1);
        }
    }


}
