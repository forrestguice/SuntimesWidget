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
import android.os.Build;
import android.util.Log;

/**
 * Mollweide / Elliptical
 */
public class WorldMapMollweide extends WorldMapVanDerGrinten
{
    private static final double PI_OVER_2 = Math.PI / 2d;
    private static final double SQRT2 = Math.sqrt(2);

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double radLon = Math.toRadians(lon);
        double radLat = Math.toRadians(lat);

        double theta = radLat;       // solve `2θ + sin(2θ) = PI * sin(lat)` with newtons method
        for (int i=0; i<5; i++) {
            theta -= (2 * theta + Math.sin(2 * theta) - (Math.PI * Math.sin(radLat)))
                    / (2 + 2 * Math.cos(2 * theta));
        }

        double radX = (2 * SQRT2 / Math.PI) * radLon * Math.cos(theta);
        double radY = SQRT2 * Math.sin(theta);

        int[] p = new int[2];
        p[0] = (int) (mid[0] + ((radX / (2 * SQRT2)) * mid[0]));
        p[1] = (int) (mid[1] - ((radY / SQRT2) * mid[1]));
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

        double theta;
        double radX, radY;
        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        for (int j=0; j<h; j++)
        {
            radY = SQRT2 * ((m[1] - j) / m[1]);
            theta = Math.asin(radY / SQRT2);
            radLat = Math.asin((2d * theta + Math.sin(2d * theta)) / Math.PI);
            cosLat = Math.cos(radLat);

            for (int i=0; i<w; i++)
            {
                radX = 2 * SQRT2 * ((i - m[0]) / m[0]);
                radLon = ((Math.PI * radX) / (2d * SQRT2 * Math.cos(theta)));
                cosLon = Math.cos(radLon);
                sinLon = Math.sin(radLon);

                v[i + (size[0] * j)] = cosLon * cosLat;
                v[i + (size[0] * (size[1] + j))] = sinLon * cosLat;
                v[i + (size[0] * ((size[1] * 2) + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make mollweide world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + size[0] + ", " + size[1]);
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

        paintMask_srcIn.setColor(Color.WHITE);
        maskedCanvas.drawBitmap(b, 0, 0, paintMask_srcIn);
        b.recycle();
        return masked;
        //return b;
    }

}
