/**
    Copyright (C) 2021-2023 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.display.AngleDisplay;
import com.forrestguice.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public final class MoonElevationEvent extends ElevationEvent
{
    public static final String NAME_PREFIX = "MOON_";

    public MoonElevationEvent(double angle, int offset, boolean rising) {
        super(angle, offset, rising);
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        return offsetDisplay(context.getResources()) + context.getResources().getString(R.string.moonevent_title) + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
    }
    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        return offsetDisplay(context.getResources()) + context.getResources().getString(R.string.moonevent_title) + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
    }
    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return context.getString(R.string.moonevent_phrase_gender);
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        AngleDisplay utils = new AngleDisplay();
        String angle = utils.formatAsElevation(getAngle(), 1).toString();
        if (offset == 0) {
            return offsetDisplay(context.getResources()) + context.getString(R.string.moonevent_summary_format, context.getString(R.string.moonevent_title), angle);
        } else {
            return context.getString(R.string.moonevent_summary_format1, offsetDisplay(context.getResources()), context.getString(R.string.moonevent_title), angle);
        }
    }

    public static boolean isMoonElevationEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. MOON_-10r          (@ -10 degrees (rising)),
     *              MOON_-10|-300000r  (5m before @ 10 degrees (rising))
     */
    @Override
    public String getEventName() {
        return getEventName(angle, offset, rising);
    }
    public static String getEventName(double angle, int offset, @Nullable Boolean rising) {
        String name = NAME_PREFIX
                + angle
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (rising != null) {
            name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
        }
        return name;
    }

    @Nullable
    public static MoonElevationEvent valueOf(String eventName)
    {
        if (isMoonElevationEvent(eventName))
        {
            double angle;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
            try {
                String contentString = eventName.substring(NAME_PREFIX.length(), eventName.length() - (hasSuffix ? 1 : 0));
                String[] contentParts = contentString.split("\\|");

                angle = Double.parseDouble(contentParts[0]);
                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("ElevationEvent", "createEvent: bad angle: " + eventName + ": " + e);
                return null;
            }
            boolean rising = eventName.endsWith(SUFFIX_RISING);
            return new MoonElevationEvent(angle, (offsetMinutes * 60 * 1000), rising);
        } else return null;
    }

    @android.support.annotation.Nullable
    public static Calendar updateAlarmTime(Context context, @NonNull MoonElevationEvent event, @NonNull SuntimesMoonData data, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(now.getTimeInMillis() - (24 * 60 * 60 * 1000));    // start the search from yesterday
        data.setTodayIs(day);
        data.calculate(context);

        eventTime = getMoonElevationEventCalendar(event, data);
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

            android.util.Log.w("AlarmReceiver", "updateAlarmTime: moonElevationEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            data.setTodayIs(day);
            data.calculate(context);

            eventTime = getMoonElevationEventCalendar(event, data);
            if (eventTime != null) {
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    public static Calendar getMoonElevationEventCalendar(@NonNull MoonElevationEvent event, @NonNull SuntimesMoonData data)
    {
        Calendar noon0 = data.getLunarNoonToday();
        Calendar noon1 = data.getLunarNoonTomorrow();
        Calendar midnight0 = data.getLunarMidnightToday();
        Calendar midnight1 = data.getLunarMidnightTomorrow();

        Calendar start;
        Calendar end;

        boolean isRising = event.isRising();
        if (isRising) {
            start = midnight0;
            end = (noon0 == null || midnight0.after(noon0)) ? noon1 : noon0;
        } else {
            start = noon0;
            end = (midnight0 == null || noon0.after(midnight0)) ? midnight1 : midnight0;
        }
        if (start == null || end == null) {
            return null;
        }

        double eventAngle = event.getAngle();
        SuntimesCalculator calculator = data.calculator();

        Calendar mid = Calendar.getInstance();
        long left = start.getTimeInMillis();
        long right = end.getTimeInMillis();

        while (left <= right)    // binary search
        {
            long m = left + ((right - left) / 2L);
            mid.setTimeInMillis(m);
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(mid);

            if (almostEquals(position.elevation, eventAngle)) {
                return mid;

            } else {
                boolean isToRight = (isRising) ? position.elevation < eventAngle
                                               : position.elevation > eventAngle;
                if (isToRight) {
                    left = m + 1;
                    continue;
                }
                boolean isToLeft = (isRising) ? position.elevation > eventAngle
                                              : position.elevation < eventAngle;
                if (isToLeft) {
                    right = m - 1;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
        }
        return null;
    }

    private static boolean almostEquals(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
