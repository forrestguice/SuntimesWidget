package com.forrestguice.suntimeswidget.getfix;

import android.os.Parcel;
import androidx.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimes.calculator.core.Location;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class PlaceItemTest
{
    @Test
    public void test_PlaceItem_parcelable()
    {
        PlaceItem item0 = new PlaceItem();
        item0.rowID = 10;
        item0.location = new Location("test", SuntimesActivityTestBase.TESTLOC_0_LAT, SuntimesActivityTestBase.TESTLOC_0_LON, "110");
        assertFalse(item0.isDefault());

        item0.comment = PlaceItem.TAG_DEFAULT;
        test_PlaceItem_parcelable(item0);

        item0.comment = null;
        test_PlaceItem_parcelable(item0);

        long rowID = 1;
        for (Location location : GetFixDatabaseAdapterTest.locations)
        {
            if (location != null)
            {
                PlaceItem item = new PlaceItem(rowID, location);
                assertFalse(item0.isDefault());
                test_PlaceItem_parcelable(item);
                rowID++;
            }
        }
    }

    public void test_PlaceItem_parcelable(PlaceItem item0)
    {
        Parcel parcel0 = Parcel.obtain();
        item0.writeToParcel(parcel0, 0);
        parcel0.setDataPosition(0);

        PlaceItem item = (PlaceItem) PlaceItem.CREATOR.createFromParcel(parcel0);
        assertEquals(item0.rowID, item.rowID);
        assertEquals(item0.isDefault(), item.isDefault());
        assertEquals(item0.location, item.location);
    }
}
