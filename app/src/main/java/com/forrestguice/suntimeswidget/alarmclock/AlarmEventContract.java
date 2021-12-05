/**
    Copyright (C) 2021 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock;

/**
 * AlarmEventContract
 * @version 1 (0.1.0)
 *
 * CHANGES
 *   1 initial version
 */
public interface AlarmEventContract
{
    String AUTHORITY = "suntimeswidget.event.provider";
    String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    String COLUMN_CONFIG_PROVIDER = "provider";         // String (provider reference)
    String COLUMN_EVENT_NAME = "event_name";            // String (alarm/event ID)
    String COLUMN_EVENT_TITLE = "event_title";          // String (display string)
    String COLUMN_EVENT_SUMMARY = "event_summary";      // String (extended display string)
    String COLUMN_EVENT_TIMEMILLIS = "event_time";      // long (timestamp millis)

    String QUERY_EVENT_INFO = "eventInfo";
    String[] QUERY_EVENT_INFO_PROJECTION = new String[] {
            COLUMN_EVENT_NAME, COLUMN_EVENT_TITLE, COLUMN_EVENT_SUMMARY
    };
    String QUERY_EVENT_CALC = "eventCalc";
    String[] QUERY_EVENT_CALC_PROJECTION = new String[] {
            COLUMN_EVENT_NAME, COLUMN_EVENT_TIMEMILLIS
    };

    String EXTRA_ALARM_EVENT = "alarm_event";              // eventID
    String EXTRA_ALARM_NOW = "alarm_now";                  // long (millis)
    String EXTRA_ALARM_REPEAT = "alarm_repeat";            // boolean
    String EXTRA_ALARM_REPEAT_DAYS = "alarm_repeat_days";  // Integer[] as String; e.g. "[1,2,3]"
    String EXTRA_ALARM_OFFSET = "alarm_offset";            // long (millis)

    String EXTRA_LOCATION_LABEL = "location_label";        // AlarmClockActivity.EXTRA_LOCATION_LABEL;
    String EXTRA_LOCATION_LAT = "latitude";                // AlarmClockActivity.EXTRA_LOCATION_LAT;
    String EXTRA_LOCATION_LON = "longitude";               // AlarmClockActivity.EXTRA_LOCATION_LON;
    String EXTRA_LOCATION_ALT = "altitude";                // AlarmClockActivity.EXTRA_LOCATION_ALT;
}
