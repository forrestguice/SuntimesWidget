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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;

import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class WorldMapDialog extends BottomSheetDialogFragment
{
    public static final String LOGTAG = "WorldMapDialog";

    private TextView dialogTitle;
    private WorldMapView worldmap;
    private TextView empty;
    private View dialogContent = null;
    private TextView utcTime, offsetTime;
    private Spinner mapSelector;
    private SeekBar seekbar;
    private ImageButton playButton, pauseButton, resetButton, nextButton, prevButton, menuButton;
    private View radioGroup;
    private ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode> mapAdapter;
    private WorldMapWidgetSettings.WorldMapWidgetMode mapMode = null;

    private int color_disabled = Color.DKGRAY;
    private int color_pressed = Color.BLUE;
    private int color_normal = Color.WHITE;
    private int color_accent = Color.GREEN;

    private SuntimesUtils utils = new SuntimesUtils();

    private SuntimesRiseSetDataset data;
    public void setData( SuntimesRiseSetDataset data )
    {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_worldmap, parent, false);

        initLocale(getContext());
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            Log.d(LOGTAG, "WorldMapDialog onCreate (restoreState)");
        }
        themeViews(dialogContent.getContext());

        return dialogContent;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onShowDialogListener);
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

    private void expandSheet(Dialog dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(true);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

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
            Context context = getContext();
            if (data != null && context != null && !worldmap.isAnimated())
            {
                Calendar now = Calendar.getInstance();
                long mapTime = worldmap.getNow() + (worldmap.getOffsetMinutes()  * 60 * 1000);

                long timeDiff = Math.abs(now.getTimeInMillis() - mapTime);
                if (timeDiff > 60 * 1000 && timeDiff < 2 * 60 * 1000) {
                    worldmap.resetAnimation(true);
                } else {
                    updateTimeText();
                }
            }
            if (dialogContent != null)
                dialogContent.postDelayed(this, UPDATE_RATE);
        }
    };

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WorldMapWidgetSettings.initDisplayStrings(dialogContent.getContext());

        int[] colorAttrs = { R.attr.text_disabledColor, R.attr.buttonPressColor, android.R.attr.textColorPrimary, R.attr.text_accentColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        color_disabled = ContextCompat.getColor(context, typedArray.getResourceId(0, color_disabled));
        color_pressed = ContextCompat.getColor(context, typedArray.getResourceId(1, color_pressed));
        color_normal = ContextCompat.getColor(context, typedArray.getResourceId(2, color_normal));
        color_accent = ContextCompat.getColor(context, typedArray.getResourceId(3, color_accent));
        typedArray.recycle();
    }

    public void initViews(final Context context, View dialogView)
    {
        dialogTitle = (TextView)dialogView.findViewById(R.id.worldmapdialog_title);
        utcTime = (TextView)dialogView.findViewById(R.id.info_time_utc);
        offsetTime = (TextView)dialogView.findViewById(R.id.info_time_offset);
        empty = (TextView)dialogView.findViewById(R.id.txt_empty);
        worldmap = (WorldMapView)dialogView.findViewById(R.id.info_time_worldmap);
        worldmap.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view) {
                return showContextMenu(context, dialogTitle);
            }
        });

        ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>(Arrays.asList(WorldMapWidgetSettings.WorldMapWidgetMode.values()));
        mapAdapter = new ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode>(context, R.layout.layout_listitem_oneline_alt, modes);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSelector = (Spinner)dialogView.findViewById(R.id.worldmap_selector);
        mapSelector.setAdapter(mapAdapter);

        mapMode = WorldMapWidgetSettings.loadSunPosMapModePref(context, 0, WorldMapWidgetSettings.MAPTAG_DEF);
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

        if (radioGroup != null && option_sun != null && option_moon != null && option_sunmoon != null)
        {
            if (options.showSunShadow && options.showMoonLight)
                option_sunmoon.setChecked(true);
            else if (options.showSunShadow)
                option_sun.setChecked(true);
            else option_moon.setChecked(true);

            option_sun.setOnClickListener(onRadioButtonClicked);
            option_moon.setOnClickListener(onRadioButtonClicked);
            option_sunmoon.setOnClickListener(onRadioButtonClicked);
        }

        seekbar = (SeekBar)dialogView.findViewById(R.id.seek_map);
        if (seekbar != null)
        {
            seekbar.setMax(seek_totalMinutes);
            seekbar.setProgress(seek_now);
            seekbar.setOnSeekBarChangeListener(seekBarListener);
        }

        playButton = (ImageButton)dialogView.findViewById(R.id.media_play_map);
        if (playButton != null) {
            playButton.setOnClickListener(playClickListener);
            ImageViewCompat.setImageTintList(playButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }

        pauseButton = (ImageButton)dialogView.findViewById(R.id.media_pause_map);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(pauseClickListener);
            ImageViewCompat.setImageTintList(pauseButton, SuntimesUtils.colorStateList(color_accent, color_disabled, color_pressed));
        }

        resetButton = (ImageButton)dialogView.findViewById(R.id.media_reset_map);
        if (resetButton != null) {
            resetButton.setOnClickListener(resetClickListener);
            ImageViewCompat.setImageTintList(resetButton, SuntimesUtils.colorStateList(color_accent, color_disabled, color_pressed));
            resetButton.setEnabled(false);
        }

        nextButton = (ImageButton)dialogView.findViewById(R.id.media_next_map);
        if (nextButton != null)
        {
            nextButton.setOnClickListener(nextClickListener);
            ImageViewCompat.setImageTintList(nextButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }

        prevButton = (ImageButton)dialogView.findViewById(R.id.media_prev_map);
        if (prevButton != null)
        {
            prevButton.setOnClickListener(prevClickListener);
            ImageViewCompat.setImageTintList(prevButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }

        menuButton = (ImageButton)dialogView.findViewById(R.id.map_menu);
        if (menuButton != null)
        {
            menuButton.setOnClickListener(menuClickListener);
            ImageViewCompat.setImageTintList(menuButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }
    }

    @SuppressWarnings("ResourceType")
    public void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            dialogTitle.setTextColor(themeOverride.getTitleColor());
            utcTime.setTextColor(themeOverride.getTimeColor());
            worldmap.themeViews(context, themeOverride);
            color_pressed = themeOverride.getActionColor();
            color_normal = themeOverride.getTitleColor();
            color_accent = themeOverride.getAccentColor();
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
            Location location = WidgetSettings.loadLocationPref(context, 0);
            WorldMapTask.WorldMapOptions options = worldmap.getOptions();
            options.showSunShadow = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2);
            options.showMoonLight = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2);
            options.showMajorLatitudes = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);

            if (WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2)) {
                options.locations = new double[][] {{location.getLatitudeAsDouble(), location.getLongitudeAsDouble()}};
            } else options.locations = null;

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
        if (featureSupported)
        {
            worldmap.setMapTaskListener(onWorldMapUpdate);
            worldmap.updateViews(data);
        }
        startUpdateTask();
    }

    private void updateTimeText()
    {
        Context context = getContext();
        WorldMapTask.WorldMapOptions options = worldmap.getOptions();

        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        long offsetMillis = options.offsetMinutes * 60 * 1000;
        long mapTimeMillis = options.now + offsetMillis;

        Calendar mapTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        mapTime.setTimeInMillis(mapTimeMillis);

        String suffix = "";
        if (Math.abs(nowMillis - mapTimeMillis) > 60 * 1000) {
            suffix = (now.after(mapTime)) ? context.getString(R.string.past_today) : context.getString(R.string.future_today);
        }

        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, mapTime);
        if (utcTime != null) {
            utcTime.setText(getString(R.string.datetime_format_verylong, timeText.toString(), mapTime.getTimeZone().getID()) + " " + suffix);
        }
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
                WorldMapWidgetSettings.saveSunPosMapModePref(context, 0, mapMode, WorldMapWidgetSettings.MAPTAG_DEF);
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
            //boolean checked = ((RadioButton) view).isChecked();
            Context context = getContext();
            switch(v.getId())
            {
                case R.id.radio_sun:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, true);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2,false);
                    break;

                case R.id.radio_moon:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, false);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2,true);
                    break;

                case R.id.radio_sunmoon:
                default:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, true);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2,true);
                    break;
            }
            Log.d(WorldMapView.LOGTAG, "onOptionSelected: sunlight/moonlight option changed so triggering update...");
            updateViews();
        }
    };

    private void showEmptyView( boolean show )
    {
        if (empty != null) {
            empty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (worldmap != null) {
            worldmap.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (mapSelector != null) {
            mapSelector.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (radioGroup != null) {
            radioGroup.setVisibility(show ? View.GONE : View.GONE);  // TODO
        }
    }

    protected boolean showContextMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.mapmenu, menu.getMenu());
        menu.setOnMenuItemClickListener(onContextMenuClick);

        updateContextMenu(context, menu);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

    private void updateContextMenu(Context context, PopupMenu menu)
    {
        MenuItem option_latitudes = menu.getMenu().findItem(R.id.mapOption_majorLatitudes);
        if (option_latitudes != null) {
            option_latitudes.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        MenuItem option_location = menu.getMenu().findItem(R.id.mapOption_location);
        if (option_location != null) {
            option_location.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2));
        }
    }

    private PopupMenu.OnMenuItemClickListener onContextMenuClick = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            boolean toggledValue;
            switch (item.getItemId())
            {
                // TODO: additional share options; e.g. animated over range

                case R.id.shareMap:
                    worldmap.shareBitmap();
                    return true;

                case R.id.mapOption_location:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.mapOption_majorLatitudes:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                default:
                    return false;
            }
        }
    };

    private int seek_totalMinutes = 12 * 60 * 2;  // +- 12 hours
    private int seek_now = seek_totalMinutes / 2;     // with "now" at center point

    private View.OnClickListener playClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            playMap();
        }
    };
    private void playMap()
    {
        if (playButton != null) {
            playButton.setVisibility(View.GONE);
        }
        if (pauseButton != null) {
            pauseButton.setVisibility(View.VISIBLE);
        }
        worldmap.startAnimation();
    }

    private View.OnClickListener pauseClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            stopMap(false);
        }
    };
    private View.OnClickListener resetClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            stopMap(true);
        }
    };
    private void stopMap(boolean reset)
    {
        if (pauseButton != null) {
            pauseButton.setVisibility(View.GONE);
        }
        if (playButton != null) {
            playButton.setVisibility(View.VISIBLE);
        }
        if (reset) {
            worldmap.resetAnimation(true);
        } else {
            worldmap.stopAnimation();
        }
    }

    private View.OnClickListener menuClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showContextMenu(getContext(), v);
        }
    };

    private View.OnClickListener nextClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() + 15);   // advance 1hr
        }
    };

    private View.OnClickListener prevClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() - 15);   // rewind 1hr
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            worldmap.setOffsetMinutes(seekBar.getProgress() - seek_now);
        }
    };

    private WorldMapTask.WorldMapTaskListener onWorldMapUpdate = new WorldMapTask.WorldMapTaskListener()
    {
        @Override
        public void onFrame(Bitmap result, int offsetMinutes)
        {
            expandSheet(getDialog());
            if (seekbar != null)
            {
                int progress = seek_now + offsetMinutes;
                if (progress > 0 && progress < seek_totalMinutes) {
                    seekbar.setProgress(progress);
                }

                SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(0, offsetMinutes * 60 * 1000, false, true, false);
                offsetText.setSuffix("");
                String displayString = getContext().getString(( offsetMinutes <= 0 ? R.string.ago : R.string.hence), offsetText.toString() + "\n");
                offsetTime.setText(displayString);

                updateTimeText();
                resetButton.setEnabled(offsetMinutes != 0);
            }
        }

        @Override
        public void onFinished(Bitmap result) {
            expandSheet(getDialog());
        }
    };

}
