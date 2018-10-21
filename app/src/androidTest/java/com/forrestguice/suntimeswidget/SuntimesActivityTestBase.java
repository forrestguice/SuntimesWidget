/**
    Copyright (C) 2017 Forrest Guice
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
import android.content.pm.ActivityInfo;

import android.media.MediaScannerConnection;
//import android.os.Environment;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.jraska.falcon.Falcon;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.File;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public abstract class SuntimesActivityTestBase
{
    public static final String TESTLOC_0_LABEL = "Test Location 0";
    public static final String TESTLOC_0_LAT = "35";
    public static final String TESTLOC_0_LON = "-112";

    public static final String TESTLOC_1_LABEL = "Test Location 1";
    public static final String TESTLOC_1_LAT = "83.124";
    public static final String TESTLOC_1_LON = "23.1592";
    public static final String TESTLOC_1_ALT = "10";

    public static final String TESTLOC_2_LABEL = "Iron Springs";
    public static final String TESTLOC_2_LAT = "34.58742";
    public static final String TESTLOC_2_LON = "-112.57367";

    public static final String TESTTZID_0 = "US/Eastern";
    public static final String TESTTZID_1 = "US/Pacific";
    public static final String TESTTZID_2 = "US/Arizona";

    public static final int TESTDATE_0_YEAR = 2017;
    public static final int TESTDATE_0_MONTH = 1;    // feb 19, 2017
    public static final int TESTDATE_0_DAY = 19;


    public static final String SCREENSHOT_DIR = "test-screenshots";

    protected static ViewAssertion assertShown = matches(isDisplayed());
    protected static ViewAssertion assertShownCompletely = matches(isDisplayingAtLeast(90));
    protected static ViewAssertion assertHidden = matches(not(isDisplayed()));
    protected static ViewAssertion assertEnabled = matches(allOf(isEnabled(), isDisplayed()));
    protected static ViewAssertion assertDisabled = matches(allOf(not(isEnabled()), isDisplayed()));
    protected static ViewAssertion assertFocused = matches(allOf(isEnabled(), isDisplayed(), hasFocus()));
    protected static ViewAssertion assertClickable = matches(isClickable());
    protected static ViewAssertion assertSelected = matches(isSelected());
    protected static ViewAssertion assertChecked = matches(isChecked());
    protected static ViewAssertion assertNotChecked = matches(isNotChecked());

    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    /**
     * Rotate the device to landscape and back.
     */
    public void rotateDevice()
    {
        rotateDevice(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rotateDevice(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Rotate to given orientation.
     * @param orientation ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
     */
    public void rotateDevice( int orientation )
    {
        activityRule.getActivity().setRequestedOrientation(orientation);
    }

    public static Matcher<View> withResourceName(String resourceName) {
        return withResourceName(is(resourceName));
    }

    /**
     * copied from https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!searchin/android-test-kit-discuss/ActionBar/android-test-kit-discuss/mlMbTR30-0U/WljZkKBbdU0J
     * @param resourceNameMatcher a view matcher
     * @return a view matcher
     */
    public static Matcher<View> withResourceName(final Matcher<String> resourceNameMatcher)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("with resource name: ");
                resourceNameMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view)
            {
                int id = view.getId();
                return ((id != View.NO_ID) && (id != 0) && (view.getResources() != null)
                         && (resourceNameMatcher.matches(view.getResources().getResourceName(id))));
            }
        };
    }

    /**
     * @param name screenshot name
     */
    public void captureScreenshot(String name)
    {
        SuntimesActivityTestBase.captureScreenshot(activityRule.getActivity(), name);
    }
    public void captureScreenshot(String subdir, String name)
    {
        SuntimesActivityTestBase.captureScreenshot(activityRule.getActivity(), subdir, name);
    }

    /**
     * @param activity Activity context
     * @param name screenshot name
     */
    public static void captureScreenshot(Activity activity, String name)
    {
        SuntimesActivityTestBase.captureScreenshot(activity, "", name);
    }

    public static void captureScreenshot(Activity activity, String subdir, String name)
    {
        subdir = subdir.trim();
        if (!subdir.isEmpty() && !subdir.startsWith("/"))
        {
            subdir = "/" + subdir;
        }

        // saves to..
        //     SD card\Android\data\com.forrestguice.suntimeswidget\files\Pictures\test-screenshots\subdir
        File d = activity.getExternalFilesDir(DIRECTORY_PICTURES);
        if (d != null)
        {
            String dirPath = d.getAbsolutePath() + "/" + SCREENSHOT_DIR + subdir;

            File dir = new File(dirPath);
            boolean dirCreated = dir.mkdirs();

            String path = dirPath + "/" + name + ".png";
            File file = new File(path);
            if (file.exists())
            {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    Log.w("captureScreenshot", "Failed to delete file! " + path);
                }
            }

            try {
                Falcon.takeScreenshot(activity, file);
                MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);

            } catch (Exception e1) {
                Log.e("captureScreenshot", "Failed to write file! " + e1);
            }
        } else {
            Log.e("captureScreenshot", "Failed to write file! getExternalFilesDir() returns null..");
        }
    }

    /**
     * @param viewId view identifier
     */
    public static boolean viewIsDisplayed(int viewId)
    {
        final boolean[] isDisplayed = {true};
        onView(withId(viewId)).withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                isDisplayed[0] = false;
            }
        }).check(matches(isDisplayed()));
        return isDisplayed[0];
    }


    /**
     * @param viewInteraction a ViewInteraction wrapping some view
     * @return true view is checked, false otherwise
     */
    public static boolean viewIsChecked(ViewInteraction viewInteraction)
    {
        final boolean[] isChecked = {true};
        viewInteraction.withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                isChecked[0] = false;
            }
        }).check(matches(isChecked()));
        return isChecked[0];
    }

    /**
     * @param spinnerId spinner ID
     * @param text text
     * @return true if spinner displays given text
     */
    public static boolean spinnerDisplaysText(int spinnerId, String text)
    {
        final boolean[] displaysText = {true};
        onView(withId(spinnerId)).withFailureHandler(new FailureHandler()
        {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher)
            {
                displaysText[0] = false;
            }
        }).check(matches(withSpinnerText(text)));
        return displaysText[0];
    }
}
