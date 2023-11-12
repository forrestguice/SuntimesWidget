/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.equinox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @see EquinoxDataViewHolder
 */
public class EquinoxDataAdapter extends RecyclerView.Adapter<EquinoxDataViewHolder>
{
    public static final int MAX_POSITIONS = 208;
    public static final int CENTER_POSITION = 104;

    protected final WeakReference<Context> contextRef;
    protected final EquinoxViewOptions options;

    public EquinoxDataAdapter(Context context, WidgetSettings.SolsticeEquinoxMode[] modes, EquinoxViewOptions options)
    {
        this.contextRef = new WeakReference<>(context);
        this.modes = modes;
        this.options = options;
    }

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, SuntimesEquinoxSolsticeData> data = new HashMap<>();

    @Override
    public EquinoxDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(EquinoxDataViewHolder.suggestedLayoutResID(), parent, false);
        return new EquinoxDataViewHolder(view);
    }

    /**
     * Initialize data at position (returns cached data if it already exists).
     * @param context
     * @param position
     * @return
     */
    public SuntimesEquinoxSolsticeData initData(Context context, int position)
    {
        SuntimesEquinoxSolsticeData retValue = data.get(position);
        if (retValue == null) {
            data.put(position, retValue = createData(context, position));   // data is removed in onViewRecycled
            //Log.d("DEBUG", "add data " + position);
        }
        return retValue;
    }

    protected SuntimesEquinoxSolsticeData createData(Context context, int position)
    {
        int n = modes.length;
        int d = position - CENTER_POSITION;
        int i = d % n;
        int y = d / n;

        if (i < 0) {
            i = n + i;
            y -= 1;
        }

        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, y);

        SuntimesEquinoxSolsticeData retValue = new SuntimesEquinoxSolsticeData(context, 0);
        retValue.setTimeMode(modes[i]);
        retValue.setTodayIs(date);
        retValue.calculate();
        return retValue;
    }

    @Override
    public void onViewRecycled(EquinoxDataViewHolder holder)
    {
        detachListeners(holder);
        if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2)) {
            data.remove(holder.position);
            //Log.d("DEBUG", "remove data " + holder.position);
        }
        holder.position = RecyclerView.NO_POSITION;
    }

    @Override
    public void onBindViewHolder(EquinoxDataViewHolder holder, int position)
    {
        Context context = contextRef.get();
        if (context == null) {
            Log.w("EquinoxDataAdapter", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("EquinoxDataAdapter", "onBindViewHolder: null view holder!");
            return;
        }

        holder.selected = (selected_position != null && (position == selected_position));
        holder.highlighted = (position == options.highlightPosition);
        holder.bindDataToPosition(context, initData(context, position), position, options);

        if (options.columnWidthPx >= 0) {
            holder.adjustLabelWidth(options.columnWidthPx);
        }

        attachListeners(holder, position);
    }

    @Override
    public int getItemCount() {
        return MAX_POSITIONS;
    }

    protected WidgetSettings.SolsticeEquinoxMode[] modes = WidgetSettings.SolsticeEquinoxMode.values();
    public void setModes(WidgetSettings.SolsticeEquinoxMode[] modes) {
        this.modes = modes;
    }

    private EquinoxAdapterListener adapterListener;
    public void setAdapterListener( EquinoxAdapterListener listener ) {
        adapterListener = listener;
    }

    private void attachListeners(final EquinoxDataViewHolder holder, final int position)
    {
        holder.clickArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (adapterListener != null) {
                    adapterListener.onClick(position);
                }
            }
        });

        Context context = contextRef.get();
        SuntimesEquinoxSolsticeData data = initData(context, position);
        Calendar calendar = data.eventCalendarThisYear();
        if (calendar == null) {
            calendar = Calendar.getInstance();
            Log.e("DEBUG", "calendar is null!");
        }
        holder.button_menu.setOnClickListener(onMenuClick(holder.button_menu, position, data.timeMode(), calendar.getTimeInMillis()));
    }

    private void detachListeners(EquinoxDataViewHolder holder)
    {
        holder.clickArea.setOnClickListener(null);
        holder.button_menu.setOnClickListener(null);
    }

    public void setThemeOverride( SuntimesTheme theme ) {
        options.themeOverride = theme;
    }

    public boolean hasSelection() {
        return (selected_position != null);
    }
    public WidgetSettings.SolsticeEquinoxMode getSelection() {
        return this.selected_mode;
    }
    public void setSelection(@Nullable WidgetSettings.SolsticeEquinoxMode mode ) {
        this.selected_mode = mode;
        notifyDataSetChanged();
    }
    protected WidgetSettings.SolsticeEquinoxMode selected_mode = null;

    public void setSelection(Integer position) {
        selected_position = position;
        notifyDataSetChanged();
    }
    protected Integer selected_position = null;

    private View.OnClickListener onMenuClick(final View v, final int position, final WidgetSettings.SolsticeEquinoxMode selection, final long selectionTime) {
        return new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterListener != null) {
                    adapterListener.onMenuClick(v, position, selection, selectionTime);
                }
            }
        });
    }

    public int highlightNote(Context context)
    {
        ArrayList<Pair<Integer,Calendar>> notes = new ArrayList<>();
        int position = CENTER_POSITION - 16;
        do {
            SuntimesEquinoxSolsticeData data1 = initData(context, position);
            notes.add(new Pair<Integer,Calendar>(position, data1.eventCalendarThisYear()));
            position++;
        } while (position < CENTER_POSITION + 16);

        return highlightPosition(findClosestNote(Calendar.getInstance(), options.trackingMode, notes));
    }

    public int highlightPosition(int position)
    {
        if (options.highlightPosition != position) {
            options.highlightPosition = position;
            notifyDataSetChanged();
        }
        return options.highlightPosition;
    }

    public static int findClosestNote(Calendar now, WidgetSettings.TrackingMode mode, ArrayList<Pair<Integer, Calendar>> notes)
    {
        if (notes == null || now == null) {
            return -1;
        }

        boolean upcoming = (mode == WidgetSettings.TrackingMode.SOONEST);
        boolean recent = (mode == WidgetSettings.TrackingMode.RECENT);

        Integer closest = null;
        long timeDeltaMin = Long.MAX_VALUE;
        for (Pair<Integer, Calendar> note : notes)
        {
            Calendar noteTime = note.second;
            if (noteTime != null)
            {
                if ((upcoming && !noteTime.after(now)) || (recent && !noteTime.before(now)))
                    continue;

                long timeDelta = Math.abs(noteTime.getTimeInMillis() - now.getTimeInMillis());
                if (timeDelta < timeDeltaMin)
                {
                    timeDeltaMin = timeDelta;
                    closest = note.first;
                }
            }
        }
        return closest != null ? closest : -1;
    }

}