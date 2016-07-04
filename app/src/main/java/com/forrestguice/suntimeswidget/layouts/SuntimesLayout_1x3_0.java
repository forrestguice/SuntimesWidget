/**
   Copyright (C) 2014 Forrest Guice
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
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;

public class SuntimesLayout_1x3_0 extends SuntimesLayout
{
    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x3_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        // update sunrise time
        TimeDisplayText sunriseString = utils.calendarTimeShortDisplayString(context, data.sunriseCalendarToday());
        views.setTextViewText(R.id.text_time_sunrise, sunriseString.getValue());
        views.setTextViewText(R.id.text_time_sunrise_suffix, sunriseString.getSuffix());

        // upset sunset time
        TimeDisplayText sunsetString = utils.calendarTimeShortDisplayString(context, data.sunsetCalendarToday());
        views.setTextViewText(R.id.text_time_sunset, sunsetString.getValue());
        views.setTextViewText(R.id.text_time_sunset_suffix, sunsetString.getSuffix());

        // update sunrise delta
        //String sunriseDeltaString = calendarDeltaShortDisplayString(sunriseCalendarToday, sunriseCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunrise, sunriseDeltaString);

        // update sunset delta
        //String sunsetDeltaString = calendarDeltaShortDisplayString(sunsetCalendarToday, sunsetCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunset, sunsetDeltaString);

        // update day delta
        TimeDisplayText dayDeltaDisplay = utils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther());
        String dayDeltaValue = dayDeltaDisplay.getValue();
        String dayDeltaUnits = dayDeltaDisplay.getUnits();
        String dayDeltaSuffix = dayDeltaDisplay.getSuffix();

        views.setTextViewText(R.id.text_delta_day_prefix, data.dayDeltaPrefix());
        views.setTextViewText(R.id.text_delta_day_value, dayDeltaValue);
        views.setTextViewText(R.id.text_delta_day_units, dayDeltaUnits);
        views.setTextViewText(R.id.text_delta_day_suffix, dayDeltaSuffix);

        //views.setViewVisibility(R.id.text_delta_day_units, (dayDeltaUnits.trim().equals("") ? View.GONE : View.VISIBLE));
        //views.setViewVisibility(R.id.text_delta_day_suffix, (dayDeltaSuffix.trim().equals("") ? View.GONE : View.VISIBLE));
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        int sunriseColor = theme.getSunriseTextColor();
        int sunsetColor = theme.getSunsetTextColor();
        int suffixColor = theme.getThemeTimeSuffixColor();
        //int titleColor = theme.getTitleColor();
        //int textColor = theme.getTextColor();

        // theme sunrise text
        views.setTextColor(R.id.text_time_sunrise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunrise, sunriseColor);

        // theme sunset text
        views.setTextColor(R.id.text_time_sunset_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunset, sunsetColor);

        //views.setTextColor(R.id.text_delta_sunrise, sunriseColor);
        //views.setTextColor(R.id.text_delta_sunset, sunsetColor);

        //views.setTextColor(R.id.text_delta_day_prefix, textColor);
        //views.setTextColor(R.id.text_delta_day_value, sunsetColor);
        //views.setTextColor(R.id.text_delta_day_units, textColor);
        //views.setTextColor(R.id.text_delta_day_suffix, suffixColor);
    }
}
