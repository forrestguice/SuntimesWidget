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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.net.Uri;

import com.forrestguice.suntimeswidget.ExportTask;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * AsyncTask that writes EventAlias objects to text file (json array).
 * @see EventSettings.EventAlias
 */
public class EventExportTask extends ExportTask
{
    public static final String FILEEXT = ".txt";
    public static final String MIMETYPE = "text/plain";

    public EventExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public EventExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }
    public EventExportTask(Context context, Uri exportUri)
    {
        super(context, exportUri);
        initTask();
    }

    private void initTask()
    {
        ext = FILEEXT;
        mimeType = MIMETYPE;
    }

    private EventSettings.EventAlias[] items = null;
    public void setItems( EventSettings.EventAlias[] values) {
        items = values;
    }
    public EventSettings.EventAlias[] getItems() {
        return items;
    }

    public static String toJson(EventSettings.EventAlias item)
    {
        HashMap<String,String> map = toMap(item.toContentValues());
        return new JSONObject(map).toString();
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (items != null)
        {
            numEntries = items.length;
            out.write("[".getBytes());
            for (int i=0; i<items.length; i++)
            {
                String jsonString = toJson(items[i]);
                out.write(jsonString.getBytes());
                if (i != items.length-1) {
                    out.write(", ".getBytes());
                }
            }
            out.write("]".getBytes());
            return true;
        }
        return false;
    }

}
