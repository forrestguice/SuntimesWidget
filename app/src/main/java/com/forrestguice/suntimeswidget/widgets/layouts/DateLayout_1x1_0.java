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
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calendar.CalendarFormat;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.layouts.ClockLayout;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;
import java.util.TimeZone;

public class DateLayout_1x1_0 extends DateLayout
{
    public DateLayout_1x1_0() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_date_1x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_date_1x1_0_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_date_1x1_0_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_date_1x1_0_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_date_1x1_0;         // center
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        Calendar now = Calendar.getInstance(data.timezone());
        CalendarMode mode = CalendarSettings.loadCalendarModePref(context, appWidgetId);
        String pattern = CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, mode.name());
        if (!CalendarFormat.isValidPattern(pattern)) {
            Log.w(getClass().getSimpleName(), "updateViews: invalid pattern! " + pattern + ", falling back to default..");
            pattern = mode.getDefaultPattern();
        }
        String displayString = CalendarMode.formatDate(mode, pattern, now) ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                float maxSp = DateLayout.MAX_SP;
                int[] maxDp = new int[] { maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) };
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldDate, displayString, dateSizeSp, maxSp, "", 0);

                if (adjustedSizeSp[0] != dateSizeSp) {
                    views.setTextViewTextSize(R.id.text_date, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                }
            }
        }

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        if (showLabels)
        {
            String extrasDisplayString = "TODO (extra labels)";    // TODO
            views.setTextViewText(R.id.text_date_extras, extrasDisplayString);
            views.setViewVisibility(R.id.text_date_extras, View.VISIBLE);
        } else views.setViewVisibility(R.id.text_date_extras, View.GONE);

        views.setTextViewText(R.id.text_date, displayString);
    }

    protected int dateColor = Color.WHITE;
    protected int textColor = Color.WHITE;
    //protected int suffixColor = Color.GRAY;
    private boolean boldDate = false;
    protected float titleSizeSp = 10;
    protected float dateSizeSp = 12;
    //protected float suffixSizeSp = 8;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        dateColor = theme.getTimeColor();
        textColor = theme.getTextColor();
        //suffixColor = theme.getTimeSuffixColor();
        boldTime = theme.getTimeBold();
        paddingDp = theme.getPadding();

        views.setTextColor(R.id.text_date, dateColor);
        views.setTextColor(R.id.text_date_extras, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            dateSizeSp = theme.getTimeSizeSp();
            //suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_date, TypedValue.COMPLEX_UNIT_DIP, dateSizeSp);
            views.setTextViewTextSize(R.id.text_date_extras, TypedValue.COMPLEX_UNIT_DIP, theme.getTextSizeSp());
        }
    }

}
