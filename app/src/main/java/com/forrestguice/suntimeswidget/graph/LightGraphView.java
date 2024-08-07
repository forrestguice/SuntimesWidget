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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import static com.forrestguice.suntimeswidget.graph.LightGraphDialog.MAPTAG_LIGHTGRAPH;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_STROKE;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_ASTRONOMICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_AXIS;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_CIVIL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_DAY;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_GRID_MAJOR;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_GRID_MINOR;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_LABELS;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_LABELS_BG;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NAUTICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NIGHT;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_STROKE;

/**
 * LightGraphView
 */
public class LightGraphView extends android.support.v7.widget.AppCompatImageView
{
    public static final String PREF_KEY_GRAPH_SHOWPOINTS = "showPoints";
    public static final boolean DEF_KEY_GRAPH_SHOWPOINTS = true;

    public static final String PREF_KEY_GRAPH_SHOWTWILIGHT = "showTwilight";
    public static final boolean DEF_KEY_GRAPH_SHOWTWILIGHT = true;

    public static final String PREF_KEY_GRAPH_SHOWCIVIL = "showCivil";
    public static final boolean DEF_KEY_GRAPH_SHOWCIVIL = true;

    public static final String PREF_KEY_GRAPH_SHOWNAUTICAL = "showNautical";
    public static final boolean DEF_KEY_GRAPH_SHOWNAUTICAL = true;

    public static final String PREF_KEY_GRAPH_SHOWASTRO = "showAstro";
    public static final boolean DEF_KEY_GRAPH_SHOWASTRO = true;

    public static final String PREF_KEY_GRAPH_SHOWSEASONS = "showSeasons";
    public static final boolean DEF_KEY_GRAPH_SHOWSEASONS = true;

    public static final String PREF_KEY_GRAPH_SHOWCROSSHAIR = "showCrosshair";
    public static final boolean DEF_KEY_GRAPH_SHOWCROSSHAIR = true;

    public static final int MINUTES_IN_DAY = 24 * 60;
    public static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LightGraphTask drawTask = null;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LightGraphOptions options;
    private SuntimesRiseSetDataset data0 = null;
    private SuntimesRiseSetDataset[] data = null;
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
            setBackgroundColor(options.colors.getColor(COLOR_DAY));
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
            options = new LightGraphOptions(context);
        }

        options.colors.setColor(COLOR_NIGHT, theme.getNightColor());
        options.colors.setColor(COLOR_DAY, theme.getDayColor());
        options.colors.setColor(COLOR_ASTRONOMICAL, theme.getAstroColor());
        options.colors.setColor(COLOR_NAUTICAL, theme.getNauticalColor());
        options.colors.setColor(COLOR_CIVIL, theme.getCivilColor());
        options.colors.setColor(COLOR_POINT_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        options.colors.setColor(COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
    }

    public void setData(@Nullable SuntimesRiseSetDataset value)
    {
        this.data0 = value;
        this.data = null;

        if (graphListener != null) {
            graphListener.onProgress(true);
        }

        if (this.data0 == null) {
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Context context = getContext();
                Location location = ((data0 != null) ? data0.location() : null);
                double longitude = ((location != null) ? location.getLongitudeAsDouble() : 0);

                String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, WidgetTimezones.LocalMeanTime.TIMEZONEID);
                TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data0.timezone() : WidgetTimezones.getTimeZone(tzId, longitude, data0.calculator());

                data = LightGraphTask.createYearData(getContext(), data0, timezone);
                handler.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        updateViews(true);
                        if (graphListener != null) {
                            graphListener.onProgress(false);
                        }
                    }
                });
            }
        });
    }

    public SuntimesRiseSetDataset getData0() {
        return data0;
    }
    public SuntimesRiseSetDataset[] getData() {
        return this.data;
    }

    /**
     * throttled update method
     */
    public void updateViews( boolean forceUpdate )
    {
        long timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);
        if (forceUpdate || timeSinceLastUpdate >= maxUpdateRate)
        {
            updateViews();
            lastUpdate = System.currentTimeMillis();
        }
    }

    public void updateViews()
    {
        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            Log.w(LightGraphView.class.getSimpleName(), "updateViews: task already running: " + data0 + " (" + Integer.toHexString(LightGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } // else Log.d(LightGraphView.class.getSimpleName(), "updateViews: starting task " + data0);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LightGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        if (data == null) {
            return;
        }

        drawTask = new LightGraphTask();
        drawTask.setData(data);
        drawTask.setListener(drawTaskListener);

        if (Build.VERSION.SDK_INT >= 11) {
            drawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data0, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetDays);
        } else {
            drawTask.execute(data0, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetDays);
        }
    }

    private final LightGraphTaskListener drawTaskListener = new LightGraphTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onStarted: " + Integer.toHexString(LightGraphView.this.hashCode()));
            if (graphListener != null) {
                graphListener.onStarted();
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetDays) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onFrame: " + Integer.toHexString(LightGraphView.this.hashCode()));
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
            //if (data0 != null) {
                //Calendar calendar = Calendar.getInstance(data0.timezone());
                //data0.setTodayIs(calendar);
                //data0.calculateData();
            //}
        }
        updateViews(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data0 != null) {
            //Log.d(LightGraphView.class.getSimpleName(), "onAttachedToWindow: update views " + data);
            updateViews(true);
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
        public static SuntimesRiseSetDataset[] createYearData(Context context, @Nullable SuntimesRiseSetDataset data0) {
            return createYearData(context, data0, null);
        }

        @Nullable
        public static SuntimesRiseSetDataset[] createYearData(Context context, @Nullable SuntimesRiseSetDataset data0, @Nullable TimeZone timezone)
        {
            if (data0 != null && data0.dataActual != null)
            {
                long bench_start = System.nanoTime();
                SuntimesRiseSetDataset[] yearData = new SuntimesRiseSetDataset[366];

                if (timezone == null) {
                    timezone = data0.calendar().getTimeZone();
                }

                Calendar date0 = Calendar.getInstance(timezone);    // data uses the configured time zone; when drawn values are shifted by the lmt hour offset to center the graph
                date0.setTimeInMillis(data0.calendar().getTimeInMillis());
                date0.set(Calendar.MONTH, 0);
                date0.set(Calendar.DAY_OF_MONTH, 1);
                date0.set(Calendar.HOUR_OF_DAY, 12);

                WidgetSettings.TimeMode[] modes = new WidgetSettings.TimeMode[] { WidgetSettings.TimeMode.OFFICIAL, WidgetSettings.TimeMode.CIVIL, WidgetSettings.TimeMode.NAUTICAL, WidgetSettings.TimeMode.ASTRONOMICAL };

                for (int i = 0; i < yearData.length; i++)
                {
                    Calendar date = Calendar.getInstance(timezone);
                    date.setTimeInMillis(date0.getTimeInMillis());
                    date.add(Calendar.DATE, i);

                    SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(data0, modes);
                    data.setTimeZone(context, timezone);
                    data.setTodayIs(date);
                    data.calculateData();
                    yearData[i] = data;
                }

                long bench_end = System.nanoTime();
                //Log.d("BENCH", "make light graph (data) :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
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
                Calendar now = Calendar.getInstance(yearData[0].timezone()); // graphTime(yearData[0], options);
                options.location = yearData[0].location();

                drawPaths(now, yearData, c, paintPath, options);
                drawGrid(now, yearData, c, p, options);
                drawAxisUnder(now, yearData, c, p, options);
                drawAxisOver(now, yearData, c, p, options);
                drawNow(now, c, p, options);
                drawLabels(now, yearData, c, paintText, options);
            }

            long bench_end = System.nanoTime();
            //Log.d("BENCH", "make light graph :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
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

        /**
         * @param hours lmt_hour
         * @return bitmap coordinates
         */
        protected double hoursToBitmapCoords(Canvas c, double hours, LightGraphOptions options)
        {
            int h = c.getHeight();
            return h - Math.round((hours / (options.graph_height)) * h)
                    + Math.round((options.graph_y_offset / (options.graph_height)) * h);
        }

        protected void drawBackground(Canvas c, Paint p, LightGraphOptions options)
        {
            p.setColor(options.colors.getColor(COLOR_DAY));
            drawRect(c, p);
        }

        protected void drawNow(Calendar now, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.option_drawNow > 0)
            {
                int pointRadius = (options.option_drawNow_pointSizePx <= 0)
                        ? (int) textSize(c, 365 / 7f) //(int)(c.getWidth() * (5 / 365d))
                        : options.option_drawNow_pointSizePx;
                int pointStroke = (int)Math.ceil(pointRadius / 3d);

                if (options.option_drawNow_crosshair) {
                    drawVerticalLine(now, c, p, 2 * pointStroke / 3, options.colors.getColor(COLOR_SUN_STROKE), null);
                    drawHorizontalLine(now, c, p, 2 * pointStroke / 3, options.colors.getColor(COLOR_SUN_STROKE), null);
                }

                switch (options.option_drawNow)
                {
                    case LightGraphOptions.DRAW_NOW2:
                        DashPathEffect dashed = new DashPathEffect(new float[] {4, 2}, 0);
                        drawPoint(now, pointRadius, pointStroke, c, p, Color.TRANSPARENT, options.colors.getColor(COLOR_SUN_STROKE), dashed);
                        break;

                    case LightGraphOptions.DRAW_NOW1:
                    default:
                        drawPoint(now, pointRadius, pointStroke, c, p, options.colors.getColor(COLOR_SUN_FILL), options.colors.getColor(COLOR_SUN_STROKE), null);
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
                    drawPoint(point[0], point[1], (int)pointSize, 0, c, p, options.colors.getColor(COLOR_POINT_FILL), options.colors.getColor(COLOR_POINT_STROKE), null);
                }
            }
        }

        protected void drawPaths(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            if (options.sunPath_show_line || options.sunPath_show_fill)
            {
                WidgetSettings.TimeMode nightBoundary = WidgetSettings.TimeMode.OFFICIAL;
                paintPath.setColor(options.colors.getColor(COLOR_NIGHT));
                drawPath(now, data, nightBoundary, true, c, paintPath, options);
                drawPath(now, data, nightBoundary, false, c, paintPath, options);

                if (options.showCivil)
                {
                    nightBoundary = WidgetSettings.TimeMode.CIVIL;
                    paintPath.setColor(options.colors.getColor(COLOR_CIVIL));
                    drawPath(now, data, WidgetSettings.TimeMode.OFFICIAL, true, c, paintPath, options);
                    drawPath(now, data, WidgetSettings.TimeMode.OFFICIAL, false, c, paintPath, options);
                }

                if (options.showNautical)
                {
                    nightBoundary = WidgetSettings.TimeMode.NAUTICAL;
                    paintPath.setColor(options.colors.getColor(COLOR_NAUTICAL));
                    drawPath(now, data, WidgetSettings.TimeMode.CIVIL, true, c, paintPath, options);
                    drawPath(now, data, WidgetSettings.TimeMode.CIVIL, false, c, paintPath, options);
                }

                if (options.showAstro)
                {
                    nightBoundary = WidgetSettings.TimeMode.ASTRONOMICAL;
                    paintPath.setColor(options.colors.getColor(COLOR_ASTRONOMICAL));
                    drawPath(now, data, WidgetSettings.TimeMode.NAUTICAL, true, c, paintPath, options);
                    drawPath(now, data, WidgetSettings.TimeMode.NAUTICAL, false, c, paintPath, options);
                }

                paintPath.setColor(options.colors.getColor(COLOR_NIGHT));
                drawPath(now, data, nightBoundary, true, c, paintPath, options);
                drawPath(now, data, nightBoundary, false, c, paintPath, options);

                publishMinMax(WidgetSettings.TimeMode.OFFICIAL.name(), c, options);
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
                for (Path path : sunFill.keySet()) {
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

        /**
         * @param rising true rising event, false setting event
         * @param hour lmt_hour
         * @param day day_of_year
         */
        private void trackMinMax(String mode, boolean rising, double hour, double day)
        {
            if (rising)
            {
                Pair<Double,Double> value = options.t_sunrise_earliest.get(mode);
                if (value == null || hour < value.second) {
                    options.t_sunrise_earliest.put(mode, new Pair<>(day, hour));
                }
                value = options.t_sunrise_latest.get(mode);
                if (value == null || hour > value.second) {
                    options.t_sunrise_latest.put(mode, new Pair<>(day, hour));
                }

            } else {
                Pair<Double,Double> value = options.t_sunset_earliest.get(mode);
                if (value == null || hour < value.second) {
                    options.t_sunset_earliest.put(mode, new Pair<>(day, hour));
                }
                value = options.t_sunset_latest.get(mode);
                if (value == null || hour > value.second) {
                    options.t_sunset_latest.put(mode, new Pair<>(day, hour));
                }
            }
        }
        private void resetMinMax(String mode, boolean rising) {
            if (rising) {
                options.t_sunrise_earliest.remove(mode);
                options.t_sunrise_latest.remove(mode);
            } else {
                options.t_sunset_earliest.remove(mode);
                options.t_sunset_latest.remove(mode);
            }
            options.t_earliest_latest_isReady.remove(mode);
        }
        private void publishMinMax(String mode, Canvas c, LightGraphOptions options)
        {
            Pair<Double,Double>[] points = new Pair[] {
                    options.t_sunrise_earliest.get(mode),
                    options.t_sunrise_latest.get(mode),
                    options.t_sunset_earliest.get(mode),
                    options.t_sunset_latest.get(mode)
            };

            ArrayList<float[]> p = new ArrayList<>();
            for (Pair<Double,Double> point : points)
            {
                if (point != null)
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_YEAR, point.first.intValue());
                    double offset = lmtOffsetHours(calendar.getTimeInMillis()) - lmtOffsetHours();  // offset lmt_hour to lmt_hour + dst

                    float x = (float) daysToBitmapCoords(c, point.first, options);
                    float y = (float) hoursToBitmapCoords(c, wrapHour(point.second + offset), options);
                    p.add(new float[] {x, y});
                }
            }
            options.sunPath_points = p.toArray(new float[0][0]);
            options.t_earliest_latest_isReady.put(mode, true);
            //Log.d("DEBUG", "sunPath_points: " + options.sunPath_points.length);
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
            double lmtOffsetHours = lmtOffsetHours();

            Path path = null;
            int day = 0;
            while (day < data.length)
            {
                SuntimesRiseSetData d = data[day].getData(mode.name());
                event = (rising ? d.sunriseCalendarToday() : d.sunsetCalendarToday());
                if (event != null)
                {
                    double tzHour = event.get(Calendar.HOUR_OF_DAY)
                            + (event.get(Calendar.MINUTE) / 60d)
                            + (event.get(Calendar.SECOND) / (60d * 60d))
                            + (event.get(Calendar.MILLISECOND) / (60d * 60d * 1000d));
                    hour = wrapHour(tzHour - lmtOffsetHours);    // lmt_hour + dst
                    trackMinMax(mode.name(), rising, wrapHour(tzHour - lmtOffsetHours(event.getTimeInMillis())), day);

                } else {
                    hour = (rising ? 0 : 24);
                }

                if (Math.abs(hour - hour_prev) > 12) {   // ignore sudden shifts (polar regions near graph edge)
                    hour = hour_prev;
                }

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
            if (options.showSeasons) {
                drawSeasonsBar(c, p, options);
            }
            if (options.axisX_labels_show) {
                drawYLabels(c, p, options);
            }
            if (options.axisY_labels_show) {
                drawXLabels(c, p, options);
            }
        }

        protected void drawAxisUnder(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            double r = Math.sqrt(c.getWidth() * c.getHeight());
            if (options.axisY_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.colors.getColor(COLOR_AXIS));
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
                p.setColor(options.colors.getColor(COLOR_AXIS));
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
                p.setColor(options.colors.getColor(COLOR_GRID_MINOR));
                drawGridX(c, p, options.gridX_minor_interval, options);
            }
            if (options.gridY_minor_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridY_minor_width));
                p.setColor(options.colors.getColor(COLOR_GRID_MINOR));
                drawGridY(now, data, c, p, options.gridY_minor_interval, options);
            }
            if (options.gridX_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridX_major_width));
                p.setColor(options.colors.getColor(COLOR_GRID_MAJOR));
                drawGridX(c, p, options.gridX_major_interval, options);
            }
            if (options.gridY_major_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((float)(r / options.gridY_major_width));
                p.setColor(options.colors.getColor(COLOR_GRID_MAJOR));
                drawGridY(now, data, c, p, options.gridY_major_interval, options);
            }
        }

        /**
         * @return raw offset in hours between time zone and local mean time (ignores dst)
         */
        protected double lmtOffsetHours()
        {
            long lonOffsetMs = Math.round(options.location.getLongitudeAsDouble() * MILLIS_IN_DAY / 360d);
            long rawOffsetMs = options.timezone.getRawOffset();
            return (rawOffsetMs - lonOffsetMs) / (1000d * 60d * 60d);
        }

        /**
         * @param date long date+time
         * @return offset in hours between time zone and local mean time (with dst)
         */
        protected double lmtOffsetHours(long date)
        {
            long lonOffsetMs = Math.round(options.location.getLongitudeAsDouble() * MILLIS_IN_DAY / 360d);
            long zoneOffsetMs = options.timezone.getOffset(date);
            return (zoneOffsetMs - lonOffsetMs) / (1000d * 60d * 60d);
        }

        protected void drawAxisX(Canvas c, Paint p, LightGraphOptions options)
        {
            Calendar calendar0 = Calendar.getInstance(options.timezone);
            Calendar calendar = Calendar.getInstance(yearData[0].timezone());
            double offsetHours = lmtOffsetHours();

            float textSize = textSize(c, options.axisY_labels_textsize_ratio);
            float left = (float)(c.getWidth() - (1.5 * textSize));

            int w = c.getWidth();
            for (int hour = 0; hour < 24; hour++)
            {
                calendar0.set(Calendar.HOUR_OF_DAY, hour);
                calendar0.set(Calendar.MINUTE, 0);  //calendar0.set(Calendar.MINUTE, (int)((hour - hour.intValue()) * 60d));
                calendar0.set(Calendar.SECOND, 0);
                calendar0.set(Calendar.MILLISECOND, 0);

                float x = (hour % 6 == 0) ? 0 : left;

                calendar.setTimeInMillis(calendar0.getTimeInMillis());
                double h = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60d);
                float y = (float) hoursToBitmapCoords(c, wrapHour(h - offsetHours), options);
                c.drawLine(x, y, w, y, p);
            }
        }


        protected void drawXLabels(Canvas c, Paint p, LightGraphOptions options)
        {
            float textSize = textSize(c, options.axisY_labels_textsize_ratio);
            float left = (float)(c.getWidth() - (1.5 * textSize));

            p.setColor(options.colors.getColor(COLOR_LABELS_BG));
            //p.setAlpha(128);
            float top = (textSize + (textSize/4));
            c.drawRect(left, 0, left + (int)(1.5 * textSize), c.getHeight() - top, p);
            //p.setAlpha(255);

            Calendar calendar0 = Calendar.getInstance(options.timezone);
            Calendar calendar = Calendar.getInstance(yearData[0].timezone());
            double offsetHours = lmtOffsetHours();

            int i = (int) options.axisY_labels_interval;
            while (i < 24)
            {
                calendar0.set(Calendar.HOUR_OF_DAY, i);
                calendar0.set(Calendar.MINUTE, 0);
                calendar0.set(Calendar.SECOND, 0);
                calendar0.set(Calendar.MILLISECOND, 0);

                calendar.setTimeInMillis(calendar0.getTimeInMillis());
                double h = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60d);
                float y = (float) hoursToBitmapCoords(c, h - offsetHours, options);

                p.setColor(options.colors.getColor(COLOR_LABELS));
                p.setTextSize(textSize);
                String label = ((options.is24 || i == 12) ? i : (i % 12)) + "";
                c.drawText(label, left + (textSize * 0.75f), y + textSize/3 , p);
                i += options.axisY_labels_interval;
            }
        }

        protected void drawAxisY(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
        {
            boolean showCrossQuarter = false;

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
                if (showCrossQuarter) {
                    c.drawLine((x + x0)/2, 0, (x + x0)/2, h, p);
                }
                x0 = x;
            }

            float textSize = textSize(c, options.axisY_labels_textsize_ratio);
            float top = c.getHeight() - (textSize + (textSize/4));

            int interval = 30;
            int n = 365 - interval;
            int i = interval;
            while (i < n)
            {
                float x = (float) daysToBitmapCoords(c, i, options);
                c.drawLine(x, top, x, h, p);
                i += interval;
            }

        }
        private Calendar drawAxisY_calendar = null;

        protected float textSize(Canvas c, float ratio)
        {
            //int s = Math.min(c.getWidth(), c.getHeight());
            int s = (int)((c.getWidth() + c.getHeight()) / 2d);
            return (float)(Math.sqrt(s * (s/2d)) / ratio);
        }

        protected void drawYLabels(Canvas c, Paint p, LightGraphOptions options)
        {
            int n = 365;    // days
            int h = c.getHeight();
            float textSize = textSize(c, options.axisX_labels_textsize_ratio);

            p.setColor(options.colors.getColor(COLOR_LABELS_BG));
            //p.setAlpha(128);
            c.drawRect(0, c.getHeight() - (textSize + (textSize/4)), c.getWidth(), c.getHeight(), p);
            //p.setAlpha(255);

            int i = (int) options.axisX_labels_interval;
            while (i <= n)
            {
                float x = (float) daysToBitmapCoords(c, i - options.axisX_labels_interval, options);
                int month = (i / 30);

                p.setColor(options.colors.getColor(COLOR_LABELS));
                p.setTextSize(textSize);
                c.drawText("" + month, x + textSize/2, h - textSize/4, p);
                i += options.axisX_labels_interval;
            }
        }

        public Shader makeGradient(int x0, int gradientWidth, int gradientStart, int gradientEnd) {
            return new LinearGradient(x0, 0, x0 + gradientWidth, 0, gradientStart, gradientEnd, Shader.TileMode.CLAMP);
        }

        private final Shader[] seasonGradients = new Shader[4];
        protected void drawSeasonsBar(Canvas c, Paint p, LightGraphOptions options)
        {
            int colorSpring = options.colors.getColor(LightGraphColorValues.COLOR_SPRING);
            int colorSummer = options.colors.getColor(LightGraphColorValues.COLOR_SUMMER);
            int colorAutumn = options.colors.getColor(LightGraphColorValues.COLOR_AUTUMN);
            int colorWinter = options.colors.getColor(LightGraphColorValues.COLOR_WINTER);

            int gradientWidth = (int)(c.getWidth() / 4d);
            int[] gradientColors = (options.location.getLatitudeAsDouble() < 0 && options.localizeToHemisphere)
                    ? new int[] { colorSummer, colorAutumn, colorWinter, colorSpring, colorSummer}
                    : new int[] { colorWinter, colorSpring, colorSummer, colorAutumn, colorWinter};

            for (int i=0; i<4; i++) {
                if (seasonGradients[i] == null) {
                    seasonGradients[i] = makeGradient(gradientWidth * i, gradientWidth, gradientColors[i], gradientColors[i+1]);
                }
            }

            Shader shader0 = p.getShader();
            p.setDither(true);

            int height = (int) textSize(c, options.axisX_labels_textsize_ratio) / 2;
            int offset = (int) (1.25 * textSize(c, options.axisX_labels_textsize_ratio));

            int y0 = c.getHeight() - (offset + height);
            int y1 = c.getHeight() - offset;

            int x0, x1 = 0;
            for (int i=0; i < seasonGradients.length; i++)
            {
                x0 = x1;
                x1 = x0 + gradientWidth;
                p.setShader(seasonGradients[i]);
                c.drawRect(x0, y0, x1, y1, p);
            }

            p.setShader(shader0);
        }

        protected void drawGridX(Canvas c, Paint p, float interval, LightGraphOptions options)
        {
            int hourMin = 0;
            int hourMax = 24;
            double offsetHours = lmtOffsetHours();

            int w = c.getWidth();
            int i = hourMin;
            while (i < hourMax)
            {
                float y = (float) hoursToBitmapCoords(c, i - offsetHours, options);
                c.drawLine(0, y, w, y, p);
                i += interval;
            }
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

        protected Path createHorizontalPath(@Nullable Calendar calendar, Canvas c)
        {
            if (calendar != null) {
                return createHorizontalPath(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.getTimeZone(), c);
            } else return null;
        }
        protected Path createHorizontalPath(double hour, TimeZone timezone, Canvas c) {
            return createHorizontalPath((int) hour, (int)((hour - (int)hour) * 60), timezone, c);
        }

        protected Path createHorizontalPath(int hour, int minute, TimeZone timezone, Canvas c)
        {
            Calendar calendar0 = Calendar.getInstance(timezone);
            calendar0.set(Calendar.DAY_OF_YEAR, 0);
            calendar0.set(Calendar.HOUR_OF_DAY, hour);
            calendar0.set(Calendar.MINUTE, minute);
            calendar0.set(Calendar.SECOND, 0);
            calendar0.set(Calendar.MILLISECOND, 0);

            float x, y;
            double lmtHour;
            double lmtOffsetHours = lmtOffsetHours();

            Path path = new Path();
            for (int day=0; day<365; day++)
            {
                double tzHour = calendar0.get(Calendar.HOUR_OF_DAY) + (calendar0.get(Calendar.MINUTE) / 60d) + (calendar0.get(Calendar.SECOND) / (60d * 60d));
                lmtHour = wrapHour(tzHour - lmtOffsetHours);
                x = (float) daysToBitmapCoords(c, day, options);
                y = (float) hoursToBitmapCoords(c, lmtHour, options);

                if (day == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
                calendar0.add(Calendar.HOUR, 24);
            }
            return path;
        }

        protected void drawPoint(Calendar calendar, int radius, int strokeWidth, Canvas c, Paint p, int fillColor, int strokeColor, DashPathEffect strokeEffect)
        {
            if (calendar != null)
            {
                double day = calendar.get(Calendar.DAY_OF_YEAR);
                double tzHour = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60d) + (calendar.get(Calendar.SECOND) / (60d * 60d));
                double hour = wrapHour(tzHour - lmtOffsetHours());
                drawPoint(day, hour, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
            }
        }

        /**
         * @param day day_of_year
         * @param hour lmt_hour
         */
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

        /**
         * @param hour raw hour value
         * @return hour value within range [0, 24]
         */
        protected double wrapHour(double hour)
        {
            double v = hour;
            while (v < 0) {
                v += 24;
            }
            while (v > 24) {
                v -= 24;
            }
            return v;
        }
        protected double clampHour(double hour)
        {
            if (hour < 0) {
                hour = 0;
            }
            if (hour > 24) {
                hour = 24;
            }
            return hour;
        }

        protected void drawVerticalLine(@Nullable Calendar calendar, Canvas c, Paint p, int lineWidth, int lineColor, DashPathEffect lineEffect)
        {
            if (calendar != null)
            {
                double day = calendar.get(Calendar.DAY_OF_YEAR);
                drawVerticalLine(day, c, p, lineWidth, lineColor, lineEffect);
            }
        }
        protected void drawVerticalLine(double day, Canvas c, Paint p, int lineWidth, int lineColor, @Nullable DashPathEffect lineEffect)
        {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(lineWidth);
            p.setColor(lineColor);

            if (lineEffect != null) {
                p.setPathEffect(lineEffect);
            }

            float x = (float) daysToBitmapCoords(c, day, options);
            c.drawLine(x, 0, x, c.getHeight(), p);
        }

        /**
         * @param calendar calendar with given time zone
         */
        protected void drawHorizontalLine(@Nullable Calendar calendar, Canvas c, Paint p, int lineWidth, int lineColor, @Nullable DashPathEffect lineEffect)
        {
            if (calendar != null)
            {
                double tzHour = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60d) + (calendar.get(Calendar.SECOND) / (60d * 60d));
                double hour = wrapHour(tzHour - lmtOffsetHours());
                drawHorizontalLine(hour, c, p, lineWidth, lineColor, lineEffect);
            }
        }
        protected void drawHorizontalLine(double hour, Canvas c, Paint p, int lineWidth, int lineColor, @Nullable DashPathEffect lineEffect)
        {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(lineWidth);
            p.setColor(lineColor);

            if (lineEffect != null) {
                p.setPathEffect(lineEffect);
            }

            float y = (float) hoursToBitmapCoords(c, hour, options);
            c.drawLine(0, y, c.getWidth(), y, p);
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
        public void onFrame(Bitmap frame, long offsetDays) {}
        public void afterFrame(Bitmap frame, long offsetDays) {}
        public void onFinished(Bitmap result) {}
        public void onProgress(boolean value) {}
    }

    private LightGraphTaskListener graphListener = null;
    public void setTaskListener( LightGraphTaskListener listener ) {
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
        public double axisX_width = 365;   // days

        public boolean axisX_labels_show = true;
        public float axisX_labels_textsize_ratio = 20;
        public float axisX_labels_interval = 30;  // days

        // Y-Axis
        public boolean axisY_show = true;
        public double axisY_width = 300;    // ~5m minutes
        public int axisY_interval = 60 * 12;        // dp

        public boolean axisY_labels_show = true;
        public float axisY_labels_textsize_ratio = 20;
        public float axisY_labels_interval = 3;  // hours

        // Grid-X
        public boolean gridX_major_show = true;
        public double gridX_major_width = 300;        // minutes
        public float gridX_major_interval = axisY_labels_interval;    // hours

        public boolean gridX_minor_show = true;
        public double gridX_minor_width = 400;        // minutes
        public float gridX_minor_interval = 1;    // hours

        // Grid-Y
        public boolean gridY_major_show = true;
        public double gridY_major_width = 300;       // minutes
        public float gridY_major_interval = axisX_labels_interval;   // days

        public boolean gridY_minor_show = true;
        public double gridY_minor_width = 400;       // minutes
        public float gridY_minor_interval = 5;       // days

        public boolean sunPath_show_line = true;
        public boolean sunPath_show_fill = true;
        public boolean sunPath_show_points = DEF_KEY_GRAPH_SHOWPOINTS;

        public double sunPath_width = 140;       // (1440 min/day) / 140 = 10 min wide
        //public int sunPath_interval = 1;   // 1 day

        public float[][] sunPath_points = new float[0][0];
        //public int sunPath_points_color = Color.MAGENTA;
        public float sunPath_points_width = 150;

        public boolean localizeToHemisphere = true;
        public boolean showSeasons = true;
        public boolean showCivil = true, showNautical = true, showAstro = true;
        public int option_drawNow = DRAW_NOW1;
        public int option_drawNow_pointSizePx = -1;    // when set, use a fixed point size

        public boolean option_drawNow_crosshair = DEF_KEY_GRAPH_SHOWCROSSHAIR;

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

        public TimeZone timezone = null;
        public LightGraphColorValues colors;

        public final Map<String, Pair<Double,Double>> t_sunrise_earliest = new HashMap<>();
        public final Map<String, Pair<Double,Double>> t_sunset_earliest = new HashMap<>();
        public final Map<String, Pair<Double,Double>> t_sunrise_latest = new HashMap<>();
        public final Map<String, Pair<Double,Double>> t_sunset_latest = new HashMap<>();
        public final Map<String, Boolean> t_earliest_latest_isReady = new HashMap<>();

        public LightGraphOptions() {}

        @SuppressWarnings("ResourceType")
        public LightGraphOptions(Context context) {
            init(context);
        }

        protected void init(Context context)
        {
            colors = new LightGraphColorValues(context);
            //gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
            //gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
            //axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
            //axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
            //sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
            //axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
            //axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());
        }

        public void initDefaultDark(Context context) {
            init(context);
        }

        public void initDefaultLight(Context context) {
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
