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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

public class WorldMapView extends android.support.v7.widget.AppCompatImageView
{
    public static final String LOGTAG = "WorldMap";
    public static final int DEFAULT_MAX_UPDATE_RATE = 1 * 1000;  // ms value; once a second

    private WorldMapTask drawTask;
    private WorldMapTask.WorldMapOptions options = new WorldMapTask.WorldMapOptions();
    private WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE;

    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;
    private int mapW = 0, mapH = 0;
    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

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
        setMapMode(context, mode);
    }

    public WorldMapWidgetSettings.WorldMapWidgetMode getMapMode()
    {
        return mode;
    }

    @SuppressLint("ResourceType")
    public void setMapMode(Context context, WorldMapWidgetSettings.WorldMapWidgetMode mode )
    {
        this.mode = mode;
        switch (mode)
        {
            case EQUIAZIMUTHAL_SIMPLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap2);
                options.foregroundColor = ContextCompat.getColor(context, R.color.map_foreground);
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.land_shallow_topo_1024);
                options.foregroundColor = Color.TRANSPARENT;
                break;

            case EQUIRECTANGULAR_SIMPLE:
            default:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
                options.foregroundColor = ContextCompat.getColor(context, R.color.map_foreground);
                break;
        }

        options.backgroundColor = ContextCompat.getColor(context, R.color.map_background);
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

    public WorldMapTask.WorldMapOptions getOptions()
    {
        return options;
    }

    public void setOptions( WorldMapTask.WorldMapOptions options )
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
        Log.w(LOGTAG, "onSizeChanged: " + oldw + ", " + oldh + " => " + w + ", " + h );

        if (resizable && w > 0 && h > 0)
        {
            Log.w(LOGTAG, "onSizeChanged: valid dimensions, triggering update...");
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
        boolean sameData = (this.data == data);
        this.data = data;

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            drawTask.cancel(true);
        }

        int w = getWidth();
        int h = (int)(w * ((double)options.map.getIntrinsicHeight() / (double)options.map.getIntrinsicWidth()));

        if (w > 0 && h > 0)
        {
            boolean sameDimensions = (w == mapW && h == mapH);
            boolean sameOptions = !options.modified;
            boolean throttleUpdate = ((System.currentTimeMillis() - lastUpdate) < maxUpdateRate);

            boolean skipUpdate = (sameData && sameDimensions && sameOptions && throttleUpdate);
            if (skipUpdate)
            {
                Log.w(LOGTAG, "updateViews: " + w + ", " + h + " (image is unchanged; skipping)");
                return;
            }

            drawTask = new WorldMapTask();
            drawTask.setListener(new WorldMapTaskListener()
            {
                @Override
                public void onFinished(Bitmap result)
                {
                    mapW = result.getWidth();
                    mapH = result.getHeight();
                    setImageBitmap(result);
                }
            });

            WorldMapTask.WorldMapProjection projection;
            switch (mode)
            {
                case EQUIAZIMUTHAL_SIMPLE:
                    projection = new WorldMapEquiazimuthal();
                    break;

                case EQUIRECTANGULAR_BLUEMARBLE:
                case EQUIRECTANGULAR_SIMPLE:
                default:
                    projection = new WorldMapEquirectangular();
                    break;
            }

            Log.w(LOGTAG, "updateViews: " + w + ", " + h );
            drawTask.execute(data, w, h, options, projection);
            options.modified = false;
            lastUpdate = System.currentTimeMillis();
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
     * WorldMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class WorldMapTaskListener
    {
        public void onFinished( Bitmap result ) {}
    }

}
