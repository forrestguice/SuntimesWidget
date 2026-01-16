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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.util.Log;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

public class LightMapTask extends AsyncTask<Object, Bitmap, Bitmap>
{
    protected static final double MINUTES_IN_DAY = 24 * 60;
    protected static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    protected static final double ONE_DIVIDED_MILLIS_IN_DAY = 1d / MILLIS_IN_DAY;

    private final WeakReference<Context> contextRef;
    private LightMapOptions colors;

    @Nullable
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
            colors = (LightMapOptions)params[3];

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

    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, LightMapOptions colors)
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

            // draw solar noon
            if (colors.option_drawNoon)
            {
                int lineWidth = (int)Math.ceil(c.getWidth() / (24d * 12d));     // a line that is 5 minutes wide
                p.setColor(colors.values.getColor(LightMapColorValues.COLOR_SUN_STROKE));
                drawVerticalLine(data.dataNoon.sunriseCalendarToday(), data.dataNoon, lineWidth, c, p);
            }

            // draw now marker
            if (colors.option_drawNow > 0)
            {
                //if (colors.option_lmt)
                //{
                    TimeZone lmt = WidgetTimezones.localMeanTime(data.location());
                    Calendar nowLmt = Calendar.getInstance(lmt);
                    nowLmt.setTimeInMillis(now.getTimeInMillis());
                    now = nowLmt;
                //}

                drawSunSymbol(colors.option_drawNow, now, c, p, colors);
            }
        }

        Bitmap retValue = b;
        if (!colors.option_lmt)    // re-center around noon
        {
            Bitmap b0 = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c0 = new Canvas(b0);

            long zoneOffsetMs = ((data != null) ? data.timezone().getOffset(now.getTimeInMillis()) : 0);
            long lonOffsetMs = ((data != null) ? Math.round(data.location().getLongitudeAsDouble() * MILLIS_IN_DAY / 360d) : 0);
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

    protected static Calendar mapTime(@Nullable SuntimesRiseSetDataset data, @NonNull LightMapOptions options)
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

    protected void drawSunSymbol(int symbol, Calendar calendar, Canvas c, Paint p, LightMapOptions options)
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

        double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int x = (int) Math.round((minute / MINUTES_IN_DAY) * c.getWidth());
        int y = c.getHeight() / 2;

        SunSymbolBitmap.drawSunSymbol(symbol, x, y, pointRadius, c, p, options.values);

        int w = c.getWidth();
        if (x + pointRadius > w) {    // point cropped at image bounds, so translate and draw it again
            SunSymbolBitmap.drawSunSymbol(symbol, x - w, y, pointRadius, c, p, options.values);
        } else if (x - pointRadius < 0) {
            SunSymbolBitmap.drawSunSymbol(symbol, x + w, y, pointRadius, c, p, options.values);
        }
    }

    /////////////////////////////////////////////

    protected void drawRect(Canvas c, Paint p)
    {
        int w = c.getWidth();
        int h = c.getHeight();
        c.drawRect(0, 0, w, h, p);
    }

    protected void drawVerticalLine(Calendar calendar0, SuntimesRiseSetData data, int lineWidth, Canvas c, Paint p)
    {
        Location location = data.location();
        if (location != null)
        {
            Calendar calendar = Calendar.getInstance(WidgetTimezones.localMeanTime(location));
            calendar.setTimeInMillis(calendar0.getTimeInMillis());
            double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            int x = (int) Math.round((minute / MINUTES_IN_DAY) * c.getWidth());
            c.drawRect(x - (lineWidth / 2f), 0, x + (lineWidth / 2f), c.getHeight(), p);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean drawRect(LightMapOptions options, SuntimesRiseSetData data, Canvas c, Paint p )
    {
        Calendar riseTime = data.sunriseCalendarToday();
        Calendar setTime = data.sunsetCalendarToday();
        if (riseTime == null && setTime == null)
        {
            return false;
        }

        Location location = data.location();
        if (location != null)
        {
            TimeZone lmt = WidgetTimezones.localMeanTime(location);
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
        }

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

    protected void drawVerticalLine(Calendar calendar, int lineWidth, Canvas c, Paint p, int color, @Nullable DashPathEffect effect)
    {
        double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int x = (int) Math.round((minute / MINUTES_IN_DAY) * c.getWidth());
        SunSymbolBitmap.drawVerticalLine(x, lineWidth, c, p, color, effect);
    }

    protected void drawCross(Calendar calendar, int radius, int strokeWidth, Canvas c, Paint p, int color, @Nullable DashPathEffect effect)
    {
        double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int cX = (int) Math.round((minute / MINUTES_IN_DAY) * c.getWidth());
        int cY = c.getHeight() / 2;
        SunSymbolBitmap.drawCross(cX, cY, radius, strokeWidth, c, p, color, effect);
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
            SunSymbolBitmap.drawPoint(x, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);

            if (x + radius > w) {    // point cropped at image bounds, so translate and draw it again
                SunSymbolBitmap.drawPoint(x - w, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
            } else if (x - radius < 0) {
                SunSymbolBitmap.drawPoint(x + w, y, radius, strokeWidth, c, p, fillColor, strokeColor, strokeEffect);
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
