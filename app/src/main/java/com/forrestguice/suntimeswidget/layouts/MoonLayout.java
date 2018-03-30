/**
   Copyright (C) 2018 Forrest Guice
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
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;
import java.util.HashMap;

public abstract class MoonLayout extends SuntimesLayout
{
    public MoonLayout()
    {
        initLayoutID();
    }

    /**
     * Called by widget before themeViews and updateViews to give the layout obj an opportunity to
     * modify its state based on the supplied data.
     * @param data the data object (should be the same as supplied to updateViews)
     */
    public void prepareForUpdate(SuntimesMoonData data)
    {
        // EMPTY
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     * @param data the data object to apply to the views
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        // update title
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = utils.displayStringForTitlePattern(context, titlePattern, data);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected void updateViewsMoonRiseSetText(Context context, RemoteViews views, SuntimesMoonData data, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText riseText = utils.calendarTimeShortDisplayString(context, data.moonriseCalendarToday(), showSeconds);
        String riseString = riseText.getValue();
        CharSequence riseSequence = (boldTime ? SuntimesUtils.createBoldSpan(null, riseString, riseString) : riseString);
        views.setTextViewText(R.id.text_time_moonrise, riseSequence);
        views.setTextViewText(R.id.text_time_moonrise_suffix, riseText.getSuffix());

        SuntimesUtils.TimeDisplayText setText = utils.calendarTimeShortDisplayString(context, data.moonsetCalendarToday(), showSeconds);
        String setString = setText.getValue();
        CharSequence setSequence = (boldTime ? SuntimesUtils.createBoldSpan(null, setString, setString) : setString);
        views.setTextViewText(R.id.text_time_moonset, setSequence);
        views.setTextViewText(R.id.text_time_moonset_suffix, setText.getSuffix());
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
    }

    protected HashMap<MoonPhaseDisplay, Integer> phaseColors = new HashMap<>();

    protected void themeViewsMoonPhase(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int waningColor = theme.getMoonWaningColor();
        int waxingColor = theme.getMoonWaxingColor();

        phaseColors.put(MoonPhaseDisplay.FIRST_QUARTER, waxingColor);
        phaseColors.put(MoonPhaseDisplay.WAXING_CRESCENT, waxingColor);
        phaseColors.put(MoonPhaseDisplay.WAXING_GIBBOUS, waxingColor);

        phaseColors.put(MoonPhaseDisplay.NEW, theme.getMoonNewColor());
        phaseColors.put(MoonPhaseDisplay.FULL, theme.getMoonFullColor());

        phaseColors.put(MoonPhaseDisplay.THIRD_QUARTER, waningColor);
        phaseColors.put(MoonPhaseDisplay.WANING_CRESCENT, waningColor);
        phaseColors.put(MoonPhaseDisplay.WANING_GIBBOUS, waningColor);
    }

    protected void themeViewsMoonPhaseIcons(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int colorWaxing = theme.getMoonWaxingColor();
        int colorWaning = theme.getMoonWaningColor();
        int colorFull = theme.getMoonFullColor();
        int colorNew = theme.getMoonNewColor();

        // full and new
        Bitmap fullMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, theme.getMoonFullStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_info_moonphase_full, fullMoon);

        Bitmap newMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, theme.getMoonNewStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_info_moonphase_new, newMoon);

        // waxing
        Bitmap waxingCrescent = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_CRESCENT.getIcon(), colorWaxing, colorWaxing, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waxing_crescent, waxingCrescent);

        Bitmap waxingQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waxing_quarter, waxingQuarter);

        Bitmap waxingGibbous = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_GIBBOUS.getIcon(), colorWaxing, colorWaxing, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waxing_gibbous, waxingGibbous);

        // waning
        Bitmap waningCrescent = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_CRESCENT.getIcon(), colorWaning, colorWaning, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waning_crescent, waningCrescent);

        Bitmap waningQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waning_quarter, waningQuarter);

        Bitmap waningGibbous = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_GIBBOUS.getIcon(), colorWaning, colorWaning, 0);
        views.setImageViewBitmap(R.id.icon_info_moonphase_waning_gibbous, waningGibbous);
    }

    protected void themeViewsMoonPhaseText(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.text_info_moonillum, textColor);
        views.setTextColor(R.id.text_info_moonphase, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.text_info_moonphase, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.text_info_moonillum, TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }

    protected void themeViewsMoonRiseSetText(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int moonriseColor = theme.getMoonriseTextColor();
        int suffixColor = theme.getTimeSuffixColor();
        views.setTextColor(R.id.text_time_moonrise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_moonrise, moonriseColor);

        int moonsetColor = theme.getMoonsetTextColor();
        views.setTextColor(R.id.text_time_moonset_suffix, suffixColor);
        views.setTextColor(R.id.text_time_moonset, moonsetColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            float suffSize = theme.getTimeSuffixSizeSp();

            views.setTextViewTextSize(R.id.text_time_moonrise_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);
            views.setTextViewTextSize(R.id.text_time_moonrise, TypedValue.COMPLEX_UNIT_SP, timeSize);

            views.setTextViewTextSize(R.id.text_time_moonset, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.text_time_moonset_suffix, TypedValue.COMPLEX_UNIT_SP, suffSize);
        }
    }

    protected void themeViewsMoonRiseSetIcons(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int moonriseColor = theme.getMoonriseTextColor();
        Bitmap moonriseIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_moon_rise, moonriseColor, moonriseColor, 0);
        views.setImageViewBitmap(R.id.icon_time_moonrise, moonriseIcon);

        int moonsetColor = theme.getMoonsetTextColor();
        Bitmap moonsetIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_moon_set, moonsetColor, moonsetColor, 0);
        views.setImageViewBitmap(R.id.icon_time_moonset, moonsetIcon);
    }

    protected int chooseMoonLayout(int layout1, int layout2, SuntimesMoonData data)
    {
        Calendar riseTime = data.moonriseCalendarToday();
        Calendar setTime = data.moonsetCalendarToday();
        if (riseTime != null && setTime != null)
        {
            if (riseTime.before(setTime))
                return layout1;      // moon rises then sets
            else return layout2;    // moon sets then rises

        } else if (riseTime == null && setTime == null) {
            return layout1;  // moon doesn't rise or set today

        } else if (setTime != null) {
            return layout2;  // moon doesn't rise (but it sets)

        } else {
            return layout1;  // moon doesn't set (but it rises)
        }
    }

}
