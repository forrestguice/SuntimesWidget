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

package com.forrestguice.suntimeswidget.settings;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.colors.BrightAlarmColorValuesCollection;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValuesCollection;
import com.forrestguice.util.prefs.PrefTypeInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AppSettingsTest0
{
    @Test
    public void test_themeNames()
    {
        for (String theme0 : AppSettings.THEMES) {
            for (String theme1 : AppSettings.THEMES) {
                //noinspection StringEquality
                if (theme0 == theme1) {
                    continue;
                }
                assertFalse(theme1 + " starts with " + theme0 + " (must be unique)", theme1.startsWith(theme0));
            }
        }
    }

    @Test
    public void test_getPrefTypes()
    {
        test_prefTypeInfo("AppColors", AppColorValuesCollection.getPrefTypeInfo(), AppColorValuesCollection.getPrefTypes());
        test_prefTypeInfo("MapColors", WorldMapColorValuesCollection.getPrefTypeInfo(), WorldMapColorValuesCollection.getPrefTypes());
        test_prefTypeInfo("AlarmColors", BrightAlarmColorValuesCollection.getPrefTypeInfo(), BrightAlarmColorValuesCollection.getPrefTypes());
        test_prefTypeInfo("AppSettings", AppSettings.getPrefTypeInfo(), AppSettings.getPrefTypes());
        test_prefTypeInfo("AlarmSettings", AlarmSettings.getPrefTypeInfo(), AlarmSettings.getPrefTypes());
        test_prefTypeInfo("CalendarSettings", CalendarSettings.getPrefTypeInfo(), CalendarSettings.getPrefTypes());
        test_prefTypeInfo("WidgetActions", WidgetActions.getPrefTypeInfo(), WidgetActions.getPrefTypes());
        test_prefTypeInfo("WidgetSettings", WidgetSettings.getPrefTypeInfo(), WidgetSettings.getPrefTypes());
        test_prefTypeInfo("WidgetSettingsMetadata", WidgetSettingsMetadata.getPrefTypeInfo(), WidgetSettingsMetadata.getPrefTypes());
        test_prefTypeInfo("WorldMapWidgetSettings", WorldMapWidgetSettings.getPrefTypeInfo(), WorldMapWidgetSettings. getPrefTypes());
    }

    public void test_prefTypeInfo(String tag, PrefTypeInfo info, Map<String,Class> keys)
    {
        ArrayList<String> allKeys1 = new ArrayList<>();    // "all keys" should contain every subset
        allKeys1.addAll(Arrays.asList(info.intKeys()));
        allKeys1.addAll(Arrays.asList(info.longKeys()));
        allKeys1.addAll(Arrays.asList(info.floatKeys()));
        allKeys1.addAll(Arrays.asList(info.boolKeys()));
        test_containsAll(tag + " (all keys)", new ArrayList<>(Arrays.asList(info.allKeys())), allKeys1);
        //test_containsAll(tag + " (types0)", new ArrayList<>(keys.keySet()), new ArrayList<>(Arrays.asList(info.allKeys())));  // types should contain "all keys"
        //test_containsAll(tag + " (types1)", new ArrayList<>(Arrays.asList(info.allKeys())), new ArrayList<>(keys.keySet()));  // "all keys" should contain every key in type

        for (String key : info.intKeys()) {
            assertEquals(tag + " (" + key + ")", Integer.class, keys.get(key));
        }
        for (String key : info.longKeys()) {
            assertEquals(tag + " (" + key + ")", Long.class, keys.get(key));
        }
        for (String key : info.floatKeys()) {
            assertEquals(tag + " (" + key + ")", Float.class, keys.get(key));
        }
        for (String key : info.boolKeys()) {
            assertEquals(tag + " (" + key + ")", Boolean.class, keys.get(key));
        }
    }

    /**
     * asserts that list0 contains all items in list1
     */
    public void test_containsAll(String tag, List<String> list0, List<String> list1) {
        for (String key : list1) {
            assertTrue(tag + " :: list should contain " + key, list0.contains(key));
        }
    }
}
