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
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This task writes one or more (worldmap) bitmaps to zip file.
 */
public class WorldMapExportTask extends ExportTask
{
    public WorldMapExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        setZippedOutput(zippedOutput);
    }
    public WorldMapExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        setZippedOutput(zippedOutput);
    }

    private Bitmap[] bitmaps;
    public void setBitmaps( Bitmap[] bitmaps ) {
        this.bitmaps = bitmaps;
    }
    public Bitmap[] getBitmaps() {
        return this.bitmaps;
    }

    private String imageExt = ".png";
    private String imageMimeType = "image/png";
    private Bitmap.CompressFormat imageFormat = Bitmap.CompressFormat.PNG;
    private int imageQuality = 100;
    public void setImageFormat(Bitmap.CompressFormat format, int quality, String fileExt)
    {
        imageFormat = format;
        imageQuality = quality;
        imageExt = fileExt;
        switch (imageFormat)
        {
            case PNG:
                imageMimeType = "image/png";
                break;
            case JPEG:
                imageMimeType = "image/jpg";
                break;
            default:
                imageMimeType = "image/*";
                break;
        }
    }

    private boolean zippedOutput = false;
    public void setZippedOutput(boolean value)
    {
        zippedOutput = value;
        if (zippedOutput)
        {
            ext = ".zip";
            mimeType = "application/zip";

        } else {
            ext = imageExt;
            mimeType = imageMimeType;
        }
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (bitmaps != null && bitmaps.length > 0)
        {
            if (zippedOutput)    // write entire bitmap array to zip
            {
                ZipOutputStream zippedOut = new ZipOutputStream(out);
                try {
                    for (int i=0; i<bitmaps.length; i++)
                    {
                        Bitmap bitmap = bitmaps[i];
                        if (bitmap != null)
                        {
                            ZipEntry entry = new ZipEntry(i + imageExt);
                            entry.setMethod(ZipEntry.DEFLATED);
                            zippedOut.putNextEntry(entry);
                            bitmap.compress(imageFormat, imageQuality, zippedOut);
                            zippedOut.flush();
                        }
                    }

                } catch (IOException e) {
                    Log.e("ExportTask", "Error writing zip file: " + e);
                    throw e;

                } finally {
                    zippedOut.close();
                }

            } else {    // write first bitmap in array to file
                Bitmap bitmap = bitmaps[0];
                bitmap.compress(imageFormat, imageQuality, out);
                out.flush();
            }
            return true;
        } else return true;
    }

}
