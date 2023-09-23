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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BedtimeItemAdapter extends RecyclerView.Adapter<BedtimeViewHolder>
{
    protected ArrayList<BedtimeItem> items = new ArrayList<>();
    protected WeakReference<Context> contextRef;

    public BedtimeItemAdapter(Context context)
    {
        contextRef = new WeakReference<>(context);
        initItems();
    }

    protected void initItems() {
        items.clear();
        items.add(new BedtimeItem(BedtimeItem.ItemType.BEDTIME));
        items.add(new BedtimeItem(BedtimeItem.ItemType.BEDTIME_REMINDER));
        items.add(new BedtimeItem(BedtimeItem.ItemType.SLEEP_CYCLE));
        items.add(new BedtimeItem(BedtimeItem.ItemType.WAKEUP_ALARM));
        items.add(new BedtimeItem(BedtimeItem.ItemType.BEDTIME_NOW));
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

    public void moveItem(int position, int toPosition)
    {
        if (position >= 0 && position < items.size())
        {
            BedtimeItem item = items.get(position);
            if (toPosition >= 0 && position < items.size())
            {
                if (toPosition < position) {
                    items.remove(position);
                    items.add(toPosition, item);
                } else {
                    items.add(toPosition, item);
                    items.remove(position);
                }
                notifyItemMoved(position, toPosition);
            }
        }
    }

    public int findItemPosition(BedtimeItem.ItemType type)
    {
        for (int i=0; i<items.size(); i++)
        {
            BedtimeItem item0 = getItem(i);
            if (item0 != null
                    && item0.getItemType() == type) {
                return i;
            }
        }
        return -1;
    }

    public int findItemPosition(BedtimeItem item)
    {
        for (int i=0; i<items.size(); i++)
        {
            BedtimeItem item0 = getItem(i);
            if (item0 != null
                    && item0.getItemType() == item.getItemType()) {
                return i;
            }
        }
        return -1;
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
            case SLEEP_CYCLE: return new BedtimeViewHolder.AlarmBedtimeViewHolder_SleepCycle(view);
            case BEDTIME_NOW: return new BedtimeViewHolder.AlarmBedtimeViewHolder_BedtimeNow(view);
            case WAKEUP_ALARM: return new BedtimeViewHolder.AlarmBedtimeViewHolder_Wakeup(view);
            case BEDTIME: return new BedtimeViewHolder.BedtimeViewHolder_Bedtime(view);
            case BEDTIME_REMINDER: return new BedtimeViewHolder.BedtimeViewHolder_BedtimeReminder(view);
            case NONE: default: return new BedtimeViewHolder.BedtimeViewHolder_Welcome(view);
        }
    }

    public int getLayoutResID(BedtimeItem.ItemType type)
    {
        switch (type)
        {
            case SLEEP_CYCLE: return BedtimeViewHolder.AlarmBedtimeViewHolder_SleepCycle.getLayoutResID();
            case BEDTIME_NOW: return BedtimeViewHolder.AlarmBedtimeViewHolder_BedtimeNow.getLayoutResID();
            case WAKEUP_ALARM: return BedtimeViewHolder.AlarmBedtimeViewHolder_Wakeup.getLayoutResID();
            case BEDTIME: return BedtimeViewHolder.BedtimeViewHolder_Bedtime.getLayoutResID();
            case BEDTIME_REMINDER: return BedtimeViewHolder.BedtimeViewHolder_BedtimeReminder.getLayoutResID();
            case NONE: default: return BedtimeViewHolder.BedtimeViewHolder_Welcome.getLayoutResID();
        }
    }

    @Override
    public void onBindViewHolder(BedtimeViewHolder holder, int position)
    {
        Context context = contextRef.get();
        final BedtimeItem item = getItem(position);
        holder.bindDataToHolder(context, item);
        attachClickListeners(context, holder, item);
    }

    protected void attachClickListeners(final Context context, final BedtimeViewHolder holder, final BedtimeItem item)
    {
        holder.attachClickListeners(context, item);

        View actionView = holder.getActionView();
        if (actionView != null)
        {
            actionView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null ) {
                        adapterListener.onItemAction(item);
                    }
                }
            });
        }

        View configActionView = holder.getConfigureActionView();
        if (configActionView != null)
        {
            configActionView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null ) {
                        adapterListener.onItemConfigure(item);
                    }
                }
            });
        }
    }

    @Override
    public void onViewRecycled(BedtimeViewHolder holder)
    {
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
        void onItemAction(BedtimeItem item);
        void onItemConfigure(BedtimeItem item);
    }
}
