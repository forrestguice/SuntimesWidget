/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Set;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.replaceText;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.isShowingError;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class EventListActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<EventListActivity> activityRule = new ActivityTestRule<>(EventListActivity.class, false, false);

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

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test @QuickTest
    public void test_EventListActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new EventListActivityRobot()
                .assertActivityShown(activity);
    }

    @Test
    public void test_EventListActivity_menu()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        EventListActivityRobot robot = new EventListActivityRobot()
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.clickAddButton(activity)
                .assertAddMenuIsShown(activity)
                .cancelAddMenu(activity);
    }

    @Test
    public void test_EventListActivity_help()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new EventListActivityRobot()
                .assertActivityShown(activity)
                .showOverflowMenu(activity)
                .assertOverflowMenuShown(activity)
                .clickOverflowMenu_help();
        new DialogTest.HelpDialogRobot()
                .assertDialogShown(activity)
                .assertOnlineHelpButtonShown(activity);
    }

    @Test
    public void test_EventListActivity_clear()
    {
        Set<String> eventList = EventSettingsTest.populateEventListWithTestItems(getContext());
        assertFalse(eventList.isEmpty());

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        EventListActivityRobot robot = new EventListActivityRobot()
                .assertActivityShown(activity)
                .assertListHasAtLeast(activity, eventList.size())

                .showOverflowMenu(activity)
                .clickOverflowMenu_clear(activity)
                .assertClearDialogShown()
                .clickConfirmClear();

        Set<String> events = EventSettings.loadEventList(activity);
        assertTrue(events.isEmpty());
        robot.assertEmptyListShown(activity)
                .assertListHasItems(activity, 0);
    }

    @Test
    public void test_EventListActivity_addEvent_cancel()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        EventListActivityRobot robot = new EventListActivityRobot()
                .assertActivityShown(activity);

        robot.clickAddButton(activity)
                .assertAddMenuIsShown(activity)
                .clickAddMenu_elevation(activity);
        new ElevationEventDialogRobot()
                .assertDialogShown(activity)
                .clickCancelButton()
                .assertDialogNotShown(activity);

        robot.clickAddButton(activity)
                .assertAddMenuIsShown(activity)
                .clickAddMenu_shadow(activity);
        new ShadowEventDialogRobot()
                .assertDialogShown(activity)
                .clickCancelButton()
                .assertDialogNotShown(activity);
    }

    @Test
    public void test_EventListActivity_addEvent_elevation()
    {
        String label = "15 degrees";
        String angle = "15";
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        Set<String> eventList0 = EventSettings.loadEventList(activity);

        EventListActivityRobot robot = new EventListActivityRobot()
                .assertActivityShown(activity)
                .clickAddButton(activity)
                .clickAddMenu_elevation(activity);

        new ElevationEventDialogRobot()
                .assertDialogShown(activity)
                .inputEventLabel(label)
                .inputEventAngle(angle)
                .clickSaveButton()
                .assertNoErrorsShown(activity)
                .assertDialogNotShown(activity);
        robot.assertListHasItems(activity, eventList0.size() + 1)
                .assertHasItemWithLabel(label);
    }

    @Test
    public void test_EventListActivity_addEvent_elevation_errors()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new EventListActivityRobot()
                .assertActivityShown(activity)
                .clickAddButton(activity)
                .clickAddMenu_elevation(activity);

        new ElevationEventDialogRobot()
                .assertDialogShown(activity)
                .inputEventLabel("")
                .inputEventAngle("91")
                .clickSaveButton()
                .assertLabelErrorShown(activity, true)
                .assertAngleErrorShown(activity, true);
    }

    @Test
    public void test_EventListActivity_addEvent_shadowLength()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        Set<String> eventList0 = EventSettings.loadEventList(activity);

        EventListActivityRobot robot = new EventListActivityRobot()
                .assertActivityShown(activity)
                .clickAddButton(activity)
                .clickAddMenu_shadow(activity);

        new ShadowEventDialogRobot()
                .assertDialogShown(activity)
                .inputEventLabel("1:1")
                .inputObjectHeight("1")
                .inputShadowLength("1")
                .clickSaveButton()
                .assertNoErrorsShown(activity)
                .assertDialogNotShown(activity);
        robot.assertListHasItems(activity, eventList0.size() + 1)
                .assertHasItemWithLabel("1:1");
    }

    @Test
    public void test_EventListActivity_addEvent_shadowLength_errors()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new EventListActivityRobot()
                .assertActivityShown(activity)
                .clickAddButton(activity)
                .clickAddMenu_shadow(activity);

        new ShadowEventDialogRobot()
                .assertDialogShown(activity)
                .inputEventLabel("")
                .inputObjectHeight("")
                .inputShadowLength("-1")
                .clickSaveButton()
                .assertLabelErrorShown(activity, true)
                .assertHeightErrorShown(activity, true)
                .assertLengthErrorShown(activity, true);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * EventListActivityRobot
     */
    public static class EventListActivityRobot extends ActivityRobot<EventListActivityRobot>
    {
        public EventListActivityRobot() {
            setRobot(this);
        }

        protected EventListActivityRobot showActivity(Activity activity) {
            new SuntimesSettingsActivityTest.SettingsActivityRobot()
                    .showActivity(activity)
                    .clickHeader_userInterfaceSettings()
                    .clickPref_manageEvents();
            return this;
        }

        public EventListActivityRobot clickAddButton(Context context) {
            onView(withContentDescription(R.string.configAction_addEvent)).perform(click());
            return this;
        }
        public EventListActivityRobot cancelAddMenu(Context context) {
            onView(withText(R.string.configAction_addEvent_sunEvent)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public EventListActivityRobot clickAddMenu_elevation(Context context) {
            onView(withText(R.string.configAction_addEvent_sunEvent)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EventListActivityRobot clickAddMenu_shadow(Context context) {
            onView(withText(R.string.configAction_addEvent_shadowEvent)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public EventListActivityRobot clickBackButton(Context context) {
            onView(navigationButton()).perform(click());
            return this;
        }

        public EventListActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearEvents)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EventListActivityRobot clickOverflowMenu_export(Context context) {
            onView(withText(R.string.configAction_exportEvents)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EventListActivityRobot clickOverflowMenu_import(Context context) {
            onView(withText(R.string.configAction_importEvents)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EventListActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public EventListActivityRobot clickConfirmClear() {
            onView(withText(R.string.configAction_clearEvents)).perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public EventListActivityRobot assertActivityShown(Context context) {
            onView(allOf(withClassName(endsWith("TextView")), withText(R.string.eventlist_dialog_title),
                    isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            return this;
        }

        public EventListActivityRobot assertAddMenuIsShown(Context context) {
            onView(withText(R.string.configAction_addEvent_sunEvent)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_addEvent_shadowEvent)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public EventListActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_clearEvents)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportEvents)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_importEvents)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public EventListActivityRobot assertClearDialogShown() {
            onView(withText(R.string.clearevents_dialog_msg)).check(assertShown);
            return this;
        }

        public EventListActivityRobot assertListHasItems(Context context, int numItems)
        {
            if (numItems == 0) {
                onView(allOf(withId(R.id.list_events), hasMinimumChildCount(1))).check(doesNotExist());
            } else onView(allOf(withId(R.id.list_events), hasChildCount(numItems))).check(assertShown);
            return this;
        }

        public EventListActivityRobot assertListHasAtLeast(Context context, int numItems) {
            if (numItems > 0) {
                onView(allOf(withId(R.id.list_events), hasMinimumChildCount(numItems))).check(assertShown);
            }
            return this;
        }

        public EventListActivityRobot assertHasItemWithLabel(String s) {
            // TODO
            return this;
        }

        public EventListActivityRobot assertEmptyListShown(Context context) {
            onView(withId(android.R.id.empty)).check(assertShown);
            onView(withId(R.id.list_events)).check(assertHidden);
            return this;
        }
    }

    /**
     * EventDialogRobot
     */
    public static abstract class EventDialogRobot<T> extends DialogTest.DialogRobot<T>
    {
        public T clickSaveButton() {
            onView(withId(R.id.save_button)).perform(click());
            return robot;
        }
        public T clickCancelButton() {
            onView(withId(R.id.cancel_button)).perform(click());
            return robot;
        }
        public T clickOffsetButton() {
            onView(withId(R.id.chip_event_offset)).perform(click());
            return robot;
        }

        public T inputEventLabel(String label) {
            onView(withId(R.id.edit_event_label)).perform(replaceText(label));
            return robot;
        }

        @Override
        public T assertDialogShown(Context context) {
            onView(withText(R.string.editevent_dialog_title)).check(assertShown);
            onView(withId(R.id.edit_event_label)).check(assertShown);
            onView(withId(R.id.edit_event_offset)).check(assertShown);
            onView(withId(R.id.cancel_button)).check(assertShown);
            onView(withId(R.id.save_button)).check(assertShown);
            return robot;
        }

        public T assertLabelErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.editevent_dialog_label_error)))).check(isShown ? assertShown : doesNotExist());
            return robot;
        }
    }

    /**
     * ElevationEventDialogRobot
     */
    public static class ElevationEventDialogRobot extends EventDialogRobot<ElevationEventDialogRobot>
    {
        public ElevationEventDialogRobot()
        {
            super();
            setRobot(this);
        }

        public ElevationEventDialogRobot inputEventAngle(String angle) {
            onView(withId(R.id.edit_event_angle)).perform(replaceText(angle));
            return this;
        }

        public ElevationEventDialogRobot assertAngleErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.editevent_dialog_angle_error)))).check(isShown ? assertShown : doesNotExist());
            return robot;
        }

        public ElevationEventDialogRobot assertNoErrorsShown(Context context) {
            assertLabelErrorShown(context, false);
            assertAngleErrorShown(context, false);
            return this;
        }

        @Override
        public ElevationEventDialogRobot assertDialogShown(Context context)
        {
            super.assertDialogShown(context);
            onView(withId(R.id.edit_event_angle)).check(assertShown).check(assertEnabled);
            return this;
        }
    }

    /**
     * ShadowEventDialogRobot
     */
    public static class ShadowEventDialogRobot extends EventDialogRobot<ShadowEventDialogRobot>
    {
        public ShadowEventDialogRobot()
        {
            super();
            setRobot(this);
        }

        public ShadowEventDialogRobot inputShadowLength(String length) {
            onView(withId(R.id.edit_event_length)).perform(replaceText(length));
            return this;
        }
        public ShadowEventDialogRobot inputObjectHeight(String height) {
            onView(withId(R.id.edit_event_height)).perform(replaceText(height));
            return this;
        }

        public ShadowEventDialogRobot assertLengthErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.editevent_dialog_length_error)))).check(isShown ? assertShown : doesNotExist());
            return this;
        }
        public ShadowEventDialogRobot assertHeightErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.editevent_dialog_height_error)))).check(isShown ? assertShown : doesNotExist());
            return this;
        }

        public ShadowEventDialogRobot assertNoErrorsShown(Context context) {
            assertLabelErrorShown(context, false);
            assertLengthErrorShown(context, false);
            assertHeightErrorShown(context, false);
            return this;
        }

        @Override
        public ShadowEventDialogRobot assertDialogShown(Context context)
        {
            super.assertDialogShown(context);
            onView(withId(R.id.edit_event_height)).check(assertShown).check(assertEnabled);
            onView(withId(R.id.edit_event_length)).check(assertShown).check(assertEnabled);
            return this;
        }
    }

}
