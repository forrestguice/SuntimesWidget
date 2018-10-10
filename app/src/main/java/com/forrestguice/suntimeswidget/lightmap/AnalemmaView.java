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
package com.forrestguice.suntimeswidget.lightmap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * AnalemmaView
 */
public class AnalemmaView extends android.support.v7.widget.AppCompatImageView
{
    //private static final double MINUTES_IN_DAY = 24 * 60;

    public static final int DEFAULT_MAX_UPDATE_RATE = 15 * 1000;  // ms value; once every 15s
    public static final int DEFAULT_POINT_RADIUS = 6;
    public static final int DEFAULT_STROKE_WIDTH = 2;
    public static final int DEFAULT_LINE_WIDTH = 2;
    public static final int DEFAULT_AXIS_WIDTH = 2;
    public static final int DEFAULT_TICK_WIDTH = 1;
    public static final int DEFAULT_TICK_LENGTH = 4;

    private AnalemmaTask drawTask;

    private int maxUpdateRate = DEFAULT_MAX_UPDATE_RATE;

    private SuntimesRiseSetDataset data = null;

    private long lastUpdate = 0;
    private boolean resizable = true;

    public AnalemmaView(Context context)
    {
        super(context);
        init(context);
    }

    public AnalemmaView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        init(context);
    }

    /**
     * @param context a context used to access resources
     */
    private void init(Context context)
    {
        options = new AnalemmaOptions(context);
        if (isInEditMode())
        {
            setBackgroundColor(options.colorBackground);
        }
    }

    private AnalemmaDataPoints dataPoints;
    public AnalemmaDataPoints getDataPoints()
    {
        return dataPoints;
    }

    private AnalemmaOptions options;
    public AnalemmaOptions getOptions()
    {
        return options;
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
        Log.d("DEBUG", "AnalemmaView onResume");
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
            lastUpdate = System.currentTimeMillis();
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
        drawTask = new AnalemmaTask();
        drawTask.setListener(new AnalemmaTaskListener()
        {
            @Override
            public void onFinished(Bitmap result, AnalemmaDataPoints dataPoints)
            {
                AnalemmaView.this.dataPoints = dataPoints;
                setImageBitmap(result);

                if (listener != null) {
                    listener.onFinished(result, dataPoints);
                }
            }
        });
        drawTask.execute(data, getWidth(), getHeight(), options);
    }

    /**
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        //Log.d("DEBUG", "AnalemmaView loadSettings (prefs)");
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
    /**protected void loadSettings(Context context, Bundle bundle )
    {
        //Log.d("DEBUG", "AnalemmaView loadSettings (bundle)");
    }*/


    /**
     * @param context a context used to access shared prefs
     * @return true settings were saved
     */
    /**protected boolean saveSettings(Context context)
    {
        //Log.d("DEBUG", "LightMap loadSettings (prefs)");
        return true;
    }*/

    /**
     * @param bundle a Bundle used to save state
     * @return true settings were saved
     */
    /**protected boolean saveSettings(Bundle bundle)
    {
        //Log.d("DEBUG", "AnalemmaView saveSettings (bundle)");
        return true;
    }*/

    /**
     * AnalemmaTask
     */
    public static class AnalemmaTask extends AsyncTask<Object, Void, Bitmap>
    {
        private AnalemmaOptions options;
        private int pointRadius = DEFAULT_POINT_RADIUS;
        private int pointStrokeWidth = DEFAULT_STROKE_WIDTH;
        private int lineWidth = DEFAULT_LINE_WIDTH;
        private int axisWidth = DEFAULT_AXIS_WIDTH;
        private int tickWidth = DEFAULT_TICK_WIDTH;
        private int tickLength = DEFAULT_TICK_LENGTH;

        private AnalemmaDataPoints dataPoints;

        /**
         * @param params 0: SuntimesRiseSetDataset,
         *               1: Integer (width),
         *               2: Integer (height)
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
                options = (AnalemmaOptions)params[3];

            } catch (ClassCastException e) {
                Log.w("AnalemmaTask", "Invalid params; using [null, 0, 0]");
                return null;
            }
            return makeBitmap(data, w, h, options);
        }

        public Bitmap makeBitmap(SuntimesRiseSetDataset data, int w, int h, AnalemmaOptions options )
        {
            long bench_start = System.nanoTime();
            if (w <= 0 || h <= 0) {
                return null;
            }

            if (options == null) {
                return null;
            }
            this.options = options;

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            p.setColor(options.colorBackground);         // background
            c.drawRect(0, 0, w, h, p);

            int[] mid = new int[2];                     // canvas center
            mid[0] = w / 2;
            mid[1] = h / 2;

            double[] canvasBounds = new double[2];      // bounds from center (0,0)
            canvasBounds[0] = (mid[0] - options.padding[0]);
            canvasBounds[1] = (mid[1] - options.padding[1]);

            if (data != null)
            {
                SuntimesCalculator calculator = data.calculator();
                Calendar date = SuntimesRiseSetDataset.midnight(data.calendar());
                date.set(Calendar.HOUR_OF_DAY, options.date_hour);
                date.set(Calendar.MINUTE, options.date_minute);

                dataPoints = new AnalemmaDataPoints(calculator, date, options.mode);
                ArrayList<double[]> d = dataPoints.getData();

                double[] dataBounds = new double[2];           // bounds from center (0,0)
                dataBounds[0] = Math.max( Math.abs(dataPoints.getMinX()), Math.abs(dataPoints.getMaxX()) );
                dataBounds[1] = Math.max( Math.abs(dataPoints.getMinY()), Math.abs(dataPoints.getMaxY()) );

                // draw axis (and ticks)
                if (options.showAxis)
                {
                    int[] axis = getAxis(w, h, mid);

                    p.setColor(options.colorAxis_y);             // y-axis
                    p.setStrokeWidth(axisWidth);
                    c.drawLine(axis[1], 0, axis[1], h, p);

                    p.setColor(options.colorAxis_x);             // x-axis
                    p.setStrokeWidth(axisWidth);
                    c.drawLine(0, axis[0], w, axis[0], p);

                    if (options.showTicks)
                    {
                        p.setStrokeWidth(tickWidth);

                        int numTicks = (int)(dataBounds[1] / options.tickScale_y) + 1;
                        for (int i=1; i<=numTicks; i++)         // x-axis
                        {
                            int pa[] = projectToCanvas(new double[] {i * options.tickScale_x, 0}, w, h, mid, dataBounds, canvasBounds);
                            int pb[] = projectToCanvas(new double[] {-i * options.tickScale_x, 0}, w, h, mid, dataBounds, canvasBounds);
                            p.setColor(options.colorAxis_x);
                            c.drawLine(pa[0], pa[1] + tickLength, pa[0], pa[1] - tickLength, p);
                            c.drawLine(pb[0], pb[1] + tickLength, pb[0], pb[1] - tickLength, p);
                        }

                        numTicks = (int)(dataBounds[1] / options.tickScale_y) + 1;
                        for (int i=1; i<=numTicks; i++)   // y axis
                        {
                            int pa[] = projectToCanvas(new double[] {0, i * options.tickScale_y}, w, h, mid, dataBounds, canvasBounds);
                            int pb[] = projectToCanvas(new double[] {0, -i * options.tickScale_y}, w, h, mid, dataBounds, canvasBounds);
                            p.setColor(options.colorAxis_y);
                            c.drawLine(pa[0] + tickLength, pa[1], pa[0] - tickLength, pa[1], p);
                            c.drawLine(pb[0] + tickLength, pb[1], pb[0] - tickLength, pb[1], p);
                        }
                    }
                }

                // draw analemma
                p.setColor(options.colorLine);
                p.setStrokeWidth(lineWidth);
                for (int i=0; i<d.size(); i++)
                {
                    double[] d0 = d.get(i);
                    double[] d1 = (i == d.size()-1 ? d.get(0) : d.get(i+1));
                    int[] p0 = projectToCanvas(d0, w, h, mid, dataBounds, canvasBounds);
                    int[] p1 = projectToCanvas(d1, w, h, mid, dataBounds, canvasBounds);
                    c.drawLine(p0[0], p0[1], p1[0], p1[1], p);
                }

                // draw sun position
                if (options.showSun)
                {
                    double[] sunData = dataPoints.getData().get(dataPoints.getDayOfYear());
                    int[] sun = projectToCanvas(sunData, w, h, mid, dataBounds, canvasBounds);

                    p.setStyle(Paint.Style.FILL);
                    p.setColor(options.sunPointFill);
                    c.drawCircle(sun[0], sun[1], options.sunPointWidth, p);

                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(options.sunStrokeWidth);
                    p.setColor(options.sunPointStroke);
                    c.drawCircle(sun[0], sun[1], options.sunPointWidth, p);
                }
            }

            long bench_end = System.nanoTime();
            Log.d("Analemma", "makeBitmap :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
            return b;
        }

        /**
         * projectToCanvas
         * @param d [x,y] dataPoint to be projected
         * @param w canvas width
         * @param h canvas height
         * @param mid canvas midpoint
         * @param dataBounds data [xMax,yMax]
         * @param canvasBounds canvas [xMax, yMax]
         * @return projected [canvasX, canvasY]
         */
        private int[] projectToCanvas(double[] d, int w, int h, int[] mid, double[] dataBounds, double[] canvasBounds)
        {
            switch (options.mode)
            {
                case ALT_AZ: return projectToCanvas_fromBottom(d, w, h, mid, dataBounds, canvasBounds);
                case ALT_EOT: return projectToCanvas_fromBottom(d, w, h, mid, dataBounds, canvasBounds);
                case DEC_EOT:
                default: return projectToCanvas_fromCenter(d, mid, dataBounds, canvasBounds);
            }
        }
        private int[] projectToCanvas_fromBottom(double[] d, int w, int h, int[] mid, double[] dataBounds, double[] canvasBounds)
        {
            int[] p = new int[2];
            p[0] = (int)(mid[0] + ((d[0] / dataBounds[0]) * canvasBounds[0]));
            p[1] = (int)(h - options.padding[1] - ((d[1] / dataBounds[1]) * canvasBounds[0]*2));
            return p;
        }
        private int[] projectToCanvas_fromCenter(double[] d, int[] mid, double[] dataBounds, double[] canvasBounds)
        {
            int[] p = new int[2];
            p[0] = (int)(mid[0] + ((d[0] / dataBounds[0]) * canvasBounds[0]));
            p[1] = (int)(mid[1] - ((d[1] / dataBounds[1]) * canvasBounds[1]));
            return p;
        }

        private int[] getAxis(int w, int h, int[] mid)
        {
            int[] axis;
            switch (options.mode)
            {
                case ALT_AZ:
                case ALT_EOT:
                    axis = new int[] {h - options.padding[1], mid[0]};
                    break;
                case DEC_EOT:
                default:
                    axis = new int[] {mid[1], mid[0]};
                    break;
            }
            return axis;
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

        protected void onFinished( Bitmap result )
        {
            if (listener != null)
            {
                listener.onFinished(result, dataPoints);
            }
        }

        private AnalemmaTaskListener listener = null;
        public void setListener( AnalemmaTaskListener listener )
        {
            this.listener = listener;
        }
        public void clearListener()
        {
            this.listener = null;
        }
    }

    /**
     * AnalemmaTaskListener
     */
    @SuppressWarnings("EmptyMethod")
    public static abstract class AnalemmaTaskListener
    {
        public void onFinished( Bitmap result, AnalemmaDataPoints dataPoints ) {}
    }

    private AnalemmaTaskListener listener = null;
    public void setAnalemmaListener( AnalemmaTaskListener listener )
    {
        this.listener = listener;
    }
    public void clearAnalemmaListener()
    {
        this.listener = null;
    }

    /**
     * AnalemmaOptions
     */
    @SuppressWarnings("WeakerAccess")
    public static class AnalemmaOptions
    {
        public LightMapWidgetSettings.AnalemmaWidgetMode mode = LightMapWidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA;
        public int[] padding = new int[] {8, 8};
        public boolean showAxis = true;
        public boolean showTicks = true;
        public boolean showSun = true;
        public int tickScale_x = 5, tickScale_y = 5;
        public int colorAxis_x, colorAxis_y;
        public int colorBackground, colorLine;
        public int sunPointFill, sunPointStroke;
        public int sunPointWidth = 6, sunStrokeWidth = 2;
        public int date_hour = 12, date_minute = 0;

        public AnalemmaOptions() {}

        @SuppressWarnings("ResourceType")
        public AnalemmaOptions(Context context)
        {
            int[] colorAttrs = { R.attr.graphColor_day,     // 0
                    R.attr.graphColor_civil,                // 1
                    R.attr.graphColor_nautical,             // 2
                    R.attr.graphColor_astronomical,         // 3
                    R.attr.graphColor_night,                // 4
                    R.attr.graphColor_pointFill,            // 5
                    R.attr.graphColor_pointStroke };        // 6
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;

            colorLine = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            colorAxis_x = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            colorAxis_y = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
            colorBackground = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
            sunPointFill = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
            sunPointStroke = ContextCompat.getColor(context, typedArray.getResourceId(6, def));

            typedArray.recycle();
        }

        public void initDefaultDark(Context context)
        {
            colorLine = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            colorAxis_x = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            colorAxis_y = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            colorBackground = ContextCompat.getColor(context, R.color.graphColor_night_dark);
            sunPointFill = ContextCompat.getColor(context, R.color.sunIcon_color_setting_dark);
            sunPointStroke = ContextCompat.getColor(context, R.color.grey_800);
        }

        public void initDefaultLight(Context context)
        {
            colorLine = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            colorAxis_x = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            colorAxis_y = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            colorBackground = ContextCompat.getColor(context, R.color.graphColor_night_light);
            sunPointFill = ContextCompat.getColor(context, R.color.sunIcon_color_setting_light);
            sunPointStroke = ContextCompat.getColor(context, R.color.grey_800);
        }
    }

    /**
     * AnalemmaDataPoints
     */
    public static class AnalemmaDataPoints
    {
        public AnalemmaDataPoints(@NonNull SuntimesCalculator calculator, @NonNull Calendar date, @NonNull LightMapWidgetSettings.AnalemmaWidgetMode mode)
        {
            this.date = date;
            this.mode = mode;
            generateDataPoints(this, calculator, date, mode);
        }

        private LightMapWidgetSettings.AnalemmaWidgetMode mode;
        public LightMapWidgetSettings.AnalemmaWidgetMode getMode()
        {
             return mode;
        }

        private Calendar date;
        public Calendar getDate()
        {
            return date;
        }

        protected ArrayList<double[]> data = new ArrayList<>();
        public ArrayList<double[]> getData()
        {
            return data;
        }

        protected int dayOfYear = 1;
        public int getDayOfYear()
        {
            return dayOfYear;
        }

        protected double maxX = 0;
        public double getMaxX()
        {
            return maxX;
        }

        protected double minX = 0;
        private double getMinX()
        {
            return minX;
        }

        protected double maxY = 0;
        public double getMaxY()
        {
            return maxY;
        }

        protected double minY = 0;
        public double getMinY()
        {
            return minY;
        }

        public static void generateDataPoints(@NonNull AnalemmaDataPoints dataPoints, @NonNull SuntimesCalculator calculator, @NonNull Calendar date0, @NonNull LightMapWidgetSettings.AnalemmaWidgetMode mode)
        {
            Calendar date = Calendar.getInstance();
            date.setTimeZone(date0.getTimeZone());
            date.setTimeInMillis(date0.getTimeInMillis());
            dataPoints.dayOfYear = date0.get(Calendar.DAY_OF_YEAR);

            for (int day=1; day <= 365; day++)
            {
                date.set(Calendar.DAY_OF_YEAR, day);
                SuntimesCalculator.SunPosition sunPos = calculator.getSunPosition(date);

                double[] point = new double[2];
                switch (mode)
                {
                    case ALT_AZ:
                        point[0] = sunPos.azimuth;                                                  // azimuth degrees
                        point[1] = sunPos.elevation;                                                // altitude degrees
                        break;

                    case ALT_EOT:
                        point[0] = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(day);     // eot minutes
                        point[1] = sunPos.elevation;                                                // altitude degrees
                        break;

                    case DEC_EOT:
                    default:
                        point[0] = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(day);     // eot minutes
                        point[1] = sunPos.declination;                                              // declination degrees
                        break;
                }
                dataPoints.data.add(point);

                if (point[0] < dataPoints.minX) {
                    dataPoints.minX = point[0];
                }
                if (point[1] < dataPoints.minY) {
                    dataPoints.minY = point[1];
                }
                if (point[0] > dataPoints.maxX) {
                    dataPoints.maxX = point[0];
                }
                if (point[1] > dataPoints.maxY) {
                    dataPoints.maxY = point[1];
                }
            }
        }
    }


}
