/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar;

import com.forrestguice.util.ContextInterface;
import com.forrestguice.util.Resources;

public interface CalendarSettingsInterface extends ContextInterface
{
    String PREF_PREFIX_KEY_CALENDAR = "_calendar_";

    String PREF_KEY_CALENDAR_SHOWDATE = "showDate";        // always true for the DateWidget; used by other widget to optionally show a date
    boolean PREF_DEF_CALENDAR_SHOWDATE = false;

    String PREF_KEY_CALENDAR_MODE = "calendarMode";
    CalendarMode PREF_DEF_CALENDAR_MODE = CalendarMode.GREGORIAN;

    String PREF_KEY_CALENDAR_FORMATPATTERN = "calendarFormat";

    ///////////////////////////////////////////////////////

    void saveCalendarFlag(int appWidgetId, String key, boolean value);

    boolean loadCalendarFlag(int appWidgetId, String key, boolean defValue);

    void saveCalendarModePref(int appWidgetId, CalendarMode mode);
    CalendarMode loadCalendarModePref(int appWidgetId);

    void saveCalendarFormatPatternPref(int appWidgetId, String tag, String formatString);

    String loadCalendarFormatPatternPref(int appWidgetId, String tag);

    void deleteCalendarFormatPatternPref(int appWidgetId, String tag);

    String defaultCalendarFormatPattern(String tag);

    void deleteCalendarPref(int appWidgetId, String key);
    void deletePrefs(int appWidgetId);

    void initDisplayStrings(Resources context);
}
