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

package com.forrestguice.suntimeswidget.themes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.actions.ActionListActivity;
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
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WidgetThemeListActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<WidgetThemeListActivity> activityRule = new ActivityTestRule<>(WidgetThemeListActivity.class, false, false);

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
    public void test_ThemeListActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        ThemeListActivityRobot robot = new ThemeListActivityRobot()
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);
    }

    @Test
    public void test_ThemeListActivity_backgroundShade()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new ThemeListActivityRobot()
                .assertActivityShown(activity)
                .clickBackgroundShadeButton().sleep(1000)
                .clickBackgroundShadeButton();
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * ThemeListActivityRobot
     */
    public static class ThemeListActivityRobot extends ActivityRobot<ThemeListActivityRobot>
    {
        public ThemeListActivityRobot() {
            setRobot(this);
        }

        public ThemeListActivityRobot clickBackButton() {
            onView(navigationButton()).perform(click());
            return this;
        }

        public ThemeListActivityRobot clickBackgroundShadeButton() {
            onView(withId(R.id.themegrid_bottom)).perform(click());
            return this;
        }

        public ThemeListActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public ThemeListActivityRobot assertActivityShown(Context context) {
            onView(allOf(withClassName(endsWith("TextView")), withText(R.string.configLabel_widgetThemeListSelect),
                    isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            return this;
        }

        public ThemeListActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_import)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_export)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

    }
}
