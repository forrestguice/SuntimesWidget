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

package com.forrestguice.suntimeswidget.graph;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.map.WorldMapDialogTest;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.support.annotation.NonNull;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appCalculator;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appLocation;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.appTimeZone;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_ApparentSolar;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_LocalMean;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_Suntimes;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.ActivityRobot.timeZone_UTC;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.MAPTAG_LIGHTMAP;
import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.replaceText;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.matches;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.withTextAsDate;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.withTextAsDoubleApproximateTo;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class LightMapDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class, false, false);

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

    @Test
    public void test_MainCard_showLightmap()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        if (AppSettings.loadShowLightmapPref(context))
        {
            onView(withId(R.id.info_time_lightmap)).check(assertShown);
            onView(withId(R.id.info_time_lightmap)).perform(click());

            LightMapDialogRobot robot = new LightMapDialogRobot();
            robot.sleep(500)
                    .assertDialogShown(context)
                    .assertIsReset(context)
                    .assertShowsDate(context, robot.now(context));
            robot.cancelDialog(context);

        } else {
            onView(withId(R.id.info_time_lightmap)).check(assertHidden);
        }
    }

    @Test @QuickTest
    public void test_LightmapDialog() {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertIsReset(context)
                .assertShowsDate(context, robot.now(context));
        //.captureScreenshot(context, "suntimes-dialog-lightmap0")
    }

    @Test
    public void test_LightmapDialog_menu()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context).sleep(500)
                .assertDialogShown(context);

        robot.clickTimeZoneLabel(context)
                .assertOverflowMenu_TimeZone(context)
                .cancelOverflowMenu_TimeZone(context);

        robot.clickObjectShadow(context)
                .assertOverflowMenu_ObjectShadow(context)
                .cancelOverflowMenu_ObjectShadow(context);

        robot.clickAltitudeField(context)
                .assertOverflowMenu_SeekAltitude(context)
                .cancelOverflowMenu_SeekAltitude(context);

        // :
        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .cancelOverflowMenu(context).sleep(1000);

        // : -> Seek Altitude
        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenu_SeekAltitude(context)
                .assertOverflowMenu_SeekAltitude(context)
                .cancelOverflowMenu_SeekAltitude(context).sleep(1000);

        // : -> Object Shadow
        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenu_ObjectShadow(context)
                .assertOverflowMenu_ObjectShadow(context)
                .click_ObjectShadow_more(context).click_ObjectShadow_less(context)
                .click_ObjectShadow_less(context).click_ObjectShadow_more(context)
                .cancelOverflowMenu_ObjectShadow(context).sleep(1000);

        // : -> timezone
        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context)
                .assertOverflowMenu_TimeZone(context)
                .cancelOverflowMenu_TimeZone(context).sleep(1000);

        // : -> view
        robot.showOverflowMenu(context)
                .clickOverflowMenu_ViewWith(context)
                .assertOverflowMenu_ViewWith(context)
                .cancelOverflowMenu_ViewWith(context).sleep(1000);

        // : -> options
        robot.showOverflowMenu(context)
                .clickOverflowMenu_Options(context)
                .assertOverflowMenu_Options(context)
                .cancelOverflowMenu_Options(context).sleep(1000);

        robot.clickSunlightButton(context);
        new LightGraphDialogTest.LightGraphDialogRobot().assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);

        robot.doubleRotateDevice(context)
                .assertDialogShown(context)
                .cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_playPauseReset()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context).sleep(500)
                .assertDialogShown(context)
                .assertIsReset(context)
                .assertShowsDate(context, robot.now(context));

        // TODO

        // play
        //robot.clickPlayButton(context).sleep(1000)
        //.assertIsPlaying(context)
        //.sleep(1000);
        //;

        // pause
        //robot.clickPauseButton(context).sleep(1000)
        //.assertIsPaused(context)
        //.sleep(1000);
        //;

        // play
        //robot.clickPlayButton(context)
        //.assertIsPlaying(context)
        //.sleep(1000);
        //;

        // reset
        //robot.clickResetButton(context)
        //        .assertIsReset(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_seekAltitude()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        double[] altitudes = new double[] { -25, -18, -12, -6, 6, 12, 18, 25 };
        for (int i=0; i<2; i++)
        {
            for (double altitude : altitudes)
            {
                robot.clickAltitudeField(context)
                        .input_SeekAltitude_altitude(context, altitude)
                        .assert_SeekAltitude_altitude(context, altitude).sleep(750);

                if (i % 2 == 0)
                    robot.click_SeekAltitude_rising(context);
                else robot.click_SeekAltitude_setting(context);

                robot.sleep(1000)
                        .assertAltitudeIsApproximateTo(context, altitude, 0.25)
                        .clickResetButton(context).sleep(250);
            }
        }

        robot.clickNoonButton(context)
                .assertCanBeReset(context)
                .clickResetButton(context).sleep(500)
                .assertIsReset(context).sleep(1000);

        robot.clickSunriseButton(context)
                .assertAltitudeIsApproximateTo(context, 0, 1.5)
                .clickResetButton(context).sleep(500)
                .assertIsReset(context).sleep(1000);

        robot.clickSunsetButton(context)
                .assertAltitudeIsApproximateTo(context, 0, 1.5)
                .clickResetButton(context).sleep(500)
                .assertIsReset(context).sleep(1000);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_timezone()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertShowsDate(context, robot.now(context));

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenu_TimeZone(context).assertOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_LocalMean(context)
                .assertShowsDate(context, robot.now(timeZone_LocalMean(context)))
                .assert_TimeZone_LocalMean(context).sleep(1000);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_ApparentSolar(context)
                .assertShowsDate(context, robot.now(timeZone_ApparentSolar(context)))
                .assert_TimeZone_ApparentSolar(context).sleep(1000);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_UTC(context)
                .assert_TimeZone_UTC(context)
                .assertShowsDate(context, robot.now(timeZone_UTC()))
                .doubleRotateDevice(context)
                .assertDialogShown(context);
        robot.assert_TimeZone_UTC(context).sleep(1000);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_TimeZone(context)
                .clickOverflowMenu_TimeZone_Suntimes(context)
                .assertShowsDate(context, robot.now(timeZone_Suntimes(context)))
                .assert_TimeZone_Suntimes(context).sleep(1000);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_viewWith_Suntimes()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertIsReset(context)
                .assertShowsDate(context, robot.now(context));

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenu_ViewWith(context)
                .assertOverflowMenu_ViewWith(context).sleep(500)
                .clickOverflowMenu_ViewWith_Suntimes(context).sleep(500)
                .assertDialogShown(context);    // still shown
                // TODO: assert date

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_viewWith_moon()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertIsReset(context)
                .assertShowsDate(context, robot.now(context));

        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .clickOverflowMenu_ViewWith(context)
                .assertOverflowMenu_ViewWith(context).sleep(500)
                .clickOverflowMenu_ViewWith_MoonDialog(context).sleep(500);

        new MoonDialogTest.MoonDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
                // TODO: assert date

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_LightmapDialog_viewWith_worldMap()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightMapDialogRobot robot = new LightMapDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertIsReset(context)
                .assertShowsDate(context, robot.now(context));

        robot.showOverflowMenu(context)
                .clickOverflowMenu_ViewWith(context)
                .assertOverflowMenu_ViewWith(context).sleep(500)
                .clickOverflowMenu_ViewWith_WorldMapDialog(context).sleep(500);

        WorldMapDialogTest.WorldMapDialogRobot robot1 = new WorldMapDialogTest.WorldMapDialogRobot();
        robot1.assertDialogShown(context)
                .assertCanBeReset(context)
                .assertShowsDate(context, robot1.now(context))
                .cancelDialog(context).assertDialogNotShown(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    /**
     * LightmapDialogRobot
     */
    public static class LightMapDialogRobot extends DialogTest.DialogRobot<LightMapDialogRobot>
    {
        public LightMapDialogRobot() {
            super();
            setRobot(this);
        }

        public LightMapDialogRobot showDialog(Activity context) {
            //onView(withId(R.id.info_time_lightmap)).perform(click());
            openActionBarOverflowOrOptionsMenu(context);
            onView(withText(R.string.configAction_sunDialog)).perform(click());
            return this;
        }
        @Override
        public LightMapDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.dialog_lightmap_layout)).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickTimeZoneLabel(Context context) {
            onView(withId(R.id.info_time_solar)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickAltitudeField(Context context) {
            onView(withId(R.id.clickArea_altitude)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickObjectShadow(Context context) {
            onView(withId(R.id.info_shadow_layout)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickSunlightButton(Context context) {
            onView(withId(R.id.lightgraph_button)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickSunriseButton(Context context) {
            onView(withId(R.id.clickArea_rising)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickNoonButton(Context context) {
            onView(withId(R.id.clickArea_noon)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickSunsetButton(Context context) {
            onView(withId(R.id.clickArea_setting)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickPlayButton(Context context) {
            onView(withId(R.id.media_play)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickPauseButton(Context context) {
            onView(withId(R.id.media_pause)).perform(click());
            return this;
        }
        public LightMapDialogRobot clickResetButton(Context context) {
            onView(withId(R.id.media_reset)).perform(click());
            return this;
        }
        public LightMapDialogRobot showOverflowMenu(Context context) {
            onView(withId(R.id.media_menu)).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ViewWith(Context context) {
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu_ViewWith(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ViewWith_Suntimes(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ViewWith_MoonDialog(Context context) {
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ViewWith_WorldMapDialog(Context context) {
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ViewWith_Calendar(Context context) {
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_TimeZone_UTC(Context context) {
            onView(withText(R.string.time_utc)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_TimeZone_LocalMean(Context context) {
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_TimeZone_ApparentSolar(Context context) {
            onView(withText(R.string.time_apparent)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_TimeZone_Suntimes(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_SeekAltitude(Context context) {
            onView(withText(R.string.configAction_seekAltitude)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu_SeekAltitude(Context context) {
            onView(withText(R.string.seekAltitude_rising)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot clickOverflowMenu_ObjectShadow(Context context) {
            onView(withText(R.string.configLabel_general_observerheight)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot cancelOverflowMenu_ObjectShadow(Context context) {
            onView(withId(R.id.btn_less)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightMapDialogRobot click_ObjectShadow_less(Context context) {
            onView(withId(R.id.btn_less)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot click_ObjectShadow_more(Context context) {
            onView(withId(R.id.btn_more)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot click_SeekAltitude_rising(Context context) {
            onView(withText(R.string.seekAltitude_rising)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot click_SeekAltitude_setting(Context context) {
            onView(withText(R.string.seekAltitude_setting)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightMapDialogRobot input_SeekAltitude_altitude(Context context, double altitude)
        {
            onView(withId(R.id.edit_altitude)).inRoot(isPlatformPopup()).perform(replaceText(altitude + ""));
            return this;
        }

        @Override
        public LightMapDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.dialog_lightmap_layout)).check(assertShown);
            onView(withId(R.id.lightgraph_button)).check(assertShown);
            onView(withId(R.id.lightgraph_button)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.lightgraph_button)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.info_time_solar)).check(assertShown);
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.media_reset)).check(assertShown);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.media_play)).check(assertShown);
            onView(withId(R.id.media_play)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.media_play)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.media_menu)).check(assertShown);
            onView(withId(R.id.media_menu)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.media_menu)).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        @Override
        public LightMapDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.dialog_lightmap_layout)).check(doesNotExist());
            return this;
        }
        public LightMapDialogRobot assertOverflowMenuShown(Context context)
        {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_seekAltitude)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public LightMapDialogRobot assertOverflowMenu_ViewWith(Context context)
        {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public LightMapDialogRobot assertOverflowMenu_Options(Context context)
        {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_lineGraph)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.graph_option_axis)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.graph_option_grid)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.graph_option_labels)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.graph_option_moon)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.graph_option_filledpath)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public LightMapDialogRobot assertOverflowMenu_TimeZone(Context context)
        {
            onView(withText(R.string.time_utc)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.time_apparent)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public LightMapDialogRobot assertOverflowMenu_SeekAltitude(Context context)
        {
            onView(withId(R.id.edit_altitude)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withId(R.id.edit_altitude)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertEnabled);
            onView(withText(R.string.seekAltitude_rising)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.seekAltitude_rising)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertEnabled);
            onView(withText(R.string.seekAltitude_rising)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            onView(withText(R.string.seekAltitude_setting)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.seekAltitude_setting)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertEnabled);
            onView(withText(R.string.seekAltitude_setting)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        public LightMapDialogRobot assertOverflowMenu_ObjectShadow(Context context)
        {
            onView(withId(R.id.btn_less)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withId(R.id.btn_less)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.seek_objheight)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withId(R.id.btn_more)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withId(R.id.btn_more)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        public LightMapDialogRobot assert_TimeZone_UTC(Context context) {
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertContainsText("UTC"));
            return this;
        }
        public LightMapDialogRobot assert_TimeZone_LocalMean(Context context) {
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertContainsText("Local Mean"));
            return this;
        }
        public LightMapDialogRobot assert_TimeZone_ApparentSolar(Context context) {
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertContainsText("Apparent Solar"));
            return this;
        }
        public LightMapDialogRobot assert_TimeZone_Suntimes(Context context) {
            String tzID = WidgetSettings.loadTimezonePref(context, 0);
            onView(withId(R.id.info_time_solar)).check(ViewAssertionHelper.assertContainsText(tzID));
            return this;
        }
        public LightMapDialogRobot assertIsPlaying(Context context) {
            //onView(withId(R.id.media_play)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.media_pause)).check(assertShown);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }
        public LightMapDialogRobot assertIsPaused(Context context) {
            onView(withId(R.id.media_play)).check(assertShown);
            onView(withId(R.id.media_pause)).check(assertHidden);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }
        public LightMapDialogRobot assertIsReset(Context context) {
            onView(withId(R.id.media_play)).check(assertShown);
            onView(withId(R.id.media_pause)).check(assertHidden);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertDisabled);
            return this;
        }
        public LightMapDialogRobot assertCanBeReset(Context context) {
            onView(withId(R.id.media_reset)).check(assertShown);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        public LightMapDialogRobot assert_SeekAltitude_altitude(Context context, double altitude)
        {
            onView(withId(R.id.edit_altitude)).inRoot(isPlatformPopup()).check(matches(withText(altitude + "")));
            return this;
        }
        public LightMapDialogRobot assertAltitudeIsApproximateTo(Context context, double altitude, double tolerance)
        {
            onView(allOf(
                    withId(R.id.info_sun_elevation_current),
                            withTextAsDoubleApproximateTo(altitude, tolerance))
            ).check(assertShown);
            return this;
        }

        public LightMapDialogRobot assertShowsDate(Context context, @NonNull Calendar date) {
            return assertShowsDate(date, WidgetSettings.loadTimeFormatModePref(context, 0), false);
        }
        public LightMapDialogRobot assertShowsDate(@NonNull Calendar date, WidgetSettings.TimeFormatMode withMode, boolean withSeconds)
        {
            SimpleDateFormat[] formats = (withMode == WidgetSettings.TimeFormatMode.MODE_12HR)
                    ? (withSeconds ? timeDateFormats12s : timeDateFormats12)
                    : (withSeconds ? timeDateFormats24s : timeDateFormats24);
            long tolerance = (withSeconds
                    ? 10 * 1000
                    : 90 * 1000);

            onView(allOf(withId(R.id.info_time_solar),
                    withTextAsDate(formats, date, tolerance, true)
            )).check(assertShown);
            return this;
        }

        @Override
        public Calendar now(Context context)
        {
            String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTMAP, WidgetTimezones.LocalMeanTime.TIMEZONEID);
            TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId)
                    ? appTimeZone(context)
                    : WidgetTimezones.getTimeZone(tzId, appLocation(context).getLongitudeAsDouble(), appCalculator(context));
            return now(timezone);
        }

    }

}
