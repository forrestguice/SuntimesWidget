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
    public static final int DEFAULT_WIDTH = 288;
    public static final int DEFAULT_HEIGHT = 16;

    private int imgWidth = DEFAULT_WIDTH, imgHeight = DEFAULT_HEIGHT;
    private int colorNight, colorAstro, colorNautical, colorCivil, colorDay, colorPointFill, colorPointStroke;

    private ImageView mainView;

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

    private void initLayout(Context context)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.info_time_lightmap, this);
    }

    /**
     *
     * @param context
     */
    protected void initViews( Context context )
    {
        Log.d("DEBUG", "LightMapView initViews");

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

    }

    public void onResume()
    {
        Log.d("DEBUG", "LightMapView onResume");
    }

    /**
     * @param data
     */
    public void updateViews(SuntimesRiseSetDataset data)
    {
        if (mainView != null)
        {
            int w = imgWidth, h = imgHeight;

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            // draw background (night)
            p.setColor(colorNight);
            c.drawRect(0, 0, w, h, p);

            // draw astro twilight
            p.setColor(colorAstro);
            drawRect(data.dataAstro, c, p);

            // draw nautical twilight
            p.setColor(colorNautical);
            drawRect(data.dataNautical, c, p);

            // draw civil twilight
            p.setColor(colorCivil);
            drawRect(data.dataCivil, c, p);

            // draw foreground (day)
            p.setColor(colorDay);
            drawRect(data.dataActual, c, p);

            mainView.setImageBitmap(b);
            Log.d("updateViews", "lightmap updated");
        }
    }

    private static final double MINUTES_IN_DAY = 24 * 60;

    private void drawRect( SuntimesRiseSetData data, Canvas c, Paint p )
    {
        int w = c.getWidth();
        int h = c.getHeight();

        Calendar riseTime = data.sunriseCalendarToday();
        double riseMinute = riseTime.get(Calendar.HOUR_OF_DAY) * 60 + riseTime.get(Calendar.MINUTE);
        double riseR = riseMinute / MINUTES_IN_DAY;
        int left = (int)Math.round(riseR * w);

        Calendar setTime = data.sunsetCalendarToday();
        double setMinute = setTime.get(Calendar.HOUR_OF_DAY) * 60 + setTime.get(Calendar.MINUTE);
        double setR = setMinute / MINUTES_IN_DAY;
        int right = (int)Math.round(setR * w);

        c.drawRect(left, 0, right, h, p);
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
