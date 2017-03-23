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
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.jraska.falcon.Falcon;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesActivityTestBase
{
    public static final String SCREENSHOT_DIR = "test-screenshots";

    protected static ViewAssertion assertShown = matches(isDisplayed());
    protected static ViewAssertion assertHidden = matches(not(isDisplayed()));
    protected static ViewAssertion assertEnabled = matches(allOf(isEnabled(), isDisplayed()));
    protected static ViewAssertion assertDisabled = matches(allOf(not(isEnabled()), isDisplayed()));
    protected static ViewAssertion assertFocused = matches(allOf(isEnabled(), isDisplayed(), hasFocus()));

    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void initTestBase()
    {
    }

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
     * @param resourceNameMatcher
     * @return
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
     * @param name
     */
    public void captureScreenshot(String name)
    {
        SuntimesActivityTestBase.captureScreenshot(activityRule.getActivity(), name);
    }

    /**
     * @param activity
     * @param name
     */
    public static void captureScreenshot(Activity activity, String name)
    {
        View view = activity.getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SCREENSHOT_DIR;
        File dir = new File(dirPath);
        dir.mkdirs();

        String path = dirPath + "/" + name + ".png";
        Log.d("DEBUG", "screenshot path: " + path);

        File file = new File(path);
        if (file.exists())
        {
            file.delete();
        }

        try {
            Falcon.takeScreenshot(activity, file);
            MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);

        } catch (Exception e1) {
            Log.e("captureScreenshot", "Failed to write file! " + e1);
        }
    }

}
