package com.forrestguice.suntimeswidget.getfix;

import com.forrestguice.suntimeswidget.calculator.core.Location;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class BuildPlacesTaskTest0
{
    @Test
    public void test_csvItemToLocation_valid()
    {
        String[] valid = new String[] {
                "Sydney, -33.8981, 151.2328",
                "Sydney, -33.8981, 151.2328, 40",
                "\"Sydney\", -33.8981, 151.2328, 40",
                "Pago Pago, -14.27176, -170.70223, 0",
                "\"San José\", 9.9356, -84.1483, 1017",
                "\"Willemstad, Curaçao\", 12.11018, -68.95539, 0",
                "\"St. George\'s, Bermuda\", 32.37944, -64.67777, 0"
        };
        for (String item : valid) {
            test_csvItemToLocation(item, true);
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
            test_csvItemToLocation(item, false);
        }
    }

    public Location test_csvItemToLocation(String item, boolean isValid)
    {
        Location location = BuildPlacesTask.csvItemToLocation(item);
        if (isValid) {
            assertNotNull(location);
        } else {
            assertNull(location);
        }
        return location;
    }

}
