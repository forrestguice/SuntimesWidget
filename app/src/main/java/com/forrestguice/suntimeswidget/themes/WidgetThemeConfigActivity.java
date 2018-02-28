/**
    Copyright (C) 2017-2018 Forrest Guice
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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.PaddingChooser;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;

import static com.forrestguice.suntimeswidget.themes.SuntimesTheme.THEME_NAME;

public class WidgetThemeConfigActivity extends AppCompatActivity
{
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_PREVIEWID = "previewID";
    public static final String PARAM_WALLPAPER = "useWallpaper";

    public static final int SAVE_ITEM_DELAY = 1000;

    public static final int PREVIEWID_SUN_2x1 = 0;
    public static final int PREVIEWID_MOON_2x1 = 1;
    public static final int PREVIEWID_MOON_3x1 = 2;

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

    private ActionBar actionBar;
    private EditText editDisplay;
    private SizeChooser chooseTitleSize, chooseTextSize, chooseTimeSize, chooseSuffixSize;
    private SizeChooser chooseIconStroke, chooseNoonIconStroke;
    private SizeChooser chooseMoonStroke;
    private ArrayList<SizeChooser> sizeChoosers;
    private ThemeNameChooser chooseName;
    private PaddingChooser choosePadding;

    private ColorChooser chooseColorRise, chooseColorRiseIconFill, chooseColorRiseIconStroke;
    private ColorChooser chooseColorNoon, chooseColorNoonIconFill, chooseColorNoonIconStroke;
    private ColorChooser chooseColorSet, chooseColorSetIconFill, chooseColorSetIconStroke;
    private ColorChooser chooseColorTitle, chooseColorText, chooseColorTime, chooseColorSuffix;
    private ColorChooser chooseColorSpring, chooseColorSummer, chooseColorFall, chooseColorWinter;
    private ColorChooser chooseColorMoonrise, chooseColorMoonset;
    private ColorChooser chooseColorMoonWaning, chooseColorMoonNew, chooseColorMoonWaxing, chooseColorMoonFull;
    private ArrayList<ColorChooser> colorChoosers;
    private CheckBox checkUseFill, checkUseStroke, checkUseNoon;

    private CheckBox checkTitleBold, checkTimeBold;

    private Spinner spinBackground;

    private ViewFlipper preview;

    private SuntimesUtils utils = new SuntimesUtils();

    public WidgetThemeConfigActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
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

    private SuntimesRiseSetData data;
    private SuntimesMoonData data2;
    private void initData(Context context)
    {
        data = new SuntimesRiseSetData(context, 0);   // use app configuration
        data.setCompareMode(WidgetSettings.CompareMode.TOMORROW);
        data.setTimeMode(WidgetSettings.TimeMode.OFFICIAL);
        data.calculate();

        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data);
        noonData.setTimeMode(WidgetSettings.TimeMode.NOON);
        noonData.calculate();
        data.linkData(noonData);

        data2 = new SuntimesMoonData(context, 0);
        data2.calculate();
    }

    private void initLocale()
    {
        AppSettings.initLocale(this);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
        ThemeBackground.initDisplayStrings(this);
        SuntimesUtils.initDisplayStrings(this);
    }

    protected void initViews( Context context )
    {
        colorChoosers = new ArrayList<>();
        sizeChoosers = new ArrayList<>();

        initPreview(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<ThemeBackground> spinBackground_adapter = new ArrayAdapter<>(this, R.layout.layout_listitem_oneline, ThemeBackground.values());
        spinBackground_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBackground = (Spinner)findViewById(R.id.editSpin_background);
        spinBackground.setAdapter(spinBackground_adapter);
        spinBackground.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                updatePreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                updatePreview();
            }
        });

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

        chooseTitleSize = createSizeChooser(this, R.id.edit_titleSize, SuntimesTheme.THEME_TITLESIZE_MIN, SuntimesTheme.THEME_TITLESIZE_MAX, SuntimesTheme.THEME_TITLESIZE);
        chooseTextSize = createSizeChooser(this, R.id.edit_textSize, SuntimesTheme.THEME_TEXTSIZE_MIN, SuntimesTheme.THEME_TEXTSIZE_MAX, SuntimesTheme.THEME_TEXTSIZE);
        chooseTimeSize = createSizeChooser(this, R.id.edit_timeSize, SuntimesTheme.THEME_TIMESIZE_MIN, SuntimesTheme.THEME_TIMESIZE_MAX, SuntimesTheme.THEME_TIMESIZE);
        chooseSuffixSize = createSizeChooser(this, R.id.edit_suffixSize, SuntimesTheme.THEME_TIMESUFFIXSIZE_MIN, SuntimesTheme.THEME_TIMESUFFIXSIZE_MAX, SuntimesTheme.THEME_TIMESUFFIXSIZE);
        chooseIconStroke = createSizeChooser(this, R.id.edit_iconStroke, SuntimesTheme.THEME_SETICON_STROKE_WIDTH_MIN, SuntimesTheme.THEME_SETICON_STROKE_WIDTH_MAX, SuntimesTheme.THEME_SETICON_STROKE_WIDTH);
        chooseNoonIconStroke = createSizeChooser(this, R.id.edit_noonIconStroke, SuntimesTheme.THEME_NOONICON_STROKE_WIDTH_MIN, SuntimesTheme.THEME_NOONICON_STROKE_WIDTH_MAX, SuntimesTheme.THEME_NOONICON_STROKE_WIDTH);
        chooseMoonStroke = createSizeChooser(this, SuntimesTheme.THEME_MOON_STROKE_MIN, SuntimesTheme.THEME_MOON_STROKE_MAX, SuntimesTheme.THEME_MOONFULL_STROKE_WIDTH);

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
        chooseColorRise = createColorChooser(this, R.id.editLabel_sunriseColor, R.id.edit_sunriseColor, R.id.editButton_sunriseColor, SuntimesTheme.THEME_SUNRISECOLOR);
        chooseColorRiseIconFill = createColorChooser(this, R.id.editLabel_sunriseFillColor, R.id.edit_sunriseFillColor, R.id.editButton_sunriseFillColor, SuntimesTheme.THEME_RISEICON_FILL_COLOR);
        chooseColorRiseIconStroke = createColorChooser(this, R.id.editLabel_sunriseStrokeColor, R.id.edit_sunriseStrokeColor, R.id.editButton_sunriseStrokeColor, SuntimesTheme.THEME_RISEICON_STROKE_COLOR);

        // noon colors
        chooseColorNoon = createColorChooser(this, R.id.editLabel_noonColor, R.id.edit_noonColor, R.id.editButton_noonColor, SuntimesTheme.THEME_NOONCOLOR);
        chooseColorNoonIconFill = createColorChooser(this, R.id.editLabel_noonFillColor, R.id.edit_noonFillColor,  R.id.editButton_noonFillColor, SuntimesTheme.THEME_NOONICON_FILL_COLOR);
        chooseColorNoonIconStroke = createColorChooser(this, R.id.editLabel_noonStrokeColor, R.id.edit_noonStrokeColor, R.id.editButton_noonStrokeColor, SuntimesTheme.THEME_NOONICON_STROKE_COLOR);

        // sunset colors
        chooseColorSet = createColorChooser(this, R.id.editLabel_sunsetColor, R.id.edit_sunsetColor, R.id.editButton_sunsetColor, SuntimesTheme.THEME_SUNSETCOLOR);
        chooseColorSetIconFill = createColorChooser(this, R.id.editLabel_sunsetFillColor, R.id.edit_sunsetFillColor, R.id.editButton_sunsetFillColor, SuntimesTheme.THEME_SETICON_FILL_COLOR);
        chooseColorSetIconStroke = createColorChooser(this, R.id.editLabel_sunsetStrokeColor, R.id.edit_sunsetStrokeColor, R.id.editButton_sunsetStrokeColor, SuntimesTheme.THEME_SETICON_STROKE_COLOR);

        // season colors
        chooseColorSpring = createColorChooser(this, R.id.editLabel_springColor, R.id.edit_springColor, R.id.editButton_springColor, SuntimesTheme.THEME_SPRINGCOLOR );
        chooseColorSummer = createColorChooser(this, R.id.editLabel_summerColor, R.id.edit_summerColor, R.id.editButton_summerColor, SuntimesTheme.THEME_SUMMERCOLOR);
        chooseColorFall = createColorChooser(this, R.id.editLabel_fallColor, R.id.edit_fallColor, R.id.editButton_fallColor, SuntimesTheme.THEME_FALLCOLOR);
        chooseColorWinter = createColorChooser(this, R.id.editLabel_winterColor, R.id.edit_winterColor, R.id.editButton_winterColor, SuntimesTheme.THEME_WINTERCOLOR);

        // moon colors
        chooseColorMoonrise = createColorChooser(this, R.id.editLabel_moonriseColor, R.id.edit_moonriseColor, R.id.editButton_moonriseColor, SuntimesTheme.THEME_MOONRISECOLOR);
        chooseColorMoonset = createColorChooser(this, R.id.editLabel_moonsetColor, R.id.edit_moonsetColor, R.id.editButton_moonsetColor, SuntimesTheme.THEME_MOONSETCOLOR);

        chooseColorMoonWaning = createColorChooser(this, R.id.editLabel_moonWaningColor, R.id.edit_moonWaningColor, R.id.editButton_moonWaningColor, SuntimesTheme.THEME_MOONWANINGCOLOR);
        chooseColorMoonNew = createColorChooser(this, R.id.editLabel_moonNewColor, R.id.edit_moonNewColor, R.id.editButton_moonNewColor, SuntimesTheme.THEME_MOONNEWCOLOR);
        chooseColorMoonWaxing = createColorChooser(this, R.id.editLabel_moonWaxingColor, R.id.edit_moonWaxingColor, R.id.editButton_moonWaxingColor, SuntimesTheme.THEME_MOONWAXINGCOLOR);
        chooseColorMoonFull = createColorChooser(this, R.id.editLabel_moonFullColor, R.id.edit_moonFullColor, R.id.editButton_moonFullColor, SuntimesTheme.THEME_MOONFULLCOLOR);

        // other colors
        chooseColorTitle = createColorChooser(this, R.id.editLabel_titleColor, R.id.edit_titleColor, R.id.editButton_titleColor, SuntimesTheme.THEME_TITLECOLOR);
        chooseColorText = createColorChooser(this, R.id.editLabel_textColor, R.id.edit_textColor, R.id.editButton_textColor, SuntimesTheme.THEME_TEXTCOLOR);
        chooseColorTime = createColorChooser(this, R.id.editLabel_timeColor, R.id.edit_timeColor, R.id.editButton_timeColor, SuntimesTheme.THEME_TIMECOLOR);
        chooseColorSuffix = createColorChooser(this, R.id.editLabel_suffixColor, R.id.edit_suffixColor, R.id.editButton_suffixColor, SuntimesTheme.THEME_TIMESUFFIXCOLOR);

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
                actionBar.setTitle(getString(R.string.configLabel_widgetThemeEdit));
                labelName.setEnabled(false);
                labelName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                editName.setEnabled(false);
                editName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                break;

            case ADD_THEME:
            default:
                actionBar.setTitle(getString(R.string.configLabel_widgetThemeAdd));
                labelName.setEnabled(true);
                labelName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                editName.setEnabled(true);
                editName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                break;

        }
    }

    private CompoundButton.OnCheckedChangeListener onCheckChanged = new CompoundButton.OnCheckedChangeListener()
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
        return createColorChooser(context, null, null, null, id);
    }
    private ColorChooser createColorChooser(Context context, int labelID, int editID, int buttonID, String id)
    {
        TextView label = (TextView)findViewById(labelID);
        EditText edit = (EditText)findViewById(editID);
        ImageButton button = (ImageButton)findViewById(buttonID);
        return createColorChooser(context, label, edit, button, id);
    }
    private ColorChooser createColorChooser(Context context, TextView label, EditText edit, ImageButton button, String id)
    {
        ColorChooser chooser = new ColorChooser(context, label, edit, button, id);
        colorChoosers.add(chooser);
        return chooser;
    }

    private SizeChooser createSizeChooser(Context context, float min, float max, String id)
    {
        return createSizeChooser(context, null, min, max, id);
    }
    private SizeChooser createSizeChooser(Context context, int editID, float min, float max, String id)
    {
        EditText edit = (EditText)findViewById(editID);
        return createSizeChooser(context, edit, min, max, id);
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
            chooser.setFragmentManager(getSupportFragmentManager());
            chooser.setCollapsed(true);
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
            ThemeBackground background = (ThemeBackground)spinBackground.getSelectedItem();
            if (background != null)
            {
                int[] padding = choosePadding.getPaddingPixels(this);
                previewBackground.setBackgroundResource(background.getResID());
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
                previewTitle.setText(SuntimesUtils.createBoldSpan(titleText, titleText));
            else previewTitle.setText(titleText);

            updateSizeFromChooser(previewTitle, chooseTitleSize);
        }

        updatePreview_sun(previewLayout);
        updatePreview_solstice(previewLayout);
        updatePreview_moon(previewLayout);
    }

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    protected void updatePreview_sun(View previewLayout)
    {
        // Noon
        TextView previewNoon = (TextView)previewLayout.findViewById(R.id.text_time_noon);
        TextView previewNoonSuffix = (TextView)previewLayout.findViewById(R.id.text_time_noon_suffix);

        SuntimesRiseSetData noonData = data.getLinked();
        SuntimesUtils.TimeDisplayText noonText = ((noonData != null)
                ? utils.calendarTimeShortDisplayString(this, noonData.sunriseCalendarToday())
                : new SuntimesUtils.TimeDisplayText("12:00"));
        if (previewNoon != null)
        {
            String noonString = noonText.getValue();
            CharSequence noon = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(noonString, noonString) : noonString);
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

        SuntimesUtils.TimeDisplayText riseText = utils.calendarTimeShortDisplayString(this, data.sunriseCalendarToday());
        if (previewRise != null)
        {
            String riseString = riseText.getValue();
            CharSequence rise = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(riseString, riseString) : riseString);
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

        SuntimesUtils.TimeDisplayText setText = utils.calendarTimeShortDisplayString(this, data.sunsetCalendarToday());
        if (previewSet != null)
        {
            String setString = setText.getValue();
            CharSequence set = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(setString, setString) : setString);
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
            previewTimeDelta.setText(utils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther()).getValue());
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
    protected void updatePreview_solstice(View previewLayout)
    {
        // TODO: spring color
        //chooseColorSpring.getColor();
        // TODO: summer color
        //chooseColorSummer.getColor();
        // TODO: autumn color
        //chooseColorFall.getColor();
        // TODO: winter color
        //chooseColorWinter.getColor();
    }

    /**
     * Update the provided preview layout.
     * @param previewLayout the layout to update
     */
    protected void updatePreview_moon(View previewLayout)
    {
        // Moonrise
        TextView previewMoonrise = (TextView)previewLayout.findViewById(R.id.text_time_moonrise);
        TextView previewMoonriseSuffix = (TextView)previewLayout.findViewById(R.id.text_time_moonrise_suffix);

        SuntimesUtils.TimeDisplayText moonriseText = utils.calendarTimeShortDisplayString(this, data2.moonriseCalendarToday());
        if (previewMoonrise != null)
        {
            String riseString = moonriseText.getValue();
            CharSequence rise = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(riseString, riseString) : riseString);
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

        SuntimesUtils.TimeDisplayText moonsetText = utils.calendarTimeShortDisplayString(this, data2.moonsetCalendarToday());
        if (previewMoonset != null)
        {
            String setString = moonsetText.getValue();
            CharSequence set = (checkTimeBold.isChecked() ? SuntimesUtils.createBoldSpan(setString, setString) : setString);
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

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
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putString(SuntimesTheme.THEME_NAME, chooseName.getThemeName());
        outState.putString(SuntimesTheme.THEME_DISPLAYSTRING, editDisplay.getText().toString());
        outState.putInt(PARAM_PREVIEWID, preview.getDisplayedChild());

        ThemeBackground background = (ThemeBackground)spinBackground.getSelectedItem();
        if (background != null)
        {
            outState.putInt(SuntimesTheme.THEME_BACKGROUND, background.getResID());
        }

        for (SizeChooser chooser : sizeChoosers)
        {
            outState.putInt(chooser.getID(), chooser.getValue());
        }
        for (ColorChooser chooser : colorChoosers)
        {
            outState.putInt(chooser.getID(), chooser.getColor());
        }
        outState.putIntArray(SuntimesTheme.THEME_PADDING, choosePadding.getPadding());
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
            chooseName.setThemeName(getString(savedState, SuntimesTheme.THEME_NAME, chooseName.getThemeName()));
        }
        editDisplay.setText(getString(savedState, SuntimesTheme.THEME_DISPLAYSTRING, editDisplay.getText().toString()));

        flipToPreview(savedState.getInt(PARAM_PREVIEWID, -1));

        ThemeBackground background = (ThemeBackground)spinBackground.getSelectedItem();
        setSelectedBackground(savedState.getInt(SuntimesTheme.THEME_BACKGROUND, (background != null ? background.getResID() : DarkTheme.THEMEDEF_BACKGROUND_ID)));

        for (SizeChooser chooser : sizeChoosers)
        {
            chooser.setValue(savedState);
        }
        for (ColorChooser chooser : colorChoosers)
        {
            chooser.setColor(savedState);
        }
        choosePadding.setPadding(savedState.getIntArray(SuntimesTheme.THEME_PADDING));
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
        preview.getHandler().postDelayed(new Runnable()
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
        switch (item.getItemId())
        {
            case R.id.saveTheme:
                onSaveClicked();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSaveClicked()
    {
        if (validateInput())
        {
            SuntimesTheme theme = saveTheme();
            if (theme != null)
            {
                Intent intent = new Intent();
                intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
                WidgetThemes.addValue(WidgetThemeConfigActivity.this, theme.themeDescriptor());
                setResult(Activity.RESULT_OK, intent);
                finish();
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
            chooseTitleSize.setValue((int)theme.getTitleSizeSp());
            chooseTextSize.setValue((int)theme.getTextSizeSp());
            chooseTimeSize.setValue((int)theme.getTimeSizeSp());
            chooseSuffixSize.setValue((int)theme.getTimeSuffixSizeSp());
            chooseColorTitle.setColor(theme.getTitleColor());
            chooseColorText.setColor(theme.getTextColor());
            chooseColorTime.setColor(theme.getTimeColor());
            chooseColorSuffix.setColor(theme.getTimeSuffixColor());

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

            chooseColorSpring.setColor(theme.getSpringColor());
            chooseColorSummer.setColor(theme.getSummerColor());
            chooseColorFall.setColor(theme.getFallColor());
            chooseColorWinter.setColor(theme.getWinterColor());

            chooseColorMoonrise.setColor(theme.getMoonriseTextColor());
            chooseColorMoonset.setColor(theme.getMoonsetTextColor());
            chooseColorMoonWaning.setColor(theme.getMoonWaningColor());
            chooseColorMoonNew.setColor(theme.getMoonNewColor());
            chooseColorMoonWaxing.setColor(theme.getMoonWaxingColor());
            chooseColorMoonFull.setColor(theme.getMoonFullColor());

            chooseMoonStroke.setValue(theme.getMoonFullStroke());

            choosePadding.setPadding(theme.getPadding());
            setSelectedBackground(theme.getBackgroundId());

        } catch (InvalidParameterException e) {
            Log.e("loadTheme", "unable to load theme: " + e);
        }

        toggleRiseSetIconFill(usingRiseSetIconFill(), true);
        toggleRiseSetIconStroke(usingRiseSetIconStroke(), true);
        toggleNoonIconColor(usingNoonIconColor(), true);
    }

    private void setSelectedBackground(int resId)
    {
        int backgroundPos = ThemeBackground.ordinal(resId);
        spinBackground.setSelection( backgroundPos < 0 ? 0 : backgroundPos );
    }

    /**
     *
     */
    protected SuntimesTheme saveTheme()
    {
        SuntimesTheme theme = new SuntimesTheme()
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

                this.themeTitleBold = checkTitleBold.isChecked();
                this.themeTimeBold = checkTimeBold.isChecked();

                this.themeSunriseTextColor = chooseColorRise.getColor();
                this.themeSunriseIconColor = chooseColorRiseIconFill.getColor();
                this.themeSunriseIconStrokeColor = chooseColorRiseIconStroke.getColor();
                this.themeSunriseIconStrokeWidth = chooseIconStroke.getValue();

                this.themeNoonTextColor = chooseColorNoon.getColor();
                this.themeNoonIconColor = chooseColorNoonIconFill.getColor();
                this.themeNoonIconStrokeColor = chooseColorNoonIconStroke.getColor();
                this.themeNoonIconStrokeWidth = chooseNoonIconStroke.getValue();

                this.themeSunsetTextColor = chooseColorSet.getColor();
                this.themeSunsetIconColor = chooseColorSetIconFill.getColor();
                this.themeSunsetIconStrokeColor = chooseColorSetIconStroke.getColor();
                this.themeSunsetIconStrokeWidth = chooseIconStroke.getValue();

                this.themeSpringColor = chooseColorSpring.getColor();
                this.themeSummerColor = chooseColorSummer.getColor();
                this.themeFallColor = chooseColorFall.getColor();
                this.themeWinterColor = chooseColorWinter.getColor();

                this.themeMoonriseTextColor = chooseColorMoonrise.getColor();
                this.themeMoonsetTextColor = chooseColorMoonset.getColor();
                this.themeMoonWaningColor = chooseColorMoonWaning.getColor();
                this.themeMoonNewColor = chooseColorMoonNew.getColor();
                this.themeMoonWaxingColor = chooseColorMoonWaxing.getColor();
                this.themeMoonFullColor = chooseColorMoonFull.getColor();

                this.themeMoonFullStroke = chooseMoonStroke.getValue();
                this.themeMoonNewStroke = chooseMoonStroke.getValue();

                this.themePadding = choosePadding.getPadding();
                ThemeBackground backgroundItem = (ThemeBackground)spinBackground.getSelectedItem();
                if (backgroundItem != null)
                {
                    this.themeBackground = backgroundItem.getResID();
                }
                return this;
            }
        }.init();

        SharedPreferences themePref = getSharedPreferences(WidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
        theme.saveTheme(themePref);
        return theme;
    }

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
     * @param suggestedName the desired themeName (might not be unique/available)
     * @return a unique themeName
     */
    protected String generateThemeName( String suggestedName )
    {
        int i = 1;
        String generatedName = suggestedName;
        while (WidgetThemes.valueOf(generatedName) != null)
        {
            generatedName = getString(R.string.addtheme_copyname, suggestedName, i+"");
            i++;
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
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        if (wallpaperManager != null)
        {
            ImageView background = (ImageView)findViewById(R.id.preview_background);
            Drawable wallpaper = wallpaperManager.getDrawable();
            if (background != null && wallpaper != null)
            {
                background.setImageDrawable(wallpaper);
            }
        }
    }

    /**
     * ThemeNameChooser
     */
    private class ThemeNameChooser implements TextWatcher, View.OnFocusChangeListener
    {
        private EditText edit;
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
    private class ColorChooser extends com.forrestguice.suntimeswidget.settings.ColorChooser
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

}
