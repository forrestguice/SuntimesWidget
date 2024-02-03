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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesConfigActivity0;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;

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

        hideOption1x1LayoutMode();
        hideLayoutSettings();
        hideTimeZoneSettings();
        hideLocationSettings();

        //moveSectionToTop(R.id.appwidget_general_layout);
    }

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
        return SuntimesCalculatorDescriptor.values(this, requiredFeatures);
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

}
