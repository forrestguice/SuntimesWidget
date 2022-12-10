/**
    Copyright (C) 2018-2020 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;

/**
 * AlarmClockAdapterListener
 */
public interface AlarmItemAdapterListener
{
    void onTypeChanged(AlarmClockItem forItem);
    void onRequestLabel(AlarmClockItem forItem);
    void onRequestNote(AlarmClockItem forItem);
    void onRequestRingtone(AlarmClockItem forItem);
    void onRequestSolarEvent(AlarmClockItem forItem);
    void onRequestLocation(AlarmClockItem forItem);
    void onRequestTime(AlarmClockItem forItem);
    void onRequestOffset(AlarmClockItem forItem);
    void onRequestRepetition(AlarmClockItem forItem);
    void onRequestAction(AlarmClockItem forItem, int actionNum);
    void onRequestDialog(AlarmClockItem forItem);
}
