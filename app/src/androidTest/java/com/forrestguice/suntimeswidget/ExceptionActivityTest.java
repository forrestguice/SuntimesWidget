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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShownCompletely;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class ExceptionActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<ExceptionActivity> activityRule = new ActivityTestRule<>(ExceptionActivity.class, false, false);

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
    public void test_ExceptionActivity()
    {
        String report = "report text";
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(ExceptionActivity.EXTRA_REPORT, report);

        activityRule.launchActivity(intent);
        Activity activity = activityRule.getActivity();
        ExceptionActivityRobot robot = new ExceptionActivityRobot()
                .assertActivityShown(activity)
                .assertReportIsShown(activity, report);

        robot.clickOnCopy()
                .assertReportIsCopied(activity, report);
    }

    @Test
    public void test_ExceptionActivity_missingReport()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        assertTrue("EXTRA_REPORT was not supplied; the activity should finish early...", activity.isFinishing());
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * ExceptionActivityRobot
     */
    public static class ExceptionActivityRobot extends ActivityRobot<ExceptionActivityRobot>
    {
        public ExceptionActivityRobot() {
            setRobot(this);
        }

        public ExceptionActivityRobot clickOnCopy() {
            onView(withId(R.id.button_copy)).perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public ExceptionActivityRobot assertActivityShown(Context context)
        {
            onView(withId(R.id.text_title)).check(assertShown);
            onView(withId(R.id.text_message)).check(assertShown);
            onView(withId(R.id.text_message1)).check(assertShown);
            onView(withId(R.id.text_exception)).check(assertShown);
            onView(withId(R.id.button_copy)).check(assertShown).check(assertClickable).check(assertEnabled);
            return this;
        }

        public ExceptionActivityRobot assertReportIsShown(Context context, String report) {
            onView(withText(containsString(report))).check(assertShownCompletely);
            return this;
        }

        public ExceptionActivityRobot assertReportIsCopied(Context context, String report) 
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            assertNotNull(clipboard);
            assertTrue(clipboard.hasPrimaryClip());

            ClipData clip = clipboard.getPrimaryClip();
            assertNotNull(clip);
            assertTrue(clip.getItemCount() > 0);

            ClipData.Item item = clip.getItemAt(0);
            assertNotNull(item);
            assertEquals("clipboard should contain matching text", report, item.getText());
            return this;
        }
    }
}
