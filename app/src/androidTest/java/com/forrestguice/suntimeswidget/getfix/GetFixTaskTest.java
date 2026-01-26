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

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import android.location.Location;
import androidx.test.rule.ActivityTestRule;

import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SuntimesJUnitTestRunner.class)
public class GetFixTaskTest
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void test_getFix()
    {
        AppCompatActivity activity = (AppCompatActivity) activityRule.getActivity();
        GetFixHelper.GetFixHelperListener listener = new GetFixHelper.GetFixHelperListener() {
            @Override
            public void onRequestPermissions(String[] permissions, int requestID) {}
        };
        LocationHelper helper = new GetFixHelper(activity, uiObj, listener) {
            public int getMinElapsedTime() {
                return 0;
            }
        };

        assertTrue("This test requires granting location permissions before running.", helper.hasLocationPermission(activity));

        helper.addGetFixTaskListener(taskListener);
        helper.addUI(uiObj);
        assertEquals(uiObj, helper.getUI());

        Location location = helper.getLastKnownLocation(activity);
        Log.d("TEST", "getFix; lastKnownLocation: " + location + " (from " + GetFixHelper.t_locationProvider + " provider)");

        assertFalse("hasFix should return false", helper.hasFix());
        assertFalse("gettingFix should return false", helper.gettingFix());

        long bench_start = System.nanoTime();
        assertTrue(helper.getFix());
        assertTrue("gettingFix should return true", helper.gettingFix());
        assertTrue(waitForTask);
        //noinspection ConstantConditions,StatementWithEmptyBody,WhileLoopSpinsOnField
        while (waitForTask) {
            /* busy wait for completion */
        }
        long bench_end = System.nanoTime();
        double bench_time = (bench_end - bench_start) / 1000000.0;
        Log.d("TEST", "getFix: " + taskResult + " (from " + GetFixHelper.t_locationProvider + " provider in " + bench_time + " ms)");
        Log.d("TEST", "getFix: bench: " + bench_time + " ms");

        assertFalse(waitForTask);
        assertFalse("gettingFix should return false", helper.gettingFix());
        assertTrue("hasFix should return true", helper.hasFix());
    }

    @Test
    public void test_getLastKnownLocation()
    {
        GetFixHelper.GetFixHelperListener listener = new GetFixHelper.GetFixHelperListener() {
            @Override
            public void onRequestPermissions(String[] permissions, int requestID) {}
        };
        LocationHelper helper = new GetFixHelper(activityRule.getActivity(), uiObj, listener);

        long bench_start = System.nanoTime();
        Location location = helper.getLastKnownLocation(activityRule.getActivity());
        long bench_end = System.nanoTime();

        double bench_time = (bench_end - bench_start) / 1000000.0;
        Log.d("TEST", "lastKnownLocation: " + location + " (from " + GetFixHelper.t_locationProvider + " provider in " + bench_time + " ms)");
    }

    private boolean waitForTask = false;
    @Nullable
    private Location taskResult = null;
    private final GetFixTaskListener taskListener = new GetFixTaskListener()
    {
        @Override
        public void onStarted() {
            super.onStarted();
            waitForTask = true;
            Log.d("TEST", "onStarted");
        }

        @Override
        public void onFinished(Location result) {
            super.onFinished(result);
            taskResult = result;
            waitForTask = false;
            Log.d("TEST", "onFinished");
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            taskResult = null;
            waitForTask = false;
            Log.d("TEST", "onCancelled");
        }
    };

    private final GetFixUI uiObj = new GetFixUI()
    {
        @Override
        public void enableUI(boolean value) {
            Log.d("TEST", "UI: enableUI: " + value);
        }

        @Override
        public void updateUI(Location... locations) {
            Log.d("TEST", "UI: updateUI: " + locations[0]);
        }

        @Override
        public void showProgress(boolean showProgress) {
            Log.d("TEST", "UI: showProgress: " + showProgress);
        }

        @Override
        public void onStart() {
            Log.d("TEST", "UI: onStart");
        }

        @Override
        public void onResult(LocationResult result) {
            Log.d("TEST", "UI: onResult: " + result);
        }
    };
}
