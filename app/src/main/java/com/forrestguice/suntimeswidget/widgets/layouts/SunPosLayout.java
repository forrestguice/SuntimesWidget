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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.Calendar;

public abstract class SunPosLayout extends PositionLayout
{
    protected float titleSizeSp = 10;
    protected float textSizeSp = 12;
    protected float timeSizeSp = 12;
    protected float suffixSizeSp = 8;

    protected boolean scaleBase = WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE;

    public void prepareForUpdate(Context context, int appWidgetId, SuntimesRiseSetDataset dataset, int[] widgetSize)
    {
        this.scaleBase = WidgetSettings.loadScaleBasePref(context, appWidgetId);
        dataset.calculateData(context);
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
        String titleText = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, dataset.dataActual);
        CharSequence title = (boldTitle ? SpanUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

    protected SuntimesCalculator.SunPosition updateLabels(Context context, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        Calendar now = dataset.now();
        SuntimesCalculator calculator = dataset.calculator();
        SuntimesCalculator.SunPosition sunPosition = (calculator != null ? calculator.getSunPosition(now) : null);

        SuntimesRiseSetData riseSetData = dataset.dataActual;
        Calendar riseTime = (riseSetData != null ? riseSetData.sunriseCalendarToday() : null);
        SuntimesCalculator.SunPosition risingPosition = (riseTime != null && calculator != null ? calculator.getSunPosition(riseTime) : null);

        SuntimesRiseSetData noonData = dataset.dataNoon;
        Calendar noonTime = (noonData != null ? noonData.sunriseCalendarToday() : null);
        SuntimesCalculator.SunPosition noonPosition = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);

        Calendar setTime = (riseSetData != null ? riseSetData.sunsetCalendarToday() : null);
        SuntimesCalculator.SunPosition settingPosition = (setTime != null && calculator != null ? calculator.getSunPosition(setTime) : null);

        updateViewsAzimuthElevationText(context, views, sunPosition, noonPosition);
        updateViewsAzimuthElevationText(context, views, sunPosition, risingPosition, noonPosition, settingPosition);
        return sunPosition;
    }

    @TargetApi(16)
    protected void scaleLabels(Context context, RemoteViews views)
    {
        int[] maxDp = new int[] {maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), ((maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3])) / 4)};
        float[] adjustedSizeSp = adjustTextSize(context, maxDp, paddingDp, "sans-serif", boldTime, "MMM.MMM MMM.MMM MMM.MMM MMM.MMM MMM.MMM", timeSizeSp, SuntimesLayout.MAX_SP, "", suffixSizeSp);
        if (adjustedSizeSp[0] > timeSizeSp)
        {
            float textScale = Math.max(adjustedSizeSp[0] / timeSizeSp, 1);
            float scaledPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textScale * 2, context.getResources().getDisplayMetrics());

            views.setViewPadding(R.id.text_title, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

            views.setTextViewTextSize(R.id.info_sun_azimuth_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setViewPadding(R.id.info_sun_azimuth_current, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

            views.setTextViewTextSize(R.id.info_sun_elevation_current, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setViewPadding(R.id.info_sun_elevation_current, (int)(scaledPadding), 0, (int)(scaledPadding), 0);

            views.setTextViewTextSize(R.id.info_sun_azimuth_rising, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setViewPadding(R.id.info_sun_azimuth_rising, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

            views.setTextViewTextSize(R.id.info_sun_elevation_atnoon, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setViewPadding(R.id.info_sun_elevation_atnoon, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));

            views.setTextViewTextSize(R.id.info_sun_azimuth_setting, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp[0]);
            views.setViewPadding(R.id.info_sun_azimuth_setting, (int)(scaledPadding), 0, (int)(scaledPadding), (int)(scaledPadding));
        }
    }

    protected void updateViewsAzimuthElevationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition, @Nullable SuntimesCalculator.SunPosition noonPosition)
    {
        if (sunPosition != null)
        {
            TimeDisplayText azimuthDisplay = angle_utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_current, styleAzimuthText(azimuthDisplay, highlightColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                TimeDisplayText azimuthDescription = angle_utils.formatAsDirection2(sunPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_current, angle_utils.formatAsDirection(azimuthDescription.getValue(), azimuthDescription.getSuffix()));
            }

            int elevationColor = (sunPosition.elevation <= 0 ? highlightColor :
                    (SuntimesRiseSetDataset.isRising(sunPosition, noonPosition) ? risingColor : settingColor));
            views.setTextViewText(R.id.info_sun_elevation_current, styleElevationText(sunPosition.elevation, elevationColor, suffixColor, boldTime));
        }
    }

    public static SpannableString styleRightAscText(@NonNull SuntimesCalculator.SunPosition sunPosition, int highlightColor, int suffixColor, boolean boldTime)
    {
        TimeDisplayText rightAscDisplay = angle_utils.formatAsRightAscension(sunPosition.rightAscension, DECIMAL_PLACES);
        String rightAscSymbol = rightAscDisplay.getSuffix();
        String rightAscString = angle_utils.formatAsRightAscension(rightAscDisplay.getValue(), rightAscSymbol);
        SpannableString rightAsc = SpanUtils.createColorSpan(null, rightAscString, rightAscString, highlightColor, boldTime);
        rightAsc = SpanUtils.createBoldColorSpan(rightAsc, rightAscString, rightAscSymbol, suffixColor);
        rightAsc = SpanUtils.createRelativeSpan(rightAsc, rightAscString, rightAscSymbol, SYMBOL_RELATIVE_SIZE);
        return rightAsc;
    }

    public static SpannableString styleDeclinationText(@NonNull SuntimesCalculator.SunPosition sunPosition, int highlightColor, int suffixColor, boolean boldTime)
    {
        TimeDisplayText declinationDisplay = angle_utils.formatAsDeclination(sunPosition.declination, DECIMAL_PLACES);
        String declinationSymbol = declinationDisplay.getSuffix();
        String declinationString = angle_utils.formatAsDeclination(declinationDisplay.getValue(), declinationSymbol);
        SpannableString declination = SpanUtils.createColorSpan(null, declinationString, declinationString, highlightColor, boldTime);
        declination = SpanUtils.createBoldColorSpan(declination, declinationString, declinationSymbol, suffixColor);
        declination = SpanUtils.createRelativeSpan(declination, declinationString, declinationSymbol, SYMBOL_RELATIVE_SIZE);
        return declination;
    }

    protected void updateViewsRightAscDeclinationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition)
    {
        if (sunPosition != null)
        {
            views.setTextViewText(R.id.info_sun_rightascension_current, styleRightAscText(sunPosition, highlightColor, suffixColor, boldTime));
            views.setTextViewText(R.id.info_sun_declination_current, styleDeclinationText(sunPosition, highlightColor, suffixColor, boldTime));
        }
    }

    protected void updateViewsAzimuthElevationText(Context context, @NonNull RemoteViews views, @Nullable SuntimesCalculator.SunPosition sunPosition, @Nullable SuntimesCalculator.SunPosition risingPosition, @Nullable SuntimesCalculator.SunPosition noonPosition, @Nullable SuntimesCalculator.SunPosition settingPosition)
    {
        if (risingPosition != null)
        {
            TimeDisplayText azimuthRising = angle_utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_rising, styleAzimuthText(azimuthRising, risingColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                TimeDisplayText azimuthRisingDesc = angle_utils.formatAsDirection2(risingPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_rising, angle_utils.formatAsDirection(azimuthRisingDesc.getValue(), azimuthRisingDesc.getSuffix()));
            }
        }

        if (noonPosition != null) {
            views.setTextViewText(R.id.info_sun_elevation_atnoon, styleElevationText(noonPosition.elevation, settingColor, suffixColor, boldTime));
        }

        if (settingPosition != null)
        {
            TimeDisplayText azimuthSetting = angle_utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, false);
            views.setTextViewText(R.id.info_sun_azimuth_setting, styleAzimuthText(azimuthSetting, settingColor, suffixColor, boldTime));

            if (Build.VERSION.SDK_INT >= 15) {
                TimeDisplayText azimuthSettingDesc = angle_utils.formatAsDirection2(settingPosition.azimuth, DECIMAL_PLACES, true);
                views.setContentDescription(R.id.info_sun_azimuth_setting, angle_utils.formatAsDirection(azimuthSettingDesc.getValue(), azimuthSettingDesc.getSuffix()));
            }
        }
    }

    protected int risingColor = Color.YELLOW;
    protected int settingColor = Color.RED;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        risingColor = theme.getSunriseTextColor();
        settingColor = theme.getSunsetTextColor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            titleSizeSp = theme.getTitleSizeSp();
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
