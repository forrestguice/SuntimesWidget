package com.forrestguice.suntimeswidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.util.Date;
import java.util.GregorianCalendar;
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
        SuntimesWidgetSettings.TimeMode.initDisplayStrings(context);

        final int numWidgets = appWidgetIds.length;
        for (int i = 0; i < numWidgets; i++)
        {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        final int numWidgets = appWidgetIds.length;
        for (int i = 0; i < numWidgets; i++)
        {
            SuntimesWidgetSettings.deletePrefs(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        // functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // functionality for when the last widget is disabled
    }

    public static TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal)
    {
        Date time = cal.getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
        SimpleDateFormat suffixFormat = new SimpleDateFormat("a");

        return new TimeDisplayText( timeFormat.format(time), "", suffixFormat.format(time) );
        //return DateUtils.formatDateTime(context, cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_A);
    }

    static String calendarDeltaShortDisplayString(Calendar c1, Calendar c2)
    {
        return "";  // TODO
    }

    /**
     * @param timeSpan1 first event
     * @param timeSpan2 second event
     * @return a TimeDisplayText object that describes difference between the two spans
     */
    static TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2)
    {
        String value = "";
        String units = "";
        String suffix = "";

        long timeSpan = timeSpan2 - timeSpan1;
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(timeSpan);
        long timeInMillis = d.getTimeInMillis();

        long numberOfSeconds = timeInMillis / 1000;
        suffix += ((numberOfSeconds > 0) ? "longer" : "shorter");
        numberOfSeconds = Math.abs(numberOfSeconds);

        long numberOfMinutes = numberOfSeconds / 60;
        long remainingSeconds = numberOfSeconds % 60;

        value += ((numberOfMinutes < 1) ? "" : numberOfMinutes + "m")
                 + " " +
                 ((remainingSeconds < 1) ? "" : remainingSeconds + "s");

        return new TimeDisplayText(value, units, suffix);
    }

    /**
     * Creates a title string from a given "title pattern".
     *
     * The following substitutions are supported:
     *   %% .. the % character
     *   %m .. the time mode (e.g. nautical twilight / civil twilight / actual time) -> timeMode.getLongDisplayString()
     *   %M .. the time mode (short version) -> timeMode.getShortDisplayString()
     *
     * @param titlePattern a pattern string (simple substitutions)
     * @return a display string suitable for display as a widget title
     */
    private static String displayStringForTitlePattern(String titlePattern, Context context, int appWidgetId)
    {
        SuntimesWidgetSettings.TimeMode timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);

        String displayString = titlePattern;
        String modePattern = "%M";
        String modePatternShort = "%m";
        String percentPattern = "%%";

        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        displayString = displayString.replaceAll(percentPattern, "%");
        return displayString;
    }

    /**
     * @param context the application context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of widget to be updated
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int widgetRows = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        int widgetCols = getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
        RemoteViews views = getWidgetViews(context, widgetRows, widgetCols);

        Location location = SuntimesWidgetSettings.loadLocationPref(context, appWidgetId);
        String timezone = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);

        SuntimesWidgetSettings.TimeMode timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);

        SuntimesWidgetSettings.LocationMode locationMode = SuntimesWidgetSettings.loadLocationModePref(context, appWidgetId);
        if (locationMode == SuntimesWidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            // TODO: get current location
        }

        SuntimesWidgetSettings.TimezoneMode timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        if (timezoneMode == SuntimesWidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
        {
            timezone = TimeZone.getDefault().getID();
        }

        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        String titlePattern = SuntimesWidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = displayStringForTitlePattern(titlePattern, context, appWidgetId);
        views.setTextViewText(R.id.text_title, titleText);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        boolean compareToTomorrow = false;      // TODO: from settings

        Log.v("DEBUG", "rows: " + widgetRows + ", " + "cols: " + widgetCols);
        Log.v("DEBUG", "show title: " + showTitle);
        Log.v("DEBUG", "title text: " + titleText);
        Log.v("DEBUG", "time mode: " + timeMode);
        Log.v("DEBUG", "location_mode: " + locationMode.name());
        Log.v("DEBUG", "latitude: " + location.getLatitude().toPlainString());
        Log.v("DEBUG", "longitude: " + location.getLongitude().toPlainString());
        Log.v("DEBUG", "timezone_mode: " + timezoneMode.name());
        Log.v("DEBUG", "timezone: " + timezone);
        Log.v("DEBUG", "compareToTomorrow: " + compareToTomorrow);

        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, timezone);
        Calendar todaysCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.add(Calendar.DAY_OF_MONTH, ((compareToTomorrow) ? 1 : -1));

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
        TimeDisplayText sunriseString = calendarTimeShortDisplayString(context, sunriseCalendarToday);
        views.setTextViewText(R.id.text_time_sunrise, sunriseString.getValue());
        views.setTextViewText(R.id.text_time_sunrise_suffix, sunriseString.getSuffix());

        // upset sunset time
        TimeDisplayText sunsetString = calendarTimeShortDisplayString(context, sunsetCalendarToday);
        views.setTextViewText(R.id.text_time_sunset, sunsetString.getValue());
        views.setTextViewText(R.id.text_time_sunset_suffix, sunsetString.getSuffix());

        // update sunrise delta
        //String sunriseDeltaString = calendarDeltaShortDisplayString(sunriseCalendarToday, sunriseCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunrise, sunriseDeltaString);

        // update sunset delta
        //String sunsetDeltaString = calendarDeltaShortDisplayString(sunsetCalendarToday, sunsetCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunset, sunsetDeltaString);

        // update day delta
        String dayDeltaPrefix = (compareToTomorrow) ? context.getString(R.string.delta_day_tomorrow) :
                                                      context.getString(R.string.delta_day_yesterday);

        long dayLengthToday = sunsetCalendarToday.getTimeInMillis() - sunriseCalendarToday.getTimeInMillis();
        long dayLengthOther = sunsetCalendarOther.getTimeInMillis() - sunriseCalendarOther.getTimeInMillis();

        TimeDisplayText dayDeltaDisplay = timeDeltaLongDisplayString(dayLengthToday, dayLengthOther);
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

    /**
     * @param context the application context
     * @param rows number of rows in widget
     * @param columns number of cols in widget
     * @return a RemoteViews instance for the specified widget size
     */
    private static RemoteViews getWidgetViews(Context context, int rows, int columns)
    {
        if (columns >= 3)
        {
            return new RemoteViews(context.getPackageName(), R.layout.layout_sunwidget1x3);
        }

        if (columns >= 2)
        {
            return new RemoteViews(context.getPackageName(), R.layout.layout_sunwidget1x2);
        }

        return new RemoteViews(context.getPackageName(), R.layout.layout_sunwidget1x1);
    }


    private static int getCellsForSize( int specifiedSize )
    {
        int numCells = 1;
        while (getSizeForCells(numCells) < specifiedSize)
        {
            numCells++;
        }
        return numCells-1;
    }

    private static int getSizeForCells( int numCells )
    {
        return (70 * numCells) - 30;
    }

    /**
     * TimeDisplayText : class
     */
    public static class TimeDisplayText
    {
        private String value;
        private String units;
        private String suffix;

        public TimeDisplayText(String value, String units, String suffix)
        {
            this.value = value;
            this.units = units;
            this.suffix = suffix;
        }

        public String getValue()
        {
            return value;
        }

        public String getUnits()
        {
            return units;
        }

        public String getSuffix()
        {
            return suffix;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(value);
            s.append(" ");
            s.append(units);
            s.append(" ");
            s.append(suffix);
            return s.toString();
        }
    }

}


