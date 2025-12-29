/**
    Copyright (C) 2019-2022 Forrest Guice
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.display.LengthUnitDisplay;
import com.forrestguice.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData0;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonApsisColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.util.android.AndroidResources;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

public class MoonApsisView extends LinearLayout
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private LinearLayout content;
    private RecyclerView card_view;
    private MoonApsisAdapter card_adapter;
    private LinearLayoutManager card_layout;
    private ImageButton forwardButton, backButton;
    private int colorEnabled, colorPressed, colorAccent, colorDisabled, colorBackground;

    public MoonApsisView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonApsisView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        SuntimesUtils.initDisplayStrings(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonapsis, this, true);
        content = (LinearLayout)findViewById(R.id.moonapsis_layout);

        card_layout = new LinearLayoutManager(context);
        card_layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        card_view = (RecyclerView)findViewById(R.id.moonapsis_card);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.setLayoutManager(card_layout);

        card_adapter = new MoonApsisAdapter(context);
        card_adapter.setAdapterListener(card_listener);
        card_adapter.setItemWidth(Resources.getSystem().getDisplayMetrics().widthPixels / 3);  // initial width; 3 to screen; reassigned later in onSizeChanged

        card_view.setAdapter(card_adapter);
        card_view.scrollToPosition(MoonApsisAdapter.CENTER_POSITION);

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
                if (position == MoonApsisAdapter.CENTER_POSITION) {
                    ViewUtils.fadeOutButton(backButton, ViewUtils.ANIM_VERYLONG);
                }
            }
        }, 1200);

        card_view.setOnScrollListener(onScrollChanged);

        initTheme(context);
        if (isInEditMode()) {
            updateViews(context);
        }
    }

    @Override
    public void onSizeChanged( int w, int h, int oldWidth, int oldHeight )
    {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        if (card_adapter != null) {
            int margin = 8;
            card_adapter.setItemWidth((w - (margin * 2)) / 2);   // 2 to view
        }
    }

    private final MoonApsisAdapterListener card_listener = new MoonApsisAdapterListener()
    {
        @Override
        public void onClick(View view, MoonApsisAdapter adapter, int position, boolean isRising) {
            if (viewListener != null) {
                viewListener.onClick(view, adapter, position, isRising);
            }
        }
    };

    private RecyclerView.OnScrollListener onScrollChanged = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            int position = card_layout.findFirstVisibleItemPosition();

            if (position < MoonApsisAdapter.CENTER_POSITION)
            {
                ViewUtils.fadeInButton(forwardButton, ViewUtils.ANIM_VERYLONG);
                backButton.setVisibility(View.GONE);

            } else if (position > MoonApsisAdapter.CENTER_POSITION) {
                forwardButton.setVisibility(View.GONE);
                ViewUtils.fadeInButton(backButton, ViewUtils.ANIM_VERYLONG);

            } else {
                ViewUtils.fadeOutButton(forwardButton, ViewUtils.ANIM_LONG);
                ViewUtils.fadeOutButton(backButton, ViewUtils.ANIM_LONG);
            }
        }
    };

    private OnClickListener onResetClick0 = new OnClickListener() {
        @Override
        public void onClick(View v) {      // back to position; scrolling from right-to-left
            card_view.scrollToPosition(MoonApsisAdapter.CENTER_POSITION);
            card_view.smoothScrollBy(1, 0); // triggers a snap
        }
    };
    private OnClickListener onResetClick1 = new OnClickListener() {
        @Override
        public void onClick(View v) {      // forward to position; scrolling from left-to-right
            card_view.scrollToPosition(MoonApsisAdapter.CENTER_POSITION + 1);
            card_view.smoothScrollBy(1, 0); // triggers a snap
        }
    };

    @SuppressLint("ResourceType")
    public void initTheme(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.buttonPressColor, R.attr.text_disabledColor, R.attr.colorBackgroundFloating, R.attr.text_accentColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        colorEnabled = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        colorPressed = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        colorDisabled = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        colorBackground = ColorUtils.setAlphaComponent(ContextCompat.getColor(context, typedArray.getResourceId(3, def)), (int)(9d * (254d / 10d)));
        colorAccent = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        typedArray.recycle();
        themeDrawables();
    }

    @Deprecated
    public void themeViews(Context context, SuntimesTheme theme)
    {
        colorPressed = theme.getActionColor();
        colorAccent = theme.getAccentColor();
        card_adapter.applyTheme(context, theme);
        themeDrawables();
    }

    private void themeDrawables()
    {
        ImageViewCompat.setImageTintList(forwardButton, SuntimesUtils.colorStateList(colorAccent, colorDisabled, colorPressed));
        ImageViewCompat.setImageTintList(backButton, SuntimesUtils.colorStateList(colorAccent, colorDisabled, colorPressed));

        if (Build.VERSION.SDK_INT < 21) {
            SuntimesUtils.colorizeImageView(forwardButton, colorBackground);
            SuntimesUtils.colorizeImageView(backButton, colorBackground);
        }
    }

    public void updateViews( Context context ) { /* EMPTY */ }

    public boolean isRising() {
        return card_adapter.isRising();
    }

    public void scrollToCenter() {
        card_layout.scrollToPositionWithOffset(MoonApsisAdapter.CENTER_POSITION, 0);
        card_view.smoothScrollBy(1, 0); // triggers a snap
    }
    public void scrollToDate( long datetime )
    {
        int position = card_adapter.getPositionForDate(getContext(), datetime);
        boolean alreadyInPosition = (position == card_layout.findFirstVisibleItemPosition());
        card_layout.scrollToPositionWithOffset(position, 0);
        if (!alreadyInPosition) {
            card_view.smoothScrollBy(1, 0);   // triggers snap
        }
    }

    public void lockScrolling() {
        card_view.setLayoutFrozen(true);
    }

    public void unlockScrolling() {
        card_view.setLayoutFrozen(false);
    }

    public void setOnClickListener( OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

    private MoonApsisViewListener viewListener = null;
    public void setViewListener(MoonApsisViewListener listener) {
        this.viewListener = listener;
    }
    
    /**
     * MoonApsisViewListener
     */
    public static class MoonApsisViewListener extends  MoonApsisAdapterListener {}

    /**
     * MoonApsisAdapterListener
     */
    public static class MoonApsisAdapterListener
    {
        public void onClick(View view, MoonApsisAdapter adapter, int position, boolean isRising) {}
    }

    /**
     * MoonApsisAdapter
     */
    public static class MoonApsisAdapter extends RecyclerView.Adapter<MoonApsisField>
    {
        public static final int MAX_POSITIONS = 200;
        public static final int CENTER_POSITION = 100;

        private WeakReference<Context> contextRef;
        @SuppressLint("UseSparseArrays")
        private HashMap<Integer, SuntimesMoonData0> data = new HashMap<>();
        private boolean isRising = false;

        private MoonApsisColorValues colors;
        private int colorNote, colorTitle, colorTime, colorText, colorDisabled;
        private Float spTitle = null, spTime = null, spText = null, spSuffix = null;
        private boolean boldTitle = false, boldTime = false;

        public MoonApsisAdapter(Context context)
        {
            contextRef = new WeakReference<>(context);
            initData(context);
            initTheme(context);
        }

        private int itemWidth = -1;
        public void setItemWidth( int pixels ) {
            itemWidth = pixels;
            notifyDataSetChanged();
        }

        @Override
        public MoonApsisField onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_view_moonapsis1, parent, false);
            return new MoonApsisField(view);
        }

        @Override
        public void onViewRecycled(MoonApsisField holder)
        {
            detachClickListeners(holder);
            if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2)) {
                data.remove(holder.position);
            }
            holder.position = RecyclerView.NO_POSITION;
        }

        @Override
        public void onBindViewHolder(MoonApsisField holder, int position)
        {
            Context context = contextRef.get();
            if (context == null) {
                Log.e("MoonApsisAdapter", "null context!");
                return;
            }

            if (itemWidth > 0) {
                holder.resizeField(itemWidth);
            }

            SuntimesMoonData0 moon = initData(context, position);

            int rawOffset = (position - CENTER_POSITION);
            boolean isAgo = rawOffset < 0;
            int offset = rawOffset % 2;

            //noinspection SimplifiableConditionalExpression
            holder.isRising = (isRising ? (offset == 0) : (offset != 0));
            themeViews(context, holder, isAgo);
            holder.bindDataToPosition(context, moon, holder.isRising, position);
            attachClickListeners(holder, position, holder.isRising);
        }

        protected void initData( Context context ) {
            SuntimesMoonData0 moon = initData(context, CENTER_POSITION);
            Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = moon.getMoonPerigee();
            Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = moon.getMoonApogee();
            isRising = (perigee.first != null && !perigee.first.before(apogee.first));
        }

        public SuntimesMoonData0 initData( Context context, int position )
        {
            int offset = (position - CENTER_POSITION) % 2;
            int firstPosition = position;
            if (offset > 0) {
                firstPosition = position - (offset);
            } else if (offset < 0) {
                firstPosition = position - (2 + (offset));
            }

            SuntimesMoonData0 moon = data.get(firstPosition);
            if (moon == null)
            {
                moon = createData(context, firstPosition);
                for (int i=0; i<2; i++) {
                    data.put(firstPosition + i, moon);
                }
            }
            return moon;
        }

        protected SuntimesMoonData0 createData( Context context, int position )
        {
            SuntimesMoonData moon = new SuntimesMoonData(context, 0, "moon");
            if (position != CENTER_POSITION)
            {
                SuntimesMoonData0 moon0 = initData(context, CENTER_POSITION);
                Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = moon0.getMoonPerigee();
                Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = moon0.getMoonApogee();
                if (perigee.first != null && apogee.first != null)
                {
                    Calendar date = Calendar.getInstance(moon.timezone());
                    date.setTimeInMillis(isRising ? apogee.first.getTimeInMillis() : perigee.first.getTimeInMillis());

                    double anomalisticMinutes = 27.55455d * 24d * 60d;  // may be up to 2 to 3 days longer than actual
                    date.add(Calendar.DATE, -7);                   // so offset 1 week

                    int rawOffset = position - CENTER_POSITION;
                    double minuteOffset = (rawOffset >> 1) * anomalisticMinutes;
                    date.add(Calendar.MINUTE, (int)minuteOffset);
                    moon.setTodayIs(date);

                    // e.g. on 2019-9-7 there is a 36 hr difference from mean
                    //double minuteOffset0 = (isRising ? perigee.first.getTimeInMillis() - apogee.first.getTimeInMillis()
                    //                                : apogee.first.getTimeInMillis() - perigee.first.getTimeInMillis()) / 1000d / 60d;
                    //double minuteOffset2 = 0.5d * 2 * 27.55455d * 24d * 60d;
                    //Log.d("DEBUG", "minuteOffset: " + minuteOffset2 / 60d + " (" + minuteOffset0 / 60d + ", " + ((minuteOffset0 - minuteOffset2) / 60d) + ")");
                }
            }
            moon.calculate(context);
            return moon;
        }

        public int getPositionForDate(Context context, long datetime)
        {
            double offset = 0;
            SuntimesMoonData0 moon0 = initData(context, CENTER_POSITION);
            Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = moon0.getMoonPerigee();
            Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = moon0.getMoonApogee();
            if (perigee.first != null && apogee.first != null)
            {
                long dateCenter = (isRising ? apogee.first.getTimeInMillis() : perigee.first.getTimeInMillis());
                long deltaMs = (datetime - dateCenter);
                double deltaHours = deltaMs / (1000d * 60d * 60d);
                double deltaMonth = (deltaHours / (27.55455d * 24d));
                offset = (2 * deltaMonth);
            }
            return (int)(CENTER_POSITION + offset);
        }

        @Override
        public int getItemCount() {
            return MAX_POSITIONS;
        }

        @SuppressLint("ResourceType")
        protected void initTheme(Context context)
        {
            colors = new MoonApsisColorValues(AndroidResources.wrap(context));
            int[] colorAttrs = { android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, R.attr.text_disabledColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;
            colorNote = colorTitle = colorTime = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            colorText = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            colorDisabled = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            typedArray.recycle();
        }

        protected void applyTheme(Context context, SuntimesTheme theme)
        {
            colorNote = theme.getTimeColor();
            colorTitle = theme.getTitleColor();
            colorTime = theme.getTimeColor();
            colorText = theme.getTextColor();
            colors.setColor(MoonApsisColorValues.COLOR_MOON_APOGEE_TEXT, theme.getMoonriseTextColor());
            colors.setColor(MoonApsisColorValues.COLOR_MOON_PERIGEE_TEXT, theme.getMoonsetTextColor());
            spText = theme.getTextSizeSp();
            spTime = theme.getTimeSizeSp();
            spTitle = theme.getTitleSizeSp();
            spSuffix = theme.getTimeSuffixSizeSp();
            boldTitle = theme.getTitleBold();
            boldTime = theme.getTimeBold();
        }

        protected void themeViews(Context context, @NonNull MoonApsisField holder, boolean isAgo)
        {
            holder.timeColor = colorNote;
            MoonApsisField.disabledColor = colorDisabled;

            int titleColor = isAgo ? colorDisabled : colorTitle;
            int timeColor = isAgo ? colorDisabled : colorTime;
            int textColor = isAgo ? colorDisabled : colorText;
            int apogeeColor = isAgo ? colorDisabled : colors.getColor(MoonApsisColorValues.COLOR_MOON_APOGEE_TEXT);
            int perigeeColor = isAgo ? colorDisabled : colors.getColor(MoonApsisColorValues.COLOR_MOON_PERIGEE_TEXT);
            holder.themeView(titleColor, textColor, timeColor, apogeeColor, perigeeColor, spTitle, boldTitle, spTime, boldTime, spText, spSuffix);
        }

        public boolean isRising() {
            return isRising;
        }

        private void attachClickListeners(@NonNull MoonApsisField holder, int position, boolean isRising) {
            holder.layout.setOnClickListener(onItemClick(position, isRising));
        }

        private void detachClickListeners(@NonNull MoonApsisField holder) {
            holder.layout.setOnClickListener(null);
        }

        private OnClickListener onItemClick(final int position, final boolean isRising) {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onClick(v, MoonApsisAdapter.this, position, isRising);
                    }
                }
            };
        }

        /**
         * setAdapterListener
         * @param listener
         */
        public void setAdapterListener( @NonNull MoonApsisAdapterListener listener ) {
            adapterListener = listener;
        }
        private MoonApsisAdapterListener adapterListener = new MoonApsisAdapterListener();
    }

    public void setColors(Context context, @Nullable ColorValues colors)
    {
        if (card_adapter != null)
        {
            if (colors != null) {
                card_adapter.colors = new MoonApsisColorValues(colors);
            } else {
                card_adapter.initTheme(context);
            }
            card_adapter.notifyDataSetChanged();
        }
    }
    public ColorValues getColors() {
        if (card_adapter != null) {
            return card_adapter.colors;
        } else return null;
    }

    /**
     * MoonApsisField
     */
    public static class MoonApsisField extends RecyclerView.ViewHolder
    {
        public int position = RecyclerView.NO_POSITION;
        public View layout;
        public TextView labelView;
        public TextView timeView;
        public TextView positionView;
        public TextView noteView;
        public boolean isRising = true;

        public int timeColor = Color.WHITE;
        public static int disabledColor = Color.GRAY;

        public MoonApsisField(View view)
        {
            super(view);
            layout = view.findViewById(R.id.moonapsis_layout);
            labelView = (TextView)view.findViewById(R.id.moonapsis_label);
            timeView = (TextView)view.findViewById(R.id.moonapsis_date);
            positionView = (TextView)view.findViewById(R.id.moonapsis_distance);
            noteView = (TextView)view.findViewById(R.id.moonapsis_note);
        }

        public void bindDataToPosition(Context context, SuntimesMoonData0 data, boolean isRising, int position)
        {
            this.position = position;
            this.isRising = isRising;

            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

            if (data != null && data.isCalculated())
            {
                Pair<Calendar, SuntimesCalculator.MoonPosition> event = isRising ? data.getMoonApogee() : data.getMoonPerigee();
                updateField(context, event, showTime, showWeeks, showHours, showSeconds, units);

            } else {
                updateField(context, null, showTime, showWeeks, showHours, showSeconds, units);
            }
        }

        public void themeView(int titleColor, int textColor, int timeColor, int risingColor, int settingColor, @Nullable Float titleSizeSp, boolean titleBold, @Nullable Float timeSizeSp, boolean timeBold, @Nullable Float textSizeSp, @Nullable Float suffixSizeSp)
        {
            this.timeColor = timeColor;
            timeView.setTextColor(timeColor);
            if (timeSizeSp != null) {
                timeView.setTextSize(timeSizeSp);
                timeView.setTypeface(timeView.getTypeface(), (timeBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            positionView.setTextColor(isRising ? risingColor : settingColor);
            if (suffixSizeSp != null) {
                positionView.setTextSize(suffixSizeSp);
            }

            noteView.setTextColor(textColor);
            if (timeSizeSp != null) {
                noteView.setTextSize(timeSizeSp);
            }

            labelView.setTextColor(titleColor);
            if (titleSizeSp != null) {
                labelView.setTextSize(titleSizeSp);
                labelView.setTypeface(labelView.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));
            }
        }

        public void updateField(Context context, Pair<Calendar,SuntimesCalculator.MoonPosition> apsis, boolean showTime, boolean showWeeks, boolean showHours, boolean showSeconds, LengthUnit units)
        {
            if (apsis != null)
            {
                labelView.setText(context.getString(isRising ? R.string.label_apogee : R.string.label_perigee));
                timeView.setText(utils.calendarDateTimeDisplayString(context, apsis.first, showTime, showSeconds).getValue());
                noteView.setText(createApsisNote(context, apsis.first, showWeeks, showHours, timeColor));
                positionView.setText(SuntimesUtils.formatAsDistance(context, LengthUnitDisplay.formatAsDistance(AndroidResources.wrap(context), apsis.second.distance, units, 2, true)));

                timeView.setVisibility(View.VISIBLE);
                noteView.setVisibility(View.VISIBLE);
                positionView.setVisibility(View.VISIBLE);
                labelView.setVisibility(View.VISIBLE);

            } else {
                timeView.setVisibility(View.GONE);
                noteView.setVisibility(View.GONE);
                positionView.setVisibility(View.GONE);
                labelView.setVisibility(View.GONE);
            }
        }

        public void resizeField(int pixels) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
            params.width = pixels;
            layout.setLayoutParams( params );
        }

        private CharSequence createApsisNote(Context context, Calendar dateTime, boolean showWeeks, boolean showHours, int noteColor)
        {
            Calendar now = Calendar.getInstance();
            String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
            String noteString = now.after(dateTime) ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
            return SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor);
        }

    }

}
