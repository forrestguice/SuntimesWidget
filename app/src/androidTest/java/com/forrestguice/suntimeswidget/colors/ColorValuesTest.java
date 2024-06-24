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

package com.forrestguice.suntimeswidget.colors;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
@RunWith(AndroidJUnit4.class)
public class ColorValuesTest
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);
    private Context context;

    @Before
    public void init()
    {
        context = activityRule.getActivity();
    }

    @Test
    public void test_ColorsValues_Default()
    {
        ColorValues colors0 = TestColorValues.createTestColorValues();
        TestColorValues.verify_testColorValues(colors0);
    }

    @Test
    public void test_ColorsValues_Other()
    {
        ColorValues colors0 = TestColorValues.createTestColorValues();
        TestColorValues.verify_testColorValues(colors0);

        assertNull(colors0.getID());
        colors0.setID("colors0");
        assertEquals("colors0", colors0.getID());

        //colors0.setColor("extra", Color.BLUE);
        //colors0.setLabel("extra", "BLUE");    // "extra" is not one of the predefined keys

        ColorValues colors1 = new TestColorValues(colors0);
        TestColorValues.verify_testColorValues(colors1);

        //assertEquals(Color.BLUE, colors1.getColor("extra"));
        //assertEquals("BLUE", colors1.getLabel("extra"));

        assertEquals("colors0", colors1.getID());
        colors1.setID("colors1");
        assertEquals("colors1", colors1.getID());
    }

    @Test
    public void test_ColorsValues_ContentValues()
    {
        ColorValues colors0 = TestColorValues.createTestColorValues();
        colors0.setID("colors0");
        assertEquals("colors0", colors0.getID());
        TestColorValues.verify_testColorValues(colors0);

        ContentValues contentValues0 = colors0.getContentValues();
        assertNotNull(contentValues0);
        assertTrue(contentValues0.containsKey("0"));
        assertTrue(contentValues0.containsKey("1"));
        assertTrue(contentValues0.containsKey("2"));
        assertTrue(contentValues0.containsKey("3"));
        assertEquals(new Integer(Color.MAGENTA), (Integer) contentValues0.getAsInteger("0"));
        assertEquals(new Integer(Color.RED), (Integer) contentValues0.getAsInteger("1"));
        assertEquals(new Integer(Color.YELLOW), (Integer) contentValues0.getAsInteger("2"));
        assertEquals(new Integer(Color.GREEN), (Integer) contentValues0.getAsInteger("3"));

        ColorValues colors2 = new TestColorValues();
        colors2.setID("colors2");    // overwritten by loadColorValues
        colors2.loadColorValues(contentValues0);
        TestColorValues.verify_testColorValues(colors2);
        assertEquals("colors0", colors2.getID());

        ColorValues colors3 = new TestColorValues(contentValues0);
        TestColorValues.verify_testColorValues(colors2);
        assertEquals("colors0", colors2.getID());
    }

    @Test
    public void test_ColorsValues_Parcelable()
    {
        ColorValues colors0 = TestColorValues.createTestColorValues();
        colors0.setID("colors0");
        assertEquals("colors0", colors0.getID());
        TestColorValues.verify_testColorValues(colors0);

        Parcel parcel = Parcel.obtain();
        colors0.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ColorValues colors1 = TestColorValues.CREATOR.createFromParcel(parcel);
        TestColorValues.verify_testColorValues(colors1);
        assertEquals(colors0.getID(), colors1.getID());
    }

    @Test
    public void test_ColorsValues_JSON()
    {
        ColorValues colors0 = TestColorValues.createTestColorValues();
        colors0.setID("colors0");
        assertEquals("colors0", colors0.getID());
        TestColorValues.verify_testColorValues(colors0);

        String json0 = colors0.toJSON(true);
        assertNotNull(json0);

        ColorValues colors = new TestColorValues(json0);
        TestColorValues.verify_testColorValues(colors);
        assertEquals("colors0", colors.getID());
    }

    /**
     * TestColorValues
     */
    public static class TestColorValues extends ColorValues
    {
        public TestColorValues() {
            super();
        }

        public TestColorValues(ColorValues other) {
            super(other);
        }

        public TestColorValues(ContentValues values) {
            super(values);
        }

        public TestColorValues(String json) {
            super(json);
        }

        protected TestColorValues(Parcel in) {
            super(in);
        }

        @Override
        public String[] getColorKeys() {
            return new String[] {"0", "1", "2", "3"};
        }

        public static ColorValues createTestColorValues()
        {
            ColorValues values = new TestColorValues();
            values.setColor("0", Color.MAGENTA);
            values.setColor("1", Color.RED);
            values.setColor("2", Color.YELLOW);
            values.setColor("3", Color.GREEN);
            values.setLabel("0", "MAGENTA");
            values.setLabel("1", "RED");
            values.setLabel("2", "YELLOW");
            //values.setLabel("3", "GREEN");    // label unset
            return values;
        }

        public static void verify_testColorValues(ColorValues values)
        {
            String[] keys = values.getColorKeys();
            assertNotNull(keys);
            assertEquals(4, keys.length);
            assertEquals(0, values.colorKeyIndex("0"));
            assertEquals(1, values.colorKeyIndex("1"));
            assertEquals(2, values.colorKeyIndex("2"));
            assertEquals(3, values.colorKeyIndex("3"));
            assertEquals(-1, values.colorKeyIndex("4"));    // no "4"

            ContentValues v = values.getContentValues();
            for (String key : v.keySet()) {
                Log.d("DEBUG", key + "=" + v.getAsString(key));
            }

            assertEquals(Color.MAGENTA, values.getColor("0"));
            assertEquals(Color.RED, values.getColor("1"));
            assertEquals(Color.YELLOW, values.getColor("2"));
            assertEquals(Color.GREEN, values.getColor("3"));
            assertEquals(values.getFallbackColor(), values.getColor("4"));    // no "4"

            assertEquals("MAGENTA", values.getLabel("0"));
            assertEquals("RED", values.getLabel("1"));
            assertEquals("YELLOW", values.getLabel("2"));
            //assertEquals("GREEN", values.getLabel("3"));
            assertEquals("3", values.getLabel("3"));    // label unset
            assertEquals("4", values.getLabel("4"));    // no "4"

            ArrayList<Integer> c0 = values.getColors();
            assertEquals(c0.size(), 4);
            assertTrue(c0.contains(Color.MAGENTA));
            assertTrue(c0.contains(Color.RED));
            assertTrue(c0.contains(Color.YELLOW));
            assertTrue(c0.contains(Color.GREEN));
        }

        public static final Creator<TestColorValues> CREATOR = new Creator<TestColorValues>()
        {
            public TestColorValues createFromParcel(Parcel in) {
                return new TestColorValues(in);
            }
            public TestColorValues[] newArray(int size) {
                return new TestColorValues[size];
            }
        };
    };

}
