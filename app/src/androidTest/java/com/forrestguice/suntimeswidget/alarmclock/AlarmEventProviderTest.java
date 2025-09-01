/**
    Copyright (C) 2024 Forrest Guice
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.suntimeswidget.events.EventType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AlarmEventProviderTest
{
    public Context context;

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_EventType_resolveEventType()
    {
        String[] events = new String[] { "SUN_-6.0|5r", "SHADOW_1:1|5r", "123456789", "SUNSET" };
        EventType[] expected = new EventType[] {
                EventType.SUN_ELEVATION, EventType.SHADOWLENGTH, EventType.DATE, EventType.SOLAREVENT };

        for (int i=0; i<events.length; i++) {
            assertEquals(expected[i], EventType.resolveEventType(AndroidSuntimesDataSettings.wrap(context), events[i]));
        }
    }

}
