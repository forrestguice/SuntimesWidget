/**
   Copyright (C) 2019-2022 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calendar.CalendarFormat;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockLayout_1x1_0 extends ClockLayout
{
    public ClockLayout_1x1_0()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_clock_1x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);  //(scaleBase ? R.layout.layout_widget_clock_1x1_0_align_fill : R.layout.layout_widget_clock_1x1_0);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_clock_1x1_0_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_clock_1x1_0_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_clock_1x1_0_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_clock_1x1_0;         // center
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.text_time_extras, showLabels ? View.VISIBLE : View.GONE);

        boolean showDate = CalendarSettings.loadCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE);
        views.setViewVisibility(R.id.text_date, showDate ? View.VISIBLE : View.GONE);

        Calendar now = data.calendar();
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        SuntimesUtils.TimeDisplayText nowText = utils.calendarTimeShortDisplayString(context, now, false, timeFormat);
        String nowString = nowText.getValue();
        CharSequence nowChars = (boldTime ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

        String dateString = null;
        if (showDate)
        {
            CalendarMode mode = CalendarMode.GREGORIAN;
            String pattern = CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, mode.name());
            if (!CalendarFormat.isValidPattern(pattern)) {
                Log.w(getClass().getSimpleName(), "updateViews: invalid pattern! " + pattern + ", falling back to default..");
                pattern = mode.getDefaultPattern();
            }
            dateString = CalendarMode.formatDate(mode, pattern, now);
            views.setTextViewText(R.id.text_date, dateString);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId, true))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);

                float[] adjustedSizeSp0 = new float[] {timeSizeSp, timeSizeSp};
                float[] adjustedSizeSp1 = new float[] {textSizeSp, textSizeSp};

                int c = 0;
                boolean rescale = false;
                do
                {
                    float maxSp = SuntimesLayout.MAX_SP;  // ((category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) ? CLOCKFACE_MAX_SP : -1);
                    int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle) - ((int)adjustedSizeSp1[0] * (showDate ? 1 : 0)))};

                    adjustedSizeSp0 = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime,"00:00", timeSizeSp, maxSp, "MM", suffixSizeSp);
                    if (adjustedSizeSp0[0] != timeSizeSp) {
                        views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp0[0]);
                        views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp0[1]);
                    }

                    if (showDate && dateString != null) {
                        maxSp *= 0.33f;
                        maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle) - (int)adjustedSizeSp0[0])};
                        adjustedSizeSp1 = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, dateString, textSizeSp, maxSp, "", 0);
                        if (adjustedSizeSp1[0] != textSizeSp) {
                            views.setTextViewTextSize(R.id.text_date, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp1[0]);
                            rescale = true;
                        }
                    }
                    c++;
                } while (rescale && c < 2);
            }
        }

        views.setTextViewText(R.id.text_time, nowChars);
        views.setTextViewText(R.id.text_time_suffix, nowText.getSuffix());

        if (showLabels)
        {
            int stringResID;
            Long offset = null;
            if (data.timezoneMode() == WidgetSettings.TimezoneMode.SOLAR_TIME)
            {
                stringResID = R.string.timezoneExtraApparentSolar_short;
                if (WidgetSettings.loadSolarTimeModePref(context, appWidgetId) == SolarTimeMode.APPARENT_SOLAR_TIME) {
                    offset = (long)data.calculator().equationOfTime(now) * 1000L;  //(long)WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(now.getTimeInMillis());
                }

            } else {
                stringResID = R.string.timezoneExtraDST_short;
                TimeZone timezone = data.timezone();
                boolean usesDST = (Build.VERSION.SDK_INT < 24 ? timezone.useDaylightTime() : timezone.observesDaylightTime());
                boolean inDST = usesDST && timezone.inDaylightTime(now.getTime());
                if (inDST) {
                    offset = (long) timezone.getDSTSavings();
                }
            }

            if (offset != null)
            {
                SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(0L, offset, false, false, true);
                String offsetString = (offsetText.getRawValue() < 0 ? "-" : "+") + offsetText.getValue();
                String extrasString = context.getString(stringResID, offsetString);
                SpannableString boldedExtrasSpan = SuntimesUtils.createBoldColorSpan(SpannableString.valueOf(extrasString), extrasString, offsetString, timeColor);
                views.setTextViewText(R.id.text_time_extras, boldedExtrasSpan);
                views.setViewVisibility(R.id.text_time_extras, View.VISIBLE);

            } else {
                views.setViewVisibility(R.id.text_time_extras, View.GONE);
            }
        }
    }

    protected int timeColor = Color.WHITE;
    protected int textColor = Color.WHITE;
    protected int suffixColor = Color.GRAY;
    private boolean boldTime = false;
    protected float titleSizeSp = 10;
    protected float timeSizeSp = 12;
    protected float textSizeSp = 12;
    protected float suffixSizeSp = 8;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        timeColor = theme.getTimeColor();
        textColor = theme.getTextColor();
        suffixColor = theme.getTimeSuffixColor();
        boldTime = theme.getTimeBold();
        paddingDp = theme.getPadding();

        views.setTextColor(R.id.text_time, timeColor);
        views.setTextColor(R.id.text_time_suffix, suffixColor);
        views.setTextColor(R.id.text_time_extras, textColor);
        views.setTextColor(R.id.text_date, timeColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            timeSizeSp = theme.getTimeSizeSp();
            textSizeSp = theme.getTextSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);
            views.setTextViewTextSize(R.id.text_time_extras, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_date, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
        }
    }

}
