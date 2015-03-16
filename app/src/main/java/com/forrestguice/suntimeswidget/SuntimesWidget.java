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

package com.forrestguice.suntimeswidget;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorFactory;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetSettings;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetTheme;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetThemes;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SuntimesWidgetSettingsActivity SuntimesWidgetSettingsActivity}
 */
public class SuntimesWidget extends AppWidgetProvider
{
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        SuntimesWidgetThemes.initThemes(context);
        SuntimesCalculatorFactory.initCalculators(context);
        SuntimesWidgetSettings.TimeMode.initDisplayStrings(context);

        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            SuntimesWidgetSettings.deletePrefs(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {

    }

    @Override
    public void onDisabled(Context context)
    {
        // when the last widget is disabled
    }

    /**
     * @param context the application context
     * @param rows number of rows in widget
     * @param columns number of cols in widget
     * @return a RemoteViews instance for the specified widget size
     */
    private static RemoteViews getWidgetViews(Context context, int rows, int columns)
    {
        RemoteViews views;
        if (columns >= 3)
        {
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x3);

        } else if (columns == 2)
        {
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x2);

        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x1);
        }
        return views;
    }

    /**
     * @param context
     * @param views
     * @param theme
     */
    static void themeViews(Context context, RemoteViews views, SuntimesWidgetTheme theme)
    {
        Resources resources = context.getResources();
        int[] padding = new int[] { (int)resources.getDimension(R.dimen.widget_padding_left),
                                    (int)resources.getDimension(R.dimen.widget_padding_top),
                                    (int)resources.getDimension(R.dimen.widget_padding_right),
                                    (int)resources.getDimension(R.dimen.widget_padding_bottom) };
        int sunriseColor = theme.getSunriseTextColor();
        int sunsetColor = theme.getSunsetTextColor();
        int suffixColor = theme.getThemeTimeSuffixColor();
        int textColor = theme.getTextColor();

        views.setInt(R.id.widgetframe_inner_1x1, "setBackgroundResource", theme.getBackgroundId());
        views.setViewPadding( R.id.widgetframe_inner_1x1, padding[0], padding[1], padding[2], padding[3] );

        views.setInt(R.id.widgetframe_inner_1x2, "setBackgroundResource", theme.getBackgroundId());
        views.setViewPadding( R.id.widgetframe_inner_1x2, padding[0], padding[1], padding[2], padding[3] );

        views.setInt(R.id.widgetframe_inner_1x3, "setBackgroundResource", theme.getBackgroundId());
        views.setViewPadding( R.id.widgetframe_inner_1x3, padding[0], padding[1], padding[2], padding[3] );

        views.setTextColor(R.id.text_title, theme.getTitleColor());

        views.setTextColor(R.id.text_time_sunrise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunrise, sunriseColor);
        views.setTextColor(R.id.text_delta_sunrise, sunriseColor);

        views.setTextColor(R.id.text_time_sunset_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunset, sunsetColor);
        views.setTextColor(R.id.text_delta_sunset, sunsetColor);

        views.setTextColor(R.id.text_delta_day_prefix, textColor);
        views.setTextColor(R.id.text_delta_day_value, sunsetColor);
        views.setTextColor(R.id.text_delta_day_units, textColor);
        views.setTextColor(R.id.text_delta_day_suffix, suffixColor);
    }

    private static SuntimesWidgetSettings.Location getCurrentLocation(Context context)
    {
        return null;   // TODO
    }

    /**
     * @param context the application context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of widget to be updated
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int widgetRows = SuntimesWidgetUtils.getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        int widgetCols = SuntimesWidgetUtils.getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));

        // apply appearance settings
        SuntimesWidgetTheme theme = SuntimesWidgetSettings.loadThemePref(context, appWidgetId);
        RemoteViews views = getWidgetViews(context, widgetRows, widgetCols);
        themeViews(context, views, theme);

        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        String titlePattern = SuntimesWidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = SuntimesWidgetUtils.displayStringForTitlePattern(titlePattern, context, appWidgetId);
        views.setTextViewText(R.id.text_title, titleText);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        // apply general settings
        SuntimesCalculatorDescriptor calculatorMode = SuntimesWidgetSettings.loadCalculatorModePref(context, appWidgetId);
        SuntimesWidgetSettings.TimeMode timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);
        SuntimesWidgetSettings.CompareMode compareMode = SuntimesWidgetSettings.loadCompareModePref(context, appWidgetId);

        // get location settings
        SuntimesWidgetSettings.Location location = SuntimesWidgetSettings.loadLocationPref(context, appWidgetId);
        SuntimesWidgetSettings.LocationMode locationMode = SuntimesWidgetSettings.loadLocationModePref(context, appWidgetId);
        if (locationMode == SuntimesWidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            location = getCurrentLocation(context);
        }

        // get timezone settings
        String timezone = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);
        SuntimesWidgetSettings.TimezoneMode timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        if (timezoneMode == SuntimesWidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
        {
            timezone = TimeZone.getDefault().getID();
        }

        // DEBUG (comment me)
        Log.v("DEBUG", "rows: " + widgetRows + ", " + "cols: " + widgetCols);
        Log.v("DEBUG", "show title: " + showTitle);
        Log.v("DEBUG", "title text: " + titleText);
        Log.v("DEBUG", "time mode: " + timeMode);
        Log.v("DEBUG", "location_mode: " + locationMode.name());
        Log.v("DEBUG", "latitude: " + location.getLatitude());
        Log.v("DEBUG", "longitude: " + location.getLongitude());
        Log.v("DEBUG", "timezone_mode: " + timezoneMode.name());
        Log.v("DEBUG", "timezone: " + timezone);
        Log.v("DEBUG", "compare mode: " + compareMode.name());
        Log.v("DEBUG", "theme: " + theme.getThemeName());

        String dayDeltaPrefix;
        Calendar todaysCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();

        switch (compareMode)
        {
            case YESTERDAY:
                otherCalendar.add(Calendar.DAY_OF_MONTH, -1);
                dayDeltaPrefix = context.getString(R.string.delta_day_yesterday);
                break;

            case TOMORROW:
            default:
                dayDeltaPrefix = context.getString(R.string.delta_day_tomorrow);
                otherCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        if (location == null)
        {
            // TODO: display error state
            return;
        }

        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);
        Calendar sunriseCalendarToday;
        Calendar sunsetCalendarToday;
        Calendar sunriseCalendarOther;
        Calendar sunsetCalendarOther;

        switch (timeMode)
        {
            case CIVIL:
                sunriseCalendarToday = calculator.getCivilSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getCivilSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getCivilSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getCivilSunsetCalendarForDate(otherCalendar);
                break;

            case NAUTICAL:
                sunriseCalendarToday = calculator.getNauticalSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getNauticalSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getNauticalSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getNauticalSunsetCalendarForDate(otherCalendar);
                break;

            case ASTRONOMICAL:
                sunriseCalendarToday = calculator.getAstronomicalSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getAstronomicalSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getAstronomicalSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getAstronomicalSunsetCalendarForDate(otherCalendar);
                break;

            case OFFICIAL:
            default:
                sunriseCalendarToday = calculator.getOfficialSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getOfficialSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getOfficialSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getOfficialSunsetCalendarForDate(otherCalendar);
                break;
        }

        // update sunrise time
        SuntimesWidgetUtils.TimeDisplayText sunriseString = SuntimesWidgetUtils.calendarTimeShortDisplayString(context, sunriseCalendarToday);
        views.setTextViewText(R.id.text_time_sunrise, sunriseString.getValue());
        views.setTextViewText(R.id.text_time_sunrise_suffix, sunriseString.getSuffix());

        // upset sunset time
        SuntimesWidgetUtils.TimeDisplayText sunsetString = SuntimesWidgetUtils.calendarTimeShortDisplayString(context, sunsetCalendarToday);
        views.setTextViewText(R.id.text_time_sunset, sunsetString.getValue());
        views.setTextViewText(R.id.text_time_sunset_suffix, sunsetString.getSuffix());

        // update sunrise delta
        //String sunriseDeltaString = calendarDeltaShortDisplayString(sunriseCalendarToday, sunriseCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunrise, sunriseDeltaString);

        // update sunset delta
        //String sunsetDeltaString = calendarDeltaShortDisplayString(sunsetCalendarToday, sunsetCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunset, sunsetDeltaString);

        // update day delta
        long dayLengthToday = sunsetCalendarToday.getTimeInMillis() - sunriseCalendarToday.getTimeInMillis();

        long sunriseOther = sunriseCalendarOther.getTime().getTime();
        long sunsetOther = sunsetCalendarOther.getTime().getTime();
        long dayLengthOther = sunsetOther - sunriseOther;

        SuntimesWidgetUtils.TimeDisplayText dayDeltaDisplay = SuntimesWidgetUtils.timeDeltaLongDisplayString(dayLengthToday, dayLengthOther);
        String dayDeltaValue = dayDeltaDisplay.getValue();
        String dayDeltaUnits = dayDeltaDisplay.getUnits();
        String dayDeltaSuffix = dayDeltaDisplay.getSuffix();

        views.setTextViewText(R.id.text_delta_day_prefix, dayDeltaPrefix);
        views.setTextViewText(R.id.text_delta_day_value, dayDeltaValue);
        views.setTextViewText(R.id.text_delta_day_units, dayDeltaUnits);
        views.setTextViewText(R.id.text_delta_day_suffix, dayDeltaSuffix);

        views.setViewVisibility(R.id.text_delta_day_units, (dayDeltaUnits.trim().equals("") ? View.GONE : View.VISIBLE));
        views.setViewVisibility(R.id.text_delta_day_suffix, (dayDeltaSuffix.trim().equals("") ? View.GONE : View.VISIBLE));

        views.setViewVisibility(R.id.text_title, ((showTitle) ? View.VISIBLE : View.GONE));

        // update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


