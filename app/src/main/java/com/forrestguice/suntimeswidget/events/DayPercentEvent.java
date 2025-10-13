/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.support.annotation.NonNull;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public final class DayPercentEvent extends ElevationEvent
{
    public static final String NAME_PREFIX = "DAYPERCENT_";

    /**
     * @param percent percent relative to start/end of the day (+ percentages are after sunrise, before sunset; - percentages are before sunrise, after sunset).
     * @param offset time offset in milliseconds
     * @param isRising rising vs setting time (percent relative to sunrise vs percent relative to sunset)
     */
    public DayPercentEvent(double percent, int offset, boolean isRising)
    {
        super(0, offset, isRising);
        this.percent = percent;
        this.angle = (percent / 100d / 2d) * 90;    // angle is used solely to sort values in a list of ElevationEvents
    }

    protected double percent;
    public double getPercentValue() {
        return percent;
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        String eventTitle = context.getResources().getString(percent >= 0 ? R.string.daypercentevent_title_day : R.string.daypercentevent_title_night);
        return offsetDisplay(context.getResources()) + eventTitle + " " + (rising ? "rising" : "setting") + " (" + percent + ")";   // TODO: format
    }
    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        String eventTitle = context.getResources().getString(percent >= 0 ? R.string.daypercentevent_title_day : R.string.daypercentevent_title_night);
        return offsetDisplay(context.getResources()) + eventTitle + " " + (rising ? "rising" : "setting") + " at " + percent;   // TODO: format
    }
    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return context.getString(R.string.daypercentevent_phrase_gender);
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        String percentDisplay = getPercentValue() + "";   // TODO
        String eventTitle = context.getResources().getString(percent >= 0 ? R.string.daypercentevent_title_day : R.string.daypercentevent_title_night);
        if (offset == 0) {
            return offsetDisplay(context.getResources()) + context.getString(R.string.daypercentevent_summary_format, eventTitle, percentDisplay);
        } else {
            return context.getString(R.string.daypercentevent_summary_format1, offsetDisplay(context.getResources()), eventTitle, percentDisplay);
        }
    }

    public static boolean isDayPercentEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. DAYPERCENT_25r          (@ 25% (after rising)),
     *              DAYPERCENT_25.5r        (@ 25.5% (after rising)),
     *              DAYPERCENT_50r          (@ 50% (after rising) (noon)),
     *              DAYPERCENT_75r          (@ 75% (after rising)),
     *              DAYPERCENT_25s          (@ 25% (before setting)),
     *
     *              DAYPERCENT_-50r         (@ 50% (before rising) (midnight)),
     *              DAYPERCENT_-50s         (@ 50% (after setting) (midnight)),
     *              DAYPERCENT_-75s         (@ 25% (75 after setting)),
     *              DAYPERCENT_-25r         (@ 25% (before rising)),
     *
     *              DAYPERCENT_25|-300000r  (5m before @ 25% (after rising))
     */
    @Override
    public String getEventName() {
        return getEventName(angle, offset, rising);
    }
    public static String getEventName(double percent, int offset, @Nullable Boolean rising)
    {
        String name = NAME_PREFIX
                + percent
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (rising != null) {
            name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
        }
        return name;
    }

    @Nullable
    public static DayPercentEvent valueOf(String eventName)
    {
        if (isDayPercentEvent(eventName))
        {
            double percent;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
            try {
                String contentString = eventName.substring(NAME_PREFIX.length(), eventName.length() - (hasSuffix ? 1 : 0));    // DAYPERCENT_<contentString>
                String[] contentParts = contentString.split("\\|");
                percent = Double.parseDouble(contentParts[0]);

                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("DayPercentEvent", "createEvent: " + e);
                return null;
            }
            boolean rising = eventName.endsWith(SUFFIX_RISING);
            return new DayPercentEvent(percent, (offsetMinutes * 60 * 1000), rising);

        } else return null;
    }

    /**
     * @param context context
     * @param event DayPercentEvent
     * @param data SuntimesData
     * @param offset offset millis
     * @param repeating true/false
     * @param repeatingDays e.g. "[1,2,3,4,5,6,7]"
     * @param now now
     * @return calendar or null
     */
    public static Calendar updateAlarmTime(Context context, @NonNull DayPercentEvent event, @NonNull SuntimesData data, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        data.initCalculator();
        SuntimesCalculator calculator = data.calculator();

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        eventTime = getDayPercentEventCalendar(day, event, calculator);
        if (eventTime != null) {
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }

        int c = 0;
        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime)
                || eventTime == null
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                android.util.Log.e("AlarmReceiver", "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                return null;
            }

            android.util.Log.d("AlarmReceiver", "updateAlarmTime: dayPercentEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            data.setTodayIs(day);
            data.calculate(context);

            eventTime = getDayPercentEventCalendar(day, event, calculator);
            if (eventTime != null) {
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
                event.setAngle(calculator.getSunPosition(eventTime).elevation);
            }
            c++;
        }
        return eventTime;
    }

    @android.support.annotation.Nullable
    public static Calendar getDayPercentEventCalendar(@NonNull Calendar day, @NonNull DayPercentEvent event, @NonNull SuntimesCalculator calculator)
    {
        double percent = event.getPercentValue() / 100d;
        if (percent >= 0)    // positive values; day duration
        {
            Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(day);
            Calendar sunset = calculator.getOfficialSunsetCalendarForDate(day);
            if (sunrise != null && sunset != null)
            {
                long duration = (sunset.getTimeInMillis() - sunrise.getTimeInMillis());
                Calendar eventTime = Calendar.getInstance();
                eventTime.setTimeInMillis((long) (event.isRising()
                        ? sunrise.getTimeInMillis() + (percent * duration)
                        : sunset.getTimeInMillis() - (percent * duration)));
                return eventTime;
            } // else // TODO: support edge cases
            return null;

        } else {    // negative values; night duration
            Calendar sunset = calculator.getOfficialSunriseCalendarForDate(day);
            Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(tomorrowCalendar(day));
            if (sunset != null && sunrise != null)
            {
                long duration = (sunrise.getTimeInMillis() - sunset.getTimeInMillis());
                Calendar eventTime = Calendar.getInstance();
                eventTime.setTimeInMillis((long) (event.isRising()
                        ? sunrise.getTimeInMillis() - (-percent * duration)
                        : sunset.getTimeInMillis() + (-percent * duration)));
                return eventTime;
            } // else // TODO: support edge cases
            return null;
        }
    }

    private static Calendar tomorrowCalendar(Calendar day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(day.getTimeInMillis() + (24 * 60 * 60 * 1000));
        return calendar;
    }

}
