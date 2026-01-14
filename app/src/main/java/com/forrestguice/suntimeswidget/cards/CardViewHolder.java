/**
    Copyright (C) 2019-2023 Forrest Guice
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;

import com.forrestguice.suntimeswidget.calculator.settings.display.AngleDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.graph.LightMapDialog;
import com.forrestguice.support.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import com.forrestguice.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.DateMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.events.DayPercentEvent;
import com.forrestguice.suntimeswidget.events.ElevationEvent;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventSettingsInterface;
import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.suntimeswidget.events.ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.SunElevationEvent;
import com.forrestguice.suntimeswidget.graph.LightMapView;
import com.forrestguice.suntimeswidget.graph.SunSymbol;
import com.forrestguice.suntimeswidget.graph.SunSymbolBitmap;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.moon.MoonPhaseView;
import com.forrestguice.suntimeswidget.moon.MoonRiseSetView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.support.widget.RecyclerView;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import static com.forrestguice.suntimeswidget.graph.LightMapView.LightMapColors.MAPTAG_LIGHTMAP;
import static com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings.PREF_DEF_GRAPH_SUNSYMBOL;

public class CardViewHolder extends RecyclerView.ViewHolder
{
    protected static SuntimesUtils utils = new SuntimesUtils();
    protected static AngleDisplay angle_utils = new AngleDisplay();

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
    public TimeFieldRow row_astro, row_nautical, row_civil, row_actual, row_solarnoon, row_midnight;
    public TimeFieldRow row_gold, row_blue8, row_blue4;
    public HashMap<String, TextView> timeFields;
    public View noonClickArea;

    public CustomRows customRows;

    public LinearLayout layout_daylength;
    public TextView txt_daylength;
    public TextView txt_lightlength;

    public TextView moonlabel;
    public MoonPhaseView moonphase;
    public MoonRiseSetView moonrise;
    public View moonClickArea;

    public LightMapView lightmap;
    public View lightmapLayout;

    public TextView txt_comparison;

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

        noonClickArea = view.findViewById(R.id.noon_clickArea);

        moonlabel = (TextView) view.findViewById(R.id.text_time_label_moon);
        moonphase = (MoonPhaseView) view.findViewById(R.id.moonphase_view);
        moonClickArea = view.findViewById(R.id.moonphase_clickArea);
        moonrise = (MoonRiseSetView) view.findViewById(R.id.moonriseset_view);
        moonrise.setShowExtraField(false);

        txt_comparison = (TextView) view.findViewById(R.id.text_comparison);

        rows = new ArrayList<>();
        rows.add(row_actual = new TimeFieldRow(view, R.id.text_time_label_official, R.id.text_time_sunrise_actual, R.id.text_time_sunset_actual));
        rows.add(row_civil = new TimeFieldRow(view, R.id.text_time_label_civil, R.id.text_time_sunrise_civil, R.id.text_time_sunset_civil));
        rows.add(row_nautical = new TimeFieldRow(view, R.id.text_time_label_nautical, R.id.text_time_sunrise_nautical, R.id.text_time_sunset_nautical));
        rows.add(row_astro = new TimeFieldRow(view, R.id.text_time_label_astro, R.id.text_time_sunrise_astro, R.id.text_time_sunset_astro));
        rows.add(row_solarnoon = new TimeFieldRow(view, R.id.text_time_label_noon, R.id.text_position_noon, R.id.text_time_noon));
        rows.add(row_gold = new TimeFieldRow(view, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening));
        rows.add(row_blue8 = new TimeFieldRow(view, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening));
        rows.add(row_blue4 = new TimeFieldRow(view, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening));
        rows.add(row_midnight = new TimeFieldRow(view, R.id.text_time_label_midnight, R.id.text_time_midnight));

        Set<String> customEvents = EventSettings.loadVisibleEvents(AndroidEventSettings.wrap(view.getContext()));
        customRows = new CustomRows(view, options);
        rows.addAll(customRows.initRows(view.getContext(), customEvents));

        timeFields = new HashMap<>();
        timeFields.put(SolarEvents.SUNRISE.name(), row_actual.getField(0));
        timeFields.put(SolarEvents.SUNSET.name(), row_actual.getField(1));
        timeFields.put(SolarEvents.MORNING_CIVIL.name(), row_civil.getField(0));
        timeFields.put(SolarEvents.EVENING_CIVIL.name(), row_civil.getField(1));
        timeFields.put(SolarEvents.MORNING_NAUTICAL.name(), row_nautical.getField(0));
        timeFields.put(SolarEvents.EVENING_NAUTICAL.name(), row_nautical.getField(1));
        timeFields.put(SolarEvents.MORNING_ASTRONOMICAL.name(), row_astro.getField(0));
        timeFields.put(SolarEvents.EVENING_ASTRONOMICAL.name(), row_astro.getField(1));
        timeFields.put(SolarEvents.NOON.name(), row_solarnoon.getField(1));
        timeFields.put(SolarEvents.MIDNIGHT.name(), row_midnight.getField(0));
        timeFields.put(SolarEvents.MORNING_GOLDEN.name(), row_gold.getField(0));
        timeFields.put(SolarEvents.EVENING_GOLDEN.name(), row_gold.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE8.name(), row_blue8.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE8.name(), row_blue8.getField(1));
        timeFields.put(SolarEvents.MORNING_BLUE4.name(), row_blue4.getField(0));
        timeFields.put(SolarEvents.EVENING_BLUE4.name(), row_blue4.getField(1));
        timeFields.put(SolarEvents.MOONRISE.name(), moonrise.getTimeViews(SolarEvents.MOONRISE)[0]);
        timeFields.put(SolarEvents.MOONSET.name(), moonrise.getTimeViews(SolarEvents.MOONSET)[0]);

        HashMap<String,TimeFieldRow> timeFields0 = customRows.getTimeFieldRows();
        for (String eventID : timeFields0.keySet())
        {
            TimeFieldRow row = timeFields0.get(eventID);
            if (row != null) {
                timeFields.put(eventID + "_" + ElevationEvent.SUFFIX_RISING, row.getField(0));
                timeFields.put(eventID + "_" + ElevationEvent.SUFFIX_SETTING, row.getField(1));
            }
        }

        TimeFieldRow primaryRow = getRow(AppSettings.loadEmphasizeFieldPref(view.getContext()));
        if (primaryRow != null) {
            primaryRow.setEmphasized(view.getContext(), true);
        }

        lightmap = (LightMapView) view.findViewById(R.id.info_time_lightmap);
        lightmap.setUseMainThread(true);

        lightmapLayout = view.findViewById(R.id.info_time_lightmap_layout);
        lightmapLayout.setClickable(true);

        btn_flipperNext = (ImageButton)view.findViewById(R.id.info_time_nextbtn);
        btn_flipperPrev = (ImageButton)view.findViewById(R.id.info_time_prevbtn);

        themeCardViews(view.getContext(), options);
    }

    public void bindDataToPosition(@NonNull Context context, int position, @Nullable Pair<SuntimesRiseSetDataset, SuntimesMoonData> data, CardAdapter.CardAdapterOptions options)
    {
        this.position = position;
        SuntimesRiseSetDataset sun = ((data == null) ? null : data.first);
        SuntimesMoonData moon = ((data == null) ? null : data.second);

        AppColorValues colors = AppColorValuesCollection.initSelectedColors(context);

        updateHeaderViews(context, data, options);
        row_actual.setVisible(options.showActual);
        row_civil.setVisible(options.showCivil);
        row_nautical.setVisible(options.showNautical);
        row_astro.setVisible(options.showAstro);
        row_solarnoon.setVisible(options.showNoon);
        row_midnight.setVisible(options.showMidnight);
        row_blue8.setVisible(options.showBlue);
        row_blue4.setVisible(options.showBlue);
        row_gold.setVisible(options.showGold);

        resetHighlight();
        if (options.highlightEventID != null && options.highlightPosition == position) {
            highlightField(options.highlightEventID);
        }

        // sun fields
        String notCalculated = context.getString(R.string.time_loading);
        if (sun != null && sun.isCalculated())
        {
            if (options.showActual) {
                TimeDisplayText sunriseString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunriseCalendarToday(), options.showSeconds);
                TimeDisplayText sunsetString_actualTime = utils.calendarTimeShortDisplayString(context, sun.dataActual.sunsetCalendarToday(), options.showSeconds);
                row_actual.updateFields(sunriseString_actualTime.toString(), sunsetString_actualTime.toString());
            }

            if (options.showCivil) {
                TimeDisplayText sunriseString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunriseCalendarToday(), options.showSeconds);
                TimeDisplayText sunsetString_civilTime = utils.calendarTimeShortDisplayString(context, sun.dataCivil.sunsetCalendarToday(), options.showSeconds);
                row_civil.updateFields(sunriseString_civilTime.toString(), sunsetString_civilTime.toString());
            }

            if (options.showNautical) {
                TimeDisplayText sunriseString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunriseCalendarToday(), options.showSeconds);
                TimeDisplayText sunsetString_nauticalTime = utils.calendarTimeShortDisplayString(context, sun.dataNautical.sunsetCalendarToday(), options.showSeconds);
                row_nautical.updateFields(sunriseString_nauticalTime.toString(), sunsetString_nauticalTime.toString());
            }

            if (options.showAstro) {
                TimeDisplayText sunriseString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunriseCalendarToday(), options.showSeconds);
                TimeDisplayText sunsetString_astroTime = utils.calendarTimeShortDisplayString(context, sun.dataAstro.sunsetCalendarToday(), options.showSeconds);
                row_astro.updateFields(sunriseString_astroTime.toString(), sunsetString_astroTime.toString());
            }

            if (options.showNoon)
            {
                Calendar noonTime = sun.dataNoon.sunriseCalendarToday();
                String noonString = utils.calendarTimeShortDisplayString(context, noonTime, options.showSeconds).toString();

                SuntimesCalculator calculator = sun.calculator();
                SpannableString positionSpan = new SpannableString("");
                SuntimesCalculator.SunPosition positionNoon = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);
                if (positionNoon != null) {
                    TimeDisplayText elevationText = angle_utils.formatAsElevation(positionNoon.elevation, 1);
                    String elevationString = angle_utils.formatAsElevation(elevationText.getValue(), elevationText.getSuffix());
                    positionSpan = SuntimesUtils.createRelativeSpan(null, elevationString, elevationText.getSuffix(), 0.7f);
                }

                row_solarnoon.updateFields(positionSpan, noonString);
            }
            if (options.showMidnight)
            {
                Calendar midnightTime = sun.dataMidnight.sunriseCalendarToday();
                String midnightString = utils.calendarTimeShortDisplayString(context, midnightTime, options.showSeconds).toString();
                row_midnight.updateFields(midnightString);
            }
            noonClickArea.setVisibility((options.showNoon || options.showMidnight) ? View.VISIBLE : View.GONE);

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

            HashMap<String, TimeFieldRow> rows = customRows.getTimeFieldRows();
            for (String eventID : rows.keySet())
            {
                TimeFieldRow row = rows.get(eventID);
                if (row != null)
                {
                    SuntimesRiseSetData rowData = sun.getData(eventID);
                    if (rowData != null)
                    {
                        String sunriseString = utils.calendarTimeShortDisplayString(context, rowData.sunriseCalendarToday(), options.showSeconds).toString();
                        String sunsetString = utils.calendarTimeShortDisplayString(context, rowData.sunsetCalendarToday(), options.showSeconds).toString();
                        row.updateFields(sunriseString, sunsetString);
                    } else {
                        row.updateFields(notCalculated, notCalculated);
                    }
                }
            }

            updateDayLengthViews(context, txt_daylength, sun.dataActual.dayLengthToday(), R.string.length_day, options.showSeconds, options.color_textTimeDelta);

            if (sun.dataActual.dayLengthToday() == SuntimesData.DAY_MILLIS
                    || sun.dataCivil.dayLengthToday() == SuntimesData.DAY_MILLIS
                    || sun.dataCivil.dayLengthToday() <= 0) {
                txt_lightlength.setText(LightMapView.getLabel(context, sun));
            } else {
                updateDayLengthViews(context, txt_lightlength, sun.dataCivil.dayLengthToday(), R.string.length_light, options.showSeconds, options.color_textTimeDelta);
            }

            if (txt_comparison != null) {
                txt_comparison.setVisibility(options.showComparison ? View.VISIBLE : View.GONE);
                txt_comparison.setText(options.showComparison ? comparisonDisplayString(context, sun.dataActual, options) : "");
            }

            // date field
            Date data_date = sun.dataActual.date();
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context.getApplicationContext());   // Apr 11, 2016
            dateFormat.setTimeZone(sun.timezone());

            int i = (position - CardAdapter.TODAY_POSITION);
            boolean showDateWarning = (options.dateMode != DateMode.CURRENT_DATE && (i > 1 || i < -1));
            ImageSpan dateWarningIcon = (options.showWarnings && showDateWarning) ? SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.statusIcon_size)) : null;

            Pair<String,String> label = getCardLabel(context, i, options);
            String dateString = context.getString(R.string.dateField, label.first, dateFormat.format(data_date));
            SpannableStringBuilder dateSpan = SuntimesUtils.createSpan(context, dateString, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
            txt_date.setText((label.second == null) ? dateSpan : SuntimesUtils.createColorSpan(SpannableString.valueOf(dateSpan), dateString, label.second, options.color_warning));
            txt_date.setContentDescription(dateString.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), ""));

        } else {
            for (TimeFieldRow row : customRows.getTimeFieldRows().values()) {
                row.updateFields(notCalculated, notCalculated);
            }
            row_solarnoon.updateFields(notCalculated, notCalculated);
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
            moonrise.setColors(context, colors);
            moonrise.updateViews(context, moon);

            moonphase.setColors(context, colors);
            moonphase.updateViews(context, moon);
        }

        // lightmap
        updateLightmapColors(context, colors);
        lightmapLayout.setVisibility(options.showLightmap ? View.VISIBLE : View.GONE);
        LightMapView.LightMapColors lightmapOptions = lightmap.getColors();

        SunSymbol sunSymbol = SunSymbol.valueOfOrNull(WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_GRAPH_SUNSYMBOL, MAPTAG_LIGHTMAP, PREF_DEF_GRAPH_SUNSYMBOL.name()));
        int symbol = SunSymbolBitmap.fromSunSymbol(sunSymbol);
        lightmapOptions.option_drawNow = (position == CardAdapter.TODAY_POSITION) ? symbol : SunSymbolBitmap.DRAW_SUN_CIRCLE_DASHED;
        lightmapOptions.option_drawNoon = WorldMapWidgetSettings.loadWorldMapPref(context, 0, LightMapDialog.PREF_KEY_GRAPH_SHOWNOON, LightMapView.LightMapColors.MAPTAG_LIGHTMAP, LightMapDialog.DEF_KEY_GRAPH_SHOWNOON);

        lightmapOptions.option_lmt = true;
        lightmap.setData(options.showLightmap ? sun : null);
        //Log.d("DEBUG", "bindDataToPosition: " + sun.dataActual.sunsetCalendarToday().get(Calendar.DAY_OF_YEAR));

        themeCardViews(context, colors);

        toggleNextPrevButtons(position);
    }

    protected void updateLightmapColors(Context context, ColorValues values)
    {
        if (values != null) {
            lightmap.getColors().values = new LightMapColorValues(values);
        } else if (lightmap.getColors().values == null) {
            lightmap.getColors().init(context);
        }
    }

    protected CharSequence comparisonDisplayString(Context context, SuntimesRiseSetData data, CardAdapter.CardAdapterOptions options)
    {
        TimeDisplayText deltaText = utils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther(), true);
        String deltaString = deltaText.getValue() + " " + deltaText.getUnits();
        String compareString = (data.dayLengthToday() == data.dayLengthOther())
                ? context.getString(data.dayDeltaPrefix()) + " " + deltaText.getSuffix()
                : context.getString(data.dayDeltaPrefix()) + " " + deltaString + deltaText.getSuffix();
        return SuntimesUtils.createBoldColorSpan(null, compareString, deltaString, options.color_textTimeDelta);
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
        ImageViewCompat.setImageTintList(btn_flipperNext, SuntimesUtils.colorStateList(options.color_accent, options.color_disabled, options.color_pressed));
        ImageViewCompat.setImageTintList(btn_flipperPrev, SuntimesUtils.colorStateList(options.color_accent, options.color_disabled, options.color_pressed));
    }

    protected void themeCardViews(Context context, @Nullable ColorValues colors)
    {
        if (colors == null) {
            return;
        }

        int color_sunrise = colors.getColor(CardColorValues.COLOR_RISING_SUN_TEXT);
        int color_sunset =  colors.getColor(CardColorValues.COLOR_SETTING_SUN_TEXT);

        for (CardViewHolder.TimeFieldRow row : rows) {
            if (row != null) {
                row.setTextColor(0, color_sunrise);
                row.setTextColor(1, color_sunset);
            }
        }

        row_blue4.setTextColor(0, color_sunset);
        row_blue4.setTextColor(1, color_sunrise);
        row_gold.setTextColor(0, color_sunset);
        row_gold.setTextColor(1, color_sunrise);
        row_solarnoon.setTextColor(0, color_sunset);

        int sunriseIconColor = colors.getColor(CardColorValues.COLOR_RISING_SUN);
        int sunriseIconColor2 = colors.getColor(CardColorValues.COLOR_RISING_SUN);
        int sunriseIconStrokeWidth = 0;
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunrise.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        header_sunrise.setTextColor(color_sunrise);

        int sunsetIconColor = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
        int sunsetIconColor2 = colors.getColor(CardColorValues.COLOR_SETTING_SUN);
        int sunsetIconStrokeWidth = 0;
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunset.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        header_sunset.setTextColor(color_sunset);
    }

    /*@Deprecated
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
        float titleSizeSp = theme.getTitleSizeSp();
        float textSizeSp = theme.getTextSizeSp();
        float timeSizeSp = theme.getTimeSizeSp();
        //boolean boldTime = theme.getTimeBold();
        boolean boldTitle = theme.getTitleBold();

        txt_daylength.setTextColor(color_text);
        txt_daylength.setTextSize(textSizeSp);

        txt_lightlength.setTextColor(color_text);
        txt_lightlength.setTextSize(textSizeSp);

        int labelColor = theme.getTitleColor();
        for (CardViewHolder.TimeFieldRow row : rows)
        {
            row.label.setTextColor(labelColor);
            row.label.setTextSize(titleSizeSp);
            row.label.setTypeface(row.label.getTypeface(), (boldTitle ? Typeface.BOLD : Typeface.NORMAL));

            row.setTextColor(0, color_sunrise);
            row.setTextColor(1, color_sunset);

            for (int i=0; i<2; i++) {
                if (row.getField(i) != null) {
                    row.setTextSize(i, timeSizeSp);
                    //row.getField(i).setTypeface(row.getField(i).getTypeface(), (timeBold ? Typeface.BOLD : Typeface.NORMAL));
                }
            }
        }

        row_blue4.setTextColor(0, color_sunset);
        row_blue4.setTextColor(1, color_sunrise);
        row_gold.setTextColor(0, color_sunset);
        row_gold.setTextColor(1, color_sunrise);
        row_solarnoon.setTextColor(0, color_sunset);

        txt_date.setTextColor(SuntimesUtils.colorStateList(labelColor, options.color_disabled, color_action));
        txt_date.setTextSize(titleSizeSp);
        txt_date.setTypeface(txt_date.getTypeface(), (boldTitle ? Typeface.BOLD : Typeface.NORMAL));

        int sunriseIconColor = theme.getSunriseIconColor();
        int sunriseIconColor2 = theme.getSunriseIconStrokeColor();
        int sunriseIconStrokeWidth = theme.getSunriseIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunrise.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        header_sunrise.setTextColor(color_sunrise);
        header_sunrise.setTextSize(titleSizeSp);
        header_sunrise.setTypeface(header_sunrise.getTypeface(), (boldTitle ? Typeface.BOLD : Typeface.NORMAL));

        int sunsetIconColor = theme.getSunsetIconColor();
        int sunsetIconColor2 = theme.getSunsetIconStrokeColor();
        int sunsetIconStrokeWidth = theme.getSunsetIconStrokePixels(context);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunset.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        header_sunset.setTextColor(color_sunset);
        header_sunset.setTextSize(titleSizeSp);
        header_sunset.setTypeface(header_sunset.getTypeface(), (boldTitle ? Typeface.BOLD : Typeface.NORMAL));

        moonrise.themeViews(context, theme);
        moonphase.themeViews(context, theme);
        moonlabel.setTextColor(labelColor);
        moonlabel.setTextSize(titleSizeSp);
        moonlabel.setTypeface(moonlabel.getTypeface(), (boldTitle ? Typeface.BOLD : Typeface.NORMAL));

        lightmap.themeViews(context, theme);
    }*/

    /**
     * @param context used to getStrings from resources
     * @param i position relative to TODAY
     * @return display string; today / tomorrow / yesterday / past (-n) / future (+n)
     */
    private Pair<String,String> getCardLabel(Context context, int i, CardAdapter.CardAdapterOptions options)
    {
        String dayOffset = ((i < 0) ? "-" : "+") + Math.abs(i);
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

    protected void updateHeaderViews(Context context, Pair<SuntimesRiseSetDataset, SuntimesMoonData> data, CardAdapter.CardAdapterOptions options)
    {
        int textVisibility = (options.showHeaderText != AppSettings.HEADER_TEXT_NONE ? View.VISIBLE : View.GONE);
        header_sunrise.setVisibility(textVisibility);
        header_sunset.setVisibility(textVisibility);

        int iconVisibility = (options.showHeaderIcon ? View.VISIBLE : View.GONE);
        icon_sunrise.setVisibility(iconVisibility);
        icon_sunset.setVisibility(iconVisibility);

        int resID_risingLabel = ((options.showHeaderText == AppSettings.HEADER_TEXT_LABEL_ALT) ? R.string.dawn : R.string.sunrise_short);
        int resID_settingLabel = ((options.showHeaderText == AppSettings.HEADER_TEXT_LABEL_ALT) ? R.string.dusk : R.string.sunset_short);
        boolean showPosition = (options.showHeaderText == AppSettings.HEADER_TEXT_AZIMUTH);
        SuntimesRiseSetDataset sun = ((data == null) ? null : data.first);
        if (showPosition && sun != null)
        {
            SuntimesCalculator calculator = sun.calculator();
            SuntimesRiseSetData d = sun.dataActual;    // TODO: configurable

            Calendar riseTime = (d != null ? d.sunriseCalendarToday() : null);
            SuntimesCalculator.SunPosition positionRising = (riseTime != null && calculator != null ? calculator.getSunPosition(riseTime) : null);
            if (positionRising != null) {
                styleAzimuthText(header_sunrise, positionRising.azimuth, null, 1);
            } else {
                header_sunrise.setText(context.getString(resID_risingLabel));    // R.string.sunrise_short
            }

            Calendar setTime = (d != null ? d.sunsetCalendarToday() : null);
            SuntimesCalculator.SunPosition positionSetting = (setTime != null && calculator != null ? calculator.getSunPosition(setTime) : null);
            if (positionSetting != null) {
                styleAzimuthText(header_sunset, positionSetting.azimuth, null, 1);
            } else {
                header_sunset.setText(context.getString(resID_settingLabel));    // R.string.sunset_short
            }

        } else {
            header_sunrise.setText(context.getString(resID_risingLabel));
            header_sunset.setText(context.getString(resID_settingLabel));
        }
    }

    public static void styleAzimuthText(TextView view, double azimuth, Integer color, int places)
    {
        TimeDisplayText azimuthText = angle_utils.formatAsDirection2(azimuth, places, false);
        String azimuthString = angle_utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
        SpannableString azimuthSpan = null;
        if (color != null) {
            //noinspection ConstantConditions
            azimuthSpan = SuntimesUtils.createColorSpan(azimuthSpan, azimuthString, azimuthString, color);
        }
        azimuthSpan = SuntimesUtils.createRelativeSpan(azimuthSpan, azimuthString, azimuthText.getSuffix(), 0.7f);
        azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
        view.setText(azimuthSpan);

        TimeDisplayText azimuthDesc = angle_utils.formatAsDirection2(azimuth, places, true);
        view.setContentDescription(angle_utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
    }

    private void updateDayLengthViews(Context context, TextView textView, long dayLength, int labelID, boolean showSeconds, int highlightColor)
    {
        TimeDisplayText dayLengthDisplay;
        if (dayLength <= 0)
            dayLengthDisplay = new TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 0, (showSeconds ? SuntimesUtils.strSeconds : SuntimesUtils.strMinutes)), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else if (dayLength >= SuntimesData.DAY_MILLIS)
            dayLengthDisplay = new TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 24, SuntimesUtils.strHours), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else dayLengthDisplay = utils.timeDeltaLongDisplayString(0, dayLength, showSeconds);

        dayLengthDisplay.setSuffix("");
        String dayLengthStr = dayLengthDisplay.getValue();
        String dayLength_label = context.getString(labelID, dayLengthStr);
        textView.setText(SuntimesUtils.createBoldColorSpan(null, dayLength_label, dayLengthStr, highlightColor));
    }

    public void highlightField( String highlightEventID )
    {
        for (String eventID : timeFields.keySet()) {
            if (eventID.equals(highlightEventID)) {
                TimeFieldRow.highlight(timeFields.get(eventID));
                break;
            }
        }
    }

    public void resetHighlight()
    {
        for (TimeFieldRow row : rows) {
            if (row != null) {
                row.resetHighlight();
            }
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

    @Nullable
    public TimeFieldRow getRow(String name)
    {
        TimeMode mode;
        try {
            mode = TimeMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            Log.w(getClass().getSimpleName(), "getRow: unrecognized:" + e);
            return null;
        }
        return getRow(mode);
    }

    @Nullable
    public TimeFieldRow getRow(TimeMode mode)
    {
        switch (mode)
        {
            case GOLD: return row_gold;
            case NOON: return row_solarnoon;
            case OFFICIAL: return row_actual;
            case CIVIL: return row_civil;
            case NAUTICAL: return row_nautical;
            case ASTRONOMICAL: return row_astro;
            case BLUE4: return row_blue4;
            case BLUE8: return row_blue8;
            case MIDNIGHT: return null;  // TODO
            default: return null;
        }
    }

    /**
     * startUpdateTask
     */
    public void startUpdateTask()
    {
        //Log.d("DEBUG", "startUpdateTask: " + this);
        if (lightmap != null) {
            lightmap.removeCallbacks(updateTask);
            updateTask_isRunning = true;
            lightmap.post(updateTask);
        }
    }

    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (lightmap != null && updateTask_isRunning)
            {
                lightmap.getColors().now = Calendar.getInstance().getTimeInMillis();
                //Log.d("DEBUG", "updating lightmap id-" + getAdapterPosition() + " @ " + lightmap.getNow() + "\t :: view-" + Integer.toHexString(lightmap.getColors().hashCode()));
                lightmap.updateViews(true);
                lightmap.postDelayed(this, UPDATE_RATE);
            }
        }
    };
    private boolean updateTask_isRunning = false;
    public static final int UPDATE_RATE = 30000;

    /**
     * stopUpdateTask
     */
    public void stopUpdateTask()
    {
        //Log.d("DEBUG", "stopUpdateTask: " + this);
        if (lightmap != null) {
            updateTask_isRunning = false;
            lightmap.removeCallbacks(updateTask);
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
                    this.fields[i] = ((fieldIDs[i] != 0) ? (TextView) parent.findViewById(fieldIDs[i]) : null);
                }
            }
        }

        public TextView getLabel()
        {
            return label;
        }

        @Nullable
        public TextView getField( int i )
        {
            if (i >= 0 && i < fields.length)
                return fields[i];
            else return null;
        }

        public void setTextColor(int i, int color)
        {
            TextView v = getField(i);
            if (v != null) {
                v.setTextColor(color);
            }
        }

        public void setTextSize(int i, float size)
        {
            TextView v = getField(i);
            if (v != null) {
                v.setTextSize(size);
            }
        }

        private boolean isEmphasized = false;
        public boolean isEmphasized() {
            return isEmphasized;
        }
        public void setEmphasized(Context context, boolean value)
        {
            isEmphasized = value;
            float textSizePx = label.getTextSize() + (isEmphasized ? context.getResources().getDimension(R.dimen.table_row_fontsize_emphasized) : 0);
            for (TextView field : fields) {
                if (field != null) {
                    field.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                }
            }
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

        public void updateFields( CharSequence ...values )
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

    /**
     * CustomRows
     */
    public static class CustomRows
    {
        public static int[] resID_labels = new int[] {R.id.bucket1_labels, R.id.bucket2_labels, R.id.bucket3_labels, R.id.bucket4_labels, R.id.bucket5_labels, R.id.bucket6_labels, R.id.bucket7_labels, R.id.bucket8_labels};
        public static int[] resID_rising = new int[] {R.id.bucket1_rising, R.id.bucket2_rising, R.id.bucket3_rising, R.id.bucket4_rising, R.id.bucket5_rising, R.id.bucket6_rising, R.id.bucket7_rising, R.id.bucket8_rising};
        public static int[] resID_setting = new int[] {R.id.bucket1_setting, R.id.bucket2_setting, R.id.bucket3_setting, R.id.bucket4_setting, R.id.bucket5_setting, R.id.bucket6_setting, R.id.bucket7_setting, R.id.bucket8_setting};

        public LinearLayout[] layout_labels = new LinearLayout[resID_labels.length];
        public LinearLayout[] layout_rising = new LinearLayout[layout_labels.length];
        public LinearLayout[] layout_setting = new LinearLayout[layout_labels.length];
        public ArrayList<ArrayList<Double>> angleList = new ArrayList<ArrayList<Double>>();

        public CustomRows(View view, CardAdapter.CardAdapterOptions options)
        {
            for (int i = 0; i< layout_labels.length; i++) {
                layout_labels[i] = (LinearLayout) view.findViewById(resID_labels[i]);
                layout_rising[i] = (LinearLayout) view.findViewById(resID_rising[i]);
                layout_setting[i] = (LinearLayout) view.findViewById(resID_setting[i]);
                angleList.add(new ArrayList<Double>());
            }
            hideAll();
        }

        public Collection<TimeFieldRow> initRows(Context context, Set<String> events)
        {
            clearAll();
            EventSettingsInterface contextInterface = AndroidEventSettings.wrap(context);
            for (String eventID : events) {
                TimeFieldRow row = addRow(context, EventSettings.loadEvent(contextInterface, eventID));
                rows.put(eventID, row);
            }
            adjustBottomMargin();
            return rows.values();
        }

        protected HashMap<String, TimeFieldRow> rows = new HashMap<>();
        public HashMap<String, TimeFieldRow> getTimeFieldRows() {
            return rows;
        }

        @SuppressLint("ResourceType")
        public TimeFieldRow addRow(Context context, EventAlias event)
        {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.table_risingColor, R.attr.table_settingColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int color_label = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.grey_50));
            int color_rising = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.sunIcon_color_rising_dark));
            int color_setting = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.sunIcon_color_setting_dark));
            typedArray.recycle();

            if (event.getType() == EventType.SHADOWLENGTH || event.getType() == EventType.SUN_ELEVATION || event.getType() == EventType.DAYPERCENT)
            {
                double angle = 0;
                switch (event.getType())
                {
                    case DAYPERCENT:
                        DayPercentEvent event2 = DayPercentEvent.valueOf(Uri.parse(event.getUri()).getLastPathSegment());
                        angle = (event2 != null ? event2.getAngle() : 0);
                        break;

                    case SHADOWLENGTH:
                        ShadowLengthEvent event1 = ShadowLengthEvent.valueOf(Uri.parse(event.getUri()).getLastPathSegment());
                        angle = (event1 != null ? event1.getAngle() : 0);
                        break;

                    case SUN_ELEVATION:
                        SunElevationEvent event0 = SunElevationEvent.valueOf(Uri.parse(event.getUri()).getLastPathSegment());
                        angle = (event0 != null ? event0.getAngle() : 0);
                        break;
                }

                int i = getLayoutForAngle(angle);
                ArrayList<Double> angles = angleList.get(i);
                int j = getPositionForAngle(angles, angle);
                angles.add(j, angle);

                int margin = (int)context.getResources().getDimension(R.dimen.table_cell_spacing);

                TextView text_label = initTextView(context, initLayoutParams(0, 0, 0, margin));
                text_label.setText(event.getLabel());  // + " " + j + " " + angle);
                text_label.setTextColor(color_label);
                layout_labels[i].addView(text_label, j);

                TextView text_rising = initTextView(context, initLayoutParams(0, 0, 0, margin));
                text_rising.setTextColor(angle > 0 ? color_setting : color_rising);
                layout_rising[i].addView(text_rising, j);

                TextView text_setting = initTextView(context, initLayoutParams(0, 0, 0, margin));
                text_setting.setTextColor(angle > 0 ? color_rising : color_setting);
                layout_setting[i].addView(text_setting, j);

                setVisibility(i, true);
                return new TimeFieldRow(text_label, text_rising, text_setting);

            } else {
                return null;
            }
        }

        protected void adjustBottomMargin()
        {
            for (LinearLayout layout : layout_labels) {
                adjustBottomMargin(layout);
            }
            for (LinearLayout layout : layout_rising) {
                adjustBottomMargin(layout);
            }
            for (LinearLayout layout : layout_setting) {
                adjustBottomMargin(layout);
            }
        }

        protected void adjustBottomMargin(LinearLayout layout)
        {
            if (layout != null) {
                View v = layout.getChildAt(layout.getChildCount()-1);
                if (v != null) {
                    v.setLayoutParams(initLayoutParams(0, 0, 0, 0));
                }
            }
        }

        protected LinearLayout.LayoutParams initLayoutParams(int marginLeft, int marginTop, int marginRight, int marginBottom)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            params.gravity = Gravity.CENTER_VERTICAL;
            return params;
        }

        protected TextView initTextView(Context context, LinearLayout.LayoutParams layoutParams)
        {
            int[] attr = { R.attr.text_size_small };
            TypedArray typedArray = context.obtainStyledAttributes(attr);
            float textSizePx = context.getResources().getDimension(typedArray.getResourceId(0, R.dimen.tablerow_label_fontsize));
            typedArray.recycle();

            TextView view = new TextView(context, null, R.style.SunsetTimeTextView);
            view.setTextAppearance(context, android.R.style.TextAppearance_Small);
            view.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
            view.getPaint().setAntiAlias(true);
            view.setLayoutParams(layoutParams);
            view.setPadding(0, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= 21) {
                view.setLetterSpacing(0.01f);
            }
            view.setText(context.getString(R.string.time_none));
            return view;
        }

        public int getPositionForAngle(ArrayList<Double> angles, double angle) {
            for (int j = 0; j < angles.size(); j++) {
                if (angle <= angles.get(j)) {
                    return j;
                }
            }
            return angles.size();
        }

        public int getLayoutForAngle(double angle) {
            if (angle >= 6) {
                return 7;
            } else if (angle >= 0) {
                return 6;
            } else if (angle >= -4) {
                return 5;
            } else if (angle >= -6) {
                return 4;
            } else if (angle >= -8) {
                return 3;
            } else if (angle >= -12) {
                return 2;
            } else if (angle >= -18) {
                return 1;
            } else return 0;
        }

        public void setVisibility(int i, boolean visible)
        {
            if (i>=0 && i< layout_labels.length) {
                int visibility = (visible ? View.VISIBLE : View.GONE);
                layout_labels[i].setVisibility(visibility);
                layout_rising[i].setVisibility(visibility);
                layout_setting[i].setVisibility(visibility);
            }
        }

        protected void clearRow(int i)
        {
            if (i>=0 && i< layout_labels.length) {
                layout_labels[i].removeAllViews();
                layout_rising[i].removeAllViews();
                layout_setting[i].removeAllViews();
                angleList.get(i).clear();
            }
        }

        public void hideAll() {
            for (int i = 0; i< layout_labels.length; i++) {
                setVisibility(i, false);
            }
        }

        public void clearAll() {
            for (int i = 0; i< layout_labels.length; i++) {
                clearRow(i);
            }
            rows.clear();
        }
    }

}