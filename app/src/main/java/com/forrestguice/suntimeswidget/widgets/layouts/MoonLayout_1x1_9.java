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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calendar.AndroidCalendarDisplayFactory;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * Moon Day Widget
 */
public class MoonLayout_1x1_9 extends MoonLayout
{
    public static final int MAX_SP = 144;

    public MoonLayout_1x1_9()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_9;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_moon_1x1_9_align_fill;
            case 1: return R.layout.layout_widget_moon_1x1_9_align_float_1;
            case 2: return R.layout.layout_widget_moon_1x1_9_align_float_2;
            case 3: return R.layout.layout_widget_moon_1x1_9_align_float_3;
            case 4: return R.layout.layout_widget_moon_1x1_9_align_float_4;
            case 6: return R.layout.layout_widget_moon_1x1_9_align_float_6;
            case 7: return R.layout.layout_widget_moon_1x1_9_align_float_7;
            case 8: return R.layout.layout_widget_moon_1x1_9_align_float_8;
            case 9: return R.layout.layout_widget_moon_1x1_9_align_float_9;
            case 5: default: return R.layout.layout_widget_moon_1x1_9;
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / (showLabels ? 2 : 1))};
                String labelText = context.getString(R.string.widgetMode1x1_moonday);
                String scaleToText = (showLabels ? "_" + labelText + "_" : "_00_");
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, scaleToText, timeSizeSp, MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_moon_day, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_moon_day, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

                    views.setTextViewTextSize(R.id.info_moon_day_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_moon_day_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                }
            }
        }

        int dayNum = AndroidCalendarDisplayFactory.create().hijriLunarDayNumber();
        int dayOffset = WidgetSettings.loadDateOffsetPref(context, appWidgetId);
        int moonDay = dayNum + dayOffset;
        String moonDayDisplay = moonDay + "";
        views.setTextViewText(R.id.info_moon_day, moonDayDisplay);

        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_moon_day_label, visibility);
    }

    protected int highlightColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        highlightColor = theme.getTimeColor();
        int textColor = theme.getTextColor();

        views.setTextColor(R.id.info_moon_day_label, textColor);
        views.setTextColor(R.id.info_moon_day, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            float timeSize = theme.getTimeSizeSp();

            views.setTextViewTextSize(R.id.info_moon_day_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.info_moon_day, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

    @Override
    public boolean saveNextSuggestedUpdate(Context context, int appWidgetId)
    {
        long updateInterval = (5 * 60 * 1000);                 // update every 5 min    // TODO: daily interval
        long nextUpdate = Calendar.getInstance().getTimeInMillis() + updateInterval;
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate);
        Log.d("MoonLayout9", "saveNextSuggestedUpdate: " + utils.calendarDateTimeDisplayString(context, nextUpdate).toString());
        return true;
    }
}
