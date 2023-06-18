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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class BedtimeItemAdapter extends RecyclerView.Adapter<BedtimeViewHolder>
{
    protected ArrayList<BedtimeItem> items;

    public BedtimeItemAdapter() {
        initItems();
    }

    protected void initItems() {
        items = new ArrayList<>();
        items.add(new BedtimeItem(BedtimeItem.ItemType.BEDTIME_NOTIFICATION));  // 0
        items.add(new BedtimeItem(BedtimeItem.ItemType.WAKEUP_ALARM));          // 1
    }

    @Nullable
    public BedtimeItem getItem(int position)
    {
        BedtimeItem item = null;
        if (position >= 0 && position < items.size()) {
            item = items.get(position);
        }
        return item;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        BedtimeItem item = getItem(position);
        if (item != null) {
            return item.getItemType().ordinal();
        } else return BedtimeItem.ItemType.NONE.ordinal();
    }

    public BedtimeItem.ItemType getItemType(int viewType)
    {
        BedtimeItem.ItemType[] types = BedtimeItem.ItemType.values();
        if (viewType >= 0 && viewType < types.length) {
            return BedtimeItem.ItemType.values()[viewType];
        } else return BedtimeItem.ItemType.NONE;
    }

    @Override
    public BedtimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        BedtimeItem.ItemType type = getItemType(viewType);
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(getLayoutResID(type), parent, false);
        switch (type)
        {
            case WAKEUP_ALARM: return new BedtimeViewHolder.AlarmBedtimeViewHolder_Wakupe(view);
            case BEDTIME_NOTIFICATION: return new BedtimeViewHolder.BedtimeViewHolder_BedtimeNotification(view);
            case NONE: default: return new BedtimeViewHolder.BedtimeViewHolder_Welcome(view);
        }
    }

    public int getLayoutResID(BedtimeItem.ItemType type)
    {
        switch (type)
        {
            case WAKEUP_ALARM: return BedtimeViewHolder.AlarmBedtimeViewHolder_Wakupe.getLayoutResID();
            case BEDTIME_NOTIFICATION: return BedtimeViewHolder.BedtimeViewHolder_BedtimeNotification.getLayoutResID();
            case NONE: default: return BedtimeViewHolder.BedtimeViewHolder_Welcome.getLayoutResID();
        }
    }

    @Override
    public void onBindViewHolder(BedtimeViewHolder holder, int position)
    {
        holder.bindDataToHolder(getItem(position));
        holder.attachClickListeners();
    }

    @Override
    public void onViewRecycled(BedtimeViewHolder holder) {
        holder.detachClickListeners();
    }

    protected AdapterListener adapterListener = null;
    public void setAdapterListener(AdapterListener listener) {
        this.adapterListener = listener;
    }

    /**
     * AdapterListener
     */
    public interface AdapterListener {
    }
}
