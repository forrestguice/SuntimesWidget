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

package com.forrestguice.suntimeswidget.graph;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.util.Log;

public class LightGraphTask extends AsyncTask<Object, Bitmap, Bitmap>
{
    private final LightGraphBitmap lightgraph = new LightGraphBitmap();

    @Nullable
    protected SuntimesRiseSetDataset[] yearData = null;
    public void setData(SuntimesRiseSetDataset[] data) {
        yearData = data;
    }
    public void invalidData() {
        yearData = null;
    }

    /**
     * @param params 0: SuntimesRiseSetDataset,
     *               1: Integer (width),
     *               2: Integer (height)
     *               3: options (optional),
     *               4: num frames (optional),
     *               5: initial offset (optional)
     * @return a bitmap, or null params are invalid
     */
    @Override
    protected Bitmap doInBackground(Object... params)
    {
        LightGraphOptions options;
        int w, h;
        int numFrames = 1;
        long frameDuration = 250000000;    // nanoseconds (250 ms)
        long initialOffset = 0;            // days
        SuntimesRiseSetDataset data0;

        try {
            data0 = (SuntimesRiseSetDataset)params[0];
            w = (Integer)params[1];
            h = (Integer)params[2];
            options = (LightGraphOptions)params[3];

            if (params.length > 4) {
                numFrames = (int)params[4];
            }
            if (params.length > 5) {
                initialOffset = (long)params[5];
            }
            frameDuration = options.anim_frameLengthMs * 1000000;   // ms to ns

        } catch (ClassCastException e) {
            Log.w(LightGraphTask.class.getSimpleName(), "Invalid params; using [null, 0, 0]");
            return null;
        }

        Bitmap frame = null;
        long time0 = System.nanoTime();
        options.offsetDays = initialOffset;

        if (yearData != null)
        {
            int i = 0;
            while (i < numFrames || numFrames <= 0)
            {
                //Log.d(getClass().getSimpleName(), "generating frame " + i + " | " + w + "," + h + " :: " + numFrames);
                if (isCancelled()) {
                    break;
                }
                options.acquireDrawLock();

                frame = lightgraph.makeBitmap(yearData, w, h, options);

                long time1 = System.nanoTime();
                while ((time1 - time0) < frameDuration) {
                    time1 = System.nanoTime();
                }

                publishProgress(frame);
                if (listener != null) {
                    listener.afterFrame(frame, options.offsetDays);
                }
                options.offsetDays += options.anim_frameOffsetDays;
                time0 = System.nanoTime();
                i++;
                options.releaseDrawLock();
            }
        }

        options.offsetDays -= options.anim_frameOffsetDays;
        return frame;
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
            notifyIfDataModified();
            LightGraphOptions options = lightgraph.getOptions();
            if (options != null) {
                for (int i = 0; i < frames.length; i++) {
                    listener.onFrame(frames[i], options.offsetDays);
                }
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
            notifyIfDataModified();
            listener.onFinished(lastFrame);
        }
    }

    protected void notifyIfDataModified()
    {
        /*if (t_data != null) {
            listener.onDataModified(t_data);
            t_data = null;
        }*/
    }

    @Nullable
    private LightGraphView.LightGraphTaskListener listener = null;
    public void setListener( @Nullable LightGraphView.LightGraphTaskListener listener ) {
        this.listener = listener;
    }
    public void clearListener() {
        this.listener = null;
    }
}
