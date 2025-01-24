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

package com.forrestguice.suntimeswidget.map;

import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
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
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeDown;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeUp;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class WorldMapDialogTest extends SuntimesActivityTestBase
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

    @Test
    public void test_showWorldMapDialog()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot()
                .showDialog(context).assertDialogShown(context);
                //.captureScreenshot(context, "suntimes-dialog-worldmap0");

        robot.clickTimeZoneLabel(context)
                .assertOverflowMenu_TimeZone(context).sleep(500)
                .cancelOverflowMenu_TimeZone(context).sleep(500);

        // :
        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .cancelOverflowMenu(context).sleep(1000);

        // : -> Options
        robot.showOverflowMenu(context)
                .clickOverflowMenu_Options(context)
                .assertOverflowMenu_Options(context).sleep(500)
                .cancelOverflowMenu_Options(context).sleep(500);

        // : -> View
        robot.showOverflowMenu(context)
                .clickOverflowMenu_View(context)
                .assertOverflowMenu_View(context).sleep(500)
                .cancelOverflowMenu_View(context).sleep(500);

        // : -> Time Zone
        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context)
                .assertOverflowMenu_TimeZone(context).sleep(500)
                .cancelOverflowMenu_TimeZone(context).sleep(500);

        // Map Menu
        robot.showMapMenu(context)
                .assertMapMenuShown(context).sleep(500)
                .cancelMapMenu(context).sleep(500);

        // Collapse / Expand
        robot.collapseSheet().sleep(1500)
                .assertSheetIsCollapsed(context)
                .expandSheet().sleep(1500)
                .assertDialogShown(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_showWorldMapDialog_maps()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot()
                .showDialog(context)
                .assertDialogShown(context);

        robot.showMapMenu(context)
                .assertMapMenuShown(context)
                .clickMapMenu_AzimuthalNorth(context)
                .assertMap_AzimuthalNorth(context)
                .sleep(1000);

        robot.showMapMenu(context)
                .clickMapMenu_AzimuthalSouth(context)
                .assertMap_AzimuthalSouth(context)
                .sleep(1000);

        robot.showMapMenu(context)
                .clickMapMenu_AzimuthalCentered(context)
                .assertMap_AzimuthalCentered(context)
                .sleep(1000);

        robot.showMapMenu(context)
                .clickMapMenu_BlueMarble(context)
                .assertMap_BlueMarble(context)
                .sleep(1000);

        robot.showMapMenu(context)
                .clickMapMenu_SimpleRectangular(context)
                .assertMap_SimpleRectangular(context)
                .sleep(1000);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_showWorldMapDialog_viewDate()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot()
                .showDialog(context)
                .assertDialogShown(context);

        // : -> View date -> Suntimes
        robot.showOverflowMenu(context)
                .clickOverflowMenu_View(context)
                .assertOverflowMenu_View(context).sleep(500)
                .clickOverflowMenu_View_Suntimes(context)
                .assertSheetIsCollapsed(context)
                .expandSheet().sleep(1000)
                .assertDialogShown(context);    // dialog still shown

        // : -> View date -> Moon
        robot.showOverflowMenu(context)
                .clickOverflowMenu_View(context)
                .assertOverflowMenu_View(context).sleep(500)
                .clickOverflowMenu_View_Moon(context);
        new MoonDialogTest.MoonDialogRobot()
                .assertDialogShown(context).sleep(500)
                .cancelDialog(context)
                .assertDialogNotShown(context);
        robot.expandSheet().sleep(1000)
                .assertDialogShown(context);

        // : -> View date -> Sun Position
        robot.showOverflowMenu(context)
                .clickOverflowMenu_View(context)
                .assertOverflowMenu_View(context).sleep(500)
                .clickOverflowMenu_View_Sun(context);
        new LightMapDialogTest.LightMapDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context)
                .assertDialogNotShown(context);
        robot.expandSheet().sleep(1000)
                .assertDialogShown(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    /**
     * WorldMapDialogRobot
     */
    public static class WorldMapDialogRobot extends DialogTest.DialogRobotBase implements DialogTest.DialogRobot
    {
        @Override
        public WorldMapDialogRobot sleep(long ms) {
            super.sleep(ms);
            return this;
        }
        @Override
        public WorldMapDialogRobot rotateDevice(Activity activity) {
            super.rotateDevice(activity);
            return this;
        }

        @Override
        public WorldMapDialogRobot expandSheet() {
            onView(withId(R.id.worldmapdialog_header)).perform(swipeUp());
            return this;
        }
        @Override
        public WorldMapDialogRobot collapseSheet() {
            onView(withId(R.id.worldmapdialog_header)).perform(swipeDown());
            return this;
        }

        @Override
        public WorldMapDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(context);
            onView(withText(R.string.configAction_worldMap)).perform(click());
            return this;
        }
        @Override
        public WorldMapDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.info_time_utc)).perform(pressBack());
            return this;
        }

        public WorldMapDialogRobot clickTimeZoneLabel(Context context) {
            onView(withId(R.id.info_time_utc)).perform(click());
            return this;
        }

        public WorldMapDialogRobot clickResetButton(Context context) {
            onView(withId(R.id.media_reset)).perform(click());
            return this;
        }

        public WorldMapDialogRobot showMapMenu(Context context) {
            onView(withId(R.id.map_modemenu)).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelMapMenu(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simplerectangular)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public WorldMapDialogRobot clickMapMenu_SimpleRectangular(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simplerectangular)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickMapMenu_BlueMarble(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_bluemarble)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickMapMenu_AzimuthalNorth(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickMapMenu_AzimuthalSouth(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_south)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickMapMenu_AzimuthalCentered(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_location)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public WorldMapDialogRobot showOverflowMenu(Context context) {
            onView(withId(R.id.map_menu)).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public WorldMapDialogRobot clickOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_Options_Colors(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public WorldMapDialogRobot clickOverflowMenu_View(Context context) {
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_View_Sun(Context context) {
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_View_Moon(Context context) {
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_View_Suntimes(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_View_Calendar(Context context) {
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelOverflowMenu_View(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public WorldMapDialogRobot clickOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        @Override
        public WorldMapDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.info_time_utc)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_worldmap)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.seek_map)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.map_menu)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.map_menu)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.map_menu)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.media_reset_map)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.media_reset_map)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.map_modemenu)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.map_modemenu)).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        @Override
        public WorldMapDialogRobot assertDialogNotShown(Context context)
        {
            super.assertDialogNotShown(context);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            // TODO: other options
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_View(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.time_utc)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }

        public WorldMapDialogRobot assertMapMenuShown(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simplerectangular)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_bluemarble)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_south)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_location)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }

        public WorldMapDialogRobot assertMap_SimpleRectangular(Context context) {
            // TODO
            return this;
        }
        public WorldMapDialogRobot assertMap_BlueMarble(Context context) {
            // TODO
            return this;
        }
        public WorldMapDialogRobot assertMap_AzimuthalNorth(Context context) {
            // TODO
            return this;
        }
        public WorldMapDialogRobot assertMap_AzimuthalSouth(Context context) {
            // TODO
            return this;
        }
        public WorldMapDialogRobot assertMap_AzimuthalCentered(Context context) {
            // TODO
            return this;
        }

        public WorldMapDialogRobot assertIsReset(Context context) {
            onView(withId(R.id.media_reset_map)).check(ViewAssertionHelper.assertDisabled);
            onView(withId(R.id.media_play_map)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.media_pause_map)).check(ViewAssertionHelper.assertHidden);
            return this;
        }
        public WorldMapDialogRobot assertCanBeReset(Context context) {
            onView(withId(R.id.media_reset_map)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }

        @Override
        public WorldMapDialogRobot assertSheetIsCollapsed(Context context) {
            onView(withId(R.id.info_time_utc)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_worldmap)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.map_menu)).check(ViewAssertionHelper.assertHidden);
            return this;
        }
    }
}
