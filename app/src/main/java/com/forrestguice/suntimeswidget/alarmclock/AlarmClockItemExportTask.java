/**
    Copyright (C) 2022-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock;

import android.content.Context;
import android.net.Uri;

import com.forrestguice.suntimeswidget.ExportTask;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * AsyncTask that writes AlarmClockItem objects to text file (json array).
 * @see AlarmClockItem
 */
public class AlarmClockItemExportTask extends ExportTask
{
    public static final String FILEEXT = ".txt";
    public static final String MIMETYPE = "text/plain";

    public AlarmClockItemExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public AlarmClockItemExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }
    public AlarmClockItemExportTask(Context context, Uri exportUri)
    {
        super(context, exportUri);
        initTask();
    }

    private void initTask()
    {
        ext = FILEEXT;
        mimeType = MIMETYPE;
    }

    private AlarmClockItem[] items = null;
    public void setItems( AlarmClockItem[] values) {
        items = values;
    }
    public AlarmClockItem[] getItems() {
        return items;
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (items != null)
        {
            numEntries = items.length;
            writeAlarmItemsJSONArray(context, items, out);
            return true;
        }
        return false;
    }

    /**
     * writeAlarmItemsJSONArray
     */
    public static void writeAlarmItemsJSONArray(Context context, AlarmClockItem[] items, BufferedOutputStream out) throws IOException
    {
        out.write("[".getBytes());
        for (int i=0; i<items.length; i++)
        {
            String jsonString = AlarmClockItemImportTask.AlarmClockItemJson.toJson(items[i]);
            out.write(jsonString.getBytes());
            if (i != items.length-1) {
                out.write(", ".getBytes());
            }
        }
        out.write("]".getBytes());
        out.flush();
    }

}
