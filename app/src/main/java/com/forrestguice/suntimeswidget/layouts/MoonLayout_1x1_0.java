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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * Moonrise / Moonset (1x1)
 */
public class MoonLayout_1x1_0 extends MoonLayout
{
    protected int moonriseColor = Color.WHITE;
    protected int moonsetColor = Color.GRAY;

    public MoonLayout_1x1_0()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);
        order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(position, data);
        //this.layoutID = (scaleBase
        //        ? chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_fill, R.layout.layout_widget_moon_1x1_01_align_fill, data, order)
        //        : chooseMoonLayout(R.layout.layout_widget_moon_1x1_0, R.layout.layout_widget_moon_1x1_01, data, order));
    }

    protected int chooseLayout(int position, SuntimesMoonData data)
    {
        switch (position) {
            case 0: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_fill, R.layout.layout_widget_moon_1x1_01_align_fill, data, order);
            case 1: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_1, R.layout.layout_widget_moon_1x1_01_align_float_1, data, order);
            case 2: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_2, R.layout.layout_widget_moon_1x1_01_align_float_2, data, order);
            case 3: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_3, R.layout.layout_widget_moon_1x1_01_align_float_3, data, order);
            case 4: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_4, R.layout.layout_widget_moon_1x1_01_align_float_4, data, order);
            case 6: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_6, R.layout.layout_widget_moon_1x1_01_align_float_6, data, order);
            case 7: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_7, R.layout.layout_widget_moon_1x1_01_align_float_7, data, order);
            case 8: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_8, R.layout.layout_widget_moon_1x1_01_align_float_8, data, order);
            case 9: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0_align_float_9, R.layout.layout_widget_moon_1x1_01_align_float_9, data, order);
            case 5: default: return chooseMoonLayout(R.layout.layout_widget_moon_1x1_0, R.layout.layout_widget_moon_1x1_01, data, order);
        }
    }

    private WidgetSettings.RiseSetOrder order = WidgetSettings.RiseSetOrder.TODAY;

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (2*(paddingDp[0] + paddingDp[2])), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / 2)};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, (showSeconds ? "00:00:00" : "00:00"), timeSizeSp, SuntimesLayout.MAX_SP, "MM", suffixSizeSp, iconSizeDp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setTextViewTextSize(R.id.text_time_moonrise, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_moonrise_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

                    views.setTextViewTextSize(R.id.text_time_moonset, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_moonset_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setViewPadding(R.id.text_time_moonset, 0, 0, (int)scaledPadding/2, 0);
                    views.setViewPadding(R.id.text_time_moonset_suffix, 0, 0, (int)scaledPadding, 0);
                    views.setViewPadding(R.id.icon_time_moonset, (int)(scaledPadding), 0, (int)scaledPadding/2, 0);

                    views.setViewPadding(R.id.text_time_moonrise, 0, 0, (int)scaledPadding/2, 0);
                    views.setViewPadding(R.id.text_time_moonrise_suffix, 0, 0, (int)scaledPadding, 0);
                    views.setViewPadding(R.id.icon_time_moonrise, (int)(scaledPadding), 0, (int)scaledPadding/2, 0);

                    Drawable moonriseIcon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunrise1, null), moonriseColor);
                    views.setImageViewBitmap(R.id.icon_time_moonrise, SuntimesUtils.drawableToBitmap(context, moonriseIcon, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));

                    Drawable moonsetIcon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunset1, null), moonsetColor);
                    views.setImageViewBitmap(R.id.icon_time_moonset, SuntimesUtils.drawableToBitmap(context, moonsetIcon, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));
                }
            }
        }

        updateViewsMoonRiseSetText(context, views, data, showSeconds, order, timeFormat);
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        iconSizeDp = 18;   // override 32
        moonriseColor = theme.getMoonriseTextColor();
        moonsetColor = theme.getMoonsetTextColor();
        themeViewsMoonRiseSetText(context, views, theme);
        themeViewsMoonRiseSetIcons(context, views, theme);
    }

}
