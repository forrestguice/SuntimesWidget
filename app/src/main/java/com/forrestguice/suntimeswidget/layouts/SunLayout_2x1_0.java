/**
   Copyright (C) 2014 Forrest Guice
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
import android.os.Build;
import android.text.SpannableString;
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

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        updateViewsSunRiseSetText(context, views, data, showSeconds);

        // update day delta
        boolean showDayDelta = WidgetSettings.loadShowComparePref(context, appWidgetId);
        TimeDisplayText dayDeltaDisplay = utils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther(), true);
        String dayDeltaValue = dayDeltaDisplay.getValue();
        String dayDeltaUnits = dayDeltaDisplay.getUnits();
        String dayDeltaSuffix = dayDeltaDisplay.getSuffix();

        views.setTextViewText(R.id.text_delta_day_prefix, data.dayDeltaPrefix());   // TODO: refactor to use only a single TextView and SpannableString
        views.setTextViewText(R.id.text_delta_day_value, (boldTime ? SuntimesUtils.createBoldSpan(dayDeltaValue, dayDeltaValue) : dayDeltaValue));
        views.setTextViewText(R.id.text_delta_day_units, dayDeltaUnits);
        views.setTextViewText(R.id.text_delta_day_suffix, dayDeltaSuffix);
        views.setViewVisibility(R.id.layout_delta_day, (showDayDelta ? View.VISIBLE : View.GONE));

        // update solar noon
        SuntimesRiseSetData noonData = data.getLinked();
        boolean showSolarNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        if (showSolarNoon && noonData != null)
        {
            updateViewsNoonText(context, views, noonData, showSeconds);
            views.setViewVisibility(R.id.layout_noon, View.VISIBLE);

        } else {
            views.setViewVisibility(R.id.layout_noon, View.GONE);
        }
    }

    private boolean boldTime = false;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        int sunriseColor = theme.getSunriseTextColor();
        int sunsetColor = theme.getSunsetTextColor();
        int noonColor = theme.getNoonTextColor();
        int suffixColor = theme.getTimeSuffixColor();
        int timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        boldTime = theme.getTimeBold();

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
            float textSize = theme.getTextSizeSp();
            float timeSize = theme.getTimeSizeSp();
            float suffSize = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time_rise_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);
            views.setTextViewTextSize(R.id.text_time_rise, TypedValue.COMPLEX_UNIT_SP, timeSize);

            views.setTextViewTextSize(R.id.text_time_noon, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.text_time_noon_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);

            views.setTextViewTextSize(R.id.text_time_set, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.text_time_set_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);

            views.setTextViewTextSize(R.id.text_delta_day_prefix, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.text_delta_day_value, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.text_delta_day_units, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.text_delta_day_suffix, TypedValue.COMPLEX_UNIT_SP, textSize);
        }

        Bitmap sunriseIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunrise0, theme.getSunriseIconColor(), theme.getSunriseIconStrokeColor(), theme.getSunriseIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_sunrise, sunriseIcon);

        Bitmap noonIcon = SuntimesUtils.gradientDrawableToBitmap(context, R.drawable.ic_noon_large0, theme.getNoonIconColor(), theme.getNoonIconStrokeColor(), theme.getNoonIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_noon, noonIcon);

        Bitmap sunsetIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunset0, theme.getSunsetIconColor(), theme.getSunsetIconStrokeColor(), theme.getSunsetIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_sunset, sunsetIcon);
    }
}
