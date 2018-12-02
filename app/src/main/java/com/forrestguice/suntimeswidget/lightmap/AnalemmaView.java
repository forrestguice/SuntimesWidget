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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
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
            setBackgroundColor(options.backgroundColor);
        }
    }

    private AnalemmaData dataPoints;
    public AnalemmaData getDataPoints()
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
            public void onFinished(AnalemmaTaskResult result)
            {
                AnalemmaView.this.dataPoints = result.data;
                if (result.bitmap != null) {
                    setImageBitmap(result.bitmap);
                }

                if (listener != null) {
                    listener.onFinished(result);
                }
            }
        });
        drawTask.execute(data, getWidth(), getHeight(), options, dataPoints);
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
        //Log.d("DEBUG", "AnalemmaView loadSettings (prefs)");
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
    public static class AnalemmaTask extends AsyncTask<Object, Void, AnalemmaTaskResult>
    {
        private AnalemmaOptions options;

        /**
         * @param params 0: SuntimesRiseSetDataset,
         *               1: Integer (width),
         *               2: Integer (height)
         * @return a bitmap, or null params are invalid
         */
        @Override
        protected AnalemmaTaskResult doInBackground(Object... params)
        {
            int w, h;
            SuntimesRiseSetDataset dataset;
            AnalemmaData lastData = null;
            try {
                dataset = (SuntimesRiseSetDataset)params[0];
                w = (Integer)params[1];
                h = (Integer)params[2];
                options = (AnalemmaOptions)params[3];

                if (params.length > 4) {
                    lastData = (AnalemmaData)params[4];
                }

            } catch (ClassCastException e) {
                Log.w("AnalemmaTask", "Invalid params; using [null, 0, 0]");
                return null;
            }

            if (lastData == null)
                return makeBitmap(dataset, w, h, options);
            else return makeBitmap(lastData, w, h, options);
        }

        public AnalemmaTaskResult makeBitmap(SuntimesRiseSetDataset data, int w, int h, AnalemmaOptions options )
        {
            long bench_start = System.nanoTime();
            if (options == null) {
                return new AnalemmaTaskResult();
            }
            this.options = options;

            AnalemmaTaskResult result;
            if (data != null)
            {
                SuntimesCalculator calculator = data.calculator();
                Calendar date = SuntimesRiseSetDataset.midnight(data.calendar());
                date.set(Calendar.HOUR_OF_DAY, options.date_hour);
                date.set(Calendar.MINUTE, options.date_minute);
                result = makeBitmap(new AnalemmaData(calculator, date, options.mode), w, h, options);
            } else result = new AnalemmaTaskResult();

            long bench_end = System.nanoTime();
            Log.d("Analemma", "makeBitmap0 :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
            return result;
        }

        public AnalemmaTaskResult makeBitmap(AnalemmaData dataPoints, int w, int h, AnalemmaOptions options )
        {
            long bench_start = System.nanoTime();
            if (w <= 0 || h <= 0 || dataPoints == null || options == null) {
                return new AnalemmaTaskResult();
            }
            this.options = options;

            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            p.setColor(options.backgroundColor);         // background
            c.drawRect(0, 0, w, h, p);

            double[] canvasBounds = new double[2];
            canvasBounds[0] = w;
            canvasBounds[1] = h;


            ArrayList<AnalemmaPoint> d = dataPoints.getPoints();
            double[][] dataBounds = dataPoints.getBounds(options.mode);
            Matrix projection = initProjection(dataBounds, canvasBounds, options);

            // draw axis (and ticks)
            if (options.showAxis)
            {
                int[] axis = projectToCanvas(0, 0, projection);

                p.setColor(options.axisColor_y);             // y-axis
                p.setStrokeWidth(options.axisWidth);
                c.drawLine(axis[0], 0, axis[0], h, p);

                p.setColor(options.axisColor_x);             // x-axis
                p.setStrokeWidth(options.axisWidth);
                c.drawLine(0, axis[1], w, axis[1], p);

                if (options.showTicks)
                {
                    p.setStrokeWidth(options.tickWidth);
                    p.setColor(options.axisColor_x);
                    int numTicks = (int)(90 / options.tickScale_x) + 1;
                    for (int i=1; i<=numTicks; i++)         // x-axis
                    {
                        int pa[] = projectToCanvas(new double[] {i * options.tickScale_x, 0}, projection);
                        int pb[] = projectToCanvas(new double[] {-i * options.tickScale_x, 0}, projection);
                        c.drawLine(pa[0], pa[1] + options.tickLength, pa[0], pa[1] - options.tickLength, p);
                        c.drawLine(pb[0], pb[1] + options.tickLength, pb[0], pb[1] - options.tickLength, p);
                    }

                    p.setStrokeWidth(options.tickWidth);
                    p.setColor(options.axisColor_y);
                    numTicks = (int)(90 / options.tickScale_y) + 1;
                    for (int i=1; i<=numTicks; i++)   // y axis
                    {
                        int pa[] = projectToCanvas(new double[] {0, i * options.tickScale_y}, projection);
                        int pb[] = projectToCanvas(new double[] {0, -i * options.tickScale_y}, projection);
                        c.drawLine(pa[0] + options.tickLength, pa[1], pa[0] - options.tickLength, pa[1], p);
                        c.drawLine(pb[0] + options.tickLength, pb[1], pb[0] - options.tickLength, pb[1], p);
                    }
                }
            }

            // draw analemma line
            p.setColor(options.lineColor);
            p.setStrokeWidth(options.lineWidth);
            for (int i=0; i<d.size(); i++)
            {
                double[] d0 = d.get(i).getPoint(options.mode);
                double[] d1 = (i == d.size()-1 ? d.get(0) : d.get(i+1)).getPoint(options.mode);
                int[] p0 = projectToCanvas(d0, projection);
                int[] p1 = projectToCanvas(d1, projection);
                c.drawLine(p0[0], p0[1], p1[0], p1[1], p);
            }

            // draw month markers
            if (options.showMonths)
            {
                for (int i=0; i<12; i++)
                {
                    int[] monthPoint = projectToCanvas(dataPoints.monthPoint[i].getPoint(options.mode), projection);
                    drawPoint(monthPoint, options.monthPointWidth, options.monthColor, options.monthColor, 0, c, p);
                }
            }

            // draw season markers
            if (options.showSeasons)
            {
                int[] springPoint = projectToCanvas(dataPoints.springPoint.getPoint(options.mode), projection);
                drawPoint(springPoint, options.seasonPointWidth, options.springColor, options.springColor, 0, c, p);

                int[] summerPoint = projectToCanvas(dataPoints.summerPoint.getPoint(options.mode), projection);
                drawPoint(summerPoint, options.seasonPointWidth, options.summerColor, options.summerColor, 0, c, p);

                int[] fallPoint = projectToCanvas(dataPoints.fallPoint.getPoint(options.mode), projection);
                drawPoint(fallPoint, options.seasonPointWidth, options.fallColor, options.fallColor, 0, c, p);

                int[] winterPoint = projectToCanvas(dataPoints.winterPoint.getPoint(options.mode), projection);
                drawPoint(winterPoint, options.seasonPointWidth, options.winterColor, options.winterColor, 0, c, p);
            }

            // draw sun position
            if (options.showSun)
            {
                double[] sunData = dataPoints.getPoints().get(dataPoints.getDayOfYear()).getPoint(options.mode);
                int[] sunPoint = projectToCanvas(sunData, projection);
                drawPoint(sunPoint, options.sunPointWidth, options.sunPointFill, options.sunPointStroke, options.sunStrokeWidth, c, p);
            }

            long bench_end = System.nanoTime();
            Log.d("Analemma", "makeBitmap1 :: " + ((bench_end - bench_start) / 1000000.0) + " ms; " + w + ", " + h);
            return new AnalemmaTaskResult(b, dataPoints);
        }

        /**
         * drawPoint
         * @param pX x position
         * @param pY y position
         * @param r radius
         * @param colorFill fill color
         * @param colorStroke stroke color
         * @param strokeWidth stroke width
         * @param c canvas
         * @param p paint
         */
        private void drawPoint(int pX, int pY, int r, int colorFill, int colorStroke, int strokeWidth, Canvas c, Paint p)
        {
            p.setStyle(Paint.Style.FILL);
            p.setColor(colorFill);
            c.drawCircle(pX, pY, r, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(strokeWidth);
            p.setColor(colorStroke);
            c.drawCircle(pX, pY, r, p);
        }
        private void drawPoint(int[] pos, int r, int colorFill, int colorStroke, int strokeWidth, Canvas c, Paint p)
        {
            drawPoint(pos[0], pos[1], r, colorFill, colorStroke, strokeWidth, c, p);
        }

        private Matrix initProjection(double[][] dataBounds, double[] canvasBounds, AnalemmaOptions options)
        {
            float[] mid = new float[] {(float)(canvasBounds[0] / 2d), (float)(canvasBounds[1] / 2d)};
            double xAbsMax = (Math.max(Math.abs(dataBounds[0][0]), Math.abs(dataBounds[0][1])));
            double yAbsMax = (Math.max(Math.abs(dataBounds[1][0]), Math.abs(dataBounds[1][1])));

            Matrix m = new Matrix();
            switch (options.mode)
            {
                case ALT_AZ:
                    double xMid = dataBounds[0][0] + ((dataBounds[0][1] - dataBounds[0][0]) * 0.5f);
                    Log.d("DEBUG", "min: " + dataBounds[0][0] + ", max: " + dataBounds[0][1] + ", mid: " + xMid);

                    m.preTranslate((float)(mid[0] - xMid), (float)yAbsMax);
                    m.preScale(1, -1);
                    m.postScale( (float)((mid[0] - options.padding[0]) / xAbsMax),
                            (float)((canvasBounds[1] - (2 * options.padding[1])) / 45f) );
                    m.postTranslate(options.padding[0], options.padding[1]);
                    break;

                case ALT_EOT:
                    m.preTranslate((float)xAbsMax, (float)yAbsMax);
                    m.preScale(1, -1);
                    m.postScale( (float)((mid[0] - options.padding[0]) / xAbsMax),
                                 (float)((canvasBounds[1] - (2 * options.padding[1])) / 45f) );
                    m.postTranslate(options.padding[0], options.padding[1]);
                    break;

                case DEC_EOT:
                default:
                    m.preTranslate((float)xAbsMax, (float)yAbsMax);
                    m.preScale(1, -1);
                    m.postScale( (float)((mid[0] - options.padding[0]) / xAbsMax),
                                 (float)((mid[1] - options.padding[1]) / yAbsMax) );
                    m.postTranslate(options.padding[0], options.padding[1]);
                    break;
            }
            return m;
        }

        private int[] projectToCanvas(double[] d, @NonNull Matrix m)
        {
            return projectToCanvas(d[0], d[1], m);
        }

        private int[] projectToCanvas(double x, double y, @NonNull Matrix m)
        {
            float[] src = {(float)x, (float)y};
            float[] dst = new float[2];
            m.mapPoints(dst, src);
            return new int[] {(int)dst[0], (int)dst[1]};
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
        protected void onPostExecute( AnalemmaTaskResult result )
        {
            if (isCancelled())
            {
                result = null;
            }
            onFinished(result);
        }

        protected void onFinished( AnalemmaTaskResult result )
        {
            if (listener != null)
            {
                listener.onFinished(result);
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
        public void onFinished( AnalemmaTaskResult result ) {}
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
     * AnalemmaTaskResult
     */
    public static class AnalemmaTaskResult
    {
        public Bitmap bitmap;
        public AnalemmaData data;

        public AnalemmaTaskResult() {}
        public AnalemmaTaskResult(Bitmap b, AnalemmaData d)
        {
            this.bitmap = b;
            this.data = d;
        }
    }

    /**
     * AnalemmaOptions
     */
    @SuppressWarnings("WeakerAccess")
    public static class AnalemmaOptions
    {
        public int date_hour = 12, date_minute = 0;
        public LightMapWidgetSettings.AnalemmaWidgetMode mode = LightMapWidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_ANALEMMA;

        public int backgroundColor;
        public int[] padding = new int[] {8, 8};

        public boolean showAxis = true;
        private int axisWidth = DEFAULT_AXIS_WIDTH;
        public int axisColor_x, axisColor_y;

        public boolean showTicks = true;
        public int tickScale_x = 5, tickScale_y = 5;
        private int tickWidth = DEFAULT_TICK_WIDTH;
        private int tickLength = DEFAULT_TICK_LENGTH;

        public int lineColor;
        private int lineWidth = DEFAULT_LINE_WIDTH;

        public boolean showSun = true;
        public int sunPointFill, sunPointStroke;
        public int sunPointWidth = 6, sunStrokeWidth = 2;

        public boolean showMonths = true;
        public int monthPointWidth = 2, monthColor;

        public boolean showSeasons = true;
        public int springColor, summerColor, fallColor, winterColor;
        public int seasonPointWidth = 3, seasonStrokeWidth = 0;

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
                    R.attr.graphColor_pointStroke,          // 6
                    R.attr.springColor,                     // 7
                    R.attr.summerColor,                     // 8
                    R.attr.fallColor,                       // 9
                    R.attr.winterColor,                     // 10
            };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;

            lineColor = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            axisColor_x = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            axisColor_y = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
            backgroundColor = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
            sunPointFill = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
            sunPointStroke = ContextCompat.getColor(context, typedArray.getResourceId(6, def));

            springColor = ContextCompat.getColor(context, typedArray.getResourceId(7, def));
            summerColor = ContextCompat.getColor(context, typedArray.getResourceId(8, def));
            fallColor = ContextCompat.getColor(context, typedArray.getResourceId(9, def));
            winterColor = ContextCompat.getColor(context, typedArray.getResourceId(10, def));

            monthColor = lineColor;

            typedArray.recycle();
        }

        public void initDefaultDark(Context context)
        {
            lineColor = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            axisColor_x = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            axisColor_y = ContextCompat.getColor(context, R.color.graphColor_astronomical_dark);
            backgroundColor = ContextCompat.getColor(context, R.color.graphColor_night_dark);
            sunPointFill = ContextCompat.getColor(context, R.color.sunIcon_color_setting_dark);
            sunPointStroke = ContextCompat.getColor(context, R.color.grey_800);

            springColor = ContextCompat.getColor(context, R.color.springColor_dark);
            summerColor = ContextCompat.getColor(context, R.color.summerColor_dark);
            fallColor = ContextCompat.getColor(context, R.color.fallColor_dark);
            winterColor = ContextCompat.getColor(context, R.color.winterColor_dark);

            monthColor = lineColor;
        }

        public void initDefaultLight(Context context)
        {
            lineColor = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            axisColor_x = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            axisColor_y = ContextCompat.getColor(context, R.color.graphColor_astronomical_light);
            backgroundColor = ContextCompat.getColor(context, R.color.graphColor_night_light);
            sunPointFill = ContextCompat.getColor(context, R.color.sunIcon_color_setting_light);
            sunPointStroke = ContextCompat.getColor(context, R.color.grey_800);

            springColor = ContextCompat.getColor(context, R.color.springColor_light);
            summerColor = ContextCompat.getColor(context, R.color.summerColor_light);
            fallColor = ContextCompat.getColor(context, R.color.fallColor_light);
            winterColor = ContextCompat.getColor(context, R.color.winterColor_light);

            monthColor = lineColor;
        }
    }

    /**
     * AnalemmaPoint
     */
    public static class AnalemmaPoint
    {
        public Calendar date, sunrise, sunset;
        public SuntimesCalculator.SunPosition position;
        public double eot;

        public AnalemmaPoint(Calendar date, @NonNull SuntimesCalculator calculator)
        {
            this.date = date;
            this.eot = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(date.get(Calendar.DAY_OF_YEAR));
            this.position = calculator.getSunPosition(date);
            this.sunrise = calculator.getOfficialSunriseCalendarForDate(date);
            this.sunset = calculator.getOfficialSunsetCalendarForDate(date);
        }

        public double[] getPoint(LightMapWidgetSettings.AnalemmaWidgetMode mode)
        {
            switch (mode)
            {
                case ALT_AZ: return new double[] {position.azimuth, position.elevation};
                case ALT_EOT: return new double[] {eot, position.elevation};
                case DEC_EOT:
                default: return new double[] {eot, position.declination};
            }
        }
    }

    /**
     * AnalemmaData
     */
    public static class AnalemmaData
    {
        private Calendar date;
        private LightMapWidgetSettings.AnalemmaWidgetMode mode;

        protected AnalemmaPoint springPoint, summerPoint, fallPoint, winterPoint;
        protected AnalemmaPoint[] monthPoint = new AnalemmaPoint[12];
        protected ArrayList<AnalemmaPoint> points0 = new ArrayList<>();

        protected int i_maxX_eot = 0, i_maxX_az = 0;           // indices min/max
        protected int i_minX_eot = 0, i_minX_az = 0;

        protected int i_maxY_dec = 0, i_maxY_el = 0;          // indices min/max
        protected int i_minY_dec = 0, i_minY_el = 0;

        protected int i_earliest_sunrise = 0, i_earliest_sunset = 0;
        protected int i_latest_sunrise = 0, i_latest_sunset = 0;

        public AnalemmaData(@NonNull SuntimesCalculator calculator, @NonNull Calendar date, @NonNull LightMapWidgetSettings.AnalemmaWidgetMode mode)
        {
            this.date = date;
            this.mode = mode;
            generateDataPoints(calculator);
        }

        public LightMapWidgetSettings.AnalemmaWidgetMode getMode()
        {
             return mode;
        }

        public Calendar getDate()
        {
            return date;
        }
        public int getDayOfYear()
        {
            return date.get(Calendar.DAY_OF_YEAR);
        }

        public ArrayList<AnalemmaPoint> getPoints()
        {
            return points0;
        }

        public Calendar getEarliestSunrise()
        {
            return points0.get(i_earliest_sunrise).sunrise;
        }
        public Calendar getLatestSunrise()
        {
            return points0.get(i_latest_sunrise).sunrise;
        }

        public Calendar getEarliestSunset()
        {
            return points0.get(i_earliest_sunset).sunset;
        }
        public Calendar getLatestSunset()
        {
            return points0.get(i_latest_sunset).sunset;
        }

        public double getMaxX_EOT()
        {
            return points0.get(i_maxX_eot).eot;
        }
        public double getMinX_EOT()
        {
            return points0.get(i_minX_eot).eot;
        }

        public double getMaxX_azimuth()
        {
            return points0.get(i_maxX_az).position.azimuth;
        }
        public double getMinX_azimuth()
        {
            return points0.get(i_minX_az).position.azimuth;
        }

        public double getMaxY_declination()
        {
            return points0.get(i_maxY_dec).position.declination;
        }
        public double getMinY_declination()
        {
            return points0.get(i_minY_dec).position.declination;
        }

        public double getMaxY_elevation()
        {
            return points0.get(i_maxY_el).position.elevation;
        }
        public double getMinY_elevation()
        {
            return points0.get(i_minY_el).position.elevation;
        }

        public double[][] getBounds(LightMapWidgetSettings.AnalemmaWidgetMode mode)
        {
            double[][] dataBounds = new double[2][2];
            switch (mode)
            {
                case ALT_AZ:
                    dataBounds[0][0] = getMinX_azimuth();
                    dataBounds[0][1] = getMaxX_azimuth();
                    dataBounds[1][0] = getMinY_elevation();
                    dataBounds[1][1] = getMaxY_elevation();
                    break;
                case ALT_EOT:
                    dataBounds[0][0] = getMinX_EOT();
                    dataBounds[0][1] = getMaxX_EOT();
                    dataBounds[1][0] = getMinY_elevation();
                    dataBounds[1][1] = getMaxY_elevation();
                    break;
                case DEC_EOT:
                default:
                    dataBounds[0][0] = getMinX_EOT();
                    dataBounds[0][1] = getMaxX_EOT();
                    dataBounds[1][0] = getMinY_declination();
                    dataBounds[1][1] = getMaxY_declination();
                    break;
            }
            return dataBounds;
        }

        public boolean add( AnalemmaPoint point )
        {
            boolean retValue = points0.add(point);
            if (retValue)
            {
                int i = points0.size()-1;

                if (point.eot < getMinX_EOT()) {
                    i_minX_eot = i;
                }
                if (point.eot > getMaxX_EOT()) {
                    i_maxX_eot = i;
                }

                if (point.position.azimuth < getMinX_azimuth()) {
                    i_minX_az = i;
                }
                if (point.position.azimuth > getMaxX_azimuth()) {
                    i_maxX_az = i;
                }

                if (point.position.declination < getMinY_declination()) {
                    i_minY_dec = i;
                }
                if (point.position.declination > getMaxY_declination()) {
                    i_maxY_dec = i;
                }

                if (point.position.elevation < getMinY_elevation()) {
                    i_minY_el = i;
                }
                if (point.position.elevation > getMaxY_elevation()) {
                    i_maxY_el = i;
                }

                if (point.sunrise.before( SuntimesData.nowThen(getEarliestSunrise(), point.sunrise) )) {
                    i_earliest_sunrise = i;
                }
                if (point.sunset.before( SuntimesData.nowThen(getEarliestSunset(), point.sunset) )) {
                    i_earliest_sunset = i;
                }

                if (point.sunrise.after( SuntimesData.nowThen(getLatestSunrise(), point.sunrise) )) {
                    i_latest_sunrise = i;
                }
                if (point.sunset.after( SuntimesData.nowThen(getLatestSunset(), point.sunset) )) {
                    i_latest_sunset = i;
                }
            }
            return retValue;
        }

        private void generateDataPoints(@NonNull SuntimesCalculator calculator)
        {
            Calendar d = Calendar.getInstance();
            d.setTimeZone(date.getTimeZone());
            d.setTimeInMillis(date.getTimeInMillis());

            for (int day=1; day <= 365; day++)
            {
                d.set(Calendar.DAY_OF_YEAR, day);
                add(new AnalemmaPoint(d, calculator));
            }

            for (int month=0; month<12; month++)
            {
                d.set(Calendar.MONTH, month);
                d.set(Calendar.DAY_OF_MONTH, 1);
                monthPoint[month] =  points0.get(d.get(Calendar.DAY_OF_YEAR));
            }

            springPoint = points0.get(calculator.getVernalEquinoxForYear(date).get(Calendar.DAY_OF_YEAR));
            summerPoint = points0.get(calculator.getSummerSolsticeForYear(date).get(Calendar.DAY_OF_YEAR));
            fallPoint = points0.get(calculator.getAutumnalEquinoxForYear(date).get(Calendar.DAY_OF_YEAR));
            winterPoint = points0.get(calculator.getWinterSolsticeForYear(date).get(Calendar.DAY_OF_YEAR));
        }
    }

}
