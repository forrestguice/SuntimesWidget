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
import android.content.Context;
import android.net.Uri;

import com.forrestguice.suntimeswidget.ExportTask;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WidgetSettingsExportTask extends ExportTask
{
    public static final String FILEEXT = ".txt";
    public static final String MIMETYPE = "text/plain";

    public WidgetSettingsExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public WidgetSettingsExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }
    public WidgetSettingsExportTask(Context context, Uri uri)
    {
        super(context, uri);
        initTask();
    }

    private void initTask()
    {
        ext = FILEEXT;
        mimeType = MIMETYPE;
    }

    @Override
    public boolean export( Context context, BufferedOutputStream out ) throws IOException
    {
        for (int i=0; i<appWidgetIds.size(); i++)
        {
            Integer appWidgetId = appWidgetIds.get(i);
            if (appWidgetId != null) {
                String json = WidgetSettingsImportTask.WidgetSettingsJson.toJson(WidgetSettings.toContentValues(context, appWidgetId));
                out.write(json.getBytes());
            }
        }
        out.flush();
        return true;
    }

    /**
     * @param value export single appWidgetId
     */
    public void setAppWidgetId(int value)
    {
        appWidgetIds.clear();
        appWidgetIds.add(value);
    }
    public void setAppWidgetIds(ArrayList<Integer> values)
    {
        appWidgetIds.clear();
        appWidgetIds.addAll(values);
    }
    protected ArrayList<Integer> appWidgetIds = new ArrayList<>();

}
