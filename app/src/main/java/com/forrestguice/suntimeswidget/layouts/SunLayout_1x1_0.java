/**
   Copyright (C) 2014-2021 Forrest Guice
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
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays both the sunrise and sunset time.
 */
public class SunLayout_1x1_0 extends SunLayout
{
    protected int sunriseIconColor = Color.YELLOW;
    protected int sunriseIconStrokeColor = Color.YELLOW;
    protected int sunriseIconStrokePixels = 0;

    protected int sunsetIconColor = Color.YELLOW;
    protected int sunsetIconStrokeColor = Color.YELLOW;
    protected int sunsetIconStrokePixels = 0;

    public SunLayout_1x1_0()
    {
        super();
    }

    public SunLayout_1x1_0(int layoutID )
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1_0;
    }

    private WidgetSettings.RiseSetOrder order = WidgetSettings.RiseSetOrder.TODAY;

    @Override
    public void prepareForUpdate(Context context, int appWidgetID, SuntimesRiseSetData data)
    {
        super.prepareForUpdate(context, appWidgetID, data);
        order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetID);

        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetID);
        this.layoutID = chooseLayout(position, data);
        //this.layoutID = (scaleBase
        //        ? chooseSunLayout(R.layout.layout_widget_1x1_0_align_fill, R.layout.layout_widget_1x1_01_align_fill, data, order)
        //        : chooseSunLayout(R.layout.layout_widget_1x1_0, R.layout.layout_widget_1x1_01, data, order));
    }

    protected int chooseLayout(int position, SuntimesRiseSetData data)
    {
        switch (position) {
            case 0: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_fill, R.layout.layout_widget_1x1_01_align_fill, data, order);
            case 1: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_1, R.layout.layout_widget_1x1_01_align_float_1, data, order);
            case 2: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_2, R.layout.layout_widget_1x1_01_align_float_2, data, order);
            case 3: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_3, R.layout.layout_widget_1x1_01_align_float_3, data, order);
            case 4: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_4, R.layout.layout_widget_1x1_01_align_float_4, data, order);
            case 6: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_6, R.layout.layout_widget_1x1_01_align_float_6, data, order);
            case 7: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_7, R.layout.layout_widget_1x1_01_align_float_7, data, order);
            case 8: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_8, R.layout.layout_widget_1x1_01_align_float_8, data, order);
            case 9: return chooseSunLayout(R.layout.layout_widget_1x1_0_align_float_9, R.layout.layout_widget_1x1_01_align_float_9, data, order);
            case 5: default: return chooseSunLayout(R.layout.layout_widget_1x1_0, R.layout.layout_widget_1x1_01, data, order);
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
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
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, new int[] {8,2}, "sans-serif", boldTime, (showSeconds ? "00:00:00" : "00:00"), timeSizeSp, ClockLayout.CLOCKFACE_MAX_SP, "MM", suffixSizeSp, iconSizeDp);
                if (adjustedSizeSp[0] > timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

                    views.setTextViewTextSize(R.id.text_time_set, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_set_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

                    views.setViewPadding(R.id.text_time_set, 0, 0, (int)scaledPadding/2, 0);
                    views.setViewPadding(R.id.text_time_set_suffix, 0, 0, (int)scaledPadding, 0);
                    views.setViewPadding(R.id.icon_time_sunset, (int)(scaledPadding), 0, (int)scaledPadding/2, 0);

                    views.setViewPadding(R.id.text_time_rise, 0, 0, (int)scaledPadding/2, 0);
                    views.setViewPadding(R.id.text_time_rise_suffix, 0, 0, (int)scaledPadding, 0);
                    views.setViewPadding(R.id.icon_time_sunrise, (int)(scaledPadding), 0, (int)scaledPadding/2, 0);

                    Drawable sunriseIcon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunrise1, null), sunriseIconColor);
                    views.setImageViewBitmap(R.id.icon_time_sunrise, SuntimesUtils.drawableToBitmap(context, sunriseIcon, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));

                    Drawable sunsetIcon = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunset1, null), sunsetIconColor);
                    views.setImageViewBitmap(R.id.icon_time_sunset, SuntimesUtils.drawableToBitmap(context, sunsetIcon, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));
                }
            }
        }

        updateViewsSunRiseSetText(context, views, data, showSeconds, order, timeFormat);
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        iconSizeDp = 18;   // override 32
        int suffixColor = theme.getTimeSuffixColor();
        views.setTextColor(R.id.text_time_rise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_rise, theme.getSunriseTextColor());
        views.setTextColor(R.id.text_time_set_suffix, suffixColor);
        views.setTextColor(R.id.text_time_set, theme.getSunsetTextColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);
            views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);

            views.setTextViewTextSize(R.id.text_time_set, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_set_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);
        }

        sunriseIconColor = theme.getSunriseIconColor();
        sunriseIconStrokeColor = theme.getSunriseIconStrokeColor();
        sunriseIconStrokePixels = theme.getSunriseIconStrokePixels(context);

        sunsetIconColor = theme.getSunsetIconColor();
        sunsetIconStrokeColor = theme.getSunsetIconStrokeColor();
        sunsetIconStrokePixels = theme.getSunsetIconStrokePixels(context);

        Bitmap sunriseIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunrise0, sunriseIconColor, sunriseIconStrokeColor, sunriseIconStrokePixels);
        views.setImageViewBitmap(R.id.icon_time_sunrise, sunriseIcon);

        Bitmap sunsetIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunset0, sunsetIconColor, sunsetIconStrokeColor, sunsetIconStrokePixels);
        views.setImageViewBitmap(R.id.icon_time_sunset, sunsetIcon);
    }
}
