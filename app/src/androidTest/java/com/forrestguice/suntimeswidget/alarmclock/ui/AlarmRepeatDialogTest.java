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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmRepeatDialogTest
{
    private Context context;

    @Before
    public void setup() {
        context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_AlarmRepeatDialog_new()
    {
        AlarmRepeatDialog dialog = new AlarmRepeatDialog();
        assertFalse(AlarmRepeatDialog.PREF_DEF_ALARM_REPEAT);
        assertTrue(isEveryday(AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS));
        assertEquals(AlarmRepeatDialog.PREF_DEF_ALARM_REPEAT, dialog.getRepetition());
        assertTrue(isEveryday(dialog.getRepetitionDays()));
    }

    @Test
    public void test_AlarmRepeatDialog_saveSettings()
    {
        // saveSettings
        Bundle bundle0 = new Bundle();
        AlarmRepeatDialog dialog0 = new AlarmRepeatDialog();
        dialog0.setRepetition(true, new ArrayList<>(Arrays.asList(1, 2)));
        dialog0.saveSettings(bundle0);

        assertTrue(bundle0.getBoolean(AlarmRepeatDialog.PREF_KEY_ALARM_REPEAT));
        ArrayList<Integer> days0 = bundle0.getIntegerArrayList(AlarmRepeatDialog.PREF_KEY_ALARM_REPEATDAYS);
        assertNotNull(days0);
        assertEquals(2, days0.size());
        assertTrue(days0.contains(1));
        assertTrue(days0.contains(2));

        // loadSettings
        Bundle bundle1 = new Bundle();
        AlarmRepeatDialog dialog1 = new AlarmRepeatDialog();
        dialog1.loadSettings(bundle0);
        dialog1.saveSettings(bundle1);

        assertEquals(bundle0.getBoolean(AlarmRepeatDialog.PREF_KEY_ALARM_REPEAT), bundle1.getBoolean(AlarmRepeatDialog.PREF_KEY_ALARM_REPEAT));
        ArrayList<Integer> days1 = bundle1.getIntegerArrayList(AlarmRepeatDialog.PREF_KEY_ALARM_REPEATDAYS);
        assertNotNull(days1);
        assertEquals(days0.size(), days1.size());
        assertTrue(days1.contains(1));
        assertTrue(days1.contains(2));
    }

    @Test
    public void test_AlarmRepeatDialog_setRepetition()
    {
        // true, everyday
        AlarmRepeatDialog dialog0 = new AlarmRepeatDialog();
        dialog0.setRepetition(true, AlarmClockItem.everyday());    // everyday
        assertTrue(dialog0.getRepetition());
        assertTrue(isEveryday(dialog0.getRepetitionDays()));

        // false, null
        AlarmRepeatDialog dialog1 = new AlarmRepeatDialog();
        dialog1.setRepetition(false, null);
        assertFalse(dialog1.getRepetition());
        assertTrue(isEveryday(dialog1.getRepetitionDays()));

        // true, {1,2}
        AlarmRepeatDialog dialog2 = new AlarmRepeatDialog();
        dialog2.setRepetition(true, new ArrayList<Integer>(Arrays.asList(1, 2)));
        assertTrue(dialog2.getRepetition());

        ArrayList<Integer> days2 = dialog2.getRepetitionDays();
        assertEquals(2, days2.size());
        assertTrue(days2.contains(1));
        assertTrue(days2.contains(2));

        // true, {}
        AlarmRepeatDialog dialog3 = new AlarmRepeatDialog();
        dialog3.setRepetition(true, new ArrayList<Integer>());
        assertTrue(dialog3.getRepetition());

        ArrayList<Integer> days3 = dialog3.getRepetitionDays();
        assertNotNull(days3);
        assertEquals(0, days3.size());
    }

    @Test
    public void test_tagToDay()
    {
        for (int i=1; i<=7; i++) {
            assertEquals((Integer)i, AlarmRepeatDialog.tagToDay("" + i));
        }
        assertNull(AlarmRepeatDialog.tagToDay("not-a-number"));
        assertNull(AlarmRepeatDialog.tagToDay(""));
    }

    public boolean isEveryday(ArrayList<Integer> days)
    {
        if (days == null || days.size() != 7) {
            return false;
        }
        for (int i=1; i<=7; i++) {
            if (!days.contains(i)) {
                return false;
            }
        }
        return true;
    }

}
