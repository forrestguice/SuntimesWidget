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

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class AlarmSettingsTest0
{
    @Test
    public void test_allPrefKeysUnique()
    {
        String[] testKeys = new String[] {
                AlarmSettings.PREF_KEY_ALARM_CATEGORY,
                AlarmSettings.PREF_KEY_ALARM_BATTERYOPT,
                AlarmSettings.PREF_KEY_ALARM_NOTIFICATIONS,
                AlarmSettings.PREF_KEY_ALARM_VOLUMES,
                AlarmSettings.PREF_KEY_ALARM_HARDAREBUTTON_ACTION,
                AlarmSettings.PREF_KEY_ALARM_SILENCEAFTER,
                AlarmSettings.PREF_KEY_ALARM_TIMEOUT,
                AlarmSettings.PREF_KEY_ALARM_SNOOZE,
                AlarmSettings.PREF_KEY_ALARM_UPCOMING,
                AlarmSettings.PREF_KEY_ALARM_AUTOENABLE,
                AlarmSettings.PREF_KEY_ALARM_AUTOVIBRATE,
                AlarmSettings.PREF_KEY_ALARM_RINGTONE_URI_ALARM,
                AlarmSettings.PREF_KEY_ALARM_RINGTONE_NAME_ALARM,
                AlarmSettings.PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION,
                AlarmSettings.PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION,
                AlarmSettings.PREF_KEY_ALARM_ALLRINGTONES,
                AlarmSettings.PREF_KEY_ALARM_SHOWLAUNCHER,
                AlarmSettings.PREF_KEY_ALARM_POWEROFFALARMS,
                AlarmSettings.PREF_KEY_ALARM_UPCOMING_ALARMID,
                AlarmSettings.PREF_KEY_ALARM_FADEIN,
                AlarmSettings.PREF_KEY_ALARM_SORT,
                AlarmSettings.PREF_KEY_ALARM_BOOTCOMPLETED,
                AlarmSettings.PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED,
                AlarmSettings.PREF_KEY_ALARM_BOOTCOMPLETED_DURATION,
                AlarmSettings.PREF_KEY_ALARM_BOOTCOMPLETED_RESULT,
                AlarmSettings.PREF_KEY_ALARM_SYSTEM_TIMEZONE_ID,
                AlarmSettings.PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET,
                AlarmSettings.PREF_KEY_ALARM_DND_PERMISSION,
        };

        Set<String> set = new HashSet<>();
        for (String key : testKeys) {
            if (set.contains(key)) {
                fail("AlarmSettings key is not unique! " + key);
            } else set.add(key);
        }
    }

    @Test
    public void test_bootCompletedInfo()
    {
        AlarmSettings.BootCompletedInfo info = new AlarmSettings.BootCompletedInfo(1, 2, 3, true);
        assertEquals(1, info.getTimeMillis());
        assertEquals(2, info.getAtElapsedMillis());
        assertEquals(3, info.getDurationMillis());
        assertTrue(info.getResult());

        AlarmSettings.BootCompletedInfo info1 = new AlarmSettings.BootCompletedInfo(4, 5, 6, false);
        assertEquals(4, info1.getTimeMillis());
        assertEquals(5, info1.getAtElapsedMillis());
        assertEquals(6, info1.getDurationMillis());
        assertFalse(info1.getResult());
    }

}
