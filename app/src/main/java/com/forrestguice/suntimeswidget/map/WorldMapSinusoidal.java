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
import android.graphics.Path;

import com.forrestguice.util.Log;
import com.forrestguice.annotation.Nullable;

/**
 * Sinusoidal
 */
public class WorldMapSinusoidal extends WorldMapVanDerGrinten
{
    private static final double PI_OVER_2 = Math.PI / 2d;

    @Override
    public int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon)
    {
        double radLon = Math.toRadians(lon);
        double radLat = Math.toRadians(lat);
        double radX = (radLon - 0) * Math.cos(radLat);
        double radY = radLat;

        int[] p = new int[2];
        p[0] = (int) (mid[0] + ((radX / Math.PI) * mid[0]));
        p[1] = (int) (mid[1] - ((radY / PI_OVER_2) * mid[1]));
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
        double ih0 = (1d / h) * 180d;

        double radX, radY;
        double radLon, cosLon, sinLon;
        double radLat, cosLat;

        for (int j = 0; j < h; j++)    // for each pixel(i,j) transform into point(x,y) to find coordinate(lon,lat)
        {
            radY = Math.toRadians(-1 * (((double) j * ih0) - 90d));      // j in [0,h] to [0,180] to [-90,90] (inverted to canvas); every Y is 1 degrees
            radLat = radY;
            cosLat = Math.cos(radLat);

            for (int i = 0; i < w; i++)
            {
                radX = Math.toRadians(((double) i * iw0) - 180d);  // i in [0,w] to [0,360] to [-180,180]; every x is 1 degree
                radLon = 0 + (radX / Math.cos(radLat));   // + center_longitude
                cosLon = Math.cos(radLon);
                sinLon = Math.sin(radLon);

                v[i + (size[0] * j)] = cosLon * cosLat;
                v[i + (size[0] * (size[1] + j))] = sinLon * cosLat;
                v[i + (size[0] * ((size[1] * 2) + j))] = Math.sin(radLat);
            }
        }

        long bench_end = System.nanoTime();
        Log.d(WorldMapView.LOGTAG, "make sinusoidal world map :: initMatrix :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + size[0] + ", " + size[1]);
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
        Bitmap masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);    // mask final image to fit within a circle
        Canvas maskedCanvas = new Canvas(masked);
        maskedCanvas.drawPath(initBackgroundMaskPath(w, h), paintMask_srcOver);
        maskedCanvas.drawBitmap(b, 0, 0, paintMask_srcIn);
        b.recycle();
        return masked;
    }

    protected Path initBackgroundMaskPath(int w, int h)
    {
        if (backgroundMaskPath == null || w != backgroundMaskSize[0] || h != backgroundMaskSize[1])
        {
            backgroundMaskPath = createBackgroundMaskPath(w, h);
            backgroundMaskSize[0] = w;
            backgroundMaskSize[1] = h;
        }
        return backgroundMaskPath;
    }
    protected Path backgroundMaskPath = null;
    protected int[] backgroundMaskSize= new int[] { 0, 0 };

    protected Path createBackgroundMaskPath(int w, int h)
    {
        double[] mid = new double[] { w / 2d, h / 2d };

        Path path = new Path();
        path.moveTo((float)mid[0], h);

        int[] p;
        for (int latitude = -90; latitude <= 90; latitude += 2) {
            p = toBitmapCoords(w, h, mid, latitude, -180d);
            path.lineTo((float) p[0], (float) p[1]);
        }
        for (int latitude = 90; latitude >= -90; latitude -= 2) {
            p = toBitmapCoords(w, h, mid, latitude, 180d);
            path.lineTo((float) p[0], (float) p[1]);
        }

        path.close();
        return path;
    }

}
