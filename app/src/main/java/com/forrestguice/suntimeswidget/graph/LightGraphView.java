/**
    Copyright (C) 2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import android.view.View;

import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.Log;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.ImageView;
import com.forrestguice.util.android.AndroidResources;

import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.forrestguice.suntimeswidget.graph.LightGraphDialog.MAPTAG_LIGHTGRAPH;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_SUN_STROKE;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_ASTRONOMICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_CIVIL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_DAY;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NAUTICAL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_NIGHT;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_FILL;
import static com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues.COLOR_POINT_STROKE;

public class LightGraphView extends ImageView
{
    public static final String PREF_KEY_GRAPH_SHOWPOINTS = "showPoints";
    public static final boolean DEF_KEY_GRAPH_SHOWPOINTS = true;

    public static final String PREF_KEY_GRAPH_SHOWTWILIGHT = "showTwilight";
    public static final boolean DEF_KEY_GRAPH_SHOWTWILIGHT = true;

    public static final String PREF_KEY_GRAPH_SHOWCIVIL = "showCivil";
    public static final boolean DEF_KEY_GRAPH_SHOWCIVIL = true;

    public static final String PREF_KEY_GRAPH_SHOWNAUTICAL = "showNautical";
    public static final boolean DEF_KEY_GRAPH_SHOWNAUTICAL = true;

    public static final String PREF_KEY_GRAPH_SHOWASTRO = "showAstro";
    public static final boolean DEF_KEY_GRAPH_SHOWASTRO = true;

    public static final String PREF_KEY_GRAPH_SHOWSEASONS = "showSeasons";
    public static final boolean DEF_KEY_GRAPH_SHOWSEASONS = true;

    public static final String PREF_KEY_GRAPH_SHOWCROSSHAIR = "showCrosshair";
    public static final boolean DEF_KEY_GRAPH_SHOWCROSSHAIR = true;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private final ExecutorService executor = new ThreadPoolExecutor(0, 5, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    protected Executor getExecutor() {
        return executor;
    }
    private LightGraphTask drawTask = null;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LightGraphOptions options;
    @Nullable
    private SuntimesRiseSetDataset data0 = null;
    @Nullable
    private SuntimesRiseSetDataset[] data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LightGraphView(Context context)
    {
        super(context);
        init(context);
    }

    public LightGraphView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        options = new LightGraphOptions(AndroidResources.wrap(context));
        if (isInEditMode()) {
            setBackgroundColor(options.colors.getColor(COLOR_DAY));
        }
    }

    public int getMaxUpdateRate() {
        return maxUpdateRate;
    }

    public void setResizable( boolean value ) {
        resizable = value;
    }

    /**
     * onResume
     */
    public void onResume() {
        //Log.d(LightGraphView.class.getSimpleName(), "onResume");
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
            Log.d(LightGraphView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    @Override
    public void setImageBitmap(Bitmap b)
    {
        super.setImageBitmap(b);
        bitmap = b;
    }
    private Bitmap bitmap;

    public LightGraphOptions getOptions() {
        return options;
    }

    /**
     * themeViews
     */
    @Deprecated
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (options == null) {
            options = new LightGraphOptions(AndroidResources.wrap(context));
        }

        options.colors.setColor(COLOR_NIGHT, theme.getNightColor());
        options.colors.setColor(COLOR_DAY, theme.getDayColor());
        options.colors.setColor(COLOR_ASTRONOMICAL, theme.getAstroColor());
        options.colors.setColor(COLOR_NAUTICAL, theme.getNauticalColor());
        options.colors.setColor(COLOR_CIVIL, theme.getCivilColor());
        options.colors.setColor(COLOR_POINT_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        options.colors.setColor(COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
    }

    public void setData(@Nullable SuntimesRiseSetDataset value)
    {
        this.data0 = value;
        this.data = null;

        if (graphListener != null) {
            graphListener.onProgress(true);
        }

        if (this.data0 == null) {
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Context context = getContext();
                Location location = ((data0 != null) ? data0.location() : null);
                double longitude = ((location != null) ? location.getLongitudeAsDouble() : 0);

                String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID);
                TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data0.timezone() : WidgetTimezones.getTimeZone(tzId, longitude, data0.calculator());

                data = LightGraphBitmap.createYearData(getContext(), data0, timezone);
                if (data != null) {
                    options.earliestLatestData = EarliestLatestSunriseSunsetData.findEarliestLatest(TimeMode.OFFICIAL, data);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        updateViews(true);
                        if (graphListener != null) {
                            graphListener.onProgress(false);
                        }
                    }
                });
            }
        });
    }

    @Nullable
    public SuntimesRiseSetDataset getData0() {
        return data0;
    }
    public SuntimesRiseSetDataset[] getData() {
        return this.data;
    }

    /**
     * throttled update method
     */
    public void updateViews( boolean forceUpdate )
    {
        long timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);
        if (forceUpdate || timeSinceLastUpdate >= maxUpdateRate)
        {
            updateViews();
            lastUpdate = System.currentTimeMillis();
        }
    }

    public void updateViews()
    {
        if (drawTask != null && drawTask.getStatus() == LightGraphTask.Status.RUNNING)
        {
            Log.w(LightGraphView.class.getSimpleName(), "updateViews: task already running: " + data0 + " (" + Integer.toHexString(LightGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel();
        } // else Log.d(LightGraphView.class.getSimpleName(), "updateViews: starting task " + data0);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LightGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        if (data == null) {
            return;
        }

        drawTask = new LightGraphTask(new Object[] { data0, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetDays } );
        drawTask.setData(data);
        drawTask.setListener(drawTaskListener);
        ExecutorUtils.runProgress("LightGraphTask", getExecutor(), drawTask, drawTaskListener);
    }

    private final LightGraphTaskListener drawTaskListener = new LightGraphTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onStarted: " + Integer.toHexString(LightGraphView.this.hashCode()));
            if (graphListener != null) {
                graphListener.onStarted();
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetDays) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onFrame: " + Integer.toHexString(LightGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFrame(frame, offsetDays);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetDays) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: afterFrame: " + Integer.toHexString(LightGraphView.this.hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LightGraphView.class.getSimpleName(), "LightGraphView.updateViews: onFinished: " + Integer.toHexString(LightGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFinished(frame);
            }
        }
    };

    /**
     * @param context a context used to access shared prefs
     */
    public void loadSettings(Context context)
    {
        //Log.d("DEBUG", "LightGraphView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LightGraphView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetDays = bundle.getLong("offsetDays", options.offsetDays);
        options.now = bundle.getLong("now", options.now);
    }

    public boolean saveSettings(Bundle bundle)
    {
        //Log.d(LightGraphView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetDays", options.offsetDays);
        bundle.putLong("now", options.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LightGraphView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LightGraphView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel();
        }
    }

    public void resetAnimation( boolean updateTime )
    {
        //Log.d(LightGraphView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        options.offsetDays = 0;
        if (updateTime)
        {
            options.now = -1;
            //if (data0 != null) {
                //Calendar calendar = Calendar.getInstance(data0.timezone());
                //data0.setTodayIs(calendar);
                //data0.calculateData();
            //}
        }
        updateViews(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data0 != null) {
            //Log.d(LightGraphView.class.getSimpleName(), "onAttachedToWindow: update views " + data);
            updateViews(true);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d(LightGraphView.class.getSimpleName(), "onDetachedFromWindow: cancel task " + Integer.toHexString(LightGraphView.this.hashCode()));
            drawTask.cancel();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        //Log.d("DEBUG", "onVisibilityChanged: " + visibility);
        if (visibility != View.VISIBLE && drawTask != null) {
            drawTask.cancel();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)    // TODO: only called for api 24+ ?
    {
        super.onVisibilityAggregated(isVisible);
        //Log.d("DEBUG", "onVisibilityAggregated: " + isVisible);
        if (!isVisible && drawTask != null) {
            drawTask.cancel();
        }
    }

    public void seekDateTime( Context context, long datetime )
    {
        long offsetMillis = datetime - options.now;
        options.offsetDays = (offsetMillis / 1000 / 60 / 6 / 24);
        updateViews(true);
    }

    public void setOffsetDays( long value ) {
        options.offsetDays = value;
        updateViews(true);
    }
    public long getOffsetDays() {
        return options.offsetDays;
    }
    public long getNow() {
        return options.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * shareBitmap
     */
    public void shareBitmap(ExportTask.TaskListener listener)
    {
        if (bitmap != null)
        {
            LightGraphExportTask exportTask = new LightGraphExportTask(getContext(), "SuntimesLightGraph", true, true);
            exportTask.setBitmaps(new Bitmap[] { bitmap });
            exportTask.setWaitForFrames(animated);
            exportTask.setZippedOutput(animated);
            ExecutorUtils.runProgress("ShareBitmapTask", getExecutor(), exportTask, listener);

        } else Log.w(LightGraphView.class.getSimpleName(), "shareBitmap: null!");
    }

    private LightGraphTaskListener graphListener = null;
    public void setTaskListener( LightGraphTaskListener listener ) {
        graphListener = listener;
    }

}
