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
import android.content.Intent;
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.actions.ActionListActivityTest;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivityTest;
import com.forrestguice.suntimeswidget.widgets.SuntimesWidgetListActivity;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.navigationButton;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WidgetListActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesWidgetListActivity> activityRule = new ActivityTestRule<>(SuntimesWidgetListActivity.class, false, false);

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
    public void test_WidgetListActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WidgetListActivityRobot robot = new WidgetListActivityRobot()
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

    @Test @QuickTest
    public void test_WidgetListActivity_manageThemes()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WidgetListActivityRobot robot = new WidgetListActivityRobot()
                .assertActivityShown(activity)
                .clickManageThemesButton(activity);
        new WidgetThemeListActivityTest.ThemeListActivityRobot()
                .assertActivityShown(activity)
                .clickBackButton();
        robot.assertActivityShown(activity);
    }

    @Test @QuickTest
    public void test_WidgetListActivity_manageActions()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WidgetListActivityRobot robot = new WidgetListActivityRobot()
                .assertActivityShown(activity)
                .clickManageActionsButton(activity);
        new ActionListActivityTest.ActionListActivityRobot()
                .assertActivityShown(activity)
                .clickBackButton();
        robot.assertActivityShown(activity);
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

        public WidgetListActivityRobot clickManageThemesButton(Context context) {
            onView(withContentDescription(R.string.configLabel_widgetThemeList)).perform(click());
            return this;
        }
        public WidgetListActivityRobot clickManageActionsButton(Context context) {
            onView(withContentDescription(R.string.loadaction_dialog_title)).perform(click());
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
