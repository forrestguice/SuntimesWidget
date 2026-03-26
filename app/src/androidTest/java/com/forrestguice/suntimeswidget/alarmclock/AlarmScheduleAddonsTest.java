/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.util.InstrumentationUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;

@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmScheduleAddonsTest extends AlarmScheduleTestBase
{
    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void init() {
        super.init();
        EventUri.setBuildConfigInfo(new EventUri.BuildConfigInfo() {
            @Override
            public String AUTHORITY_ROOT() {
                return BuildConfig.SUNTIMES_AUTHORITY_ROOT;
            }
        });
    }

    public String[] getAddonTestEvents()
    {
        ArrayList<String> events = new ArrayList<>();
        events.add("content://suntimes.naturalhour.provider/eventInfo/0_1_0");   // Natural Hour
        events.add("content://suntimes.intervalmidpoints.provider/eventInfo/sunset_astrorise_2_0");   // Interval Midpoints
        return events.toArray(new String[0]);
    }

    @Test
    @Category(UnlistedTest.class)
    public void test_updateAlarmTime_addonAlarm()
    {
        Calendar now = AlarmScheduleTest.getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (String event : getAddonTestEvents())
        {
            test_updateAlarmTime_repeatingAlarm(event, null, now);
            test_updateAlarmTime_repeatingAlarm(event, AlarmItemInterface.everyday(), now);
            test_updateAlarmTime_alarm(event, now);
        }
    }
}