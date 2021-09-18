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

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;
import java.util.TimeZone;

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
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        this.layoutID = (scaleBase ? R.layout.layout_widget_clock_1x1_0_align_fill : R.layout.layout_widget_clock_1x1_0);
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.text_time_extras, showLabels ? View.VISIBLE : View.GONE);

        Calendar now = data.calendar();
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        SuntimesUtils.TimeDisplayText nowText = utils.calendarTimeShortDisplayString(context, now, false, timeFormat);
        String nowString = nowText.getValue();
        CharSequence nowChars = (boldTime ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId, true))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                float maxSp = ClockLayout.CLOCKFACE_MAX_SP;  // ((category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) ? CLOCKFACE_MAX_SP : -1);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle))};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime,"00:00", timeSizeSp, maxSp, "MM", suffixSizeSp);
                if (adjustedSizeSp[0] != timeSizeSp) {
                    views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
                }
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
                if (WidgetSettings.loadSolarTimeModePref(context, appWidgetId) == WidgetSettings.SolarTimeMode.APPARENT_SOLAR_TIME) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);
            views.setTextViewTextSize(R.id.text_time_extras, TypedValue.COMPLEX_UNIT_DIP, theme.getTextSizeSp());
        }
    }

}
