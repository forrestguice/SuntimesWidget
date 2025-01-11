/**
    Copyright (C) 2022-2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.moon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.graphics.ColorUtils;
import com.forrestguice.support.design.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonRiseSetColorValues;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public class MoonRiseSetView1 extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;

    private RecyclerView card_view;
    private MoonRiseSetAdapter card_adapter;
    private LinearLayoutManager card_layout;
    private ImageButton forwardButton, backButton;

    public MoonRiseSetView1(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonRiseSetView1(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonRiseSetView1, 0, 0);
        try {
            setShowPosition(a.getBoolean(R.styleable.MoonRiseSetView_showPosition, false));
        } finally {
            a.recycle();
        }
    }

    protected void initAdapter(Context context)
    {
        if (card_adapter != null) {
            card_view.setAdapter(null);
        }

        card_adapter = new MoonRiseSetAdapter(context, showLunarNoon);
        card_adapter.setAdapterListener(card_listener);
        card_adapter.setShowPosition(showPosition);

        initDecorations(context);
        card_view.setAdapter(card_adapter);
    }

    public MoonRiseSetAdapter getAdapter() {
        return card_adapter;
    }

    public void setColors(Context context, @Nullable ColorValues colors)
    {
        if (card_adapter != null)
        {
            if (colors != null) {
                card_adapter.colors = new MoonRiseSetColorValues(colors);
            } else {
                card_adapter.initTheme(context);
            }
            card_adapter.notifyDataSetChanged();
        }
    }

    private void initDecorations(Context context)
    {
        if (dividers != null) {
            card_view.removeItemDecoration(dividers);
            dividers = null;
        }
        dividers = new MoonRiseSetDivider1(context, MoonRiseSetAdapter.CENTER_POSITION, card_adapter.getItemsPerDay());
        card_view.addItemDecoration(dividers);
    }
    private MoonRiseSetDivider1 dividers = null;

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonriseset1, this, true);

        if (attrs != null)
        {
            LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }
        showLunarNoon = AppSettings.loadShowLunarNoonPref(context);

        card_layout = new LinearLayoutManager(context);
        card_layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        card_view = (RecyclerView)findViewById(R.id.moonriseset_card);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.setLayoutManager(card_layout);

        initAdapter(context);
        card_view.scrollToPosition(MoonRiseSetAdapter.CENTER_POSITION);

        GravitySnapHelper snapHelper = new GravitySnapHelper(Gravity.START); // new LinearSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        forwardButton = (ImageButton)findViewById(R.id.info_time_nextbtn);
        TooltipCompat.setTooltipText(forwardButton, forwardButton.getContentDescription());
        forwardButton.setOnClickListener(onResetClick1);
        forwardButton.setVisibility(GONE);

        backButton = (ImageButton)findViewById(R.id.info_time_prevbtn);
        TooltipCompat.setTooltipText(backButton, backButton.getContentDescription());
        backButton.setOnClickListener(onResetClick0);
        backButton.setVisibility(VISIBLE);
        backButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = card_layout.findFirstVisibleItemPosition();
                if (position == MoonRiseSetAdapter.CENTER_POSITION) {
                    ViewUtils.fadeOutButton(backButton, ViewUtils.ANIM_VERYLONG);
                }
            }
        }, 1200);

        //card_view.setOnScrollListener(onScrollChanged);
        card_view.addOnScrollListener(onScrollChanged);

        initTheme(context);
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        isRtl = AppSettings.isLocaleRtl(context);
    }

    @Override
    public void onSizeChanged( int w, int h, int oldWidth, int oldHeight )
    {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        if (card_adapter != null) {
            int margin = 0;
            card_adapter.setItemWidth((w - (margin * 2)) / itemsPerView);
        }
    }
    private int itemsPerView = 3;

    @SuppressLint("ResourceType")
    protected void initTheme(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.buttonPressColor, R.attr.text_disabledColor, R.attr.colorBackgroundFloating, R.attr.text_accentColor, R.attr.tagColor_warning };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        colorEnabled = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.grey_50));
        colorPressed = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.btn_tint_pressed));
        colorDisabled = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.text_accent));
        colorBackground = ColorUtils.setAlphaComponent(ContextCompat.getColor(context, typedArray.getResourceId(3, def)), (int)(9d * (254d / 10d)));
        colorAccent = ContextCompat.getColor(context, typedArray.getResourceId(4, R.color.text_accent));
        colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(5, R.color.warningTag));
        typedArray.recycle();

        themeDrawables();
    }
    private int colorEnabled, colorPressed, colorDisabled, colorBackground, colorAccent, colorWarning;

    public void themeViews(Context context, SuntimesTheme theme)
    {
        card_adapter.applyTheme(context, theme);
        colorAccent = theme.getAccentColor();
        colorPressed = theme.getActionColor();
        colorWarning = theme.getActionColor();
        themeDrawables();
    }

    private void themeDrawables()
    {
        ImageViewCompat.setImageTintList(forwardButton, SuntimesUtils.colorStateList(colorWarning, colorDisabled, colorPressed));
        ImageViewCompat.setImageTintList(backButton, SuntimesUtils.colorStateList(colorWarning, colorDisabled, colorPressed));

        if (Build.VERSION.SDK_INT < 21) {
            SuntimesUtils.colorizeImageView(backButton, colorBackground);
            SuntimesUtils.colorizeImageView(forwardButton, colorBackground);
        }
    }

    public void updateViews(Context context) {
        /* EMPTY */
    }

    private void setShowPosition(boolean value) {
        showPosition = value;
        if (card_adapter != null) {
            card_adapter.setShowPosition(value);
        }
    }
    private boolean showPosition = false;

    public void setShowLunarNoon(boolean value) {
        showLunarNoon = value;
        if (card_adapter != null)
        {
            Long date = (getData() != null ? getData().calendar().getTimeInMillis() : null);
            initAdapter(getContext());
            onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
            if (date != null) {
                scrollToDate(date);
            }
        }
    }
    private boolean showLunarNoon = AppSettings.PREF_DEF_UI_SHOWLUNARNOON;

    public int getItemsPerDay() {
        return card_adapter.getItemsPerDay();
    }

    public SuntimesMoonData getData() {
        return card_adapter.initData(getContext(), getDataPosition());
    }
    public SuntimesMoonData getData(int adapterPosition)
    {
        if (card_adapter != null) {
            return card_adapter.initData(getContext(), adapterPosition);
        }
        return null;
    }
    public SuntimesMoonData getDataAtCenter() {
        return card_adapter.initData(getContext(), MoonRiseSetAdapter.CENTER_POSITION);
    }
    public int getDataPosition() {
        return card_layout.findFirstCompletelyVisibleItemPosition();
    }

    private final MoonRiseSetAdapterListener card_listener = new MoonRiseSetAdapterListener()
    {
        @Override
        public void onClick(View v, MoonRiseSetAdapter adapter, int position, String eventID)
        {
            if (viewListener != null) {
                viewListener.onClick(v, adapter, position, eventID);
            }
        }
    };

    private final RecyclerView.OnScrollListener onScrollChanged = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            int position = card_layout.findFirstVisibleItemPosition();

            if (position < MoonRiseSetView1.MoonRiseSetAdapter.CENTER_POSITION)
            {
                ViewUtils.fadeInButton(forwardButton, ViewUtils.ANIM_VERYLONG);
                backButton.setVisibility(View.GONE);

            } else if (position > MoonRiseSetView1.MoonRiseSetAdapter.CENTER_POSITION) {
                forwardButton.setVisibility(View.GONE);
                ViewUtils.fadeInButton(backButton, ViewUtils.ANIM_VERYLONG);

            } else {
                ViewUtils.fadeOutButton(forwardButton, ViewUtils.ANIM_LONG);
                ViewUtils.fadeOutButton(backButton, ViewUtils.ANIM_LONG);
            }

            if (viewListener != null) {
                viewListener.onScrollStateChanged(recyclerView, newState, position);
            }
        }
    };

    private final OnClickListener onResetClick0 = new OnClickListener() {
        @Override
        public void onClick(View v) {      // back to position; scrolling from right-to-left
            if (viewListener != null) {
                viewListener.onResetClick(v);
            }
            scrollToCenter();
        }
    };
    private final OnClickListener onResetClick1 = new OnClickListener() {
        @Override
        public void onClick(View v) {      // forward to position; scrolling from left-to-right
            if (viewListener != null) {
                viewListener.onResetClick(v);
            }
            scrollToCenter();
        }
    };

    public void scrollToCenter() {
        card_layout.scrollToPositionWithOffset(MoonRiseSetAdapter.CENTER_POSITION, 0);
        card_view.smoothScrollBy(1, 0); // triggers a snap
    }
    public void scrollToDate( long datetime ) {
        card_layout.scrollToPositionWithOffset(card_adapter.getPositionForDate(getContext(), datetime), 0);
    }
    public void lockScrolling() {
        card_view.setLayoutFrozen(true);
    }
    public void unlockScrolling() {
        card_view.setLayoutFrozen(false);
    }

    private MoonRiseSetViewListener viewListener = null;
    public void setViewListener(MoonRiseSetViewListener listener) {
        this.viewListener = listener;
    }

    /**
     * MoonRiseSetViewListener
     */
    public static class MoonRiseSetViewListener extends MoonRiseSetAdapterListener
    {
        public void onScrollStateChanged(RecyclerView recyclerView, int newState, int position) { /* EMPTY */ }
        public void onResetClick(View v) { /* EMPTY */ }
    }

    /**
     * MoonRiseSetAdapterListener
     */
    public static class MoonRiseSetAdapterListener
    {
        public void onClick(View v, MoonRiseSetAdapter adapter, int position, String eventID) { /* EMPTY */ }
    }

    /**
     * MoonRiseSetAdapter
     */
    public static class MoonRiseSetAdapter extends RecyclerView.Adapter<MoonRiseSetField>
    {
        public static final int CENTER_POSITION = 1000;
        public static final int MAX_POSITIONS = CENTER_POSITION * 2;

        private final WeakReference<Context> contextRef;
        @SuppressLint("UseSparseArrays")
        private final HashMap<Integer, SuntimesMoonData> data = new HashMap<>();

        public MoonRiseSetAdapter(Context context, boolean showLunarNoon)
        {
            contextRef = new WeakReference<>(context);
            allEvents = (showLunarNoon
                    ? new MoonRiseSetEvent[] { MoonRiseSetEvent.MOONRISE, MoonRiseSetEvent.MOONNOON, MoonRiseSetEvent.MOONSET, MoonRiseSetEvent.MOONNIGHT }
                    : new MoonRiseSetEvent[] { MoonRiseSetEvent.MOONRISE, MoonRiseSetEvent.MOONSET });
            initData(context);
            initTheme(context);
        }

        private int itemWidth = LayoutParams.WRAP_CONTENT;
        public void setItemWidth( int pixels ) {
            itemWidth = pixels;
            notifyDataSetChanged();
        }

        @Override
        public MoonRiseSetField onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(MoonRiseSetField.getLayoutID(), parent, false);
            return new MoonRiseSetField(view, itemWidth);
        }

        @Override
        public void onBindViewHolder(MoonRiseSetField holder, int position)
        {
            Context context = contextRef.get();
            if (context == null) {
                Log.e("MoonRiseSetAdapter", "null context!");
                return;
            }

            SuntimesMoonData d = initData(context, position);
            holder.setShowPosition(showPosition);   // option must be set before binding data
            holder.onBindDataToPosition(context, d, position, getEventAt(d, position));

            Calendar event = MoonRiseSetEvent.getCalendarForEvent(d, holder.eventID);
            boolean isAgo = Calendar.getInstance().after(event);
            themeViews(context, holder, isAgo);
            if (event != null) {
                holder.resizeField(itemWidth);
            } else holder.hideField();

            attachClickListeners(holder, position);
        }

        private boolean boldTime = false;
        private Float spTime = null, spText = null;
        private int colorDisabled, colorTime, colorText;
        protected MoonRiseSetColorValues colors;

        @SuppressLint("ResourceType")
        private void initTheme(Context context)
        {
            colors = new MoonRiseSetColorValues(context);

            int[] colorAttrs = { android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, R.attr.text_disabledColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;
            colorTime = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            colorText = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            colorDisabled = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            typedArray.recycle();
        }

        protected void applyTheme(Context context, SuntimesTheme theme)
        {
            colorTime = theme.getTimeColor();
            colorText = theme.getTextColor();
            spTime = theme.getTimeSizeSp();
            spText = theme.getTextSizeSp();
            boldTime = theme.getTimeBold();
            colors.setColor(MoonRiseSetColorValues.COLOR_RISING_MOON, theme.getMoonriseTextColor());
            colors.setColor(MoonRiseSetColorValues.COLOR_SETTING_MOON, theme.getMoonsetTextColor());
        }

        protected void themeViews(Context context, @NonNull MoonRiseSetField holder, boolean isAgo)
        {
            int timeColor = isAgo ? colorDisabled : colorTime;
            int textColor = isAgo ? colorDisabled : colorText;
            holder.themeViews(timeColor, spTime, boldTime, textColor, spText, colors);
        }

        @Override
        public void onViewRecycled(MoonRiseSetField holder)
        {
            detachClickListeners(holder);
            if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2)) {
                data.remove(holder.position);
            }
            holder.resizeField(itemWidth);
            holder.onRecycled();
        }

        @Override
        public int getItemCount() {
            return MAX_POSITIONS;
        }

        protected void initData( Context context ) {
            SuntimesMoonData d = initData(context, CENTER_POSITION);
        }

        @NonNull
        public SuntimesMoonData initData( Context context, int position )
        {
            int itemsPerDay = getItemsPerDay();
            int offset = (position - CENTER_POSITION) % itemsPerDay;

            int position0 = position;
            if (offset > 0) {
                position0 = position - offset;
            } else if (offset < 0) {
                position0 = position - (itemsPerDay + offset);
            }

            SuntimesMoonData d = data.get(position0);
            if (d == null) {
                d = createData(context, position0);
                for (int i=0; i<itemsPerDay; i++) {
                    data.put(position0 + i, d);
                }
            }
            return d;
        }

        @NonNull
        public SuntimesMoonData createData( Context context, int position )
        {
            SuntimesMoonData d = new SuntimesMoonData(context, 0, "moon");
            if (position != CENTER_POSITION)
            {
                SuntimesMoonData d0 = initData(context, CENTER_POSITION);
                Calendar rising = d0.moonriseCalendarToday();
                Calendar setting = d0.moonsetCalendarToday();
                boolean isRising = (rising != null && rising.before(setting));

                Calendar date = Calendar.getInstance(d.timezone());
                if (isRising && rising != null) {
                    date.setTimeInMillis(rising.getTimeInMillis());
                } else if (setting != null) {
                    date.setTimeInMillis(setting.getTimeInMillis());
                } else if (rising != null) {
                    date.setTimeInMillis(rising.getTimeInMillis());
                } else {
                    date.setTimeInMillis(d0.calendar().getTimeInMillis());
                }

                date.setTimeInMillis((long)(date.getTimeInMillis() + getOffsetMillis(position)));
                d.setTodayIs(date);
            }
            d.calculate();
            return d;
        }

        /*protected double getOffsetMillis(int position)
        {
            double dayMillis = 24d * 60d * 60d * 1000;
            int rawOffset = position - CENTER_POSITION;
            return (rawOffset >> 1) * dayMillis;
        }*/

        protected double getOffsetMillis(int position)
        {
            double dayMillis = 24d * 60d * 60d * 1000;
            int rawOffset = position - CENTER_POSITION;
            return ((rawOffset / (1d * getItemsPerDay())) * dayMillis);
        }

        public int getItemsPerDay() {
            return allEvents.length;
        }
        protected MoonRiseSetEvent[] getAllEvents() {
            return allEvents;
        }
        private final MoonRiseSetEvent[] allEvents;

        protected MoonRiseSetEvent getFirstEvent(SuntimesMoonData d) {
            return MoonRiseSetEvent.findFirstEvent(d, getAllEvents());
        }

        protected MoonRiseSetEvent getEventAt(SuntimesMoonData d, int position)
        {
            MoonRiseSetEvent[] allEvents = getAllEvents();
            int firstEvent = MoonRiseSetEvent.getEventOrdinal(allEvents, getFirstEvent(d));
            int offset = (position - MoonRiseSetAdapter.CENTER_POSITION) % allEvents.length;
            if (offset < 0) {
                offset += allEvents.length;
            }
            return allEvents[(firstEvent + offset) % allEvents.length];
        }

        public int getPositionForDate(Context context, long datetime)
        {
            SuntimesMoonData d = initData(context, CENTER_POSITION);
            Calendar dateCenter = MoonRiseSetEvent.getCalendarForEvent(d, getEventAt(d, CENTER_POSITION));
            if (dateCenter != null)
            {
                long deltaMs = (datetime - dateCenter.getTimeInMillis());
                double deltaDays = deltaMs / (1000d * 60d * 60d * 24d);
                return (CENTER_POSITION + (getItemsPerDay() * (int)Math.floor(deltaDays)));
            }
            return CENTER_POSITION;
        }

        private void setShowPosition(boolean value) {
            showPosition = value;
        }
        private boolean showPosition = false;

        private void attachClickListeners(@NonNull MoonRiseSetField holder, int position) {
            holder.layout.setOnClickListener(onItemClick(position, holder.eventID.name()));
        }

        private void detachClickListeners(@NonNull MoonRiseSetField holder) {
            holder.layout.setOnClickListener(null);
        }

        private OnClickListener onItemClick(final int position, final String eventID) {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onClick(v, MoonRiseSetAdapter.this, position, eventID);
                    }
                }
            };
        }

        /**
         * setAdapterListener
         * @param listener
         */
        public void setAdapterListener( @NonNull MoonRiseSetAdapterListener listener ) {
            adapterListener = listener;
        }
        private MoonRiseSetAdapterListener adapterListener = new MoonRiseSetAdapterListener();
    }

    /**
     * MoonRiseSetField
     */
    public static class MoonRiseSetField extends RecyclerView.ViewHolder
    {
        public int position = RecyclerView.NO_POSITION;
        public MoonRiseSetEvent eventID = null;

        public View layout;
        public ImageView iconView;
        public TextView timeView;
        public TextView positionView;

        public Drawable icon_rising = null, icon_setting = null, icon_noon = null, icon_midnight = null;
        private final SuntimesUtils utils = new SuntimesUtils();

        public static int getLayoutID() {
            return R.layout.info_time_moonriseset;
        }

        public MoonRiseSetField(View view, int itemWidth)
        {
            super(view);
            layout = view.findViewById(R.id.layout_moonriseset);
            iconView = (ImageView)view.findViewById(R.id.icon_time_moonriseset);
            timeView = (TextView)view.findViewById(R.id.text_time_moonriseset);
            positionView = (TextView)view.findViewById(R.id.text_info_moonriseset);
            resizeField(itemWidth);
            initDrawables(view.getContext());
        }

        protected void initDrawables(Context context)
        {
            TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.moonriseIcon, R.attr.moonsetIcon, R.attr.moonnoonIcon, R.attr.moonnightIcon });
            icon_rising = ContextCompat.getDrawable(context, a.getResourceId(0, R.drawable.ic_moon_rise)).mutate();
            icon_setting = ContextCompat.getDrawable(context, a.getResourceId(1, R.drawable.ic_moon_set)).mutate();
            icon_noon = ContextCompat.getDrawable(context, a.getResourceId(3, R.drawable.ic_moon_noon)).mutate();
            icon_midnight = ContextCompat.getDrawable(context, a.getResourceId(4, R.drawable.ic_moon_night)).mutate();
            a.recycle();
        }

        protected Drawable getIconForEvent(@Nullable MoonRiseSetEvent event)
        {
            if (event == null) {
                return null;
            }
            switch (event) {
                case MOONNOON: return icon_noon;
                case MOONNIGHT: return icon_midnight;
                case MOONSET: return icon_setting;
                case MOONRISE: default: return icon_rising;
            }
        }

        protected int getIconColorForEvent(@Nullable MoonRiseSetEvent event, MoonRiseSetColorValues colors)
        {
            if (event == null) {
                return Color.TRANSPARENT;
            }
            switch (event) {
                case MOONNIGHT: case MOONSET: return colors.getColor(MoonRiseSetColorValues.COLOR_SETTING_MOON);
                case MOONNOON: case MOONRISE: default: return colors.getColor(MoonRiseSetColorValues.COLOR_RISING_MOON);
            }
        }

        public void onBindDataToPosition(Context context, @Nullable SuntimesMoonData data, int position, @Nullable MoonRiseSetEvent eventID)
        {
            this.position = position;
            this.eventID = eventID;

            Calendar event = MoonRiseSetEvent.getCalendarForEvent(data, eventID);
            if (Build.VERSION.SDK_INT >= 16) {
                iconView.setBackground(getIconForEvent(eventID));
            } else {
                iconView.setBackgroundDrawable(getIconForEvent(eventID));
            }
            updateField(context, event, WidgetSettings.loadShowSecondsPref(context, 0));
            if (positionView.getVisibility() == VISIBLE && data != null)
            {
                SuntimesCalculator calculator = data.calculator();
                updateField(context, ((event != null) ? calculator.getMoonPosition(event) : null));
            }
            layout.setVisibility(event != null ? VISIBLE : INVISIBLE);
        }

        public void onRecycled()
        {
            this.position = RecyclerView.NO_POSITION;
            this.eventID = null;
        }

        public void themeViews(int timeColor, @Nullable Float timeSizeSp, boolean timeBold, int textColor, @Nullable Float textSizeSp, MoonRiseSetColorValues colors)
        {
            timeView.setTextColor(timeColor);
            positionView.setTextColor(textColor);

            if (timeSizeSp != null) {
                positionView.setTextSize(timeSizeSp);
                timeView.setTextSize(timeSizeSp);
                timeView.setTypeface(timeView.getTypeface(), (timeBold ? Typeface.BOLD : Typeface.NORMAL));
            }
            if (textSizeSp != null) {
                positionView.setTextSize(textSizeSp);
            }

            int color = getIconColorForEvent(eventID, colors);
            if (iconView.getBackground() instanceof LayerDrawable) {
                SuntimesUtils.tintDrawable((LayerDrawable) iconView.getBackground(), color, color, 0);
            } else if (iconView.getBackground() instanceof InsetDrawable) {
                SuntimesUtils.tintDrawable((InsetDrawable) iconView.getBackground(), color, color, 0);
            }
        }

        public void updateField(Context context, Calendar dateTime, boolean showSeconds)
        {
            SuntimesUtils.TimeDisplayText text = utils.calendarTimeShortDisplayString(context, dateTime, showSeconds);
            timeView.setText(text.toString());
        }

        public void updateField(Context context, SuntimesCalculator.Position position)
        {
            if (position == null)
            {
                positionView.setText("");
                positionView.setContentDescription("");

            } else {
                SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(position.azimuth, 1, false);
                String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
                SpannableString azimuthSpan = SuntimesUtils.createRelativeSpan(null, azimuthString, azimuthText.getSuffix(), 0.7f);
                azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
                positionView.setText(azimuthSpan);

                SuntimesUtils.TimeDisplayText azimuthDesc = utils.formatAsDirection2(position.azimuth, 1, true);
                positionView.setContentDescription(utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
            }
        }

        public void setShowPosition( boolean value ) {
            positionView.setVisibility((value ? View.VISIBLE : View.GONE));
        }

        public void setMarginStartEnd( int startMargin, int endMargin )
        {
            if (layout.getLayoutParams() instanceof MarginLayoutParams)
            {
                MarginLayoutParams params = (MarginLayoutParams) layout.getLayoutParams();
                if (Build.VERSION.SDK_INT < 17)
                {
                    params.setMargins(startMargin, 0, endMargin, 0);

                } else if (layout.getLayoutParams() instanceof MarginLayoutParams) {
                    params.setMarginStart(startMargin);
                    params.setMarginEnd(endMargin);
                }
                layout.setLayoutParams(params);
                layout.requestLayout();
            }
        }

        public void resizeField(int pixels) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
            params.width = pixels;
            params.height = LayoutParams.WRAP_CONTENT;
            layout.setLayoutParams( params );
        }

        public void hideField() {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
            params.width = params.height = 0;
            layout.setLayoutParams( params );
        }
    }

    /**
     * MoonRiseSetEvent
     */
    public static enum MoonRiseSetEvent
    {
        MOONRISE, MOONNOON, MOONSET, MOONNIGHT;    // needs to match enum values from SolarEvents
        private MoonRiseSetEvent() {}

        public static Calendar getCalendarForEvent(@Nullable SuntimesMoonData data, @Nullable String eventID)
        {
            if (eventID == null || data == null) {
                return null;
            }
            try {
                return getCalendarForEvent(data, MoonRiseSetEvent.valueOf(eventID));
            } catch (IllegalArgumentException e) {
                Log.w("MoonRiseSetEvent", "Unrecognized eventID: " + eventID + ": " + e);
                return getCalendarForEvent(data, (MoonRiseSetEvent) null);
            }
        }
        public static Calendar getCalendarForEvent(@Nullable SuntimesMoonData data, @Nullable MoonRiseSetEvent event)
        {
            if (event == null || data == null) {
                return null;
            }
            switch (event) {
                case MOONNOON: return data.getLunarNoonToday();
                case MOONNIGHT: return data.getLunarMidnightToday();
                case MOONSET: return data.moonsetCalendarToday();
                case MOONRISE: default: return data.moonriseCalendarToday();
            }
        }

        public static MoonRiseSetEvent findFirstEvent(SuntimesMoonData d, MoonRiseSetEvent[] events)
        {
            MoonRiseSetEvent firstEvent = events[0];
            Calendar firstCalendar = MoonRiseSetEvent.getCalendarForEvent(d, events[0]);
            for (int i=1; i<events.length; i++)
            {
                Calendar calendar = MoonRiseSetEvent.getCalendarForEvent(d, events[i]);
                if (calendar != null && calendar.before(firstCalendar)) {
                    firstEvent = events[i];
                    firstCalendar = calendar;
                }
            }
            return firstEvent;
        }

        public static int getEventOrdinal(MoonRiseSetEvent[] events, MoonRiseSetEvent event) {
            for (int i=0; i<events.length; i++) {
                if (events[i] == event) {
                    return i;
                }
            }
            return -1;
        }

    }

    /**
     * MoonRiseSetDivider1
     */
    private class MoonRiseSetDivider1 extends MoonRiseSetDivider
    {
        protected final Paint paintText = new Paint();
        protected final int[] text_offset = new int[] {0, 0};

        public MoonRiseSetDivider1(Context context, int centerPosition, int itemsPerDay)
        {
            super(context, centerPosition, itemsPerDay);
            paintText.setAntiAlias(true);
            paintText.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.text_size_tiny));
            paintText.setTypeface(Typeface.DEFAULT_BOLD);
            text_offset[0] = SuntimesUtils.dpToPixels(getContext(), 8);   // 8dp (from left side)
            text_offset[1] = SuntimesUtils.dpToPixels(getContext(), 2);   // 2dp (from bottom)
        }

        @Override
        protected void drawFooter(Canvas c, int position, float x, float y)
        {
            SuntimesMoonData d = card_adapter.initData(getContext(), position);
            if (d != null)
            {
                Calendar date = d.calendar();
                String dateText = utils.calendarDateDisplayString(getContext(), date).toString();

                int textColor = colorDisabled;
                Calendar now = d.now();
                if (date.get(Calendar.YEAR) == now.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                    textColor = colorAccent;
                } else if (now.before(date)) {
                    textColor = colorEnabled;
                }
                paintText.setColor(textColor);

                c.drawText(dateText, x + text_offset[0], y - text_offset[1], paintText);
            }
        }
    }

    /**
     * MoonRiseSetDivider
     */
    public static class MoonRiseSetDivider extends RecyclerView.ItemDecoration
    {
        protected Drawable divider;
        protected int centerPosition;
        protected int itemsPerDay;
        private final Rect bounds = new Rect();

        public MoonRiseSetDivider(Context context, int centerPosition, int itemsPerDay)
        {
            this.centerPosition = centerPosition;
            this.itemsPerDay = itemsPerDay;
            initDrawables(context);
        }

        protected void initDrawables(Context context)
        {
            TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.listDivider });
            divider = a.getDrawable(0);
            a.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state)
        {
            if (parent.getLayoutManager() == null) {
                return;
            }

            c.save();
            int top, bottom;
            if (parent.getClipToPadding())
            {
                top = parent.getPaddingTop();
                bottom = parent.getHeight() - parent.getPaddingBottom();
                c.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
            } else {
                top = 0;
                bottom = parent.getHeight();
            }

            int n = parent.getChildCount();
            for (int i=0; i<n; i++)
            {
                View child = parent.getChildAt(i);
                int position = parent.getChildAdapterPosition(child);
                parent.getLayoutManager().getDecoratedBoundsWithMargins(child, bounds);

                int offset = (position - centerPosition) % itemsPerDay;
                if (offset < 0) {
                    offset += itemsPerDay;
                }

                if (offset == 0) {
                    int left = bounds.left + Math.round(ViewCompat.getTranslationX(child));
                    drawHeader(c, position, left, top);
                    drawFooter(c, position, left, bottom);

                } else if (offset == (itemsPerDay - 1)) {
                    int right = bounds.right + Math.round(ViewCompat.getTranslationX(child));
                    int left = right - divider.getIntrinsicWidth();
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }
            c.restore();
        }

        protected void drawFooter(Canvas c, int position, float x, float y) {
            /* EMPTY */
        }

        protected void drawHeader(Canvas c, int position, float x, float y) {
            /* EMPTY */
        }

        @Override
        public void getItemOffsets(Rect rect, View v, RecyclerView parent, RecyclerView.State state) {
            rect.set(0, 0, divider.getIntrinsicWidth(), 0);
        }
    }
}