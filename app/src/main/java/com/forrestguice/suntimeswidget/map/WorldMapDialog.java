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

import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class WorldMapDialog extends DialogFragment
{
    public static final String PREF_KEY_UI_MAP_SUNSHADOW = "map_showsunshadow";
    public static final boolean PREF_DEF_UI_MAP_SUNSHADOW = true;

    public static final String PREF_KEY_UI_MAP_MOONLIGHT = "map_showmoonlight";
    public static final boolean PREF_DEF_UI_MAP_MOONLIGHT = true;

    public static final String LOGTAG = "WorldMapDialog";

    private TextView dialogTitle;
    private WorldMapView worldmap;
    private TextView empty;
    private View dialogContent = null;
    private TextView utcTime;
    private Spinner mapSelector;
    private View radioGroup;
    private ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode> mapAdapter;
    private WorldMapWidgetSettings.WorldMapWidgetMode mapMode = null;

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
        WorldMapWidgetSettings.initDisplayStrings(myParent);

        LayoutInflater inflater = myParent.getLayoutInflater();
        final ViewGroup viewGroup = null;
        dialogContent = inflater.inflate(R.layout.layout_dialog_worldmap, viewGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        initViews(getContext(), dialogContent);
        if (savedInstanceState != null)
        {
            Log.d(LOGTAG, "WorldMapDialog onCreate (restoreState)");
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
            Log.d(WorldMapView.LOGTAG, "onShowDialog: triggering update...");
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
                    utcTime.setText(getString(R.string.datetime_format_verylong, timeText.toString(), nowUtc.getTimeZone().getID()));
                }
                // TODO: periodic update bitmap
            }
            if (dialogContent != null)
                dialogContent.postDelayed(this, UPDATE_RATE);
        }
    };

    public void initViews(final Context context, View dialogView)
    {
        dialogTitle = (TextView)dialogView.findViewById(R.id.worldmapdialog_title);
        utcTime = (TextView)dialogView.findViewById(R.id.info_time_utc);
        empty = (TextView)dialogView.findViewById(R.id.txt_empty);
        worldmap = (WorldMapView)dialogView.findViewById(R.id.info_time_worldmap);
        worldmap.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view) {
                return showShareMenu(context, view);
            }
        });

        ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>(Arrays.asList(WorldMapWidgetSettings.WorldMapWidgetMode.values()));
        //modes.remove(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE);  // option disabled; TODO: fix layout issues

        mapAdapter = new ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode>(context, R.layout.layout_listitem_oneline_alt, modes);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSelector = (Spinner)dialogView.findViewById(R.id.worldmap_selector);
        mapSelector.setAdapter(mapAdapter);

        mapMode = WorldMapWidgetSettings.loadSunPosMapModePref(context, 0);
        int modePosition = mapAdapter.getPosition(mapMode);
        mapSelector.setSelection((modePosition >= 0) ? modePosition : 0);
        worldmap.setMapMode(context, (WorldMapWidgetSettings.WorldMapWidgetMode) mapSelector.getSelectedItem());

        mapSelector.setOnItemSelectedListener(onMapSelected);

        WorldMapTask.WorldMapOptions options = worldmap.getOptions();
        updateOptions(getContext());

        radioGroup = dialogView.findViewById(R.id.radio_group);
        RadioButton option_sun = (RadioButton)dialogView.findViewById(R.id.radio_sun);
        RadioButton option_moon = (RadioButton)dialogView.findViewById(R.id.radio_moon);
        RadioButton option_sunmoon = (RadioButton)dialogView.findViewById(R.id.radio_sunmoon);

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
        if (themeOverride != null)
        {
            dialogTitle.setTextColor(themeOverride.getTitleColor());
            utcTime.setTextColor(themeOverride.getTimeColor());
            worldmap.themeViews(context, themeOverride);
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null)
        {
            themeOverride = theme;
            if (worldmap != null) {
                themeViews(context);
            }
        }
    }

    public void updateOptions(Context context)
    {
        if (context != null)
        {
            WorldMapTask.WorldMapOptions options = worldmap.getOptions();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            options.showSunShadow = pref.getBoolean(PREF_KEY_UI_MAP_SUNSHADOW, PREF_DEF_UI_MAP_SUNSHADOW);
            options.showMoonLight = pref.getBoolean(PREF_KEY_UI_MAP_MOONLIGHT, PREF_DEF_UI_MAP_MOONLIGHT);
            options.modified = true;
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

        SuntimesCalculatorDescriptor calculatorDescriptor = data.calculatorMode();
        boolean featureSupported = calculatorDescriptor != null && calculatorDescriptor.hasRequestedFeature(SuntimesCalculator.FEATURE_POSITION);

        showEmptyView(!featureSupported);
        if (featureSupported) {
            worldmap.updateViews(data);
        }

        startUpdateTask();
    }

    private AdapterView.OnItemSelectedListener onMapSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = (WorldMapWidgetSettings.WorldMapWidgetMode) parent.getItemAtPosition(position);
            Context context = getContext();
            if (context != null && mode != mapMode)
            {
                mapMode = mode;
                WorldMapWidgetSettings.saveSunPosMapModePref(context, 0, mapMode);
                worldmap.setMapMode(context, mapMode);
                Log.d(WorldMapView.LOGTAG, "onMapSelected: mapMode changed so triggering update...");
                updateViews();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

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
            Log.d(WorldMapView.LOGTAG, "onOptionSelected: sunlight/moonlight option changed so triggering update...");
            updateViews();
        }
    };

    private void showEmptyView( boolean show )
    {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        worldmap.setVisibility(show ? View.GONE : View.VISIBLE);
        mapSelector.setVisibility(show ? View.GONE : View.VISIBLE);
        radioGroup.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    protected boolean showShareMenu(Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.mapshare, menu.getMenu());

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    // TODO: additional share options; e.g. animated over range

                    case R.id.shareMap:
                        worldmap.shareBitmap();
                        return true;

                    default:
                        return false;
                }
            }
        });
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

}
