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
import com.forrestguice.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    private ConcurrentLinkedQueue<byte[]> bitmaps;
    public void setBitmaps( @NonNull Bitmap[] bitmaps ) {
        this.bitmaps = new ConcurrentLinkedQueue<>();
        for (Bitmap bitmap : bitmaps) {
            addBitmap(bitmap);
        }
    }
    public void addBitmap(@NonNull Bitmap bitmap)
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(imageFormat, imageQuality, byteStream);
        byte[] bytes = byteStream.toByteArray();
        bitmaps.add(bytes);
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

    private boolean waitForFrames = false;
    public void setWaitForFrames(boolean value) {
        waitForFrames = value;
    }

    @Override
    protected boolean export(Context context, BufferedOutputStream out) throws IOException
    {
        if (bitmaps != null && bitmaps.size() > 0)
        {
            if (zippedOutput)    // write entire bitmap array to zip
            {
                ZipOutputStream zippedOut = new ZipOutputStream(out);
                try {

                    int c = 0;
                    while (!isCancelled() && (!bitmaps.isEmpty() || waitForFrames))
                    {
                        byte[] bitmap = bitmaps.poll();
                        if (bitmap != null)
                        {
                            ZipEntry entry = new ZipEntry(c + imageExt);
                            entry.setMethod(ZipEntry.DEFLATED);
                            zippedOut.putNextEntry(entry);
                            zippedOut.write(bitmap);
                            zippedOut.flush();
                            c++;
                        }
                    }

                } catch (IOException e) {
                    Log.e("ExportTask", "Error writing zip file: " + e);
                    throw e;

                } finally {
                    zippedOut.close();
                }

            } else {
                out.write(bitmaps.peek());
                out.flush();
            }
            return true;
        } else return true;
    }

}
