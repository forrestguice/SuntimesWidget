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

import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.suntimeswidget.events.ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.SunElevationEvent;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AlarmEventProviderTest0
{
    @Test
    public void test_SunElevationEvent()
    {
        boolean[] rising = new boolean[] { true, false, true, false };
        double[] angle = new double[] { -6.0, -6.0, 12.0, 12.0 };
        int[] offset = new int[] { 300000, 300000, 0, 0 };    // offset millis

        for (int i=0; i<angle.length; i++)
        {
            SunElevationEvent event = new SunElevationEvent(angle[i], offset[i], rising[i]);
            assertEquals(angle[i], event.getAngle());
            assertEquals(offset[i], event.getOffset());
            assertEquals(rising[i], event.isRising());
        }
    }

    @Test
    public void test_SunElevationEvent_isElevationEvent()
    {
        String[] events = new String[] { "SUN_-6.0|5r", "SUN_-6.0|5s", "SUN_12.0r", "SUN_12.0s", "OTHER_10s", "SHADOW_1:1|5r", null, "" };
        boolean[] expected = new boolean[] {true, true, true, true, false, false, false, false};
        for (int i=0; i<events.length; i++) {
            assertEquals(expected[i], SunElevationEvent.isElevationEvent(events[i]));
        }
    }

    @Test
    public void test_SunElevationEvent_getEventName()
    {
        boolean[] rising = new boolean[] { true, false, true, false };
        double[] angle = new double[] { -6.0, -6.0, 12.0, 12.0 };
        int[] offset = new int[] { 300000, 300000, 0, 0 };    // offset millis
        String[] expected = new String[] { "SUN_-6.0|5r", "SUN_-6.0|5s", "SUN_12.0r", "SUN_12.0s" };

        for (int i=0; i<angle.length; i++) {
            assertEquals(expected[i], SunElevationEvent.getEventName(angle[i], offset[i], rising[i]));
        }
    }

    @Test
    public void test_SunElevationEvent_valueOf()
    {
        String[] events = new String[] { "SUN_-6.0|5r", "SUN_-6.0r", "SUN_-6.0s", "SUN_-6.0|5s" };
        boolean[] rising = new boolean[] { true, true, false, false };
        double[] angle = new double[] { -6.0, -6.0, -6.0, -6.0 };
        int[] offset = new int[] { 300000, 0, 0, 300000 };    // offset millis

        for (int i=0; i<angle.length; i++)
        {
            SunElevationEvent event = SunElevationEvent.valueOf(events[i]);
            assertNotNull(event);
            assertEquals(angle[i], event.getAngle());
            assertEquals(offset[i], event.getOffset());
            assertEquals(rising[i], event.isRising());
        }
        assertEquals(-6.0, SunElevationEvent.valueOf("SUN_-6").getAngle());      // no suffix, no offset

        assertNull( SunElevationEvent.valueOf("") );             // empty
        assertNull( SunElevationEvent.valueOf(null) );           // null
        assertNull( SunElevationEvent.valueOf("OTHER_") );       // non SunElevationEvents
        assertNull( SunElevationEvent.valueOf("SUN_X|5r") );     // bad angle (not a number)
        assertNull( SunElevationEvent.valueOf("SUN_-6|Xr") );    // bad offset (not a number)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_ShadowLengthEvent()
    {
        boolean[] rising = new boolean[] { true, false, true, false };
        double[] length = new double[] { 6.0, 6.0, 12.0, 12.0 };
        double[] height = new double[] { 1.0, 6.0, 1.0, 6.0 };
        int[] offset = new int[] { 300000, 300000, 0, 0 };    // offset millis

        for (int i=0; i<rising.length; i++)
        {
            ShadowLengthEvent event = new ShadowLengthEvent(height[i], length[i], offset[i], rising[i]);
            assertEquals(height[i], event.getObjHeight());
            assertEquals(length[i], event.getLength());
            assertEquals(offset[i], event.getOffset());
            assertEquals(rising[i], event.isRising());
        }
    }

    @Test
    public void test_ShadowLengthEvent_isShadowLengthEvent()
    {
        String[] events = new String[] { "SHADOW_1:1|5r", "SHADOW_1|5s", "SHADOW_1:1s", "SHADOW_1:1r", "OTHER_10s", "SUN_-6.0|5r", null, "" };
        boolean[] expected = new boolean[] {true, true, true, true, false, false, false, false};
        for (int i=0; i<events.length; i++) {
            assertEquals(expected[i], ShadowLengthEvent.isShadowLengthEvent(events[i]));
        }
    }

    @Test
    public void test_ShadowLengthEvent_getEventName()
    {
        boolean[] rising = new boolean[] { true, false, true, false };
        double[] length = new double[] { 6.0, 6.0, 12.0, 12.0 };
        double[] height = new double[] { 1.0, 6.0, 1.0, 6.0 };
        int[] offset = new int[] { 300000, 300000, 0, 0 };    // offset millis
        String[] expected = new String[] { "SHADOW_1.0:6.0|5r", "SHADOW_6.0:6.0|5s", "SHADOW_1.0:12.0r", "SHADOW_6.0:12.0s" };

        for (int i=0; i<rising.length; i++) {
            assertEquals(expected[i], ShadowLengthEvent.getEventName(height[i], length[i], offset[i], rising[i]));
        }
    }

    @Test
    public void test_ShadowLengthEvent_valueOf()
    {
        String[] events = new String[] { "SHADOW_1.0:6.0|5r", "SHADOW_6.0:6.0|5s", "SHADOW_1.0:12.0r", "SHADOW_6.0:12.0s" };
        boolean[] rising = new boolean[] { true, false, true, false };
        double[] length = new double[] { 6.0, 6.0, 12.0, 12.0 };
        double[] height = new double[] { 1.0, 6.0, 1.0, 6.0 };
        int[] offset = new int[] { 300000, 300000, 0, 0 };    // offset millis

        for (int i=0; i<rising.length; i++)
        {
            ShadowLengthEvent event = ShadowLengthEvent.valueOf(events[i]);
            assertNotNull(event);
            assertEquals(length[i], event.getLength());
            assertEquals(height[i], event.getObjHeight());
            assertEquals(offset[i], event.getOffset());
            assertEquals(rising[i], event.isRising());
        }
        assertEquals(6.0, ShadowLengthEvent.valueOf("SHADOW_6").getLength());      // no suffix, no offset

        assertNull( SunElevationEvent.valueOf("") );             // empty
        assertNull( SunElevationEvent.valueOf(null) );           // null
        assertNull( SunElevationEvent.valueOf("OTHER_") );       // non SunElevationEvents
        assertNull( SunElevationEvent.valueOf("SHADOW_X|5r") );     // bad length (not a number)
        assertNull( SunElevationEvent.valueOf("SHADOW_1.0:X|5r") );     // bad length (not a number)
        assertNull( SunElevationEvent.valueOf("SHADOW_X:1.0|5r") );     // bad height (not a number)
        assertNull( SunElevationEvent.valueOf("SHADOW_-6|Xr") );    // bad offset (not a number)
    }

    @Test
    public void test_EventType_isNumeric()
    {
        assertTrue(EventType.isNumeric(""));    // empty string
        assertTrue(EventType.isNumeric("0"));
        assertTrue(EventType.isNumeric("1"));
        assertTrue(EventType.isNumeric("100"));
        assertTrue(EventType.isNumeric("1234567890"));
        assertTrue(EventType.isNumeric("001"));     // leading 0s

        assertFalse(EventType.isNumeric("1.1"));    // accepts ints only
        assertFalse(EventType.isNumeric("x1"));     // contains x
        assertFalse(EventType.isNumeric("1x"));     // contains x
        assertFalse(EventType.isNumeric("nan"));    // no numerals
    }

}
