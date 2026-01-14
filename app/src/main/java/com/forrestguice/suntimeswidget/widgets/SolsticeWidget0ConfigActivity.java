/**
    Copyright (C) 2017-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimes.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidCalendarSettings;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.calendar.CalendarSettingsInterface;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesThemeContract;
import com.forrestguice.suntimeswidget.widgets.layouts.SolsticeLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.SolsticeLayout_1x1_0;

import java.util.List;

import static com.forrestguice.suntimeswidget.widgets.SolsticeWidgetSettings.PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER;
import static com.forrestguice.suntimeswidget.widgets.SolsticeWidgetSettings.PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER;

/**
 * Solstice / Equinox widget config activity.
 */
public class SolsticeWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public SolsticeWidget0ConfigActivity()
    {
        super();
    }

    @Override
    protected Class<?> getWidgetClass() {
        return SolsticeWidget0.class;
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_solsticewidget0));
        showOptionRiseSetOrder(false);
        hideOptionUseAltitude();
        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        showOptionWeeks(true);
        showOptionHours(true);
        showOptionTimeDate(true);
        showOptionShowDate(true);
        showOptionAbbrvMonth(true);
        showOptionLabels(true);
        showOptionShowNoon(false);
        disableOptionAllowResize();
        showOptionTrackingMode(true);
        showOptionTimeModeOverride(true);
        showDataSource(false);  // temporarily hidden; atm all entries point to same implementation (false choice)
        showOptionLocalizeHemisphere(true);
        hideLayoutSettings();
        showMoreGeneralSettings(true);
        reorderOptions();
    }

    protected void reorderOptions() {
        moveViewToBeforeOther(R.id.appwidget_general_layout0, R.id.appwidget_general_showDate_layout, R.id.appwidget_appearance_showLabels);
    }

    @Override
    protected void loadCalendarSettings(Context context) {
        checkbox_showDate.setChecked(CalendarSettings.loadCalendarFlag(AndroidCalendarSettings.wrap(context), appWidgetId, CalendarSettingsInterface.PREF_KEY_CALENDAR_SHOWDATE, SolsticeLayout.PREF_DEF_CALENDAR_SHOWDATE));
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //SolsticeWidget0.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    protected int getAboutIconID()
    {
        return R.mipmap.ic_launcher;
    }

    protected void initTimeModeAdapter(Context context, boolean allValues)
    {
        if (spinner_timeMode != null)
        {
            SolsticeEquinoxMode[] values = (allValues)
                    ? SolsticeEquinoxMode.values()
                    : SolsticeEquinoxMode.partialValues(false);

            EquinoxModeAdapter adapter = new EquinoxModeAdapter(this, R.layout.layout_listitem_oneline, values);
            adapter.setDropDownViewResource(R.layout.layout_listitem_one_line_colortab);
            adapter.setThemeValues(themeValues);
            spinner_timeMode.setAdapter(adapter);
            addOnItemSelectedListener(spinner_timeMode, null);
        }
    }

    @Override
    protected void initTimeMode( Context context )
    {
        initTimeModeAdapter(context, true);  //SolsticeWidgetSettings.loadWidgetBool(context, appWidgetId, PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER));

        if (button_timeModeHelp != null)
        {
            button_timeModeHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimeModeHelp();
                }
            });
            button_timeModeHelp.setVisibility(View.VISIBLE);
        }

        if (button_timeModeMenu != null) {
            button_timeModeMenu.setVisibility(View.GONE);
        }

        if (checkbox_timeModeOverride != null)
        {
            checkbox_timeModeOverride.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if (spinner_timeMode != null)
                    {
                        spinner_timeMode.setEnabled(!isChecked);
                    }
                }
            });
        }
    }

    @Override
    protected void showTimeModeHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_general2, getString(R.string.help_general_timeMode2), getString(R.string.help_general_timeMode2_1)));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    @Override
    protected void updateTimeModeAdapter(ContentValues themeValues)
    {
        if (spinner_timeMode != null)
        {
            EquinoxModeAdapter adapter = (EquinoxModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null)
            {
                SolsticeEquinoxMode selected = (SolsticeEquinoxMode) spinner_timeMode.getSelectedItem();
                adapter.setThemeValues(themeValues);
                spinner_timeMode.setAdapter(adapter);
                spinner_timeMode.setSelection(adapter.getPosition(selected));
            }
        }
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators()
    {
        return SuntimesCalculatorDescriptor.values(requiredFeatures);
    }
    private static final int[] requiredFeatures = new int[] { SuntimesCalculator.FEATURE_SOLSTICE };

    @Override
    protected void loadTimeMode(Context context)
    {
        if (spinner_timeMode != null) {
            EquinoxModeAdapter adapter = (EquinoxModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null) {
                SolsticeEquinoxMode timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
                spinner_timeMode.setSelection(adapter.getPosition(timeMode));
            }
        }
    }

    @Override
    protected void saveTimeMode(Context context, int appWidgetId)
    {
        if (spinner_timeMode != null) {
            EquinoxModeAdapter adapter = (EquinoxModeAdapter) spinner_timeMode.getAdapter();
            if (adapter != null) {
                SolsticeEquinoxMode timeMode = adapter.getItem(spinner_timeMode.getSelectedItemPosition());
                WidgetSettings.saveTimeMode2Pref(context, appWidgetId, ((timeMode != null) ? timeMode : WidgetSettings.PREF_DEF_GENERAL_TIMEMODE2));
            }
        }
    }

    public static final boolean DEF_SHOWTITLE = false;
    public static final String DEF_TITLETEXT = "%M";

    @Override
    protected void loadTitleSettings(Context context)
    {
        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId, DEF_SHOWTITLE);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);

        String titleText = WidgetSettings.loadTitleTextPref(context, appWidgetId, DEF_TITLETEXT);
        text_titleText.setText(titleText);
    }

    @Override
    protected void initWidgetModeLayout(Context context) {
    }

    public static final String TAG_FRAGMENT_MOREGENERALSETTINGS = "MoreGeneralSettings";

    @Override
    protected void initMoreGeneralSettings(final Context context)
    {
        SolsticeWidget0ConfigFragment fragment = new SolsticeWidget0ConfigFragment();
        fragment.setDialogListener(moreGeneralSettingsListener);
        loadMoreGeneralSettings(context, fragment);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.appwidget_general_moreOptions_fragmentContainer, fragment, TAG_FRAGMENT_MOREGENERALSETTINGS)
                .commit();
    }

    @Override
    protected void saveMoreGeneralSettings(final Context context, int appWidgetId)
    {
        SolsticeWidget0ConfigFragment fragment = (SolsticeWidget0ConfigFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MOREGENERALSETTINGS);
        if (fragment != null)
        {
            boolean showCrossQuarter = fragment.getWidgetBool(PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER);
            SolsticeWidgetSettings.saveWidgetValue(context, appWidgetId, PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, showCrossQuarter);
        }
    }

    @Override
    protected void loadMoreGeneralSettings(final Context context)
    {
        loadMoreGeneralSettings(context, (SolsticeWidget0ConfigFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MOREGENERALSETTINGS));
    }

    protected void loadMoreGeneralSettings(final Context context, @Nullable SolsticeWidget0ConfigFragment fragment)
    {
        if (fragment != null) {
            boolean showCrossQuarter = SolsticeWidgetSettings.loadWidgetBool(context, appWidgetId, PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER);
            fragment.setWidgetValue(PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, showCrossQuarter);
        }
    }

    private final SolsticeWidget0ConfigFragment.DialogListener moreGeneralSettingsListener = new SolsticeWidget0ConfigFragment.DialogListener()
    {
        @Override
        public void onChanged(SolsticeWidget0ConfigFragment dialog, String key)
        {
            /*if (PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER.equals(key))
            {
                boolean existingValue = SolsticeWidgetSettings.loadWidgetBool(SolsticeWidget0ConfigActivity.this, appWidgetId, PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER, PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER);
                initTimeModeAdapter(SolsticeWidget0ConfigActivity.this, dialog.getWidgetBool(key, existingValue));
            }*/
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        SolsticeWidget0ConfigFragment fragment = (SolsticeWidget0ConfigFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MOREGENERALSETTINGS);
        if (fragment != null) {
            fragment.setDialogListener(moreGeneralSettingsListener);
        }
    }

    /**
     * EquinoxModeAdapter
     */
    public static class EquinoxModeAdapter extends ModeAdapterBase<SolsticeEquinoxMode>
    {
        public EquinoxModeAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
        public EquinoxModeAdapter(@NonNull Context context, int resource, @NonNull SolsticeEquinoxMode[] objects) {
            super(context, resource, objects);
        }
        public EquinoxModeAdapter(@NonNull Context context, int resource, @NonNull List<SolsticeEquinoxMode> objects) {
            super(context, resource, objects);
        }

        @Override
        protected String getNameForMode(SolsticeEquinoxMode mode) {
            return mode.name();
        }

        @Override
        protected int getColorForMode(SolsticeEquinoxMode mode)
        {
            if (themeValues == null) {
                return Color.TRANSPARENT;
            }
            switch (mode)
            {
                case SOLSTICE_SUMMER: case CROSS_SUMMER: return themeValues.getAsInteger(SuntimesThemeContract.THEME_SUMMERCOLOR);
                case EQUINOX_AUTUMNAL: case CROSS_AUTUMN: return themeValues.getAsInteger(SuntimesThemeContract.THEME_FALLCOLOR);
                case SOLSTICE_WINTER: case CROSS_WINTER: return themeValues.getAsInteger(SuntimesThemeContract.THEME_WINTERCOLOR);
                case EQUINOX_SPRING: case CROSS_SPRING: default:
                return themeValues.getAsInteger(SuntimesThemeContract.THEME_SPRINGCOLOR);
            }
        }
    }

    @Override
    protected boolean supportsPreview() {
        return true;
    }

    @Override
    protected View createPreview(Context context, int appWidgetId, SuntimesWidget0.AppWidgetManagerView appWidgetManager)
    {
        SolsticeWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, defaultSolsticeLayout(context));
        return appWidgetManager.getView();
    }

    protected SolsticeLayout defaultSolsticeLayout(Context context) {
        return new SolsticeLayout_1x1_0();
    }

}
