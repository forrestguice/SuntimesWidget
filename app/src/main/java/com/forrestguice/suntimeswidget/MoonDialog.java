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

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.MoonApsisView;

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
    private MoonApsisView moonapsis;
    private TextView moondistance, moondistance_label, moondistance_note;

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

        moonapsis = (MoonApsisView) dialogView.findViewById(R.id.moonapsis_view);
        moondistance = (TextView) dialogView.findViewById(R.id.moonapsis_current_distance);
        moondistance_label = (TextView) dialogView.findViewById(R.id.moonapsis_current_label);
        moondistance_note = (TextView) dialogView.findViewById(R.id.moonapsis_current_note);
        moondistance_note.setVisibility(View.GONE);

        Context context = dialogView.getContext();
        if (context != null) {
            currentphase.adjustColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.moonphase_column0_width));
        }
    }

    @SuppressLint("ResourceType")
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
            moonapsis.themeViews(context, themeOverride);

            moondistance_label.setTextColor(titleColor);
            moondistance.setTextColor(textColor);
            moondistance_note.setTextColor(timeColor);

        } else {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.moonriseColor, R.attr.moonsetColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            timeColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
            riseColor = ContextCompat.getColor(context, typedArray.getResourceId(1, timeColor));
            setColor = ContextCompat.getColor(context, typedArray.getResourceId(2, timeColor));
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
        moonapsis.updateViews(context, data);
        updateMoonApsis();
        startUpdateTask();
    }

    private void updateMoonApsis()
    {
        Context context = getContext();
        if (context != null && data != null && data.isCalculated())
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(data.nowThen(data.calendar()));
            if (position != null)
            {
                SuntimesUtils.TimeDisplayText distance = SuntimesUtils.formatAsDistance(context, position.distance, units, 2, true);
                moondistance.setText(SuntimesUtils.createColorSpan(null, distance.toString(), distance.getValue(), (moonapsis.isRising() ? riseColor : setColor)));

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
        }
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
