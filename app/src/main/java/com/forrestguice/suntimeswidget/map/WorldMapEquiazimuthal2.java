/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * WorldMapEquiazimuthal
 * An azimuthal projection centered on arbitrary coordinates.
 */
public class WorldMapEquiazimuthal2 extends WorldMapEquiazimuthal
{
    protected double[] center = new double[] {90, 0};
    public double[] getCenter() {
        return center;
    }

    @Override
    public double[] getMatrix() {
        return matrix;
    }
    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public double[] initMatrix()
    {
        long bench_start = System.nanoTime();

        int[] size = matrixSize();
        int w = size[0];
        int h = size[1];
        double[] v = new double[w * h * 3];

        double squareR = (0.5 * w + 1) * (0.5 * w + 1);
        double negPiOver2 = -0.5 * Math.PI;
        double[] polar = new double[2];

        double x, y;
        double radLon, cosLon, sinLon, sinLat;
        double radLat, cosLat;

        double squareP, squareX, squareY;
        for (int i = 0; i < w; i++)
        {
            x = ((double)i) - 180d;   // [-180,180]
            squareX = x * x;
            if (x == 0) {
                x += 0.0001;
            }

            for (int j = 0; j < h; j++)
            {
                y = ((double)(h - j)) - 180d;   // [-180,180]
                squareY = y * y;
                squareP = squareX + squareY;
                if (squareP > squareR)
                    continue;
                //Log.d("DEBUG", "pX: " + x + ", pY: " + y);

                polar[0] = Math.atan(x / y);                     // theta
                polar[1] = Math.toRadians(Math.sqrt(squareP));   // p

                radLon = polar[0];
                radLat = (y < 0) ? negPiOver2 - polar[1]
                        : Math.asin(-1 * Math.cos(polar[1]));
                //Log.d("DEBUG", "angle: " + polar[0] + ", dist: " + polar[1]);

                cosLat = Math.cos(radLat);
                cosLon = Math.cos(radLon);
                sinLon = Math.sin(radLon);

                v[i + (360 * j)] = cosLon * cosLat;
                v[i + (360 * (360 + j))] = sinLon * cosLat;
                v[i + (360 * (720 + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equiazimuthal world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        return v;
    }

    /**
     * point (angle, distance) from center[lat,lon]
     * @param lat [-90,90] north
     * @param lon [-180,180] east
     * @return [angleDegrees][distanceOfPI]
     */
    @Override
    protected double[] toPolar(double lat, double lon)
    {
        // x = k` * cosLat * sinDistance
        // y = k` * (cosLat1 * sinLat + sinLat1 * cosLat * cosDistance)
        // here
        //     k` = c / sin C
        // and
        //     cosC = sinLat1 * sinLat + cosLat1 * cosLat * cosDistance
        // where c is the angular distance from center

        double distance = lon - center[1];
        double radDistance = Math.toRadians(distance);
        double sinDistance = Math.sin(radDistance);
        double cosDistance = Math.cos(radDistance);

        double radLat1 = Math.toRadians(center[0]);
        double sinLat1 = Math.sin(radLat1);
        double cosLat1 = Math.cos(radLat1);

        double radLat = Math.toRadians(lat);
        double sinLat = Math.sin(radLat);
        double cosLat = Math.cos(radLat);

        double cosC = (sinLat1 * sinLat) + (cosLat1 * cosLat * cosDistance);
        double c = Math.acos(cosC);
        double k = c / Math.sin(c);
        double east = k * cosLat * sinDistance;
        double north = k * ((cosLat1 * sinLat) + (sinLat1 * cosLat * cosDistance));

        double[] polar = new double[2];
        polar[0] = Math.toDegrees(Math.atan2(east, north));
        polar[1] = Math.toDegrees(c);
        //if (polar[1] > 360) {
        //    Log.d(WorldMapView.LOGTAG, "toPolar: [" + lat + ", " + lon + "] -> [" + polar[0] + ", " + polar[1] + "]");
        //}
        return polar;
    }

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options)
    {
        long bench_start = System.nanoTime();
        if (w <= 0 || h <= 0) {
            return null;
        }

        center = options.center;
        if (matrix == null) {
            matrix = initMatrix();
        }

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        if (!paintInitialized) {
            initPaint(options);
        }

        ////////////////
        // draw base map
        drawMap(c, w, h, paintForeground, options);
        if (options.showMajorLatitudes) {
            drawMajorLatitudes(c, w, h, mid, options);
        }

        drawData: if (data != null)
        {
            Calendar now = mapTime(data, options);
            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.SunPosition sunPos = calculator.getSunPosition(now);
            SuntimesCalculator.MoonPosition moonPos = calculator.getMoonPosition(now);
            Location location = data.location();

            if (sunPos == null || moonPos == null) {
                Log.e(WorldMapView.LOGTAG, "not supported by this data source");
                break drawData;
            }

            long gmtMillis = now.getTimeInMillis() + (long)(WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(now.get(Calendar.MONTH)) * 60 * 1000);
            double gmtHours = (((gmtMillis / 1000d) / 60d) / 60d) % 24d;
            double gmtArc = gmtHours * 15d;

            double ghaSun = gmtArc;            // [0, 360] west
            if (ghaSun < 180)
                ghaSun += 180;
            else if (ghaSun > 180)
                ghaSun -= 180;

            double ghaSun180 = ghaSun;         // gha adjusted to [180, -180] west
            if (ghaSun180 > 180)
                ghaSun180 = ghaSun180 - 360;

            double[] sunPos2 = gha(location, sunPos);
            //Log.d("DEBUG", "gmtHours is " + gmtHours + ", gmtArc is " + gmtArc + ", ghaSun is " + ghaSun + " (" + sunPos2[0] + "), ghaSun180 is " + ghaSun180);

            double sunLon = -1 * ghaSun180;  // gha180 adjusted to [-180, 180] east
            double sunLat = sunPos2[1];
            double[] sunUp = unitVector(sunLat, sunLon);

            double[] moonPos2 = gha(location, moonPos);                        // [180, -180] west
            if (moonPos2[0] > 180)
                moonPos2[0] = moonPos2[0] - 360;

            double moonLon = -1 * moonPos2[0];
            double moonLat = moonPos2[1];
            double[] moonUp = unitVector(moonLat, moonLon);

            ////////////////
            // draw sunlight / moonlight
            // algorithm described at https://gis.stackexchange.com/questions/17184/method-to-shade-or-overlay-a-raster-map-to-reflect-time-of-day-and-ambient-light
            /*if (options.showSunPosition || options.showMoonPosition)
            {
                int[] size = matrixSize();
                Bitmap lightBitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                int[] pixels = initPixels(size[0], size[1], sunUp, moonUp, options);
                lightBitmap.setPixels(pixels, 0, size[0], 0, 0, size[0], size[1]);

                Rect src = new Rect(0,0,size[0]-1, size[1]-1);
                Rect dst = new Rect(0,0,w-1, h-1);
                c.drawBitmap(lightBitmap, src, dst, paintScaled);
                lightBitmap.recycle();
            }*/

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                double[] point = toCartesian(toPolar(sunLat, sunLon));
                int sunX = (int)(mid[0] + ((point[0] / 180d) * mid[0]) );
                int sunY = (int)(mid[1] - ((point[1] / 180d) * mid[1]) );
                drawSun(c, sunX, sunY, paintSun_fill, paintSun_stroke, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                double[] point = toCartesian(toPolar(moonLat, moonLon));
                int moonX = (int)(mid[0] + ((point[0] / 180d) * mid[0]) );
                int moonY = (int)(mid[1] - ((point[1] / 180d) * mid[1]) );
                drawMoon(c, moonX, moonY, paintMoon_fill, paintMoon_stroke, options);
            }

            ////////////////
            // draw locations
            if (options.locations != null) {
                drawLocations(c, w, h, paintLocation_fill, paintLocation_stroke, options);
            }

            if (options.translateToLocation)
            {
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(-1 * (float)sunLon);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), rotateMatrix, true);
                b = Bitmap.createBitmap(b, ((b.getWidth() - w) / 2), ((b.getHeight() - h) / 2), w, h);
            }
        }

        ////////////////
        // draw background color
        if (options.hasTransparentBaseMap) {
            c.drawCircle((float)mid[0], (float)mid[1], (float)mid[0] - 2, paintBackground);
        }

        // mask final image to fit within a circle (fixes fuzzy edges from base maps)
        Bitmap masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas maskedCanvas = new Canvas(masked);
        maskedCanvas.drawCircle((float)mid[0], (float)mid[1], (float)mid[0] - 2, paintMask_srcOver);
        maskedCanvas.drawBitmap(b, 0, 0, paintMask_srcIn);
        b.recycle();

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equiazimuthal2 world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return masked;
    }

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(options.hasTransparentBaseMap ? new PorterDuffXfermode(PorterDuff.Mode.DST_OVER) : new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        Paint.Style prevStyle = p.getStyle();
        PathEffect prevEffect = p.getPathEffect();
        float prevStrokeWidth = p.getStrokeWidth();

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setColor(Color.GREEN);
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        drawConnectedLines(c, createLatitudePath(mid, 0, -180, 0), p);

        p.setColor(Color.RED);
        drawConnectedLines(c, createLatitudePath(mid, 0, 0, 180), p);

        p.setColor(options.latitudeColors[0]);
        for (int i=-179; i<180; i+=15) {
            drawConnectedLines(c, createLongitudePath(mid, i), p);
        }
        p.setColor(Color.BLUE);
        drawConnectedLines(c, createLongitudePath(mid, 180), p);
        p.setColor(Color.YELLOW);
        drawConnectedLines(c, createLongitudePath(mid, 0), p);
        p.setColor(Color.RED);
        drawConnectedLines(c, createLongitudePath(mid, 90), p);
        p.setColor(Color.GREEN);
        drawConnectedLines(c, createLongitudePath(mid, -90), p);

        p.setColor(Color.CYAN);
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        for (int i=0; i<90; i+=5) {
            drawConnectedLines(c, createLatitudePath(mid, i), p);
        }
        p.setColor(Color.WHITE);
        for (int i=-90; i<0; i+=5) {
            drawConnectedLines(c, createLatitudePath(mid, i), p);
        }

        //p.setColor(options.latitudeColors[1]);
        //p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        //c.drawPath(createLatitudePath(mid, 23.439444), p);
        //c.drawPath(createLatitudePath(mid, -23.439444), p);

        p.setColor(options.latitudeColors[2]);
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        p.setColor(Color.RED);
        drawConnectedLines(c, createLatitudePath(mid, 66.560833), p);
        p.setColor(Color.MAGENTA);
        drawConnectedLines(c, createLatitudePath(mid, -66.560833), p);

        p.setStyle(prevStyle);
        p.setPathEffect(prevEffect);
        p.setStrokeWidth(prevStrokeWidth);
    }

    protected void drawConnectedLines(Canvas c, float[] lines, Paint p)
    {
        c.drawLines(lines, 0, lines.length, p);
        c.drawLines(lines, 2,lines.length-2, p);
    }

    protected float[] createLatitudePath(double[] mid, double latitude) {
        return createLatitudePath(mid, latitude, -180, 180);
    }

    protected float[] createLatitudePath(double[] mid, double latitude, double min, double max)
    {
        double[] point = toCartesian(toPolar(latitude, min));
        float x = (int)(mid[0] + ((point[0] / 180d) * mid[0]));
        float y = (int)(mid[1] - ((point[1] / 180d) * mid[1]));

        ArrayList<Float> path = new ArrayList<>();
        path.add(x);
        path.add(y);
        for (int longitude=(int)min; longitude <= max; longitude++)
        {
            point = toCartesian(toPolar(latitude, longitude));
            x = (int)(mid[0] + ((point[0] / 180d) * mid[0]));
            y = (int)(mid[1] - ((point[1] / 180d) * mid[1]));
            path.add(x);
            path.add(y);
        }

        float[] retvalue = new float[path.size()];
        for (int i=0; i<retvalue.length; i++) {
            retvalue[i] = path.get(i);
        }
        return retvalue;
    }

    protected float[] createLongitudePath(double[] mid, double longitude)
    {
        double[] point = toCartesian(toPolar(-88, longitude));
        float x = (int)(mid[0] + ((point[0] / 180d) * mid[0]));
        float y = (int)(mid[1] - ((point[1] / 180d) * mid[1]));

        ArrayList<Float> path = new ArrayList<>();
        path.add(x);
        path.add(y);
        for (int latitude = -88; latitude<88; latitude++)
        {
            point = toCartesian(toPolar(latitude, longitude));
            x = (int)(mid[0] + ((point[0] / 180d) * mid[0]));
            y = (int)(mid[1] - ((point[1] / 180d) * mid[1]));
            path.add(x);
            path.add(y);
        }

        float[] retvalue = new float[path.size()];
        for (int i=0; i<retvalue.length; i++) {
            retvalue[i] = path.get(i);
        }
        return retvalue;
    }

}
