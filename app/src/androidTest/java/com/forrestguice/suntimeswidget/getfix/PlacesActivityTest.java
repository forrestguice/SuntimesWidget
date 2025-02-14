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

package com.forrestguice.suntimeswidget.getfix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest;
import com.forrestguice.suntimeswidget.calculator.core.Location;
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
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.replaceText;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShownCompletely;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.isShowingError;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.withIndex;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class PlacesActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<PlacesActivity> activityRule = new ActivityTestRule<>(PlacesActivity.class, false, false);

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
    public void test_PlacesActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new PlacesActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    @Test
    public void test_PlacesActivit_menu()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        PlacesActivityRobot robot = new PlacesActivityRobot()
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_add(activity).sleep(1000)
                .assertAddDialogShown(activity)
                .clickAddDialog_cancelButton()
                .assertAddDialogNotShown(activity);
    }

    @Test
    public void test_PlacesActivity_search()
    {
        String locationName = "Search";
        Location location1 = new Location(locationName + "1", "33.5773682", "-112.55131");
        Location location2 = new Location(locationName + "2", "33.5773682", "-112.55131");
        Location location3 = new Location("Test2", "33.5773682", "-112.55131");

        addLocationToDatabaseIfMissing(getContext(), location1);
        addLocationToDatabaseIfMissing(getContext(), location2);
        addLocationToDatabaseIfMissing(getContext(), location3);

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();

        PlacesActivityRobot robot = new PlacesActivityRobot()
                .assertActivityShown(activity)
                .assertActionBarTitleShown(activity, true)
                .assertSearchButtonShown(activity, true)
                .assertSearchFieldShown(activity, false)
                .assertListHasAtLeast(activity, 3);

        robot.clickSearchButton()
                .assertSearchFieldShown(activity, true)
                .assertActionBarTitleShown(activity, false)
                .assertSearchButtonShown(activity, false);

        robot.inputSearchField("asdf;lkj").sleep(500)
                .assertListIsEmpty(activity);
        robot.inputSearchField("Search1").sleep(500)
                .assertListHasItems(activity, 1);
        robot.inputSearchField("").sleep(500)
                .assertListHasAtLeast(activity, 3);
        robot.inputSearchField("Search").sleep(500)
                .assertListHasItems(activity, 2);
        robot.inputSearchField("Search2").sleep(500)
                .assertListHasItems(activity, 1);

        robot.clickSearchClearButton().sleep(500)
                .assertSearchButtonShown(activity, false)
                .assertSearchFieldShown(activity, true)
                .assertListHasAtLeast(activity, 3);

        robot.clickBackButton()
                .assertSearchButtonShown(activity, true)
                .assertSearchFieldShown(activity, false);
    }

    protected static void clearPlaces(Context context)
    {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        db.clearPlaces();
        db.close();
    }

    protected static void removeLocationFromDatabaseIfExists(Context context, String label)
    {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        Cursor cursor = db.getAllPlaces(0, false);
        int p = GetFixDatabaseAdapter.findPlaceByName(label, cursor);
        if (p >= 0) {
            long id = cursor.getInt(0);
            db.removePlace(id);
        }
        cursor.close();
        db.close();
    }

    protected static Location addLocationToDatabaseIfMissing(Context context, Location location)
    {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        Cursor cursor = db.getAllPlaces(0, false);
        int p = GetFixDatabaseAdapter.findPlaceByName(location.getLabel(), cursor);
        if (p < 0) {
            db.addPlace(location, PlaceItem.TAG_DEFAULT);
        }
        cursor.close();
        db.close();
        return location;
    }

    protected static boolean placeExistsInDatabase(Context context, String label) {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        Cursor cursor = db.getAllPlaces(0, false);
        int p = GetFixDatabaseAdapter.findPlaceByName(label, cursor);
        boolean retValue = (p >= 0);
        cursor.close();
        db.close();
        return retValue;
    }

    protected static int numPlaces(Context context) {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        int n = db.getPlaceCount();
        db.close();
        return n;
    }

    @Test
    public void test_PlacesActivity_build()
    {
        clearPlaces(getContext());
        assertEquals(0, numPlaces(getContext()));

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        PlacesActivityRobot robot = new PlacesActivityRobot()
                .assertActivityShown(activity)
                .assertEmptyPlaces(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_build(activity).sleep(1000)
                .assertBuildDialogShown(activity)
                .clickBuildDialog_cancelButton()
                .assertBuildDialogNotShown(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_build(activity)
                .assertBuildDialogShown(activity)
                .clickBuildDialog_addButton().sleep(500);
        assertTrue(numPlaces(activity) > 0);
        robot.assertListHasAtLeast(activity, 1);
    }

    @Test
    public void test_PlacesActivity_clear()
    {
        String locationName = "Search";
        Location location1 = new Location(locationName + "1", "33.5773682", "-112.55131");
        Location location2 = new Location(locationName + "2", "33.5773682", "-112.55131");
        Location location3 = new Location("Test2", "33.5773682", "-112.55131");

        addLocationToDatabaseIfMissing(getContext(), location1);
        addLocationToDatabaseIfMissing(getContext(), location2);
        addLocationToDatabaseIfMissing(getContext(), location3);
        int n = numPlaces(getContext());

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        PlacesActivityRobot robot = new PlacesActivityRobot()
                .assertActivityShown(activity)
                .assertListHasAtLeast(activity, 3);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_clear(activity).sleep(1000)
                .assertClearDialogShown(activity)
                .clickClearDialog_cancelButton()
                .assertClearDialogNotShown(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_clear(activity)
                .assertClearDialogShown(activity)
                .clickClearDialog_clearButton().sleep(1000)
                .assertEmptyPlaces(activity)
                .assertUndoClearSnackbarShown(activity);
        assertEquals(0, numPlaces(activity));

        robot.clickSnackbar_undo()
                .assertUndoClearSnackbarNotShown(activity)
                .assertListHasAtLeast(activity, 3);
        assertEquals(n, numPlaces(activity));
    }

    @Test
    public void test_PlacesActivity_add()
    {
        clearPlaces(getContext());
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();

        PlacesActivityRobot robot = new PlacesActivityRobot()
                .showOverflowMenu(activity)
                .clickOverflowMenu_add(activity).sleep(1000)
                .assertAddDialogShown(activity);

        robot.inputAddDialog_location("Test Location", "25.55", "180", "0")
                .clickAddDialog_saveButton().sleep(500)
                .assertAddDialogNotShown(activity)
                .assertListShowsItem("Test Location");
        assertTrue(placeExistsInDatabase(activity, "Test Location"));
        // TODO: assert ActionMode displayed
    }

    @Test
    public void test_PlacesActivity_add_errors()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        PlacesActivityRobot robot = new PlacesActivityRobot()
                .showOverflowMenu(activity)
                .clickOverflowMenu_add(activity)
                .assertAddDialogShown(activity)
                .assertAddDialog_noErrorsShown(activity);

        robot.clickAddDialog_saveButton()
                .assertAddDialogShown(activity)
                .assertAddDialog_labelErrorShown(activity, true)        // error: must not be empty
                .assertAddDialog_latitudeErrorShown(activity, true)
                .assertAddDialog_longitudeErrorShown(activity, true);
    }

    @Test
    public void test_PlacesActivity_add_errors_longitude()
    {
        String label = "Test Location";
        String latitude = "25.55";
        String[] longitudes = new String[] { "", "-180.1", "180.1" };

        for (int i=0; i<longitudes.length; i++)
        {
            activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
            Activity activity = activityRule.getActivity();

            PlacesActivityRobot robot = new PlacesActivityRobot();
            robot.showOverflowMenu(activity)
                    .clickOverflowMenu_add(activity).sleep(500)
                    .assertAddDialogShown(activity)
                    .assertAddDialog_noErrorsShown(activity);

            robot.inputAddDialog_location(label, latitude, longitudes[i], "")
                    .clickAddDialog_saveButton().sleep(500)
                    .assertAddDialog_labelErrorShown(activity, false)
                    .assertAddDialog_latitudeErrorShown(activity, false)
                    .assertAddDialog_longitudeErrorShown(activity, true);

            robot.clickAddDialog_cancelButton()
                    .clickBackButton();
        }
    }

    @Test
    public void test_PlacesActivity_add_errors_latitude()
    {
        String label = "Test Location";
        String longitude = "0";
        String[] latitudes = new String[] { "", "-90.1", "90.1" };

        for (int i=0; i<latitudes.length; i++)
        {
            activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
            Activity activity = activityRule.getActivity();

            PlacesActivityRobot robot = new PlacesActivityRobot();
            robot.showOverflowMenu(activity)
                    .clickOverflowMenu_add(activity).sleep(500)
                    .assertAddDialogShown(activity)
                    .assertAddDialog_noErrorsShown(activity);

            robot.inputAddDialog_location(label, latitudes[i], longitude, "")
                    .clickAddDialog_saveButton().sleep(500)
                    .assertAddDialog_labelErrorShown(activity, false)
                    .assertAddDialog_longitudeErrorShown(activity, false)
                    .assertAddDialog_latitudeErrorShown(activity, true);

            robot.clickAddDialog_cancelButton()
                    .clickBackButton();
        }
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * PlacesActivityRobot
     */
    public static class PlacesActivityRobot extends ActivityRobot<PlacesActivityRobot>
    {
        public PlacesActivityRobot() {
            setRobot(this);
        }

        protected PlacesActivityRobot showActivity(Activity activity) {
            new SuntimesSettingsActivityTest.SettingsActivityRobot()
                    .showActivity(activity)
                    .clickHeader_placeSettings()
                    .clickPref_managePlaces();
            return this;
        }

        public PlacesActivityRobot clickBackButton() {
            onView(navigationButton()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickSearchButton() {
            onView(withContentDescription(R.string.configAction_searchPlace)).perform(click());
            return this;
        }
        public PlacesActivityRobot clickSearchClearButton()
        {
            onView(allOf( withClassName(endsWith("ImageView")),
                    hasSibling(withClassName(endsWith("AutoComplete"))),
                    isDescendantOfA(withClassName(endsWith("SearchView")))
            )).perform(click());
            return this;
        }
        public PlacesActivityRobot inputSearchField(String text) {
            onView(allOf( withClassName(endsWith("AutoComplete")),
                    isDescendantOfA(withClassName(endsWith("SearchView")))
            )).perform(replaceText(text));
            return this;
        }

        public PlacesActivityRobot clickOverflowMenu_add(Context context) {
            onView(withText(R.string.configAction_addPlace)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_build(Context context) {
            onView(withText(R.string.configLabel_places_build)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_import(Context context) {
            onView(withText(R.string.configAction_importPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_export(Context context) {
            onView(withText(R.string.configAction_exportPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public PlacesActivityRobot clickAcceptButton() {
            onView(allOf(withContentDescription(R.string.configAction_selectPlace), withClassName(endsWith("ActionMenuItemView")))).perform(click());
            return this;
        }

        public PlacesActivityRobot clickOnItem(int position) {
            onView(withIndex(withParent(withClassName(endsWith("RecyclerView"))), position)).perform(click());
            return this;
        }

        public PlacesActivityRobot clickBuildDialog_addButton() {
            onView(allOf(withText(R.string.configLabel_places_build), withClassName(endsWith("Button")))).perform(click());
            return this;
        }
        public PlacesActivityRobot clickBuildDialog_cancelButton() {
            onView(allOf(withText(R.string.dialog_cancel), withClassName(endsWith("Button")))).perform(click());
            return this;
        }

        public PlacesActivityRobot clickClearDialog_clearButton() {
            onView(allOf(withText(R.string.configAction_clearPlaces), withClassName(endsWith("Button")))).perform(click());
            return this;
        }
        public PlacesActivityRobot clickClearDialog_cancelButton() {
            onView(allOf(withText(R.string.dialog_cancel), withClassName(endsWith("Button")))).perform(click());
            return this;
        }
        public PlacesActivityRobot clickSnackbar_undo() {
            onView(allOf(withText(R.string.configAction_undo), withClassName(endsWith("Button")))).perform(click());
            return this;
        }

        public PlacesActivityRobot clickAddDialog_saveButton() {
            onView(withContentDescription(R.string.configAction_savePlace)).perform(click());
            return this;
        }
        public PlacesActivityRobot clickAddDialog_cancelButton() {
            onView(withContentDescription(R.string.dialog_cancel)).perform(click());
            return this;
        }

        public PlacesActivityRobot inputAddDialog_location(String label, String latitude, String longitude, String elevationMeters)
        {
            inputAddDialog_label(label);
            inputAddDialog_latitude(latitude);
            inputAddDialog_longitude(longitude);
            inputAddDialog_elevation(elevationMeters);
            return this;
        }
        public PlacesActivityRobot inputAddDialog_label(String label) {
            onView(withId(R.id.appwidget_location_name)).perform(replaceText(label));
            return this;
        }
        public PlacesActivityRobot inputAddDialog_latitude(String latitude) {
            onView(withId(R.id.appwidget_location_lat)).perform(replaceText(latitude));
            return this;
        }
        public PlacesActivityRobot inputAddDialog_longitude(String longitude) {
            onView(withId(R.id.appwidget_location_lon)).perform(replaceText(longitude));
            return this;
        }
        public PlacesActivityRobot inputAddDialog_elevation(String meters) {
            onView(withId(R.id.appwidget_location_alt)).perform(replaceText(meters));
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public PlacesActivityRobot assertActivityShown(Context context) {
            onView(allOf(withClassName(endsWith("TextView")), withText(R.string.configLabel_places),
                    isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            return this;
        }

        public PlacesActivityRobot assertActionBarTitleShown(Context context, boolean isShown) {
            onView(allOf(withClassName(endsWith("TextView")), withText(R.string.configLabel_places),
                    isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(isShown ? assertShown : doesNotExist());
            return this;
        }

        public PlacesActivityRobot assertSearchButtonShown(Context context, boolean isShown) {
            onView(withContentDescription(R.string.configAction_searchPlace)).check(isShown ? assertShownCompletely : assertHidden);
            return this;
        }
        public PlacesActivityRobot assertSearchFieldShown(Context context, boolean isShown) {
            onView(allOf( withClassName(endsWith("SearchView")),
                    isDescendantOfA(withClassName(endsWith("Toolbar")))
            )).check(isShown ? assertShownCompletely : doesNotExist());
            return this;
        }

        public PlacesActivityRobot assertProgressViewShown(Context context, boolean isShown) {
            // TODO
            return this;
        }

        public PlacesActivityRobot assertListIsEmpty(Context context) {
            assertListHasItems(context, 0);
            return this;
        }
        public PlacesActivityRobot assertListHasItems(Context context, int numItems) {
            if (numItems == 0) {
                onView(allOf(withId(R.id.placesList), hasMinimumChildCount(1))).check(doesNotExist());
            } else onView(allOf(withId(R.id.placesList), hasChildCount(numItems))).check(assertShown);
            return this;
        }
        public PlacesActivityRobot assertListHasAtLeast(Context context, int numItems) {
            if (numItems > 0) {
                onView(allOf(withId(R.id.placesList), hasMinimumChildCount(numItems))).check(assertShown);
            }
            return this;
        }
        public PlacesActivityRobot assertListShowsItem(String label) {
            onView(allOf(withText(label), isDescendantOfA(withId(R.id.placesList)))).check(assertShown);
            return this;
        }

        public PlacesActivityRobot assertEmptyPlaces(Context context) {
            onView(withId(android.R.id.empty)).check(assertShown);
            onView(withId(R.id.placesList)).check(assertHidden);
            return this;
        }

        public PlacesActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_addPlace)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configLabel_places_build)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_importPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public PlacesActivityRobot assertClearDialogShown(Context context) {
            onView(withText(R.string.locationclear_dialog_message)).check(assertShown);
            return this;
        }
        public PlacesActivityRobot assertClearDialogNotShown(Context context) {
            onView(withText(R.string.locationclear_dialog_message)).check(doesNotExist());
            return this;
        }
        public PlacesActivityRobot assertUndoClearSnackbarShown(Context context) {
            onView(allOf(withText(R.string.configAction_undo), withClassName(endsWith("Button")))).check(assertShown);
            return this;
        }
        public PlacesActivityRobot assertUndoClearSnackbarNotShown(Context context) {
            onView(allOf(withText(R.string.configAction_undo), withClassName(endsWith("Button")))).check(doesNotExist());
            return this;
        }

        public PlacesActivityRobot assertBuildDialogShown(Context context)
        {
            onView(withText(R.string.place_label_AF)).check(assertShown);
            onView(withText(R.string.place_label_AN)).check(assertShown);
            onView(withText(R.string.place_label_AS)).check(assertShown);
            onView(withText(R.string.place_label_EU)).check(assertShown);
            onView(withText(R.string.place_label_NA)).check(assertShown);
            onView(withText(R.string.place_label_OC)).check(assertShown);
            onView(withText(R.string.place_label_other)).check(assertShown);
            onView(withText(R.string.place_label_SA)).check(assertShown);
            return this;
        }
        public PlacesActivityRobot assertBuildDialogNotShown(Context context)
        {
            onView(withText(R.string.place_label_AF)).check(doesNotExist());
            onView(withText(R.string.place_label_AN)).check(doesNotExist());
            onView(withText(R.string.place_label_AS)).check(doesNotExist());
            onView(withText(R.string.place_label_EU)).check(doesNotExist());
            onView(withText(R.string.place_label_NA)).check(doesNotExist());
            onView(withText(R.string.place_label_OC)).check(doesNotExist());
            onView(withText(R.string.place_label_other)).check(doesNotExist());
            onView(withText(R.string.place_label_SA)).check(doesNotExist());
            return this;
        }

        public PlacesActivityRobot assertAddDialogShown(Context context)
        {
            onView(withId(R.id.appwidget_location_name)).check(assertShown);
            onView(withId(R.id.appwidget_location_lat)).check(assertShown);
            onView(withId(R.id.appwidget_location_lon)).check(assertShown);
            onView(withId(R.id.appwidget_location_alt)).check(assertShown);
            return this;
        }
        public PlacesActivityRobot assertAddDialogNotShown(Context context) {
            onView(withId(R.id.appwidget_location_name)).check(doesNotExist());
            onView(withId(R.id.appwidget_location_lat)).check(doesNotExist());
            onView(withId(R.id.appwidget_location_lon)).check(doesNotExist());
            onView(withId(R.id.appwidget_location_alt)).check(doesNotExist());
            return this;
        }

        public PlacesActivityRobot assertAddDialog_noErrorsShown(Context context) {
            assertAddDialog_labelErrorShown(context, false);
            assertAddDialog_latitudeErrorShown(context, false);
            assertAddDialog_longitudeErrorShown(context, false);
            return this;
        }

        public PlacesActivityRobot assertAddDialog_labelErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.location_dialog_error_name)))).check(isShown ? assertShown : doesNotExist());
            return this;
        }
        public PlacesActivityRobot assertAddDialog_latitudeErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.location_dialog_error_lat)))).check(isShown ? assertShown : doesNotExist());
            return this;
        }
        public PlacesActivityRobot assertAddDialog_longitudeErrorShown(Context context, boolean isShown) {
            onView(allOf(isShowingError(), hasErrorText(context.getString(R.string.location_dialog_error_lon)))).check(isShown ? assertShown : doesNotExist());
            return this;
        }

    }
}
