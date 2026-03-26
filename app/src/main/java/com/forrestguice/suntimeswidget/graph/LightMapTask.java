/**
    Copyright (C) 2014-2022 Forrest Guice
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

import android.content.Context;
import android.graphics.Bitmap;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.util.Log;
import com.forrestguice.util.concurrent.ProgressCallable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Collection;

public class LightMapTask extends ProgressCallable<Bitmap, Bitmap>
{
    private final WeakReference<Context> contextRef;
    protected LightMapBitmap lightmap = new LightMapBitmap();

    @Nullable
    private SuntimesRiseSetDataset t_data = null;
    private final Object[] params;

    /**
     * @param params 0: SuntimesRiseSetDataset,
     *               1: Integer (width),
     *               2: Integer (height)
     *               3: LightMapOptions
     *               4: numFrames (optional: 1)
     *               5: initialOffset (millis) (optional: 0)
     */
    public LightMapTask(Context context, Object[] params) {
        contextRef = new WeakReference<>(context);
        this.params = params;
    }

    /**
     * @return a bitmap, or null params are invalid
     */

    @Override
    public Bitmap call() throws Exception
    {
        int w, h;
        int numFrames = 1;
        long frameDuration = 250000000;    // nanoseconds (250 ms)
        long initialOffset = 0;
        LightMapOptions options;
        SuntimesRiseSetDataset data;
        try {
            data = (SuntimesRiseSetDataset)params[0];
            w = (Integer)params[1];
            h = (Integer)params[2];
            options = (LightMapOptions)params[3];

            if (params.length > 4) {
                numFrames = (int)params[4];
            }
            if (params.length > 5) {
                initialOffset = (long)params[5];
            }
            frameDuration = options.anim_frameLengthMs * 1000000;   // ms to ns

        } catch (ClassCastException e) {
            Log.w(LightMapTask.class.getSimpleName(), "Invalid params; using [null, 0, 0]");
            return null;
        }

        long time0 = System.nanoTime();
        Bitmap frame = null;
        options.offsetMinutes = initialOffset;

        int i = 0;
        while (i < numFrames || numFrames <= 0)
        {
            //Log.d(LightMapTask.class.getSimpleName(), "generating frame " + i + " | " + w + "," + h);
            if (isCancelled()) {
                break;
            }
            options.acquireDrawLock();

            if (data != null && data.dataActual != null)
            {
                Calendar maptime = LightMapBitmap.mapTime(data, options);
                Calendar datatime = data.dataActual.calendar();
                long data_age = Math.abs(maptime.getTimeInMillis() - datatime.getTimeInMillis());
                if (data_age >= (12 * 60 * 60 * 1000)) {    // TODO: more precise

                    //Log.d(LightMapTask.class.getSimpleName(), "recalculating dataset with adjusted date: " + data_age);
                    Calendar calendar = Calendar.getInstance(data.timezone());
                    calendar.setTimeInMillis(maptime.getTimeInMillis());

                    data = new SuntimesRiseSetDataset(data);
                    data.setTodayIs(calendar);
                    data.calculateData(contextRef.get());
                    t_data = data;
                }
            }

            frame = lightmap.makeBitmap(data, w, h, options);

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
            options.releaseDrawLock();
        }
        options.offsetMinutes -= options.anim_frameOffsetMinutes;

        //Log.d("DEBUG", "doInBackground: done: " + (data != null ? data.dataActual.sunsetCalendarToday().get(Calendar.DAY_OF_YEAR) : "null"));
        return frame;
    }

    @Override
    public void onProgressUpdate( Collection<Bitmap> frames0 )
    {
        if (listener != null)
        {
            if (t_data != null) {
                listener.onDataModified(t_data);
                t_data = null;
            }
            LightMapOptions options = lightmap.getOptions();
            if (options != null) {
                Bitmap[] frames = frames0.toArray(new Bitmap[0]);
                for (int i = 0; i < frames.length; i++) {
                    listener.onFrame(frames[i], options.offsetMinutes);
                }
            }
        }
    }

    @Override
    public void onPostExecute( Bitmap lastFrame )
    {
        if (listener != null)
        {
            if (t_data != null) {
                listener.onDataModified(t_data);
                t_data = null;
            }
        }
    }

    @Nullable
    private LightMapTaskListener listener = null;
    public void setListener( @Nullable LightMapTaskListener value ) {
        listener = value;
    }
    public void clearListener() {
        listener = null;
    }

}
