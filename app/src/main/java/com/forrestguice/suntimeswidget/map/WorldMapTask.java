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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;

/**
 * WorldMapTask
 */
public class WorldMapTask extends AsyncTask<Object, Void, Bitmap>
{
    private WorldMapProjection projection = new WorldMapEquirectangular();
    private WorldMapOptions options = new WorldMapOptions();

    public WorldMapTask()
    {
    }

    /**
     * @param params 0: SuntimesRiseSetDataset,
     *               1: Integer (width),
     *               2: Integer (height),
     *               3: Drawable (map)
     * @return a bitmap, or null params are invalid
     */
    @Override
    protected Bitmap doInBackground(Object... params)
    {
        int w, h;
        SuntimesRiseSetDataset data;
        try {
            data = (SuntimesRiseSetDataset)params[0];
            w = (Integer)params[1];
            h = (Integer)params[2];
            if (params.length > 3) {
                options = (WorldMapOptions) params[3];
            }
            if (params.length > 4) {
                projection = (WorldMapProjection) params[4];
            }

        } catch (ClassCastException e) {
            Log.w("WorldMapTask", "Invalid params; using [null, 0, 0]");
            return null;
        }
        return makeBitmap(data, w, h, options);
    }

    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options)
    {
        return projection.makeBitmap(data, w, h, options);
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected void onProgressUpdate( Void... progress )
    {
    }

    @Override
    protected void onPostExecute( Bitmap result )
    {
        if (isCancelled())
        {
            result = null;
        }
        onFinished(result);
    }

    /////////////////////////////////////////////

    protected void onFinished( Bitmap result )
    {
        if (listener != null)
        {
            listener.onFinished(result);
        }
    }

    private WorldMapView.WorldMapTaskListener listener = null;
    public void setListener( WorldMapView.WorldMapTaskListener listener )
    {
        this.listener = listener;
    }
    public void clearListener()
    {
        this.listener = null;
    }

    /**
     * WorldMapOptions
     */
    public static class WorldMapOptions
    {
        public boolean modified = false;

        public Drawable map = null;
        public int backgroundColor = Color.BLUE;
        public int foregroundColor = Color.TRANSPARENT;

        public boolean showGrid = false;
        public int gridXColor = Color.LTGRAY;
        public int gridYColor = Color.WHITE;

        public boolean showMajorLatitudes = false;
        public int[] latitudeColors = { Color.BLACK, Color.BLUE, Color.BLUE };

        public boolean showSunPosition = true;
        public int sunFillColor = Color.YELLOW;
        public int sunStrokeColor = Color.BLACK;
        public int sunScale = 48;                     // 48; default 48 suns fit within the width of the image (which is 24 hr wide meaning the sun has diameter of a half-hour)
        public int sunStrokeScale = 3;                // 3; default 3 strokes fit within the radius of the sun (i.e. the stroke is 1/3 the width)

        public boolean showSunShadow = true;
        public int sunShadowColor = Color.BLACK;

        public boolean showMoonPosition = true;
        public int moonFillColor = Color.WHITE;
        public int moonStrokeColor = Color.BLACK;
        public int moonScale = 72;                    // 72; default moonscale is 3/4 the size of the sun (48)
        public int moonStrokeScale = 3;               // 3; default 3 strokes fit within the radius of the moon

        public boolean showMoonLight = true;
        public int moonLightColor = Color.LTGRAY;

        public boolean translateToLocation = false;
    }

    /**
     * WorldMapProjection
     */
    public static abstract class WorldMapProjection
    {
        public abstract Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options);

        /**
         * Implemented using algorithm found at
         * http://129.79.46.40/~foxd/cdrom/musings/formulas/formulas.htm (Useful Formulas for Amateur SETI)
         * "Hour Angle(HA) and Declination(DE) given the Altitude(AL) and Azimuth(AZ) of a star and the observers Latitude(LA) and Longitude(LO)"
         *
         * 1. Convert Azimuth(AZ) and Altitude(AL) to decimal degrees.
         * 2. Compute sin(DE)=(sin(AL)*sin(LA))+(cos(AL)*cos(LA)*cos(AZ)).
         * 3. Take the inverse sine of sin(DE) to get the declination.
         * 4. Compute cos(HA)=(sin(AL)-(sin(LA)*sin(DE)))/(cos(LA)*cos(DE)).
         * 5. Take the inverse cosine of cos(HA).
         * 6. Take the sine of AZ. If it is positive then HA=360-HA.
         *
         * @param location latitude and longitude
         * @param pos azimuth and altitude
         * @return { greenwich hour angle, declination }
         */
        protected double[] gha(Location location, @NonNull SuntimesCalculator.Position pos)
        {
            double radLat = Math.toRadians(location.getLatitudeAsDouble());
            double sinLat = Math.sin(radLat);
            double cosLat = Math.cos(radLat);

            double radAlt = Math.toRadians(pos.elevation);
            double sinAlt = Math.sin(radAlt);
            double cosAlt = Math.cos(radAlt);

            double radAz = Math.toRadians(pos.azimuth);
            double sinAz = Math.sin(radAz);
            double cosAz = Math.cos(radAz);

            double sinDec = (sinAlt * sinLat) + (cosAlt * cosLat * cosAz);
            double dec = Math.asin(sinDec);  // radians

            double cosHourAngle = (sinAlt - (sinLat * sinDec)) / (cosLat * Math.cos(dec));
            double hourAngle = Math.toDegrees(Math.acos(cosHourAngle));  // local hour angle (degrees)
            if (Math.toDegrees(sinAz) > 0)
                hourAngle = 360 - hourAngle;

            hourAngle = (hourAngle - location.getLongitudeAsDouble()) % 360; // greenwich hour angle (degrees)
            //Log.d(WorldMapView.LOGTAG, "hourAngle is " + hourAngle + ", dec is " + Math.toDegrees(dec) + " (" + pos.declination + ")");
            return new double[] { hourAngle, Math.toDegrees(dec) };
        }

        protected double[] unitVector( double lat, double lon )
        {
            double radLon = Math.toRadians(lon);
            double radLat = Math.toRadians(lat);
            double cosLat = Math.cos(radLat);
                                                       // spherical coordinates to unit vector
            double[] retValue = new double[3];            // v[3] = { (cos(lon)cos(lat), sin(lon)cos(lat), sin(lat)) }
            retValue[0] = Math.cos(radLon) * cosLat;
            retValue[1] = Math.sin(radLon) * cosLat;
            retValue[2] = Math.sin(radLat);
            return retValue;
        }

        protected void drawMap(Canvas c, int w, int h, Paint p, WorldMapTask.WorldMapOptions options)
        {
            Bitmap mapBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas mapCanvas = new Canvas(mapBitmap);
            options.map.setBounds(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
            options.map.draw(mapCanvas);

            if (options.foregroundColor != Color.TRANSPARENT) {
                mapBitmap = SuntimesUtils.tintBitmap(mapBitmap, options.foregroundColor);
            }
            c.drawBitmap(mapBitmap, 0, 0, p);
            mapBitmap.recycle();
        }

        protected void drawSun(Canvas c, int x, int y, Paint p, WorldMapTask.WorldMapOptions options)
        {
            double sunDiameter = (int)Math.ceil(c.getWidth() / (double)options.sunScale);
            int sunRadius = (int)Math.ceil(sunDiameter / 2d);
            int sunStroke = (int)Math.ceil(sunRadius / (double)options.sunStrokeScale);

            p.setStyle(Paint.Style.FILL);
            p.setColor(options.sunFillColor);
            c.drawCircle(x, y, sunRadius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(sunStroke);
            p.setColor(options.sunStrokeColor);
            c.drawCircle(x, y, sunRadius, p);
        }

        protected void drawMoon(Canvas c, int x, int y, Paint p, WorldMapTask.WorldMapOptions options)
        {
            double moonDiameter = Math.ceil(c.getWidth() / (double)options.moonScale);
            int moonRadius = (int)Math.ceil(moonDiameter / 2d);
            int moonStroke = (int)Math.ceil(moonRadius / (double)options.moonStrokeScale);

            p.setStyle(Paint.Style.FILL);
            p.setColor(options.moonFillColor);
            c.drawCircle(x, y, moonRadius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(moonStroke);
            p.setColor(options.moonStrokeColor);
            c.drawCircle(x, y, moonRadius, p);
        }

    }
}
