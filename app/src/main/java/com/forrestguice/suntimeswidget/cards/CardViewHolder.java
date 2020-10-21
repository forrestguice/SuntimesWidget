/**
    Copyright (C) 2019-2020 Forrest Guice
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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.LightMapView;
import com.forrestguice.suntimeswidget.MoonPhaseView;
import com.forrestguice.suntimeswidget.MoonRiseSetView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CardViewHolder extends RecyclerView.ViewHolder
{
    protected static SuntimesUtils utils = new SuntimesUtils();

    public ImageButton btn_flipperNext;
    public ImageButton btn_flipperPrev;

    public View sunriseHeader;
    public TextView header_sunrise;
    public ImageView icon_sunrise;

    public View sunsetHeader;
    public TextView header_sunset;
    public ImageView icon_sunset;

    public TextView txt_date;

    public ArrayList<TimeFieldRow> rows;
    public TimeFieldRow row_astro, row_nautical, row_civil, row_actual, row_solarnoon;
    public TimeFieldRow row_gold, row_blue8, row_blue4;
    public HashMap<SolarEvents, TextView> timeFields;

    public LinearLayout layout_daylength;
    public TextView txt_daylength;
    public TextView txt_lightlength;

    public TextView moonlabel;
    public MoonPhaseView moonphase;
    public MoonRiseSetView moonrise;
    public View moonClickArea;

    public LightMapView lightmap;
    public View lightmapLayout;

    public int position = RecyclerView.NO_POSITION;

    public CardViewHolder(View view, CardAdapter.CardAdapterOptions options)
    {
        super(view);

        txt_date = (TextView) view.findViewById(R.id.text_date);

        sunriseHeader = view.findViewById(R.id.header_time_sunrise);
        header_sunrise = (TextView) view.findViewById(R.id.label_time_sunrise);
        icon_sunrise = (ImageView) view.findViewById(R.id.icon_time_sunrise);

        sunsetHeader = view.findViewById(R.id.header_time_sunset);
        header_sunset = (TextView) view.findViewById(R.id.label_time_sunset);
        icon_sunset = (ImageView) view.findViewById(R.id.icon_time_sunset);

        layout_daylength = (LinearLayout) view.findViewById(R.id.layout_daylength);
        txt_daylength = (TextView) view.findViewById(R.id.text_daylength);
        txt_lightlength = (TextView) view.findViewById(R.id.text_lightlength);

        moonlabel = (TextView) view.findViewById(R.id.text_time_label_moon);
        moonphase = (MoonPhaseView) view.findViewById(R.id.moonphase_view);
        moonClickArea = view.findViewById(R.id.moonphase_clickArea);
        moonrise = (MoonRiseSetView) view.findViewById(R.id.moonriseset_view);
        moonrise.setShowExtraField(false);

        rows = new ArrayList<>();
        rows.add(row_actual = new TimeFieldRow(view, R.id.text_time_label_official, R.id.text_time_sunrise_actual, R.id.text_time_sunset_actual));
        rows.add(row_civil = new TimeFieldRow(view, R.id.text_time_label_civil, R.id.text_time_sunrise_civil, R.id.text_time_sunset_civil));
        rows.add(row_nautical = new TimeFieldRow(view, R.id.text_time_label_nautical, R.id.text_time_sunrise_nautical, R.id.text_time_sunset_nautical));
        rows.add(row_astro = new TimeFieldRow(view, R.id.text_time_label_astro, R.id.text_time_sunrise_astro, R.id.text_time_sunset_astro));
        rows.add(row_solarnoon = new TimeFieldRow(view, R.id.text_time_label_noon, R.id.text_time_noon));
        rows.add(row_gold = new TimeFieldRow(view, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening));
        rows.add(row_blue8 = new TimeFieldRow(view, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening));
        rows.add(row_blue4 = new TimeFieldRow(view, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening));

        timeFields = new HashMap<>();
        timeFields.put(SolarEvents.SUNRISE, row_actual.getField(0));
        timeFields.put(SolarEvents.SUNSET, row_actual.getField(1));
        timeFields.put(SolarEvents.MORNING_CIVIL, row_civil.getField(0));
        timeFields.put(SolarEvents.EVENING_CIVIL, row_civil.getField(1));
        timeFields.put(SolarEvents.MORNING_NAUTICAL, row_nautical.getField(0));
        timeFields.put(SolarEvents.EVENING_NAUTICAL, row_nautical.getField(1));
        timeFields.put(SolarEvents.MORNING_ASTRONOMICAL, row_astro.getField(0));
        timeFields.put(SolarEvents.EVENING_ASTRONOMICAL, row_astro.getField(1));
        timeFields.put(SolarEvents.NOON, row_solarnoon.getField(0));
        timeFields.put(SolarEvents.MORNING_GOLDEN, row_gold.getField(0));
        timeFields.put(SolarEvents.EVENING_GOLDEN, row_gold.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE8, row_blue8.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE8, row_blue8.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE4, row_blue4.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE4, row_blue4.getField(1));
        timeFields.put(SolarEvents.MOONRISE, moonrise.getTimeViews(SolarEvents.MOONRISE)[0]);
        timeFields.put(SolarEvents.MOONSET, moonrise.getTimeViews(SolarEvents.MOONSET)[0]);

        lightmap = (LightMapView) view.findViewById(R.id.info_time_lightmap);

        lightmapLayout = view.findViewById(R.id.info_time_lightmap_layout);
        lightmapLayout.setClickable(true);

        btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
        btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);

        themeCardViews(view.getContext(), options);
    }

    public void bindDataToPosition(@NonNull Context context, int position, Pair<SuntimesRiseSetDataset, SuntimesMoonData> data, CardAdapter.CardAdapterOptions options)
    {
        this.position = position;
        SuntimesRiseSetDataset sun = ((data == null) ? null : data.first);
        SuntimesMoonData moon = ((data == null) ? null : data.second);

        row_actual.setVisible(options.showActual);
        row_civil.setVisible(options.showCivil);
        row_nautical.setVisible(options.showNautical);
        row_astro.setVisible(options.showAstro);
        row_solarnoon.setVisible(options.showNoon);
        row_blue8.setVisible(options.showBlue);
        row_blue4.setVisible(options.showBlue);
        row_gold.setVisible(options.showGold);

        resetHighlight();
        if (options.highlightEvent != null && options.highlightPosition == position) {
            highlightField(options.highlightEvent);
        }

        // sun fields
        if (sun != null && sun.isCalculated())
        {
            if (options.showActual) {
                SuntimesUtils.TimeDisplayText sunriseString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunriseCalendarToday(), options.showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunsetCalendarToday(), options.showSeconds);
                row_actual.updateFields(sunriseString_actualTime.toString(), sunsetString_actualTime.toString());
            }

            if (options.showCivil) {
                SuntimesUtils.TimeDisplayText sunriseString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunriseCalendarToday(), options.showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunsetCalendarToday(), options.showSeconds);
                row_civil.updateFields(sunriseString_civilTime.toString(), sunsetString_civilTime.toString());
            }

            if (options.showNautical) {
                SuntimesUtils.TimeDisplayText sunriseString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunriseCalendarToday(), options.showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunsetCalendarToday(), options.showSeconds);
                row_nautical.updateFields(sunriseString_nauticalTime.toString(), sunsetString_nauticalTime.toString());
            }

            if (options.showAstro) {
                SuntimesUtils.TimeDisplayText sunriseString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunriseCalendarToday(), options.showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunsetCalendarToday(), options.showSeconds);
                row_astro.updateFields(sunriseString_astroTime.toString(), sunsetString_astroTime.toString());
            }

            if (options.showNoon) {
                SuntimesUtils.TimeDisplayText noonString = utils.calendarTimeShortDisplayString(context, sun.dataNoon.sunriseCalendarToday(), options.showSeconds);
                row_solarnoon.updateFields(noonString.toString());
            }

            if (options.showBlue) {
                String sunriseString_blue8 = utils.calendarTimeShortDisplayString(context, sun.dataBlue8.sunriseCalendarToday(), options.showSeconds).toString();
                String sunsetString_blue8 = utils.calendarTimeShortDisplayString(context, sun.dataBlue8.sunsetCalendarToday(), options.showSeconds).toString();
                row_blue8.updateFields(sunriseString_blue8, sunsetString_blue8);

                String sunriseString_blue4 = utils.calendarTimeShortDisplayString(context, sun.dataBlue4.sunriseCalendarToday(), options.showSeconds).toString();
                String sunsetString_blue4 = utils.calendarTimeShortDisplayString(context, sun.dataBlue4.sunsetCalendarToday(), options.showSeconds).toString();
                row_blue4.updateFields(sunriseString_blue4, sunsetString_blue4);
            }

            if (options.showGold) {
                String sunriseString_gold = utils.calendarTimeShortDisplayString(context, sun.dataGold.sunriseCalendarToday(), options.showSeconds).toString();
                String sunsetString_gold = utils.calendarTimeShortDisplayString(context, sun.dataGold.sunsetCalendarToday(), options.showSeconds).toString();
                row_gold.updateFields(sunriseString_gold, sunsetString_gold);
            }

            updateDayLengthViews(context, txt_daylength, sun.dataActual.dayLengthToday(), R.string.length_day, options.showSeconds, options.color_textTimeDelta);
            updateDayLengthViews(context, txt_lightlength, sun.dataCivil.dayLengthToday(), R.string.length_light, options.showSeconds, options.color_textTimeDelta);

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

            boolean showDateWarning = (options.dateMode != WidgetSettings.DateMode.CURRENT_DATE && (i > 1 || i < -1));
            ImageSpan dateWarningIcon = (options.showWarnings && showDateWarning) ? SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.statusIcon_size)) : null;

            Pair<String,String> label = getCardLabel(context, i, options);
            String dateString = context.getString(R.string.dateField, label.first, dateFormat.format(data_date));
            SpannableStringBuilder dateSpan = SuntimesUtils.createSpan(context, dateString, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
            txt_date.setText((label.second == null) ? dateSpan : SuntimesUtils.createColorSpan(SpannableString.valueOf(dateSpan), dateString, label.second, options.color_warning));
            txt_date.setContentDescription(dateString.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), ""));

        } else {
            String notCalculated = context.getString(R.string.time_loading);
            row_solarnoon.updateFields(notCalculated);
            row_actual.updateFields(notCalculated, notCalculated);
            row_civil.updateFields(notCalculated, notCalculated);
            row_nautical.updateFields(notCalculated, notCalculated);
            row_astro.updateFields(notCalculated, notCalculated);
            row_gold.updateFields(notCalculated, notCalculated);
            row_blue8.updateFields(notCalculated, notCalculated);
            row_blue4.updateFields(notCalculated, notCalculated);
            txt_daylength.setText("");
            txt_lightlength.setText("");
            txt_date.setText("\n\n");
        }

        // moon fields
        boolean supportsMoon = (moon != null);
        int visibility = (supportsMoon && options.showMoon ? View.VISIBLE : View.GONE);
        moonClickArea.setVisibility(visibility);
        moonlabel.setVisibility(visibility);
        moonrise.setVisibility(visibility);
        moonphase.setVisibility(visibility);

        if (options.showMoon)
        {
            sunsetHeader.measure(0, 0);      // adjust moonrise/moonset columns to match width of sunrise/sunset columns
            int sunsetHeaderWidth = sunsetHeader.getMeasuredWidth();
            moonrise.adjustColumnWidth(context, sunsetHeaderWidth);
            moonphase.updateViews(context, moon);
            moonrise.updateViews(context, moon);
        }

        // lightmap
        lightmapLayout.setVisibility(options.showLightmap ? View.VISIBLE : View.GONE);
        lightmap.getColors().option_drawNow = (position == CardAdapter.TODAY_POSITION) ? LightMapView.LightMapColors.DRAW_SUN1 : LightMapView.LightMapColors.DRAW_SUN2;
        lightmap.updateViews(options.showLightmap ? sun : null);

        toggleNextPrevButtons(position);
    }

    public void toggleNextPrevButtons(int position)
    {
        int offset = (position - CardAdapter.TODAY_POSITION);
        if (offset > 1 || offset < -1) {
            btn_flipperNext.setVisibility(offset > 0 ? View.GONE : View.VISIBLE);
            btn_flipperPrev.setVisibility(offset < 0 ? View.GONE : View.VISIBLE);
        } else {
            btn_flipperNext.setVisibility(View.GONE);
            btn_flipperPrev.setVisibility(View.GONE);
        }
    }

    protected void themeCardViews(Context context, CardAdapter.CardAdapterOptions options)
    {
        if (options.themeOverride != null) {
            themeCardViews(context, options.themeOverride, options);
        }
        ImageViewCompat.setImageTintList(btn_flipperNext, SuntimesUtils.colorStateList(options.color_accent, options.color_disabled, options.color_pressed));
        ImageViewCompat.setImageTintList(btn_flipperPrev, SuntimesUtils.colorStateList(options.color_accent, options.color_disabled, options.color_pressed));
    }

    protected void themeCardViews(Context context, @NonNull SuntimesTheme theme, CardAdapter.CardAdapterOptions options)
    {
        options.color_textTimeDelta = theme.getTimeColor();
        options.color_pressed = theme.getActionColor();
        options.color_warning = theme.getActionColor();
        options.color_accent = theme.getAccentColor();
        int color_text = theme.getTextColor();
        int color_sunrise = theme.getSunriseTextColor();
        int color_sunset = theme.getSunsetTextColor();
        int color_action = theme.getActionColor();

        txt_daylength.setTextColor(color_text);
        txt_lightlength.setTextColor(color_text);

        row_actual.getField(0).setTextColor(color_sunrise);
        row_civil.getField(0).setTextColor(color_sunrise);
        row_nautical.getField(0).setTextColor(color_sunrise);
        row_astro.getField(0).setTextColor(color_sunrise);
        row_gold.getField(1).setTextColor(color_sunrise);
        row_blue8.getField(0).setTextColor(color_sunrise);
        row_blue4.getField(0).setTextColor(color_sunset);

        row_actual.getField(1).setTextColor(color_sunset);
        row_civil.getField(1).setTextColor(color_sunset);
        row_nautical.getField(1).setTextColor(color_sunset);
        row_astro.getField(1).setTextColor(color_sunset);
        row_solarnoon.getField(0).setTextColor(color_sunset);
        row_gold.getField(0).setTextColor(color_sunset);
        row_blue8.getField(1).setTextColor(color_sunset);
        row_blue4.getField(1).setTextColor(color_sunrise);

        int labelColor = theme.getTitleColor();
        for (CardViewHolder.TimeFieldRow row : rows) {
            row.label.setTextColor(labelColor);
        }

        txt_date.setTextColor(SuntimesUtils.colorStateList(labelColor, options.color_disabled, color_action));

        int sunriseIconColor = theme.getSunriseIconColor();
        int sunriseIconColor2 = theme.getSunriseIconStrokeColor();
        int sunriseIconStrokeWidth = theme.getSunriseIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunrise.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        header_sunrise.setTextColor(color_sunrise);

        int sunsetIconColor = theme.getSunsetIconColor();
        int sunsetIconColor2 = theme.getSunsetIconStrokeColor();
        int sunsetIconStrokeWidth = theme.getSunsetIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunset.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        header_sunset.setTextColor(color_sunset);

        moonrise.themeViews(context, theme);
        moonphase.themeViews(context, theme);
        moonlabel.setTextColor(labelColor);

        lightmap.themeViews(context, theme);
    }

    /**
     * @param context used to getStrings from resources
     * @param i position relative to TODAY
     * @return display string; today / tomorrow / yesterday / past (-n) / future (+n)
     */
    private Pair<String,String> getCardLabel(Context context, int i, CardAdapter.CardAdapterOptions options)
    {
        String dayOffset = ((i < 0) ? "-" : "+") + Integer.toString(Math.abs(i));
        String label = context.getString(R.string.today);
        if (i == 1) {
            return new Pair<>(context.getString(R.string.tomorrow), null);
        } else if (i == -1) {
            return new Pair<>(context.getString(R.string.yesterday), null);
        } else if (i > 0) {
            return new Pair<>(context.getString(R.string.future_n, dayOffset), dayOffset);
        } else if (i < 0) {
            return new Pair<>(context.getString(R.string.past_n, dayOffset), dayOffset);
        }
        return new Pair<>(label, null);
    }

    private void updateDayLengthViews(Context context, TextView textView, long dayLength, int labelID, boolean showSeconds, int highlightColor)
    {
        SuntimesUtils.TimeDisplayText dayLengthDisplay;
        if (dayLength <= 0)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 0, (showSeconds ? SuntimesUtils.strSeconds : SuntimesUtils.strMinutes)), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else if (dayLength >= SuntimesData.DAY_MILLIS)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 24, SuntimesUtils.strHours), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else dayLengthDisplay = utils.timeDeltaLongDisplayString(0, dayLength, showSeconds);

        dayLengthDisplay.setSuffix("");
        String dayLengthStr = dayLengthDisplay.getValue();
        String dayLength_label = context.getString(labelID, dayLengthStr);
        textView.setText(SuntimesUtils.createBoldColorSpan(null, dayLength_label, dayLengthStr, highlightColor));
    }

    public void highlightField( SolarEvents highlightEvent )
    {
        for (SolarEvents event : timeFields.keySet()) {
            if (event == highlightEvent) {
                TimeFieldRow.highlight(timeFields.get(event));
                break;
            }
        }
    }

    public void resetHighlight()
    {
        for (TimeFieldRow row : rows) {
            row.resetHighlight();
        }
        TextView[] views0 = moonrise.getTimeViews(SolarEvents.MOONRISE);
        for (TextView view : views0) {
            TimeFieldRow.resetHighlight(view);
        }
        TextView[] views1 = moonrise.getTimeViews(SolarEvents.MOONSET);
        for (TextView view : views1) {
            TimeFieldRow.resetHighlight(view);
        }
    }

    /**
     * TimeFieldRow
     */
    public static class TimeFieldRow
    {
        protected TextView label;
        private TextView[] fields;

        public TimeFieldRow( TextView label, TextView ...fields )
        {
            this.label = label;
            this.fields = fields;
        }

        public TimeFieldRow(View parent, int labelID, int... fieldIDs)
        {
            if (parent != null)
            {
                this.label = (TextView) parent.findViewById(labelID);
                this.fields = new TextView[fieldIDs.length];

                for (int i=0; i<fieldIDs.length; i++) {
                    this.fields[i] = (TextView) parent.findViewById(fieldIDs[i]);
                }
            }
        }

        public TextView getLabel()
        {
            return label;
        }

        public TextView getField( int i )
        {
            if (i >= 0 && i < fields.length)
                return fields[i];
            else return null;
        }

        public void resetHighlight()
        {
            for (int i=0; i<fields.length; i++) {
                if (fields[i] != null) {
                    resetHighlight(fields[i]);
                }
            }
        }

        public static void highlight(TextView textView)
        {
            if (textView != null && textView.getVisibility() == View.VISIBLE) {
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }

        public static void resetHighlight(TextView textView)
        {
            if (textView != null && textView.getVisibility() == View.VISIBLE) {
                textView.setTypeface(Typeface.create(textView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            }
        }

        public void updateFields( String ...values )
        {
            for (int i=0; i<values.length; i++)
            {
                if (i >= fields.length)
                    break;

                if (fields[i] != null) {
                    fields[i].setText( values[i] );
                }
            }
        }

        public void setVisible( boolean show )
        {
            int visibility = (show ? View.VISIBLE : View.GONE);

            if (label != null) {
                label.setVisibility(visibility);
            }

            for (int i=0; i<fields.length; i++) {
                if (fields[i] != null) {
                    fields[i].setVisibility(visibility);
                }
            }
        }

    }

}