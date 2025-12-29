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
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

/**
 * WorldMapEquirectangular
 */
public class WorldMapEquirectangular extends WorldMapTask.WorldMapProjection
{
    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        int[] r = new int[2];
        r[0] = (int) (mid[0] + ((lon / 180d) * mid[0]));
        r[1] = (int) (mid[1] - ((lat / 90d) * mid[1]));
        return r;
    }

    @Override
    public double[] fromBitmapCoords(int x, int y, double[] mid, int w, int h)
    {
        double[] r = new double[2];
        r[0] = 180d * (x - mid[0]) / mid[0];
        r[1] = 90d * (y - mid[1]) / -mid[1];
        return r;
    }

    protected boolean paintInitialized = false;
    protected Paint paintBackground = null;
    protected Paint paintForeground = null;
    protected Paint paintMoonlight = null;
    protected Paint paintSunshadow = null;
    protected Paint paintLocation_fill = null, paintLocation_stroke = null;
    protected Paint paintMask_srcIn = null;
    protected Paint paintMask_srcOver = null;
    protected Paint paintMoon_fill = null;
    protected Paint paintMoon_stroke = null;
    protected Paint paintSun_fill = null;
    protected Paint paintSun_stroke = null;
    protected Paint paintGrid = null;

    @Override
    public void initPaint(WorldMapTask.WorldMapOptions options)
    {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(options.colors.getColor(WorldMapColorValues.COLOR_BACKGROUND));
        paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        paintForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintForeground.setColorFilter(new LightingColorFilter(options.foregroundColor, 0));

        paintMask_srcOver = new Paint(Paint.ANTI_ALIAS_FLAG);    // to create a mask
        paintMask_srcOver.setColor(Color.WHITE);
        paintMask_srcOver.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        paintMask_srcIn = new Paint(Paint.ANTI_ALIAS_FLAG);      // to apply a mask
        paintMask_srcIn.setColor(Color.WHITE);
        paintMask_srcIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        paintSunshadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSunshadow.setColor(options.colors.getColor(WorldMapColorValues.COLOR_SUN_SHADOW));
        paintSunshadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        paintMoonlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoonlight.setColor(options.colors.getColor(WorldMapColorValues.COLOR_MOON_LIGHT));
        paintMoonlight.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        paintLocation_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLocation_fill.setStyle(Paint.Style.FILL);
        paintLocation_fill.setColor(options.colors.getColor(WorldMapColorValues.COLOR_POINT_FILL));

        paintLocation_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLocation_stroke.setStyle(Paint.Style.STROKE);
        paintLocation_stroke.setColor(options.colors.getColor(WorldMapColorValues.COLOR_POINT_STROKE));
        paintLocation_stroke.setStrokeWidth(0.5f);

        paintSun_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSun_fill.setStyle(Paint.Style.FILL);
        paintSun_fill.setColor(options.colors.getColor(WorldMapColorValues.COLOR_SUN_FILL));

        paintSun_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSun_stroke.setStyle(Paint.Style.STROKE);
        paintSun_stroke.setColor(options.colors.getColor(WorldMapColorValues.COLOR_SUN_STROKE));

        paintMoon_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoon_fill.setStyle(Paint.Style.FILL);
        paintMoon_fill.setColor(options.colors.getColor(WorldMapColorValues.COLOR_MOON_FILL));

        paintMoon_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoon_stroke.setStyle(Paint.Style.STROKE);
        paintMoon_stroke.setColor(options.colors.getColor(WorldMapColorValues.COLOR_MOON_STROKE));

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setXfermode(options.hasTransparentBaseMap ? new PorterDuffXfermode(PorterDuff.Mode.DST_OVER) : new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeCap(Paint.Cap.ROUND);

        paintInitialized = true;
    }

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options)
    {
        long bench_start = System.nanoTime();
        if (w <= 0 || h <= 0) {
            return null;
        }

        double[] matrix = getMatrix();

        double[] mid = new double[2];
        mid[0] = w/2d;
        mid[1] = h/2d;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        if (!paintInitialized) {
            initPaint(options);
        }

        drawMap(c, w, h, paintForeground, options);
        if (options.showDebugLines) {
            drawDebugLines(c, w, h, mid, options);
        } else if (options.showMajorLatitudes) {
            drawMajorLatitudes(c, w, h, mid, options);
        }
        if (options.showGrid) {
            drawGrid(c, w, h, mid, options);
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

            long gmtMillis = now.getTimeInMillis() + (long)(TimeZones.ApparentSolarTime.equationOfTimeOffset(now.get(Calendar.MONTH)) * 60 * 1000);
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
                if (sunMaskBitmap == null || moonMaskBitmap == null) {
                    initBitmap(size[0], size[1]);
                }

                int z = 0;
                int k0, k1, k2;
                double v0, v1, v2;
                double sunIntensity, moonIntensity;
                int[] sun_pixels = new int[size[0] * size[1]];
                int[] moon_pixels = new int[size[0] * size[1]];
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
                            if (sunIntensity <= 0) {
                                sun_pixels[z] = Color.WHITE;
                            }
                        }

                        if (options.showMoonLight)
                        {
                            moonIntensity = (moonUp[0] * v0) + (moonUp[1] * v1) + (moonUp[2] * v2);
                            if (moonIntensity > 0) {
                                moon_pixels[z] = Color.WHITE;
                            }
                        }
                        z++;
                    }
                }
                sunMaskBitmap.setPixels(sun_pixels, 0, size[0], 0, 0, size[0], size[1]);
                moonMaskBitmap.setPixels(moon_pixels, 0, size[0], 0, 0, size[0], size[1]);

                // draw sun shadow
                Bitmap sunMask = Bitmap.createScaledBitmap(sunMaskBitmap, w, h, true);
                Bitmap shadowBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas shadowCanvas = new Canvas(shadowBitmap);
                shadowCanvas.drawBitmap(sunMask, 0, 0, paintMask_srcOver);

                if (options.map_night != null)
                {
                    Bitmap nightBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas nightCanvas = new Canvas(nightBitmap);
                    options.map_night.setBounds(0, 0, nightCanvas.getWidth(), nightCanvas.getHeight());
                    options.map_night.draw(nightCanvas);

                    shadowCanvas.drawBitmap(nightBitmap, 0, 0, paintMask_srcIn);
                    nightBitmap.recycle();

                } else {
                    shadowCanvas.drawPaint(paintSunshadow);
                }

                c.drawBitmap(shadowBitmap, 0, 0, paintMask_srcOver);
                shadowBitmap.recycle();
                sunMask.recycle();

                // draw moon light
                Bitmap moonMask = Bitmap.createScaledBitmap(moonMaskBitmap, w, h, true);
                Bitmap moonBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas moonCanvas = new Canvas(moonBitmap);
                moonCanvas.drawBitmap(moonMask, 0, 0, paintMask_srcOver);
                moonCanvas.drawPaint(paintMoonlight);

                c.drawBitmap(moonBitmap, 0, 0, paintMask_srcOver);
                moonBitmap.recycle();
                moonMask.recycle();
            }

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                int sunX = (int) (mid[0] - ((ghaSun180 / 180d) * mid[0]));
                int sunY = (int) (mid[1] - ((sunPos.declination / 90d) * mid[1]));
                drawSun(c, sunX, sunY, paintSun_fill, paintSun_stroke, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                int moonX = (int) (mid[0] - ((moonPos2[0] / 180d) * mid[0]));
                int moonY = (int) (mid[1] - ((moonPos2[1] / 90d) * mid[1]));
                drawMoon(c, moonX, moonY, paintMoon_fill, paintMoon_stroke, options);
            }
        }

        ////////////////
        // draw locations
        if (options.locations != null) {
            drawLocations(c, w, h, paintLocation_fill, paintLocation_stroke, options);
        }

        if (options.hasTransparentBaseMap) {
            c.drawRect(0, 0, w, h, paintBackground);
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equirectangular world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return b;
    }

    protected Bitmap makeMaskedBitmap(int w, int h, Bitmap b) {
        return b;
    }

    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public void resetMatrix() {
        matrix = null;
    }

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

    private Bitmap sunMaskBitmap = null;
    private Bitmap moonMaskBitmap = null;

    private void initBitmap(int w, int h)
    {
        sunMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        moonMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
    }

    @Override
    protected int k(int i, int j, int k)
    {
        return i + (720 * ((360 * k) + j));
    }

    @Override
    public double[] getMatrix()
    {
        if (matrix == null) {
            matrix = initMatrix();
        }
        return matrix;
    }

    @Override
    public int[] matrixSize()
    {
        return new int[] {720, 360};
    }

    @Override
    public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        for (int i=0; i < 180; i = i + 15)
        {
            double offset = (i / 180d) * mid[0];
            int eastX = (int)(mid[0] + offset);
            int westX = (int)(mid[0] - offset);
            c.drawLine(eastX, 0, eastX, h, paintGrid);
            c.drawLine(westX, 0, westX, h, paintGrid);
        }

        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        for (int i=0; i < 90; i = i + 15)
        {
            double offset = (i / 90d) * mid[1];
            int northY = (int)(mid[1] + offset);
            int southY = (int)(mid[1] - offset);
            c.drawLine(0, northY, w, northY, paintGrid);
            c.drawLine(0, southY, w, southY, paintGrid);
        }
    }

    private static double r_tropics = (23.439444 / 90d);
    private static double r_polar = (66.560833 / 90d);

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        Paint p = paintGrid;
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);

        p.setColor(options.latitudeColors[0]);                    // equator, prime meridian
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        c.drawLine(0, (int)mid[1], w, (int)mid[1], p);
        c.drawLine((int)mid[0], 0, (int)mid[0], h, p);

        p.setColor(options.latitudeColors[0]);                    // east, west meridians
        c.drawLine((int)mid[0]/2f, 0, (int)mid[0]/2f, h, p);
        c.drawLine((int)(3*mid[0]/2f), 0, (int)(3*mid[0]/2f), h, p);

        double tropics = r_tropics * mid[1];
        int tropicsY0 = (int)(mid[1] + tropics);
        int tropicsY1 = (int)(mid[1] - tropics);
        p.setColor(options.latitudeColors[1]);                    // tropics
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        c.drawLine(0, tropicsY0, w, tropicsY0, p);
        c.drawLine(0, tropicsY1, w, tropicsY1, p);

        double polar = r_polar * mid[1];
        int polarY0 = (int)(mid[1] + polar);
        int polarY1 = (int)(mid[1] - polar);
        p.setColor(options.latitudeColors[2]);                    // polar
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        c.drawLine(0, polarY0, w, polarY0, p);
        c.drawLine(0, polarY1, w, polarY1, p);
    }

    @Override
    public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        Paint p = paintGrid;
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);

        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        p.setColor(Color.GREEN);
        c.drawLine(0, (int)mid[1], (int)mid[0], (int)mid[1], p);
        p.setColor(Color.RED);
        c.drawLine((int)mid[0], (int)mid[1], w, (int)mid[1], p);
        p.setColor(Color.YELLOW);                    // equator, prime meridian
        c.drawLine((int)mid[0], 0, (int)mid[0], h, p);
        p.setColor(Color.GREEN);
        c.drawLine((int)mid[0]/2f, 0, (int)mid[0]/2f, h, p);
        p.setColor(Color.RED);                       // east, west meridians
        c.drawLine((int)(3*mid[0]/2f), 0, (int)(3*mid[0]/2f), h, p);

        double tropics = r_tropics * mid[1];
        int tropicsY0 = (int)(mid[1] + tropics);
        int tropicsY1 = (int)(mid[1] - tropics);
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        p.setColor(Color.WHITE);                    // tropics
        c.drawLine(0, tropicsY0, w, tropicsY0, p);
        c.drawLine(0, tropicsY1, w, tropicsY1, p);

        double polar = r_polar * mid[1];
        int polarY0 = (int)(mid[1] + polar);
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        p.setColor(Color.GREEN);                  // polar
        c.drawLine(0, polarY0, w, polarY0, p);

        int polarY1 = (int)(mid[1] - polar);
        p.setColor(Color.RED);
        c.drawLine(0, polarY1, w, polarY1, p);
    }

}
