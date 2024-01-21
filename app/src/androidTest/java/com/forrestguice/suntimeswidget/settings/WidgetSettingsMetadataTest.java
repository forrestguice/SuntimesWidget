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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;

public class WidgetSettingsMetadataTest
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


    protected ContentValues createContentValues0(Long appWidgetId0)
    {
        ContentValues values0 = new ContentValues();
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, className);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_VERSIONCODE, versionCode);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_CATEGORY, category);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_WIDTH_MIN, minWidth);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_HEIGHT_MIN, minHeight);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_WIDTH_MAX, maxWidth);
        values0.put(WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_HEIGHT_MAX, maxHeight);
        return values0;
    }

    @Test
    public void test_WidgetMetadata_getMetaDataFromValues()
    {
        long appWidgetId0 = 100;
        ContentValues values0 = createContentValues0(appWidgetId0);
        WidgetSettingsMetadata.WidgetMetadata d0 = WidgetSettingsMetadata.WidgetMetadata.getMetaDataFromValues(values0, appWidgetId0);
        assertEquals(d0.getWidgetClassName(), className);
        assertEquals(d0.getVersionCode(), versionCode);
        assertEquals(d0.getCategory(), category);
        assertEquals(d0.getMinDimensions()[0], minWidth);
        assertEquals(d0.getMinDimensions()[1], minHeight);
        assertEquals(d0.getMaxDimensions()[0], maxWidth);
        assertEquals(d0.getMaxDimensions()[1], maxHeight);

        WidgetSettingsMetadata.WidgetMetadata d1 = WidgetSettingsMetadata.WidgetMetadata.getMetaDataFromValues(values0);
        assertEquals(d1.getWidgetClassName(), className);
        assertEquals(d1.getVersionCode(), versionCode);
        assertEquals(d1.getCategory(), category);
        assertEquals(d1.getMinDimensions()[0], minWidth);
        assertEquals(d1.getMinDimensions()[1], minHeight);
        assertEquals(d1.getMaxDimensions()[0], maxWidth);
        assertEquals(d1.getMaxDimensions()[1], maxHeight);
    }

    @Test
    public void test_WidgetMetadata_findAppWidgetIdFromFirstKey()
    {
        Long appWidgetId0 = 100L;
        ContentValues values0 = createContentValues0(appWidgetId0);
        Long appWidgetId1 = WidgetSettingsImportTask.findAppWidgetIdFromFirstKey(values0);
        assertNotNull(appWidgetId1);
        assertEquals(appWidgetId1, appWidgetId0);

        ContentValues values2 = new ContentValues();
        Long appWidgetId3 = WidgetSettingsImportTask.findAppWidgetIdFromFirstKey(values2);
        assertNull(appWidgetId3);
    }

    @Test
    public void test_WidgetMetadata_replaceKeyPrefix()
    {
        Long appWidgetId0 = 100L;
        ContentValues values0 = createContentValues0(appWidgetId0);
        String key0 = WidgetSettings.PREF_PREFIX_KEY + appWidgetId0 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + "testKey";
        String value0 = "testValue";
        values0.put(key0, value0);

        Long appWidgetId1 = 200L;
        ContentValues values1 = WidgetSettingsImportTask.replaceKeyPrefix(values0, appWidgetId1);
        String key1 = WidgetSettings.PREF_PREFIX_KEY + appWidgetId1 + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + "testKey";
        assertNotNull(values1);
        assertTrue(values1.containsKey(key1));
        assertEquals(values1.get(key1), value0);
    }

}
