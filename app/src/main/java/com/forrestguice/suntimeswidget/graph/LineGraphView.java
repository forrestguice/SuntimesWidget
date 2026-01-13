/**
    Copyright (C) 2022 Forrest Guice
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
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.ImageView;
import com.forrestguice.util.android.AndroidResources;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

/**
 * LineGraphView
 */
public class LineGraphView extends ImageView
{
    public static final int MINUTES_IN_DAY = 24 * 60;
    public static final double MINUTES_IN_DAY_RATIO = 1d / (24 * 60);

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LineGraphTask drawTask = null;

    private final int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LineGraphOptions options;
    @Nullable
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LineGraphView(Context context)
    {
        super(context);
        init(context);
    }

    public LineGraphView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        options = new LineGraphOptions(context);
        if (isInEditMode())
        {
            setBackgroundColor(options.getColor(LineGraphColorValues.COLOR_GRAPH_BG));
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
        //Log.d(LineGraphView.class.getSimpleName(), "onResume");
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
            Log.d(LineGraphView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LineGraphOptions getOptions() {
        return options;
    }

    /**
     * themeViews
     */
    @Deprecated
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (options == null) {
            options = new LineGraphOptions(context);
        }
        options.colors.setColor(LineGraphColorValues.COLOR_GRAPH_BG, theme.getNightColor());
        options.colors.setColor(LineGraphColorValues.COLOR_POINT_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(LineGraphColorValues.COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        options.colors.setColor(LineGraphColorValues.COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(LineGraphColorValues.COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
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

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            Log.w(LineGraphView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(LineGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } // else Log.d(LineGraphView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LineGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        drawTask = new LineGraphTask(getContext());
        drawTask.setListener(drawTaskListener);

        if (Build.VERSION.SDK_INT >= 11) {
            drawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetMinutes);
        } else {
            drawTask.execute(data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetMinutes);
        }
    }

    private final LineGraphTaskListener drawTaskListener = new LineGraphTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onStarted: " + Integer.toHexString(LineGraphView.this.hashCode()));
            if (graphListener != null) {
                graphListener.onStarted();
            }
        }

        @Override
        public void onDataModified(SuntimesRiseSetDataset data) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onDataModified: " + Integer.toHexString(LineGraphView.this.hashCode()));
            LineGraphView.this.data = data;
            if (graphListener != null) {
                graphListener.onDataModified(data);
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onFrame: " + Integer.toHexString(LineGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFrame(frame, offsetMinutes);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: afterFrame: " + Integer.toHexString(LineGraphView.this.hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onFinished: " + Integer.toHexString(LineGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFinished(frame);
            }
        }
    };

    /**
     * @param context a context used to access shared prefs
     */
    public void loadSettings(Context context)
    {
        //Log.d("DEBUG", "LineGraphView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetMinutes = bundle.getLong("offsetMinutes", options.offsetMinutes);
        options.now = bundle.getLong("now", options.now);
    }

    public boolean saveSettings(Bundle bundle)
    {
        //Log.d(LineGraphView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetMinutes", options.offsetMinutes);
        bundle.putLong("now", options.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LineGraphView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LineGraphView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void resetAnimation( final boolean updateTime )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        options.offsetMinutes = 0;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                if (updateTime)
                {
                    options.now = -1;
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
            //Log.d(LineGraphView.class.getSimpleName(), "onAttachedToWindow: update views " + data);
            updateViews(data);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d(LineGraphView.class.getSimpleName(), "onDetachedFromWindow: cancel task " + Integer.toHexString(LineGraphView.this.hashCode()));
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
    public void onVisibilityAggregated(boolean isVisible)    // TODO: only called for api 24+ ?
    {
        super.onVisibilityAggregated(isVisible);
        //Log.d("DEBUG", "onVisibilityAggregated: " + isVisible);
        if (!isVisible && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void seekDateTime( Context context, long datetime )
    {
        long offsetMillis = datetime - options.now;
        options.offsetMinutes = (offsetMillis / 1000 / 60);
        updateViews(true);
    }

    public void setOffsetMinutes( long value ) {
        options.offsetMinutes = value;
        updateViews(true);
    }
    public long getOffsetMinutes() {
        return options.offsetMinutes;
    }
    public long getNow() {
        return options.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * LineGraphTask
     */
    public static class LineGraphTask extends AsyncTask<Object, Bitmap, Bitmap>
    {
        private final WeakReference<Context> contextRef;
        private LineGraphOptions options;

        @Nullable
        private SuntimesRiseSetDataset t_data = null;

        public LineGraphTask(Context context) {
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
                options = (LineGraphOptions)params[3];

                if (params.length > 4) {
                    numFrames = (int)params[4];
                }
                if (params.length > 5) {
                    initialOffset = (long)params[5];
                }
                frameDuration = options.anim_frameLengthMs * 1000000;   // ms to ns

            } catch (ClassCastException e) {
                Log.w(LineGraphTask.class.getSimpleName(), "Invalid params; using [null, 0, 0]");
                return null;
            }

            long time0 = System.nanoTime();
            Bitmap frame = null;
            options.offsetMinutes = initialOffset;

            int i = 0;
            while (i < numFrames || numFrames <= 0)
            {
                //Log.d(getClass().getSimpleName(), "generating frame " + i + " | " + w + "," + h + " :: " + numFrames);
                if (isCancelled()) {
                    break;
                }
                options.acquireDrawLock();

                if (data != null && data.dataActual != null)
                {
                    Calendar maptime = graphTime(data, options);
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
                options.releaseDrawLock();
            }
            options.offsetMinutes -= options.anim_frameOffsetMinutes;
            return frame;
        }

        public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, LineGraphOptions options )
        {
            long bench_start = System.nanoTime();

            if (w <= 0 || h <= 0) {
                return null;
            }
            if (options == null) {
                return null;
            }

            this.options = options;
            Calendar now = graphTime(data, options);
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            b.setDensity(options.densityDpi);
            Canvas c = new Canvas(b);
            initPaint();

            drawBackground(c, paintBackground, options);
            if (data != null)
            {
                options.location = data.location();

                /*Calendar lmt = Calendar.getInstance(tzLmt(options.location));
                lmt.setTimeInMillis(now.getTimeInMillis());
                options.graph_x_offset = (lmt.get(Calendar.HOUR_OF_DAY) * 60 + lmt.get(Calendar.MINUTE)) / 2d;
                options.graph_width = MINUTES_IN_DAY;
                options.graph_height = 180;
                options.graph_y_offset = 0;*/

                drawGrid(now, data, c, p, options);
                drawAxisUnder(now, data, c, p, options);
                drawPaths(now, data.calculator(), c, p, options);
                drawAxisOver(now, data, c, p, options);
                drawLabels(now, data, c, paintText, options);
                drawNow(now, data.calculator(), c, p, options);
            }

            long bench_end = System.nanoTime();
            //Log.d("BENCH", "make line graph :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            return b;
        }
        protected void initPaint()
        {
            if (p == null) {
                p = new Paint(Paint.ANTI_ALIAS_FLAG);
            }

            if (paintBackground == null)
            {
                paintBackground = new Paint();
                paintBackground.setStyle(Paint.Style.FILL);
            }

            if (paintText == null)
            {
                paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintText.setAntiAlias(true);
                paintText.setTextAlign(Paint.Align.CENTER);
                paintText.setStyle(Paint.Style.FILL);
                paintText.setTypeface(Typeface.DEFAULT_BOLD);
            }

            if (paintPath == null)
            {
                paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintPath.setAntiAlias(true);
            }
        }
        private Paint p, paintBackground, paintText, paintPath;

        protected Calendar graphTime(@Nullable SuntimesRiseSetDataset data, @NonNull LineGraphOptions options)
        {
            Calendar mapTime;
            if (options.now >= 0)
            {
                mapTime = Calendar.getInstance(data != null ? data.timezone() : TimeZone.getDefault());
                mapTime.setTimeInMillis(options.now);       // preset time

            } else if (data != null) {
                mapTime = data.nowThen(data.calendar());    // the current time (maybe on some other day)
                options.now = mapTime.getTimeInMillis();

            } else {
                mapTime = Calendar.getInstance();
                options.now = mapTime.getTimeInMillis();
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

        /**
         * @param c canvas (bitmap dimensions)
         * @param minute local mean time (noon @ 12*60 hw)
         * @return x bitmap coordinate
         */
        protected double minutesToBitmapCoords(Canvas c, double minute, LineGraphOptions options) {
            return Math.round((minute / options.graph_width) * c.getWidth() - (options.graph_x_offset / options.graph_width) * c.getWidth());
        }
        /*protected double timeToBitmapCoords(Canvas c, long timestamp, LineGraphOptions options)
        {
            if (tz_lmt == null) {
                tz_lmt = WidgetTimezones.localMeanTime(null, options.location);
            }
            Calendar lmt = Calendar.getInstance(tz_lmt);
            lmt.setTimeInMillis(timestamp);
            int minute = lmt.get(Calendar.HOUR_OF_DAY) * 60 + lmt.get(Calendar.MINUTE);
            return minutesToBitmapCoords(c, minute, options);
        }*/
        /*private TimeZone tz_lmt = null;
        public TimeZone tzLmt(Location location) {
            if (tz_lmt == null) {
                tz_lmt = WidgetTimezones.localMeanTime(null, location);
            }
            return tz_lmt;
        }*/

        /**
         * @param c canvas (bitmap dimensions)
         * @param degrees [-90, 90]
         * @return y bitmap coordinate
         */
        protected double degreesToBitmapCoords(Canvas c, double degrees, LineGraphOptions options)
        {
            while (degrees > 90) {
                degrees -= 90;
            }
            while (degrees < -90) {
                degrees += 90;
            }
            int h = c.getHeight();
            float cY = h / 2f;
            return cY - Math.round((degrees / (options.graph_height)) * h) + Math.round((options.graph_y_offset / (options.graph_height)) * h);
        }

        protected void drawBackground(Canvas c, Paint p, LineGraphOptions options)
        {
            p.setColor(options.getColor(LineGraphColorValues.COLOR_GRAPH_BG));
            drawRect(c, p);
        }

        protected void drawNow(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.option_drawNow > 0)
            {
                int pointRadius;
                if ((options.option_drawNow_pointSizePx <= 0)) {
                    pointRadius = (int)Math.ceil(c.getWidth() / (48d * 2d));      // a circle that is 1/2 hr wide
                    int maxPointRadius = (int) (c.getHeight() / 16d);
                    if (pointRadius + (pointRadius / 3d) > maxPointRadius) {
                        pointRadius = maxPointRadius;
                    }
                } else {
                    pointRadius = options.option_drawNow_pointSizePx;
                }
                int pointStroke = (int)Math.ceil(pointRadius / 3d);

                switch (options.option_drawNow) {
                    case LineGraphOptions.DRAW_SUN2:
                        DashPathEffect dashed = new DashPathEffect(new float[] {4, 2}, 0);
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, Color.TRANSPARENT, options.getColor(LineGraphColorValues.COLOR_SUN_STROKE), dashed);
                        break;

                    case LineGraphOptions.DRAW_SUN1:
                    default:
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, options.getColor(LineGraphColorValues.COLOR_SUN_FILL), options.getColor(LineGraphColorValues.COLOR_SUN_STROKE), null);
                        break;
                }
            }
        }

        protected void drawSunPathPoints(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.sunPath_show_points && options.sunPath_points_elevations != null)
            {
                double pointSize = Math.sqrt(c.getWidth() * c.getHeight()) / options.sunPath_points_width;
                for (double degrees : options.sunPath_points_elevations)
                {
                    Integer[] minutes = findMinutes(now, degrees, calculator);
                    if (minutes != null) {
                        for (Integer m : minutes) {
                            drawPoint(m, degrees, (int)pointSize, 0, c, p, options.colors.getColor(LineGraphColorValues.COLOR_POINT_FILL), options.colors.getColor(LineGraphColorValues.COLOR_POINT_STROKE), null);
                        }
                    }
                }
            }
        }

        @Nullable
        protected Integer[] findMinutes(Calendar now, double degrees, SuntimesCalculator calculator)
        {
            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(now.getTimeInMillis());
            lmt = toStartOfDay(lmt);
            long startMillis = lmt.getTimeInMillis();
            lmt = toNoon(lmt);
            long midMillis = lmt.getTimeInMillis();
            lmt = toEndOfDay(lmt);
            long endMillis = lmt.getTimeInMillis();

            ArrayList<Integer> results = new ArrayList<>();
            Long[] millis = new Long[2];
            millis[0] = findMillis(degrees, calculator, lmt, startMillis, midMillis-1, true);
            millis[1] = findMillis(degrees, calculator, lmt, midMillis+1, endMillis, false);

            for (Long time : millis) {
                if (time != null) {
                    lmt.setTimeInMillis(time);
                    results.add((lmt.get(Calendar.HOUR_OF_DAY) * 60) + lmt.get(Calendar.MINUTE));
                }
            }
            return ((results.size() > 0) ? results.toArray(new Integer[0]) : null);
        }

        @Nullable
        protected Long findMillis(double degrees, SuntimesCalculator calculator, Calendar lmt, long minMillis, long maxMillis, boolean ascending)
        {
            if (minMillis > maxMillis) {
                return null;
            }

            lmt.setTimeInMillis(minMillis + ((maxMillis - minMillis) / 2));
            SuntimesCalculator.SunPosition position = calculator.getSunPosition(lmt);

            if (Math.abs(position.elevation - degrees) < 0.25) {
                return lmt.getTimeInMillis();

            } else if ((ascending && (degrees > position.elevation))
                    || (!ascending && (degrees < position.elevation))) {
                return findMillis(degrees, calculator, lmt, lmt.getTimeInMillis() + 1, maxMillis, ascending);

            } else {
                return findMillis(degrees, calculator, lmt, minMillis, lmt.getTimeInMillis() - 1, ascending);
            }
        }

        public static Calendar toStartOfDay(Calendar calendar)
        {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }
        public static Calendar toNoon(Calendar calendar)
        {
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }
        public static Calendar toEndOfDay(Calendar calendar)
        {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar;
        }

        protected void drawPaths(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.moonPath_show_line) {
                drawMoonPath(now, calculator, c, paintPath, options);
            }
            if (options.sunPath_show_line)
            {
                drawSunPath(now, calculator, c, paintPath, options);
                drawSunPathPoints(now, calculator, c, p, options);
            }
        }

        private final ArrayList<Path> sun_paths = new ArrayList<>(), moon_paths = new ArrayList<>();
        private final HashMap<Path, Double> sun_elevations = new HashMap<>(), moon_elevations = new HashMap<>();

        protected void drawMoonPath(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.moonPath_show_fill)
            {
                HashMap<Path, Double> moonFill = createMoonPath(now, calculator, c, options, true, moon_paths, moon_elevations);
                p.setStyle(Paint.Style.FILL);
                for (Path path : moonFill.keySet())
                {
                    boolean isDay = (moonFill.get(path) >= 0);
                    p.setColor(isDay ? options.getColor(LineGraphColorValues.COLOR_MOONPATH_DAY_FILL) : options.getColor(LineGraphColorValues.COLOR_MOONPATH_NIGHT_FILL));
                    p.setAlpha(isDay ? options.moonPath_color_day_closed_alpha : options.moonPath_color_night_closed_alpha);
                    c.drawPath(path, p);
                }
            }

            if (options.moonPath_show_line)
            {
                double r = Math.sqrt(c.getWidth() * c.getHeight());
                HashMap<Path, Double> moonPath = createMoonPath(now, calculator, c, options, false, moon_paths, moon_elevations);
                p.setStrokeWidth((float)(r / (float)options.moonPath_width));
                p.setStyle(Paint.Style.STROKE);
                for (Path path : moonPath.keySet())
                {
                    boolean isDay = (moonPath.get(path) >= 0);
                    p.setColor(isDay ? options.getColor(LineGraphColorValues.COLOR_MOONPATH_DAY_STROKE) : options.getColor(LineGraphColorValues.COLOR_MOONPATH_NIGHT_STROKE));
                    p.setAlpha(255);
                    c.drawPath(path, p);
                }
            }
        }
        protected HashMap<Path, Double> createMoonPath(Calendar now, SuntimesCalculator calculator, Canvas c, LineGraphOptions options, boolean closed, ArrayList<Path> paths, HashMap<Path,Double> elevations)
        {
            int path_width = 2 * MINUTES_IN_DAY ; // options.graph_width;

            paths.clear();
            elevations.clear();

            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(now.getTimeInMillis());
            toStartOfDay(lmt);

            SuntimesCalculator.MoonPosition position;
            double elevation_prev = -90;   // sun elevation (previous iteration)
            elevation_min = elevation_max = 0;
            float x = 0, y = 0;

            Path path = null;
            int minute = 0;
            while (minute < path_width)
            {
                position = calculator.getMoonPosition(lmt);
                double elevation = (position != null ? position.elevation : 0);

                if (elevation < elevation_min) {
                    elevation_min = elevation;
                } else if (elevation > elevation_max) {
                    elevation_max = elevation;
                }

                int d = (int)(minute / MINUTES_IN_DAY);
                double m = (d * MINUTES_IN_DAY) + (lmt.get(Calendar.HOUR_OF_DAY) * 60) + lmt.get(Calendar.MINUTE);
                x = (float) minutesToBitmapCoords(c, m, options);
                y = (float) degreesToBitmapCoords(c, elevation, options);

                if (path != null
                        && ((elevation_prev < 0 && elevation >= 0)
                        || (elevation_prev >= 0 && elevation < 0))) {
                    path.lineTo(x, y);
                    if (closed) {
                        path.close();
                    }
                    path = null;
                }

                if (path == null)
                {
                    path = new Path();
                    paths.add(path);
                    elevations.put(path, elevation);

                    if (closed) {
                        path.moveTo(x, (float)degreesToBitmapCoords(c, 0, options));
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                    }

                } else {
                    path.lineTo(x, y);
                }

                elevation_prev = elevation;
                lmt.add(Calendar.MINUTE, options.moonPath_interval);
                minute += options.moonPath_interval;
            }

            if (closed)
            {
                path = paths.get(paths.size()-1);
                path.lineTo(x, (float)degreesToBitmapCoords(c, 0, options));
                path.close();
            }
            return elevations;
        }


        protected void drawSunPath(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.sunPath_show_fill)
            {
                HashMap<Path, Double> sunFill = createSunPath(now, calculator, c, options, true, sun_paths, sun_elevations);
                p.setStyle(Paint.Style.FILL);
                for (Path path : sunFill.keySet())
                {
                    boolean isDay = (sunFill.get(path) >= 0);
                    p.setColor(isDay ? options.getColor(LineGraphColorValues.COLOR_SUNPATH_DAY_FILL) : options.getColor(LineGraphColorValues.COLOR_SUNPATH_NIGHT_FILL));
                    p.setAlpha(isDay ? options.sunPath_color_day_closed_alpha : options.sunPath_color_night_closed_alpha);
                    c.drawPath(path, p);
                }
            }

            if (options.sunPath_show_line)
            {
                double r = Math.sqrt(c.getWidth() * c.getHeight());
                HashMap<Path, Double> sunPath = createSunPath(now, calculator, c, options, false, sun_paths, sun_elevations);
                p.setStrokeWidth((float)(r / (float)options.sunPath_width));
                p.setStyle(Paint.Style.STROKE);
                for (Path path : sunPath.keySet())
                {
                    boolean isDay = (sunPath.get(path) >= 0);
                    p.setColor(isDay ? options.getColor(LineGraphColorValues.COLOR_SUNPATH_DAY_STROKE) : options.getColor(LineGraphColorValues.COLOR_SUNPATH_NIGHT_STROKE));
                    p.setAlpha(255);
                    c.drawPath(path, p);
                }
            }
        }

        private double elevation_min = -90, elevation_max = 90;
        protected HashMap<Path, Double> createSunPath(Calendar now, SuntimesCalculator calculator, Canvas c, LineGraphOptions options, boolean closed, ArrayList<Path> paths, HashMap<Path,Double> elevations)
        {
            int path_width = 2 * MINUTES_IN_DAY ; // options.graph_width;

            paths.clear();
            elevations.clear();

            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(now.getTimeInMillis());
            toStartOfDay(lmt);

            SuntimesCalculator.SunPosition position;
            double elevation_prev = -90;   // sun elevation (previous iteration)
            elevation_min = elevation_max = 0;
            float x = 0, y = 0;

            Path path = null;
            int minute = 0;
            while (minute < path_width)
            {
                position = calculator.getSunPosition(lmt);
                double elevation = (position != null ? position.elevation : 0);

                if (elevation < elevation_min) {
                    elevation_min = elevation;
                } else if (elevation > elevation_max) {
                    elevation_max = elevation;
                }

                int d = (int)(minute / MINUTES_IN_DAY);
                double m = (d * MINUTES_IN_DAY) + (lmt.get(Calendar.HOUR_OF_DAY) * 60) + lmt.get(Calendar.MINUTE);
                x = (float) minutesToBitmapCoords(c, m, options);
                y = (float) degreesToBitmapCoords(c, elevation, options);

                if (path != null
                        && ((elevation_prev < 0 && elevation >= 0)
                        || (elevation_prev >= 0 && elevation < 0))) {
                    path.lineTo(x, y);
                    if (closed) {
                        path.close();
                    }
                    path = null;
                }

                if (path == null)
                {
                    path = new Path();
                    paths.add(path);
                    elevations.put(path, elevation);

                    if (closed) {
                        path.moveTo(x, (float)degreesToBitmapCoords(c, 0, options));
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                    }

                } else {
                    path.lineTo(x, y);
                }

                elevation_prev = elevation;
                lmt.add(Calendar.MINUTE, options.sunPath_interval);
                minute += options.sunPath_interval;
            }

            if (closed)
            {
                path = paths.get(paths.size()-1);
                path.lineTo(x, (float)degreesToBitmapCoords(c, 0, options));
                path.close();
            }
            return elevations;
        }
        protected void closePaths(List<Path> paths)
        {
            for (Path path : paths) {
                path.close();
            }
        }

        protected void drawLabels(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.axisX_labels_show) {
                drawAxisXLabels(now, data, c, p, options);
            }
            if (options.axisY_labels_show) {
                drawAxisYLabels(c, p, options);
            }
        }

        protected void drawAxisUnder(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.axisY_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.getColor(LineGraphColorValues.COLOR_AXIS));
                p.setStrokeWidth((float)(c.getWidth() * options.axisY_width));
                drawAxisY(now, data, c, p, options);
            }
        }
        protected void drawAxisOver(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.axisX_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.getColor(LineGraphColorValues.COLOR_AXIS));
                p.setStrokeWidth((float)(c.getWidth() * options.axisX_width));
                drawAxisX(c, p, options);
            }
        }

        protected void drawGrid(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            double w = c.getWidth();
            if (options.gridX_minor_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(w * options.gridX_minor_width));
                p.setColor(options.getColor(LineGraphColorValues.COLOR_GRID_MINOR));
                drawGridX(c, p, options.gridX_minor_interval, options);
            }
            if (options.gridY_minor_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(w * options.gridY_minor_width));
                p.setColor(options.getColor(LineGraphColorValues.COLOR_GRID_MINOR));
                drawGridY(now, data, c, p, options.gridY_minor_interval, options);
            }
            if (options.gridX_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(w * options.gridX_major_width));
                p.setColor(options.getColor(LineGraphColorValues.COLOR_GRID_MAJOR));
                drawGridX(c, p, options.gridX_major_interval, options);
            }
            if (options.gridY_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(w * options.gridY_major_width));
                p.setColor(options.getColor(LineGraphColorValues.COLOR_GRID_MAJOR));
                drawGridY(now, data, c, p, options.gridY_major_interval, options);
            }
        }

        protected void drawAxisX(Canvas c, Paint p, LineGraphOptions options)
        {
            int w = c.getWidth();
            float y0 = (float) degreesToBitmapCoords(c, 0, options);
            c.drawLine(0, y0, w, y0, p);
        }
        protected void drawAxisY(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            int h = c.getHeight();

            TimeZone tz = (options.timezone != null) ? options.timezone : now.getTimeZone();
            Calendar calendar = Calendar.getInstance(tz);
            calendar.setTimeInMillis(graphTime(data, options).getTimeInMillis());
            toStartOfDay(calendar);
            int dstOffsetMin = tz.getDSTSavings() / (1000 * 60);
            boolean inDst0 = tz.inDaylightTime(calendar.getTime());

            Calendar lmt = lmt(data.location());
            lmt.setTimeInMillis(calendar.getTimeInMillis());

            int hours = lmt.get(Calendar.HOUR_OF_DAY);
            int i = (hours * 60) + lmt.get(Calendar.MINUTE) - (hours > 0 ? MINUTES_IN_DAY : 0);
            while (i <= MINUTES_IN_DAY)
            {
                boolean inDst = tz.inDaylightTime(calendar.getTime());
                if (inDst != inDst0) {
                    i += (inDst ? -1 : 1) * dstOffsetMin;
                    calendar.add(Calendar.MINUTE, (inDst ? -1 : 1) * dstOffsetMin);
                }
                inDst0 = inDst;

                float x = (float) minutesToBitmapCoords(c, i, options);
                c.drawLine(x, 0, x, h, p);
                i += options.axisY_interval;
                calendar.add(Calendar.MINUTE, (int) options.axisY_interval);
            }
        }

        protected void drawAxisXLabels(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, LineGraphOptions options)
        {
            int h = c.getHeight();
            float textSize = textSize(c, options.axisX_labels_textsize_ratio);

            TimeZone tz = (options.timezone != null) ? options.timezone : now.getTimeZone();
            Calendar calendar = Calendar.getInstance(tz);
            calendar.setTimeInMillis(graphTime(data, options).getTimeInMillis());
            toStartOfDay(calendar);
            int dstOffsetMin = tz.getDSTSavings() / (1000 * 60);
            boolean inDst0 = tz.inDaylightTime(calendar.getTime());

            Calendar lmt = lmt(data.location());
            lmt.setTimeInMillis(calendar.getTimeInMillis());

            int hours = lmt.get(Calendar.HOUR_OF_DAY);
            int i = (hours * 60) + lmt.get(Calendar.MINUTE) - (hours > 0 ? MINUTES_IN_DAY : 0);
            while (i <= MINUTES_IN_DAY)
            {
                boolean inDst = tz.inDaylightTime(calendar.getTime());
                if (inDst != inDst0) {
                    i += (inDst ? -1 : 1) * dstOffsetMin;
                    calendar.add(Calendar.MINUTE, (inDst ? -1 : 1) * dstOffsetMin);
                }
                inDst0 = inDst;

                int hourLabel = (options.is24 ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR));
                if (!options.is24 && hourLabel == 0) {
                    hourLabel = 12;
                }

                float x = (float) minutesToBitmapCoords(c, i, options);
                p.setColor(options.getColor(LineGraphColorValues.COLOR_LABELS));
                p.setTextSize((float) textSize);
                c.drawText(hourLabel + "", x - textSize/2, h - textSize/4, p);
                i += options.axisX_labels_interval;
                calendar.add(Calendar.MINUTE, (int) options.axisX_labels_interval);
            }
        }
        protected void drawAxisYLabels(Canvas c, Paint p, LineGraphOptions options)
        {
            float textSize = textSize(c, options.axisY_labels_textsize_ratio);
            int i = -1 * (int) options.axisY_labels_interval;
            while (i < 90)
            {
                float y = (float) degreesToBitmapCoords(c, i, options);
                p.setColor(options.getColor(LineGraphColorValues.COLOR_LABELS));
                p.setTextSize((float)textSize);
                c.drawText((i > 0 ? "+" : "") + i + "°", 0 + (float)(1.25 * textSize), y + textSize/3 , p);
                i += options.axisY_labels_interval;
            }
        }

        protected float textSize(Canvas c, float ratio)
        {
            //int s = (int)((c.getWidth() + c.getHeight()) / 2d);
            //return (float)(Math.sqrt(s * (s/2d)) / ratio);
            //return (float)(Math.sqrt(c.getWidth() * c.getHeight()) / ratio);
            return (float)(c.getHeight() / ratio);
        }

        protected void drawGridX(Canvas c, Paint p, float interval, LineGraphOptions options)
        {
            int degreeMin = -90;
            int degreeMax = 90;

            int w = c.getWidth();
            int i = degreeMin;
            while (i < degreeMax)
            {
                float y = (float) degreesToBitmapCoords(c, i, options);
                c.drawLine(0, y, w, y, p);
                i += interval;
            }
        }

        private Calendar lmt = null;
        private Calendar lmt(Location location)
        {
            if (lmt == null) {
                lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(location));
            }
            return lmt;
        }

        protected void drawGridY(Calendar now, SuntimesRiseSetDataset data, Canvas c, Paint p, float interval, LineGraphOptions options)
        {
            TimeZone tz = (options.timezone != null) ? options.timezone : now.getTimeZone();
            Calendar calendar = Calendar.getInstance(tz);
            calendar.setTimeInMillis(graphTime(data, options).getTimeInMillis());
            toStartOfDay(calendar);
            int dstOffsetMin = tz.getDSTSavings() / (1000 * 60);
            boolean inDst0 = tz.inDaylightTime(calendar.getTime());

            Calendar lmt = lmt(data.location());
            lmt.setTimeInMillis(calendar.getTimeInMillis());

            int h = c.getHeight();
            int hours = lmt.get(Calendar.HOUR_OF_DAY);
            int i = (hours * 60) + lmt.get(Calendar.MINUTE) - (hours > 0 ? MINUTES_IN_DAY : 0);
            while (i < MINUTES_IN_DAY)
            {
                boolean inDst = tz.inDaylightTime(calendar.getTime());
                if (inDst != inDst0) {
                    i += (inDst ? -1 : 1) * dstOffsetMin;
                    calendar.add(Calendar.MINUTE, (inDst ? -1 : 1) * dstOffsetMin);
                }
                inDst0 = inDst;

                float x = (float) minutesToBitmapCoords(c, i, options);
                c.drawLine(x, 0, x, h, p);
                i += interval;
                calendar.add(Calendar.MINUTE, (int) interval);
            }
        }

        protected void drawRect(Canvas c, Paint p)
        {
            int w = c.getWidth();
            int h = c.getHeight();
            c.drawRect(0, 0, w, h, p);
        }

        protected void drawPoint(Calendar calendar, SuntimesCalculator calculator, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            if (calendar != null) {
                drawPoint(calendar.getTimeInMillis(), calculator, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
            }
        }

        protected void drawPoint(long time, SuntimesCalculator calculator, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(time);
            double minute = lmt.get(Calendar.HOUR_OF_DAY) * 60 + lmt.get(Calendar.MINUTE);
            SuntimesCalculator.SunPosition position = calculator.getSunPosition(lmt);
            double degrees = (position != null ? position.elevation : 0);
            drawPoint(minute, degrees, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
        }

        protected void drawPoint(double minute, double degrees, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            float x = (float) minutesToBitmapCoords(c, minute, options);
            float y = (float) degreesToBitmapCoords(c, degrees, options);

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

        @Nullable
        private LineGraphTaskListener listener = null;
        public void setListener( @Nullable LineGraphTaskListener listener )
        {
            this.listener = listener;
        }
        public void clearListener()
        {
            this.listener = null;
        }
    }

    /**
     * LineGraphTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class LineGraphTaskListener
    {
        public void onStarted() {}
        public void onDataModified(SuntimesRiseSetDataset data) {}
        public void onFrame(Bitmap frame, long offsetMinutes) {}
        public void afterFrame(Bitmap frame, long offsetMinutes) {}
        public void onFinished(Bitmap result) {}
    }

    private LineGraphTaskListener graphListener = null;
    public void setMapTaskListener( LineGraphTaskListener listener ) {
        graphListener = listener;
    }

    /**
     * LineGraphOptions
     */
    @SuppressWarnings("WeakerAccess")
    public static class LineGraphOptions
    {
        public static final int DRAW_NONE = 0;
        public static final int DRAW_SUN1 = 1;    // solid stroke
        public static final int DRAW_SUN2 = 2;    // dashed stroke

        public double graph_width = MINUTES_IN_DAY * 0.5d;    // minutes
        public double graph_x_offset = MINUTES_IN_DAY / 4d;   // minutes

        public double graph_height = 55;                     // degrees
        public double graph_y_offset = 20;                   // degrees

        // X-Axis
        public boolean axisX_show = true;
        public double axisX_width = 5d * MINUTES_IN_DAY_RATIO;   // minutes ratio

        public boolean axisX_labels_show = true;
        public float axisX_labels_textsize_ratio = 10;
        public float axisX_labels_interval = 60 * 3;  // minutes

        // Y-Axis
        public boolean axisY_show = true;
        public double axisY_width = 7.5d * MINUTES_IN_DAY_RATIO;     // minutes ratio
        public int axisY_interval = 60 * 12;        // dp

        public boolean axisY_labels_show = true;
        public float axisY_labels_textsize_ratio = 10;
        public float axisY_labels_interval = 45;  // degrees

        // Grid-X
        public boolean gridX_major_show = true;
        public double gridX_major_width = 4d * MINUTES_IN_DAY_RATIO;        // minutes ratio
        public float gridX_major_interval = axisY_labels_interval;    // degrees

        public boolean gridX_minor_show = true;
        public double gridX_minor_width = 2d * MINUTES_IN_DAY_RATIO;        // minutes ratio
        public float gridX_minor_interval = 5;    // degrees

        // Grid-Y
        public boolean gridY_major_show = true;
        public double gridY_major_width = 4d * MINUTES_IN_DAY_RATIO;       // minutes ratio
        public float gridY_major_interval = axisX_labels_interval;   // minutes

        public boolean gridY_minor_show = true;
        public double gridY_minor_width = 2d * MINUTES_IN_DAY_RATIO;       // minutes ratio
        public float gridY_minor_interval = 60;   // minutes

        public boolean sunPath_show_line = true;
        public boolean sunPath_show_fill = true;
        public boolean sunPath_show_points = false;
        //public int sunPath_color_day_closed = Color.YELLOW;
        public int sunPath_color_day_closed_alpha = 200;
        //public int sunPath_color_night_closed = Color.BLUE;
        public int sunPath_color_night_closed_alpha = 200;
        public double sunPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
        public int sunPath_interval = 5;   // minutes

        public double[] sunPath_points_elevations = new double[] { 30, -50 };  // TODO
        //public int sunPath_points_color = Color.MAGENTA;    // TODO
        public float sunPath_points_width = 150;

        public boolean moonPath_show_line = true;
        public boolean moonPath_show_fill = true;
        //public int moonPath_color_day_closed = Color.LTGRAY;
        public int moonPath_color_day_closed_alpha = 200;
        //public int moonPath_color_night_closed = Color.CYAN;
        public int moonPath_color_night_closed_alpha = 200;
        public double moonPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
        public int moonPath_interval = 5;   // minutes

        public int option_drawNow = DRAW_SUN1;
        public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size

        public int densityDpi = DisplayMetrics.DENSITY_DEFAULT;

        public LineGraphColorValues colors;
        public int getColor(String key) {
            return colors.getColor(key);
        }

        public boolean is24 = false;
        public void setTimeFormat(Context context, TimeFormatMode timeFormat) {
            is24 = ((timeFormat == TimeFormatMode.MODE_24HR) || (timeFormat == TimeFormatMode.MODE_SYSTEM && android.text.format.DateFormat.is24HourFormat(context)));
        }

        public Location location = null;
        public TimeZone timezone = null;

        public long offsetMinutes = 0;
        public long now = -1L;
        public int anim_frameLengthMs = 100;         // frames shown for 200 ms
        public int anim_frameOffsetMinutes = 1;      // each frame 1 minute apart
        public Lock anim_lock = null;

        public LineGraphOptions() {
            colors = new LineGraphColorValues();
        }

        @SuppressWarnings("ResourceType")
        public LineGraphOptions(Context context) {
            init(context);
        }

        public void init(Context context)
        {
            colors = new LineGraphColorValues(AndroidResources.wrap(context));
            //gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
            //gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
            //axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
            //axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
            //sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
            //axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
            //axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());
        }

        public void initDefaultDark(Context context)
        {
            init(context);
            colors = new LineGraphColorValues(colors.getDefaultValues(AndroidResources.wrap(context), true));  // TODO: Resources
        }

        public void initDefaultLight(Context context)
        {
            init(context);
            colors = new LineGraphColorValues(colors.getDefaultValues(AndroidResources.wrap(context), false));  // TODO: Resources
        }

        public void acquireDrawLock()
        {
            if (anim_lock != null) {
                anim_lock.lock();
                //Log.d("DEBUG", "GraphView :: acquire " + anim_lock);
            }
        }
        public void releaseDrawLock()
        {
            if (anim_lock != null) {
                //Log.d("DEBUG", "GraphView :: release " + anim_lock);
                anim_lock.unlock();
            }
        }

    }

}
