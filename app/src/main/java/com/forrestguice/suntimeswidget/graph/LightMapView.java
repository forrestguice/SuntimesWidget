/**
    Copyright (C) 2014-2022 Forrest Guice
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
import android.os.Bundle;

import android.util.AttributeSet;

import android.view.View;

import com.forrestguice.util.Log;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.ImageView;

import java.util.Calendar;

/**
 * LightMapView .. a stacked bar graph over the duration of a day showing relative duration of
 * night, day, and twilight times.
 */
public class LightMapView extends ImageView
{
    protected static final double MINUTES_IN_DAY = 24 * 60;
    protected static final double MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    protected static final double ONE_DIVIDED_MILLIS_IN_DAY = 1d / MILLIS_IN_DAY;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s

    private LightMapTask drawTask = null;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private LightMapOptions colors;
    @Nullable
    private SuntimesRiseSetDataset data = null;
    private long lastUpdate = 0;
    private boolean resizable = true;

    private boolean animated = false;

    public LightMapView(Context context)
    {
        super(context);
        init(context);
    }

    public LightMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    public void setUseMainThread(boolean value) {
        useMainThread = value;
    }
    private boolean useMainThread = false;

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        colors = new LightMapOptions(context);
        if (isInEditMode())
        {
            setBackgroundColor(colors.values.getColor(LightMapColorValues.COLOR_NIGHT));
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
        //Log.d(LightMapView.class.getSimpleName(), "onResume");
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
            //Log.d(LightMapView.class.getSimpleName(), "onSizeChanged: " + oldw + "," + oldh + " -> " + w + "," + h);
            updateViews(true);
        }
    }

    public LightMapOptions getColors() {
        return colors;
    }

    /**
     * themeViews
     */
    @Deprecated
    public void themeViews( Context context, @NonNull SuntimesTheme theme )
    {
        if (colors == null) {
            colors = new LightMapOptions();
        }
        themeViews(context, theme, colors);
    }
    public static void themeViews( Context context, @NonNull SuntimesTheme theme, @NonNull LightMapOptions colors )
    {
        colors.values.setColor(LightMapColorValues.COLOR_NIGHT, theme.getNightColor());
        colors.values.setColor(LightMapColorValues.COLOR_DAY, theme.getDayColor());
        colors.values.setColor(LightMapColorValues.COLOR_ASTRONOMICAL, theme.getAstroColor());
        colors.values.setColor(LightMapColorValues.COLOR_NAUTICAL, theme.getNauticalColor());
        colors.values.setColor(LightMapColorValues.COLOR_CIVIL, theme.getCivilColor());
        colors.values.setColor(LightMapColorValues.COLOR_POINT_FILL, theme.getGraphPointFillColor());
        colors.values.setColor(LightMapColorValues.COLOR_POINT_STROKE, theme.getGraphPointStrokeColor());
        colors.values.setColor(LightMapColorValues.COLOR_SUN_FILL, theme.getGraphPointFillColor());
        colors.values.setColor(LightMapColorValues.COLOR_SUN_STROKE, theme.getGraphPointStrokeColor());
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
        //Log.d("DEBUG", "updateViews: " + data.dataActual.sunsetCalendarToday().get(Calendar.DAY_OF_YEAR));

        if (drawTask != null && drawTask.getStatus() == AsyncTask.Status.RUNNING)
        {
            //Log.d("DEBUG", "updateViews: canceling existing task..");
            //Log.w(LightMapView.class.getSimpleName(), "updateViews: task already running: " + data + " (" + Integer.toHexString(getColors().hashCode())  +  ") .. restarting task.");
            drawTask.cancel(true);
        } //else Log.d(LightMapView.class.getSimpleName(), "updateViews: starting task " + data);

        if (getWidth() == 0 || getHeight() == 0) {
            Log.w(LightMapView.class.getSimpleName(), "updateViews: width or height 0; skipping update.. :: view-" + Integer.toHexString(getColors().hashCode()));
            return;
        }

        if (useMainThread)
        {
            //Log.d("DEBUG", "updating lightmap on main thread.. " + getWidth() + "x" + getHeight() + " @ " + getNow() + " :: view-" + Integer.toHexString(getColors().hashCode()));
            LightMapTask draw = new LightMapTask(getContext());
            Bitmap b = draw.makeBitmap(data, getWidth(), getHeight(), colors);
            drawTaskListener.onFinished(b);

        } else {
            drawTask = new LightMapTask(getContext());
            drawTask.setListener(drawTaskListener);
            drawTask.execute(data, getWidth(), getHeight(), colors, (animated ? 0 : 1), colors.offsetMinutes);
        }
    }

    private final LightMapTaskListener drawTaskListener = new LightMapTaskListener() {
        @Override
        public void onStarted() {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onStarted: " + Integer.toHexString(getColors().hashCode()));
            if (mapListener != null) {
                mapListener.onStarted();
            }
        }

        @Override
        public void onDataModified(SuntimesRiseSetDataset data) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onDataModified: " + Integer.toHexString(getColors().hashCode()));
            LightMapView.this.data = data;
            if (mapListener != null) {
                mapListener.onDataModified(data);
            }
        }

        @Override
        public void onFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onFrame: " + Integer.toHexString(getColors().hashCode()));
            setImageBitmap(frame);
            if (mapListener != null) {
                mapListener.onFrame(frame, offsetMinutes);
            }
        }

        @Override
        public void afterFrame(Bitmap frame, long offsetMinutes) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: afterFrame: " + Integer.toHexString(getColors().hashCode()));
        }

        @Override
        public void onFinished(Bitmap frame) {
            //Log.d(LightMapView.class.getSimpleName(), "LightmapView.updateViews: onFinished: " + Integer.toHexString(getColors().hashCode()));
            setImageBitmap(frame);
            if (mapListener != null) {
                mapListener.onFinished(frame);
            }
        }
    };

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        //Log.d("DEBUG", "LightMapView loadSettings (prefs)");
        if (isInEditMode())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected void loadSettings(Context context, @NonNull Bundle bundle )
    {
        //Log.d(LightMapView.class.getSimpleName(), "loadSettings (bundle)");
        animated = bundle.getBoolean("animated", animated);
        colors.offsetMinutes = bundle.getLong("offsetMinutes", colors.offsetMinutes);
        colors.now = bundle.getLong("now", colors.now);
    }

    protected boolean saveSettings(Bundle bundle)
    {
        //Log.d(LightMapView.class.getSimpleName(), "saveSettings (bundle)");
        bundle.putBoolean("animated", animated);
        bundle.putLong("offsetMinutes", colors.offsetMinutes);
        bundle.putLong("now", colors.now);
        return true;
    }

    public void startAnimation() {
        //Log.d(LightMapView.class.getSimpleName(), "startAnimation");
        animated = true;
        updateViews(true);
    }

    public void stopAnimation() {
        //Log.d(LightMapView.class.getSimpleName(), "stopAnimation");
        animated = false;
        if (drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void resetAnimation( final boolean updateTime )
    {
        //Log.d(LightMapView.class.getSimpleName(), "resetAnimation");
        stopAnimation();
        colors.offsetMinutes = 0;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                if (updateTime)
                {
                    colors.now = -1;
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
            //Log.d("DEBUG", "onAttachedToWindow: update views :: view-" + Integer.toHexString(getColors().hashCode()));
            updateViews(data);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (drawTask != null) {
            //Log.d("DEBUG", "onDetachedFromWindow: cancel task :: view-" + Integer.toHexString(getColors().hashCode()));
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
    public void onVisibilityAggregated(boolean isVisible)
    {
        super.onVisibilityAggregated(isVisible);
        //Log.d("DEBUG", "onVisibilityAggregated: " + isVisible);
        if (!isVisible && drawTask != null) {
            drawTask.cancel(true);
        }
    }

    public void seekDateTime( Context context, @Nullable Calendar calendar )
    {
        if (calendar != null) {
            seekDateTime(context, calendar.getTimeInMillis());
        }
    }
    public void seekDateTime( Context context, long datetime )
    {
        long offsetMillis = datetime - colors.now;
        colors.offsetMinutes = (offsetMillis / 1000 / 60);
        updateViews(true);
    }
    public Long seekAltitude( Context context, @Nullable Integer degrees, boolean rising )
    {
        if (data != null && degrees != null)
        {
            Long event = findAltitude(context, degrees, rising);
            if (event != null) {
                seekDateTime(context, event);
                return event;
            }
        }
        return null;
    }
    public Long findAltitude( Context context, @Nullable Integer degrees, boolean rising )
    {
        if (data != null && degrees != null)
        {
            Calendar calendar = Calendar.getInstance(data.timezone());
            calendar.setTimeInMillis(colors.now + (colors.offsetMinutes * 60 * 1000));
            Calendar event = rising ? data.calculator().getSunriseCalendarForDate(calendar, degrees)
                    : data.calculator().getSunsetCalendarForDate(calendar, degrees);
            return ((event != null) ? event.getTimeInMillis() : null);
        } else return null;
    }

    public Long findShadow( Context context, @Nullable Double shadowLengthMeters, @Nullable Double objHeightMeters, boolean rising )
    {
        if (data != null && shadowLengthMeters != null && objHeightMeters != null)
        {
            Calendar calendar = Calendar.getInstance(data.timezone());
            calendar.setTimeInMillis(colors.now + (colors.offsetMinutes * 60 * 1000));
            Calendar event = rising ? data.calculator().getTimeOfShadowBeforeNoon(calendar, objHeightMeters, shadowLengthMeters)
                                    : data.calculator().getTimeOfShadowAfterNoon(calendar, objHeightMeters, shadowLengthMeters);
            return ((event != null) ? event.getTimeInMillis() : null);
        } else return null;
    }

    public void setOffsetMinutes( long value ) {
        colors.offsetMinutes = value;
        updateViews(true);
    }
    public long getOffsetMinutes() {
        return colors.offsetMinutes;
    }
    public long getNow() {
        return colors.now;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * LightMapTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class LightMapTaskListener
    {
        public void onStarted() {}
        public void onDataModified( SuntimesRiseSetDataset data ) {}
        public void onFrame(Bitmap frame, long offsetMinutes ) {}
        public void afterFrame(Bitmap frame, long offsetMinutes ) {}
        public void onFinished( Bitmap result ) {}
    }

    private LightMapTaskListener mapListener = null;
    public void setMapTaskListener( LightMapTaskListener listener ) {
        mapListener = listener;
    }

    public static CharSequence getLabel(Context context, SuntimesRiseSetDataset data)
    {
        if (data.dayLength() <= 0) {
            return context.getString(R.string.timeMode_polarnight);

        } else if (data.nightLength() == 0) {
            if (data.civilTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnightsun);

            } else if (data.nauticalTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnighttwilight_whitenight);

            } else if (data.astroTwilightLength()[1] <= 0) {
                return context.getString(R.string.timeMode_midnighttwilight);

            } else {
                return context.getString(R.string.timeMode_midnighttwilight);
            }
        }
        return null;
    }
}
