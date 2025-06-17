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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.Log;
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
 * Moon Position Widget (dec, right asc)
 */
public class MoonLayout_1x1_6 extends MoonLayout
{
    public MoonLayout_1x1_6()
    {
        super();
    }

    /**public MoonLayout_1x1_6(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_6;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position);  // (scaleBase ? R.layout.layout_widget_moon_1x1_6_align_fill : R.layout.layout_widget_moon_1x1_6);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_moon_1x1_6_align_fill;
            case 1: return R.layout.layout_widget_moon_1x1_6_align_float_1;
            case 2: return R.layout.layout_widget_moon_1x1_6_align_float_2;
            case 3: return R.layout.layout_widget_moon_1x1_6_align_float_3;
            case 4: return R.layout.layout_widget_moon_1x1_6_align_float_4;
            case 6: return R.layout.layout_widget_moon_1x1_6_align_float_6;
            case 7: return R.layout.layout_widget_moon_1x1_6_align_float_7;
            case 8: return R.layout.layout_widget_moon_1x1_6_align_float_8;
            case 9: return R.layout.layout_widget_moon_1x1_6_align_float_9;
            case 5: default: return R.layout.layout_widget_moon_1x1_6;
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / (showLabels ? 4 : 2))};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "0000000000", timeSizeSp, SuntimesLayout.MAX_SP, "", suffixSizeSp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_moon_declination_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_moon_declination_current, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_moon_declination_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_moon_declination_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setTextViewTextSize(R.id.info_moon_rightascension_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setViewPadding(R.id.info_moon_rightascension_current, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding / 2));

                    views.setTextViewTextSize(R.id.info_moon_rightascension_current_label, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setViewPadding(R.id.info_moon_rightascension_current_label, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                }
            }
        }

        SuntimesCalculator calculator = data.calculator();
        SuntimesCalculator.MoonPosition moonPosition = calculator.getMoonPosition(data.now());
        updateViewsRightAscDeclinationText(context, views, moonPosition);

        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_moon_rightascension_current_label, visibility);
        views.setViewVisibility(R.id.info_moon_declination_current_label, visibility);
    }

    protected int suffixColor = Color.GRAY;
    protected int highlightColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        highlightColor = theme.getTimeColor();
        suffixColor = theme.getTimeSuffixColor();
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_moon_rightascension_current_label, textColor);
        views.setTextColor(R.id.info_moon_declination_current_label, textColor);
        views.setTextColor(R.id.info_moon_rightascension_current, textColor);
        views.setTextColor(R.id.info_moon_declination_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            views.setTextViewTextSize(R.id.info_moon_rightascension_current_label, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.info_moon_declination_current_label, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);

            views.setTextViewTextSize(R.id.info_moon_rightascension_current, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.info_moon_declination_current, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
        }
    }

    public static SpannableString styleRightAscText(SuntimesCalculator.MoonPosition moonPosition, boolean boldTime, int highlightColor, int suffixColor)
    {
        SuntimesUtils.TimeDisplayText rightAscDisplay = utils.formatAsRightAscension(moonPosition.rightAscension, PositionLayout.DECIMAL_PLACES);
        String rightAscSymbol = rightAscDisplay.getSuffix();
        String rightAscString = utils.formatAsRightAscension(rightAscDisplay.getValue(), rightAscSymbol);
        SpannableString rightAsc = SuntimesUtils.createColorSpan(null, rightAscString, rightAscString, highlightColor, boldTime);
        rightAsc = SuntimesUtils.createBoldColorSpan(rightAsc, rightAscString, rightAscSymbol, suffixColor);
        rightAsc = SuntimesUtils.createRelativeSpan(rightAsc, rightAscString, rightAscSymbol, PositionLayout.SYMBOL_RELATIVE_SIZE);
        return rightAsc;
    }

    public static SpannableString styleDeclinationText(SuntimesCalculator.MoonPosition moonPosition, boolean boldTime, int highlightColor, int suffixColor)
    {
        SuntimesUtils.TimeDisplayText declinationDisplay = utils.formatAsDeclination(moonPosition.declination, PositionLayout.DECIMAL_PLACES);
        String declinationSymbol = declinationDisplay.getSuffix();
        String declinationString = utils.formatAsDeclination(declinationDisplay.getValue(), declinationSymbol);
        SpannableString declination = SuntimesUtils.createColorSpan(null, declinationString, declinationString, highlightColor, boldTime);
        declination = SuntimesUtils.createBoldColorSpan(declination, declinationString, declinationSymbol, suffixColor);
        declination = SuntimesUtils.createRelativeSpan(declination, declinationString, declinationSymbol, PositionLayout.SYMBOL_RELATIVE_SIZE);
        return declination;
    }

    protected void updateViewsRightAscDeclinationText(Context context, RemoteViews views, SuntimesCalculator.MoonPosition moonPosition)
    {
        views.setTextViewText(R.id.info_moon_rightascension_current, styleRightAscText(moonPosition, boldTime, highlightColor, suffixColor));
        views.setTextViewText(R.id.info_moon_declination_current, styleDeclinationText(moonPosition, boldTime, highlightColor, suffixColor));
    }

    @Override
    public boolean saveNextSuggestedUpdate(Context context, int appWidgetId)
    {
        long updateInterval = (5 * 60 * 1000);                 // update every 5 min
        long nextUpdate = Calendar.getInstance().getTimeInMillis() + updateInterval;
        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, nextUpdate);
        Log.d("MoonLayout", "saveNextSuggestedUpdate: " + utils.calendarDateTimeDisplayString(context, nextUpdate).toString());
        return true;
    }
}
