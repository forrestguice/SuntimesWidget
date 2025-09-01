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

package com.forrestguice.suntimeswidget.calculator.core;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class LocationTest0
{
    @Test public void test_location()
    {
        Location loc0 = new Location("0", "0");
        assertEquals(0d, loc0.getLatitudeAsDouble());
        assertEquals(0d, loc0.getLongitudeAsDouble());
        assertEquals(0d, loc0.getAltitudeAsDouble());
        assertEquals(loc0.getLabel(), "");
        assertTrue(loc0.useAltitude());

        Location loc1 = new Location("90", "180");
        assertEquals(90d, loc1.getLatitudeAsDouble());
        assertEquals(-180d, loc1.getLongitudeAsDouble());

        Location loc2 = new Location("-90", "-180");
        assertEquals(-90d, loc2.getLatitudeAsDouble());
        assertEquals(-180d, loc2.getLongitudeAsDouble());

        Location loc7 = new Location("test", "1", "2");
        assertEquals(1d, loc7.getLatitudeAsDouble());
        assertEquals(2d, loc7.getLongitudeAsDouble());
        assertEquals(0d, loc7.getAltitudeAsDouble());
        assertEquals("test", loc7.getLabel());

        Location loc8 = new Location("test", "1", "2", "3");
        assertEquals(1d, loc8.getLatitudeAsDouble());
        assertEquals(2d, loc8.getLongitudeAsDouble());
        assertEquals(3d, loc8.getAltitudeAsDouble());
        assertEquals("test", loc8.getLabel());
        assertTrue(loc8.useAltitude());

        loc8.setUseAltitude(false);
        assertFalse(loc8.useAltitude());

        Location loc9 = new Location("test", "1", "2", "3", false);
        assertEquals(1d, loc9.getLatitudeAsDouble());
        assertEquals(2d, loc9.getLongitudeAsDouble());
        assertEquals(0.9144f, (float)((double)loc9.getAltitudeAsDouble()));
        assertEquals("test", loc9.getLabel());
        assertTrue(loc9.useAltitude());
    }

    @Test public void test_location_range()
    {
        Location loc3 = new Location("91", "181");
        assertEquals(89d, loc3.getLatitudeAsDouble());
        assertEquals(-179d, loc3.getLongitudeAsDouble());

        Location loc4 = new Location("181", "359");
        assertEquals(89d, loc4.getLatitudeAsDouble());
        assertEquals(-1d, loc4.getLongitudeAsDouble());

        Location loc5 = new Location("-91", "-181");
        assertEquals(-89d, loc5.getLatitudeAsDouble());
        assertEquals(179d, loc5.getLongitudeAsDouble());

        Location loc6 = new Location("-179", "-359");
        assertEquals(-1d, loc6.getLatitudeAsDouble());
        assertEquals(1d, loc6.getLongitudeAsDouble());
    }


    @Test public void test_location_other()
    {
        Location location0 = new Location("test", "1", "2", "3");
        location0.setUseAltitude(false);

        Location location1 = new Location(location0);
        test_equals(location0, location1);

        location0.setUseAltitude(true);
        Location location2 = new Location(location0);
        assertTrue(location2.useAltitude());
        test_equals(location0, location2);
    }

    public static void test_equals(Location location0, Location location)
    {
        assertEquals(location0.getLatitude(), location.getLatitude());
        assertEquals(location0.getLatitudeAsDouble(), location.getLatitudeAsDouble());
        assertEquals(location0.getAltitudeAsInteger(), location.getAltitudeAsInteger());
        assertEquals(location0.getLongitude(), location.getLongitude());
        assertEquals(location0.getLongitudeAsDouble(), location.getLongitudeAsDouble());
        assertEquals(location0.getAltitude(), location.getAltitude());
        assertEquals(location0.getAltitudeAsDouble(), location.getAltitudeAsDouble());
        assertEquals(location0.getAltitudeAsInteger(), location.getAltitudeAsInteger());
        assertEquals(location0.useAltitude(), location.useAltitude());
        assertEquals(location0.toString(), location.toString());
        assertEquals(location0.getLabel(), location.getLabel());
        assertEquals(location0, location);  // test isEquals
    }

    public static final float FLOAT_TOLERANCE = 0.01f;
    protected boolean equals(float float1, float float2) {
        return (Math.abs(float1 - float2) < FLOAT_TOLERANCE);
    }

}
