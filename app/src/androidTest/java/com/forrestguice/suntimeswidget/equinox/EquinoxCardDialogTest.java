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

import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.suntimeswidget.map.WorldMapDialogTest;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
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
import java.util.Calendar;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeLeft;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeRight;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.matches;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isCheckBoxWithTextInDropDownMenu;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isMenuDropDownListView;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isRadioButtonWithTextInDropDownMenu;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withChild;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EquinoxCardDialogTest extends SuntimesActivityTestBase
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
    public void test_showEquinoxDialog()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);
                //.captureScreenshot(context, "suntimes-dialog-equinox0")

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

        robot.rotateDevice(context)
                .assertDialogShown(context);

        robot.cancelDialog(context).sleep(1500)
                .assertDialogNotShown(context);

        //if (AppSettings.loadShowEquinoxPref(context)) {   // TODO: move
        //    onView(withId(R.id.info_date_solsticequinox)).check(matches(isDisplayed()));
        //} else {
        //    onView(withId(R.id.info_date_solsticequinox)).check(matches(not(isDisplayed())));
        //}
    }

    @Test
    public void test_showEquinoxDialog_changeYear()
    {
        Activity context = activityRule.getActivity();
        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);

        // <- Year -> (buttons)
        robot.clickYearLabel(context)
                .assertYearLabelIs_thisYear(context);
        robot.clickYearPrevious(context)
                .assertYearLabelIs_lastYear(context);
        robot.clickYearLabel(context)
                .assertYearLabelIs_thisYear(context);
        robot.clickYearNext(context)
                .assertYearLabelIs_nextYear(context);

        // <- Year -> (swipe gesture)
        robot.clickYearLabel(context)
                .assertYearLabelIs_thisYear(context);
        robot.swipeCardPrevious(context).sleep(1000)
                .assertYearLabelIs_lastYear(context);
        robot.clickYearLabel(context)
                .assertYearLabelIs_thisYear(context);
        robot.swipeCardNext(context).sleep(1000)
                .assertYearLabelIs_nextYear(context)
                .clickYearLabel(context);

        robot.cancelDialog(context).sleep(1500)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_showEquinoxDialog_collapseExpand()
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
    public void test_showEquinoxDialog_tracking()
    {
        Activity context = activityRule.getActivity();
        WidgetSettings.TrackingMode savedState = WidgetSettings.loadTrackingModePref(context, 0);
        WidgetSettings.saveTrackingModePref(context, 0, WidgetSettings.TrackingMode.SOONEST);

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isSoonest(context)            // assert "soonest"

                .clickOverflowMenu_Track_Closest(context).sleep(1000)   // click "closest"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(WidgetSettings.TrackingMode.CLOSEST, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isClosest(context)            // assert "closest"

                .clickOverflowMenu_Track_Recent(context).sleep(1000)    // "click "recent"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(WidgetSettings.TrackingMode.RECENT, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isRecent(context)            // assert "recent"

                .clickOverflowMenu_Track_Soonest(context).sleep(1000)    // "click "soonest"
                .assertDialogNotShown(context);    // closes dialog
        assertEquals(WidgetSettings.TrackingMode.SOONEST, WidgetSettings.loadTrackingModePref(context, 0));

        robot.showDialog(context)
                .assertDialogShown(context)
                .showOverflowMenu(context).sleep(500)
                .clickOverflowMenu_Track(context).sleep(500)
                .assertOverflowMenu_Track_isSoonest(context)            // assert "soonest"
                .cancelOverflowMenu_Track(context).sleep(500);

        WidgetSettings.saveTrackingModePref(context, 0, savedState);
        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_showEquinoxDialog_crossQuarterDays()
    {
        Activity context = activityRule.getActivity();
        boolean savedState = AppSettings.loadShowCrossQuarterPref(context);
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
        AppSettings.saveShowCrossQuarterPref(context, savedState);
    }

    @Test
    public void test_showEquinoxDialog_cardItems()
    {
        Activity context = activityRule.getActivity();
        boolean savedState = AppSettings.loadShowCrossQuarterPref(context);
        AppSettings.saveShowCrossQuarterPref(context, false);    // test begins with "show cross-quarter days" set to false

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertCard_noFocus(context);

        // click each item and check focus
        for (WidgetSettings.SolsticeEquinoxMode mode : WidgetSettings.SolsticeEquinoxMode.partialValues(false))
        {
            robot.clickCardItem(context, mode).sleep(1000)
                    .assertCard_isFocused(context, mode, true)

                    .clickCardItem_menu(context, mode).sleep(500)
                    .assertCard_isMenuShown(context, mode)
                    .cancelCardItem_menu(context, mode).sleep(500);
        }

        AppSettings.saveShowCrossQuarterPref(context, savedState);
        robot.cancelDialog(context);
    }

    @Test
    public void test_showEquinoxDialog_cardItems_viewWith()
    {
        Activity context = activityRule.getActivity();
        boolean savedState = AppSettings.loadShowCrossQuarterPref(context);
        AppSettings.saveShowCrossQuarterPref(context, false);    // test begins with "show cross-quarter days" set to false

        EquinoxDialogRobot robot = new EquinoxDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .assertCard_noFocus(context);

        for (WidgetSettings.SolsticeEquinoxMode mode : WidgetSettings.SolsticeEquinoxMode.partialValues(false))
        {
            /*robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Moon(context, mode).sleep(1000);
            new MoonDialogTest.MoonDialogRobot()
                    .assertDialogShown(context)
                    .cancelDialog(context);*/

            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Sun(context, mode).sleep(1000);
            new LightMapDialogTest.LightMapDialogRobot()
                    .assertDialogShown(context)
                    .cancelDialog(context);

            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_WorldMap(context, mode).sleep(1000);
            new WorldMapDialogTest.WorldMapDialogRobot()
                    .assertDialogShown(context)
                    .cancelDialog(context);

            robot.clickCardItem(context, mode).sleep(1000)
                    .clickCardItem_menu(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith(context, mode).sleep(500)
                    .clickCardItem_menu_viewWith_Suntimes(context, mode).sleep(1000)
                    .assertSheetIsCollapsed(context);
        }

        AppSettings.saveShowCrossQuarterPref(context, savedState);
        robot.cancelDialog(context);
    }

    /**
     * EquinoxDialogRobot
     */
    public static class EquinoxDialogRobot extends DialogTest.DialogRobotBase implements DialogTest.DialogRobot
    {
        @Override
        public EquinoxDialogRobot sleep(long ms) {
            super.sleep(ms);
            return this;
        }

        @Override
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

        public EquinoxDialogRobot clickCardItem(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(mode.getLongDisplayString())).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(allOf( withId(R.id.menu_button),
                    withParent(hasSibling(withText(mode.getLongDisplayString())))
            )).perform(click());
            return this;
        }
        public EquinoxDialogRobot cancelCardItem_menu(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public EquinoxDialogRobot clickCardItem_menu_viewWith(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Suntimes(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Sun(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_Moon(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot clickCardItem_menu_viewWith_WorldMap(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public EquinoxDialogRobot cancelCardItem_menu_viewWith(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
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
        public EquinoxDialogRobot assertDialogShown(Context context) {
            onView(withId(R.id.dialog_header)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.year_info_layout)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public EquinoxDialogRobot assertOverflowMenuShown(Context context) {
            onView(isMenuDropDownListView()).inRoot(isPlatformPopup()).check(matches(hasMinimumChildCount(3)));
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public DialogTest.DialogRobot assertSheetIsCollapsed(Context context)
        {
            onView(withId(R.id.dialog_header)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.year_info_layout)).check(ViewAssertionHelper.assertHidden);
            return this;
        }

        public EquinoxDialogRobot assertOverflowMenu_Track(Context context)
        {
            onView(withText(R.string.trackingMode_soonest)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.trackingMode_closest)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.trackingMode_recent)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);

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
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);

            onView(withText(R.string.configLabel_ui_showCrossQuarter)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
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
            onView(withId(R.id.txt_help_content)).check(ViewAssertionHelper.assertShown);
            return this;
        }

        public EquinoxDialogRobot assertYearLabelIs_thisYear(Context context) {
            return assertYearLabelIs(Calendar.getInstance().get(Calendar.YEAR) + "");
        }
        public EquinoxDialogRobot assertYearLabelIs_nextYear(Context context) {
            return assertYearLabelIs((Calendar.getInstance().get(Calendar.YEAR) + 1) + "");
        }
        public EquinoxDialogRobot assertYearLabelIs_lastYear(Context context) {
            return assertYearLabelIs((Calendar.getInstance().get(Calendar.YEAR) - 1) + "");
        }
        public EquinoxDialogRobot assertYearLabelIs(String yearText)
        {
            onView(allOf(
                    hasSibling(withId(R.id.info_time_nextbtn1)),
                    hasSibling(withId(R.id.info_time_prevbtn1)),
                    withText(yearText))
            ).check(ViewAssertionHelper.assertShown);
            return this;
        }

        public EquinoxDialogRobot assertCard_noFocus(Context context)
        {
            for (WidgetSettings.SolsticeEquinoxMode mode : WidgetSettings.SolsticeEquinoxMode.partialValues(false)) {
                assertCard_isFocused(context, mode, false);
            }
            return this;
        }
        public EquinoxDialogRobot assertCard_isFocused(Context context, WidgetSettings.SolsticeEquinoxMode mode, boolean focused)
        {
            onView(allOf(withId(R.id.focusView),
                    hasSibling(withChild(withText(mode.getLongDisplayString())))
            )).check(focused ? ViewAssertionHelper.assertShown : ViewAssertionHelper.assertHidden);

            onView(allOf(withId(R.id.menu_button),
                    withParent(hasSibling(withText(mode.getLongDisplayString())))
            )).check(focused ? ViewAssertionHelper.assertShown : ViewAssertionHelper.assertHidden);
            return this;
        }

        public EquinoxDialogRobot assertCard_isMenuShown(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.configAction_setAlarm)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_viewDateWith)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public EquinoxDialogRobot assertCard_isMenuShown_viewWith(Context context, WidgetSettings.SolsticeEquinoxMode mode) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_sunDialog)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_moon)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_worldMap)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_showCalendar)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }

    }
}
