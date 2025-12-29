/**
    Copyright (C) 2024 Forrest Guice
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

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.MenuAddon;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetDialog;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.dialog.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.ShareUtils;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWCROSSHAIR;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWCROSSHAIR;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_WORLDMAP_MINORGRID;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWASTRO;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWCIVIL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWNAUTICAL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWPOINTS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWSEASONS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.DEF_KEY_GRAPH_SHOWTWILIGHT;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWASTRO;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWCIVIL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWNAUTICAL;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWPOINTS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWSEASONS;
import static com.forrestguice.suntimeswidget.graph.LightGraphView.PREF_KEY_GRAPH_SHOWTWILIGHT;
import static com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID;

public class LightGraphDialog extends BottomSheetDialogBase
{
    public static final String MAPTAG_LIGHTGRAPH = "_lightgraph";

    public static final String DIALOGTAG_COLORS = "lightgraph_colors";
    public static final String DIALOGTAG_HELP = "lightgraph_help";
    protected static SuntimesUtils utils = new SuntimesUtils();

    protected TextView text_title, text_time;
    protected LightGraphView graph;
    protected LightMapView lightmap;
    protected ProgressBar progress;
    protected ImageButton btn_menu;

    protected TextView text_sunrise_early, text_sunrise_late;
    protected TextView text_sunset_early, text_sunset_late;
    protected View layout_sunrise_early, layout_sunrise_late;
    protected View layout_sunset_early, layout_sunset_late;

    protected LightGraphView.LightGraphOptions options; // = new LightGraphView.LightGraphOptions();

    public LightGraphDialog() {
        setArguments(new Bundle());
    }

    protected SuntimesRiseSetDataset data = null;
    public SuntimesRiseSetDataset getData() {
        return data;
    }
    public void setData(Context context, SuntimesRiseSetDataset data) {
        setData(context, data, true);
    }
    public void setData(Context context, SuntimesRiseSetDataset data, boolean updateNow)
    {
        this.data = data;
        if (isAdded())
        {
            graph.setData(data);
            lightmap.setData(data);

            if (updateNow) {
                updateViews(getActivity());
            }
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
            }
        };
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        //options.isRtl = AppSettings.isLocaleRtl(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View v = inflater.cloneInContext(context).inflate(R.layout.layout_dialog_lightgraph, parent, false);
        initColors(context);

        progress = (ProgressBar) v.findViewById(R.id.progress);

        lightmap = (LightMapView) v.findViewById(R.id.info_time_lightmap);
        if (lightmap != null)
        {
            LightMapView.LightMapColors options = lightmap.getColors();
            options.option_lmt = true;
            lightmap.setData(data);
        }

        graph = (LightGraphView) v.findViewById(R.id.info_time_lightgraph);
        if (graph != null)
        {
            options = graph.getOptions();
            options.init(context);

            graph.setTaskListener(new LightGraphView.LightGraphTaskListener()
            {
                @Override
                public void onFinished(Bitmap result) {
                    updateEarliestLatestText(getActivity());
                }

                @Override
                public void onProgress(boolean value) {
                    showProgress(value);
                }
            });
            graph.setData(data);    // returns immediately; makes async call to updateViews when data is actually set
        }
        initLocale(context);

        text_title = (TextView) v.findViewById(R.id.dialog_title);
        if (text_title != null) {
            //text_title.setOnClickListener(onTitleClicked);
        }

        text_time = (TextView) v.findViewById(R.id.info_time_graph);
        if (text_time != null) {
            text_time.setOnClickListener(onTimeClicked);
        }

        btn_menu = (ImageButton) v.findViewById(R.id.menu_button);
        if (btn_menu != null)
        {
            TooltipCompat.setTooltipText(btn_menu, btn_menu.getContentDescription());
            btn_menu.setOnClickListener(onMenuClicked);
            if (AppSettings.isTelevision(getActivity())) {
                btn_menu.setFocusableInTouchMode(true);
            }
        }

        ImageButton btn_position = (ImageButton) v.findViewById(R.id.sunposition_button);
        if (btn_position != null)
        {
            TooltipCompat.setTooltipText(btn_position, btn_position.getContentDescription());
            btn_position.setOnClickListener(onPositionButtonClicked);
        }

        text_sunrise_early = (TextView) v.findViewById(R.id.text_time_sunrise_early);
        text_sunrise_late = (TextView) v.findViewById(R.id.text_time_sunrise_late);
        text_sunset_early = (TextView) v.findViewById(R.id.text_time_sunset_early);
        text_sunset_late = (TextView) v.findViewById(R.id.text_time_sunset_late);

        layout_sunrise_early = v.findViewById(R.id.layout_time_sunrise_early_layout);
        layout_sunrise_late = v.findViewById(R.id.layout_time_sunrise_late_layout);
        layout_sunset_early = v.findViewById(R.id.layout_time_sunset_early_layout);
        layout_sunset_late = v.findViewById(R.id.layout_time_sunset_late_layout);

        if (savedState != null) {
            loadState(savedState);
        }

        themeViews(context);
        updateViews(getContext());
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (graph != null) {
            graph.onResume();
        }

        Context context = getActivity();
        FragmentManager fragments = getChildFragmentManager();
        ColorValuesSheetDialog colorDialog = (ColorValuesSheetDialog) fragments.findFragmentByTag(DIALOGTAG_COLORS);
        if (colorDialog != null)
        {
            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            colorDialog.setAppWidgetID((isNightMode ? 1 : 0));
            colorDialog.setColorTag(AppColorValues.TAG_APPCOLORS);
            colorDialog.setColorCollection(colors);
            colorDialog.setDialogListener(colorDialogListener);
        }

        //HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        //if (helpDialog != null) {
        //    helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        //}

        expandSheet(getDialog());
    }

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior<?> bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
    private void collapseSheet(Dialog dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior<?> bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }
    @Nullable
    private BottomSheetBehavior<?> initSheet(DialogInterface dialog)
    {
        if (dialog != null)
        {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(sheetFrameID);
            if (layout != null)
            {
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(true);
                ViewUtils.initPeekHeight(getDialog(), peekViewID);
                return behavior;
            }
        }
        return null;
    }
    private final int peekViewID =  R.id.layout_graph;
    private final int sheetFrameID = ViewUtils.getBottomSheetResourceID();

    private final DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface)
        {
            Context context = getContext();
            if (context != null)
            {
                updateViews(getContext());
                if (text_title != null) {
                    text_title.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewUtils.initPeekHeight(getDialog(), peekViewID);
                        }
                    });
                }

                if (AppSettings.isTelevision(getActivity())) {
                    btn_menu.requestFocus();
                }

            } else Log.w("LightGraphDialog.onShow", "null context! skipping update");
        }
    };

    private final View.OnClickListener onMenuClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getContext(), v);
        }
    });

    private void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            /*
            options.init(themeOverride);
            card_adapter.setThemeOverride(themeOverride);

            text_title.setTextColor(options.titleColor);
            if (options.titleSizeSp != null)
            {
                text_title.setTextSize(options.titleSizeSp);
                text_title.setTypeface(text_title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            if (options.textColor != null) {
                //text_year_length.setTextColor(options.textColor);
            }
            if (options.timeSizeSp != null) {
                //text_year_length.setTextSize(options.timeSizeSp);
            }
            */  // TODO
        }
        updateGraphColors(context);
    }

    private SuntimesTheme themeOverride = null;
    @Deprecated
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            themeViews(context);
        }
    }


    protected void showProgress(boolean value)
    {
        if (progress != null) {
            progress.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }

    public void updateViews(Context context)
    {
        stopUpdateTask();
        updateTimeViews(context);
        updateGraphViews(context);
        startUpdateTask();
        //Log.d("DEBUG", "LightGraphDialog updated");
    }

    public void updateTimeViews(Context context)
    {
        //WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, WidgetTimezones.LocalMeanTime.TIMEZONEID)

        if (context == null || data == null) {
            return;
        }

        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        long mapTimeMillis = nowMillis;

        if (graph.isAnimated() || graph.getOffsetDays() != 0) {
            mapTimeMillis = getMapTime(now.getTimeInMillis());
        }

        String suffix = "";
        boolean nowIsAfter = false;

        String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID);
        TimeZone tz = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data.timezone()
                : WidgetTimezones.getTimeZone(tzId, data.location().getLongitudeAsDouble(), data.calculator());
        Calendar mapTime = Calendar.getInstance(tz);

        mapTime.setTimeInMillis(mapTimeMillis);
        nowIsAfter = now.after(mapTime);

        boolean isOffset = Math.abs(nowMillis - mapTimeMillis) > 60 * 1000;
        if (isOffset) {
            suffix = ((nowIsAfter) ? context.getString(R.string.past_today) : context.getString(R.string.future_today));
        }

        TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, mapTime);
        if (text_time != null)
        {
            String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, mapTime.getTimeZone());
            if (suffix.isEmpty())
                text_time.setText(getString(R.string.datetime_format_verylong, timeText.toString(), tzDisplay));
            else text_time.setText(SuntimesUtils.createBoldColorSpan(null, getString(R.string.datetime_format_verylong1, timeText.toString(), tzDisplay, suffix), suffix, Color.RED));    // TODO: warning color
        }

        /*if (offsetTime != null)
        {
            if (isOffset) {
                SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(nowMillis, mapTimeMillis, false, true, false);
                offsetText.setSuffix("");
                String displayString = getContext().getString((nowIsAfter ? R.string.ago : R.string.hence), offsetText.toString() + "\n");
                offsetTime.setText(displayString);
            } else {
                offsetTime.setText(" \n ");
            }
        }*/
    }

    private long getMapTime(long now)
    {
        long offsetMillis = graph.getOffsetDays() * 24 * 60 * 60 * 1000;
        return ((graph.getNow() == -1) ? now : graph.getNow() + offsetMillis);
    }

    protected Calendar getCalendar(Context context, int day, double hour)
    {
        SuntimesRiseSetDataset data0 = (graph != null ? graph.getData0() : null);
        SuntimesRiseSetDataset[] data = (graph != null ? graph.getData() : null);
        if (context != null && data != null && data.length > 0 && data[0] != null && data0 != null)
        {
            double minute = (int)((hour - (int) hour) * 60d);
            double second = (int)((minute - (int) minute) * 60d);
            double millisecond = (int)((second - (int) second) * 1000d);

            Calendar calendar = Calendar.getInstance(WidgetTimezones.getTimeZone(TimeZones.LocalMeanTime.TIMEZONEID, data0.location().getLongitudeAsDouble(), data0.calculator()));
            calendar.set(Calendar.YEAR, data[0].calendar().get(Calendar.YEAR));
            calendar.set(Calendar.DAY_OF_YEAR, day);
            calendar.set(Calendar.HOUR_OF_DAY, (int) hour);
            calendar.set(Calendar.MINUTE, (int) minute);
            calendar.set(Calendar.SECOND, (int) second);
            calendar.set(Calendar.MILLISECOND, (int) millisecond);

            String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID);
            TimeZone timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data0.timezone() :
                    WidgetTimezones.getTimeZone(tzId, data0.location().getLongitudeAsDouble(), data0.calculator());
            Calendar c = Calendar.getInstance(timezone);
            c.setTimeInMillis(calendar.getTimeInMillis());
            return c;
        } else return null;
    }

    public void updateGraphColors(Context context)
    {
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        AppColorValues values = (AppColorValues) colors.getSelectedColors(context, (isNightMode ? 1 : 0), AppColorValues.TAG_APPCOLORS);

        if (values != null)
        {
            options.colors = new LightGraphColorValues(values);
            if (lightmap != null) {
                lightmap.getColors().values = new LightMapColorValues(values);
            }

        } else if (options.colors == null) {
            options.init(context);
            if (lightmap != null) {
                lightmap.getColors().init(context);
            }
        }
    }

    public void updateGraphViews(Context context)
    {
        if (context != null)
        {
            options.showSeasons = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWSEASONS);
            options.axisX_labels_show = options.axisY_labels_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWLABELS);
            options.axisX_show = options.axisY_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWAXIS);
            options.gridX_minor_show = options.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, DEF_KEY_WORLDMAP_MINORGRID);
            options.gridX_major_show = options.gridY_major_show = false;
            options.sunPath_show_points = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWPOINTS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWPOINTS);
            options.showCivil = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCIVIL);
            options.showNautical = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWNAUTICAL);
            options.showAstro = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWASTRO);
            options.option_drawNow_crosshair = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCROSSHAIR, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCROSSHAIR);

            if (data != null)
            {
                String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID);
                options.timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data.timezone()
                        : WidgetTimezones.getTimeZone(tzId, data.location().getLongitudeAsDouble(), data.calculator());
            }
            options.is24 = SuntimesUtils.is24();
        }

        long nowMillis = Calendar.getInstance().getTimeInMillis();
        if (graph != null) {
            graph.getOptions().now = nowMillis;
            graph.updateViews(true);
        }
        if (lightmap != null) {
            lightmap.getColors().now = nowMillis;
            lightmap.updateViews(true);
        }

        updateEarliestLatestText(context);
    }

    protected void updateEarliestLatestText(Context context)
    {
        if (options.earliestLatestData != null)
        {
            if (text_sunrise_early != null) {
                updateEarliestLatestText(context, text_sunrise_early, layout_sunrise_early, options.earliestLatestData.early_sunrise_day, options.earliestLatestData.early_sunrise_hour, R.string.configLabel_earliest_sunrise);
            }
            if (text_sunrise_late != null) {
                updateEarliestLatestText(context, text_sunrise_late, layout_sunrise_late, options.earliestLatestData.late_sunrise_day, options.earliestLatestData.late_sunrise_hour, R.string.configLabel_latest_sunrise);
            }
            if (text_sunset_early != null) {
                updateEarliestLatestText(context, text_sunset_early, layout_sunset_early, options.earliestLatestData.early_sunset_day, options.earliestLatestData.early_sunset_hour, R.string.configLabel_earliest_sunset);
            }
            if (text_sunset_late != null) {
                updateEarliestLatestText(context, text_sunset_late, layout_sunset_late, options.earliestLatestData.late_sunset_day, options.earliestLatestData.late_sunset_hour, R.string.configLabel_latest_sunset);
            }
        }
    }

    protected void updateEarliestLatestText(Context context, TextView textView, View layout, int day, double hour, int labelResID)
    {
        Calendar calendar = getCalendar(context, day, hour);
        if (calendar != null) {
            textView.setText(utils.calendarDateTimeDisplayString(context, calendar).toString());
        } else textView.setText("");
        if (calendar != null && context != null) {
            layout.setOnClickListener(onMoreInfoClicked(context.getString(labelResID), calendar.getTimeInMillis()));
        } else layout.setOnClickListener(null);
    }

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (graph != null) {
            updateTask_isRunning = true;
            graph.post(updateTask);
        }
    }
    private void stopUpdateTask()
    {
        if (graph != null) {
            updateTask_isRunning = false;
            graph.removeCallbacks(updateTask);
        }
    }

    public static final int UPDATE_RATE = 3000;
    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && !graph.isAnimated()) {
                updateTimeViews(getActivity());
                updateGraphViews(getActivity());
            }
            if (graph != null && updateTask_isRunning) {
                graph.postDelayed(this, UPDATE_RATE);
            }
        }
    };
    private boolean updateTask_isRunning = false;

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        //outState.putInt("currentCardPosition", currentCardPosition());
        super.onSaveInstanceState(outState);
    }

    public void loadState(Bundle bundle)
    {
        /*int cardPosition = bundle.getInt("currentCardPosition", EquinoxDatasetAdapter.CENTER_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = EquinoxDatasetAdapter.CENTER_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap*/
    }

    protected void showHelp(Context context)
    {
        String topic1 = context.getString(R.string.help_general_timeMode2);      // TODO: help
        String topic2 = context.getString(R.string.help_general_timeMode2_1);
        String topic3 = context.getString(R.string.help_general_tropicalyear);
        String helpContent = context.getString(R.string.help_general3, topic1, topic2, topic3);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpContent);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showOverflowMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.lightgraphmenu, menu.getMenu());
        menu.setOnMenuItemClickListener(onOverflowMenuClick);
        updateOverflowMenu(context, menu);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

    private void updateOverflowMenu(Context context, PopupMenu popup)
    {
        Menu menu = popup.getMenu();

        MenuItem graphOption_showCrosshair = menu.findItem(R.id.graphOption_showCrosshair);
        if (graphOption_showCrosshair != null) {
            graphOption_showCrosshair.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCROSSHAIR, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCROSSHAIR));
        }
        MenuItem graphOption_showGrid = menu.findItem(R.id.graphOption_showGrid);
        if (graphOption_showGrid != null) {
            graphOption_showGrid.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, DEF_KEY_WORLDMAP_MINORGRID));
        }
        MenuItem graphOption_showSeasons = menu.findItem(R.id.graphOption_showSeasons);
        if (graphOption_showSeasons != null) {
            graphOption_showSeasons.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWSEASONS));
        }
        MenuItem graphOption_showLabels = menu.findItem(R.id.graphOption_showLabels);
        if (graphOption_showLabels != null) {
            graphOption_showLabels.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWLABELS));
        }
        MenuItem graphOption_showAxis = menu.findItem(R.id.graphOption_showAxis);
        if (graphOption_showAxis != null) {
            graphOption_showAxis.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWAXIS));
        }
        MenuItem graphOption_showPoints = menu.findItem(R.id.graphOption_showPoints);
        if (graphOption_showPoints != null) {
            graphOption_showPoints.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWPOINTS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWPOINTS));
        }

        MenuItem graphOption_showTwilights = menu.findItem(R.id.graphOption_showTwilights);
        if (graphOption_showTwilights != null) {
            graphOption_showTwilights.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWTWILIGHT));
        }
        MenuItem graphOption_showCivil = menu.findItem(R.id.graphOption_showCivil);
        if (graphOption_showCivil != null) {
            graphOption_showCivil.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCIVIL));
        }
        MenuItem graphOption_showNautical = menu.findItem(R.id.graphOption_showNautical);
        if (graphOption_showNautical != null) {
            graphOption_showNautical.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWNAUTICAL));
        }
        MenuItem graphOption_showAstro = menu.findItem(R.id.graphOption_showAstro);
        if (graphOption_showAstro != null) {
            graphOption_showAstro.setChecked(WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWASTRO));
        }

    }

    private final PopupMenu.OnMenuItemClickListener onOverflowMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getActivity();
            if (context == null) {
                return false;
            }

            boolean toggledValue;
            switch (item.getItemId())
            {
                case R.id.graphOption_colors:
                    showColorDialog(getActivity());
                    return true;

                case R.id.graphOption_showPoints:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWPOINTS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWPOINTS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWPOINTS, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showAxis:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWAXIS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showCrosshair:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCROSSHAIR, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCROSSHAIR);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCROSSHAIR, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showGrid:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, DEF_KEY_WORLDMAP_MINORGRID);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showLabels:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWLABELS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showTwilights:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWTWILIGHT, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWLABELS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWTWILIGHT, MAPTAG_LIGHTGRAPH, toggledValue);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, toggledValue);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, toggledValue);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showCivil:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWCIVIL);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWCIVIL, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showNautical:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWNAUTICAL);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWNAUTICAL, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showAstro:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWASTRO);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWASTRO, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.graphOption_showSeasons:
                    toggledValue = !WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWSEASONS);
                    WorldMapWidgetSettings.saveWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWSEASONS, MAPTAG_LIGHTGRAPH, toggledValue);
                    item.setChecked(toggledValue);
                    updateViews(context);
                    return true;

                case R.id.action_timezone:
                    showTimeZoneMenu(context, text_time);
                    return true;

                case R.id.action_share:
                    shareItem(getContext(), null);
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    });

    private final View.OnClickListener onPositionButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (dialogListener != null) {
                dialogListener.onShowPosition(Calendar.getInstance().getTimeInMillis());
            }
        }
    };

    private final View.OnClickListener onTimeClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showTimeZoneMenu(getActivity(), text_time);
        }
    });
    protected boolean showTimeZoneMenu(Context context, View view)
    {
        PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.lightgraphmenu_tz, onTimeZoneMenuClick);
        WidgetTimezones.updateTimeZoneMenu(menu.getMenu(), WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, TimeZones.LocalMeanTime.TIMEZONEID));
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
                    WorldMapWidgetSettings.saveWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, tzID);
                    setData(context, data, false);    // reconstructs year data using given timezone
                    updateViews(getActivity());
                }
                return (tzID != null);
            } else return false;
        }
    });

    protected View.OnClickListener onMoreInfoClicked(final String label, final long datetime)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContextMenu(getActivity(), v, label, datetime);
            }
        };
    }

    protected void showContextMenu(Context context, View v, final String label, final long datetime)
    {
        PopupMenu menu = new PopupMenu(context, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.lightgraphmenu_context, menu.getMenu());
        menu.setOnMenuItemClickListener(onContextMenuClicked);
        updateContextMenu(context, menu, label, datetime);
        PopupMenuCompat.forceActionBarIcons(menu.getMenu());
        menu.show();
    }
    private void updateContextMenu(Context context, PopupMenu menu, final String label, final long datetime)
    {
        Intent data = new Intent();
        data.putExtra(MenuAddon.EXTRA_SHOW_DATE, datetime);
        data.putExtra("label", label);

        Menu m = menu.getMenu();
        setDataToMenu(m, data);

        MenuItem addonSubmenuItem = m.findItem(R.id.addonSubMenu);
        if (addonSubmenuItem != null) {
            List<MenuAddon.ActivityItemInfo> addonMenuItems = MenuAddon.queryAddonMenuItems(context);
            if (!addonMenuItems.isEmpty()) {
                PopupMenuCompat.forceActionBarIcons(addonSubmenuItem.getSubMenu());
                MenuAddon.populateSubMenu(addonSubmenuItem, addonMenuItems, datetime);
            } //else addonSubmenuItem.setVisible(false);
        }
    }
    private static void setDataToMenu(Menu m, Intent data)
    {
        if (m != null) {
            for (int i = 0; i < m.size(); i++) {
                m.getItem(i).setIntent(data);
                setDataToMenu(m.getItem(i).getSubMenu(), data);
            }
        }
    }

    protected PopupMenu.OnMenuItemClickListener onContextMenuClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            Intent itemData = item.getIntent();
            long itemTime = ((itemData != null) ? itemData.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, -1L) : -1L);

            switch (item.getItemId())
            {
                //case R.id.action_alarm:
                //    if (dialogListener != null) {
                //        dialogListener.onSetAlarm(itemMode);
                //    }
                //    return true;

                case R.id.action_sunposition:
                    if (dialogListener != null) {
                        dialogListener.onShowPosition(itemTime);
                    }
                    return true;

                case R.id.action_moon:
                    if (dialogListener != null) {
                        dialogListener.onShowMoonInfo(itemTime);
                    }
                    return true;

                case R.id.action_worldmap:
                    if (dialogListener != null) {
                        dialogListener.onShowMap(itemTime);
                    }
                    return true;

                case R.id.action_date:
                    if (dialogListener != null) {
                        dialogListener.onShowDate(itemTime);
                    }
                    collapseSheet(getDialog());
                    return true;

                case R.id.action_calendar:
                    openCalendar(context, itemTime);
                    return true;

                case R.id.action_share:
                    shareItem(getActivity(), itemData);
                    return true;

                default:
                    return false;
            }
        }
    };

    protected void openCalendar(Context context, long itemMillis)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/time/" + itemMillis));
        context.startActivity(intent);
    }

    protected void shareItem(Context context, @Nullable Intent itemData)
    {
        if (itemData != null)
        {
            // share itemData
            String label = itemData.getStringExtra("label");
            long itemMillis = itemData.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, -1L);
            if (itemMillis != -1L)
            {
                Calendar itemTime = Calendar.getInstance();
                itemTime.setTimeInMillis(itemMillis);
                boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
                boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

                SuntimesUtils utils = new SuntimesUtils();
                SuntimesUtils.initDisplayStrings(context);
                String itemDisplay = context.getString(R.string.share_format, label, utils.calendarDateTimeDisplayString(context, itemTime, showTime, showSeconds).toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText(label, itemDisplay));
                    }
                } else {
                    @SuppressWarnings("deprecation")
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setText(itemDisplay);
                    }
                }
                Toast.makeText(getContext(), itemDisplay, Toast.LENGTH_SHORT).show();
            }

        } else {
            // share the graph bitmap
            graph.shareBitmap(new ExportTask.TaskListener()
            {
                @Override
                public void onStarted() {
                    showProgress(true);
                }

                @Override
                public void onFinished(ExportTask.ExportResult result)
                {
                    showProgress(false);
                    Context context = getContext();
                    if (context != null)
                    {
                        if (result.getResult())
                        {
                            String successMessage = context.getString(R.string.msg_export_success, result.getExportFile().getAbsolutePath());
                            Toast.makeText(context.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();
                            ShareUtils.shareFile(context, ExportTask.FILE_PROVIDER_AUTHORITY(), result.getExportFile(), result.getMimeType());

                        } else {
                            File file = result.getExportFile();
                            String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.msg_export_failure, path), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
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
        dialog.setColorTag(AppColorValues.TAG_APPCOLORS);
        dialog.setColorCollection(colors);
        dialog.setDialogListener(colorDialogListener);
        dialog.setFilter(new LightMapColorValues().getColorKeys(),
                         new LightGraphColorValues().getColorKeys());
        dialog.show(getChildFragmentManager(), DIALOGTAG_COLORS);
    }
    private final ColorValuesSheetDialog.DialogListener colorDialogListener = new ColorValuesSheetDialog.DialogListener()
    {
        @Override
        public void onColorValuesSelected(ColorValues values)
        {
            options.colors = null;
            updateGraphColors(getActivity());
            updateGraphViews(getActivity());

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
            return new AppColorValues(AndroidResources.wrap(getActivity()), true);
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
        colors = new AppColorValuesCollection<>(context);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener ) {
        dialogListener = listener;
    }

    /**
     * DialogListener
     */
    public static class DialogListener
    {
        //public void onSetAlarm( WidgetSettings.SolsticeEquinoxMode suggestedEvent ) {}
        public void onShowMap( long suggestedDate ) {}
        public void onShowPosition( long suggestedDate ) {}
        public void onShowMoonInfo( long suggestedDate ) {}
        public void onShowDate( long suggestedDate ) {}
        public void onOptionsModified(boolean closeDialog) {}
        public void onColorsModified(ColorValues values) {}
    }
}
