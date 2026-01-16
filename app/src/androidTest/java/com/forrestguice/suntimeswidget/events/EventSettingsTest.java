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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.graphics.Color;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(SuntimesJUnitTestRunner.class)
public class EventSettingsTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void test_saveLoadDeleteEvent()
    {
        AndroidEventSettings context = AndroidEventSettings.wrap(activityRule.getActivity());

        EventType type0 = EventType.SUN_ELEVATION;
        String id0 = "TEST0";
        String label0 = "label0";
        String uri0 = "uri0";
        Integer color0 = Color.GREEN;

        EventSettings.deleteEvent(context, id0);
        assertFalse(EventSettings.hasEvent(context, id0));

        Set<String> list0 = EventSettings.loadEventList(context, type0);
        assertFalse(list0.contains(id0));

        EventAlias alias0 = new EventAlias(EventType.SUN_ELEVATION, id0, label0, color0, uri0, false);
        verify_eventAlias(type0, id0, label0, color0, uri0, alias0);

        EventSettings.saveEvent(context, alias0);
        assertTrue(EventSettings.hasEvent(context, id0));

        Set<String> list1 = EventSettings.loadEventList(context, type0);
        assertTrue(list1.contains(id0));

        EventAlias alias1 = EventSettings.loadEvent(context, id0);
        verify_eventAlias(type0, id0, label0, color0, uri0, alias1);

        EventSettings.deleteEvent(context, id0);
        assertFalse(EventSettings.hasEvent(context, id0));

        Set<String> list2 = EventSettings.loadEventList(context, type0);
        assertFalse(list2.contains(id0));
    }

    protected void verify_eventAlias(EventType type, String id, String label, Integer color, String uri, EventAlias alias)
    {
        assertEquals(type, alias.getType());
        assertEquals(id, alias.getID());
        assertEquals(label, alias.getLabel());
        assertEquals(color, alias.getColor());
        assertEquals(uri, alias.getUri());
    }

    public static Set<String> populateEventListWithTestItems(Context context)
    {
        EventSettings.saveEvent(AndroidEventSettings.wrap(context), new EventAlias(EventType.SUN_ELEVATION, "TEST0", "label0", Color.GREEN, "uri0", false));
        EventSettings.saveEvent(AndroidEventSettings.wrap(context), new EventAlias(EventType.SHADOWLENGTH, "TEST1", "label1", Color.RED, "uri1", false));
        EventSettings.saveEvent(AndroidEventSettings.wrap(context), new EventAlias(EventType.DAYPERCENT, "TEST2", "label2", Color.BLUE, "uri2", false));
        EventSettings.saveEvent(AndroidEventSettings.wrap(context), new EventAlias(EventType.MOONILLUM, "TEST3", "label3", Color.CYAN, "uri3", false));
        EventSettings.saveEvent(AndroidEventSettings.wrap(context), new EventAlias(EventType.MOON_ELEVATION, "TEST4", "label4", Color.MAGENTA, "uri4", false));
        return EventSettings.loadEventList(AndroidEventSettings.wrap(context));
    }

}
