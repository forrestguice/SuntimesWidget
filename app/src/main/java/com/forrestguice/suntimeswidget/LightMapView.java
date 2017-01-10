/**
    Copyright (C) 2014 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

import java.util.Calendar;

/**
 * LightMapView .. a stacked bar graph over the duration of a day showing relative duration of
 * night, day, and twilight times.
 */
public class LightMapView extends LinearLayout
{
    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s
    public static final int DEFAULT_WIDTH = 288;
    public static final int DEFAULT_HEIGHT = 16;

    private int imgWidth = DEFAULT_WIDTH, imgHeight = DEFAULT_HEIGHT;
    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;
    private int colorNight, colorAstro, colorNautical, colorCivil, colorDay, colorPointFill, colorPointStroke;

    private ImageView mainView;
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;

    public LightMapView(Context context)
    {
        super(context);
        initLayout(context);
        initViews(context);
    }

    public LightMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        initLayout(context);
        initViews(context);
    }

    /**
     * @param context
     */
    private void initLayout(Context context)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.info_time_lightmap, this);
    }

    /**
     * @param context
     */
    protected void initViews( Context context )
    {
        mainView = (ImageView) findViewById(R.id.lightmap_view);
        initColors(context);
    }

    /**
     * @param context
     */
    private void initColors(Context context)
    {
        int[] colorAttrs = { R.attr.graphColor_night,   // 0
                R.attr.graphColor_astronomical,         // 1
                R.attr.graphColor_nautical,             // 2
                R.attr.graphColor_civil,                // 3
                R.attr.graphColor_day,                  // 4
                R.attr.graphColor_pointFill,            // 5
                R.attr.graphColor_pointStroke };        // 6
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.color_transparent;

        colorNight = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        colorNautical = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        colorDay = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        colorPointFill = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
        colorPointStroke = ContextCompat.getColor(context, typedArray.getResourceId(6, def));

        typedArray.recycle();
    }

    public int getMaxUpdateRate()
    {
        return maxUpdateRate;
    }

    /**
     *
     */
    public void onResume()
    {
        Log.d("DEBUG", "LightMapView onResume");
    }

    /**
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

     * @param data
     */
    public void updateViews(SuntimesRiseSetDataset data)
    {
        this.data = data;
        int w = getWidth();
        int h = getHeight();

        if (mainView != null && w > 0 && h > 0)
        {
            long bench_start = System.currentTimeMillis();

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            // draw background (night)
            p.setColor(colorNight);
            c.drawRect(0, 0, w, h, p);

            if (data != null)
            {
                // draw astro twilight
                p.setColor(colorAstro);
                boolean wasDrawn = drawRect(data.dataAstro, c, p, false);

                // draw nautical twilight
                p.setColor(colorNautical);
                wasDrawn |= drawRect(data.dataNautical, c, p, wasDrawn);

                // draw civil twilight
                p.setColor(colorCivil);
                wasDrawn |= drawRect(data.dataCivil, c, p, wasDrawn);

                // draw foreground (day)
                p.setColor(colorDay);
                wasDrawn |= drawRect(data.dataActual, c, p, wasDrawn);

                // draw now marker
                drawPoint(data.now(), pointRadius, c, p);
            }

            mainView.setImageBitmap(b);
            lastUpdate = System.currentTimeMillis();
            Log.d("DEBUG", "updateViews " + w + ", " + h + ", " + mainView);
            Log.d("DEBUG", "lightmap updated in " + (lastUpdate - bench_start) + "ms");
        }
    }

    private void drawPoint( Calendar calendar, int radius, Canvas c, Paint p )
    {
        if (calendar != null)
        {
            int w = c.getWidth();
            int h = c.getHeight();

            double minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            int x = (int) Math.round((minute / MINUTES_IN_DAY) * w);
            int y = h / 2;

            p.setStyle(Paint.Style.FILL);
            p.setColor(colorPointFill);
            c.drawCircle(x, y, radius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(pointStrokeWidth);
            p.setColor(colorPointStroke);
            c.drawCircle(x, y, radius, p);
        }
    }

    /**
     * @param data draw rise/set period of data
     * @param c the canvas to draw to
     * @param p the paint object to use
     * @param isFloatingLayer true if this rect is supposed to be layered onto another layer, false if this is the base layer
     * @return
     */
    private boolean drawRect( SuntimesRiseSetData data, Canvas c, Paint p, boolean isFloatingLayer)
    {
        Calendar riseTime = data.sunriseCalendarToday();
        Calendar setTime = data.sunsetCalendarToday();
        if (riseTime == null && setTime == null && isFloatingLayer)
        {
            Log.d("DEBUG", "floating layer with no rise/set.. return false:");
            return false;
        }

        int w = c.getWidth();
        int h = c.getHeight();
        int left = 0;
        int right = w;

        if (riseTime != null)
        {
            double riseMinute = riseTime.get(Calendar.HOUR_OF_DAY) * 60 + riseTime.get(Calendar.MINUTE);
            double riseR = riseMinute / MINUTES_IN_DAY;
            left = (int) Math.round(riseR * w);
        }

        if (setTime != null)
        {
            double setMinute = setTime.get(Calendar.HOUR_OF_DAY) * 60 + setTime.get(Calendar.MINUTE);
            double setR = setMinute / MINUTES_IN_DAY;
            right = (int) Math.round(setR * w);
        }

        c.drawRect(left, 0, right, h, p);
        return true;
    }

    /**
     *
     */
    protected void loadSettings(Context context)
    {
        Log.d("DEBUG", "LightMapView loadSettings (prefs)");
        if (isInEditMode())
            return;
    }

    /**
     *
     */
    protected void loadSettings(Context context, Bundle bundle )
    {
        Log.d("DEBUG", "LightMapView loadSettings (bundle)");
    }

    /**
     *
     */
    protected boolean saveSettings(Context context)
    {
        Log.d("DEBUG", "LightMap loadSettings (prefs)");
        return false;
    }

    /**
     * @param bundle
     * @return
     */
    protected boolean saveSettings(Bundle bundle)
    {
        Log.d("DEBUG", "LightMapView saveSettings (bundle)");
        return true;
    }

}
