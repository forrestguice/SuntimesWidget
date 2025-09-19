/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimeswidget.getfix;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GnssStatusBarView extends GnssStatusView
{
    protected RecyclerView list;
    protected SatelliteAdapter adapter;
    protected TextView label;

    public GnssStatusBarView(Context context) {
        super(context);
    }

    public GnssStatusBarView(Context context, AttributeSet attribs) {
        super(context, attribs);
    }

    @Override
    protected void initLayout(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_view_gpsstatus, this);
    }

    @Override
    protected void initViews(Context context)
    {
        super.initViews(context);

        label = (TextView) findViewById(R.id.text_satellite_count);

        list = (RecyclerView) findViewById(R.id.list_satellites);
        list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        list.setItemViewCacheSize(40);

        adapter = new SatelliteAdapter(context);
        list.setAdapter(adapter);
    }

    @TargetApi(24)
    @Override
    protected void updateViews(@Nullable GnssStatus status) {
        if (adapter != null) {
            adapter.updateViews(status);
        }
        updateViews();
    }

    @Override
    protected void updateViews(@Nullable GpsStatus status) {
        if (adapter != null) {
            adapter.updateViews(status);
        }
        updateViews();
    }

    protected void updateViews() {
        if (label != null) {
            label.setText(adapter != null ? adapter.getUsedItemCount() + "/" + adapter.getItemCount() : "");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH)
    {
        super.onSizeChanged(w, h, oldW, oldH);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * SatelliteAdapter
     */
    public static class SatelliteAdapter extends RecyclerView.Adapter<SatelliteViewHolder>
    {
        private final ArrayList<SatelliteItem> items = new ArrayList<>();
        private final WeakReference<Context> contextRef;

        public SatelliteAdapter(Context context) {
            contextRef = new WeakReference<>(context);
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public SatelliteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(SatelliteViewHolder.suggestedLayoutResID(), parent, false);
            return new SatelliteViewHolder(contextRef.get(), view);
        }

        @Override
        public void onBindViewHolder(@NonNull SatelliteViewHolder holder, int i) {
            holder.onBindDataToViewHolder(items.get(i));
        }

        @TargetApi(24)
        protected void updateViews(@Nullable GnssStatus status)
        {
            if (status != null)
            {
                markAllStale();
                for (int i=0; i<status.getSatelliteCount(); i++)
                {
                    SatelliteItem item = new SatelliteItem(i, status.getSvid(i), status.getConstellationType(i));
                    if (!items.contains(item)) {
                        items.add(item);
                    } else item = items.get(items.indexOf(item));
                    updateItem(status, i, item);
                }
                removeStaleItems();
                Collections.sort(items);
                notifyDataSetChanged();
            }
        }

        protected void updateViews(@Nullable GpsStatus status)
        {
            if (status != null)
            {
                markAllStale();
                int i = 0;
                for (GpsSatellite satellite : status.getSatellites())
                {
                    SatelliteItem item = new SatelliteItem(i, satellite.getPrn(), 0);
                    int position = items.indexOf(item);
                    if (position < 0) {
                        items.add(item);
                    }
                    updateItem(satellite, item);
                    i++;
                }
                removeStaleItems();
                notifyDataSetChanged();
            }
        }

        protected void updateItem(GnssStatus status, int i, SatelliteItem item)
        {
            item.snr = status.getCn0DbHz(i);
            item.hasAlmanac = status.hasAlmanacData(i);
            item.hasEphemeris = status.hasEphemerisData(i);
            item.usedInFix = status.usedInFix(i);
            item.azimuth = status.getAzimuthDegrees(i);
            item.elevation = status.getElevationDegrees(i);
            item.isStale = false;
        }

        protected void updateItem(GpsSatellite satellite, SatelliteItem item)
        {
            item.snr = satellite.getSnr();
            item.hasAlmanac = satellite.hasAlmanac();
            item.hasEphemeris = satellite.hasEphemeris();
            item.usedInFix = satellite.usedInFix();
            item.azimuth = satellite.getAzimuth();
            item.elevation = satellite.getElevation();
            item.isStale = false;
        }

        protected void markAllStale() {
            for (SatelliteItem item : items) {
                item.isStale = true;
            }
        }
        protected void removeStaleItems() {
            while ((removeFirstStaleItem()) >= 0) {
                /* EMPTY */
            }
        }
        protected int removeFirstStaleItem()
        {
            for (int i=0; i<items.size(); i++)
            {
                SatelliteItem item = items.get(i);
                if (item.isStale) {
                    items.remove(item);
                    return i;
                }
            }
            return -1;
        }

        public int getUsedItemCount()
        {
            int i = 0;
            for (SatelliteItem item : items) {
                if (item.usedInFix) {
                    i++;
                }
            }
            return i;
        }
    }

    /**
     * SatelliteViewHolderOptions
     */
    public static class SatelliteViewHolderOptions
    {
        public int colorUsed = Color.GREEN, colorSignal = Color.BLUE, colorAlmanac = Color.BLUE, colorEphemeris = Color.BLUE;
        private boolean debug = false;

        public SatelliteViewHolderOptions(Context context)
        {
            if (context != null)
            {
                initConstellationColors(context);
                colorUsed = ContextCompat.getColor(context, R.color.gnss_used);
                colorEphemeris = ContextCompat.getColor(context, R.color.gnss_signal_ephemeris);
                colorAlmanac = ContextCompat.getColor(context, R.color.gnss_signal_almanac);
                colorSignal = ContextCompat.getColor(context, R.color.gnss_signal);
                debug = LocationHelperSettings.keepLastLocationLog(context);
            }
        }

        private final HashMap<Integer, Integer> constellationColors = new HashMap<>();
        private void initConstellationColors(Context context) {
            int[] colors = context.getResources().getIntArray(R.array.gnss_constellationsColors);
            for (int i=0; i<colors.length; i++) {
                constellationColors.put(i, colors[i]);
            }
        }
        private int getConstellationColor(int constellation) {
            if (constellationColors.containsKey(constellation)) {
                return constellationColors.get(constellation);
            }
            return Color.LTGRAY;
        }
    }

    /**
     * SatelliteViewHolder
     */
    public static class SatelliteViewHolder extends RecyclerView.ViewHolder
    {
        public View layout;
        public View bar_signal;
        public View bar_used;
        public View bar_constellation;
        public TextView text_id;

        public SatelliteViewHolderOptions options;

        public SatelliteViewHolder(Context context, @NonNull View itemView)
        {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_item);
            bar_signal = itemView.findViewById(R.id.bar_signal);
            bar_used = itemView.findViewById(R.id.bar_used);
            bar_constellation = itemView.findViewById(R.id.bar_constellation);
            text_id = itemView.findViewById(R.id.text_id);
            options = new SatelliteViewHolderOptions(context);
        }

        protected void onBindDataToViewHolder(SatelliteItem item)
        {
            if (bar_signal != null) {
                bar_signal.setScaleY(item.snr == 0 ? 0 : (float) (item.snr / SatelliteItem.MAX_SNR));
                bar_signal.setBackgroundColor(
                        item.hasEphemeris ? options.colorEphemeris
                        : item.hasAlmanac ? options.colorAlmanac : options.colorSignal);
            }
            if (bar_used != null) {
                bar_used.setBackgroundColor(options.colorUsed);
                bar_used.setVisibility(item.usedInFix ? View.VISIBLE : View.INVISIBLE);
            }
            if (bar_constellation != null) {
                bar_constellation.setBackgroundColor(options.getConstellationColor(item.constellation));
            }
            if (text_id != null)
            {
                if (options.debug) {
                    String symbol = item.hasEphemeris ? GpsDebugDisplay.SYMBOL_HAS_EPHEMERIS
                            : item.hasAlmanac ? GpsDebugDisplay.SYMBOL_HAS_ALMANAC : GpsDebugDisplay.SYMBOL_HAS_NO_ALMANAC;
                    text_id.setText(symbol);
                } else {
                    text_id.setText(item.id + "");
                }
            }
        }

        public static int suggestedLayoutResID() {
            return R.layout.layout_listitem_gnssbar;
        }
    }

}
