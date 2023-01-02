/**
    Copyright (C) 2022 Forrest Guice
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
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesConfigActivity0;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calendar.CalendarFormat;
import com.forrestguice.suntimeswidget.calendar.CalendarFormatDialog;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;

import java.util.Calendar;

/**
 * Date widget config activity.
 */
@SuppressWarnings("Convert2Diamond")
public class DateWidget0ConfigActivity extends SuntimesConfigActivity0
{
    protected Spinner spinner_calendarMode;

    public DateWidget0ConfigActivity() {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_datewidget0));

        showCalendarMode(true);
        showCalendarFormat(true);
        showOptionLabels(true);

        showOptionShowDate(false);    // always true
        showTimeFormatMode(false);
        showOptionRiseSetOrder(false);
        hideOptionUseAltitude();
        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        showOptionWeeks(false);
        showOptionHours(false);
        showOptionTimeDate(false);
        hideOptionShowSeconds();
        showOptionTrackingMode(false);
        showOptionTimeModeOverride(false);
        showDataSource(false);
        showTimeMode(false);
        showOptionShowNoon(false);

        hideLayoutSettings();

        moveSectionToTop(R.id.appwidget_general_layout);
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, DateWidget0.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);
    }

    @Override
    protected int getAboutIconID() {
        return R.drawable.ic_calendar;
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
        intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_DATE_1x1);
        return intent;
    }

    @Override
    protected void initCalendarMode(final Context context)
    {
        CalendarMode mode = CalendarSettings.loadCalendarModePref(DateWidget0ConfigActivity.this, appWidgetId);
        String pattern = CalendarSettings.loadCalendarFormatPatternPref(DateWidget0ConfigActivity.this, appWidgetId, mode.name());

        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null)
        {
            FragmentTransaction transaction = fragments.beginTransaction();
            CalendarFormatDialog fragment = new CalendarFormatDialog();
            fragment.setCalendarMode(mode);
            fragment.setFormatPattern(pattern);
            fragment.updateCustomCalendarFormat(pattern);
            fragment.setDialogListener(calendarFormatDialogListener);
            transaction.replace(R.id.appwidget_general_calendarFormat_fragmentContainer, fragment, "CalendarFormatDialog");
            transaction.commit();
        }

        spinner_calendarMode = (Spinner) findViewById(R.id.appwidget_general_calendarMode);
        if (spinner_calendarMode != null)
        {
            final ArrayAdapter<CalendarMode> adapter = new ArrayAdapter<CalendarMode>(this, R.layout.layout_listitem_oneline, CalendarMode.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_calendarMode.setAdapter(adapter);
            spinner_calendarMode.setOnItemSelectedListener(onCalendarModeSelected);
        }
    }

    private final CalendarFormatDialog.DialogListener calendarFormatDialogListener = new CalendarFormatDialog.DialogListener()
    {
        @Override
        public void onChanged(CalendarFormatDialog dialog) {}

        @Override
        public void onEditClick(CalendarFormatDialog dialog) {}

        @Override
        public void onHelpClick(CalendarFormatDialog dialog)
        {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.setContent(getString(R.string.help_general_calendarFormatPattern));
            helpDialog.setNeutralButtonListener(onCalendarFormatPatternHelpRestoreDefaults, "calendarFormatPattern");
            helpDialog.setShowNeutralButton(getString(R.string.configAction_restoreDefaults));
            helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
        }
    };

    private final AdapterView.OnItemSelectedListener onCalendarModeSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            CalendarMode mode = (CalendarMode) spinner_calendarMode.getItemAtPosition(position);
            String pattern = CalendarSettings.loadCalendarFormatPatternPref(DateWidget0ConfigActivity.this, appWidgetId, mode.name());

            FragmentManager fragments = getSupportFragmentManager();
            if (fragments != null)
            {
                CalendarFormatDialog calendarFormat = (CalendarFormatDialog) fragments.findFragmentByTag("CalendarFormatDialog");
                if (calendarFormat != null)
                {
                    CalendarFormat.initDisplayStrings(DateWidget0ConfigActivity.this, mode, Calendar.getInstance());
                    calendarFormat.setCalendarMode(mode);
                    calendarFormat.setFormatPattern(pattern);
                    calendarFormat.updateCustomCalendarFormat(pattern);
                }
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private final View.OnClickListener onCalendarFormatPatternHelpRestoreDefaults = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissHelpDialog();
            CalendarMode mode = (CalendarMode) spinner_calendarMode.getSelectedItem();
            setCalendarFormat(mode.getDefaultPattern());
        }
    };

    protected int setCalendarMode(@NonNull CalendarMode mode)
    {
        if (spinner_calendarMode != null) {
            SpinnerAdapter adapter = spinner_calendarMode.getAdapter();
            int n = (adapter != null ? adapter.getCount() : 0);
            for (int i=0; i<n; i++) {
                CalendarMode item = (CalendarMode) adapter.getItem(i);
                if (mode.equals(item)) {
                    spinner_calendarMode.setSelection(i);
                    return i;
                }
            }
        }
        return -1;
    }

    protected void setCalendarFormat(@NonNull String pattern)
    {
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null) {
            CalendarFormatDialog calendarFormat = (CalendarFormatDialog) fragments.findFragmentByTag("CalendarFormatDialog");
            if (calendarFormat != null) {
                calendarFormat.setFormatPattern(pattern);
            }
        }
    }

    @Override
    protected void saveCalendarSettings(Context context)
    {
        super.saveCalendarSettings(context);

        // save: calendar mode
        CalendarMode calendarMode = (CalendarMode) spinner_calendarMode.getSelectedItem();
        CalendarSettings.saveCalendarModePref(context, appWidgetId, calendarMode);

        // save: calendar format pattern
        FragmentManager fragments = getSupportFragmentManager();
        if (fragments != null) {
            CalendarFormatDialog calendarFormatDialog = (CalendarFormatDialog) fragments.findFragmentByTag("CalendarFormatDialog");
            if (calendarFormatDialog != null) {
                CalendarSettings.saveCalendarFormatPatternPref(context, appWidgetId, calendarMode.name(), calendarFormatDialog.getFormatPattern());
            }
        }
    }

    @Override
    protected void loadCalendarSettings(Context context)
    {
        super.loadCalendarSettings(context);

        CalendarMode calendarMode = CalendarSettings.loadCalendarModePref(context, appWidgetId);
        setCalendarMode(calendarMode);
        setCalendarFormat(CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, calendarMode.name()));
    }
}
