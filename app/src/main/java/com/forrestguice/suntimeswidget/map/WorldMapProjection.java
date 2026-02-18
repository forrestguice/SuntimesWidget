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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import java.util.ArrayList;
import java.util.Calendar;

public abstract class WorldMapProjection
{
    /**
     * algorithm described at https://gis.stackexchange.com/questions/17184/method-to-shade-or-overlay-a-raster-map-to-reflect-time-of-day-and-ambient-light
     */
    public abstract Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options);
    public abstract void initPaint(WorldMapOptions options);
    public abstract double[] initMatrix();            // creates flattened multi-dimensional array; [lon][lat][v(3)]
    public abstract double[] getMatrix();
    public abstract void resetMatrix();
    public abstract int[] matrixSize();               // [width(lon), height(lat)]
    protected abstract int k(int x, int y, int z);    // returns index into flattened array
    public abstract int[] toBitmapCoords(int w, int h, double[] mid, double lat, double lon);
    public abstract double[] fromBitmapCoords(int x, int y, double[] mid, int w, int h);
    public double[] getCenter() { return new double[] {0,0}; }

    protected Calendar mapTime(SuntimesRiseSetDataset data, WorldMapOptions options)
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

    protected void drawMap(Canvas c, int w, int h, @NonNull Paint paintForeground, WorldMapOptions options)
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

    protected double sunRadius(Canvas c, WorldMapOptions options)
    {
        double sunDiameter = (int)Math.ceil(c.getWidth() / (double)options.sunScale);
        return (int)Math.ceil(sunDiameter * 0.5d);
    }

    protected int sunStroke(Canvas c, WorldMapOptions options)
    {
        return (int)Math.ceil(sunRadius(c, options) / (double)options.sunStrokeScale);
    }

    protected void drawSun(Canvas c, int x, int y, @NonNull Paint paintFill, @NonNull Paint paintStroke, WorldMapOptions options)
    {
        int sunRadius = (int)sunRadius(c, options);
        int sunStroke = (int)Math.ceil(sunRadius / (double)options.sunStrokeScale);

        paintStroke.setStrokeWidth(sunStroke);
        c.drawCircle(x, y, sunRadius, paintFill);
        c.drawCircle(x, y, sunRadius, paintStroke);
    }

    protected void drawMoon(Canvas c, int x, int y, @NonNull Paint paintFill, @NonNull Paint paintStroke, WorldMapOptions options)
    {
        double moonDiameter = Math.ceil(c.getWidth() / (double)options.moonScale);
        int moonRadius = (int)Math.ceil(moonDiameter * 0.5d);
        int moonStroke = (int)Math.ceil(moonRadius / (double)options.moonStrokeScale);

        paintStroke.setStrokeWidth(moonStroke);
        c.drawCircle(x, y, moonRadius, paintFill);
        c.drawCircle(x, y, moonRadius, paintStroke);
    }

    public void drawGrid(Canvas c, int w, int h, double[] mid, WorldMapOptions options) { /* EMPTY */ }
    public void drawMajorLatitudes(Canvas c, int w, int h, double[] mid, WorldMapOptions options) { /* EMPTY */ }
    public void drawDebugLines(Canvas c, int w, int h, double[] mid, WorldMapOptions options) { /* EMPTY */ }
    public void drawLocations(Canvas c, int w, int h, Paint p1, Paint p2, WorldMapOptions options)
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

    protected void drawLocation(Canvas c, int x, int y, Paint p1, Paint p2, WorldMapOptions options)
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
