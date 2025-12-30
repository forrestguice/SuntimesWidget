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

import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @see EquinoxDatasetViewHolder
 */
public class EquinoxDatasetAdapter extends RecyclerView.Adapter<EquinoxDatasetViewHolder>
{
    public static final int MAX_POSITIONS = 200;
    public static final int CENTER_POSITION = 100;
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, SuntimesEquinoxSolsticeDataset> data = new HashMap<>();

    private final WeakReference<Context> contextRef;
    private final EquinoxViewOptions options;

    public EquinoxDatasetAdapter(Context context, EquinoxViewOptions options)
    {
        this.contextRef = new WeakReference<>(context);
        this.options = options;
    }

    @NonNull
    @Override
    public EquinoxDatasetViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(EquinoxDatasetViewHolder.getSuggestedLayoutResID(), parent, false);
        return new EquinoxDatasetViewHolder(view, options);
    }

    @Override
    public void onBindViewHolder(@NonNull EquinoxDatasetViewHolder holder, int position)
    {
        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("EquinoxViewAdapter", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("EquinoxViewAdapter", "onBindViewHolder: null view holder!");
            return;
        }
        SuntimesEquinoxSolsticeDataset dataset = initData(context, position);
        holder.bindDataToPosition(context, dataset, position, options);
        holder.setSelected(getSelection());

        if (dataset.isCalculated() && dataset.isImplemented())
        {
            holder.enableNotes(!options.minimized);
            if (position == options.highlightPosition || options.minimized)
            {
                EquinoxDatasetViewHolder.EquinoxNote nextNote = findClosestNote(dataset.now(), options.trackingMode, holder.notes);
                if (nextNote == null) {
                    nextNote = holder.notes.get(0);
                }
                if (nextNote != null) {
                    nextNote.setHighlighted(true);
                }
            }
        }

        attachListeners(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull EquinoxDatasetViewHolder holder)
    {
        detachListeners(holder);

        if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2))
        {
            data.remove(holder.position);
            //Log.d("DEBUG", "remove data " + holder.position);
        }
        holder.position = RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemCount() {
        return MAX_POSITIONS;
    }

    public boolean hasSelection() {
        return (selected_mode != null);
    }
    public SolsticeEquinoxMode getSelection() {
        return this.selected_mode;
    }
    public void setSelection(@Nullable SolsticeEquinoxMode mode ) {
        this.selected_mode = mode;
        notifyDataSetChanged();
    }
    protected SolsticeEquinoxMode selected_mode = null;

    public void setSelection(Integer position) {
        selected_position = position;
        notifyDataSetChanged();
    }
    protected Integer selected_position = null;

    /**
     * Clear existing data and initialize the center position.
     * @param context Context
     * @return data for the center position
     */
    public SuntimesEquinoxSolsticeDataset initData(Context context)
    {
        data.clear();
        SuntimesEquinoxSolsticeDataset retValue = initData(context, CENTER_POSITION);
        notifyDataSetChanged();
        return retValue;
    }

    /**
     * Initialize data at position (returns cached data if it already exists).
     * @param context context
     * @param position position
     * @return data
     */
    public SuntimesEquinoxSolsticeDataset initData(Context context, int position)
    {
        SuntimesEquinoxSolsticeDataset retValue = data.get(position);
        if (retValue == null) {
            data.put(position, retValue = createData(context, position));   // data is removed in onViewRecycled
            //Log.d("DEBUG", "add data " + position);
        }
        return retValue;
    }

    protected SuntimesEquinoxSolsticeDataset createData(Context context, int position)
    {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, position - CENTER_POSITION);

        SuntimesEquinoxSolsticeDataset retValue = new SuntimesEquinoxSolsticeDataset(context, 0);
        retValue.setTodayIs(date);
        retValue.calculateData(context);
        return retValue;
    }

    public int highlightNote(Context context)
    {
        ArrayList<Pair<Integer,Calendar>> pageInfo = new ArrayList<>();
        int position = CENTER_POSITION - 1;
        do {
            SuntimesEquinoxSolsticeDataset dataset1 = initData(context, position);
            pageInfo.add(new Pair<Integer,Calendar>(position, dataset1.dataEquinoxSpring.eventCalendarThisYear()));
            pageInfo.add(new Pair<Integer,Calendar>(position, dataset1.dataEquinoxAutumnal.eventCalendarThisYear()));
            pageInfo.add(new Pair<Integer,Calendar>(position, dataset1.dataSolsticeSummer.eventCalendarThisYear()));
            pageInfo.add(new Pair<Integer,Calendar>(position, dataset1.dataSolsticeWinter.eventCalendarThisYear()));
            position++;
        } while (position < CENTER_POSITION + 2);

        SuntimesEquinoxSolsticeDataset dataset = initData(context, CENTER_POSITION);
        options.highlightPosition = findClosestPage(dataset.now(), options.trackingMode, pageInfo);

        notifyDataSetChanged();
        return options.highlightPosition;
    }

    @Deprecated
    public void setThemeOverride( SuntimesTheme theme ) {
        options.themeOverride = theme;
    }

    private EquinoxAdapterListener adapterListener;
    public void setAdapterListener( EquinoxAdapterListener listener ) {
        adapterListener = listener;  // TODO
    }

    private void attachListeners(final EquinoxDatasetViewHolder holder, final int position)
    {
        holder.title.setOnClickListener(onTitleClick(position));
        holder.btn_flipperNext.setOnClickListener(onNextClick(position));
        holder.btn_flipperPrev.setOnClickListener(onPrevClick(position));

        for (int i=0; i <holder.notes.size(); i++) {
            EquinoxDatasetViewHolder.EquinoxNote note = holder.notes.get(i);
            if (note.contextMenu != null && note.time != null) {
                note.contextMenu.setOnClickListener(onMenuClick(note.contextMenu, position, SolsticeEquinoxMode.values()[i], note.time.getTimeInMillis()));
            }
        }

        for (int i=0; i <holder.clickAreas.length; i++) {
            if (holder.clickAreas[i] != null) {
                holder.clickAreas[i].setOnClickListener(onNoteClick(holder, position, i));
                holder.clickAreas[i].setVisibility(options.minimized ? View.GONE : View.VISIBLE);
            }
        }

        if (options.minimized) {
            holder.clickArea.setOnClickListener(onClick(position));
            holder.clickArea.setOnLongClickListener(onLongClick(position));
        }
    }

    private void detachListeners(EquinoxDatasetViewHolder holder)
    {
        holder.title.setOnClickListener(null);
        holder.btn_flipperNext.setOnClickListener(null);
        holder.btn_flipperPrev.setOnClickListener(null);
        holder.clickArea.setOnClickListener(null);
        holder.clickArea.setOnLongClickListener(null);
    }

    private View.OnClickListener onClick( final int position ) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterListener != null) {
                    adapterListener.onClick(position);
                }
            }
        };
    }
    private View.OnLongClickListener onLongClick( final int position ) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (adapterListener != null) {
                    return adapterListener.onLongClick(position);
                } else return false;
            }
        };
    }
    private View.OnClickListener onTitleClick( final int position ) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapterListener != null) {
                    adapterListener.onTitleClick(position);
                }
            }
        };
    }
    private View.OnClickListener onNextClick( final int position ) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterListener != null) {
                    adapterListener.onNextClick(position);
                }
            }
        };
    }
    private View.OnClickListener onPrevClick( final int position ) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterListener != null) {
                    adapterListener.onPrevClick(position);
                }
            }
        };
    }
    private View.OnClickListener onMenuClick(final View v, final int position, final SolsticeEquinoxMode selection, final long selectionTime) {
        return new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterListener != null) {
                    adapterListener.onMenuClick(v, position, selection, selectionTime);
                }
            }
        });
    }
    private View.OnClickListener onNoteClick(final EquinoxDatasetViewHolder holder, final int position, final int i)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SolsticeEquinoxMode mode = SolsticeEquinoxMode.values()[i];
                if (holder.getSelected() == mode) {
                    holder.notes.get(i).contextMenu.performClick();

                } else {
                    setSelection(mode);
                    if (adapterListener != null) {
                        adapterListener.onSelected(i, mode);
                    }
                }
            }
        };
    }

    public static EquinoxDatasetViewHolder.EquinoxNote findClosestNote(Calendar now, TrackingMode mode, ArrayList<EquinoxDatasetViewHolder.EquinoxNote> notes)
    {
        if (notes == null || now == null) {
            return null;
        }

        boolean upcoming = (mode == TrackingMode.SOONEST);
        boolean recent = (mode == TrackingMode.RECENT);

        EquinoxDatasetViewHolder.EquinoxNote closest = null;
        long timeDeltaMin = Long.MAX_VALUE;
        for (EquinoxDatasetViewHolder.EquinoxNote note : notes)
        {
            Calendar noteTime = note.getTime();
            if (noteTime != null)
            {
                if ((upcoming && !noteTime.after(now)) || (recent && !noteTime.before(now)))
                    continue;

                long timeDelta = Math.abs(noteTime.getTimeInMillis() - now.getTimeInMillis());
                if (timeDelta < timeDeltaMin)
                {
                    timeDeltaMin = timeDelta;
                    closest = note;
                }
            }
        }
        return closest;
    }

    public static int findClosestPage(Calendar now, TrackingMode mode, ArrayList<Pair<Integer, Calendar>> notes)
    {
        if (notes == null || now == null) {
            return -1;
        }

        boolean upcoming = (mode == TrackingMode.SOONEST);
        boolean recent = (mode == TrackingMode.RECENT);

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