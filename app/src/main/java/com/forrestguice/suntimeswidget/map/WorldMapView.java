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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

public class WorldMapView extends android.support.v7.widget.AppCompatImageView
{
    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private WorldMapTask drawTask;
    private WorldMapOptions options = new WorldMapOptions();

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
    @SuppressLint("ResourceType")
    private void init(Context context)
    {
        if (isInEditMode())
        {
            setBackgroundColor(Color.WHITE);

            Bitmap b = Bitmap.createBitmap(512, 256, Bitmap.Config.ARGB_8888);
            setImageBitmap(b);
        }

        options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
        options.map = ContextCompat.getDrawable(context, R.drawable.land_shallow_topo_1024);
        options.foregroundColor = ContextCompat.getColor(context, R.color.map_moonlight);

        options.backgroundColor = ContextCompat.getColor(context, R.color.map_background);
        //options.foregroundColor = ContextCompat.getColor(context, R.color.map_foreground);

        options.sunShadowColor = ContextCompat.getColor(context, R.color.map_sunshadow);
        options.moonLightColor = ContextCompat.getColor(context, R.color.map_moonlight);
        options.gridXColor = options.moonLightColor;
        options.gridYColor = options.moonLightColor;

        options.showMajorLatitudes = false;

        int[] colorAttrs = {
                R.attr.graphColor_pointFill,            // 0
                R.attr.graphColor_pointStroke,          // 1
                R.attr.moonriseColor,                   // 2
                R.attr.moonsetColor                     // 3
        };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;

        options.sunFillColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        options.sunStrokeColor = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        options.moonFillColor = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        options.moonStrokeColor = ContextCompat.getColor(context, typedArray.getResourceId(3, def));

        typedArray.recycle();
    }

    public WorldMapOptions getOptions()
    {
        return options;
    }

    public void setOptions( WorldMapOptions options )
    {
        this.options = options;
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

        int w = getWidth();
        if (w > 0)
        {
            drawTask = new WorldMapTask();
            drawTask.setListener(new WorldMapTaskListener()
            {
                @Override
                public void onFinished(Bitmap result)
                {
                    setImageBitmap(result);
                }
            });

            int h = (int)(w * ((double)options.map.getIntrinsicHeight() / (double)options.map.getIntrinsicWidth()));
            drawTask.execute(data, w, h, options);
        }
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
     * WorldMapOptions
     */
    public static class WorldMapOptions
    {
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
        public int sunRadius = 6;
        public int sunStroke = 2;

        public boolean showSunShadow = true;
        public int sunShadowColor = Color.BLACK;

        public boolean showMoonPosition = true;
        public int moonFillColor = Color.WHITE;
        public int moonStrokeColor = Color.BLACK;
        public int moonRadius = 5;
        public int moonStroke = 2;

        public boolean showMoonLight = true;
        public int moonLightColor = Color.LTGRAY;
    }

    /**
     * WorldMapTask
     */
    public static class WorldMapTask extends AsyncTask<Object, Void, Bitmap>
    {
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
                options = (WorldMapOptions)params[3];

            } catch (ClassCastException e) {
                Log.w("WorldMapTask", "Invalid params; using [null, 0, 0]");
                return null;
            }
            return makeBitmap(data, w, h, options);
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
        private double[] gha(WidgetSettings.Location location, SuntimesCalculator.Position pos)
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
            return new double[] { hourAngle, Math.toDegrees(dec) };
        }

        private double[] unitVector( double lat, double lon )
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

        /**
         * @param data
         * @param w
         * @param h
         * @return
         */
        public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, WorldMapOptions options)
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
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

            ////////////////
            // draw background
            p.setColor(options.backgroundColor);
            c.drawRect(0, 0, w, h, p);

            if (options.map != null)
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

            ////////////////
            // draw grid
            if (options.showGrid)
            {
                p.setColor(options.gridXColor);
                for (int i=0; i < 180; i = i + 15)
                {
                    double offset = (i / 180d) * mid[0];
                    int eastX = (int)(mid[0] + offset);
                    int westX = (int)(mid[0] - offset);
                    c.drawLine(eastX, 0, eastX, h, p);
                    c.drawLine(westX, 0, westX, h, p);
                }

                p.setColor(options.gridYColor);
                for (int i=0; i < 90; i = i + 15)
                {
                    double offset = (i / 90d) * mid[1];
                    int northY = (int)(mid[1] + offset);
                    int southY = (int)(mid[1] - offset);
                    c.drawLine(0, northY, w, northY, p);
                    c.drawLine(0, southY, w, southY, p);
                }
            }

            if (options.showMajorLatitudes)
            {
                p.setColor(options.latitudeColors[0]);                    // equator
                c.drawLine(0, (int)mid[1], w, (int)mid[1], p);

                double tropics = (23.439444 / 90d) * mid[1];
                int tropicsY0 = (int)(mid[1] + tropics);
                int tropicsY1 = (int)(mid[1] - tropics);
                p.setColor(options.latitudeColors[1]);                    // tropics
                c.drawLine(0, tropicsY0, w, tropicsY0, p);
                c.drawLine(0, tropicsY1, w, tropicsY1, p);

                double polar = (66.560833 / 90d) * mid[1];
                int polarY0 = (int)(mid[1] + polar);
                int polarY1 = (int)(mid[1] - polar);
                p.setColor(options.latitudeColors[2]);                    // polar
                c.drawLine(0, polarY0, w, polarY0, p);
                c.drawLine(0, polarY1, w, polarY1, p);
            }

            if (data != null)
            {
                Calendar now = data.now();
                SuntimesCalculator calculator = data.calculator();
                SuntimesCalculator.SunPosition sunPos = calculator.getSunPosition(now);
                SuntimesCalculator.MoonPosition moonPos = calculator.getMoonPosition(now);
                WidgetSettings.Location location = data.location();

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
                Log.d("DEBUG", "gmtHours is " + gmtHours + ", gmtArc is " + gmtArc + ", ghaSun is " + ghaSun + " (" + sunPos2[0] + "), ghaSun180 is " + ghaSun180);

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
                    int w0 = 512;
                    int h0 = 256;
                    double iw0 = (1d / w0) * 360d;
                    double ih0 = (1d / h0) * 180d;

                    Bitmap lightBitmap = Bitmap.createBitmap(w0, h0, Bitmap.Config.ARGB_8888);
                    Canvas lightCanvas = new Canvas(lightBitmap);

                    Paint paintShadow = new Paint();
                    paintShadow.setColor(options.sunShadowColor);
                    paintShadow.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                    Paint paintMoonlight = new Paint();
                    paintMoonlight.setColor(options.moonLightColor);
                    paintMoonlight.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                    double radLon, cosLon, sinLon;
                    double radLat, cosLat;
                    double[] v = new double[3];
                    double sunIntensity, moonIntensity;

                    for (int i = 0; i < w0; i++)
                    {
                        radLon = Math.toRadians( ((double) i * iw0) - 180d );  // i in [0,w] to [0,360] to [-180,180]
                        cosLon = Math.cos(radLon);
                        sinLon = Math.sin(radLon);

                        for (int j = 0; j < h0; j++)
                        {
                            radLat = Math.toRadians( -1 * (((double) j * ih0) - 90d) );      // j in [0,h] to [0,180] to [-90,90] (inverted to canvas)
                            cosLat = Math.cos(radLat);

                            v[0] = cosLon * cosLat;
                            v[1] = sinLon * cosLat;
                            v[2] = Math.sin(radLat);

                            if (options.showSunShadow)
                            {
                                sunIntensity = (sunUp[0] * v[0]) + (sunUp[1] * v[1]) + (sunUp[2] * v[2]);    // intensity = up.dotProduct(v)
                                if (sunIntensity <= 0) {                                                               // values less equal 0 are in shadow
                                    lightCanvas.drawPoint(i, j, paintShadow);
                                }
                            }

                            if (options.showMoonLight)
                            {
                                moonIntensity = (moonUp[0] * v[0]) + (moonUp[1] * v[1]) + (moonUp[2] * v[2]);
                                if (moonIntensity > 0) {
                                    lightCanvas.drawPoint(i, j, paintMoonlight);
                                }
                            }
                        }
                    }

                    Bitmap scaledLightBitmap = Bitmap.createScaledBitmap(lightBitmap, w, h, true);
                    c.drawBitmap(scaledLightBitmap, 0, 0, p);
                    scaledLightBitmap.recycle();
                    lightBitmap.recycle();
                }

                ////////////////
                // draw sun
                if (options.showSunPosition)
                {
                    int sunX = (int) (mid[0] - ((ghaSun180 / 180d) * mid[0]));
                    int sunY = (int) (mid[1] - ((sunPos.declination / 90d) * mid[1]));

                    p.setStyle(Paint.Style.FILL);
                    p.setColor(options.sunFillColor);
                    c.drawCircle(sunX, sunY, options.sunRadius, p);

                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(options.sunStroke);
                    p.setColor(options.sunStrokeColor);
                    c.drawCircle(sunX, sunY, options.sunRadius, p);
                }

                ////////////////
                // draw moon
                if (options.showMoonPosition)
                {
                    int moonX = (int) (mid[0] - ((moonPos2[0] / 180d) * mid[0]));
                    int moonY = (int) (mid[1] - ((moonPos2[1] / 90d) * mid[1]));

                    p.setStyle(Paint.Style.FILL);
                    p.setColor(options.moonFillColor);
                    c.drawCircle(moonX, moonY, options.moonRadius, p);

                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(options.moonStroke);
                    p.setColor(options.moonStrokeColor);
                    c.drawCircle(moonX, moonY, options.moonRadius, p);
                }
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
