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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class WidgetSettingsTest extends SuntimesActivityTestBase
{
    @Test
    public void test_timeFormatModePref()
    {
        Context context = activityRule.getActivity();
        int appWidgetId = Integer.MAX_VALUE;

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_SYSTEM);
        WidgetSettings.TimeFormatMode mode3 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be system but was " + mode3, mode3 == WidgetSettings.TimeFormatMode.MODE_SYSTEM);

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_24HR);
        WidgetSettings.TimeFormatMode mode2 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be 24 hr but was " + mode2, mode2 == WidgetSettings.TimeFormatMode.MODE_24HR);

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_12HR);
        WidgetSettings.TimeFormatMode mode1 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be 12 hr but was " + mode1, mode1 == WidgetSettings.TimeFormatMode.MODE_12HR);

        WidgetSettings.deleteTimeFormatModePref(context, appWidgetId);
        WidgetSettings.TimeFormatMode mode0 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be default (system) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_APPEARANCE_TIMEFORMATMODE &&
                mode0 == WidgetSettings.TimeFormatMode.MODE_SYSTEM);
    }
}
