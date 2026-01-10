/**
    Copyright (C) 2017-2022 Forrest Guice
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

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import android.content.Context;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class ExportThemesTask extends ExportTask
{
    public static final String FILEEXT = ".xml";
    public static final String MIMETYPE = "text/xml";

    public ExportThemesTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public ExportThemesTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }
    public ExportThemesTask(Context context, Uri uri)
    {
        super(context, uri);
        initTask();
    }

    private void initTask()
    {
        ext = FILEEXT;
        mimeType = MIMETYPE;
    }

    private SuntimesTheme.ThemeDescriptor[] descriptors = null;
    public void setDescriptors( SuntimesTheme.ThemeDescriptor[] values)
    {
        descriptors = values;
    }
    public SuntimesTheme.ThemeDescriptor[] getDescriptors()
    {
        return descriptors;
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (descriptors != null)
        {
            numEntries = descriptors.length;
            final SuntimesTheme[] themes = new SuntimesTheme[numEntries];
            for (int i=0; i<numEntries; i++)
            {
                SuntimesTheme.ThemeDescriptor themeDesc = descriptors[i];
                themes[i] = WidgetThemes.loadTheme(context, themeDesc.name());
            }

            SuntimesThemeIO xml = new SuntimesThemeXML();
            xml.setProgressListener(new SuntimesThemeIO.ProgressListener()
            {
                @Override
                public void onExported(SuntimesTheme theme, int i, int n)
                {
                    String msg = themes[i].themeName();
                    ExportProgress progressObj = new ExportProgress(i, n, msg);
                    publishProgress(progressObj);
                }
            });
            return xml.write(context, out, themes);
        }
        return false;
    }

}
