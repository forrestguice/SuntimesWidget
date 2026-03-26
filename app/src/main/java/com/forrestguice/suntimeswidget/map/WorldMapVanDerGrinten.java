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

package com.forrestguice.suntimeswidget.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Path;

import com.forrestguice.util.Log;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;

import java.util.ArrayList;

/**
 * van der Grinten
 */
public class WorldMapVanDerGrinten extends WorldMapMercator
{
    private static final double ONE_OVER_3 = 1d / 3d;
    private static final double PI_OVER_3 = Math.PI / 3d;
    private static final double ONE_OVER_27 = 1d / 27d;

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        if (lat == 0) {
            lat = 0.01d;
        } else if (lat == 90) {
            lat = 89.9;
        } else if (lat == -90) {
            lat = -89.9;
        }
        if (lon == 0) {
            lon += 0.01d;
        }

        double radLat = Math.toRadians(lat);
        double T = Math.asin(Math.abs((2 * radLat) / Math.PI));
        double sinT = Math.sin(T);
        double cosT = Math.cos(T);

        double G = Math.cos(T) / (sinT + cosT - 1d);
        double P = G * ((2d / sinT) - 1d);
        double P2 = P * P;

        double lambda = Math.toRadians(lon - 0d);    // - center_longitude
        double A = 0.5d * Math.abs((Math.PI / lambda) - (lambda / Math.PI));
        double A2 = A * A;
        double Q = G + A2;

        double P2_PLUS_A2 = P2 + A2;
        double G_MINUS_P2 = G - P2;
        double x = Math.signum(lambda) * ((Math.PI * ((A * G_MINUS_P2) + Math.sqrt(A2 * G_MINUS_P2 * G_MINUS_P2 - P2_PLUS_A2 * (G*G - P2)))) / (P2_PLUS_A2));
        double y = Math.signum(radLat) * ((Math.PI * Math.abs(P * Q - A * Math.sqrt((A2 + 1) * P2_PLUS_A2 - (Q * Q))) / (P2_PLUS_A2)));

        int[] p = new int[2];
        p[0] = (int) (mid[0] + ((x / Math.PI) * mid[0]));
        p[1] = (int) (mid[1] - ((y / Math.PI) * mid[1]));
        return p;
    }

    @Override
    public double[] initMatrix()
    {
        long bench_start = System.nanoTime();

        int[] size = matrixSize();
        int w = size[0];
        int h = size[1];
        double[] v = new double[w * h * 3];

        double iw0 = (1d / w) * 360d;
        double ih0 = (1d / h) * 360d;

        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        double radX, radY;
        double X, X2, Y, Y2, X2_PLUS_Y2, TWO_Y2;
        double c1, c2, c2_2, c3, c3_2, three_c3;
        double d, a1, m1, T1;

        for (int i = 0; i < w; i++)    // for each pixel(i,j) transform into point(x,y) to find coordinate(lon,lat)
        {
            radX = Math.toRadians(((double) i * iw0) - 180d);  // i in [0,w] to [0,360] to [-180,180]; every x is 1 degree
            if (radX == 0) {
                radX += 0.01d;
            }

            X = radX / Math.PI;
            X2 = X * X;

            for (int j = 0; j < h; j++)
            {
                radY = Math.toRadians(-1 * (((double) j * ih0) - 180d));      // j in [0,h] to [0,360] to [-180,180] (inverted to canvas); every Y is 0.5 degrees
                if (radY == 0) {
                    radY += 0.01d;
                }
                Y = radY / Math.PI;
                Y2 = Y * Y;
                TWO_Y2 = 2d * Y2;
                X2_PLUS_Y2 = X2 + Y2;

                c1 = -Math.abs(Y) * (1d + X2_PLUS_Y2);
                c2 = c1 - TWO_Y2 + X2;
                c3 = (-2d * c1) + 1d + TWO_Y2 + (X2_PLUS_Y2 * X2_PLUS_Y2);

                three_c3 = 3d * c3;
                c2_2 = c2 * c2;
                c3_2 = c3 * c3;

                d = (Y2 / c3) + (ONE_OVER_27 * (((2d * c2 * c2_2) / (c3 * c3_2)) - ((9d * c1 * c2) / c3_2)));
                a1 = (1d / c3) * (c1 - (c2_2 / three_c3));
                m1 = 2d * Math.sqrt(-ONE_OVER_3 * a1);
                T1 = ONE_OVER_3 * Math.acos((3d * d) / (a1 * m1));

                radLat = Math.signum(radY) * Math.PI * ((-m1 * Math.cos(T1 + PI_OVER_3)) - (c2 / three_c3));
                cosLat = Math.cos(radLat);

                radLon = (Math.PI * (X2_PLUS_Y2 - 1d + Math.sqrt(1d + (2d * (X2 - Y2)) + (X2_PLUS_Y2 * X2_PLUS_Y2))) / (2d * X)) + 0d;    // + center_longitude
                cosLon = Math.cos(radLon);
                sinLon = Math.sin(radLon);

                v[i + (size[0] * j)] = cosLon * cosLat;
                v[i + (size[0] * (size[1] + j))] = sinLon * cosLat;
                v[i + (size[0] * ((size[1] * 2) + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make mercator world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + size[0] + ", " + size[1]);
        return v;
    }

    @Override
    public double[] getMatrix()
    {
        if (matrix == null) {
            matrix = initMatrix();
        }
        return matrix;
    }
    @Nullable
    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public void resetMatrix() {
        matrix = null;
    }

    @Override
    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options) {
        return makeMaskedBitmap(w, h, super.makeBitmap(data, w, h, options));
    }

    protected Bitmap makeMaskedBitmap(int w, int h, Bitmap b)
    {
        Bitmap masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);    // mask final image to fit within a circle
        Canvas maskedCanvas = new Canvas(masked);
        maskedCanvas.drawCircle(w/2f, h/2f, w/2f - 2f, paintMask_srcOver);

        /* // mask out polar regions
        double[] mid = new double[] { w/2d, h/2d };
        Path pole0 = createPolarMaskPath(w, h, mid, true);
        Path pole1 = createPolarMaskPath(w, h, mid, false);
        paintMask_srcIn.setColor(Color.TRANSPARENT);
        maskedCanvas.drawPath(pole0, paintMask_srcIn);
        maskedCanvas.drawPath(pole1, paintMask_srcIn);
        */

        maskedCanvas.drawBitmap(b, 0, 0, paintMask_srcIn);
        b.recycle();
        return masked;
    }

    protected Path createPolarMaskPath(int w, int h, double[] mid, boolean northward)
    {
        Path path = new Path();
        path.moveTo(0, northward ? 0 : h);

        int[] p;
        double latitude = northward ? 88 : -88;
        for (int longitude=(int)-180; longitude <= 180; longitude+=2)
        {
            p = toBitmapCoords(w, h, mid, latitude, longitude);
            path.lineTo((float) p[0], (float) p[1]);
        }

        path.lineTo(w, northward ? 0 : h);
        path.close();
        return path;
    }

    private ArrayList<float[]> grid_x = null, grid_y = null;
    private boolean grid_initialized = false;

    protected void initGrid(int w, int h, double[] mid)
    {
        long bench_start = System.nanoTime();
        grid_x = new ArrayList<>();
        grid_y = new ArrayList<>();
        for (int i=0; i<=180; i+=15) {
            grid_x.add(createLongitudePath(w, h, mid, i));
            grid_x.add(createLongitudePath(w, h, mid, -i));
        }
        for (int i=0; i<90; i+=15) {
            grid_y.add(createLatitudePath(w, h, mid, i));
            grid_y.add(createLatitudePath(w, h, mid, -i));
        }
        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "initGrid :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        grid_initialized = true;
    }

    @Override
    public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        if (!grid_initialized) {
            initGrid(w, h, mid);
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

    private float[] majorline_equator_east, majorline_equator_west;
    private float[] majorline_tropics_north, majorline_tropics_south;
    private float[] majorline_polar_north, majorline_polar_south;
    private float[] majorline_dateline, majorline_prime, majorline_prime_east, majorline_prime_west;
    private boolean majorline_initialized = false;

    protected void initMajorLines(int w, int h, double[] mid)
    {
        majorline_equator_west = createLatitudePath(w, h, mid, 0, -180, 0);
        majorline_equator_east = createLatitudePath(w, h, mid, 0, 0, 180);

        majorline_tropics_north = createLatitudePath(w, h, mid, 23.439444);
        majorline_tropics_south = createLatitudePath(w, h, mid, -23.439444);
        majorline_polar_north = createLatitudePath(w, h, mid, 66.560833);
        majorline_polar_south = createLatitudePath(w, h, mid, -66.560833);

        majorline_dateline = createLongitudePath(w, h, mid, 180);
        majorline_prime = createLongitudePath(w, h, mid, 0);
        majorline_prime_east = createLongitudePath(w, h, mid, 90);
        majorline_prime_west = createLongitudePath(w, h, mid, -90);

        majorline_initialized = true;
    }

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        if (!majorline_initialized) {
            initMajorLines(w, h, mid);
        }

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setColor(options.latitudeColors[0]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        drawConnectedLines(c, majorline_equator_east, paintGrid);
        drawConnectedLines(c, majorline_equator_west, paintGrid);

        drawConnectedLines(c, majorline_dateline, paintGrid);
        drawConnectedLines(c, majorline_prime, paintGrid);
        drawConnectedLines(c, majorline_prime_east, paintGrid);
        drawConnectedLines(c, majorline_prime_west, paintGrid);

        paintGrid.setColor(options.latitudeColors[1]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        drawConnectedLines(c, majorline_tropics_north, paintGrid);
        drawConnectedLines(c, majorline_tropics_south, paintGrid);

        paintGrid.setColor(options.latitudeColors[2]);
        paintGrid.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        drawConnectedLines(c, majorline_polar_north, paintGrid);
        drawConnectedLines(c, majorline_polar_south, paintGrid);
    }

    @Override
    public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        if (!majorline_initialized) {
            initMajorLines(w, h, mid);
        }

        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, majorline_equator_west, paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, majorline_equator_east, paintGrid);

        paintGrid.setColor(Color.BLUE);
        drawConnectedLines(c, majorline_dateline, paintGrid);
        paintGrid.setColor(Color.YELLOW);
        drawConnectedLines(c, majorline_prime, paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, majorline_prime_east, paintGrid);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, majorline_prime_west, paintGrid);

        paintGrid.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        paintGrid.setColor(Color.WHITE);
        drawConnectedLines(c, majorline_tropics_north, paintGrid);
        drawConnectedLines(c, majorline_tropics_south, paintGrid);

        paintGrid.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        paintGrid.setColor(Color.GREEN);
        drawConnectedLines(c, majorline_polar_south, paintGrid);
        paintGrid.setColor(Color.RED);
        drawConnectedLines(c, majorline_polar_north, paintGrid);
    }

    protected float[] createLatitudePath(int w, int h, double[] mid, double latitude) {
        return createLatitudePath(w, h, mid, latitude, -180, 180);
    }
    protected float[] createLatitudePath(int w, int h, double[] mid, double latitude, double minLongitude, double maxLongitude)
    {
        int[] p;
        ArrayList<Float> path = new ArrayList<>();
        for (int longitude=(int)minLongitude; longitude <= maxLongitude; longitude+=2)
        {
            p = toBitmapCoords(w, h, mid, latitude, longitude);
            path.add((float) p[0]);
            path.add((float) p[1]);
        }
        return toFloatArray(path);
    }

    protected float[] createLongitudePath(int w, int h, double[] mid, double longitude) {
        return createLongitudePath(w, h, mid, longitude, -90, 90);
    }
    protected float[] createLongitudePath(int w, int h, double[] mid, double longitude, double minLatitude, double maxLatitude)
    {
        int[] p;
        ArrayList<Float> path = new ArrayList<>();
        for (int latitude = (int)minLatitude; latitude<maxLatitude; latitude+=2)
        {
            p = toBitmapCoords(w, h, mid, latitude, longitude);
            path.add((float) p[0]);
            path.add((float) p[1]);
        }
        return toFloatArray(path);
    }

}
