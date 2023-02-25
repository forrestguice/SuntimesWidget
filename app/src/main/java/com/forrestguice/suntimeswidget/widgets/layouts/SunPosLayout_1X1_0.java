/**
   Copyright (C) 2018-2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays azimuth and elevation.
 */
public class SunPosLayout_1X1_0 extends SunPosLayout
{
    public SunPosLayout_1X1_0()
    {
        super();
    }

    /**public SunPosLayout_1X1_0(int layoutID )
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_1x1_5;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        super.prepareForUpdate(context, appWidgetId, dataset, widgetSize);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);  // (scaleBase ? R.layout.layout_widget_sunpos_1x1_5_align_fill : R.layout.layout_widget_sunpos_1x1_5);
        dataset.dataActual.calculate();
        dataset.dataNoon.calculate();
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_sunpos_1x1_5_align_fill;
            case 1: return R.layout.layout_widget_sunpos_1x1_5_align_float_1;
            case 2: return R.layout.layout_widget_sunpos_1x1_5_align_float_2;
            case 3: return R.layout.layout_widget_sunpos_1x1_5_align_float_3;
            case 4: return R.layout.layout_widget_sunpos_1x1_5_align_float_4;
            case 6: return R.layout.layout_widget_sunpos_1x1_5_align_float_6;
            case 7: return R.layout.layout_widget_sunpos_1x1_5_align_float_7;
            case 8: return R.layout.layout_widget_sunpos_1x1_5_align_float_8;
            case 9: return R.layout.layout_widget_sunpos_1x1_5_align_float_9;
            case 5: default: return R.layout.layout_widget_sunpos_1x1_5;
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / (showLabels ? 4 : 2))};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "0000000000", timeSizeSp, SuntimesLayout.MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_azimuth_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_azimuth_current, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_sun_azimuth_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_sun_azimuth_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_elevation_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_elevation_current, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_elevation_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_sun_elevation_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                }
            }
        }

        SuntimesCalculator calculator = dataset.calculator();
        SuntimesCalculator.SunPosition sunPosition = (calculator != null ? calculator.getSunPosition(dataset.now()) : null);

        SuntimesRiseSetData noonData = dataset.dataNoon;
        Calendar noonTime = (noonData != null ? noonData.sunriseCalendarToday() : null);
        SuntimesCalculator.SunPosition noonPosition = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);

        updateViewsAzimuthElevationText(context, views, sunPosition, noonPosition);

        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_sun_azimuth_current_label, visibility);
        views.setViewVisibility(R.id.info_sun_elevation_current_label, visibility);
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        themeViewsAzimuthElevationText(context, views, theme);
    }
}
