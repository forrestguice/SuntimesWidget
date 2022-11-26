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

import android.graphics.Color;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LightmapTest0
{
    @Test
    public void test_LightMapKeyInfo_new()
    {
        LightMapDialog.LightMapKeyInfo info0 = new LightMapDialog.LightMapKeyInfo(70000, 80000);
        assertEquals(70000, info0.duration);
        assertEquals("1m", info0.durationString(false));
        assertEquals("1m\u00A010s", info0.durationString(true));
        assertNull(info0.durationColor);

        assertEquals(80000, info0.delta);
        assertEquals("1m", info0.deltaString(false));
        assertEquals("1m\u00A020s", info0.deltaString(true));
        assertNull(info0.deltaColor);
    }

    @Test
    public void test_LightMapKeyInfo_createInfoArray()
    {
        long[] durations0 = new long[] {1, 2, 3};
        LightMapDialog.LightMapKeyInfo[] array0 = LightMapDialog.createInfoArray(durations0);
        assertNotNull(array0);
        assertEquals(durations0.length, array0.length);
        for (int i=0; i<array0.length; i++)
        {
            LightMapDialog.LightMapKeyInfo info = array0[i];
            assertNotNull(info);
            assertEquals(0, info.delta);
            assertEquals(durations0[i], info.duration);
        }

        long[] durations1 = new long[] {0, 0, 0};
        LightMapDialog.LightMapKeyInfo[] array1 = LightMapDialog.createInfoArray(durations1);
        assertNotNull(array1);
        assertEquals(0, array1.length);

        LightMapDialog.LightMapKeyInfo[] array2 = LightMapDialog.createInfoArray(70000, 80000, Color.BLUE);
        assertNotNull(array2);
        assertEquals(1, array2.length);
        assertEquals(70000, array2[0].duration);
        assertEquals(80000, array2[0].delta);
        assertEquals(Color.BLUE, (int)array2[0].durationColor);
    }

}
