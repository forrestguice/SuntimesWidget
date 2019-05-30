/**
    Copyright (C) 2019 Forrest Guice
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
package com.forrestguice.suntimeswidget.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class MoonApsisView extends LinearLayout
{
    private SuntimesUtils utils = new SuntimesUtils();

    private LinearLayout content;
    private MoonApsisField perigeeField, apogeeField;
    private ArrayList<MoonApsisField> f = new ArrayList<>();

    public MoonApsisView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonApsisView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        SuntimesUtils.initDisplayStrings(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonapsis, this, true);
        content = (LinearLayout)findViewById(R.id.moonapsis_layout);

        f.clear();
        f.add(perigeeField = new MoonApsisField(R.id.moonapsis_perigee_layout, R.id.moonapsis_perigee_label,  R.id.moonapsis_perigee_date, R.id.moonapsis_perigee_distance, R.id.moonapsis_perigee_note, true));
        f.add(apogeeField = new MoonApsisField(R.id.moonapsis_apogee_layout, R.id.moonapsis_apogee_label, R.id.moonapsis_apogee_date, R.id.moonapsis_apogee_distance, R.id.moonapsis_apogee_note, false));
        themeViews(context);

        if (isInEditMode()) {
            updateViews(context, null);
        }
    }

    public void themeViews(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        int timeColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();

        for (MoonApsisField field : f) {
            field.themeView(timeColor);
        }
    }

    public void themeViews(Context context, SuntimesTheme theme)
    {
        for (MoonApsisField field : f) {
            field.themeView(theme);
        }
    }

    public void updateViews( Context context, SuntimesMoonData data )
    {
        if (isInEditMode()) {
            return;
        }

        clearLayout();
        if (data != null && data.isCalculated())
        {
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

            Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = data.getMoonPerigee();
            perigeeField.updateField(context, perigee, showTime, showWeeks, showHours, showSeconds, units);

            Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = data.getMoonApogee();
            apogeeField.updateField(context, apogee, showTime, showWeeks, showHours, showSeconds, units);

            if (perigee.first != null)    // reorder layouts
            {
                if (perigee.first.before(apogee.first))
                {
                    perigeeField.addToLayout(content);
                    apogeeField.addToLayout(content);
                    isRising = false;

                } else {
                    apogeeField.addToLayout(content);
                    perigeeField.addToLayout(content);
                    isRising = true;
                }
            }
        }
    }

    private boolean isRising = true;
    public boolean isRising() {
        return isRising;
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
        for (MoonApsisField field : f) {
            field.removeFromLayout(content);
        }
    }

    private CharSequence createApsisNote(Context context, Calendar dateTime, boolean showWeeks, boolean showHours, int noteColor)
    {
        Calendar now = Calendar.getInstance();
        String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
        String noteString = now.after(dateTime) ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
        return SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor);
    }

    /**
     * MoonApsisField
     */
    private class MoonApsisField
    {
        protected View layout;
        protected TextView labelView;
        protected TextView timeView;
        protected TextView positionView;
        protected TextView noteView;
        protected boolean isRising = true;

        protected int timeColor = Color.WHITE;

        public MoonApsisField(int layoutID, int labelViewID, int timeViewID, int positionViewID, int noteViewID, boolean isRising)
        {
            layout = findViewById(layoutID);
            labelView = (TextView)findViewById(labelViewID);
            timeView = (TextView)findViewById(timeViewID);
            positionView = (TextView)findViewById(positionViewID);
            noteView = (TextView)findViewById(noteViewID);
            this.isRising = isRising;
        }

        public void themeView(int timeColor) {
            this.timeColor = timeColor;
        }

        public void themeView(SuntimesTheme theme)
        {
            timeColor = theme.getTimeColor();
            timeView.setTextColor(timeColor);
            positionView.setTextColor(isRising ? theme.getMoonriseTextColor() : theme.getMoonsetTextColor());
            noteView.setTextColor(theme.getTextColor());
            labelView.setTextColor(theme.getTitleColor());
        }

        public void updateField(Context context, Pair<Calendar,SuntimesCalculator.MoonPosition> apsis, boolean showTime, boolean showWeeks, boolean showHours, boolean showSeconds, WidgetSettings.LengthUnit units)
        {
            if (apsis != null)
            {
                timeView.setText(utils.calendarDateTimeDisplayString(context, apsis.first, showTime, showSeconds).getValue());
                noteView.setText(createApsisNote(context, apsis.first, showWeeks, showHours, timeColor));
                positionView.setText(SuntimesUtils.formatAsDistance(context, apsis.second.distance, units, 2, true).toString());

                timeView.setVisibility(View.VISIBLE);
                noteView.setVisibility(View.VISIBLE);
                positionView.setVisibility(View.VISIBLE);
                labelView.setVisibility(View.VISIBLE);

            } else {
                timeView.setVisibility(View.GONE);
                noteView.setVisibility(View.GONE);
                positionView.setVisibility(View.GONE);
                labelView.setVisibility(View.GONE);
            }
        }

        public void setMarginStartEnd( int startMargin, int endMargin )
        {
            if (layout.getLayoutParams() instanceof MarginLayoutParams)
            {
                MarginLayoutParams params = (MarginLayoutParams) layout.getLayoutParams();
                if (Build.VERSION.SDK_INT < 17)
                {
                    params.setMargins(startMargin, 0, endMargin, 0);

                } else if (layout.getLayoutParams() instanceof MarginLayoutParams) {
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
