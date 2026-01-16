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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_STROKE;

public class LightGraphBitmap
{
    public static final int MINUTES_IN_DAY = 24 * 60;
    public static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    protected LightGraphOptions options = new LightGraphOptions();   // set in `makeBitmap`
    public LightGraphOptions getOptions() {
        return options;
    }

    @Nullable
    protected SuntimesRiseSetDataset[] yearData = null;    // set in `makeBitmap`
    public SuntimesRiseSetDataset[] getYearData() {
        return yearData;
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

            TimeMode[] modes = new TimeMode[] { TimeMode.OFFICIAL, TimeMode.CIVIL, TimeMode.NAUTICAL, TimeMode.ASTRONOMICAL };

            for (int i = 0; i < yearData.length; i++)
            {
                Calendar date = Calendar.getInstance(timezone);
                date.setTimeInMillis(date0.getTimeInMillis());
                date.add(Calendar.DATE, i);

                SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(data0, modes);
                data.setTimeZone(timezone);
                data.setTodayIs(date);
                data.calculateData(context);
                yearData[i] = data;
            }

            long bench_end = System.nanoTime();
            Log.d("BENCH", "make light graph (data) :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            return yearData;
        }
        return null;
    }

    public Bitmap makeBitmap(SuntimesRiseSetDataset[] yearData, int w, int h, @NonNull LightGraphOptions options)
    {
        long bench_start = System.nanoTime();

        if (w <= 0 || h <= 0 || options == null) {
            return null;
        }
        this.options = options;
        this.yearData = yearData;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        b.setDensity(options.densityDpi);
        Canvas c = new Canvas(b);
        initPaint();

        drawBackground(c, paintBackground, options);

        if (yearData != null)
        {
            Calendar now = Calendar.getInstance(yearData[0].timezone()); // graphTime(yearData[0], options);
            options.setLocation(yearData[0].location());

            drawPaths(now, yearData, c, paintPath, options);
            drawGrid(now, yearData, c, p, options);
            drawAxisUnder(now, yearData, c, p, options);
            drawAxisOver(now, yearData, c, p, options);
            drawPoints(c, p, options);
            drawNow(now, c, p, options);
            drawLabels(now, yearData, c, paintText, options);
        }

        long bench_end = System.nanoTime();
        Log.d("BENCH", "make light graph :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
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

    /*protected Calendar graphTime(@Nullable SuntimesRiseSetDataset data, @NonNull LightGraphOptions options)
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
    }*/

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

    protected void drawPoints(Canvas c, Paint p, LightGraphOptions options)
    {
        if (options.sunPath_show_points && (options.sunPath_show_line || options.sunPath_show_fill))
        {
            float[][] points = createPathPoints(c, options);
            if (points != null)
            {
                double pointSize = Math.sqrt(c.getWidth() * c.getHeight()) / options.sunPath_points_width;
                for (float[] point : points) {
                    drawPoint(point[0], point[1], (int)pointSize, 0, c, p, options.colors.getColor(COLOR_POINT_FILL), options.colors.getColor(COLOR_POINT_STROKE), null);
                }
            }
        }
    }

    protected void drawPaths(Calendar now, SuntimesRiseSetDataset[] data, Canvas c, Paint p, LightGraphOptions options)
    {
        if (options.sunPath_show_line || options.sunPath_show_fill)
        {
            TimeMode nightBoundary = TimeMode.OFFICIAL;
            paintPath.setColor(options.colors.getColor(COLOR_NIGHT));
            drawPath(now, data, nightBoundary, true, c, paintPath, options);
            drawPath(now, data, nightBoundary, false, c, paintPath, options);

            if (options.showCivil)
            {
                nightBoundary = TimeMode.CIVIL;
                paintPath.setColor(options.colors.getColor(COLOR_CIVIL));
                drawPath(now, data, TimeMode.OFFICIAL, true, c, paintPath, options);
                drawPath(now, data, TimeMode.OFFICIAL, false, c, paintPath, options);
            }

            if (options.showNautical)
            {
                nightBoundary = TimeMode.NAUTICAL;
                paintPath.setColor(options.colors.getColor(COLOR_NAUTICAL));
                drawPath(now, data, TimeMode.CIVIL, true, c, paintPath, options);
                drawPath(now, data, TimeMode.CIVIL, false, c, paintPath, options);
            }

            if (options.showAstro)
            {
                nightBoundary = TimeMode.ASTRONOMICAL;
                paintPath.setColor(options.colors.getColor(COLOR_ASTRONOMICAL));
                drawPath(now, data, TimeMode.NAUTICAL, true, c, paintPath, options);
                drawPath(now, data, TimeMode.NAUTICAL, false, c, paintPath, options);
            }

            paintPath.setColor(options.colors.getColor(COLOR_NIGHT));
            drawPath(now, data, nightBoundary, true, c, paintPath, options);
            drawPath(now, data, nightBoundary, false, c, paintPath, options);
        }
    }

    private final ArrayList<Path> sun_paths = new ArrayList<>();
    private final HashMap<Path, Double> sun_hours = new HashMap<>();


    protected void drawPath(Calendar now, SuntimesRiseSetDataset[] data, TimeMode mode, boolean rising, Canvas c, Paint p, LightGraphOptions options)
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

    private float[][] createPathPoints(Canvas c, LightGraphOptions options)
    {
        EarliestLatestSunriseSunsetData data = options.earliestLatestData;
        if (data == null) {
            return null;
        }

        double[][] points = new double[][]
        {
                new double[] { data.early_sunrise_day,  data.early_sunrise_hour },
                new double[] { data.early_sunset_day,  data.early_sunset_hour },
                new double[] { data.late_sunrise_day,  data.late_sunrise_hour },
                new double[] { data.late_sunset_day,  data.late_sunset_hour },
        };

        ArrayList<float[]> p = new ArrayList<>();
        for (double[] point : points)
        {
            if (point != null)
            {
                Calendar calendar = Calendar.getInstance(options.timezone);
                calendar.set(Calendar.DAY_OF_YEAR, (int) point[0]);
                double offset = lmtOffsetHours(calendar.getTimeInMillis()) - lmtOffsetHours();  // offset lmt_hour to lmt_hour + dst

                float x = (float) daysToBitmapCoords(c, point[0], options);
                float y = (float) hoursToBitmapCoords(c, wrapHour(point[1] + offset), options);
                p.add(new float[] {x, y});
            }
        }
        return p.toArray(new float[0][0]);
        //Log.d("DEBUG", "sunPath_points: " + options.sunPath_points.length);
    }

    protected HashMap<Path, Double> createSunPath(Calendar now, SuntimesRiseSetDataset[] data, TimeMode mode, boolean rising, Canvas c, LightGraphOptions options, boolean closed, ArrayList<Path> paths, HashMap<Path,Double> hours)
    {
        paths.clear();
        hours.clear();

        Calendar event;
        double hour;
        double hour_prev = (rising ? 0 : 24);  // previous iteration
        float x = 0, y = 0;
        double lmtOffsetHours = lmtOffsetHours();

        long dayLength;
        int nullHour;
        int nullHour0 = (rising ? 0 : 24);    // outward to edges to reveal day color
        int nullHour1 = 12;                   // inward to middle to overdraw night color

        Path path = null;
        int day = 0;
        while (day < data.length)
        {
            SuntimesRiseSetData d = data[day].getData(mode.name());
            event = (rising ? d.sunriseCalendarToday() : d.sunsetCalendarToday());
            dayLength = d.dayLengthToday();
            nullHour = (dayLength == SuntimesRiseSetDataset.NONE_NIGHT) ? nullHour1 : nullHour0;
            hour = (event != null) ? wrapHour(tzHour(event) - lmtOffsetHours) : nullHour;    // lmt_hour + dst

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
        long lonOffsetMs = Math.round(options.longitude * MILLIS_IN_DAY / 360d);
        long rawOffsetMs = options.timezone.getRawOffset();
        return (rawOffsetMs - lonOffsetMs) / (1000d * 60d * 60d);
    }

    /**
     * @param date long date+time
     * @return offset in hours between time zone and local mean time (with dst)
     */
    protected double lmtOffsetHours(long date) {
        return lmtOffsetHours(date, options.timezone, options.longitude);
    }

    protected void drawAxisX(Canvas c, Paint p, LightGraphOptions options)
    {
        TimeZone tz = ((yearData != null && yearData[0] != null) ? yearData[0].timezone() : TimeZone.getDefault());
        Calendar calendar0 = Calendar.getInstance(options.timezone);
        Calendar calendar = Calendar.getInstance(tz);
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

        TimeZone tz = (yearData != null && yearData[0] != null ? yearData[0].timezone() : TimeZone.getDefault());
        Calendar calendar0 = Calendar.getInstance(options.timezone);
        Calendar calendar = Calendar.getInstance(tz);
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
        Calendar calendar = events.get(events.size() - 1);
        float x0 = -1 * (c.getWidth() - (float) daysToBitmapCoords(c, ((calendar != null) ? calendar.get(Calendar.DAY_OF_YEAR) : 1), options));
        for (int i=0; i<events.size(); i++)
        {
            Calendar event = events.get(i);
            float x = (float) daysToBitmapCoords(c, ((event != null) ? event.get(Calendar.DAY_OF_YEAR) : 1), options);
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
            lmtHour = wrapHour(tzHour(calendar0) - lmtOffsetHours);
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
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            double hour = wrapHour(tzHour(calendar) - lmtOffsetHours());
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

    /**
     * @param hour raw hour value
     * @return hour value within range [0, 24]
     */
    protected static double wrapHour(double hour)
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
    protected static double clampHour(double hour)
    {
        if (hour < 0) {
            hour = 0;
        }
        if (hour > 24) {
            hour = 24;
        }
        return hour;
    }

    public static double lmtHour(@NonNull Calendar event, double longitude) {
        return wrapHour(tzHour(event) - lmtOffsetHours(event.getTimeInMillis(), event.getTimeZone(), longitude));
    }

    public static double tzHour(@NonNull Calendar event)
    {
        return event.get(Calendar.HOUR_OF_DAY)
                + (event.get(Calendar.MINUTE) / 60d)
                + (event.get(Calendar.SECOND) / (60d * 60d))
                + (event.get(Calendar.MILLISECOND) / (60d * 60d * 1000d));
    }

    public static double lmtOffsetHours(long date, TimeZone tz, double longitude)
    {
        long lonOffsetMs = Math.round(longitude * MILLIS_IN_DAY / 360d);
        return (tz.getOffset(date) - lonOffsetMs) / (1000d * 60d * 60d);
    }
}
