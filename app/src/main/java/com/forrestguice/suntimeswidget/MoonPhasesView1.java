/**
    Copyright (C) 2018-2022 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData1;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public class MoonPhasesView1 extends LinearLayout
{
    private static SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;

    private RecyclerView card_view;
    private PhaseAdapter card_adapter;
    private LinearLayoutManager card_layout;
    private ImageButton forwardButton, backButton;
    private TextView empty;

    private int colorEnabled = Color.WHITE, colorDisabled = Color.GRAY, colorPressed = Color.GREEN, colorAccent = Color.BLUE, colorBackground = Color.BLACK;

    public MoonPhasesView1(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhasesView1(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonphases1, this, true);

        if (attrs != null)
        {
            LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);
        card_view = (RecyclerView)findViewById(R.id.moonphases_card);

        card_layout = new LinearLayoutManager(context);
        card_layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.setLayoutManager(card_layout);

        card_adapter = new PhaseAdapter(context);
        card_adapter.setAdapterListener(card_listener);
        card_adapter.setItemWidth(Resources.getSystem().getDisplayMetrics().widthPixels / 4);  // initial width; reassigned later in onSizeChanged

        card_view.setAdapter(card_adapter);
        card_view.scrollToPosition(PhaseAdapter.CENTER_POSITION);

        GravitySnapHelper snapHelper = new GravitySnapHelper(Gravity.START); // new LinearSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        //card_scroller = new CardAdapter.CardViewScroller(context);
        //card_view.setOnScrollListener(onCardScrollListener);

        forwardButton = (ImageButton)findViewById(R.id.info_time_nextbtn);
        forwardButton.setOnClickListener(onResetClick1);
        forwardButton.setVisibility(GONE);

        backButton = (ImageButton)findViewById(R.id.info_time_prevbtn);
        backButton.setOnClickListener(onResetClick0);
        backButton.setVisibility(VISIBLE);
        backButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = card_layout.findFirstVisibleItemPosition();
                if (position == PhaseAdapter.CENTER_POSITION) {
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

    @SuppressLint("ResourceType")
    protected void initTheme(Context context)
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

    public void themeViews(Context context, SuntimesTheme theme)
    {
        card_adapter.applyTheme(context, theme);
        colorPressed = theme.getActionColor();
        colorAccent = theme.getAccentColor();
        themeDrawables();
    }

    private void themeDrawables()
    {
        ImageViewCompat.setImageTintList(forwardButton, SuntimesUtils.colorStateList(colorAccent, colorDisabled, colorPressed));
        SuntimesUtils.colorizeImageView(forwardButton, colorBackground);

        ImageViewCompat.setImageTintList(backButton, SuntimesUtils.colorStateList(colorAccent, colorDisabled, colorPressed));
        SuntimesUtils.colorizeImageView(backButton, colorBackground);
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        SuntimesUtils.initDisplayStrings(context);
        ViewUtils.initUtils(context);
        WidgetSettings.MoonPhaseMode.initDisplayStrings(context);
        MoonPhaseDisplay.initDisplayStrings(context);
    }

    @Override
    public void onSizeChanged( int w, int h, int oldWidth, int oldHeight )
    {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        if (card_adapter != null) {
            int margin = 8;
            card_adapter.setItemWidth((w - (margin * 2)) / 4);
        }
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        card_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context )
    {
        if (isInEditMode()) {
            return;
        }

        boolean hasSupport = false;
        if (card_adapter != null) {
            SuntimesMoonData1 data = card_adapter.initData(context, PhaseAdapter.CENTER_POSITION);
            hasSupport = (data != null && data.isCalculated());
        }
        showEmptyView( !hasSupport );
    }

    private PhaseAdapterListener card_listener = new PhaseAdapterListener()
    {
        @Override
        public void onClick(View v, PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase)
        {
            if (viewListener != null) {
                viewListener.onClick(v, adapter, position, phase);
            }
        }
    };

    private RecyclerView.OnScrollListener onScrollChanged = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            int position = card_layout.findFirstVisibleItemPosition();

            if (position < PhaseAdapter.CENTER_POSITION)
            {
                ViewUtils.fadeInButton(forwardButton, ViewUtils.ANIM_VERYLONG);
                backButton.setVisibility(View.GONE);

            } else if (position > PhaseAdapter.CENTER_POSITION) {
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
            card_view.scrollToPosition(PhaseAdapter.CENTER_POSITION);
            card_view.smoothScrollBy(1, 0); // triggers a snap
        }
    };
    private OnClickListener onResetClick1 = new OnClickListener() {
        @Override
        public void onClick(View v) {      // forward to position; scrolling from left-to-right
            card_view.scrollToPosition(PhaseAdapter.CENTER_POSITION + 3);
            card_view.smoothScrollBy(1, 0); // triggers a snap
        }
    };

    /**@Override
    public void setOnClickListener( OnClickListener listener )
    {
        super.setOnClickListener(listener);
        // TODO
        //content.setOnClickListener(listener);
    }*/

    /**@Override
    public void setOnLongClickListener( OnLongClickListener listener)
    {
        super.setOnLongClickListener(listener);
        // TODO
        //content.setOnLongClickListener(listener);
    }*/

    public void scrollToDate( long datetime )
    {
        int position = card_adapter.getPositionForDate(getContext(), datetime);
        position += ((position > PhaseAdapter.CENTER_POSITION) ? 3 : 0);
        card_view.scrollToPosition(position);
    }

    public void lockScrolling() {
        card_view.setLayoutFrozen(true);
    }

    public void unlockScrolling() {
        card_view.setLayoutFrozen(false);
    }

    private MoonPhasesViewListener viewListener = null;
    public void setViewListener(MoonPhasesViewListener listener) {
        this.viewListener = listener;
    }

    /**
     * MoonPhasesViewListener
     */
    public static class MoonPhasesViewListener extends PhaseAdapterListener {}

    /**
     * PhaseAdapterListener
     */
    public static class PhaseAdapterListener
    {
        public void onClick(View v, PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase) {}
    }

    /**
     * PhaseAdapter
     */
    public static class PhaseAdapter extends RecyclerView.Adapter<PhaseField>
    {
        public static final int MAX_POSITIONS = 200;
        public static final int CENTER_POSITION = 100;

        private WeakReference<Context> contextRef;
        @SuppressLint("UseSparseArrays")
        private HashMap<Integer, SuntimesMoonData1> data = new HashMap<>();
        private SuntimesCalculator.MoonPhase nextPhase = SuntimesCalculator.MoonPhase.FULL;

        private int colorNote, colorTitle, colorTime, colorText, colorWaxing, colorWaning, colorFull, colorNew, colorDisabled;
        private float strokePixelsNew, strokePixelsFull;
        private Float spTime = null, spText = null, spTitle = null, spSuffix = null;
        private boolean boldTime, boldTitle;

        public PhaseAdapter(Context context) {
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
        public PhaseField onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.info_time_moonphase, parent, false);
            return new PhaseField(view);
        }

        @Override
        public void onBindViewHolder(PhaseField holder, int position)
        {
            Context context = contextRef.get();
            if (context == null) {
                Log.e("PhaseAdapter", "null context!");
                return;
            }

            if (itemWidth > 0) {
                holder.resizeField(itemWidth);
            }

            int phaseOffset = (position - CENTER_POSITION) % 4;
            int phaseOrdinal = nextPhase.ordinal() + phaseOffset;
            while (phaseOrdinal >= 4) {
                phaseOrdinal = phaseOrdinal - 4;
            }
            while (phaseOrdinal < 0) {
                phaseOrdinal = phaseOrdinal + 4;
            }
            holder.phase = SuntimesCalculator.MoonPhase.values()[phaseOrdinal];

            SuntimesMoonData1 moon = initData(context, position);
            Calendar phaseDate = moon.moonPhaseCalendar(holder.phase);
            boolean isAgo = moon.now().after(phaseDate);
            holder.northward = WidgetSettings.loadLocalizeHemispherePref(context, 0) && (moon.location().getLatitudeAsDouble() < 0);
            themeViews(context, holder, isAgo);

            holder.bindDataToPosition(context, moon, holder.phase, position);
            attachClickListeners(holder, position);
        }

        @Override
        public void onViewRecycled(PhaseField holder)
        {
            detachClickListeners(holder);
            if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2)) {
                data.remove(holder.position);
            }
            holder.position = RecyclerView.NO_POSITION;
        }

        @Override
        public int getItemCount() {
            return MAX_POSITIONS;
        }

        protected void initData( Context context ) {
            SuntimesMoonData1 moon = initData(context, CENTER_POSITION);
            nextPhase = moon.nextPhase(moon.calendar());
        }

        public SuntimesMoonData1 initData( Context context, int position )
        {
            int offset = (position - CENTER_POSITION) % 4;
            int firstPosition = position;
            if (offset > 0) {
                firstPosition = position - (offset);
            } else if (offset < 0) {
                firstPosition = position - (4 + (offset));
            }

            SuntimesMoonData1 moon = data.get(firstPosition);
            if (moon == null)
            {
                moon = createData(context, firstPosition);
                for (int i=0; i<4; i++) {
                    data.put(firstPosition + i, moon);
                }
            }
            return moon;
        }

        protected SuntimesMoonData1 createData(Context context, int position )
        {
            SuntimesMoonData1 moon = new SuntimesMoonData1(context, 0, "moon");

            if (position != CENTER_POSITION)
            {
                SuntimesMoonData1 moon0 = initData(context, CENTER_POSITION);

                Calendar date = Calendar.getInstance(moon.timezone());
                date.setTimeInMillis(moon0.moonPhaseCalendar(moon0.nextPhase(moon.now())).getTimeInMillis());
                date.add(Calendar.HOUR, (int)(((position - CENTER_POSITION) / 4d) * 29.53d * 24d));   // avg length of synodic month (29.53) may vary ~ +-6 hr
                date.add(Calendar.HOUR, (int)(-24 * 3.5));                                            // so offset several days to overcome potential drift
                moon.setTodayIs(date);
            }

            moon.calculate();
            return moon;
        }

        public int getPositionForDate(Context context, long datetime)
        {
            SuntimesMoonData1 moon0 = initData(context, CENTER_POSITION);
            long dateCenter = moon0.moonPhaseCalendar(moon0.nextPhase(moon0.now())).getTimeInMillis();
            long deltaMs = (datetime - dateCenter);
            double deltaHours = deltaMs / (1000d * 60d * 60d);
            double deltaMonth = (deltaHours / (29.53d * 24d));
            return (int)(CENTER_POSITION + (4 * deltaMonth));
        }

        @SuppressLint("ResourceType")
        protected void initTheme(Context context)
        {
            int[] colorAttrs = { android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, R.attr.text_disabledColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;
            colorNote = colorTitle = colorTime = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            colorText = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
            colorDisabled = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
            typedArray.recycle();

            strokePixelsFull = strokePixelsNew = context.getResources().getDimension(R.dimen.moonIcon_stroke_full);
            colorWaxing = ContextCompat.getColor(context, R.color.moonIcon_color_waxing);
            colorWaning = ContextCompat.getColor(context, R.color.moonIcon_color_waning);
            colorFull = ContextCompat.getColor(context, R.color.moonIcon_color_full);
            colorNew = ContextCompat.getColor(context, R.color.moonIcon_color_new);
        }

        protected void applyTheme(Context context, SuntimesTheme theme)
        {
            colorNote = theme.getTimeColor();
            colorTitle = theme.getTitleColor();
            colorTime = theme.getTimeColor();
            colorText = theme.getTextColor();
            colorWaxing = theme.getMoonWaxingColor();
            colorWaning = theme.getMoonWaningColor();
            colorFull = theme.getMoonFullColor();
            colorNew = theme.getMoonNewColor();
            strokePixelsNew = theme.getMoonNewStrokePixels(context);
            strokePixelsFull = theme.getMoonFullStrokePixels(context);
            spTime = theme.getTimeSizeSp();
            spText = theme.getTextSizeSp();
            spTitle = theme.getTitleSizeSp();
            spSuffix = theme.getTimeSuffixSizeSp();
            boldTitle = theme.getTitleBold();
            boldTime = theme.getTimeBold();
        }

        protected void themeViews(Context context, @NonNull PhaseField holder, boolean isAgo)
        {
            Bitmap bitmap;
            switch (holder.phase)
            {
                case NEW: bitmap = SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(holder.northward), colorNew, colorWaxing, (int)strokePixelsNew); break;
                case FIRST_QUARTER: bitmap = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(holder.northward), colorWaxing, colorWaxing, 0); break;
                case THIRD_QUARTER: bitmap = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(holder.northward), colorWaning, colorWaning, 0); break;
                case FULL: default: bitmap = SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(holder.northward), colorFull, colorWaning, (int)strokePixelsFull); break;
            }
            holder.noteColor = colorNote;
            PhaseField.disabledColor = colorDisabled;

            int titleColor = isAgo ? colorDisabled : colorTitle;
            int timeColor = isAgo ? colorDisabled : colorTime;
            int textColor = isAgo ? colorDisabled : colorText;
            holder.themeViews(titleColor, spTitle, boldTitle, timeColor, spTime, boldTime, textColor, bitmap);
        }

        private void attachClickListeners(@NonNull PhaseField holder, int position) {
            holder.layout.setOnClickListener(onItemClick(position, holder.phase));
        }

        private void detachClickListeners(@NonNull PhaseField holder) {
            holder.layout.setOnClickListener(null);
        }

        private OnClickListener onItemClick(final int position, final SuntimesCalculator.MoonPhase phase) {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onClick(v, PhaseAdapter.this, position, phase);
                    }
                }
            };
        }

        /**
         * setAdapterListener
         * @param listener
         */
        public void setAdapterListener( @NonNull PhaseAdapterListener listener ) {
            adapterListener = listener;
        }
        private PhaseAdapterListener adapterListener = new PhaseAdapterListener();
    }

    /**
     * PhaseField
     */
    public static class PhaseField extends RecyclerView.ViewHolder
    {
        public View layout;
        public TextView field;
        public TextView note;
        public TextView label;
        public ImageView icon;

        public int noteColor = Color.WHITE;
        public static int disabledColor = Color.GRAY;

        public int position = RecyclerView.NO_POSITION;
        public SuntimesCalculator.MoonPhase phase = SuntimesCalculator.MoonPhase.FULL;
        public boolean northward = false;

        public PhaseField(@NonNull View parent)
        {
            super(parent);
            layout = parent.findViewById(R.id.moonphase_item_layout);
            label = (TextView)parent.findViewById(R.id.moonphase_item_label);
            field = (TextView)parent.findViewById(R.id.moonphase_item_date);
            note = (TextView)parent.findViewById(R.id.moonphase_item_note);
            icon = (ImageView)parent.findViewById(R.id.moonphase_item_icon);
        }

        public PhaseField(@NonNull View parent, int layoutID, int labelID, int dateTextID, int noteTextID, int imageViewID)
        {
            super(parent);
            layout = parent.findViewById(layoutID);
            label = (TextView)parent.findViewById(labelID);
            field = (TextView)parent.findViewById(dateTextID);
            note = (TextView)parent.findViewById(noteTextID);
            icon = (ImageView)parent.findViewById(imageViewID);
        }

        public void bindDataToPosition(Context context, SuntimesMoonData1 data, SuntimesCalculator.MoonPhase phase, int position)
        {
            this.position = position;
            this.phase = phase;

            showLabel(true);
            if (data == null || !data.isImplemented() || !data.isCalculated())
            {
                field.setText("");
                note.setText("");
                label.setText("");
                icon.setImageDrawable(null);
                return;
            }

            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

            Calendar phaseDate = data.moonPhaseCalendar(phase);
            MoonPhaseDisplay phaseDisplay = SuntimesMoonData1.toPhase(phase);
            CharSequence phaseLabel = phaseDisplay.getLongDisplayString();
            if (phase == SuntimesCalculator.MoonPhase.FULL || phase == SuntimesCalculator.MoonPhase.NEW)
            {
                SuntimesCalculator.MoonPosition phasePosition = data.calculator().getMoonPosition(phaseDate);
                if (phasePosition != null)
                {
                    if (SuntimesMoonData1.isSuperMoon(phasePosition)) {
                        String labelText = context.getString(phase == SuntimesCalculator.MoonPhase.FULL ? R.string.timeMode_moon_superfull : R.string.timeMode_moon_supernew);

                        if (phase == SuntimesCalculator.MoonPhase.FULL)
                            phaseLabel = SuntimesUtils.createBoldSpan(null, labelText, labelText);
                        else phaseLabel = SuntimesUtils.createItalicSpan(null, labelText, labelText);

                    } else if (SuntimesMoonData1.isMicroMoon(phasePosition)) {
                        String labelText = context.getString(phase == SuntimesCalculator.MoonPhase.FULL ? R.string.timeMode_moon_microfull : R.string.timeMode_moon_micronew);
                        phaseLabel = SuntimesUtils.createItalicSpan(null, labelText, labelText);
                    }
                }
            }

            updateField(context, data.now(), phaseDate, showWeeks, showTime, showHours, showSeconds);
            setLabel(phaseLabel);
        }

        public void themeViews(int labelColor, @Nullable Float labelSizeSp, boolean labelBold, int timeColor, @Nullable Float timeSizeSp, boolean timeBold, int textColor, @NonNull Bitmap bitmap)
        {
            label.setTextColor(labelColor);
            if (labelSizeSp != null) {
                label.setTextSize(labelSizeSp);
                label.setTypeface(label.getTypeface(), (labelBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            field.setTextColor(timeColor);
            note.setTextColor(textColor);

            if (timeSizeSp != null) {
                note.setTextSize(timeSizeSp);
                field.setTextSize(timeSizeSp);
                field.setTypeface(field.getTypeface(), (timeBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            icon.setImageBitmap(bitmap);
        }

        public void updateField(Context context, Calendar now, Calendar dateTime, boolean showWeeks, boolean showTime, boolean showHours, boolean showSeconds)
        {
            if (field != null)
            {
                field.setText(utils.calendarDateTimeDisplayString(context, dateTime, showTime, showSeconds).getValue());
            }

            if (note != null)
            {
                boolean isAgo = now.after(dateTime);
                String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
                String noteString = isAgo ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
                note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, noteText, (isAgo ? disabledColor : noteColor)));
                note.setVisibility(View.VISIBLE);
            }
        }

        public void setLabel(CharSequence text)
        {
            label.setText(text);
        }

        public void showLabel(boolean value)
        {
            label.setVisibility(value ? View.VISIBLE : View.GONE);
        }

        public void resizeField(int pixels) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
            params.width = pixels;
            layout.setLayoutParams( params );
        }
    }

}
