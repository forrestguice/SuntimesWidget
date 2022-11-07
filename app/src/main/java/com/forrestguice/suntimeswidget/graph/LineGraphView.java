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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * LineGraphView
 */
public class LineGraphView extends android.support.v7.widget.AppCompatImageView
{
    private static final int MINUTES_IN_DAY = 24 * 60;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LineGraphTask drawTask;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LineGraphOptions options;
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
            setBackgroundColor(options.colorNight);
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
            //Log.d(LineGraphView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LineGraphOptions getOptions() {
        return options;
    }

    /**
     * themeViews
     */
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (options == null) {
            options = new LineGraphOptions();
        }
        options.colorNight = theme.getNightColor();
        options.colorDay = theme.getDayColor();
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
            //Log.w(LineGraphView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(LineGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } //else Log.d(LineGraphView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LineGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        drawTask = new LineGraphTask();
        drawTask.setListener(drawTaskListener);
        drawTask.execute(data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetMinutes);
    }

    private LineGraphTaskListener drawTaskListener = new LineGraphTaskListener() {
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
    protected void loadSettings(Context context)
    {
        //Log.d("DEBUG", "LineGraphView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetMinutes = bundle.getLong("offsetMinutes", options.offsetMinutes);
        options.now = bundle.getLong("now", options.now);
    }

    protected boolean saveSettings(Bundle bundle)
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

    public void resetAnimation( boolean updateTime )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        options.offsetMinutes = 0;
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
        private LineGraphOptions options;

        private SuntimesRiseSetDataset t_data = null;

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
                //Log.d(LightMapTask.class.getSimpleName(), "generating frame " + i + " | " + w + "," + h);
                if (isCancelled()) {
                    break;
                }

                if (data != null && data.dataActual != null)
                {
                    Calendar maptime = graphTime(data, options);
                    Calendar datatime = data.dataActual.calendar();
                    long data_age = (maptime.getTimeInMillis() - datatime.getTimeInMillis());
                    if (data_age >= (12 * 60 * 60 * 1000)) {    // TODO: more precise

                        //Log.d(LightMapTask.class.getSimpleName(), "recalculating dataset with adjusted date: " + data_age);
                        Calendar calendar = Calendar.getInstance(data.timezone());
                        calendar.setTimeInMillis(maptime.getTimeInMillis());

                        data = new SuntimesRiseSetDataset(data);
                        data.setTodayIs(calendar);
                        data.calculateData();
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
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            drawBackground(c, p, options);
            if (data != null)
            {
                options.location = data.location();
                drawAxis(now, data.dataActual, c, p, options);
                drawSunPath(now, data.calculator(), c, p, options);
                drawNow(now, data.calculator(), c, p, options);
            }

            long bench_end = System.nanoTime();
            Log.d("BENCH", "make line graph :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            return b;
        }

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
            return (Math.round((minute / options.graph_width) * c.getWidth()));
        }
        protected double timeToBitmapCoords(Canvas c, long timestamp, LineGraphOptions options)
        {
            if (tz_lmt == null) {
                tz_lmt = WidgetTimezones.localMeanTime(null, options.location);
            }
            Calendar lmt = Calendar.getInstance(tz_lmt);
            lmt.setTimeInMillis(timestamp);
            int minute = lmt.get(Calendar.HOUR_OF_DAY) * 60 + lmt.get(Calendar.MINUTE);
            return (Math.round((minute / options.graph_width) * c.getWidth()));
        }
        private TimeZone tz_lmt = null;

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
            float cY = c.getHeight() / 2f;
            return cY - (cY * (degrees / options.graph_height));
        }

        protected void drawBackground(Canvas c, Paint p, LineGraphOptions options)
        {
            p.setColor(options.colorNight);
            drawRect(c, p);
        }

        protected void drawNow(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.option_drawNow > 0)
            {
                int pointRadius = (options.option_drawNow_pointSizePx <= 0) ? (int)(c.getWidth() * (20 / (float)MINUTES_IN_DAY)) : options.option_drawNow_pointSizePx;
                int pointStroke = (int)Math.ceil(pointRadius / 3d);

                switch (options.option_drawNow) {
                    case LineGraphOptions.DRAW_SUN2:
                        DashPathEffect dashed = new DashPathEffect(new float[] {4, 2}, 0);
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, Color.TRANSPARENT, options.colorPointStroke, dashed);
                        break;

                    case LineGraphOptions.DRAW_SUN1:
                    default:
                        drawPoint(now, calculator, pointRadius, pointStroke, c, p, options.colorPointFill, options.colorPointStroke, null);
                        break;
                }
            }
        }

        private double elevation_min = -90, elevation_max = 90;
        protected void drawSunPath(Calendar now, SuntimesCalculator calculator, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.sunPath_show)
            {
                ArrayList<Path> paths = new ArrayList<>();
                HashMap<Path, Double> elevations = new HashMap<>();

                Calendar lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(null, calculator.getLocation()));
                lmt.setTimeInMillis(now.getTimeInMillis());
                lmt.set(Calendar.HOUR_OF_DAY, 0);
                lmt.set(Calendar.MINUTE, 0);
                lmt.set(Calendar.SECOND, 0);

                SuntimesCalculator.SunPosition position;
                double elevation_prev = -90;   // sun elevation (previous iteration)
                elevation_min = elevation_max = 0;
                float x = 0, y = 0;

                Path path = null;
                int minute = 0;
                while (minute < options.graph_width)
                {
                    position = calculator.getSunPosition(lmt);
                    if (position.elevation < elevation_min) {
                        elevation_min = position.elevation;
                    } else if (position.elevation > elevation_max) {
                        elevation_max = position.elevation;
                    }

                    int d = (int)(minute / MINUTES_IN_DAY);
                    double m = (d * MINUTES_IN_DAY) + (lmt.get(Calendar.HOUR_OF_DAY) * 60) + lmt.get(Calendar.MINUTE);
                    x = (float) minutesToBitmapCoords(c, m, options);
                    y = (float) degreesToBitmapCoords(c, position.elevation, options);

                    if (path != null
                            && ((elevation_prev < 0 && position.elevation >= 0)
                                || (elevation_prev >= 0 && position.elevation < 0))) {
                        path.lineTo(x, y);
                        if (options.sunPath_closed) {
                            path.close();
                        }
                        path = null;
                    }

                    if (path == null)
                    {
                        path = new Path();
                        paths.add(path);
                        elevations.put(path, position.elevation);

                        if (options.sunPath_closed) {
                            path.moveTo(x, (float)degreesToBitmapCoords(c, 0, options));
                            path.lineTo(x, y);
                        } else {
                            path.moveTo(x, y);
                        }

                    } else {
                        path.lineTo(x, y);
                    }

                    elevation_prev = position.elevation;
                    lmt.add(Calendar.MINUTE, options.sunPath_interval);
                    minute += options.sunPath_interval;
                }

                if (options.sunPath_closed)
                {
                    path = paths.get(paths.size()-1);
                    path.lineTo(x, (float)degreesToBitmapCoords(c, 0, options));
                    path.close();
                }

                p.setStrokeWidth(options.sunPath_width);
                for (int i=0; i<paths.size(); i++)
                {
                    path = paths.get(i);
                    p.setStyle(options.sunPath_closed ? Paint.Style.FILL : Paint.Style.STROKE);
                    p.setColor(elevations.get(path) >= 0 ? options.sunPath_color_day : options.sunPath_color_night);
                    c.drawPath(path, p);
                }
            }
        }

        protected void drawAxis(Calendar now, SuntimesData data, Canvas c, Paint p, LineGraphOptions options)
        {
            if (options.gridX_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(options.gridX_width);
                p.setColor(options.gridX_color);
                drawGridX(c, p, options);
            }
            if (options.gridY_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(options.gridY_width);
                p.setColor(options.gridY_color);
                drawGridY(now, data, c, p, options);
            }
            if (options.axisX_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.axisX_color);
                p.setStrokeWidth(options.axisX_width);
                drawAxisX(c, p, options);
                if (options.axisX_labels_show) {
                    drawAxisXLabels(c, p, options);
                }
            }
            if (options.axisY_show)
            {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(options.axisY_color);
                p.setStrokeWidth(options.axisY_width);
                drawAxisY(now, data, c, p, options);
                if (options.axisY_labels_show) {
                    drawAxisYLabels(c, p, options);
                }
            }
            if (options.axisX_show && options.axisX_labels_show) {
                drawAxisXLabels(c, p, options);
            }
            if (options.axisY_show && options.axisY_labels_show) {
                drawAxisYLabels(c, p, options);
            }
        }

        protected void drawAxisX(Canvas c, Paint p, LineGraphOptions options)
        {
            int w = c.getWidth();
            float y0 = (float) degreesToBitmapCoords(c, 0, options);
            c.drawLine(0, y0, w, y0, p);
        }
        protected void drawAxisY(Calendar now, SuntimesData data, Canvas c, Paint p, LineGraphOptions options)
        {
            int h = c.getHeight();

            Calendar calendar = Calendar.getInstance(now.getTimeZone());
            calendar.set(Calendar.HOUR_OF_DAY, (options.axisY_interval / 60));
            calendar.set(Calendar.MINUTE, (options.axisY_interval % 60));
            calendar.set(Calendar.SECOND, 0);

            Calendar lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(null, data.location()));
            lmt.setTimeInMillis(calendar.getTimeInMillis());

            int hour = lmt.get(Calendar.HOUR_OF_DAY);
            int i = (hour * 60) + lmt.get(Calendar.MINUTE);
            while (i <= (options.graph_width - 1))
            {
                float x = (float) minutesToBitmapCoords(c, i, options);
                c.drawLine(x, 0, x, h, p);
                i += options.axisY_interval;
            }
        }

        protected void drawAxisXLabels(Canvas c, Paint p, LineGraphOptions options)
        {
            int h = c.getHeight();
            int i = (int) options.axisX_labels_interval;
            while (i <= (options.graph_width - options.axisX_labels_interval))
            {
                float x = (float) minutesToBitmapCoords(c, i, options);
                int hour = (options.is24 ? (i / 60) : (i / 60) % 12);
                if (!options.is24 && hour == 0) {
                    hour = 12;
                }
                p.setAntiAlias(true);
                p.setTextAlign(Paint.Align.CENTER);
                p.setColor(options.axisX_labels_color);
                p.setTextSize(options.axisX_labels_textsize);
                p.setLinearText(true);
                p.setFakeBoldText(false);
                c.drawText("" + hour, x, h - options.axisX_labels_textsize/4, p);
                i += options.axisX_labels_interval;
            }
        }
        protected void drawAxisYLabels(Canvas c, Paint p, LineGraphOptions options)
        {
            int i = -1 * (int) options.axisY_labels_interval;
            while (i < 90)
            {
                float y = (float) degreesToBitmapCoords(c, i, options);
                p.setAntiAlias(true);
                p.setTextAlign(Paint.Align.CENTER);
                p.setColor(options.axisY_labels_color);
                p.setTextSize(options.axisY_labels_textsize);
                p.setFakeBoldText(false);
                c.drawText("" + i, 0 + options.axisY_labels_textsize, y + options.axisY_labels_textsize/3 , p);
                i += options.axisY_labels_interval;
            }
        }

        protected void drawGridX(Canvas c, Paint p, LineGraphOptions options)
        {
            int degreeMin = -90;
            int degreeMax = 90;

            int w = c.getWidth();
            int i = degreeMin;
            while (i < degreeMax)
            {
                float y = (float) degreesToBitmapCoords(c, i, options);
                c.drawLine(0, y, w, y, p);
                i += options.gridX_interval;
            }
        }

        protected void drawGridY(Calendar now, SuntimesData data, Canvas c, Paint p, LineGraphOptions options)
        {
            Calendar calendar = Calendar.getInstance(now.getTimeZone());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Calendar lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(null, data.location()));
            lmt.setTimeInMillis(calendar.getTimeInMillis());

            int h = c.getHeight();
            int hours = lmt.get(Calendar.HOUR_OF_DAY);
            int i = (hours * 60) + lmt.get(Calendar.MINUTE) - (hours > 0 ? MINUTES_IN_DAY : 0);
            while (i < options.graph_width)
            {
                float x = (float) minutesToBitmapCoords(c, i, options);
                c.drawLine(x, 0, x, h, p);
                i += options.gridY_interval;
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
            if (calendar != null)
            {
                Calendar lmt = Calendar.getInstance(WidgetTimezones.localMeanTime(null, calculator.getLocation()));
                lmt.setTimeInMillis(calendar.getTimeInMillis());

                double minute = lmt.get(Calendar.HOUR_OF_DAY) * 60 + lmt.get(Calendar.MINUTE);
                float x = (float) minutesToBitmapCoords(c, minute, options);

                double degrees = calculator.getSunPosition(lmt).elevation;
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
        }

        private LineGraphTaskListener listener = null;
        public void setListener( LineGraphTaskListener listener )
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

        public double graph_width = MINUTES_IN_DAY;    // minutes
        public double graph_height = 80;               // +- 80 degrees

        public boolean axisX_labels_show = false;
        public int axisX_labels_color = Color.WHITE;
        public float axisX_labels_textsize = 16;  // sp
        public float axisX_labels_interval = 60 * 3;  // minutes

        public boolean axisY_labels_show = false;
        public int axisY_labels_color = Color.LTGRAY;
        public float axisY_labels_textsize = 16;  // sp
        public float axisY_labels_interval = 45;  // degrees

        public boolean axisX_show = true;
        public int axisX_color = Color.BLACK;
        public int axisX_width = 4;        // dp

        public boolean axisY_show = true;
        public int axisY_color = Color.BLACK;
        public int axisY_width = 4;        // dp
        public int axisY_interval = 60 * 6;        // dp

        public boolean gridX_show = true;
        public int gridX_color = Color.GRAY;
        public int gridX_width = 2;        // dp
        public int gridX_interval = 10;    // degrees

        public boolean gridY_show = true;
        public int gridY_color = Color.GRAY;
        public int gridY_width = 1;       // dp
        public int gridY_interval = 60;   // minutes

        public boolean sunPath_show = true;
        public int sunPath_color_day = Color.YELLOW;
        public int sunPath_color_night = Color.BLUE;
        public int sunPath_width = 8;       // dp
        public int sunPath_interval = 2;   // minutes
        public boolean sunPath_closed = true;

        public int colorDay, colorCivil, colorNautical, colorAstro, colorNight;
        public int colorPointFill, colorPointStroke;
        public int option_drawNow = DRAW_SUN1;
        public int option_drawNow_pointSizePx = -1;    // when set, used a fixed point size

        public int densityDpi = DisplayMetrics.DENSITY_DEFAULT;

        public boolean is24 = false;
        public Location location = null;

        public long offsetMinutes = 0;
        public long now = -1L;
        public int anim_frameLengthMs = 100;         // frames shown for 200 ms
        public int anim_frameOffsetMinutes = 1;      // each frame 1 minute apart

        public LineGraphOptions() {}

        @SuppressWarnings("ResourceType")
        public LineGraphOptions(Context context)
        {
            int[] colorAttrs = { R.attr.graphColor_day,     // 0
                    R.attr.graphColor_civil,                // 1
                    R.attr.graphColor_nautical,             // 2
                    R.attr.graphColor_astronomical,         // 3
                    R.attr.graphColor_night,                // 4
                    R.attr.graphColor_pointFill,            // 5
                    R.attr.graphColor_pointStroke };        // 6
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;

            colorDay = sunPath_color_day = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            colorNautical = sunPath_color_night = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
            colorNight = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
            colorPointFill = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
            colorPointStroke = ContextCompat.getColor(context, typedArray.getResourceId(6, def));

            gridX_width = SuntimesUtils.dpToPixels(context, gridX_width);
            gridY_width = SuntimesUtils.dpToPixels(context, gridY_width);
            axisX_width = SuntimesUtils.dpToPixels(context, axisX_width);
            axisY_width = SuntimesUtils.dpToPixels(context, axisY_width);
            sunPath_width = SuntimesUtils.dpToPixels(context, sunPath_width);
            axisX_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisX_labels_textsize, context.getResources().getDisplayMetrics());
            axisY_labels_textsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, axisY_labels_textsize, context.getResources().getDisplayMetrics());

            typedArray.recycle();
        }

        public void initDefaultDark(Context context)
        {
            colorDay = sunPath_color_day = ContextCompat.getColor(context, R.color.graphColor_day_dark);
            colorCivil = ContextCompat.getColor(context, R.color.graphColor_civil_dark);
            colorNautical = sunPath_color_night = ContextCompat.getColor(context, R.color.graphColor_nautical_dark);
            colorAstro = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            colorNight = ContextCompat.getColor(context, R.color.graphColor_night_dark);
            colorPointFill = ContextCompat.getColor(context, R.color.graphColor_pointFill_dark);
            colorPointStroke = ContextCompat.getColor(context, R.color.graphColor_pointStroke_dark);
        }

        public void initDefaultLight(Context context)
        {
            colorDay = sunPath_color_day = ContextCompat.getColor(context, R.color.graphColor_day_light);
            colorCivil = ContextCompat.getColor(context, R.color.graphColor_civil_light);
            colorNautical = sunPath_color_night = ContextCompat.getColor(context, R.color.graphColor_nautical_light);
            colorAstro = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            colorNight = ContextCompat.getColor(context, R.color.graphColor_night_light);
            colorPointFill = ContextCompat.getColor(context, R.color.graphColor_pointFill_light);
            colorPointStroke = ContextCompat.getColor(context, R.color.graphColor_pointStroke_light);
        }
    }

}
