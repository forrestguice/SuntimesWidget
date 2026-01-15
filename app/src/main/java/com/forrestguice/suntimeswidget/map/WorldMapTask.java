/**
    Copyright (C) 2018-2019 Forrest Guice
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

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.forrestguice.util.Log;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

public class WorldMapTask extends AsyncTask<Object, Bitmap, Bitmap>
{
    private WorldMapProjection projection = new WorldMapEquirectangular();
    private WorldMapOptions options = new WorldMapOptions();

    public WorldMapTask()
    {
    }

    /**
     * @param params 0: SuntimesRiseSetDataset,
     *               1: Integer (width),
     *               2: Integer (height),
     *               3: Drawable (map)
     * @return a bitmap, or null params are invalid
     */
    @Override
    protected Bitmap doInBackground(Object... params)
    {
        int w, h;
        int numFrames = 1;
        long frameDuration = 250000000;    // nanoseconds (250 ms)
        long initialOffset = 0;
        SuntimesRiseSetDataset data;
        try {
            data = (SuntimesRiseSetDataset)params[0];
            w = (Integer)params[1];
            h = (Integer)params[2];
            if (params.length > 3) {
                options = (WorldMapOptions) params[3];
            }
            if (params.length > 4) {
                projection = (WorldMapProjection) params[4];
            }
            if (params.length > 5) {
                numFrames = (int)params[5];
            }
            if (params.length > 6) {
                initialOffset = (long)params[6];
            }
            frameDuration = options.anim_frameLengthMs * 1000000;   // ms to ns

        } catch (ClassCastException e) {
            Log.w("WorldMapTask", "Invalid params; using [null, 0, 0]");
            return null;
        }

        long time0 = System.nanoTime();
        Bitmap frame = null;
        options.offsetMinutes = initialOffset;

        int i = 0;
        while (i < numFrames || numFrames <= 0)
        {
            if (isCancelled()) {
                break;
            }
            frame = makeBitmap(data, w, h, options);

            long time1 = System.nanoTime();
            while ((time1 - time0) < frameDuration) {
                time1 = System.nanoTime();
            }

            publishProgress(frame);
            if (listener != null) {
                listener.afterFrame(frame, options.offsetMinutes);
            }
            options.offsetMinutes += options.anim_frameOffsetMinutes;
            time0 = System.nanoTime();
            i++;
        }
        options.offsetMinutes -= options.anim_frameOffsetMinutes;
        return frame;
    }

    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options)
    {
        return projection.makeBitmap(data, w, h, options);
    }

    @Override
    protected void onPreExecute()
    {
        if (listener != null) {
            listener.onStarted();
        }
    }

    @Override
    protected void onProgressUpdate( Bitmap... frames )
    {
        if (listener != null)
        {
            for (int i=0; i<frames.length; i++) {
                listener.onFrame(frames[i], options.offsetMinutes);
            }
        }
    }

    @Override
    protected void onPostExecute( Bitmap lastFrame )
    {
        if (isCancelled()) {
            lastFrame = null;
        }
        if (listener != null) {
            listener.onFinished(lastFrame);
        }
    }

    /////////////////////////////////////////////

    private WorldMapTaskListener listener = null;
    public void setListener( WorldMapTaskListener listener ) {
        this.listener = listener;
    }

    /**
     * WorldMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class WorldMapTaskListener
    {
        public void onStarted() {}
        public void onFrame(Bitmap frame, long offsetMinutes ) {}
        public void afterFrame(Bitmap frame, long offsetMinutes ) {}
        public void onFinished( Bitmap result ) {}
    }

}
