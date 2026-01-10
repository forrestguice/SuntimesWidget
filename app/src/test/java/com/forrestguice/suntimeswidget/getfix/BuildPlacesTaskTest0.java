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

package com.forrestguice.suntimeswidget.getfix;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BuildPlacesTaskTest0
{
    @Test
    public void test_csvItemToLocation_valid()
    {
        String[] valid = new String[] {
                "Sydney, -33.8981, 151.2328",
                "Sydney, -33.8981, 151.2328, 40",
                "Sydney, -33.8981, 151.2328, 40, [default]",
                "\"Sydney\", -33.8981, 151.2328, 40",
                "Pago Pago, -14.27176, -170.70223, 0",
                "\"San José\", 9.9356, -84.1483, 1017",
                "\"Willemstad, Curaçao\", 12.11018, -68.95539, 0",
                "\"St. George\'s, Bermuda\", 32.37944, -64.67777, 0",
                "\"St. George\'s, Bermuda\", 32.37944, -64.67777, 0, [default]"
        };
        for (String item : valid) {
            test_csvToPlaceItem(item, true);
        }
    }

    @Test
    public void test_csvItemToLocation_invalid()
    {
        String[] invalid = new String[] {
                null, "", "1017", "-84.1483, 1017",     // null, empty, insufficient args
                "Willemstad, Curaçao, 12.11018, -68.95539, 0",  // comma in label (missing quotes)
        };
        for (String item : invalid) {
            test_csvToPlaceItem(item, false);
        }
    }

    public PlaceItem test_csvToPlaceItem(String value, boolean isValid)
    {
        PlaceItem item = BuildPlacesTask.csvItemToPlaceItem(value);
        if (isValid) {
            assertNotNull(item);
            assertNotNull(item.location);
        } else {
            assertNull(item);
        }
        return item;
    }

}
