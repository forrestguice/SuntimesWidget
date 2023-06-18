/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

public class BedtimeItem
{
    public BedtimeItem(ItemType type) {
        this.type = type;
    }

    protected ItemType type;
    public ItemType getItemType() {
        return type;
    }

    /**
     * ItemType
     */
    public static enum ItemType
    {
        NONE,
        WAKEUP_ALARM,
        BEDTIME_NOTIFICATION;
        private ItemType() {
        }
    }
}
