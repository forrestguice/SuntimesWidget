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

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
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

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class EventListActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(activityRule.getActivity());
        overrideConfigState(activityRule.getActivity());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(activityRule.getActivity());
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_EventListActivity()
    {
        Activity activity = activityRule.getActivity();
        EventListActivityRobot robot = new EventListActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.clickAddButton(activity).sleep(1000)
                .assertAddMenuIsShown(activity)
                .cancelAddMenu(activity);
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
            // TODO
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

        /////////////////////////////////////////////////////////////////////////

        public EventListActivityRobot assertActivityShown(Context context)
        {
            // TODO
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

    }
}
