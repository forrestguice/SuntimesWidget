/**
   Copyright (C) 2018 Forrest Guice
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
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.NumberFormat;
import java.util.Calendar;

public class MoonLayout_3x1_0 extends MoonLayout
{
    public MoonLayout_3x1_0()
    {
        super();
    }

    public MoonLayout_3x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_3x1_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        SuntimesUtils.TimeDisplayText newMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW));
        views.setTextViewText(R.id.moonphase_new_date, newMoonString.getValue());

        SuntimesUtils.TimeDisplayText firstQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER));
        views.setTextViewText(R.id.moonphase_firstquarter_date, firstQuarterMoonString.getValue());

        SuntimesUtils.TimeDisplayText fullMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL));
        views.setTextViewText(R.id.moonphase_full_date, fullMoonString.getValue());

        SuntimesUtils.TimeDisplayText thirdQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER));
        views.setTextViewText(R.id.moonphase_thirdquarter_date, thirdQuarterMoonString.getValue());

        /**boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);

        SuntimesUtils.TimeDisplayText riseString = utils.calendarTimeShortDisplayString(context, data.moonriseCalendarToday(), showSeconds);
        views.setTextViewText(R.id.text_time_moonrise, riseString.getValue());
        views.setTextViewText(R.id.text_time_moonrise_suffix, riseString.getSuffix());

        SuntimesUtils.TimeDisplayText setString = utils.calendarTimeShortDisplayString(context, data.moonsetCalendarToday(), showSeconds);
        views.setTextViewText(R.id.text_time_moonset, setString.getValue());
        views.setTextViewText(R.id.text_time_moonset_suffix, setString.getSuffix());

        NumberFormat percentage = NumberFormat.getPercentInstance();
        String illum = percentage.format(data.getMoonIlluminationToday());
        String illumNote = context.getString(R.string.moon_illumination, illum);
        SpannableString illumNoteSpan = SuntimesUtils.createColorSpan(illumNote, illum, illumColor);
        views.setTextViewText(R.id.text_info_moonillum, illumNoteSpan);

        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            views.setViewVisibility(moonPhase.getView(), View.GONE);
        }

        MoonPhaseDisplay phase = data.getMoonPhaseToday();
        if (phase != null)
        {
            views.setTextViewText(R.id.text_info_moonphase, phase.getLongDisplayString());
            views.setViewVisibility(phase.getView(), View.VISIBLE);
        }*/
    }

    private int illumColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        /**illumColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.text_info_moonillum, textColor);
        views.setTextColor(R.id.text_info_moonphase, textColor);

        int moonriseColor = theme.getMoonriseTextColor();
        int suffixColor = theme.getTimeSuffixColor();
        views.setTextColor(R.id.text_time_moonrise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_moonrise, moonriseColor);

        int moonsetColor = theme.getMoonsetTextColor();
        views.setTextColor(R.id.text_time_moonset_suffix, suffixColor);
        views.setTextColor(R.id.text_time_moonset, moonsetColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            float suffSize = theme.getTimeSuffixSizeSp();
            float textSize = theme.getTextSizeSp();

            views.setTextViewTextSize(R.id.text_info_moonphase, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.text_info_moonillum, TypedValue.COMPLEX_UNIT_SP, textSize);

            views.setTextViewTextSize(R.id.text_time_moonrise_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);
            views.setTextViewTextSize(R.id.text_time_moonrise, TypedValue.COMPLEX_UNIT_SP, timeSize);

            views.setTextViewTextSize(R.id.text_time_moonset, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.text_time_moonset_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);
        }

        Bitmap moonriseIcon = SuntimesUtils.insetDrawableToBitmap(context, R.drawable.ic_moon_rise, moonriseColor, moonriseColor, 0);
        views.setImageViewBitmap(R.id.icon_time_moonrise, moonriseIcon);

        Bitmap moonsetIcon = SuntimesUtils.insetDrawableToBitmap(context, R.drawable.ic_moon_set, moonsetColor,moonsetColor, 0);
        views.setImageViewBitmap(R.id.icon_time_moonset, moonsetIcon);*/
    }

    @Override
    public void prepareForUpdate(SuntimesMoonData data)
    {
        /**Calendar riseTime = data.moonriseCalendarToday();
        Calendar setTime = data.moonsetCalendarToday();

        if (riseTime != null && setTime != null)
        {
            if (riseTime.before(setTime))
                this.layoutID = R.layout.layout_widget_moon_2x1_0;      // moon rises then sets
            else this.layoutID = R.layout.layout_widget_moon_2x1_01;    // moon sets then rises

        } else if (riseTime == null && setTime == null) {
            this.layoutID = R.layout.layout_widget_moon_2x1_0;  // moon doesn't rise or set today

        } else if (setTime != null) {
            this.layoutID = R.layout.layout_widget_moon_2x1_01;  // moon doesn't rise (but it sets)

        } else {
            this.layoutID = R.layout.layout_widget_moon_2x1_0;  // moon doesn't set (but it rises)
        }*/
    }
}

