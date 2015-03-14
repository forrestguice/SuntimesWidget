package com.forrestguice.suntimeswidget.calculator.sunrisesunset_java;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import java.util.Calendar;
import com.forrestguice.suntimeswidget.SuntimesWidgetSettings;

public class SunriseSunsetSuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "sunrisesunsetlib";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator";
    SunriseSunsetCalculator calculator = null;

    public SunriseSunsetSuntimesCalculator()
    {
    }

    public void init(SuntimesWidgetSettings.Location locationSetting, String timezone)
    {
        Location location = new Location(locationSetting.getLatitude(), locationSetting.getLongitude());
        calculator = new SunriseSunsetCalculator(location, timezone);
    }

    public String name() {
        return NAME;
    }

    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunriseCalendarForDate(date);
    }

    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
         return calculator.getNauticalSunriseCalendarForDate(date);
    }

    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunriseCalendarForDate(date);
    }

    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunriseCalendarForDate(date);
    }



    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunsetCalendarForDate(date);
    }

    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getNauticalSunsetCalendarForDate(date);
    }

    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunsetCalendarForDate(date);
    }

    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunsetCalendarForDate(date);
    }

}

