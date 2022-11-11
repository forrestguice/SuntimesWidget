/**
    Copyright (C) 2017-2019 Forrest Guice
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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.media.MediaScannerConnection;
//import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.jraska.falcon.Falcon;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

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

    protected static ViewAssertion assertShown = matches(isDisplayed());
    protected static ViewAssertion assertShownCompletely = matches(isDisplayingAtLeast(90));
    protected static ViewAssertion assertHidden = matches(not(isDisplayed()));
    protected static ViewAssertion assertEnabled = matches(allOf(isEnabled(), isDisplayed()));
    protected static ViewAssertion assertDisabled = matches(allOf(not(isEnabled()), isDisplayed()));
    protected static ViewAssertion assertFocused = matches(allOf(isEnabled(), isDisplayed(), hasFocus()));
    protected static ViewAssertion assertClickable = matches(isClickable());
    protected static ViewAssertion assertSelected = matches(isSelected());
    protected static ViewAssertion assertChecked = matches(isChecked());
    protected static ViewAssertion assertNotChecked = matches(isNotChecked());

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * SuntimesTestConfig
     */
    public static class SuntimesTestConfig
    {
        public Location location;
        public String timezoneID;
        public WidgetSettings.TimeFormatMode timeformat;
        public WidgetSettings.LengthUnit lengthUnits;

        public SuntimesTestConfig(Location location, String timezoneID, boolean format24, String units)
        {
            this.location = location;
            this.timezoneID = timezoneID;
            this.timeformat = (format24 ? WidgetSettings.TimeFormatMode.MODE_24HR : WidgetSettings.TimeFormatMode.MODE_12HR);
            this.lengthUnits = WidgetSettings.LengthUnit.valueOf(units);
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
        WidgetSettings.saveDateModePref(context, 0, WidgetSettings.DateMode.CURRENT_DATE);
        WidgetSettings.saveTrackingModePref(context, 0, WidgetSettings.TrackingMode.RECENT);
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
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWEQUINOX, true);
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

        WidgetSettings.saveLocationModePref(context, 0, WidgetSettings.LocationMode.CUSTOM_LOCATION);
        WidgetSettings.saveLocationPref(context, 0, configuration.location);

        WidgetSettings.saveTimezoneModePref(context, 0, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        WidgetSettings.saveTimezonePref(context, 0, configuration.timezoneID);
    }


}
