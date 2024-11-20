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
package com.forrestguice.suntimeswidget.moon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonPhasesColorValues;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.text.NumberFormat;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonPhaseView extends LinearLayout
{
    protected SuntimesUtils utils = new SuntimesUtils();
    protected boolean isRtl = false;
    protected boolean centered = false;
    protected boolean illumAtNoon = false, illumRange = false;
    protected boolean showPosition = false;
    protected boolean northward = false;

    protected LinearLayout content;
    protected TextView phaseText, illumText, azimuthText, elevationText;

    protected SuntimesMoonData data = null;  // cached

    public MoonPhaseView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    protected void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonPhaseView, 0, 0);
        try {
            illumAtNoon = a.getBoolean(R.styleable.MoonPhaseView_illuminationAtLunarNoon, illumAtNoon);
            illumRange = a.getBoolean(R.styleable.MoonPhaseView_illuminationRange, illumRange);
            showPosition = a.getBoolean(R.styleable.MoonPhaseView_showPosition, false);
        } finally {
            a.recycle();
        }
    }

    protected int getLayoutResID() {
        return R.layout.layout_view_moonphase;
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        themeViews(context);
        LayoutInflater.from(context).inflate(getLayoutResID(), this, true);

        if (attrs != null)
        {
            LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        content = (LinearLayout)findViewById(R.id.moonphase_layout);

        phaseText = (TextView)findViewById(R.id.text_info_moonphase);
        illumText = (TextView)findViewById(R.id.text_info_moonillum);
        azimuthText = (TextView)findViewById(R.id.text_info_moon_azimuth);
        elevationText = (TextView)findViewById(R.id.text_info_moon_elevation);

        if (isInEditMode())
        {
            updateViews(context, null);
        }
    }

    private float strokePixels;
    private int noteColor;
    private MoonPhasesColorValues colors;

    private void themeViews(Context context)
    {
        colors = new MoonPhasesColorValues(context);
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();

        strokePixels = context.getResources().getDimension(R.dimen.moonIcon_stroke_full);
        themeIcons(context, null);
    }

    public void setColors(Context context, @Nullable ColorValues colors)
    {
        if (colors != null) {
            this.colors = new MoonPhasesColorValues(colors);
            themeIcons(context, null);
        } else {
            themeViews(context);
        }
    }

    protected SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        this.themeOverride = theme;
        noteColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        int timeColor = theme.getTimeColor();
        float suffixSizeSp = theme.getTimeSuffixSizeSp();
        float timeSizeSp = theme.getTimeSizeSp();

        illumText.setTextColor(textColor);
        illumText.setTextSize(suffixSizeSp);

        azimuthText.setTextColor(textColor);
        azimuthText.setTextSize(suffixSizeSp);

        elevationText.setTextColor(textColor);
        elevationText.setTextSize(suffixSizeSp);

        phaseText.setTextColor(timeColor);
        phaseText.setTextSize(timeSizeSp);
        phaseText.setTypeface(phaseText.getTypeface(), (theme.getTimeBold() ? Typeface.BOLD : Typeface.NORMAL));

        themeIcons(context, theme);
    }

    private void themeIcons(Context context, SuntimesTheme theme)
    {
        int[] viewID = getIconViewIDs();
        Bitmap[] bitmaps = getThemedBitmaps(context, theme);
        for (int i=0; i<viewID.length; i++)
        {
            ImageView view = (ImageView)findViewById(viewID[i]);
            if (view != null && bitmaps[i] != null) {
                view.setImageBitmap(bitmaps[i]);
            }
        }
    }

    private Bitmap[] getThemedBitmaps(Context context, @Nullable SuntimesTheme theme)
    {
        int fullColor = colors.getColor(MoonPhasesColorValues.COLOR_MOON_FULL);
        int newColor = colors.getColor(MoonPhasesColorValues.COLOR_MOON_NEW);
        int waxingColor = colors.getColor(MoonPhasesColorValues.COLOR_MOON_WANING);
        int waningColor = colors.getColor(MoonPhasesColorValues.COLOR_MOON_WANING);

        int colorWaxing = (theme != null) ? theme.getMoonWaxingColor() : waxingColor;
        int colorWaning = (theme != null) ? theme.getMoonWaningColor() : waningColor;
        int colorFull = (theme != null) ? theme.getMoonFullColor() : fullColor;
        int colorNew = (theme != null) ? theme.getMoonNewColor() : newColor;
        float stroke = (theme != null) ? theme.getMoonFullStrokePixels(context) : strokePixels;

        return new Bitmap[] {
                SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(northward), colorFull, colorWaning, (int)stroke),
                SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(northward), colorNew, colorWaxing, (int)stroke),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_CRESCENT.getIcon(northward), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(northward), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_GIBBOUS.getIcon(northward), colorWaxing, colorWaxing, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_CRESCENT.getIcon(northward), colorWaning, colorWaning, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(northward), colorWaning, colorWaning, 0),
                SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_GIBBOUS.getIcon(northward), colorWaning, colorWaning, 0)
        };
    }
    private static int[] getIconViewIDs()
    {
        return new int[] { R.id.icon_info_moonphase_full, R.id.icon_info_moonphase_new,
                R.id.icon_info_moonphase_waxing_crescent, R.id.icon_info_moonphase_waxing_quarter,
                R.id.icon_info_moonphase_waxing_gibbous, R.id.icon_info_moonphase_waning_crescent,
                R.id.icon_info_moonphase_waning_quarter, R.id.icon_info_moonphase_waning_gibbous };
    }

    private boolean tomorrowMode = false;
    public void setTomorrowMode( boolean value )
    {
        tomorrowMode = value;
    }
    public boolean isTomorrowMode()
    {
        return tomorrowMode;
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        SuntimesUtils.initDisplayStrings(context);
        MoonPhaseDisplay.initDisplayStrings(context);
    }

    public void updateViews(Context context, SuntimesMoonData data) {
        updateViews(context, data, null);
    }
    public void updateViews(Context context, SuntimesMoonData data, @Nullable Calendar dateTime)
    {
        int positionVisibility = (showPosition ? View.VISIBLE : View.GONE);
        azimuthText.setVisibility(positionVisibility);
        elevationText.setVisibility(positionVisibility);

        if (isInEditMode())
        {
            return;
        }

        this.data = data;
        if (data != null && data.isCalculated())
        {
            northward = WidgetSettings.loadLocalizeHemispherePref(context, 0) && (data.location().getLatitudeAsDouble() < 0);
            themeIcons(context, themeOverride);
            hideIcons();

            MoonPhaseDisplay phase = (dateTime != null) ? SuntimesMoonData.findCurrentPhaseOf(dateTime, data)
                                                        : (tomorrowMode ? data.getMoonPhaseTomorrow() : data.getMoonPhaseToday());

            if (phase != null)
            {
                if (phase == MoonPhaseDisplay.FULL || phase == MoonPhaseDisplay.NEW) {
                    SuntimesCalculator.MoonPhase majorPhase = (phase == MoonPhaseDisplay.FULL ? SuntimesCalculator.MoonPhase.FULL : SuntimesCalculator.MoonPhase.NEW);
                    phaseText.setText(data.getMoonPhaseLabel(context, majorPhase));
                } else phaseText.setText(phase.getLongDisplayString());

                View phaseIcon = findViewById(phase.getView());
                if (phaseIcon != null) {
                    phaseIcon.setVisibility(View.VISIBLE);
                }

                /**Integer phaseColor = phaseColors.get(phase);
                if (phaseColor != null)
                {
                    phaseText.setTextColor(phaseColor);
                }*/
            }

            updateIllumination(context, dateTime);
            updatePosition(dateTime);

        } else {
            phaseText.setText("");
            illumText.setText("");
            azimuthText.setText("");
            elevationText.setText("");
            hideIcons();
        }
    }

    private void hideIcons()
    {
        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            View view = findViewById(moonPhase.getView());
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    public void updateIllumination(Context context) {
        updateIllumination(context, null);
    }
    public void updateIllumination(Context context, @Nullable Calendar datetime)
    {
        if (data != null && data.isCalculated())
        {
            NumberFormat formatter = NumberFormat.getPercentInstance();
            formatter.setMinimumFractionDigits(0);
            formatter.setMaximumFractionDigits((illumAtNoon || illumRange ? 0 : 1));

            if (illumAtNoon)
            {
                Calendar noonToday = data.getLunarNoonToday();
                long noonTodayMillis = ((noonToday != null) ? noonToday.getTimeInMillis() : 0);
                Calendar noonTomorrow = data.getLunarNoonTomorrow();
                long noonTomorrowMillis = ((noonTomorrow != null) ? noonTomorrow.getTimeInMillis() : 0);
                boolean sharedNoon = (noonTodayMillis == noonTomorrowMillis);

                String illum, illumTime;
                if (tomorrowMode)
                {
                    illum = formatter.format(data.getMoonIlluminationTomorrow());
                    illumTime = utils.calendarTimeShortDisplayString(context, noonTomorrow).toString();

                } else {
                    illum = formatter.format(data.getMoonIlluminationToday());
                    illumTime = (sharedNoon)
                            ? utils.calendarDateTimeDisplayString(context, noonToday).toString()
                            : utils.calendarTimeShortDisplayString(context, noonToday).toString();
                }
                String illumNote = (context == null ? illum : context.getString(sharedNoon ? R.string.moon_illumination : R.string.moon_illumination_at, illum, illumTime));
                illumText.setText(SuntimesUtils.createColorSpan(null, illumNote, illum, noteColor));

            } else if (illumRange) {
                datetime = data.calendar();
                Calendar startDate = Calendar.getInstance(datetime.getTimeZone());
                startDate.setTimeInMillis(datetime.getTimeInMillis());
                startDate.set(Calendar.HOUR_OF_DAY, 0);
                startDate.set(Calendar.MINUTE, 0);
                startDate.set(Calendar.SECOND, 0);

                Calendar endDate = Calendar.getInstance(datetime.getTimeZone());
                endDate.setTimeInMillis(datetime.getTimeInMillis());
                endDate.set(Calendar.HOUR_OF_DAY, 23);
                endDate.set(Calendar.MINUTE, 59);
                endDate.set(Calendar.SECOND, 59);

                String illum1 = formatter.format(data.getMoonIllumination(startDate));
                String illum2 = formatter.format(data.getMoonIllumination(endDate));
                if (illum1.equals(illum2)) {
                    String illumNote = (context != null ? context.getString(R.string.moon_illumination_short, illum1) : illum1);
                    illumText.setText(SuntimesUtils.createColorSpan(null, illumNote, illum1, noteColor));
                } else {
                    String illumNote = (context != null ? context.getString(R.string.moon_illumination_range, illum1, illum2) : illum1+" "+illum2);
                    illumText.setText(SuntimesUtils.createColorSpan(SuntimesUtils.createColorSpan(null, illumNote, illum1, noteColor), illumNote, illum2, noteColor));
                }

            } else {
                String illum = formatter.format(datetime != null ? data.getMoonIllumination(datetime) : data.getMoonIlluminationNow());
                String illumNote = (context == null ? illum : context.getString(R.string.moon_illumination, illum));
                illumText.setText(SuntimesUtils.createColorSpan(null, illumNote, illum, noteColor));
            }

        } else {
            illumText.setText("");
        }
    }

    public void updatePosition() {
        updatePosition((data != null && data.isCalculated()) ? data.nowThen(data.calendar()) : null);
    }
    public void updatePosition(@Nullable Calendar datetime)
    {
        if (datetime != null && data != null && data.isCalculated())
        {
            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.Position position = calculator.getMoonPosition(datetime);
            updatePosition(position);

        } else {
            updatePosition((SuntimesCalculator.Position) null);
        }
    }

    public void updatePosition(@Nullable SuntimesCalculator.Position position)
    {
        if (position == null)
        {
            if (azimuthText != null)
            {
                azimuthText.setText("");
                azimuthText.setContentDescription("");
            }
            if (elevationText != null)
            {
                elevationText.setText("");
                elevationText.setContentDescription("");
            }
            return;
        }

        if (azimuthText != null)
        {
            SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(position.azimuth, 2, false);
            String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
            SpannableString azimuthSpan = SuntimesUtils.createRelativeSpan(null, azimuthString, azimuthText.getSuffix(), 0.7f);
            azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
            this.azimuthText.setText(azimuthSpan);

            SuntimesUtils.TimeDisplayText azimuthDesc = utils.formatAsDirection2(position.azimuth, 2, true);
            this.azimuthText.setContentDescription(utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
        }

        if (elevationText != null)
        {
            //int elevationColor = Color.WHITE;
            SuntimesUtils.TimeDisplayText elevationText = utils.formatAsElevation(position.elevation, 2);
            String elevationString = utils.formatAsElevation(elevationText.getValue(), elevationText.getSuffix());
            SpannableString elevationSpan = SuntimesUtils.createRelativeSpan(null, elevationString, elevationText.getSuffix(), 0.7f);
            //elevationSpan = SuntimesUtils.createColorSpan(elevationSpan, elevationString, elevationString, elevationColor);
            this.elevationText.setText(elevationSpan);
        }
    }

    public void adjustColumnWidth(int columnWidthPx)
    {
        phaseText.setMaxWidth(columnWidthPx);
    }

    /**public boolean saveState(Bundle bundle)
    {
        return true;
    }*/

    /**public void loadState(Bundle bundle) {}*/

    public void setOnClickListener( OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

}
