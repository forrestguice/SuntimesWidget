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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

/**
 * LightMapView .. a stacked bar graph over the duration of a day showing relative duration of
 * night, day, and twilight times.
 */
public class LightMapView extends android.support.v7.widget.AppCompatImageView
{
    private static final double MINUTES_IN_DAY = 24 * 60;
    private static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    private static final double ONE_DIVIDED_MILLIS_IN_DAY = 1d / MILLIS_IN_DAY;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LightMapTask drawTask = null;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LightMapColors colors;
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LightMapView(Context context)
    {
        super(context);
        init(context);
    }

    public LightMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    public void setUseMainThread(boolean value) {
        useMainThread = value;
    }
    private boolean useMainThread = false;

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        colors = new LightMapColors(context);
        if (isInEditMode())
        {
            setBackgroundColor(colors.values.getColor(LightMapColorValues.COLOR_NIGHT));
        }
    }

    public int getMaxUpdateRate()
    {
        return maxUpdateRate;
    }

    public void setResizable( boolean value )
    {
        resizable = value;
    }

    /**
     *
     */
    public void onResume()
    {
        //Log.d(LightMapView.class.getSimpleName(), "onResume");
    }

    /**
     * @param w the changed width
     * @param h the changed height
     * @param oldw the previous width
     * @param oldh the previous height
     */
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (resizable)
        {
            //Log.d(LightMapView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LightMapColors getColors() {
        return colors;
    }

    /**
     * themeViews
     */
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (colors == null) {
            colors = new LightMapColors();
        }
        colors.values.setColor(LightMapColorValues.COLOR_NIGHT, theme.getNightColor());
        colors.values.setColor(LightMapColorValues.COLOR_DAY, theme.getDayColor());
        colors.values.setColor(LightMapColorValues.COLOR_ASTRONOMICAL, theme.getAstroColor());
        colors.values.setColor(LightMapColorValues.COLOR_NAUTICAL, theme.getNauticalColor());
        colors.values.setColor(LightMapColorValues.COLOR_CIVIL, theme.getCivilColor());
        colors.values.setColor(LightMapColorValues.COLOR_POINT_FILL, theme.getGraphPointFillColor());
        colors.values.setColor(LightMapColorValues.COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        colors.values.setColor(LightMapColorValues.COLOR_SUN_FILL, theme.getGraphPointFillColor());
        colors.values.setColor(LightMapColorValues.COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
    }

    public void setData(@Nullable SuntimesRiseSetDataset data) {
        this.data = data;
    }

    /**
     * throttled update method
     */
    public void updateViews( boolean forceUpdate )
    {
        long timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);
        if (forceUpdate || timeSinceLastUpdate >= maxUpdateRate)
        {
            updateViews(data);
            lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * @param data an instance of SuntimesRiseSetDataset
     */
    public void updateViews(@Nullable SuntimesRiseSetDataset data)
    {
        setData(data);
        //Log.d("DEBUG", "updateViews: " + data.dataActual.sunsetCalendarToday().get(Calendar.DAY_OF_YEAR));

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            //Log.d("DEBUG", "updateViews: canceling existing task..");
            //Log.w(LightMapView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(getColors().hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } //else Log.d(LightMapView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            Log.w(LightMapView.class.getSimpleName(), "updateViews: width or height 0; skipping update.. :: view-" + Integer.toHexString(getColors().hashCode()));
            return;
        }

        if (useMainThread)
        {
            //Log.d("DEBUG", "updating lightmap on main thread.. " + getWidth() + "x" + getHeight() + " @ " + getNow() + " :: view-" + Integer.toHexString(getColors().hashCode()));
            LightMapTask draw = new LightMapTask(getContext());
            Bitmap b = draw.makeBitmap(data, getWidth(), getHeight(), colors);
            drawTaskListener.onFinished(b);

        } else {
            drawTask = new LightMapTask(getContext());
            drawTask.setListener(drawTaskListener);
            drawTask.execute(data, getWidth(), getHeight(), colors, (animated ? 0 : 1), colors.offsetMinutes);
        }
    }

    private final LightMapTaskListener drawTaskListener = new LightMapTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onStarted: " + Integer.toHexString(getColors().hashCode()));
            if (mapListener != null) {
                mapListener.onStarted();
            }
        }

        @Override
        public void onDataModified(SuntimesRiseSetDataset data) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onDataModified: " + Integer.toHexString(getColors().hashCode()));
            LightMapView.this.data = data;
            if (mapListener != null) {
                mapListener.onDataModified(data);
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onFrame: " + Integer.toHexString(getColors().hashCode()));
            setImageBitmap(frame);
            if (mapListener != null) {
                mapListener.onFrame(frame, offsetMinutes);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: afterFrame: " + Integer.toHexString(getColors().hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onFinished: " + Integer.toHexString(getColors().hashCode()));
            setImageBitmap(frame);
            if (mapListener != null) {
                mapListener.onFinished(frame);
            }
        }
    };

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        //Log.d("DEBUG", "LightMapView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LightMapView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        colors.offsetMinutes = bundle.getLong("offsetMinutes", colors.offsetMinutes);
        colors.now = bundle.getLong("now", colors.now);
    }

    protected boolean saveSettings(Bundle bundle)
    {
        //Log.d(LightMapView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetMinutes", colors.offsetMinutes);
        bundle.putLong("now", colors.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LightMapView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LightMapView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void resetAnimation( final boolean updateTime )
    {
        //Log.d(LightMapView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        colors.offsetMinutes = 0;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                if (updateTime)
                {
                    colors.now = -1;
                    if (data != null) {
                        Calendar calendar = Calendar.getInstance(data.timezone());
                        data.setTodayIs(calendar);
                        data.calculateData(getContext());
                    }
                }

                updateViews(true);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data != null) {
            //Log.d("DEBUG", "onAttachedToWindow: update views :: view-" + Integer.toHexString(getColors().hashCode()));
            updateViews(data);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d("DEBUG", "onDetachedFromWindow: cancel task :: view-" + Integer.toHexString(getColors().hashCode()));
            drawTask.cancel(true);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        //Log.d("DEBUG", "onVisibilityChanged: " + visibility);
        if (visibility != View.VISIBLE && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)
    {
        super.onVisibilityAggregated(isVisible);
        //Log.d("DEBUG", "onVisibilityAggregated: " + isVisible);
        if (!isVisible && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void seekDateTime( Context context, @Nullable Calendar calendar )
    {
        if (calendar != null) {
            seekDateTime(context, calendar.getTimeInMillis());
        }
    }
    public void seekDateTime( Context context, long datetime )
    {
        long offsetMillis = datetime - colors.now;
        colors.offsetMinutes = (offsetMillis / 1000 / 60);
        updateViews(true);
    }
    public Long seekAltitude( Context context, @Nullable Integer degrees, boolean rising )
    {
        if (data != null && degrees != null)
        {
            Long event = findAltitude(context, degrees, rising);
            if (event != null) {
                seekDateTime(context, event);
                return event;
            }
        }
        return null;
    }
    public Long findAltitude( Context context, @Nullable Integer degrees, boolean rising )
    {
        if (data != null && degrees != null)
        {
            Calendar calendar = Calendar.getInstance(data.timezone());
            calendar.setTimeInMillis(colors.now + (colors.offsetMinutes * 60 * 1000));
            Calendar event = rising ? data.calculator().getSunriseCalendarForDate(calendar, degrees)
                    : data.calculator().getSunsetCalendarForDate(calendar, degrees);
            return ((event != null) ? event.getTimeInMillis() : null);
        } else return null;
    }

    public void setOffsetMinutes( long value ) {
        colors.offsetMinutes = value;
        updateViews(true);
    }
    public long getOffsetMinutes() {
        return colors.offsetMinutes;
    }
    public long getNow() {
        return colors.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * LightMapTask
     */
    public static class LightMapTask extends AsyncTask<Object, Bitmap, Bitmap>
    {
        private WeakReference<Context> contextRef;
        private LightMapColors colors;

        private SuntimesRiseSetDataset t_data = null;

        public LightMapTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        /**
         * @param params 0: SuntimesRiseSetDataset,
         *               1: Integer (width),
         *               2: Integer (height)
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
                colors = (LightMapColors)params[3];

                if (params.length > 4) {
                    numFrames = (int)params[4];
                }
                if (params.length > 5) {
                    initialOffset = (long)params[5];
                }
                frameDuration = colors.anim_frameLengthMs * 1000000;   // ms to ns

            } catch (ClassCastException e) {
                Log.w(LightMapTask.class.getSimpleName(), "Invalid params; using [null, 0, 0]");
                return null;
            }

            long time0 = System.nanoTime();
            Bitmap frame = null;
            colors.offsetMinutes = initialOffset;

            int i = 0;
            while (i < numFrames || numFrames <= 0)
            {
                //Log.d(LightMapTask.class.getSimpleName(), "generating frame " + i + " | " + w + "," + h);
                if (isCancelled()) {
                    break;
                }
                colors.acquireDrawLock();

                if (data != null && data.dataActual != null)
                {
                    Calendar maptime = mapTime(data, colors);
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

                frame = makeBitmap(data, w, h, colors);

                long time1 = System.nanoTime();
                while ((time1 - time0) < frameDuration) {
                    time1 = System.nanoTime();
                }

                publishProgress(frame);
                if (listener != null) {
                    listener.afterFrame(frame, colors.offsetMinutes);
                }
                colors.offsetMinutes += colors.anim_frameOffsetMinutes;
                time0 = System.nanoTime();
                i++;
                colors.releaseDrawLock();
            }
            colors.offsetMinutes -= colors.anim_frameOffsetMinutes;

            //Log.d("DEBUG", "doInBackground: done: " + (data != null ? data.dataActual.sunsetCalendarToday().get(Calendar.DAY_OF_YEAR) : "null"));
            return frame;
        }

        public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, LightMapColors colors)
        {
            if (w <= 0 || h <= 0)
            {
                return null;
            }

            if (colors == null)
            {
                return null;
            }

            //long bench_start = System.nanoTime();

            this.colors = colors;
            Calendar now = mapTime(data, colors);
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            boolean layer_astro, layer_nautical, layer_civil;

            // draw background (night)
            p.setColor(colors.values.getColor(LightMapColorValues.COLOR_NIGHT));
            drawRect(c, p);

            if (data != null)
            {
                // draw astro twilight
                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_ASTRONOMICAL));
                if (!(layer_astro = drawRect(colors, data.dataAstro, c, p)))
                {
                    if (data.dataNautical.hasSunriseTimeToday() || data.dataNautical.hasSunsetTimeToday())
                    {
                        drawRect(c, p);
                    }
                }

                // draw nautical twilight
                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_NAUTICAL));
                if (!(layer_nautical = drawRect(colors, data.dataNautical, c, p)))
                {
                    if (data.dataCivil.hasSunriseTimeToday() || data.dataCivil.hasSunsetTimeToday())
                    {
                        drawRect(c, p);
                    }
                }

                // draw civil twilight
                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_CIVIL));
                if (!(layer_civil = drawRect(colors, data.dataCivil, c, p)))
                {
                    if (data.dataActual.hasSunriseTimeToday() || data.dataActual.hasSunsetTimeToday())
                    {
                        drawRect(c, p);
                    }
                }

                // draw foreground (day)
                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_DAY));
                if (!drawRect(colors, data.dataActual, c, p))
                {
                    boolean noLayers = !layer_astro && !layer_nautical && !layer_civil;
                    if (noLayers)
                    {
                        Calendar calendar = data.nowThen(data.dataNoon.calendar());
                        SuntimesCalculator calculator = data.calculator();
                        SuntimesCalculator.SunPosition position = (calculator != null ? calculator.getSunPosition(calendar) : null);

                        if (position == null)
                        {
                            if (calculator != null && calculator.isDay(calendar))
                            {
                                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_DAY));
                                drawRect(c, p);
                            }

                        } else if (position.elevation > 0) {
                            p.setColor(colors.values.getColor(LightMapColorValues.COLOR_DAY));
                            drawRect(c, p);

                        } else if (position.elevation > -6) {
                            p.setColor(colors.values.getColor(LightMapColorValues.COLOR_CIVIL));
                            drawRect(c, p);

                        } else if (position.elevation > -12) {
                            p.setColor(colors.values.getColor(LightMapColorValues.COLOR_NAUTICAL));
                            drawRect(c, p);

                        } else if (position.elevation > -18) {
                            p.setColor(colors.values.getColor(LightMapColorValues.COLOR_ASTRONOMICAL));
                            drawRect(c, p);
                        }
                    }
                }

                // draw now marker
                if (colors.option_drawNow > 0)
                {
                    int pointRadius;
                    if (colors.option_drawNow_pointSizePx <= 0)
                    {
                        pointRadius = (int)Math.ceil(c.getWidth() / (48d * 2d));      // a circle that is 1/2 hr wide
                        int maxPointRadius = (int)(c.getHeight() / 2d);
                        if ((pointRadius + (pointRadius / 3d)) > maxPointRadius) {
                            pointRadius = (maxPointRadius - (pointRadius/3));
                        }
                    } else {
                        pointRadius = colors.option_drawNow_pointSizePx;
                    }
                    int pointStroke = (int)Math.ceil(pointRadius / 3d);

                    //if (colors.option_lmt)
                    //{
                        TimeZone lmt = WidgetTimezones.localMeanTime(null, data.location());
                        Calendar nowLmt = Calendar.getInstance(lmt);
                        nowLmt.setTimeInMillis(now.getTimeInMillis());
                        now = nowLmt;
                    //}

                    switch (colors.option_drawNow) {
                        case LightMapColors.DRAW_SUN2:
                            DashPathEffect dashed = new DashPathEffect(new float[] {4, 2}, 0);
                            drawPoint(now, pointRadius, pointStroke, c, p, Color.TRANSPARENT, colors.values.getColor(LightMapColorValues.COLOR_SUN_STROKE), dashed);
                            break;

                        case LightMapColors.DRAW_SUN1:
                        default:
                            drawPoint(now, pointRadius, pointStroke, c, p, colors.values.getColor(LightMapColorValues.COLOR_SUN_FILL), colors.values.getColor(LightMapColorValues.COLOR_SUN_STROKE), null);
                            break;
                    }
                }
            }

            Bitmap retValue = b;
            if (!colors.option_lmt)    // re-center around noon
            {
                Bitmap b0 = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
                Canvas c0 = new Canvas(b0);

                long zoneOffsetMs = data.timezone().getOffset(now.getTimeInMillis());
                long lonOffsetMs = Math.round(data.location().getLongitudeAsDouble() * MILLIS_IN_DAY / 360d);
                long offsetMs = zoneOffsetMs - lonOffsetMs;

                float left = (float)(offsetMs * ONE_DIVIDED_MILLIS_IN_DAY * w);
                if (left > 0) {
                    c0.drawBitmap(b, left - w, 0, p);
                }
                c0.drawBitmap(b, left, 0, p);
                if (left < 0) {
                    c0.drawBitmap(b, left + w, 0, p);
                }
                retValue = b0;
                b.recycle();
            }

            //long bench_end = System.nanoTime();
            //Log.d("BENCH", "make lightmap :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            return retValue;
        }

        protected Calendar mapTime(@Nullable SuntimesRiseSetDataset data, @NonNull LightMapColors options)
        {
            Calendar mapTime;
            if (options.now >= 0)
            {
                mapTime = Calendar.getInstance(data != null ? data.timezone() : TimeZone.getDefault());
                mapTime.setTimeInMillis(options.now);       // preset time
                //Log.d("DEBUG", "lightmap time: preset: " + mapTime.getTimeInMillis() + " :: view-" + Integer.toHexString(options.hashCode()));

            } else if (data != null) {
                mapTime = data.nowThen(data.calendar());    // the current time (maybe on some other day)
                options.now = mapTime.getTimeInMillis();
                //Log.d("DEBUG", "lightmap time: from data: " + mapTime.getTimeInMillis() + " :: view-" + Integer.toHexString(options.hashCode()));

            } else {
                mapTime = Calendar.getInstance();
                options.now = mapTime.getTimeInMillis();
                //Log.d("DEBUG", "lightmap time: now: " + mapTime.getTimeInMillis() + " :: view-" + Integer.toHexString(options.hashCode()));
            }

            long minutes = options.offsetMinutes;
            while (minutes > Integer.MAX_VALUE) {
                minutes = minutes - Integer.MAX_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MAX_VALUE);
            }
            while (minutes < Integer.MIN_VALUE) {
                minutes = minutes + Integer.MIN_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MIN_VALUE);
            }
            mapTime.add(Calendar.MINUTE, (int)minutes);    // remaining minutes

            return mapTime;
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
                if (t_data != null) {
                    listener.onDataModified(t_data);
                    t_data = null;
                }
                for (int i=0; i<frames.length; i++) {
                    listener.onFrame(frames[i], colors.offsetMinutes);
                }
            }
        }

        @Override
        protected void onPostExecute( Bitmap lastFrame )
        {
            if (isCancelled()) {
                lastFrame = null;
            }
            if (listener != null)
            {
                if (t_data != null) {
                    listener.onDataModified(t_data);
                    t_data = null;
                }
                listener.onFinished(lastFrame);
            }
        }

        /////////////////////////////////////////////

        protected void drawRect(Canvas c, Paint p)
        {
            int w = c.getWidth();
            int h = c.getHeight();
            c.drawRect(0, 0, w, h, p);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        protected boolean drawRect( LightMapColors options, SuntimesRiseSetData data, Canvas c, Paint p )
        {
            Calendar riseTime = data.sunriseCalendarToday();
            Calendar setTime = data.sunsetCalendarToday();
            if (riseTime == null && setTime == null)
            {
                return false;
            }

            //if (options.option_lmt)
            //{
                TimeZone lmt = WidgetTimezones.localMeanTime(null, data.location());
                if (riseTime != null)
                {
                    Calendar riseTimeLmt = Calendar.getInstance(lmt);
                    riseTimeLmt.setTimeInMillis(riseTime.getTimeInMillis());
                    riseTime = riseTimeLmt;
                }
                if (setTime != null)
                {
                    Calendar setTimeLmt = Calendar.getInstance(lmt);
                    setTimeLmt.setTimeInMillis(setTime.getTimeInMillis());
                    setTime = setTimeLmt;
                }
            //}

            int w = c.getWidth();
            int h = c.getHeight();

            int left = 0;
            if (riseTime != null)
            {
                int dayDiff = riseTime.get(Calendar.DAY_OF_YEAR) - data.calendar().get(Calendar.DAY_OF_YEAR);  // average case: 0; edge cases: -1, 1
                double riseMinute = riseTime.get(Calendar.HOUR_OF_DAY) * 60 + riseTime.get(Calendar.MINUTE);
                double riseR = ((dayDiff * 60 * 24) + riseMinute) / MINUTES_IN_DAY;
                if (riseR > 1) {
                    riseR = 1;
                } else if (riseR < 0) {
                    riseR = 0;
                }
                left = (int) Math.round(riseR * w);
            }

            int right = w;
            if (setTime != null)
            {
                int dayDiff = setTime.get(Calendar.DAY_OF_YEAR) - data.calendar().get(Calendar.DAY_OF_YEAR);  // average case: 0; edge cases: -1, 1
                double setMinute = setTime.get(Calendar.HOUR_OF_DAY) * 60 + setTime.get(Calendar.MINUTE);
                double setR = ((dayDiff * 60 * 24) + setMinute) / MINUTES_IN_DAY;
                if (setR > 1) {
                    setR = 1;
                } else if (setR < 0) {
                    setR = 0;
                }
                right = (int) Math.round(setR * w);
            }

            boolean setTimeBeforeRiseTime = (riseTime != null && setTime != null && setTime.getTime().before(riseTime.getTime()));
            if (setTimeBeforeRiseTime)
            {
                c.drawRect(0, 0, right, h, p);
                c.drawRect(left, 0, w, h, p);

            } else {
                c.drawRect(left, 0, right, h, p);
            }
            return true;
        }

        protected void drawPoint(Calendar calendar, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            if (calendar != null)
            {
                int w = c.getWidth();
                int h = c.getHeight();

                double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int x = (int) Math.round((minute / MINUTES_IN_DAY) * w);
                int y = h / 2;
                drawPoint(x, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);

                if (x + radius > w) {    // point cropped at image bounds, so translate and draw it again
                    drawPoint(x - w, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
                } else if (x - radius < 0) {
                    drawPoint(x + w, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
                }
            }
        }
        protected void drawPoint(int x, int y, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            p.setStyle(Paint.Style.FILL);
            p.setColor(fillColor);
            c.drawCircle(x, y, radius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(strokeWidth);
            p.setColor(strokeColor);

            if (strokeEffect != null) {
                p.setPathEffect(strokeEffect);
            }

            c.drawCircle(x, y, radius, p);
        }

        private LightMapTaskListener listener = null;
        public void setListener( LightMapTaskListener value ) {
            listener = value;
        }
        public void clearListener() {
            listener = null;
        }
    }

    /**
     * LightMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class LightMapTaskListener
    {
        public void onStarted() {}
        public void onDataModified( SuntimesRiseSetDataset data ) {}
        public void onFrame(Bitmap frame, long offsetMinutes ) {}
        public void afterFrame(Bitmap frame, long offsetMinutes ) {}
        public void onFinished( Bitmap result ) {}
    }

    private LightMapTaskListener mapListener = null;
    public void setMapTaskListener( LightMapTaskListener listener ) {
        mapListener = listener;
    }

    /**
     * LightMapColors
     */
    @SuppressWarnings("WeakerAccess")
    public static class LightMapColors
    {
        public static final int DRAW_NONE = 0;
        public static final int DRAW_SUN1 = 1;    // solid stroke
        public static final int DRAW_SUN2 = 2;    // dashed stroke

        public int option_drawNow = DRAW_SUN1;
        public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size
        public boolean option_lmt = false;

        public long offsetMinutes = 0;
        public long now = -1L;
        public int anim_frameLengthMs = 100;         // frames shown for 200 ms
        public int anim_frameOffsetMinutes = 1;      // each frame 1 minute apart
        public Lock anim_lock = null;

        public LightMapColorValues values;

        public LightMapColors() {
            values = new LightMapColorValues();
        }

        @SuppressWarnings("ResourceType")
        public LightMapColors(Context context) {
            init(context);
        }

        public void initDefaultDark(Context context) {
            values = new LightMapColorValues(values.getDefaultValues(context, true));
        }

        public void initDefaultLight(Context context) {
            values = new LightMapColorValues(values.getDefaultValues(context, false));
        }

        public void init(Context context) {
            values = new LightMapColorValues(context);
        }

        public void acquireDrawLock()
        {
            if (anim_lock != null) {
                anim_lock.lock();
                //Log.d("DEBUG", "MapView :: acquire " + anim_lock);
            }
        }
        public void releaseDrawLock()
        {
            if (anim_lock != null) {
                //Log.d("DEBUG", "MapView :: release " + anim_lock);
                anim_lock.unlock();
            }
        }
    }

    public static CharSequence getLabel(Context context, SuntimesRiseSetDataset data)
    {
        if (data.dayLength() <= 0) {
            return context.getString(R.string.timeMode_polarnight);

        } else if (data.nightLength() == 0) {
            if (data.civilTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnightsun);

            } else if (data.nauticalTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnighttwilight_whitenight);

            } else if (data.astroTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnighttwilight);

            } else {
                return context.getString(R.string.timeMode_midnighttwilight);
            }
        }
        return null;
    }
}
