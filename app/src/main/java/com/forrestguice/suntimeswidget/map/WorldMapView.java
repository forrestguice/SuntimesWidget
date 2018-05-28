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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

public class WorldMapView extends android.support.v7.widget.AppCompatImageView
{
    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private WorldMapTask drawTask;
    private Drawable background;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;


    public WorldMapView(Context context)
    {
        super(context);
        init(context);
    }

    public WorldMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        if (isInEditMode())
        {
            setBackgroundColor(Color.WHITE);
        }

        background = ContextCompat.getDrawable(context, R.drawable.world_map_blank_without_borders);
    }

    public int getMaxUpdateRate()
    {
        return maxUpdateRate;
    }

    public void setResizable( boolean value )
    {
        resizable = value;
    }

    /**
     *
     */
    public void onResume()
    {
        Log.d("DEBUG", "WorldMapView onResume");
    }

    /**
     * @param w the changed width
     * @param h the changed height
     * @param oldw the previous width
     * @param oldh the previous height
     */
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (resizable)
        {
            updateViews(true);
        }
    }

    /**
     * throttled update method
     */
    public void updateViews( boolean forceUpdate )
    {
        long timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);
        if (forceUpdate || timeSinceLastUpdate >= maxUpdateRate)
        {
            updateViews(data);
        }
    }

    /**
     * @param data an instance of SuntimesRiseSetDataset
     */
    public void updateViews(SuntimesRiseSetDataset data)
    {
        this.data = data;

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            drawTask.cancel(true);
        }
        drawTask = new WorldMapTask(getContext());
        drawTask.setListener(new WorldMapTaskListener()
        {
            @Override
            public void onFinished(Bitmap result)
            {
                setImageBitmap(result);
            }
        });
        drawTask.execute(data, background.getIntrinsicWidth(), background.getIntrinsicHeight(), background);
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        //Log.d("DEBUG", "WorldMapView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    /**
     * @param context a context used to access resources
     * @param bundle a Bundle used to load state
     */
    protected void loadSettings(Context context, Bundle bundle )
    {
        //Log.d("DEBUG", "WorldMapView loadSettings (bundle)");
    }

    /**
     * @param context a context used to access shared prefs
     * @return true settings were saved
     */
    protected boolean saveSettings(Context context)
    {
        //Log.d("DEBUG", "WorldMapView saveSettings (prefs)");
        return true;
    }

    /**
     * @param bundle a Bundle used to save state
     * @return true settings were saved
     */
    protected boolean saveSettings(Bundle bundle)
    {
        //Log.d("DEBUG", "WorldMapView saveSettings (bundle)");
        return true;
    }

    /**
     * WorldMapTask
     */
    public static class WorldMapTask extends AsyncTask<Object, Void, Bitmap>
    {
        private Drawable map;
        private int shadowColor = Color.GRAY;
        private int backgroundColor = Color.DKGRAY;

        private int sunFillColor, sunStrokeColor;
        private int sunRadius = 6;
        private int sunStroke = 2;

        private int moonFillColor, moonStrokeColor;
        private int moonRadius = 5;
        private int moonStroke = 2;

        @SuppressLint("ResourceType")
        public WorldMapTask(Context context)
        {
            int[] colorAttrs = {
                    R.attr.graphColor_pointFill,            // 0
                    R.attr.graphColor_pointStroke,          // 1
                    R.attr.moonriseColor,                   // 2
                    R.attr.moonsetColor                     // 3
            };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;

            sunFillColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            sunStrokeColor = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            moonFillColor = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            moonStrokeColor = ContextCompat.getColor(context, typedArray.getResourceId(3, def));

            typedArray.recycle();

            shadowColor = ContextCompat.getColor(context, R.color.card_bg_darktrans);
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
                map = (Drawable)params[3];

            } catch (ClassCastException e) {
                Log.w("WorldMapTask", "Invalid params; using [null, 0, 0]");
                return null;
            }
            return makeBitmap(data, w, h);
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
         * @return greenwich hour angle
         */
        private double gha(WidgetSettings.Location location, SuntimesCalculator.Position pos)
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
            Log.d("DEBUG", "hourAngle is " + hourAngle + ", dec is " + Math.toDegrees(dec) + " (" + pos.declination + ")");
            return hourAngle;
        }

        /**
         * @param data
         * @param w
         * @param h
         * @return
         */
        public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h)
        {
            long bench_start = System.nanoTime();
            if (w <= 0 || h <= 0)
            {
                return null;
            }

            double[] mid = new double[2];
            mid[0] = w/2d;
            mid[1] = h/2d;

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            ////////////////
            // draw background
            p.setColor(backgroundColor);
            c.drawRect(0, 0, w, h, p);

            if (map != null)
            {
                Bitmap mapBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas mapCanvas = new Canvas(mapBitmap);
                map.setBounds(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
                map.draw(mapCanvas);
                c.drawBitmap(mapBitmap, 0, 0, p);
            }

            if (data != null)
            {
                Calendar now = data.now();
                SuntimesCalculator calculator = data.calculator();
                SuntimesCalculator.SunPosition sunPos = calculator.getSunPosition(now);
                SuntimesCalculator.MoonPosition moonPos = calculator.getMoonPosition(now);
                WidgetSettings.Location location = data.location();

                ////////////////
                // draw sun shadow

                //TODO: for algorithm see https://gis.stackexchange.com/questions/17184/method-to-shade-or-overlay-a-raster-map-to-reflect-time-of-day-and-ambient-light
                /**p.setColor(shadowColor);
                 for (int i=0; i<w; i++)
                 {
                 double lon = (((double)i / (double)w) * 360d) - 180d;  // i in [0,w] to [0,360] to [-180,180]
                 for (int j=0; j<h; j++)
                 {
                 double lat = (((double)j / (double)h) * 180d) - 90d;      // j in [0,h] to [0,180] to [-90,90]
                 // TODO
                 }
                 }*/

                ////////////////
                // draw sun
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

                double sunHourAngle = gha(location, sunPos);
                Log.d("DEBUG", "gmtHours is " + gmtHours + ", gmtArc is " + gmtArc + ", ghaSun is " + ghaSun + " (" + sunHourAngle + "), ghaSun180 is " + ghaSun180);

                int sunX = (int)(mid[0] - ((ghaSun180 / 180d) * mid[0]));
                int sunY = (int)(mid[1] - ((sunPos.declination / 90d) * mid[1]));

                p.setStyle(Paint.Style.FILL);
                p.setColor(sunFillColor);
                c.drawCircle(sunX, sunY, sunRadius, p);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(sunStroke);
                p.setColor(sunStrokeColor);
                c.drawCircle(sunX, sunY, sunRadius, p);

                ////////////////
                // draw moon shadow

                // TODO

                ////////////////
                // draw moon
                double ghaMoon180 = gha(location, moonPos);                        // [180, -180] west
                if (ghaMoon180 > 180)
                    ghaMoon180 = ghaMoon180 - 360;

                int moonX = (int)(mid[0] - ((ghaMoon180 / 180d) * mid[0]));
                int moonY = (int)(mid[1] - ((moonPos.declination / 90d) * mid[1]));

                p.setStyle(Paint.Style.FILL);
                p.setColor(moonFillColor);
                c.drawCircle(moonX, moonY, moonRadius, p);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(moonStroke);
                p.setColor(moonStrokeColor);
                c.drawCircle(moonX, moonY, moonRadius, p);
            }

            long bench_end = System.nanoTime();
            Log.d("DEBUG", "make world map :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            return b;
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

        private WorldMapTaskListener listener = null;
        public void setListener( WorldMapTaskListener listener )
        {
            this.listener = listener;
        }
        public void clearListener()
        {
            this.listener = null;
        }
    }

    /**
     * WorldMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class WorldMapTaskListener
    {
        public void onFinished( Bitmap result ) {}
    }

}
