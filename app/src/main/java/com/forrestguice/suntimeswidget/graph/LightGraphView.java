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

import android.content.Context;
import android.content.res.TypedArray;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import net.time4j.calendar.astro.SolarTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

/**
 * LightGraphView
 */
public class LightGraphView extends android.support.v7.widget.AppCompatImageView
{
    public static final int MINUTES_IN_DAY = 24 * 60;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LightGraphTask drawTask = null;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LightGraphOptions options;
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LightGraphView(Context context)
    {
        super(context);
        init(context);
    }

    public LightGraphView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        options = new LightGraphOptions(context);
        if (isInEditMode()) {
            setBackgroundColor(options.colorBackground);
        }
    }

    public int getMaxUpdateRate() {
        return maxUpdateRate;
    }

    public void setResizable( boolean value ) {
        resizable = value;
    }

    /**
     * onResume
     */
    public void onResume() {
        //Log.d(LightGraphView.class.getSimpleName(), "onResume");
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
            Log.d(LightGraphView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LightGraphOptions getOptions() {
        return options;
    }

    /**
     * themeViews
     */
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (options == null) {
            options = new LightGraphOptions();
        }
        options.colorNight = theme.getNightColor();
        options.colorDay = options.colorBackground = theme.getDayColor();
        options.colorAstro = theme.getAstroColor();
        options.colorNautical = theme.getNauticalColor();
        options.colorCivil = theme.getCivilColor();
        options.colorPointFill = theme.getGraphPointFillColor();
        options.colorPointStroke = theme.getGraphPointStrokeColor();
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
            Log.w(LightGraphView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(LightGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } else Log.d(LightGraphView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LightGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        drawTask = new LightGraphTask();
        drawTask.setListener(drawTaskListener);

        if (Build.VERSION.SDK_INT >= 11) {
            drawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetDays);
        } else {
            drawTask.execute(data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetDays);
        }
    }

    private final LightGraphTaskListener drawTaskListener = new LightGraphTaskListener() {
        @Override
        public void onStarted() {
            Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onStarted: " + Integer.toHexString(LightGraphView.this.hashCode()));
            if (graphListener != null) {
                graphListener.onStarted();
            }
        }

        /*@Override
        public void onDataModified(SuntimesRiseSetDataset data)
        {
            Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onDataModified: " + Integer.toHexString(LightGraphView.this.hashCode()));
            LightGraphView.this.data = data;
            if (graphListener != null) {
                graphListener.onDataModified(data);
            }
        }*/

        @Override
        public void onFrame(Bitmap frame, long offsetDays) {
            Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onFrame: " + Integer.toHexString(LightGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFrame(frame, offsetDays);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetDays) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: afterFrame: " + Integer.toHexString(LightGraphView.this.hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onFinished: " + Integer.toHexString(LightGraphView.this.hashCode()));
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
        //Log.d("DEBUG", "LightGraphView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LightGraphView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetDays = bundle.getLong("offsetDays", options.offsetDays);
        options.now = bundle.getLong("now", options.now);
    }

    public boolean saveSettings(Bundle bundle)
    {
        //Log.d(LightGraphView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetDays", options.offsetDays);
        bundle.putLong("now", options.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LightGraphView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LightGraphView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void resetAnimation( boolean updateTime )
    {
        //Log.d(LightGraphView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        options.offsetDays = 0;
        if (updateTime)
        {
            options.now = -1;
            if (data != null) {
                Calendar calendar = Calendar.getInstance(data.timezone());
                data.setTodayIs(calendar);
                data.calculateData();
            }
        }
        updateViews(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data != null) {
            //Log.d(LightGraphView.class.getSimpleName(), "onAttachedToWindow: update views " + data);
            updateViews(data);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d(LightGraphView.class.getSimpleName(), "onDetachedFromWindow: cancel task " + Integer.toHexString(LightGraphView.this.hashCode()));
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
        options.offsetDays = (offsetMillis / 1000 / 60 / 6 / 24);
        updateViews(true);
    }

    public void setOffsetDays( long value ) {
        options.offsetDays = value;
        updateViews(true);
    }
    public long getOffsetDays() {
        return options.offsetDays;
    }
    public long getNow() {
        return options.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * LightGraphTask
     */
    public static class LightGraphTask extends AsyncTask<Object, Bitmap, Bitmap>
    {
        private LightGraphOptions options;

        //private SuntimesRiseSetDataset t_data = null;

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

            SuntimesRiseSetDataset[] yearData = createYearData(data0);
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

                    frame = makeBitmap(yearData, w, h, options);

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

        @Nullable
        public static SuntimesRiseSetDataset[] createYearData(@Nullable SuntimesRiseSetDataset data0)
        {
            if (data0 != null && data0.dataActual != null)
            {
                SuntimesRiseSetDataset[] yearData = new SuntimesRiseSetDataset[366];

                Calendar date0 = Calendar.getInstance(data0.calendar().getTimeZone());
                date0.setTimeInMillis(data0.calendar().getTimeInMillis());
                date0.set(Calendar.MONTH, 0);
                date0.set(Calendar.DAY_OF_MONTH, 1);
                date0.set(Calendar.HOUR_OF_DAY, 12);

                for (int i = 0; i < yearData.length; i++)
                {
                    Calendar date = Calendar.getInstance();
                    date.setTimeInMillis(date0.getTimeInMillis());
                    date.add(Calendar.DATE, i);

                    SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(data0);
                    data.setTodayIs(date);
                    data.calculateData();
                    yearData[i] = data;
                }

                //if (!data0.isCalculated()) {
                //    data0.calculateData();
                //}

                return yearData;
            }
            return null;
        }

        public Bitmap makeBitmap(SuntimesRiseSetDataset[] yearData, int w, int h, LightGraphOptions options )
        {
            long bench_start = System.nanoTime();

            if (w <= 0 || h <= 0 || options == null) {
                return null;
            }
            this.options = options;

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            b.setDensity(options.densityDpi);
            Canvas c = new Canvas(b);
            initPaint();

            drawBackground(c, paintBackground, options);

            if (yearData != null)
            {
                Calendar now = Calendar.getInstance(); // graphTime(yearData[0], options);
                options.location = yearData[0].location();

                drawPaths(now, yearData, c, paintPath, options);
                drawGrid(now, yearData, c, p, options);
                drawAxisUnder(now, yearData, c, p, options);
                drawAxisOver(now, yearData, c, p, options);
                drawLabels(now, yearData, c, paintText, options);
                drawNow(now, yearData[0].calculator(), c, p, options);
            }

            long bench_end = System.nanoTime();
            Log.d("BENCH", "make line graph :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
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

        protected Calendar graphTime(@Nullable SuntimesRiseSetDataset data, @NonNull LightGraphOptions options)
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

            long days = options.offsetDays;
            while (days > Integer.MAX_VALUE) {
                days = days - Integer.MAX_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MAX_VALUE);
            }
            while (days < Integer.MIN_VALUE) {
                days = days + Integer.MIN_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MIN_VALUE);
            }
            mapTime.add(Calendar.DATE, (int)days);

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
                notifyIfDataModified();
                for (int i=0; i<frames.length; i++) {
                    listener.onFrame(frames[i], options.offsetDays);
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
        //private SuntimesRiseSetDataset[] t_data = null;

        /////////////////////////////////////////////

        protected double daysToBitmapCoords(Canvas c, double day, LightGraphOptions options) {
            return Math.round((day / options.graph_width) * c.getWidth() - (options.graph_x_offset / options.graph_width) * c.getWidth());
        }

        protected double hoursToBitmapCoords(Canvas c, double hours, LightGraphOptions options)
        {
            int h = c.getHeight();
            return h - Math.round((hours / (options.graph_height)) * h)
                    + Math.round((options.graph_y_offset / (options.graph_height)) * h);
        }

        protected void drawBackground(Canvas c, Paint p, LightGraphOptions options)
        {
            p.setColor(options.colorBackground);
            drawRect(c, p);
        }

        protected void drawNow(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.option_drawNow > 0)
            {
                int pointRadius = (options.option_drawNow_pointSizePx <= 0) ? (int)(c.getWidth() * (20 / (float)MINUTES_IN_DAY)) : options.option_drawNow_pointSizePx;
                int pointStroke = (int)Math.ceil(pointRadius / 3d);

                switch (options.option_drawNow)
                {
                    case LightGraphOptions.DRAW_NOW2:
                        DashPathEffect dashed = new DashPathEffect(new float[] {4, 2}, 0);
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, Color.TRANSPARENT, options.colorPointStroke, dashed);
                        break;

                    case LightGraphOptions.DRAW_NOW1:
                    default:
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, options.colorPointFill, options.colorPointStroke, null);
                        Log.d("DEBUG", "drawing now");
                        //drawVerticalLine(now, calculator, c, p, pointStroke, options.colorPointFill, null);
                        break;
                }
            }
        }

        protected void drawPathPoints(Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.sunPath_show_points)
            {
                double pointSize = Math.sqrt(c.getWidth() * c.getHeight()) / options.sunPath_points_width;
                for (float[] point : options.sunPath_points) {
                    drawPoint(point[0], point[1], (int)pointSize, 0, c, p, options.sunPath_points_color, options.sunPath_points_color, null);
                }
            }
        }

        protected void drawPaths(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.sunPath_show_line || options.sunPath_show_fill)
            {
                paintPath.setColor(options.colorCivil);
                drawPath(now, data, WidgetSettings.TimeMode.OFFICIAL, true, c, paintPath, options);
                drawPath(now, data, WidgetSettings.TimeMode.OFFICIAL, false, c, paintPath, options);

                paintPath.setColor(options.colorNautical);
                drawPath(now, data, WidgetSettings.TimeMode.CIVIL, true, c, paintPath, options);
                drawPath(now, data, WidgetSettings.TimeMode.CIVIL, false, c, paintPath, options);

                paintPath.setColor(options.colorAstro);
                drawPath(now, data, WidgetSettings.TimeMode.NAUTICAL, true, c, paintPath, options);
                drawPath(now, data, WidgetSettings.TimeMode.NAUTICAL, false, c, paintPath, options);

                paintPath.setColor(options.colorNight);
                drawPath(now, data, WidgetSettings.TimeMode.ASTRONOMICAL, true, c, paintPath, options);
                drawPath(now, data, WidgetSettings.TimeMode.ASTRONOMICAL, false, c, paintPath, options);

                drawPathPoints(c, p, options);
            }
        }

        private ArrayList<Path> sun_paths = new ArrayList<>();
        private HashMap<Path, Double> sun_hours = new HashMap<>();


        protected void drawPath(Calendar now, SuntimesRiseSetDataset[] data, WidgetSettings.TimeMode mode, boolean rising, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.sunPath_show_fill)
            {
                HashMap<Path, Double> sunFill = createSunPath(now, data, mode, rising, c, options, true, sun_paths, sun_hours);
                p.setStyle(Paint.Style.FILL);
                for (Path path : sunFill.keySet())
                {
                    //boolean isDay = (sunFill.get(path) >= 0);
                    //p.setAlpha(isDay ? options.sunPath_color_day_closed_alpha : options.sunPath_color_night_closed_alpha);
                    c.drawPath(path, p);
                }
            }

            if (options.sunPath_show_line)
            {
                double r = Math.sqrt(c.getWidth() * c.getHeight());
                HashMap<Path, Double> sunPath = createSunPath(now, data, mode, rising, c, options, false, sun_paths, sun_hours);
                p.setStrokeWidth((float)(r / (float)options.sunPath_width));
                p.setStyle(Paint.Style.STROKE);
                for (Path path : sunPath.keySet())
                {
                    p.setAlpha(255);
                    c.drawPath(path, p);
                }
            }
        }

        private final Map<String, Pair<Double,Double>> sunrise_earliest = new HashMap<>();
        private final Map<String, Pair<Double,Double>> sunset_earliest = new HashMap<>();
        private final Map<String, Pair<Double,Double>> sunrise_latest = new HashMap<>();
        private final Map<String, Pair<Double,Double>> sunset_latest = new HashMap<>();

        private void trackMinMax(String mode, boolean rising, double hour, double day)
        {
            if (rising)
            {
                Pair<Double,Double> value = sunrise_earliest.get(mode);
                if (value == null || hour < value.second) {
                    sunrise_earliest.put(mode, new Pair<>(day, hour));
                }
                value = sunrise_latest.get(mode);
                if (value == null || hour > value.second) {
                    sunrise_latest.put(mode, new Pair<>(day, hour));
                }

            } else {
                Pair<Double,Double> value = sunset_earliest.get(mode);
                if (value == null || hour < value.second) {
                    sunset_earliest.put(mode, new Pair<>(day, hour));
                }
                value = sunset_latest.get(mode);
                if (value == null || hour > value.second) {
                    sunset_latest.put(mode, new Pair<>(day, hour));
                }
            }
        }
        private void resetMinMax(String mode, boolean rising) {
            if (rising) {
                sunrise_earliest.remove(mode);
                sunrise_latest.remove(mode);
            } else {
                sunset_earliest.remove(mode);
                sunset_latest.remove(mode);
            }
        }
        private void publishMinMax(String mode, Canvas c, LightGraphOptions options)
        {
            Pair<Double,Double>[] points = new Pair[] {
                    sunrise_earliest.get(mode),
                    sunrise_latest.get(mode),
                    sunset_earliest.get(mode),
                    sunset_latest.get(mode)
            };

            ArrayList<float[]> p = new ArrayList<>();
            for (Pair<Double,Double> point : points)
            {
                if (point != null)
                {
                    float x = (float) daysToBitmapCoords(c, point.first, options);
                    float y = (float) hoursToBitmapCoords(c, point.second, options);
                    p.add(new float[] {x, y});
                }
            }
            options.sunPath_points = p.toArray(new float[0][0]);
            Log.d("DEBUG", "sunPath_points: " + options.sunPath_points.length);
        }

        protected HashMap<Path, Double> createSunPath(Calendar now, SuntimesRiseSetDataset[] data, WidgetSettings.TimeMode mode, boolean rising, Canvas c, LightGraphOptions options, boolean closed, ArrayList<Path> paths, HashMap<Path,Double> hours)
        {
            paths.clear();
            hours.clear();
            resetMinMax(mode.name(), rising);

            Calendar event;
            double hour;
            double hour_prev = (rising ? 0 : 24);  // previous iteration
            float x = 0, y = 0;

            Path path = null;
            int day = 0;
            while (day < data.length)
            {
                SuntimesRiseSetData d = data[day].getData(mode.name());
                event = (rising ? d.sunriseCalendarToday() : d.sunsetCalendarToday());
                hour = event.get(Calendar.HOUR_OF_DAY) + (event.get(Calendar.MINUTE) / 60d) + (event.get(Calendar.SECOND) / (60d * 60d));
                trackMinMax(mode.name(), rising, hour, day);

                x = (float) daysToBitmapCoords(c, day, options);
                y = (float) hoursToBitmapCoords(c, hour, options);

                if (path != null
                        && ((hour_prev < 0 && hour >= 0)
                        || (hour_prev >= 0 && hour < 0))) {
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
                    hours.put(path, hour);

                    if (closed) {
                        path.moveTo(x, (float)hoursToBitmapCoords(c, (rising ? 0 : 24), options));
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                    }
                } else {
                    path.lineTo(x, y);
                }

                hour_prev = hour;
                day++;
            }

            if (closed)
            {
                path = paths.get(paths.size()-1);
                path.lineTo(x, (float)hoursToBitmapCoords(c, (rising ? 0 : 24), options));
                path.close();
            }

            if (mode == WidgetSettings.TimeMode.OFFICIAL) {
                publishMinMax(mode.name(), c, options);
            }
            return hours;
        }
        protected void closePaths(List<Path> paths)
        {
            for (Path path : paths) {
                path.close();
            }
        }

        protected void drawLabels(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.axisY_labels_show) {
                drawAxisYLabels(c, p, options);
            }
            if (options.axisX_labels_show) {
                drawAxisXLabels(c, p, options);
            }
        }

        protected void drawAxisUnder(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            double r = Math.sqrt(c.getWidth() * c.getHeight());
            if (options.axisY_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.axisY_color);
                p.setStrokeWidth((float)(r / options.axisY_width));
                drawAxisY(now, data, c, p, options);
            }
        }
        protected void drawAxisOver(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            double r = Math.sqrt(c.getWidth() * c.getHeight());
            if (options.axisX_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.axisX_color);
                p.setStrokeWidth((float)(r / options.axisX_width));
                drawAxisX(c, p, options);
            }
        }

        protected void drawGrid(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            double r = Math.sqrt(c.getWidth() * c.getHeight());
            if (options.gridX_minor_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridX_minor_width));
                p.setColor(options.gridX_minor_color);
                drawGridX(c, p, options.gridX_minor_interval, options);
            }
            if (options.gridY_minor_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridY_minor_width));
                p.setColor(options.gridY_minor_color);
                drawGridY(now, data, c, p, options.gridY_minor_interval, options);
            }
            if (options.gridX_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridX_major_width));
                p.setColor(options.gridX_major_color);
                drawGridX(c, p, options.gridX_major_interval, options);
            }
            if (options.gridY_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridY_major_width));
                p.setColor(options.gridY_major_color);
                drawGridY(now, data, c, p, options.gridY_major_interval, options);
            }
        }

        protected void drawAxisX(Canvas c, Paint p, LightGraphOptions options)
        {
            ArrayList<Double> hours = new ArrayList<>();
            //hours.add(6d);

            hours.add(12d);
            //hours.add(18d);

            int w = c.getWidth();
            for (Double hour : hours)
            {
                float y = (float) hoursToBitmapCoords(c, hour, options);
                c.drawLine(0, y, w, y, p);
            }
        }
        protected void drawAxisY(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            SuntimesCalculator calculator = data[0].calculator();
            ArrayList<Calendar> events = new ArrayList<>();
            events.add(calculator.getSpringEquinoxForYear(data[0].calendar()));
            events.add(calculator.getSummerSolsticeForYear(data[0].calendar()));
            events.add(calculator.getAutumnalEquinoxForYear(data[0].calendar()));
            events.add(calculator.getWinterSolsticeForYear(data[0].calendar()));

            float h = c.getHeight();
            float x0 = -1 * (c.getWidth() - (float) daysToBitmapCoords(c, events.get(events.size() - 1).get(Calendar.DAY_OF_YEAR), options));
            for (int i=0; i<events.size(); i++)
            {
                Calendar event = events.get(i);
                float x = (float) daysToBitmapCoords(c, event.get(Calendar.DAY_OF_YEAR), options);
                c.drawLine(x, 0, x, h, p);
                c.drawLine((x + x0)/2, 0, (x + x0)/2, h, p);
                x0 = x;
            }
        }
        private Calendar drawAxisY_calendar = null;

        protected float textSize(Canvas c, float ratio) {
            return (float)(Math.sqrt(c.getWidth() * (c.getWidth()/2d)) / ratio);
        }

        protected void drawAxisXLabels(Canvas c, Paint p, LightGraphOptions options)
        {
            int n = 365;    // days
            int h = c.getHeight();
            float textSize = textSize(c, options.axisX_labels_textsize_ratio);

            p.setColor(options.axisX_labels_bgcolor);
            p.setAlpha(128);
            c.drawRect(0, c.getHeight() - (textSize + (textSize/4)), c.getWidth(), c.getHeight(), p);
            p.setAlpha(255);

            int i = (int) options.axisX_labels_interval;
            while (i <= n)
            {
                float x = (float) daysToBitmapCoords(c, i - options.axisX_labels_interval, options);
                int month = (i / 30);

                p.setColor(options.axisX_labels_color);
                p.setTextSize(textSize);
                c.drawText("" + month, x + textSize/2, h - textSize/4, p);
                i += options.axisX_labels_interval;
            }
        }
        protected void drawAxisYLabels(Canvas c, Paint p, LightGraphOptions options)
        {
            float textSize = textSize(c, options.axisY_labels_textsize_ratio);
            float left = (float)(c.getWidth() - (1.5 * textSize));

            p.setColor(options.axisY_labels_bgcolor);
            p.setAlpha(128);
            c.drawRect(left, 0, left + (int)(1.5 * textSize), c.getHeight(), p);
            p.setAlpha(255);

            int i = (int) options.axisY_labels_interval;
            while (i < 24)
            {
                float y = (float) hoursToBitmapCoords(c, i, options);
                p.setColor(options.axisY_labels_color);
                p.setTextSize(textSize);
                String label = ((options.is24 || i == 12) ? i : (i % 12)) + "";
                c.drawText(label, left + (textSize * 0.75f), y + textSize/3 , p);
                i += options.axisY_labels_interval;
            }
        }

        protected void drawGridX(Canvas c, Paint p, float interval, LightGraphOptions options)
        {
            int hourMin = 0;
            int hourMax = 24;

            int w = c.getWidth();
            int i = hourMin;
            while (i < hourMax)
            {
                float y = (float) hoursToBitmapCoords(c, i, options);
                c.drawLine(0, y, w, y, p);
                i += interval;
            }
        }

        private Calendar lmt = null;
        private Calendar lmt(Location location)
        {
            if (lmt == null) {
                lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(null, location));
            }
            return lmt;
        }

        protected void drawGridY(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, float interval, LightGraphOptions options)
        {
            int n = 365;
            int h = c.getHeight();
            int i = 0;
            while (i < n)
            {
                float x = (float) daysToBitmapCoords(c, i, options);
                c.drawLine(x, 0, x, h, p);
                i += interval;
            }
        }

        protected void drawRect(Canvas c, Paint p)
        {
            int w = c.getWidth();
            int h = c.getHeight();
            c.drawRect(0, 0, w, h, p);
        }

        protected void drawPoint(Calendar calendar, SuntimesCalculator calculator, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect) {
            if (calendar != null) {
                drawPoint(calendar.getTimeInMillis(), calculator, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
            }
        }
        protected void drawPoint(long time, SuntimesCalculator calculator, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(time);
            double day = lmt.get(Calendar.DAY_OF_YEAR);
            double hour = lmt.get(Calendar.HOUR_OF_DAY);
            drawPoint(day, hour, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
        }
        protected void drawPoint(double day, double hour, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            float x = (float) daysToBitmapCoords(c, day, options);
            float y = (float) hoursToBitmapCoords(c, hour, options);
            drawPoint(x, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
        }

        protected void drawPoint(float x, float y, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
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

        protected void drawVerticalLine(Calendar calendar, SuntimesCalculator calculator, Canvas c, Paint p, int lineWidth, int lineColor, DashPathEffect lineEffect) {
            if (calendar != null) {
                drawVerticalLine(calendar.getTimeInMillis(), calculator, c, p, lineWidth, lineColor, lineEffect);
            }
        }
        protected void drawVerticalLine(long time, SuntimesCalculator calculator, Canvas c, Paint p, int lineWidth, int lineColor, DashPathEffect lineEffect)
        {
            Calendar lmt = lmt(calculator.getLocation());
            lmt.setTimeInMillis(time);
            double day = lmt.get(Calendar.DAY_OF_YEAR);
            drawVerticalLine(day, c, p, lineWidth, lineColor, lineEffect);
        }
        protected void drawVerticalLine(double day, Canvas c, Paint p, int lineWidth, int lineColor, @Nullable DashPathEffect lineEffect)
        {
            float x = (float) daysToBitmapCoords(c, day, options);
            c.drawLine(x, 0, x, c.getHeight(), p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(lineWidth);
            p.setColor(lineColor);

            if (lineEffect != null) {
                p.setPathEffect(lineEffect);
            }
        }

        private LightGraphTaskListener listener = null;
        public void setListener( LightGraphTaskListener listener ) {
            this.listener = listener;
        }
        public void clearListener() {
            this.listener = null;
        }
    }

    /**
     * LightGraphTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class LightGraphTaskListener
    {
        public void onStarted() {}
        //public void onDataModified(SuntimesRiseSetDataset data) {}
        public void onFrame(Bitmap frame, long offsetDays) {}
        public void afterFrame(Bitmap frame, long offsetDays) {}
        public void onFinished(Bitmap result) {}
    }

    private LightGraphTaskListener graphListener = null;
    public void setMapTaskListener( LightGraphTaskListener listener ) {
        graphListener = listener;
    }

    /**
     * LightGraphOptions
     */
    @SuppressWarnings("WeakerAccess")
    public static class LightGraphOptions
    {
        public static final int DRAW_NONE = 0;
        public static final int DRAW_NOW1 = 1;    // solid
        public static final int DRAW_NOW2 = 2;    // dashed

        public double graph_width = 365;    // days
        public double graph_x_offset = 0;   // days

        public double graph_height = 24;                     // hours
        public double graph_y_offset = 0;                    // hours

        // X-Axis
        public boolean axisX_show = true;
        public int axisX_color = Color.BLACK;
        public double axisX_width = 365;   // days

        public boolean axisX_labels_show = true;
        public int axisX_labels_color = Color.WHITE;
        public int axisX_labels_bgcolor = Color.BLACK;
        public float axisX_labels_textsize_ratio = 20;
        public float axisX_labels_interval = 30;  // days

        // Y-Axis
        public boolean axisY_show = true;
        public int axisY_color = Color.BLACK;
        public double axisY_width = 300;    // ~5m minutes
        public int axisY_interval = 60 * 12;        // dp

        public boolean axisY_labels_show = true;
        public int axisY_labels_color = Color.LTGRAY;
        public int axisY_labels_bgcolor = Color.BLACK;
        public float axisY_labels_textsize_ratio = 20;
        public float axisY_labels_interval = 3;  // hours

        // Grid-X
        public boolean gridX_major_show = true;
        public int gridX_major_color = Color.BLACK;
        public double gridX_major_width = 300;        // minutes
        public float gridX_major_interval = axisY_labels_interval;    // hours

        public boolean gridX_minor_show = true;
        public int gridX_minor_color = Color.GRAY;
        public double gridX_minor_width = 400;        // minutes
        public float gridX_minor_interval = 1;    // hours

        // Grid-Y
        public boolean gridY_major_show = true;
        public int gridY_major_color = Color.BLACK;
        public double gridY_major_width = 300;       // minutes
        public float gridY_major_interval = axisX_labels_interval;   // days

        public boolean gridY_minor_show = true;
        public int gridY_minor_color = Color.GRAY;
        public double gridY_minor_width = 400;       // minutes
        public float gridY_minor_interval = 5;       // days

        public boolean sunPath_show_line = true;
        public boolean sunPath_show_fill = true;
        public boolean sunPath_show_points = false;
        public int sunPath_color_day = Color.YELLOW;
        public int sunPath_color_day_closed = Color.YELLOW;
        public int sunPath_color_day_closed_alpha = 200;
        public int sunPath_color_night = Color.BLUE;
        public int sunPath_color_night_closed = Color.BLUE;
        public int sunPath_color_night_closed_alpha = 200;
        public double sunPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
        //public int sunPath_interval = 1;   // 1 day

        public float[][] sunPath_points = new float[0][0];
        public int sunPath_points_color = Color.MAGENTA;
        public float sunPath_points_width = 150;

        public int colorDay, colorCivil, colorNautical, colorAstro, colorNight;
        public int colorBackground;
        public int colorPointFill, colorPointStroke;
        public int option_drawNow = DRAW_NOW1;
        public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size

        public int densityDpi = DisplayMetrics.DENSITY_DEFAULT;

        public boolean is24 = false;
        public void setTimeFormat(Context context, WidgetSettings.TimeFormatMode timeFormat) {
            is24 = ((timeFormat == WidgetSettings.TimeFormatMode.MODE_24HR) || (timeFormat == WidgetSettings.TimeFormatMode.MODE_SYSTEM && android.text.format.DateFormat.is24HourFormat(context)));
        }

        public Location location = null;

        public long offsetDays = 0;
        public long now = -1L;
        public int anim_frameLengthMs = 100;         // frames shown for 200 ms
        public int anim_frameOffsetDays = 1;         // each frame 1 day apart
        public Lock anim_lock = null;

        public LightGraphOptions() {}

        @SuppressWarnings("ResourceType")
        public LightGraphOptions(Context context)
        {
            int[] colorAttrs = { R.attr.graphColor_day,     // 0
                    R.attr.graphColor_civil,                // 1
                    R.attr.graphColor_nautical,             // 2
                    R.attr.graphColor_astronomical,         // 3
                    R.attr.graphColor_night,                // 4
                    R.attr.graphColor_pointFill,            // 5
                    R.attr.graphColor_pointStroke,          // 6
                    R.attr.graphColor_axis,                 // 7
                    R.attr.graphColor_grid,                 // 8
                    R.attr.graphColor_labels,               // 9
                    //R.attr.moonriseColor,                   // 10
                    //R.attr.moonsetColor                     // 11
            };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            colorDay = colorBackground = sunPath_color_day = sunPath_color_day_closed = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
            colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.transparent));
            colorNautical = sunPath_color_night = sunPath_color_night_closed = ContextCompat.getColor(context, typedArray.getResourceId(2,R.color.transparent));
            colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(3, R.color.transparent));
            colorNight = ContextCompat.getColor(context, typedArray.getResourceId(4, R.color.transparent));
            colorPointFill = ContextCompat.getColor(context, typedArray.getResourceId(5, R.color.transparent));
            colorPointStroke = ContextCompat.getColor(context, typedArray.getResourceId(6, R.color.transparent));
            axisX_color = axisY_color = gridX_major_color = gridY_major_color = ContextCompat.getColor(context, typedArray.getResourceId(7, R.color.graphColor_axis_dark));
            gridX_minor_color = gridY_minor_color = ContextCompat.getColor(context, typedArray.getResourceId(8, R.color.graphColor_grid_dark));
            axisX_labels_color = axisY_labels_color = ContextCompat.getColor(context, typedArray.getResourceId(9, R.color.graphColor_labels_dark));
            //moonPath_color_day = moonPath_color_day_closed = ContextCompat.getColor(context, typedArray.getResourceId(10, R.color.moonIcon_color_rising_dark));
            //moonPath_color_night = moonPath_color_night_closed = ContextCompat.getColor(context, typedArray.getResourceId(11, R.color.moonIcon_color_setting_dark));
            typedArray.recycle();
            init(context);
        }

        protected void init(Context context)
        {
            //gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
            //gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
            //axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
            //axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
            //sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
            //axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
            //axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());

            //ColorUtils.setAlphaComponent(sunPath_color_day, sunPath_color_day_alpha);
            //ColorUtils.setAlphaComponent(sunPath_color_night, sunPath_color_night_alpha);
        }

        public void initDefaultDark(Context context)
        {
            colorDay = colorBackground = sunPath_color_day = sunPath_color_day_closed = ContextCompat.getColor(context, R.color.graphColor_day_dark);
            colorCivil = ContextCompat.getColor(context, R.color.graphColor_civil_dark);
            colorNautical = sunPath_color_night = sunPath_color_night_closed = ContextCompat.getColor(context, R.color.graphColor_nautical_dark);
            colorAstro = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            colorNight = ContextCompat.getColor(context, R.color.graphColor_night_dark);
            colorPointFill = ContextCompat.getColor(context, R.color.graphColor_pointFill_dark);
            colorPointStroke = ContextCompat.getColor(context, R.color.graphColor_pointStroke_dark);
            axisX_color = axisY_color = gridX_major_color = gridY_major_color = ContextCompat.getColor(context, R.color.graphColor_axis_dark);
            gridX_minor_color = gridY_minor_color = ContextCompat.getColor(context, R.color.graphColor_grid_dark);
            axisX_labels_color = axisY_labels_color = ContextCompat.getColor(context, R.color.graphColor_labels_dark);
            //moonPath_color_day = moonPath_color_day_closed = ContextCompat.getColor(context, R.color.moonIcon_color_rising_dark);
            //moonPath_color_night = moonPath_color_night_closed = ContextCompat.getColor(context, R.color.moonIcon_color_setting_dark);
            init(context);
        }

        public void initDefaultLight(Context context)
        {
            colorDay = colorBackground = sunPath_color_day = sunPath_color_day_closed = ContextCompat.getColor(context, R.color.graphColor_day_light);
            colorCivil = ContextCompat.getColor(context, R.color.graphColor_civil_light);
            colorNautical = sunPath_color_night = sunPath_color_night_closed = ContextCompat.getColor(context, R.color.graphColor_nautical_light);
            colorAstro = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            colorNight = ContextCompat.getColor(context, R.color.graphColor_night_light);
            colorPointFill = ContextCompat.getColor(context, R.color.graphColor_pointFill_light);
            colorPointStroke = ContextCompat.getColor(context, R.color.graphColor_pointStroke_light);
            axisX_color = axisY_color = gridX_major_color = gridY_major_color = ContextCompat.getColor(context, R.color.graphColor_axis_light);
            gridX_minor_color = gridY_minor_color = ContextCompat.getColor(context, R.color.graphColor_grid_light);
            axisX_labels_color = axisY_labels_color = ContextCompat.getColor(context, R.color.graphColor_labels_light);
            //moonPath_color_day = moonPath_color_day_closed = ContextCompat.getColor(context, R.color.moonIcon_color_rising_light);
            //moonPath_color_night = moonPath_color_night_closed = ContextCompat.getColor(context, R.color.moonIcon_color_setting_light);
            init(context);
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
