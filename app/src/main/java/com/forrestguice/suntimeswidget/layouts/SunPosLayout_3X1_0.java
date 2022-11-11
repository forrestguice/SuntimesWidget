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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.LightMapView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 3x1 layout with the lightmap graph.
 */
public class SunPosLayout_3X1_0 extends SunPosLayout
{
    public SunPosLayout_3X1_0()
    {
        super();
    }

    /**public SunPosLayout_3X1_0(int layoutID )
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_3x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);
        int position = (scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId));
        this.layoutID = chooseLayout(position); //(scaleBase ? R.layout.layout_widget_sunpos_3x1_0_align_fill : R.layout.layout_widget_sunpos_3x1_0);
        if (Build.VERSION.SDK_INT >= 16)
        {
            this.dpWidth = widgetSize[0];
            //this.dpHeight = widgetSize[1];
        }
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_sunpos_3x1_0_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_sunpos_3x1_0_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_sunpos_3x1_0_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_sunpos_3x1_0;         // center
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3])) / 4)};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "MMM.MMM MMM.MMM MMM.MMM MMM.MMM MMM.MMM", timeSizeSp, ClockLayout.CLOCKFACE_MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_azimuth_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_azimuth_current, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_sun_elevation_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_elevation_current, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_azimuth_rising, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_azimuth_rising, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_sun_elevation_atnoon, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_elevation_atnoon, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_sun_azimuth_setting, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_azimuth_setting, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));
                }
            }
        }

        Calendar now = dataset.now();
        SuntimesCalculator calculator = dataset.calculator();
        SuntimesCalculator.SunPosition sunPosition = (calculator != null ? calculator.getSunPosition(now) : null);

        SuntimesRiseSetData riseSetData = dataset.dataActual;
        Calendar riseTime = (riseSetData != null ? riseSetData.sunriseCalendarToday() : null);
        SuntimesCalculator.SunPosition risingPosition = (riseTime != null && calculator != null ? calculator.getSunPosition(riseTime) : null);

        SuntimesRiseSetData noonData = dataset.dataNoon;
        Calendar noonTime = (noonData != null ? noonData.sunriseCalendarToday() : null);
        SuntimesCalculator.SunPosition noonPosition = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);

        Calendar setTime = (riseSetData != null ? riseSetData.sunsetCalendarToday() : null);
        SuntimesCalculator.SunPosition settingPosition = (setTime != null && calculator != null ? calculator.getSunPosition(setTime) : null);

        updateViewsAzimuthElevationText(context, views, sunPosition, noonPosition);
        updateViewsAzimuthElevationText(context, views, sunPosition, risingPosition, noonPosition, settingPosition);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_time_lightmap_labels, visibility);

        LightMapView.LightMapTask drawTask = new LightMapView.LightMapTask();
        Bitmap bitmap = drawTask.makeBitmap(dataset, SuntimesUtils.dpToPixels(context, dpWidth), SuntimesUtils.dpToPixels(context, dpHeight), colors);
        views.setImageViewBitmap(R.id.info_time_lightmap, bitmap);

        if (Build.VERSION.SDK_INT >= 15) {
            views.setContentDescription(R.id.info_time_lightmap, buildContentDescription(context, now, sunPosition));
        }
    }

    public static String buildContentDescription(Context context, Calendar now, SuntimesCalculator.SunPosition sunPosition)
    {
        String contentDescription = utils.calendarTimeShortDisplayString(context, now, false).toString();
        if (sunPosition != null)
        {
            SuntimesUtils.TimeDisplayText elevationDisplay = utils.formatAsElevation(sunPosition.elevation, DECIMAL_PLACES);
            contentDescription += ", " + utils.formatAsElevation(elevationDisplay.getValue(), elevationDisplay.getSuffix());

            SuntimesUtils.TimeDisplayText azimuthDisplay = utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, true);
            contentDescription += ", " + utils.formatAsDirection(azimuthDisplay.getValue(), azimuthDisplay.getSuffix());
        }
        return contentDescription;        // time, elevation, azimuth
    }

    public static final int HEIGHT_TINY   = 16;
    public static final int HEIGHT_SMALL  = 16;
    public static final int HEIGHT_MEDIUM = 28;
    public static final int HEIGHT_LARGE  = 40;

    protected LightMapView.LightMapColors colors;
    protected int dpWidth = 320, dpHeight = HEIGHT_LARGE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        themeViewsAzimuthElevationText(context, views, theme);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_rising, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_atnoon, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_azimuth_setting, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }

        colors = new LightMapView.LightMapColors();
        if (theme.getBackground() == SuntimesTheme.ThemeBackground.LIGHT)
            colors.initDefaultLight(context);
        else colors.initDefaultDark(context);

        colors.colorDay = theme.getDayColor();
        colors.colorCivil = theme.getCivilColor();
        colors.colorNautical = theme.getNauticalColor();
        colors.colorAstro = theme.getAstroColor();
        colors.colorNight = theme.getNightColor();
        colors.colorPointFill = theme.getGraphPointFillColor();
        colors.colorPointStroke = theme.getGraphPointStrokeColor();
    }

}
