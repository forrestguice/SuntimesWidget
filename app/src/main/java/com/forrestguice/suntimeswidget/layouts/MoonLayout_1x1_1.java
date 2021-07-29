/**
   Copyright (C) 2018-2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.NumberFormat;

/**
 * Moon Phase + Illumination (1x1)
 */
public class MoonLayout_1x1_1 extends MoonLayout
{
    public MoonLayout_1x1_1()
    {
        super();
    }

    /**public MoonLayout_1x1_1(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_1;
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
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / (showLabels ? 2 : 1)};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "0000", timeSizeSp, ClockLayout.CLOCKFACE_MAX_SP, "", suffixSizeSp, iconSizeDp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                    views.setViewPadding(R.id.text_info_moonphase, (int)scaledPadding/2, 0, (int)scaledPadding, 0);
                    views.setViewPadding(R.id.text_info_moonillum, (int)scaledPadding/2, 0, (int)scaledPadding, (int)(scaledPadding)/2);

                    /*views.setViewPadding(R.id.icon_info_moonphase_full, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_new, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waxing_crescent, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waxing_quarter, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waxing_gibbous, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waning_crescent, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waning_quarter, (int)(scaledPadding), 0, 0, 0);
                    views.setViewPadding(R.id.icon_info_moonphase_waning_gibbous, (int)(scaledPadding), 0, 0, 0);*/

                    views.setTextViewTextSize(R.id.text_info_moonillum, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
                    views.setTextViewTextSize(R.id.text_info_moonphase, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);

                    // TODO: scale icons
                }
            }
        }

        NumberFormat percentage = NumberFormat.getPercentInstance();
        String illum = percentage.format(data.getMoonIlluminationToday());
        String illumNote = context.getString(R.string.moon_illumination_short, illum);
        SpannableString illumNoteSpan = (boldTime ? SuntimesUtils.createBoldColorSpan(null, illumNote, illum, illumColor) : SuntimesUtils.createColorSpan(null, illumNote, illum, illumColor));
        views.setTextViewText(R.id.text_info_moonillum, illumNoteSpan);

        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values()) {
            views.setViewVisibility(moonPhase.getView(), View.GONE);
        }

        MoonPhaseDisplay phase = data.getMoonPhaseToday();
        if (phase != null)
        {
            if (phase == MoonPhaseDisplay.FULL || phase == MoonPhaseDisplay.NEW) {
                SuntimesCalculator.MoonPhase majorPhase = (phase == MoonPhaseDisplay.FULL ? SuntimesCalculator.MoonPhase.FULL : SuntimesCalculator.MoonPhase.NEW);
                views.setTextViewText(R.id.text_info_moonphase, data.getMoonPhaseLabel(context, majorPhase));
            } else views.setTextViewText(R.id.text_info_moonphase, phase.getLongDisplayString());

            views.setViewVisibility(R.id.text_info_moonphase, (showLabels ? View.VISIBLE : View.GONE));
            views.setViewVisibility(phase.getView(), View.VISIBLE);

            Integer phaseColor = phaseColors.get(phase);
            if (phaseColor != null) {
                views.setTextColor(R.id.text_info_moonphase, phaseColor);
            }
        }
    }

    protected int illumColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        illumColor = theme.getTimeColor();

        themeViewsMoonPhase(context, views, theme);
        themeViewsMoonPhaseText(context, views, theme);
        themeViewsMoonPhaseIcons(context, views, theme);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        // EMPTY
    }
}
