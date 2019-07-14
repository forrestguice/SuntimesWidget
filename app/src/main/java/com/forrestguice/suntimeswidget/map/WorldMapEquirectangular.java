/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

/**
 * WorldMapEquirectangular
 */
public class WorldMapEquirectangular extends WorldMapTask.WorldMapProjection
{
    @Override
    public int[] toBitmapCoords(int w, int h, double lat, double lon)
    {
        double[] m = new double[2];
        m[0] = w/2d;
        m[1] = h/2d;

        int[] r = new int[2];
        r[0] = (int) (m[0] + ((lon / 180d) * m[0]));
        r[1] = (int) (m[1] - ((lat / 90d) * m[1]));
        return r;
    }

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options)
    {
        long bench_start = System.nanoTime();
        if (w <= 0 || h <= 0) {
            return null;
        }

        if (matrix == null) {
            matrix = initMatrix();
        }

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        drawMap(c, w, h, options);
        if (options.showMajorLatitudes) {
            drawMajorLatitudes(c, w, h, null, options);
        }
        if (options.showGrid) {
            drawGrid(c, w, h, null, options);
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
            if (options.showSunPosition || options.showMoonPosition)
            {
                int[] size = matrixSize();
                Bitmap sunMaskBitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                Bitmap moonMaskBitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);

                int k0, k1, k2;
                double v0, v1, v2;
                double sunIntensity, moonIntensity;
                for (int j = 0; j < size[1]; j++)
                {
                    k0 = size[0] * j;
                    k1 = size[0] * (size[1] + j);
                    k2 = size[0] * ((size[1] * 2) + j);

                    for (int i = 0; i < size[0]; i++)
                    {
                        v0 = matrix[i + k0];
                        v1 = matrix[i + k1];
                        v2 = matrix[i + k2];

                        if (options.showSunShadow)
                        {
                            sunIntensity = (sunUp[0] * v0) + (sunUp[1] * v1) + (sunUp[2] * v2);    // intensity = up.dotProduct(v)
                            if (sunIntensity <= 0) {                                                               // values less equal 0 are in shadow
                                sunMaskBitmap.setPixel(i, j, Color.WHITE);
                            }
                        }

                        if (options.showMoonLight)
                        {
                            moonIntensity = (moonUp[0] * v0) + (moonUp[1] * v1) + (moonUp[2] * v2);
                            if (moonIntensity > 0) {
                                moonMaskBitmap.setPixel(i, j, Color.WHITE);
                            }
                        }
                    }
                }

                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(Color.WHITE);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                // draw sun shadow
                Bitmap sunMask = Bitmap.createScaledBitmap(sunMaskBitmap, w, h, true);
                Bitmap shadowBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas shadowCanvas = new Canvas(shadowBitmap);
                shadowCanvas.drawBitmap(sunMask, 0, 0, p);

                if (options.map_night != null)
                {
                    Paint paintShadow = new Paint();
                    paintShadow.setColor(Color.WHITE);
                    paintShadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

                    Bitmap nightBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas nightCanvas = new Canvas(nightBitmap);
                    options.map_night.setBounds(0, 0, nightCanvas.getWidth(), nightCanvas.getHeight());
                    options.map_night.draw(nightCanvas);

                    shadowCanvas.drawBitmap(nightBitmap, 0, 0, paintShadow);
                    nightBitmap.recycle();

                } else {
                    Paint paintShadow = new Paint();
                    paintShadow.setColor(options.sunShadowColor);
                    paintShadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    shadowCanvas.drawPaint(paintShadow);
                }

                c.drawBitmap(shadowBitmap, 0, 0, p);
                shadowBitmap.recycle();
                sunMask.recycle();
                sunMaskBitmap.recycle();

                // draw moon light
                Paint paintMoonlight = new Paint();
                paintMoonlight.setColor(options.moonLightColor);
                paintMoonlight.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

                Bitmap moonMask = Bitmap.createScaledBitmap(moonMaskBitmap, w, h, true);
                Bitmap moonBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas moonCanvas = new Canvas(moonBitmap);
                moonCanvas.drawBitmap(moonMask, 0, 0, p);
                moonCanvas.drawPaint(paintMoonlight);

                c.drawBitmap(moonBitmap, 0, 0, p);
                moonBitmap.recycle();
                moonMask.recycle();
                moonMaskBitmap.recycle();
            }

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                int sunX = (int) (mid[0] - ((ghaSun180 / 180d) * mid[0]));
                int sunY = (int) (mid[1] - ((sunPos.declination / 90d) * mid[1]));
                drawSun(c, sunX, sunY, null, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                int moonX = (int) (mid[0] - ((moonPos2[0] / 180d) * mid[0]));
                int moonY = (int) (mid[1] - ((moonPos2[1] / 90d) * mid[1]));
                drawMoon(c, moonX, moonY, null, options);
            }

            ////////////////
            // draw locations
            if (options.locations != null) {
                drawLocations(c, w, h, null, options);
            }
        }

        if (options.hasTransparentBaseMap)
        {
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(options.backgroundColor);
            backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
            c.drawRect(0, 0, w, h, backgroundPaint);
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equirectangular world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return b;
    }

    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public double[] initMatrix()
    {
        long bench_start = System.nanoTime();

        int[] size = matrixSize();
        double[] v = new double[size[0] * size[1] * 3];
        double iw0 = (1d / size[0]) * 360d;
        double ih0 = (1d / size[1]) * 180d;

        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        for (int i = 0; i < size[0]; i++)
        {
            radLon = Math.toRadians(((double) i * iw0) - 180d);  // i in [0,w] to [0,360] to [-180,180]
            cosLon = Math.cos(radLon);
            sinLon = Math.sin(radLon);

            for (int j = 0; j < size[1]; j++)
            {
                radLat = Math.toRadians(-1 * (((double) j * ih0) - 90d));      // j in [0,h] to [0,180] to [-90,90] (inverted to canvas)
                cosLat = Math.cos(radLat);

                v[i + (size[0] * j)] = cosLon * cosLat;
                v[i + (size[0] * (size[1] + j))] = sinLon * cosLat;
                v[i + (size[0] * ((size[1] * 2) + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equirectangular world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + size[0] + ", " + size[1]);
        return v;
    }

    @Override
    protected int k(int i, int j, int k)
    {
        return i + (720 * ((360 * k) + j));
    }

    @Override
    public int[] matrixSize()
    {
        return new int[] {720, 360};
    }

    protected void drawGrid(Canvas c, int w, int h, Paint p, WorldMapTask.WorldMapOptions options)
    {
        if (p == null) {
            p = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        p.setColor(options.gridXColor);
        for (int i=0; i < 180; i = i + 15)
        {
            double offset = (i / 180d) * mid[0];
            int eastX = (int)(mid[0] + offset);
            int westX = (int)(mid[0] - offset);
            c.drawLine(eastX, 0, eastX, h, p);
            c.drawLine(westX, 0, westX, h, p);
        }

        p.setColor(options.gridYColor);
        for (int i=0; i < 90; i = i + 15)
        {
            double offset = (i / 90d) * mid[1];
            int northY = (int)(mid[1] + offset);
            int southY = (int)(mid[1] - offset);
            c.drawLine(0, northY, w, northY, p);
            c.drawLine(0, southY, w, southY, p);
        }
    }

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, Paint p, WorldMapTask.WorldMapOptions options)
    {
        if (p == null) {
            p = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        p.setXfermode(new PorterDuffXfermode( options.hasTransparentBaseMap ? PorterDuff.Mode.DST_OVER : PorterDuff.Mode.SRC_OVER ));

        Paint.Style prevStyle = p.getStyle();
        PathEffect prevEffect = p.getPathEffect();
        float prevStrokeWidth = p.getStrokeWidth();

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);

        p.setColor(options.latitudeColors[0]);                    // equator
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        c.drawLine(0, (int)mid[1], w, (int)mid[1], p);

        double tropics = (23.439444 / 90d) * mid[1];
        int tropicsY0 = (int)(mid[1] + tropics);
        int tropicsY1 = (int)(mid[1] - tropics);
        p.setColor(options.latitudeColors[1]);                    // tropics
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        c.drawLine(0, tropicsY0, w, tropicsY0, p);
        c.drawLine(0, tropicsY1, w, tropicsY1, p);

        double polar = (66.560833 / 90d) * mid[1];
        int polarY0 = (int)(mid[1] + polar);
        int polarY1 = (int)(mid[1] - polar);
        p.setColor(options.latitudeColors[2]);                    // polar
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        c.drawLine(0, polarY0, w, polarY0, p);
        c.drawLine(0, polarY1, w, polarY1, p);

        p.setStyle(prevStyle);
        p.setPathEffect(prevEffect);
        p.setStrokeWidth(prevStrokeWidth);
        p.setXfermode(new PorterDuffXfermode( PorterDuff.Mode.SRC_OVER) );
    }
}
