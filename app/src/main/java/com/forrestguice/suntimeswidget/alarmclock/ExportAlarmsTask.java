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

package com.forrestguice.suntimeswidget.alarmclock;

import android.content.ContentValues;
import android.content.Context;

import com.forrestguice.suntimeswidget.ExportTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class ExportAlarmsTask extends ExportTask
{
    public ExportAlarmsTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public ExportAlarmsTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }

    private void initTask()
    {
        ext = ".json";
        mimeType = "text/plain";
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
            out.write("[".getBytes());
            for (int i=0; i<items.length; i++)
            {
                ContentValues values = items[i].asContentValues(true);
                HashMap<String,String> map = new HashMap<>();
                for (String key : values.keySet()) {
                    map.put(key, values.getAsString(key));
                }

                JSONObject json = new JSONObject(map);
                out.write(json.toString().getBytes());
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
