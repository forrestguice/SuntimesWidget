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
import com.forrestguice.support.content.ContextCompat;

import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.widget.LinearLayoutManager;
import com.forrestguice.support.widget.RecyclerView;

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

        initViewListener();
    }

    @TargetApi(24)
    @Override
    protected void updateViews(@Nullable GnssStatus status) {
        if (adapter != null) {
            adapter.updateViews(status);
        }
        updateViews(getContext());
    }

    @Override
    protected void updateViews(@Nullable GpsStatus status) {
        if (adapter != null) {
            adapter.updateViews(status);
        }
        updateViews(getContext());
    }

    protected void updateViews(Context context)
    {
        if (label != null) {
            label.setText((adapter != null && context != null)
                    ? context.getString(R.string.configLabel_getFix_num_satellites, adapter.getUsedItemCount() + "", adapter.getItemCount() + "")
                    : "");
            label.setVisibility(adapter.getOptions().showLabels ? View.VISIBLE : View.INVISIBLE);
        }
        updateSatellitePopup(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH)
    {
        super.onSizeChanged(w, h, oldW, oldH);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void toggleLabelVisibility(Context context)
    {
        if (adapter != null && context != null)
        {
            SatelliteViewHolderOptions options = adapter.getOptions();

            if (!options.showLabels && !options.debug) {
                options.showLabels = true;
            } else if (options.showLabels && !options.debug) {
                options.debug = true;
            } else {
                options.showLabels = false;
                options.debug = false;
            }

            adapter.notifyDataSetChanged();
            updateViews(context);
        }
    }

    /**
     * SatellitePopupView
     */
    public void showSatellitePopup(View v, SatelliteItem item)
    {
        if (popup != null) {
            popup.dismiss();
            return;
        }

        popupItem = new GnssStatusItemView(v.getContext());
        popupItem.updateViews(v.getContext(), item);
        popup = new PopupWindow(popupItem, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        popup.setOnDismissListener(onSatellitePopupDismissed);
        popup.showAtLocation(v, Gravity.END, 0, getTop());
    }
    private final PopupWindow.OnDismissListener onSatellitePopupDismissed= new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            popup = null;
            popupItem = null;
        }
    };
    protected void updateSatellitePopup(Context context) {
        if (popupItem != null && adapter != null) {
            SatelliteItem item = adapter.getItem(popupItem.id, popupItem.constellation);
            if (item != null) {
                popupItem.updateViews(context, item);
            } else popup.dismiss();
        }
    }
    @Nullable
    protected PopupWindow popup = null;
    @Nullable
    protected GnssStatusItemView popupItem;

    /**
     * SatelliteAdapter
     */
    public static class SatelliteAdapter extends RecyclerView.Adapter<SatelliteViewHolder>
    {
        private final ArrayList<SatelliteItem> items = new ArrayList<>();
        private final WeakReference<Context> contextRef;
        private final SatelliteViewHolderOptions options;

        public SatelliteAdapter(Context context) {
            contextRef = new WeakReference<>(context);
            options = new SatelliteViewHolderOptions(context);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public SatelliteViewHolderOptions getOptions() {
            return options;
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
        public void onBindViewHolder(@NonNull SatelliteViewHolder holder, int i)
        {
            SatelliteItem item = items.get(i);
            holder.onBindDataToViewHolder(item, options);
            attachClickListeners(holder, item);
        }

        @Override
        public void onViewRecycled(@NonNull SatelliteViewHolder holder)
        {
            detachClickListeners(holder);
            super.onViewRecycled(holder);
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

            } else {
                items.clear();
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
                    int constellation = (satellite.getPrn() > 65 && satellite.getPrn() < 88) ? 3 : 1;    // GnssStatus.CONSTELLATION_GPS(1), GLONASS(3)
                    SatelliteItem item = new SatelliteItem(i, satellite.getPrn(), constellation);
                    int position = items.indexOf(item);
                    if (position < 0) {
                        items.add(item);
                    } else item = items.get(items.indexOf(item));
                    updateItem(satellite, item);
                    i++;
                }
                removeStaleItems();
                notifyDataSetChanged();
            }
        }

        @TargetApi(24)
        protected void updateItem(GnssStatus status, int i, SatelliteItem item)
        {
            item.cnr = status.getCn0DbHz(i);
            item.hasAlmanac = status.hasAlmanacData(i);
            item.hasEphemeris = status.hasEphemerisData(i);
            item.usedInFix = status.usedInFix(i);
            item.azimuth = status.getAzimuthDegrees(i);
            item.elevation = status.getElevationDegrees(i);
            item.isStale = false;
        }

        protected void updateItem(GpsSatellite satellite, SatelliteItem item)
        {
            item.cnr = satellite.getSnr();
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
            //noinspection StatementWithEmptyBody
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

        @Nullable
        public SatelliteItem getItem(int id, int constellation)
        {
            for (SatelliteItem item : items) {
                if (item.id == id && item.constellation == constellation) {
                    return item;
                }
            }
            return null;
        }

        protected void attachClickListeners(SatelliteViewHolder holder, SatelliteItem item) {
            if (hasAdapterListener()) {
                if (holder.layout != null) {
                    holder.layout.setOnClickListener(onItemClicked(item.id, item.constellation));
                    holder.layout.setOnLongClickListener(onItemLongClicked(item.id, item.constellation));
                }
            }
        }
        protected void detachClickListeners(SatelliteViewHolder holder) {
            if (holder.layout != null) {
                holder.layout.setOnClickListener(null);
            }
        }

        protected View.OnClickListener onItemClicked(final int id, final int constellation) {
            return new OnClickListener() {
                public void onClick(View v) {
                    notifyItemClicked(v, getItem(id, constellation));
                }
            };
        }

        protected View.OnLongClickListener onItemLongClicked(final int id, final int constellation) {
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return notifyItemLongClicked(v, getItem(id, constellation));
                }
            };
        }

        /**
         * AdapterListener
         */
        public interface AdapterListener
        {
            void onItemClicked(View v, SatelliteAdapter adapter, SatelliteItem item);
            boolean onItemLongClicked(View v, SatelliteAdapter adapter, SatelliteItem item);
            void onOptionsChanged(SatelliteViewHolderOptions options);
        }

        public void setAdapterListener(AdapterListener listener) {
            adapterListener = listener;
        }
        protected boolean hasAdapterListener() {
            return adapterListener != null;
        }
        private AdapterListener adapterListener = null;

        protected void notifyItemClicked(View v, SatelliteItem item) {
            if (item != null && adapterListener != null) {
                adapterListener.onItemClicked(v, SatelliteAdapter.this, item);
            }
        }
        protected boolean notifyItemLongClicked(View v, SatelliteItem item) {
            if (item != null && adapterListener != null) {
                return adapterListener.onItemLongClicked(v, this, item);
            } else return false;
        }

        public void notifyOptionsChanged() {
            if (adapterListener != null) {
                adapterListener.onOptionsChanged(options);
            }
        }
    }

    /**
     * SatelliteViewHolderOptions
     */
    public static class SatelliteViewHolderOptions
    {
        public int colorUsed = Color.GREEN, colorSignal = Color.BLUE, colorAlmanac = Color.BLUE, colorEphemeris = Color.BLUE;
        public boolean debug = false;
        public boolean showLabels = false;

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
            if (constellationColors.containsKey(constellation))
            {
                Integer result = constellationColors.get(constellation);
                if (result != null) {
                    return result;
                }
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
        public View bar_signal, bar_signal1;
        public View bar_used;
        public View bar_constellation;
        public TextView text_id;
        public TextView text_debug;

        public SatelliteViewHolder(Context context, @NonNull View itemView)
        {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_item);
            bar_signal = itemView.findViewById(R.id.bar_signal);
            bar_signal1 = itemView.findViewById(R.id.bar_signal1);
            bar_used = itemView.findViewById(R.id.bar_used);
            bar_constellation = itemView.findViewById(R.id.bar_constellation);
            text_id = itemView.findViewById(R.id.text_id);
            text_debug = itemView.findViewById(R.id.text_debug);
        }

        protected void onBindDataToViewHolder(SatelliteItem item, SatelliteViewHolderOptions options)
        {
            if (bar_signal != null) {
                bar_signal.setScaleY(item.cnr == 0 ? 0 : (float) (item.cnr / SatelliteItem.MAX_CNR));
                bar_signal.setBackgroundColor(
                        item.hasEphemeris ? options.colorEphemeris
                                : item.hasAlmanac ? options.colorAlmanac : options.colorSignal);
            }
            if (bar_signal1 != null) {
                bar_signal1.setBackgroundColor(
                        item.hasEphemeris ? options.colorEphemeris
                                : item.hasAlmanac ? options.colorAlmanac : 0);
                bar_signal1.setVisibility(item.cnr <= 0 ? View.VISIBLE : View.INVISIBLE);
            }
            if (bar_used != null) {
                bar_used.setBackgroundColor(options.colorUsed);
                bar_used.setVisibility(item.usedInFix ? View.VISIBLE : View.INVISIBLE);
            }
            if (bar_constellation != null) {
                bar_constellation.setBackgroundColor(options.getConstellationColor(item.constellation));
            }
            if (text_debug != null) {
                String symbol = item.hasEphemeris ? GpsDebugDisplay.SYMBOL_HAS_EPHEMERIS
                        : item.hasAlmanac ? GpsDebugDisplay.SYMBOL_HAS_ALMANAC : GpsDebugDisplay.SYMBOL_HAS_NO_ALMANAC;
                text_debug.setText(options.debug && options.showLabels ? symbol : "");
                text_debug.setVisibility(options.debug && options.showLabels ? View.VISIBLE : View.GONE);
            }
            if (text_id != null) {
                text_id.setText(options.showLabels ? item.id + "" : "");
                text_id.setVisibility(options.showLabels ? View.VISIBLE : View.GONE);
            }
        }

        public static int suggestedLayoutResID() {
            return R.layout.layout_listitem_gnssbar;
        }
    }

    /**
     * ViewListener
     */
    public interface ViewListener extends SatelliteAdapter.AdapterListener {
        /* EMPTY */
    }

    public void setViewListener(@Nullable ViewListener listener) {
        viewListener = listener;
        if (adapter != null) {
            adapter.setAdapterListener(viewListener);
        }
    }

    protected ViewListener viewListener;
    protected void initViewListener()
    {
        View clickArea = findViewById(R.id.clickArea);
        if (clickArea != null) {
            clickArea.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleLabelVisibility(getContext());
                }
            });
        }

        viewListener = new ViewListener()
        {
            @Override
            public void onItemClicked(View v, SatelliteAdapter adapter, SatelliteItem item) {
                showSatellitePopup(v, item);
            }

            @Override
            public boolean onItemLongClicked(View v, SatelliteAdapter adapter, SatelliteItem item) {
                toggleLabelVisibility(getContext());
                return true;
            }

            @Override
            public void onOptionsChanged(SatelliteViewHolderOptions options) {
                updateViews(getContext());
            }
        };
        if (adapter != null) {
            adapter.setAdapterListener(viewListener);
        }
    }

}
