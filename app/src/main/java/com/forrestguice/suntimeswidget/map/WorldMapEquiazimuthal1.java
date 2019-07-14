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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

/**
 * WorldMapEquiazimuthal
 * An azimuthal projection centered on south pole.
 */
public class WorldMapEquiazimuthal1 extends WorldMapEquiazimuthal
{
    @Override
    protected double[] toPolar(double lat, double lon)
    {
        double[] polar = new double[2];
        polar[0] = 360 - lon;
        polar[1] = -90 - lat;
        //Log.d(WorldMapView.LOGTAG, "toPolar: [" + lat + ", " + lon + "] -> [" + polar[0] + ", " + polar[1] + "]");
        return polar;
    }

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

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options)
    {
        long bench_start = System.nanoTime();
        if (w <= 0 || h <= 0)
        {
            return null;
        }

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        ////////////////
        // draw base map
        drawMap(c, w, h, options);
        if (options.showMajorLatitudes) {
            drawMajorLatitudes(c, w, h, null, options);
        }

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
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
                int w0 = 360;
                int h0 = 360;

                double iw0 = (1d / w0) * 360d;
                double ih0 = (1d / h0) * 360d;

                Bitmap lightBitmap = Bitmap.createBitmap(w0, h0, Bitmap.Config.ARGB_8888);
                Canvas lightCanvas = new Canvas(lightBitmap);

                Paint paintShadow = new Paint();
                paintShadow.setColor(options.sunShadowColor);
                paintShadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                Paint paintMoonlight = new Paint();
                paintMoonlight.setColor(options.moonLightColor);
                paintMoonlight.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                double radLon, cosLon, sinLon, sinLat;
                double radLat, cosLat;
                double[] v = new double[3];
                double sunIntensity, moonIntensity;
                double squareR = (0.5 * w0 + 1) * (0.5 * w0 + 1);
                double negPiOver2 = -0.5 * Math.PI;
                double[] polar = new double[2];

                double x, y;
                for (int i = 0; i < w0; i++)
                {
                    x = ((double)i * iw0) - 180d;   // [-180,180]
                    double squareX = x * x;
                    if (x == 0) {
                        x += 0.0001;
                    }

                    for (int j = 0; j < h0; j++)
                    {
                        y = ((double)(h0 - j) * ih0) - 180d;   // [-180,180]
                        double squareP = squareX + (y * y);
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
                        sinLat = Math.sin(radLat);
                        sinLon = Math.sin(radLon);

                        v[0] = cosLon * cosLat;
                        v[1] = sinLon * cosLat;
                        v[2] = sinLat;

                        if (options.showSunShadow)
                        {
                            sunIntensity = (sunUp[0] * v[0]) + (sunUp[1] * v[1]) + (sunUp[2] * v[2]);    // intensity = up.dotProduct(v)
                            if (sunIntensity <= 0) {                                                               // values less equal 0 are in shadow
                                lightCanvas.drawPoint(i, j, paintShadow);
                            }
                        }

                        if (options.showMoonLight)
                        {
                            moonIntensity = (moonUp[0] * v[0]) + (moonUp[1] * v[1]) + (moonUp[2] * v[2]);
                            if (moonIntensity > 0) {
                                lightCanvas.drawPoint(i, j, paintMoonlight);
                            }
                        }
                    }
                }

                Bitmap scaledLightBitmap = Bitmap.createScaledBitmap(lightBitmap, w, h, true);
                c.drawBitmap(scaledLightBitmap, 0, 0, p);
                lightBitmap.recycle();
                scaledLightBitmap.recycle();
            }

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                double[] point = toCartesian(toPolar(sunLat, sunLon));
                int sunX = (int)(mid[0] + ((point[0] / 180d) * mid[0]) );
                int sunY = (int)(mid[1] - ((point[1] / 180d) * mid[1]) );
                drawSun(c, sunX, sunY, null, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                double[] point = toCartesian(toPolar(moonLat, moonLon));
                int moonX = (int)(mid[0] + ((point[0] / 180d) * mid[0]) );
                int moonY = (int)(mid[1] - ((point[1] / 180d) * mid[1]) );
                drawMoon(c, moonX, moonY, null, options);
            }

            ////////////////
            // draw locations
            if (options.locations != null) {
                drawLocations(c, w, h, null, options);
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
        if (options.hasTransparentBaseMap)
        {
            Paint paintBackground = new Paint();
            paintBackground.setColor(options.backgroundColor);
            paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
            c.drawCircle((float)mid[0], (float)mid[1], (float)mid[0] - 2, paintBackground);
        }

        // mask final image to fit within a circle (fixes fuzzy edges from base maps)
        Bitmap masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas maskedCanvas = new Canvas(masked);
        Paint paintMask = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintMask.setColor(Color.WHITE);
        paintMask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        maskedCanvas.drawCircle((float)mid[0], (float)mid[1], (float)mid[0] - 2, paintMask);

        paintMask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        maskedCanvas.drawBitmap(b, 0, 0, paintMask);
        b.recycle();

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equiazimuthal world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return masked;
    }

}
