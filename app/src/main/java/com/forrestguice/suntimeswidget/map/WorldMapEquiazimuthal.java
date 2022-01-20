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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

/**
 * WorldMapEquiazimuthal
 * An azimuthal projection centered on north pole.
 */
public class WorldMapEquiazimuthal extends WorldMapTask.WorldMapProjection
{
    /**
     * point (angle, distance) from north pole
     *   angle = longitude
     *   distance = (PI / 2) - lat
     * @param lat [-90,90] north
     * @param lon [-180,180] east
     * @return [angleRads][distanceOfPI]
     */
    protected double[] toPolar(double lat, double lon)
    {
        double[] polar = new double[2];
        polar[0] = lon;
        polar[1] = 90 - lat;
        Log.d(WorldMapView.LOGTAG, "toPolar: [" + lat + ", " + lon + "] -> [" + polar[0] + ", " + polar[1] + "]");
        return polar;
    }

    /**
     * x = distance * sin(angle)  ..  y = -1 * distance * cos(angle)
     * @param polar [angleDegrees][distance]
     * @return cartesian point [x,y]
     */
    protected double[] toCartesian(double[] polar)
    {
        double[] point = new double[2];
        point[0] = polar[1] * Math.sin(Math.toRadians(polar[0]));
        point[1] = -1d * polar[1] * Math.cos(Math.toRadians(polar[0]));
        //Log.d(WorldMapView.LOGTAG, "toCartesian: [" + polar[0] + ", " + polar[1] + "] -> [" + point[0] + ", " + point[1] + "]");
        return point;
    }

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double[] point = toCartesian(toPolar(lat, lon));
        int[] r = new int[2];
        r[0] = (int)(mid[0] + ((point[0] / 180d) * mid[0]));
        r[1] = (int)(mid[1] - ((point[1] / 180d) * mid[1]));
        return r;
    }

    protected int[] initPixels(int w, int h, double[] sunUp, double[] moonUp, WorldMapTask.WorldMapOptions options)
    {
        int combinedColor = ColorUtils.compositeColors(options.moonLightColor, options.sunShadowColor);

        int z = 0;
        int j0, j1, j2;
        double v0, v1, v2;
        double sunIntensity, moonIntensity;
        int[] pixels = new int[w * h];
        double[] m = getMatrix();

        for (int j = 0; j < h; j++)
        {
            j0 = (360 * j);
            j1 = (360 * (360 + j));
            j2 = (360 * (720 + j));

            for (int i = 0; i < w; i++)
            {
                v0 = m[i + j0];
                v1 = m[i + j1];
                v2 = m[i + j2];

                if (options.showSunShadow && options.showMoonLight)
                {
                    sunIntensity = (sunUp[0] * v0) + (sunUp[1] * v1) + (sunUp[2] * v2);
                    moonIntensity = (moonUp[0] * v0) + (moonUp[1] * v1) + (moonUp[2] * v2);

                    if (sunIntensity <= 0 && moonIntensity > 0) {
                        pixels[z] = combinedColor;
                    } else if (sunIntensity <= 0) {
                        pixels[z] = options.sunShadowColor;
                    } else if (moonIntensity > 0) {
                        pixels[z] = options.moonLightColor;
                    }

                } else if (options.showSunShadow) {
                    sunIntensity = (sunUp[0] * v0) + (sunUp[1] * v1) + (sunUp[2] * v2);
                    pixels[z] = (sunIntensity <= 0) ? options.sunShadowColor : Color.TRANSPARENT;

                } else if (options.showMoonLight) {
                    moonIntensity = (moonUp[0] * v0) + (moonUp[1] * v1) + (moonUp[2] * v2);
                    pixels[z] = (moonIntensity > 0) ? options.moonLightColor : Color.TRANSPARENT;
                }
                z++;
            }
        }
        return pixels;
    }

    protected boolean paintInitialized = false;
    protected Paint paintScaled = null;
    protected Paint paintBackground = null;
    protected Paint paintForeground = null;
    protected Paint paintLocation_fill = null, paintLocation_stroke;
    protected Paint paintMask_srcOver = null;
    protected Paint paintMask_srcIn = null;
    protected Paint paintSun_fill = null;
    protected Paint paintSun_stroke = null;
    protected Paint paintMoon_fill = null;
    protected Paint paintMoon_stroke = null;

    @Override
    public void initPaint(WorldMapTask.WorldMapOptions options)
    {
        paintScaled = new Paint(Paint.ANTI_ALIAS_FLAG);         // to scale one bitmap into another
        paintScaled.setDither(true);
        paintScaled.setAntiAlias(true);
        paintScaled.setFilterBitmap(true);

        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);      // to draw background color
        paintBackground.setColor(options.backgroundColor);
        paintBackground.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        paintForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintForeground.setColorFilter(new LightingColorFilter(options.foregroundColor, 0));

        paintMask_srcOver = new Paint(Paint.ANTI_ALIAS_FLAG);    // to create a mask
        paintMask_srcOver.setColor(Color.WHITE);
        paintMask_srcOver.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        paintMask_srcIn = new Paint(Paint.ANTI_ALIAS_FLAG);      // to apply a mask
        paintMask_srcIn.setColor(Color.WHITE);
        paintMask_srcIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        paintLocation_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLocation_fill.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLocation_fill.setColor(options.locationFillColor);

        paintLocation_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLocation_stroke.setStyle(Paint.Style.STROKE);
        paintLocation_stroke.setColor(options.locationStrokeColor);
        paintLocation_stroke.setStrokeWidth(0.5f);

        paintSun_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSun_fill.setStyle(Paint.Style.FILL);
        paintSun_fill.setColor(options.sunFillColor);

        paintSun_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSun_stroke.setStyle(Paint.Style.STROKE);
        paintSun_stroke.setColor(options.sunStrokeColor);

        paintMoon_fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoon_fill.setStyle(Paint.Style.FILL);
        paintMoon_fill.setColor(options.moonFillColor);

        paintMoon_stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoon_stroke.setStyle(Paint.Style.STROKE);
        paintMoon_stroke.setColor(options.moonStrokeColor);

        paintInitialized = true;
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

        if (!paintInitialized) {
            initPaint(options);
        }

        ////////////////
        // draw base map
        drawMap(c, w, h, paintForeground, options);
        if (options.showMajorLatitudes) {
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
            //
            if (options.showSunPosition || options.showMoonPosition)
            {
                int[] size = matrixSize();
                Bitmap lightBitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                int[] pixels = initPixels(size[0], size[1], sunUp, moonUp, options);
                lightBitmap.setPixels(pixels, 0, size[0], 0, 0, size[0], size[1]);

                Rect src = new Rect(0,0,size[0]-1, size[1]-1);
                Rect dst = new Rect(0,0,w-1, h-1);
                c.drawBitmap(lightBitmap, src, dst, paintScaled);
                lightBitmap.recycle();
            }

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
        Log.d(WorldMapView.LOGTAG, "make equiazimuthal world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return masked;
    }

    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public double[] getMatrix() {
        return matrix;
    }

    @Override
    public void resetMatrix() {
        matrix = null;
    }

    @Override
    public int[] matrixSize()
    {
        return new int[] {360, 360};
    }

    @Override
    public double[] initMatrix()
    {
        long bench_start = System.nanoTime();

        int[] size = matrixSize();
        int w = size[0];
        int h = size[1];
        double[] v = new double[w * h * 3];

        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        double squareR = (0.5 * w + 1) * (0.5 * w + 1);
        double[] polar = new double[2];

        double x, y;
        for (int i = 0; i < w; i++)
        {
            x = ((double)i) - 180d;   // [-180,180]
            double squareX = x * x;
            if (x == 0) {
                x += 0.0001;
            }
            for (int j = 0; j < h; j++)
            {
                y = ((double)(h - j)) - 180d;   // [-180,180]
                if ((squareX + y*y) > squareR)
                    continue;
                //Log.d("DEBUG", "pX: " + x + ", pY: " + y);

                polar[0] = -1 * Math.atan(x / y);
                radLon = polar[0];
                sinLon = Math.sin(radLon);
                polar[1] = x / sinLon;
                //Log.d("DEBUG", "angle: " + polar[0] + ", dist: " + polar[1]);

                radLat = Math.toRadians(90 - polar[1]);
                cosLat = Math.cos(radLat);
                cosLon = Math.cos(radLon);

                v[i + (360 * j)] = cosLon * cosLat;
                v[i + (360 * (360 + j))] = sinLon * cosLat;
                v[i + (360 * (720 + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make equiazimuthal world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
        return v;
    }

    @Override
    protected int k(int i, int j, int k)
    {
        return i + (360 * ((360 * k) + j));
    }

    private static double r_equator = 0.5;
    private static double r_tropics = (23.439444 / 180d);
    private static double r_polar = (66.560833 / 180d);

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(options.hasTransparentBaseMap ? new PorterDuffXfermode(PorterDuff.Mode.DST_OVER) : new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        Paint.Style prevStyle = p.getStyle();
        PathEffect prevEffect = p.getPathEffect();
        float prevStrokeWidth = p.getStrokeWidth();

        double equator = mid[1] * r_equator;
        double tropics = mid[1] * r_tropics;
        double polar = mid[1] * r_polar;

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setColor(options.latitudeColors[0]);
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);

        c.drawCircle((int)mid[0], (int)mid[1], (int)equator, p);

        p.setColor(options.latitudeColors[1]);
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        c.drawCircle((int)mid[0], (int)mid[1], (int)(equator + tropics), p);
        c.drawCircle((int)mid[0], (int)mid[1], (int)(equator - tropics), p);

        p.setColor(options.latitudeColors[2]);
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        c.drawCircle((int)mid[0], (int)mid[1], (int)(equator + polar), p);
        c.drawCircle((int)mid[0], (int)mid[1], (int)(equator - polar), p);

        p.setStyle(prevStyle);
        p.setPathEffect(prevEffect);
        p.setStrokeWidth(prevStrokeWidth);
    }

}
