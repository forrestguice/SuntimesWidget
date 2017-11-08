/**
    Copyright (C) 2017 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;

public class ExportThemesTask extends ExportTask
{
    public ExportThemesTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
    }
    public ExportThemesTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        return false;
    }

    @Override
    protected void cleanup(Context context)
    {
    }
}
