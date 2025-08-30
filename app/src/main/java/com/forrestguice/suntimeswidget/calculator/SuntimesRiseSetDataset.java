/**
    Copyright (C) 2014-2022 Forrest Guice
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

import android.content.Context;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class SuntimesRiseSetDataset
{
    public SuntimesRiseSetData dataActual;
    public SuntimesRiseSetData dataCivil;
    public SuntimesRiseSetData dataNautical;
    public SuntimesRiseSetData dataAstro;
    public SuntimesRiseSetData dataNoon;
    public SuntimesRiseSetData dataMidnight;
    public SuntimesRiseSetData dataGold;
    public SuntimesRiseSetData dataBlue8;
    public SuntimesRiseSetData dataBlue4;

    private HashMap<String, SuntimesRiseSetData> dataset = new HashMap<>();
    public SuntimesRiseSetData getData(String id) {
        return dataset.get(id);
    }
    public void putData(String id, SuntimesRiseSetData data) {
        dataset.put(id, data);
    }
    public Collection<String> getDataModes() {
        return dataset.keySet();
    }
    public int size() {
        return dataset.size();
    }

    public SuntimesRiseSetDataset(Context context)
    {
        init(context, 0);
    }

    public SuntimesRiseSetDataset(Context context, int appWidgetID)
    {
        init(context, appWidgetID);
    }

    public SuntimesRiseSetDataset(@NonNull SuntimesRiseSetDataset other) {
        this(other, WidgetSettings.TimeMode.values());
    }

    public SuntimesRiseSetDataset(@NonNull SuntimesRiseSetDataset other, WidgetSettings.TimeMode[] modes)
    {
        this.calculator = other.calculator;
        this.calculatorDescriptor = other.calculatorDescriptor;

        for (WidgetSettings.TimeMode mode : modes)
        {
            switch (mode)
            {
                case OFFICIAL: dataset.put(WidgetSettings.TimeMode.OFFICIAL.name(), this.dataActual = new SuntimesRiseSetData(other.dataActual)); break;
                case CIVIL: dataset.put(WidgetSettings.TimeMode.CIVIL.name(), this.dataCivil = new SuntimesRiseSetData(other.dataCivil)); break;
                case NAUTICAL: dataset.put(WidgetSettings.TimeMode.NAUTICAL.name(), this.dataNautical = new SuntimesRiseSetData(other.dataNautical)); break;
                case ASTRONOMICAL: dataset.put(WidgetSettings.TimeMode.ASTRONOMICAL.name(), this.dataAstro = new SuntimesRiseSetData(other.dataAstro)); break;
                case NOON: dataset.put(WidgetSettings.TimeMode.NOON.name(), this.dataNoon = new SuntimesRiseSetData(other.dataNoon)); break;
                case MIDNIGHT: dataset.put(WidgetSettings.TimeMode.MIDNIGHT.name(), this.dataMidnight = new SuntimesRiseSetData(other.dataMidnight)); break;
                case GOLD: dataset.put(WidgetSettings.TimeMode.GOLD.name(), this.dataGold = new SuntimesRiseSetData(other.dataGold)); break;
                case BLUE8: dataset.put(WidgetSettings.TimeMode.BLUE8.name(), this.dataBlue8 = new SuntimesRiseSetData(other.dataBlue8)); break;
                case BLUE4: dataset.put(WidgetSettings.TimeMode.BLUE4.name(), this.dataBlue4 = new SuntimesRiseSetData(other.dataBlue4)); break;
            }
        }
    }

    private void init(Context context, int appWidgetID)
    {
        dataActual = new SuntimesRiseSetData(context, appWidgetID);
        dataActual.setCompareMode(WidgetSettings.CompareMode.TOMORROW);
        dataActual.setTimeMode(WidgetSettings.TimeMode.OFFICIAL);
        dataset.put(WidgetSettings.TimeMode.OFFICIAL.name(), dataActual);

        dataCivil = new SuntimesRiseSetData(dataActual);
        dataCivil.setTimeMode(WidgetSettings.TimeMode.CIVIL);
        dataset.put(WidgetSettings.TimeMode.CIVIL.name(), dataCivil);

        dataNautical = new SuntimesRiseSetData(dataActual);
        dataNautical.setTimeMode(WidgetSettings.TimeMode.NAUTICAL);
        dataset.put(WidgetSettings.TimeMode.NAUTICAL.name(), dataNautical);

        dataAstro = new SuntimesRiseSetData(dataActual);
        dataAstro.setTimeMode(WidgetSettings.TimeMode.ASTRONOMICAL);
        dataset.put(WidgetSettings.TimeMode.ASTRONOMICAL.name(), dataAstro);

        dataNoon = new SuntimesRiseSetData(dataActual);
        dataNoon.setTimeMode(WidgetSettings.TimeMode.NOON);
        dataset.put(WidgetSettings.TimeMode.NOON.name(), dataNoon);

        dataMidnight = new SuntimesRiseSetData(dataActual);
        dataMidnight.setTimeMode(WidgetSettings.TimeMode.MIDNIGHT);
        dataset.put(WidgetSettings.TimeMode.MIDNIGHT.name(), dataMidnight);

        dataGold = new SuntimesRiseSetData(dataActual);
        dataGold.setTimeMode(WidgetSettings.TimeMode.GOLD);
        dataset.put(WidgetSettings.TimeMode.GOLD.name(), dataGold);

        dataBlue8 = new SuntimesRiseSetData(dataActual);
        dataBlue8.setTimeMode(WidgetSettings.TimeMode.BLUE8);
        dataset.put(WidgetSettings.TimeMode.BLUE8.name(), dataBlue8);

        dataBlue4 = new SuntimesRiseSetData(dataActual);
        dataBlue4.setTimeMode(WidgetSettings.TimeMode.BLUE4);
        dataset.put(WidgetSettings.TimeMode.BLUE4.name(), dataBlue4);
    }

    public void calculateData(Context context)
    {
        SuntimesCalculator calculator = this.calculator;
        SuntimesCalculatorDescriptor descriptor = this.calculatorDescriptor;

        boolean first = true;
        for (SuntimesRiseSetData data : dataset.values())
        {
            if (first && descriptor == null)
            {
                data.calculate(context);
                calculator = data.calculator();
                descriptor = data.calculatorMode();
                first = false;

            } else {
                data.setCalculator(calculator, descriptor);
                data.calculate(context);
            }
        }

        makeDayLengthCorrections(this, calculator, false);
        makeDayLengthCorrections(this, calculator, true);
    }

    public boolean isCalculated()
    {
        return dataActual.isCalculated();
    }

    public void invalidateCalculation()
    {
        for (SuntimesRiseSetData data : dataset.values() )
        {
            data.invalidateCalculation();
        }
    }

    public static class SearchResult
    {
        private Calendar calendar;
        private boolean isRising;
        private WidgetSettings.RiseSetDataMode mode;

        public SearchResult(WidgetSettings.RiseSetDataMode mode, Calendar calendar, boolean isRising)
        {
            this.mode = mode;
            this.calendar = calendar;
            this.isRising = isRising;
        }
        public Calendar getCalendar() {
            return calendar;
        }
        public boolean isRising() {
            return isRising;
        }
        public WidgetSettings.RiseSetDataMode getMode() {
            return mode;
        }
    }

    public SearchResult findNextEvent()
    {
        Calendar now = now();
        long nearestTime = -1;
        boolean isRising = false;
        WidgetSettings.RiseSetDataMode mode = null;

        Collection<SuntimesRiseSetData> values = dataset.values();
        Calendar nearest = values.toArray(new SuntimesRiseSetData[0])[0].sunriseCalendarToday();
        for (SuntimesRiseSetData data : values)
        {
            Calendar[] events = new Calendar[] { data.sunriseCalendarToday(), data.sunsetCalendarToday(),
                                                 data.sunriseCalendarOther(), data.sunsetCalendarOther() };
            for (int i=0; i<events.length; i++)
            {
                Calendar event = events[i];
                if (event != null)
                {
                    long timeUntil = event.getTime().getTime() - now.getTime().getTime();
                    if ((timeUntil > 0 && timeUntil < nearestTime) || nearestTime < 0)
                    {
                        nearestTime = timeUntil;
                        nearest = event;
                        isRising = (i % 2 == 0);
                        mode = ((data.dataMode() != null) ? data.dataMode() : data.timeMode());
                        //Log.d("DEBUG", "findNextEvent: mode is: " + mode);
                    }
                }
            }
        }
        return new SearchResult(mode, nearest, isRising);
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

    public Location location()
    {
        return dataActual.location();
    }
    public void setLocation(Location location)
    {
        for (SuntimesRiseSetData data : dataset.values() ) {
            data.setLocation(location);
        }
    }

    public void setTodayIs(Calendar date)
    {
        for (SuntimesRiseSetData data : dataset.values() ) {
            data.setTodayIs(date);
        }
    }

    public TimeZone timezone() {
        return dataActual.timezone();
    }
    public void setTimeZone(Context context, TimeZone value) {
        for (SuntimesRiseSetData data : dataset.values()) {
            data.setTimeZoneMode(WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
            data.setTimezone(value);
            data.calculator = null;   // reset calculator because it may require re-initialization w/ timezone
        }
    }

    public Date date()
    {
        return dataActual.date();
    }

    public Calendar calendar()
    {
        return dataActual.calendar();
    }

    public Calendar otherCalendar()
    {
        return dataActual.getOtherCalendar();
    }

    public WidgetSettings.TimezoneMode timezoneMode() {
        return dataActual.timezoneMode();
    }
    public void setTimeZoneMode(WidgetSettings.TimezoneMode value) {
        for (SuntimesRiseSetData data : dataset.values()) {
            data.setTimeZoneMode(value);
            data.calculator = null;   // reset calculator because it may require re-initialization w/ timezone
        }
    }

    public SuntimesCalculator calculator() {
        return (calculator != null ? calculator : dataActual.calculator());
    }
    public SuntimesCalculatorDescriptor calculatorMode() {
        return (calculatorDescriptor != null ? calculatorDescriptor: dataActual.calculatorMode());
    }

    public void setCalculator(Context context, SuntimesCalculatorDescriptor value)
    {
        this.calculatorDescriptor = value;
        this.calculator = new SuntimesCalculatorFactory(value).createCalculator(location(), timezone());
    }
    public void setCalculator(Context context, SuntimesCalculatorDescriptor calculatorDescriptor, SuntimesCalculator calculator) {
        this.calculatorDescriptor = calculatorDescriptor;
        this.calculator = calculator;
    }
    protected SuntimesCalculator calculator;
    protected SuntimesCalculatorDescriptor calculatorDescriptor;

    public Calendar now()
    {
        return dataActual.now();
    }

    public Calendar nowThen(Calendar date)
    {
        return dataActual.nowThen(date);
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
        Calendar endSet;

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
        Calendar startRise;

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

    /**
     * @param position current sunPosition
     * @param noonPosition the sunPosition at noon
     * @return true if rising (before noon) or if either position or noonPosition is null, false if setting (on or after noon)
     */
    public static boolean isRising(SuntimesCalculator.SunPosition position, SuntimesCalculator.SunPosition noonPosition)
    {
        if (position == null || noonPosition == null) {
            return true;

        } else if (noonPosition.azimuth > 90 && noonPosition.azimuth < 270) {    // noon is southward
            return (position.azimuth < noonPosition.azimuth);

        } else {                                                      // noon is northward
            if (noonPosition.azimuth <= 90)
                return (position.azimuth > noonPosition.azimuth && position.azimuth <= 180);
            else return (position.azimuth > noonPosition.azimuth) || (position.azimuth <= 90);
        }
    }

    public Calendar[] getRiseSetEvents(String eventID)
    {
        //Log.d("DEBUG", "getRiseSetEvents: " + eventID);
        SuntimesRiseSetData d;
        if (SolarEvents.hasValue(eventID))
        {
            try {
                SolarEvents event = SolarEvents.valueOf(eventID);
                d = getData(SolarEvents.toTimeMode(eventID).name());
                if (d != null) {
                    return d.getEvents(event.isRising());
                } else return new Calendar[] { null, null };

            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w(getClass().getSimpleName(), "getRisSetEvents: " + e);
            }
        }

        //Log.d("DEBUG", "getRiseSetEvents: EventAlias: " + eventID);
        boolean isRising = eventID.endsWith(AlarmEventProvider.SunElevationEvent.SUFFIX_RISING);
        if (eventID.endsWith("_" + AlarmEventProvider.SunElevationEvent.SUFFIX_RISING) || eventID.endsWith("_" + AlarmEventProvider.SunElevationEvent.SUFFIX_SETTING)) {
            eventID = eventID.substring(0, eventID.lastIndexOf("_"));
        }
        d = getData(eventID);

        if (d != null) {
            return d.getEvents(isRising);
        } else return new Calendar[] { null, null };
    }

    @Override
    public String toString()
    {
        return "" + date().getTime();
    }

    public static int NONE_DAY = 0;      // no daylength because the sun doesn't set lower
    public static int NONE_NIGHT = -1;    // no daylength because the sun doesn't rise higher

    public static void makeDayLengthCorrections(SuntimesRiseSetDataset data, SuntimesCalculator calculator, boolean isOther)
    {
        SuntimesRiseSetData dataActual = data.dataActual;
        SuntimesRiseSetData dataCivil = data.dataCivil;
        SuntimesRiseSetData dataNautical = data.dataNautical;
        SuntimesRiseSetData dataAstro = data.dataAstro;

        Calendar calendar = (isOther ? dataActual.getOtherCalendar() : dataActual.calendar());
        Calendar midnight = (calculator != null ? calculator.getSolarMidnightCalendarForDate(calendar) : null);
        SuntimesCalculator.SunPosition atMidnight = (calculator != null && midnight != null ? calculator.getSunPosition(midnight) : null);
        if (atMidnight != null)
        {
            if (atMidnight.elevation > -2) {                              // [0, ...]
                if (dayLengthIs(dataActual, isOther, 0)) {
                    setDayLength(dataActual, isOther, SuntimesData.DAY_MILLIS);     // polar day (midnight sun)
                    setDayLength(dataCivil, isOther, NONE_DAY);
                    setDayLength(dataNautical, isOther, NONE_DAY);
                    setDayLength(dataAstro, isOther, NONE_DAY);
                }

            } else if (atMidnight.elevation >= -6) {                       // (-6, 0]
                if (dayLengthIs(dataCivil, isOther, 0)) {
                    setDayLength(dataCivil, isOther, SuntimesData.DAY_MILLIS);       // midnight twilight
                    setDayLength(dataNautical, isOther, NONE_DAY);
                    setDayLength(dataAstro, isOther, NONE_DAY);
                }

            } else if (atMidnight.elevation >= -12) {                      // (-12, -6]
                if (dayLengthIs(dataNautical, isOther, 0)) {
                    setDayLength(dataNautical, isOther, SuntimesData.DAY_MILLIS);    // midnight twilight
                    setDayLength(dataAstro, isOther, NONE_DAY);
                }

            } else if (atMidnight.elevation >= -18) {                      // (-18, 12]
                if (dayLengthIs(dataAstro, isOther, 0)) {
                    setDayLength(dataAstro, isOther, SuntimesData.DAY_MILLIS);       // midnight twilight
                }
            }
        }

        Calendar noon = (calculator != null ? calculator.getSolarNoonCalendarForDate(calendar) : null);
        SuntimesCalculator.SunPosition atNoon = (calculator != null && noon != null ? calculator.getSunPosition(noon) : null);
        if (atNoon != null)
        {
            if (atNoon.elevation <= 0) {                                             // polar night
                if (dayLengthIs(dataActual, isOther, 0)) {
                    setDayLength(dataActual, isOther, NONE_NIGHT);
                }

                if (atNoon.elevation < -6) {
                    if (dayLengthIs(dataCivil, isOther, 0)) {
                        setDayLength(dataCivil, isOther, NONE_NIGHT);
                    }

                    if (atNoon.elevation < -12) {
                        if (dayLengthIs(dataNautical, isOther, 0)) {
                            setDayLength(dataNautical, isOther, NONE_NIGHT);
                        }

                        if (atNoon.elevation < -18 && dayLengthIs(dataAstro, isOther, 0)) {
                            setDayLength(dataAstro, isOther, NONE_NIGHT);
                        }
                    }
                }
            }
        }
    }

    public static boolean dayLengthIs(SuntimesRiseSetData data,  boolean other, long length) {
        return other ? (data.dayLengthOther == length)
                : (data.dayLengthToday == length);
    }
    public static void setDayLength(SuntimesRiseSetData data, boolean other, long length)
    {
        if (other) {
            data.dayLengthOther = length;
        } else {
            data.dayLengthToday = length;
        }
    }

}