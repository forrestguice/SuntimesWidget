/**
    Copyright (C) 2018-2024 Forrest Guice
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
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.content.ContextCompat;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.DateInfo;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetDialog;
import com.forrestguice.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings.MapSpeed;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValuesCollection;
import com.forrestguice.suntimeswidget.timepicker.TimeDateDialog;
import com.forrestguice.suntimeswidget.timepicker.TimeDialog;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.MenuAddon;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class WorldMapDialog extends BottomSheetDialogBase
{
    public static final String LOGTAG = "WorldMapDialog";
    public static final String ARG_DATETIME = "datetime";

    public static final String DIALOGTAG_COLORS = "worldmap_colors";
    public static final String DIALOGTAG_TIME = "worldmap_time";

    public static final int REQUEST_BACKGROUND = 400;

    private View dialogHeader;
    private TextView dialogTitle;
    private WorldMapView worldmap;
    private TextView empty;
    private View dialogContent = null;
    private TextView utcTime, offsetTime;
    private Spinner mapSelector;
    private WorldMapSeekBar seekbar;
    private ImageButton playButton, pauseButton, recordButton, resetButton, nextButton, prevButton, menuButton, modeButton;
    private TextView speedButton;
    private View mediaGroup, seekGroup;
    //private View radioGroup;
    private ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode> mapAdapter;
    private WorldMapWidgetSettings.WorldMapWidgetMode mapMode = null;

    private int color_disabled = Color.DKGRAY;
    private int color_pressed = Color.BLUE;
    private int color_normal = Color.WHITE;
    private int color_accent = Color.GREEN;
    private int color_warning = Color.RED;
    private int color_sun = Color.RED;

    private final SuntimesUtils utils = new SuntimesUtils();

    public WorldMapDialog()
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATETIME, -1);
        setArguments(args);
    }

    private SuntimesRiseSetDataset data;
    public void setData( SuntimesRiseSetDataset data )
    {
        this.data = data;
    }

    public void showPositionAt(@Nullable Long datetime) {
        getArgs().putLong(ARG_DATETIME, (datetime == null ? -1 : datetime));
        if (isAdded()) {
            updateViews();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        Context context = requireContext();
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_worldmap, parent, false);
        initColors(contextWrapper);

        initLocale(context);
        initViews(context, dialogContent);
        if (savedState != null)
        {
            Log.d(LOGTAG, "WorldMapDialog onCreate (restoreState)");
            worldmap.loadSettings(context, savedState);
        }
        themeViews(context);

        return dialogContent;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedState)
    {
        Dialog dialog = super.onCreateDialog(savedState);
        dialog.setOnShowListener(onShowDialogListener);
        expandSheet(dialog);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        worldmap.saveSettings(state);
    }

    private final DialogInterface.OnShowListener onShowDialogListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            Log.d(WorldMapView.LOGTAG, "onShowDialog: triggering update...");
            updateViews();
            startUpdateTask();

            if (AppSettings.isTelevision(getActivity())) {
                menuButton.requestFocus();
            }
        }
    };

    @Override
    protected boolean getBottomSheetBehavior_skipCollapsed() {
        return true;
    }
    @Override
    protected boolean getBottomSheetBehavior_hideable() {
        return false;
    }
    @Override
    protected int getPeekHeight() {
        return (int)(dialogHeader.getHeight() + getResources().getDimension(R.dimen.dialog_margin));
    }

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (dialogContent != null) {
            updateTask_isRunning = true;
            dialogContent.post(updateTask);
        }
    }
    private void stopUpdateTask()
    {
        if (dialogContent != null) {
            updateTask_isRunning = false;
            dialogContent.removeCallbacks(updateTask);
        }
    }

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    public static final int UPDATE_RATE = 3000;
    public static final int[] RESET_THRESHOLD = new int[] {60 * 1000, 2 * 60 * 1000 };    // (1m, 2m)
    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            Context context = getContext();
            if (data != null && context != null && !worldmap.isAnimated())
            {
                Calendar now = Calendar.getInstance();
                long mapNow = worldmap.getNow();
                long mapTime = ((mapNow == -1) ? now.getTimeInMillis()
                        : mapNow + (worldmap.getOffsetMinutes()  * 60 * 1000));

                long timeDiff = Math.abs(now.getTimeInMillis() - mapTime);
                if (timeDiff > RESET_THRESHOLD[0] && timeDiff < RESET_THRESHOLD[1]) {
                    worldmap.resetAnimation(true);

                } else {
                    updateTimeText();
                    if (timeDiff >= RESET_THRESHOLD[1]) {
                        resetButton.setEnabled(true);
                    }
                }
            }
            if (dialogContent != null && updateTask_isRunning) {
                dialogContent.postDelayed(this, UPDATE_RATE);
            }
        }
    };
    private boolean updateTask_isRunning = false;

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WorldMapWidgetSettings.initDisplayStrings(dialogContent.getContext());
        WidgetSettings.initDisplayStrings_SolarTimeMode(dialogContent.getContext());

        int[] colorAttrs = { R.attr.text_disabledColor, R.attr.buttonPressColor, android.R.attr.textColorPrimary, R.attr.text_accentColor, R.attr.tagColor_warning, R.attr.graphColor_pointFill };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        color_disabled = ContextCompat.getColor(context, typedArray.getResourceId(0, color_disabled));
        color_pressed = ContextCompat.getColor(context, typedArray.getResourceId(1, color_pressed));
        color_normal = ContextCompat.getColor(context, typedArray.getResourceId(2, color_normal));
        color_accent = ContextCompat.getColor(context, typedArray.getResourceId(3, color_accent));
        color_warning = ContextCompat.getColor(context, typedArray.getResourceId(4, color_warning));
        color_sun = ContextCompat.getColor(context, typedArray.getResourceId(5, color_sun));
        typedArray.recycle();
    }

    public void initViews(final Context context, View dialogView)
    {
        dialogHeader = dialogView.findViewById(R.id.worldmapdialog_header);
        dialogTitle = (TextView)dialogView.findViewById(R.id.worldmapdialog_title);

        utcTime = (TextView)dialogView.findViewById(R.id.info_time_utc);
        utcTime.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeZoneMenu(context, v);
            }
        }));

        offsetTime = (TextView)dialogView.findViewById(R.id.info_time_offset);
        empty = (TextView)dialogView.findViewById(R.id.txt_empty);
        worldmap = (WorldMapView)dialogView.findViewById(R.id.info_time_worldmap);

        worldmap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                stopMap(false);
            }
        });

        worldmap.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view) {
                if (worldmap.isAnimated())
                    stopMap(false);
                else playMap();
                return true;
            }
        });

        ArrayList<WorldMapWidgetSettings.WorldMapWidgetMode> modes = new ArrayList<>(Arrays.asList(WorldMapWidgetSettings.WorldMapWidgetMode.values()));
        mapAdapter = new ArrayAdapter<WorldMapWidgetSettings.WorldMapWidgetMode>(context, R.layout.layout_listitem_oneline_alt, modes);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapSelector = (Spinner)dialogView.findViewById(R.id.worldmap_selector);
        mapSelector.setAdapter(mapAdapter);

        mapMode = WorldMapWidgetSettings.loadSunPosMapModePref(context, 0, WorldMapWidgetSettings.MAPTAG_DEF);
        int modePosition = mapAdapter.getPosition(mapMode);
        mapSelector.setSelection(Math.max(modePosition, 0));

        updateOptions(getContext());
        worldmap.setMapMode(context, (WorldMapWidgetSettings.WorldMapWidgetMode) mapSelector.getSelectedItem());
        mapSelector.setOnItemSelectedListener(onMapSelected);

        //WorldMapTask.WorldMapOptions options = worldmap.getOptions();

        modeButton = (ImageButton)dialogView.findViewById(R.id.map_modemenu);
        if (modeButton != null) {
            modeButton.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMapModeMenu(context, modeButton);
                }
            }));
        }

        /**radioGroup = dialogView.findViewById(R.id.radio_group);
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
        }*/

        seekbar = (WorldMapSeekBar)dialogView.findViewById(R.id.seek_map);
        if (seekbar != null)
        {
            seekbar.setMax(seek_totalMinutes);
            seekbar.setProgress(seek_now);
            seekbar.setOnSeekBarChangeListener(seekBarListener);
            updateSeekbarDrawables(context);
        }

        playButton = (ImageButton)dialogView.findViewById(R.id.media_play_map);
        if (playButton != null) {
            playButton.setOnClickListener(playClickListener);
            playButton.setOnLongClickListener(playLongClickListener);
        }

        pauseButton = (ImageButton)dialogView.findViewById(R.id.media_pause_map);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(pauseClickListener);
        }

        recordButton = (ImageButton)dialogView.findViewById(R.id.media_record_map);
        if (recordButton != null) {
            recordButton.setOnClickListener(pauseClickListener);   // stop-record is same as pause
        }

        resetButton = (ImageButton)dialogView.findViewById(R.id.media_reset_map);
        if (resetButton != null) {
            resetButton.setEnabled(false);
            TooltipCompat.setTooltipText(resetButton, resetButton.getContentDescription());
            resetButton.setOnClickListener(resetClickListener);
        }

        nextButton = (ImageButton)dialogView.findViewById(R.id.media_next_map);
        if (nextButton != null) {
            TooltipCompat.setTooltipText(nextButton, nextButton.getContentDescription());
            nextButton.setOnClickListener(nextClickListener);
        }

        prevButton = (ImageButton)dialogView.findViewById(R.id.media_prev_map);
        if (prevButton != null) {
            TooltipCompat.setTooltipText(prevButton, prevButton.getContentDescription());
            prevButton.setOnClickListener(prevClickListener);
        }

        menuButton = (ImageButton)dialogView.findViewById(R.id.map_menu);
        if (menuButton != null)
        {
            TooltipCompat.setTooltipText(menuButton, menuButton.getContentDescription());
            menuButton.setOnClickListener(menuClickListener);
            if (AppSettings.isTelevision(getActivity())) {
                menuButton.setFocusableInTouchMode(true);
            }
        }

        speedButton = (TextView)dialogView.findViewById(R.id.map_speed);
        if (speedButton != null) {
            speedButton.setOnClickListener(speedClickListener);
        }

        mediaGroup = dialogView.findViewById(R.id.media_actions);
        seekGroup = dialogView.findViewById(R.id.media_seek);
    }

    private void updateSeekbarDrawables(Context context)
    {
        if (context == null) {
            return;
        }
        /**LightMapView.LightMapTask lightMapTask = new LightMapView.LightMapTask();
        LightMapView.LightMapColors colors = new LightMapView.LightMapColors();
        colors.initDefaultDark(context);
        colors.option_drawNow = false;
        Bitmap lightmap = lightMapTask.makeBitmap(data, worldmap.getWidth(), 1, colors);
        BitmapDrawable lightmapDrawable = new BitmapDrawable(context.getResources(), lightmap);*/

        int color = getColor(WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2));
        seekbar.setTrackColor(color);
        seekbar.setTickColor(color, color, color);
        seekbar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private int getColor(MapSpeed value) {
        switch (value) {
            case ONE_WEEK: case ONE_DAY: return color_warning;
            case FIVE_MINUTES: case ONE_MINUTE: default: return color_accent;
        }
    }

    private int getFrameOffsetMinutes(MapSpeed value) {
        switch (value) {
            case FIVE_MINUTES: return 3;
            case ONE_WEEK: case ONE_DAY: return MapSpeed.ONE_DAY.getStepMinutes();
            default: return value.getStepMinutes();
        }
    }

    @SuppressWarnings("ResourceType")
    public void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            color_pressed = color_warning = themeOverride.getActionColor();
            color_normal = themeOverride.getTitleColor();
            color_accent = themeOverride.getAccentColor();

            dialogTitle.setTextColor(themeOverride.getTitleColor());
            dialogTitle.setTextSize(themeOverride.getTitleSizeSp());
            dialogTitle.setTypeface(dialogTitle.getTypeface(), (themeOverride.getTitleBold() ? Typeface.BOLD : Typeface.NORMAL));

            utcTime.setTextColor(themeOverride.getTimeColor());
            utcTime.setTextSize(themeOverride.getTimeSizeSp());
            utcTime.setTypeface(utcTime.getTypeface(), (themeOverride.getTimeBold() ? Typeface.BOLD : Typeface.NORMAL));

            worldmap.themeViews(context, themeOverride);
        } else {
            worldmap.themeViews(context);
        }

        if (seekbar != null) {
            seekbar.setTrackColor(color_accent);
            seekbar.setTickColor(color_accent, color_accent, color_accent);
            seekbar.getThumb().setColorFilter(color_accent, PorterDuff.Mode.SRC_IN);
        }

        ImageViewCompat.setImageTintList(playButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(resetButton, SuntimesUtils.colorStateList(color_warning, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(recordButton, SuntimesUtils.colorStateList(color_warning, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(pauseButton, SuntimesUtils.colorStateList(color_accent, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(nextButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(prevButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(menuButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));

        if (speedButton != null) {
            speedButton.setTextColor(SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }
    }

    private SuntimesTheme themeOverride = null;
    @Deprecated
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
            options.showGrid = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2);
            options.showDebugLines = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_DEBUGLINES, WorldMapWidgetSettings.MAPTAG_3x2);
            options.anim_frameOffsetMinutes = getFrameOffsetMinutes(WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2));

            try {
                options.center = WorldMapWidgetSettings.loadWorldMapCenter(context, 0, mapMode.getMapTag(), mapMode.getProjectionCenter());
            } catch (NumberFormatException | NullPointerException e) {
                options.center = new double[] {location.getLatitudeAsDouble(), location.getLongitudeAsDouble()};
            }
            options.tintForeground = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapMode.getMapTag());

            if (WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2)) {
                options.locations = new double[][] {{location.getLatitudeAsDouble(), location.getLongitudeAsDouble()}};
            } else options.locations = null;

            long now = getArgs().getLong(ARG_DATETIME);
            if (now != -1L)
            {
                getArgs().putLong(ARG_DATETIME, -1L);
                options.now = now;
                options.offsetMinutes = 1;
                //Log.d("DEBUG", "updateOptions: now: " + now);
            }

            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            WorldMapColorValues values = (WorldMapColorValues) colors.getSelectedColors(context, (isNightMode ? 1 : 0), WorldMapColorValues.TAG_WORLDMAP);
            if (values != null) {
                options.colors = values;
            } else if (options.colors == null) {
                options.init(context);
            }
            worldmap.themeViews(context);

            options.modified = true;
        }
    }

    public void updateViews()
    {
        updateOptions(getContext());
        if (data != null) {
            updateViews(data);
        }
        updateSeekbarDrawables(getContext());
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
            worldmap.updateViews(data, false);
            updateMediaButtons();
        }

        startUpdateTask();
    }
    private void updateMediaButtons()
    {
        if (mediaGroup != null)
        {
            if (worldmap.isAnimated())
            {
                if (worldmap.isRecording())
                {
                    pauseButton.setVisibility(View.GONE);
                    playButton.setVisibility(View.GONE);
                    recordButton.setVisibility(View.VISIBLE);

                } else {
                    pauseButton.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    recordButton.setVisibility(View.GONE);
                }

            } else {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                recordButton.setVisibility(View.GONE);
            }
        }

        Context context = getContext();
        if (speedButton != null && context != null)
        {
            MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2);
            speedButton.setText(mapSpeed.getDisplayString(context));
            speedButton.setTextColor(getColor(mapSpeed));
        }
    }

    private DateInfo getMapDate() {
        return new DateInfo(getMapTime(Calendar.getInstance().getTimeInMillis()));
    }
    private long getMapTime(long now)
    {
        WorldMapTask.WorldMapOptions options = worldmap.getOptions();
        long offsetMillis = options.offsetMinutes * 60 * 1000;
        return ((options.now == -1) ? now : options.now + offsetMillis);
    }

    private void updateTimeText()
    {
        Context context = getContext();
        if (context == null) {
            return;
        }

        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        long mapTimeMillis = getMapTime(nowMillis);

        String suffix = "";
        boolean nowIsAfter = false;

        String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, WorldMapWidgetSettings.MAPTAG_3x2, WorldMapWidgetSettings.PREF_DEF_WORLDMAP_TIMEZONE);
        TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data.timezone()
                : WidgetTimezones.getTimeZone(tzId, data.location().getLongitudeAsDouble(), data.calculator());
        Calendar mapTime = Calendar.getInstance(timezone);
        if (empty.getVisibility() != View.VISIBLE)
        {
            mapTime.setTimeInMillis(mapTimeMillis);
            nowIsAfter = now.after(mapTime);
            if (Math.abs(nowMillis - mapTimeMillis) > 60 * 1000) {
                suffix = ((nowIsAfter) ? context.getString(R.string.past_today) : context.getString(R.string.future_today));
            }
        }

        TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, mapTime);
        if (utcTime != null)
        {
            String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, mapTime.getTimeZone());
            if (suffix.isEmpty())
                utcTime.setText(getString(R.string.datetime_format_verylong, timeText.toString(), tzDisplay));
            else utcTime.setText(SuntimesUtils.createBoldColorSpan(null, getString(R.string.datetime_format_verylong1, timeText.toString(), tzDisplay, suffix), suffix, color_warning));
        }

        TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(nowMillis, mapTimeMillis, false, true, false);
        offsetText.setSuffix("");
        String displayString = getContext().getString((nowIsAfter ? R.string.ago : R.string.hence), offsetText.toString() + "\n");
        offsetTime.setText(displayString);
    }

    private final AdapterView.OnItemSelectedListener onMapSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setMapMode(getContext(), (WorldMapWidgetSettings.WorldMapWidgetMode) parent.getItemAtPosition(position));
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    public void setMapMode(Context context, WorldMapWidgetSettings.WorldMapWidgetMode mode)
    {
        if (context != null && mode != mapMode)
        {
            mapMode = mode;
            WorldMapWidgetSettings.saveSunPosMapModePref(context, 0, mapMode, WorldMapWidgetSettings.MAPTAG_DEF);
            updateOptions(context);
            worldmap.setMapMode(context, mapMode);
            Log.d(WorldMapView.LOGTAG, "onMapSelected: mapMode changed so triggering update...");
            updateViews();
        }
    }

    /*private View.OnClickListener onRadioButtonClicked = new View.OnClickListener()
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
    };*/

    private void showEmptyView( boolean show )
    {
        if (empty != null) {
            empty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (worldmap != null) {
            worldmap.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (mapSelector != null) {
            mapSelector.setVisibility(show ? View.GONE : View.GONE);   // always hidden
        }
        if (mediaGroup != null) {
            mediaGroup.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (seekGroup != null) {
            seekGroup.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        /**if (radioGroup != null) {
            radioGroup.setVisibility(show ? View.GONE : View.GONE);
        }*/

        expandSheet(getDialog());
    }

    protected boolean showTimeZoneMenu(Context context, View view)
    {
        PopupMenuCompat.createMenu(context, view, R.menu.mapmenu_tz, onTimeZoneMenuClick).show();
        return true;
    }
    private final PopupMenuCompat.PopupMenuListener onTimeZoneMenuClick = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            WidgetTimezones.updateTimeZoneMenu(menu, WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            if (item.getItemId() == R.id.action_seektime) {
                showSeekTimeDialog(getActivity());
                return true;

            } else {
                String tzID = WidgetTimezones.timeZoneForMenuItem(item.getItemId());
                if (tzID != null)
                {
                    WorldMapWidgetSettings.saveWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, WorldMapWidgetSettings.MAPTAG_3x2, tzID);
                    updateViews();
                }
                return (tzID != null);
            }
        }
    });

    protected void showSeekTimeDialog(Context context)
    {
        TimeDateDialog dialog = new TimeDateDialog();
        dialog.loadSettings(getActivity());
        dialog.setTimeIs24(WidgetSettings.loadTimeFormatModePref(context, 0) == TimeFormatMode.MODE_24HR);
        dialog.setDialogTitle(context.getString(R.string.configAction_seekTime));
        dialog.setOnAcceptedListener(onSeekTimeDialogAccepted(dialog));
        dialog.show(getChildFragmentManager(), DIALOGTAG_TIME);
    }
    private DialogInterface.OnClickListener onSeekTimeDialogAccepted(final TimeDialog dialog) {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                Context context = getActivity();
                if (context != null)
                {
                    String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, WorldMapWidgetSettings.MAPTAG_3x2, WorldMapWidgetSettings.PREF_DEF_WORLDMAP_TIMEZONE);
                    TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data.timezone() : WidgetTimezones.getTimeZone(tzId, data.location().getLongitudeAsDouble(), data.calculator());
                    Calendar mapTime = Calendar.getInstance(timezone);
                    mapTime.setTimeInMillis(getMapTime(Calendar.getInstance().getTimeInMillis()));
                    seekDateTime(context, TimeDialog.getCalendar(dialog.getSelected(), mapTime));
                }
            }
        };
    }

    @Nullable
    public Long seekDateTime( Context context, @Nullable Calendar calendar ) {
        return (calendar != null ? seekDateTime(context, calendar.getTimeInMillis()) : null);
    }
    @Nullable
    public Long seekDateTime( Context context, @Nullable Long datetime )
    {
        if (datetime != null)
        {
            stopMap(false);
            worldmap.seekDateTime(context, datetime);
        }
        return datetime;
    }

    protected boolean showMapModeMenu(final Context context, View view)
    {
        PopupMenuCompat.createMenu(context, view, R.menu.mapmenu_mode, onMapModeMenuClick).show();
        return true;
    }
    private void updateMapModeMenu(Context context, Menu m)
    {
        MenuItem option_mapmode = m.findItem(menuItemForMapMode(mapMode));
        if (option_mapmode != null) {
            option_mapmode.setChecked(true);
        }
    }
    private final PopupMenuCompat.PopupMenuListener onMapModeMenuClick = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            updateMapModeMenu(context, menu);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            int itemId = item.getItemId();
            if (itemId == R.id.action_worldmap_simplemercator || itemId == R.id.action_worldmap_simplesinusoidal || itemId == R.id.action_worldmap_simplevandergrinten || itemId == R.id.action_worldmap_simplerectangular || itemId == R.id.action_worldmap_bluemarble || itemId == R.id.action_worldmap_simpleazimuthal || itemId == R.id.action_worldmap_simpleazimuthal_south || itemId == R.id.action_worldmap_simpleazimuthal_location) {
                item.setChecked(true);
                setMapMode(context, mapModeForMenuItem(item));
                return true;
            }
            return false;
        }
    });

    protected boolean showSpeedMenu(final Context context, View view)
    {
        PopupMenuCompat.createMenu(context, view, R.menu.mapmenu_speed, onSpeedMenuClick).show();
        return true;
    }

    private void updateSpeedMenu(Context context, Menu m)
    {
        MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2);

        MenuItem speed_15m = m.findItem(R.id.mapSpeed_15m);
        if (speed_15m != null) {
            speed_15m.setChecked(mapSpeed == MapSpeed.FIFTEEN_MINUTES);
        }

        MenuItem speed_1d = m.findItem(R.id.mapSpeed_1d);
        if (speed_1d != null) {
            speed_1d.setChecked(mapSpeed == MapSpeed.ONE_DAY);
        }

        MenuItem speed_7d = m.findItem(R.id.mapSpeed_7d);
        if (speed_7d != null) {
            speed_7d.setChecked(mapSpeed == MapSpeed.ONE_WEEK);
        }
    }

    private final PopupMenuCompat.PopupMenuListener onSpeedMenuClick = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            updateSpeedMenu(context, menu);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            int itemId = item.getItemId();
            if (itemId == R.id.mapSpeed_7d) {
                WorldMapWidgetSettings.saveMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2, MapSpeed.ONE_WEEK);
                item.setChecked(true);
                updateViews();
                return true;

            } else if (itemId == R.id.mapSpeed_1d) {
                WorldMapWidgetSettings.saveMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2, MapSpeed.ONE_DAY);
                item.setChecked(true);
                updateViews();
                return true;

            } else if (itemId == R.id.mapSpeed_15m) {
                WorldMapWidgetSettings.saveMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2, MapSpeed.FIFTEEN_MINUTES);
                item.setChecked(true);
                updateViews();
                return true;
            }
            return false;
        }
    });

    protected boolean showContextMenu(final Context context, View view)
    {
        PopupMenuCompat.createMenu(context, view, R.menu.mapmenu, onContextMenuClick).show();
        return true;
    }

    private void updateContextMenu(Context context, Menu m)
    {
        WorldMapTask.WorldMapOptions options = worldmap.getOptions();

        MenuItem option_latitudes = m.findItem(R.id.mapOption_majorLatitudes);
        if (option_latitudes != null) {
            option_latitudes.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        MenuItem option_debuglines = m.findItem(R.id.mapOption_debugLines);
        if (option_debuglines != null) {
            option_debuglines.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_DEBUGLINES, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        MenuItem option_tintMap = m.findItem(R.id.mapOption_tintMap);
        if (option_tintMap != null) {
            option_tintMap.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapMode.getMapTag()));
        }

        MenuItem option_minorgrid = m.findItem(R.id.mapOption_minorgrid);
        if (option_minorgrid != null) {
            option_minorgrid.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        MenuItem option_location = m.findItem(R.id.mapOption_location);
        if (option_location != null) {
            option_location.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2));
        }

        MenuItem option_sunlight = m.findItem(R.id.mapOption_sunlight);
        if (option_sunlight != null) {
            option_sunlight.setChecked(options.showSunShadow);
        }

        MenuItem option_moonlight = m.findItem(R.id.mapOption_moonlight);
        if (option_moonlight != null) {
            option_moonlight.setChecked(options.showMoonLight);
        }

        //MenuItem action_date = m.findItem(R.id.action_date);
        //if (action_date != null) {
        //    action_date.setEnabled( !WidgetSettings.DateInfo.isToday(getMapDate()) );
        //}

        MenuItem action_center_current = m.findItem(R.id.mapOption_center_current);
        if (action_center_current != null) {
            double[] center = WorldMapWidgetSettings.loadWorldMapCenter(context, 0, mapMode.getMapTag(), mapMode.getProjectionCenter());
            String locationDisplay = getString(R.string.location_format_latlon, Double.toString(center[0]), Double.toString(center[1]));
            action_center_current.setTitle(context.getString(R.string.worldmap_dialog_option_center_current, locationDisplay));
        }

        MenuItem action_center_set = m.findItem(R.id.mapOption_center);
        if (action_center_set != null) {
            action_center_set.setVisible(mapMode.supportsCenter());
        }

        MenuItem action_center_clear = m.findItem(R.id.mapOption_center_clear);
        if (action_center_clear != null) {
            action_center_clear.setVisible(mapMode.supportsCenter());
        }

        MenuItem action_background_clear = m.findItem(R.id.mapOption_background_clear);
        if (action_background_clear != null) {
            double[] center = WorldMapWidgetSettings.loadWorldMapCenter(context, 0, mapMode.getMapTag(), mapMode.getProjectionCenter());
            action_background_clear.setEnabled(null != WorldMapWidgetSettings.loadWorldMapBackground(context, 0, mapMode.getMapTag(), center));
        }

        MenuItem addonSubmenuItem = m.findItem(R.id.addonSubMenu0);
        if (addonSubmenuItem != null) {
            List<MenuAddon.ActivityItemInfo> addonMenuItems = MenuAddon.queryAddonMenuItems(context);
            if (!addonMenuItems.isEmpty()) {
                MenuAddon.populateSubMenu(addonSubmenuItem, addonMenuItems, getMapTime(System.currentTimeMillis()));
            } //else addonSubmenuItem.setVisible(false);
        }
    }

    private int menuItemForMapMode(WorldMapWidgetSettings.WorldMapWidgetMode mode) {
        switch (mode) {
            case MERCATOR_SIMPLE: return R.id.action_worldmap_simplemercator;
            case VANDERGRINTEN_SIMPLE: return R.id.action_worldmap_simplevandergrinten;
            case SINUSOIDAL_SIMPLE: return R.id.action_worldmap_simplesinusoidal;
            case EQUIAZIMUTHAL_SIMPLE: return R.id.action_worldmap_simpleazimuthal;
            case EQUIAZIMUTHAL_SIMPLE1: return R.id.action_worldmap_simpleazimuthal_south;
            case EQUIAZIMUTHAL_SIMPLE2: return R.id.action_worldmap_simpleazimuthal_location;
            case EQUIRECTANGULAR_BLUEMARBLE: return R.id.action_worldmap_bluemarble;
            case EQUIRECTANGULAR_SIMPLE: default: return R.id.action_worldmap_simplerectangular;
        }
    }
    private WorldMapWidgetSettings.WorldMapWidgetMode mapModeForMenuItem(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.action_worldmap_simplemercator) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.MERCATOR_SIMPLE;

        } else if (itemId == R.id.action_worldmap_simplevandergrinten) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.VANDERGRINTEN_SIMPLE;

        } else if (itemId == R.id.action_worldmap_simplesinusoidal) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.SINUSOIDAL_SIMPLE;

        } else if (itemId == R.id.action_worldmap_simpleazimuthal) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE;

        } else if (itemId == R.id.action_worldmap_simpleazimuthal_south) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE1;

        } else if (itemId == R.id.action_worldmap_simpleazimuthal_location) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE2;

        } else if (itemId == R.id.action_worldmap_bluemarble) {
            return WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_BLUEMARBLE;
        }
        return WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE;
    }

    private void shareMap()
    {
        if (!worldmap.isRecording()) {
            worldmap.shareBitmap();
        } else worldmap.stopAnimation();
        updateMediaButtons();
    }

    private void setMapCenter(Context context)
    {
        Location location = WidgetSettings.loadLocationPref(context, 0);
        double[] center = new double[] {location.getLatitudeAsDouble(), location.getLongitudeAsDouble()};

        WorldMapWidgetSettings.saveWorldMapCenter(context, 0, mapMode.getMapTag(), center);
        WorldMapWidgetSettings.saveWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_CENTER_LABEL, mapMode.getMapTag(), location.getLabel());

        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2, true);
        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2, true);

        String locationDisplay = getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
        Toast.makeText(context, context.getString(R.string.worldmap_dialog_option_center_msg, locationDisplay), Toast.LENGTH_LONG).show();

        updateOptions(getContext());
        worldmap.setMapMode(context, mapMode);
        updateViews();
    }

    private void clearMapCenter(Context context)
    {
        WorldMapWidgetSettings.deleteWorldMapCenter(context, 0, mapMode.getMapTag());
        WorldMapWidgetSettings.initWorldMapBackgroundDefaults(context);   // restores background if removed

        double[] center = WorldMapWidgetSettings.loadWorldMapCenter(context, 0, mapMode.getMapTag(), mapMode.getProjectionCenter());
        String locationDisplay = getString(R.string.location_format_latlon, Double.toString(center[0]), Double.toString(center[1]));
        Toast.makeText(context, context.getString(R.string.worldmap_dialog_option_center_clear_msg, locationDisplay), Toast.LENGTH_LONG).show();

        updateOptions(getContext());
        worldmap.setMapMode(context, mapMode);
        updateViews();
    }

    private void setMapBackground(final Context context)
    {
        if (context != null)
        {
            WorldMapWidgetSettings.WorldMapWidgetMode modes = worldmap.getMapMode();
            double[] center = worldmap.getOptions().center;

            String title = context.getString(R.string.worldmap_dialog_option_background);
            String message = context.getString(R.string.help_worldmap_background, modes.getProjectionTitle(), center[0]+"", center[1]+"", modes.getProj4(center));

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(title).setMessage(message).setIcon(R.drawable.ic_action_settings)
                    .setPositiveButton(context.getString(R.string.dialog_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mapBackgroundFilePicker(REQUEST_BACKGROUND);
                        }
                    })
                    .setNeutralButton(context.getString(R.string.configAction_onlineHelp),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.help_url) + context.getString(R.string.help_worldmap_background_path))));
                        }
                    })
                    .setNegativeButton(context.getString(R.string.dialog_cancel), null);

            Dialog dialog = builder.show();

            if (Build.VERSION.SDK_INT >= 11) {
                View textView = dialog.findViewById(android.R.id.message);
                if (textView instanceof TextView) {
                    ((TextView) textView).setTextIsSelectable(true);
                }
            }
        }
    }

    private void clearMapBackground(Context context)
    {
        double[] center = worldmap.getOptions().center;
        String mapTag = mapMode.getMapTag();
        String mapBackgroundString = WorldMapWidgetSettings.loadWorldMapBackground(context, 0, mapTag, center);
        Uri uri = mapBackgroundString != null ? Uri.parse(mapBackgroundString) : null;

        if (Build.VERSION.SDK_INT >= 19)
        {
            if (uri != null) {
                try {
                    context.getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    Log.w(LOGTAG, "Failed to release URI permissions for " + uri.toString() + "; " + e);
                }
            }
        }

        WorldMapWidgetSettings.deleteWorldMapBackground(context,0, mapTag, center);
        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapTag, true);   // reset tint flag

        updateOptions(context);
        worldmap.setMapMode(context, mapMode);
        updateViews();
    }

    protected void onMapBackgroundResult(Context context, int requestCode, Uri uri)
    {
        Drawable background = WorldMapView.loadDrawableFromUri(context, uri.toString());
        if (background == null) {
            Toast.makeText(context, context.getString(R.string.worldmap_dialog_option_background_error0), Toast.LENGTH_LONG).show();
            return;
        }

        String mapTag = mapMode.getMapTag();
        double aspectRatio0 = mapTag.startsWith(WorldMapWidgetSettings.MAPTAG_3x3) ? 1 : 2;
        double aspectRatio1 = ((double)background.getIntrinsicWidth() / (double)background.getIntrinsicHeight());
        if (Math.abs(aspectRatio1 - aspectRatio0) > 0.01)
        {
            String aspectWarning = context.getString(R.string.worldmap_dialog_option_background_warning0, Double.toString(aspectRatio1), Double.toString(aspectRatio0));
            Toast.makeText(context, aspectWarning, Toast.LENGTH_LONG).show();
        }

        double[] center = worldmap.getOptions().center;    // TODO: read center/projection info from image exif data?
        WorldMapWidgetSettings.saveWorldMapBackground(context, 0, mapTag, center, uri.toString());
        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapTag, false);    // TODO: automatically set tint flag based on image transparency?

        updateOptions(context);
        worldmap.setMapMode(context, mapMode);
        updateViews();
    }

    protected void onMapBackgroundResult(int requestCode, int resultCode, Intent data)
    {
        Context context = getContext();
        if (resultCode == Activity.RESULT_OK && context != null && data != null && data.getData() != null)
        {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT >= 19) {
                final int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                context.getContentResolver().takePersistableUriPermission(uri, flags);
            }
            onMapBackgroundResult(context, requestCode, uri);
        } else {
            Log.d(LOGTAG, "onActivityResult: bad result: " + resultCode + ", " + data);
        }
    }

    protected void mapBackgroundFilePicker(int requestCode)
    {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 19)
        {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.worldmap_dialog_option_background)), requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_BACKGROUND:
                onMapBackgroundResult(requestCode, resultCode, data);
                break;
        }
    }

    private final PopupMenuCompat.PopupMenuListener onContextMenuClick = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            updateContextMenu(context, menu);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            WorldMapTask.WorldMapOptions options = worldmap.getOptions();

            boolean toggledValue;
            int itemId = item.getItemId();
            if (itemId == R.id.action_sunposition) {
                if (dialogListener != null) {
                    dialogListener.onShowPosition(getMapTime(Calendar.getInstance().getTimeInMillis()));
                    collapseSheet(getDialog());
                }
                return true;

            } else if (itemId == R.id.action_moon) {
                if (dialogListener != null) {
                    dialogListener.onShowMoonInfo(getMapTime(Calendar.getInstance().getTimeInMillis()));
                    collapseSheet(getDialog());
                }
                return true;

            } else if (itemId == R.id.action_date) {
                if (dialogListener != null) {
                    dialogListener.onShowDate(getMapTime(Calendar.getInstance().getTimeInMillis()));
                    collapseSheet(getDialog());
                }
                return true;

            } else if (itemId == R.id.action_calendar) {
                openCalendar(context, getMapTime(Calendar.getInstance().getTimeInMillis()));
                return true;

            } else if (itemId == R.id.action_timezone) {
                showTimeZoneMenu(context, utcTime);
                return true;

            } else if (itemId == R.id.shareMap) {
                shareMap();
                return true;

            } else if (itemId == R.id.recordMap) {
                playMap();
                shareMap();
                return true;

            } else if (itemId == R.id.mapOption_center) {
                setMapCenter(context);
                return true;

            } else if (itemId == R.id.mapOption_center_clear) {
                clearMapCenter(context);
                return true;

            } else if (itemId == R.id.mapOption_background) {
                setMapBackground(context);
                return true;

            } else if (itemId == R.id.mapOption_background_clear) {
                clearMapBackground(context);
                return true;

            } else if (itemId == R.id.mapOption_location) {
                toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2);
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                item.setChecked(toggledValue);
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_tintMap) {
                toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapMode.getMapTag());
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TINTMAP, mapMode.getMapTag(), toggledValue);
                item.setChecked(toggledValue);
                updateOptions(context);
                worldmap.setMapMode(context, mapMode);
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_debugLines) {
                toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_DEBUGLINES, WorldMapWidgetSettings.MAPTAG_3x2);
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_DEBUGLINES, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                item.setChecked(toggledValue);
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_majorLatitudes) {
                toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                item.setChecked(toggledValue);
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_minorgrid) {
                toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2);
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                item.setChecked(toggledValue);
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_sunlight) {
                toggledValue = !options.showSunShadow;
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                if (!toggledValue && !options.showMoonLight) {
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2, true);
                }
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_moonlight) {
                toggledValue = !options.showMoonLight;
                WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                if (!toggledValue && !options.showSunShadow) {
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, true);
                }
                updateViews();
                return true;

            } else if (itemId == R.id.mapOption_colors) {
                showColorDialog(context);
                return true;
            }
            return false;
        }
    });

    protected void openCalendar(Context context, long itemMillis)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/time/" + itemMillis));
        context.startActivity(intent);
    }

    public static final int SEEK_TOTALMINUTES_15m = 12 * 2 * 60;   // +- 12 hours
    public static final int SEEK_TOTALMINUTES_1d = 364 * 24 * 60;  // +- 182 days

    private final int seek_totalMinutes = SEEK_TOTALMINUTES_15m;
    private final int seek_now = seek_totalMinutes / 2;     // with "now" at center point

    private final View.OnClickListener playClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            playMap();
            if (AppSettings.isTelevision(getActivity())) {
                if (pauseButton != null) {
                    pauseButton.requestFocus();
                }
            }
        }
    };
    private final View.OnLongClickListener playLongClickListener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v)
        {
            if (worldmap.isAnimated()) {
                stopMap(false);
                if (AppSettings.isTelevision(getActivity())) {
                    if (playButton != null) {
                        playButton.requestFocus();
                    }
                }

            } else {
                playMap();
                shareMap();
                if (AppSettings.isTelevision(getActivity())) {
                    if (recordButton != null) {
                        recordButton.requestFocus();
                    }
                }
            }
            return true;
        }
    };
    private void playMap()
    {
        worldmap.startAnimation();
        updateMediaButtons();
    }

    private final View.OnClickListener pauseClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            stopMap(false);
            if (AppSettings.isTelevision(getActivity())) {
                if (playButton != null) {
                    playButton.requestFocus();
                }
            }
        }
    };
    private final View.OnClickListener resetClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            stopMap(true);
            if (AppSettings.isTelevision(getActivity())) {
                if (playButton != null) {
                    playButton.requestFocus();
                }
            }
        }
    };
    private void stopMap(boolean reset)
    {
        if (reset) {
            worldmap.resetAnimation(true);
        } else {
            worldmap.stopAnimation();
        }
        updateMediaButtons();
    }

    private final View.OnClickListener menuClickListener = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showContextMenu(getContext(), dialogTitle);
        }
    });

    private final View.OnClickListener speedClickListener = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showSpeedMenu(getContext(), v);
        }
    });

    private final View.OnClickListener nextClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2);
                worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() + mapSpeed.getStepMinutes());
            }
        }
    };

    private final View.OnClickListener prevClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2);
                worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() - mapSpeed.getStepMinutes());
            }
        }
    };

    private int t_prevProgress = -1;
    private final SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (t_prevProgress == -1) {
                t_prevProgress = seek_now;
            }

            if (fromUser)
            {
                long offset = progress - t_prevProgress;
                MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(getContext(), 0, WorldMapWidgetSettings.MAPTAG_3x2);
                boolean speed_1d = (mapSpeed == MapSpeed.ONE_DAY);
                long scaledOffset = (speed_1d ? ((offset * ((SEEK_TOTALMINUTES_1d) / seek_totalMinutes)) / mapSpeed.getStepMinutes()) * mapSpeed.getStepMinutes() : offset);
                worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() + scaledOffset);
                updateTimeText();
            }

            t_prevProgress = progress;
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final WorldMapTask.WorldMapTaskListener onWorldMapUpdate = new WorldMapTask.WorldMapTaskListener()
    {
        @Override
        public void onFrame(Bitmap result, long offsetMinutes)
        {
            if (seekbar != null)
            {
                setSeekProgress(offsetMinutes);
                updateTimeText();
                resetButton.setEnabled(offsetMinutes != 0);
            }
        }

        @Override
        public void onFinished(Bitmap result)
        {
            expandSheet(getDialog());

            Context context = getContext();
            if (seekbar != null && context != null) {
                updateSeekbarDrawables(context);
            }
        }
    };

    private void setSeekProgress( long offsetMinutes )
    {
        Context context = getContext();
        if (context == null) {
            return;
        }
        MapSpeed mapSpeed = WorldMapWidgetSettings.loadMapSpeed(context, 0, WorldMapWidgetSettings.MAPTAG_3x2);
        boolean speed_days = (mapSpeed == MapSpeed.ONE_DAY || mapSpeed == MapSpeed.ONE_WEEK);
        long offsetMinutes1 = (speed_days ? offsetMinutes / ((SEEK_TOTALMINUTES_1d) / seek_totalMinutes) : offsetMinutes);

        //long offset = progress - t_prevProgress;

        long progress = seek_now + offsetMinutes1;
        if (progress > seek_totalMinutes) {
            seekbar.setProgress(!speed_days ? seek_now : seek_totalMinutes);
        } else if (progress < 0) {
            seekbar.setProgress(!speed_days ? seek_now : 0);
        } else {
            seekbar.setProgress((int)progress);
        }
    }

    private WorldMapDialogListener dialogListener = null;
    public void setDialogListener(WorldMapDialogListener listener) {
        dialogListener = listener;
    }

    public static class WorldMapDialogListener
    {
        public void onShowDate( long suggestedDate ) {}
        public void onShowPosition( long suggestDate ) {}
        public void onShowMoonInfo( long suggestDate ) {}
    }

    /**
     * onResume
     */
    @Override
    public void onResume()
    {
        super.onResume();

        Context context = getActivity();
        ColorValuesSheetDialog colorDialog = (ColorValuesSheetDialog) getChildFragmentManager().findFragmentByTag(DIALOGTAG_COLORS);
        if (colorDialog != null)
        {
            boolean isNightMode = getResources().getBoolean(R.bool.is_nightmode);
            colorDialog.setAppWidgetID((isNightMode ? 1 : 0));
            colorDialog.setColorTag(WorldMapColorValues.TAG_WORLDMAP);
            colorDialog.setColorCollection(colors);
            colorDialog.setDialogListener(colorDialogListener);
        }

        TimeDateDialog timeDialog = (TimeDateDialog) getChildFragmentManager().findFragmentByTag(DIALOGTAG_TIME);
        if (timeDialog != null) {
            timeDialog.setOnAcceptedListener(onSeekTimeDialogAccepted(timeDialog));
        }
    }

    /**
     * showColorDialog
     */
    protected void showColorDialog(Context context)
    {
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        ColorValuesSheetDialog dialog = new ColorValuesSheetDialog();
        dialog.setAppWidgetID((isNightMode ? 1 : 0));
        dialog.setColorTag(WorldMapColorValues.TAG_WORLDMAP);
        dialog.setColorCollection(colors);
        dialog.setDialogListener(colorDialogListener);
        dialog.show(getChildFragmentManager(), DIALOGTAG_COLORS);
    }
    private final ColorValuesSheetDialog.DialogListener colorDialogListener = new ColorValuesSheetDialog.DialogListener()
    {
        @Override
        public void onColorValuesSelected(ColorValues values) {
            updateColors(values);
        }

        protected void updateColors(ColorValues values)
        {
            if (values != null) {
                worldmap.getOptions().colors = new WorldMapColorValues(values);
            } else {
                worldmap.getOptions().init(getActivity());
            }
            worldmap.themeViews(getActivity());
            worldmap.setMapMode(getActivity(), mapMode);
            updateViews();
        }

        public void requestPeekHeight(int height) {}
        public void requestHideSheet() {}
        public void requestExpandSheet() {}
        public void onModeChanged(int mode) {}

        @SuppressWarnings("ConstantConditions")
        @Nullable
        @Override
        public ColorValues getDefaultValues() {
            return new WorldMapColorValues(AndroidResources.wrap(getActivity()), true);
        }
    };

    private ColorValuesCollection<ColorValues> colors;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colors = collection;
    }
    public ColorValuesCollection<ColorValues> getColorCollection() {
        return colors;
    }
    protected void initColors(Context context) {
        colors = new WorldMapColorValuesCollection<WorldMapColorValues>(context);
    }

}
