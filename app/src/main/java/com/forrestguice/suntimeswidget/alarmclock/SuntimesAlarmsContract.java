/*
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
 * SuntimesAlarmsContract
 * @version 1 (0.1.0)
 *
 * CHANGES
 *   1 initial version
 */
public interface SuntimesAlarmsContract
{
    String AUTHORITY = "suntimeswidget.alarm.provider";

    String KEY_ROWID = "_id";                                                   // row ID
    String KEY_ALARM_TYPE = "alarmType";                                        // type (ALARM, NOTIFICATION)
    String KEY_ALARM_ENABLED = "enabled";                                       // enabled flag (0: false, 1: true)
    String KEY_ALARM_REPEATING = "repeating";                                   // repeating flag (0: false, 1: true)
    String KEY_ALARM_REPEATING_DAYS = "repeatdays";                             // repeating days (a list as String; e.g. [Calendar.SUNDAY, Calendar.Monday, ...])
    String KEY_ALARM_DATETIME = "datetime";                                     // timestamp, the (original) datetime this alarm should sound; one-time alarm if SOLAREVENT is null; repeating-alarm if SOLAREVENT is provided (DATETIME recalculated on repeat).
    String KEY_ALARM_DATETIME_ADJUSTED = "alarmtime";                           // timestamp, the (adjusted) datetime this alarm should sound; one-time alarm if SOLAREVENT is null; repeating-alarm if SOLAREVENT is provided (DATETIME recalculated on repeat).
    String KEY_ALARM_DATETIME_HOUR = "hour";                                    // hour [0,23] (optional); the hour may be used to calculate the alarm datetime
    String KEY_ALARM_DATETIME_MINUTE = "minute";                                // minute [0,59] (optional); the minute may be used to calculate the alarm datetime
    String KEY_ALARM_DATETIME_OFFSET = "timeoffset";                            // timestamp offset; alarm is triggered at some offset (+)before or (-)after timestamp.
    String KEY_ALARM_LABEL = "alarmlabel";                                      // alarm label (optional)
    String KEY_ALARM_SOLAREVENT = "event";                                      // SolarEvent ENUM (optional), the ALARM_DATETIME may be (re)calculated using this value
    String KEY_ALARM_TIMEZONE = "timezone";                                     // TZ_ID; applied to clock time when SOLAREVENT is null
    String KEY_ALARM_PLACELABEL = "place";                                      // place label (optional), the ALARM_LABEL may include this value
    String KEY_ALARM_LATITUDE = "latitude";                                     // latitude (dd) (optional), the ALARM_DATETIME may be (re)calculated using this value
    String KEY_ALARM_LONGITUDE = "longitude";                                   // longitude (dd) (optional), the ALARM_DATETIME may be (re)calculated from this value
    String KEY_ALARM_ALTITUDE = "altitude";                                     // altitude (meters) (optional), the ALARM_DATETIME may be (re)calculated from this value
    String KEY_ALARM_VIBRATE = "vibrate";                                       // vibrate flag (0: false, 1: true)
    String KEY_ALARM_ACTION0 = "actionID0";                                     // actionID 0 (optional)  .. action on trigger
    String KEY_ALARM_ACTION1 = "actionID1";                                     // actionID 1 (optional)  .. action on dismiss
    String KEY_ALARM_RINGTONE_NAME = "ringtoneName";                            // ringtone uri (optional)
    String KEY_ALARM_RINGTONE_URI = "ringtoneURI";

    String QUERY_ALARMS = "alarms";
    String[] QUERY_ALARMS_PROJECTION_MIN = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_DATETIME, KEY_ALARM_LABEL };
    String[] QUERY_ALARMS_PROJECTION_FULL = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_LABEL,
            KEY_ALARM_REPEATING, KEY_ALARM_REPEATING_DAYS,
            KEY_ALARM_DATETIME_ADJUSTED, KEY_ALARM_DATETIME, KEY_ALARM_DATETIME_HOUR, KEY_ALARM_DATETIME_MINUTE, KEY_ALARM_DATETIME_OFFSET,
            KEY_ALARM_SOLAREVENT, KEY_ALARM_PLACELABEL, KEY_ALARM_LATITUDE, KEY_ALARM_LONGITUDE, KEY_ALARM_ALTITUDE,
            KEY_ALARM_VIBRATE, KEY_ALARM_RINGTONE_NAME, KEY_ALARM_RINGTONE_URI,
            KEY_ALARM_TIMEZONE, KEY_ALARM_ACTION0, KEY_ALARM_ACTION1 };

    String KEY_STATE_ALARMID = "alarmID";
    String KEY_STATE = "state";

    String QUERY_ALARMSTATE = "state";
    String[] QUERY_ALARMSTATE_PROJECTION = new String[] { KEY_STATE_ALARMID, KEY_STATE };
}
