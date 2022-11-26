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

import android.support.design.widget.Snackbar;

import org.junit.Test;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class SuntimesWarningTest0
{
    @Test
    public void test_SuntimesWarning_new()
    {
        SuntimesWarning warning0 = new SuntimesWarning("test");    // un-initialized warning
        assertEquals("test", warning0.id);
        assertNull(warning0.getSnackbar());
        assertNull(warning0.warningListener);
        assertFalse(warning0.wasDismissed());

        assertEquals(Snackbar.LENGTH_INDEFINITE, warning0.getDuration());
        warning0.setDuration(Snackbar.LENGTH_SHORT);
        assertEquals(Snackbar.LENGTH_SHORT, warning0.getDuration());

        assertNull(warning0.contentDescription);
        warning0.setContentDescription("desc");
        assertEquals("desc", warning0.contentDescription);

        assertFalse(warning0.shouldShow());
        warning0.setShouldShow(true);
        assertTrue(warning0.shouldShow());

        TestWarningListener listener0 = new TestWarningListener();
        warning0.setWarningListener(listener0);
        assertEquals(listener0, warning0.warningListener);
    }

    @Test
    public void test_SuntimesWarning_reset()
    {
        SuntimesWarning warning0 = new SuntimesWarning("test");
        assertFalse(warning0.wasDismissed());
        assertFalse(warning0.shouldShow());

        warning0.wasDismissed = true;
        warning0.setShouldShow(true);
        assertTrue(warning0.shouldShow());

        warning0.reset();
        assertFalse(warning0.wasDismissed());
        assertFalse(warning0.shouldShow());
    }

    @Test
    public void test_SuntimesWarning_showNextWarning()
    {
        SuntimesWarning warning0 = new SuntimesWarning("test");
        assertNull(warning0.warningListener);

        TestWarningListener listener0 = new TestWarningListener();
        warning0.setWarningListener(listener0);
        assertEquals(listener0, warning0.warningListener);

        warning0.showNextWarning();
        assertTrue(listener0.wasRun);
    }

    public static final class TestWarningListener extends SuntimesWarning.SuntimesWarningListener
    {
        public boolean wasRun = false;
        @Override
        public void onShowNextWarning() {
            wasRun = true;
        }
    }

}
