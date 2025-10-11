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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.util.android.AndroidResources;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * WorldMapTask
 */
public class WorldMapTask extends AsyncTask<Object, Bitmap, Bitmap>
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
        int numFrames = 1;
        long frameDuration = 250000000;    // nanoseconds (250 ms)
        long initialOffset = 0;
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
            if (params.length > 5) {
                numFrames = (int)params[5];
            }
            if (params.length > 6) {
                initialOffset = (long)params[6];
            }
            frameDuration = options.anim_frameLengthMs * 1000000;   // ms to ns

        } catch (ClassCastException e) {
            Log.w("WorldMapTask", "Invalid params; using [null, 0, 0]");
            return null;
        }

        long time0 = System.nanoTime();
        Bitmap frame = null;
        options.offsetMinutes = initialOffset;

        int i = 0;
        while (i < numFrames || numFrames <= 0)
        {
            if (isCancelled()) {
                break;
            }
            frame = makeBitmap(data, w, h, options);

            long time1 = System.nanoTime();
            while ((time1 - time0) < frameDuration) {
                time1 = System.nanoTime();
            }

            publishProgress(frame);
            if (listener != null) {
                listener.afterFrame(frame, options.offsetMinutes);
            }
            options.offsetMinutes += options.anim_frameOffsetMinutes;
            time0 = System.nanoTime();
            i++;
        }
        options.offsetMinutes -= options.anim_frameOffsetMinutes;
        return frame;
    }

    public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options)
    {
        return projection.makeBitmap(data, w, h, options);
    }

    @Override
    protected void onPreExecute()
    {
        if (listener != null) {
            listener.onStarted();
        }
    }

    @Override
    protected void onProgressUpdate( Bitmap... frames )
    {
        if (listener != null)
        {
            for (int i=0; i<frames.length; i++) {
                listener.onFrame(frames[i], options.offsetMinutes);
            }
        }
    }

    @Override
    protected void onPostExecute( Bitmap lastFrame )
    {
        if (isCancelled()) {
            lastFrame = null;
        }
        if (listener != null) {
            listener.onFinished(lastFrame);
        }
    }

    /////////////////////////////////////////////

    private WorldMapTaskListener listener = null;
    public void setListener( WorldMapTaskListener listener ) {
        this.listener = listener;
    }

    /**
     * WorldMapOptions
     */
    public static class WorldMapOptions
    {
        public boolean modified = false;

        public WorldMapOptions() {
            colors = new WorldMapColorValues();
        }
        public WorldMapOptions(Context context) {
            init(context);
        }

        public void init(Context context) {
            colors = new WorldMapColorValues(AndroidResources.wrap(context), true);
        }
        public WorldMapColorValues colors;
        public int foregroundColor = Color.TRANSPARENT;

        public Drawable map = null;                  // BitmapDrawable
        public Drawable map_night = null;            // BitmapDrawable
        public boolean tintForeground = true;
        public boolean hasTransparentBaseMap = true;
        public boolean showDebugLines = false;

        public boolean showGrid = false;

        public boolean showMajorLatitudes = false;
        public int[] latitudeColors = { Color.DKGRAY, Color.WHITE, Color.DKGRAY };    // equator, tropics, polar circle
        float[][] latitudeLinePatterns = new float[][] {{ 0, 0 }, {5, 10}, {10, 5}};    // {dash-on, dash-off} .. for equator, tropics, and polar circle .. dash-on 0 for a solid line
        public float latitudeLineScale = 0.5f;

        public boolean showSunPosition = true;
        public boolean showSunShadow = true;
        public boolean showMoonPosition = true;
        public boolean showMoonLight = true;

        public int sunScale = 48;                     // 48; default 48 suns fit within the width of the image (which is 24 hr wide meaning the sun has diameter of a half-hour)
        public int sunStrokeScale = 3;                // 3; default 3 strokes fit within the radius of the sun (i.e. the stroke is 1/3 the width)

        public int moonScale = 72;                    // 72; default moonscale is 3/4 the size of the sun (48)
        public int moonStrokeScale = 3;               // 3; default 3 strokes fit within the radius of the moon

        public boolean translateToLocation = false;

        public double[] center = null;
        public double[][] locations = null;  // a list of locations {{lat, lon}, {lat, lon}, ...} or null
        public double locationScale = 1 / 192d;

        public long offsetMinutes = 0;    // minutes offset from "now" (default 0)
        public long now = -1;            // -1 (current)

        public int anim_frameLengthMs = 100;         // frames shown for 100 ms
        public int anim_frameOffsetMinutes = 3;      // each frame 3 minutes apart
    }

    /**
     * WorldMapProjection
     */
    public static abstract class WorldMapProjection
    {
        /**
         * algorithm described at https://gis.stackexchange.com/questions/17184/method-to-shade-or-overlay-a-raster-map-to-reflect-time-of-day-and-ambient-light
         */
        public abstract Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapTask.WorldMapOptions options);
        public abstract void initPaint(WorldMapTask.WorldMapOptions options);
        public abstract double[] initMatrix();            // creates flattened multi-dimensional array; [lon][lat][v(3)]
        public abstract double[] getMatrix();
        public abstract void resetMatrix();
        public abstract int[] matrixSize();               // [width(lon), height(lat)]
        protected abstract int k(int x, int y, int z);    // returns index into flattened array
        public abstract int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon);
        public double[] getCenter() { return new double[] {0,0}; }

        protected Calendar mapTime(SuntimesRiseSetDataset data, WorldMapTask.WorldMapOptions options)
        {
            Calendar mapTime;
            if (options.now >= 0)
            {
                mapTime = Calendar.getInstance();
                mapTime.setTimeInMillis(options.now);       // preset time

            } else {
                mapTime = data.nowThen(data.calendar());    // the current time (maybe on some other day)
                options.now = mapTime.getTimeInMillis();
            }

            long minutes = options.offsetMinutes;
            while (minutes > Integer.MAX_VALUE) {
                minutes = minutes - Integer.MAX_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MAX_VALUE);
            }
            while (minutes < Integer.MIN_VALUE) {
                minutes = minutes + Integer.MIN_VALUE;
                mapTime.add(Calendar.MINUTE, Integer.MIN_VALUE);
            }
            mapTime.add(Calendar.MINUTE, (int)minutes);    // remaining minutes

            return mapTime;
        }

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

        protected void drawMap(Canvas c, int w, int h, @NonNull Paint paintForeground, WorldMapTask.WorldMapOptions options)
        {
            if (options.map != null)
            {
                if (options.foregroundColor != Color.TRANSPARENT)
                {
                    Bitmap b = ((BitmapDrawable)options.map).getBitmap();
                    Rect src = new Rect(0,0, b.getWidth(), b.getHeight());
                    Rect dst = new Rect(0,0, w, h);
                    c.drawBitmap(b, src, dst, paintForeground);

                } else {
                    options.map.setBounds(0, 0, w, h);
                    options.map.draw(c);
                }
            }
        }

        protected double sunRadius(Canvas c, WorldMapTask.WorldMapOptions options)
        {
            double sunDiameter = (int)Math.ceil(c.getWidth() / (double)options.sunScale);
            return (int)Math.ceil(sunDiameter * 0.5d);
        }

        protected int sunStroke(Canvas c, WorldMapTask.WorldMapOptions options)
        {
            return (int)Math.ceil(sunRadius(c, options) / (double)options.sunStrokeScale);
        }

        protected void drawSun(Canvas c, int x, int y, @NonNull Paint paintFill, @NonNull Paint paintStroke, WorldMapTask.WorldMapOptions options)
        {
            int sunRadius = (int)sunRadius(c, options);
            int sunStroke = (int)Math.ceil(sunRadius / (double)options.sunStrokeScale);

            paintStroke.setStrokeWidth(sunStroke);
            c.drawCircle(x, y, sunRadius, paintFill);
            c.drawCircle(x, y, sunRadius, paintStroke);
        }

        protected void drawMoon(Canvas c, int x, int y, @NonNull Paint paintFill, @NonNull Paint paintStroke, WorldMapTask.WorldMapOptions options)
        {
            double moonDiameter = Math.ceil(c.getWidth() / (double)options.moonScale);
            int moonRadius = (int)Math.ceil(moonDiameter * 0.5d);
            int moonStroke = (int)Math.ceil(moonRadius / (double)options.moonStrokeScale);

            paintStroke.setStrokeWidth(moonStroke);
            c.drawCircle(x, y, moonRadius, paintFill);
            c.drawCircle(x, y, moonRadius, paintStroke);
        }

        public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options) { /* EMPTY */ }
        public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options) { /* EMPTY */ }
        public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapTask.WorldMapOptions options) { /* EMPTY */ }
        public void drawLocations(Canvas c, int w, int h, Paint p1, Paint p2, WorldMapTask.WorldMapOptions options)
        {
            if (options.locations != null && options.locations.length > 0)
            {
                double[] mid = new double[] { w/2d, h/2d };
                for (int i=0; i<options.locations.length; i++)
                {
                    int[] point = toBitmapCoords(w, h, mid, options.locations[i][0], options.locations[i][1]);
                    drawLocation(c, point[0], point[1], p1, p2, options);
                    //Log.d("DEBUG", "drawLocations: " + options.locations[i][0] + ", " + options.locations[i][1]);
                }
            }
        }

        protected void drawLocation(Canvas c, int x, int y, Paint p1, Paint p2, WorldMapTask.WorldMapOptions options)
        {
            double pointDiameter = (int)Math.ceil(c.getWidth() * options.locationScale);
            int pointRadius = (int)Math.ceil(pointDiameter * 0.5d);

            if (p1 != null) {
                c.drawCircle(x, y, pointRadius, p1);
            }
            if (p2 != null) {
                c.drawCircle(x, y, pointRadius, p2);
            }
        }

        protected void drawConnectedLines(Canvas c, float[] lines, Paint p)
        {
            c.drawLines(lines, 0, lines.length, p);
            c.drawLines(lines, 2,lines.length-2, p);
        }

        public static float[] toFloatArray(ArrayList<Float> values)
        {
            float[] retvalue = new float[values.size()];
            for (int i=0; i<retvalue.length; i++) {
                retvalue[i] = values.get(i);
            }
            return retvalue;
        }
    }

    /**
     * WorldMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class WorldMapTaskListener
    {
        public void onStarted() {}
        public void onFrame(Bitmap frame, long offsetMinutes ) {}
        public void afterFrame(Bitmap frame, long offsetMinutes ) {}
        public void onFinished( Bitmap result ) {}
    }

}
