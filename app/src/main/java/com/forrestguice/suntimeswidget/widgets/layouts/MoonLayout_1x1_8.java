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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * Moon Apogee / Perigee Widget (next apogee / perigee)
 */
public class MoonLayout_1x1_8 extends MoonLayout
{
    public MoonLayout_1x1_8()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_8;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);  // (scaleBase ? R.layout.layout_widget_moon_1x1_8_align_fill : R.layout.layout_widget_moon_1x1_8);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_moon_1x1_8_align_fill;
            case 1: return R.layout.layout_widget_moon_1x1_8_align_float_1;
            case 2: return R.layout.layout_widget_moon_1x1_8_align_float_2;
            case 3: return R.layout.layout_widget_moon_1x1_8_align_float_3;
            case 4: return R.layout.layout_widget_moon_1x1_8_align_float_4;
            case 6: return R.layout.layout_widget_moon_1x1_8_align_float_6;
            case 7: return R.layout.layout_widget_moon_1x1_8_align_float_7;
            case 8: return R.layout.layout_widget_moon_1x1_8_align_float_8;
            case 9: return R.layout.layout_widget_moon_1x1_8_align_float_9;
            case 5: default: return R.layout.layout_widget_moon_1x1_8;
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int numRows = 4;
                if (showLabels) {
                    numRows++;
                }
                int[] maxDp = new int[] {(maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2])),
                        ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3])) / numRows)};
                float maxSp = SuntimesLayout.MAX_SP;
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, (showSeconds ? "September MM, 00:00:00 MM" : "September MM, 00:00 MM"), timeSizeSp, maxSp, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setTextViewTextSize(R.id.moonapsis_apogee_date, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.moonapsis_apogee_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setTextViewTextSize(R.id.moonapsis_apogee_distance, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setTextViewTextSize(R.id.moonapsis_apogee_note, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);

                    views.setTextViewTextSize(R.id.moonapsis_perigee_date, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.moonapsis_perigee_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setTextViewTextSize(R.id.moonapsis_perigee_distance, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setTextViewTextSize(R.id.moonapsis_perigee_note, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                    views.setViewPadding(R.id.moonapsis_apogee_layout, 0, 0, 0, (int)scaledPadding/2);
                    views.setViewPadding(R.id.moonapsis_perigee_layout, 0, 0, 0, (int)scaledPadding/2);
                }
            }
        }

        if (data != null && data.isCalculated())
        {
            Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = data.getMoonApogee();
            Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = data.getMoonPerigee();
            updateApogeePerigee(context, appWidgetId, views, data.now(), apogee, perigee);
            hideDistant(views, apogee, perigee);

        } else {
            views.setViewVisibility(R.id.moonapsis_apogee_layout, View.GONE);
            views.setViewVisibility(R.id.moonapsis_perigee_layout, View.GONE);
        }
    }

    protected void hideDistant(RemoteViews views,
                               Pair<Calendar, SuntimesCalculator.MoonPosition> apogee, Pair<Calendar, SuntimesCalculator.MoonPosition> perigee)
    {
        if (apogee != null && apogee.first != null && perigee != null)
        {
            if (apogee.first.before(perigee.first)) {
                views.setViewVisibility(R.id.moonapsis_apogee_layout, View.VISIBLE);
                views.setViewVisibility(R.id.moonapsis_perigee_layout, View.GONE);
            } else {
                views.setViewVisibility(R.id.moonapsis_apogee_layout, View.GONE);
                views.setViewVisibility(R.id.moonapsis_perigee_layout, View.VISIBLE);
            }
        } else {
            views.setViewVisibility(R.id.moonapsis_apogee_layout, View.GONE);
            views.setViewVisibility(R.id.moonapsis_perigee_layout, View.VISIBLE);
        }
    }

    protected void updateApogeePerigee(Context context, int appWidgetId, RemoteViews views, Calendar now,
                                       Pair<Calendar, SuntimesCalculator.MoonPosition> apogee, Pair<Calendar, SuntimesCalculator.MoonPosition> perigee)
    {
        boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        boolean showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        boolean abbreviate = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);

        if (apogee != null)
        {
            SuntimesUtils.TimeDisplayText apogeeString = utils.calendarDateTimeDisplayString(context, apogee.first, showTimeDate, showSeconds, abbreviate);
            views.setTextViewText(R.id.moonapsis_apogee_date, apogeeString.getValue());
            views.setTextViewText(R.id.moonapsis_apogee_note, noteSpan(context, now, apogee.first, showWeeks, showHours, timeColor, boldTime));
            if (apogee.second != null) {
                views.setTextViewText(R.id.moonapsis_apogee_distance, distanceSpan(context, apogee.second.distance, units, settingColor, suffixColor, boldTime));
            }
        } else {
            views.setViewVisibility(R.id.moonapsis_apogee_layout, View.GONE);
        }

        if (perigee != null)
        {
            SuntimesUtils.TimeDisplayText perigeeString = utils.calendarDateTimeDisplayString(context, perigee.first, showTimeDate, showSeconds, abbreviate);
            views.setTextViewText(R.id.moonapsis_perigee_date, perigeeString.getValue());
            views.setTextViewText(R.id.moonapsis_perigee_note, noteSpan(context, now, perigee.first, showWeeks, showHours, timeColor, boldTime));
            if (perigee.second != null) {
                views.setTextViewText(R.id.moonapsis_perigee_distance, distanceSpan(context, perigee.second.distance, units, risingColor, suffixColor, boldTime));
            }
        } else {
            views.setViewVisibility(R.id.moonapsis_perigee_layout, View.GONE);
        }

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.moonapsis_apogee_label, visibility);
        views.setViewVisibility(R.id.moonapsis_perigee_label, visibility);
    }

    public static SpannableString distanceSpan(Context context, double distance, LengthUnit units, int color, int suffixColor, boolean boldTime)
    {
        SuntimesUtils.TimeDisplayText distanceDisplay = SuntimesUtils.formatAsDistance(context, distance, units, PositionLayout.DECIMAL_PLACES, true);
        String unitsSymbol = distanceDisplay.getUnits();
        String distanceString = SuntimesUtils.formatAsDistance(context, distanceDisplay);
        SpannableString distanceSpan = SuntimesUtils.createColorSpan(null, distanceString, distanceString, color, boldTime);
        distanceSpan = SuntimesUtils.createBoldColorSpan(distanceSpan, distanceString, unitsSymbol, suffixColor);
        distanceSpan = SuntimesUtils.createRelativeSpan(distanceSpan, distanceString, unitsSymbol, PositionLayout.SYMBOL_RELATIVE_SIZE);
        return distanceSpan;
    }

    public static SpannableString noteSpan(Context context, @NonNull Calendar now, @NonNull Calendar event, boolean showWeeks, boolean showHours, int timeColor, boolean boldTime)
    {
        String noteTime = utils.timeDeltaDisplayString(now.getTime(), event.getTime(), showWeeks, showHours).toString();
        String noteString = context.getString((event.before(now) ? R.string.ago : R.string.hence), noteTime);
        return (boldTime ? SuntimesUtils.createBoldColorSpan(null, noteString, noteTime, timeColor) : SuntimesUtils.createColorSpan(null, noteString, noteTime, timeColor));
    }

    protected int suffixColor = Color.GRAY;
    protected int timeColor = Color.WHITE;
    protected int risingColor = Color.WHITE;
    protected int settingColor = Color.GRAY;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        timeColor = theme.getTimeColor();
        suffixColor = theme.getTimeSuffixColor();
        int textColor = theme.getTextColor();
        risingColor = theme.getMoonriseTextColor();
        settingColor = theme.getMoonsetTextColor();

        views.setTextColor(R.id.moonapsis_apogee_label, textColor);
        views.setTextColor(R.id.moonapsis_apogee_date, timeColor);
        views.setTextColor(R.id.moonapsis_apogee_note, textColor);
        views.setTextColor(R.id.moonapsis_apogee_distance, textColor);

        views.setTextColor(R.id.moonapsis_perigee_label, textColor);
        views.setTextColor(R.id.moonapsis_perigee_date, timeColor);
        views.setTextColor(R.id.moonapsis_perigee_note, textColor);
        views.setTextColor(R.id.moonapsis_perigee_distance, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.moonapsis_apogee_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonapsis_apogee_note, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonapsis_apogee_distance, TypedValue.COMPLEX_UNIT_DIP, textSize);

            views.setTextViewTextSize(R.id.moonapsis_perigee_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonapsis_perigee_note, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonapsis_perigee_distance, TypedValue.COMPLEX_UNIT_DIP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.moonapsis_apogee_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.moonapsis_perigee_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

    @Override
    public boolean saveNextSuggestedUpdate(Context context, int appWidgetId)
    {
        long updateInterval = (5 * 60 * 1000);                 // update every 5 min  // TODO
        long nextUpdate = Calendar.getInstance().getTimeInMillis() + updateInterval;
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate);
        Log.d("MoonLayout", "saveNextSuggestedUpdate: " + utils.calendarDateTimeDisplayString(context, nextUpdate).toString());
        return true;
    }
}
