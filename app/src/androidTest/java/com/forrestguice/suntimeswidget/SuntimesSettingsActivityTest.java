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
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesSettingsActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    /**
     * UI Test
     * test_showSettingsActivity
     */
    @Test
    public void test_showSettingsActivity()
    {
        showSettingsActivity(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings0");

        showGeneralSettings(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-general0");
        onView(isRoot()).perform(pressBack());

        showLocaleSettings(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-locale0");
        onView(isRoot()).perform(pressBack());

        showPlacesSettings(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-places0");
        onView(isRoot()).perform(pressBack());

        showUISettings(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-ui0");
        onView(isRoot()).perform(pressBack());

        showWidgetSettings(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-settings-widgets0");
        onView(isRoot()).perform(pressBack());

        onView(isRoot()).perform(pressBack());
    }

    public static void showSettingsActivity(Activity activity)
    {
        String actionSettingsText = activity.getString(R.string.configAction_settings);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionSettingsText)).perform(click());
        verifySettingsActivity(activity);
    }

    public static void verifySettingsActivity(Activity activity)
    {
        onView(withIndex(withText(activity.getString(R.string.configLabel_general)),0)).check(assertShown);
        onView(withIndex(withText(activity.getString(R.string.configLabel_locale)),0)).check(assertShown);
        onView(withIndex(withText(activity.getString(R.string.configLabel_places)),0)).check(assertShown);
        onView(withIndex(withText(activity.getString(R.string.configLabel_ui)),0)).check(assertShown);
        onView(withIndex(withText(activity.getString(R.string.configLabel_widgetList)),0)).check(assertShown);
    }

    /**
     * UI Test
     * test_showSettingsActivity_general
     */
    @Test
    public void test_showSettingsActivity_general()
    {
        showSettingsActivity(activityRule.getActivity());
        showGeneralSettings(activityRule.getActivity());

    }

    public static void showGeneralSettings(Activity activity)
    {
        onView(withIndex(withText(activity.getString(R.string.configLabel_general)),0)).perform(click());
        verifyGeneralSettings(activity);
    }

    public static void verifyGeneralSettings(Context context)
    {
        verifyUISettings_timeFormatMode(context);
        verifyGeneralSettings_dataSource(context);
    }

    public static void verifyGeneralSettings_dataSource(Context context)
    {
        DataInteraction dataSourcePref = onData(allOf( is(instanceOf(Preference.class)), withKey("appwidget_0_general_calculator")) ).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        dataSourcePref.check(assertEnabled);

        SuntimesCalculatorDescriptor dataSource = WidgetSettings.loadCalculatorModePref(context, 0);
        DataInteraction dataSourcePref_text = dataSourcePref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(dataSource.getName())));
        dataSourcePref_text.check(assertShown);
    }

    public static void verifyPlacesSettings_gpsTimeLimit(Context context)
    {
        DataInteraction gpsTimePref = onData(
                allOf( is(instanceOf(Preference.class)), withKey("getFix_maxElapsed")) )
                .inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        gpsTimePref.check(assertEnabled);
        // TODO: verify correct setting
    }

    public static void verifyPlacesSettings_gpsMaxAge(Context context)
    {
        DataInteraction gpsAgePref = onData(
                allOf( is(instanceOf(Preference.class)), withKey("getFix_maxAge")) )
                .inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))) );
        gpsAgePref.check(assertEnabled);
        // TODO: verify correct setting
    }

    /**
     * UI Test
     * test_showSettingsActivity_locale
     */
    @Test
    public void test_showSettingsActivity_locale()
    {
        showSettingsActivity(activityRule.getActivity());
        showLocaleSettings(activityRule.getActivity());
    }

    public static void showLocaleSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_locale))).perform(click());
        verifyLocaleSettings(activity);
    }

    public static void verifyLocaleSettings(Context context)
    {
        // verify the mode selector
        DataInteraction modePref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_locale_mode")))
                .inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        modePref.check(assertEnabled);

        AppSettings.LocaleMode mode = AppSettings.loadLocaleModePref(context);
        DataInteraction modePref_text = modePref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(mode.getDisplayString())));
        modePref_text.check(assertShown);

        // verify the language selector
        DataInteraction langPref = onData(allOf(is(instanceOf(Preference.class)), withKey("app_locale")))
                .inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        langPref.check(assertShown);

        if (mode == AppSettings.LocaleMode.SYSTEM_LOCALE)
            langPref.check(assertDisabled);       // language disabled for system mode
        else langPref.check(assertEnabled);

        String lang = getLocaleDisplayString(context, AppSettings.loadLocalePref(context));
        DataInteraction langPref_text = langPref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(lang)));
        langPref_text.check(assertShown);
    }

    /**
     * UI Test
     * test_showSettingsActivity_places
     */
    @Test
    public void test_showSettingsActivity_places()
    {
        showSettingsActivity(activityRule.getActivity());
        showPlacesSettings(activityRule.getActivity());

        showClearPlacesDialog(activityRule.getActivity());
        onView(isRoot()).perform(pressBack());

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
    }

    public static void showPlacesSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_places))).perform(click());
        verifyPlacesSettings(activity);
    }

    public static void verifyPlacesSettings(Context context)
    {
        verifyPlacesSettings_gpsTimeLimit(context);
        verifyPlacesSettings_gpsMaxAge(context);
        //onView(withText(context.getString(R.string.configLabel_places_export))).check(assertEnabled);  // TODO: move to PlacesActivity
        //onView(withText(context.getString(R.string.configLabel_places_clear))).check(assertEnabled);
    }

    public static void showClearPlacesDialog(Context context)
    {
        //onView(withText(context.getString(R.string.configLabel_places_clear))).perform(click());  // TODO: move to PlacesActivity
        //verifyClearPlacesDialog(context);
    }

    public static void verifyClearPlacesDialog(Context context)
    {
        onView(withText(context.getString(R.string.locationclear_dialog_message))).check(assertShown);
    }

    /**
     * UI Test
     * test_showSettingsActivity_ui
     */
    @Test
    public void test_showSettingsActivity_ui()
    {
        showSettingsActivity(activityRule.getActivity());
        showUISettings(activityRule.getActivity());
        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());

        boolean showLightmap = AppSettings.loadShowLightmapPref(activityRule.getActivity());
        flipUISettings_lightmap(activityRule.getActivity());
        flipUISettings_lightmap(activityRule.getActivity());
        assertTrue(showLightmap == AppSettings.loadShowLightmapPref(activityRule.getActivity()));

        boolean showWarnings = AppSettings.loadShowWarningsPref(activityRule.getActivity());
        flipUISettings_uiWarnings(activityRule.getActivity());
        flipUISettings_uiWarnings(activityRule.getActivity());
        assertTrue(showWarnings == AppSettings.loadShowWarningsPref(activityRule.getActivity()));
    }

    public static void showUISettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_ui))).perform(click());
        verifyUISettings(activity);
    }

    public static void verifyUISettings(Activity context)
    {
        verifyUISettings_theme(context);
        verifyUISettings_clockTap(context);
        verifyUISettings_dateTap(context);
        verifyUISettings_noteTap(context);
        verifyUISettings_lightmap(context);
        verifyUISettings_uiWarnings(context);
    }

    public static void inputUISettings_lightmap(Context context, boolean checked)
    {
        Matcher<Preference> pref = allOf(is(instanceOf(Preference.class)), withKey("app_ui_showlightmap"));
        Matcher<View> list = allOf(is(instanceOf(ListView.class)), hasFocus());
        ViewInteraction lightmapPref = onData(pref).inAdapterView(list).onChildView(withClassName(is(CheckBox.class.getName()))).check(assertShown);

        boolean prefChecked = viewIsChecked(lightmapPref);
        if (prefChecked && !checked || !prefChecked && checked)
        {
            lightmapPref.perform(click());
        }
    }

    public static void flipUISettings_lightmap(SuntimesActivity activity)
    {
        SuntimesActivityTest.verifyLightmap(activity);
        showSettingsActivity(activity);
        showUISettings(activity);

        boolean shouldShowLightmap = !AppSettings.loadShowLightmapPref(activity);
        inputUISettings_lightmap(activity, shouldShowLightmap);

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
        assertTrue(shouldShowLightmap == AppSettings.loadShowLightmapPref(activity));
        SuntimesActivityTest.verifyLightmap(activity);
    }

    public static void verifyUISettings_lightmap(Context context)
    {
        DataInteraction lightmapPref = onData(allOf(is(instanceOf(Preference.class)), withKey("app_ui_showlightmap"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        lightmapPref.check(assertEnabled);

        DataInteraction lightmapPref_checkBox = lightmapPref.onChildView(withClassName(is(CheckBox.class.getName())));
        if (AppSettings.loadShowLightmapPref(context))
            lightmapPref_checkBox.check(assertChecked);
        else lightmapPref_checkBox.check(assertNotChecked);
    }

    public static void inputUISettings_uiWarnings(Context context, boolean checked)
    {
        Matcher<Preference> pref = allOf(is(instanceOf(Preference.class)), withKey("app_ui_showwarnings"));
        Matcher<View> list = allOf(is(instanceOf(ListView.class)), hasFocus());
        ViewInteraction warningPref = onData(pref).inAdapterView(list).onChildView(withClassName(is(CheckBox.class.getName()))).check(assertShown);

        boolean prefChecked = viewIsChecked(warningPref);
        if (prefChecked && !checked || !prefChecked && checked)
        {
            warningPref.perform(click());
        }
    }

    public static void flipUISettings_uiWarnings(SuntimesActivity activity)
    {
        showSettingsActivity(activity);
        showUISettings(activity);

        boolean shouldShowWarnings = !AppSettings.loadShowWarningsPref(activity);
        inputUISettings_uiWarnings(activity, shouldShowWarnings);

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
        assertTrue(shouldShowWarnings == AppSettings.loadShowWarningsPref(activity));
    }

    public static void verifyUISettings_uiWarnings(Context context)
    {
        DataInteraction warningPref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_ui_showwarnings"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))) );
        warningPref.check(assertEnabled);

        DataInteraction warningPref_checkBox = warningPref.onChildView(withClassName(is(CheckBox.class.getName())));
        if (AppSettings.loadShowWarningsPref(context))
            warningPref_checkBox.check(assertChecked);
        else warningPref_checkBox.check(assertNotChecked);
    }

    public static void verifyUISettings_noteTap(Context context)
    {
        DataInteraction notetapPref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_ui_notetapaction"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        notetapPref.check(assertEnabled);

        //AppSettings.ClockTapAction action = AppSettings.loadNoteTapActionPref(context);
        //DataInteraction notetapPref_text = notetapPref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(containsString(action.getDisplayString()))));
        //notetapPref_text.check(assertShown);
        // TODO
    }

    public static void verifyUISettings_dateTap(Context context)
    {
        DataInteraction datetapPref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_ui_datetapaction"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))) );
        datetapPref.check(assertEnabled);

        //AppSettings.DateTapAction action = AppSettings.loadDateTapActionPref(context);
        //DataInteraction datetapPref_text = datetapPref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(containsString(action.getDisplayString()))));
        //datetapPref_text.check(assertShown);
        // TODO
    }

    public static void verifyUISettings_clockTap(Context context)
    {
        DataInteraction clocktapPref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_ui_clocktapaction"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))) );
        clocktapPref.check(assertEnabled);

        //AppSettings.ClockTapAction action = AppSettings.loadClockTapActionPref(context);
        //DataInteraction clocktapPref_text = clocktapPref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(containsString(action.getDisplayString()))));
        //clocktapPref_text.check(assertShown);
        // TODO
    }

    public static void verifyUISettings_timeFormatMode(Context context)
    {
        DataInteraction formatPref = onData(allOf( is(instanceOf(Preference.class)), withKey("appwidget_0_appearance_timeformatmode"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        formatPref.check(assertEnabled);

        WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
        String modeSummary = String.format(SuntimesSettingsActivity.timeFormatPrefSummary(mode, context), mode.getDisplayString());
        DataInteraction formatPref_text = formatPref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(modeSummary)));
        formatPref_text.check(assertShown);
    }

    public static void verifyUISettings_theme(Activity activity)
    {
        DataInteraction themePref = onData(allOf( is(instanceOf(Preference.class)), withKey("app_appearance_theme"))).inAdapterView(allOf(hasFocus(), is(instanceOf(ListView.class))));
        themePref.check(assertEnabled);

        String themeName = AppSettings.loadThemePref(activity);
        String themeDisplay = getThemeDisplayString(activity, themeName);
        DataInteraction themePref_text = themePref.onChildView(allOf(withClassName(is(TextView.class.getName())), withText(themeDisplay)));
        themePref_text.check(assertShown);
    }

    /**
     * UI Test
     * test_showSettingsActivity_widgets
     */
    @Test
    public void test_showSettingsActivity_widgets()
    {
        showSettingsActivity(activityRule.getActivity());
        showWidgetSettings(activityRule.getActivity());
    }

    public static void showWidgetSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_widgetList))).perform(click());
        verifyWidgetSettings(activity);
    }

    public static void verifyWidgetSettings(@NonNull Context context)
    {
        ArrayAdapter widgetAdapter = SuntimesWidgetListActivity.WidgetListAdapter.createWidgetListAdapter(context);
        if (widgetAdapter.isEmpty())
            onView(withId(android.R.id.empty)).check(assertEnabled);
        else onView(withId(R.id.widgetList)).check(assertEnabled);
    }

    /**
     * @param context  a context used to access resources
     * @param themeName theme name
     * @return a localized display string for given theme
     */
    private static String getThemeDisplayString(Context context, String themeName)
    {
        String[] values = context.getResources().getStringArray(R.array.appThemes_values);
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(themeName))
                return context.getResources().getStringArray(R.array.appThemes_display)[i];
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

    /* https://stackoverflow.com/a/39756832 */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

}
