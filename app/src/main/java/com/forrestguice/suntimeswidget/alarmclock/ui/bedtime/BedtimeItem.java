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

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;

import java.util.List;

public class BedtimeItem
{
    public BedtimeItem(ItemType type) {
        this.type = type;
    }

    public BedtimeItem(ItemType type, String slot) {
        this.type = type;
        this.slot = slot;
    }

    protected ItemType type;
    public ItemType getItemType() {
        return type;
    }

    public Long getAlarmID(Context context) {
        return ((slot != null) ? alarmId = BedtimeSettings.loadAlarmID(context, getSlot()) : null);
    }
    protected long alarmId = BedtimeSettings.ID_NONE;
    public Long cachedAlarmID() {
        return alarmId;
    }

    protected AlarmClockItem alarmItem = null;
    @Nullable
    public AlarmClockItem getAlarmItem() {
        return alarmItem;
    }
    public void setAlarmItem(@Nullable AlarmClockItem item) {
        alarmItem = item;
    }
    protected void loadAlarmItem(Context context, final AlarmListDialog.AlarmListTask.AlarmListTaskListener onItemLoaded)
    {
        setAlarmItem(null);
        final Long alarmId = getAlarmID(context);
        if (alarmId != null && alarmId != BedtimeSettings.ID_NONE)
        {
            BedtimeAlarmHelper.loadAlarmItem(context, alarmId, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0) {
                        setAlarmItem(result.get(0));
                    }
                    if (onItemLoaded != null) {
                        onItemLoaded.onLoadFinished(result);
                    }
                }
            });
        }
    }

    public String slot = null;
    public String getSlot() {
        return slot;
    }
    public void setSlot(String slot) {
        this.slot = slot;
    }

    /**
     * ItemType
     */
    public static enum ItemType
    {
        NONE,
        SLEEP_CYCLE,
        WAKEUP_ALARM,
        BEDTIME,
        BEDTIME_REMINDER,
        BEDTIME_NOW;
        private ItemType() {
        }
    }

}
