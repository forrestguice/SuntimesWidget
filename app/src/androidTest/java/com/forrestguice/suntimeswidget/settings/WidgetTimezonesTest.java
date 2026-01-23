/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import androidx.test.rule.ActivityTestRule;

import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;
import com.forrestguice.util.android.AndroidTaskHandler;
import com.forrestguice.util.concurrent.SimpleTaskListener;
import com.forrestguice.util.concurrent.TaskListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(SuntimesJUnitTestRunner.class)
public class WidgetTimezonesTest extends SuntimesActivityTestBase
{
    private Context context;

    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void init()
    {
        context = activityRule.getActivity();
    }

    @Test
    public void test_timeZonesLoadTask()
    {
        WidgetTimezones.TimeZonesLoadTask task = new WidgetTimezones.TimeZonesLoadTask(context, WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);
        TaskListener<WidgetTimezones.TimeZoneItemAdapter> taskListener = new SimpleTaskListener<WidgetTimezones.TimeZoneItemAdapter>()
        {
            @Override
            public void onFinished(WidgetTimezones.TimeZoneItemAdapter result)
            {
                super.onFinished(result);

                int i_ist = result.ordinal("Asia/Kolkata");  // "Indian Standard Time" (+5:30)
                assertTrue(i_ist >= 0);
                assertTrue(result.getItem(i_ist).getRawOffsetHr() == 5.5);

                int i_npt = result.ordinal("Asia/Katmandu");  // "Nepal Time" (+5:45)
                assertTrue(i_npt >= 0);
                assertTrue(result.getItem(i_npt).getRawOffsetHr() == 5.75);
                assertTrue(i_npt > i_ist);
            }
        };
        ExecutorUtils.runTask("TimeZoneLoadTest", AndroidTaskHandler.get(), task, taskListener);
    }

}
