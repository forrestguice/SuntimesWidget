package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.SuntimesWidgetSettings;

import java.util.Calendar;

public interface SuntimesCalculator
{
    public String name();
    public void init( SuntimesWidgetSettings.Location location, String timezone );

    public Calendar getCivilSunriseCalendarForDate( Calendar date );
    public Calendar getNauticalSunriseCalendarForDate( Calendar date );
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date );
    public Calendar getOfficialSunriseCalendarForDate( Calendar date );

    public Calendar getCivilSunsetCalendarForDate( Calendar date );
    public Calendar getNauticalSunsetCalendarForDate( Calendar date );
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date );
    public Calendar getOfficialSunsetCalendarForDate( Calendar date );
}
