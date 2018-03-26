/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesRiseSetDataset
{
    public SuntimesRiseSetData dataActual;
    public SuntimesRiseSetData dataCivil;
    public SuntimesRiseSetData dataNautical;
    public SuntimesRiseSetData dataAstro;
    public SuntimesRiseSetData dataNoon;
    public SuntimesRiseSetData dataGold;
    public SuntimesRiseSetData dataBlue8;
    public SuntimesRiseSetData dataBlue4;
    private ArrayList<SuntimesRiseSetData> dataset = new ArrayList<>();

    public SuntimesRiseSetDataset(Context context)
    {
        dataActual = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        dataActual.setCompareMode(WidgetSettings.CompareMode.TOMORROW);
        dataActual.setTimeMode(WidgetSettings.TimeMode.OFFICIAL);
        dataset.add(dataActual);

        dataCivil = new SuntimesRiseSetData(dataActual);
        dataCivil.setTimeMode(WidgetSettings.TimeMode.CIVIL);
        dataset.add(dataCivil);

        dataNautical = new SuntimesRiseSetData(dataActual);
        dataNautical.setTimeMode(WidgetSettings.TimeMode.NAUTICAL);
        dataset.add(dataNautical);

        dataAstro = new SuntimesRiseSetData(dataActual);
        dataAstro.setTimeMode(WidgetSettings.TimeMode.ASTRONOMICAL);
        dataset.add(dataAstro);

        dataNoon = new SuntimesRiseSetData(dataActual);
        dataNoon.setTimeMode(WidgetSettings.TimeMode.NOON);
        dataset.add(dataNoon);

        dataGold = new SuntimesRiseSetData(dataActual);
        dataGold.setTimeMode(WidgetSettings.TimeMode.GOLD);
        dataset.add(dataGold);

        dataBlue8 = new SuntimesRiseSetData(dataActual);
        dataBlue8.setTimeMode(WidgetSettings.TimeMode.BLUE8);
        dataset.add(dataBlue8);

        dataBlue4 = new SuntimesRiseSetData(dataActual);
        dataBlue4.setTimeMode(WidgetSettings.TimeMode.BLUE4);
        dataset.add(dataBlue4);
    }

    public void calculateData()
    {
        for (SuntimesRiseSetData data : dataset )
        {
            data.calculate();
        }
    }

    public boolean isCalculated()
    {
        return dataActual.isCalculated();
    }

    public void invalidateCalculation()
    {
        for (SuntimesRiseSetData data : dataset )
        {
            data.invalidateCalculation();
        }
    }

    public Calendar findNextEvent()
    {
        Calendar now = now();
        long nearestTime = -1;

        Calendar nearest = dataset.get(0).sunriseCalendarToday();
        for (SuntimesRiseSetData data : dataset)
        {
            Calendar[] events = new Calendar[] { data.sunriseCalendarToday(), data.sunriseCalendarOther(),
                                                 data.sunsetCalendarToday(), data.sunsetCalendarOther() };
            for (Calendar event : events)
            {
                if (event != null)
                {
                    long timeUntil = event.getTime().getTime() - now.getTime().getTime();
                    if ((timeUntil > 0 && timeUntil < nearestTime) || nearestTime < 0)
                    {
                        nearestTime = timeUntil;
                        nearest = event;
                    }
                }
            }
        }
        return nearest;
    }

    public Calendar todayIs()
    {
        return dataActual.todayIs();
    }

    public boolean todayIsNotToday()
    {
        return dataActual.todayIsNotToday();
    }

    public boolean isNight()
    {
        return isNight(this.now());
    }

    public boolean isNight( Calendar dateTime )
    {
        Date time = dateTime.getTime();
        Date sunrise = dataActual.sunriseCalendarToday().getTime();
        Date sunsetAstroTwilight = dataAstro.sunsetCalendarToday().getTime();
        return (time.before(sunrise) || time.after(sunsetAstroTwilight));
    }

    public boolean isDay()
    {
        return isDay(this.now());
    }
    public boolean isDay(Calendar dateTime)
    {
        if (dataActual.calculator == null)
        {
            Calendar sunsetCal = dataActual.sunsetCalendarToday();
            if (sunsetCal == null)    // no sunset time, must be day
                return true;

            Calendar sunriseCal = dataActual.sunriseCalendarToday();
            if (sunriseCal == null)   // no sunrise time, must be night
                return false;

            Date time = dateTime.getTime();
            Date sunrise = sunriseCal.getTime();
            Date sunset = sunsetCal.getTime();
            return (time.after(sunrise) && time.before(sunset));

        } else {
            return dataActual.isDay(dateTime);
        }
    }

    public WidgetSettings.Location location()
    {
        return dataActual.location();
    }

    public TimeZone timezone()
    {
        return dataActual.timezone();
    }

    public Date date()
    {
        return dataActual.date();
    }

    public WidgetSettings.TimezoneMode timezoneMode()
    {
        return dataActual.timezoneMode();
    }

    public SuntimesCalculatorDescriptor calculatorMode()
    {
        return dataActual.calculatorMode();
    }

    public Calendar now()
    {
        return Calendar.getInstance(timezone());
    }

    public static Calendar midnight(Calendar date)
    {
        Calendar midnight = (Calendar)date.clone();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        return midnight;
    }

    public long nightLength()
    {
        Calendar astroSet = dataAstro.sunsetCalendarToday();
        Calendar astroRise = dataAstro.sunriseCalendarOther();
        if (astroRise == null || astroSet == null)
            return 0;
        else return astroRise.getTimeInMillis() - astroSet.getTimeInMillis();
    }

    public long[] astroTwilightLength()
    {
        long[] durations = new long[2];
        durations[0] = morningTwilightLength(dataAstro, dataNautical);
        durations[1] = eveningTwilightLength(dataNautical, dataAstro);
        return durations;
    }

    public long[] nauticalTwilightLength()
    {
        long[] durations = new long[2];
        durations[0] = morningTwilightLength(dataNautical, dataCivil);
        durations[1] = eveningTwilightLength(dataCivil, dataNautical);
        return durations;
    }

    public long[] civilTwilightLength()
    {
        long[] durations = new long[2];
        durations[0] = morningTwilightLength(dataCivil, dataActual);
        durations[1] = eveningTwilightLength(dataActual, dataCivil);
        return durations;
    }

    public long dayLength()
    {
        return dataActual.dayLengthToday();
    }

    public long dayLengthOther()
    {
        return dataActual.dayLengthOther();
    }

    /**
     * @param data0 data for this twilight (e.g. nautical)
     * @param data1 data for next twilight (e.g. civil)
     * @return the (morning) duration of this twilight
     */
    public static long morningTwilightLength(SuntimesRiseSetData data0, SuntimesRiseSetData data1)
    {
        Calendar startRise = data0.sunriseCalendarToday();  // twilight is starting (rising)
        Calendar endRise = data1.sunriseCalendarToday();    // twilight is ending (rising next twilight)
        Calendar endSet = null;                             // twilight is ending (setting)

        if (startRise != null && endRise != null)
        {
            return endRise.getTimeInMillis() - startRise.getTimeInMillis();                // avg:  >..N../  T  /---D---\  T  \..N..<
                                                                                           //            twilight rising to next
        } else if (startRise != null) {
            endSet = data0.sunsetCalendarToday();
            if (endSet != null)
            {
                return endSet.getTimeInMillis() - startRise.getTimeInMillis();          // special: >..N..../  T   \.....N..<
                                                                                        //               twilight is peak (rising today / setting today)
            } else {
                endRise = data1.sunriseCalendarOther();
                if (endRise != null)
                {
                    return endRise.getTimeInMillis() - startRise.getTimeInMillis();     // special: >..N...../  T   <>   /----D----<
                                                                                        //             twilight straddles day (rising to next tomorrow)
                } else {
                    endSet = data0.sunsetCalendarOther();
                    if (endSet != null)
                    {
                        return endSet.getTimeInMillis() - startRise.getTimeInMillis();  // special: >..N...../  T   <>   \......N..<
                                                                                        //              twilight is peak (rising today / setting tomorrow)
                    } else {
                        return 0;                                                       // unknown: >..N...../  T   <>             <
                    }                                                                   //              twilight is peak (but does not set tomorrow)
                }
            }
        //} else if (endRise != null) {
        //    return endRise.getTimeInMillis() - midnight(endRise).getTimeInMillis();     // special: >   T       /---D---\       T   >
                                                                                        //              twilight starts w/ day (rose yesterday, rising to next today)
        } else {
            return 0;                                                                   // unknown: >.................<
        }                                                                               //              twilight DNE (no rise or set times)
    }

    /**
     * @param data0 data for prev twilight (e.g. civil)
     * @param data1 data for this twilight (e.g. nautical)
     * @return the (evening) duration of this twilight
     */
    public static long eveningTwilightLength(SuntimesRiseSetData data0, SuntimesRiseSetData data1)
    {
        Calendar startSet = data0.sunsetCalendarToday();  // civil
        Calendar endSet = data1.sunsetCalendarToday();    // nautical
        Calendar startRise = null;

        if (startSet != null && endSet != null)
        {
            return endSet.getTimeInMillis() - startSet.getTimeInMillis();                  // avg:  >..N../  T  /---D---\  T  \..N..<

        } else if (startSet != null) {
            startRise = data0.sunriseCalendarOther();
            if (startRise != null)
            {
                return startRise.getTimeInMillis() - startSet.getTimeInMillis();        // special: >   T       /---D---\       T   >
                                                                                        //              twilight ends w/ day (no night)
            } else {
                return 0;
            }

        } else {
            return 0;                                                                   //            twilight setting to next
        }
    }

}


