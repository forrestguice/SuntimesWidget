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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;

import java.util.HashMap;
import java.util.Locale;

/**
 * Shared preferences used by the app; uses getDefaultSharedPreferences (stored in com.forrestguice.suntimeswidget_preferences.xml).
 */
public class AppSettings
{
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DAYNIGHT = "daynight";

    public static final String PREF_KEY_APPEARANCE_THEME = "app_appearance_theme";
    public static final String PREF_DEF_APPEARANCE_THEME = THEME_DARK;

    public static final String PREF_KEY_APPEARANCE_THEME_LIGHT = "app_appearance_theme_light";
    public static final String PREF_DEF_APPEARANCE_THEME_LIGHT = "default";

    public static final String PREF_KEY_APPEARANCE_THEME_DARK = "app_appearance_theme_dark";
    public static final String PREF_DEF_APPEARANCE_THEME_DARK = "default";

    public static final String PREF_KEY_LOCALE_MODE = "app_locale_mode";
    public static final LocaleMode PREF_DEF_LOCALE_MODE = LocaleMode.SYSTEM_LOCALE;

    public static final String PREF_KEY_LOCALE = "app_locale";
    public static final String PREF_DEF_LOCALE = "en";

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

    public static final String PREF_KEY_UI_SHOWMOON = "app_ui_showmoon";
    public static final boolean PREF_DEF_UI_SHOWMOON = true;

    public static final String PREF_KEY_UI_SHOWDATASOURCE = "app_ui_showdatasource";
    public static final boolean PREF_DEF_UI_SHOWDATASOURCE = true;

    public static final String PREF_KEY_UI_SHOWFIELDS = "app_ui_showfields";
    public static final byte PREF_DEF_UI_SHOWFIELDS = 0b00111111;
    public static final int FIELD_ACTUAL = 0;  // bit positions
    public static final int FIELD_CIVIL = 1;
    public static final int FIELD_NAUTICAL = 2;
    public static final int FIELD_ASTRO = 3;
    public static final int FIELD_NOON = 4;
    public static final int FIELD_GOLD = 5;
    public static final int FIELD_BLUE = 6;
    public static final int NUM_FIELDS = 7;

    public static final String PREF_KEY_ACCESSIBILITY_VERBOSE = "app_accessibility_verbose";
    public static final boolean PREF_DEF_ACCESSIBILITY_VERBOSE = false;

    public static final String PREF_KEY_UI_TIMEZONESORT = "app_ui_timezonesort";
    public static final WidgetTimezones.TimeZoneSort PREF_DEF_UI_TIMEZONESORT = WidgetTimezones.TimeZoneSort.SORT_BY_ID;

    public static final String PREF_KEY_GETFIX_MINELAPSED = "getFix_minElapsed";
    public static final String PREF_KEY_GETFIX_MAXELAPSED = "getFix_maxElapsed";
    public static final String PREF_KEY_GETFIX_MAXAGE = "getFix_maxAge";

    public static final String PREF_KEY_GETFIX_PASSIVE = "getFix_passiveMode";
    public static final boolean PREF_DEF_GETFIX_PASSIVE = false;

    public static final String PREF_KEY_PLUGINS_ENABLESCAN = "app_plugins_enabled";
    public static final boolean PREF_DEF_PLUGINS_ENABLESCAN = false;

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
        //noinspection SimplifiableIfStatement
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

    private static @NonNull Locale localeForLanguageTag(@NonNull String languageTag)
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

    public static void setTimeZoneSortPref( Context context, WidgetTimezones.TimeZoneSort sortMode )
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

    public static boolean loadShowMoonPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWMOON, PREF_DEF_UI_SHOWMOON);
    }

    public static boolean loadDatasourceUIPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_UI_SHOWDATASOURCE, PREF_DEF_UI_SHOWDATASOURCE);
    }

    public static boolean[] loadShowFieldsPref( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int showFields = pref.getInt(PREF_KEY_UI_SHOWFIELDS, PREF_DEF_UI_SHOWFIELDS);

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
     * @return a theme identifier
     */
    public static String loadThemePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME, PREF_DEF_APPEARANCE_THEME);
    }

    public static String loadThemeLightPref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME_LIGHT, PREF_DEF_APPEARANCE_THEME_LIGHT);
    }

    public static String loadThemeDarkPref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_KEY_APPEARANCE_THEME_DARK, PREF_DEF_APPEARANCE_THEME_DARK);
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
        int styleID = R.style.AppTheme_Dark;
        if (themeName != null)
        {
            //noinspection IfCanBeSwitch
            if (themeName.equals(THEME_LIGHT))
            {
                styleID = R.style.AppTheme_Light;

            } else if (themeName.equals(THEME_DARK)) {
                styleID = R.style.AppTheme_Dark;

            } else if (themeName.equals(THEME_DAYNIGHT)) {
                if (data == null)
                {
                    data = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                    data.initCalculator(context);
                }
                styleID = (data.isDay() ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
            }
        }
        return styleID;
    }

    public static String getThemeOverride(Context context, int appThemeResID)
    {
        String themeOverride = ((appThemeResID == R.style.AppTheme_Light) ? AppSettings.loadThemeLightPref(context) : AppSettings.loadThemeDarkPref(context));
        return ((themeOverride != null && !themeOverride.equals("default")) ? themeOverride : null);
    }

    /**
     * @param prefs an instance of SharedPreferences
     * @param defaultValue the default max age value if pref can't be loaded
     * @return the gps max age value (milliseconds)
     */
    public static int loadPrefGpsMaxAge(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String maxAgeString = prefs.getString(PREF_KEY_GETFIX_MAXAGE, defaultValue+"");
            retValue = Integer.parseInt(maxAgeString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMaxAge", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @param prefs an instance of SharedPreferences
     * @param defaultValue the default min elapsed value if pref can't be loaded
     * @return the gps min elapsed value (milliseconds)
     */
    public static int loadPrefGpsMinElapsed(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String minAgeString = prefs.getString(PREF_KEY_GETFIX_MINELAPSED, defaultValue+"");
            retValue = Integer.parseInt(minAgeString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMinElapsed", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @param prefs an instance of SharedPreferences
     * @param defaultValue the default max elapsed value if pref can't be loaded
     * @return the gps max elapsed value (milliseconds)
     */
    public static int loadPrefGpsMaxElapsed(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String maxElapsedString = prefs.getString(PREF_KEY_GETFIX_MAXELAPSED, defaultValue+"");
            retValue = Integer.parseInt(maxElapsedString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMaxElapsed", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @return true use the passive provider (don't prompt when other providers are disabled), false use the gps/network provider (prompt when disabled)
     */
    public static boolean loadPrefGpsPassiveMode( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_GETFIX_PASSIVE, PREF_DEF_GETFIX_PASSIVE);
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

}
