/**
    Copyright (C) 2017-2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.graph;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.MenuAddon;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.cards.CardColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetDialog;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.map.WorldMapDialog;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class LightMapDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_COLORS = "lightmap_colors";

    public static final String DIALOGTAG_HELP = "lightmap_help";
    public static final int HELP_PATH_ID = R.string.help_sun_path;

    public static final String ARG_DATETIME = "datetime";

    private static SuntimesUtils utils = new SuntimesUtils();

    private TextView dialogTitle;
    private View sunLayout;

    private TextView sunTime, offsetTime;
    private TextView sunAzimuth, sunAzimuthRising, sunAzimuthSetting, sunAzimuthAtNoon, sunAzimuthLabel;
    private TextView sunElevation, sunElevationAtNoon, sunElevationLabel;
    private ImageView sunElevationHighlight;
    private ImageView riseIcon, setIcon;
    private TextView sunShadowObj, sunShadowLength, sunShadowLengthAtNoon;

    private View mediaGroup;
    private ImageButton playButton, pauseButton, resetButton, nextButton, prevButton, menuButton;
    private TextView speedButton;
    private int color_normal, color_disabled, color_pressed, color_warning, color_accent;

    private LineGraphView graphView;
    private final Lock anim_lock = new ReentrantLock(true);    // synchronize animations

    private LightMapView lightmap;
    private LightMapKey field_night, field_astro, field_nautical, field_civil, field_day;
    private int colorNight, colorAstro, colorNautical, colorCivil, colorDay;
    private int colorRising, colorSetting;
    private int colorLabel;
    private int colorRisingLabel, colorSettingLabel, colorCivilLabel, colorNauticalLabel, colorAstroLabel;
    private boolean showSeconds = true;
    private int decimalPlaces = 1;
    private View dialogContent = null;

    public LightMapDialog()
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATETIME, -1);
        setArguments(args);
    }

    public void showPositionAt(@Nullable Long datetime)
    {
        getArguments().putLong(ARG_DATETIME, (datetime == null ? -1 : datetime));
        if (isAdded()) {
            updateViews();
        }
    }
    public long showingPositionAt() {
        return getMapTime(System.currentTimeMillis());
    }

    private TimeZone data_timezone = null;
    private SuntimesRiseSetDataset data;
    public void setData(@NonNull Context context, @NonNull SuntimesRiseSetDataset values)
    {
        this.data_timezone = values.timezone();
        this.data = new SuntimesRiseSetDataset(values);
        this.data.invalidateCalculation();
        this.data.setTimeZone(context, WidgetTimezones.localMeanTime(context, values.location()));
        this.data.setTodayIs(Calendar.getInstance(data.timezone()));
        this.data.calculateData(context);
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onShowDialogListener);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_lightmap, parent, false);
        initColors(contextWrapper);

        SuntimesUtils.initDisplayStrings(getActivity());
        WidgetSettings.SolarTimeMode.initDisplayStrings(getActivity());
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            //Log.d("DEBUG", "LightMapDialog onCreate (restoreState)");
            loadSettings(savedState);
        }
        themeViews(getContext());
        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());

        Context context = getActivity();
        FragmentManager fragments = getChildFragmentManager();
        ColorValuesSheetDialog colorDialog = (ColorValuesSheetDialog) fragments.findFragmentByTag(DIALOGTAG_COLORS);
        if (colorDialog != null && context != null)
        {
            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            colorDialog.setAppWidgetID((isNightMode ? 1 : 0));
            colorDialog.setColorTag(AppColorValues.TAG_APPCOLORS);
            colorDialog.setColorCollection(colors);
            colorDialog.setDialogListener(colorDialogListener);
        }

        HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        }

        updateViews();
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private void initPeekHeight(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null)
            {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                ViewGroup dialogLayout = (ViewGroup) bottomSheet.findViewById(R.id.dialog_lightmap_layout);
                int resID = getResources().getBoolean(R.bool.is_watch) ? R.id.sundialog_gutter2 : R.id.media_actions;
                View divider1 = bottomSheet.findViewById(resID);
                if (dialogLayout != null && divider1 != null)
                {
                    Rect headerBounds = new Rect();
                    divider1.getDrawingRect(headerBounds);
                    dialogLayout.offsetDescendantRectToMyCoords(divider1, headerBounds);
                    behavior.setPeekHeight(headerBounds.bottom); // + (int)getResources().getDimension(R.dimen.dialog_margin));

                } else {
                    behavior.setPeekHeight(-1);
                }
            }
        }
    }

    private final DialogInterface.OnShowListener onShowDialogListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            startUpdateTask();
            dialogTitle.post(new Runnable() {
                @Override
                public void run() {
                    initPeekHeight(getDialog());
                }
            });

            if (AppSettings.isTelevision(getActivity())) {
                menuButton.requestFocus();
            }
        }
    };

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (sunElevation != null) {
            updateTask_isRunning = true;
            sunElevation.post(updateTask);
        }
    }
    private void stopUpdateTask()
    {
        if (sunElevation != null) {
            updateTask_isRunning = false;
            sunElevation.removeCallbacks(updateTask);
        }
    }

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    public static final int UPDATE_RATE = 3000;
    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && !lightmap.isAnimated())
            {
                updateLightmapViews(data);
                updateGraphViews(data);
                updateSunPositionViews(data);
                updateTimeText(data);
            }
            if (sunElevation != null && updateTask_isRunning) {
                sunElevation.postDelayed(this, UPDATE_RATE);
            }
        }
    };
    private boolean updateTask_isRunning = false;

    public void initViews(final Context context, View dialogView)
    {
        dialogTitle = (TextView)dialogView.findViewById(R.id.sundialog_title);
        lightmap = (LightMapView)dialogView.findViewById(R.id.info_time_lightmap);
        graphView = (LineGraphView)dialogView.findViewById(R.id.info_time_graph);
        sunTime = (TextView)dialogView.findViewById(R.id.info_time_solar);
        if (sunTime != null) {
            sunTime.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimeZoneMenu(getContext(), v);
                }
            }));
        }
        offsetTime = (TextView)dialogView.findViewById(R.id.info_time_offset);

        sunLayout = dialogView.findViewById(R.id.info_sun_layout);
        sunElevation = (TextView)dialogView.findViewById(R.id.info_sun_elevation_current);
        sunElevationHighlight = (ImageView) dialogView.findViewById(R.id.info_sun_elevation_current_highlight);
        sunElevationAtNoon = (TextView)dialogView.findViewById(R.id.info_sun_elevation_atnoon);
        sunElevationLabel = (TextView)dialogView.findViewById(R.id.info_sun_elevation_current_label);

        sunAzimuth = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_current);
        sunAzimuthRising = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_rising);
        sunAzimuthAtNoon = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_atnoon);
        sunAzimuthSetting = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_setting);
        sunAzimuthLabel = (TextView)dialogView.findViewById(R.id.info_sun_azimuth_current_label);

        View clickArea_altitude = dialogView.findViewById(R.id.clickArea_altitude);
        if (clickArea_altitude != null) {
            clickArea_altitude.setOnClickListener(onAltitudeLayoutClick);
        }

        View clickArea_rising = dialogView.findViewById(R.id.clickArea_rising);
        if (clickArea_rising != null) {
            clickArea_rising.setOnClickListener(onSunriseLayoutClick);
        }
        View clickArea_noon = dialogView.findViewById(R.id.clickArea_noon);
        if (clickArea_noon != null) {
            clickArea_noon.setOnClickListener(onNoonLayoutClick);
        }
        View clickArea_setting = dialogView.findViewById(R.id.clickArea_setting);
        if (clickArea_setting != null) {
            clickArea_setting.setOnClickListener(onSunsetLayoutClick);
        }

        View shadowLayout = dialogView.findViewById(R.id.info_shadow_layout);
        if (shadowLayout != null) {
            shadowLayout.setOnClickListener(onShadowLayoutClick);
        }

        sunShadowObj = (TextView)dialogView.findViewById(R.id.info_shadow_height);
        sunShadowLength = (TextView)dialogView.findViewById(R.id.info_shadow_length);
        sunShadowLengthAtNoon = (TextView)dialogView.findViewById(R.id.info_shadow_length_atnoon);

        field_night = new LightMapKey(dialogView, R.id.info_time_lightmap_key_night_icon, R.id.info_time_lightmap_key_night_label, R.id.info_time_lightmap_key_night_duration);
        field_astro = new LightMapKey(dialogView, R.id.info_time_lightmap_key_astro_icon, R.id.info_time_lightmap_key_astro_label, R.id.info_time_lightmap_key_astro_duration);
        field_nautical = new LightMapKey(dialogView, R.id.info_time_lightmap_key_nautical_icon, R.id.info_time_lightmap_key_nautical_label, R.id.info_time_lightmap_key_nautical_duration);
        field_civil = new LightMapKey(dialogView, R.id.info_time_lightmap_key_civil_icon, R.id.info_time_lightmap_key_civil_label, R.id.info_time_lightmap_key_civil_duration);
        field_day = new LightMapKey(dialogView, R.id.info_time_lightmap_key_day_icon, R.id.info_time_lightmap_key_day_label, R.id.info_time_lightmap_key_day_duration);

        riseIcon = (ImageView)dialogView.findViewById(R.id.sundialog_riseicon);
        setIcon = (ImageView)dialogView.findViewById(R.id.sundialog_seticon);

        playButton = (ImageButton)dialogView.findViewById(R.id.media_play);
        if (playButton != null) {
            playButton.setOnClickListener(playClickListener);
        }

        pauseButton = (ImageButton)dialogView.findViewById(R.id.media_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(pauseClickListener);
        }

        resetButton = (ImageButton)dialogView.findViewById(R.id.media_reset);
        if (resetButton != null) {
            resetButton.setEnabled(false);
            TooltipCompat.setTooltipText(resetButton, resetButton.getContentDescription());
            resetButton.setOnClickListener(resetClickListener);
        }

        nextButton = (ImageButton)dialogView.findViewById(R.id.media_next);
        if (nextButton != null) {
            TooltipCompat.setTooltipText(nextButton, nextButton.getContentDescription());
            nextButton.setOnClickListener(nextClickListener);
        }

        prevButton = (ImageButton)dialogView.findViewById(R.id.media_prev);
        if (prevButton != null) {
            TooltipCompat.setTooltipText(prevButton, prevButton.getContentDescription());
            prevButton.setOnClickListener(prevClickListener);
        }

        menuButton = (ImageButton)dialogView.findViewById(R.id.media_menu);
        if (menuButton != null)
        {
            TooltipCompat.setTooltipText(menuButton, menuButton.getContentDescription());
            menuButton.setOnClickListener(menuClickListener);
            if (AppSettings.isTelevision(getActivity())) {
                menuButton.setFocusableInTouchMode(true);
            }
        }

        ImageButton lightgraphButton = (ImageButton) dialogView.findViewById(R.id.lightgraph_button);
        if (lightgraphButton != null)
        {
            TooltipCompat.setTooltipText(lightgraphButton, lightgraphButton.getContentDescription());
            lightgraphButton.setOnClickListener(onLightGraphButtonClicked);
        }

        speedButton = (TextView)dialogView.findViewById(R.id.media_speed);
        if (speedButton != null) {
            speedButton.setOnClickListener(speedClickListener);
        }

        mediaGroup = dialogView.findViewById(R.id.media_actions);

        if (graphView != null)
        {
            boolean showGraph = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_LIGHTMAP_SHOWGRAPH, MAPTAG_LIGHTMAP, DEF_KEY_LIGHTMAP_SHOWGRAPH);
            resizeLightMapView(context, showGraph);
            graphView.setVisibility(showGraph ? View.VISIBLE : View.GONE);
            /*graphView.setMapTaskListener(new LineGraphView.LineGraphTaskListener() {
                @Override
                public void onDataModified(SuntimesRiseSetDataset data) {}
                @Override
                public void onFrame(Bitmap frame, long offsetMinutes) {}
            });*/
        }

        if (lightmap != null)
        {
            lightmap.setMapTaskListener(new LightMapView.LightMapTaskListener()
            {
                @Override
                public void onDataModified( SuntimesRiseSetDataset data ) {
                    LightMapDialog.this.data = data;
                    updateLightmapKeyViews(data);
                    if (BuildConfig.DEBUG) {
                        Log.d("DEBUG", "onDataModified: " + data.calendar().get(Calendar.DAY_OF_YEAR));
                    }
                    //if (graphView != null && graphView.getVisibility() == View.VISIBLE) {
                    //    graphView.updateViews(data);
                    //}
                }

                @Override
                public void onFrame(Bitmap frame, long offsetMinutes)
                {
                    //getArguments().putLong(EXTRA_DATETIME, lightmap.getNow());
                    updateTimeText(data);
                    updateSunPositionViews(data);
                    resetButton.setEnabled(offsetMinutes != 0);

                    //if (graphView != null && graphView.getVisibility() == View.VISIBLE)
                    //{
                    //    Log.d("DEBUG", "offset is " + offsetMinutes);
                    //    graphView.getOptions().offsetMinutes = offsetMinutes;
                    //    graphView.updateViews(true);
                    //}
                }
            });
        }
        updateOptions(getContext());
    }

    private final View.OnClickListener onLightGraphButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (dialogListener != null) {
                dialogListener.onShowChart(lightmap.getNow());
            }
        }
    };

    protected void resizeLightMapView(Context context, boolean showGraph)
    {
        if (lightmap != null) {
            ViewGroup.LayoutParams params = lightmap.getLayoutParams();
            params.height = SuntimesUtils.dpToPixels(context, (showGraph ? 14 : 32));
            lightmap.setLayoutParams(params);
        }
    }

    public static final String MAPTAG_LIGHTMAP = "_lightmap";
    public static final String PREF_KEY_LIGHTMAP_SHOWGRAPH = "showgraph";
    public static final boolean DEF_KEY_LIGHTMAP_SHOWGRAPH = false;
    public static final String PREF_KEY_LIGHTMAP_SEEKALTITUDE = "seekaltitude";
    public static final String DEF_KEY_LIGHTMAP_SEEKALTITUDE = "";

    public static final String PREF_KEY_GRAPH_SHOWMOON = "showmoon";
    public static final boolean DEF_KEY_GRAPH_SHOWMOON = false;

    public static final String PREF_KEY_GRAPH_SHOWLABELS = "showlabels";
    public static final boolean DEF_KEY_GRAPH_SHOWLABELS = true;

    public static final String PREF_KEY_GRAPH_SHOWAXIS = "showaxis";
    public static final boolean DEF_KEY_GRAPH_SHOWAXIS = true;

    public static final String PREF_KEY_GRAPH_FILLPATH = "fillpath";
    public static final boolean DEF_KEY_GRAPH_FILLPATH = true;

    public static final boolean DEF_KEY_WORLDMAP_MINORGRID = false;

    private final View.OnClickListener playClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            playMap();
            if (AppSettings.isTelevision(getActivity())) {
                if (pauseButton != null) {
                    pauseButton.requestFocus();
                }
            }
        }
    };
    private final View.OnClickListener pauseClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
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
        public void onClick(View v)
        {
            stopMap(true);
            updateLightmapKeyViews(data);
            if (AppSettings.isTelevision(getActivity())) {
                if (playButton != null) {
                    playButton.requestFocus();
                }
            }
        }
    };
    private final View.OnClickListener menuClickListener = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showContextMenu(getContext(), v);
        }
    });
    private final View.OnClickListener speedClickListener = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showSpeedMenu(getContext(), v);
        }
    });
    private View.OnClickListener nextClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                boolean speed1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP);
                lightmap.setOffsetMinutes(lightmap.getOffsetMinutes() + (speed1d ? WorldMapDialog.SEEK_STEPSIZE_1d : WorldMapDialog.SEEK_STEPSIZE_5m));
                if (graphView != null) {
                    graphView.setOffsetMinutes(graphView.getOffsetMinutes() + (speed1d ? WorldMapDialog.SEEK_STEPSIZE_1d : WorldMapDialog.SEEK_STEPSIZE_5m));
                }
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
                boolean speed1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP);
                lightmap.setOffsetMinutes(lightmap.getOffsetMinutes() - (speed1d ? WorldMapDialog.SEEK_STEPSIZE_1d : WorldMapDialog.SEEK_STEPSIZE_5m));
                if (graphView != null) {
                    graphView.setOffsetMinutes(graphView.getOffsetMinutes() - (speed1d ? WorldMapDialog.SEEK_STEPSIZE_1d : WorldMapDialog.SEEK_STEPSIZE_5m));
                }
            }
        }
    };



    private final PopupMenu.OnMenuItemClickListener onContextMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
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
                case R.id.graphOption_colors:
                    showColorDialog(getActivity());
                    return true;

                case R.id.action_showgraph:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_LIGHTMAP_SHOWGRAPH, MAPTAG_LIGHTMAP, DEF_KEY_LIGHTMAP_SHOWGRAPH);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_LIGHTMAP_SHOWGRAPH, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    resizeLightMapView(context, toggledValue);
                    graphView.setVisibility(toggledValue ? View.VISIBLE : View.GONE);
                    graphView.post(new Runnable() {
                        @Override
                        public void run() {
                            initPeekHeight(getDialog());
                        }
                    });
                    updateViews();
                    return true;

                case R.id.graphOption_showGrid:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, DEF_KEY_WORLDMAP_MINORGRID);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.graphOption_showLabels:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWLABELS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.graphOption_showAxis:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWAXIS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.graphOption_fillPath:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.graphOption_showMoon:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWMOON);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews();
                    return true;

                case R.id.action_date:
                    if (dialogListener != null) {
                        dialogListener.onShowDate(getMapTime(System.currentTimeMillis()));
                    }
                    return true;

                case R.id.action_calendar:
                    openCalendar(context, getMapTime(System.currentTimeMillis()));
                    return true;

                case R.id.action_worldmap:
                    if (dialogListener != null) {
                        dialogListener.onShowMap(getMapTime(System.currentTimeMillis()));
                    }
                    return true;

                case R.id.action_seekaltitude:
                    showSeekAltitudePopup(context, sunElevation);
                    return true;

                case R.id.action_moon:
                    if (dialogListener != null) {
                        dialogListener.onShowMoonInfo(getMapTime(System.currentTimeMillis()));
                    }
                    return true;

                case R.id.action_observerheight:
                    showShadowObjHeightPopup(context, sunShadowObj);
                    return true;

                case R.id.action_timezone:
                    showTimeZoneMenu(context, sunTime);
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    });

    private void updateContextMenu(Context context, PopupMenu popupMenu)
    {
        Menu menu = popupMenu.getMenu();

        MenuItem showGraphItem = menu.findItem(R.id.action_showgraph);
        if (showGraphItem != null) {
            showGraphItem.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_LIGHTMAP_SHOWGRAPH, MAPTAG_LIGHTMAP, DEF_KEY_LIGHTMAP_SHOWGRAPH));
        }
        MenuItem graphOption_showGrid = menu.findItem(R.id.graphOption_showGrid);
        if (graphOption_showGrid != null) {
            graphOption_showGrid.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, DEF_KEY_WORLDMAP_MINORGRID));
        }
        MenuItem graphOption_showLabels = menu.findItem(R.id.graphOption_showLabels);
        if (graphOption_showLabels != null) {
            graphOption_showLabels.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWLABELS));
        }
        MenuItem graphOption_showAxis = menu.findItem(R.id.graphOption_showAxis);
        if (graphOption_showAxis != null) {
            graphOption_showAxis.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWAXIS));
        }
        MenuItem graphOption_fillPath = menu.findItem(R.id.graphOption_fillPath);
        if (graphOption_fillPath != null) {
            graphOption_fillPath.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH));
        }
        MenuItem graphOption_showMoon = menu.findItem(R.id.graphOption_showMoon);
        if (graphOption_showMoon != null) {
            graphOption_showMoon.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWMOON));
        }

        MenuItem submenuItem = menu.findItem(R.id.addonSubMenu);
        if (submenuItem != null) {
            List<MenuAddon.ActivityItemInfo> addonMenuItems = MenuAddon.queryAddonMenuItems(context);
            if (!addonMenuItems.isEmpty()) {
                MenuAddon.populateSubMenu(submenuItem, addonMenuItems, getMapTime(System.currentTimeMillis()));
            } //else submenuItem.setVisible(false);
        }
    }

    protected boolean showContextMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.lightmapmenu, menu.getMenu());
        menu.setOnMenuItemClickListener(onContextMenuClick);
        updateContextMenu(context, menu);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

    protected boolean showSpeedMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.mapmenu_speed1, menu.getMenu());
        menu.setOnMenuItemClickListener(onSpeedMenuClick);

        updateSpeedMenu(context, menu);
        menu.show();
        return true;
    }

    private void updateSpeedMenu(Context context, PopupMenu menu)
    {
        Menu m = menu.getMenu();
        boolean is1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP);
        //Log.d("DEBUG", "updateSpeedMenu: is1d: " + is1d);

        MenuItem speed_5m = m.findItem(R.id.mapSpeed_5m);
        if (speed_5m != null) {
            speed_5m.setChecked(!is1d);
        }

        MenuItem speed_1d = m.findItem(R.id.mapSpeed_1d);
        if (speed_1d != null) {
            speed_1d.setChecked(is1d);
        }
    }

    private final PopupMenu.OnMenuItemClickListener onSpeedMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
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
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP, true);
                    //Log.d("DEBUG", "onSpeedMenuClick: is1d: true");
                    item.setChecked(true);
                    updateViews();
                    return true;

                case R.id.mapSpeed_5m:
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP, false);
                    //Log.d("DEBUG", "onSpeedMenuClick: is1d: false");
                    item.setChecked(true);
                    updateViews();
                    return true;

                default:
                    return false;
            }
        }
    });

    protected boolean showTimeZoneMenu(Context context, View view)
    {
        PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.lightmapmenu_tz, onTimeZoneMenuClick);
        WidgetTimezones.updateTimeZoneMenu(menu.getMenu(), WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTMAP, WidgetTimezones.LocalMeanTime.TIMEZONEID));
        menu.show();
        return true;
    }
    private final PopupMenu.OnMenuItemClickListener onTimeZoneMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context != null) {
                String tzID = WidgetTimezones.timeZoneForMenuItem(item.getItemId());
                if (tzID != null) {
                    WorldMapWidgetSettings.saveWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTMAP, tzID);
                    updateViews();
                }
                return (tzID != null);
            } else return false;
        }
    });

    private void updateMediaButtons()
    {
        if (mediaGroup != null)
        {
            boolean isAnimated = lightmap.isAnimated();
            if (isAnimated)
            {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);

            } else {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }

            //resetButton.setEnabled(lightmap != null && lightmap.getColors().offsetMinutes != 0);
        }

        Context context = getContext();
        if (speedButton != null && context != null)
        {
            boolean speed_1d = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP);
            //Log.d("DEBUG", "updateMediaButtons: is1d: " + speed_1d);
            speedButton.setText( context.getString(speed_1d ? R.string.worldmap_dialog_speed_1d : R.string.worldmap_dialog_speed_5m));
            speedButton.setTextColor( speed_1d ? color_warning : color_accent );
        }
    }

    public void updateOptions(Context context)
    {
        if (context != null)
        {
            LightMapView.LightMapColors options = lightmap.getColors();
            long now = getArguments().getLong(ARG_DATETIME);
            if (now != -1L)
            {
                getArguments().putLong(ARG_DATETIME, -1L);
                options.now = now;
                options.offsetMinutes = 1;
                //Log.d("DEBUG", "updateOptions: now: " + now);
            }
            options.anim_lock = anim_lock;
            options.anim_frameOffsetMinutes = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_SPEED1D, MAPTAG_LIGHTMAP)
                    ? 24 * 60 : 1;

            if (graphView != null && graphView.getVisibility() == View.VISIBLE)
            {
                LineGraphView.LineGraphOptions options1 = graphView.getOptions();
                options1.now = options.now;
                options1.timezone = getSelectedTZ(context, data);
                options1.is24 = SuntimesUtils.is24();
                options1.offsetMinutes = options.offsetMinutes;
                options1.anim_frameOffsetMinutes = options.anim_frameOffsetMinutes;
                options1.graph_width = LineGraphView.MINUTES_IN_DAY;
                options1.graph_height = 180;
                options1.graph_x_offset = options1.graph_y_offset = 0;
                options1.gridX_minor_show = options1.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, DEF_KEY_WORLDMAP_MINORGRID);
                options1.axisX_labels_show = options1.axisY_labels_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWLABELS);
                options1.axisX_show = options1.axisY_show = options1.gridY_major_show = options1.gridX_major_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWAXIS);
                options1.sunPath_show_line = true;
                options1.sunPath_show_fill = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH);
                options1.moonPath_show_line = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWMOON);
                options1.moonPath_show_fill = options1.sunPath_show_fill;
                options1.anim_lock = anim_lock;
            }
        }
    }

    private void playMap()
    {
        lightmap.startAnimation();
        if (graphView != null && graphView.getVisibility() == View.VISIBLE) {
            graphView.startAnimation();
        }
        updateMediaButtons();
    }

    private void stopMap(boolean reset)
    {
        if (reset) {
            lightmap.resetAnimation(true);
            if (graphView != null && graphView.getVisibility() == View.VISIBLE) {
                graphView.resetAnimation(true);
            }
        } else {
            lightmap.stopAnimation();
            if (graphView != null && graphView.getVisibility() == View.VISIBLE) {
                graphView.stopAnimation();
            }
        }
        updateMediaButtons();
    }

    @Override
    public void onSaveInstanceState( Bundle state ) {
        lightmap.saveSettings(state);
        if (graphView != null) {
            graphView.saveSettings(state);
        }
    }
    protected void loadSettings(Bundle bundle)
    {
        lightmap.loadSettings(getContext(), bundle);
        if (graphView != null) {
            graphView.loadSettings(getContext(), bundle);
        }
    }

    private final View.OnClickListener onSunriseLayoutClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(@NonNull View v)
        {
            Context context = getContext();
            if (context != null) {
                seekSunrise(context);
            }
        }
    });
    private final View.OnClickListener onSunsetLayoutClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(@NonNull View v)
        {
            Context context = getContext();
            if (context != null) {
                seekSunset(context);
            }
        }
    });
    private final View.OnClickListener onNoonLayoutClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(@NonNull View v)
        {
            Context context = getContext();
            if (context != null) {
                seekNoon(context);
            }
        }
    });

    private final View.OnClickListener onAltitudeLayoutClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(@NonNull View v)
        {
            Context context = getContext();
            if (context != null) {
                showSeekAltitudePopup(context, v);
            }
        }
    });

    private PopupWindow seekAltitudePopup = null;
    protected void showSeekAltitudePopup(@NonNull final Context context, @NonNull View v)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            PopupWindow popupWindow = new PopupWindow(createSeekAltitudePopupView(context), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    seekAltitudePopup = null;
                }
            });
            seekAltitudePopup = popupWindow;
            popupWindow.showAsDropDown(v);
        }
    }
    protected void dismissSeekAltitudePopup()
    {
        if (seekAltitudePopup != null) {
            seekAltitudePopup.dismiss();
        }
    }
    protected View createSeekAltitudePopupView(@NonNull final Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            @SuppressLint("InflateParams")
            View popupView = inflater.inflate(R.layout.layout_dialog_seekaltitude, null);
            if (popupView != null)
            {
                String lastInput = WorldMapWidgetSettings.loadWorldMapString(context, 0, PREF_KEY_LIGHTMAP_SEEKALTITUDE, MAPTAG_LIGHTMAP, DEF_KEY_LIGHTMAP_SEEKALTITUDE);
                final EditText editText = (EditText) popupView.findViewById(R.id.edit_altitude);
                if (editText != null)
                {
                    /*editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
                    {
                        private final View.OnClickListener onEditorDone = onSeekAltitudeClicked(context, editText, true);
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                onEditorDone.onClick(v);
                            }
                            return false;
                        }
                    });*/
                    editText.setText(lastInput);
                    editText.selectAll();
                    editText.requestFocus();
                }
                final Button risingButton = (Button) popupView.findViewById(R.id.button_rising);
                if (risingButton != null) {
                    risingButton.setOnClickListener(onSeekAltitudeClicked(context, editText, true));
                }
                final Button settingButton = (Button) popupView.findViewById(R.id.button_setting);
                if (settingButton != null) {
                    settingButton.setOnClickListener(onSeekAltitudeClicked(context, editText, false));
                }
            }
            return popupView;
        }
        return null;
    }

    protected View.OnClickListener onSeekAltitudeClicked(final Context context, final EditText edit, final boolean rising)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit != null) {
                    try {
                        double input = Double.parseDouble(edit.getText().toString());
                        if (seekAltitude(context, input, rising) != null) {
                            dismissSeekAltitudePopup();
                        } else {
                            Toast.makeText(context, context.getString(R.string.schedalarm_dialog_note2, utils.formatAsElevation(input, 0)), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Log.w(getClass().getSimpleName(), "onSeekAltitudeClicked: Failed to parse input; " + e);
                    }
                }
            }
        };
    }

    public Long seekAltitude( Context context, @Nullable Double degrees, boolean rising )
    {
        WorldMapWidgetSettings.saveWorldMapString(context, 0, PREF_KEY_LIGHTMAP_SEEKALTITUDE, MAPTAG_LIGHTMAP, (degrees != null ? degrees + "" : ""));
        if (degrees != null) {
            return seekDateTime(context, lightmap.findAltitude(context, (int)((double)degrees), rising));
        } else return null;
    }
    public Long seekNoon(Context context) {
        return seekDateTime(context, data.dataNoon.sunriseCalendarToday());
    }
    public Long seekSunrise(Context context) {
        return seekDateTime(context, data.dataActual.sunriseCalendarToday());
    }
    public Long seekSunset(Context context) {
        return seekDateTime(context, data.dataActual.sunsetCalendarToday());
    }
    public Long seekDateTime( Context context, @Nullable Calendar calendar ) {
        return (calendar != null ? seekDateTime(context, calendar.getTimeInMillis()) : null);
    }
    public Long seekDateTime( Context context, Long datetime )
    {
        if (datetime != null)
        {
            stopMap(false);
            if (graphView != null) {
                graphView.seekDateTime(context, datetime);
            }
            lightmap.seekDateTime(context, datetime);
        }
        return datetime;
    }

    private final View.OnClickListener onShadowLayoutClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(@NonNull View v)
        {
            Context context = getContext();
            if (context != null) {
                showShadowObjHeightPopup(context, v);
            }
        }
    });
    protected void showShadowObjHeightPopup(@NonNull final Context context, @NonNull View v)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            PopupWindow popupWindow = new PopupWindow(createShadowObjHeightPopupView(context), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAsDropDown(v);
        }
    }
    protected View createShadowObjHeightPopupView(@NonNull final Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            @SuppressLint("InflateParams")
            View popupView = inflater.inflate(R.layout.layout_dialog_objheight, null);
            if (popupView != null)
            {
                SeekBar seekbar = (SeekBar) popupView.findViewById(R.id.seek_objheight);
                if (seekbar != null)
                {
                    int centimeters = (int) (WidgetSettings.loadObserverHeightPref(context, 0) * 100) + 1;
                    centimeters = (centimeters < 1 ? 1 : Math.min(centimeters, SEEK_CENTIMETERS_MAX));
                    seekbar.setMax(SEEK_CENTIMETERS_MAX);
                    if (Build.VERSION.SDK_INT >= 24) {
                        seekbar.setProgress(centimeters, false);
                    } else {
                        seekbar.setProgress(centimeters);
                    }
                    seekbar.setOnSeekBarChangeListener(onObjectHeightSeek);
                }
                ImageButton moreButton = (ImageButton) popupView.findViewById(R.id.btn_more);
                if (moreButton != null) {
                    moreButton.setOnClickListener(onObjectHeightMoreLess(true));
                }
                ImageButton lessButton = (ImageButton) popupView.findViewById(R.id.btn_less);
                if (lessButton != null) {
                    lessButton.setOnClickListener(onObjectHeightMoreLess(false));
                }
            }
            return popupView;
        }
        return null;
    }
    private View.OnClickListener onObjectHeightMoreLess( final boolean more ) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                if (context != null) {
                    float currentHeight = WidgetSettings.loadObserverHeightPref(context, 0);
                    WidgetSettings.saveObserverHeightPref(getContext(), 0, currentHeight + ((more ? 1 : -1) * (SEEK_CENTIMETERS_INC / 100f)));
                    updateViews();
                }
            }
        };
    }
    private SeekBar.OnSeekBarChangeListener onObjectHeightSeek = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int centimeters, boolean fromUser)
        {
            Context context = getContext();
            if (fromUser && context != null)
            {
                if (centimeters < 1) {
                    centimeters = 1;
                }
                WidgetSettings.saveObserverHeightPref(getContext(), 0, (centimeters / 100f));
                updateViews();
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private static final int SEEK_CENTIMETERS_MAX = 5 * 100;
    private static final int SEEK_CENTIMETERS_INC = 1;

    @SuppressWarnings("ResourceType")
    public void themeViews(@Nullable Context context)
    {
        if (context == null) {
            return;
        }
        updateLightmapColors(context);

        int[] colorAttrs = { R.attr.graphColor_night,   // 0
                R.attr.graphColor_astronomical,         // 1
                R.attr.graphColor_nautical,             // 2
                R.attr.graphColor_civil,                // 3
                R.attr.graphColor_day,                  // 4
                R.attr.sunriseColor,                    // 5
                R.attr.sunsetColor,                     // 6
                R.attr.text_disabledColor,              // 7
                R.attr.buttonPressColor,                // 8
                android.R.attr.textColorPrimary,        // 9
                R.attr.text_accentColor,                // 10
                R.attr.tagColor_warning,                // 11
                R.attr.table_risingColor,               // 12
                R.attr.table_settingColor,              // 13
                R.attr.table_civilColor,                // 14
                R.attr.table_nauticalColor,             // 15
                R.attr.table_astroColor,                // 16
                R.attr.table_nightColor,                // 17
        };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = R.color.transparent;
        colorNight = ContextCompat.getColor(context, typedArray.getResourceId(0, def));
        colorAstro = ContextCompat.getColor(context, typedArray.getResourceId(1, def));
        colorNautical = ContextCompat.getColor(context, typedArray.getResourceId(2, def));
        colorCivil = ContextCompat.getColor(context, typedArray.getResourceId(3, def));
        colorDay = ContextCompat.getColor(context, typedArray.getResourceId(4, def));
        colorRising = ContextCompat.getColor(context, typedArray.getResourceId(5, def));
        colorSetting = ContextCompat.getColor(context, typedArray.getResourceId(6, def));
        color_disabled = ContextCompat.getColor(context, typedArray.getResourceId(7, Color.GRAY));
        color_pressed = ContextCompat.getColor(context, typedArray.getResourceId(8, Color.BLUE));
        color_normal = ContextCompat.getColor(context, typedArray.getResourceId(9, Color.WHITE));
        color_accent = ContextCompat.getColor(context, typedArray.getResourceId(10, Color.YELLOW));
        color_warning = ContextCompat.getColor(context, typedArray.getResourceId(11, Color.YELLOW));
        colorRisingLabel = ContextCompat.getColor(context, typedArray.getResourceId(12, def));
        colorSettingLabel = ContextCompat.getColor(context, typedArray.getResourceId(13, def));
        colorCivilLabel = ContextCompat.getColor(context, typedArray.getResourceId(14, def));
        colorNauticalLabel = ContextCompat.getColor(context, typedArray.getResourceId(15, def));
        colorAstroLabel = ContextCompat.getColor(context, typedArray.getResourceId(16, def));
        colorLabel = ContextCompat.getColor(context, typedArray.getResourceId(17, def));
        typedArray.recycle();

        if (themeOverride != null)
        {
            int titleColor = themeOverride.getTitleColor();
            float textSizeSp = themeOverride.getTextSizeSp();
            float titleSizeSp = themeOverride.getTitleSizeSp();
            float timeSizeSp = themeOverride.getTimeSizeSp();
            float suffixSizeSp = themeOverride.getTimeSuffixSizeSp();

            color_pressed = color_warning = themeOverride.getActionColor();
            color_normal = themeOverride.getTitleColor();
            color_accent = themeOverride.getAccentColor();

            dialogTitle.setTextColor(titleColor);
            dialogTitle.setTextSize(titleSizeSp);
            dialogTitle.setTypeface(dialogTitle.getTypeface(), (themeOverride.getTitleBold() ? Typeface.BOLD : Typeface.NORMAL));

            sunTime.setTextColor(titleColor);
            sunTime.setTextSize(timeSizeSp);

            offsetTime.setTextColor(themeOverride.getTimeColor());
            offsetTime.setTextSize(timeSizeSp);

            sunElevationLabel.setTextColor(titleColor);
            sunElevationLabel.setTextSize(suffixSizeSp);

            sunAzimuthLabel.setTextColor(titleColor);
            sunAzimuthLabel.setTextSize(suffixSizeSp);

            lightmap.themeViews(context, themeOverride);
            if (graphView != null) {
                graphView.themeViews(context, themeOverride);
            }

            colorNight = themeOverride.getNightColor();
            colorDay = themeOverride.getDayColor();
            colorAstro = themeOverride.getAstroColor();
            colorNautical = themeOverride.getNauticalColor();
            colorCivil = themeOverride.getCivilColor();
            colorRising = themeOverride.getSunriseTextColor();
            colorSetting = themeOverride.getSunsetTextColor();
            colorRisingLabel = colorRising;
            colorSettingLabel = colorSetting;
            colorCivilLabel = colorCivil;
            colorNauticalLabel = colorNautical;
            colorAstroLabel = colorAstro;

            field_astro.themeViews(themeOverride);
            field_nautical.themeViews(themeOverride);
            field_civil.themeViews(themeOverride);
            field_day.themeViews(themeOverride);
            field_night.themeViews(themeOverride);

            sunAzimuth.setTextColor(themeOverride.getTimeColor());
            sunAzimuth.setTextSize(timeSizeSp);
            sunAzimuthRising.setTextSize(timeSizeSp);
            sunAzimuthSetting.setTextSize(timeSizeSp);

            sunElevation.setTextColor(themeOverride.getTimeColor());
            sunElevation.setTextSize(timeSizeSp);
            sunElevationAtNoon.setTextSize(timeSizeSp);

            sunAzimuthAtNoon.setTextColor(themeOverride.getTimeColor());
            sunAzimuthAtNoon.setTextSize(timeSizeSp);

            sunShadowObj.setTextColor(themeOverride.getTitleColor());
            sunShadowObj.setTextSize(timeSizeSp);

            sunShadowLength.setTextColor(themeOverride.getTimeColor());
            sunShadowLength.setTextSize(timeSizeSp);

            sunShadowLengthAtNoon.setTextColor(themeOverride.getSunsetTextColor());
            sunShadowLengthAtNoon.setTextSize(timeSizeSp);
        }

        AppColorValues colors = initGraphColorValues(context);
        if (colors != null)
        {
            colorNight = colors.getColor(AppColorKeys.COLOR_NIGHT);
            colorAstro = colors.getColor(AppColorKeys.COLOR_ASTRONOMICAL);
            colorNautical = colors.getColor(AppColorKeys.COLOR_NAUTICAL);
            colorCivil = colors.getColor(AppColorKeys.COLOR_CIVIL);
            colorDay = colors.getColor(AppColorKeys.COLOR_DAY);
            colorRising = colors.getColor(AppColorKeys.COLOR_RISING_SUN);
            colorRisingLabel = colors.getColor(AppColorKeys.COLOR_RISING_SUN_TEXT);
            colorSetting = colors.getColor(AppColorKeys.COLOR_SETTING_SUN);
            colorSettingLabel = colors.getColor(AppColorKeys.COLOR_SETTING_SUN_TEXT);
        }

        ImageViewCompat.setImageTintList(playButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(resetButton, SuntimesUtils.colorStateList(color_warning, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(pauseButton, SuntimesUtils.colorStateList(color_accent, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(nextButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(prevButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        ImageViewCompat.setImageTintList(menuButton, SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));

        if (speedButton != null) {
            speedButton.setTextColor(SuntimesUtils.colorStateList(color_normal, color_disabled, color_pressed));
        }

        SuntimesUtils.colorizeImageView(field_night.icon, colorNight);
        SuntimesUtils.colorizeImageView(field_astro.icon, colorAstro);
        SuntimesUtils.colorizeImageView(field_nautical.icon, colorNautical);
        SuntimesUtils.colorizeImageView(field_civil.icon, colorCivil);
        SuntimesUtils.colorizeImageView(field_day.icon, colorDay);

        if (themeOverride != null) {
            SuntimesUtils.tintDrawable((InsetDrawable)riseIcon.getBackground(), themeOverride.getSunriseIconColor(), themeOverride.getSunriseIconStrokeColor(), themeOverride.getSunriseIconStrokePixels(context));
            SuntimesUtils.tintDrawable((InsetDrawable)setIcon.getBackground(), themeOverride.getSunsetIconColor(), themeOverride.getSunsetIconStrokeColor(), themeOverride.getSunsetIconStrokePixels(context));
        } else {
            SuntimesUtils.tintDrawable((InsetDrawable)riseIcon.getBackground(), colorRising, colorRising, getResources().getDimensionPixelSize(R.dimen.sunIcon_width_risingBorder));
            SuntimesUtils.tintDrawable((InsetDrawable)setIcon.getBackground(), colorSetting, colorSetting, getResources().getDimensionPixelSize(R.dimen.sunIcon_width_settingBorder));
        }

        //colorLabel = field_night.label.getTextColors().getColorForState(new int[] { -android.R.attr.state_enabled }, Color.BLUE); // field_night.label.getCurrentTextColor()
    }

    private SuntimesTheme themeOverride = null;
    @Deprecated
    public void themeViews(Context context, @Nullable SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (lightmap != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        updateOptions(getContext());
        updateMediaButtons();
        if (data != null) {
            updateViews(data);
        }
    }

    protected void updateViews( @NonNull SuntimesRiseSetDataset data )
    {
        stopUpdateTask();
        updateLightmapViews(data);
        updateGraphViews(data);
        updateSunPositionViews(data);
        updateTimeText(data);
        startUpdateTask();
    }

    protected void updateLightmapColors(Context context)
    {
        AppColorValues values = initGraphColorValues(context);

        if (lightmap != null)
        {
            if (values != null) {
                lightmap.getColors().values = new LightMapColorValues(values);
            } else if (lightmap.getColors().values == null) {
                lightmap.getColors().init(context);
            }
        }

        if (graphView != null)
        {
            if (values != null) {
                graphView.getOptions().colors = new LineGraphColorValues(values);
            } else if (graphView.getOptions().colors == null) {
                graphView.getOptions().init(context);
            }
            graphView.updateViews(graphView.getVisibility() == View.VISIBLE ? data : null);
        }
    }

    protected void updateLightmapViews(@NonNull SuntimesRiseSetDataset data)
    {
        Context context = getContext();
        if (context == null || data == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "updateLightmapViews");
        }

        updateLightmapKeyViews(data);
        if (lightmap != null) {
            lightmap.updateViews(data);
        }
    }
    protected void updateLightmapKeyViews(@NonNull SuntimesRiseSetDataset data)
    {
        Context context = getContext();
        if (context == null || data == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "updateLightmapKeyViews");
        }

        field_civil.updateInfo(context, createInfoArray(data.civilTwilightLength()));
        field_civil.highlight(false);

        field_nautical.updateInfo(context, createInfoArray(data.nauticalTwilightLength()));
        field_nautical.highlight(false);

        field_astro.updateInfo(context, createInfoArray(data.astroTwilightLength()));
        field_astro.highlight(false);

        CharSequence nightLabel = LightMapView.getLabel(context, data);
        Drawable nightDrawable = null;
        boolean replaceNightLabel;
        if (replaceNightLabel = (data.nightLength() == 0))
        {
            if (data.civilTwilightLength()[1] <= 0) {
                nightDrawable = field_day.getDefaultIconDrawable();

            } else if (data.nauticalTwilightLength()[1] <= 0) {
                nightDrawable = field_civil.getDefaultIconDrawable();

            } else if (data.astroTwilightLength()[1] <= 0) {
                nightDrawable = field_nautical.getDefaultIconDrawable();

            } else {
                nightDrawable = field_astro.getDefaultIconDrawable();
            }
        }

        field_night.setLabelText(replaceNightLabel ? nightLabel : null);    // null resets label
        field_night.updateInfo(context, createInfoArray(new long[]{data.nightLength()}), " ");
        field_night.setIconDrawable(replaceNightLabel ? nightDrawable : null);    // null resets icon
        field_night.highlight(false);

        CharSequence dayLabel = null;
        Drawable dayDrawable = null;
        boolean replaceDayLabel;
        if (replaceDayLabel = (data.dayLength() <= 0))
        {
            dayDrawable = field_civil.getDefaultIconDrawable();
            dayLabel = context.getString(R.string.timeMode_polarnight);
        }

        long dayDelta = data.dayLengthOther() - data.dayLength();
        field_day.setLabelText(replaceDayLabel ? dayLabel : null);    // null resets label
        field_day.updateInfo(context, createInfoArray(data.dayLength(), dayDelta, colorRisingLabel), " ");
        field_day.setIconDrawable(replaceDayLabel ? dayDrawable : null);    // null resets icon
        field_day.highlight(false);
    }
    protected void updateGraphViews(@NonNull SuntimesRiseSetDataset data)
    {
        Context context = getContext();
        if (context == null || data == null) {
            return;
        }
        if (graphView != null) {
            graphView.updateViews(graphView.getVisibility() == View.VISIBLE ? data : null);
        }
    }

    @Nullable
    protected AppColorValues initGraphColorValues(Context context)
    {
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        return (AppColorValues) colors.getSelectedColors(context, (isNightMode ? 1 : 0), AppColorValues.TAG_APPCOLORS);
    }

    private void styleAzimuthText(TextView view, double azimuth, Integer color, int places)
    {
        SuntimesUtils.TimeDisplayText azimuthText = utils.formatAsDirection2(azimuth, places, false);
        String azimuthString = utils.formatAsDirection(azimuthText.getValue(), azimuthText.getSuffix());
        SpannableString azimuthSpan = null;
        if (color != null) {
            //noinspection ConstantConditions
            azimuthSpan = SuntimesUtils.createColorSpan(azimuthSpan, azimuthString, azimuthString, color);
        }
        azimuthSpan = SuntimesUtils.createRelativeSpan(azimuthSpan, azimuthString, azimuthText.getSuffix(), 0.7f);
        azimuthSpan = SuntimesUtils.createBoldSpan(azimuthSpan, azimuthString, azimuthText.getSuffix());
        view.setText(azimuthSpan);

        SuntimesUtils.TimeDisplayText azimuthDesc = utils.formatAsDirection2(azimuth, places, true);
        view.setContentDescription(utils.formatAsDirection(azimuthDesc.getValue(), azimuthDesc.getSuffix()));
    }

    private CharSequence styleElevationText(double elevation, @Nullable Integer color, int places)
    {
        SuntimesUtils.TimeDisplayText elevationText = utils.formatAsElevation(elevation, places);
        String elevationString = utils.formatAsElevation(elevationText.getValue(), elevationText.getSuffix());
        SpannableString span = null;
        //noinspection ConstantConditions
        span = SuntimesUtils.createRelativeSpan(span, elevationString, elevationText.getSuffix(), 0.7f);
        if (color != null) {
            span = SuntimesUtils.createColorSpan(span, elevationString, elevationString, color);
        }
        return (span != null ? span : elevationString);
    }

    private CharSequence styleLengthText(@NonNull Context context, double meters, WidgetSettings.LengthUnit units)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);
        if (meters < Double.POSITIVE_INFINITY)
            return SuntimesUtils.formatAsDistance(context, SuntimesUtils.formatAsHeight(context, meters, units, 2, true));
        else return formatter.format(meters);
    }

    private int getColorForPosition(SuntimesCalculator.SunPosition position, SuntimesCalculator.SunPosition noonPosition)
    {
        if (position.elevation >= 0)
            return (SuntimesRiseSetDataset.isRising(position, noonPosition) ? colorRising : colorSetting);

        if (position.elevation >= -6)
            return colorCivil;

        if (position.elevation >= -12)
            return colorNautical;

        if (position.elevation >= -18)
            return colorAstro;

        return colorLabel;
    }

    private CharSequence getTextForPosition(@Nullable Context context, @NonNull SuntimesCalculator.SunPosition position)
    {
        if (context == null) {
            return "";
        }
        if (position.elevation >= 0) {
            return context.getString(R.string.timeMode_day);
        }
        if (position.elevation >= -6) {
            return context.getString(R.string.timeMode_civil);
        }
        if (position.elevation >= -12) {
            return context.getString(R.string.timeMode_nautical);
        }
        if (position.elevation >= -18) {
            return context.getString(R.string.timeMode_astronomical);
        }
        return context.getString(R.string.timeMode_night);
    }

    private void highlightLightmapKey(double elevation)
    {
        if (elevation >= 0)
            field_day.highlight(true);

        else if (elevation >= -6)
            field_civil.highlight(true);

        else if (elevation >= -12)
            field_nautical.highlight(true);

        else if (elevation >= -18)
            field_astro.highlight(true);

        else field_night.highlight(true);
    }

    protected String getSelectedTZID(Context context) {
        return WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTMAP, WidgetTimezones.LocalMeanTime.TIMEZONEID);
    }
    protected TimeZone getSelectedTZ(Context context, @Nullable SuntimesRiseSetDataset data)
    {
        if (data == null) {
            Log.w("DEBUG", "getSelectedTZ: data is null!! ");
            return null;
        }
        String tzId = getSelectedTZID(context);
        return WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data_timezone
                : WidgetTimezones.getTimeZone(tzId, data.location().getLongitudeAsDouble(), data.calculator());
    }

    protected static boolean useDST(TimeZone timezone) {
        return (Build.VERSION.SDK_INT < 24 ? timezone.useDaylightTime() : timezone.observesDaylightTime());
    }
    public static boolean inDST(TimeZone timezone, Calendar calendar) {
        return useDST(timezone) && timezone.inDaylightTime(calendar.getTime());
    }

    protected void updateTimeText(@NonNull SuntimesRiseSetDataset data)
    {
        Context context = getContext();
        if (context == null) {
            return;
        }

        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        long mapTimeMillis = nowMillis;

        if (lightmap.isAnimated() || lightmap.getOffsetMinutes() != 0) {
            mapTimeMillis = getMapTime(now.getTimeInMillis());
        }

        String suffix = "";
        boolean nowIsAfter = false;

        TimeZone tz = getSelectedTZ(context, data);
        Calendar mapTime = Calendar.getInstance(tz);

        mapTime.setTimeInMillis(mapTimeMillis);
        nowIsAfter = now.after(mapTime);

        boolean isOffset = Math.abs(nowMillis - mapTimeMillis) > 60 * 1000;
        if (isOffset) {
            suffix = ((nowIsAfter) ? context.getString(R.string.past_today) : context.getString(R.string.future_today));
        }

        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, mapTime);
        if (sunTime != null)
        {
            TimeZone timezone = mapTime.getTimeZone();
            CharSequence tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);

            CharSequence dstIcon = null;
            if (inDST(timezone, mapTime))
            {
                int iconSize = (int) getResources().getDimension(R.dimen.statusIcon_size);
                SuntimesUtils.ImageSpanTag[] spanTags = {
                        new SuntimesUtils.ImageSpanTag(SuntimesUtils.SPANTAG_DST, SuntimesUtils.createDstSpan(context, iconSize))
                };
                String spanString = " " + SuntimesUtils.SPANTAG_DST;
                dstIcon = SuntimesUtils.createSpan(context, spanString, spanTags);
            }

            CharSequence timeDisplay;
            if (suffix.isEmpty())
                timeDisplay = getString(R.string.datetime_format_verylong, timeText.toString(), tzDisplay);
            else timeDisplay = SuntimesUtils.createBoldColorSpan(null, getString(R.string.datetime_format_verylong1, timeText.toString(), tzDisplay, suffix), suffix, color_warning);

            if (dstIcon != null) {
                timeDisplay = TextUtils.concat(timeDisplay, dstIcon);
            }

            sunTime.setText(timeDisplay);
        }

        if (offsetTime != null)
        {
            if (isOffset) {
                SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(nowMillis, mapTimeMillis, false, true, false);
                offsetText.setSuffix("");
                String displayString = getContext().getString((nowIsAfter ? R.string.ago : R.string.hence), offsetText.toString() + "\n");
                offsetTime.setText(displayString);
            } else {
                offsetTime.setText(" \n ");
            }
        }
    }

    private long getMapTime(long now)
    {
        long offsetMillis = lightmap.getOffsetMinutes() * 60 * 1000;
        return ((lightmap.getNow() == -1) ? now : lightmap.getNow() + offsetMillis);
    }

    protected void updateSunPositionViews(@NonNull SuntimesRiseSetDataset data)
    {
        SuntimesCalculator calculator = data.calculator();
        if (sunLayout != null)
        {
            Calendar now = data.nowThen(data.calendar());
            if (lightmap.isAnimated() || lightmap.getOffsetMinutes() != 0) {
                now.setTimeInMillis(getMapTime(now.getTimeInMillis()));
            }

            SuntimesRiseSetData noonData = data.dataNoon;
            Calendar noonTime = (noonData != null ? noonData.sunriseCalendarToday() : null);
            SuntimesCalculator.SunPosition noonPosition = (noonTime != null && calculator != null ? calculator.getSunPosition(noonTime) : null);
            SuntimesCalculator.SunPosition currentPosition = (calculator != null ? calculator.getSunPosition(now) : null);

            if (currentPosition != null)
            {
                styleAzimuthText(sunAzimuth, currentPosition.azimuth, null, 2);
                int textColor = getColorForPosition(currentPosition, noonPosition);

                //sunElevation.setText(styleElevationText(currentPosition.elevation, null,2));
                //sunElevation.setShadowLayer(8f, 0, 0, textColor);

                //sunElevation.setText(styleElevationText(currentPosition.elevation, null,2));
                //sunElevation.setBackgroundColor(textColor);

                //sunElevation.setText(styleElevationText(currentPosition.elevation, textColor,2));
                //sunElevation.setShadowLayer(32f, 0, 0, SuntimesUtils.getContrastGlow(textColor));

                sunElevation.setText(styleElevationText(currentPosition.elevation, null, 2));
                SuntimesUtils.colorizeImageView(sunElevationHighlight, textColor);
                sunElevationHighlight.setVisibility(View.VISIBLE);
                sunElevationHighlight.setContentDescription(getTextForPosition(getActivity(), currentPosition));

                highlightLightmapKey(currentPosition.elevation);

            } else {
                sunAzimuth.setText("");
                sunAzimuth.setContentDescription("");
                sunElevation.setText("");
                sunElevationHighlight.setVisibility(View.GONE);
            }

            SuntimesRiseSetData riseSetData = data.dataActual;
            Calendar riseTime = (riseSetData != null ? riseSetData.sunriseCalendarToday() : null);
            SuntimesCalculator.SunPosition positionRising = (riseTime != null && calculator != null ? calculator.getSunPosition(riseTime) : null);
            if (positionRising != null) {
                styleAzimuthText(sunAzimuthRising, positionRising.azimuth, colorRisingLabel, decimalPlaces);

            } else {
                sunAzimuthRising.setText("");
                sunAzimuthRising.setContentDescription("");
            }

            Calendar setTime = (riseSetData != null ? riseSetData.sunsetCalendarToday() : null);
            SuntimesCalculator.SunPosition positionSetting = (setTime != null && calculator != null ? calculator.getSunPosition(setTime) : null);
            if (positionSetting != null) {
                styleAzimuthText(sunAzimuthSetting, positionSetting.azimuth, colorSettingLabel, decimalPlaces);

            } else {
                sunAzimuthSetting.setText("");
                sunAzimuthSetting.setContentDescription("");
            }

            if (noonPosition != null)
            {
                sunElevationAtNoon.setText(styleElevationText(noonPosition.elevation, colorSettingLabel, decimalPlaces));
                styleAzimuthText(sunAzimuthAtNoon, noonPosition.azimuth, null, decimalPlaces);

            } else {
                sunElevationAtNoon.setText("");
                sunAzimuthAtNoon.setText("");
                sunAzimuthAtNoon.setContentDescription("");
            }
            
            Context context = getContext();
            if (context != null && calculator != null)
            {
                double objectHeight = WidgetSettings.loadObserverHeightPref(context, 0);
                if (objectHeight > 0)
                {
                    WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

                    if (sunShadowObj != null) {
                        sunShadowObj.setText(styleLengthText(context, objectHeight, units));
                    }
                    if (sunShadowLength != null) {
                        double shadowLength = calculator.getShadowLength(objectHeight, now);
                        sunShadowLength.setText((shadowLength >= 0) ? styleLengthText(context, shadowLength, units) : "");
                    }
                    if (sunShadowLengthAtNoon != null && noonTime != null) {
                        double shadowLengthAtNoon = calculator.getShadowLength(objectHeight, noonTime );
                        sunShadowLengthAtNoon.setText((shadowLengthAtNoon >= 0) ? styleLengthText(context, shadowLengthAtNoon, units) : "");
                    }
                }
            }

            showSunPosition(currentPosition != null);
        }
    }

    private void showSunPosition(boolean show)
    {
        if (sunLayout != null)
        {
            int updatedVisibility = (show ? View.VISIBLE : View.GONE);
            if (sunLayout.getVisibility() != updatedVisibility)
            {
                sunLayout.setVisibility(updatedVisibility);
                if (dialogContent != null) {
                    dialogContent.requestLayout();
                }
            }
        }
    }

    protected void openCalendar(Context context, long itemMillis)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/time/" + itemMillis));
        context.startActivity(intent);
    }

    @SuppressLint("ResourceType")
    protected void showHelp(Context context)
    {
        int iconSize = (int) getResources().getDimension(R.dimen.helpIcon_size);
        int[] iconAttrs = { R.attr.icActionShadow, R.attr.tagColor_dst, R.attr.icActionDst };
        TypedArray typedArray = context.obtainStyledAttributes(iconAttrs);
        ImageSpan shadowIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(0, R.drawable.ic_action_shadow), iconSize, iconSize, 0);
        int dstColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.dstTag_dark));
        ImageSpan dstIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(2, R.drawable.ic_weather_sunny), iconSize, iconSize, dstColor);
        typedArray.recycle();

        SuntimesUtils.ImageSpanTag[] helpTags = {
                new SuntimesUtils.ImageSpanTag("[Icon Shadow]", shadowIcon),
                new SuntimesUtils.ImageSpanTag("[Icon DST]", dstIcon),
        };

        final WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
        double observerHeight = WidgetSettings.loadObserverHeightPref(context, 0);
        String observerHeightDisplay = SuntimesUtils.formatAsHeight(context, observerHeight, units, true, 2);
        String shadowSummary = getString(R.string.configLabel_general_observerheight_summary, observerHeightDisplay);
        String shadowHelp = getString(R.string.help_shadowlength, shadowSummary);
        SpannableStringBuilder shadowHelpSpan = SuntimesUtils.createSpan(context, shadowHelp, helpTags);

        CharSequence dstHelp = "\n" + SuntimesUtils.fromHtml(getString(R.string.help_general_dst));
        SpannableStringBuilder dstHelpSpan = SuntimesUtils.createSpan(context, dstHelp, helpTags);

        CharSequence twilightHelp = SuntimesUtils.fromHtml(getString(R.string.help_general_twilight));
        CharSequence nightHelp = SuntimesUtils.fromHtml(getString(R.string.help_general_twilight_more));
        CharSequence helpSpan = TextUtils.concat(twilightHelp, nightHelp, dstHelpSpan, shadowHelpSpan);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpSpan);
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * LightMapKey
     */
    private class LightMapKey
    {
        protected ImageView icon;
        protected TextView label;
        protected TextView text;

        protected CharSequence defaultLabel = "";
        protected Drawable defaultIcon = null;

        public LightMapKey(ImageView icon, TextView label, TextView duration)
        {
            this.icon = icon;
            this.label = label;
            this.text = duration;
            this.defaultLabel = label.getText();
            this.defaultIcon = icon.getBackground();
        }

        public LightMapKey(@NonNull View parent, int iconRes, int labelRes, int durationRes)
        {
            icon = (ImageView)parent.findViewById(iconRes);
            label = (TextView)parent.findViewById(labelRes);
            text = (TextView)parent.findViewById(durationRes);
            if (label != null) {
                defaultLabel = label.getText();
            }
            if (icon != null) {
                defaultIcon = icon.getBackground();
            }
        }

        public void themeViews(SuntimesTheme theme)
        {
            if (theme != null)
            {
                label.setTextColor(theme.getTextColor());
                label.setTextSize(theme.getTextSizeSp());
                text.setTextColor(theme.getTimeColor());
                text.setTextSize(theme.getTimeSizeSp());
                text.setTypeface(text.getTypeface(), (theme.getTimeBold() ? Typeface.BOLD : Typeface.NORMAL));
            }
        }

        public void setVisible(boolean visible)
        {
            int visibility = (visible ? View.VISIBLE : View.GONE);
            if (label != null) {
                label.setVisibility(visibility);
            }
            if (text != null) {
                text.setVisibility(visibility);
            }
            if (icon != null) {
                icon.setVisibility(visibility);
            }
        }

        public void setIconDrawable(@Nullable Drawable d)
        {
            if (icon != null && (d != null || defaultIcon != null)) {
                icon.setBackgroundDrawable(d != null ? d : defaultIcon);
            }
        }
        public Drawable getDefaultIconDrawable() {
            return defaultIcon;
        }

        public void setLabelText(@Nullable CharSequence text)
        {
            if (label != null) {
                label.setText(text != null ? text : defaultLabel);
            }
        }
        public void setLabelVisible(boolean visible)
        {
            int visibility = (visible ? View.VISIBLE : View.INVISIBLE);
            if (label != null) {
                label.setVisibility(visibility);
            }
        }

        public void highlight(boolean highlight)
        {
            if (label != null)
            {
                label.setTypeface(null, (highlight ? Typeface.BOLD : Typeface.NORMAL));
                if (highlight)
                    label.setPaintFlags(label.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                else label.setPaintFlags(label.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            }

            //if (text != null)
                //text.setTypeface(null, (highlight ? Typeface.BOLD : Typeface.NORMAL));
        }

        public void updateInfo(Context context, LightMapKeyInfo[] info) {
            updateInfo(context, info, "");
        }
        public void updateInfo(Context context, LightMapKeyInfo[] info, CharSequence noneText)
        {
            if (text == null || info == null || context == null)
                return;

            if (info.length == 1)
            {
                String duration = info[0].durationString(showSeconds);
                if (info[0].delta > 0) {
                    String s = context.getString(R.string.length_twilight1e_pos, duration, info[0].deltaString(showSeconds));
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));

                } else if (info[0].delta < 0) {
                    String s = context.getString(R.string.length_twilight1e_neg, duration, info[0].deltaString(showSeconds));
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));

                } else {
                    String s = context.getString(R.string.length_twilight1, duration);
                    if (info[0].durationColor != null)
                        text.setText(SuntimesUtils.createColorSpan(null, s, duration, info[0].durationColor));
                    else text.setText(new SpannableString(s));
                }
                setVisible(true);

            } else if (info.length >= 2) {
                String s = context.getString(R.string.length_twilight2, info[0].durationString(showSeconds), info[1].durationString(showSeconds));
                String delimiter = context.getString(R.string.length_delimiter);
                text.setText(SuntimesUtils.createBoldColorSpan(null, s, delimiter, colorDay));
                setVisible(true);

            } else {
                text.setText(new SpannableString(noneText != null ? noneText : ""));
                setVisible(noneText != null && !noneText.toString().isEmpty());
            }
        }
    }

    /**
     * LightMapKeyInfo
     */
    public static class LightMapKeyInfo
    {
        public LightMapKeyInfo(long duration, long delta)
        {
            this.duration = duration;
            this.delta = delta;
        }

        public long duration = 0;
        public Integer durationColor = null;
        public String durationString(boolean showSeconds)
        {
            return utils.timeDeltaLongDisplayString(duration, showSeconds).toString();
        }

        public long delta = 0;
        public Integer deltaColor = null;
        public String deltaString(boolean showSeconds)
        {
            return utils.timeDeltaLongDisplayString(delta, showSeconds).toString();
        }
    }

    public static LightMapKeyInfo[] createInfoArray(long durations, long delta, int color)
    {
        if (durations != 0)
        {
            LightMapKeyInfo[] info = new LightMapKeyInfo[1];
            info[0] = new LightMapKeyInfo(durations, delta);
            info[0].durationColor = color;
            return info;

        } else {
            return new LightMapKeyInfo[0];
        }
    }

    public static LightMapKeyInfo[] createInfoArray(long[] durations)
    {
        ArrayList<LightMapKeyInfo> info = new ArrayList<>();
        for (int i=0; i<durations.length; i++)
        {
            if (durations[i] != 0) {
                info.add(new LightMapKeyInfo(durations[i], 0));
            }
        }
        return info.toArray(new LightMapKeyInfo[0]);
    }

    private LightMapDialogListener dialogListener = null;
    public void setDialogListener(LightMapDialogListener listener) {
        dialogListener = listener;
    }

    /**
     * LightMapDialogListener
     */
    public static class LightMapDialogListener
    {
        public void onShowDate( long suggestDate ) {}
        public void onShowMap( long suggestDate ) {}
        public void onShowChart( long suggestDate ) {}
        public void onShowMoonInfo( long suggestDate ) {}
        public void onColorsModified(ColorValues values) {}
    }

    /**
     * ColorCollection
     */
    private ColorValuesCollection<ColorValues> colors;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colors = collection;
    }
    public ColorValuesCollection<ColorValues> getColorCollection() {
        return colors;
    }
    protected void initColors(Context context) {
        colors = new AppColorValuesCollection<>(context);
    }

    protected void showColorDialog(Context context)
    {
        boolean showGraph = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_LIGHTMAP_SHOWGRAPH, MAPTAG_LIGHTMAP, DEF_KEY_LIGHTMAP_SHOWGRAPH);
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        ColorValuesSheetDialog dialog = new ColorValuesSheetDialog();
        dialog.setAppWidgetID((isNightMode ? 1 : 0));
        dialog.setColorTag(AppColorValues.TAG_APPCOLORS);
        dialog.setFilter(new CardColorValues().getColorKeys(),
                        (showGraph ? new LineGraphColorValues().getColorKeys() : null),
                         new LightMapColorValues().getColorKeys());
        dialog.setColorCollection(colors);
        dialog.setDialogListener(colorDialogListener);
        dialog.show(getChildFragmentManager(), DIALOGTAG_COLORS);
    }
    private final ColorValuesSheetDialog.DialogListener colorDialogListener = new ColorValuesSheetDialog.DialogListener()
    {
        @Override
        public void onColorValuesSelected(ColorValues values)
        {
            if (lightmap != null) {
                if (values != null) {
                    lightmap.getColors().values = new LightMapColorValues(values);
                } else {
                    lightmap.getColors().init(getActivity());
                }
            }
            if (graphView != null) {
                if (values != null) {
                    graphView.getOptions().colors = new LineGraphColorValues(values);
                } else {
                    graphView.getOptions().init(getActivity());
                }
            }
            themeViews(getActivity());
            updateViews();

            if (dialogListener != null) {
                dialogListener.onColorsModified(values);
            }
        }

        public void requestPeekHeight(int height) {}
        public void requestHideSheet() {}
        public void requestExpandSheet() {}
        public void onModeChanged(int mode) {}

        @Nullable
        @Override
        public ColorValues getDefaultValues() {
            return new AppColorValues(getActivity(), true);
        }
    };

}
