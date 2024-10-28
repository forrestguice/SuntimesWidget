/**
    Copyright (C) 2021-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.graph.LightMapView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.graph.LightGraphView;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.graph.LineGraphView;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_6;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_7;
import com.forrestguice.suntimeswidget.widgets.layouts.MoonLayout_1x1_8;
import com.forrestguice.suntimeswidget.widgets.layouts.PositionLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SunPosLayout_3X2_0;
import com.forrestguice.suntimeswidget.widgets.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.map.WorldMapTask;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_FILLPATH;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_GRAPH_SHOWMOON;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.DEF_KEY_WORLDMAP_MINORGRID;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.MAPTAG_LIGHTMAP;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_FILLPATH;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWAXIS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWLABELS;
import static com.forrestguice.suntimeswidget.graph.LightMapDialog.PREF_KEY_GRAPH_SHOWMOON;
import static com.forrestguice.suntimeswidget.graph.LightGraphDialog.MAPTAG_LIGHTGRAPH;

public class WidgetThemePreview
{
    public WidgetThemePreview(Context context, int appWidgetId) {
        initData(context, appWidgetId);
    }

    private SuntimesUtils utils = new SuntimesUtils();

    private int appWidgetId = 0;
    public int getAppWidgetId() {
        return appWidgetId;
    }

    private boolean showTitle = true;
    public void setShowTitle(boolean value) {
        showTitle = value;
    }
    public boolean showTitle() {
        return showTitle;
    }

    private boolean showLabels = true;
    private boolean showWeeks = false;
    private boolean showHours = false;
    private boolean showTimeDate = false;
    private boolean showSeconds = false;
    private WidgetSettings.LengthUnit units = WidgetSettings.LengthUnit.METRIC;

    private SuntimesRiseSetDataset data0;
    private SuntimesRiseSetData data1;
    private SuntimesMoonData data2;

    private SuntimesCalculator.SunPosition sunPosition = null;
    private SuntimesCalculator.MoonPosition moonPosition = null;
    private Pair<Calendar, SuntimesCalculator.MoonPosition> apogee = null;
    private Pair<Calendar, SuntimesCalculator.MoonPosition> perigee = null;

    protected void initData(Context context, int appWidgetId)
    {
        this.appWidgetId = appWidgetId;
        data0 = new SuntimesRiseSetDataset(context, appWidgetId);  // use app configuration
        data0.calculateData();

        data1 = data0.dataActual;
        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data1);
        noonData.setTimeMode(WidgetSettings.TimeMode.NOON);
        noonData.calculate();
        data1.linkData(noonData);

        data2 = new SuntimesMoonData(context, appWidgetId, "moon");
        data2.calculate();

        showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        units = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
    }

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    public void updatePreview(@Nullable Integer layoutID, View previewLayout, ContentValues values )
    {
        View previewBackground = previewLayout.findViewById(R.id.widgetframe_inner);
        if (previewBackground != null)
        {
            String backgroundName = values.getAsString(SuntimesThemeContract.THEME_BACKGROUND);
            if (backgroundName != null)
            {
                SuntimesTheme.ThemeBackground background = SuntimesTheme.ThemeBackground.valueOf(backgroundName);
                if (background.supportsCustomColors())
                    previewBackground.setBackgroundColor(values.getAsInteger(SuntimesThemeContract.THEME_BACKGROUND_COLOR));
                else previewBackground.setBackgroundResource(background.getResID());

                int[] paddingPx = new int[] {
                        values.getAsInteger(SuntimesThemeContract.THEME_PADDING_LEFT + SuntimesThemeContract.THEME_PADDING_PIXELS),
                        values.getAsInteger(SuntimesThemeContract.THEME_PADDING_TOP + SuntimesThemeContract.THEME_PADDING_PIXELS),
                        values.getAsInteger(SuntimesThemeContract.THEME_PADDING_RIGHT + SuntimesThemeContract.THEME_PADDING_PIXELS),
                        values.getAsInteger(SuntimesThemeContract.THEME_PADDING_BOTTOM + SuntimesThemeContract.THEME_PADDING_PIXELS),
                };
                previewBackground.setPadding(paddingPx[0], paddingPx[1], paddingPx[2], paddingPx[3]);
            }
        }

        TextView previewTitle = (TextView)previewLayout.findViewById(R.id.text_title);
        if (previewTitle != null)
        {
            String displayText = values.getAsString(SuntimesThemeContract.THEME_DISPLAYSTRING);
            String titleText = (displayText.isEmpty() ? values.getAsString(SuntimesThemeContract.THEME_NAME) : displayText);
            previewTitle.setVisibility(showTitle ? View.VISIBLE : View.GONE);
            previewTitle.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TITLECOLOR));

            boolean boldText = values.getAsBoolean(SuntimesThemeContract.THEME_TITLEBOLD);
            if (boldText)
                previewTitle.setText(SuntimesUtils.createBoldSpan(null, titleText, titleText));
            else previewTitle.setText(titleText);

            updateSize(previewTitle, values.getAsFloat(SuntimesThemeContract.THEME_TITLESIZE), SuntimesThemeContract.THEME_TITLESIZE_MIN, SuntimesThemeContract.THEME_TITLESIZE_MAX);
        }

        if (layoutID == null)
        {
            updatePreview_sun(previewLayout, values);
            updatePreview_moon(previewLayout, values);
            updatePreview_clock(previewLayout, values);
            updatePreview_position0(previewLayout, values, 256, 32);
            updatePreview_position1(previewLayout, values, WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE);
            updatePreview_position2(previewLayout, values);
            updatePreview_position3(previewLayout, values, 256, 256);
            updatePreview_position4(previewLayout, values, 256, 256);
            //updatePreview_solstice(previewLayout);  // TODO

        } else if (WidgetSettings.WidgetModeSun1x1.supportsLayout(layoutID) || WidgetSettings.WidgetModeSun2x1.supportsLayout(layoutID) || WidgetSettings.WidgetModeSun3x1.supportsLayout(layoutID)) {
            updatePreview_sun(previewLayout, values);

        } else if (WidgetSettings.WidgetModeMoon1x1.supportsLayout(layoutID) || WidgetSettings.WidgetModeMoon2x1.supportsLayout(layoutID) || WidgetSettings.WidgetModeMoon3x1.supportsLayout(layoutID)) {
            updatePreview_moon(previewLayout, values);

        } else if (WidgetSettings.WidgetModeSunPos3x1.supportsLayout(layoutID)) {
            WidgetSettings.WidgetModeSunPos3x1 mode;
            try {
                mode = WidgetSettings.WidgetModeSunPos3x1.valueOf(values.getAsString(WidgetSettings.PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x1));
            } catch (IllegalArgumentException e) {
                mode = WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP;
            }
            switch (mode) {
                case MODE3x1_LIGHTMAP_SMALL: updatePreview_position0(previewLayout, values, 128, SunPosLayout_3X1_0.HEIGHT_SMALL - 4); break;
                case MODE3x1_LIGHTMAP_MEDIUM: updatePreview_position0(previewLayout, values, 128, SunPosLayout_3X1_0.HEIGHT_MEDIUM - 4); break;
                case MODE3x1_LIGHTMAP: default: updatePreview_position0(previewLayout, values, 128, SunPosLayout_3X1_0.HEIGHT_LARGE - 4); break;
            }

        } else if (WidgetSettings.WidgetModeSunPos3x2.supportsLayout(layoutID)) {
            WidgetSettings.WidgetModeSunPos3x2 mode;
            try {
                mode = WidgetSettings.WidgetModeSunPos3x2.valueOf(values.getAsString(WidgetSettings.PREF_KEY_APPEARANCE_WIDGETMODE_SUNPOS3x2));
            } catch (IllegalArgumentException e) {
                mode = WidgetSettings.WidgetModeSunPos3x2.MODE3x2_WORLDMAP;
            }
            switch (mode) {
                case MODE3x2_LINEGRAPH: updatePreview_position3(previewLayout, values, 128, 64); break;
                case MODE3x2_LIGHTGRAPH: updatePreview_position4(previewLayout, values, 128, 64); break;
                case MODE3x2_WORLDMAP: default:
                    WorldMapWidgetSettings.WorldMapWidgetMode mapMode = WorldMapWidgetSettings.WorldMapWidgetMode.findMode(layoutID);
                    updatePreview_position1(previewLayout, values, (mapMode != null ? mapMode : WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE));
                    break;
            }

        } else if (WorldMapWidgetSettings.WorldMapWidgetMode.supportsLayout(layoutID)) {
            WorldMapWidgetSettings.WorldMapWidgetMode mode = WorldMapWidgetSettings.WorldMapWidgetMode.findMode(layoutID);
            updatePreview_position1(previewLayout, values, (mode != null ? mode : WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE));

        } else if (WidgetSettings.WidgetModeSunPos1x1.supportsLayout(layoutID)) {
            updatePreview_position2(previewLayout, values);

        } else {
            updatePreview_clock(previewLayout, values);
            //updatePreview_solstice(previewLayout);  // TODO
        }
    }

    public void updatePreview_position0(View previewLayout, ContentValues values, int widthDp, int heightDp)
    {
        final ImageView view = (ImageView)previewLayout.findViewById(R.id.info_time_lightmap);
        if (view != null)
        {
            Context context = view.getContext();
            LightMapView.LightMapColors colors = new LightMapView.LightMapColors();
            colors.initDefaultDark(previewLayout.getContext());

            colors.values.setColor(LightMapColorValues.COLOR_DAY, values.getAsInteger(SuntimesThemeContract.THEME_DAYCOLOR));
            colors.values.setColor(LightMapColorValues.COLOR_CIVIL, values.getAsInteger(SuntimesThemeContract.THEME_CIVILCOLOR));
            colors.values.setColor(LightMapColorValues.COLOR_NAUTICAL, values.getAsInteger(SuntimesThemeContract.THEME_NAUTICALCOLOR));
            colors.values.setColor(LightMapColorValues.COLOR_ASTRONOMICAL, values.getAsInteger(SuntimesThemeContract.THEME_ASTROCOLOR));
            colors.values.setColor(LightMapColorValues.COLOR_NIGHT, values.getAsInteger(SuntimesThemeContract.THEME_NIGHTCOLOR));
            colors.values.setColor(LightMapColorValues.COLOR_POINT_FILL, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR));
            colors.values.setColor(LightMapColorValues.COLOR_POINT_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR));
            colors.values.setColor(LightMapColorValues.COLOR_SUN_FILL, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR));
            colors.values.setColor(LightMapColorValues.COLOR_SUN_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR));

            if (values.getAsInteger("option_drawNow") != null) {
                colors.option_drawNow = values.getAsInteger("option_drawNow");
            }
            if (values.getAsInteger("option_drawNow_pointSizePx") != null) {
                colors.option_drawNow_pointSizePx = values.getAsInteger("option_drawNow_pointSizePx");
            }

            LightMapView.LightMapTask drawTask = new LightMapView.LightMapTask();
            drawTask.setListener(new LightMapView.LightMapTaskListener()
            {
                @Override
                public void onFinished(Bitmap result)
                {
                    super.onFinished(result);
                    view.setImageBitmap(result);
                }
            });

            int widthPx = SuntimesUtils.dpToPixels(context, widthDp);
            int heightPx = SuntimesUtils.dpToPixels(context, heightDp);
            view.setMinimumWidth(widthPx);
            view.setMinimumHeight(heightPx);
            drawTask.execute(data0, widthPx, heightPx, colors);
        }
    }

    public void updatePreview_position1(View previewLayout, ContentValues values, WorldMapWidgetSettings.WorldMapWidgetMode mode)
    {
        final ImageView view = (ImageView)previewLayout.findViewById(R.id.info_time_worldmap);
        if (view != null)
        {
            Context context = previewLayout.getContext();

            WorldMapTask.WorldMapOptions options = new WorldMapTask.WorldMapOptions();
            options.map = ContextCompat.getDrawable(context, R.drawable.worldmap);
            options.colors.setColor(WorldMapColorValues.COLOR_BACKGROUND, values.getAsInteger(SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR));
            options.colors.setColor(WorldMapColorValues.COLOR_FOREGROUND, values.getAsInteger(SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR));
            options.colors.setColor(WorldMapColorValues.COLOR_SUN_SHADOW, values.getAsInteger(SuntimesThemeContract.THEME_MAP_SHADOWCOLOR));
            options.colors.setColor(WorldMapColorValues.COLOR_MOON_LIGHT, values.getAsInteger(SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR));

            options.colors.setColor(WorldMapColorValues.COLOR_SUN_FILL, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR));
            options.colors.setColor(WorldMapColorValues.COLOR_SUN_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR));
            options.sunScale = 24;      // extra large so preview of colors is visible

            options.colors.setColor(WorldMapColorValues.COLOR_MOON_FILL, values.getAsInteger(SuntimesThemeContract.THEME_MOONFULLCOLOR));
            options.colors.setColor(WorldMapColorValues.COLOR_MOON_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_MOONWANINGCOLOR));
            options.moonScale = 32;

            int[] sizeDp = suggestedPreviewSizeDp(mode);
            WorldMapTask.WorldMapProjection projection = SunPosLayout_3X2_0.createProjectionForMode(context, mode, options);
            WorldMapTask drawTask = new WorldMapTask();
            drawTask.setListener(new WorldMapTask.WorldMapTaskListener()
            {
                @Override
                public void onFinished(Bitmap lastFrame)
                {
                    super.onFinished(lastFrame);
                    view.setImageBitmap(lastFrame);
                }
            });

            int widthPx = SuntimesUtils.dpToPixels(context, sizeDp[0]);
            int heightPx = SuntimesUtils.dpToPixels(context, sizeDp[1]);
            view.setMinimumWidth(widthPx);
            view.setMinimumHeight(heightPx);
            drawTask.execute(data0,  widthPx, heightPx, options, projection);
        }
    }

    public static int[] suggestedPreviewSizeDp(WorldMapWidgetSettings.WorldMapWidgetMode mode)
    {
        switch (mode)
        {
            case MERCATOR_SIMPLE:
            case VANDERGRINTEN_SIMPLE:
            case EQUIAZIMUTHAL_SIMPLE:
            case EQUIAZIMUTHAL_SIMPLE1:
            case EQUIAZIMUTHAL_SIMPLE2:
                return new int[] { 128, 128 };

            case SINUSOIDAL_SIMPLE:
            case EQUIRECTANGULAR_BLUEMARBLE:
            case EQUIRECTANGULAR_SIMPLE:
            default:
                return new int[] { 128, 64 };
        }
    }

    public void updatePreview_position2(View previewLayout, ContentValues values)
    {
        boolean boldTime = values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD);
        int highlightColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR);
        int suffixColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR);
        int textColor = values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR);

        TextView previewAzimuth = (TextView) previewLayout.findViewById(R.id.info_sun_azimuth_current);
        TextView previewElevation = (TextView) previewLayout.findViewById(R.id.info_sun_elevation_current);
        TextView previewRightAsc = (TextView) previewLayout.findViewById(R.id.info_sun_rightascension_current);
        TextView previewDeclination = (TextView) previewLayout.findViewById(R.id.info_sun_declination_current);

        if (previewAzimuth != null || previewElevation != null || previewRightAsc != null || previewDeclination != null)
        {
            if (sunPosition == null) {
                sunPosition = data0.calculator().getSunPosition(data0.now());
            }
            if (sunPosition == null) {
                return;
            }

            if (previewAzimuth != null)
            {
                previewAzimuth.setTextColor(textColor);
                previewAzimuth.setText(SunPosLayout.styleAzimuthText(utils.formatAsDirection2(sunPosition.azimuth, PositionLayout.DECIMAL_PLACES, false), highlightColor, suffixColor, boldTime));
                updateSize(previewAzimuth, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewAzimuthLabel = (TextView) previewLayout.findViewById(R.id.info_sun_azimuth_current_label);
                if (previewAzimuthLabel != null)
                {
                    previewAzimuthLabel.setTextColor(textColor);
                    updateSize(previewAzimuthLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewElevation != null)
            {
                previewElevation.setTextColor(textColor);
                previewElevation.setText(SunPosLayout.styleElevationText(sunPosition.elevation, highlightColor, suffixColor, boldTime));
                updateSize(previewElevation, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewElevationLabel = (TextView) previewLayout.findViewById(R.id.info_sun_elevation_current_label);
                if (previewElevationLabel != null)
                {
                    previewElevationLabel.setTextColor(textColor);
                    updateSize(previewElevationLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewRightAsc != null)
            {
                previewRightAsc.setTextColor(textColor);
                previewRightAsc.setText(SunPosLayout.styleRightAscText(sunPosition, highlightColor, suffixColor, boldTime));
                updateSize(previewRightAsc, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewRightAscLabel = (TextView) previewLayout.findViewById(R.id.info_sun_rightascension_current_label);
                if (previewRightAscLabel != null)
                {
                    previewRightAscLabel.setTextColor(textColor);
                    updateSize(previewRightAscLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewDeclination != null)
            {
                previewDeclination.setTextColor(textColor);
                previewDeclination.setText(SunPosLayout.styleDeclinationText(sunPosition, highlightColor, suffixColor, boldTime));
                updateSize(previewDeclination, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewDeclinationLabel = (TextView) previewLayout.findViewById(R.id.info_sun_declination_current_label);
                if (previewDeclinationLabel != null)
                {
                    previewDeclinationLabel.setTextColor(textColor);
                    updateSize(previewDeclinationLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }
        }
    }

    public void updatePreview_position3(View previewLayout, ContentValues values, int widthDp, int heightDp)
    {
        final ImageView view = (ImageView)previewLayout.findViewById(R.id.info_time_graph);
        if (view != null)
        {
            Context context = view.getContext();
            LineGraphView.LineGraphOptions options = new LineGraphView.LineGraphOptions(context);
            options.initDefaultDark(previewLayout.getContext());
            options.graph_width = LineGraphView.MINUTES_IN_DAY;
            options.graph_height = 180;
            options.graph_x_offset = options.graph_y_offset = 0;
            options.gridX_minor_show = options.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTMAP, DEF_KEY_WORLDMAP_MINORGRID);
            options.axisX_labels_show = options.axisY_labels_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWLABELS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWLABELS);
            options.axisX_show = options.axisY_show = options.gridY_major_show = options.gridX_major_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWAXIS);
            options.sunPath_show_line = true;
            options.sunPath_show_fill = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_FILLPATH, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_FILLPATH);
            options.moonPath_show_line = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWMOON, MAPTAG_LIGHTMAP, DEF_KEY_GRAPH_SHOWMOON);
            options.moonPath_show_fill = options.sunPath_show_fill;
            options.densityDpi = context.getResources().getDisplayMetrics().densityDpi;
            options.setTimeFormat(context, WidgetSettings.loadTimeFormatModePref(context, 0));

            LineGraphView.LineGraphTask drawTask = new LineGraphView.LineGraphTask();
            drawTask.setListener(new LineGraphView.LineGraphTaskListener() 
            {
                @Override
                public void onFinished(Bitmap result) {
                    super.onFinished(result);
                    view.setImageBitmap(result);
                }
            });

            int widthPx = SuntimesUtils.dpToPixels(context, widthDp);
            int heightPx = SuntimesUtils.dpToPixels(context, heightDp);
            view.setMinimumWidth(widthPx);
            view.setMinimumHeight(heightPx);
            drawTask.execute(data0, widthPx, heightPx, options);
        }
    }

    public void updatePreview_position4(View previewLayout, ContentValues values, final int widthDp, final int heightDp)
    {
        final ImageView view = (ImageView) previewLayout.findViewById(R.id.info_time_graph);
        if (view != null)
        {
            final Context context = view.getContext();
            final LightGraphView.LightGraphOptions options = new LightGraphView.LightGraphOptions(context);

            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            options.colors = LightGraphColorValues.getColorDefaults(context, isNightMode);

            String tzId = WorldMapWidgetSettings.loadWorldMapString(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_TIMEZONE, MAPTAG_LIGHTGRAPH, WidgetTimezones.LocalMeanTime.TIMEZONEID);
            options.timezone = WidgetTimezones.TZID_SUNTIMES.equals(tzId) ? data0.timezone()
                    : WidgetTimezones.getTimeZone(tzId, data0.location().getLongitudeAsDouble(), data0.calculator());

            options.graph_width = 365;    // days
            options.graph_height = 24;    // hours
            options.graph_x_offset = options.graph_y_offset = 0;
            options.densityDpi = context.getResources().getDisplayMetrics().densityDpi;
            options.setTimeFormat(context, WidgetSettings.loadTimeFormatModePref(context, 0));

            options.axisX_show = options.axisY_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, PREF_KEY_GRAPH_SHOWAXIS, MAPTAG_LIGHTGRAPH, DEF_KEY_GRAPH_SHOWAXIS);
            options.gridX_minor_show = options.gridY_minor_show = WorldMapWidgetSettings.loadWorldMapPref(context, 0, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MINORGRID, MAPTAG_LIGHTGRAPH, DEF_KEY_WORLDMAP_MINORGRID);
            options.gridX_major_show = options.gridY_major_show = false;
            options.axisX_width = options.axisY_width = 365;
            options.sunPath_show_points = true;
            options.sunPath_show_fill = true;

            options.colors.setColor(LightGraphColorValues.COLOR_NIGHT, values.getAsInteger(SuntimesThemeContract.THEME_NIGHTCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_DAY, values.getAsInteger(SuntimesThemeContract.THEME_DAYCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_CIVIL, values.getAsInteger(SuntimesThemeContract.THEME_CIVILCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_NAUTICAL, values.getAsInteger(SuntimesThemeContract.THEME_NAUTICALCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_ASTRONOMICAL, values.getAsInteger(SuntimesThemeContract.THEME_ASTROCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_POINT_FILL, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_POINT_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_SUN_FILL, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_SUN_STROKE, values.getAsInteger(SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_SPRING, values.getAsInteger(SuntimesThemeContract.THEME_SPRINGCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_SUMMER, values.getAsInteger(SuntimesThemeContract.THEME_SUMMERCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_AUTUMN, values.getAsInteger(SuntimesThemeContract.THEME_FALLCOLOR));
            options.colors.setColor(LightGraphColorValues.COLOR_WINTER, values.getAsInteger(SuntimesThemeContract.THEME_WINTERCOLOR));

            final int widthPx = SuntimesUtils.dpToPixels(context, widthDp);
            final int heightPx = SuntimesUtils.dpToPixels(context, heightDp);
            view.setMinimumWidth(widthPx);
            view.setMinimumHeight(heightPx);

            final LightGraphView.LightGraphTask drawTask = new LightGraphView.LightGraphTask();
            drawTask.setListener(new LightGraphView.LightGraphTaskListener()
            {
                @Override
                public void onFinished(Bitmap result)
                {
                    super.onFinished(result);
                    view.setImageBitmap(result);
                }
            });

            ExecutorService executor = Executors.newSingleThreadExecutor();
            final Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(data0);
                    //data.setCalculator(context, com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator.getDescriptor());
                    data.calculateData();
                    drawTask.setData(LightGraphView.LightGraphTask.createYearData(context, data));
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run() {
                            drawTask.execute(data0, widthPx, heightPx, options);
                        }
                    });
                }
            });
        }
    }

    /**
     * @param previewLayout the layout to update
     */
    public void updatePreview_clock(View previewLayout, ContentValues values)
    {
        Context context = previewLayout.getContext();
        TextView previewTime = (TextView)previewLayout.findViewById(R.id.text_time);
        TextView previewTimeSuffix = (TextView)previewLayout.findViewById(R.id.text_time_suffix);
        if (previewTime != null && previewTimeSuffix != null)
        {
            int[] padding = new int[] {
                    values.getAsInteger(SuntimesThemeContract.THEME_PADDING_LEFT),
                    values.getAsInteger(SuntimesThemeContract.THEME_PADDING_TOP),
                    values.getAsInteger(SuntimesThemeContract.THEME_PADDING_RIGHT),
                    values.getAsInteger(SuntimesThemeContract.THEME_PADDING_BOTTOM),
            };

            float[] adjustedSizeSp = SuntimesLayout.adjustTextSize(context, new int[] {80, 80}, padding,
                    "sans-serif", values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD),"00:00", values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MAX, "MM", values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE));
            previewTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[0]);
            previewTimeSuffix.setTextSize(TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[1]);

            Calendar now = Calendar.getInstance();
            WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, 0);
            SuntimesUtils.TimeDisplayText nowText = utils.calendarTimeShortDisplayString(context, now, false, timeFormat);
            String nowString = nowText.getValue();
            CharSequence nowChars = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

            previewTime.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR));
            previewTime.setText(nowChars);

            previewTimeSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            previewTimeSuffix.setText(nowText.getSuffix());
        }
    }

    /**
     * @param previewLayout the layout to update
     */
    public void updatePreview_sun(View previewLayout, ContentValues values)
    {
        Context context = previewLayout.getContext();

        // Noon
        TextView previewNoon = (TextView)previewLayout.findViewById(R.id.text_time_noon);
        TextView previewNoonSuffix = (TextView)previewLayout.findViewById(R.id.text_time_noon_suffix);

        SuntimesRiseSetData noonData = data1.getLinked();
        SuntimesUtils.TimeDisplayText noonText = ((noonData != null)
                ? utils.calendarTimeShortDisplayString(previewLayout.getContext(), noonData.sunriseCalendarToday())
                : new SuntimesUtils.TimeDisplayText("12:00"));
        if (previewNoon != null)
        {
            String noonString = noonText.getValue();
            CharSequence noon = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, noonString, noonString) : noonString);
            previewNoon.setText(noon);
            previewNoon.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_NOONCOLOR));
            updateSize(previewNoon, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
        }
        if (previewNoonSuffix != null)
        {
            previewNoonSuffix.setText(noonText.getSuffix());
            previewNoonSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            updateSize(previewNoonSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE), SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN, SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX);
        }

        // Sunrise
        TextView previewRise = (TextView)previewLayout.findViewById(R.id.text_time_rise);
        TextView previewRiseSuffix = (TextView)previewLayout.findViewById(R.id.text_time_rise_suffix);

        SuntimesUtils.TimeDisplayText riseText = utils.calendarTimeShortDisplayString(context, data1.sunriseCalendarToday());
        if (previewRise != null)
        {
            String riseString = riseText.getValue();
            CharSequence rise = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, riseString, riseString) : riseString);
            previewRise.setText(rise);
            previewRise.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_SUNRISECOLOR));
            updateSize(previewRise, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
        }
        if (previewRiseSuffix != null)
        {
            previewRiseSuffix.setText(riseText.getSuffix());
            previewRiseSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            updateSize(previewRiseSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE), SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN, SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX);
        }

        // Sunset
        TextView previewSet = (TextView)previewLayout.findViewById(R.id.text_time_set);
        TextView previewSetSuffix = (TextView)previewLayout.findViewById(R.id.text_time_set_suffix);

        SuntimesUtils.TimeDisplayText setText = utils.calendarTimeShortDisplayString(context, data1.sunsetCalendarToday());
        if (previewSet != null)
        {
            String setString = setText.getValue();
            CharSequence set = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, setString, setString) : setString);
            previewSet.setText(set);
            previewSet.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_SUNSETCOLOR));
            updateSize(previewSet, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
        }
        if (previewSetSuffix != null)
        {
            previewSetSuffix.setText(setText.getSuffix());
            previewSetSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            updateSize(previewSetSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE), SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN, SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX);
        }

        // Time Delta
        TextView previewTimeDelta = (TextView)previewLayout.findViewById(R.id.text_delta_day_value);
        TextView previewTimeDeltaPrefix = (TextView)previewLayout.findViewById(R.id.text_delta_day_prefix);
        TextView previewTimeDeltaSuffix = (TextView)previewLayout.findViewById(R.id.text_delta_day_suffix);

        if (previewTimeDelta != null)
        {
            previewTimeDelta.setText(utils.timeDeltaLongDisplayString(data1.dayLengthToday(), data1.dayLengthOther()).getValue());
            previewTimeDelta.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR));
            updateSize(previewTimeDelta, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }
        if (previewTimeDeltaPrefix != null)
        {
            previewTimeDeltaPrefix.setText(context.getString(R.string.delta_day_tomorrow));
            previewTimeDeltaPrefix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR));
            updateSize(previewTimeDeltaPrefix, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }
        if (previewTimeDeltaSuffix != null)
        {
            previewTimeDeltaSuffix.setText(context.getString(R.string.delta_day_shorter));
            previewTimeDeltaSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR));
            updateSize(previewTimeDeltaSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        // Icons
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int strokePixels = (int)((metrics.density * values.getAsFloat(SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH)) + 0.5f);
        int noonStrokePixels = (int)((metrics.density * values.getAsFloat(SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH)) + 0.5f);

        ImageView previewRiseIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_sunrise);
        if (previewRiseIcon != null) {
            previewRiseIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunrise0, values.getAsInteger(SuntimesThemeContract.THEME_RISEICON_FILL_COLOR), values.getAsInteger(SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR), strokePixels));
        }

        ImageView previewSetIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_sunset);
        if (previewSetIcon != null) {
            previewSetIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_sunset0, values.getAsInteger(SuntimesThemeContract.THEME_SETICON_FILL_COLOR), values.getAsInteger(SuntimesThemeContract.THEME_SETICON_STROKE_COLOR), strokePixels));
        }

        ImageView previewNoonIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_noon);
        if (previewNoonIcon != null) {
            Bitmap noonIcon = SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_noon_large1, values.getAsInteger(SuntimesThemeContract.THEME_NOONICON_FILL_COLOR), values.getAsInteger(SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR), noonStrokePixels);   // doesn't call mutate (themes other Drawable instances)
            Drawable d = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_noon_large1, null);
            previewNoonIcon.setImageDrawable(d);
        }
    }

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    /**protected void updatePreview_solstice(View previewLayout)
    {
        // TODO: spring color
        //chooseColorSpring.getColor();
        // TODO: summer color
        //chooseColorSummer.getColor();
        // TODO: autumn color
        //chooseColorFall.getColor();
        // TODO: winter color
        //chooseColorWinter.getColor();
    }*/

    /**protected void updatePreview_position(View previewLayout)
    {
        // TODO: day color
        //chooseColorDay.getColor();
        // TODO: civil color
        //chooseColorCivil.getColor();
        // TODO: nautical color
        //chooseColorNautical.getColor();
        // TODO: astro color
        //chooseColorAstro.getColor();
        // TODO: night color
        //chooseColorNight.getColor();
    }*/

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    public void updatePreview_moon(View previewLayout, ContentValues values)
    {
        Context context = previewLayout.getContext();

        // Moonrise
        TextView previewMoonrise = (TextView)previewLayout.findViewById(R.id.text_time_moonrise);
        TextView previewMoonriseSuffix = (TextView)previewLayout.findViewById(R.id.text_time_moonrise_suffix);

        SuntimesUtils.TimeDisplayText moonriseText = utils.calendarTimeShortDisplayString(context, data2.moonriseCalendarToday());
        if (previewMoonrise != null)
        {
            String riseString = moonriseText.getValue();
            CharSequence rise = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, riseString, riseString) : riseString);
            previewMoonrise.setText(rise);
            previewMoonrise.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_MOONRISECOLOR));
            updateSize(previewMoonrise, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
        }
        if (previewMoonriseSuffix != null)
        {
            previewMoonriseSuffix.setText(moonriseText.getSuffix());
            previewMoonriseSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            updateSize(previewMoonriseSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE), SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN, SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX);
        }

        // Moonset
        TextView previewMoonset = (TextView)previewLayout.findViewById(R.id.text_time_moonset);
        TextView previewMoonsetSuffix = (TextView)previewLayout.findViewById(R.id.text_time_moonset_suffix);

        SuntimesUtils.TimeDisplayText moonsetText = utils.calendarTimeShortDisplayString(context, data2.moonsetCalendarToday());
        if (previewMoonset != null)
        {
            String setString = moonsetText.getValue();
            CharSequence set = (values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD) ? SuntimesUtils.createBoldSpan(null, setString, setString) : setString);
            previewMoonset.setText(set);
            previewMoonset.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_MOONSETCOLOR));
            updateSize(previewMoonset, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
        }
        if (previewMoonsetSuffix != null)
        {
            previewMoonsetSuffix.setText(moonsetText.getSuffix());
            previewMoonsetSuffix.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR));
            updateSize(previewMoonsetSuffix, values.getAsFloat(SuntimesThemeContract.THEME_TIMESUFFIXSIZE), SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN, SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX);
        }

        // Moon Phase / Illumination
        TextView previewMoonPhase = (TextView)previewLayout.findViewById(R.id.text_info_moonphase);
        if (previewMoonPhase != null)
        {
            int phaseColor = colorForMoonPhase(data2.getMoonPhaseToday(), values);
            previewMoonPhase.setText(data2.getMoonPhaseToday().getLongDisplayString());
            previewMoonPhase.setTextColor(phaseColor);
            updateSize(previewMoonPhase, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        TextView previewMoonIllum = (TextView)previewLayout.findViewById(R.id.text_info_moonillum);
        if (previewMoonIllum != null)
        {
            NumberFormat percentage = NumberFormat.getPercentInstance();
            previewMoonIllum.setText(percentage.format(data2.getMoonIlluminationToday()));
            previewMoonIllum.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR));
            updateSize(previewMoonPhase, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        // Position
        updatePreview_moonPosition(previewLayout, values);
        updatePreview_moonApogeePerigee(previewLayout, values);
        updatePreview_moonDay(previewLayout, values);

        // Moon Labels
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_new_label), values);
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_firstquarter_label), values);
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_full_label), values);
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_thirdquarter_label), values);

        // Moon Icons

        ImageView previewMoonriseIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_moonrise);
        if (previewMoonriseIcon != null)
        {
            int moonriseColor = values.getAsInteger(SuntimesThemeContract.THEME_MOONRISECOLOR);
            previewMoonriseIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_moon_rise, moonriseColor, moonriseColor, 0));
        }

        ImageView previewMoonsetIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_moonset);
        if (previewMoonsetIcon != null)
        {
            int moonsetColor = values.getAsInteger(SuntimesThemeContract.THEME_MOONSETCOLOR);
            previewMoonsetIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, R.drawable.ic_moon_set, moonsetColor, moonsetColor, 0));
        }

        int colorWaxing = values.getAsInteger(SuntimesThemeContract.THEME_MOONWAXINGCOLOR);
        int colorWaning = values.getAsInteger(SuntimesThemeContract.THEME_MOONWANINGCOLOR);
        int colorFull = values.getAsInteger(SuntimesThemeContract.THEME_MOONFULLCOLOR);
        int colorNew =  values.getAsInteger(SuntimesThemeContract.THEME_MOONNEWCOLOR);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int moonStrokePx =  (int)((metrics.density * values.getAsFloat(SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH)) + 0.5f);

        // full and new
        ImageView previewMoonFullIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_full);
        if (previewMoonFullIcon != null) {
            previewMoonFullIcon.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, moonStrokePx));
        }

        ImageView previewMoonNewIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_new);
        if (previewMoonNewIcon != null) {
            previewMoonNewIcon.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, moonStrokePx));
        }

        // waxing
        ImageView previewMoonWaxingCrescentIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waxing_crescent);
        if (previewMoonWaxingCrescentIcon != null) {
            previewMoonWaxingCrescentIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_CRESCENT.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaxingQuarterIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waxing_quarter);
        if (previewMoonWaxingQuarterIcon != null) {
            previewMoonWaxingQuarterIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaxingGibbousIcon = (ImageView) previewLayout.findViewById(R.id.icon_info_moonphase_waxing_gibbous);
        if (previewMoonWaxingGibbousIcon != null) {
            previewMoonWaxingGibbousIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WAXING_GIBBOUS.getIcon(), colorWaxing, colorWaxing, 0));
        }

        // waning
        ImageView previewMoonWaningCrescentIcon = (ImageView) previewLayout.findViewById(R.id.icon_info_moonphase_waning_crescent);
        if (previewMoonWaningCrescentIcon != null) {
            previewMoonWaningCrescentIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_CRESCENT.getIcon(), colorWaning, colorWaning, 0));
        }

        ImageView previewMoonWaningQuarterIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waning_quarter);
        if (previewMoonWaningQuarterIcon != null) {
            previewMoonWaningQuarterIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0));
        }

        ImageView previewMoonWaningGibbousIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waning_gibbous);
        if (previewMoonWaningGibbousIcon != null) {
            previewMoonWaningGibbousIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.WANING_GIBBOUS.getIcon(), colorWaning, colorWaning, 0));
        }

        MoonPhaseDisplay phase = data2.getMoonPhaseToday();
        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            View iconView = previewLayout.findViewById(moonPhase.getView());
            if (iconView != null) {
                iconView.setVisibility((phase == moonPhase) ? View.VISIBLE : View.GONE);
            }
        }

        ImageView previewMoonFullIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_full_icon);
        if (previewMoonFullIcon1 != null) {
            previewMoonFullIcon1.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, moonStrokePx));
        }

        ImageView previewMoonNewIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_new_icon);
        if (previewMoonNewIcon1 != null) {
            previewMoonNewIcon1.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, moonStrokePx));
        }

        ImageView previewMoonWaxingQuarterIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_firstquarter_icon);
        if (previewMoonWaxingQuarterIcon1 != null) {
            previewMoonWaxingQuarterIcon1.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaningQuarterIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_thirdquarter_icon);
        if (previewMoonWaningQuarterIcon1 != null) {
            previewMoonWaningQuarterIcon1.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0));
        }
    }

    public void updatePreview_moonPosition(View previewLayout, ContentValues values)
    {
        Context context = previewLayout.getContext();
        boolean boldTime = values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD);
        int highlightColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR);
        int suffixColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR);
        int textColor = values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR);

        TextView previewMoonElevation = (TextView)previewLayout.findViewById(R.id.info_moon_elevation_current);
        TextView previewMoonAzimuth = (TextView)previewLayout.findViewById(R.id.info_moon_azimuth_current);
        TextView previewMoonDeclination = (TextView)previewLayout.findViewById(R.id.info_moon_declination_current);
        TextView previewMoonRightAsc = (TextView)previewLayout.findViewById(R.id.info_moon_rightascension_current);
        if (previewMoonDeclination != null || previewMoonRightAsc != null || previewMoonElevation != null || previewMoonAzimuth != null)
        {
            if (moonPosition == null) {
                moonPosition = data2.calculator().getMoonPosition(data2.now());    // lazy init
            }

            if (previewMoonDeclination != null) {
                previewMoonDeclination.setTextColor(textColor);
                previewMoonDeclination.setText(MoonLayout_1x1_6.styleDeclinationText(moonPosition, boldTime, highlightColor, suffixColor));
                updateSize(previewMoonDeclination, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewMoonDeclinationLabel = (TextView)previewLayout.findViewById(R.id.info_moon_declination_current_label);
                if (previewMoonDeclinationLabel != null) {
                    previewMoonDeclinationLabel.setTextColor(textColor);
                    updateSize(previewMoonDeclinationLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewMoonRightAsc != null) {
                previewMoonRightAsc.setTextColor(textColor);
                previewMoonRightAsc.setText(MoonLayout_1x1_6.styleRightAscText(moonPosition, boldTime, highlightColor, suffixColor));
                updateSize(previewMoonRightAsc, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewMoonRightAscLabel = (TextView)previewLayout.findViewById(R.id.info_moon_rightascension_current_label);
                if (previewMoonRightAscLabel != null) {
                    previewMoonRightAscLabel.setTextColor(textColor);
                    updateSize(previewMoonRightAscLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewMoonAzimuth != null)
            {
                SuntimesUtils.TimeDisplayText azimuthDisplay = utils.formatAsDirection2(moonPosition.azimuth, PositionLayout.DECIMAL_PLACES, false);
                previewMoonAzimuth.setTextColor(textColor);
                previewMoonAzimuth.setText(PositionLayout.styleAzimuthText(azimuthDisplay, highlightColor, suffixColor, boldTime));
                updateSize(previewMoonAzimuth, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewMoonAzimuthLabel = (TextView)previewLayout.findViewById(R.id.info_moon_azimuth_current_label);
                if (previewMoonAzimuthLabel != null) {
                    previewMoonAzimuthLabel.setTextColor(textColor);
                    updateSize(previewMoonAzimuthLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }

            if (previewMoonElevation != null)
            {
                previewMoonElevation.setTextColor(textColor);
                previewMoonElevation.setText(PositionLayout.styleElevationText(moonPosition.elevation, highlightColor, suffixColor, boldTime));
                updateSize(previewMoonElevation, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

                TextView previewMoonElevationLabel = (TextView)previewLayout.findViewById(R.id.info_moon_elevation_current_label);
                if (previewMoonElevationLabel != null) {
                    previewMoonElevationLabel.setTextColor(textColor);
                    updateSize(previewMoonElevationLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
                }
            }
        }

        TextView previewMoonDistance = (TextView)previewLayout.findViewById(R.id.info_moon_distance_current);
        if (previewMoonDistance != null)
        {
            if (moonPosition == null) {
                moonPosition = data2.calculator().getMoonPosition(data2.now());    // lazy init
            }

            previewMoonDistance.setText(MoonLayout_1x1_7.styleDistanceText(context, moonPosition, units, highlightColor, suffixColor, boldTime));
            updateSize(previewMoonDistance, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);

            TextView previewMoonDistanceLabel = (TextView)previewLayout.findViewById(R.id.info_moon_distance_current_label);
            if (previewMoonDistanceLabel != null) {
                previewMoonDistanceLabel.setTextColor(textColor);
                updateSize(previewMoonDistanceLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
            }
        }
    }

    public void updatePreview_moonDay(View previewLayout, ContentValues values)
    {
        //Context context = previewLayout.getContext();
        //int timeColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR);
        int textColor = values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR);

        TextView previewMoonDay = (TextView)previewLayout.findViewById(R.id.info_moon_day);
        if (previewMoonDay != null)
        {
            previewMoonDay.setText("4");    // TODO
            previewMoonDay.setTextColor(textColor);
            updateSize(previewMoonDay, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        TextView previewLabel = (TextView)previewLayout.findViewById(R.id.info_moon_day_label);
        if (previewLabel != null) {
            previewLabel.setTextColor(textColor);
            updateSize(previewLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }
    }

    public void updatePreview_moonApogeePerigee(View previewLayout, ContentValues values)
    {
        Context context = previewLayout.getContext();

        boolean boldTime = values.getAsBoolean(SuntimesThemeContract.THEME_TIMEBOLD);
        int suffixColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMESUFFIXCOLOR);
        int textColor = values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR);
        int timeColor = values.getAsInteger(SuntimesThemeContract.THEME_TIMECOLOR);

        TextView previewMoonApogee = (TextView)previewLayout.findViewById(R.id.moonapsis_apogee_distance);
        if (previewMoonApogee != null)
        {
            TextView previewMoonApogeeLabel = (TextView)previewLayout.findViewById(R.id.moonapsis_apogee_label);
            TextView previewMoonApogeeDate = (TextView)previewLayout.findViewById(R.id.moonapsis_apogee_date);
            TextView previewMoonApogeeNote = (TextView)previewLayout.findViewById(R.id.moonapsis_apogee_note);

            previewMoonApogeeLabel.setTextColor(textColor);
            previewMoonApogeeDate.setTextColor(timeColor);
            previewMoonApogeeNote.setTextColor(textColor);
            previewMoonApogee.setTextColor(textColor);

            if (apogee == null) {
                apogee = data2.getMoonApogee();
            }
            if (apogee != null)
            {
                SuntimesUtils.TimeDisplayText perigeeString = utils.calendarDateTimeDisplayString(context, apogee.first, showTimeDate, showSeconds);
                previewMoonApogeeDate.setText(perigeeString.getValue());
                previewMoonApogeeNote.setText(MoonLayout_1x1_8.noteSpan(context, data2.now(), apogee.first, showWeeks, showHours, timeColor, boldTime));

                if (apogee.second != null) {
                    int risingColor = values.getAsInteger(SuntimesThemeContract.THEME_MOONRISECOLOR);
                    previewMoonApogee.setText(MoonLayout_1x1_8.distanceSpan(context, apogee.second.distance, units, risingColor, suffixColor, boldTime));
                }
            }

            updateSize(previewMoonApogeeLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
            updateSize(previewMoonApogeeDate, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
            updateSize(previewMoonApogeeNote, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
            updateSize(previewMoonApogee, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        TextView previewMoonPerigee = (TextView)previewLayout.findViewById(R.id.moonapsis_perigee_distance);
        if (previewMoonPerigee != null)
        {
            TextView previewMoonPerigeeLabel = (TextView)previewLayout.findViewById(R.id.moonapsis_perigee_label);
            TextView previewMoonPerigeeDate = (TextView)previewLayout.findViewById(R.id.moonapsis_perigee_date);
            TextView previewMoonPerigeeNote = (TextView)previewLayout.findViewById(R.id.moonapsis_perigee_note);

            previewMoonPerigeeLabel.setTextColor(textColor);
            previewMoonPerigeeDate.setTextColor(timeColor);
            previewMoonPerigeeNote.setTextColor(textColor);
            previewMoonPerigee.setTextColor(textColor);

            if (perigee == null) {
                perigee = data2.getMoonPerigee();
            }
            if (perigee != null)
            {
                SuntimesUtils.TimeDisplayText perigeeString = utils.calendarDateTimeDisplayString(context, perigee.first, showTimeDate, showSeconds);
                previewMoonPerigeeDate.setText(perigeeString.getValue());
                previewMoonPerigeeNote.setText(MoonLayout_1x1_8.noteSpan(context, data2.now(), perigee.first, showWeeks, showHours, timeColor, boldTime));

                if (perigee.second != null) {
                    int risingColor = values.getAsInteger(SuntimesThemeContract.THEME_MOONRISECOLOR);
                    previewMoonPerigee.setText(MoonLayout_1x1_8.distanceSpan(context, perigee.second.distance, units, risingColor, suffixColor, boldTime));
                }
            }

            updateSize(previewMoonPerigeeLabel, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
            updateSize(previewMoonPerigeeDate, values.getAsFloat(SuntimesThemeContract.THEME_TIMESIZE), SuntimesThemeContract.THEME_TIMESIZE_MIN, SuntimesThemeContract.THEME_TIMESIZE_MAX);
            updateSize(previewMoonPerigeeNote, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
            updateSize(previewMoonPerigee, values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE), SuntimesThemeContract.THEME_TEXTSIZE_MIN, SuntimesThemeContract.THEME_TEXTSIZE_MAX);
        }

        View previewMoonPerigeeLayout = previewLayout.findViewById(R.id.moonapsis_perigee_layout);
        View previewMoonApogeeLayout = previewLayout.findViewById(R.id.moonapsis_apogee_layout);
        if (previewMoonApogeeLayout != null && previewMoonPerigeeLayout != null)
        {
            if (apogee != null && apogee.first != null && perigee != null)
            {
                if (apogee.first.before(perigee.first)) {
                    previewMoonApogeeLayout.setVisibility(View.VISIBLE);
                    previewMoonPerigeeLayout.setVisibility( View.GONE);
                } else {
                    previewMoonApogeeLayout.setVisibility(View.GONE);
                    previewMoonPerigeeLayout.setVisibility( View.VISIBLE);
                }
            } else {
                previewMoonApogeeLayout.setVisibility(View.GONE);
                previewMoonPerigeeLayout.setVisibility( View.VISIBLE);
            }
        }
    }

    private void updatePreview_moonPhaseLabel(TextView label, ContentValues values)
    {
        if (label != null)
        {
            label.setTextColor(values.getAsInteger(SuntimesThemeContract.THEME_TEXTCOLOR));
            label.setTextSize(values.getAsFloat(SuntimesThemeContract.THEME_TEXTSIZE));
            label.setVisibility(View.VISIBLE);
        }
    }

    private static int colorForMoonPhase( MoonPhaseDisplay phase, ContentValues values )
    {
        switch (phase)
        {
            case NEW:
                return values.getAsInteger(SuntimesThemeContract.THEME_MOONNEWCOLOR);

            case WAXING_CRESCENT:
            case FIRST_QUARTER:
            case WAXING_GIBBOUS:
                return values.getAsInteger(SuntimesThemeContract.THEME_MOONWAXINGCOLOR);

            case WANING_CRESCENT:
            case THIRD_QUARTER:
            case WANING_GIBBOUS:
                return values.getAsInteger(SuntimesThemeContract.THEME_MOONWANINGCOLOR);

            case FULL:
            default:
                return values.getAsInteger(SuntimesThemeContract.THEME_MOONFULLCOLOR);
        }
    }

    private static void updateSize(@Nullable TextView text, float value, float min, float max)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (text != null && value >= min && value <= max) {
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, value);
            }
        }
    }

}
