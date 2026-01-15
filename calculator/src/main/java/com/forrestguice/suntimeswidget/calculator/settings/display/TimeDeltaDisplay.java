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

package com.forrestguice.suntimeswidget.calculator.settings.display;

import com.forrestguice.util.Resources;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.GregorianCalendar;

public class TimeDeltaDisplay
{
    protected static String strTimeShorter = "shorter";
    protected static String strTimeLonger = "longer";
    protected static String strTimeSame = "the same";
    protected static String strSpace = "\u00A0";
    public static String strEmpty = "";
    protected static String strYears = "y";
    protected static String strWeeks = "w";
    protected static String strDays = "d";
    public static String strHours = "h";
    public static String strMinutes = "m";
    public static String strSeconds = "s";
    public static String strTimeDeltaFormat = "%1$s"  + strEmpty + "%2$s";
    public static boolean initialized = false;

    public static void initDisplayStrings(Resources res, ResID_TimeDeltaDisplay r)
    {
        strTimeShorter = res.getString(r.string_strTimeShorter());
        strTimeLonger = res.getString(r.string_strTimeLonger());
        strTimeSame = res.getString(r.string_strTimeSame());
        strYears = res.getString(r.string_strYears());
        strWeeks = res.getString(r.string_strWeeks());
        strDays = res.getString(r.string_strDays());
        strHours = res.getString(r.string_strHours());
        strMinutes = res.getString(r.string_strMinutes());
        strSeconds = res.getString(r.string_strSeconds());
        strTimeDeltaFormat = res.getString(r.string_strTimeDeltaFormat());
        initialized = true;
    }

    /**
     * @param timeSpan1 first event
     * @param timeSpan2 second event
     * @return a display string that describes difference between the two spans
     */
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2) {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, false);
    }
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showSeconds) {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, showSeconds);
    }

    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showWeeks, boolean showHours, boolean showSeconds) {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, showWeeks, showHours, true, showSeconds);
    }
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showWeeks, boolean showHours, boolean showMinutes, boolean showSeconds)
    {
        String value = strEmpty;
        String units = strEmpty;
        String suffix = strEmpty;

        long timeSpan = timeSpan2 - timeSpan1;
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(timeSpan);
        long timeInMillis = d.getTimeInMillis();

        long numberOfSeconds = timeInMillis / 1000;
        suffix += (numberOfSeconds == 0) ? strTimeSame
                : ((numberOfSeconds > 0) ? strTimeLonger : strTimeShorter);
        numberOfSeconds = Math.abs(numberOfSeconds);

        long numberOfMinutes = numberOfSeconds / 60;
        long numberOfHours = numberOfMinutes / 60;
        long numberOfDays = numberOfHours / 24;
        long numberOfWeeks = numberOfDays / 7;
        long numberOfYears = numberOfDays / 365;

        long remainingWeeks = (long)(numberOfWeeks % 52.1429);
        long remainingDays = (showWeeks ? (numberOfDays % 7) : (numberOfDays % 365));
        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingYears = (numberOfYears > 0);
        if (showingYears)
            value += String.format(strTimeDeltaFormat, numberOfYears, strYears);

        boolean showingWeeks = (showWeeks && numberOfWeeks > 0);
        if (showingWeeks)
            value += (showingYears ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingWeeks, strWeeks);

        boolean showingDays = (remainingDays > 0);
        if (showingDays)
            value += (showingYears || showingWeeks ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingDays, strDays);

        boolean showingHours = (!showingYears && !showingWeeks && remainingHours > 0);
        boolean showingMinutes = (showMinutes && !showingDays && !showingWeeks && !showingYears && remainingMinutes > 0);
        boolean showingSeconds = (showSeconds && !showingDays && !showingWeeks && !showingYears && (remainingSeconds > 0));

        if (showHours || !showingYears && !showingWeeks && remainingDays < 2)
        {
            if (showingHours)
                value += (showingYears || showingWeeks || showingDays ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingHours, strHours);

            if (showingMinutes)
                value += (showingYears || showingWeeks || showingDays || showingHours ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingMinutes, strMinutes);

            if (showingSeconds)
                value += (showingHours || showingMinutes ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingSeconds, strSeconds);
        }

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays && !showingWeeks && !showingYears)
        {
            if (showSeconds)
                value += String.format(strTimeDeltaFormat, "0", strSeconds);
            else value += String.format(strTimeDeltaFormat, "1", strMinutes);
        }

        TimeDisplayText text = new TimeDisplayText(value.trim(), units, suffix);
        text.setRawValue(timeSpan);
        return text;
    }

    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan, boolean showSeconds)
    {
        TimeDisplayText text = timeDeltaLongDisplayString(0, timeSpan, showSeconds);
        text.setSuffix("");
        return text;
    }

    public interface ResID_TimeDeltaDisplay
    {
        int string_strTimeShorter(); // R.string.delta_day_shorter
        int string_strTimeLonger();  // R.string.delta_day_longer
        int string_strTimeSame();    // R.string.delta_day_same
        int string_strYears();       // R.string.delta_years
        int string_strWeeks();       // R.string.delta_weeks
        int string_strDays();        // R.string.delta_days
        int string_strHours();       // R.string.delta_hours
        int string_strMinutes();     // R.string.delta_minutes
        int string_strSeconds();     // R.string.delta_seconds
        int string_strTimeDeltaFormat(); // R.string.delta_format
    }
}
