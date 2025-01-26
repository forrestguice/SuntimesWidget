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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.forrestguice.suntimeswidget.equinox.EquinoxCardDialogTest;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.support.test.espresso.IdlingPolicies;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;

import com.forrestguice.support.test.espresso.ElapsedTimeIdlingResource;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.notes.NoteData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.hamcrest.Matcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.registerIdlingResources;
import static com.forrestguice.support.test.espresso.Espresso.unregisterIdlingResources;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.longClick;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.replaceText;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeLeft;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeRight;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.matches;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isRoot;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.LocationDialogTest.applyLocationDialog;
import static com.forrestguice.suntimeswidget.LocationDialogTest.inputLocationDialog_mode;
import static com.forrestguice.suntimeswidget.LocationDialogTest.showLocationDialog;
import static com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest.verifyGeneralSettings;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    /**
     * UI Test; open the activity, take a screenshot, swap the card, take a screenshot, and then rotate.
     */
    @Test
    public void test_mainActivity()
    {
        verifyActivity();
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-main0");

        new MainActivityRobot()
                .swapCard(activityRule.getActivity());
        verifyTimeCard();
        captureScreenshot(activityRule.getActivity(), "suntimes-activity-main1");

        rotateDevice(activityRule);
        verifyActivity();
    }

    public void verifyActivity()
    {
        verifyTheme(activityRule.getActivity());
        verifyLocale(activityRule.getActivity());
        verifyActionBar();
        verifyClock();
        verifyNote(activityRule.getActivity());
        verifyTimeCard();
        verifyLightmap(activityRule.getActivity());
        verifySolsticeEquinox(activityRule.getActivity());
        verifyDataSourceUI(activityRule.getActivity());
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

    public void verifyActionBar()
    {
        onView(withId(R.id.action_location_add)).check(assertShown);

        WidgetSettings.LocationMode mode = WidgetSettings.loadLocationModePref(activityRule.getActivity(), 0);
        if (mode == WidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            onView(withId(R.id.action_location_refresh)).check(assertShown);

        } else {
            onView(withId(R.id.action_location_refresh)).check(doesNotExist());
        }
    }

    public void verifyClock()
    {
        SuntimesActivity activity = activityRule.getActivity();
        SuntimesRiseSetDataset dataset = activity.dataset;
        SuntimesUtils.TimeDisplayText timeText = SuntimesActivity.utils.calendarTimeShortDisplayString(activity, dataset.now());
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

    public static void verifyLightmap(Context context)
    {
        Matcher<View> lightmap = allOf(withId(R.id.info_time_lightmap), withParent(isDisplayed()));
        if (AppSettings.loadShowLightmapPref(context)) {
            onView(lightmap).check(assertShown);
        } else {
            onView(lightmap).check(matches(not(isDisplayed())));
        }
    }

    public static void verifySolsticeEquinox(Context context)
    {
        if (AppSettings.loadShowEquinoxPref(context))
        {
            onView(withId(R.id.info_date_solsticequinox)).check(assertShown);
        } else {
            onView(withId(R.id.info_date_solsticequinox)).check(matches(not(isDisplayed())));
        }
    }

    public static void verifyDataSourceUI(Context context)
    {
        if (AppSettings.loadDatasourceUIPref(context))
        {
            onView(withId(R.id.txt_datasource)).check(assertShown);

            SuntimesCalculatorDescriptor dataSource = WidgetSettings.loadCalculatorModePref(context, 0);
            if (dataSource != null) {
                onView(withId(R.id.txt_datasource)).check(matches(withText(dataSource.getName())));
            }
            // else { // TODO: test conditions when dataSource==null }

        } else {
            onView(withId(R.id.txt_datasource)).check(matches(not(isDisplayed())));
        }
    }

    /**
     * UI Test; click on the data source label and verify setting activity is displayed.
     */
    @Test
    public void test_mainActivity_onDataSourceUIClick()
    {
        verifyDataSourceUI(activityRule.getActivity());
        if (AppSettings.loadDatasourceUIPref(activityRule.getActivity()))
        {
            new MainActivityRobot()
                    .clickDataSourceLabel();
            //verifyGeneralSettings(activityRule.getActivity());  // TODO
            onView(isRoot()).perform(pressBack());
        }
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
        String savedState = AppSettings.loadClockTapActionPref(activityRule.getActivity());
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(activityRule.getActivity()).edit();
        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.ALARM.name()).apply();

        new MainActivityRobot()
                .clickOnClock();
        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.ALARM.name(), noteIndex);

        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, savedState).apply();
    }

    @Test
    public void test_mainActivity_onClockClick_nextNote()
    {
        String savedState = AppSettings.loadClockTapActionPref(activityRule.getActivity());
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(activityRule.getActivity()).edit();
        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.NEXT_NOTE.name()).apply();

        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .clickOnClock();
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.NEXT_NOTE.name(), noteIndex);

        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, savedState).apply();
    }

    @Test
    public void test_mainActivity_onClockClick_prevNote()
    {
        String savedState = AppSettings.loadClockTapActionPref(activityRule.getActivity());
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(activityRule.getActivity()).edit();
        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, WidgetActions.SuntimesAction.PREV_NOTE.name()).apply();

        int noteIndex = activityRule.getActivity().notes.getNoteIndex();
        new MainActivityRobot()
                .clickOnClock().sleep(1000);
        verifyOnClockClick(activityRule.getActivity(), WidgetActions.SuntimesAction.PREV_NOTE.name(), noteIndex);

        config.putString(AppSettings.PREF_KEY_UI_CLOCKTAPACTION, savedState).apply();
    }

    public static void verifyOnClockClick(SuntimesActivity activity, String tapAction0, int noteIndex)
    {
        String tapAction = AppSettings.loadClockTapActionPref(activity);
        assertEquals(tapAction0, tapAction);

        if (tapAction.equals(WidgetActions.SuntimesAction.ALARM.name()))
        {
            new AlarmDialogTest.AlarmDialogRobot()
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
            new AlarmDialogTest.AlarmDialogRobot()
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
        String savedState = AppSettings.loadDateTapActionPref(activityRule.getActivity());
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(activityRule.getActivity()).edit();
        config.putString(AppSettings.PREF_KEY_UI_DATETAPACTION, WidgetActions.SuntimesAction.CONFIG_DATE.name()).apply();

        String tapAction = AppSettings.loadDateTapActionPref(activityRule.getActivity());
        assertEquals(WidgetActions.SuntimesAction.CONFIG_DATE.name(), tapAction);

        new MainActivityRobot()
                .clickCardDate();
        new TimeDateDialogTest.TimeDateDialogRobot()
                .assertDialogShown(activityRule.getActivity())
                .cancelDialog(activityRule.getActivity());

        config.putString(AppSettings.PREF_KEY_UI_DATETAPACTION, savedState).apply();
    }
    // TODO: SHOW_CALENDAR, ...

    /**
     * UI Test; click the date field and verify the configured action.
     */
    @Test
    public void test_mainActivity_onDateClick_swapCard()
    {
        Context context = activityRule.getActivity();
        String savedState = AppSettings.loadDateTapActionPref(context);
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(context).edit();
        config.putString(AppSettings.PREF_KEY_UI_DATETAPACTION, WidgetActions.SuntimesAction.SWAP_CARD.name()).apply();

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

        config.putString(AppSettings.PREF_KEY_UI_DATETAPACTION, savedState).apply();
    }

    /**
     * UI Test; test app crash (latitude edge case) described in issue #74.
     */
    @Test
    public void test_appCrash74()
    {
        int year = 2017; int month = 1; int day = 19;
        String latitude = "83.124";
        String longitude = "23.1592";

        // open the location dialog, and set test location
        showLocationDialog();
        inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);
        onView(withId(R.id.appwidget_location_edit)).perform(click());
        onView(withId(R.id.appwidget_location_name)).perform(replaceText("TestAppCrash74"));
        onView(withId(R.id.appwidget_location_lat)).perform(replaceText(latitude));
        onView(withId(R.id.appwidget_location_lon)).perform(replaceText(longitude));
        applyLocationDialog(activityRule.getActivity());

        // open the date dialog and change the date
        Activity context = activityRule.getActivity();
        TimeDateDialogTest.TimeDateDialogRobot robot = new TimeDateDialogTest.TimeDateDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .selectDate(year, month, day)
                .applyDialog(context);

        verifyActivity();
    }

    @Test
    public void test_mainActivity_userSwappedCard_withSwipe()
    {
        showLocationDialog();
        inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);
        onView(withId(R.id.appwidget_location_edit)).perform(click());
        onView(withId(R.id.appwidget_location_name)).perform(replaceText(TESTLOC_0_LABEL));
        onView(withId(R.id.appwidget_location_lat)).perform(replaceText(TESTLOC_0_LAT));
        onView(withId(R.id.appwidget_location_lon)).perform(replaceText(TESTLOC_0_LON));
        applyLocationDialog(activityRule.getActivity());

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
        ElapsedTimeIdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);

        // during that minute
        // the app will update the clock and note area at least once

        registerIdlingResources(waitForResource);       // afterward...
        verifyTimeCard_today();
        unregisterIdlingResources(waitForResource);
    }



    public void verifyTimeCard()
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
    public void test_mainActivity_fullUpdateReciever()
    {
        // test PendingIntent
        final SuntimesActivity activity = (SuntimesActivity) activityRule.getActivity();
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
    public void test_issue408()
    {
        // test "show moon" enabled
        SuntimesActivity activity = (SuntimesActivity) activityRule.getActivity();
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        pref.putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, true).commit();
        activity.finish();
        activityRule.launchActivity(activity.getIntent());
        test_mainActivity_fullUpdateReciever();

        // test "show moon" disabled
        pref.putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, false).commit();
        activity = (SuntimesActivity) activityRule.getActivity();
        activity.finish();
        activityRule.launchActivity(activity.getIntent());
        test_mainActivity_fullUpdateReciever();
    }

    /**
     * MainActivityRobot
     */
    public static class MainActivityRobot
    {
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

        public MainActivityRobot sleep(long ms) {
            SystemClock.sleep(ms);
            return this;
        }
    }
}
