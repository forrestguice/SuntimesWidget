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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class LineGraphBitmap
{
    public static final int MINUTES_IN_DAY = 24 * 60;
    public static final double MINUTES_IN_DAY_RATIO = 1d / (24 * 60);

    @Nullable
    protected LineGraphOptions options = null;
    @Nullable
    public LineGraphOptions getOptions() {
        return options;
    }

    @Nullable
    private SuntimesRiseSetDataset t_data = null;

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
                Double v = moonFill.get(path);
                boolean isDay = (v != null && v >= 0);
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
                Double v = moonPath.get(path);
                boolean isDay = (v!= null && v >= 0);
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
                Double v = sunFill.get(path);
                boolean isDay = (v != null && v >= 0);
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
                Double v = sunPath.get(path);
                boolean isDay = (v != null && v >= 0);
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

}
