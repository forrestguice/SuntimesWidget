/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.cards;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class CardAdapter extends RecyclerView.Adapter<CardViewHolder>
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private WeakReference<Context> contextRef;

    private WidgetSettings.DateMode dateMode = WidgetSettings.DateMode.CURRENT_DATE;
    private WidgetSettings.DateInfo dateInfo = null;
    private TimeZone timezone = null;

    private boolean supportsGoldBlue = false;
    private boolean showSeconds = false;
    private boolean showWarnings = false;

    private boolean[] showFields = null;
    private boolean showActual = true;
    private boolean showCivil = true;
    private boolean showNautical = true;
    private boolean showAstro = true;
    private boolean showNoon = true;
    private boolean showGold = false;
    private boolean showBlue = false;

    private int color_textTimeDelta, color_enabled, color_disabled, color_pressed;

    private int highlightPosition = -1;
    private SolarEvents highlightEvent = null;

    public CardAdapter(Context context) {
        initTheme(context);
    }

    private void initTheme(Context context)
    {
        int[] attrs = new int[] { android.R.attr.textColorPrimary, R.attr.buttonPressColor, R.attr.text_disabledColor };
        TypedArray a = context.obtainStyledAttributes(attrs);
        color_textTimeDelta = ContextCompat.getColor(context, a.getResourceId(0, Color.WHITE));
        color_enabled = color_textTimeDelta;
        color_pressed = ContextCompat.getColor(context, a.getResourceId(1, R.color.btn_tint_pressed_dark));
        color_disabled = ContextCompat.getColor(context, a.getResourceId(2, R.color.text_disabled_dark));
        a.recycle();
    }

    public static final int MAX_POSITIONS = 2000;
    public static final int TODAY_POSITION = (MAX_POSITIONS / 2);      // middle position is today
    private HashMap<Integer, SuntimesRiseSetDataset> sunData = new HashMap<>();
    private HashMap<Integer, SuntimesMoonData> moonData = new HashMap<>();

    @Override
    public int getItemCount() {
        return MAX_POSITIONS;
    }

    public void initData(Context context, SuntimesRiseSetDataset sunSeed, SuntimesMoonData moonSeed)
    {
        initOptions(context, sunSeed, moonSeed);

        sunData.clear();
        moonData.clear();

        initData(context, TODAY_POSITION + 1);
        initData(context, TODAY_POSITION);
        initData(context, TODAY_POSITION - 1);

        notifyDataSetChanged();
    }

    protected void initData(Context context, int position)
    {
        Calendar date = Calendar.getInstance(timezone);
        if (dateMode != WidgetSettings.DateMode.CURRENT_DATE) {
            date.set(dateInfo.getYear(), dateInfo.getMonth(), dateInfo.getDay());
        }
        date.add(Calendar.DATE, position - TODAY_POSITION);

        SuntimesRiseSetDataset sun = new SuntimesRiseSetDataset(context);
        sun.setTodayIs(date);
        sun.calculateData();
        sunData.put(position, sun);

        SuntimesMoonData moon = new SuntimesMoonData(context, 0, "moon");
        moon.setTodayIs(date);
        moon.calculate();
        moonData.put(position, moon);
    }

    public void initOptions(Context context, SuntimesRiseSetDataset sunSeed, SuntimesMoonData moonSeed)
    {
        contextRef = new WeakReference<>(context);

        dateMode = WidgetSettings.loadDateModePref(context, 0);
        dateInfo = WidgetSettings.loadDatePref(context, 0);
        timezone = sunSeed.timezone();

        supportsGoldBlue = sunSeed.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
        showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
        showWarnings = AppSettings.loadShowWarningsPref(context);

        showFields = AppSettings.loadShowFieldsPref(context);
        showActual = showFields[AppSettings.FIELD_ACTUAL];
        showCivil = showFields[AppSettings.FIELD_CIVIL];
        showNautical = showFields[AppSettings.FIELD_NAUTICAL];
        showAstro = showFields[AppSettings.FIELD_ASTRO];
        showNoon = showFields[AppSettings.FIELD_NOON];
        showGold = showFields[AppSettings.FIELD_GOLD] && supportsGoldBlue;
        showBlue = showFields[AppSettings.FIELD_BLUE] && supportsGoldBlue;
    }

    /**
     * onViewRecycled
     * @param holder
     */
    @Override
    public void onViewRecycled(CardViewHolder holder) {
        detachClickListeners(holder);
    }

    /**
     * onCreateViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater layout = LayoutInflater.from(parent.getContext());
        View view = layout.inflate(R.layout.info_time_card1, parent, false);
        return new CardViewHolder(view);
    }

    /**
     * onBindViewHolder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(CardViewHolder holder, int position)
    {
        Context context = (contextRef != null ? contextRef.get() : null);
        if (context == null) {
            Log.w("CardAdapter", "onBindViewHolder: null context!");
            return;
        }
        if (holder == null) {
            Log.w("CardAdapter", "onBindViewHolder: null view holder!");
            return;
        }

        SuntimesRiseSetDataset sun = sunData.get(position);
        SuntimesMoonData moon = moonData.get(position);
        if (sun == null || moon == null)
        {
            initData(context, position);
            sun = sunData.get(position);
            moon = moonData.get(position);
        }

        if (themeOverride != null) {
            themeCardViews(context, themeOverride, holder);
        }
        themeCardViews(context, holder);

        holder.row_actual.setVisible(showActual);
        holder.row_civil.setVisible(showCivil);
        holder.row_nautical.setVisible(showNautical);
        holder.row_astro.setVisible(showAstro);
        holder.row_solarnoon.setVisible(showNoon);


        holder.row_blue8.setVisible(showBlue);
        holder.row_blue4.setVisible(showBlue);
        holder.row_gold.setVisible(showGold);

        holder.resetHighlight();
        if (highlightEvent != null && highlightPosition == position) {
            holder.highlightField(highlightEvent);
        }

        // sun fields
        if (sun != null && sun.isCalculated())
        {
            if (showActual) {
                SuntimesUtils.TimeDisplayText sunriseString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunsetCalendarToday(), showSeconds);
                holder.row_actual.updateFields(sunriseString_actualTime.toString(), sunsetString_actualTime.toString());
            }

            if (showCivil) {
                SuntimesUtils.TimeDisplayText sunriseString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunsetCalendarToday(), showSeconds);
                holder.row_civil.updateFields(sunriseString_civilTime.toString(), sunsetString_civilTime.toString());
            }

            if (showNautical) {
                SuntimesUtils.TimeDisplayText sunriseString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunsetCalendarToday(), showSeconds);
                holder.row_nautical.updateFields(sunriseString_nauticalTime.toString(), sunsetString_nauticalTime.toString());
            }

            if (showAstro) {
                SuntimesUtils.TimeDisplayText sunriseString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunsetCalendarToday(), showSeconds);
                holder.row_astro.updateFields(sunriseString_astroTime.toString(), sunsetString_astroTime.toString());
            }

            if (showNoon) {
                SuntimesUtils.TimeDisplayText noonString = utils.calendarTimeShortDisplayString(context, sun.dataNoon.sunriseCalendarToday(), showSeconds);
                holder.row_solarnoon.updateFields(noonString.toString());
            }

            if (showBlue) {
                String sunriseString_blue8 = utils.calendarTimeShortDisplayString(context, sun.dataBlue8.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_blue8 = utils.calendarTimeShortDisplayString(context, sun.dataBlue8.sunsetCalendarToday(), showSeconds).toString();
                holder.row_blue8.updateFields(sunriseString_blue8, sunsetString_blue8);

                String sunriseString_blue4 = utils.calendarTimeShortDisplayString(context, sun.dataBlue4.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_blue4 = utils.calendarTimeShortDisplayString(context, sun.dataBlue4.sunsetCalendarToday(), showSeconds).toString();
                holder.row_blue4.updateFields(sunriseString_blue4, sunsetString_blue4);
            }

            if (showGold) {
                String sunriseString_gold = utils.calendarTimeShortDisplayString(context, sun.dataGold.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_gold = utils.calendarTimeShortDisplayString(context, sun.dataGold.sunsetCalendarToday(), showSeconds).toString();
                holder.row_gold.updateFields(sunriseString_gold, sunsetString_gold);
            }

            updateDayLengthViews(context, holder.txt_daylength, sun.dataActual.dayLengthToday(), R.string.length_day);
            updateDayLengthViews(context, holder.txt_lightlength, sun.dataCivil.dayLengthToday(), R.string.length_light);

            // date field
            Calendar now = sun.now();
            Date data_date = sun.dataActual.date();
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context.getApplicationContext());   // Apr 11, 2016
            dateFormat.setTimeZone(sun.timezone());

            int i = 0;
            int diffDays = (int)((data_date.getTime() - now.getTimeInMillis()) / 1000L / 60L / 60L / 24L);
            if (data_date.after(now.getTime())) {
                i = diffDays + 1;
            } else if (data_date.before(now.getTime())) {
                i = diffDays;
            }

            boolean showDateWarning = (dateMode != WidgetSettings.DateMode.CURRENT_DATE && (i > 1 || i < -1));
            ImageSpan dateWarningIcon = (showWarnings && showDateWarning) ? SuntimesUtils.createWarningSpan(context, holder.txt_date.getTextSize()) : null;
            String dateString = context.getString(R.string.dateField, getCardLabel(context, i), dateFormat.format(data_date));
            SpannableStringBuilder dateSpan = SuntimesUtils.createSpan(context, dateString, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
            holder.txt_date.setText(dateSpan);
            holder.txt_date.setContentDescription(dateString.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), ""));

        } else {
            String notCalculated = context.getString(R.string.time_loading);
            holder.row_solarnoon.updateFields(notCalculated);
            holder.row_actual.updateFields(notCalculated, notCalculated);
            holder.row_civil.updateFields(notCalculated, notCalculated);
            holder.row_nautical.updateFields(notCalculated, notCalculated);
            holder.row_astro.updateFields(notCalculated, notCalculated);
            holder.row_gold.updateFields(notCalculated, notCalculated);
            holder.row_blue8.updateFields(notCalculated, notCalculated);
            holder.row_blue4.updateFields(notCalculated, notCalculated);
            holder.txt_daylength.setText("");
            holder.txt_lightlength.setText("");
            holder.txt_date.setText("\n\n");
        }

        // moon fields
        holder.sunsetHeader.measure(0, 0);      // adjust moonrise/moonset columns to match width of sunrise/sunset columns
        int sunsetHeaderWidth = holder.sunsetHeader.getMeasuredWidth();
        holder.moonrise.adjustColumnWidth(context, sunsetHeaderWidth);
        holder.moonphase.updateViews(context, moon);
        holder.moonrise.updateViews(context, moon);

        // listeners
        attachClickListeners(holder, position);
    }

    /**
     * @param context used to getStrings from resources
     * @param i position relative to TODAY
     * @return display string; today / tomorrow / yesterday / past (-n) / future (+n)
     */
    public String getCardLabel(Context context, int i )
    {
        String label = context.getString(R.string.today);
        if (i == 1) {
            label = context.getString(R.string.tomorrow);
        } else if (i == -1) {
            label = context.getString(R.string.yesterday);
        } else if (i > 0) {
            label = context.getString(R.string.future_n, Integer.toString(i));
        } else if (i < 0) {
            label = context.getString(R.string.past_n, Integer.toString(Math.abs(i)));
        }
        return label;
    }

    private void updateDayLengthViews(Context context, TextView textView, long dayLength, int labelID)
    {
        SuntimesUtils.TimeDisplayText dayLengthDisplay;
        if (dayLength <= 0)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 0, (showSeconds ? SuntimesUtils.strSeconds : SuntimesUtils.strMinutes)), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else if (dayLength >= SuntimesData.DAY_MILLIS)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 24, SuntimesUtils.strHours), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else dayLengthDisplay = utils.timeDeltaLongDisplayString(0, dayLength, showSeconds);

        dayLengthDisplay.setSuffix("");
        String dayLengthStr = dayLengthDisplay.toString();
        String dayLength_label = context.getString(labelID, dayLengthStr);
        textView.setText(SuntimesUtils.createBoldColorSpan(null, dayLength_label, dayLengthStr, color_textTimeDelta));
    }

    protected void themeCardViews(Context context, CardViewHolder holder)
    {
        if (themeOverride != null) {
            themeCardViews(context, themeOverride, holder);
        }
        ImageViewCompat.setImageTintList(holder.btn_flipperNext, SuntimesUtils.colorStateList(color_enabled, this.color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(holder.btn_flipperPrev, SuntimesUtils.colorStateList(color_enabled, this.color_disabled, color_pressed));
    }

    protected void themeCardViews(Context context, @NonNull SuntimesTheme theme, CardViewHolder holder)
    {
        color_textTimeDelta = theme.getTimeColor();
        color_pressed = theme.getActionColor();
        int color_text = theme.getTextColor();
        int color_sunrise = theme.getSunriseTextColor();
        int color_sunset = theme.getSunsetTextColor();
        int color_action = theme.getActionColor();

        holder.txt_daylength.setTextColor(color_text);
        holder.txt_lightlength.setTextColor(color_text);

        holder.row_actual.getField(0).setTextColor(color_sunrise);
        holder.row_civil.getField(0).setTextColor(color_sunrise);
        holder.row_nautical.getField(0).setTextColor(color_sunrise);
        holder.row_astro.getField(0).setTextColor(color_sunrise);
        holder.row_gold.getField(1).setTextColor(color_sunrise);
        holder.row_blue8.getField(0).setTextColor(color_sunrise);
        holder.row_blue4.getField(0).setTextColor(color_sunset);

        holder.row_actual.getField(1).setTextColor(color_sunset);
        holder.row_civil.getField(1).setTextColor(color_sunset);
        holder.row_nautical.getField(1).setTextColor(color_sunset);
        holder.row_astro.getField(1).setTextColor(color_sunset);
        holder.row_solarnoon.getField(0).setTextColor(color_sunset);
        holder.row_gold.getField(0).setTextColor(color_sunset);
        holder.row_blue8.getField(1).setTextColor(color_sunset);
        holder.row_blue4.getField(1).setTextColor(color_sunrise);

        int labelColor = theme.getTitleColor();
        for (CardViewHolder.TimeFieldRow row : holder.rows) {
            row.label.setTextColor(labelColor);
        }

        holder.txt_date.setTextColor(SuntimesUtils.colorStateList(labelColor, color_disabled, color_action));

        int sunriseIconColor = theme.getSunriseIconColor();
        int sunriseIconColor2 = theme.getSunriseIconStrokeColor();
        int sunriseIconStrokeWidth = theme.getSunriseIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)holder.icon_sunrise.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        holder.header_sunrise.setTextColor(color_sunrise);

        int sunsetIconColor = theme.getSunsetIconColor();
        int sunsetIconColor2 = theme.getSunsetIconStrokeColor();
        int sunsetIconStrokeWidth = theme.getSunsetIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)holder.icon_sunset.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        holder.header_sunset.setTextColor(color_sunset);

        holder.moonrise.themeViews(context, theme);
        holder.moonphase.themeViews(context, theme);
        holder.moonlabel.setTextColor(labelColor);
    }

    /**
     * Highlight next occurring event (and removes any previous highlight).
     * @param event SolarEvents enum
     * @return the event's card position if event was found and highlighted, -1 otherwise
     */
    public int highlightField(SolarEvents event)
    {
        highlightEvent = null;
        highlightPosition = -1;

        Calendar[] eventCalendars;
        int position = TODAY_POSITION;
        do {
            SuntimesMoonData moon = moonData.get(position);
            SuntimesRiseSetDataset sun = sunData.get(position);
            Calendar now = sun.now();

            boolean found;
            switch (event) {
                case MOONRISE: case MOONSET:
                    eventCalendars = moon.getRiseSetEvents(event);  // { yesterday, today, tomorrow }
                    found = now.before(eventCalendars[1]) && now.after(eventCalendars[0]);
                    break;
                default:
                    eventCalendars = sun.getRiseSetEvents(event);  // { today, tomorrow }
                    found = now.before(eventCalendars[0]);
                    break;
            }

            if (found) {
                highlightEvent = event;
                highlightPosition = position;
                break;
            }
            position++;
        } while (position < TODAY_POSITION + 2);

        notifyDataSetChanged();
        return highlightPosition;
    }

    private SuntimesTheme themeOverride = null;
    public void setThemeOverride(@NonNull SuntimesTheme theme) {
        themeOverride = theme;
    }

    /**
     * setCardAdapterListener
     * @param listener
     */
    public void setCardAdapterListener( @NonNull CardAdapterListener listener ) {
        adapterListener = listener;
    }
    private CardAdapterListener adapterListener = new CardAdapterListener();

    private void attachClickListeners(@NonNull CardViewHolder holder, int position)
    {
        holder.txt_date.setOnClickListener(onDateClick(position));
        holder.txt_date.setOnLongClickListener(onDateLongClick(position));
        holder.sunriseHeader.setOnClickListener(onSunriseHeaderClick(position));
        holder.sunriseHeader.setOnLongClickListener(onSunriseHeaderLongClick(position));
        holder.sunsetHeader.setOnClickListener(onSunsetHeaderClick(position));
        holder.sunsetHeader.setOnLongClickListener(onSunsetHeaderLongClick(position));
        holder.moonClickArea.setOnClickListener(onMoonHeaderClick(position));
        holder.moonClickArea.setOnLongClickListener(onMoonHeaderLongClick(position));
        holder.btn_flipperNext.setOnClickListener(onNextClick(position));
        holder.btn_flipperPrev.setOnClickListener(onPrevClick(position));
    }

    private void detachClickListeners(@NonNull CardViewHolder holder)
    {
        holder.txt_date.setOnClickListener(null);
        holder.txt_date.setOnLongClickListener(null);
        holder.sunriseHeader.setOnClickListener(null);
        holder.sunriseHeader.setOnLongClickListener(null);
        holder.sunsetHeader.setOnClickListener(null);
        holder.sunsetHeader.setOnLongClickListener(null);
        holder.moonClickArea.setOnClickListener(null);
        holder.moonClickArea.setOnLongClickListener(null);
        holder.btn_flipperNext.setOnClickListener(null);
        holder.btn_flipperPrev.setOnClickListener(null);
    }

    private View.OnClickListener onDateClick(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onDateClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnLongClickListener onDateLongClick(final int position) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return adapterListener.onDateLongClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnClickListener onSunriseHeaderClick(final int position) {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onSunriseHeaderClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnLongClickListener onSunriseHeaderLongClick(final int position)
    {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return adapterListener.onSunriseHeaderLongClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnClickListener onSunsetHeaderClick(final int position) {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onSunsetHeaderClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnLongClickListener onSunsetHeaderLongClick(final int position)
    {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return adapterListener.onSunsetHeaderLongClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnClickListener onMoonHeaderClick(final int position) {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onMoonHeaderClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnLongClickListener onMoonHeaderLongClick(final int position)
    {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return adapterListener.onMoonHeaderLongClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnClickListener onNextClick(final int position) {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onNextClick(CardAdapter.this, position);
            }
        };
    }
    private View.OnClickListener onPrevClick(final int position) {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onPrevClick(CardAdapter.this, position);
            }
        };
    }

    /**
     * CardAdapterListener
     */
    public static class CardAdapterListener
    {
        public void onDateClick(CardAdapter adapter, int position) {}
        public boolean onDateLongClick(CardAdapter adapter, int position)
        {
            return false;
        }

        public void onSunriseHeaderClick(CardAdapter adapter, int position) {}
        public boolean onSunriseHeaderLongClick(CardAdapter adapter, int position)
        {
            return false;
        }

        public void onSunsetHeaderClick(CardAdapter adapter, int position) {}
        public boolean onSunsetHeaderLongClick(CardAdapter adapter, int position)
        {
            return false;
        }

        public void onMoonHeaderClick(CardAdapter adapter, int position) {}
        public boolean onMoonHeaderLongClick(CardAdapter adapter, int position)
        {
            return false;
        }

        public void onNextClick(CardAdapter adapter, int position) {}
        public void onPrevClick(CardAdapter adapter, int position) {}
    }

    /**
     * CardViewDecorator
     */
    public static class CardViewDecorator extends RecyclerView.ItemDecoration
    {
        private int marginPx;

        public CardViewDecorator( Context context ) {
            marginPx = (int)context.getResources().getDimension(R.dimen.activity_margin);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            outRect.left = outRect.right = marginPx;
            outRect.top = outRect.bottom = 0;
        }
    }

    /**
     * CardViewScroller
     */
    public static class CardViewScroller extends LinearSmoothScroller
    {
        private static final float MILLISECONDS_PER_INCH = 125f;

        public CardViewScroller(Context context) {
            super(context);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }
    }
}


