/**
    Copyright (C) 2018 Forrest Guice
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
import android.graphics.Paint;
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
        // draw background
        p.setColor(options.backgroundColor);
        c.drawRect(0, 0, w, h, p);
        if (options.map != null)
        {
            drawMap(c, w, h, p, options);
        }

        ////////////////
        // draw grid
        if (options.showGrid)
        {
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

        if (options.showMajorLatitudes)
        {
            p.setColor(options.latitudeColors[0]);                    // equator
            c.drawLine(0, (int)mid[1], w, (int)mid[1], p);

            double tropics = (23.439444 / 90d) * mid[1];
            int tropicsY0 = (int)(mid[1] + tropics);
            int tropicsY1 = (int)(mid[1] - tropics);
            p.setColor(options.latitudeColors[1]);                    // tropics
            c.drawLine(0, tropicsY0, w, tropicsY0, p);
            c.drawLine(0, tropicsY1, w, tropicsY1, p);

            double polar = (66.560833 / 90d) * mid[1];
            int polarY0 = (int)(mid[1] + polar);
            int polarY1 = (int)(mid[1] - polar);
            p.setColor(options.latitudeColors[2]);                    // polar
            c.drawLine(0, polarY0, w, polarY0, p);
            c.drawLine(0, polarY1, w, polarY1, p);
        }

        drawData: if (data != null)
        {
            Calendar now = data.nowThen(data.calendar());
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
                int w0 = (w < 512 ? w : 512);
                int h0 = w0 / 2;

                double iw0 = (1d / w0) * 360d;
                double ih0 = (1d / h0) * 180d;

                Bitmap lightBitmap = Bitmap.createBitmap(w0, h0, Bitmap.Config.ARGB_8888);
                Canvas lightCanvas = new Canvas(lightBitmap);

                Paint paintShadow = new Paint();
                paintShadow.setColor(options.sunShadowColor);
                paintShadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                Paint paintMoonlight = new Paint();
                paintMoonlight.setColor(options.moonLightColor);
                paintMoonlight.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                double radLon, cosLon, sinLon;
                double radLat, cosLat;
                double[] v = new double[3];
                double sunIntensity, moonIntensity;

                for (int i = 0; i < w0; i++)
                {
                    radLon = Math.toRadians( ((double) i * iw0) - 180d );  // i in [0,w] to [0,360] to [-180,180]
                    cosLon = Math.cos(radLon);
                    sinLon = Math.sin(radLon);

                    for (int j = 0; j < h0; j++)
                    {
                        radLat = Math.toRadians( -1 * (((double) j * ih0) - 90d) );      // j in [0,h] to [0,180] to [-90,90] (inverted to canvas)
                        cosLat = Math.cos(radLat);

                        v[0] = cosLon * cosLat;
                        v[1] = sinLon * cosLat;
                        v[2] = Math.sin(radLat);

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
                scaledLightBitmap.recycle();
                lightBitmap.recycle();
            }

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                int sunX = (int) (mid[0] - ((ghaSun180 / 180d) * mid[0]));
                int sunY = (int) (mid[1] - ((sunPos.declination / 90d) * mid[1]));
                drawSun(c, sunX, sunY, p, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                int moonX = (int) (mid[0] - ((moonPos2[0] / 180d) * mid[0]));
                int moonY = (int) (mid[1] - ((moonPos2[1] / 90d) * mid[1]));
                drawMoon(c, moonX, moonY, p, options);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equirectangular world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return b;
    }
}
