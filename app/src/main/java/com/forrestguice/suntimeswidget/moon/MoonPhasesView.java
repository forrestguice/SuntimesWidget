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
package com.forrestguice.suntimeswidget.moon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.settings.display.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.util.android.AndroidResources;

import java.util.ArrayList;
import java.util.Calendar;

@Deprecated
public class MoonPhasesView extends LinearLayout
{
    private final SuntimesUtils utils = new SuntimesUtils();
    private boolean isRtl = false;
    private boolean centered = false;

    private LinearLayout content;
    private PhaseField phaseNew, phaseFirst, phaseFull, phaseLast;
    private final ArrayList<PhaseField> phases = new ArrayList<>();
    private TextView empty;

    public MoonPhasesView(Context context)
    {
        super(context);
        init(context, null);
    }

    public MoonPhasesView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //applyAttributes(context, attrs);
        init(context, attrs);
    }

    /**private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EquinoxView, 0, 0);
        try {
            setMinimized(a.getBoolean(R.styleable.EquinoxView_minimized, false));
        } finally {
            a.recycle();
        }
    }*/

    private void init(Context context, AttributeSet attrs)
    {
        initLocale(context);
        themeViews(context);
        LayoutInflater.from(context).inflate(R.layout.layout_view_moonphases, this, true);

        if (attrs != null)
        {
            LinearLayout.LayoutParams lp = generateLayoutParams(attrs);
            centered = ((lp.gravity == Gravity.CENTER) || (lp.gravity == Gravity.CENTER_HORIZONTAL));
        }

        empty = (TextView)findViewById(R.id.txt_empty);
        content = (LinearLayout)findViewById(R.id.moonphases_layout);

        phases.add( phaseNew = new PhaseField(this, R.id.moonphase_new_layout, R.id.moonphase_new_label, R.id.moonphase_new_date, R.id.moonphase_new_note, R.id.moonphase_new_icon) );
        phases.add( phaseFirst = new PhaseField(this, R.id.moonphase_firstquarter_layout, R.id.moonphase_firstquarter_label, R.id.moonphase_firstquarter_date, R.id.moonphase_firstquarter_note, R.id.moonphase_firstquarter_icon) );
        phases.add( phaseFull = new PhaseField(this, R.id.moonphase_full_layout, R.id.moonphase_full_label, R.id.moonphase_full_date, R.id.moonphase_full_note, R.id.moonphase_full_icon) );
        phases.add( phaseLast = new PhaseField(this, R.id.moonphase_thirdquarter_layout, R.id.moonphase_thirdquarter_label, R.id.moonphase_thirdquarter_date, R.id.moonphase_thirdquarter_note, R.id.moonphase_thirdquarter_icon) );

        if (isInEditMode())
        {
            updateViews(context, null);
        }
    }

    private int noteColor;
    private void themeViews(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        typedArray.recycle();
    }

    public void themeViews(Context context, SuntimesTheme theme)
    {
        noteColor = theme.getTimeColor();

        int colorTitle = theme.getTitleColor();
        int colorTime = theme.getTimeColor();
        int colorText = theme.getTextColor();
        int colorWaxing = theme.getMoonWaxingColor();
        int colorWaning = theme.getMoonWaningColor();
        int colorFull = theme.getMoonFullColor();
        int colorNew = theme.getMoonNewColor();

        Bitmap fullMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, theme.getMoonFullStrokePixels(context));
        Bitmap newMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, theme.getMoonNewStrokePixels(context));
        Bitmap waxingQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0);
        Bitmap waningQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0);

        phaseNew.themeViews(colorTitle, colorTime, colorText, newMoon);
        phaseFirst.themeViews(colorTitle, colorTime, colorText, waxingQuarter);
        phaseFull.themeViews(colorTitle, colorTime, colorText, fullMoon);
        phaseLast.themeViews(colorTitle, colorTime, colorText, waningQuarter);
    }

    public void initLocale(Context context)
    {
        isRtl = AppSettings.isLocaleRtl(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings_MoonPhaseMode(context);
        MoonPhaseDisplay.initDisplayStrings(AndroidResources.wrap(context));
    }

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        content.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected void updateViews( Context context, SuntimesMoonData data )
    {
        for (PhaseField phase : phases) {
            phase.showLabel(true);
        }

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
            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

            Calendar newMoonDate = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW);
            Calendar firstQuarterDate = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER);
            Calendar fullMoonDate = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL);
            Calendar thirdQuarterDate = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER);

            SuntimesCalculator.MoonPosition newMoonPosition = data.calculator().getMoonPosition(newMoonDate);
            SuntimesCalculator.MoonPosition fullMoonPosition = data.calculator().getMoonPosition(fullMoonDate);

            phaseNew.updateField(context, data.now(), newMoonDate, showWeeks, showTime, showHours, showSeconds);
            phaseFirst.updateField(context, data.now(), firstQuarterDate, showWeeks, showTime, showHours, showSeconds);
            phaseFull.updateField(context, data.now(), fullMoonDate, showWeeks, showTime, showHours, showSeconds);
            phaseLast.updateField(context, data.now(), thirdQuarterDate, showWeeks, showTime, showHours, showSeconds);

            if (newMoonPosition != null) {
                if (SuntimesMoonData.isSuperMoon(newMoonPosition)) {
                    phaseNew.setLabel(context.getString(R.string.timeMode_moon_supernew));
                } else if (SuntimesMoonData.isMicroMoon(newMoonPosition)) {
                    phaseNew.setLabel(context.getString(R.string.timeMode_moon_micronew));
                } else phaseNew.setLabel(context.getString(R.string.timeMode_moon_new));
            } else phaseNew.setLabel(context.getString(R.string.timeMode_moon_new));

            if (fullMoonPosition != null) {
                if (SuntimesMoonData.isSuperMoon(fullMoonPosition)) {
                    phaseFull.setLabel(context.getString(R.string.timeMode_moon_superfull));
                } else if (SuntimesMoonData.isMicroMoon(fullMoonPosition)) {
                    phaseFull.setLabel(context.getString(R.string.timeMode_moon_microfull));
                } else phaseFull.setLabel(context.getString(R.string.timeMode_moon_full));
            } else phaseFull.setLabel(context.getString(R.string.timeMode_moon_full));

            reorderLayout(data.nextPhase(data.midnight()));

        } else {
            showEmptyView(true);
        }
    }

    private void clearLayout()
    {
        for (PhaseField phase : phases) {
            phase.removeFromLayout(content);
        }
    }

    private void reorderLayout( SuntimesCalculator.MoonPhase nextPhase )
    {
        clearLayout();
        switch (nextPhase)
        {
            case THIRD_QUARTER:
                phaseLast.addToLayout(content);
                phaseNew.addToLayout(content);
                phaseFirst.addToLayout(content);
                phaseFull.addToLayout(content);
                break;

            case FULL:
                phaseFull.addToLayout(content);
                phaseLast.addToLayout(content);
                phaseNew.addToLayout(content);
                phaseFirst.addToLayout(content);
                break;

            case FIRST_QUARTER:
                phaseFirst.addToLayout(content);
                phaseFull.addToLayout(content);
                phaseLast.addToLayout(content);
                phaseNew.addToLayout(content);
                break;

            case NEW:
            default:
                phaseNew.addToLayout(content);
                phaseFirst.addToLayout(content);
                phaseFull.addToLayout(content);
                phaseLast.addToLayout(content);
                break;
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

    public void setOnClickListener( View.OnClickListener listener )
    {
        content.setOnClickListener(listener);
    }

    public void setOnLongClickListener( View.OnLongClickListener listener)
    {
        content.setOnLongClickListener(listener);
    }

    /**
     * PhaseField
     */
    private class PhaseField
    {
        public View layout;
        public TextView field;
        public TextView note;
        public TextView label;
        public ImageView icon;

        public PhaseField(@NonNull View parent, int layoutID, int labelID, int dateTextID, int noteTextID, int imageViewID)
        {
            layout = parent.findViewById(layoutID);
            label = (TextView)parent.findViewById(labelID);
            field = (TextView)parent.findViewById(dateTextID);
            note = (TextView)parent.findViewById(noteTextID);
            icon = (ImageView)parent.findViewById(imageViewID);
        }

        public void themeViews(int labelColor, int timeColor, int textColor, @NonNull Bitmap bitmap)
        {
            label.setTextColor(labelColor);
            field.setTextColor(timeColor);
            note.setTextColor(textColor);
            icon.setImageBitmap(bitmap);
        }

        public void updateField(Context context, Calendar now, Calendar dateTime, boolean showWeeks, boolean showTime, boolean showHours, boolean showSeconds)
        {
            if (field != null)
            {
                field.setText(utils.calendarDateTimeDisplayString(context, dateTime, showTime, showSeconds).getValue());
            }

            if (note != null)
            {
                String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
                String noteString = now.after(dateTime) ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
                note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor));
                note.setVisibility(View.VISIBLE);
            }
        }

        public void setLabel(CharSequence text)
        {
            label.setText(text);
        }

        public void showLabel(boolean value)
        {
            label.setVisibility(value ? View.VISIBLE : View.GONE);
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
