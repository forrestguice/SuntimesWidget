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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.forrestguice.suntimeswidget.R;

public abstract class BedtimeViewHolder extends RecyclerView.ViewHolder
{
    public BedtimeViewHolder(View view) {
        super(view);
    }

    public abstract void bindDataToHolder(@Nullable BedtimeItem item);

    protected void attachClickListeners() {}
    protected void detachClickListeners() {}

    /**
     * Welcome
     */
    public static final class BedtimeViewHolder_Welcome extends BedtimeViewHolder
    {
        public BedtimeViewHolder_Welcome(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_alarmclock2;   // TODO
        }

        @Override
        public void bindDataToHolder(@Nullable BedtimeItem item) {
            // TODO
        }
    }

    /**
     * BedtimeNotify
     */
    public static final class BedtimeViewHolder_BedtimeNotification extends BedtimeViewHolder
    {
        public BedtimeViewHolder_BedtimeNotification(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_alarmclock2;   // TODO
        }

        @Override
        public void bindDataToHolder(@Nullable BedtimeItem item) {
            // TODO
        }
    }

    /**
     * WakeupAlarm
     */
    public static final class AlarmBedtimeViewHolder_Wakupe extends BedtimeViewHolder
    {
        public AlarmBedtimeViewHolder_Wakupe(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_alarmclock2;   // TODO
        }

        @Override
        public void bindDataToHolder(@Nullable BedtimeItem item) {
            // TODO
        }
    }
}