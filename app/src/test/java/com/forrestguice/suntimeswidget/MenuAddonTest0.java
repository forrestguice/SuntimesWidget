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

package com.forrestguice.suntimeswidget;

import android.content.Context;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MenuAddonTest0
{
    @Test
    public void test_ActivityItemInfo_new()
    {
        MenuAddon.ActivityItemInfo info0 = new MenuAddon.ActivityItemInfo("title", 1, null);
        assertEquals("title", info0.getTitle());
        assertEquals(info0.getTitle(), info0.toString());
        assertEquals(1, info0.getIcon());
        assertNull(info0.getInfo());

        MenuAddon.ActivityItemInfo info1 = new MenuAddon.ActivityItemInfo((Context)null, "title", null);
        assertEquals("title", info1.getTitle());
        assertEquals(info0.getTitle(), info1.toString());
        assertEquals(0, info1.getIcon());                   // null context (icon never assigned)
        assertNull(info0.getInfo());
    }

    @Test
    public void test_ActivityItemInfo_compare()
    {
        MenuAddon.ActivityItemInfo info0 = new MenuAddon.ActivityItemInfo("title", 1, null);
        MenuAddon.ActivityItemInfo info1 = new MenuAddon.ActivityItemInfo("a title", 1, null);
        MenuAddon.ActivityItemInfo info3 = new MenuAddon.ActivityItemInfo("title", 1, null);

        assertTrue(MenuAddon.ActivityItemInfo.title_comparator.compare(info0, info1) > 0);
        assertTrue(MenuAddon.ActivityItemInfo.title_comparator.compare(info1, info0) < 0);
        assertEquals(0, MenuAddon.ActivityItemInfo.title_comparator.compare(info0, info3));
    }
}
