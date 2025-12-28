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

package com.forrestguice.suntimeswidget.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesConfigActivity0;
import com.forrestguice.suntimeswidget.SuntimesWidget0;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.MODE_2x2;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.MODE_3x2;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_TYPES;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_TYPES;

/**
 * Alarm widget config activity.
 */
@SuppressWarnings("Convert2Diamond")
public class AlarmWidget0ConfigActivity extends SuntimesConfigActivity0
{
    public AlarmWidget0ConfigActivity() {
        super();
    }

    @Override
    protected Class getWidgetClass() {
        return AlarmWidget0.class;
    }

    @Override
    protected WidgetSettings.ActionMode defaultActionMode() {
        return WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY;
    }

    @Override
    protected ContentValues launchActionIntentDefaults() {
        return WidgetActions.SuntimesAction.OPEN_ALARM_LIST.toContentValues();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.app_name_alarmwidget0));

        showOptionLabels(true);
        showTimeFormatMode(true);
        showMoreGeneralSettings(true);

        showCalendarMode(false);
        showCalendarFormat(false);
        showOptionShowDate(false);

        showTimeMode(false);
        showOptionRiseSetOrder(false);
        hideOptionUseAltitude();
        hideOptionCompareAgainst();
        showOptionWeeks(false);
        showOptionHours(false);
        showOptionTimeDate(false);
        hideOptionShowSeconds();
        showOptionTrackingMode(false);
        showOptionTimeModeOverride(false);
        showDataSource(false);
        showOptionShowNoon(false);

        hideTimeZoneSettings();
        hideLocationSettings();

        //moveSectionToTop(R.id.appwidget_general_layout);
    }

    public static final String TAG_FRAGMENT_MOREGENERALSETTINGS = "MoreGeneralSettings";

    @Override
    protected void initMoreGeneralSettings(final Context context)
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null)
        {
            FragmentTransaction transaction = fragments.beginTransaction();
            AlarmWidget0ConfigFragment fragment = new AlarmWidget0ConfigFragment();
            loadMoreGeneralSettings(context, fragment);
            transaction.replace(R.id.appwidget_general_moreOptions_fragmentContainer, fragment, TAG_FRAGMENT_MOREGENERALSETTINGS);
            transaction.commit();
        }
    }

    @Override
    protected void saveMoreGeneralSettings(final Context context, int appWidgetId)
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null)
        {
            AlarmWidget0ConfigFragment fragment = (AlarmWidget0ConfigFragment) fragments.findFragmentByTag(TAG_FRAGMENT_MOREGENERALSETTINGS);
            if (fragment != null)
            {
                int sortOrder = fragment.getAlarmWidgetInt(PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER);
                boolean enabledOnly = fragment.getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_ENABLEDONLY, PREF_DEF_ALARMWIDGET_ENABLEDONLY);
                boolean showIcons = fragment.getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
                Set<String> filterTypes = new TreeSet<String>(Arrays.asList(fragment.getAlarmWidgetStringSet(PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES)));

                AlarmWidgetSettings.saveAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_SORTORDER, sortOrder);
                AlarmWidgetSettings.saveAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_ENABLEDONLY, enabledOnly);
                AlarmWidgetSettings.saveAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_SHOWICONS, showIcons);
                AlarmWidgetSettings.saveAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_TYPES, filterTypes);
            }
        }
    }

    @Override
    protected void loadMoreGeneralSettings(final Context context)
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null) {
            loadMoreGeneralSettings(context, (AlarmWidget0ConfigFragment) fragments.findFragmentByTag(TAG_FRAGMENT_MOREGENERALSETTINGS));
        }
    }

    protected void loadMoreGeneralSettings(final Context context, @Nullable AlarmWidget0ConfigFragment fragment)
    {
        Set<String> filterTypes = AlarmWidgetSettings.loadAlarmWidgetStringSet(context, appWidgetId, PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES);
        int sortOrder = AlarmWidgetSettings.loadAlarmWidgetInt(context, appWidgetId, PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER);
        boolean enabledOnly = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetId, PREF_KEY_ALARMWIDGET_ENABLEDONLY, PREF_DEF_ALARMWIDGET_ENABLEDONLY);
        boolean showIcons = AlarmWidgetSettings.loadAlarmWidgetBool(context, appWidgetId, PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);

        if (fragment != null)
        {
            //Log.d("DEBUG", "load fragment settings2");
            fragment.setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_TYPES, filterTypes.toArray(new String[0]));
            fragment.setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_SORTORDER, sortOrder);
            fragment.setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_ENABLEDONLY, enabledOnly);
            fragment.setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_SHOWICONS, showIcons);
        }
    }
    
    @Override
    protected void initWidgetModeLayout(Context context)
    {
        super.initWidgetModeLayout(context);
        showOption2x2LayoutMode(true);
        showOption3x2LayoutMode(true);
        showOption2x1LayoutMode(false);
        showOption3x1LayoutMode(false);
    }

    /**
     * Mode 1x1
     */

    @Override
    protected void initWidgetMode1x1(Context context)
    {
        if (spinner_1x1mode != null) {
            spinner_1x1mode.setAdapter(createAdapter_widgetMode1x1());
            addOnItemSelectedListener(spinner_1x1mode, null);
        }
    }
    @Override
    protected void saveWidgetMode1x1(Context context, int appWidgetId)
    {
        if (spinner_1x1mode != null)
        {
            final AlarmWidgetSettings.WidgetModeAlarm1x1[] modes = AlarmWidgetSettings.WidgetModeAlarm1x1.values();
            AlarmWidgetSettings.WidgetModeAlarm1x1 mode = modes[spinner_1x1mode.getSelectedItemPosition()];
            AlarmWidgetSettings.saveAlarmModePref(context, appWidgetId, mode.name(), AlarmWidgetSettings.MODE_1x1);
        }
    }
    @Override
    protected void loadWidgetMode1x1(Context context)
    {
        AlarmWidgetSettings.WidgetModeAlarm1x1 mode1x1 = AlarmWidgetSettings.loadAlarm1x1ModePref(context, appWidgetId);
        if (spinner_1x1mode != null)
        {
            int p = searchForIndex(spinner_1x1mode, mode1x1);
            if (p >= 0) {
                spinner_1x1mode.setSelection(mode1x1.ordinal());
            }
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode1x1()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, AlarmWidgetSettings.WidgetModeAlarm1x1.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    /**
     *  Mode 2x2
     */

    @Override
    protected void initWidgetMode2x2(Context context)
    {
        if (spinner_2x2mode != null) {
            spinner_2x2mode.setAdapter(createAdapter_widgetMode2x2());
            addOnItemSelectedListener(spinner_2x2mode, null);
        }
    }
    @Override
    protected void saveWidgetMode2x2(Context context, int appWidgetId)
    {
        if (spinner_2x2mode != null)
        {
            final AlarmWidgetSettings.WidgetModeAlarm2x2[] modes = AlarmWidgetSettings.WidgetModeAlarm2x2.values();
            AlarmWidgetSettings.WidgetModeAlarm2x2 mode = modes[spinner_2x2mode.getSelectedItemPosition()];
            AlarmWidgetSettings.saveAlarmModePref(context, appWidgetId, mode.name(), MODE_2x2);
        }
    }
    @Override
    protected void loadWidgetMode2x2(Context context)
    {
        AlarmWidgetSettings.WidgetModeAlarm2x2 mode2x2 = AlarmWidgetSettings.loadAlarm2x2ModePref(context, appWidgetId);
        if (spinner_2x2mode != null)
        {
            int p = searchForIndex(spinner_2x2mode, mode2x2);
            if (p >= 0) {
                spinner_2x2mode.setSelection(p);
            }
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode2x2()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, AlarmWidgetSettings.WidgetModeAlarm2x2.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }

    /**
     * Mode 3x2
     */

    @Override
    protected void initWidgetMode3x2(Context context)
    {
        if (spinner_3x2mode != null) {
            spinner_3x2mode.setAdapter(createAdapter_widgetMode3x2());
            addOnItemSelectedListener(spinner_3x2mode, null);
        }
    }
    protected WidgetModeAdapter createAdapter_widgetMode3x2()
    {
        WidgetModeAdapter adapter = new WidgetModeAdapter(this, R.layout.layout_listitem_oneline, AlarmWidgetSettings.WidgetModeAlarm3x2.values());
        adapter.setDropDownViewResource(R.layout.layout_listitem_layouts);
        adapter.setThemeValues(themeValues);
        return adapter;
    }
    @Override
    protected void saveWidgetMode3x2(Context context, int appWidgetId)
    {
        if (spinner_3x2mode != null)
        {
            final AlarmWidgetSettings.WidgetModeAlarm3x2[] modes = AlarmWidgetSettings.WidgetModeAlarm3x2.values();
            AlarmWidgetSettings.WidgetModeAlarm3x2 mode = modes[spinner_3x2mode.getSelectedItemPosition()];
            AlarmWidgetSettings.saveAlarmModePref(context, appWidgetId, mode.name(), MODE_3x2);
            //Log.d("DEBUG", "Saved mode: " + mode.name());
        }
    }
    @Override
    protected void loadWidgetMode3x2(Context context)
    {
        AlarmWidgetSettings.WidgetModeAlarm3x2 mode3x2 = AlarmWidgetSettings.loadAlarm3x2ModePref(context, appWidgetId);
        if (spinner_3x2mode != null)
        {
            int p = searchForIndex(spinner_3x2mode, mode3x2);
            if (p >= 0) {
                spinner_3x2mode.setSelection(mode3x2.ordinal());
            }
        }
    }

    protected static int searchForIndex(Spinner spinner, Object enumValue)
    {
        for (int i=0; i<spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).equals(enumValue)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * updateWidgets
     */
    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);
    }

    @Override
    protected int getAboutIconID() {
        return R.drawable.ic_suntimesalarms;
    }

    @Override
    protected SuntimesCalculatorDescriptor[] supportingCalculators() {
        return SuntimesCalculatorDescriptor.values(requiredFeatures);
    }
    private static int[] requiredFeatures = new int[] {};

    @Override
    protected boolean getDefaultScaleText() {
        return true;
    }

    @Override
    protected Intent themeEditorIntent(Context context)
    {
        Intent intent = super.themeEditorIntent(context);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_ALARM_1x1);
        return intent;
    }

    @Override
    protected TextView getPrimaryWidgetModeLabel() {
        return label_1x1mode;
    }

    @Override
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode };
    }

    @Override
    protected View[] getSecondaryWidgetModeViews() {
        return new View[] { label_2x2mode, spinner_2x2mode, label_3x2mode, spinner_3x2mode };
    }

    @Override
    protected boolean supportsPreview() {
        return true;
    }

    @Override
    protected View createPreview(final Context context, int appWidgetId, SuntimesWidget0.AppWidgetManagerView appWidgetManager)
    {
        int[] defaultSizePx = getWidgetSizeConstraints(context, getPrimaryWidgetModeSize());
        AlarmWidget0.updateAppWidget(context, appWidgetManager, appWidgetId, getWidgetClass(), defaultSizePx, defaultAlarmLayout(context, appWidgetId));
        return appWidgetManager.getView();
    }

    protected AlarmLayout defaultAlarmLayout(Context context, int appWidgetId) {
        return AlarmWidgetSettings.loadAlarm1x1ModePref_asLayout(context, appWidgetId);
    }

}
