/**
    Copyright (C) 2018-2019 Forrest Guice
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

public class MoonDialog extends DialogFragment
{
    private SuntimesUtils utils = new SuntimesUtils();

    private SuntimesMoonData data;
    public void setData( SuntimesMoonData data )
    {
        if (data != null && !data.isCalculated() && data.isImplemented())
        {
            data.calculate();
        }
        this.data = data;
    }

    private TextView dialogTitle;
    private MoonRiseSetView moonriseset;
    private MoonPhaseView currentphase;
    private MoonPhasesView moonphases;

    private TextView moondistance, moondistance_label, moondistance_note;
    private TextView apogee_label, perigee_label;
    private TextView apogee_date, perigee_date;
    private TextView apogee_note, perigee_note;
    private TextView apogee_distance, perigee_distance;

    private int riseColor, setColor, timeColor;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();

        final ViewGroup viewGroup = null;
        View dialogContent = inflater.inflate(R.layout.layout_dialog_moon, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        initViews(dialogContent);
        if (savedInstanceState != null)
        {
            Log.d("DEBUG", "MoonDialog onCreate (restoreState)");
            //currentphase.loadState(savedInstanceState);
            //moonphases.loadState(savedInstanceState);
            //moonriseset.loadState(savedInstanceState);
        }

        dialog.setOnShowListener(onShowListener);
        themeViews(getContext());
        return dialog;
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface)
        {
            Context context = getContext();
            if (context != null) {
                updateViews();
            }
            startUpdateTask();
        }
    };

    public void initViews(View dialogView)
    {
        dialogTitle = (TextView) dialogView.findViewById(R.id.moondialog_title);
        moonriseset = (MoonRiseSetView) dialogView.findViewById(R.id.moonriseset_view);
        currentphase = (MoonPhaseView) dialogView.findViewById(R.id.moonphase_view);
        moonphases = (MoonPhasesView) dialogView.findViewById(R.id.moonphases_view);

        moondistance = (TextView) dialogView.findViewById(R.id.moonapsis_current_distance);
        moondistance_label = (TextView) dialogView.findViewById(R.id.moonapsis_current_label);
        moondistance_note = (TextView) dialogView.findViewById(R.id.moonapsis_current_note);
        apogee_date = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_date);
        apogee_note = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_note);
        apogee_distance = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_distance);
        apogee_label = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_label);
        perigee_date = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_date);
        perigee_note = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_note);
        perigee_distance = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_distance);
        perigee_label = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_label);

        Context context = dialogView.getContext();
        if (context != null) {
            currentphase.adjustColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.moonphase_column0_width));
        }
    }

    public void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            int titleColor = themeOverride.getTitleColor();
            timeColor = themeOverride.getTimeColor();
            int textColor = themeOverride.getTextColor();
            riseColor = themeOverride.getMoonriseTextColor();
            setColor = themeOverride.getMoonsetTextColor();

            dialogTitle.setTextColor(titleColor);
            moonriseset.themeViews(context, themeOverride);
            currentphase.themeViews(context, themeOverride);
            moonphases.themeViews(context, themeOverride);

            moondistance_label.setTextColor(titleColor);
            apogee_label.setTextColor(titleColor);
            perigee_label.setTextColor(titleColor);

            moondistance.setTextColor(textColor);
            apogee_distance.setTextColor(setColor);
            perigee_distance.setTextColor(riseColor);

            moondistance_note.setTextColor(timeColor);
            apogee_date.setTextColor(timeColor);
            perigee_date.setTextColor(timeColor);

            apogee_note.setTextColor(textColor);
            perigee_note.setTextColor(textColor);

        } else {
            int[] colorAttrs = { android.R.attr.textColorPrimary };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int def = R.color.transparent;
            timeColor = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
            typedArray.recycle();
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (moonriseset != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        stopUpdateTask();
        Context context = getContext();
        moonriseset.updateViews(context, data);
        currentphase.updateViews(context, data);
        moonphases.updateViews(context, data);
        updateMoonApsis();
        startUpdateTask();
    }

    private void updateMoonApsis()
    {
        Context context = getContext();
        if (context != null && data != null && data.isCalculated())
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, 0);
            boolean showHours = WidgetSettings.loadShowHoursPref(context, 0);

            Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = data.getMoonApogee();
            if (apogee != null)
            {
                apogee_date.setText(utils.calendarDateTimeDisplayString(context, apogee.first, showTime, showSeconds).getValue());
                apogee_note.setText(createApsisNote(context, apogee.first, showWeeks, showHours, timeColor));
                apogee_distance.setText(SuntimesUtils.formatAsDistance(context, apogee.second.distance, units, 2, true).toString());

                apogee_date.setVisibility(View.VISIBLE);
                apogee_note.setVisibility(View.VISIBLE);
                apogee_distance.setVisibility(View.VISIBLE);
                apogee_label.setVisibility(View.VISIBLE);

            } else {
                apogee_date.setVisibility(View.GONE);
                apogee_note.setVisibility(View.GONE);
                apogee_distance.setVisibility(View.GONE);
                apogee_label.setVisibility(View.GONE);
            }

            Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = data.getMoonPerigee();
            if (perigee != null)
            {
                perigee_date.setText(utils.calendarDateTimeDisplayString(context, perigee.first, showTime, showSeconds).getValue());
                perigee_note.setText(createApsisNote(context, perigee.first, showWeeks, showHours, timeColor));
                perigee_distance.setText(SuntimesUtils.formatAsDistance(context, perigee.second.distance, units, 2, true).toString());

                perigee_date.setVisibility(View.VISIBLE);
                perigee_note.setVisibility(View.VISIBLE);
                perigee_distance.setVisibility(View.VISIBLE);
                perigee_label.setVisibility(View.VISIBLE);

            } else {
                perigee_date.setVisibility(View.GONE);
                perigee_note.setVisibility(View.GONE);
                perigee_distance.setVisibility(View.GONE);
                perigee_label.setVisibility(View.GONE);
            }

            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(data.nowThen(data.calendar()));
            if (position != null)
            {
                SuntimesUtils.TimeDisplayText distance = SuntimesUtils.formatAsDistance(context, position.distance, units, 2, true);
                moondistance.setText(SuntimesUtils.createColorSpan(null, distance.toString(), distance.getValue(), timeColor));

                if (SuntimesMoonData.isSuperMoon(position))
                    moondistance_note.setText(context.getString(R.string.timeMode_moon_super));
                else if (SuntimesMoonData.isMicroMoon(position))
                    moondistance_note.setText(context.getString(R.string.timeMode_moon_micro));
                else moondistance_note.setText("");

                moondistance.setVisibility(View.VISIBLE);
            } else moondistance.setVisibility(View.GONE);

        } else {
            moondistance.setVisibility(View.GONE);
            moondistance_note.setVisibility(View.GONE);
            perigee_date.setVisibility(View.GONE);
            perigee_note.setVisibility(View.GONE);
            perigee_distance.setVisibility(View.GONE);
            apogee_date.setVisibility(View.GONE);
            apogee_note.setVisibility(View.GONE);
            apogee_distance.setVisibility(View.GONE);
        }
    }

    private CharSequence createApsisNote(Context context, Calendar dateTime, boolean showWeeks, boolean showHours, int noteColor)
    {
        Calendar now = Calendar.getInstance();
        String noteText = (dateTime == null ? "" : utils.timeDeltaDisplayString(now.getTime(), dateTime.getTime(), showWeeks, showHours).toString());
        String noteString = now.after(dateTime) ? context.getString(R.string.ago, noteText) : context.getString(R.string.hence, noteText);
        return SuntimesUtils.createBoldColorSpan(null, noteString, noteText, noteColor);
    }

    /**@Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        //moonriseset.saveState(outState);
        //currentphase.saveState(outState);
        //moonphases.saveState(outState);
    }*/

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (currentphase != null) {
            currentphase.post(updateTask0);
            currentphase.post(updateTask1);
        }
    }

    private void stopUpdateTask()
    {
        if (currentphase != null) {
            currentphase.removeCallbacks(updateTask0);
            currentphase.removeCallbacks(updateTask1);
        }
    }

    public static final int UPDATE_RATE0 = 3 * 1000;       // 3sec
    private Runnable updateTask0 = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && currentphase != null)
            {
                currentphase.updatePosition();
                currentphase.postDelayed(this, UPDATE_RATE0);
            }
        }
    };

    public static final int UPDATE_RATE1 = 5 * 60 * 1000;  // 5min
    private Runnable updateTask1 = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && currentphase != null)
            {
                currentphase.updateIllumination(getContext());
                currentphase.postDelayed(this, UPDATE_RATE1);
            }
        }
    };

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }
}
