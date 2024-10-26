/**
    Copyright (C) 2018-2022 Forrest Guice
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
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.views.ShareUtils;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class WorldMapView extends android.support.v7.widget.AppCompatImageView
{
    public static final String LOGTAG = "WorldMap";
    public static final int DEFAULT_MAX_UPDATE_RATE = 1000;  // ms value; once a second

    private WorldMapTask drawTask;
    private WorldMapTask.WorldMapOptions options;
    private WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE;

    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;
    private int mapW = 0, mapH = 0;
    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;
    private boolean animated = false;

    public WorldMapView(Context context)
    {
        super(context);
        init(context);
    }

    public WorldMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        applyAttributes(context, attribs);
        init(context);
    }

    private boolean matchHeight = false;
    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WorldMapView, 0, 0);
        try {
            matchHeight = a.getBoolean(R.styleable.WorldMapView_matchHeight, false);
        } finally {
            a.recycle();
        }
    }

    /**
     * @param context a context used to access resources
     */
    @SuppressLint("ResourceType")
    private void init(Context context)
    {
        options = new WorldMapTask.WorldMapOptions(context);
        if (isInEditMode())
        {
            setBackgroundColor(Color.WHITE);
            setImageBitmap(Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888));
        }
        themeViews(context);
        setMapMode(context, mode);
    }

    public WorldMapWidgetSettings.WorldMapWidgetMode getMapMode()
    {
        return mode;
    }

    @SuppressLint("ResourceType")
    public void setMapMode(Context context, WorldMapWidgetSettings.WorldMapWidgetMode mode)
    {
        Drawable background = loadBackgroundDrawable(context, mode.getMapTag(), options.center);
        this.mode = mode;
        switch (mode)
        {
            case MERCATOR_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap2);    // TODO: mercator drawable
                options.map_night = null;
                options.foregroundColor = (options.tintForeground ? foregroundColor : Color.TRANSPARENT);
                options.hasTransparentBaseMap = true;
                break;

            case EQUIAZIMUTHAL_SIMPLE:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap2);
                options.map_night = null;
                options.foregroundColor = (options.tintForeground ? foregroundColor : Color.TRANSPARENT);
                options.hasTransparentBaseMap = true;
                break;

            case EQUIAZIMUTHAL_SIMPLE1:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap3);
                options.map_night = null;
                options.foregroundColor = (options.tintForeground ? foregroundColor : Color.TRANSPARENT);
                options.hasTransparentBaseMap = true;
                break;

            case EQUIAZIMUTHAL_SIMPLE2:
                options.map = background;
                options.map_night = null;
                options.foregroundColor = (options.tintForeground ? foregroundColor : Color.TRANSPARENT);
                options.hasTransparentBaseMap = true;
                break;

            case EQUIRECTANGULAR_BLUEMARBLE:
                options.map = ContextCompat.getDrawable(context, R.drawable.world_topo_bathy_1024x512);   // land_shallow_topo_1024);
                options.map_night = ContextCompat.getDrawable(context, R.drawable.earth_lights_lrg_1024);
                options.foregroundColor = Color.TRANSPARENT;
                options.hasTransparentBaseMap = false;
                break;

            case EQUIRECTANGULAR_SIMPLE:
            default:
                options.map = (background != null) ? background : ContextCompat.getDrawable(context, R.drawable.worldmap);
                options.map_night = null;
                options.foregroundColor = (options.tintForeground ? foregroundColor : Color.TRANSPARENT);
                options.hasTransparentBaseMap = true;
                break;
        }
    }

    @Nullable
    public static Drawable loadBackgroundDrawable(Context context, String mapTag, double[] center)
    {
        String backgroundString = WorldMapWidgetSettings.loadWorldMapBackground(context, 0, mapTag, center);
        Drawable drawable = loadDrawableFromUri(context, backgroundString);
        if (drawable != null)
        {
            int w = 1024;
            int h = mapTag.startsWith(WorldMapWidgetSettings.MAPTAG_3x3) ? 1024 : 512;
            return new BitmapDrawable(Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(), w, h, true));
        }
        return null;
    }

    @Nullable
    public static Drawable loadDrawableFromUri(Context context, @Nullable String uriString)
    {
        Uri backgroundUri = (uriString != null) ? Uri.parse(uriString) : null;
        if (backgroundUri != null)
        {
            InputStream in = null;
            Drawable drawable;
            try {
                in = context.getContentResolver().openInputStream(backgroundUri);
                drawable = Drawable.createFromStream(in, backgroundUri.toString());

            } catch (FileNotFoundException | SecurityException | OutOfMemoryError e) {
                Log.e(LOGTAG, "Failed to open map background: " + e);
                drawable = null;

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) { /* EMPTY */ }
            }
            return drawable;

        } else {
            return null;
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
        //Log.d("DEBUG", "WorldMapView onResume");
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
    public void themeViews(Context context)
    {
        foregroundColor = options.colors.getColor(WorldMapColorValues.COLOR_FOREGROUND);
        options.latitudeColors[0] = options.colors.getColor(WorldMapColorValues.COLOR_AXIS);
        options.latitudeColors[1] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);
        options.latitudeColors[2] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);
    }

    private int foregroundColor;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        options.colors.setColor(WorldMapColorValues.COLOR_FOREGROUND, theme.getMapForegroundColor());
        foregroundColor = theme.getMapForegroundColor();
        options.colors.setColor(WorldMapColorValues.COLOR_BACKGROUND, theme.getMapBackgroundColor());
        options.colors.setColor(WorldMapColorValues.COLOR_SUN_SHADOW, theme.getMapShadowColor());
        options.colors.setColor(WorldMapColorValues.COLOR_MOON_LIGHT, theme.getMapHighlightColor());
        int sunShadowColor = options.colors.getColor(WorldMapColorValues.COLOR_SUN_SHADOW);
        int moonLightColor = options.colors.getColor(WorldMapColorValues.COLOR_MOON_LIGHT);
        options.colors.setColor(WorldMapColorValues.COLOR_AXIS, sunShadowColor);
        options.colors.setColor(WorldMapColorValues.COLOR_GRID_MAJOR, moonLightColor);
        options.colors.setColor(WorldMapColorValues.COLOR_GRID_MINOR, moonLightColor);
        options.latitudeColors[0] = options.colors.getColor(WorldMapColorValues.COLOR_AXIS);
        options.latitudeColors[1] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);
        options.latitudeColors[2] = options.colors.getColor(WorldMapColorValues.COLOR_GRID_MAJOR);
        options.colors.setColor(WorldMapColorValues.COLOR_POINT_FILL, theme.getActionColor());
        options.colors.setColor(WorldMapColorValues.COLOR_SUN_FILL, theme.getNoonIconColor());
        options.colors.setColor(WorldMapColorValues.COLOR_SUN_STROKE, theme.getNoonIconStrokeColor());
        options.colors.setColor(WorldMapColorValues.COLOR_MOON_FILL, theme.getMoonFullColor());
        options.colors.setColor(WorldMapColorValues.COLOR_MOON_STROKE, theme.getMoonWaningColor());
        setMapMode(context, mode);    // options.foregroundColor is assigned with the mapMode
    }

    /**
     * throttled update method
     */
    public void updateViews( boolean forceUpdate )
    {
        long timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);
        if (forceUpdate || timeSinceLastUpdate >= maxUpdateRate)
        {
            updateViews(data, forceUpdate);
        }
    }

    /**
     * @param context
     * @return available screen height int pixels
     */
    private int getScreenHeight(Context context)
    {
        if (Build.VERSION.SDK_INT >= 13)
        {
            Configuration config = context.getResources().getConfiguration();
            return SuntimesUtils.dpToPixels(context, config.screenHeightDp - 32);

        } else {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null)
            {
                Display display = windowManager.getDefaultDisplay();
                return display.getHeight() - SuntimesUtils.dpToPixels(context, 32);

            } else {
                return 0;
            }
        }
    }

    /**
     * @param data an instance of SuntimesRiseSetDataset
     */
    public void updateViews(SuntimesRiseSetDataset data, boolean forceUpdate)
    {
        boolean sameData = (this.data == data);
        this.data = data;

        boolean wasCancelled = false;
        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            Log.w(LOGTAG, "updateViews: task already running");
            drawTask.cancel(true);
            wasCancelled = true;
        }

        int w = getWidth();
        int h = getHeight();
        WorldMapTask.WorldMapProjection projection;
        switch (mode)
        {
            case MERCATOR_SIMPLE:
            case EQUIAZIMUTHAL_SIMPLE:
            case EQUIAZIMUTHAL_SIMPLE1:
            case EQUIAZIMUTHAL_SIMPLE2:
                //Log.d("DEBUG", "matchHeight: " + matchHeight);
                projection = getMapProjection(mode);
                if (w > 0)
                {
                    if (h > 0)
                    {
                        if (matchHeight) {
                            w = h = getScreenHeight(getContext());
                        } else {
                            w = h = Math.max(w, h);
                        }
                    } else {
                        if (matchHeight) {
                            w = h = getScreenHeight(getContext());
                        } else {
                            h = w;
                        }
                    }
                } else if (h > 0) {
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
            if (skipUpdate && !wasCancelled && !forceUpdate)
            {
                Log.w(LOGTAG, "updateViews: " + w + ", " + h + " (image is unchanged; skipping)");
                return;
            }

            drawTask = new WorldMapTask();
            drawTask.setListener(drawListener);

            Log.w(LOGTAG, "updateViews: " + w + ", " + h );
            drawTask.execute(data, w, h, options, projection, (animated ? 0 : 1), options.offsetMinutes);
            options.modified = false;
            lastUpdate = System.currentTimeMillis();
        }
    }

    public static WorldMapTask.WorldMapProjection getMapProjection(WorldMapWidgetSettings.WorldMapWidgetMode mode)
    {
        switch (mode) {
            case MERCATOR_SIMPLE: return new WorldMapMercator();
            case EQUIAZIMUTHAL_SIMPLE: return new WorldMapEquiazimuthal();
            case EQUIAZIMUTHAL_SIMPLE1: return new WorldMapEquiazimuthal1();
            case EQUIAZIMUTHAL_SIMPLE2: return new WorldMapEquiazimuthal2();
            case EQUIRECTANGULAR_BLUEMARBLE: case EQUIRECTANGULAR_SIMPLE:
            default: return new WorldMapEquirectangular();
        }
    }

    private WorldMapTask.WorldMapTaskListener drawListener = new WorldMapTask.WorldMapTaskListener()
    {
        @Override
        public void onStarted()
        {
            if (mapListener != null) {
                mapListener.onStarted();
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetMinutes)
        {
            mapW = frame.getWidth();
            mapH = frame.getHeight();
            setImageBitmap(frame);

            if (mapListener != null) {
                mapListener.onFrame(frame, offsetMinutes);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetMinutes)
        {
            if (isRecording()) {
                exportTask.addBitmap(frame);
            }
        }

        @Override
        public void onFinished(Bitmap frame)
        {
            mapW = frame.getWidth();
            mapH = frame.getHeight();
            setImageBitmap(frame);

            if (mapListener != null) {
                mapListener.onFinished(frame);
            }
            if (exportTask != null && !drawTask.isCancelled()) {
                exportTask.setWaitForFrames(false);
            }
        }
    };

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
    protected void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d("DEBUG", "WorldMapView loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetMinutes = bundle.getLong("offsetMinutes", options.offsetMinutes);
        options.now = bundle.getLong("now", options.now);
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
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetMinutes", options.offsetMinutes);
        bundle.putLong("now", options.now);
        return true;
    }

    private WorldMapTask.WorldMapTaskListener mapListener = null;
    public void setMapTaskListener( WorldMapTask.WorldMapTaskListener listener )
    {
        mapListener = listener;
    }

    @Override
    public void setImageBitmap(Bitmap b)
    {
        super.setImageBitmap(b);
        bitmap = b;
        //postInvalidate();
        //Log.d("WorldMapView", "setImageBitmap");
    }

    private Bitmap bitmap;
    private static WorldMapExportTask exportTask = null;

    public boolean isRecording() {
        return (exportTask != null && !exportTask.isCancelled() && exportTask.getStatus() != AsyncTask.Status.FINISHED);
    }

    public void shareBitmap()
    {
        if (bitmap != null)
        {
            exportTask = new WorldMapExportTask(getContext(), "SuntimesWorldMap", true, true);
            exportTask.setTaskListener(exportListener);
            exportTask.setBitmaps(new Bitmap[] { bitmap });
            exportTask.setWaitForFrames(animated);
            exportTask.setZippedOutput(animated);
            if (Build.VERSION.SDK_INT >= 11) {
                exportTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);   // executes in parallel to draw task
            } else exportTask.execute();

        } else Log.w(LOGTAG, "shareBitmap: null!");
    }

    private ExportTask.TaskListener exportListener = new ExportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            if (!animated) {
                showProgress();
            }
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
                    String successMessage = context.getString(R.string.msg_export_success, result.getExportFile().getAbsolutePath());
                    Toast.makeText(context.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();
                    ShareUtils.shareFile(context, ExportTask.FILE_PROVIDER_AUTHORITY, result.getExportFile(), result.getMimeType());
                    return;

                } else {
                    File file = result.getExportFile();
                    String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.msg_export_failure, path), Toast.LENGTH_LONG).show();
                }
            }
            exportTask = null;
        }
    };

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
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (exportTask != null && exportTask.isPaused()) {
            exportTask.setTaskListener(exportListener);
            exportTask.resumeTask();
            stopAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        stopRunningTasks();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        if (visibility != View.VISIBLE) {
            stopRunningTasks();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)    // TODO: only called for api 24+ ?
    {
        super.onVisibilityAggregated(isVisible);
        if (!isVisible) {
            stopRunningTasks();
        }
    }

    protected void stopRunningTasks()
    {
        dismissProgress();
        if (drawTask != null) {
            drawTask.cancel(true);
        }
        if (exportTask != null) {
            exportTask.pauseTask();
        }
    }

    public void startAnimation()
    {
        animated = true;
        updateViews(true);
    }

    public void stopAnimation()
    {
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
        if (exportTask != null)
        {
            exportTask.setWaitForFrames(false);
            if (isRecording()) {
                showProgress();
            }
        }
    }

    public boolean isAnimated()
    {
        return animated;
    }

    public void resetAnimation( boolean updateTime )
    {
        stopAnimation();
        options.offsetMinutes = 0;
        if (updateTime) {
            options.now = -1;
        }
        updateViews(true);
    }

    public void setOffsetMinutes( long offsetMinutes )
    {
        options.offsetMinutes = offsetMinutes;
        updateViews(true);
    }

    public long getOffsetMinutes() {
        return options.offsetMinutes;
    }
    public long getNow() {
        return options.now;
    }

}
