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

import android.graphics.drawable.Drawable;

import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;

import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class SuntimesWidgetListTest0
{
    @Test
    public void test_WidgetListItem_new()
    {
        WidgetListAdapter.WidgetListItem item0 = new WidgetListAdapter.WidgetListItem("package", "widgetClass", 1, (Drawable)null, "title", "summary", "configClass");
        assertNull(item0.getIcon());
        assertEquals("title", item0.getTitle());
        assertEquals(item0.getTitle(), item0.toString());
        assertEquals("summary", item0.getSummary());
        assertEquals(1, item0.getWidgetId());
        assertEquals("package", item0.getPackageName());
        assertEquals("widgetClass", item0.getWidgetClass());
        assertEquals("configClass", item0.getConfigClass());
    }

}
