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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;

import com.forrestguice.annotation.NonNull;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.events.EventListActivityTest;
import com.forrestguice.suntimeswidget.getfix.PlacesActivityTest;

import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.fragments.GeneralPrefsFragment;
import com.forrestguice.suntimeswidget.support.espresso.DataInteractionHelper;
import com.forrestguice.suntimeswidget.support.espresso.ViewInteractionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import java.io.IOException;

import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertChecked;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertDisabled;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertNotChecked;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.withIndex;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class SuntimesSettingsActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesSettingsActivity> activityRule = new ActivityTestRule<>(SuntimesSettingsActivity.class, false, false);

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(getContext());
        overrideConfigState(getContext());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(getContext());
    }

    @Test @QuickTest
    public void test_settingsActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new SettingsActivityRobot()
                .assertActivityShown(activity)
                .captureScreenshot(activity, "suntimes-activity-settings0");
    }

    @Test @QuickTest
    public void test_settingsActivity_general()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-general0");
                .assertActivityShown(activity)
                .clickHeader_generalSettings()
                .assertShown_generalSettings(activity);
    }

    @Test @QuickTest
    public void test_settingsActivity_alarms()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-alarms0");
                .assertActivityShown(activity)
                .clickHeader_alarmSettings()
                .assertShown_alarmSettings(activity);
    }

    @Test @QuickTest
    public void test_settingsActivity_locale()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-locale0");
                .assertActivityShown(activity)
                .clickHeader_localeSettings()
                .assertShown_localeSettings(activity);
    }

    @Test @QuickTest
    public void test_settingsActivity_places()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-places0");
                .assertActivityShown(activityRule.getActivity())
                .clickHeader_placeSettings()
                .assertShown_placeSettings(activityRule.getActivity());
    }

    @Test @QuickTest
    public void test_settingsActivity_places_managePlaces()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SettingsActivityRobot()
                .clickHeader_placeSettings()
                .clickPref_managePlaces();
        new PlacesActivityTest.PlacesActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    @Test @QuickTest
    public void test_settingsActivity_ui()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-ui0");
                .assertActivityShown(activityRule.getActivity())
                .clickHeader_userInterfaceSettings()
                .assertShown_userInterfaceSettings(activityRule.getActivity());
    }

    @Test @QuickTest
    public void test_settingsActivity_ui_customEvents()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SettingsActivityRobot()
                .assertActivityShown(activityRule.getActivity())
                .clickHeader_userInterfaceSettings().sleep(250)
                .clickPref_manageEvents();
        new EventListActivityTest.EventListActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    @Test @QuickTest
    public void test_settingsActivity_widgets()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SettingsActivityRobot()
                //.captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-widgets0");
                .assertActivityShown(activityRule.getActivity())
                .clickHeader_widgetList();
        new WidgetListActivityTest.WidgetListActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    /*@Test
    public void test_settingsActivity_ui_flipSettings()
    {
        //onView(isRoot()).perform(pressBack());
        //onView(isRoot()).perform(pressBack());

        //boolean showLightmap = AppSettings.loadShowLightmapPref(activityRule.getActivity());
        //flipUISettings_lightmap(activityRule.getActivity());
        //flipUISettings_lightmap(activityRule.getActivity());
        //assertTrue(showLightmap == AppSettings.loadShowLightmapPref(activityRule.getActivity()));

        //boolean showWarnings = AppSettings.loadShowWarningsPref(activityRule.getActivity());
        //flipUISettings_uiWarnings(activityRule.getActivity());
        //flipUISettings_uiWarnings(activityRule.getActivity());
        //assertTrue(showWarnings == AppSettings.loadShowWarningsPref(activityRule.getActivity()));
    }*/
    /*public static void flipUISettings_lightmap(SuntimesActivity activity)
    {
        boolean shouldShowLightmap = !AppSettings.loadShowLightmapPref(activity);
        new SuntimesActivityTest.MainActivityRobot()
                .assertShown_lightmap(activity);

        SettingsActivityRobot robot = new SettingsActivityRobot();
        robot.clickHeader_userInterfaceSettings()
                .assertShown_userInterfaceSettings(activity)
                .clickPrefCheckBox("app_ui_showlightmap", shouldShowLightmap);

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
        assertEquals(shouldShowLightmap, AppSettings.loadShowLightmapPref(activity));
        new SuntimesActivityTest.MainActivityRobot()
                .assertShown_lightmap(activity);
    }*/
    /*public static void inputUISettings_uiWarnings(Context context, boolean checked)
    {
        Matcher<Preference> pref = allOf(instanceOf(Preference.class), withKey("app_ui_showwarnings"));
        Matcher<View> list = allOf(instanceOf(ListView.class), isDisplayed());
        ViewInteractionHelper.ViewInteractionInterface warningPref = ViewInteractionHelper.wrap(onData(pref).inAdapterView(list).onChildView(withClassName(is(CheckBox.class.getName()))).check(assertShown));

        boolean prefChecked = viewIsChecked(warningPref);
        if (prefChecked && !checked || !prefChecked && checked)
        {
            warningPref.perform(click());
        }
    }*/
    /*public static void flipUISettings_uiWarnings(SuntimesActivity activity)
    {
        boolean shouldShowWarnings = !AppSettings.loadShowWarningsPref(activity);
        SettingsActivityRobot robot = new SettingsActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.clickHeader_userInterfaceSettings()
                .assertShown_userInterfaceSettings(activity)
                .clickPrefCheckBox("app_ui_showwarnings", shouldShowWarnings);

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
        assertEquals(shouldShowWarnings, AppSettings.loadShowWarningsPref(activity));
    }*/

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * SettingsActivityRobot
     */
    public static class SettingsActivityRobot extends ActivityRobot<SettingsActivityRobot>
    {
        public SettingsActivityRobot() {
            setRobot(this);
        }

        public SettingsActivityRobot showActivity(Activity activity)
        {
            if (AppSettings.loadNavModePref(activity).equals(AppSettings.NAVIGATION_SIDEBAR)) {
                new SuntimesActivityTest.MainActivityRobot()
                        .showSidebarMenu(activity)
                        .clickSidebarMenu_settings(activity);
            } else {
                new SuntimesActivityTest.MainActivityRobot()
                        .showOverflowMenu(activity)
                        .clickOverflowMenu_settings();
            }
            return this;
        }

        public SettingsActivityRobot pressBack() {
            onView(isRoot()).perform(ViewActions.pressBack());
            return this;
        }

        public SettingsActivityRobot clickHeader_generalSettings() {
            onView(withText(R.string.configLabel_general)).perform(click());
            return this;
        }
        public SettingsActivityRobot clickHeader_alarmSettings() {
            onView(withText(R.string.configLabel_alarmClock)).perform(click());
            return this;
        }
        public SettingsActivityRobot clickHeader_userInterfaceSettings() {
            onView(withText(R.string.configLabel_ui)).perform(click());
            return this;
        }
        public SettingsActivityRobot clickHeader_localeSettings() {
            onView(withText(R.string.configLabel_locale)).perform(click());
            return this;
        }
        public SettingsActivityRobot clickHeader_placeSettings() {
            onView(withText(R.string.configLabel_places)).perform(click());
            return this;
        }
        public SettingsActivityRobot clickHeader_widgetList() {
            onView(withText(R.string.configLabel_widgetList)).perform(click());
            return this;
        }

        public SettingsActivityRobot clickPref_managePlaces() {
            onView(withText(R.string.configLabel_places_manage)).perform(scrollTo(), click());
            return this;
        }
        public SettingsActivityRobot clickPref_manageEvents() {
            onView(withText(R.string.configLabel_manageEvents)).perform(scrollTo(), click());
            return this;
        }
        public SettingsActivityRobot clickPref_welcomeWizard() {
            onView(withText(R.string.welcome_pref_title)).perform(scrollTo(), click());
            return this;
        }
        public SettingsActivityRobot clickPref_brightAlarmColors() {
            preferenceWithKey("app_alarms_bright_colors").get().perform(scrollTo(), click());
            return this;
        }
        public SettingsActivityRobot clickPrefButton_darkAppColors()
        {
            preferenceWithKey("app_appearance_theme_dark").get().perform(scrollTo());
            preferenceButtonWithTag("app_appearance_theme_dark").get().perform(click());
            return this;
        }
        public SettingsActivityRobot clickPrefButton_lightAppColors() {
            preferenceWithKey("app_appearance_theme_light").get().perform(scrollTo());
            preferenceButtonWithTag("app_appearance_theme_light").get().perform(click());
            return this;
        }
        public SettingsActivityRobot clickPrefCheckBox(String key, boolean checked) {
            ViewInteractionHelper.ViewInteractionInterface preference = ViewInteractionHelper.wrap(preferenceWithKey(key).get().check(assertShown));
            boolean prefChecked = ViewInteractionHelper.viewIsChecked(preference.get());
            if (prefChecked && !checked || !prefChecked && checked) {
                preference.get().perform(click());
            }
            return this;
        }

        //////////////////////////////////////////////////////////////

        protected DataInteractionHelper.DataInteractionInterface preferenceWithKey(String key) {
            return DataInteractionHelper.wrap(
                    onData(allOf(instanceOf(Preference.class), withKey(key)))
                            .inAdapterView(allOf(instanceOf(ListView.class), isDisplayed()))
            );
        }
        protected DataInteractionHelper.DataInteractionInterface preferenceCheckBoxWithKey(String key) {
            return DataInteractionHelper.wrap(
                    preferenceWithKey(key).get().onChildView(withClassName(is(CheckBox.class.getName())))
            );
        }
        protected ViewInteractionHelper.ViewInteractionInterface preferenceButtonWithTag(Object tag) {
            return ViewInteractionHelper.wrap(
                    onView(allOf(withId(R.id.actionButton0), withTagValue(is(tag))))
            );
        }

        //////////////////////////////////////////////////////////////

        public SettingsActivityRobot assertActivityShown(Activity activity)
        {
            onView(withIndex(withText(R.string.configLabel_general),0)).check(assertShown);
            onView(withIndex(withText(R.string.configLabel_locale),0)).check(assertShown);
            onView(withIndex(withText(R.string.configLabel_places),0)).check(assertShown);
            onView(withIndex(withText(R.string.configLabel_ui),0)).check(assertShown);
            onView(withIndex(withText(R.string.configLabel_widgetList),0)).check(assertShown);
            return this;
        }

        public SettingsActivityRobot assertShown_generalSettings(Activity context)
        {
            preferenceWithKey("appwidget_0_intro_screen").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_lengthunits").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_localize_hemisphere").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_showseconds").get().check(assertShown);
            preferenceWithKey("appwidget_0_location_altitude_enabled").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_calculator_help").get().check(assertShown);
            assertShown_timeFormatMode(context);
            assertShown_dataSource(context);
            return this;
        }
        public SettingsActivityRobot assertShown_userInterfaceSettings(Activity context)
        {
            assertShown_theme(context);
            preferenceWithKey("app_appearance_theme_dark").get().check(assertShown);
            preferenceWithKey("app_appearance_theme_light").get().check(assertShown);
            preferenceWithKey("app_appearance_textsize").get().check(assertShown);

            preferenceWithKey("app_navigation_mode").get().check(assertShown);
            preferenceWithKey("app_launcher_mode").get().check(assertShown);

            preferenceWithKey("manage_events").get().check(assertShown);
            for (int i=0; i<7; i++) {
                preferenceWithKey("app_ui_showfields_" + i).get().check(assertShown);
            }

            preferenceWithKey("app_ui_showmoon").get().check(assertShown);
            preferenceWithKey("app_ui_showmoon_noon").get().check(assertShown);

            preferenceWithKey("app_ui_showequinox").get().check(assertShown);
            preferenceWithKey("app_ui_showequinox_date").get().check(assertShown);
            preferenceWithKey("app_ui_showcrossquarter").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_trackingmode").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_observerheight").get().check(assertShown);

            preferenceWithKey("app_ui_showlightmap").get().check(assertShown);
            preferenceCheckBoxWithKey("app_ui_showlightmap").get().check(
                    AppSettings.loadShowLightmapPref(context) ? assertChecked : assertNotChecked);

            preferenceWithKey("app_ui_showwarnings").get().check(assertShown);
            preferenceCheckBoxWithKey("app_ui_showwarnings").get().check(
                    AppSettings.loadShowWarningsPref(context) ? assertChecked : assertNotChecked);

            preferenceWithKey("app_ui_emphasizefield").get().check(assertShown);
            preferenceWithKey("app_ui_showmapbutton").get().check(assertShown);
            preferenceWithKey("app_ui_showheader_icon").get().check(assertShown);
            preferenceWithKey("app_ui_showheader_text1").get().check(assertShown);

            preferenceWithKey("appwidget_0_general_showcompare").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_comparemode").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_showweeks").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_showhours").get().check(assertShown);
            preferenceWithKey("appwidget_0_general_showtimedate").get().check(assertShown);

            preferenceWithKey("app_ui_clocktapaction").get().check(assertShown);
            preferenceWithKey("app_ui_datetapaction").get().check(assertShown);
            preferenceWithKey("app_ui_datetapaction1").get().check(assertShown);
            preferenceWithKey("app_ui_notetapaction").get().check(assertShown);
            preferenceWithKey("app_accessibility_verbose").get().check(assertShown);
            return this;
        }

        public SettingsActivityRobot assertShown_alarmSettings(Activity context)
        {
            preferenceWithKey("app_alarms_batterytopt").get().check(assertShown);
            preferenceWithKey("app_alarms_notifications").get().check(assertShown);
            preferenceWithKey("app_alarms_dismiss_challenge").get().check(assertShown);
            preferenceWithKey("app_alarms_upcomingMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_snoozeMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_snoozeLimit").get().check(assertShown);
            preferenceWithKey("app_alarms_timeoutMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_dnd_permission").get().check(assertShown);
            preferenceWithKey("app_bedtime_dnd_rulebased").get().check(assertShown);
            preferenceWithKey("app_alarms_bright").get().check(assertShown);
            preferenceWithKey("app_alarms_bright_colors").get().check(assertShown);
            preferenceWithKey("app_alarms_bright_fadeinMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_notifyDismissMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_volumes").get().check(assertShown);
            preferenceWithKey("app_alarms_fadeinMillis").get().check(assertShown);
            preferenceWithKey("app_alarms_fadeinMethod").get().check(assertShown);
            preferenceWithKey("app_alarms_bootcompleted").get().check(assertShown);
            preferenceWithKey("app_alarms_poweroffalarms").get().check(assertShown);
            preferenceWithKey("dialog_importwarning_donotshowagain").get().check(assertShown);

            if (AlarmSettings.hasAutostartSettings(context)) {
                preferenceWithKey("app_alarms_autostart").get().check(assertShown);
            }
            if (Build.VERSION.SDK_INT >= 34) {
                preferenceWithKey("app_alarms_notifications_fullscreen").get().check(assertShown);
            }
            return this;
        }
        public SettingsActivityRobot assertShown_localeSettings(Activity context)
        {
            AppSettings.LocaleMode mode = AppSettings.loadLocaleModePref(context);
            DataInteractionHelper.DataInteractionInterface modePref = preferenceWithKey("app_locale_mode");
            DataInteractionHelper.DataInteractionInterface modePref_text = DataInteractionHelper.wrap(modePref.get().onChildView(allOf(withClassName(is(TextView.class.getName())), withText(mode.getDisplayString()))));
            modePref.get().check(assertShown);
            modePref_text.get().check(assertShown);

            String language = getLocaleDisplayString(context, AppSettings.loadLocalePref(context));
            DataInteractionHelper.DataInteractionInterface langPref = preferenceWithKey("app_locale");
            DataInteractionHelper.DataInteractionInterface langPref_text = DataInteractionHelper.wrap(langPref.get().onChildView(allOf(withClassName(endsWith("TextView")), withText(containsString(language)))));
            langPref.get().check(assertShown);
            langPref.get().check(mode == AppSettings.LocaleMode.SYSTEM_LOCALE ? assertDisabled : assertEnabled);    // language disabled for system mode
            langPref_text.get().check(assertShown);
            return this;
        }
        public SettingsActivityRobot assertShown_placeSettings(Activity context) {
            preferenceWithKey("places_manage").get().check(assertShown);
            preferenceWithKey("getFix_maxAge").get().check(assertShown);
            preferenceWithKey("getFix_maxElapsed").get().check(assertShown);
            preferenceWithKey("getFix_passiveMode").get().check(assertShown);
            return this;
        }

        public SettingsActivityRobot assertShown_timeFormatMode(Context context)
        {
            DataInteractionHelper.DataInteractionInterface formatPref = preferenceWithKey("appwidget_0_appearance_timeformatmode");
            formatPref.get().check(assertEnabled);

            TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
            String modeSummary = String.format(GeneralPrefsFragment.timeFormatPrefSummary(mode, context), mode.getDisplayString());
            DataInteractionHelper.DataInteractionInterface formatPref_text = DataInteractionHelper.wrap(formatPref.get().onChildView(allOf(withClassName(is(TextView.class.getName())), withText(modeSummary))));
            formatPref_text.get().check(assertShown);
            return this;
        }
        public SettingsActivityRobot assertShown_dataSource(Context context)
        {
            DataInteractionHelper.DataInteractionInterface dataSourcePref = preferenceWithKey("appwidget_0_general_calculator");
            dataSourcePref.get().check(assertEnabled);

            SuntimesCalculatorDescriptor dataSource = WidgetSettings.loadCalculatorModePref(context, 0);
            DataInteractionHelper.DataInteractionInterface dataSourcePref_text = DataInteractionHelper.wrap(dataSourcePref.get().onChildView(allOf(withClassName(is(TextView.class.getName())), withText(dataSource.getName()))));
            dataSourcePref_text.get().check(assertShown);
            return this;
        }
        public SettingsActivityRobot assertShown_theme(Activity activity)
        {
            DataInteractionHelper.DataInteractionInterface themePref = preferenceWithKey("app_appearance_theme");
            themePref.get().check(assertEnabled);

            String themeName = AppSettings.loadThemePref(activity);
            String themeDisplay = getThemeDisplayString(activity, themeName);

            DataInteractionHelper.DataInteractionInterface themePref_text = DataInteractionHelper.wrap(themePref.get().onChildView(allOf(withClassName(is(TextView.class.getName())), withText(themeDisplay))));
            themePref_text.get().check(assertShown);
            return this;
        }

        /**
         * @param context  a context used to access resources
         * @param themeName theme name
         * @return a localized display string for given theme
         */
        private static String getThemeDisplayString(Context context, @NonNull String themeName)
        {
            String[] values = context.getResources().getStringArray(R.array.appThemes_values);
            for (int i = 0; i < values.length; i++) {
                if (themeName.startsWith(values[i])) {
                    return context.getResources().getStringArray(R.array.appThemes_display)[i];
                }
            }
            return "";
        }

        /**
         * @param context a context used to access resources
         * @param localeId locale ID
         * @return a localized display string for given locale
         */
        private static String getLocaleDisplayString(Context context, String localeId)
        {
            String[] values = context.getResources().getStringArray(R.array.locale_values);
            for (int i = 0; i < values.length; i++)
            {
                if (values[i].equals(localeId))
                    return context.getResources().getStringArray(R.array.locale_display)[i];
            }
            return "";
        }
    }

}
