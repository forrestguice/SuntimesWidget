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

package com.forrestguice.suntimeswidget.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * WorldMapEquiazimuthal
 * An equidistant azimuthal projection centered on given coordinates.
 */
public class WorldMapEquiazimuthal2 extends WorldMapEquiazimuthal
{
    protected static double[] center = new double[] {0, 0};
    private static double sinLat1 = 0, cosLat1 = 1;
    private static Rect pixelSrc;

    @Override
    public double[] getCenter() {
        return center;
    }
    public void setCenter(double lat, double lon) {
        center[0] = lat;
        center[1] = lon;
    }
    public boolean setCenterFromOptions(@Nullable WorldMapTask.WorldMapOptions options) {
        if (options != null && options.center != null                                               // if options defines a center
                && (options.center[0] != center[0] || options.center[1] != center[1]))                  // and that center is not the same
        {
            center[0] = options.center[0];
            center[1] = options.center[1];
            return true;
        } else return false;
    }

    @Override
    public double[] getMatrix() {
        return matrix;
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
        pixelSrc = new Rect(0,0,size[0]-1, size[1]-1);
        grid_mid = null;

        int w = size[0];
        int h = size[1];
        double[] v = new double[w * h * 3];

        double x, y;
        double c, sinC, cosC;
        double radLat, cosLat;
        double radLon, cosLon, sinLon;
        double squareR = Math.PI * Math.PI;

        double radLon1 = Math.toRadians(center[1]);
        double radLat1 = Math.toRadians(center[0]);
        sinLat1 = Math.sin(radLat1);
        cosLat1 = Math.cos(radLat1);

        for (int i = 0; i < w; i++)
        {
            x = ((((double)i) - 180) / 180d) * Math.PI;    // to [-180,180] to [-PI,PI]
            double squareX = x * x;
            if (x == 0) {
                x += 0.0001;
            }

            for (int j = 0; j < h; j++)
            {
                y = ((((double)(h - j)) - 180d) / 180d) * Math.PI;    // to [-180,180] to [-PI,PI]
                if ((squareX + y*y) > squareR) {
                    continue;
                }

                c = Math.sqrt(x*x + y*y);
                if (c != 0)
                {
                    cosC = Math.cos(c);
                    sinC = Math.sin(c);
                    radLat = Math.asin((cosC * sinLat1) + ((y * sinC * cosLat1) / c));

                    if (center[0] == 90) {
                        radLon = radLon1 + Math.atan2(x,-y);
                    } else if (center[0] == -90) {
                        radLon = radLon1 + Math.atan2(x,y);
                    } else {
                        radLon = radLon1 + Math.atan2((x * sinC), (c * cosLat1 * cosC) - (y * sinLat1 * sinC));
                    }
                } else {
                    radLat = Math.toRadians(center[0]);
                    radLon = Math.toRadians(center[1]);
                }

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
        double[] polar = new double[2];
        double[] point = toCartesian(lat, lon);
        polar[0] = Math.toDegrees(Math.atan2(point[0], point[1]));
        polar[1] = Math.toDegrees(point[2]);
        //Log.d(WorldMapView.LOGTAG, "toPolar: [" + lat + ", " + lon + "] -> [" + polar[0] + ", " + polar[1] + "]");
        return polar;
    }

    protected double[] toCartesian(double lat, double lon)
    {
        double distance = lon - center[1];
        double radDistance = Math.toRadians(distance);
        double sinDistance = Math.sin(radDistance);
        double cosDistance = Math.cos(radDistance);

        double radLat = Math.toRadians(lat);
        double sinLat = Math.sin(radLat);
        double cosLat = Math.cos(radLat);

        double[] point = new double[3];
        double cosC = (sinLat1 * sinLat) + (cosLat1 * cosLat * cosDistance);
        double c = point[2] = Math.acos(cosC);
        if (c < 0.00001)
        {
            if (cosC > 0) {
                // k = 1;
                point[0] = point[1] = 0;

            } else if (cosC < 0) {
                // k = 1;
                //point[0] = -1 * center[0];    // TODO
                //point[1] = center[1] + 180;
            }
        } else {
            double k = c / Math.sin(c);
            double R = 1;
            point[0] = R * k * cosLat * sinDistance;
            point[1] = R * k * (((cosLat1 * sinLat) - (sinLat1 * cosLat * cosDistance)));
        }
        //Log.d("DEBUG", "toCartesian: [" + lat + ", " + lon + "] -> [" + point[0] + ", " + point[1] + "] .. k is " + k + ", c is " + Math.toDegrees(c) + ", cosC is " + cosC + ", distance is " + distance);
        return point;
    }

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double[] point = toCartesian(lat, lon);
        int[] r = new int[2];
        r[0] = (int)(mid[0] + ((point[0] / Math.PI) * mid[0]));
        r[1] = (int)(mid[1] - ((point[1] / Math.PI) * mid[1]));
        return r;
    }

    private Rect pixelDst = new Rect();

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options)
    {
        long bench_start = System.nanoTime();
        if (w <= 0 || h <= 0) {
            return null;
        }

        boolean recenter = setCenterFromOptions(options);
        if (matrix == null || recenter) {
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
                Bitmap lightBitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                int[] pixels = initPixels(size[0], size[1], sunUp, moonUp, options);    // TODO: double buffer?
                lightBitmap.setPixels(pixels, 0, size[0], 0, 0, size[0], size[1]);

                //Rect pixelSrc = new Rect(0,0,size[0]-1, size[1]-1);    // set by initMatrix
                pixelDst.set(0,0,w-1, h-1);
                c.drawBitmap(lightBitmap, pixelSrc, pixelDst, paintScaled);
                lightBitmap.recycle();
            }

            ////////////////
            // draw sun
            if (options.showSunPosition && options.showSunShadow)
            {
                double[] point = toCartesian(sunLat, sunLon);
                int sunX = (int)(mid[0] + ((point[0] / Math.PI) * mid[0]) );
                int sunY = (int)(mid[1] - ((point[1] / Math.PI) * mid[1]) );
                drawSun(c, sunX, sunY, paintSun_fill, paintSun_stroke, options);
            }

            ////////////////
            // draw moon
            if (options.showMoonPosition && options.showMoonLight)
            {
                double[] point = toCartesian(moonLat, moonLon);
                int moonX = (int)(mid[0] + ((point[0] / Math.PI) * mid[0]) );
                int moonY = (int)(mid[1] - ((point[1] / Math.PI) * mid[1]) );
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
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setColor(options.latitudeColors[0]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        drawConnectedLines(c, createLatitudePath(mid, 0, -180, 0), paintGrid);
        drawConnectedLines(c, createLatitudePath(mid, 0, 0, 180), paintGrid);

        drawConnectedLines(c, createLongitudePath(mid, 180), paintGrid);
        drawConnectedLines(c, createLongitudePath(mid, 0), paintGrid);
        drawConnectedLines(c, createLongitudePath(mid, 90), paintGrid);
        drawConnectedLines(c, createLongitudePath(mid, -90), paintGrid);

        paintGrid.setColor(options.latitudeColors[1]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        drawConnectedLines(c, createLatitudePath(mid, 23.439444), paintGrid);
        drawConnectedLines(c, createLatitudePath(mid, -23.439444), paintGrid);

        paintGrid.setColor(options.latitudeColors[2]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        drawConnectedLines(c, createLatitudePath(mid, 66.560833), paintGrid);
        drawConnectedLines(c, createLatitudePath(mid, -66.560833), paintGrid);
    }

    @Override
    public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, createLatitudePath(mid, 0, -180, 0), paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, createLatitudePath(mid, 0, 0, 180), paintGrid);

        paintGrid.setColor(Color.BLUE);
        drawConnectedLines(c, createLongitudePath(mid, 180), paintGrid);
        paintGrid.setColor(Color.YELLOW);
        drawConnectedLines(c, createLongitudePath(mid, 0), paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, createLongitudePath(mid, 90), paintGrid);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, createLongitudePath(mid, -90), paintGrid);

        paintGrid.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        paintGrid.setColor(Color.WHITE);
        drawConnectedLines(c, createLatitudePath(mid, 23.439444), paintGrid);
        drawConnectedLines(c, createLatitudePath(mid, -23.439444), paintGrid);

        paintGrid.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, createLatitudePath(mid, -66.560833), paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, createLatitudePath(mid, 66.560833), paintGrid);
    }

    protected void initGrid(double[] mid)
    {
        long bench_start = System.nanoTime();
        grid_mid = mid;
        grid_x = new ArrayList<>();
        grid_y = new ArrayList<>();
        for (int i=0; i<=180; i+=15) {
            grid_x.add(createLongitudePath(mid, i));
            grid_x.add(createLongitudePath(mid, -i));
        }
        for (int i=0; i<90; i+=15) {
            grid_y.add(createLatitudePath(mid, i));
            grid_y.add(createLatitudePath(mid, -i));
        }
        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "initGrid :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
    }
    private static ArrayList<float[]> grid_x = null, grid_y = null;
    private static double[] grid_mid;

    @Override
    public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options)
    {
        if (grid_mid == null || mid[0] != grid_mid[0] || mid[1] != grid_mid[1]) {
            initGrid(mid);
        }

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        for (int i=0; i<grid_x.size(); i++) {
            drawConnectedLines(c, grid_x.get(i), paintGrid);
        }

        paintGrid.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        for (int i=0; i<grid_y.size(); i++) {
            drawConnectedLines(c, grid_y.get(i), paintGrid);
        }
    }

    @Override
    protected float[] createLatitudePath(double[] mid, double latitude, double min, double max)
    {
        double[] point;
        ArrayList<Float> path = new ArrayList<>();
        for (int longitude=(int)min; longitude <= max; longitude+=2)
        {
            point = toCartesian(latitude, longitude);
            path.add((float)(mid[0] + ((point[0] / (Math.PI)) * mid[0])));
            path.add((float)(mid[1] - ((point[1] / (Math.PI)) * mid[1])));
        }
        return toFloatArray(path);
    }

    @Override
    protected float[] createLongitudePath(double[] mid, double longitude, double min, double max)
    {
        double[] point;
        ArrayList<Float> path = new ArrayList<>();
        for (int latitude = (int)min; latitude<max; latitude+=2)
        {
            point = toCartesian(latitude, longitude);
            path.add((float)(mid[0] + ((point[0] / (Math.PI)) * mid[0])));
            path.add((float)(mid[1] - ((point[1] / (Math.PI)) * mid[1])));
        }
        return toFloatArray(path);
    }

}
