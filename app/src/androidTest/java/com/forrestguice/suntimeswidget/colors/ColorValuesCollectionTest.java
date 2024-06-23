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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
@RunWith(AndroidJUnit4.class)
public class ColorValuesCollectionTest
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
    public void test_ColorsValuesCollection()
    {
        // new, getCollection, getColors
        ColorValuesCollection<ColorValues> collection0 = new TestColorValueCollection<>();
        String[] ids0 = collection0.getCollection();
        assertNotNull(ids0);
        assertEquals(0, ids0.length);
        assertNull(collection0.getColors(context, "colors0"));

        // setSolors, hasColors, getCollection
        assertFalse(collection0.hasColors("colors0"));
        ColorValues colors0 = ColorValuesTest.TestColorValues.createTestColorValues();
        collection0.setColors(context, "colors0", colors0);
        assertTrue(collection0.hasColors("colors0"));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList(collection0.getCollection()));
        assertTrue(ids.contains("colors0"));

        // getColors
        ColorValues colors = collection0.getColors(context, "colors0");
        ColorValuesTest.TestColorValues.verify_testColorValues(colors);

        // removeColors
        collection0.removeColors(context, "colors0");
        assertFalse(collection0.hasColors("colors0"));
        assertNull(collection0.getColors(context, "colors0"));
    }

    @Test
    public void test_ColorsValuesCollection_SelectedColorsID()
    {
        ColorValuesCollection<ColorValues> collection0 = new TestColorValueCollection<>();

        collection0.setSelectedColorsID(context, "selected");
        assertEquals("selected", collection0.getSelectedColorsID(context));
        collection0.clearSelectedColorsID(context);
        assertNull(collection0.getSelectedColorsID(context));

        collection0.setSelectedColorsID(context, "selected0", 0);
        assertEquals("selected0", collection0.getSelectedColorsID(context, 0));
        collection0.clearSelectedColorsID(context, 0);
        assertNull(collection0.getSelectedColorsID(context, 0));

        collection0.setSelectedColorsID(context, "selected00", 0, "tag0");
        assertEquals("selected00", collection0.getSelectedColorsID(context, 0));
        collection0.clearSelectedColorsID(context, 0, "tag0");
        assertNull(collection0.getSelectedColorsID(context, 0, "tag0"));

        collection0.setSelectedColorsID(context, "selected1", 1);
        assertEquals("selected1", collection0.getSelectedColorsID(context, 1));
        collection0.clearSelectedColorsID(context, 1);
        assertNull(collection0.getSelectedColorsID(context, 1));

        collection0.setSelectedColorsID(context, "selected11", 1, "tag1");
        assertEquals("selected11", collection0.getSelectedColorsID(context, 1));
        collection0.clearSelectedColorsID(context, 1, "tag1");
        assertNull(collection0.getSelectedColorsID(context, 1, "tag1"));
    }

    /**
     * TestColorValueCollection
     */
    public static class TestColorValueCollection<T> extends ColorValuesCollection<ColorValues>
    {
        @Override
        public ColorValues getDefaultColors(Context context)
        {
            return new ColorValues() {
                public String[] getColorKeys() {
                    return new String[0];
                }
            };
        }

        @Nullable
        @Override
        public String getSharedPrefsName() {
            return "testPrefs";
        }

        @Nullable
        @Override
        public String getCollectionSharedPrefsName() {
            return "testCollectionPrefs";
        }
    }

    public static class TestColorValueCollection1<T> extends TestColorValueCollection
    {
        @Nullable
        @Override
        public String getSharedPrefsName() {
            return null;    // use default shared prefs
        }

        @Nullable
        @Override
        public String getCollectionSharedPrefsName() {
            return "testCollectionPrefs";
        }
    }

}
