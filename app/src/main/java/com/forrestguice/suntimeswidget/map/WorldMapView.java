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

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.io.File;

public class WorldMapView extends android.support.v7.widget.AppCompatImageView
{
    public static final String LOGTAG = "WorldMap";
    public static final int DEFAULT_MAX_UPDATE_RATE = 1000;  // ms value; once a second

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
            setImageBitmap(Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888));
        }
        setMapMode(context, mode);
        themeViews(context);
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
                options.foregroundColor = foregroundColor;
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.land_shallow_topo_1024);
                options.foregroundColor = Color.TRANSPARENT;
                break;

            case EQUIRECTANGULAR_SIMPLE:
            default:
                options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
                options.foregroundColor = foregroundColor;
                break;
        }
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

    @SuppressLint("ResourceType")
    private void themeViews(Context context)
    {
        foregroundColor = ContextCompat.getColor(context, R.color.map_foreground);
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

    private int foregroundColor;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        foregroundColor = theme.getMapForegroundColor();
        options.backgroundColor = theme.getMapBackgroundColor();
        options.sunShadowColor = theme.getMapShadowColor();
        options.moonLightColor = theme.getMapHighlightColor();
        options.gridXColor = options.moonLightColor;
        options.gridYColor = options.moonLightColor;
        options.sunFillColor = theme.getNoonIconColor();
        options.sunStrokeColor = theme.getNoonIconStrokeColor();
        options.moonFillColor = theme.getMoonFullColor();
        options.moonStrokeColor = theme.getMoonWaningColor();
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
        int h = getHeight();
        WorldMapTask.WorldMapProjection projection;
        switch (mode)
        {
            case EQUIAZIMUTHAL_SIMPLE:

                //int orientation = getResources().getConfiguration().orientation;
                projection = new WorldMapEquiazimuthal();
                //w = h = (orientation == Configuration.ORIENTATION_PORTRAIT) ? Math.max(getWidth(), getHeight()) : Math.min(getWidth(), getHeight());
                if (w > 0)
                {
                    if (h > 0)
                    {
                        // fit smallest
                        // has width and height, use smallest
                        //w = h = Math.min(w, h);

                        // fit larger
                        // has width and height, use larger
                        w = h = Math.max(w, h);
                    } else {
                        // has width but no height; match width
                        h = w;
                    }
                } else if (h > 0) {
                    // has height but no width
                    w = h;
                }
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
            case EQUIRECTANGULAR_SIMPLE:
            default:
                projection = new WorldMapEquirectangular();
                w = getWidth();
                h = (int)(w * ((double)options.map.getIntrinsicHeight() / (double)options.map.getIntrinsicWidth()));
                break;
        }

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


    @Override
    public void setImageBitmap(Bitmap b)
    {
        this.bitmap = b;
        super.setImageBitmap(b);
    }

    private Bitmap bitmap;
    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public void shareBitmap()
    {
        if (bitmap != null)
        {
            WorldMapExportTask exportTask = new WorldMapExportTask(getContext(), "SuntimesWorldMap", true, true);
            exportTask.setTaskListener(new ExportTask.TaskListener()
            {
                @Override
                public void onStarted()
                {
                    showProgress();
                }

                @Override
                public void onFinished(ExportTask.ExportResult result)
                {
                    dismissProgress();

                    Context context = getContext();
                    if (context != null)
                    {
                        if (result.getResult())
                        {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType(result.getMimeType());
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            try {
                                Uri shareURI = FileProvider.getUriForFile(context, "com.forrestguice.suntimeswidget.fileprovider", result.getExportFile());
                                shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);

                                String successMessage = context.getString(R.string.msg_export_success, result.getExportFile().getAbsolutePath());
                                Toast.makeText(context.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                                context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.msg_export_to)));
                                return;   // successful export ends here...

                            } catch (Exception e) {
                                Log.e(LOGTAG, "Failed to share file URI! " + e);
                            }

                        } else {
                            File file = result.getExportFile();
                            String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.msg_export_failure, path), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
            exportTask.setBitmaps(new Bitmap[] { bitmap });
            exportTask.execute();

        } else Log.w(LOGTAG, "shareBitmap: null!");
    }

    private ProgressDialog progressDialog;
    private void showProgress()
    {
        dismissProgress();
        Context context = getContext();
        if (context != null)
        {
            progressDialog = new ProgressDialog(context);
            progressDialog.show();
        }
    }
    private void dismissProgress()
    {
        if (progressDialog != null)
        {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        dismissProgress();
    }

}
