/**
    Copyright (C) 2017-2021 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.forrestguice.colors.ColorUtils;
import com.forrestguice.suntimeswidget.graph.LightMapOptions;
import com.forrestguice.suntimeswidget.graph.LightMapTask;
import com.forrestguice.suntimeswidget.graph.LightMapTaskListener;
import com.forrestguice.suntimeswidget.map.WorldMapOptions;
import com.forrestguice.suntimeswidget.map.WorldMapProjection;
import com.forrestguice.support.app.FragmentManagerCompat;
import com.forrestguice.support.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.settings.display.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.graph.SunSymbolBitmap;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_0;
import com.forrestguice.suntimeswidget.map.WorldMapEquirectangular;
import com.forrestguice.suntimeswidget.map.WorldMapTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooserView;
import com.forrestguice.suntimeswidget.settings.PaddingChooser;
import com.forrestguice.suntimeswidget.settings.SizeEditView;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.settings.colors.ColorDialog;
import com.forrestguice.suntimeswidget.themes.defaults.DarkTheme;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.widget.Toolbar;
import com.forrestguice.util.text.TimeDisplayText;

import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACCENTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACTIONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ASTROCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_CIVILCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DAYCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_FALLCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_SHADOWCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONFULLCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONFULLCOLOR_TEXT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONNEWCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONNEWCOLOR_TEXT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONRISECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONSETCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWANINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWANINGCOLOR_TEXT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWAXINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWAXINGCOLOR_TEXT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOON_STROKE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOON_STROKE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAUTICALCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NIGHTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_BOTTOM;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_LEFT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_RIGHT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_TOP;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SPRINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUMMERCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNRISECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNSETCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_WINTERCOLOR;

public class WidgetThemeConfigActivity extends AppCompatActivity
{
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_PREVIEWID = "previewID";
    public static final String PARAM_WALLPAPER = "useWallpaper";

    public static final int SAVE_ITEM_DELAY = 1000;

    public static final int PREVIEWID_SUN_2x1 = 0;
    public static final int PREVIEWID_MOON_2x1 = 1;
    public static final int PREVIEWID_MOON_3x1 = 2;
    public static final int PREVIEWID_SUNPOS_3x1 = 3;
    public static final int PREVIEWID_SUNPOS_3x2 = 4;
    public static final int PREVIEWID_CLOCK_1x1 = 5;
    public static final int PREVIEWID_DATE_1x1 = 6;    // TODO
    public static final int PREVIEWID_ALARM_1x1 = 7;    // TODO
    public static final int PREVIEWID_ALARM_2x2 = 8;    // TODO
    public static final int PREVIEWID_ALARM_3x2 = 9;    // TODO

    public static final int ADD_THEME_REQUEST = 0;
    public static final int EDIT_THEME_REQUEST = 1;
    public static enum UIMode { ADD_THEME, EDIT_THEME }

    private static UIMode mode = UIMode.ADD_THEME;
    public UIMode getMode()
    {
        return mode;
    }

    private UIMode param_mode = null;
    private String param_themeName = null;
    private int param_previewID = 0;
    private boolean param_wallpaper = true;

    private EditText editDisplay;
    private SizeChooser chooseTitleSize, chooseTextSize, chooseTimeSize, chooseSuffixSize;
    private SizeChooser chooseIconStroke, chooseNoonIconStroke;
    private SizeChooser chooseMoonStroke;
    private ArrayList<SizeChooser> sizeChoosers;
    private ThemeNameChooser chooseName;
    private PaddingChooser choosePadding;

    private final ArrayList<Integer> recentColors = new ArrayList<>();
    private ColorChooser chooseColorRise, chooseColorRiseIconFill, chooseColorRiseIconStroke;
    private ColorChooser chooseColorNoon, chooseColorNoonIconFill, chooseColorNoonIconStroke;
    private ColorChooser chooseColorSet, chooseColorSetIconFill, chooseColorSetIconStroke;
    private ColorChooser chooseColorTitle, chooseColorText, chooseColorTime, chooseColorSuffix, chooseColorAction, chooseColorAccent;
    private ColorChooser chooseColorDay, chooseColorCivil, chooseColorNautical, chooseColorAstro, chooseColorNight, chooseColorPointFill, chooseColorPointStroke;
    private ColorChooser chooseColorSpring, chooseColorSummer, chooseColorFall, chooseColorWinter;
    private ColorChooser chooseColorMoonrise, chooseColorMoonset;
    private ColorChooser chooseColorMoonWaning, chooseColorMoonNew, chooseColorMoonWaxing, chooseColorMoonFull;
    private ColorChooser chooseColorMoonNewText, chooseColorMoonFullText;
    private ColorChooser chooseColorMapBackground, chooseColorMapForeground, chooseColorMapShadow, chooseColorMapHighlight;
    private ArrayList<ColorChooser> colorChoosers;
    private CheckBox checkUseFill, checkUseStroke, checkUseNoon;

    private CheckBox checkTitleBold, checkTimeBold;

    private Spinner spinBackground;
    private ArrayAdapter<SuntimesTheme.ThemeBackground> spinBackground_adapter;
    private ColorChooser chooseColorBackground;

    private ViewFlipper preview;

    private final SuntimesUtils utils = new SuntimesUtils();

    public WidgetThemeConfigActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        initLocale();
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_themeconfig);

        Intent intent = getIntent();
        param_mode = (UIMode)intent.getSerializableExtra(PARAM_MODE);
        param_themeName = intent.getStringExtra(THEME_NAME);
        param_previewID = intent.getIntExtra(PARAM_PREVIEWID, param_previewID);
        param_wallpaper = intent.getBooleanExtra(PARAM_WALLPAPER, param_wallpaper);

        mode = (param_mode == null) ? UIMode.ADD_THEME : param_mode;

        initData(this);
        initViews(this);
        loadTheme(param_themeName);

        flipToPreview(param_previewID);
        updatePreview();
    }

    private SuntimesRiseSetDataset data0;
    private SuntimesRiseSetData data1;
    private SuntimesMoonData data2;
    private void initData(Context context)
    {
        data0 = new SuntimesRiseSetDataset(context, 0);  // use app configuration
        data0.calculateData(context);

        data1 = data0.dataActual;
        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data1);
        noonData.setTimeMode(TimeMode.NOON);
        noonData.calculate(context);
        data1.linkData(noonData);

        data2 = new SuntimesMoonData(context, 0, "moon");
        data2.calculate(context);
    }

    private void initLocale()
    {
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
        SuntimesTheme.ThemeBackground.initDisplayStrings(this);
        SuntimesUtils.initDisplayStrings(this);
    }

    protected void initViews( Context context )
    {
        colorChoosers = new ArrayList<>();
        sizeChoosers = new ArrayList<>();

        initPreview(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinBackground_adapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, SuntimesTheme.ThemeBackground.values());
        spinBackground_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBackground = (Spinner)findViewById(R.id.editSpin_background);
        spinBackground.setAdapter(spinBackground_adapter);
        spinBackground.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                updatePreview();
                SuntimesTheme.ThemeBackground background = spinBackground_adapter.getItem(i);
                boolean enabled = (background != null && background.supportsCustomColors());
                //chooseColorBackground.setEnabled(enabled);
                chooseColorBackground.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                updatePreview();
            }
        });

        chooseColorBackground = createColorChooser(context, R.id.chooser_backgroundColor, THEME_BACKGROUND_COLOR);
        chooseColorBackground.setShowAlpha(true);

        EditText editName = (EditText)findViewById(R.id.edit_themeName);
        chooseName = new ThemeNameChooser(editName);

        editDisplay = (EditText)findViewById(R.id.edit_themeDisplay);
        editDisplay.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    if (validateThemeDisplayText(WidgetThemeConfigActivity.this, editDisplay, false))
                    {
                        updatePreview();
                    }
                }
            }
        });

        chooseTitleSize = createSizeChooser(this, R.id.chooser_titleSize, THEME_TITLESIZE_MIN, THEME_TITLESIZE_MAX, THEME_TITLESIZE);
        chooseTextSize = createSizeChooser(this, R.id.chooser_textSize, THEME_TEXTSIZE_MIN, THEME_TEXTSIZE_MAX, THEME_TEXTSIZE);
        chooseTimeSize = createSizeChooser(this, R.id.chooser_timeSize, THEME_TIMESIZE_MIN, THEME_TIMESIZE_MAX, THEME_TIMESIZE);
        chooseSuffixSize = createSizeChooser(this, R.id.chooser_suffixSize, THEME_TIMESUFFIXSIZE_MIN, THEME_TIMESUFFIXSIZE_MAX, THEME_TIMESUFFIXSIZE);

        chooseIconStroke = createSizeChooser(this, R.id.chooser_iconStroke, THEME_SETICON_STROKE_WIDTH_MIN, THEME_SETICON_STROKE_WIDTH_MAX, THEME_SETICON_STROKE_WIDTH);
        chooseNoonIconStroke = createSizeChooser(this, R.id.chooser_noonIconStroke, THEME_NOONICON_STROKE_WIDTH_MIN, THEME_NOONICON_STROKE_WIDTH_MAX, THEME_NOONICON_STROKE_WIDTH);
        chooseMoonStroke = createSizeChooser(this, THEME_MOON_STROKE_MIN, THEME_MOON_STROKE_MAX, THEME_MOONFULL_STROKE_WIDTH);

        EditText editPadding = (EditText)findViewById(R.id.edit_padding);
        choosePadding = new PaddingChooser(editPadding)
        {
            @Override
            protected void onPaddingChanged( int[] newPadding )
            {
                updatePreview();
            }
        };

        // sunrise colors
        chooseColorRise = createColorChooser(this, R.id.chooser_sunriseColor, THEME_SUNRISECOLOR);
        chooseColorRiseIconFill = createColorChooser(this, R.id.chooser_sunriseFillColor, THEME_RISEICON_FILL_COLOR);
        chooseColorRiseIconStroke = createColorChooser(this, R.id.chooser_sunriseStrokeColor, THEME_RISEICON_STROKE_COLOR);

        // noon colors
        chooseColorNoon = createColorChooser(this, R.id.chooser_noonColor, THEME_NOONCOLOR);
        chooseColorNoonIconFill = createColorChooser(this, R.id.chooser_noonFillColor, THEME_NOONICON_FILL_COLOR);
        chooseColorNoonIconStroke = createColorChooser(this, R.id.chooser_noonStrokeColor, THEME_NOONICON_STROKE_COLOR);

        // sunset colors
        chooseColorSet = createColorChooser(this, R.id.chooser_sunsetColor, THEME_SUNSETCOLOR);
        chooseColorSetIconFill = createColorChooser(this, R.id.chooser_sunsetFillColor, THEME_SETICON_FILL_COLOR);
        chooseColorSetIconStroke = createColorChooser(this, R.id.chooser_sunsetStrokeColor, THEME_SETICON_STROKE_COLOR);

        // graph colors
        chooseColorDay = createColorChooser(context, R.id.chooser_dayColor, THEME_DAYCOLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorCivil = createColorChooser(context, R.id.chooser_civilColor, THEME_CIVILCOLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorNautical = createColorChooser(context, R.id.chooser_nauticalColor, THEME_NAUTICALCOLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorAstro = createColorChooser(context, R.id.chooser_astroColor, THEME_ASTROCOLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorNight = createColorChooser(context, R.id.chooser_nightColor, THEME_NIGHTCOLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorPointFill = createColorChooser(context, R.id.chooser_pointFill, THEME_GRAPH_POINT_FILL_COLOR, PREVIEWID_SUNPOS_3x1);
        chooseColorPointStroke = createColorChooser(context, R.id.chooser_pointStroke, THEME_GRAPH_POINT_STROKE_COLOR, PREVIEWID_SUNPOS_3x1);

        // map colors
        chooseColorMapBackground = createColorChooser(context, R.id.chooser_mapBackgroundColor, THEME_MAP_BACKGROUNDCOLOR, PREVIEWID_SUNPOS_3x2);
        chooseColorMapBackground.setShowAlpha(true);

        chooseColorMapForeground = createColorChooser(context, R.id.chooser_mapForegroundColor, THEME_MAP_FOREGROUNDCOLOR, PREVIEWID_SUNPOS_3x2);

        chooseColorMapShadow = createColorChooser(context, R.id.chooser_mapSunShadowColor, THEME_MAP_SHADOWCOLOR, PREVIEWID_SUNPOS_3x2);
        chooseColorMapShadow.setShowAlpha(true);

        chooseColorMapHighlight = createColorChooser(context, R.id.chooser_mapMoonHighlightColor, THEME_MAP_HIGHLIGHTCOLOR, PREVIEWID_SUNPOS_3x2);
        chooseColorMapHighlight.setShowAlpha(true);

        // season colors
        chooseColorSpring = createColorChooser(this, R.id.chooser_springColor, THEME_SPRINGCOLOR );
        chooseColorSummer = createColorChooser(this, R.id.chooser_summerColor, THEME_SUMMERCOLOR);
        chooseColorFall = createColorChooser(this, R.id.chooser_fallColor, THEME_FALLCOLOR);
        chooseColorWinter = createColorChooser(this, R.id.chooser_winterColor, THEME_WINTERCOLOR);

        // moon colors
        chooseColorMoonrise = createColorChooser(this, R.id.chooser_moonriseColor, THEME_MOONRISECOLOR);
        chooseColorMoonset = createColorChooser(this, R.id.chooser_moonsetColor, THEME_MOONSETCOLOR);
        chooseColorMoonWaning = createColorChooser(this, R.id.chooser_moonWaningColor, THEME_MOONWANINGCOLOR);
        chooseColorMoonNew = createColorChooser(this, R.id.chooser_moonNewColor, THEME_MOONNEWCOLOR);
        chooseColorMoonWaxing = createColorChooser(this, R.id.chooser_moonWaxingColor, THEME_MOONWAXINGCOLOR);
        chooseColorMoonFull = createColorChooser(this, R.id.chooser_moonFullColor, THEME_MOONFULLCOLOR);

        //chooseColorMoonWaningText = createColorChooser(this, R.id.chooser_moonWaningColor_text, THEME_MOONWANINGCOLOR_TEXT);
        chooseColorMoonNewText = createColorChooser(this, R.id.chooser_moonNewColor_text, THEME_MOONNEWCOLOR_TEXT);
        //chooseColorMoonWaxingText = createColorChooser(this, R.id.chooser_moonWaxingColor_text, THEME_MOONWAXINGCOLOR_TEXT);
        chooseColorMoonFullText = createColorChooser(this, R.id.chooser_moonFullColor_text, THEME_MOONFULLCOLOR_TEXT);

        // other colors
        chooseColorTitle = createColorChooser(this, R.id.chooser_titleColor, THEME_TITLECOLOR);
        chooseColorText = createColorChooser(this, R.id.chooser_textColor, THEME_TEXTCOLOR);
        chooseColorTime = createColorChooser(this, R.id.chooser_timeColor, THEME_TIMECOLOR);
        chooseColorSuffix = createColorChooser(this, R.id.chooser_suffixColor, THEME_TIMESUFFIXCOLOR);
        chooseColorAction = createColorChooser(this, R.id.chooser_actionColor, THEME_ACTIONCOLOR);
        chooseColorAccent = createColorChooser(this, R.id.chooser_accentColor, THEME_ACCENTCOLOR);

        checkUseNoon = (CheckBox)findViewById(R.id.enable_noonColor);
        checkUseNoon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                toggleNoonIconColor(isChecked);
                if (!isChecked)
                {
                    chooseColorNoon.setColor(chooseColorSet.getColor());
                    chooseColorNoonIconFill.setColor(chooseColorRise.getColor());
                    chooseColorNoonIconStroke.setColor(chooseColorSet.getColor());
                    updatePreview();

                    chooseColorSet.link(chooseColorNoon);
                    chooseColorSet.link(chooseColorNoonIconStroke);
                    chooseColorRise.link(chooseColorNoonIconFill);
                } else {
                    chooseColorSet.unlink(chooseColorNoon);
                    chooseColorSet.unlink(chooseColorNoonIconStroke);
                    chooseColorRise.unlink(chooseColorNoonIconFill);
                }
            }
        });

        checkUseFill = (CheckBox)findViewById(R.id.enable_risesetFillColor);
        checkUseFill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                toggleRiseSetIconFill(isChecked);
                if (!isChecked)
                {
                    chooseColorRiseIconFill.setColor(chooseColorRise.getColor());
                    chooseColorSetIconFill.setColor(chooseColorSet.getColor());
                    updatePreview();

                    chooseColorRise.link(chooseColorRiseIconFill);
                    chooseColorSet.link(chooseColorSetIconFill);
                } else {
                    chooseColorRise.unlink(chooseColorRiseIconFill);
                    chooseColorSet.unlink(chooseColorSetIconFill);
                }
            }
        });

        checkUseStroke = (CheckBox)findViewById(R.id.enable_risesetStrokeColor);
        checkUseStroke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                toggleRiseSetIconStroke(isChecked);
                if (!isChecked)
                {
                    chooseColorRiseIconStroke.setColor(chooseColorSet.getColor());
                    chooseColorSetIconStroke.setColor(chooseColorRise.getColor());
                    updatePreview();

                    chooseColorRise.link(chooseColorSetIconStroke);
                    chooseColorSet.link(chooseColorRiseIconStroke);
                } else {
                    chooseColorRise.unlink(chooseColorSetIconStroke);
                    chooseColorSet.unlink(chooseColorRiseIconStroke);
                }
            }
        });

        checkTitleBold = (CheckBox)findViewById(R.id.check_titleBold);
        checkTitleBold.setOnCheckedChangeListener(onCheckChanged);

        checkTimeBold = (CheckBox)findViewById(R.id.check_timeBold);
        checkTimeBold.setOnCheckedChangeListener(onCheckChanged);

        initColorFields();
        initSizeFields();

        TextView labelName = (TextView)findViewById(R.id.editLabel_themeName);
        switch (mode)
        {
            case EDIT_THEME:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.configLabel_widgetThemeEdit));
                }
                labelName.setEnabled(false);
                labelName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                editName.setEnabled(false);
                editName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                break;

            case ADD_THEME:
            default:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.configLabel_widgetThemeAdd));
                }
                labelName.setEnabled(true);
                labelName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                editName.setEnabled(true);
                editName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                break;

        }
    }

    private final CompoundButton.OnCheckedChangeListener onCheckChanged = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            updatePreview();
        }
    };

    private void toggleNoonIconColor( boolean enabled )
    {
        toggleNoonIconColor(enabled, false);
    }
    private void toggleNoonIconColor( boolean enabled, boolean setChecked )
    {
        chooseColorNoon.setEnabled(enabled);
        chooseColorNoonIconFill.setEnabled(enabled);
        chooseColorNoonIconStroke.setEnabled(enabled);

        if (setChecked)
        {
            checkUseNoon.setChecked(enabled);
            if (!enabled)
            {
                chooseColorSet.link(chooseColorNoon);
                chooseColorSet.link(chooseColorNoonIconStroke);
                chooseColorRise.link(chooseColorNoonIconFill);
            }
        }
    }

    private void toggleRiseSetIconFill( boolean enabled )
    {
        toggleRiseSetIconFill(enabled, false);
    }
    private void toggleRiseSetIconFill( boolean enabled, boolean setChecked )
    {
        chooseColorRiseIconFill.setEnabled(enabled);
        chooseColorSetIconFill.setEnabled(enabled);
        if (setChecked)
        {
            checkUseFill.setChecked(enabled);
            if (!enabled)
            {
                chooseColorRise.link(chooseColorRiseIconFill);
                chooseColorSet.link(chooseColorSetIconFill);
            }
        }
    }

    private void toggleRiseSetIconStroke( boolean enabled )
    {
        toggleRiseSetIconStroke(enabled, false);
    }
    private void toggleRiseSetIconStroke( boolean enabled, boolean setChecked )
    {
        chooseColorRiseIconStroke.setEnabled(enabled);
        chooseColorSetIconStroke.setEnabled(enabled);
        if (setChecked)
        {
            checkUseStroke.setChecked(enabled);
            if (!enabled)
            {
                chooseColorRise.link(chooseColorSetIconStroke);
                chooseColorSet.link(chooseColorRiseIconStroke);
            }
        }
    }

    /**
     * @return true fill is set to something other than rise/set text color, false fill is same as text color
     */
    private boolean usingRiseSetIconFill()
    {
        return (chooseColorRise.getColor() != chooseColorRiseIconFill.getColor() ||
                chooseColorSet.getColor() != chooseColorSetIconFill.getColor());
    }

    private boolean usingRiseSetIconStroke()
    {
        return (chooseColorSet.getColor() != chooseColorRiseIconStroke.getColor() ||
                chooseColorRise.getColor() != chooseColorSetIconStroke.getColor());
    }

    private boolean usingNoonIconColor()
    {
        boolean textCondition = (chooseColorNoon.getColor() != chooseColorSet.getColor());
        boolean fillCondition = (chooseColorNoonIconFill.getColor() != chooseColorRise.getColor());
        boolean strokeCondition = (chooseColorNoonIconStroke.getColor() != chooseColorSet.getColor());
        return (textCondition || fillCondition || strokeCondition);
    }

    private ColorChooser createColorChooser(Context context, String id)
    {
        return createColorChooser(context, null, null, null, id, null);
    }
    private ColorChooser createColorChooser(Context context, int labelID, int editID, int buttonID, String id)
    {
        TextView label = (TextView)findViewById(labelID);
        EditText edit = (EditText)findViewById(editID);
        ImageButton button = (ImageButton)findViewById(buttonID);
        return createColorChooser(context, label, edit, button, id, null);
    }
    private ColorChooser createColorChooser(Context context, TextView label, EditText edit, ImageButton button, String id, @Nullable final Integer previewID)
    {
        ColorChooser chooser = new ColorChooser(context, label, edit, button, id);
        chooser.setColorChangeListener(new ColorChangeListener() {
            @Override
            public void onColorChanged(int color) {
                addRecentColor(color);
                if (previewID != null) {
                    flipToPreview(previewID);
                    updatePreview();
                }
            }
        });
        colorChoosers.add(chooser);
        return chooser;
    }
    private ColorChooser createColorChooser(Context context, int colorChooserID, String id, @Nullable Integer previewID)
    {
        ColorChooserView view = (ColorChooserView)findViewById(colorChooserID);
        return createColorChooser(this, view.getLabel(), view.getEdit(), view.getButton(), id, previewID);
    }
    private ColorChooser createColorChooser(Context context, int colorChooserID, String id)
    {
        ColorChooserView view = (ColorChooserView)findViewById(colorChooserID);
        return createColorChooser(this, view.getLabel(), view.getEdit(), view.getButton(), id, null);
    }

    private SizeChooser createSizeChooser(Context context, float min, float max, String id)
    {
        return createSizeChooser(context, null, min, max, id);
    }
    private SizeChooser createSizeChooser(Context context, int sizeEditViewID, float min, float max, String id)
    {
        SizeEditView sizeEdit = (SizeEditView)findViewById(sizeEditViewID);
        return createSizeChooser(context, sizeEdit.getEdit(), min, max, id);
    }
    private SizeChooser createSizeChooser(Context context, EditText edit, float min, float max, String id)
    {
        SizeChooser chooser = new SizeChooser(context, edit, min, max, id);
        sizeChoosers.add(chooser);
        return chooser;
    }

    private void initColorFields()
    {
        for (ColorChooser chooser : colorChoosers)
        {
            chooser.setFragmentManager(FragmentManagerCompat.from(this));
            chooser.setCollapsed(true);
        }
    }

    private void addRecentColor(int color)
    {
        if (!recentColors.contains(color)) {
            recentColors.add(0, color);
        }
    }

    private void updateRecentColors()
    {
        recentColors.clear();
        for (ColorChooser chooser : colorChoosers) {
            addRecentColor(chooser.getColor());
        }
        Collections.sort(recentColors, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                double[] lab0 = new double[3];
                double[] lab1 = new double[3];
                double[] lab2 = new double[3];
                ColorUtils.colorToLAB(Color.BLACK, lab0);
                ColorUtils.colorToLAB(o1, lab1);
                ColorUtils.colorToLAB(o2, lab2);

                Double e1 = ColorUtils.distanceEuclidean(lab1, lab0);
                Double e2 = ColorUtils.distanceEuclidean(lab2, lab0);
                return e2.compareTo(e1);
            }
        });

        for (ColorChooser chooser : colorChoosers) {
            chooser.setRecentColors(recentColors);
        }
    }

    private void initSizeFields()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            for (SizeChooser chooser : sizeChoosers)
            {
                chooser.setEnabled(false);  // changing text size requires api 16+
            }
            choosePadding.setEnabled(false);  // changing padding requires api 16+

            chooseIconStroke.setEnabled(true);
            chooseNoonIconStroke.setEnabled(true);
        }
    }

    protected void initPreview(Context context)
    {
        preview = (ViewFlipper)findViewById(R.id.preview_area);
        preview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                preview.showNext();
                updatePreview();
            }
        });
    }

    /**
     * Update the preview area.
     */
    protected void updatePreview()
    {
        View previewLayout = preview.getCurrentView();
        if (previewLayout != null)
        {
            updatePreview(previewLayout);
        }
    }

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    protected void updatePreview( View previewLayout )
    {
        View previewBackground = previewLayout.findViewById(R.id.widgetframe_inner);
        if (previewBackground != null)
        {
            SuntimesTheme.ThemeBackground background = (SuntimesTheme.ThemeBackground)spinBackground.getSelectedItem();
            if (background != null)
            {
                if (background.supportsCustomColors())
                    previewBackground.setBackgroundColor(chooseColorBackground.getColor());
                else previewBackground.setBackgroundResource(background.getResID());

                int[] padding = choosePadding.getPaddingPixels(this);
                previewBackground.setPadding(padding[0], padding[1], padding[2], padding[3]);
            }
        }

        TextView previewTitle = (TextView)previewLayout.findViewById(R.id.text_title);
        if (previewTitle != null)
        {
            String displayText = editDisplay.getText().toString().trim();
            String titleText = (displayText.isEmpty() ? chooseName.getThemeName() : displayText);
            previewTitle.setVisibility(View.VISIBLE);
            previewTitle.setTextColor(chooseColorTitle.getColor());

            boolean boldText = checkTitleBold.isChecked();
            if (boldText)
                previewTitle.setText(SuntimesUtils.createBoldSpan(null, titleText, titleText));
            else previewTitle.setText(titleText);

            updateSizeFromChooser(previewTitle, chooseTitleSize);
        }

        updatePreview_sun(previewLayout);
        updatePreview_moon(previewLayout);
        updatePreview_clock(previewLayout);

        int displayed = preview.getDisplayedChild();
        if (displayed == PREVIEWID_SUNPOS_3x1)
            updatePreview_position0(previewLayout);
        else if (displayed == PREVIEWID_SUNPOS_3x2)
            updatePreview_position1(previewLayout);

        //updatePreview_solstice(previewLayout);  // TODO
    }

    protected void updatePreview_position0(View previewLayout)
    {
        final ImageView view = (ImageView)previewLayout.findViewById(R.id.info_time_lightmap);
        if (view != null)
        {
            LightMapOptions colors = new LightMapOptions();
            colors.initDefaultDark(this);

            colors.values.setColor(LightMapColorValues.COLOR_DAY, chooseColorDay.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_CIVIL, chooseColorCivil.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_NAUTICAL, chooseColorNautical.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_ASTRONOMICAL, chooseColorAstro.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_NIGHT, chooseColorNight.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_POINT_FILL, chooseColorPointFill.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_POINT_STROKE, chooseColorPointStroke.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_SUN_FILL, chooseColorPointFill.getColor());
            colors.values.setColor(LightMapColorValues.COLOR_SUN_STROKE, chooseColorPointStroke.getColor());

            colors.option_drawNow = SunSymbolBitmap.DRAW_SUN1;
            colors.option_drawNoon = true;
            colors.option_drawNow_pointSizePx = SuntimesUtils.dpToPixels(this, 8);

            int dpWidth = 256;
            int dpHeight = 64;
            LightMapTask drawTask = new LightMapTask(view.getContext());
            drawTask.setListener(new LightMapTaskListener()
            {
                @Override
                public void onFinished(Bitmap result)
                {
                    super.onFinished(result);
                    view.setImageBitmap(result);
                }
            });
            drawTask.execute(data0, SuntimesUtils.dpToPixels(this, dpWidth), SuntimesUtils.dpToPixels(this, dpHeight), colors);
        }
    }

    protected void updatePreview_position1(View previewLayout)
    {
        final ImageView view = (ImageView)previewLayout.findViewById(R.id.info_time_worldmap);
        if (view != null)
        {
            WorldMapOptions options = new WorldMapOptions();
            options.map = ContextCompat.getDrawable(this, R.drawable.worldmap);
            options.colors.setColor(WorldMapColorValues.COLOR_BACKGROUND, chooseColorMapBackground.getColor());
            options.colors.setColor(WorldMapColorValues.COLOR_FOREGROUND, chooseColorMapForeground.getColor());
            options.colors.setColor(WorldMapColorValues.COLOR_SUN_SHADOW, chooseColorMapShadow.getColor());
            options.colors.setColor(WorldMapColorValues.COLOR_MOON_LIGHT, chooseColorMapHighlight.getColor());

            options.colors.setColor(WorldMapColorValues.COLOR_SUN_FILL, chooseColorPointFill.getColor());
            options.colors.setColor(WorldMapColorValues.COLOR_SUN_STROKE, chooseColorPointStroke.getColor());
            options.sunScale = 24;      // extra large so preview of colors is visible

            options.colors.setColor(WorldMapColorValues.COLOR_MOON_FILL, chooseColorMoonFull.getColor());
            options.colors.setColor(WorldMapColorValues.COLOR_MOON_STROKE, chooseColorMoonWaning.getColor());
            options.moonScale = 32;

            int dpWidth = 128;
            int dpHeight = 64;
            WorldMapProjection projection = new WorldMapEquirectangular();
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
            drawTask.execute(data0,  SuntimesUtils.dpToPixels(this, dpWidth), SuntimesUtils.dpToPixels(this, dpHeight), options, projection);
        }
    }

    /**
     * @param previewLayout the layout to update
     */
    protected void updatePreview_clock(View previewLayout)
    {
        TextView previewTime = (TextView)previewLayout.findViewById(R.id.text_time);
        TextView previewTimeSuffix = (TextView)previewLayout.findViewById(R.id.text_time_suffix);
        if (previewTime != null && previewTimeSuffix != null)
        {
            float[] adjustedSizeSp = ClockLayout_1x1_0.adjustTextSize(this, new int[] {80, 80}, choosePadding.getPadding(),
                    "sans-serif", checkTimeBold.isChecked(),"00:00", (float)chooseTimeSize.getValue(), THEME_TIMESIZE_MAX, "MM", (float)chooseSuffixSize.getValue());
            previewTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[0]);
            previewTimeSuffix.setTextSize(TypedValue.COMPLEX_UNIT_SP, adjustedSizeSp[1]);

            Calendar now = Calendar.getInstance();
            TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(this, 0);
            TimeDisplayText nowText = utils.calendarTimeShortDisplayString(this, now, false, timeFormat);
            String nowString = nowText.getValue();
            CharSequence nowChars = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, nowString, nowString) : nowString);

            previewTime.setTextColor(chooseColorTime.getColor());
            previewTime.setText(nowChars);

            previewTimeSuffix.setTextColor(chooseColorSuffix.getColor());
            previewTimeSuffix.setText(nowText.getSuffix());
        }
    }

    /**
     * @param previewLayout the layout to update
     */
    protected void updatePreview_sun(View previewLayout)
    {
        // Noon
        TextView previewNoon = (TextView)previewLayout.findViewById(R.id.text_time_noon);
        TextView previewNoonSuffix = (TextView)previewLayout.findViewById(R.id.text_time_noon_suffix);

        SuntimesRiseSetData noonData = data1.getLinked();
        TimeDisplayText noonText = ((noonData != null)
                ? utils.calendarTimeShortDisplayString(this, noonData.sunriseCalendarToday())
                : new TimeDisplayText("12:00"));
        if (previewNoon != null)
        {
            String noonString = noonText.getValue();
            CharSequence noon = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, noonString, noonString) : noonString);
            previewNoon.setText(noon);
            previewNoon.setTextColor(chooseColorNoon.getColor());
            updateSizeFromChooser(previewNoon, chooseTimeSize);
        }
        if (previewNoonSuffix != null)
        {
            previewNoonSuffix.setText(noonText.getSuffix());
            previewNoonSuffix.setTextColor(chooseColorSuffix.getColor());
            updateSizeFromChooser(previewNoonSuffix, chooseSuffixSize);
        }

        // Sunrise
        TextView previewRise = (TextView)previewLayout.findViewById(R.id.text_time_rise);
        TextView previewRiseSuffix = (TextView)previewLayout.findViewById(R.id.text_time_rise_suffix);

        TimeDisplayText riseText = utils.calendarTimeShortDisplayString(this, data1.sunriseCalendarToday());
        if (previewRise != null)
        {
            String riseString = riseText.getValue();
            CharSequence rise = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, riseString, riseString) : riseString);
            previewRise.setText(rise);
            previewRise.setTextColor(chooseColorRise.getColor());
            updateSizeFromChooser(previewRise, chooseTimeSize);
        }
        if (previewRiseSuffix != null)
        {
            previewRiseSuffix.setText(riseText.getSuffix());
            previewRiseSuffix.setTextColor(chooseColorSuffix.getColor());
            updateSizeFromChooser(previewRiseSuffix, chooseSuffixSize);
        }

        // Sunset
        TextView previewSet = (TextView)previewLayout.findViewById(R.id.text_time_set);
        TextView previewSetSuffix = (TextView)previewLayout.findViewById(R.id.text_time_set_suffix);

        TimeDisplayText setText = utils.calendarTimeShortDisplayString(this, data1.sunsetCalendarToday());
        if (previewSet != null)
        {
            String setString = setText.getValue();
            CharSequence set = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, setString, setString) : setString);
            previewSet.setText(set);
            previewSet.setTextColor(chooseColorSet.getColor());
            updateSizeFromChooser(previewSet, chooseTimeSize);
        }
        if (previewSetSuffix != null)
        {
            previewSetSuffix.setText(setText.getSuffix());
            previewSetSuffix.setTextColor(chooseColorSuffix.getColor());
            updateSizeFromChooser(previewSetSuffix, chooseSuffixSize);
        }

        // Time Delta
        TextView previewTimeDelta = (TextView)previewLayout.findViewById(R.id.text_delta_day_value);
        TextView previewTimeDeltaPrefix = (TextView)previewLayout.findViewById(R.id.text_delta_day_prefix);
        TextView previewTimeDeltaSuffix = (TextView)previewLayout.findViewById(R.id.text_delta_day_suffix);

        if (previewTimeDelta != null)
        {
            previewTimeDelta.setText(utils.timeDeltaLongDisplayString(data1.dayLengthToday(), data1.dayLengthOther()).getValue());
            previewTimeDelta.setTextColor(chooseColorTime.getColor());
            updateSizeFromChooser(previewTimeDelta, chooseTextSize);
        }
        if (previewTimeDeltaPrefix != null)
        {
            previewTimeDeltaPrefix.setText(getString(R.string.delta_day_tomorrow));
            previewTimeDeltaPrefix.setTextColor(chooseColorText.getColor());
            updateSizeFromChooser(previewTimeDeltaPrefix, chooseTextSize);
        }
        if (previewTimeDeltaSuffix != null)
        {
            previewTimeDeltaSuffix.setText(getString(R.string.delta_day_shorter));
            previewTimeDeltaSuffix.setTextColor(chooseColorText.getColor());
            updateSizeFromChooser(previewTimeDeltaSuffix, chooseTextSize);
        }

        // Icons
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int strokePixels = (int)((metrics.density * chooseIconStroke.getValue()) + 0.5f);
        int noonStrokePixels = (int)((metrics.density * chooseNoonIconStroke.getValue()) + 0.5f);

        ImageView previewRiseIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_sunrise);
        if (previewRiseIcon != null)
        {
            previewRiseIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, R.drawable.ic_sunrise0, chooseColorRiseIconFill.getColor(), chooseColorRiseIconStroke.getColor(), strokePixels));
        }

        ImageView previewSetIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_sunset);
        if (previewSetIcon != null)
        {
            previewSetIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, R.drawable.ic_sunset0, chooseColorSetIconFill.getColor(), chooseColorSetIconStroke.getColor(), strokePixels));
        }

        ImageView previewNoonIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_noon);
        if (previewNoonIcon != null)
        {
            previewNoonIcon.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(this, R.drawable.ic_noon_large0, chooseColorNoonIconFill.getColor(), chooseColorNoonIconStroke.getColor(), noonStrokePixels));
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
    protected void updatePreview_moon(View previewLayout)
    {
        // Moonrise
        TextView previewMoonrise = (TextView)previewLayout.findViewById(R.id.text_time_moonrise);
        TextView previewMoonriseSuffix = (TextView)previewLayout.findViewById(R.id.text_time_moonrise_suffix);

        TimeDisplayText moonriseText = utils.calendarTimeShortDisplayString(this, data2.moonriseCalendarToday());
        if (previewMoonrise != null)
        {
            String riseString = moonriseText.getValue();
            CharSequence rise = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, riseString, riseString) : riseString);
            previewMoonrise.setText(rise);
            previewMoonrise.setTextColor(chooseColorMoonrise.getColor());
            updateSizeFromChooser(previewMoonrise, chooseTimeSize);
        }
        if (previewMoonriseSuffix != null)
        {
            previewMoonriseSuffix.setText(moonriseText.getSuffix());
            previewMoonriseSuffix.setTextColor(chooseColorSuffix.getColor());
            updateSizeFromChooser(previewMoonriseSuffix, chooseSuffixSize);
        }

        // Moonset
        TextView previewMoonset = (TextView)previewLayout.findViewById(R.id.text_time_moonset);
        TextView previewMoonsetSuffix = (TextView)previewLayout.findViewById(R.id.text_time_moonset_suffix);

        TimeDisplayText moonsetText = utils.calendarTimeShortDisplayString(this, data2.moonsetCalendarToday());
        if (previewMoonset != null)
        {
            String setString = moonsetText.getValue();
            CharSequence set = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(null, setString, setString) : setString);
            previewMoonset.setText(set);
            previewMoonset.setTextColor(chooseColorMoonset.getColor());
            updateSizeFromChooser(previewMoonset, chooseTimeSize);
        }
        if (previewMoonsetSuffix != null)
        {
            previewMoonsetSuffix.setText(moonsetText.getSuffix());
            previewMoonsetSuffix.setTextColor(chooseColorSuffix.getColor());
            updateSizeFromChooser(previewMoonsetSuffix, chooseSuffixSize);
        }

        // Moon Phase / Illumination
        TextView previewMoonPhase = (TextView)previewLayout.findViewById(R.id.text_info_moonphase);
        if (previewMoonPhase != null)
        {
            int phaseColor = colorForMoonPhase(data2.getMoonPhaseToday());
            previewMoonPhase.setText(data2.getMoonPhaseToday().getLongDisplayString());
            previewMoonPhase.setTextColor(phaseColor);
            updateSizeFromChooser(previewMoonPhase, chooseTextSize);
        }

        TextView previewMoonIllum = (TextView)previewLayout.findViewById(R.id.text_info_moonillum);
        if (previewMoonIllum != null)
        {
            NumberFormat percentage = NumberFormat.getPercentInstance();
            previewMoonIllum.setText(percentage.format(data2.getMoonIlluminationToday()));
            previewMoonIllum.setTextColor(chooseColorTime.getColor());
            updateSizeFromChooser(previewMoonPhase, chooseTextSize);
        }

        // Moon Labels
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_new_label));
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_firstquarter_label));
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_full_label));
        updatePreview_moonPhaseLabel((TextView)previewLayout.findViewById(R.id.moonphase_thirdquarter_label));

        // Moon Icons

        ImageView previewMoonriseIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_moonrise);
        if (previewMoonriseIcon != null)
        {
            previewMoonriseIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, R.drawable.ic_moon_rise, chooseColorMoonrise.getColor(), chooseColorMoonrise.getColor(), 0));
        }

        ImageView previewMoonsetIcon = (ImageView)previewLayout.findViewById(R.id.icon_time_moonset);
        if (previewMoonsetIcon != null)
        {
            previewMoonsetIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, R.drawable.ic_moon_set, chooseColorMoonset.getColor(), chooseColorMoonset.getColor(), 0));
        }

        int colorWaxing = chooseColorMoonWaxing.getColor();
        int colorWaning = chooseColorMoonWaning.getColor();
        int colorFull = chooseColorMoonFull.getColor();
        int colorNew = chooseColorMoonNew.getColor();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int moonStrokePx =  (int)((metrics.density * chooseMoonStroke.getValue()) + 0.5f);

        // full and new
        ImageView previewMoonFullIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_full);
        if (previewMoonFullIcon != null)
        {
            previewMoonFullIcon.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(this, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, moonStrokePx));
        }

        ImageView previewMoonNewIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_new);
        if (previewMoonNewIcon != null)
        {
            previewMoonNewIcon.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(this, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, moonStrokePx));
        }

        // waxing
        ImageView previewMoonWaxingCrescentIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waxing_crescent);
        if (previewMoonWaxingCrescentIcon != null)
        {
            previewMoonWaxingCrescentIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.WAXING_CRESCENT.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaxingQuarterIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waxing_quarter);
        if (previewMoonWaxingQuarterIcon != null)
        {
            previewMoonWaxingQuarterIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaxingGibbousIcon = (ImageView) previewLayout.findViewById(R.id.icon_info_moonphase_waxing_gibbous);
        if (previewMoonWaxingGibbousIcon != null)
        {
            previewMoonWaxingGibbousIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.WAXING_GIBBOUS.getIcon(), colorWaxing, colorWaxing, 0));
        }

        // waning
        ImageView previewMoonWaningCrescentIcon = (ImageView) previewLayout.findViewById(R.id.icon_info_moonphase_waning_crescent);
        if (previewMoonWaningCrescentIcon != null)
        {
            previewMoonWaningCrescentIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.WANING_CRESCENT.getIcon(), colorWaning, colorWaning, 0));
        }

        ImageView previewMoonWaningQuarterIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waning_quarter);
        if (previewMoonWaningQuarterIcon != null)
        {
            previewMoonWaningQuarterIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0));
        }

        ImageView previewMoonWaningGibbousIcon = (ImageView)previewLayout.findViewById(R.id.icon_info_moonphase_waning_gibbous);
        if (previewMoonWaningGibbousIcon != null)
        {
            previewMoonWaningGibbousIcon.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.WANING_GIBBOUS.getIcon(), colorWaning, colorWaning, 0));
        }

        MoonPhaseDisplay phase = data2.getMoonPhaseToday();
        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            View iconView = findViewById(moonPhase.getView());
            if (iconView != null)
            {
                iconView.setVisibility((phase == moonPhase) ? View.VISIBLE : View.GONE);
            }
        }

        ImageView previewMoonFullIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_full_icon);
        if (previewMoonFullIcon1 != null)
        {
            previewMoonFullIcon1.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(this, MoonPhaseDisplay.FULL.getIcon(), colorFull, colorWaning, moonStrokePx));
        }

        ImageView previewMoonNewIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_new_icon);
        if (previewMoonNewIcon1 != null)
        {
            previewMoonNewIcon1.setImageBitmap(SuntimesUtils.gradientDrawableToBitmap(this, MoonPhaseDisplay.NEW.getIcon(), colorNew, colorWaxing, moonStrokePx));
        }

        ImageView previewMoonWaxingQuarterIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_firstquarter_icon);
        if (previewMoonWaxingQuarterIcon1 != null)
        {
            previewMoonWaxingQuarterIcon1.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.FIRST_QUARTER.getIcon(), colorWaxing, colorWaxing, 0));
        }

        ImageView previewMoonWaningQuarterIcon1 = (ImageView)previewLayout.findViewById(R.id.moonphase_thirdquarter_icon);
        if (previewMoonWaningQuarterIcon1 != null)
        {
            previewMoonWaningQuarterIcon1.setImageBitmap(SuntimesUtils.layerDrawableToBitmap(this, MoonPhaseDisplay.THIRD_QUARTER.getIcon(), colorWaning, colorWaning, 0));
        }
    }

    private void updatePreview_moonPhaseLabel(TextView label)
    {
        if (label != null)
        {
            label.setTextColor(chooseColorText.getColor());
            label.setTextSize(chooseTextSize.getValue());
            label.setVisibility(View.VISIBLE);
        }
    }

    protected int colorForMoonPhase( MoonPhaseDisplay phase )
    {
        switch (phase)
        {
            case NEW:
                return chooseColorMoonNew.getColor();

            case WAXING_CRESCENT:
            case FIRST_QUARTER:
            case WAXING_GIBBOUS:
                return chooseColorMoonWaxing.getColor();

            case WANING_CRESCENT:
            case THIRD_QUARTER:
            case WANING_GIBBOUS:
                return chooseColorMoonWaning.getColor();

            case FULL:
            default:
                return chooseColorMoonFull.getColor();
        }
    }

    private static void updateSizeFromChooser(TextView text, SizeChooser chooser)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = chooser.getValue();
            if (textSize >= chooser.getMin() && textSize <= chooser.getMax())
            {
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, chooser.getValue());
            }
        }
    }

    /*@Override
    public void onDestroy()
    {
        super.onDestroy();
    }*/

    @Override
    public void onResume()
    {
        super.onResume();
        if (param_wallpaper)
        {
            initWallpaper();
        }
        for (ColorChooser chooser : colorChoosers)
        {
            chooser.onResume();
        }
    }

    /**
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putString(THEME_NAME, chooseName.getThemeName());
        outState.putString(THEME_DISPLAYSTRING, editDisplay.getText().toString());
        outState.putInt(PARAM_PREVIEWID, preview.getDisplayedChild());

        SuntimesTheme.ThemeBackground background = (SuntimesTheme.ThemeBackground)spinBackground.getSelectedItem();
        if (background != null)
        {
            outState.putString(THEME_BACKGROUND, background.name());
        }

        for (SizeChooser chooser : sizeChoosers)
        {
            outState.putFloat(chooser.getID(), chooser.getValue());
        }
        for (ColorChooser chooser : colorChoosers)
        {
            outState.putInt(chooser.getID(), chooser.getColor());
        }
        outState.putIntArray(THEME_PADDING, choosePadding.getPadding());
        outState.putIntegerArrayList(ColorDialog.KEY_RECENT, recentColors);
    }

    /**
     * @param savedState Bundle
     */
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        if (mode == UIMode.ADD_THEME)
        {
            chooseName.setThemeName(getString(savedState, THEME_NAME, chooseName.getThemeName()));
        }
        editDisplay.setText(getString(savedState, THEME_DISPLAYSTRING, editDisplay.getText().toString()));

        flipToPreview(savedState.getInt(PARAM_PREVIEWID, -1));

        SuntimesTheme.ThemeBackground background = (SuntimesTheme.ThemeBackground)spinBackground.getSelectedItem();
        String backgroundName = savedState.getString(THEME_BACKGROUND);
        if (backgroundName == null)
        {
            backgroundName = (background != null ? background.name() : DarkTheme.THEMEDEF_BACKGROUND.name());
        }

        try {
            setSelectedBackground(SuntimesTheme.ThemeBackground.valueOf(backgroundName));
        } catch (IllegalArgumentException e) {
            Log.e("setBackground", "Unable to resolve ThemeBackground " + backgroundName);
            spinBackground.setSelection(0);
        }

        for (SizeChooser chooser : sizeChoosers) {
            chooser.setValue(savedState);
        }

        ArrayList<Integer> colors = savedState.getIntegerArrayList(ColorDialog.KEY_RECENT);
        if (colors != null) {
            recentColors.clear();
            recentColors.addAll(colors);
        }

        for (ColorChooser chooser : colorChoosers)
        {
            chooser.setRecentColors(recentColors);
            chooser.setColor(savedState);
        }

        int[] p = savedState.getIntArray(THEME_PADDING);
        if (p != null) {
            choosePadding.setPadding(p);
        }
    }

    protected void flipToPreview( int previewID )
    {
        if (previewID >= 0 && previewID < preview.getChildCount())
        {
            preview.setDisplayedChild(previewID);
        }
    }

    private String getString(Bundle bundle, String key, String defaultValue)
    {
        String value = bundle.getString(key);
        if (value != null)
            return value;
        else return defaultValue;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themeconfig, menu);

        final MenuItem saveItem = menu.findItem(R.id.saveTheme);
        preview.getHandler().postDelayed(new Runnable()   // TODO: bug here: npe this line, sometimes
        {
            public void run()
            {
                saveItem.setVisible(true);
            }
        }, SAVE_ITEM_DELAY);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.saveTheme) {
            onSaveClicked();
            return true;

        } else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveClicked()
    {
        if (validateInput())
        {
            SuntimesTheme theme = saveTheme();
            if (theme != null)
            {
                Intent intent = new Intent();
                intent.putExtra(THEME_NAME, theme.themeName());
                WidgetThemes.addValue(WidgetThemeConfigActivity.this, theme.themeDescriptor());
                setResult(Activity.RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
            }
        }
    }

    /**
     * loads values from an existing theme into ui fields,
     */
    protected void loadTheme( String themeName )
    {
        if (themeName != null)
        {
            chooseName.setThemeName( (mode == UIMode.ADD_THEME) ? generateThemeName(themeName) : themeName );

        } else if (mode == UIMode.ADD_THEME) {
            chooseName.setThemeName( suggestThemeName() );
            editDisplay.requestFocus();
        }

        try {
            String themeID = (themeName == null ? WidgetSettings.PREF_DEF_APPEARANCE_THEME : themeName);
            SuntimesTheme theme = WidgetThemes.loadTheme(this, themeID);
            if (themeName != null)
            {
                editDisplay.setText((mode == UIMode.ADD_THEME) ? generateThemeDisplayString(theme.themeDisplayString()) : theme.themeDisplayString());
            }
            chooseTitleSize.setValue(theme.getTitleSizeSp());
            chooseTextSize.setValue(theme.getTextSizeSp());
            chooseTimeSize.setValue(theme.getTimeSizeSp());
            chooseSuffixSize.setValue(theme.getTimeSuffixSizeSp());
            chooseColorTitle.setColor(theme.getTitleColor());
            chooseColorText.setColor(theme.getTextColor());
            chooseColorTime.setColor(theme.getTimeColor());
            chooseColorSuffix.setColor(theme.getTimeSuffixColor());
            chooseColorAction.setColor(theme.getActionColor());
            chooseColorAccent.setColor(theme.getAccentColor());

            checkTitleBold.setChecked(theme.getTitleBold());
            checkTimeBold.setChecked(theme.getTimeBold());

            chooseIconStroke.setValue(theme.getSunsetIconStrokeWidth());
            chooseNoonIconStroke.setValue(theme.getNoonIconStrokeWidth());

            chooseColorRise.setColor(theme.getSunriseTextColor());
            chooseColorRiseIconFill.setColor(theme.getSunriseIconColor());
            chooseColorRiseIconStroke.setColor(theme.getSunriseIconStrokeColor());

            chooseColorNoon.setColor(theme.getNoonTextColor());
            chooseColorNoonIconFill.setColor(theme.getNoonIconColor());
            chooseColorNoonIconStroke.setColor(theme.getNoonIconStrokeColor());

            chooseColorSet.setColor(theme.getSunsetTextColor());
            chooseColorSetIconFill.setColor(theme.getSunsetIconColor());
            chooseColorSetIconStroke.setColor(theme.getSunsetIconStrokeColor());

            chooseColorDay.setColor(theme.getDayColor());
            chooseColorCivil.setColor(theme.getCivilColor());
            chooseColorNautical.setColor(theme.getNauticalColor());
            chooseColorAstro.setColor(theme.getAstroColor());
            chooseColorNight.setColor(theme.getNightColor());
            chooseColorPointFill.setColor(theme.getGraphPointFillColor());
            chooseColorPointStroke.setColor(theme.getGraphPointStrokeColor());

            chooseColorSpring.setColor(theme.getSpringColor());
            chooseColorSummer.setColor(theme.getSummerColor());
            chooseColorFall.setColor(theme.getFallColor());
            chooseColorWinter.setColor(theme.getWinterColor());

            chooseColorMapBackground.setColor(theme.getMapBackgroundColor());
            chooseColorMapForeground.setColor(theme.getMapForegroundColor());
            chooseColorMapShadow.setColor(theme.getMapShadowColor());
            chooseColorMapHighlight.setColor(theme.getMapHighlightColor());

            chooseColorMoonrise.setColor(theme.getMoonriseTextColor());
            chooseColorMoonset.setColor(theme.getMoonsetTextColor());
            chooseColorMoonWaning.setColor(theme.getMoonWaningColor());
            chooseColorMoonNew.setColor(theme.getMoonNewColor());
            chooseColorMoonWaxing.setColor(theme.getMoonWaxingColor());
            chooseColorMoonFull.setColor(theme.getMoonFullColor());

            chooseColorMoonNewText.setColor(theme.getMoonNewTextColor());
            chooseColorMoonFullText.setColor(theme.getMoonFullTextColor());

            chooseMoonStroke.setValue(theme.getMoonFullStroke());

            choosePadding.setPadding(theme.getPadding());
            setSelectedBackground(theme.getBackground());
            chooseColorBackground.setColor(theme.getBackgroundColor());

        } catch (InvalidParameterException e) {
            Log.e("loadTheme", "unable to load theme: " + e);
        }

        toggleRiseSetIconFill(usingRiseSetIconFill(), true);
        toggleRiseSetIconStroke(usingRiseSetIconStroke(), true);
        toggleNoonIconColor(usingNoonIconColor(), true);
        updateRecentColors();
    }

    private void setSelectedBackground(SuntimesTheme.ThemeBackground themeBackground)
    {
        int backgroundPos = spinBackground_adapter.getPosition(themeBackground);
        spinBackground.setSelection(Math.max(backgroundPos, 0));
    }

    /**
     *
     */
    protected SuntimesTheme saveTheme()
    {
        SuntimesTheme theme = toTheme();
        SharedPreferences themePref = getSharedPreferences(WidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
        theme.saveTheme(themePref);
        return theme;
    }

    /**
     *
     */
    public SuntimesTheme toTheme()
    {
        return new SuntimesTheme()
        {
            private SuntimesTheme init()
            {
                this.themeName = chooseName.getThemeName();
                this.themeDisplayString = editDisplay.getText().toString();
                this.themeTitleSize = chooseTitleSize.getValue();
                this.themeTextSize = chooseTextSize.getValue();
                this.themeTimeSize = chooseTimeSize.getValue();
                this.themeTimeSuffixSize = chooseSuffixSize.getValue();
                this.themeTitleColor = chooseColorTitle.getColor();
                this.themeTextColor = chooseColorText.getColor();
                this.themeTimeColor = chooseColorTime.getColor();
                this.themeTimeSuffixColor = chooseColorSuffix.getColor();
                this.themeActionColor = chooseColorAction.getColor();
                this.themeAccentColor = chooseColorAccent.getColor();

                this.themeTitleBold = checkTitleBold.isChecked();
                this.themeTimeBold = checkTimeBold.isChecked();

                this.themeSunriseTextColor = chooseColorRise.getColor();
                this.themeSunriseIconColor = chooseColorRiseIconFill.getColor();
                this.themeSunriseIconStrokeColor = chooseColorRiseIconStroke.getColor();
                this.themeSunriseIconStrokeWidth = (int)chooseIconStroke.getValue();

                this.themeNoonTextColor = chooseColorNoon.getColor();
                this.themeNoonIconColor = chooseColorNoonIconFill.getColor();
                this.themeNoonIconStrokeColor = chooseColorNoonIconStroke.getColor();
                this.themeNoonIconStrokeWidth = (int)chooseNoonIconStroke.getValue();

                this.themeSunsetTextColor = chooseColorSet.getColor();
                this.themeSunsetIconColor = chooseColorSetIconFill.getColor();
                this.themeSunsetIconStrokeColor = chooseColorSetIconStroke.getColor();
                this.themeSunsetIconStrokeWidth = (int)chooseIconStroke.getValue();

                this.themeDayColor = chooseColorDay.getColor();
                this.themeCivilColor = chooseColorCivil.getColor();
                this.themeNauticalColor = chooseColorNautical.getColor();
                this.themeAstroColor = chooseColorAstro.getColor();
                this.themeNightColor = chooseColorNight.getColor();
                this.themeGraphPointFillColor = chooseColorPointFill.getColor();
                this.themeGraphPointStrokeColor = chooseColorPointStroke.getColor();

                this.themeSpringColor = chooseColorSpring.getColor();
                this.themeSummerColor = chooseColorSummer.getColor();
                this.themeFallColor = chooseColorFall.getColor();
                this.themeWinterColor = chooseColorWinter.getColor();

                this.themeMapBackgroundColor = chooseColorMapBackground.getColor();
                this.themeMapForegroundColor = chooseColorMapForeground.getColor();
                this.themeMapShadowColor = chooseColorMapShadow.getColor();
                this.themeMapHighlightColor = chooseColorMapHighlight.getColor();

                this.themeMoonriseTextColor = chooseColorMoonrise.getColor();
                this.themeMoonsetTextColor = chooseColorMoonset.getColor();
                this.themeMoonWaningColor = chooseColorMoonWaning.getColor();
                this.themeMoonNewColor = chooseColorMoonNew.getColor();
                this.themeMoonWaxingColor = chooseColorMoonWaxing.getColor();
                this.themeMoonFullColor = chooseColorMoonFull.getColor();

                this.themeMoonWaningTextColor = chooseColorMoonWaning.getColor();
                this.themeMoonNewTextColor = chooseColorMoonNewText.getColor();
                this.themeMoonWaxingTextColor = chooseColorMoonWaxing.getColor();
                this.themeMoonFullTextColor = chooseColorMoonFullText.getColor();

                this.themeMoonFullStroke = (int)chooseMoonStroke.getValue();
                this.themeMoonNewStroke = (int)chooseMoonStroke.getValue();

                this.themePadding = choosePadding.getPadding();
                ThemeBackground backgroundItem = (ThemeBackground)spinBackground.getSelectedItem();
                if (backgroundItem != null)
                {
                    this.themeBackground = backgroundItem;
                }
                this.themeBackgroundColor = chooseColorBackground.getColor();
                return this;
            }
        }.init();
    }

    protected ContentValues toContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(THEME_NAME, chooseName.getThemeName());

        values.put(THEME_DISPLAYSTRING, editDisplay.getText().toString());
        values.put(THEME_TITLESIZE, chooseTitleSize.getValue());
        values.put(THEME_TEXTSIZE, chooseTextSize.getValue());
        values.put(THEME_TIMESIZE, chooseTimeSize.getValue());
        values.put(THEME_TIMESUFFIXSIZE, chooseSuffixSize.getValue());
        values.put(THEME_TITLECOLOR, chooseColorTitle.getColor());
        values.put(THEME_TEXTCOLOR, chooseColorText.getColor());
        values.put(THEME_TIMECOLOR, chooseColorTime.getColor());
        values.put(THEME_TIMESUFFIXCOLOR, chooseColorSuffix.getColor());
        values.put(THEME_ACTIONCOLOR, chooseColorAction.getColor());
        values.put(THEME_ACCENTCOLOR, chooseColorAccent.getColor());

        values.put(THEME_TITLEBOLD, checkTitleBold.isChecked());
        values.put(THEME_TIMEBOLD, checkTimeBold.isChecked());

        values.put(THEME_SUNRISECOLOR, chooseColorRise.getColor());
        values.put(THEME_RISEICON_FILL_COLOR, chooseColorRiseIconFill.getColor());
        values.put(THEME_RISEICON_STROKE_COLOR, chooseColorRiseIconStroke.getColor());
        values.put(THEME_RISEICON_STROKE_WIDTH, (int)chooseIconStroke.getValue());

        values.put(THEME_NOONCOLOR, chooseColorNoon.getColor());
        values.put(THEME_NOONICON_FILL_COLOR, chooseColorNoonIconFill.getColor());
        values.put(THEME_NOONICON_STROKE_COLOR, chooseColorNoonIconStroke.getColor());
        values.put(THEME_NOONICON_STROKE_WIDTH, (int)chooseNoonIconStroke.getValue());

        values.put(THEME_SUNSETCOLOR, chooseColorSet.getColor());
        values.put(THEME_SETICON_FILL_COLOR, chooseColorSetIconFill.getColor());
        values.put(THEME_SETICON_STROKE_COLOR, chooseColorSetIconStroke.getColor());
        values.put(THEME_SETICON_STROKE_WIDTH, (int)chooseIconStroke.getValue());

        values.put(THEME_DAYCOLOR, chooseColorDay.getColor());
        values.put(THEME_CIVILCOLOR, chooseColorCivil.getColor());
        values.put(THEME_NAUTICALCOLOR, chooseColorNautical.getColor());
        values.put(THEME_ASTROCOLOR, chooseColorAstro.getColor());
        values.put(THEME_NIGHTCOLOR, chooseColorNight.getColor());
        values.put(THEME_GRAPH_POINT_FILL_COLOR, chooseColorPointFill.getColor());
        values.put(THEME_GRAPH_POINT_STROKE_COLOR, chooseColorPointStroke.getColor());

        values.put(THEME_SPRINGCOLOR, chooseColorSpring.getColor());
        values.put(THEME_SUMMERCOLOR, chooseColorSummer.getColor());
        values.put(THEME_FALLCOLOR, chooseColorFall.getColor());
        values.put(THEME_WINTERCOLOR, chooseColorWinter.getColor());

        values.put(THEME_MAP_BACKGROUNDCOLOR, chooseColorMapBackground.getColor());
        values.put(THEME_MAP_FOREGROUNDCOLOR, chooseColorMapForeground.getColor());
        values.put(THEME_MAP_SHADOWCOLOR, chooseColorMapShadow.getColor());
        values.put(THEME_MAP_HIGHLIGHTCOLOR, chooseColorMapHighlight.getColor());

        values.put(THEME_MOONRISECOLOR, chooseColorMoonrise.getColor());
        values.put(THEME_MOONSETCOLOR, chooseColorMoonset.getColor());
        values.put(THEME_MOONWANINGCOLOR, chooseColorMoonWaning.getColor());
        values.put(THEME_MOONNEWCOLOR, chooseColorMoonNew.getColor());
        values.put(THEME_MOONWAXINGCOLOR, chooseColorMoonWaxing.getColor());
        values.put(THEME_MOONFULLCOLOR, chooseColorMoonFull.getColor());

        values.put(THEME_MOONWANINGCOLOR_TEXT, chooseColorMoonWaning.getColor());
        values.put(THEME_MOONNEWCOLOR_TEXT, chooseColorMoonNewText.getColor());
        values.put(THEME_MOONWAXINGCOLOR_TEXT, chooseColorMoonWaxing.getColor());
        values.put(THEME_MOONFULLCOLOR_TEXT, chooseColorMoonFullText.getColor());

        values.put(THEME_MOONFULL_STROKE_WIDTH, (int)chooseMoonStroke.getValue());
        values.put(THEME_MOONNEW_STROKE_WIDTH, (int)chooseMoonStroke.getValue());

        int[] padding = choosePadding.getPadding();
        values.put(THEME_PADDING_LEFT, padding[0]);
        values.put(THEME_PADDING_TOP, padding[1]);
        values.put(THEME_PADDING_RIGHT, padding[2]);
        values.put(THEME_PADDING_BOTTOM, padding[3]);

        SuntimesTheme.ThemeBackground backgroundItem = (SuntimesTheme.ThemeBackground)spinBackground.getSelectedItem();
        if (backgroundItem != null) {
            values.put(THEME_BACKGROUND, backgroundItem.name());
        }
        values.put(THEME_BACKGROUND_COLOR, chooseColorBackground.getColor());

        return values;
    }

    /**
     * Used when adding a new theme.
     * @return a unique themeName
     */
    protected String suggestThemeName()
    {
        int i = 1;
        String generatedName;
        do {
            generatedName = getString(R.string.addtheme_custname, i+"");
            i++;
        } while (WidgetThemes.valueOf(generatedName) != null);
        return generatedName;
    }

    /**
     * Used when copying an existing theme.
     * @param suggestedName the desired themeName (might not be unique/available)
     * @return a unique themeName
     */
    protected String generateThemeName( @NonNull String suggestedName )
    {
        int i = 1;
        String copyName = getString(R.string.addtheme_copyname, "", "");
        String generatedName = suggestedName;
        while (WidgetThemes.valueOf(generatedName) != null)
        {
            String root;
            String[] parts = generatedName.split(copyName,2);
            if (parts.length < 2) {
                root = suggestedName;
                
            } else {
                root = parts[0];
                try {
                    i = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException e) {
                    i++;
                }
            }
            generatedName = getString(R.string.addtheme_copyname, root, Integer.toString(i));
        }
        return generatedName;
    }

    protected String generateThemeDisplayString( String suggestedName )
    {
        return getString(R.string.addtheme_copydisplay, suggestedName);
    }

    /**
     * @return true fields are valid, false one or more fields is invalid
     */
    protected boolean validateInput()
    {
        boolean isValid = chooseTitleSize.validateValue(this);
        isValid = isValid && chooseTextSize.validateValue(this);
        isValid = isValid && chooseTimeSize.validateValue(this);
        isValid = isValid && chooseSuffixSize.validateValue(this);
        isValid = isValid && chooseIconStroke.validateValue(this);
        isValid = isValid && chooseNoonIconStroke.validateValue(this);
        isValid = isValid && validateThemeDisplayText(editDisplay);
        isValid = isValid && validateThemeID(chooseName.getField());
        return isValid;
    }

    protected boolean validateThemeID( EditText editName )
    {
        return validateThemeID(this, editName, true);
    }
    protected static boolean validateThemeID(Context context, EditText editName, boolean grabFocus )
    {
        boolean isValid = true;
        editName.setError(null);

        String themeID = editName.getText().toString().trim();
        if (themeID.isEmpty())
        {
            isValid = false;       // themeName is required
            editName.setError(context.getString(R.string.edittheme_error_themeName_empty));
            if (grabFocus)
                editName.requestFocus();
        }
        if (mode == UIMode.ADD_THEME && WidgetThemes.valueOf(editName.getText().toString()) != null)
        {
            isValid = false;       // themeName is already taken
            editName.setError(context.getString(R.string.edittheme_error_themeName_unique));
            if (grabFocus)
                editName.requestFocus();
        }
        return isValid;
    }

    protected boolean validateThemeDisplayText( EditText editDisplay )
    {
        return validateThemeDisplayText(this, editDisplay, true);
    }
    protected static boolean validateThemeDisplayText(Context context, EditText editDisplay, boolean grabFocus )
    {
        boolean isValid = true;
        editDisplay.setError(null);

        if (editDisplay.getText().toString().trim().isEmpty())
        {
            isValid = false;     // display text is empty
            editDisplay.setError(context.getString(R.string.edittheme_error_displaytext));
            if (grabFocus)
                editDisplay.requestFocus();
        }
        return isValid;
    }

    protected void initWallpaper()
    {
        ImageView background = (ImageView)findViewById(R.id.preview_background);

        if (Build.VERSION.SDK_INT > 18)
        {
            background.setVisibility(View.GONE);
            getWindow().setBackgroundDrawable(new ColorDrawable(0));

        } else {
            try {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                if (wallpaperManager != null)
                {
                    @SuppressLint("MissingPermission")
                    Drawable wallpaper = wallpaperManager.getDrawable();
                    if (background != null && wallpaper != null) {
                        background.setImageDrawable(wallpaper);
                    }
                }
            } catch (Exception e) {
                Log.e("initWallpaper", "failed to init wallpaper; " + e);
            }
        }
    }

    /**
     * ThemeNameChooser
     */
    private class ThemeNameChooser implements TextWatcher, View.OnFocusChangeListener
    {
        private final EditText edit;
        private String themeName;

        public ThemeNameChooser( EditText editField )
        {
            edit = editField;
            edit.setRawInputType(InputType.TYPE_CLASS_TEXT);
            edit.addTextChangedListener(this);
            edit.setOnFocusChangeListener(this);
        }

        public EditText getField()
        {
            return edit;
        }

        public String getThemeName()
        {
            return themeName;
        }

        public void setThemeName( String themeName )
        {
            this.themeName = themeName;
            updateViews();
        }

        public void updateViews()
        {
            edit.setText(themeName);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable)
        {
            themeName = editable.toString();
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus)
        {
            if (!hasFocus)
            {
                afterTextChanged(edit.getText());
                if (validateThemeID(WidgetThemeConfigActivity.this, edit, false))
                {
                    updateViews();
                }
            }
        }
    }

    /**
     * TextSizeChooser
     */
    private class SizeChooser extends com.forrestguice.suntimeswidget.settings.SizeChooser
    {
        public SizeChooser(Context context, EditText editField, float min, float max, String id)
        {
            super(context, editField, min, max, id);
        }

        @Override
        public void updatePreview()
        {
            WidgetThemeConfigActivity.this.updatePreview();
        }
    }

    /**
     * ColorChooser
     */
    private class ColorChooser extends com.forrestguice.suntimeswidget.settings.colors.ColorChooser
    {
        public ColorChooser(Context context, TextView txtLabel, EditText editField, ImageButton imgButton, String id)
        {
            super(context, txtLabel, editField, imgButton, id);
        }

        @Override
        protected void onColorChanged( int newColor )
        {
            super.onColorChanged(newColor);
            updatePreview();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

}
