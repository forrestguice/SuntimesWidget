/**
    Copyright (C) 2014 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.content.Context;
import java.text.DateFormat;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SuntimesUtils
{
    public SuntimesUtils()
    {
    }

    /**
     * TimeDisplayText : class
     */
    public static class TimeDisplayText
    {
        private long rawValue = 0;
        private String value;
        private String units;
        private String suffix;

        public TimeDisplayText(String value, String units, String suffix)
        {
            this.value = value;
            this.units = units;
            this.suffix = suffix;
        }

        public void setRawValue( long value )
        {
            rawValue = value;
        }

        public long getRawValue()
        {
            return rawValue;
        }

        public String getValue()
        {
            return value;
        }

        public String getUnits()
        {
            return units;
        }

        public String getSuffix()
        {
            return suffix;
        }
        public void setSuffix( String suffix )
        {
            this.suffix = suffix;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(value);

            if (!units.isEmpty())
            {
                s.append(" ");
                s.append(units);
            }

            if (!suffix.isEmpty())
            {
                s.append(" ");
                s.append(suffix);
            }

            return s.toString();
        }
    }

    /**
     * @param context
     * @param cal
     * @return
     */
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal)
    {
        Date time = cal.getTime();
        TimeDisplayText retValue;

        boolean is24 = android.text.format.DateFormat.is24HourFormat(context);
        if (is24)
        {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            retValue = new TimeDisplayText(timeFormat.format(time), "", "");

        } else {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
            timeFormat.setTimeZone(cal.getTimeZone());

            SimpleDateFormat suffixFormat = new SimpleDateFormat("a");
            suffixFormat.setTimeZone(cal.getTimeZone());
            retValue = new TimeDisplayText( timeFormat.format(time), "", suffixFormat.format(time) );
        }

        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    /**
     * @param c1
     * @param c2
     * @return
     */
    public TimeDisplayText timeDeltaDisplayString(Date c1, Date c2)
    {
        TimeDisplayText displayText = timeDeltaLongDisplayString(c1.getTime(), c2.getTime());
        displayText.setSuffix("");
        return displayText;
    }

    /**
     * @param timeSpan1 first event
     * @param timeSpan2 second event
     * @return a TimeDisplayText object that describes difference between the two spans
     */
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2)
    {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false);
    }

    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showSeconds)
    {
        String value = " ";  // space
        String units = "";
        String suffix = "";

        long timeSpan = timeSpan2 - timeSpan1;
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(timeSpan);
        long timeInMillis = d.getTimeInMillis();

        long numberOfSeconds = timeInMillis / 1000;
        suffix += ((numberOfSeconds > 0) ? "longer" : "shorter");
        numberOfSeconds = Math.abs(numberOfSeconds);

        long numberOfMinutes = numberOfSeconds / 60;
        long numberOfHours = numberOfMinutes / 60;
        long numberOfDays = numberOfHours / 24;

        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingDays = (numberOfDays > 0);
        if (showingDays)
            value += numberOfDays + "d";

        boolean showingHours = (remainingHours > 0);
        if (showingHours)
            value += (showingDays ? " " : "") + remainingHours + "h";

        boolean showingMinutes = (remainingMinutes > 0);
        if (showingMinutes)
            value += (showingDays || showingHours ? " " : "") + remainingMinutes + "m";

        boolean showingSeconds = (showSeconds && !showingHours && !showingDays && (remainingSeconds > 0));
        if (showingSeconds)
            value += (showingMinutes ? " " : "") + remainingSeconds + "s";

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays)
            value += "1m";

        TimeDisplayText text = new TimeDisplayText(value, units, suffix);
        text.setRawValue(timeSpan);
        return text;
    }

    /**
     * Creates a title string from a given "title pattern".
     *
     * The following substitutions are supported:
     *   %% .. the % character
     *   %m .. the time mode (short version; e.g. civil)
     *   %M .. the time mode (long version; e.g. civil twilight)
     *   %t .. the timezoneID (e.g. US/Arizona)
     *   %loc .. the location (label/name)
     *   %lat .. the location (latitude)
     *   %lon .. the location (longitude)
     *
     * @param titlePattern a pattern string (simple substitutions)
     * @return a display string suitable for display as a widget title
     */
    public String displayStringForTitlePattern(String titlePattern, SuntimesData data)
    {
        String modePattern = "%M";
        String modePatternShort = "%m";
        String locPattern = "%loc";
        String latPattern = "%lat";
        String lonPattern = "%lon";
        String timezoneIDPattern = "%t";
        String percentPattern = "%%";

        WidgetSettings.TimeMode timeMode = data.timeMode();
        WidgetSettings.Location location = data.location();
        String timezoneID = data.timezone();

        String displayString = titlePattern;
        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        displayString = displayString.replaceAll(locPattern, location.getLabel());
        displayString = displayString.replaceAll(latPattern, location.getLatitude());
        displayString = displayString.replaceAll(lonPattern, location.getLongitude());
        displayString = displayString.replaceAll(timezoneIDPattern, timezoneID);
        displayString = displayString.replaceAll(percentPattern, "%");

        return displayString;
    }
}
