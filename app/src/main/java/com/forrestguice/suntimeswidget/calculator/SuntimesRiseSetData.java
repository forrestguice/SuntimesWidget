/**
    Copyright (C) 2014-2023 Forrest Guice
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
import android.net.Uri;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class SuntimesRiseSetData extends SuntimesData
{
    public SuntimesRiseSetData(Context context, int appWidgetId) {
        initFromSettings(context, appWidgetId);
    }
    public SuntimesRiseSetData(Context context, int appWidgetId, String calculatorName) {
        initFromSettings(context, appWidgetId, calculatorName);
    }
    public SuntimesRiseSetData(SuntimesRiseSetData other) {
        initFromOther(other, other.layoutID);
    }
    public SuntimesRiseSetData(SuntimesRiseSetData other, int layoutID) {
        initFromOther(other, layoutID);
    }

    /**
     * Property: layoutID
     */
    protected int layoutID = R.layout.layout_widget_1x1_0_content;
    public int layoutID()
    {
        return layoutID;
    }
    public void setLayoutID( int id )
    {
        layoutID = id;
    }

    /**
     * Property: time mode
     */
    protected WidgetSettings.TimeMode timeMode;
    public WidgetSettings.TimeMode timeMode()
    {
        return timeMode;
    }
    public void setTimeMode( WidgetSettings.TimeMode mode )
    {
        timeMode = mode;
        angle = null;
    }

    protected WidgetSettings.RiseSetDataMode dataMode;
    public void setDataMode(WidgetSettings.RiseSetDataMode value)
    {
        dataMode = value;
        if (dataMode instanceof WidgetSettings.EventAliasTimeMode)
        {
            EventSettings.EventAlias alias = ((WidgetSettings.EventAliasTimeMode) dataMode).getEvent();
            AlarmEventProvider.ElevationEvent event;
            switch (alias.getType()) {
                case SUN_ELEVATION: event = AlarmEventProvider.SunElevationEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment()); break;
                case SHADOWLENGTH: event = AlarmEventProvider.ShadowLengthEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment()); break;
                default: event = null; break;
            }
            this.angle = (event == null ? null : event.getAngle());
            this.offset = (event == null ? 0 : event.getOffset());
        }
        WidgetSettings.TimeMode mode = dataMode.getTimeMode();
        this.timeMode = ((mode != null) ? mode : WidgetSettings.PREF_DEF_GENERAL_TIMEMODE);
    }
    public WidgetSettings.RiseSetDataMode dataMode() {
        return dataMode;
    }

    /**
     * Property: sun angle (overrides time mode)
     */
    protected Double angle = null;
    public Double angle() {
        return angle;
    }
    public void setAngle( double value ) {
        angle = value;
    }

    /**
     * property: offset
     */
    protected int offset = 0;
    public void setOffset(int millis) {
        offset = millis;
    }
    public int getOffset() {
        return offset;
    }

    /**
     * Property: compare mode
     */
    protected WidgetSettings.CompareMode compareMode;
    public WidgetSettings.CompareMode compareMode()
    {
        return compareMode;
    }
    public void setCompareMode( WidgetSettings.CompareMode mode )
    {
        compareMode = mode;
    }

    /**
     * result: sunrise today
     */
    protected Calendar sunriseCalendarToday;
    public boolean hasSunriseTimeToday()
    {
        return (sunriseCalendarToday != null);
    }
    public Calendar sunriseCalendarToday()
    {
        return sunriseCalendarToday;
    }

    /**
     * result: sunset today
     */
    protected Calendar sunsetCalendarToday;
    public boolean hasSunsetTimeToday()
    {
        return (sunsetCalendarToday != null);
    }
    public Calendar sunsetCalendarToday()
    {
        return sunsetCalendarToday;
    }

    /**
     * result: sunrise other
     */
    protected Calendar sunriseCalendarOther;
    public boolean hasSunriseTimeOther()
    {
        return (sunriseCalendarOther != null);
    }
    public Calendar sunriseCalendarOther()
    {
        return sunriseCalendarOther;
    }

    /**
     * result: sunset other
     */
    protected Calendar sunsetCalendarOther;
    public boolean hasSunsetTimeOther()
    {
        return (sunsetCalendarOther != null);
    }
    public Calendar sunsetCalendarOther()
    {
        return sunsetCalendarOther;
    }

    public Calendar sunriseCalendar(int i)
    {
        if (i == 1)
            return sunriseCalendarToday;
        else return sunriseCalendarOther;
    }

    public Calendar sunsetCalendar(int i)
    {
        if (i == 1)
            return sunsetCalendarToday;
        else return sunsetCalendarOther;
    }

    public Calendar[] getEvents()
    {
        Calendar midnight = midnight();
        midnight.add(Calendar.DAY_OF_MONTH,  1);
        return new Calendar[] { sunriseCalendarToday, sunsetCalendarToday, sunriseCalendarOther, sunsetCalendarOther, midnight };
    }

    public Calendar[] getEvents(boolean isRising)
    {
        if (isRising)
            return new Calendar[] { sunriseCalendarToday, sunriseCalendarOther };
        else return new Calendar[] { sunsetCalendarToday, sunsetCalendarOther };
    }

    /**
     * Property: day delta prefix
     */
    protected String dayDeltaPrefix;
    public String dayDeltaPrefix()
    {
        return dayDeltaPrefix;
    }

    /**
     * Property: day length ("today")
     */
    protected long dayLengthToday = 0L;
    public long dayLengthToday()
    {
        return dayLengthToday;
    }

    /**
     * Property: day length ("other")
     */
    protected long dayLengthOther = 0L;
    public long dayLengthOther()
    {
        return dayLengthOther;
    }

    /**
     * Property: linked data
     */
    protected SuntimesRiseSetData linked = null;
    public SuntimesRiseSetData getLinked()
    {
        return linked;
    }
    public void linkData(SuntimesRiseSetData data)
    {
        linked = data;
    }

    /**
     * @param other another instance of SuntimesRiseSetData
     * @param layoutID an R.layout.someLayoutID to be associated w/ this data
     */
    protected void initFromOther( SuntimesRiseSetData other, int layoutID )
    {
        initFromOther(other);

        this.layoutID = layoutID;
        this.compareMode = other.compareMode();
        this.timeMode = other.timeMode();
        this.angle = other.angle;
        this.offset = other.offset;

        this.sunriseCalendarToday = other.sunriseCalendarToday();
        this.sunsetCalendarToday = other.sunsetCalendarToday();
        this.sunriseCalendarOther = other.sunriseCalendarOther();
        this.sunsetCalendarOther = other.sunsetCalendarOther();
        this.dayLengthToday = other.dayLengthToday();
        this.dayLengthOther = other.dayLengthOther();
        this.dayDeltaPrefix = other.dayDeltaPrefix();
    }

    /**
     * @param context a context used to access shared prefs
     * @param appWidgetId the widgetID to load settings from (0 for app)
     */
    @Override
    protected void initFromSettings(Context context, int appWidgetId, String calculatorName)
    {
        super.initFromSettings(context, appWidgetId, calculatorName);
        setDataMode(WidgetSettings.loadTimeModePref(context, appWidgetId));
        this.compareMode = WidgetSettings.loadCompareModePref(context, appWidgetId);
    }

    public boolean isDay()
    {
        return isDay(Calendar.getInstance(timezone()));
    }
    public boolean isDay(Calendar now)
    {
        if (calculator != null)
        {
            return calculator.isDay(now);
        } else {
            Log.w("isDay", "calculator is null! returning false");
            return false;
        }
    }

    /**
     * Calculate
     * @param context
     */
    @Override
    public void calculate(Context context)
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);
        //Log.v("SuntimesWidgetData", "compare mode: " + compareMode.name());

        initCalculator(context);
        initTimezone(context);

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            todaysCalendar.setTimeInMillis(todayIs.getTimeInMillis());
            otherCalendar.setTimeInMillis(todayIs.getTimeInMillis());
        }

        switch (compareMode)
        {
            case YESTERDAY:
                dayDeltaPrefix = context.getString(R.string.delta_day_yesterday);
                otherCalendar.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case TOMORROW:
            default:
                dayDeltaPrefix = context.getString(R.string.delta_day_tomorrow);
                otherCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        if (angle != null)
        {
            sunriseCalendarToday = calculator.getSunriseCalendarForDate(todaysCalendar, angle);
            sunsetCalendarToday = calculator.getSunsetCalendarForDate(todaysCalendar, angle);
            sunriseCalendarOther = calculator.getSunriseCalendarForDate(otherCalendar, angle);
            sunsetCalendarOther = calculator.getSunsetCalendarForDate(otherCalendar, angle);

        } else {
            switch (timeMode)
            {
                case GOLD:
                    sunriseCalendarToday = calculator.getMorningGoldenHourForDate(todaysCalendar);
                    sunsetCalendarToday = calculator.getEveningGoldenHourForDate(todaysCalendar);
                    sunriseCalendarOther = calculator.getMorningGoldenHourForDate(otherCalendar);
                    sunsetCalendarOther = calculator.getEveningGoldenHourForDate(otherCalendar);
                    break;

                case BLUE8:
                    sunriseCalendarToday = calculator.getMorningBlueHourForDate(todaysCalendar)[0];
                    sunsetCalendarToday = calculator.getEveningBlueHourForDate(todaysCalendar)[1];
                    sunriseCalendarOther = calculator.getMorningBlueHourForDate(otherCalendar)[0];
                    sunsetCalendarOther = calculator.getEveningBlueHourForDate(otherCalendar)[1];
                    break;

                case BLUE4:
                    sunriseCalendarToday = calculator.getMorningBlueHourForDate(todaysCalendar)[1];
                    sunsetCalendarToday = calculator.getEveningBlueHourForDate(todaysCalendar)[0];
                    sunriseCalendarOther = calculator.getMorningBlueHourForDate(otherCalendar)[1];
                    sunsetCalendarOther = calculator.getEveningBlueHourForDate(otherCalendar)[0];
                    break;

                case NOON:
                    sunriseCalendarToday = sunsetCalendarToday = calculator.getSolarNoonCalendarForDate(todaysCalendar);
                    sunriseCalendarOther = sunsetCalendarOther = calculator.getSolarNoonCalendarForDate(otherCalendar);
                    break;

                case MIDNIGHT:
                    sunriseCalendarToday = sunsetCalendarToday = calculator.getSolarMidnightCalendarForDate(todaysCalendar);
                    sunriseCalendarOther = sunsetCalendarOther = calculator.getSolarMidnightCalendarForDate(otherCalendar);
                    break;

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
        }

        if (offset != 0) {
            if (sunriseCalendarToday != null) {
                sunriseCalendarToday.add(Calendar.MILLISECOND, offset);
            }
            if (sunsetCalendarToday != null) {
                sunsetCalendarToday.add(Calendar.MILLISECOND, offset);
            }
            if (sunriseCalendarOther != null) {
                sunriseCalendarOther.add(Calendar.MILLISECOND, offset);
            }
            if (sunsetCalendarOther != null) {
                sunsetCalendarOther.add(Calendar.MILLISECOND, offset);
            }
        }

        dayLengthToday = determineDayLength(sunriseCalendarToday, sunsetCalendarToday);
        dayLengthOther = determineDayLength(sunriseCalendarOther, sunsetCalendarOther);

        super.calculate(context);
    }

    /**
     * @param sunrise
     * @param sunset
     * @return
     */
    protected long determineDayLength(Calendar sunrise, Calendar sunset)
    {
        if (sunrise != null && sunset != null) {
            // average case: rises and sets
            return sunset.getTimeInMillis() - sunrise.getTimeInMillis();

        } else if (sunrise == null && sunset == null) {
            // edge case: no rise or set
            return 0;

        } else if (sunrise != null) {
            // edge case.. rises but doesn't set
            Calendar midnight1 = midnight();
            midnight1.add(Calendar.DAY_OF_YEAR, 1);
            return midnight1.getTimeInMillis() - sunrise.getTimeInMillis();

        } else {
            // edge case.. sets but doesn't rise
            Calendar midnight0 = midnight();
            return sunset.getTimeInMillis() - midnight0.getTimeInMillis();
        }
    }
}


