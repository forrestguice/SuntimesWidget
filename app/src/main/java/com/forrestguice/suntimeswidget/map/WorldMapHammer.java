/**
    Copyright (C) 2024-2026 Forrest Guice
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
import android.os.Build;
import android.util.Log;

import com.forrestguice.colors.Color;

/**
 * Hammer equal-area
 */
public class WorldMapHammer extends WorldMapVanDerGrinten
{
    private static final double SQRT2 = Math.sqrt(2);
    private static final double ONE_OVER_SQRT2 = 1d / SQRT2;
    private static final double ONE_OVER_2SQRT2 = 1d / (2d * SQRT2);

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double radLat = Math.toRadians(lat);
        double radLon = Math.toRadians(lon);

        double cosLat = Math.cos(radLat);
        double D = 1d / Math.sqrt(1d + (cosLat * Math.cos(0.5d * radLon)));
        double x = (2 * SQRT2 * cosLat * Math.sin(0.5d * radLon)) * D;
        double y = (SQRT2 * Math.sin(radLat)) * D;
        //Log.d("DEBUG", "x: " + x + ", y: " + y);

        int[] p = new int[2];
        p[0] = (int)(mid[0] + ((x * mid[0]) * ONE_OVER_2SQRT2));
        p[1] = (int)(mid[1] - ((y * mid[1]) * ONE_OVER_SQRT2));
        return p;
    }

    @Override
    public double[] initMatrix()
    {
        long bench_start = System.nanoTime();

        int[] size = matrixSize();
        int w = size[0];
        int h = size[1];
        double[] m = new double[] { w/2d, h/2d };
        double[] v = new double[w * h * 3];

        double radX, radY, z;
        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        for (int j=0; j<h; j++)    // for each pixel(i,j) transform into point(x,y) to find coordinate(lon,lat)
        {
            radY = ((m[1] - j) * SQRT2) / m[1];

            for (int i=0; i<w; i++)
            {
                radX = ((i - m[0]) * 2 * SQRT2) / m[0];
                z = Math.sqrt(1d - Math.pow(0.25d * radX, 2) - Math.pow(0.5d * radY, 2));

                radLat = Math.asin(radY * z);
                cosLat = Math.cos(radLat);

                radLon = 2 * Math.atan((radX * z) / (2d * ((2d * z * z) - 1)));
                cosLon = Math.cos(radLon);
                sinLon = Math.sin(radLon);

                v[i + (size[0] * j)] = cosLon * cosLat;
                v[i + (size[0] * (size[1] + j))] = sinLon * cosLat;
                v[i + (size[0] * ((size[1] * 2) + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make hammer world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + size[0] + ", " + size[1]);
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
    private static double[] matrix = null;    // [x * y * v(3)]

    @Override
    public void resetMatrix() {
        matrix = null;
    }

    @Override
    protected int k(int i, int j, int k) {
        return i + (720 * ((360 * k) + j));
    }

    @Override
    public int[] matrixSize() {
        return new int[] {720, 360};
    }

    @Override
    protected Bitmap makeMaskedBitmap(int w, int h, Bitmap b)
    {
        Bitmap masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);    // mask final image to fit within an ellipse
        Canvas maskedCanvas = new Canvas(masked);
        if (Build.VERSION.SDK_INT >= 21) {
            maskedCanvas.drawOval(0, 0, w, h, paintMask_srcOver);
        } else {
            maskedCanvas.drawColor(Color.WHITE);
        }


        maskedCanvas.drawBitmap(b, 0, 0, paintMask_srcIn);
        b.recycle();
        return masked;
    }

}
