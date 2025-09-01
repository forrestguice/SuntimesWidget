/**
    Copyright (C) 2017-2021 Forrest Guice
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
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.TrackingMode;
import com.forrestguice.suntimeswidget.cards.CardAdapter;
import com.forrestguice.suntimeswidget.cards.CardLayoutManager;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
@Deprecated
public class EquinoxView extends LinearLayout
{
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedEquinoxCard";
    public static final String KEY_UI_CARDPOSITION = "equinoxCardPosition";
    public static final String KEY_UI_MINIMIZED = "equinoxIsMinimized";

    private static SuntimesUtils utils = new SuntimesUtils();
    private boolean userSwappedCard = false;

    private TextView empty, text_title, text_year_length;
    private ImageButton btn_next, btn_prev, btn_menu;
    private RecyclerView card_view;
    private CardLayoutManager card_layout;
    private EquinoxViewAdapter card_adapter;
    //private CardAdapter.CardViewScroller card_scroller;

    public EquinoxView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EquinoxView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        options.init(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_equinox, this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            options.centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);

        text_title = (TextView)findViewById(R.id.text_title1);
        if (text_title != null) {
            text_title.setOnClickListener(onTitleClicked);
        }

        btn_next = (ImageButton)findViewById(R.id.info_time_nextbtn1);
        if (btn_next != null) {
            btn_next.setOnClickListener(onNextClicked);
        }

        btn_prev = (ImageButton)findViewById(R.id.info_time_prevbtn1);
        if (btn_prev != null) {
            btn_prev.setOnClickListener(onPrevClicked);
        }

        btn_menu = (ImageButton)findViewById(R.id.menu_button);
        if (btn_menu != null) {
            btn_menu.setOnClickListener(onMenuClicked);
        }

        text_year_length = (TextView)findViewById(R.id.info_time_year_length);

        card_view = (RecyclerView)findViewById(R.id.info_equinoxsolstice_flipper1);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.setLayoutManager(card_layout = new CardLayoutManager(context));
        //card_view.addItemDecoration(new CardAdapter.CardViewDecorator(context));

        card_adapter = new EquinoxViewAdapter(context, options);
        card_adapter.setEquinoxViewListener(cardListener);
        card_view.setAdapter(card_adapter);
        card_view.scrollToPosition(EquinoxViewAdapter.CENTER_POSITION);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        boolean minimized = isMinimized();
        if (!minimized) {
            card_view.setOnScrollListener(onCardScrollListener);
        }
        card_view.setLayoutFrozen(minimized);

        if (isInEditMode()) {
            updateViews(context);
        }
        themeViews(context);
    }

    private EquinoxViewOptions options = new EquinoxViewOptions();

    @SuppressLint("ResourceType")
    private void themeViews(Context context) {
        themeHeaderViews();
        themePanelViews();
    }

    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null)
        {
            options.init(theme);
            card_adapter.setThemeOverride(theme);
        }
        themeHeaderViews();
        themePanelViews();
    }

    protected void themeHeaderViews()
    {
        text_title.setTextColor(options.titleColor);
        if (options.titleSizeSp != null)
        {
            text_title.setTextSize(options.titleSizeSp);
            text_title.setTypeface(text_title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
        }

        ImageViewCompat.setImageTintList(btn_next, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));
        ImageViewCompat.setImageTintList(btn_prev, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));
    }
    protected void themePanelViews()
    {
        if (options.textColor != null) {
            text_year_length.setTextColor(options.textColor);
        }
        if (options.timeSizeSp != null) {
            text_year_length.setTextSize(options.timeSizeSp);
        }
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        options.isRtl = AppSettings.isLocaleRtl(context);
    }

    public void setTrackingMode(TrackingMode mode) {
        options.trackingMode = mode;
    }
    public TrackingMode getTrackingMode() {
        return options.trackingMode;
    }

    public void setMinimized( boolean value ) {
        options.minimized = value;
    }
    public boolean isMinimized() {
        return options.minimized;
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        card_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews(Context context)
    {
        SuntimesEquinoxSolsticeDataset data = card_adapter.initData(context, EquinoxViewAdapter.CENTER_POSITION);
        showEmptyView(data == null || !data.isImplemented());

        View header = findViewById(R.id.dialog_header);
        if (header != null) {
            header.setVisibility(isMinimized() ? View.GONE : View.VISIBLE);
        }

        View infoPanel = findViewById(R.id.year_info_layout);
        if (infoPanel != null) {
            infoPanel.setVisibility(isMinimized() ? View.GONE : View.VISIBLE);
        }

        int position = card_adapter.highlightNote(context);
        if (position != -1 && !userSwappedCard) {
            card_view.setLayoutFrozen(false);
            card_view.scrollToPosition(position);
            card_view.setLayoutFrozen(isMinimized());
        }
    }

    protected void updateViews(Context context,  SuntimesEquinoxSolsticeDataset data)
    {
        text_title.setText(utils.calendarDateYearDisplayString(context, data.dataEquinoxSpring.eventCalendarThisYear()).toString());

        long yearLengthMillis = data.tropicalYearLength();
        double yearLengthDays = yearLengthMillis / 1000d / 60d / 60d / 24;
        String timeString = utils.timeDeltaLongDisplayString(yearLengthMillis);
        String daysString = context.getResources().getQuantityString(R.plurals.units_days, (int)yearLengthDays, utils.formatDoubleValue(yearLengthDays, 6));
        String yearString = context.getString(R.string.length_tropical_year, timeString, daysString);
        CharSequence yearDisplay = SuntimesUtils.createBoldColorSpan(null, yearString, timeString, options.noteColor);

        text_year_length.setText(yearDisplay);
    }

    public boolean isImplemented(Context context)
    {
        SuntimesEquinoxSolsticeDataset data = card_adapter.initData(context, EquinoxViewAdapter.CENTER_POSITION);
        return (data != null && data.isImplemented());
    }

    public boolean saveState(Bundle bundle)
    {
        bundle.putInt(EquinoxView.KEY_UI_CARDPOSITION, currentCardPosition());
        bundle.putBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        bundle.putBoolean(EquinoxView.KEY_UI_MINIMIZED, options.minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        userSwappedCard = bundle.getBoolean(EquinoxView.KEY_UI_USERSWAPPEDCARD, false);
        options.minimized = bundle.getBoolean(EquinoxView.KEY_UI_MINIMIZED, options.minimized);

        int cardPosition = bundle.getInt(EquinoxView.KEY_UI_CARDPOSITION, EquinoxViewAdapter.CENTER_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = EquinoxViewAdapter.CENTER_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap
    }

    public boolean showNextCard(int position)
    {
        int nextPosition = (position + 1);
        if (nextPosition < card_adapter.getItemCount()) {
            userSwappedCard = true;
            CardAdapter.CardViewScroller card_scroller = new CardAdapter.CardViewScroller(getContext());
            card_scroller.setTargetPosition(nextPosition);
            card_layout.startSmoothScroll(card_scroller);
        }
        return true;
    }

    public boolean showPreviousCard(int position)
    {
        int prevPosition = (position - 1);
        if (prevPosition >= 0) {
            userSwappedCard = true;
            CardAdapter.CardViewScroller card_scroller = new CardAdapter.CardViewScroller(getContext());
            card_scroller.setTargetPosition(prevPosition);
            card_layout.startSmoothScroll(card_scroller);
        }
        return true;
    }

    private View.OnClickListener onClickListener;
    public void setOnClickListener( View.OnClickListener listener ) {
        onClickListener = listener;
    }

    private View.OnLongClickListener onLongClickListener;
    public void setOnLongClickListener( View.OnLongClickListener listener) {
        onLongClickListener = listener;
    }

    private RecyclerView.OnScrollListener onCardScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            int position = currentCardPosition();
            if (position >= 0) {
                SuntimesEquinoxSolsticeDataset data = card_adapter.initData(getContext(), position);
                updateViews(getContext(), data);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState ==  RecyclerView.SCROLL_STATE_DRAGGING) {
                userSwappedCard = true;
            }
        }
    };

    private EquinoxViewListener cardListener = new EquinoxViewListener()
    {
        @Override
        public void onClick( int position ) {
            if (onClickListener != null) {
                onClickListener.onClick(EquinoxView.this);
            }
            if (viewListener != null) {
                viewListener.onClick(position);
            }

        }
        @Override
        public boolean onLongClick( int position ) {
            if (onLongClickListener != null) {
                return onLongClickListener.onLongClick(EquinoxView.this);
            } else return false;
        }
        @Override
        public void onTitleClick( int position ) {
            onTitleClicked(position);
            if (viewListener != null) {
                viewListener.onTitleClick(position);
            }
        }
        @Override
        public void onNextClick( int position ) {
            onNextClicked(position);
            if (viewListener != null) {
                viewListener.onNextClick(position);
            }
        }
        @Override
        public void onPrevClick( int position ) {
            onPrevClicked(position);
            if (viewListener != null) {
                viewListener.onPrevClick(position);
            }
        }
        @Override
        public void onMenuClick(View view, int position, SolsticeEquinoxMode mode, long datetime) {
            if (viewListener != null) {
                viewListener.onMenuClick(view, position, mode, datetime);
            }
        }
    };

    public void lockScrolling() {
        card_view.setLayoutFrozen(true);
    }

    public void unlockScrolling() {
        card_view.setLayoutFrozen(isMinimized());
    }

    public int currentCardPosition()
    {
        int first = card_layout.findFirstVisibleItemPosition();
        int last = card_layout.findLastVisibleItemPosition();
        int p = (first + last) / 2;
        //Log.d("DEBUG", "currentCardPosition: " + first + ", " + last + " => " + p);
        return p;
    }

    private OnClickListener onTitleClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onTitleClicked(currentCardPosition());
        }
    };
    protected void onTitleClicked(int position)
    {
        if (currentCardPosition() >= 0)
        {
            int seekPosition = options.highlightPosition;
            if (Math.abs(position - seekPosition) > SuntimesActivity.HIGHLIGHT_SCROLLING_ITEMS) {
                card_view.scrollToPosition(seekPosition);
            } else {
                CardAdapter.CardViewScroller card_scroller = new CardAdapter.CardViewScroller(getContext());
                card_scroller.setTargetPosition(seekPosition);
                card_layout.startSmoothScroll(card_scroller);
            }
        }
    }

    private OnClickListener onNextClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onNextClicked(currentCardPosition());
        }
    };
    protected void onNextClicked(int position) {
        if (position >= 0) {
            userSwappedCard = showNextCard(position);
        }
    }

    private OnClickListener onPrevClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onPrevClicked(currentCardPosition());
        }
    };
    protected void onPrevClicked(int position) {
        if (position >= 0) {
            userSwappedCard = showPreviousCard(position);
        }
    }

    private OnClickListener onMenuClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (viewListener != null) {
                viewListener.onMenuClick(v, currentCardPosition());
            }
        }
    };

    public void adjustColumnWidth(int columnWidthPx)
    {
        View yearLabel = findViewById(R.id.info_time_year_length_label);
        if (yearLabel != null) {
            ViewGroup.LayoutParams layoutParams = yearLabel.getLayoutParams();
            layoutParams.width = columnWidthPx;
            yearLabel.setLayoutParams(layoutParams);
        }

        options.columnWidthPx = columnWidthPx;
        card_adapter.notifyDataSetChanged();
    }

    public static EquinoxNote findClosestNote(Calendar now, TrackingMode mode, ArrayList<EquinoxNote> notes)
    {
        if (notes == null || now == null) {
            return null;
        }

        boolean upcoming = (mode == TrackingMode.SOONEST);
        boolean recent = (mode == TrackingMode.RECENT);

        EquinoxNote closest = null;
        long timeDeltaMin = Long.MAX_VALUE;
        for (EquinoxNote note : notes)
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

    /**
     * EquinoxNote
     */
    public static class EquinoxNote
    {
        protected TextView labelView, timeView, noteView;
        protected ImageButton contextMenu;
        protected Calendar time;
        protected boolean highlighted;
        protected int pageIndex;
        private EquinoxViewOptions options;
        protected View focusView, noteLayout;

        public EquinoxNote(TextView labelView, TextView timeView, TextView noteView, ImageButton contextMenu, View focusView, View noteLayout, int pageIndex, EquinoxViewOptions options)
        {
            this.labelView = labelView;
            this.timeView = timeView;
            this.noteView = noteView;
            this.contextMenu = contextMenu;
            this.focusView = focusView;
            this.noteLayout = noteLayout;
            this.pageIndex = pageIndex;
            this.options = options;
        }

        public void adjustLabelWidth( int labelWidthPx )
        {
            ViewGroup.LayoutParams layoutParams = labelView.getLayoutParams();
            layoutParams.width = labelWidthPx;
            labelView.setLayoutParams(layoutParams);
        }

        public void themeViews(@Nullable Integer labelColor, @Nullable Integer timeColor, @Nullable Integer textColor, @Nullable Float textSizeSp, @Nullable Float titleSizeSp, boolean titleBold)
        {
            if (labelColor != null) {
                labelView.setTextColor(SuntimesUtils.colorStateList(labelColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (timeColor != null) {
                timeView.setTextColor(SuntimesUtils.colorStateList(timeColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (textColor != null) {
                noteView.setTextColor(SuntimesUtils.colorStateList(textColor, options.disabledColor));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (textSizeSp != null) {
                noteView.setTextSize(textSizeSp);
                timeView.setTextSize(textSizeSp);
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");

            if (titleSizeSp != null) {
                labelView.setTextSize(titleSizeSp);
                labelView.setTypeface(labelView.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));
            } //else Log.w("EquinoxView", "themeViews: null color, ignoring...");
        }

        public void updateDate( Context context, Calendar time )
        {
            updateDate(context, time, true, false);
        }
        public void updateDate( Context context, Calendar time, boolean showTime, boolean showSeconds )
        {
            this.time = time;
            if (timeView != null)
            {
                SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, time, showTime, showSeconds);
                timeView.setText(timeText.toString());
            }
        }

        public void updateNote( Context context, Calendar now, boolean showWeeks, boolean showHours )
        {
            if (noteView != null)
            {
                if (now != null && time != null)
                {
                    String noteText = utils.timeDeltaDisplayString(now.getTime(), time.getTime(), showWeeks, showHours).toString();

                    if (time.before(Calendar.getInstance()))
                    {
                        String noteString = context.getString(R.string.ago, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, (options.minimized || highlighted ? options.noteColor : options.disabledColor))
                                                                         : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                        noteView.setText(noteSpan);

                    } else {
                        String noteString = context.getString(R.string.hence, noteText);
                        SpannableString noteSpan = (noteView.isEnabled() ? SuntimesUtils.createBoldColorSpan(null, noteString, noteText, options.noteColor)
                                                                         : SuntimesUtils.createBoldSpan(null, noteString, noteText));
                        noteView.setText(noteSpan);
                    }
                } else {
                    noteView.setText("");
                }
            }
        }

        public void setHighlighted( boolean highlighted )
        {
            this.highlighted = highlighted;
            //highlight(labelView, highlighted);
            highlight(timeView, highlighted);
            setEnabled(true);
            setVisible(true);
        }

        private void highlight( TextView view, boolean value )
        {
            if (view != null)
            {
                if (value)
                {
                    view.setTypeface(view.getTypeface(), Typeface.BOLD);
                    view.setPaintFlags(view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                } else {
                    view.setTypeface(view.getTypeface(), Typeface.NORMAL);
                    view.setPaintFlags(view.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                }
            }
        }

        public void setEnabled( boolean value)
        {
            labelView.setEnabled(value);
            timeView.setEnabled(value);
            noteView.setEnabled(value);
        }

        public void setEnabled()
        {
            if (time != null)
            {
                setEnabled(time.after(Calendar.getInstance()));

            } else {
                setEnabled(false);
            }
        }

        public void setVisible( boolean visible )
        {
            labelView.setVisibility( visible ? View.VISIBLE : View.GONE );
            timeView.setVisibility( visible ? View.VISIBLE : View.GONE );
            noteView.setVisibility( visible ? View.VISIBLE : View.GONE );
            noteLayout.setVisibility( visible ? View.VISIBLE : View.GONE );
        }

        public Calendar getTime()
        {
            return time;
        }
    }

    public SolsticeEquinoxMode getSelection() {
        return card_adapter.getSelection();
    }
    public boolean hasSelection() {
        return card_adapter.hasSelection();
    }
    public void setSelection(@Nullable SolsticeEquinoxMode mode ) {
        card_adapter.setSelection(mode);
    }

    public EquinoxViewAdapter getAdapter() {
         return card_adapter;
    }

    /**
     * EquinoxViewAdapter
     */
    public static class EquinoxViewAdapter extends RecyclerView.Adapter<EquinoxViewHolder>
    {
        public static final int MAX_POSITIONS = 200;
        public static final int CENTER_POSITION = 100;
        @SuppressLint("UseSparseArrays")
        private HashMap<Integer, SuntimesEquinoxSolsticeDataset> data = new HashMap<>();

        private WeakReference<Context> contextRef;
        private EquinoxViewOptions options;

        public EquinoxViewAdapter(Context context, EquinoxViewOptions options)
        {
            this.contextRef = new WeakReference<>(context);
            this.options = options;
        }

        @Override
        public EquinoxViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.info_time_solsticequinox1, parent, false);
            return new EquinoxViewHolder(view, options);
        }

        @Override
        public void onBindViewHolder(EquinoxViewHolder holder, int position)
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
                    EquinoxNote nextNote = findClosestNote(dataset.now(), options.trackingMode, holder.notes);
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
        public void onViewRecycled(EquinoxViewHolder holder)
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
         * @param context
         * @param position
         * @return
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

        public void setThemeOverride( SuntimesTheme theme ) {
            options.themeOverride = theme;
        }

        private EquinoxViewListener viewListener;
        public void setEquinoxViewListener( EquinoxViewListener listener ) {
            viewListener = listener;
        }

        private void attachListeners(final EquinoxViewHolder holder, final int position)
        {
            holder.title.setOnClickListener(onTitleClick(position));
            holder.btn_flipperNext.setOnClickListener(onNextClick(position));
            holder.btn_flipperPrev.setOnClickListener(onPrevClick(position));

            for (int i=0; i <holder.notes.size(); i++) {
                EquinoxNote note = holder.notes.get(i);
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

        private void detachListeners(EquinoxViewHolder holder)
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
                    if (viewListener != null) {
                        viewListener.onClick(position);
                    }
                }
            };
        }
        private View.OnLongClickListener onLongClick( final int position ) {
            return new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (viewListener != null) {
                        return viewListener.onLongClick(position);
                    } else return false;
                }
            };
        }
        private View.OnClickListener onTitleClick( final int position ) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewListener != null) {
                        viewListener.onTitleClick(position);
                    }
                }
            };
        }
        private View.OnClickListener onNextClick( final int position ) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewListener != null) {
                        viewListener.onNextClick(position);
                    }
                }
            };
        }
        private View.OnClickListener onPrevClick( final int position ) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewListener != null) {
                        viewListener.onPrevClick(position);
                    }
                }
            };
        }
        private View.OnClickListener onMenuClick(final View v, final int position, final SolsticeEquinoxMode selection, final long selectionTime) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewListener != null) {
                        viewListener.onMenuClick(v, position, selection, selectionTime);
                    }
                }
            };
        }
        private View.OnClickListener onNoteClick(final EquinoxViewHolder holder, final int position, final int i)
        {
            return new OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SolsticeEquinoxMode mode = SolsticeEquinoxMode.values()[i];
                    if (holder.getSelected() == mode) {
                        holder.notes.get(i).contextMenu.performClick();

                    } else {
                        setSelection(mode);
                        if (viewListener != null) {
                            viewListener.onSelected(i, mode);
                        }
                    }
                }
            };
        }
    }

    /**
     * EquinoxViewHolder
     */
    public static class EquinoxViewHolder extends RecyclerView.ViewHolder
    {
        public int position = RecyclerView.NO_POSITION;

        public View clickArea;
        public View[] clickAreas = new View[4];
        public SolsticeEquinoxMode selected = null;

        public View container;
        public TextView title;
        public ImageButton btn_flipperNext, btn_flipperPrev;
        public EquinoxNote note_equinox_vernal, note_solstice_summer, note_equinox_autumnal, note_solstice_winter;
        public ArrayList<EquinoxNote> notes = new ArrayList<>();

        public EquinoxViewHolder(View view, EquinoxViewOptions options)
        {
            super(view);

            container = view.findViewById(R.id.card_content);

            clickArea = view.findViewById(R.id.clickArea);
            if (!options.minimized) {
                clickArea.setVisibility(View.GONE);
            }

            int[] clickResID = new int[] { R.id.click_equinox_vernal, R.id.click_solstice_summer, R.id.click_equinox_autumnal, R.id.click_solstice_winter };
            for (int i=0; i <clickAreas.length; i++)
            {
                clickAreas[i] = view.findViewById(clickResID[i]);
                if (clickAreas[i] != null) {
                    clickAreas[i].setVisibility(options.minimized ? View.GONE : View.VISIBLE);
                }
            }

            title = (TextView)view.findViewById(R.id.text_title);
            btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
            btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);

            note_equinox_vernal = addNote(view, R.id.text_date_equinox_vernal_label, R.id.text_date_equinox_vernal, R.id.text_date_equinox_vernal_note, R.id.menu_equinox_vernal, R.id.focus_equinox_vernal, R.id.text_date_equinox_vernal_layout, 0, options.seasonColors[0], options);
            note_solstice_summer = addNote(view, R.id.text_date_solstice_summer_label, R.id.text_date_solstice_summer, R.id.text_date_solstice_summer_note, R.id.menu_solstice_summer, R.id.focus_solstice_summer, R.id.text_date_solstice_summer_layout, 0, options.seasonColors[1], options);
            note_equinox_autumnal = addNote(view, R.id.text_date_equinox_autumnal_label, R.id.text_date_equinox_autumnal, R.id.text_date_equinox_autumnal_note, R.id.menu_equinox_autumnal, R.id.focus_equinox_autumnal, R.id.text_date_equinox_autumnal_layout, 0, options.seasonColors[2], options);
            note_solstice_winter = addNote(view, R.id.text_date_solstice_winter_label, R.id.text_date_solstice_winter, R.id.text_date_solstice_winter_note, R.id.menu_solstice_winter, R.id.focus_solstice_winter, R.id.text_date_solstice_winter_layout, 0, options.seasonColors[3], options);

            if (options.columnWidthPx >= 0) {
                adjustColumnWidth(options.columnWidthPx);
            }

            if (options.centered)
            {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)view.getLayoutParams();
                //params.gravity = Gravity.CENTER_HORIZONTAL;
                view.setLayoutParams(params);
            }
        }

        public void setSelected(SolsticeEquinoxMode mode) {
            this.selected = mode;
            updateItemFocus();
        }
        public SolsticeEquinoxMode getSelected() {
            return selected;
        }

        protected void updateItemFocus()
        {
            int p = (selected != null ? selected.ordinal() : -1);
            for (int i=0; i<notes.size(); i++)
            {
                int visibility = (i == p) ? View.VISIBLE : View.GONE;
                View focusView = notes.get(i).focusView;
                if (focusView != null) {
                    focusView.setVisibility(visibility);
                }
                ImageButton menuButton = notes.get(i).contextMenu;
                if (menuButton != null) {
                    menuButton.setVisibility(visibility);
                }
            }
        }

        private EquinoxNote addNote(View view, int labelViewResID, int timeViewResID, int noteViewResID, int menuButtonResID, int focusViewResID, int noteLayoutResID, int pageIndex, Integer timeColor, EquinoxViewOptions options)
        {
            TextView txt_label = (TextView)view.findViewById(labelViewResID);
            TextView txt_time = (TextView)view.findViewById(timeViewResID);
            TextView txt_note = (TextView)view.findViewById(noteViewResID);
            ImageButton menu = (ImageButton) view.findViewById(menuButtonResID);
            View focus = view.findViewById(focusViewResID);
            View layout = view.findViewById(noteLayoutResID);
            return addNote(txt_label, txt_time, txt_note, menu, focus, layout, pageIndex, timeColor, options);
        }

        private EquinoxNote addNote(TextView labelView, TextView timeView, TextView noteView, ImageButton menuButton, View focusView, View noteLayout, int pageIndex, Integer timeColor, EquinoxViewOptions options)
        {
            EquinoxNote note = new EquinoxNote(labelView, timeView, noteView, menuButton, focusView, noteLayout, pageIndex, options);
            if (timeColor != null) {
                note.themeViews(options.labelColor, timeColor, options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
            }
            notes.add(note);
            return note;
        }

        public void disableNotes(Context context, EquinoxViewOptions options)
        {
            for (EquinoxNote note : notes)
            {
                note.setHighlighted(false);
                note.setEnabled(false);
                note.updateDate(context, null);
                note.updateNote(context, null, false, false);

                if (options.minimized) {
                    note.setVisible(false);
                }
            }
        }
        public void enableNotes(boolean visible)
        {
            for (EquinoxNote note : notes)
            {
                note.setEnabled();
                note.setVisible(visible);
            }
        }

        public void bindDataToPosition(@NonNull Context context, SuntimesEquinoxSolsticeDataset data, int position, EquinoxViewOptions options)
        {
            this.position = position;
            for (EquinoxNote note : notes) {
                note.pageIndex = position;
            }

            if (options.themeOverride != null) {
                applyTheme(options.themeOverride, options);
            }
            themeViews(options, position);

            showTitle(false);
            //showTitle(!options.minimized);

            showNextPrevButtons(false);
            //showNextPrevButtons(!options.minimized);

            if (data == null) {
                disableNotes(context, options);
                return;
            }

            if (data.isImplemented() && data.isCalculated())
            {
                SuntimesUtils.TimeDisplayText titleText = utils.calendarDateYearDisplayString(context, data.dataEquinoxSpring.eventCalendarThisYear());
                title.setText(titleText.toString());

                boolean showSeconds = !options.minimized || WidgetSettings.loadShowSecondsPref(context, 0);
                boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

                note_equinox_vernal.updateDate(context, data.dataEquinoxSpring.eventCalendarThisYear(), showTime, showSeconds);
                note_equinox_autumnal.updateDate(context, data.dataEquinoxAutumnal.eventCalendarThisYear(), showTime, showSeconds);
                note_solstice_summer.updateDate(context, data.dataSolsticeSummer.eventCalendarThisYear(), showTime, showSeconds);
                note_solstice_winter.updateDate(context, data.dataSolsticeWinter.eventCalendarThisYear(), showTime, showSeconds);

                boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
                boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
                for (EquinoxNote note : notes) {
                    note.setHighlighted(false);
                    note.updateNote(context, data.now(), showWeeks, showHours);
                }

            } else {
                disableNotes(context, options);
            }

            if (options.columnWidthPx >= 0) {
                adjustColumnWidth(options.columnWidthPx);
            }
            updateItemFocus();
        }

        public void showTitle( boolean show ) {
            title.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        public void showNextPrevButtons( boolean show )
        {
            if (show) {
                btn_flipperNext.setVisibility(View.VISIBLE);
                btn_flipperPrev.setVisibility(View.VISIBLE);
            } else {
                btn_flipperNext.setVisibility(View.GONE);
                btn_flipperPrev.setVisibility(View.GONE);
            }
        }

        public void adjustColumnWidth(int columnWidthPx)
        {
            for (EquinoxNote note : notes) {
                note.adjustLabelWidth(columnWidthPx);
            }
        }

        public void applyTheme(SuntimesTheme theme, EquinoxViewOptions options)
        {
            if (theme != null)
            {
                options.titleColor = theme.getTitleColor();
                options.textColor = theme.getTextColor();
                options.pressedColor = theme.getActionColor();
                options.seasonColors[0] = theme.getSpringColor();
                options.seasonColors[1] = theme.getSummerColor();
                options.seasonColors[2] = theme.getFallColor();
                options.seasonColors[3] = theme.getWinterColor();
            }
        }
        public void themeViews( EquinoxViewOptions options, int position )
        {
            title.setTextColor(SuntimesUtils.colorStateList((position  < EquinoxViewAdapter.CENTER_POSITION ? options.disabledColor : options.titleColor), options.disabledColor, options.pressedColor));
            if (options.titleSizeSp != null)
            {
                title.setTextSize(options.titleSizeSp);
                title.setTypeface(title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            ImageViewCompat.setImageTintList(btn_flipperNext, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));
            ImageViewCompat.setImageTintList(btn_flipperPrev, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));

            note_equinox_vernal.themeViews(options.labelColor, options.seasonColors[0], options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
            note_solstice_summer.themeViews(options.labelColor, options.seasonColors[1], options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
            note_equinox_autumnal.themeViews(options.labelColor, options.seasonColors[2], options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
            note_solstice_winter.themeViews(options.labelColor, options.seasonColors[3], options.textColor, options.timeSizeSp, options.titleSizeSp, options.titleBold);
        }
    }

    /**
     * EquinoxViewOptions
     */
    public static class EquinoxViewOptions
    {
        public boolean isRtl = false;
        public boolean minimized = false;
        public boolean centered = false;
        public int columnWidthPx = -1;
        public int highlightPosition = -1;

        public TrackingMode trackingMode = TrackingMode.SOONEST;

        public int titleColor, noteColor, disabledColor, pressedColor;
        public Integer[] seasonColors = new Integer[4];
        public Integer labelColor, textColor;
        public int resID_buttonPressColor;

        public Float timeSizeSp = null;
        public Float titleSizeSp = null;
        public boolean titleBold = false;

        private SuntimesTheme themeOverride = null;

        @SuppressLint("ResourceType")
        public void init(Context context)
        {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
            titleColor = noteColor;
            disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_disabled_dark));
            resID_buttonPressColor = typedArray.getResourceId(2, R.color.btn_tint_pressed_dark);
            pressedColor = ContextCompat.getColor(context, resID_buttonPressColor);
            labelColor = textColor = seasonColors[0] = seasonColors[1] = seasonColors[2] = seasonColors[3] = null;
            titleSizeSp = timeSizeSp = null;
            typedArray.recycle();
        }

        public void init(SuntimesTheme theme)
        {
            if (theme != null)
            {
                titleColor = theme.getTitleColor();
                noteColor = theme.getTimeColor();
                labelColor = theme.getTitleColor();
                textColor = theme.getTextColor();
                pressedColor = theme.getActionColor();
                seasonColors[0] = theme.getSpringColor();
                seasonColors[1] = theme.getSummerColor();
                seasonColors[2] = theme.getFallColor();
                seasonColors[3] = theme.getWinterColor();
                timeSizeSp = theme.getTimeSizeSp();
                titleSizeSp = theme.getTitleSizeSp();
                titleBold = theme.getTitleBold();
            }
        }
    }

    private EquinoxViewListener viewListener = null;
    public void setViewListener(EquinoxViewListener listener) {
        this.viewListener = listener;
    }

    /**
     * EquinoxViewClickListener
     */
    public static class EquinoxViewListener
    {
        public void onClick( int position ) {}
        public boolean onLongClick( int position ) { return false; }
        public void onTitleClick( int position ) {}
        public void onNextClick( int position ) {}
        public void onPrevClick( int position ) {}
        public void onSelected( int position, SolsticeEquinoxMode mode ) {}
        public void onMenuClick( View v, int position ) {}
        public void onMenuClick(View v, int position, SolsticeEquinoxMode mode, long datetime ) {}
    }

}
