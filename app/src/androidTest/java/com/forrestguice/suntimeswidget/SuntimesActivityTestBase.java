/**
    Copyright (C) 2017-2025 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimes.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimes.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.jraska.falcon.Falcon;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import static android.os.Environment.DIRECTORY_PICTURES;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public abstract class SuntimesActivityTestBase
{
    public static final String TESTLOC_0_LABEL = "Test Location 0";
    public static final String TESTLOC_0_LAT = "34";
    public static final String TESTLOC_0_LON = "-111";

    public static final String TESTLOC_1_LABEL = "Test Location 1";
    public static final String TESTLOC_1_LAT = "83.124";
    public static final String TESTLOC_1_LON = "23.1592";
    public static final String TESTLOC_1_ALT = "10";

    public static final String TESTTZID_0 = "US/Eastern";
    public static final String TESTTZID_1 = "US/Pacific";
    public static final String TESTTZID_2 = "US/Arizona";

    public static final int TESTDATE_0_YEAR = 2017;
    public static final int TESTDATE_0_MONTH = 1;    // feb 19, 2017
    public static final int TESTDATE_0_DAY = 19;


    public static final String SCREENSHOT_DIR = "test-screenshots";

    protected SharedPreferences config(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Rotate the device to landscape and back.
     */
    public void rotateDevice(ActivityTestRule activityRule)
    {
        rotateDevice(activityRule, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rotateDevice(activityRule, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Rotate to given orientation.
     * @param orientation ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
     */
    public void rotateDevice(ActivityTestRule activityRule, int orientation )
    {
        activityRule.getActivity().setRequestedOrientation(orientation);
    }

    public static Matcher<View> withResourceName(String resourceName) {
        return withResourceName(is(resourceName));
    }

    /**
     * copied from https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!searchin/android-test-kit-discuss/ActionBar/android-test-kit-discuss/mlMbTR30-0U/WljZkKBbdU0J
     * @param resourceNameMatcher a view matcher
     * @return a view matcher
     */
    public static Matcher<View> withResourceName(final Matcher<String> resourceNameMatcher)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("with resource name: ");
                resourceNameMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view)
            {
                int id = view.getId();
                return ((id != View.NO_ID) && (id != 0) && (view.getResources() != null)
                         && (resourceNameMatcher.matches(view.getResources().getResourceName(id))));
            }
        };
    }

    /**
     * @param activity Activity context
     * @param name screenshot name
     */
    public static void captureScreenshot(Activity activity, String name)
    {
        SuntimesActivityTestBase.captureScreenshot(activity, "", name);
    }

    public static void captureScreenshot(Activity activity, String subdir, String name)
    {
        subdir = subdir.trim();
        if (!subdir.isEmpty() && !subdir.startsWith("/"))
        {
            subdir = "/" + subdir;
        }

        // saves to..
        //     SD card\Android\data\com.forrestguice.suntimeswidget\files\Pictures\test-screenshots\subdir
        File d = activity.getExternalFilesDir(DIRECTORY_PICTURES);
        if (d != null)
        {
            String dirPath = d.getAbsolutePath() + "/" + SCREENSHOT_DIR + subdir;

            File dir = new File(dirPath);
            boolean dirCreated = dir.mkdirs();

            String path = dirPath + "/" + name + ".png";
            File file = new File(path);
            if (file.exists())
            {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    Log.w("captureScreenshot", "Failed to delete file! " + path);
                }
            }

            try {
                Falcon.takeScreenshot(activity, file);
                MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);

            } catch (Exception e1) {
                Log.e("captureScreenshot", "Failed to write file! " + e1);
            }
        } else {
            Log.e("captureScreenshot", "Failed to write file! getExternalFilesDir() returns null..");
        }
    }

    /**
     * @param viewId view identifier
     */
    public static boolean viewIsDisplayed(int viewId) {
        return viewIsDisplayed(viewId, null);
    }
    public static boolean viewIsDisplayed(int viewId, Context context, int textResID) {
        return viewIsDisplayed(viewId, context.getString(textResID));
    }
    public static boolean viewIsDisplayed(int viewId, String text)
    {
        final boolean[] isDisplayed = {true};
        Matcher<View> view = (text != null) ? allOf(withId(viewId), withText(containsString(text)))
                : withId(viewId);
        onView(view).withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                isDisplayed[0] = false;
            }
        }).check(matches(isDisplayed()));
        return isDisplayed[0];
    }


    /**
     * @param viewInteraction a ViewInteraction wrapping some view
     * @return true view is checked, false otherwise
     */
    public static boolean viewIsChecked(ViewInteraction viewInteraction)
    {
        final boolean[] isChecked = {true};
        viewInteraction.withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                isChecked[0] = false;
            }
        }).check(matches(isChecked()));
        return isChecked[0];
    }

    /**
     * @param spinnerId spinner ID
     * @param text text
     * @return true if spinner displays given text
     */
    public static boolean spinnerDisplaysText(int spinnerId, String text)
    {
        final boolean[] displaysText = {true};
        onView(withId(spinnerId)).withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                displaysText[0] = false;
            }
        }).check(matches(withSpinnerText(text)));
        return displaysText[0];
    }

    public static boolean spinnerDisplaysText(Context context, int spinnerId, int stringResID) {
        return spinnerDisplaysText(spinnerId, context.getString(stringResID));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * SuntimesTestConfig
     */
    public static class SuntimesTestConfig
    {
        public Location location;
        public String timezoneID;
        public TimeFormatMode timeformat;
        public LengthUnit lengthUnits;

        public SuntimesTestConfig(Location location, String timezoneID, boolean format24, String units)
        {
            this.location = location;
            this.timezoneID = timezoneID;
            this.timeformat = (format24 ? TimeFormatMode.MODE_24HR : TimeFormatMode.MODE_12HR);
            this.lengthUnits = LengthUnit.valueOf(units);
        }
    }

    protected static String version = BuildConfig.VERSION_NAME;
    protected HashMap<String, SuntimesTestConfig> config;
    protected SuntimesTestConfig defaultConfig = new SuntimesTestConfig(new Location("Phoenix", "33.45579", "-111.94580", "385"), "US/Arizona", false, "IMPERIAL");

    public void initConfigurations()
    {
        config = new HashMap<String, SuntimesTestConfig>();
        config.put("ca", new SuntimesTestConfig(new Location("Barcelona", "41.3825", "2.1769", "31"), "CET", true, "METRIC"));
        config.put("cs", new SuntimesTestConfig(new Location("Prague", "50.0595", "14.3255", "361"), "CET", true, "METRIC"));
        config.put("de", new SuntimesTestConfig(new Location("Berlin", "52.5243", "13.4105", "40"), "Europe/Berlin", true, "METRIC"));
        config.put("es_ES", new SuntimesTestConfig(new Location("Madrid", "40.4378", "-3.8196", "681"), "Europe/Madrid", false, "METRIC"));
        config.put("eu", new SuntimesTestConfig(new Location("Euskal Herriko erdigunea", "42.883008", "-1.935491", "1258"), "CET", true, "METRIC"));
        config.put("fr", new SuntimesTestConfig(new Location("Paris", "48.8566", "2.3518", "41"), "Europe/Paris", true, "METRIC"));
        config.put("hu", new SuntimesTestConfig(new Location("Budapest", "47.4811", "18.9902", "225"), "Europe/Budapest", true, "METRIC"));
        config.put("it", new SuntimesTestConfig(new Location("Roma", "41.9099", "12.3959", "79"), "CET", false, "METRIC"));
        config.put("pl", new SuntimesTestConfig(new Location("Warszawa", "52.2319", "21.0067", "143"), "Poland", true, "METRIC"));
        config.put("pt_BR", new SuntimesTestConfig(new Location("São Paulo", "-23.6821", "-46.8754", "815"), "Brazil/East", true, "METRIC"));
        config.put("nb", new SuntimesTestConfig(new Location("Oslo", "59.8937", "10.6450", "0"), "Europe/Oslo", true, "METRIC"));
        config.put("nl", new SuntimesTestConfig(new Location("Amsterdam", "52.3745", "4.758", "0"), "CET", true, "METRIC"));
        config.put("ru", new SuntimesTestConfig(new Location("Москва", "55.7539", "37.6202", "186"), "Etc/GMT-3", true, "METRIC"));
        config.put("zh_CN", new SuntimesTestConfig(new Location("Beijing", "39.9042", "116.4074", "0"), "Asia/Taipei", false, "METRIC"));
        config.put("zh_TW", new SuntimesTestConfig(new Location("Taipei", "25.0330", "121.5654", "0"), "Asia/Taipei", false, "METRIC"));

        if (!version.startsWith("v"))
            version = "v" + version;
    }

    protected void configureAppForTesting(Activity context)
    {
        WidgetSettings.saveDateModePref(context, 0, DateMode.CURRENT_DATE);
        WidgetSettings.saveTrackingModePref(context, 0, TrackingMode.RECENT);
        WidgetSettings.saveShowSecondsPref(context, 0, false);
        WidgetSettings.saveLocationAltitudeEnabledPref(context, 0, true);
        WidgetSettings.saveLocalizeHemispherePref(context, 0, true);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(AppSettings.PREF_KEY_UI_SHOWHEADER_TEXT, "" + AppSettings.HEADER_TEXT_AZIMUTH);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWHEADER_ICON, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWWARNINGS, false);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWLIGHTMAP, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, false);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWMAPBUTTON, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWCOORDINATES, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWEQUINOX, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWEQUINOXDATE, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWCROSSQUARTER, false);
        prefs.putInt(AppSettings.PREF_KEY_UI_SHOWFIELDS, AppSettings.PREF_DEF_UI_SHOWFIELDS);
        prefs.apply();
    }

    protected void configureAppForTesting(Context context, String languageTag, SuntimesTestConfig configuration, String theme)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(AppSettings.PREF_KEY_LOCALE_MODE, AppSettings.LocaleMode.CUSTOM_LOCALE.name());
        prefs.putString(AppSettings.PREF_KEY_LOCALE, languageTag);
        prefs.putString(AppSettings.PREF_KEY_APPEARANCE_THEME, theme);
        prefs.apply();

        WidgetSettings.saveTimeFormatModePref(context, 0, configuration.timeformat);
        WidgetSettings.saveLengthUnitsPref(context, 0, configuration.lengthUnits);

        WidgetSettings.saveLocationModePref(context, 0, LocationMode.CUSTOM_LOCATION);
        WidgetSettings.saveLocationPref(context, 0, configuration.location);

        WidgetSettings.saveTimezoneModePref(context, 0, TimezoneMode.CUSTOM_TIMEZONE);
        WidgetSettings.saveTimezonePref(context, 0, configuration.timezoneID);
    }

    public static SimpleDateFormat[] timeDateFormats12 = new SimpleDateFormat[] {
            new SimpleDateFormat("MMM d, h:mm a"),
            new SimpleDateFormat("MMMM d, h:mm a"),
            new SimpleDateFormat("MMMM d, yyyy, h:mm a")
    };
    public static SimpleDateFormat[] timeDateFormats12s = new SimpleDateFormat[] {
            new SimpleDateFormat("MMM d, h:mm:ss a"),
            new SimpleDateFormat("MMMM d, h:mm:ss a"),
            new SimpleDateFormat("MMMM d, yyyy, h:mm:ss a")
    };
    public static SimpleDateFormat[] timeDateFormats24 = new SimpleDateFormat[] {
            new SimpleDateFormat("MMM d, HH:mm"),
            new SimpleDateFormat("MMMM d, HH:mm"),
            new SimpleDateFormat("MMMM d, yyyy, HH:mm")
    };
    public static SimpleDateFormat[] timeDateFormats24s = new SimpleDateFormat[] {
            new SimpleDateFormat("MMM d, HH:mm:ss"),
            new SimpleDateFormat("MMMM d, HH:mm:ss"),
            new SimpleDateFormat("MMMM d, yyyy, HH:mm:ss")
    };

    public static void setAnimationsEnabled(boolean enabled) throws IOException
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            UiAutomation automation = androidx.test.InstrumentationRegistry.getInstrumentation().getUiAutomation();
            automation.executeShellCommand("settings put global transition_animation_scale " + (enabled ? "1" : "0")).close();
            automation.executeShellCommand("settings put global window_animation_scale " + (enabled ? "1" : "0")).close();
            automation.executeShellCommand("settings put global animator_duration_scale " + (enabled ? "1" : "0")).close();
        } // else // TODO
    }

    public static Context getContext() {
        return androidx.test.InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    protected void overrideConfigState(Context context)
    {
        config(context).edit().putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, false).apply();
        config(context).edit().remove(AppSettings.PREF_KEY_LOCALE);
        config(context).edit().putString(AppSettings.PREF_KEY_LOCALE_MODE, AppSettings.LocaleMode.SYSTEM_LOCALE.name()).apply();
    }
    protected void saveConfigState(Context context) {
        //savedState_localeMode = AppSettings.loadLocaleModePref(activity);
        savedState_dateTapAction = AppSettings.loadDateTapActionPref(context);
        savedState_clockTapAction = AppSettings.loadClockTapActionPref(context);
        savedState_showDataSource = AppSettings.loadDatasourceUIPref(context);
        savedState_showMapButton = AppSettings.loadShowMapButtonPref(context);
        savedState_firstLaunch = AppSettings.isFirstLaunch(context);
        savedState_navMode = AppSettings.loadNavModePref(context);
        savedState_launcherMode = AppSettings.loadLauncherModePref(context);
        savedState_locationMode = WidgetSettings.loadLocationModePref(context, 0);
    }
    protected void restoreConfigState(Context context) {
        SharedPreferences.Editor config = config(context).edit();
        //config.putString(AppSettings.PREF_KEY_LOCALE_MODE, savedState_localeMode.name()).apply();
        config.putString(AppSettings.PREF_KEY_UI_DATETAPACTION, savedState_dateTapAction).apply();
        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, savedState_clockTapAction).apply();
        config.putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, savedState_showDataSource).apply();
        config.putBoolean(AppSettings.PREF_KEY_UI_SHOWMAPBUTTON, savedState_showMapButton).apply();
        config.putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, savedState_firstLaunch).apply();
        config.putString(AppSettings.PREF_KEY_NAVIGATION_MODE, savedState_launcherMode).apply();
        config.putString(AppSettings.PREF_KEY_LAUNCHER_MODE, savedState_navMode).apply();
        if (savedState_locationMode != null) {
            WidgetSettings.saveLocationModePref(context, 0, savedState_locationMode);
        }
    }
    protected String savedState_navMode;
    protected String savedState_launcherMode;
    protected String savedState_dateTapAction;
    protected String savedState_clockTapAction;
    protected boolean savedState_showDataSource;
    protected boolean savedState_showMapButton;
    protected boolean savedState_firstLaunch;
    protected LocationMode savedState_locationMode;
    //protected AppSettings.LocaleMode savedState_localeMode;

    public static abstract class Robot<T>
    {
        protected T robot;
        public void setRobot(T robot) {
            this.robot = robot;
        }

        public T sleep(long ms) {
            SystemClock.sleep(ms);
            return robot;
        }

        public T doubleRotateDevice(Activity activity)
        {
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            sleep(1000);
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return robot;
        }
        public T rotateDevice(Activity activity, int orientation) {
            activity.setRequestedOrientation(orientation);
            return robot;
        }

        public T captureScreenshot(Activity activity, String name) {
            captureScreenshot(activity, "", name);
            return robot;
        }
        public T captureScreenshot(Activity activity, String subdir, String name) {
            SuntimesActivityTestBase.captureScreenshot(activity, subdir, name);
            return robot;
        }
    }

    /**
     * ActivityRobot
     * @param <T> robot method return type
     */
    public static abstract class ActivityRobot<T> extends Robot<T>
    {
        public ActivityRobot() {}
        public ActivityRobot(T robot) {
            this.robot = robot;
        }

        public T recreateActivity(final Activity activity)
        {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    activity.recreate();
                }
            });
            return robot;
        }

        public T finishActivity(final Activity activity)
        {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    activity.finish();
                }
            });
            return robot;
        }

        public T clickHomeButton(Context context) {
            onView(navigationButton()).perform(click());
            return robot;
        }
        public T showSidebarMenu(Context context) {
            onView(navigationButton()).perform(click());
            return robot;
        }
        public T clickSidebarMenu_clock(Context context) {
            onView(allOf(withText(R.string.configAction_clock),
                    not(withParent(withClassName(endsWith("Toolbar")))))).perform(click());
            return robot;
        }
        public T clickSidebarMenu_alarms(Context context) {
            onView(allOf(withText(R.string.configLabel_alarmClock),
                    not(withParent(withClassName(endsWith("Toolbar")))))).perform(click());
            return robot;
        }
        public T clickSidebarMenu_settings(Context context) {
            onView(allOf(withText(R.string.configAction_settings),
                    not(withParent(withClassName(endsWith("Toolbar")))))).perform(click());
            return robot;
        }
        public T clickSidebarMenu_about(Context context) {
            onView(allOf(withText(R.string.configAction_aboutWidget),
                    not(withParent(withClassName(endsWith("Toolbar")))))).perform(click());
            return robot;
        }
        public T cancelSidebarMenu(Context context) {
            onView(withText(R.string.configAction_aboutWidget)).perform(pressBack());
            return robot;
        }

        public T showOverflowMenu(Context context) {
            openActionBarOverflowOrOptionsMenu(context);
            return robot;
        }
        public T clickOverflowMenu_help() {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public T clickOverflowMenu_settings() {
            onView(withText(R.string.configAction_settings)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }
        public T clickOverflowMenu_about() {
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).perform(click());
            return robot;
        }

        public T assertOverflowMenu_hasSimpleNavigation(boolean isSimple)
        {
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).check(isSimple ? assertShown : doesNotExist());
            onView(withText(R.string.configAction_settings)).inRoot(isPlatformPopup()).check(isSimple ? assertShown : doesNotExist());
            return robot;
        }

        public T assertActionBar_homeButtonShown(boolean shown) {
            onView(allOf(navigationButton(), hasDrawable(R.drawable.ic_action_suntimes))).check(shown ? assertShown : doesNotExist());
            return robot;
        }
        public T assertActionBar_navButtonShown(boolean shown) {
            onView(navigationButton()).check(shown ? assertShown : doesNotExist());
            return robot;
        }

        public T assertSideBarMenuShown(Activity context) {
            onView(allOf(withText(R.string.configAction_clock),
                    not(withParent(withClassName(endsWith("Toolbar")))))).check(assertShown);
            onView(allOf(withText(R.string.configLabel_alarmClock),
                    not(withParent(withClassName(endsWith("Toolbar")))))).check(assertShown);
            onView(allOf(withText(R.string.configAction_settings),
                    not(withParent(withClassName(endsWith("Toolbar")))))).check(assertShown);
            onView(allOf(withText(R.string.configAction_aboutWidget),
                    not(withParent(withClassName(endsWith("Toolbar")))))).check(assertShown);
            return robot;
        }

        //////////////////////////////////////////////////////////

        public static SuntimesCalculator appCalculator(Context context) {
            SuntimesCalculator calculator = new Time4A4JSuntimesCalculator();
            calculator.init(appLocation(context), appTimeZone(context));
            return calculator;
        }
        public static Location appLocation(Context context) {
            return WidgetSettings.loadLocationPref(context, 0);
        }
        public static TimeZone appTimeZone(Context context) {
            return TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, 0));
        }

        public static TimeZone timeZone_UTC() {
            return TimeZone.getTimeZone("UTC");
        }
        public static TimeZone timeZone_ApparentSolar(Context context) {
            return WidgetTimezones.getTimeZone(TimeZones.ApparentSolarTime.TIMEZONEID, appLocation(context).getLongitudeAsDouble(), appCalculator(context));
        }
        public static TimeZone timeZone_LocalMean(Context context) {
            return WidgetTimezones.getTimeZone(TimeZones.LocalMeanTime.TIMEZONEID, appLocation(context).getLongitudeAsDouble(), appCalculator(context));
        }
        public static TimeZone timeZone_Suntimes(Context context) {
            return appTimeZone(context);
        }
    }

}
