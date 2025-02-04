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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WidgetListActivityTest extends SuntimesActivityTestBase
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
    public void test_WidgetListActivity()
    {
        Activity activity = activityRule.getActivity();
        WidgetListActivityRobot robot = new WidgetListActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_help();
        new DialogTest.HelpDialogRobot()
                .assertDialogShown(activity)
                .cancelDialog(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * WidgetListActivityRobot
     */
    public static class WidgetListActivityRobot extends ActivityRobot<WidgetListActivityRobot>
    {
        public WidgetListActivityRobot() {
            setRobot(this);
        }

        protected WidgetListActivityRobot showActivity(Activity activity) {
            new SuntimesSettingsActivityTest.SettingsActivityRobot()
                    .showActivity(activity)
                    .clickHeader_widgetList();
            return this;
        }

        public WidgetListActivityRobot clickBackButton(Context context) {
            onView(navigationButton()).perform(click());
            return this;
        }

        public WidgetListActivityRobot clickOverflowMenu_createBackup() {
            onView(withText(R.string.configAction_createBackup)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WidgetListActivityRobot clickOverflowMenu_restoreBackup() {
            onView(withText(R.string.configAction_restoreBackup)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WidgetListActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public WidgetListActivityRobot assertActivityShown(Context context)
        {
            ArrayAdapter<?> widgetAdapter = WidgetListAdapter.createWidgetListAdapter(context);
            if (widgetAdapter.isEmpty())
                onView(withId(android.R.id.empty)).check(assertEnabled);
            else onView(withId(R.id.widgetList)).check(assertEnabled);
            return this;
        }

        public WidgetListActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_createBackup)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_restoreBackup)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }
}
