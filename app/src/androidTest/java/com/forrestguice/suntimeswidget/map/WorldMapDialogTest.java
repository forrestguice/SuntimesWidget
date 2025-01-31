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

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appCalculator;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appLocation;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appTimeZone;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_LocalMean;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_Suntimes;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_UTC;
import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertDisabled;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeDown;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeUp;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.withTextAsDate;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WorldMapDialogTest extends SuntimesActivityTestBase
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

    @Test
    public void test_worldMapDialog()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));
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

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_worldMapDialog_expandCollapse()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

        robot.collapseSheet().sleep(1500)
                .assertSheetIsCollapsed(context)
                .assertShowsDate(context, robot.now(context))
                .expandSheet().sleep(1500)
                .assertDialogShown(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_worldMapDialog_maps()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

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
    public void test_worldMapDialog_timeZone()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot()
                .showDialog(context).sleep(1000)
                .assertDialogShown(context);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context).sleep(500)
                .assertOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_LocalMean(context).sleep(500)
                .assertShowsDate(context, robot.now(timeZone_LocalMean(context)));

        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_Suntimes(context).sleep(500)
                .assertShowsDate(context, robot.now(timeZone_Suntimes(context)));

        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_UTC(context).sleep(500)
                .assertShowsDate(context, robot.now(timeZone_UTC()));

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_worldMapDialog_viewDate_Suntimes()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

        robot.showOverflowMenu(context)
                .clickOverflowMenu_View(context)
                .assertOverflowMenu_View(context).sleep(500)
                .clickOverflowMenu_View_Suntimes(context)
                .assertSheetIsCollapsed(context)
                .expandSheet().sleep(1000)
                .assertDialogShown(context);    // dialog still shown

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void worldMapDialog_viewDate_Sun()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

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

    @Test
    public void test_worldMapDialog_viewDate_Moon()
    {
        Activity context = activityRule.getActivity();
        WorldMapDialogRobot robot = new WorldMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

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

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    /**
     * WorldMapDialogRobot
     */
    public static class WorldMapDialogRobot extends DialogTest.DialogRobot<WorldMapDialogRobot>
    {
        public WorldMapDialogRobot() {
            super();
            setRobot(this);
        }

        public Calendar now(Context context)
        {
            String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, WorldMapWidgetSettings.MAPTAG_3x2, WorldMapWidgetSettings.PREF_DEF_WORLDMAP_TIMEZONE);
            TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId)
                    ? appTimeZone(context)
                    : WidgetTimezones.getTimeZone(tzId, appLocation(context).getLongitudeAsDouble(), appCalculator(context));
            return now(timezone);
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
        public WorldMapDialogRobot clickOverflowMenu_TimeZone_UTC(Context context) {
            onView(withText(R.string.time_utc)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_TimeZone_LocalMean(Context context) {
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot clickOverflowMenu_TimeZone_Suntimes(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public WorldMapDialogRobot cancelOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        @Override
        public WorldMapDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.info_time_utc)).check(assertShown);
            onView(withId(R.id.info_time_worldmap)).check(assertShown);
            onView(withId(R.id.seek_map)).check(assertShown);
            onView(withId(R.id.map_menu)).check(assertShown);
            onView(withId(R.id.map_menu)).check(assertEnabled);
            onView(withId(R.id.map_menu)).check(assertClickable);
            onView(withId(R.id.media_reset_map)).check(assertShown);
            onView(withId(R.id.media_reset_map)).check(assertClickable);
            onView(withId(R.id.map_modemenu)).check(assertShown);
            onView(withId(R.id.map_modemenu)).check(assertClickable);
            return this;
        }
        @Override
        public WorldMapDialogRobot assertDialogNotShown(Context context)
        {
            super.assertDialogNotShown(context);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(assertShown);
            // TODO: other options
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_View(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public WorldMapDialogRobot assertOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.time_utc)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public WorldMapDialogRobot assertMapMenuShown(Context context) {
            onView(withText(R.string.widgetMode_sunPosMap_simplerectangular)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_bluemarble)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_south)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.widgetMode_sunPosMap_simpleazimuthal_location)).inRoot(isPlatformPopup()).check(assertShown);
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
            onView(withId(R.id.media_reset_map)).check(assertDisabled);
            onView(withId(R.id.media_play_map)).check(assertShown);
            onView(withId(R.id.media_pause_map)).check(assertHidden);
            return this;
        }
        public WorldMapDialogRobot assertCanBeReset(Context context) {
            onView(withId(R.id.media_reset_map)).check(assertEnabled);
            return this;
        }

        @Override
        public WorldMapDialogRobot assertSheetIsCollapsed(Context context) {
            onView(withId(R.id.info_time_utc)).check(assertShown);
            onView(withId(R.id.info_time_worldmap)).check(assertHidden);
            onView(withId(R.id.map_menu)).check(assertHidden);
            return this;
        }

        public WorldMapDialogRobot assertShowsDate(Context context, @NonNull Calendar date) {
            return assertShowsDate(date, WidgetSettings.loadTimeFormatModePref(context, 0), false);
        }
        public WorldMapDialogRobot assertShowsDate(@NonNull Calendar date, WidgetSettings.TimeFormatMode withMode, boolean withSeconds)
        {
            SimpleDateFormat[] formats = (withMode == WidgetSettings.TimeFormatMode.MODE_12HR)
                    ? (withSeconds ? timeDateFormats12s : timeDateFormats12)
                    : (withSeconds ? timeDateFormats24s : timeDateFormats24);
            long tolerance = (withSeconds
                    ? 10 * 1000
                    : 90 * 1000);

            onView(allOf(withId(R.id.info_time_utc),
                    withTextAsDate(formats, date, tolerance, true)
            )).check(assertShown);
            return this;
        }
    }
}
