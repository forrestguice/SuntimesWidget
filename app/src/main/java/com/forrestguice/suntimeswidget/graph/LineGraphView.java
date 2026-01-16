/**
    Copyright (C) 2022 Forrest Guice
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.forrestguice.util.Log;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.ImageView;

import java.util.Calendar;

public class LineGraphView extends ImageView
{
    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LineGraphTask drawTask = null;

    private final int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LineGraphOptions options;
    @Nullable
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LineGraphView(Context context)
    {
        super(context);
        init(context);
    }

    public LineGraphView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        options = new LineGraphOptions(context);
        if (isInEditMode())
        {
            setBackgroundColor(options.getColor(LineGraphColorValues.COLOR_GRAPH_BG));
        }
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
        //Log.d(LineGraphView.class.getSimpleName(), "onResume");
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
            Log.d(LineGraphView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LineGraphOptions getOptions() {
        return options;
    }

    /**
     * themeViews
     */
    @Deprecated
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (options == null) {
            options = new LineGraphOptions(context);
        }
        options.colors.setColor(LineGraphColorValues.COLOR_GRAPH_BG, theme.getNightColor());
        options.colors.setColor(LineGraphColorValues.COLOR_POINT_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(LineGraphColorValues.COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        options.colors.setColor(LineGraphColorValues.COLOR_SUN_FILL, theme.getGraphPointFillColor());
        options.colors.setColor(LineGraphColorValues.COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
    }

    public void setData(@Nullable SuntimesRiseSetDataset data) {
        this.data = data;
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
            lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * @param data an instance of SuntimesRiseSetDataset
     */
    public void updateViews(@Nullable SuntimesRiseSetDataset data)
    {
        setData(data);

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            Log.w(LineGraphView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(LineGraphView.this.hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } // else Log.d(LineGraphView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            //Log.d(LineGraphView.class.getSimpleName(), "updateViews: width or height 0; skipping update..");
            return;
        }

        drawTask = new LineGraphTask(getContext());
        drawTask.setListener(drawTaskListener);

        if (Build.VERSION.SDK_INT >= 11) {
            drawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetMinutes);
        } else {
            drawTask.execute(data, getWidth(), getHeight(), options, (animated ? 0 : 1), options.offsetMinutes);
        }
    }

    private final LineGraphTaskListener drawTaskListener = new LineGraphTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onStarted: " + Integer.toHexString(LineGraphView.this.hashCode()));
            if (graphListener != null) {
                graphListener.onStarted();
            }
        }

        @Override
        public void onDataModified(SuntimesRiseSetDataset data) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onDataModified: " + Integer.toHexString(LineGraphView.this.hashCode()));
            LineGraphView.this.data = data;
            if (graphListener != null) {
                graphListener.onDataModified(data);
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onFrame: " + Integer.toHexString(LineGraphView.this.hashCode()));
            setImageBitmap(frame);
            if (graphListener != null) {
                graphListener.onFrame(frame, offsetMinutes);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: afterFrame: " + Integer.toHexString(LineGraphView.this.hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LineGraphView.class.getSimpleName(), "LineGraphView.updateViews: onFinished: " + Integer.toHexString(LineGraphView.this.hashCode()));
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
        //Log.d("DEBUG", "LineGraphView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        options.offsetMinutes = bundle.getLong("offsetMinutes", options.offsetMinutes);
        options.now = bundle.getLong("now", options.now);
    }

    public boolean saveSettings(Bundle bundle)
    {
        //Log.d(LineGraphView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetMinutes", options.offsetMinutes);
        bundle.putLong("now", options.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LineGraphView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LineGraphView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void resetAnimation( final boolean updateTime )
    {
        //Log.d(LineGraphView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        options.offsetMinutes = 0;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                if (updateTime)
                {
                    options.now = -1;
                    if (data != null) {
                        Calendar calendar = Calendar.getInstance(data.timezone());
                        data.setTodayIs(calendar);
                        data.calculateData(getContext());
                    }
                }
                updateViews(true);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (data != null) {
            //Log.d(LineGraphView.class.getSimpleName(), "onAttachedToWindow: update views " + data);
            updateViews(data);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d(LineGraphView.class.getSimpleName(), "onDetachedFromWindow: cancel task " + Integer.toHexString(LineGraphView.this.hashCode()));
            drawTask.cancel(true);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        //Log.d("DEBUG", "onVisibilityChanged: " + visibility);
        if (visibility != View.VISIBLE && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)    // TODO: only called for api 24+ ?
    {
        super.onVisibilityAggregated(isVisible);
        //Log.d("DEBUG", "onVisibilityAggregated: " + isVisible);
        if (!isVisible && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void seekDateTime( Context context, long datetime )
    {
        long offsetMillis = datetime - options.now;
        options.offsetMinutes = (offsetMillis / 1000 / 60);
        updateViews(true);
    }

    public void setOffsetMinutes( long value ) {
        options.offsetMinutes = value;
        updateViews(true);
    }
    public long getOffsetMinutes() {
        return options.offsetMinutes;
    }
    public long getNow() {
        return options.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    private LineGraphTaskListener graphListener = null;
    public void setMapTaskListener( LineGraphTaskListener listener ) {
        graphListener = listener;
    }

}
