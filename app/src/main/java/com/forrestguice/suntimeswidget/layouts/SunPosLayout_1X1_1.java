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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * A 1x1 layout that displays right ascension and declination.
 */
public class SunPosLayout_1X1_1 extends SunPosLayout
{
    public SunPosLayout_1X1_1()
    {
        super();
    }

    /**public SunPosLayout_1X1_1(int layoutID )
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_sunpos_1x1_6;
    }

    @Override
    public void prepareForUpdate(SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        dataset.dataActual.initCalculator();  // init calculator only; skipping full calculate()
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
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "0000000000", timeSizeSp, ClockLayout.CLOCKFACE_MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_rightascension_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_rightascension_current, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_sun_rightascension_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_sun_rightascension_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_declination_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_sun_declination_current, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_sun_declination_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_sun_declination_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                }
            }
        }

        SuntimesCalculator calculator = dataset.calculator();
        SuntimesCalculator.SunPosition sunPosition = calculator.getSunPosition(dataset.now());
        updateViewsRightAscDeclinationText(context, views, sunPosition);

        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_sun_rightascension_current_label, visibility);
        views.setViewVisibility(R.id.info_sun_declination_current_label, visibility);
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        themeViewsRightAscDeclinationText(context, views, theme);
    }
}
