/**
    Copyright (C) 2018-2021 Forrest Guice
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonRiseSetColorValues;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonRiseSetView extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();
    //private boolean isRtl = false;
    //private boolean centered = false;
    private boolean showPosition = false;

    private LinearLayout content;
    private MoonRiseSetField risingTextField, settingTextField;
    private MoonRiseSetField risingTextField1, settingTextField1;
    private ArrayList<MoonRiseSetField> f = new ArrayList<>();
    private View divider;

    protected MoonRiseSetColorValues colors;

    public MoonRiseSetView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonRiseSetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonRiseSetView, 0, 0);
        try {
            setShowPosition(a.getBoolean(R.styleable.MoonRiseSetView_showPosition, false));
        } finally {
            a.recycle();
        }
    }

    public TextView[] getTimeViews(SolarEvents event)
    {
        switch (event)
        {
            case MOONRISE:
                return new TextView[] {risingTextField.getTimeView(), risingTextField1.getTimeView()};

            case MOONSET:
            default:
                return new TextView[] {settingTextField.getTimeView(), settingTextField1.getTimeView()};
        }

    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        initColors(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonriseset, this, true);

        //if (attrs != null)
        //{
            //LayoutParams lp = generateLayoutParams(attrs);
            //centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        //}

        content = (LinearLayout)findViewById(R.id.moonriseset_layout);

        f.clear();
        f.add(risingTextField = new MoonRiseSetField(R.id.moonrise_layout, R.id.icon_time_moonrise, R.id.text_time_moonrise, R.id.text_info_moonrise, true));
        f.add(settingTextField = new MoonRiseSetField(R.id.moonset_layout, R.id.icon_time_moonset, R.id.text_time_moonset, R.id.text_info_moonset, false));
        f.add(risingTextField1 = new MoonRiseSetField(R.id.moonrise_layout1, R.id.icon_time_moonrise1, R.id.text_time_moonrise1, R.id.text_info_moonrise1, true));
        f.add(settingTextField1 = new MoonRiseSetField(R.id.moonset_layout1, R.id.icon_time_moonset1, R.id.text_time_moonset1, R.id.text_info_moonset1, false));

        divider = findViewById(R.id.divider_moon1);

        if (isInEditMode())
        {
            updateViews(context, null);
        }
        themeViews(context, colors);
    }

    private boolean tomorrowMode = false;
    public void setTomorrowMode( boolean value )
    {
        tomorrowMode = value;
    }
    /**public boolean isTomorrowMode()
    {
        return tomorrowMode;
    }*/

    private boolean showExtraField = true;
    public void setShowExtraField( boolean value )
    {
        showExtraField = value;
    }
    /**public boolean showExtraField()
    {
        return showExtraField;
    }*/

    public void initColors(Context context)
    {
        colors = new MoonRiseSetColorValues(context);
        /*int[] colorAttrs = { android.R.attr.textColorPrimary }; //, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();*/
    }
    public void setColors(Context context, @Nullable ColorValues values)
    {
        if (values != null) {
            colors = new MoonRiseSetColorValues(values);
        } else {
            initColors(context);
        }
        themeViews(context, values);
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        //isRtl = AppSettings.isLocaleRtl(context);
    }

    public void themeViews(Context context, @Nullable ColorValues colors)
    {
        if (colors != null) {
            for (MoonRiseSetField field : f) {
                field.themeView(context, colors);
            }
        }
    }
    @Deprecated
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            for (MoonRiseSetField field : f) {
                field.themeView(theme);
            }
        }
    }

    public void updateViews(Context context, SuntimesMoonData data)
    {
        if (isInEditMode())
        {
            return;
        }

        if (data != null && data.isCalculated())
        {
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

            Calendar risingTime = data.moonriseCalendarToday();
            Calendar settingTime = data.moonsetCalendarToday();
            risingTextField.updateField(context, risingTime, showSeconds);
            settingTextField.updateField(context, settingTime, showSeconds);

            Calendar risingTime1 = data.moonriseCalendarTomorrow();
            Calendar settingTime1 = data.moonsetCalendarTomorrow();
            risingTextField1.updateField(context, risingTime1, showSeconds);
            settingTextField1.updateField(context, settingTime1, showSeconds);

            if (showPosition)
            {
                SuntimesCalculator calculator = data.calculator();

                SuntimesCalculator.MoonPosition moonPositionRising = (risingTime == null ? null : calculator.getMoonPosition(risingTime));
                SuntimesCalculator.MoonPosition moonPositionSetting = (settingTime == null ? null : calculator.getMoonPosition(settingTime));
                risingTextField.updateField(context, moonPositionRising);
                settingTextField.updateField(context, moonPositionSetting);

                SuntimesCalculator.MoonPosition moonPositionRising1 = (risingTime1 == null ? null : calculator.getMoonPosition(risingTime1));
                SuntimesCalculator.MoonPosition moonPositionSetting1 = (settingTime1 == null ? null : calculator.getMoonPosition(settingTime1));
                risingTextField1.updateField(context, moonPositionRising1);
                settingTextField1.updateField(context, moonPositionSetting1);

                setShowPosition(showPosition);
            }
            reorderLayout(risingTime, settingTime, risingTime1, settingTime1);

        } else {
            clearLayout();
        }
    }

    /**public boolean saveState(Bundle bundle)
    {
        //bundle.putBoolean(MoonPhaseView.KEY_UI_MINIMIZED, minimized);
        return true;
    }*/

    /**public void loadState(Bundle bundle)
    {
        //minimized = bundle.getBoolean(MoonPhaseView.KEY_UI_MINIMIZED, minimized);
    }*/

    public void setOnClickListener( OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

    private void setShowPosition(boolean value)
    {
        showPosition = value;
        for (MoonRiseSetField field : f)
        {
            if (field != null)
            {
                field.setShowPosition(showPosition);
            }
        }
    }

    private void clearLayout()
    {
        for (MoonRiseSetField field : f)
        {
            field.removeFromLayout(content);
        }

        if (divider != null)
        {
            divider.setVisibility(View.GONE);
            content.removeView(divider);
        }
    }

    private Calendar midday(@NonNull Calendar other)
    {
        Calendar midday = (Calendar)other.clone();
        midday.set(Calendar.HOUR_OF_DAY, 12);
        midday.set(Calendar.MINUTE, 0);
        midday.set(Calendar.SECOND, 0);
        return midday;
    }

    private void reorderLayout( Calendar rising0, Calendar setting0, Calendar rising1, Calendar setting1 )
    {
        clearLayout();

        MoonRiseSetFieldLayoutSet fields = determineLayout(rising0, setting0, rising1, setting1);
        updateMargins(getContext(), fields);

        if (fields.tomorrowMode)
            addLayout0(fields);
        else addLayout1(fields);
    }

    @SuppressWarnings("ConstantConditions")
    private MoonRiseSetFieldLayoutSet determineLayout(Calendar rising0, Calendar setting0, Calendar rising1, Calendar setting1 )
    {
        if (tomorrowMode)
        {
            if (rising1 == null && setting1 == null)
            {   // special case: no rise or set
                return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField1, settingTextField1, tomorrowMode);

            } else if (setting1 == null) {                                           // special case: no set time
                if (rising1.before(midday(rising1)))
                    return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField1, settingTextField1, tomorrowMode);   // i.e. set | rise none
                else return new MoonRiseSetFieldLayoutSet(settingTextField, settingTextField1, risingTextField1, tomorrowMode);  // i.e. set | none rise

            } else if (rising1 == null) {                                            // special case: no rise time
                if (setting1.before(midday(setting1)))
                    return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField1, risingTextField1, tomorrowMode);    // i.e. rise | set none
                else return new MoonRiseSetFieldLayoutSet(risingTextField, risingTextField1, settingTextField1, tomorrowMode);   // i.e. rise | none set

            } else {
                if (rising1.before(setting1))
                    return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField1, settingTextField1, tomorrowMode);   // i.e. set  | rise set
                else return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField1, risingTextField1, tomorrowMode);   // i.e. rise | set rise
            }

        } else {
            if (rising0 == null && setting0 == null)
            {   // special case: no rise or set
                return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField, risingTextField1, tomorrowMode);

            } else if (setting0 == null) {                                           // special case: no set time
                if (rising0.before(midday(rising0)))
                    return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField, settingTextField1, tomorrowMode);    // i.e. rise none | set
                else return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField, settingTextField1, tomorrowMode);   // i.e. none rise | set

            } else if (rising0 == null) {                                            // special case: no rise time
                if (setting0.before(midday(setting0)))
                    return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField, risingTextField1, tomorrowMode);     // i.e. set none | rise
                else return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField, risingTextField1, tomorrowMode);    // i.e. none set | rise

            } else {
                if (rising0.before(setting0))
                    return new MoonRiseSetFieldLayoutSet(risingTextField, settingTextField, risingTextField1, tomorrowMode);     // i.e. rise set | rise
                else return new MoonRiseSetFieldLayoutSet(settingTextField, risingTextField, settingTextField1, tomorrowMode);   // i.e. set rise | set
            }
        }
    }

    private void addLayout0(MoonRiseSetFieldLayoutSet fields)
    {
        if (showExtraField)
        {
            fields.field1.addToLayout(content);
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
                content.addView(divider);
            }
        }
        fields.field2.addToLayout(content);
        fields.field3.addToLayout(content);
    }

    private void addLayout1(MoonRiseSetFieldLayoutSet fields)
    {
        fields.field1.addToLayout(content);
        fields.field2.addToLayout(content);
        if (showExtraField)
        {
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
                content.addView(divider);
            }
            fields.field3.addToLayout(content);
        }
    }

    private void updateMargins(Context context, MoonRiseSetFieldLayoutSet fields)
    {
        int defaultMargin = getResources().getDimensionPixelSize(R.dimen.table_moon_startEndMargin);

        if (context != null && showExtraField)
        {
            for (MoonRiseSetField field : f)
            {
                field.setMarginStartEnd(defaultMargin, defaultMargin);
            }

        } else {
            MoonRiseSetField startField, endField;
            if (fields.tomorrowMode)
            {
                startField = fields.field2;
                endField = fields.field3;
            } else {
                startField = fields.field1;
                endField = fields.field2;
            }
            TextView v = endField.getTimeView();
            v.measure(0, 0);

            int margin = getResources().getDimensionPixelSize(R.dimen.table_set_leftMargin);
            margin -= (v.getMeasuredWidth() - matchColumnWidthPx);

            for (MoonRiseSetField field : f)
            {
                int startMargin = (((field == startField) ? defaultMargin : (field == endField) ? margin : 0));
                field.setMarginStartEnd( startMargin, 0);
            }
        }
    }

    private int matchColumnWidthPx = 0;
    public void adjustColumnWidth(Context context, int columnWidthPx)
    {
        this.matchColumnWidthPx = columnWidthPx;
    }

    /**
     * MoonRiseSetFieldLayoutSet
     */
    private class MoonRiseSetFieldLayoutSet
    {
        public boolean tomorrowMode = false;
        public MoonRiseSetField field1, field2, field3;
        public MoonRiseSetFieldLayoutSet(MoonRiseSetField field1, MoonRiseSetField field2, MoonRiseSetField field3, boolean tomorrowMode)
        {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.tomorrowMode = tomorrowMode;
        }
    }

    /**
     * MoonRiseSetField
     */
    private class MoonRiseSetField
    {
        protected View layout;
        protected ImageView iconView;
        protected TextView timeView;
        protected TextView positionView;
        protected boolean rising;

        public MoonRiseSetField(int layoutID, int iconViewID, int timeViewID, int positionViewID, boolean risingState)
        {
            layout = findViewById(layoutID);
            iconView = (ImageView)findViewById(iconViewID);
            timeView = (TextView)findViewById(timeViewID);
            positionView = (TextView)findViewById(positionViewID);
            rising = risingState;
        }

        public void themeView(Context context, @NonNull ColorValues colors)
        {
            int textColor = (rising ? colors.getColor(MoonRiseSetColorValues.COLOR_RISING_MOON_TEXT)
                                    : colors.getColor(MoonRiseSetColorValues.COLOR_SETTING_MOON_TEXT));
            timeView.setTextColor(textColor);
            positionView.setTextColor(textColor);

            int iconColor = (rising ? colors.getColor(MoonRiseSetColorValues.COLOR_RISING_MOON)
                                    : colors.getColor(MoonRiseSetColorValues.COLOR_SETTING_MOON));
            SuntimesUtils.tintDrawable((LayerDrawable)iconView.getBackground(), iconColor, iconColor, 0);
        }

        public void themeView(@NonNull SuntimesTheme theme)
        {
            int color = (rising ? theme.getMoonriseTextColor() : theme.getMoonsetTextColor());
            timeView.setTextColor(color);
            timeView.setTextSize(theme.getTimeSizeSp());
            timeView.setTypeface(timeView.getTypeface(), theme.getTimeBold() ? Typeface.BOLD : Typeface.NORMAL);
            positionView.setTextColor(color);
            positionView.setTextSize(theme.getTimeSuffixSizeSp());
            SuntimesUtils.tintDrawable((LayerDrawable)iconView.getBackground(), color, color, 0);
        }

        public void updateField(String timeText)
        {
            timeView.setText(timeText);
        }

        public void updateField(Context context, Calendar dateTime, boolean showSeconds)
        {
            SuntimesUtils.TimeDisplayText text = utils.calendarTimeShortDisplayString(context, dateTime, showSeconds);
            timeView.setText(text.toString());
        }

        public TextView getTimeView()
        {
            return timeView;
        }

        public void updateField(Context context, SuntimesCalculator.Position position)
        {
            if (position == null)
            {
                positionView.setText("");
                positionView.setContentDescription("");

            } else {
                SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(position.azimuth, 1, false);
                String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
                SpannableString azimuthSpan = SuntimesUtils.createRelativeSpan(null, azimuthString, azimuthText.getSuffix(), 0.7f);
                azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
                positionView.setText(azimuthSpan);

                SuntimesUtils.TimeDisplayText azimuthDesc = utils.formatAsDirection2(position.azimuth, 1, true);
                positionView.setContentDescription(utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
            }
        }

        public void setShowPosition( boolean value )
        {
            positionView.setVisibility((value ? View.VISIBLE : View.GONE));
        }

        public void setMarginStartEnd( int startMargin, int endMargin )
        {
            if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
                if (Build.VERSION.SDK_INT < 17)
                {
                    params.setMargins(startMargin, 0, endMargin, 0);

                } else if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    params.setMarginStart(startMargin);
                    params.setMarginEnd(endMargin);
                }
                layout.setLayoutParams(params);
                layout.requestLayout();
            }
        }

        public void addToLayout(@NonNull LinearLayout parent)
        {
            if (layout != null)
            {
                layout.setVisibility(View.VISIBLE);
                parent.addView(layout);
            }
        }

        public void removeFromLayout(@NonNull LinearLayout parent)
        {
            if (layout != null)
            {
                parent.removeView(layout);
                layout.setVisibility(View.GONE);
            }
        }

    }
}
