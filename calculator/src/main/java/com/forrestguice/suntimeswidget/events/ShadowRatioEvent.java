/**
    Copyright (C) 2026 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public final class ShadowRatioEvent extends ShadowLengthEvent
{
    public static final String NAME_PREFIX = "SHADOWRATIO_";

    public ShadowRatioEvent(double ratio, boolean relativeToNoon, int offset, boolean rising)
    {
        super(1, ratio, offset, rising);
        this.ratio = ratio;
        this.relativeToNoon = relativeToNoon;
    }

    protected final double ratio;    // shadow length / object height
    public double getRatio() {
        return ratio;
    }

    protected final boolean relativeToNoon;
    public boolean isRelativeToNoon() {
        return relativeToNoon;
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        return offsetDisplay(context.getResources()) + eventTitle + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
    }

    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        return offsetDisplay(context.getResources()) + eventTitle + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
    }

    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return (r != null) ? context.getString(r.string_phrase_gender()) : "other";
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        if (offset == 0) {
            return (r != null) ? offsetDisplay(context.getResources()) + context.getString(r.string_summary_format(), eventTitle, ratio)
                    : offsetDisplay(context.getResources()) + " " + eventTitle + " (" + ratio + ")";
        } else {
            return (r != null) ? context.getString(r.string_summary_format1(), offsetDisplay(context.getResources()), eventTitle, ratio)
                    : offsetDisplay(context.getResources()) + " " + eventTitle + " (" + ratio + ")";
        }
    }

    public static boolean isShadowRatioEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. SHADOWRATIO_A:1.5s          (1.5x absolute; shadow length is 1x object height (setting)),
     *              SHADOWRATIO_A:2|-5r         (2x absolute; 5m before factor x2 (rising))
     *              SHADOWRATIO_X:2s            (2x relative; shadow length is 2x object height plus length of shadow at noon; ie. a change in shadow factor of 2x)
     */
    @Override
    public String getEventName() {
        return getEventName(ratio, relativeToNoon, offset, rising);
    }
    public static String getEventName(double factor, boolean relativeToNoon, int offset, @Nullable Boolean rising) {
        String name = NAME_PREFIX
                + (relativeToNoon ? PREFIX_RELATIVE_NOON : PREFIX_ABSOLUTE)
                + ":" + factor
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (rising != null) {
            name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
        }
        return name;
    }
    public static final String PREFIX_ABSOLUTE = "A";
    public static final String PREFIX_RELATIVE_NOON = "X";

    @Nullable
    public static ShadowRatioEvent valueOf(String eventName)
    {
        if (isShadowRatioEvent(eventName))
        {
            double factor = 1;
            boolean relativeToNoon = true;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
            try {
                String contentString = eventName.substring(NAME_PREFIX.length(), eventName.length() - (hasSuffix ? 1 : 0));    // SHADOWRATIO_<contentString>
                String[] contentParts = contentString.split("\\|");

                String[] shadowParts = contentParts[0].split(":");
                if (shadowParts.length > 1)
                {
                    relativeToNoon = PREFIX_RELATIVE_NOON.equals(shadowParts[0]);
                    factor = Double.parseDouble(shadowParts[1]);

                } else if (shadowParts.length > 0) {
                    relativeToNoon = false;
                    factor = Double.parseDouble(shadowParts[0]);;
                }

                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("ShadowRatioEvent", "createEvent: " + e);
                return null;
            }
            boolean rising = eventName.endsWith(SUFFIX_RISING);
            return new ShadowRatioEvent(factor, relativeToNoon, (offsetMinutes * 60 * 1000), rising);
        } else return null;
    }

    /**
     * @param context context
     * @param event ShadowRatioEvent
     * @param data SuntimesData
     * @param offset offset millis
     * @param repeating true/false
     * @param repeatingDays e.g. "[1,2,3,4,5,6,7]"
     * @param now now
     * @return calendar or null
     */
    public static Calendar updateAlarmTime(Object context, @NonNull ShadowRatioEvent event, @NonNull SuntimesData data, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        Log.d("DEBUG", "updateAlarmTime: relativeToNoon? " + event.isRelativeToNoon());
        data.initCalculator();
        SuntimesCalculator calculator = data.calculator();

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        eventTime = getShadowRatioEventCalendar(calculator, day, event, offset);
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
                Log.e("AlarmReceiver", "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                return null;
            }

            Log.d("AlarmReceiver", "updateAlarmTime: shadowRatioEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            data.setTodayIs(day);
            data.calculate(context);

            eventTime = getShadowRatioEventCalendar(calculator, day, event, offset);
            if (eventTime != null) {
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    private static Calendar getShadowRatioEventCalendar(SuntimesCalculator calculator, Calendar day, ShadowRatioEvent event, long offset)
    {
        Calendar noon = calculator.getSolarNoonCalendarForDate(day);
        SuntimesCalculator.SunPosition position = (noon != null ? calculator.getSunPosition(noon) : null);
        if (position != null)
        {
            double shadowLength = (event.isRelativeToNoon()
                    ? event.getRatio() + calculator.getShadowLength(1, noon)
                    : event.getRatio());

            Calendar eventTime = (event.isRising()
                    ? calculator.getTimeOfShadowBeforeNoon(day, 1, shadowLength)
                    : calculator.getTimeOfShadowAfterNoon(day, 1, shadowLength));

            if (eventTime != null && offset != 0) {
                eventTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            return eventTime;
        } else return null;
    }

    @Nullable
    protected static ResID_ShadowRatioEvent r = null;
    public static void setResIDs(@NonNull ResID_ShadowRatioEvent values) {
        r = values;
    }

    public interface ResID_ShadowRatioEvent extends ResID_BaseEvent
    {
        int string_title();
        int string_phrase_gender();
        int string_summary_format();
        int string_summary_format1();
    }
}
