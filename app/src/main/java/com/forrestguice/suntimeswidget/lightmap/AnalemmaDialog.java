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

package com.forrestguice.suntimeswidget.lightmap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;

/**
 * AnalemmaDialog
 */
public class AnalemmaDialog extends DialogFragment
{
    private static SuntimesUtils utils = new SuntimesUtils();

    private AnalemmaView analemma;
    private TextView labelTime;
    private TextView labelLocalMean;
    private TextView labelApparentSolar;
    private TextView labelEOT;

    private int decimalPlaces = 1;
    private View dialogContent = null;

    private SuntimesRiseSetDataset data;
    public void setData( SuntimesRiseSetDataset data )
    {
        this.data = data;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LightMapWidgetSettings.initDisplayStrings(myParent);

        LayoutInflater inflater = myParent.getLayoutInflater();
        final ViewGroup viewGroup = null;
        dialogContent = inflater.inflate(R.layout.layout_dialog_analemma, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        SuntimesUtils.initDisplayStrings(myParent);
        initViews(dialogContent);
        if (savedInstanceState != null)
        {
            Log.d("DEBUG", "LightMapDialog onCreate (restoreState)");
        }

        dialog.setOnShowListener(onShowDialogListener);
        themeViews(dialog.getContext());
        return dialog;
    }

    private DialogInterface.OnShowListener onShowDialogListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            startUpdateTask();
        }
    };

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (analemma != null)
            analemma.post(updateTask);
    }
    private void stopUpdateTask()
    {
        if (analemma != null)
            analemma.removeCallbacks(updateTask);
    }

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    public static final int UPDATE_RATE = 6 * 60 * 1000;
    private Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null)
            {
                updateAnalemmaViews(data);
            }
            if (analemma != null)
                analemma.postDelayed(this, UPDATE_RATE);
        }
    };

    public void initViews(View dialogView)
    {
        analemma = (AnalemmaView)dialogView.findViewById(R.id.info_time_analemma);
        AnalemmaView.AnalemmaOptions options = analemma.getOptions();
        //options.showAxis = false;
        options.mode = LightMapWidgetSettings.AnalemmaWidgetMode.DEC_EOT;
        options.date_hour = 12;

        analemma.setAnalemmaListener(new AnalemmaView.AnalemmaTaskListener()
        {
            @Override
            public void onFinished(Bitmap result, AnalemmaView.AnalemmaDataPoints dataPoints)
            {
                super.onFinished(result, dataPoints);
                if (labelTime != null && dataPoints != null)
                {
                    labelTime.setText(utils.calendarDateTimeDisplayString(getContext(), dataPoints.getDate()).getValue());
                }
            }
        });

        labelTime = (TextView)dialogView.findViewById(R.id.info_time_analemmaTime);
        labelLocalMean = (TextView)dialogView.findViewById(R.id.info_time_localMean);
        labelApparentSolar = (TextView)dialogView.findViewById(R.id.info_time_apparentSolar);
        labelEOT = (TextView)dialogView.findViewById(R.id.info_time_eotOffset);
    }

    @SuppressWarnings("ResourceType")
    public void themeViews(Context context)
    {
        int[] colorAttrs = { R.attr.graphColor_night,   // 0
                R.attr.graphColor_astronomical,         // 1
                R.attr.graphColor_nautical,             // 2
                R.attr.graphColor_civil,                // 3
                R.attr.graphColor_day,                  // 4
                R.attr.sunriseColor,                    // 5
                R.attr.sunsetColor                      // 6
        };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        //colorNight = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        //colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        //colorNautical = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        //colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        //colorDay = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        //colorRising = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
        //colorSetting = ContextCompat.getColor(context, typedArray.getResourceId(6, def));
        typedArray.recycle();

        //colorLabel = field_night.label.getTextColors().getColorForState(new int[] { -android.R.attr.state_enabled }, Color.BLUE); // field_night.label.getCurrentTextColor()
    }

    public void updateViews()
    {
        if (data != null)
            updateViews(data);
    }

    protected void updateViews( @NonNull SuntimesRiseSetDataset data )
    {
        stopUpdateTask();
        updateAnalemmaViews(data);
        startUpdateTask();
    }

    protected void updateAnalemmaViews(@NonNull SuntimesRiseSetDataset data)
    {
        if (analemma != null)
        {
            analemma.updateViews(data);
        }

        if (labelEOT != null)
        {
            double eotOffset = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(data.calendar().get(Calendar.DAY_OF_YEAR));
            labelEOT.setText( utils.timeDeltaLongDisplayString(0, (long)(eotOffset * 60 * 1000L), false, true, true).getValue() + " equation of time" ); // TODO
        }
    }

}
