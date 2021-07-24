/**
   Copyright (C) 2018-2020 Forrest Guice
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public abstract class SunPosLayout extends PositionLayout
{
    protected float textSizeSp = 12;
    protected float timeSizeSp = 12;
    protected float suffixSizeSp = 8;
    protected int[] paddingDp = new int[] {0, 0};

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

    protected void updateViewsAzimuthElevationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition, @Nullable SuntimesCalculator.SunPosition noonPosition)
    {
        if (sunPosition != null)
        {
            SuntimesUtils.TimeDisplayText azimuthDisplay = utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_current, styleAzimuthText(azimuthDisplay, highlightColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                SuntimesUtils.TimeDisplayText azimuthDescription = utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_current, utils.formatAsDirection(azimuthDescription.getValue(), azimuthDescription.getSuffix()));
            }

            int elevationColor = (sunPosition.elevation <= 0 ? highlightColor :
                    (SuntimesRiseSetDataset.isRising(sunPosition, noonPosition) ? risingColor : settingColor));
            views.setTextViewText(R.id.info_sun_elevation_current, styleElevationText(sunPosition.elevation, elevationColor, suffixColor, boldTime));
        }
    }

    protected void updateViewsRightAscDeclinationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition)
    {
        if (sunPosition != null)
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
    }

    protected void updateViewsAzimuthElevationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition, @Nullable SuntimesCalculator.SunPosition risingPosition, @Nullable SuntimesCalculator.SunPosition noonPosition, @Nullable SuntimesCalculator.SunPosition settingPosition)
    {
        if (risingPosition != null)
        {
            SuntimesUtils.TimeDisplayText azimuthRising = utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_rising, styleAzimuthText(azimuthRising, risingColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                SuntimesUtils.TimeDisplayText azimuthRisingDesc = utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_rising, utils.formatAsDirection(azimuthRisingDesc.getValue(), azimuthRisingDesc.getSuffix()));
            }
        }

        if (noonPosition != null) {
            views.setTextViewText(R.id.info_sun_elevation_atnoon, styleElevationText(noonPosition.elevation, settingColor, suffixColor, boldTime));
        }

        if (settingPosition != null)
        {
            SuntimesUtils.TimeDisplayText azimuthSetting = utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_setting, styleAzimuthText(azimuthSetting, settingColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                SuntimesUtils.TimeDisplayText azimuthSettingDesc = utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_setting, utils.formatAsDirection(azimuthSettingDesc.getValue(), azimuthSettingDesc.getSuffix()));
            }
        }
    }

    protected int risingColor = Color.YELLOW;
    protected int settingColor = Color.RED;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        paddingDp = theme.getPadding();
        risingColor = theme.getSunriseTextColor();
        settingColor = theme.getSunsetTextColor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            textSizeSp = theme.getTextSizeSp();
            timeSizeSp = theme.getTimeSizeSp();
            suffixSizeSp = theme.getTimeSuffixSizeSp();
        }
    }

    protected void themeViewsAzimuthElevationText(Context context, @NonNull RemoteViews views, @NonNull SuntimesTheme theme)
    {
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_sun_azimuth_current_label, textColor);
        views.setTextColor(R.id.info_sun_elevation_current_label, textColor);
        views.setTextColor(R.id.info_sun_azimuth_current, textColor);
        views.setTextColor(R.id.info_sun_elevation_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_azimuth_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_elevation_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

    protected void themeViewsRightAscDeclinationText(Context context, @NonNull RemoteViews views, @NonNull SuntimesTheme theme)
    {
        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_sun_rightascension_current_label, textColor);
        views.setTextColor(R.id.info_sun_declination_current_label, textColor);
        views.setTextColor(R.id.info_sun_rightascension_current, textColor);
        views.setTextColor(R.id.info_sun_declination_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current_label, TypedValue.COMPLEX_UNIT_DIP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current, TypedValue.COMPLEX_UNIT_DIP, timeSize);
        }
    }

}
