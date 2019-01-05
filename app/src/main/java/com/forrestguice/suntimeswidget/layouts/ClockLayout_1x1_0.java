/**
   Copyright (C) 2019 Forrest Guice
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

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

public class ClockLayout_1x1_0 extends ClockLayout
{
    public ClockLayout_1x1_0()
    {
        super();
    }

    /**public ClockLayout_1x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_clock_1x1_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        Calendar now = data.calendar();
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        SuntimesUtils.TimeDisplayText nowText = utils.calendarTimeShortDisplayString(context, now, false, timeFormat);
        String nowString = nowText.getValue();
        CharSequence nowChars = (boldTime ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadAllowResizePref(context, appWidgetId))
            {
                float maxSp = ClockLayout.CLOCKFACE_MAX_SP;  // ((category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) ? CLOCKFACE_MAX_SP : -1);
                float[] adjustedSizeSp = adjustTextSize(context, maxDimensionsDp, paddingDp, "sans-serif", boldTime,"00:00", timeSizeSp, maxSp, "MM", suffixSizeSp);
                if (adjustedSizeSp[0] != timeSizeSp) {
                    views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[1]);
                }
            }
        }

        views.setTextViewText(R.id.text_time, nowChars);
        views.setTextViewText(R.id.text_time_suffix, nowText.getSuffix());
    }

    private int timeColor = Color.WHITE;
    private int suffixColor = Color.GRAY;
    private boolean boldTime = false;
    private float timeSizeSp = 12;
    private float suffixSizeSp = 8;
    private int[] paddingDp = new int[] {0, 0};

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        timeColor = theme.getTimeColor();
        suffixColor = theme.getTimeSuffixColor();
        boldTime = theme.getTimeBold();
        paddingDp = theme.getPadding();

        views.setTextColor(R.id.text_time, timeColor);
        views.setTextColor(R.id.text_time_suffix, suffixColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_SP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_SP, suffixSizeSp);
        }
    }

    @Override
    public void prepareForUpdate(SuntimesClockData data)
    {
        /* EMPTY */
    }
}
