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

package com.forrestguice.suntimeswidget.tiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTest;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.WidgetConfigActivityTest;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.suntimeswidget.tiles.AlarmTileService.ALARMTILE_APPWIDGET_ID;
import static com.forrestguice.suntimeswidget.tiles.ClockTileService.CLOCKTILE_APPWIDGET_ID;
import static com.forrestguice.suntimeswidget.tiles.NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class TileActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<TileLockScreenActivity> activityRule = new ActivityTestRule<>(TileLockScreenActivity.class, true, false);

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_TileActivity_clockTile()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, CLOCKTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        WidgetActions.deleteActionLaunchPref(activity, CLOCKTILE_APPWIDGET_ID, null);  // reset to defaults first

        TileActivityRobot robot = new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new ClockTileBase(activity))
                .captureScreenshot(activity, "tile-dialog-clock");

        robot.clickActionButton();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
    }

    @Test
    public void test_TileActivity_clockTile_config()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, CLOCKTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new ClockTileBase(activity))
                .clickSettingsButton().sleep(1000);

        new WidgetConfigActivityTest.WidgetConfigActivityRobot()
                .assertActivityShown(activity)
                .assertPageIsTitled(activity, R.string.app_name_clocktile)
                .assertHasAppWidgetId(activity, CLOCKTILE_APPWIDGET_ID)
                .assertReconfigureMode(activity, true);
    }

    @Test
    public void test_TileActivity_eventTile()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, NEXTEVENTTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        WidgetActions.deleteActionLaunchPref(activity, NEXTEVENTTILE_APPWIDGET_ID, null);  // reset to defaults first

        TileActivityRobot robot = new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new NextEventTileBase(activity))
                .captureScreenshot(activity, "tile-dialog-nextevent");

        robot.clickActionButton();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
    }

    @Test
    public void test_TileActivity_eventTile_config()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, NEXTEVENTTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new NextEventTileBase(activity))
                .clickSettingsButton();

        new WidgetConfigActivityTest.WidgetConfigActivityRobot()
                .assertActivityShown(activity)
                .assertPageIsTitled(activity, R.string.app_name_eventtile)
                .assertHasAppWidgetId(activity, NEXTEVENTTILE_APPWIDGET_ID)
                .assertReconfigureMode(activity, true);
    }

    @Test
    public void test_TileActivity_alarmTile()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, ALARMTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        WidgetActions.deleteActionLaunchPref(activity, ALARMTILE_APPWIDGET_ID, null);  // reset to defaults first

        TileActivityRobot robot = new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new AlarmTileBase(activity))
                .captureScreenshot(activity, "tile-dialog-alarm");

        robot.clickActionButton();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
    }

    @Test
    public void test_TileActivity_alarmTile_config()
    {
        Intent intent = new Intent();
        intent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, ALARMTILE_APPWIDGET_ID);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        new TileActivityRobot()
                .assertActivityShown(activity)
                .assertTileBaseEquals(activity, new AlarmTileBase(activity))
                .clickSettingsButton();

        new WidgetConfigActivityTest.WidgetConfigActivityRobot()
                .assertActivityShown(activity)
                .assertPageIsTitled(activity, R.string.app_name_alarmtile)
                .assertHasAppWidgetId(activity, ALARMTILE_APPWIDGET_ID)
                .assertReconfigureMode(activity, true);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * TileActivityRobot
     */
    public static class TileActivityRobot extends ActivityRobot<TileActivityRobot>
    {
        public TileActivityRobot() {
            setRobot(this);
        }

        public TileActivityRobot clickSettingsButton() {
            onView(withId(R.id.button_settings)).perform(click());
            return this;
        }
        public TileActivityRobot clickActionButton() {
            onView(withId(android.R.id.button1)).perform(click());
            return this;
        }
        public TileActivityRobot clickDismissActivity() {
            onView(withId(R.id.button_settings)).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public TileActivityRobot assertActivityShown(Context context)
        {
            onView(withId(R.id.button_settings)).check(assertShown);
            onView(allOf(withId(android.R.id.title), hasSibling(withId(android.R.id.icon)))).check(assertShown);
            onView(allOf(withId(android.R.id.icon), hasSibling(withId(android.R.id.title)))).check(assertShown);
            onView(allOf(withId(android.R.id.message), hasSibling(withChild(withId(android.R.id.title))))).check(assertShown);
            return this;
        }

        public TileActivityRobot assertTileBaseEquals(Context context, SuntimesTileBase tileBase)
        {
            onView(allOf(withId(android.R.id.title), hasSibling(withId(android.R.id.icon)),
                    withText(tileBase.formatDialogTitle(context).toString()))).check(assertShown);
            onView(allOf(withId(android.R.id.icon), hasSibling(withId(android.R.id.title)),
                    hasDrawable(tileBase.getDialogIcon(context)))).check(assertShown);
            onView(allOf(withId(android.R.id.message), hasSibling(withChild(withId(android.R.id.title))),
                    withText(tileBase.formatDialogMessage(context).toString()))).check(assertShown);
            onView(allOf(withId(android.R.id.button1), withText(tileBase.getLaunchIntentTitle(context))))
                    .check(tileBase.getLaunchIntent(context) != null ? assertShown : doesNotExist());
            return this;
        }
    }

}
