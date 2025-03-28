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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import com.forrestguice.support.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;

public class SunLayout_2x1_0 extends SunLayout
{
    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_2x1_0;
    }

    protected WidgetSettings.RiseSetOrder order = WidgetSettings.RiseSetOrder.TODAY;

    @Override
    public void prepareForUpdate(Context context, int appWidgetID, SuntimesRiseSetData data)
    {
        super.prepareForUpdate(context, appWidgetID, data);
        order = WidgetSettings.loadRiseSetOrderPref(context, appWidgetID);

        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetID);
        this.layoutID = chooseLayout(position, data);
        //this.layoutID = (scaleBase
        //        ? chooseSunLayout(R.layout.layout_widget_2x1_0_align_fill, R.layout.layout_widget_2x1_01_align_fill, data, order)
        //        : chooseSunLayout(R.layout.layout_widget_2x1_0, R.layout.layout_widget_2x1_01, data, order));
    }

    protected int chooseLayout(int position, SuntimesRiseSetData data)
    {
        switch (position) {
            case 0: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_fill, R.layout.layout_widget_2x1_01_align_fill, data, order);
            case 1: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_1, R.layout.layout_widget_2x1_01_align_float_1, data, order);
            case 2: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_2, R.layout.layout_widget_2x1_01_align_float_2, data, order);
            case 3: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_3, R.layout.layout_widget_2x1_01_align_float_3, data, order);
            case 4: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_4, R.layout.layout_widget_2x1_01_align_float_4, data, order);
            case 6: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_6, R.layout.layout_widget_2x1_01_align_float_6, data, order);
            case 7: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_7, R.layout.layout_widget_2x1_01_align_float_7, data, order);
            case 8: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_8, R.layout.layout_widget_2x1_01_align_float_8, data, order);
            case 9: return chooseSunLayout(R.layout.layout_widget_2x1_0_align_float_9, R.layout.layout_widget_2x1_01_align_float_9, data, order);
            case 5: default: return chooseSunLayout(R.layout.layout_widget_2x1_0, R.layout.layout_widget_2x1_01, data, order);
        }
    }

    protected float[] findAdjustedSize(Context context, int appWidgetId)
    {
        boolean showSolarNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showDayDelta = WidgetSettings.loadShowComparePref(context, appWidgetId);
        int numRows = 1, numCols = 2;
        numRows += showSolarNoon ? 1 : 0;
        numRows += showDayDelta ? 1 : 0;
        int[] maxDp = new int[] {(maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2] + 32)) / numCols,
                ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3])) / numRows)};
        float maxSp = SuntimesLayout.MAX_SP;
        return adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, (showSeconds ? "00:00:00" : "00:00"), timeSizeSp, maxSp, "MM", suffixSizeSp, iconSizeDp);
    }

    @TargetApi(16)
    public void scaleViews(Context context, int appWidgetId, RemoteViews views, float[] adjustedSizeSp)
    {
        if (adjustedSizeSp[0] > timeSizeSp)
        {
            //float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
            //float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

            views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
            views.setTextViewTextSize(R.id.text_time_set, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setTextViewTextSize(R.id.text_time_set_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
            views.setTextViewTextSize(R.id.text_time_noon, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setTextViewTextSize(R.id.text_time_noon_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);

            if (adjustedSizeSp[1] > timeSizeSp)
            {
                views.setTextViewTextSize(R.id.text_delta_day_prefix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
                views.setTextViewTextSize(R.id.text_delta_day_value, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
                views.setTextViewTextSize(R.id.text_delta_day_units, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
                views.setTextViewTextSize(R.id.text_delta_day_suffix, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[1]);
            }

            /*
            views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);
            views.setViewPadding(R.id.text_delta_day_prefix, (int)(scaledPadding), 0, 0, (int)(scaledPadding / 2));
            views.setViewPadding(R.id.text_delta_day_value, 0, 0, 0, (int)(scaledPadding / 2));
            views.setViewPadding(R.id.text_delta_day_units, 0, 0, 0, (int)(scaledPadding / 2));
            views.setViewPadding(R.id.text_delta_day_suffix, 0, 0, 0, (int)(scaledPadding / 2));

            views.setViewPadding(R.id.text_time_set_suffix, (int)(scaledPadding/2), 0, 0, 0);
            views.setViewPadding(R.id.icon_time_sunset, (int)(scaledPadding/2), 0, (int)scaledPadding/2, 0);
            views.setViewPadding(R.id.text_time_rise_suffix, (int)(scaledPadding/2), 0, 0, 0);
            views.setViewPadding(R.id.icon_time_sunrise, (int)(scaledPadding/2), 0, (int)scaledPadding/2, 0);
            views.setViewPadding(R.id.text_time_noon_suffix, (int)(scaledPadding/2), 0, 0, 0);
            views.setViewPadding(R.id.icon_time_noon, (int)(scaledPadding/2), 0, (int)scaledPadding/2, 0);
             */

            //views.setTextViewTextSize(R.id.text_delta_day_prefix, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
            //views.setTextViewTextSize(R.id.text_delta_day_value, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
            //views.setTextViewTextSize(R.id.text_delta_day_units, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);
            //views.setTextViewTextSize(R.id.text_delta_day_suffix, TypedValue.COMPLEX_UNIT_DIP, textScale * textSizeSp);

            Drawable d1 = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunrise1, null), sunriseColor);
            views.setImageViewBitmap(R.id.icon_time_sunrise, SuntimesUtils.drawableToBitmap(context, d1, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));

            Drawable d2 = SuntimesUtils.tintDrawableCompat(ResourcesCompat.getDrawable(context.getResources(), R.drawable.svg_sunset1, null), sunsetColor);
            views.setImageViewBitmap(R.id.icon_time_sunset, SuntimesUtils.drawableToBitmap(context, d2, (int)adjustedSizeSp[2], (int)adjustedSizeSp[2] / 2, false));

            int noonIconSizeDp = (int)(adjustedSizeSp[2] * 0.85);
            if (noonIconSizeDp > 24) {
                Drawable d3 = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_noon_large1, null);
                views.setImageViewBitmap(R.id.icon_time_noon, SuntimesUtils.drawableToBitmap(context, d3, noonIconSizeDp, noonIconSizeDp, false));
            }
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        boolean showSolarNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showDayDelta = WidgetSettings.loadShowComparePref(context, appWidgetId);
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId)) {
                scaleViews(context, appWidgetId, views, findAdjustedSize(context, appWidgetId));
            }
        }

        updateViewsSunRiseSetText(context, views, data, showSeconds, order, timeFormat);

        // update day delta
        TimeDisplayText dayDeltaDisplay = utils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther(), true);
        String dayDeltaValue = dayDeltaDisplay.getValue();
        String dayDeltaUnits = dayDeltaDisplay.getUnits();
        String dayDeltaSuffix = dayDeltaDisplay.getSuffix();

        views.setTextViewText(R.id.text_delta_day_prefix, data.dayDeltaPrefix());   // TODO: refactor to use only a single TextView and SpannableString
        views.setTextViewText(R.id.text_delta_day_value, (boldTime ? SuntimesUtils.createBoldSpan(null, dayDeltaValue, dayDeltaValue) : dayDeltaValue));
        views.setTextViewText(R.id.text_delta_day_units, dayDeltaUnits);
        views.setTextViewText(R.id.text_delta_day_suffix, dayDeltaSuffix);
        views.setViewVisibility(R.id.layout_delta_day, (showDayDelta ? View.VISIBLE : View.GONE));

        // update solar noon
        SuntimesRiseSetData noonData = data.getLinked();
        if (showSolarNoon && noonData != null) {
            updateViewsNoonText(context, views, noonData.sunsetCalendarToday(), showSeconds, timeFormat);
            views.setViewVisibility(R.id.layout_noon, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.layout_noon, View.GONE);
        }
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        iconSizeDp = 22;   // override 32
        int noonColor = theme.getNoonTextColor();
        int timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();

        // theme sunrise text
        views.setTextColor(R.id.text_time_rise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_rise, sunriseColor);

        // theme sunset text
        views.setTextColor(R.id.text_time_set_suffix, suffixColor);
        views.setTextColor(R.id.text_time_set, sunsetColor);

        // theme note
        views.setTextColor(R.id.text_delta_day_prefix, textColor);
        views.setTextColor(R.id.text_delta_day_value, timeColor);
        views.setTextColor(R.id.text_delta_day_units, textColor);
        views.setTextColor(R.id.text_delta_day_suffix, textColor);

        // theme noon
        views.setTextColor(R.id.text_time_noon_suffix, suffixColor);
        views.setTextColor(R.id.text_time_noon, noonColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            textSizeSp = theme.getTextSizeSp();
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);
            views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);

            views.setTextViewTextSize(R.id.text_time_noon, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_noon_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);

            views.setTextViewTextSize(R.id.text_time_set, TypedValue.COMPLEX_UNIT_DIP, timeSizeSp);
            views.setTextViewTextSize(R.id.text_time_set_suffix, TypedValue.COMPLEX_UNIT_DIP, suffixSizeSp);

            views.setTextViewTextSize(R.id.text_delta_day_prefix, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_delta_day_value, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_delta_day_units, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
            views.setTextViewTextSize(R.id.text_delta_day_suffix, TypedValue.COMPLEX_UNIT_DIP, textSizeSp);
        }

        Bitmap sunriseIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunrise0, theme.getSunriseIconColor(), theme.getSunriseIconStrokeColor(), theme.getSunriseIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_sunrise, sunriseIcon);

        Bitmap noonIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_noon_large1, theme.getNoonIconColor(), theme.getNoonIconStrokeColor(), theme.getNoonIconStrokePixels(context));   // doesn't call mutate (themes other Drawable instances)
        Drawable noonDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_noon_large1, null);    // specify 24x24 icon (intrinsic height used by layerDrawableToBitmap is wrong)
        views.setImageViewBitmap(R.id.icon_time_noon, SuntimesUtils.drawableToBitmap(context, noonDrawable, 24, 24, false));

        Bitmap sunsetIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunset0, theme.getSunsetIconColor(), theme.getSunsetIconStrokeColor(), theme.getSunsetIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_sunset, sunsetIcon);
    }

}
