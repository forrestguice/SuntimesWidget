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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
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
        SuntimesUtils.TimeDisplayText nowText = utils.calendarTimeShortDisplayString(context, now, false);
        String nowString = nowText.getValue();
        CharSequence nowChars = (boldTime ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadAllowResizePref(context, appWidgetId)) {
                adjustTextSize(context, views, "sans-serif","00:00", "MM");
            }
        }

        views.setTextViewText(R.id.text_time, nowChars);
        views.setTextViewText(R.id.text_time_suffix, nowText.getSuffix());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void adjustTextSize(Context context, RemoteViews views, String fontFamily, String timeText, String suffixText)
    {
        float adjustedTimeSizeSp = timeSizeSp;
        Rect timeBounds = new Rect();
        Paint timePaint = new Paint();
        timePaint.setTypeface(Typeface.create(fontFamily, boldTime ? Typeface.BOLD : Typeface.NORMAL));

        float adjustedSuffixSizeSp = suffixSizeSp;
        Rect suffixBounds = new Rect();
        Paint suffixPaint = new Paint();
        suffixPaint.setTypeface(Typeface.create(fontFamily, Typeface.BOLD));

        float stepSizeSp = 0.1f;                                      // upscale by stepSize (until maxWidth is filled)
        float suffixRatio = suffixSizeSp / timeSizeSp;                // preserve suffix proportions while scaling
        float maxWidthDp = (maxDimensionsDp[0] - paddingDp[0] - 8);   // maxWidth is adjusted for padding and margins
        float maxWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidthDp, context.getResources().getDisplayMetrics());

        while ((timeBounds.width() + suffixBounds.width()) < maxWidthPixels)
        {
            adjustedTimeSizeSp += stepSizeSp;
            adjustedSuffixSizeSp += stepSizeSp * suffixRatio;
            getTextBounds(context,  timeText, adjustedTimeSizeSp, timePaint, timeBounds);
            getTextBounds(context, suffixText, adjustedSuffixSizeSp, suffixPaint, suffixBounds);
        }

        if (adjustedTimeSizeSp != timeSizeSp)
        {
            views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_SP, adjustedTimeSizeSp);
            views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_SP, adjustedSuffixSizeSp);
        }
    }

    private void getTextBounds(@NonNull Context context, @NonNull String text, float textSizeSp, @NonNull Paint textPaint, @NonNull Rect textBounds)
    {
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, context.getResources().getDisplayMetrics()));
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
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
