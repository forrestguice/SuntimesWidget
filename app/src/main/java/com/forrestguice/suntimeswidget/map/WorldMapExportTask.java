/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.map;

import android.content.Context;
import android.graphics.Bitmap;

import com.forrestguice.suntimeswidget.ExportTask;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * This task writes one or more (worldmap) bitmaps to zip file.
 */
public class WorldMapExportTask extends ExportTask
{
    public WorldMapExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        ext = ".zip";
    }
    public WorldMapExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        ext = ".zip";
    }

    private Bitmap[] bitmaps;
    public void setBitmaps( Bitmap[] bitmaps ) {
        this.bitmaps = bitmaps;
    }
    public Bitmap[] getBitmaps() {
        return this.bitmaps;
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (bitmaps != null)
        {
            for (int i=0; i<bitmaps.length; i++)
            {
                Bitmap bitmap = bitmaps[i];
                if (bitmap != null) {
                    // TODO
                }
            }
        }
        return true;
    }

}
