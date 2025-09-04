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

import android.os.Parcel;
import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapterTest;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

@SuppressWarnings("ConstantConditions")
public class LocationTest
{
    @Test
    public void test_location_parcelable()
    {
        for (Location location : GetFixDatabaseAdapterTest.locations)
        {
            if (location != null) {
                test_location_parcelable(location);
            }
        }
    }

    public void test_location_parcelable(Location item0)
    {
        Parcel parcel0 = Parcel.obtain();
        item0.writeToParcel(parcel0, 0);
        parcel0.setDataPosition(0);

        Location item = (Location) Location.CREATOR.createFromParcel(parcel0);
        test_equals(item0, item);
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
        assertEquals(location0, location);
    }

    public static final float FLOAT_TOLERANCE = 0.01f;
    protected boolean equals(float float1, float float2) {
        return (Math.abs(float1 - float2) < FLOAT_TOLERANCE);
    }

}
