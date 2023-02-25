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
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays only the sunrise time.
 */
public class SunLayout_1x1_1 extends SunLayout
{
    protected int sunriseIconColor = Color.YELLOW;
    protected int sunriseIconStrokeColor = Color.YELLOW;
    protected int sunriseIconStrokePixels = 0;

    public SunLayout_1x1_1()
    {
        super();
    }

    public SunLayout_1x1_1(int layoutID )
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1_1;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetID, SuntimesRiseSetData data)
    {
        super.prepareForUpdate(context, appWidgetID, data);
        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetID);
        this.layoutID = chooseLayout(position);
    }

    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_1x1_1_align_fill;
            case 1: return R.layout.layout_widget_1x1_1_align_float_1;
            case 2: return R.layout.layout_widget_1x1_1_align_float_2;
            case 3: return R.layout.layout_widget_1x1_1_align_float_3;
            case 4: return R.layout.layout_widget_1x1_1_align_float_4;
            case 6: return R.layout.layout_widget_1x1_1_align_float_6;
            case 7: return R.layout.layout_widget_1x1_1_align_float_7;
            case 8: return R.layout.layout_widget_1x1_1_align_float_8;
            case 9: return R.layout.layout_widget_1x1_1_align_float_9;
            case 5: default: return R.layout.layout_widget_1x1_1;
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        WidgetSettings.RiseSetOrder order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) / 2)};
                //int[] maxDp = new int[] {maxDimensionsDp[0] - (int)Math.ceil(iconSizeDp), maxDimensionsDp[1]};
                float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, (showSeconds ? "00:00:00" : "00:00"), timeSizeSp, SuntimesLayout.MAX_SP, "MM", suffixSizeSp);
                if (adjustedSizeSp[0] != timeSizeSp)
                {
                    float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
                    float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

                    views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
                    views.setViewPadding(R.id.text_time_rise, (int)scaledPadding, 0, (int)(scaledPadding / 2), 0);
                    views.setViewPadding(R.id.text_time_rise_suffix, 0, 0, (int)(scaledPadding), 0);
                    views.setViewPadding(R.id.icon_time_sunrise, 0, (int)(scaledPadding), 0, 0);

                    views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
                    views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

                    Drawable d = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunrise2, null), sunriseIconColor);
                    views.setImageViewBitmap(R.id.icon_time_sunrise, SuntimesUtils.drawableToBitmap(context, d, (int)(iconSizeDp * textScale), (int)(iconSizeDp * textScale) / 4, false));
                }
            }
        }

        Calendar event = data.sunriseCalendar(1);
        if (order != WidgetSettings.RiseSetOrder.TODAY)
        {
            Calendar now = Calendar.getInstance();
            if (now.after(event)) {
                event = data.sunriseCalendar(2);
            }
        }
        updateViewsSunriseText(context, views, event, showSeconds, timeFormat);
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        views.setTextColor(R.id.text_time_rise_suffix, theme.getTimeSuffixColor());
        views.setTextColor(R.id.text_time_rise, theme.getSunriseTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, theme.getTimeSuffixSizeSp());
            views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, theme.getTimeSizeSp());
        }

        sunriseIconColor = theme.getSunriseIconColor();
        sunriseIconStrokeColor = theme.getSunriseIconStrokeColor();
        sunriseIconStrokePixels = theme.getSunriseIconStrokePixels(context);

        Bitmap sunriseIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunrise_large0, sunriseIconColor, sunriseIconStrokeColor, sunriseIconStrokePixels);
        views.setImageViewBitmap(R.id.icon_time_sunrise, sunriseIcon);
    }
}
