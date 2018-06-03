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

package com.forrestguice.suntimeswidget.map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;

import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class WorldMapDialog extends DialogFragment
{
    public static final String PREF_KEY_UI_MAP_SUNSHADOW = "map_showsunshadow";
    public static final boolean PREF_DEF_UI_MAP_SUNSHADOW = true;

    public static final String PREF_KEY_UI_MAP_MOONLIGHT = "map_showmoonlight";
    public static final boolean PREF_DEF_UI_MAP_MOONLIGHT = true;

    private WorldMapView worldmap;
    private View dialogContent = null;
    private TextView utcTime;

    private SuntimesUtils utils = new SuntimesUtils();
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
        LayoutInflater inflater = myParent.getLayoutInflater();

        final ViewGroup viewGroup = null;
        dialogContent = inflater.inflate(R.layout.layout_dialog_worldmap, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        initViews(dialogContent);
        if (savedInstanceState != null)
        {
            Log.d("DEBUG", "WorldMapDialog onCreate (restoreState)");
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
            updateViews();
            startUpdateTask();
        }
    };

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (dialogContent != null)
            dialogContent.post(updateTask);
    }
    private void stopUpdateTask()
    {
        if (dialogContent != null)
            dialogContent.removeCallbacks(updateTask);
    }

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    public static final int UPDATE_RATE = 3000;
    private Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null)
            {
                Context context = getContext();
                if (utcTime != null && context != null)
                {
                    Calendar now = data.nowThen(data.calendar());
                    Calendar nowUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    nowUtc.setTimeInMillis(now.getTimeInMillis());
                    SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, nowUtc); // utils.calendarTimeShortDisplayString(context, nowUtc);
                    utcTime.setText("UTC\n" + timeText.toString());
                }
                // TODO: periodic update
            }
            if (dialogContent != null)
                dialogContent.postDelayed(this, UPDATE_RATE);
        }
    };

    public void initViews(View dialogView)
    {
        worldmap = (WorldMapView)dialogView.findViewById(R.id.info_time_worldmap);
        RadioButton option_sun = (RadioButton)dialogView.findViewById(R.id.radio_sun);
        RadioButton option_moon = (RadioButton)dialogView.findViewById(R.id.radio_moon);
        RadioButton option_sunmoon = (RadioButton)dialogView.findViewById(R.id.radio_sunmoon);

        utcTime = (TextView)dialogView.findViewById(R.id.info_time_utc);

        WorldMapView.WorldMapOptions options = worldmap.getOptions();
        updateOptions(getContext());

        if (options.showSunShadow && options.showMoonLight)
            option_sunmoon.setChecked(true);
        else if (options.showSunShadow)
            option_sun.setChecked(true);
        else option_moon.setChecked(true);

        option_sun.setOnClickListener(onRadioButtonClicked);
        option_moon.setOnClickListener(onRadioButtonClicked);
        option_sunmoon.setOnClickListener(onRadioButtonClicked);
    }

    @SuppressWarnings("ResourceType")
    public void themeViews(Context context)
    {
    }

    public void updateOptions(Context context)
    {
        if (context != null)
        {
            WorldMapView.WorldMapOptions options = worldmap.getOptions();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            options.showSunShadow = pref.getBoolean(PREF_KEY_UI_MAP_SUNSHADOW, PREF_DEF_UI_MAP_SUNSHADOW);
            options.showMoonLight = pref.getBoolean(PREF_KEY_UI_MAP_MOONLIGHT, PREF_DEF_UI_MAP_MOONLIGHT);
        }
    }

    public void updateViews()
    {
        updateOptions(getContext());
        if (data != null)
            updateViews(data);
    }

    protected void updateViews( @NonNull SuntimesRiseSetDataset data )
    {
        stopUpdateTask();
        worldmap.updateViews(data);
        startUpdateTask();
    }

    private View.OnClickListener onRadioButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            //boolean checked = ((RadioButton) view).isChecked();
            switch(v.getId())
            {
                case R.id.radio_sun:
                    pref.putBoolean(PREF_KEY_UI_MAP_SUNSHADOW, true);
                    pref.putBoolean(PREF_KEY_UI_MAP_MOONLIGHT, false);
                    break;

                case R.id.radio_moon:
                    pref.putBoolean(PREF_KEY_UI_MAP_SUNSHADOW, false);
                    pref.putBoolean(PREF_KEY_UI_MAP_MOONLIGHT, true);
                    break;

                case R.id.radio_sunmoon:
                default:
                    pref.putBoolean(PREF_KEY_UI_MAP_SUNSHADOW, true);
                    pref.putBoolean(PREF_KEY_UI_MAP_MOONLIGHT, true);
                    break;
            }
            pref.apply();
            updateViews();
        }
    };
}
