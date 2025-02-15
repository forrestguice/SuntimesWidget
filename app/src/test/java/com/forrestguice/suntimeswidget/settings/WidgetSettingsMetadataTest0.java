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

package com.forrestguice.suntimeswidget.settings;

import android.content.ContentValues;
import com.forrestguice.support.annotation.NonNull;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class WidgetSettingsMetadataTest0
{
    private static final String className = "class0";
    private static final int versionCode = 100;
    private static final int category = 200;
    private static final int minWidth = 300;
    private static final int minHeight = 400;
    private static final int maxWidth = 500;
    private static final int maxHeight = 600;

    private static final String className1 = "class1";
    private static final int versionCode1 = 1000;
    private static final int category1 = 1200;
    private static final int minWidth1 = 1300;
    private static final int minHeight1 = 1400;
    private static final int maxWidth1 = 1500;
    private static final int maxHeight1 = 1600;


    @Test
    public void test_WidgetMetadata_new()
    {
        WidgetSettingsMetadata.WidgetMetadata d0 = new WidgetSettingsMetadata.WidgetMetadata(className, versionCode, category, minWidth, minHeight, maxWidth, maxHeight);
        assertEquals(d0.getWidgetClassName(), className);
        assertEquals(d0.getVersionCode(), versionCode);
        assertEquals(d0.getCategory(), category);
        assertEquals(d0.getMinDimensions()[0], minWidth);
        assertEquals(d0.getMinDimensions()[1], minHeight);
        assertEquals(d0.getMaxDimensions()[0], maxWidth);
        assertEquals(d0.getMaxDimensions()[1], maxHeight);

        WidgetSettingsMetadata.WidgetMetadata d1 = new WidgetSettingsMetadata.WidgetMetadata(className1, versionCode1, d0);
        assertEquals(d1.getWidgetClassName(), className1);
        assertEquals(d1.getVersionCode(), versionCode1);
        assertEquals(d1.getCategory(), category);
        assertEquals(d1.getMinDimensions()[0], minWidth);
        assertEquals(d1.getMinDimensions()[1], minHeight);
        assertEquals(d1.getMaxDimensions()[0], maxWidth);
        assertEquals(d1.getMaxDimensions()[1], maxHeight);

        WidgetSettingsMetadata.WidgetMetadata d2 = new WidgetSettingsMetadata.WidgetMetadata(d1);
        assertEquals(d2.getWidgetClassName(), className1);
        assertEquals(d2.getVersionCode(), versionCode1);
        assertEquals(d2.getCategory(), category);
        assertEquals(d2.getMinDimensions()[0], minWidth);
        assertEquals(d2.getMinDimensions()[1], minHeight);
        assertEquals(d2.getMaxDimensions()[0], maxWidth);
        assertEquals(d2.getMaxDimensions()[1], maxHeight);
    }

    @Test
    public void test_WidgetMetadata_equals()
    {
        WidgetSettingsMetadata.WidgetMetadata d0 = new WidgetSettingsMetadata.WidgetMetadata(className, versionCode, category, minWidth, minHeight, maxWidth, maxHeight);
        WidgetSettingsMetadata.WidgetMetadata d00 = new WidgetSettingsMetadata.WidgetMetadata(d0);
        WidgetSettingsMetadata.WidgetMetadata d1 = new WidgetSettingsMetadata.WidgetMetadata(className1, versionCode1, category1, minWidth1, minHeight1, maxWidth1, maxHeight1);
        assertEquals(d0, d0);      // == .. self
        assertEquals(d00, d0);     // == .. copy
        assertNotEquals(d1, d0);   // != .. different className
        assertEquals(d00.hashCode(), d0.hashCode());
        assertNotEquals(d1.hashCode(), d0.hashCode());

        WidgetSettingsMetadata.WidgetMetadata d2 = new WidgetSettingsMetadata.WidgetMetadata(className, versionCode1, d0);
        WidgetSettingsMetadata.WidgetMetadata d3 = new WidgetSettingsMetadata.WidgetMetadata(className1, versionCode1, d0);
        assertEquals(d2, d0);       // == .. versionCode ignored by equality
        assertNotEquals(d3, d0);    // != .. different className
        assertEquals(d2.hashCode(), d0.hashCode());
        assertNotEquals(d3.hashCode(), d0.hashCode());
    }

}
