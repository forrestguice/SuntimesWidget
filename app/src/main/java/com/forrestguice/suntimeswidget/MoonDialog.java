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

    private TextView moondistance;
    private TextView apogee_date, perigee_date;
    private TextView apogee_note, perigee_note;
    private TextView apogee_distance, perigee_distance;

    private int timeColor;

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
        apogee_date = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_date);
        apogee_note = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_note);
        apogee_distance = (TextView) dialogView.findViewById(R.id.moonapsis_apogee_distance);
        perigee_date = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_date);
        perigee_note = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_note);
        perigee_distance = (TextView) dialogView.findViewById(R.id.moonapsis_perigee_distance);

        Context context = dialogView.getContext();
        if (context != null) {
            currentphase.adjustColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.moonphase_column0_width));
        }
    }

    public void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            timeColor = themeOverride.getTimeColor();
            int textColor = themeOverride.getTextColor();

            dialogTitle.setTextColor(themeOverride.getTitleColor());
            moonriseset.themeViews(context, themeOverride);
            currentphase.themeViews(context, themeOverride);
            moonphases.themeViews(context, themeOverride);

            moondistance.setTextColor(timeColor);
            apogee_distance.setTextColor(timeColor);
            apogee_date.setTextColor(timeColor);
            apogee_note.setTextColor(textColor);
            perigee_distance.setTextColor(timeColor);
            perigee_date.setTextColor(timeColor);
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

            Pair<Calendar, Double> apogee = data.getMoonApogee();
            apogee_date.setText(utils.calendarDateTimeDisplayString(context, apogee.first, showTime, showSeconds).getValue());
            apogee_note.setText(createApsisNote(context, apogee.first, showWeeks, showHours, timeColor));
            apogee_distance.setText(SuntimesUtils.formatAsDistance(context, apogee.second, units, 2, true).toString());

            Pair<Calendar, Double> perigee = data.getMoonPerigee();
            perigee_date.setText(utils.calendarDateTimeDisplayString(context, perigee.first, showTime, showSeconds).getValue());
            perigee_note.setText(createApsisNote(context, perigee.first, showWeeks, showHours, timeColor));
            perigee_distance.setText(SuntimesUtils.formatAsDistance(context, perigee.second, units, 2, true).toString());

            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(data.nowThen(data.calendar()));
            if (position != null)
                moondistance.setText(SuntimesUtils.formatAsDistance(context, position.distance, units, 2, true).toString());
            else moondistance.setText("");

        } else {
            moondistance.setText("");
            perigee_date.setText("");
            perigee_note.setText("");
            perigee_distance.setText("");
            apogee_date.setText("");
            apogee_note.setText("");
            apogee_distance.setText("");
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
