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
package com.forrestguice.suntimeswidget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.NumberFormat;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonRiseSetView extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;

    private LinearLayout content;
    private MoonRiseSetField risingTextField, settingTextField;
    private MoonRiseSetField risingTextField1, settingTextField1;
    private View divider;
    private TextView empty;

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
        /**TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }*/
    }

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        initColors(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonriseset, this, true);

        if (attrs != null)
        {
            LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);
        content = (LinearLayout)findViewById(R.id.moonriseset_layout);
        risingTextField = new MoonRiseSetField(R.id.moonrise_layout , R.id.text_time_moonrise);
        settingTextField = new MoonRiseSetField(R.id.moonset_layout, R.id.text_time_moonset);
        risingTextField1 = new MoonRiseSetField(R.id.moonrise_layout1 , R.id.text_time_moonrise1);
        settingTextField1 = new MoonRiseSetField(R.id.moonset_layout1, R.id.text_time_moonset1);
        divider = findViewById(R.id.divider_moon1);

        if (isInEditMode())
        {
            updateViews(context, null);
        }
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

    private boolean showExtraField = true;
    public void setShowExtraField( boolean value )
    {
        showExtraField = value;
    }
    public boolean showExtraField()
    {
        return showExtraField;
    }

    private int noteColor;
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary }; //, R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        content.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesMoonData data )
    {
        if (isInEditMode())
        {
            return;
        }

        if (data == null)
        {
            return;
        }

        if (data.isCalculated())
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

            if (tomorrowMode)
                reorderLayout(risingTime1, settingTime1);
            else reorderLayout(risingTime, settingTime);

        } else {
            showEmptyView(true);
        }
    }

    public boolean saveState(Bundle bundle)
    {
        //bundle.putBoolean(MoonPhaseView.KEY_UI_MINIMIZED, minimized);
        return true;
    }

    public void loadState(Bundle bundle)
    {
        //minimized = bundle.getBoolean(MoonPhaseView.KEY_UI_MINIMIZED, minimized);
    }

    public void setOnClickListener( OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

    private void clearLayout()
    {
        risingTextField.removeFromLayout(content);
        settingTextField.removeFromLayout(content);
        risingTextField1.removeFromLayout(content);
        settingTextField1.removeFromLayout(content);

        if (divider != null) {
            content.removeView(divider);
        }
    }

    private void reorderLayout( Calendar riseTime, Calendar setTime )
    {
        clearLayout();
        updateMargins(getContext());
        if (tomorrowMode)
        {
            if (riseTime.before(setTime))
                addLayout0(settingTextField, risingTextField1, settingTextField1);
            else addLayout0(risingTextField, settingTextField1, risingTextField1);

        } else {
            if (riseTime.before(setTime))
                addLayout1(risingTextField, settingTextField, risingTextField1);
            else addLayout1(settingTextField, risingTextField, settingTextField1);
        }
    }

    private void addLayout0(MoonRiseSetField field1, MoonRiseSetField field2, MoonRiseSetField field3)
    {
        if (showExtraField)
        {
            field1.addToLayout(content);
            if (divider != null) {
                content.addView(divider);
            }
        }
        field2.addToLayout(content);
        field3.addToLayout(content);
    }

    private void addLayout1(MoonRiseSetField field1, MoonRiseSetField field2, MoonRiseSetField field3)
    {
        field1.addToLayout(content);
        field2.addToLayout(content);
        if (showExtraField)
        {
            if (divider != null) {
                content.addView(divider);
            }
            field3.addToLayout(content);
        }
    }

    private void updateMargins(Context context)
    {
        if (context != null && showExtraField)
        {
            int margins = SuntimesUtils.dpToPixels(context, getResources().getDimension(R.dimen.table_moon_startEndMargin));
            risingTextField.setMarginStartEnd(margins, margins);
            risingTextField1.setMarginStartEnd(margins, margins);
            settingTextField.setMarginStartEnd(margins, margins);
            settingTextField1.setMarginStartEnd(margins, margins);
        }
    }

    /**
     * MoonRiseSetField
     */
    private class MoonRiseSetField
    {
        protected View layout;
        protected TextView textView;

        public MoonRiseSetField(int layoutID, int textViewID)
        {
            layout = findViewById(layoutID);
            textView = (TextView)findViewById(textViewID);
        }

        public void updateField(Context context, Calendar dateTime, boolean showSeconds)
        {
            SuntimesUtils.TimeDisplayText text = utils.calendarTimeShortDisplayString(context, dateTime, showSeconds);
            textView.setText(text.toString());
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
                parent.addView(layout);
        }

        public void removeFromLayout(@NonNull LinearLayout parent)
        {
            if (layout != null)
                parent.removeView(layout);
        }

    }
}
