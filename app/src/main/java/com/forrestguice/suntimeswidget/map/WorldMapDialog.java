/**
    Copyright (C) 2018-2021 Forrest Guice
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
import android.graphics.PorterDuff;
import android.graphics.Typeface;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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

    private View dialogHeader;
    private TextView dialogTitle;
    private WorldMapView worldmap;
    private TextView empty;
    private View dialogContent = null;
    private TextView utcTime, offsetTime;
    private Spinner mapSelector;
    private WorldMapSeekBar seekbar;
    private ImageButton playButton, pauseButton, recordButton, resetButton, nextButton, prevButton, menuButton;
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
        if (savedState != null)
        {
            Log.d(LOGTAG, "WorldMapDialog onCreate (restoreState)");
            worldmap.loadSettings(getContext(), savedState);
        }
        themeViews(dialogContent.getContext());

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
    public void onSaveInstanceState( Bundle state )
    {
        worldmap.saveSettings(state);
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
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setPeekHeight((int)(dialogHeader.getHeight() + getResources().getDimension(R.dimen.dialog_margin) * 2));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void collapseSheet(Dialog dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(false);
            behavior.setPeekHeight((int)(dialogHeader.getHeight() + getResources().getDimension(R.dimen.dialog_margin) * 2));
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
    public static final int RESET_THRESHOLD[] = new int[] {60 * 1000, 2 * 60 * 1000 };    // (1m, 2m)
    private Runnable updateTask = new Runnable()
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
            if (dialogContent != null)
                dialogContent.postDelayed(this, UPDATE_RATE);
        }
    };

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WorldMapWidgetSettings.initDisplayStrings(dialogContent.getContext());

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
        mapSelector.setSelection((modePosition >= 0) ? modePosition : 0);
        worldmap.setMapMode(context, (WorldMapWidgetSettings.WorldMapWidgetMode) mapSelector.getSelectedItem());

        mapSelector.setOnItemSelectedListener(onMapSelected);

        //WorldMapTask.WorldMapOptions options = worldmap.getOptions();
        updateOptions(getContext());

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
            resetButton.setOnClickListener(resetClickListener);
        }

        nextButton = (ImageButton)dialogView.findViewById(R.id.media_next_map);
        if (nextButton != null) {
            nextButton.setOnClickListener(nextClickListener);
        }

        prevButton = (ImageButton)dialogView.findViewById(R.id.media_prev_map);
        if (prevButton != null) {
            prevButton.setOnClickListener(prevClickListener);
        }

        menuButton = (ImageButton)dialogView.findViewById(R.id.map_menu);
        if (menuButton != null) {
            menuButton.setOnClickListener(menuClickListener);
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
        /**LightMapView.LightMapTask lightMapTask = new LightMapView.LightMapTask();
        LightMapView.LightMapColors colors = new LightMapView.LightMapColors();
        colors.initDefaultDark(context);
        colors.option_drawNow = false;
        Bitmap lightmap = lightMapTask.makeBitmap(data, worldmap.getWidth(), 1, colors);
        BitmapDrawable lightmapDrawable = new BitmapDrawable(context.getResources(), lightmap);*/

        boolean speed_1d = WorldMapWidgetSettings.loadWorldMapPref(getContext(), 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
        int color = speed_1d ? color_warning : color_accent;
        seekbar.setTrackColor(color);
        seekbar.setTickColor(color, color, color);
        seekbar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
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
            options.anim_frameOffsetMinutes = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2)
                    ? 24 * 60 : 3;

            if (WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_LOCATION, WorldMapWidgetSettings.MAPTAG_3x2)) {
                options.locations = new double[][] {{location.getLatitudeAsDouble(), location.getLongitudeAsDouble()}};
            } else options.locations = null;

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
            boolean speed_1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
            speedButton.setText( context.getString(speed_1d ? R.string.worldmap_dialog_speed_1d : R.string.worldmap_dialog_speed_15m));
            speedButton.setTextColor( speed_1d ? color_warning : color_accent );
        }
    }

    private WidgetSettings.DateInfo getMapDate() {
        return new WidgetSettings.DateInfo(getMapTime(Calendar.getInstance().getTimeInMillis()));
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
        Calendar mapTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (empty.getVisibility() != View.VISIBLE)
        {
            mapTime.setTimeInMillis(mapTimeMillis);
            nowIsAfter = now.after(mapTime);
            if (Math.abs(nowMillis - mapTimeMillis) > 60 * 1000) {
                suffix = ((nowIsAfter) ? context.getString(R.string.past_today) : context.getString(R.string.future_today));
            }
        }

        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, mapTime);
        if (utcTime != null) {
            if (suffix.isEmpty())
                utcTime.setText(getString(R.string.datetime_format_verylong, timeText.toString(), mapTime.getTimeZone().getID()));
            else utcTime.setText(SuntimesUtils.createBoldColorSpan(null, getString(R.string.datetime_format_verylong1, timeText.toString(), mapTime.getTimeZone().getID(), suffix), suffix, color_warning));
        }

        SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(nowMillis, mapTimeMillis, false, true, false);
        offsetText.setSuffix("");
        String displayString = getContext().getString((nowIsAfter ? R.string.ago : R.string.hence), offsetText.toString() + "\n");
        offsetTime.setText(displayString);
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

    /**private View.OnClickListener onRadioButtonClicked = new View.OnClickListener()
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
            mapSelector.setVisibility(show ? View.GONE : View.VISIBLE);
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

    protected boolean showSpeedMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.mapmenu_speed, menu.getMenu());
        menu.setOnMenuItemClickListener(onSpeedMenuClick);

        updateSpeedMenu(context, menu);
        menu.show();
        return true;
    }

    private void updateSpeedMenu(Context context, PopupMenu menu)
    {
        Menu m = menu.getMenu();
        boolean is1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);

        MenuItem speed_15m = m.findItem(R.id.mapSpeed_15m);
        if (speed_15m != null) {
            speed_15m.setChecked(!is1d);
        }

        MenuItem speed_1d = m.findItem(R.id.mapSpeed_1d);
        if (speed_1d != null) {
            speed_1d.setChecked(is1d);
        }
    }

    private PopupMenu.OnMenuItemClickListener onSpeedMenuClick = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            switch (item.getItemId())
            {
                case R.id.mapSpeed_1d:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2, true);
                    item.setChecked(true);
                    updateViews();
                    return true;

                case R.id.mapSpeed_15m:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2, false);
                    item.setChecked(true);
                    updateViews();
                    return true;

                default:
                    return false;
            }
        }
    };

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
        Menu m = menu.getMenu();
        WorldMapTask.WorldMapOptions options = worldmap.getOptions();

        MenuItem option_latitudes = m.findItem(R.id.mapOption_majorLatitudes);
        if (option_latitudes != null) {
            option_latitudes.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0,  WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2));
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

        MenuItem action_date = m.findItem(R.id.setDate);
        if (action_date != null) {
            action_date.setEnabled( !WidgetSettings.DateInfo.isToday(getMapDate()) );
        }
    }

    private void shareMap()
    {
        if (!worldmap.isRecording()) {
            worldmap.shareBitmap();
        } else worldmap.stopAnimation();
        updateMediaButtons();
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

            WorldMapTask.WorldMapOptions options = worldmap.getOptions();

            boolean toggledValue;
            switch (item.getItemId())
            {
                case R.id.setDate:
                    if (dialogListener != null) {
                        dialogListener.onConfigDate(getMapTime(Calendar.getInstance().getTimeInMillis()));
                        collapseSheet(getDialog());
                    }
                    return true;

                case R.id.shareMap:
                    shareMap();
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

                case R.id.mapOption_sunlight:
                    toggledValue = !options.showSunShadow;
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                    if (!toggledValue && !options.showMoonLight) {
                        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2, true);
                    }
                    updateViews();
                    return true;

                case R.id.mapOption_moonlight:
                    toggledValue = !options.showMoonLight;
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MOONLIGHT, WorldMapWidgetSettings.MAPTAG_3x2, toggledValue);
                    if (!toggledValue && !options.showSunShadow) {
                        WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SUNSHADOW, WorldMapWidgetSettings.MAPTAG_3x2, true);
                    }
                    updateViews();
                    return true;

                default:
                    return false;
            }
        }
    };

    public static final int SEEK_TOTALMINUTES_15m = 12 * 2 * 60;   // +- 12 hours
    public static final int SEEK_STEPSIZE_15m = 15;

    public static final int SEEK_TOTALMINUTES_1d = 364 * 24 * 60;  // +- 182 days
    public static final int SEEK_STEPSIZE_1d = 24 * 60;

    private int seek_totalMinutes = SEEK_TOTALMINUTES_15m;
    private int seek_now = seek_totalMinutes / 2;     // with "now" at center point

    private View.OnClickListener playClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            playMap();
        }
    };
    private View.OnLongClickListener playLongClickListener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v)
        {
            if (worldmap.isAnimated()) {
                stopMap(false);

            } else {
                playMap();
                shareMap();
            }
            return true;
        }
    };
    private void playMap()
    {
        worldmap.startAnimation();
        updateMediaButtons();
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
        if (reset) {
            worldmap.resetAnimation(true);
        } else {
            worldmap.stopAnimation();
        }
        updateMediaButtons();
    }

    private View.OnClickListener menuClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showContextMenu(getContext(), v);
        }
    };

    private View.OnClickListener speedClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showSpeedMenu(getContext(), v);
        }
    };

    private View.OnClickListener nextClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                boolean speed1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
                worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() + (speed1d ? SEEK_STEPSIZE_1d : SEEK_STEPSIZE_15m));
            }
        }
    };

    private View.OnClickListener prevClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                boolean speed1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
                worldmap.setOffsetMinutes(worldmap.getOffsetMinutes() - (speed1d ? SEEK_STEPSIZE_1d : SEEK_STEPSIZE_15m));
            }
        }
    };

    private int t_prevProgress = -1;
    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener()
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
                boolean speed_1d = WorldMapWidgetSettings.loadWorldMapPref(getContext(), 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
                long scaledOffset = (speed_1d ? ((offset * ((SEEK_TOTALMINUTES_1d) / seek_totalMinutes)) / SEEK_STEPSIZE_1d) * SEEK_STEPSIZE_1d : offset);
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

    private WorldMapTask.WorldMapTaskListener onWorldMapUpdate = new WorldMapTask.WorldMapTaskListener()
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

            if (seekbar != null) {
                updateSeekbarDrawables(getContext());
            }
        }
    };

    private void setSeekProgress( long offsetMinutes )
    {
        boolean speed_1d = WorldMapWidgetSettings.loadWorldMapPref(getContext(), 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, WorldMapWidgetSettings.MAPTAG_3x2);
        long offsetMinutes1 = (speed_1d ? offsetMinutes / ((SEEK_TOTALMINUTES_1d) / seek_totalMinutes) : offsetMinutes);

        //long offset = progress - t_prevProgress;

        long progress = seek_now + offsetMinutes1;
        if (progress > seek_totalMinutes) {
            seekbar.setProgress(!speed_1d ? seek_now : seek_totalMinutes);
        } else if (progress < 0) {
            seekbar.setProgress(!speed_1d ? seek_now : 0);
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
        public void onConfigDate( long suggestedDate ) {}
    }

}
