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

package com.forrestguice.suntimeswidget.equinox;

import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialogTest;
import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.calculator.CalculatorProviderTest;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.suntimeswidget.map.WorldMapDialogTest;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import android.support.annotation.Nullable;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.forrestguice.suntimeswidget.DialogTest.DialogRobot.lastYear;
import static com.forrestguice.suntimeswidget.DialogTest.DialogRobot.nextYear;
import static com.forrestguice.suntimeswidget.DialogTest.DialogRobot.thisYear;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.isCheckBoxWithTextInDropDownMenu;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.isMenuDropDownListView;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.isRadioButtonWithTextInDropDownMenu;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.withTextAsDate;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class EquinoxCardDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

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

    @Override
    public void overrideConfigState(Context context) {
        super.overrideConfigState(context);
        AppSettings.saveShowCrossQuarterPref(context, false);
    }
    @Override
    protected void saveConfigState(Context context) {
        savedState_crossQuarterDays = AppSettings.loadShowCrossQuarterPref(context);
        savedState_trackingMode = WidgetSettings.loadTrackingModePref(context, 0);
    }
    @Override
    protected void restoreConfigState(Context context) {
        AppSettings.saveShowCrossQuarterPref(context, savedState_crossQuarterDays);
        WidgetSettings.saveTrackingModePref(context, 0, savedState_trackingMode);
    }
    protected boolean savedState_crossQuarterDays = false;
    protected TrackingMode savedState_trackingMode = null;

    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    @Test
    public void test_mainCard_showEquinox()
    {
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        Activity context = activityRule.getActivity();
        if (AppSettings.loadShowEquinoxPref(context))
        {
            onView(withId(R.id.info_date_solsticequinox)).check(assertShown);
            onView(withId(R.id.info_date_solsticequinox)).check(assertClickable);

            if (!AppSettings.loadShowEquinoxDatePref(context)) {
                robot.assertCard_allHideDateField(context);
            }

            onView(withId(R.id.info_date_solsticequinox)).perform(click());
            robot.assertDialogShown(context).sleep(1000)
                    .cancelDialog(context);

        } else {
            onView(withId(R.id.info_date_solsticequinox)).check(ViewAssertionHelper.assertHidden);
        }
    }

    @Test
    public void test_EquinoxDialog()
    {
        Activity context = activityRule.getActivity();
        AppSettings.saveShowCrossQuarterPref(context, false);

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);
                //.captureScreenshot(context, "suntimes-dialog-equinox0")

        robot.clickYearLabel(context).sleep(1500)
                .assertCard_allShowCorrectDate(context, thisYear(), true);

        // :
        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context).sleep(500)
                .cancelOverflowMenu(context).sleep(500);

        // : -> Options
        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Options(context).sleep(500)
                .assertOverflowMenu_Options(context)
                .cancelOverflowMenu_Options(context).sleep(500);

        // : -> Track
        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track(context)
                .cancelOverflowMenu_Track(context).sleep(500);

        // : -> Help
        robot.showOverflowMenu(context)
                .clickOverflowMenu_Help(context)
                .assertHelpShown(context).sleep(500)
                .cancelHelp(context).sleep(500);

        robot.doubleRotateDevice(context)
                .assertDialogShown(context)
                .cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_changeYear()
    {
        Activity context = activityRule.getActivity();
        AppSettings.saveShowCrossQuarterPref(context, false);

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        // <- Year -> (buttons)
        robot.clickYearLabel(context)
                .assertYearLabelIs(thisYear()).sleep(1000)
                .assertCard_allShowCorrectDate(context, thisYear(), true);

        robot.clickYearPrevious(context)
                .assertYearLabelIs(lastYear())
                .assertCard_allMarkedPast(context);

        robot.clickYearLabel(context)
                .assertYearLabelIs(thisYear());
        robot.clickYearNext(context).sleep(1000)
                .assertYearLabelIs(nextYear())
                .assertCard_allMarkedFuture(context);

        // <- Year -> (swipe gesture)
        robot.clickYearLabel(context)
                .assertYearLabelIs(thisYear());
        robot.swipeCardPrevious(context).sleep(1000)
                .assertYearLabelIs(lastYear())
                .assertCard_allMarkedPast(context);

        robot.clickYearLabel(context)
                .assertYearLabelIs(thisYear());
        robot.swipeCardNext(context).sleep(1000)
                .assertYearLabelIs(nextYear())
                .assertCard_allMarkedFuture(context);

        robot.clickYearLabel(context);
        robot.cancelDialog(context).sleep(1500)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_EquinoxDialog_collapseExpand()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        robot.collapseSheet().sleep(1500);
        robot.assertSheetIsCollapsed(context);
        robot.expandSheet().sleep(1500)
                .assertDialogShown(context);

        robot.cancelDialog(context).sleep(1500)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_EquinoxDialog_tracking()
    {
        Activity context = activityRule.getActivity();
        WidgetSettings.saveTrackingModePref(context, 0, TrackingMode.SOONEST);   // test begins with tracking set to "soonest"

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isSoonest(context)            // assert "soonest"

                .clickOverflowMenu_Track_Closest(context).sleep(1000)   // click "closest"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(TrackingMode.CLOSEST, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isClosest(context)            // assert "closest"

                .clickOverflowMenu_Track_Recent(context).sleep(1000)    // "click "recent"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(TrackingMode.RECENT, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isRecent(context)            // assert "recent"

                .clickOverflowMenu_Track_Soonest(context).sleep(1000)    // "click "soonest"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(TrackingMode.SOONEST, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isSoonest(context)            // assert "soonest"
                .cancelOverflowMenu_Track(context).sleep(500);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_EquinoxDialog_crossQuarterDays()
    {
        Activity context = activityRule.getActivity();
        AppSettings.saveShowCrossQuarterPref(context, false);    // test begins with "show cross-quarter days" set to false

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        // : -> Options -> Show Cross Quarter Days (toggle it twice)
        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Options(context).sleep(500)
                .assertOverflowMenu_Options_crossQuarterDaysDisabled(context)
                .clickOverflowMenu_Options_CrossQuarterDays(context).sleep(1000);
        assertTrue(AppSettings.loadShowCrossQuarterPref(context));

        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Options(context).sleep(500)
                .assertOverflowMenu_Options(context)
                .assertOverflowMenu_Options_crossQuarterDaysEnabled(context)
                .clickOverflowMenu_Options_CrossQuarterDays(context).sleep(1000);
        assertFalse(AppSettings.loadShowCrossQuarterPref(context));

        robot.showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Options(context).sleep(500)
                .assertOverflowMenu_Options(context)
                .assertOverflowMenu_Options_crossQuarterDaysDisabled(context)
                .cancelOverflowMenu(context).sleep(1000);

        robot.cancelDialog(context).sleep(1500)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertCard_noFocus(context);

        // click each item and check focus
        SolsticeEquinoxMode[] modes = (AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values() : SolsticeEquinoxMode.partialValues(false));

        for (SolsticeEquinoxMode mode : modes)
        {
            robot.clickCardItem(context, mode).sleep(1000)
                    .assertCard_isFocused(context, mode, true)

                    .clickCardItem_menu(context, mode).sleep(500)
                    .assertCard_isMenuShown(context, mode)
                    .cancelCardItem_menu(context, mode).sleep(500);
        }

        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_share()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        for (SolsticeEquinoxMode mode : modes)
        {
            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_share(context, mode);
        }
        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_setAlarm()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        for (SolsticeEquinoxMode mode : modes)
        {
            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_setAlarm(context, mode).sleep(500);

            new AlarmCreateDialogTest.AlarmDialogRobot()
                    .assertDialogShown(context)
                    .assertTabAtPosition(context, 0)
                    .assertAlarmDialogEvent(SolarEvents.valueOf(mode))
                    // TODO assertDate
                    .cancelDialog(context);
        }

        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_viewWith_suntimes()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        for (SolsticeEquinoxMode mode : modes)
        {
            robot.assertDialogShown(context)
                    .assertCard_showsDateFor(context, mode, thisYear(), true);

            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Suntimes(context, mode).sleep(1000)
                    .assertSheetIsCollapsed(context);

            //Calendar eventTime = CalculatorProviderTest.lookupEventTime(context, mode, thisYear(), robot1.now(context).getTimeZone());
            // TODO assertDate

            robot.expandSheet();
        }

        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_viewWith_sun()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .clickYearLabel(context)
                .assertDialogShown(context)
                .assertYearLabelIs(thisYear());

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        LightMapDialogTest.LightMapDialogRobot robot1 = new LightMapDialogTest.LightMapDialogRobot();
        for (SolsticeEquinoxMode mode : modes)
        {
            Calendar eventTime = CalculatorProviderTest.lookupEventTime(context, mode, thisYear(), robot1.now(context).getTimeZone());
            robot.assertCard_showsDateFor(context, mode, thisYear(), true);
            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Sun(context, mode).sleep(1000);
            robot1.assertDialogShown(context)
                    .assertShowsDate(context, eventTime)
                    .cancelDialog(context);
        }
        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_viewWith_moon()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .clickYearLabel(context)
                .assertYearLabelIs(thisYear());

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        MoonDialogTest.MoonDialogRobot robot1 = new MoonDialogTest.MoonDialogRobot();
        for (SolsticeEquinoxMode mode : modes)
        {
            Calendar eventTime = CalculatorProviderTest.lookupEventTime(context, mode, thisYear(), robot1.now(context).getTimeZone());
            robot.assertCard_showsDateFor(context, mode, thisYear(), true);
            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Moon(context, mode).sleep(1000);
            robot1.assertDialogShown(context)
                    //.assertShowsDate(context, eventTime)    // TODO
                    .cancelDialog(context);
        }
        robot.cancelDialog(context);
    }

    @Test
    public void test_EquinoxDialog_cardItems_viewWith_worldmap()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .clickYearLabel(context)
                .assertDialogShown(context)
                .assertYearLabelIs(thisYear());

        SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context)
                ? SolsticeEquinoxMode.values()
                : SolsticeEquinoxMode.partialValues(false);

        WorldMapDialogTest.WorldMapDialogRobot robot1 = new WorldMapDialogTest.WorldMapDialogRobot();
        for (SolsticeEquinoxMode mode : modes)
        {
            Calendar eventTime = CalculatorProviderTest.lookupEventTime(context, mode, thisYear(), robot1.now(context).getTimeZone());
            robot.assertCard_showsDateFor(context, mode, thisYear(), true);
            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_WorldMap(context, mode).sleep(1000);
            robot1.assertDialogShown(context)
                    .assertShowsDate(context, eventTime)
                    .cancelDialog(context);
        }
        robot.cancelDialog(context);
    }

    /**
     * EquinoxDialogRobot
     */
    public static class EquinoxDialogRobot extends DialogTest.DialogRobot<EquinoxDialogRobot>
    {
        public EquinoxDialogRobot() {
            super();
            setRobot(this);
        }

        public EquinoxDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(context);
            onView(withText(R.string.configAction_equinoxDialog)).perform(click());
            return this;
        }

        @Override
        public EquinoxDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.dialog_header)).perform(pressBack());
            return this;
        }

        public EquinoxDialogRobot clickYearLabel(Context context) {
            onView(withId(R.id.text_title1)).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickYearNext(Context context) {
            onView(withId(R.id.info_time_nextbtn1)).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickYearPrevious(Context context) {
            onView(withId(R.id.info_time_prevbtn1)).perform(click());
            return this;
        }
        public EquinoxDialogRobot swipeCardNext(Context context) {
            onView(withId(R.id.info_equinoxsolstice_flipper1)).perform(swipeLeft());
            return this;
        }
        public EquinoxDialogRobot swipeCardPrevious(Context context) {
            onView(withId(R.id.info_equinoxsolstice_flipper1)).perform(swipeRight());
            return this;
        }

        public EquinoxDialogRobot clickCardItem(Context context, SolsticeEquinoxMode mode) {
            onView(allOf(withText(mode.getLongDisplayString()), isDisplayed())).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu(Context context, SolsticeEquinoxMode mode) {
            onView(allOf( withId(R.id.menu_button), isDisplayed(),
                    withParent(hasSibling(withText(mode.getLongDisplayString())))
            )).perform(click());
            return this;
        }
        public EquinoxDialogRobot cancelCardItem_menu(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_share(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_setAlarm(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_setAlarm)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public EquinoxDialogRobot clickCardItem_menu_viewWith(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Suntimes(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Sun(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Moon(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_WorldMap(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot cancelCardItem_menu_viewWith(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public EquinoxDialogRobot showOverflowMenu(Context context) {
            onView(allOf(
                    withId(R.id.menu_button),
                    withParent(withId(R.id.dialog_header))
            )).perform(click());
            return this;
        }

        public EquinoxDialogRobot clickOverflowMenu_Track(Context context) {
            String text = context.getString(R.string.configLabel_general_trackingMode);
            text = text.replaceAll(":", "");
            onView(withText(text)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickOverflowMenu_Track_Closest(Context context) {
            onView(withText(R.string.trackingMode_closest)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickOverflowMenu_Track_Recent(Context context) {
            onView(withText(R.string.trackingMode_recent)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickOverflowMenu_Track_Soonest(Context context) {
            onView(withText(R.string.trackingMode_soonest)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot cancelOverflowMenu_Track(Context context) {
            onView(withText(R.string.trackingMode_closest)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public EquinoxDialogRobot clickOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickOverflowMenu_Options_CrossQuarterDays(Context context) {
            onView(withText(R.string.configLabel_ui_showCrossQuarter)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickOverflowMenu_Help(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public EquinoxDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public EquinoxDialogRobot cancelOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public EquinoxDialogRobot cancelHelp(Context context) {
            onView(withId(R.id.txt_help_content)).perform(pressBack());
            return this;
        }

        @Override
        public EquinoxDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.dialog_header)).check(assertShown);
            onView(withId(R.id.year_info_layout)).check(assertShown);

            //WidgetSettings.SolsticeEquinoxMode[] modes = (AppSettings.loadShowCrossQuarterPref(context)
            //        ? WidgetSettings.SolsticeEquinoxMode.values() : WidgetSettings.SolsticeEquinoxMode.partialValues(false));
            //for (WidgetSettings.SolsticeEquinoxMode mode : modes) {
            //    assertCard_isShown(mode, true);
            //}
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenuShown(Context context) {
            onView(isMenuDropDownListView()).inRoot(isPlatformPopup()).check(matches(hasMinimumChildCount(3)));
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        @Override
        public EquinoxDialogRobot assertSheetIsCollapsed(Context context)
        {
            onView(withId(R.id.dialog_header)).check(assertShown);
            onView(withId(R.id.year_info_layout)).check(ViewAssertionHelper.assertHidden);
            return this;
        }

        public EquinoxDialogRobot assertOverflowMenu_Track(Context context)
        {
            onView(withText(R.string.trackingMode_soonest)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.trackingMode_closest)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.trackingMode_recent)).inRoot(isPlatformPopup()).check(assertShown);

            switch (WidgetSettings.loadTrackingModePref(context, 0)) {
                case RECENT: assertOverflowMenu_Track_isRecent(context); break;
                case SOONEST: assertOverflowMenu_Track_isSoonest(context); break;
                case CLOSEST: assertOverflowMenu_Track_isClosest(context); break;
            }
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenu_Track_isSoonest(Context context) {
            onView(isRadioButtonWithTextInDropDownMenu(R.string.trackingMode_soonest)).inRoot(isPlatformPopup())
                    .check(ViewAssertionHelper.assertChecked);
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenu_Track_isClosest(Context context) {
            onView(isRadioButtonWithTextInDropDownMenu(R.string.trackingMode_closest)).inRoot(isPlatformPopup())
                    .check(ViewAssertionHelper.assertChecked);
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenu_Track_isRecent(Context context) {
            onView(isRadioButtonWithTextInDropDownMenu(R.string.trackingMode_recent)).inRoot(isPlatformPopup())
                    .check(ViewAssertionHelper.assertChecked);
            return this;
        }

        public EquinoxDialogRobot assertOverflowMenu_Options(Context context)
        {
            onView(isMenuDropDownListView()).inRoot(isPlatformPopup()).check(matches(hasMinimumChildCount(2)));
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(assertShown);

            onView(withText(R.string.configLabel_ui_showCrossQuarter)).inRoot(isPlatformPopup()).check(assertShown);
            if (AppSettings.loadShowCrossQuarterPref(context)) {
                assertOverflowMenu_Options_crossQuarterDaysEnabled(context);
            } else {
                assertOverflowMenu_Options_crossQuarterDaysDisabled(context);
            }
            return this;
        }

        public EquinoxDialogRobot assertOverflowMenu_Options_crossQuarterDaysEnabled(Context context) {
            onView(isCheckBoxWithTextInDropDownMenu(R.string.configLabel_ui_showCrossQuarter)).inRoot(isPlatformPopup())
                    .check(ViewAssertionHelper.assertChecked);
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenu_Options_crossQuarterDaysDisabled(Context context) {
            onView(isCheckBoxWithTextInDropDownMenu(R.string.configLabel_ui_showCrossQuarter)).inRoot(isPlatformPopup())
                    .check(ViewAssertionHelper.assertNotChecked);
            return this;
        }

        public EquinoxDialogRobot assertHelpShown(Context context) {
            onView(withId(R.id.txt_help_content)).check(assertShown);
            return this;
        }

        public EquinoxDialogRobot assertYearLabelIs(int year) {
            return assertYearLabelIs(year + "");
        }
        public EquinoxDialogRobot assertYearLabelIs(String yearText)
        {
            onView(allOf(
                    hasSibling(withId(R.id.info_time_nextbtn1)),
                    hasSibling(withId(R.id.info_time_prevbtn1)),
                    withText(yearText))
            ).check(assertShown);
            return this;
        }

        public EquinoxDialogRobot assertCard_noFocus(Context context)
        {
            for (SolsticeEquinoxMode mode : SolsticeEquinoxMode.partialValues(false)) {
                assertCard_isFocused(context, mode, false);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_isFocused(Context context, SolsticeEquinoxMode mode, boolean focused)
        {
            onView(allOf(withId(R.id.focusView),
                    hasSibling(withChild(withText(mode.getLongDisplayString())))
            )).check(focused ? assertShown : ViewAssertionHelper.assertHidden);

            onView(allOf(withId(R.id.menu_button),
                    withParent(hasSibling(withText(mode.getLongDisplayString())))
            )).check(focused ? assertShown : ViewAssertionHelper.assertHidden);
            return this;
        }

        public EquinoxDialogRobot assertCard_isShown(SolsticeEquinoxMode mode, boolean shown)
        {
            onView(allOf(withId(R.id.text_label), withText(mode.getLongDisplayString()))).check(assertShown);
            onView(allOf(withId(R.id.text_datetime), withParent(withParent(hasSibling(withText(mode.getLongDisplayString()))))))
                    .check(shown ? assertShown : doesNotExist());
            onView(allOf(withId(R.id.text_note), withParent(withParent(hasSibling(withText(mode.getLongDisplayString()))))))
                    .check(shown ? assertShown : doesNotExist());
            return this;
        }

        public EquinoxDialogRobot assertCard_showsDateField(Context context, SolsticeEquinoxMode mode) {
            assertCard_showsDateField(context, mode.getLongDisplayString());
            return this;
        }
        public EquinoxDialogRobot assertCard_showsDateField(Context context, String withLabel)
        {
            onView(allOf(withId(R.id.text_datetime),
                    withParent(withParent(hasSibling(withText(withLabel))))
            )).check(assertShown);
            return this;
        }

        public EquinoxDialogRobot assertCard_allHideDateField(Context context)
        {
            for (SolsticeEquinoxMode mode : SolsticeEquinoxMode.partialValues(false)) {
                assertCard_hidesDateField(context, mode);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_hidesDateField(Context context, SolsticeEquinoxMode mode) {
            assertCard_hidesDateField(context, mode.getLongDisplayString());
            return this;
        }
        public EquinoxDialogRobot assertCard_hidesDateField(Context context, String withLabel)
        {
            onView(allOf(withId(R.id.text_datetime),
                    withParent(withParent(hasSibling(withText(withLabel))))
            )).check(ViewAssertionHelper.assertHidden);
            return this;
        }

        public EquinoxDialogRobot assertCard_allShowCorrectDate(Context context, int forYear, boolean withSeconds)
        {
            for (SolsticeEquinoxMode mode : SolsticeEquinoxMode.partialValues(false)) {
                assertCard_showsDateFor(context, mode, forYear, withSeconds);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_showsDateFor(Context context, SolsticeEquinoxMode mode, int forYear, boolean withSeconds)
        {
            TimeFormatMode withMode = WidgetSettings.loadTimeFormatModePref(context, 0);
            Calendar event = CalculatorProviderTest.lookupEventTime(context, mode, forYear);
            assertCard_showsDate(event, mode.getLongDisplayString(), withMode, withSeconds);
            return this;
        }
        public EquinoxDialogRobot assertCard_showsDate(@Nullable Calendar date, String withLabel, TimeFormatMode withMode, boolean withSeconds)
        {
            if (date != null) {
                SimpleDateFormat[] formats = (withMode == TimeFormatMode.MODE_12HR)
                        ? (withSeconds ? timeDateFormats12s : timeDateFormats12)
                        : (withSeconds ? timeDateFormats24s : timeDateFormats24);
                long tolerance = (withSeconds ? 1000 : 60 * 1000);

                onView(allOf(withId(R.id.text_datetime),
                        withTextAsDate(formats, date, tolerance, false),
                        withParent(withParent(hasSibling(withText(withLabel))))
                )).check(assertShown);
            }
            return this;
        }

        public EquinoxDialogRobot assertCard_allMarkedPast(Context context) {
            for (SolsticeEquinoxMode mode : SolsticeEquinoxMode.partialValues(false)) {
                assertCard_isMarkedPast(context, mode);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_allMarkedFuture(Context context) {
            for (SolsticeEquinoxMode mode : SolsticeEquinoxMode.partialValues(false)) {
                assertCard_isMarkedFuture(context, mode);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_isMarkedPast(Context context, SolsticeEquinoxMode mode) {
            onView(allOf(isDisplayed(),withText(mode.getLongDisplayString()))).check(ViewAssertionHelper.assertDisabled);
            return this;
        }
        public EquinoxDialogRobot assertCard_isMarkedFuture(Context context, SolsticeEquinoxMode mode) {
            onView(allOf(isDisplayed(), withText(mode.getLongDisplayString()))).check(ViewAssertionHelper.assertEnabled);
            return this;
        }

        public EquinoxDialogRobot assertCard_isMenuShown(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_setAlarm)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public EquinoxDialogRobot assertCard_isMenuShown_viewWith(Context context, SolsticeEquinoxMode mode) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }
}
