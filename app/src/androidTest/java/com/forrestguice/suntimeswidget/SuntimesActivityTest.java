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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingResource;
import androidx.test.filters.LargeTest;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialogTest;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.equinox.EquinoxCardDialogTest;
import com.forrestguice.suntimeswidget.getfix.LocationDialogTest;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.notes.NoteData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper;
import com.forrestguice.util.text.TimeDisplayText;

import org.hamcrest.Matcher;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.registerIdlingResources;
import static androidx.test.espresso.Espresso.unregisterIdlingResources;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class SuntimesActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

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

    /**
     * UI Test; open the activity, take a screenshot, swap the card, take a screenshot, and then rotate.
     */
    @Test
    public void test_mainActivity()
    {
        verifyActivity((SuntimesActivity) activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-main0");

        MainActivityRobot robot = new MainActivityRobot()
                .swapCard(activityRule.getActivity());
        verifyTimeCard();
        robot.captureScreenshot(activityRule.getActivity(), "suntimes-activity-main1");

        robot.doubleRotateDevice(activityRule.getActivity());
        verifyActivity((SuntimesActivity) activityRule.getActivity());
    }

    public static void verifyActivity(SuntimesActivity activity)
    {
        verifyTheme(activity);
        verifyLocale(activity);
        verifyActionBar(activity);
        verifyClock(activity);
        verifyNote(activity);
        verifyTimeCard();
        new MainActivityRobot()
                .assertShown_lightmap(true)
                .assertShown_solsticeEquinox(activity, true)
                .assertShown_dataSourceUI(activity, true);
    }

    public static void verifyTheme(SuntimesActivity activity)
    {
        String themeName = AppSettings.loadThemePref(activity);
        int themeId = AppSettings.themePrefToStyleId(activity, themeName, activity.dataset.dataActual);
        int loadedStyleId = activity.getThemeId();
        Log.d("TEST", "themeId = " + themeId + " (" + themeName + "), loaded = " + loadedStyleId );
        assertTrue(loadedStyleId == themeId);
    }

    public static void verifyLocale(SuntimesActivity activity)
    {
        AppSettings.LocaleMode mode = AppSettings.loadLocaleModePref(activity);
        if (mode == AppSettings.LocaleMode.CUSTOM_LOCALE)
        {
            String customLocale = AppSettings.loadLocalePref(activity);
            String loadedLocale = Locale.getDefault().getLanguage();
            Log.d("TEST", "customLocale = " + customLocale + " , loaded = " + loadedLocale);
            assertTrue(loadedLocale.equals(customLocale));
        }
    }

    public static void verifyActionBar(Activity activity)
    {
        onView(withId(R.id.action_location_add)).check(assertShown);

        LocationMode mode = WidgetSettings.loadLocationModePref(activity, 0);
        if (mode == LocationMode.CURRENT_LOCATION)
        {
            onView(withId(R.id.action_location_refresh)).check(assertShown);

        } else {
            onView(withId(R.id.action_location_refresh)).check(doesNotExist());
        }
    }

    public static void verifyClock(SuntimesActivity activity)
    {
        SuntimesRiseSetDataset dataset = activity.dataset;
        TimeDisplayText timeText = SuntimesActivity.utils.calendarTimeShortDisplayString(activity, dataset.now());
        String timezoneID = dataset.timezone().getID();

        onView(withId(R.id.text_time)).check(assertShown);
        onView(withId(R.id.text_time)).check(matches(withText(timeText.getValue())));

        if (!SuntimesUtils.is24()) {
            onView(withId(R.id.text_time_suffix)).check(assertShown);
            onView(withId(R.id.text_time_suffix)).check(matches(withText(timeText.getSuffix())));
        }

        onView(withId(R.id.text_timezone)).check(assertShown);
        onView(withId(R.id.text_timezone)).check(matches(withText(containsString(timezoneID))));

        //onView(withId(R.id.layout_clock)).check(ViewAssertionHelper.assertClickable);
    }

    public static void verifyNote(SuntimesActivity activity)
    {
        onView(withId(R.id.info_note_flipper)).check(assertShown);

        NoteData note = activity.notes.getNote( activity.notes.getNoteIndex() );
        onView(allOf(withId(R.id.text_timenote1), isDisplayed())).check(matches(withText(containsString(note.timeText.getValue()))));
        onView(allOf(withId(R.id.text_timenote2), isDisplayed())).check(matches(withText(containsString(note.timeText.getSuffix()))));
        onView(allOf(withId(R.id.text_timenote3), isDisplayed())).check(matches(withText(containsString(note.noteText))));
    }

    @Test
    public void test_mainActivity_navigation_simple()
    {
        Activity context = activityRule.getActivity();
        config(context).edit().putString(AppSettings.PREF_KEY_NAVIGATION_MODE, AppSettings.NAVIGATION_SIMPLE).apply();

        MainActivityRobot robot = new MainActivityRobot();
        robot.recreateActivity(context)
                .assertActionBar_navButtonShown(false);

        robot.showOverflowMenu(context)
                .sleep(1000)
                .assertOverflowMenuShown(context)
                .assertOverflowMenu_hasSimpleNavigation(true)
                .cancelOverflowMenu(context);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_about()
                .sleep(1000);
        new DialogTest.AboutDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context);
        robot.assertActivityShown(context);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_settings()
                .sleep(1000);
        new SuntimesSettingsActivityTest.SettingsActivityRobot()
                .assertActivityShown(context)
                .pressBack();
        robot.assertActivityShown(context);
    }

    @Test
    public void test_mainActivity_navigation_sidebar()
    {
        Activity context = activityRule.getActivity();
        config(context).edit().putString(AppSettings.PREF_KEY_NAVIGATION_MODE, AppSettings.NAVIGATION_SIDEBAR).apply();

        MainActivityRobot robot = new MainActivityRobot();
        robot.recreateActivity(context);
        robot.showOverflowMenu(context).sleep(1000)
                .assertOverflowMenuShown(context)
                .assertOverflowMenu_hasSimpleNavigation(false)
                .cancelOverflowMenu(context);

        robot.assertActionBar_navButtonShown(true)
                .showSidebarMenu(context)
                .assertSideBarMenuShown(context)
                .cancelSidebarMenu(context);
    }

    @Test
    public void test_mainActivity_navigation_mapButton()
    {
        Activity context = activityRule.getActivity();
        WidgetSettings.saveLocationModePref(context, 0, LocationMode.CUSTOM_LOCATION);
        config(context).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMAPBUTTON, true).apply();
        MainActivityRobot robot = new MainActivityRobot();

        // map button [enabled]
        robot.recreateActivity(context).sleep(1000)
                .assertActionBar_mapButtonShown(true);

        robot.showOverflowMenu(context).sleep(1000)
                .assertOverflowMenuShown(context)
                .assertOverflowMenu_mapButtonShown(context, false)
                .cancelOverflowMenu(context);

        // map button [disabled]
        config(context).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMAPBUTTON, false).apply();
        robot.recreateActivity(context).sleep(1000)
                .assertActionBar_mapButtonShown(false);

        robot.showOverflowMenu(context).sleep(1000)
                .assertOverflowMenuShown(context)
                .assertOverflowMenu_mapButtonShown(context, true)
                .cancelOverflowMenu(context);
    }

    /**
     * UI Test
     * Set the location using the location dialog.
     */
    @Test
    public void test_setLocation_custom()
    {
        String[] name = {TESTLOC_0_LABEL, TESTLOC_1_LABEL};
        String[] lat  = {TESTLOC_0_LAT, TESTLOC_1_LAT };
        String[] lon  = {TESTLOC_0_LON, TESTLOC_1_LON };

        LocationDialogTest.LocationDialogRobot robot = new LocationDialogTest.LocationDialogRobot();
        for (int i=0; i<name.length; i++)
        {
            robot.showDialog(activityRule.getActivity())
                    .assertDialogShown(activityRule.getActivity())
                    .selectLocationMode(LocationMode.CUSTOM_LOCATION)
                    .clickLocationEditButton()
                    .inputLocationEditValues(name[i], lat[i], lon[i])
                    .assertLocationEditCoordinates(lat[i], lon[i])
                    .applyDialog(activityRule.getActivity());
            // TODO: verify action
        }
    }

    /**
     * UI Test
     * Set the mode to "current location" using the location dialog.
     */
    @Test
    public void test_setLocation_current()
    {
        new LocationDialogTest.LocationDialogRobot()
                .showDialog(activityRule.getActivity())
                .assertDialogShown(activityRule.getActivity())
                .selectLocationMode(LocationMode.CURRENT_LOCATION)
                .assertDialogMode_isCurrent()
                .applyDialog(activityRule.getActivity());
        // TODO: verify action
    }

    /**
     * UI Test; click on the data source label and verify setting activity is displayed.
     */
    @Test
    public void test_mainActivity_dataSourceUI()
    {
        Activity activity = activityRule.getActivity();
        config(activity).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, true).apply();

        // dataSourceUI [enabled]
        new MainActivityRobot()
                .recreateActivity(activity).sleep(1000)
                .assertShown_dataSourceUI(activity, true);

        new MainActivityRobot()
                .clickDataSourceLabel().sleep(1000);

        new SuntimesSettingsActivityTest.SettingsActivityRobot()
                .assertShown_generalSettings(activity)
                .pressBack();

        // dataSourceUI [disabled]
        config(activity).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, false).apply();
        new MainActivityRobot()
                .recreateActivity(activity).sleep(1000)
                .assertShown_dataSourceUI(activityRule.getActivity(), false);
    }

    /**
     * UI Test; click on the lightmap area and verify the lightmap dialog is displayed.
     */
    @Test
    public void test_mainActivity_onLightmapClick()
    {
        Activity context = activityRule.getActivity();
        if (AppSettings.loadShowLightmapPref(context))
        {
            new MainActivityRobot()
                    .clickLightmapField();
            new LightMapDialogTest.LightMapDialogRobot()
                    .assertDialogShown(context)
                    .cancelDialog(context)
                    .assertDialogNotShown(context);

            new MainActivityRobot()
                    .longClickLightmapField();
            new LightMapDialogTest.LightMapDialogRobot()
                    .assertDialogShown(context)
                    .cancelDialog(context)
                    .assertDialogNotShown(context);

        } else {
            onView(withId(R.id.info_time_lightmap)).check(assertHidden);
        }
    }

    /**
     * UI Test: click on the solstice area and verify the solstice dialog is displayed.
     */
    @Test
    public void test_mainActivity_onSolsticeEquinoxClick()
    {
        if (AppSettings.loadShowEquinoxPref(activityRule.getActivity()))
        {
            new MainActivityRobot()
                    .clickSolsticeField();
            new EquinoxCardDialogTest.EquinoxDialogRobot()
                    .assertDialogShown(activityRule.getActivity())
                    .cancelDialog(activityRule.getActivity());

        } else {
            onView(withId(R.id.info_date_solsticequinox)).check(assertHidden);
        }
    }
    @Test
    public void test_mainActivity_onSolsticeEquinoxSwipeNext()
    {
        if (AppSettings.loadShowEquinoxPref(activityRule.getActivity()))
        {
            new MainActivityRobot()
                    .swipeSolsticeField_next().sleep(500)
                    .swipeSolsticeField_prev().sleep(500);
            // TODO

        } else {
            onView(withId(R.id.info_date_solsticequinox)).check(assertHidden);
        }
    }
    @Test
    public void test_mainActivity_onSolsticeEquinoxSwipePrev()
    {
        if (AppSettings.loadShowEquinoxPref(activityRule.getActivity()))
        {
            new MainActivityRobot()
                    .swipeSolsticeField_next().sleep(500)
                    .swipeSolsticeField_prev().sleep(500);
            // TODO

        } else {
            onView(withId(R.id.info_date_solsticequinox)).check(assertHidden);
        }
    }

    /**
     * UI Test; click on the clock area and verify the configured action.
     */
    @Test
    public void test_mainActivity_onClockClick_alarm()
    {
        config(activityRule.getActivity()).edit().putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.ALARM.name()).apply();

        new MainActivityRobot()
                .clickOnClock();
        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.ALARM.name(), noteIndex);
    }

    @Test
    public void test_mainActivity_onClockClick_nextNote()
    {
        config(activityRule.getActivity()).edit().putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.NEXT_NOTE.name()).apply();

        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .clickOnClock();
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.NEXT_NOTE.name(), noteIndex);
    }

    @Test
    public void test_mainActivity_onClockClick_prevNote()
    {
        config(activityRule.getActivity()).edit().putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.PREV_NOTE.name()).apply();

        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .clickOnClock().sleep(1000);
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.PREV_NOTE.name(), noteIndex);
    }

    public static void verifyOnClockClick(SuntimesActivity activity, String tapAction0, int noteIndex)
    {
        String tapAction = AppSettings.loadClockTapActionPref(activity);
        assertEquals(tapAction0, tapAction);

        if (tapAction.equals(WidgetActions.SuntimesAction.ALARM.name()))
        {
            new AlarmCreateDialogTest.AlarmDialogRobot()
                    .assertDialogShown(activity)
                    .cancelDialog(activity).assertDialogNotShown(activity);

        } else if (tapAction.equals(WidgetActions.SuntimesAction.PREV_NOTE.name())) {
            verifyOnNotePrev(activity, noteIndex);

        } else if (tapAction.equals(WidgetActions.SuntimesAction.NEXT_NOTE.name())) {
            verifyOnNoteNext(activity, noteIndex);

        } /**else {
            // TODO
        }*/
    }

    /**
     * UI Test; swipe the note area and verify the note changes (next, prev).
     */
    @Test
    public void test_mainActivity_onNoteSwipe()
    {
        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .swipeNoteNext();
        verifyOnNoteNext(activityRule.getActivity(), noteIndex);

        noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .swipeNotePrev();
        verifyOnNotePrev(activityRule.getActivity(), noteIndex);
    }

    /**
     * UI Test; click the note area and verify the configured action.
     */
    @Test
    public void test_mainActivity_onNoteClick()
    {
        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .clickOnNote();
        verifyOnNoteClick(activityRule.getActivity(), noteIndex);
    }

    public static void verifyOnNoteClick(SuntimesActivity activity, int noteIndex)
    {
        String tapAction = AppSettings.loadNoteTapActionPref(activity);
        if (tapAction.equals(WidgetActions.SuntimesAction.ALARM.name()))
        {
            new AlarmCreateDialogTest.AlarmDialogRobot()
                    .assertDialogShown(activity)
                    .cancelDialog(activity).assertDialogNotShown(activity);

        } else if (tapAction.equals(WidgetActions.SuntimesAction.NEXT_NOTE.name())) {
            verifyOnNoteNext(activity, noteIndex);

        } else if (tapAction.equals(WidgetActions.SuntimesAction.PREV_NOTE.name())) {
            verifyOnNotePrev(activity, noteIndex);

        } /**else {
            // TODO
        }*/
    }

    public static void verifyOnNoteNext(SuntimesActivity activity, int prevNoteIndex)
    {
        int n = activity.notes.noteCount();
        int i = activity.notes.getNoteIndex();     // current index
        int j = (i > 0 && i < n) ? i - 1 : n - 1;  // prev index
        Log.d("DEBUG", "i=" + i + ", j=" + j + ", prev=" + prevNoteIndex);
        assertTrue(j == prevNoteIndex);  // assert prev index matches
        verifyNote(activity);
    }

    public static void verifyOnNotePrev(SuntimesActivity activity, int nextNoteIndex)
    {
        int n = activity.notes.noteCount();
        int i = activity.notes.getNoteIndex();    // current index
        int j = (i >= 0 && i < n-1) ? i + 1 : 0;  // next index
        Log.d("DEBUG", "j=" + j + ", next=" + nextNoteIndex);
        assertTrue(j == nextNoteIndex);  // assert next index matches
        verifyNote(activity);
    }

    /**
     * UI Test; click the date field and verify the configured action.
     */
    @Test
    public void test_mainActivity_onDateClick_configDate()
    {
        config(activityRule.getActivity()).edit().putString(AppSettings.PREF_KEY_UI_DATETAPACTION, WidgetActions.SuntimesAction.CONFIG_DATE.name()).apply();

        String tapAction = AppSettings.loadDateTapActionPref(activityRule.getActivity());
        assertEquals(WidgetActions.SuntimesAction.CONFIG_DATE.name(), tapAction);

        new MainActivityRobot()
                .clickCardDate();
        new TimeDateDialogTest.TimeDateDialogRobot()
                .assertDialogShown(activityRule.getActivity())
                .cancelDialog(activityRule.getActivity());
    }
    // TODO: SHOW_CALENDAR, ...

    /**
     * UI Test; click the date field and verify the configured action.
     */
    @Test
    public void test_mainActivity_onDateClick_swapCard()
    {
        Context context = activityRule.getActivity();
        config(context).edit().putString(AppSettings.PREF_KEY_UI_DATETAPACTION, WidgetActions.SuntimesAction.SWAP_CARD.name()).apply();

        String tapAction = AppSettings.loadDateTapActionPref(context);
        assertEquals(WidgetActions.SuntimesAction.SWAP_CARD.name(), tapAction);

        int c = 0;
        MainActivityRobot robot = new MainActivityRobot();
        while (c < 4)
        {
            c++;
            robot.clickCardDate()
                    .sleep(500);

            if (viewIsDisplayed(R.id.text_date, context.getString(R.string.today)))
                verifyTimeCard_today();
            else if (viewIsDisplayed(R.id.text_date, context.getString(R.string.tomorrow)))
                verifyTimeCard_tomorrow();
            else fail("swapped card does not display 'today' or 'tomorrow'!");
        }
    }

    @Test
    public void test_mainActivity_userSwappedCard_withSwipe()
    {
        new LocationDialogTest.LocationDialogRobot()
                .showDialog(activityRule.getActivity())
                .selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .clickLocationEditButton()
                .inputLocationEditValues(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON)
                .applyDialog(activityRule.getActivity());

        userSwappedCard();
    }

    private void userSwappedCard()
    {
        Matcher<View> cardFlipper = withId(R.id.info_time_flipper1);
        onView(cardFlipper).check(assertShown);   // flipper should be visible

        boolean cardSetToToday = viewIsDisplayed(R.id.text_date, activityRule.getActivity().getString(R.string.today));

        // pre-click checks
        if (cardSetToToday)
            verifyTimeCard_today();
        else verifyTimeCard_tomorrow();

        // click the next/prev button
        if (cardSetToToday)
            new MainActivityRobot().swapCardNext();
        else new MainActivityRobot().swapCardPrev();

        cardSetToToday = !cardSetToToday;

        // post-click checks
        if (cardSetToToday)
            verifyTimeCard_today();
        else verifyTimeCard_tomorrow();

        // wait a minute (and check again)
        long waitTime = 60 * 1000;
        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);

        // during that minute
        // the app will update the clock and note area at least once

        registerIdlingResources(waitForResource);       // afterward...
        verifyTimeCard_today();
        unregisterIdlingResources(waitForResource);
    }



    public static void verifyTimeCard()
    {
        onView(withId(R.id.info_time_flipper1)).check(assertShown);
        // TODO
        //if (viewIsDisplayed(R.id.info_time_all_today, "Today"))
            //verifyTimeCard_today();
        //else
        //verifyTimeCard_tomorrow();
    }

    public void verifyTimeCard(String whichCard)
    {
        Matcher<View> card = withId(R.id.info_time_flipper1);
        onView(card).check(assertShown);

        Matcher<View> dateField = allOf(withId(R.id.text_date), isDescendantOfA(card), withText(containsString(whichCard)));
        onView(dateField).check(assertShown);
        onView(dateField).check(ViewAssertionHelper.assertClickable);

        ArrayList<Matcher<View>> timeFields = timeFields(R.id.info_time_flipper1, dateField);
        for (Matcher<View> field : timeFields) {
            onView(field).check(assertShown);
        }
    }

    public void verifyTimeCard_today() {
        verifyTimeCard("Today");
    }

    public void verifyTimeCard_tomorrow() {
        verifyTimeCard("Tomorrow");
    }

    public ArrayList<Matcher<View>> timeFields(int cardId, Matcher<View> sibling)
    {
        Matcher<View> card = withId(cardId);
        ArrayList<Matcher<View>> timeFields = new ArrayList<>();

        boolean[] fields = AppSettings.loadShowFieldsPref(activityRule.getActivity());
        if (fields[AppSettings.FIELD_ASTRO]) {
            timeFields.add( allOf(withId(R.id.text_time_sunrise_astro), isDescendantOfA(card), hasSibling(sibling)) );
            timeFields.add( allOf(withId(R.id.text_time_sunset_astro), isDescendantOfA(card), hasSibling(sibling)) );
        }
        if (fields[AppSettings.FIELD_NAUTICAL]) {
            timeFields.add( allOf(withId(R.id.text_time_sunrise_nautical), isDescendantOfA(card), hasSibling(sibling)) );
            timeFields.add( allOf(withId(R.id.text_time_sunset_nautical), isDescendantOfA(card), hasSibling(sibling)) );
        }
        if (fields[AppSettings.FIELD_CIVIL]) {
            timeFields.add( allOf(withId(R.id.text_time_sunrise_civil), isDescendantOfA(card), hasSibling(sibling)) );
            timeFields.add( allOf(withId(R.id.text_time_sunset_civil), isDescendantOfA(card), hasSibling(sibling)) );
        }
        if (fields[AppSettings.FIELD_NOON]) {
            timeFields.add( allOf(withId(R.id.text_time_noon), isDescendantOfA(card), hasSibling(sibling)) );
        }
        if (fields[AppSettings.FIELD_ACTUAL]) {
            timeFields.add( allOf(withId(R.id.text_time_sunrise_actual), isDescendantOfA(card), hasSibling(sibling)) );
            timeFields.add( allOf(withId(R.id.text_time_sunset_actual), isDescendantOfA(card), hasSibling(sibling)) );
        }
        return timeFields;
    }

    @Test
    public void test_mainActivity_partialUpdateReciever()
    {
        // test PendingIntent
        final SuntimesActivity activity = (SuntimesActivity) activityRule.getActivity();
        PendingIntent partialUpdateIntent = activity.getPartialUpdateIntent(activity);
        try {
            partialUpdateIntent.send();

        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            fail("CanceledException!");
        }

        // test receiver
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.partialUpdateReceiver.onReceive(activity, new Intent(SuntimesActivity.SUNTIMES_APP_UPDATE_PARTIAL));
                activity.finish();
                assertTrue("app hasn't crashed", activity.isFinishing());
            }
        });
    }

    @Test
    public void test_mainActivity_fullUpdateReciever() {
        test_mainActivity_fullUpdateReciever(activityRule.getActivity());
    }
    public static void test_mainActivity_fullUpdateReciever(final SuntimesActivity activity)
    {
        // test PendingIntent
        PendingIntent fullUpdateIntent = activity.getFullUpdateIntent(activity);
        try {
            fullUpdateIntent.send();

        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            fail("CanceledException!");
        }

        // test receiver
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.fullUpdateReceiver.onReceive(activity, new Intent(SuntimesActivity.SUNTIMES_APP_UPDATE_FULL));
                activity.finish();
                assertTrue("app hasn't crashed", activity.isFinishing());
            }
        });
    }

    @Test
    public void test_mainActivity_about()
    {
        Activity activity = activityRule.getActivity();
        AboutActivityTest.AboutActivityRobot robot = new AboutActivityTest.AboutActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.clickHomeButton(activity);
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
    }

    /**
     * MainActivityRobot
     */
    public static class MainActivityRobot extends ActivityRobot<MainActivityRobot>
    {
        public MainActivityRobot() {
            setRobot(this);
        }

        public MainActivityRobot clickOnClock() {
            onView(withId(R.id.layout_clock)).perform(click());
            return this;
        }
        public MainActivityRobot clickCardDate()
        {
            onView(allOf( withId(R.id.text_date), isDisplayed(),
                    isDescendantOfA(withId(R.id.info_time_all_today))
            )).perform(click());
            return this;
        }
        public MainActivityRobot clickMapButton() {
            onView(withContentDescription(R.string.configAction_mapLocation)).perform(click());
            return this;
        }
        public MainActivityRobot clickDataSourceLabel() {
            onView(withId(R.id.txt_datasource)).perform(click());
            return this;
        }
        public MainActivityRobot clickSolsticeField() {
            onView(allOf(withId(R.id.info_date_solsticequinox), isDisplayed())).perform(click());
            return this;
        }
        public MainActivityRobot swipeSolsticeField_next() {
            onView(allOf(withId(R.id.info_date_solsticequinox), isDisplayed())).perform(swipeLeft());
            return this;
        }
        public MainActivityRobot swipeSolsticeField_prev() {
            onView(allOf(withId(R.id.info_date_solsticequinox), isDisplayed())).perform(swipeRight());
            return this;
        }
        public MainActivityRobot clickLightmapField() {
            onView(allOf(withId(R.id.info_time_lightmap), isDisplayed())).perform(click());
            return this;
        }
        public MainActivityRobot longClickLightmapField() {
            onView(allOf(withId(R.id.info_time_lightmap), isDisplayed())).perform(longClick());
            return this;
        }

        public MainActivityRobot swapCard(Context context) {
            if (viewIsDisplayed(R.id.info_time_all_today, context, R.string.today))
                swapCardNext();
            else swapCardPrev();
            return this;
        }
        public MainActivityRobot swapCardNext() {
            Matcher<View> cardFlipper = withId(R.id.info_time_flipper1);
            onView(cardFlipper).perform(swipeLeft());
            return this;
        }
        public MainActivityRobot swapCardPrev() {
            Matcher<View> cardFlipper = withId(R.id.info_time_flipper1);
            onView(cardFlipper).perform(swipeRight());
            return this;
        }

        public MainActivityRobot clickOnNote() {
            onView(withId(R.id.info_note_flipper)).perform(click());
            return this;
        }
        public MainActivityRobot swipeNoteNext() {
            onView(withId(R.id.info_note_flipper)).perform(swipeLeft());
            return this;
        }
        public MainActivityRobot swipeNotePrev() {
            onView(withId(R.id.info_note_flipper)).perform(swipeRight());
            return this;
        }

        public MainActivityRobot clickOverflowMenu_viewDate(Context context) {
            onView(withText(R.string.configAction_viewDate)).perform(click());
            return this;
        }
        public MainActivityRobot clickOverflowMenu_setTimeZone(Context context) {
            onView(withText(R.string.configAction_setTimeZone)).perform(click());
            return this;
        }
        public MainActivityRobot clickActionBar_updateLocation(Context context) {
            onView(withContentDescription(R.string.configAction_refreshLocation)).perform(click());
            return this;
        }
        public MainActivityRobot clickActionBar_setLocation(Context context) {
            onView(withContentDescription(R.string.configAction_addLocation)).perform(click());
            return this;
        }
        public MainActivityRobot clickOverflowMenu_sunPosition(Context context) {
            onView(withText(R.string.configAction_sunDialog)).perform(click());
            return this;
        }
        public MainActivityRobot clickOverflowMenu_sunLight(Context context) {
            onView(withText(R.string.configAction_lightGraphDialog)).perform(click());
            return this;
        }
        public MainActivityRobot clickOverflowMenu_moon(Context context) {
            onView(withText(R.string.configAction_moon)).perform(click());
            return this;
        }
        public MainActivityRobot clickOverflowMenu_worldMap(Context context) {
            onView(withText(R.string.configAction_worldMap)).perform(click());
            return this;
        }

        public MainActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_viewDate)).perform(pressBack());
            return this;
        }

        public MainActivityRobot assertActionBar_mapButtonShown(boolean shown) {
            onView(withContentDescription(R.string.configAction_mapLocation)).check(shown ? assertShown : doesNotExist());
            return this;
        }

        public MainActivityRobot assertActivityShown(Context context) {
            onView(withId(R.id.layout_clock)).check(assertShown);
            // TODO
            return this;
        }

        public MainActivityRobot assertOverflowMenuShown(Context context)
        {
            onView(withText(R.string.configAction_viewDate)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setTimeZone)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_lightGraphDialog)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public MainActivityRobot assertOverflowMenu_mapButtonShown(Activity context, boolean shown) {
            onView(withText(R.string.configAction_mapLocation)).inRoot(isPlatformPopup()).check(shown ? assertShown : doesNotExist());
            return this;
        }

        public MainActivityRobot assertShown_solsticeEquinox(Context context, boolean shown) {
            onView(withId(R.id.info_date_solsticequinox)).check(shown ? assertShown : assertHidden);
            return this;
        }
        public MainActivityRobot assertShown_lightmap(Context context) {
            return assertShown_lightmap(AppSettings.loadShowLightmapPref(context));
        }
        public MainActivityRobot assertShown_lightmap(boolean shown) {
            onView(allOf(withId(R.id.info_time_lightmap), withParent(isDisplayed()))).check(shown ? assertShown : assertHidden);
            return this;
        }
        public MainActivityRobot assertShown_dataSourceUI(Context context, boolean shown)
        {
            if (shown)
            {
                onView(withId(R.id.txt_datasource)).check(assertShown);
                SuntimesCalculatorDescriptor dataSource = WidgetSettings.loadCalculatorModePref(context, 0);
                if (dataSource != null) {
                    onView(withId(R.id.txt_datasource)).check(matches(withText(dataSource.getName())));
                } // else { // TODO: test conditions when dataSource==null }

            } else {
                onView(withId(R.id.txt_datasource)).check(assertHidden);
            }
            return this;
        }

    }

    /**
     * MainActivityAutomator
     */
    public static class MainActivityAutomator
    {
        protected UiDevice device;

        public MainActivityAutomator() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }

        public MainActivityAutomator showOverflowMenu()
        {
            final UiObject2 button = device.findObject(By.desc("More options"));
            if (button != null && button.isClickable()) {
                button.click();
            } else {
                Assert.fail("UIObject not found! overflow menu");
            }
            return this;
        }

        public MainActivityAutomator clickOverflowMenu_viewDate()
        {
            final UiObject2 button = device.findObject(By.text("View date"));    // TODO: this line doesn't work...
            if (button != null && button.isClickable()) {
                button.click();
            } else {
                Assert.fail("UIObject not found: refresh button");
            }
            return this;
        }

        public MainActivityAutomator clickActionBar_refreshLocation()
        {
            final UiObject2 button = device.findObject(By.desc("Refresh"));
            if (button != null && button.isClickable()) {
                button.click();
            } else {
                Assert.fail("UIObject not found: refresh button");
            }
            return this;
        }
    }
}
