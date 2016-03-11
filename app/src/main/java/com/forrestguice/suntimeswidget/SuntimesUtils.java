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
        private String value;
        private String units;
        private String suffix;

        public TimeDisplayText(String value, String units, String suffix)
        {
            this.value = value;
            this.units = units;
            this.suffix = suffix;
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

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(value);
            s.append(" ");
            s.append(units);
            s.append(" ");
            s.append(suffix);
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
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
        timeFormat.setTimeZone(cal.getTimeZone());

        SimpleDateFormat suffixFormat = new SimpleDateFormat("a");
        suffixFormat.setTimeZone(cal.getTimeZone());

        Date time = cal.getTime();
        return new TimeDisplayText( timeFormat.format(time), "", suffixFormat.format(time) );
    }

    /**
     * @param c1
     * @param c2
     * @return
     */
    public String calendarDeltaShortDisplayString(Calendar c1, Calendar c2)
    {
        return "";  // TODO
    }

    /**
     * @param timeSpan1 first event
     * @param timeSpan2 second event
     * @return a TimeDisplayText object that describes difference between the two spans
     */
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2)
    {
        String value = "";
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
        long remainingSeconds = numberOfSeconds % 60;

        value += ((numberOfMinutes < 1) ? "" : numberOfMinutes + "m")
                + " " +
                ((remainingSeconds < 1) ? "" : remainingSeconds + "s");

        return new TimeDisplayText(value, units, suffix);
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
