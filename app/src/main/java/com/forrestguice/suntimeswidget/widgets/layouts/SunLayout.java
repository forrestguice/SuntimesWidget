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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.settings.RiseSetOrder;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.Calendar;

public abstract class SunLayout extends SuntimesLayout
{
    protected int suffixColor = Color.GRAY;
    protected int sunriseColor = Color.YELLOW;
    protected int sunsetColor = Color.YELLOW;

    protected float titleSizeSp = 10;
    protected float textSizeSp = 12;
    protected float timeSizeSp = 12;
    protected float suffixSizeSp = 8;
    protected float iconSizeDp = 32;

    protected boolean scaleBase = WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE;

    /**
     * Called by widget before themeViews and updateViews to give the layout obj an opportunity to
     * modify its state based on the supplied data.
     * @param data the data object (should be the same as supplied to updateViews)
     */
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetData data)
    {
        this.scaleBase = WidgetSettings.loadScaleBasePref(context, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        suffixColor = theme.getTimeSuffixColor();
        sunriseColor = theme.getSunriseTextColor();
        sunsetColor = theme.getSunsetTextColor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            titleSizeSp = theme.getTitleSizeSp();
            textSizeSp = theme.getTextSizeSp();
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();
        }
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     * @param data the data object to apply to the views
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        // update title
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, data);
        CharSequence title = (boldTitle ? SpanUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected void updateViewsSunRiseSetText(Context context, RemoteViews views, SuntimesRiseSetData data, boolean showSeconds, RiseSetOrder order, TimeFormatMode timeFormat)
    {
        if (order == RiseSetOrder.TODAY)
        {
            updateViewsSunriseText(context, views, data.sunriseCalendarToday(), showSeconds, timeFormat);
            updateViewsSunsetText(context, views, data.sunsetCalendarToday(), showSeconds, timeFormat);

        } else {
            Calendar now = data.now();
            Calendar sunriseToday = data.sunriseCalendarToday();
            Calendar sunsetToday = data.sunsetCalendarToday();

            if (now.before(sunriseToday))      // in the wee hours
            {
                updateViewsSunriseText(context, views, data.sunriseCalendar(1), showSeconds, timeFormat);  // sunrise today
                updateViewsSunsetText(context, views, data.sunsetCalendar(0), showSeconds, timeFormat);    // sunset yesterday

            } else if (now.before(sunsetToday)) {       // during the day
                updateViewsSunriseText(context, views, data.sunriseCalendar(1), showSeconds, timeFormat);  // sunrise today
                updateViewsSunsetText(context, views, data.sunsetCalendar(1), showSeconds, timeFormat);    // sunset today

            } else {                          // night; the day is over (but "tomorrow" has yet to arrive)
                updateViewsSunsetText(context, views, data.sunsetCalendar(1), showSeconds, timeFormat);    // sunset today
                updateViewsSunriseText(context, views, data.sunriseCalendar(2), showSeconds, timeFormat);  // sunrise tomorrow
            }
        }

    }

    protected void updateViewsSunriseText(Context context, RemoteViews views, Calendar event, boolean showSeconds, TimeFormatMode timeFormat)
    {
        TimeDisplayText sunriseText = time_utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), event, showSeconds, timeFormat);
        String sunriseString = sunriseText.getValue();
        CharSequence sunrise = (boldTime ? SpanUtils.createBoldSpan(null, sunriseString, sunriseString) : sunriseString);
        views.setTextViewText(R.id.text_time_rise, sunrise);
        views.setTextViewText(R.id.text_time_rise_suffix, sunriseText.getSuffix());
    }

    protected void updateViewsSunsetText(Context context, RemoteViews views, Calendar event, boolean showSeconds, TimeFormatMode timeFormat)
    {
        TimeDisplayText sunsetText = time_utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), event, showSeconds, timeFormat);
        String sunsetString = sunsetText.getValue();
        CharSequence sunset = (boldTime ? SpanUtils.createBoldSpan(null, sunsetString, sunsetString) : sunsetString);
        views.setTextViewText(R.id.text_time_set, sunset);
        views.setTextViewText(R.id.text_time_set_suffix, sunsetText.getSuffix());
    }

    protected void updateViewsNoonText(Context context, RemoteViews views, Calendar event, boolean showSeconds, TimeFormatMode timeFormat)
    {
        TimeDisplayText noonText = time_utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), event, showSeconds, timeFormat);
        String noonString = noonText.getValue();
        CharSequence noon = (boldTime ? SpanUtils.createBoldSpan(null, noonString, noonString) : noonString);
        views.setTextViewText(R.id.text_time_noon, noon);
        views.setTextViewText(R.id.text_time_noon_suffix, noonText.getSuffix());
    }

    protected int chooseSunLayout(int layout1, int layout2, SuntimesRiseSetData data, RiseSetOrder order)
    {
        switch (order)
        {
            case LASTNEXT:
                Calendar now = data.now();
                if (now.before(data.sunriseCalendarToday())) {
                    return layout2;   // last sunset, next sunrise

                } else if (now.before(data.sunsetCalendarToday())) {
                    return layout1;   // last sunrise, next sunset

                } else {
                    return layout2;   // last sunset, next sunrise
                }

            case TODAY:
            default:
                return layout1;
        }
    }

}
