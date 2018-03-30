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
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public abstract class SunPosLayout extends SuntimesLayout
{
    protected static final SuntimesUtils utils = new SuntimesUtils();

    public void prepareForUpdate(SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        dataset.calculateData();
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     * @param dataset the data object to apply to the views
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        // update title
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = utils.displayStringForTitlePattern(context, titlePattern, dataset);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected static final int DECIMAL_PLACES = 1;
    protected static final float SYMBOL_RELATIVE_SIZE = 0.7f;

    protected void updateViewsAzimuthElevationText(Context context, RemoteViews views, SuntimesCalculator.SunPosition sunPosition, SuntimesCalculator.SunPosition noonPosition)
    {
        SuntimesUtils.TimeDisplayText azimuthDisplay = utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, false);
        views.setTextViewText(R.id.info_sun_azimuth_current, styleAzimuthText(azimuthDisplay, highlightColor));

        if (Build.VERSION.SDK_INT >= 15) {
            SuntimesUtils.TimeDisplayText azimuthDescription = utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, true);
            views.setContentDescription(R.id.info_sun_azimuth_current, utils.formatAsDirection(azimuthDescription.getValue(), azimuthDescription.getSuffix()));
        }

        int elevationColor = (sunPosition.elevation <= 0 ? highlightColor :
                (SuntimesRiseSetDataset.isRising(sunPosition, noonPosition) ? risingColor : settingColor));
        views.setTextViewText(R.id.info_sun_elevation_current, styleElevationText(sunPosition.elevation, elevationColor));
    }

    protected SpannableString styleAzimuthText(SuntimesUtils.TimeDisplayText azimuthDisplay, int color)
    {
        String azimuthSymbol = azimuthDisplay.getSuffix();
        String azimuthString = utils.formatAsDirection(azimuthDisplay.getValue(), azimuthSymbol);
        SpannableString azimuth = SuntimesUtils.createColorSpan(null, azimuthString, azimuthDisplay.getValue(), color, boldTime);
        azimuth = SuntimesUtils.createBoldColorSpan(azimuth, azimuthString, azimuthSymbol, suffixColor);
        azimuth = SuntimesUtils.createRelativeSpan(azimuth, azimuthString, azimuthSymbol, SYMBOL_RELATIVE_SIZE);
        //azimuth = SuntimesUtils.createAbsoluteSpan(azimuth, azimuthString, azimuthDisplay.getSuffix(), SuntimesUtils.spToPixels(context, suffixSp));
        return azimuth;
    }

    protected SpannableString styleElevationText(double value, int color)
    {
        SuntimesUtils.TimeDisplayText elevationDisplay = utils.formatAsElevation(value, DECIMAL_PLACES);
        String elevationSymbol = elevationDisplay.getSuffix();
        String elevationString = utils.formatAsElevation(elevationDisplay.getValue(), elevationSymbol);
        SpannableString elevation = SuntimesUtils.createColorSpan(null, elevationString, elevationString, color, boldTime);
        elevation = SuntimesUtils.createBoldColorSpan(elevation, elevationString, elevationSymbol, suffixColor);
        elevation = SuntimesUtils.createRelativeSpan(elevation, elevationString, elevationSymbol, SYMBOL_RELATIVE_SIZE);
        return elevation;
    }

    protected void updateViewsRightAscDeclinationText(Context context, RemoteViews views, SuntimesCalculator.SunPosition sunPosition)
    {
        SuntimesUtils.TimeDisplayText rightAscDisplay = utils.formatAsRightAscension(sunPosition.rightAscension, DECIMAL_PLACES);
        String rightAscSymbol = rightAscDisplay.getSuffix();
        String rightAscString = utils.formatAsRightAscension(rightAscDisplay.getValue(), rightAscSymbol);
        SpannableString rightAsc = SuntimesUtils.createColorSpan(null, rightAscString, rightAscString, highlightColor, boldTime);
        rightAsc = SuntimesUtils.createBoldColorSpan(rightAsc, rightAscString, rightAscSymbol, suffixColor);
        rightAsc = SuntimesUtils.createRelativeSpan(rightAsc, rightAscString, rightAscSymbol, SYMBOL_RELATIVE_SIZE);
        views.setTextViewText(R.id.info_sun_rightascension_current, rightAsc);

        SuntimesUtils.TimeDisplayText declinationDisplay = utils.formatAsDeclination(sunPosition.declination, DECIMAL_PLACES);
        String declinationSymbol = declinationDisplay.getSuffix();
        String declinationString = utils.formatAsDeclination(declinationDisplay.getValue(), declinationSymbol);
        SpannableString declination = SuntimesUtils.createColorSpan(null, declinationString, declinationString, highlightColor, boldTime);
        declination = SuntimesUtils.createBoldColorSpan(declination, declinationString, declinationSymbol, suffixColor);
        declination = SuntimesUtils.createRelativeSpan(declination, declinationString, declinationSymbol, SYMBOL_RELATIVE_SIZE);
        views.setTextViewText(R.id.info_sun_declination_current, declination);
    }

    protected void updateViewsAzimuthElevationText(Context context, RemoteViews views, SuntimesCalculator.SunPosition sunPosition, SuntimesCalculator.SunPosition risingPosition, SuntimesCalculator.SunPosition noonPosition, SuntimesCalculator.SunPosition settingPosition)
    {
        SuntimesUtils.TimeDisplayText azimuthRising = utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, false);
        views.setTextViewText(R.id.info_sun_azimuth_rising, styleAzimuthText(azimuthRising, risingColor));

        if (Build.VERSION.SDK_INT >= 15) {
            SuntimesUtils.TimeDisplayText azimuthRisingDesc = utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, true);
            views.setContentDescription(R.id.info_sun_azimuth_rising, utils.formatAsDirection(azimuthRisingDesc.getValue(), azimuthRisingDesc.getSuffix()));
        }

        views.setTextViewText(R.id.info_sun_elevation_atnoon, styleElevationText(noonPosition.elevation, settingColor));

        SuntimesUtils.TimeDisplayText azimuthSetting = utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, false);
        views.setTextViewText(R.id.info_sun_azimuth_setting, styleAzimuthText(azimuthSetting, settingColor));

        if (Build.VERSION.SDK_INT >= 15) {
            SuntimesUtils.TimeDisplayText azimuthSettingDesc = utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, true);
            views.setContentDescription(R.id.info_sun_azimuth_setting, utils.formatAsDirection(azimuthSettingDesc.getValue(), azimuthSettingDesc.getSuffix()));
        }
    }

    protected int risingColor = Color.YELLOW;
    protected int settingColor = Color.RED;
    protected int highlightColor = Color.WHITE;
    protected float suffixSp;
    protected int suffixColor = Color.GRAY;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        highlightColor = theme.getTimeColor();
        risingColor = theme.getSunriseTextColor();
        settingColor = theme.getSunsetTextColor();
        boldTime = theme.getTimeBold();
        suffixSp = theme.getTimeSuffixSizeSp();
        suffixColor = theme.getTimeSuffixColor();
    }

    protected void themeViewsAzimuthElevationText(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_sun_azimuth_current_label, textColor);
        views.setTextColor(R.id.info_sun_elevation_current_label, textColor);
        views.setTextColor(R.id.info_sun_azimuth_current, textColor);
        views.setTextColor(R.id.info_sun_elevation_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }

    protected void themeViewsRightAscDeclinationText(Context context, RemoteViews views, SuntimesTheme theme)
    {
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_sun_rightascension_current_label, textColor);
        views.setTextColor(R.id.info_sun_declination_current_label, textColor);
        views.setTextColor(R.id.info_sun_rightascension_current, textColor);
        views.setTextColor(R.id.info_sun_declination_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }

}
