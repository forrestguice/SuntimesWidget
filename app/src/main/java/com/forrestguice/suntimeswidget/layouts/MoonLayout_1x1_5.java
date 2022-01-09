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
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * Moon Position Widget (elevation, azimuth)
 */
public class MoonLayout_1x1_5 extends MoonLayout
{
    public MoonLayout_1x1_5()
    {
        super();
    }

    /**public MoonLayout_1x1_5(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_5;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        SuntimesCalculator calculator = data.calculator();
        SuntimesCalculator.MoonPosition moonPosition = (calculator != null ? calculator.getMoonPosition(data.now()) : null);
        updateViewsAzimuthElevationText(context, views, moonPosition);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_moon_azimuth_current_label, visibility);
        views.setViewVisibility(R.id.info_moon_elevation_current_label, visibility);
    }

    protected int suffixColor = Color.GRAY;
    protected int highlightColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        highlightColor = theme.getTimeColor();
        suffixColor = theme.getTimeSuffixColor();
        int textColor = theme.getTextColor();

        views.setTextColor(R.id.info_moon_azimuth_current_label, textColor);
        views.setTextColor(R.id.info_moon_elevation_current_label, textColor);
        views.setTextColor(R.id.info_moon_azimuth_current, textColor);
        views.setTextColor(R.id.info_moon_elevation_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_moon_azimuth_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.info_moon_elevation_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_moon_azimuth_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_moon_elevation_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

    protected void updateViewsAzimuthElevationText(Context context, RemoteViews views, SuntimesCalculator.MoonPosition moonPosition)
    {
        SuntimesUtils.TimeDisplayText azimuthDisplay = utils.formatAsDirection2(moonPosition.azimuth, PositionLayout.DECIMAL_PLACES, false);
        views.setTextViewText(R.id.info_moon_azimuth_current, PositionLayout.styleAzimuthText(azimuthDisplay, highlightColor, suffixColor, boldTime));

        if (Build.VERSION.SDK_INT >= 15) {
            SuntimesUtils.TimeDisplayText azimuthDescription = utils.formatAsDirection2(moonPosition.azimuth, PositionLayout.DECIMAL_PLACES, true);
            views.setContentDescription(R.id.info_moon_azimuth_current, utils.formatAsDirection(azimuthDescription.getValue(), azimuthDescription.getSuffix()));
        }
        views.setTextViewText(R.id.info_moon_elevation_current, PositionLayout.styleElevationText(moonPosition.elevation, highlightColor, suffixColor, boldTime));
    }

    @Override
    public boolean saveNextSuggestedUpdate(Context context, int appWidgetId)
    {
        long updateInterval = (5 * 60 * 1000);                 // update every 5 min
        long nextUpdate = Calendar.getInstance().getTimeInMillis() + updateInterval;
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate);
        Log.d("MoonLayout", "saveNextSuggestedUpdate: " + utils.calendarDateTimeDisplayString(context, nextUpdate).toString());
        return true;
    }

}
