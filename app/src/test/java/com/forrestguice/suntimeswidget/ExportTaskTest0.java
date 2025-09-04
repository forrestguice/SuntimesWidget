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

import android.net.Uri;

import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ExportTaskTest0
{
    @Test
    public void test_ExportResult_new()
    {
        ExportTask.ExportResult result0 = new ExportTask.ExportResult(false, null, null, null);
        assertFalse(result0.getResult());
        assertNull(result0.getExportUri());
        assertNull(result0.getExportFile());
        assertNull(result0.getMimeType());

        File file1 = new File("test");
        ExportTask.ExportResult result1 = new ExportTask.ExportResult(true, Uri.EMPTY, file1, "mime");
        assertTrue(result1.getResult());
        assertEquals(Uri.EMPTY, result1.getExportUri());
        assertEquals(file1, result1.getExportFile());
        assertEquals("mime", result1.getMimeType());
    }

    @Test
    public void test_ExportProgress_new()
    {
        ExportTask.ExportProgress progress0 = new ExportTask.ExportProgress(50, 100, "test");
        assertEquals(50, progress0.getProgress());
        assertEquals(100, progress0.getProgressMax());
        assertEquals("test", progress0.getProgressMsg());
    }

}
