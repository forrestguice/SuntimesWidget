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

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class SuntimesRiseSetData extends SuntimesData
{
    private Context context;
    private Calendar[] calendar = {null, null, null};
    private Calendar[] sunrise = {null, null, null};
    private Calendar[] sunset = {null, null, null};
    private Calendar[] daylength = {null, null, null};

    public SuntimesRiseSetData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesRiseSetData(Context context, int appWidgetId, String calculatorName)
    {
        this.context = context;
        initFromSettings(context, appWidgetId, calculatorName);
    }
    public SuntimesRiseSetData(SuntimesRiseSetData other)
    {
        this.context = other.context;
        initFromOther(other, other.layoutID);
    }
    public SuntimesRiseSetData(SuntimesRiseSetData other, int layoutID)
    {
        this.context = other.context;
        initFromOther(other, layoutID);
    }

    /**
     * Property: layoutID
     */
    private int layoutID = R.layout.layout_widget_1x1_0i;
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
    private WidgetSettings.TimeMode timeMode;
    public WidgetSettings.TimeMode timeMode()
    {
        return timeMode;
    }
    public void setTimeMode( WidgetSettings.TimeMode mode )
    {
        timeMode = mode;
    }

    /**
     * Property: compare mode
     */
    private WidgetSettings.CompareMode compareMode;
    public WidgetSettings.CompareMode compareMode()
    {
        return compareMode;
    }
    public void setCompareMode( WidgetSettings.CompareMode mode )
    {
        compareMode = mode;
    }

    protected int indexOfOther()
    {
        return (compareMode == WidgetSettings.CompareMode.TOMORROW ? 2 : 0);
    }

    /**
     * result: sunrise today
     */
    public boolean hasSunriseTimeToday()
    {
        return (sunrise[1] != null);
    }
    public Calendar sunriseCalendarToday()
    {
        return sunrise[1];
    }

    /**
     * result: sunset today
     */
    public boolean hasSunsetTimeToday()
    {
        return (sunset[1] != null);
    }
    public Calendar sunsetCalendarToday()
    {
        return sunset[1];
    }

    /**
     * result: sunrise other
     */
    public boolean hasSunriseTimeOther()
    {
        return (sunrise[indexOfOther()] != null);
    }
    public Calendar sunriseCalendarOther()
    {
        return sunrise[indexOfOther()];
    }

    /**
     * result: sunset other
     */
    public boolean hasSunsetTimeOther()
    {
        return (sunset[indexOfOther()] != null);
    }
    public Calendar sunsetCalendarOther()
    {
        return sunset[indexOfOther()];
    }

    /**
     * Property: day delta prefix
     */
    private String dayDeltaPrefix;
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
    private SuntimesRiseSetData linked = null;
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

        this.dayLengthToday = other.dayLengthToday();
        this.dayLengthOther = other.dayLengthOther();
        this.dayDeltaPrefix = other.dayDeltaPrefix();

        for (int i=0; i<calendar.length; i++)
        {
            this.calendar[i] = other.calendar[i];
            this.sunrise[i] = other.sunrise[i];
            this.sunset[i] = other.sunset[i];
            this.daylength[i] = other.daylength[i];
        }
    }

    /**
     * @param context a context used to access shared prefs
     * @param appWidgetId the widgetID to load settings from (0 for app)
     */
    @Override
    protected void initFromSettings(Context context, int appWidgetId, String calculatorName)
    {
        super.initFromSettings(context, appWidgetId, calculatorName);
        this.timeMode = WidgetSettings.loadTimeModePref(context, appWidgetId);
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

    public void initCalculator()
    {
        initCalculator(context);
    }

    /**
     * Calculate
     */
    @Override
    public void calculate()
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);
        //Log.v("SuntimesWidgetData", "compare mode: " + compareMode.name());

        initCalculator(context);

        for (int i=0; i<calendar.length; i++)
        {
            calendar[i] = Calendar.getInstance(timezone);
        }
        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            int year = todayIs.get(Calendar.YEAR);
            int month = todayIs.get(Calendar.MONTH);
            int day = todayIs.get(Calendar.DAY_OF_MONTH);
            for (int i=0; i<calendar.length; i++)
            {
                calendar[i].set(year, month, day);
            }
            todaysCalendar.set(year, month, day);
            otherCalendar.set(year, month, day);
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
        calendar[0].add(Calendar.DAY_OF_MONTH, -1);
        calendar[2].add(Calendar.DAY_OF_MONTH, 1);

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        for (int i=0; i<calendar.length; i++)
        {
            switch (timeMode)
            {
                case GOLD:
                    sunrise[i] = calculator.getMorningGoldenHourForDate(calendar[i]);
                    sunset[i] = calculator.getEveningGoldenHourForDate(calendar[i]);
                    break;

                case BLUE8:
                    sunrise[i] = calculator.getMorningBlueHourForDate(calendar[i])[0];
                    sunset[i] = calculator.getEveningBlueHourForDate(calendar[i])[1];
                    break;

                case BLUE4:
                    sunrise[i] = calculator.getMorningBlueHourForDate(calendar[i])[1];
                    sunset[i] = calculator.getEveningBlueHourForDate(calendar[i])[0];
                    break;

                case NOON:
                    sunrise[i] = sunset[i] = calculator.getSolarNoonCalendarForDate(calendar[i]);
                    break;

                case CIVIL:
                    sunrise[i] = calculator.getCivilSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getCivilSunsetCalendarForDate(calendar[i]);
                    break;

                case NAUTICAL:
                    sunrise[i] = calculator.getNauticalSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getNauticalSunsetCalendarForDate(calendar[i]);
                    break;

                case ASTRONOMICAL:
                    sunrise[i] = calculator.getAstronomicalSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getAstronomicalSunsetCalendarForDate(calendar[i]);
                    break;

                case OFFICIAL:
                default:
                    sunrise[i] = calculator.getOfficialSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getOfficialSunsetCalendarForDate(calendar[i]);
                    break;
            }
        }

        int i = indexOfOther();
        dayLengthToday = determineDayLength(sunrise[1], sunset[1]);
        dayLengthOther = determineDayLength(sunrise[i], sunset[i]);

        super.calculate();
    }

    /**
     * @param sunrise
     * @param sunset
     * @return
     */
    private long determineDayLength(Calendar sunrise, Calendar sunset)
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


