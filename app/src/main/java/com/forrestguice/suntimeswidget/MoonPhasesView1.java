/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

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
    private TextView empty;

    public MoonPhasesView1(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhasesView1(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //applyAttributes(context, attrs);
        init(context, attrs);
    }

    /**private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }
    }*/

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
        //card_view.addItemDecoration(new CardAdapter.CardViewDecorator(this));

        card_adapter = new PhaseAdapter(context);
        card_adapter.setItemWidth(getWidth() / 4);

        card_view.setAdapter(card_adapter);
        card_view.scrollToPosition(PhaseAdapter.CENTER_POSITION);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        //card_scroller = new CardAdapter.CardViewScroller(context);
        //card_view.setOnScrollListener(onCardScrollListener);

        if (isInEditMode()) {
            updateViews(context, null);
        }
    }

    public void themeViews(Context context, SuntimesTheme theme) {
        card_adapter.applyTheme(context, theme);
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.MoonPhaseMode.initDisplayStrings(context);
        MoonPhaseDisplay.initDisplayStrings(context);
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        card_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesMoonData data )
    {
        if (isInEditMode()) {
            return;
        }

        if (data == null) {
            return;
        }

        if (!data.isCalculated()) {
            showEmptyView(true);
        }
    }

    public void setOnClickListener( OnClickListener listener )
    {
        // TODO
        //content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        // TODO
        //content.setOnLongClickListener(listener);
    }

    @Override
    public void onSizeChanged( int w, int h, int oldWidth, int oldHeight )
    {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        if (card_adapter != null) {
            card_adapter.setItemWidth(w / 4);
        }
    }

    /**
     * PhaseAdapter
     */
    public static class PhaseAdapter extends RecyclerView.Adapter<PhaseField>
    {
        public static final int MAX_POSITIONS = 200;
        public static final int CENTER_POSITION = 100;

        private WeakReference<Context> contextRef;
        private HashMap<Integer, SuntimesMoonData> data = new HashMap<>();
        private SuntimesCalculator.MoonPhase nextPhase = SuntimesCalculator.MoonPhase.FULL;

        private int colorNote, colorTitle, colorTime, colorText, colorWaxing, colorWaning, colorFull, colorNew;
        private float strokePixelsNew, strokePixelsFull;

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
            themeViews(context, holder);

            SuntimesMoonData moon = initData(context, position);
            holder.bindDataToPosition(context, moon, holder.phase, position);

        }

        @Override
        public void onViewRecycled(PhaseField holder)
        {
            if (holder.position >= 0 && (holder.position < CENTER_POSITION - 1 || holder.position > CENTER_POSITION + 2)) {
                data.remove(holder.position);
                Log.d("DEBUG", "remove data " + holder.position);
            }
            holder.position = RecyclerView.NO_POSITION;
        }

        @Override
        public int getItemCount() {
            return MAX_POSITIONS;
        }

        protected void initData( Context context ) {
            SuntimesMoonData moon = initData(context, CENTER_POSITION);
            nextPhase = moon.nextPhase(moon.calendar());
        }

        protected SuntimesMoonData initData( Context context, int position )
        {
            int offset = (position - CENTER_POSITION) % 4;
            int firstPosition = position;
            if (offset != 0) {
                firstPosition -= offset;
            }

            SuntimesMoonData moon = data.get(firstPosition);
            if (moon == null)
            {
                moon = createData(context, firstPosition);
                for (int i=0; i<4; i++) {
                    data.put(firstPosition + i, moon);
                }
            }
            return moon;
        }

        protected SuntimesMoonData createData( Context context, int position )
        {
            SuntimesMoonData moon = new SuntimesMoonData(context, 0, "moon");
            Calendar date = Calendar.getInstance(moon.timezone());
            date.add(Calendar.DATE, (int)(((position - CENTER_POSITION) / 4d) * 30d));  // 29.51 + 1
            moon.setTodayIs(date);
            moon.calculate();
            return moon;
        }

        @SuppressLint("ResourceType")
        protected void initTheme(Context context)
        {
            int[] colorAttrs = { android.R.attr.textColorPrimary, android.R.attr.textColorSecondary };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;
            colorNote = colorTitle = colorTime = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            colorText = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
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
        }

        protected void themeViews(Context context, @NonNull PhaseField holder)
        {
            Bitmap bitmap;
            switch (holder.phase)
            {
                case NEW: bitmap = SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, (int)strokePixelsNew); break;
                case FIRST_QUARTER: bitmap = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0); break;
                case THIRD_QUARTER: bitmap = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0); break;
                case FULL: default: bitmap = SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, (int)strokePixelsFull); break;
            }
            holder.noteColor = colorNote;
            holder.themeViews(colorTitle, colorTime, colorText, bitmap);
        }
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

        public int position = RecyclerView.NO_POSITION;
        public SuntimesCalculator.MoonPhase phase = SuntimesCalculator.MoonPhase.FULL;

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

        public void bindDataToPosition(Context context, SuntimesMoonData data, SuntimesCalculator.MoonPhase phase, int position)
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
            MoonPhaseDisplay phaseDisplay = SuntimesMoonData.toPhase(phase);
            String phaseLabel = phaseDisplay.getLongDisplayString();
            if (phase == SuntimesCalculator.MoonPhase.FULL || phase == SuntimesCalculator.MoonPhase.NEW)
            {
                SuntimesCalculator.MoonPosition phasePosition = data.calculator().getMoonPosition(phaseDate);
                if (phasePosition != null)
                {
                    if (SuntimesMoonData.isSuperMoon(phasePosition)) {
                        phaseLabel = context.getString(phase == SuntimesCalculator.MoonPhase.FULL ? R.string.timeMode_moon_superfull : R.string.timeMode_moon_supernew);
                    } else if (SuntimesMoonData.isMicroMoon(phasePosition)) {
                        phaseLabel = context.getString(phase == SuntimesCalculator.MoonPhase.FULL ? R.string.timeMode_moon_microfull : R.string.timeMode_moon_micronew);
                    }
                }
            }

            updateField(context, data.now(), phaseDate, showWeeks, showTime, showHours, showSeconds);
            setLabel(phaseLabel);
        }

        public void themeViews(int labelColor, int timeColor, int textColor, @NonNull Bitmap bitmap)
        {
            label.setTextColor(labelColor);
            field.setTextColor(timeColor);
            note.setTextColor(textColor);
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
                String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
                String noteString = now.after(dateTime) ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
                note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor));
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
