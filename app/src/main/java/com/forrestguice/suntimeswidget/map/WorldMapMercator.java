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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.forrestguice.util.Log;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;

/**
 * Mercator map projection
 */
public class WorldMapMercator extends WorldMapEquirectangular
{
    private static final double PI_OVER_2 = Math.PI / 2d;
    private static final double PI_OVER_4 = Math.PI / 4d;

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double x = Math.toRadians(lon - 0);    // minus center_longitude
        double y = Math.log(Math.tan(PI_OVER_4 + (0.5d * Math.toRadians(lat))));

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

        double x, radY;
        for (int i = 0; i < w; i++)    // for each pixel(i,j) transform into point(x,y) to find coordinate(lon,lat)
        {
            x = ((double) i * iw0) - 180d;  // i in [0,w] to [0,360] to [-180,180]; every x is 1 degree
            radLon = Math.toRadians(x + 0);    // + center_longitude
            cosLon = Math.cos(radLon);
            sinLon = Math.sin(radLon);

            for (int j = 0; j < h; j++)
            {
                radY = Math.toRadians(-1 * (((double) j * ih0) - 180d));      // j in [0,h] to [0,360] to [-180,180] (inverted to canvas); every Y is 0.5 degrees
                //radLat = 2d * Math.atan(Math.pow(Math.E, radY)) - PI_OVER_2;    // Gudermannian
                radLat = Math.atan(Math.sinh(radY));    // Gudermannian
                cosLat = Math.cos(radLat);

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
    protected int k(int i, int j, int k) {
        return i + (360 * ((360 * k) + j));
    }

    @Override
    public int[] matrixSize() {
        return new int[] {360, 360};
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
    public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        paintGrid.setStrokeWidth(strokeWidth);

        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        for (int i=0; i < 180; i = i + 15)
        {
            int[] pEast = toBitmapCoords(w, h, mid, 0, i);
            int[] pWest = toBitmapCoords(w, h, mid, 0, -i);
            c.drawLine(pEast[0], 0, pEast[0], h, paintGrid);
            c.drawLine(pWest[0], 0, pWest[0], h, paintGrid);
        }

        paintGrid.setColor(options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR));
        paintGrid.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        for (int i=0; i < 90; i = i + 15)
        {
            int[] pNorth = toBitmapCoords(w, h, mid, i, 0);
            int[] pSouth = toBitmapCoords(w, h, mid, -i, 0);
            c.drawLine(0, pNorth[1], w, pNorth[1], paintGrid);
            c.drawLine(0, pSouth[1], w, pSouth[1], paintGrid);
        }
    }

    @Override
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        Paint p = paintGrid;
        float strokeWidth = sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);

        p.setColor(options.latitudeColors[0]);                    // equator, prime meridian
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        int[] equator0 = toBitmapCoords(w, h, mid, 0, 0);
        c.drawLine(0, equator0[1], w, equator0[1], p);
        c.drawLine(equator0[0], 0, equator0[0], h, p);

        p.setColor(options.latitudeColors[0]);                    // east, west meridians
        int[] equator1 = toBitmapCoords(w, h, mid, 0, -90);
        int[] equator2 = toBitmapCoords(w, h, mid, 0, 90);
        c.drawLine(equator1[0], 0, equator1[0], h, p);
        c.drawLine(equator2[0], 0, equator2[0], h, p);

        p.setColor(options.latitudeColors[1]);                    // tropics
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        int[] tropics0 = toBitmapCoords(w, h, mid, 23.439444, 0);
        int[] tropics1 = toBitmapCoords(w, h, mid, -23.439444, 0);
        c.drawLine(0, tropics0[1], w, tropics0[1], p);
        c.drawLine(0, tropics1[1], w, tropics1[1], p);

        p.setColor(options.latitudeColors[2]);                    // polar
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        int[] polar0 = toBitmapCoords(w, h, mid, -66.560833, 0);
        int[] polar1 = toBitmapCoords(w, h, mid, 66.560833, 0);
        c.drawLine(0, polar0[1], w, polar0[1], p);
        c.drawLine(0, polar1[1], w, polar1[1], p);
    }

    @Override
    public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapOptions options)
    {
        Paint p = paintGrid;
        float strokeWidth = 1.5f * sunStroke(c, options) * options.latitudeLineScale;
        p.setStrokeWidth(strokeWidth);

        int[] equator0 = toBitmapCoords(w, h, mid, 0, 0);
        p.setPathEffect((options.latitudeLinePatterns[0][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[0], 0) : null);
        p.setColor(Color.GREEN);
        c.drawLine(0, equator0[1], equator0[0], equator0[1], p);

        p.setColor(Color.RED);
        c.drawLine(equator0[0], equator0[1], w, equator0[1], p);

        p.setColor(Color.YELLOW);                    // equator, prime meridian
        c.drawLine(equator0[0], 0, equator0[0], h, p);

        int[] equator1 = toBitmapCoords(w, h, mid, 0, -90);
        p.setColor(Color.GREEN);
        c.drawLine(equator1[0], 0, equator1[0], h, p);

        int[] equator2 = toBitmapCoords(w, h, mid, 0, 90);
        p.setColor(Color.RED);                       // east, west meridians
        c.drawLine(equator2[0], 0, equator2[0], h, p);

        int[] tropics0 = toBitmapCoords(w, h, mid, 23.439444, 0);
        int[] tropics1 = toBitmapCoords(w, h, mid, -23.439444, 0);
        p.setPathEffect((options.latitudeLinePatterns[1][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[1], 0) : null);
        p.setColor(Color.WHITE);                    // tropics
        c.drawLine(0, tropics0[1], w, tropics0[1], p);
        c.drawLine(0, tropics1[1], w, tropics1[1], p);

        int[] polar0 = toBitmapCoords(w, h, mid, -66.560833, 0);
        p.setPathEffect((options.latitudeLinePatterns[2][0] > 0) ? new DashPathEffect(options.latitudeLinePatterns[2], 0) : null);
        p.setColor(Color.GREEN);                  // polar
        c.drawLine(0, polar0[1], w, polar0[1], p);

        int[] polar1 = toBitmapCoords(w, h, mid, 66.560833, 0);
        p.setColor(Color.RED);
        c.drawLine(0, polar1[1], w, polar1[1], p);
    }

}
