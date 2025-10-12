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

public final class MoonIllumEvent extends BaseEvent
{
    public static final String NAME_PREFIX = "MOONILLUM_";

    public static final String SUFFIX_WAXING = "r";
    public static final String SUFFIX_WANING = "s";

    /**
     * @param percent percent moon illumination
     * @param offset time offset in milliseconds
     * @param isWaxing true, waxing moon; false, waning moon
     */
    public MoonIllumEvent(double percent, int offset, boolean isWaxing)
    {
        super(offset);
        this.percent = percent;
        this.waxing = isWaxing;
    }

    protected double percent;
    public double getPercentValue() {
        return percent;
    }

    protected boolean waxing;
    public boolean isWaxing() {
        return waxing;
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        String eventTitle = context.getResources().getString(R.string.moonillumevent_title);
        return offsetDisplay(context.getResources()) + eventTitle + " " + (waxing ? "waxing" : "waning") + " (" + percent + ")";   // TODO: format
    }
    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        String eventTitle = context.getResources().getString(R.string.moonillumevent_title);
        return offsetDisplay(context.getResources()) + eventTitle + " " + (waxing ? "waxing" : "waning") + " at " + percent;   // TODO: format
    }
    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return context.getString(R.string.moonillumevent_phrase_gender);
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        String percentDisplay = getPercentValue() + "";
        String eventTitle = context.getResources().getString(R.string.moonillumevent_title);
        if (offset == 0) {
            return offsetDisplay(context.getResources()) + context.getString(R.string.moonillumevent_summary_format, eventTitle, percentDisplay);
        } else {
            return context.getString(R.string.moonillumevent_summary_format1, offsetDisplay(context.getResources()), eventTitle, percentDisplay);
        }
    }

    public static boolean isMoonIllumEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. MOONILLUM_25r          (@ 25% waxing),
     *              MOONILLUM_25s          (@ 25% waning),
     *              MOONILLUM_25|-300000r  (5m before @ 25% waxing))
     */
    @Override
    public String getEventName() {
        return getEventName(percent, offset, waxing);
    }
    public static String getEventName(double percent, int offset, @Nullable Boolean waxing)
    {
        String name = NAME_PREFIX
                + percent
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (waxing != null) {
            name += (waxing ? SUFFIX_WAXING : SUFFIX_WANING);
        }
        return name;
    }

    @Nullable
    public static MoonIllumEvent valueOf(String eventName)
    {
        if (isMoonIllumEvent(eventName))
        {
            double percent;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_WAXING) || eventName.endsWith(SUFFIX_WANING);
            try {
                String contentString = eventName.substring(NAME_PREFIX.length(), eventName.length() - (hasSuffix ? 1 : 0));    // MOONILLUM_<contentString>
                String[] contentParts = contentString.split("\\|");
                percent = Double.parseDouble(contentParts[0]);

                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("MoonIllumEvent", "createEvent: " + e);
                return null;
            }
            boolean waxing = eventName.endsWith(SUFFIX_WAXING);
            return new MoonIllumEvent(percent, (offsetMinutes * 60 * 1000), waxing);

        } else return null;
    }

    /**
     * updateAlarmTime
     * @param context context
     * @param event MoonIllumEvent
     * @param offset milliseconds
     * @param repeating true/false
     * @param repeatingDays e.g. "[1,2,3,4,5,6,7]"
     * @param now now
     * @return Calendar or null
     */
    public static Calendar updateAlarmTime(Context context, @NonNull MoonIllumEvent event, SuntimesData data, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        data.initCalculator();
        SuntimesCalculator calculator = data.calculator();

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        eventTime = getMoonIllumEventCalendar(day, event, calculator);
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

            android.util.Log.d("AlarmReceiver", "updateAlarmTime: moonIllumEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            data.setTodayIs(day);
            data.calculate(context);

            eventTime = getMoonIllumEventCalendar(day, event, calculator);
            if (eventTime != null) {
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    @android.support.annotation.Nullable
    public static Calendar getMoonIllumEventCalendar(@NonNull Calendar day, @NonNull MoonIllumEvent event, @NonNull SuntimesCalculator calculator)
    {
        boolean isWaxing = event.isWaxing();
        double eventPercent = Math.abs(event.getPercentValue() / 100d);

        Calendar startDay;
        Calendar endDay;

        if (isWaxing) {    // choose start/end days corresponding to major phases
            //if (eventPercent < 0.5) {
            startDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.NEW, day);
            //endDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FIRST_QUARTER, startDay);
            //} else {
            //startDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FIRST_QUARTER, day);
            endDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FULL, startDay);
            //}
        } else {
            //if (eventPercent >= 0.5) {
            startDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FULL, day);
            //endDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.THIRD_QUARTER, startDay);
            //} else {
            //startDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.THIRD_QUARTER, day);
            endDay = calculator.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.NEW, startDay);
            //}
        }

        Calendar mid = Calendar.getInstance();
        long left = startDay.getTimeInMillis();
        long right = endDay.getTimeInMillis();

        while (left <= right)    // binary search between phase dates
        {
            long m = left + ((right - left) / 2L);
            mid.setTimeInMillis(m);
            double illum = calculator.getMoonIlluminationForDate(mid);

            //Log.d("DEBUG", "comparing " + illum + " to " + eventPercent);
            if (almostEquals(illum, eventPercent)) {
                return mid;

            } else {
                boolean isToRight = (isWaxing) ? illum < eventPercent
                        : illum > eventPercent;
                boolean isToLeft = (isWaxing) ? illum > eventPercent
                        : illum < eventPercent;
                if (isToRight) {
                    left = m + 1;
                } else if (isToLeft) {
                    right = m - 1;
                }
            }
        }
        return null;
    }

    private static boolean almostEquals(double a, double b) {
        return Math.abs(a - b) < 0.0025;
    }
}
