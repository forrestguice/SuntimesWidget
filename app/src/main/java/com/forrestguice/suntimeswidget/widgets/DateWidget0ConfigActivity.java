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
    protected Spinner spinner_calendarFormat;
    protected EditText text_calendarFormatPattern;
    protected ImageButton button_calendarFormatPatternHelp;
    protected ImageButton button_calendarFormatEdit;

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
        showCalendarFormatPattern(true);
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
        spinner_calendarMode = (Spinner) findViewById(R.id.appwidget_general_calendarMode);
        text_calendarFormatPattern = (EditText) findViewById(R.id.appwidget_general_calendarPattern);
        button_calendarFormatEdit = (ImageButton) findViewById(R.id.appwidget_general_calendarFormat_editButton);
        button_calendarFormatPatternHelp = (ImageButton) findViewById(R.id.appwidget_general_calendarPattern_helpButton);
        spinner_calendarFormat = (Spinner) findViewById(R.id.appwidget_general_calendarFormat);

        if (spinner_calendarMode != null)
        {
            final ArrayAdapter<CalendarMode> adapter = new ArrayAdapter<CalendarMode>(this, R.layout.layout_listitem_oneline, CalendarMode.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_calendarMode.setAdapter(adapter);
            spinner_calendarMode.setOnItemSelectedListener(onCalendarModeSelected);
        }

        if (spinner_calendarFormat != null)
        {
            final ArrayAdapter<CalendarFormat> adapter = new ArrayAdapter<CalendarFormat>(this, R.layout.layout_listitem_oneline, CalendarFormat.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_calendarFormat.setAdapter(adapter);
            spinner_calendarFormat.setOnItemSelectedListener(onCalendarFormatSelected);
        }

        if (text_calendarFormatPattern != null) {
            text_calendarFormatPattern.setImeOptions(EditorInfo.IME_ACTION_DONE);
            text_calendarFormatPattern.setOnEditorActionListener(onCalendarFormatPatternEdited);
            text_calendarFormatPattern.setOnFocusChangeListener(onCalendarFormatPatternFocus);
        }

        if (button_calendarFormatEdit != null) {
            button_calendarFormatEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCalendarFormat(CalendarFormat.CUSTOM);
                }
            });
        }

        if (button_calendarFormatPatternHelp != null)
        {
            button_calendarFormatPatternHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_general_calendarFormatPattern));
                    helpDialog.setNeutralButtonListener(onCalendarFormatPatternHelpRestoreDefaults, "calendarFormatPattern");
                    helpDialog.setShowNeutralButton(context.getString(R.string.configAction_restoreDefaults));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }
    }

    protected void notifyDataSetChanged_calendarFormatAdapter()
    {
        try {
            ArrayAdapter<CalendarFormat> adapter = (ArrayAdapter<CalendarFormat>) spinner_calendarFormat.getAdapter();
            adapter.notifyDataSetChanged();
        } catch (ClassCastException e) {
            Log.e(getClass().getSimpleName(), "Failed to update calendar format adapter: " + e);
        }
    }

    protected void updateCustomCalendarFormat(String pattern) {
        CalendarMode mode = (CalendarMode) spinner_calendarMode.getSelectedItem();
        CalendarFormat.CUSTOM.setPattern(pattern);
        CalendarFormat.CUSTOM.initDisplayString(this, mode, Calendar.getInstance());
        notifyDataSetChanged_calendarFormatAdapter();
    }

    private final AdapterView.OnItemSelectedListener onCalendarModeSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            CalendarMode mode = (CalendarMode) spinner_calendarMode.getItemAtPosition(position);
            String pattern = CalendarSettings.loadCalendarFormatPatternPref(DateWidget0ConfigActivity.this, appWidgetId, mode.name());
            text_calendarFormatPattern.setText(pattern);
            setCalendarFormat(pattern);

            CalendarFormat.CUSTOM.setPattern(pattern);
            CalendarFormat.initDisplayStrings(DateWidget0ConfigActivity.this, mode, Calendar.getInstance());
            notifyDataSetChanged_calendarFormatAdapter();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private final AdapterView.OnItemSelectedListener onCalendarFormatSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            CalendarFormat item = (CalendarFormat)parent.getItemAtPosition(position);
            text_calendarFormatPattern.setEnabled(item == CalendarFormat.CUSTOM);
            button_calendarFormatEdit.setVisibility(item == CalendarFormat.CUSTOM ? View.INVISIBLE : View.VISIBLE);

            if (item != CalendarFormat.CUSTOM) {
                text_calendarFormatPattern.setText(item.getPattern());
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private final TextView.OnEditorActionListener onCalendarFormatPatternEdited = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE: case EditorInfo.IME_ACTION_NEXT: case EditorInfo.IME_ACTION_PREVIOUS:
                case EditorInfo.IME_ACTION_SEARCH: case EditorInfo.IME_ACTION_GO: case EditorInfo.IME_ACTION_SEND:
                    updateCustomCalendarFormat(v.getText().toString());
                    break;
            }
            return false;
        }
    };
    private final View.OnFocusChangeListener onCalendarFormatPatternFocus =  new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus && v.isEnabled()) {
                updateCustomCalendarFormat(text_calendarFormatPattern.getText().toString());
            }
        }
    };
    private final View.OnClickListener onCalendarFormatPatternHelpRestoreDefaults = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissHelpDialog();
            CalendarMode mode = (CalendarMode) spinner_calendarMode.getSelectedItem();
            String pattern = mode.getDefaultPattern();
            text_calendarFormatPattern.setText(pattern);
            updateCustomCalendarFormat(pattern);
            setCalendarFormat(pattern);
        }
    };

    @Override
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

    @Override
    protected int setCalendarFormat(@NonNull CalendarFormat format)
    {
        if (spinner_calendarFormat != null) {
            SpinnerAdapter adapter = spinner_calendarFormat.getAdapter();
            int n = (adapter != null ? adapter.getCount() : 0);
            for (int i=0; i<n; i++) {
                CalendarFormat item = (CalendarFormat) adapter.getItem(i);
                if (format.equals(item)) {
                    spinner_calendarFormat.setSelection(i);
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected int setCalendarFormat(@NonNull String pattern)
    {
        if (spinner_calendarFormat != null) {
            SpinnerAdapter adapter = spinner_calendarFormat.getAdapter();
            int n = (adapter != null ? adapter.getCount() : 0);
            for (int i=n-1; i>=0; i--) {    // CUSTOM (0) should be considered last
                CalendarFormat item = (CalendarFormat) adapter.getItem(i);
                if (pattern.equals(item.getPattern())) {
                    spinner_calendarFormat.setSelection(i);
                    return i;
                }
            }
            setCalendarFormat(CalendarFormat.CUSTOM);
        }
        return -1;
    }

    @Override
    protected void saveCalendarSettings(Context context)
    {
        super.saveCalendarSettings(context);

        // save: calendar mode
        CalendarMode calendarMode = (CalendarMode) spinner_calendarMode.getSelectedItem();
        CalendarSettings.saveCalendarModePref(context, appWidgetId, calendarMode);

        // save: calendar format pattern
        String calendarPattern = text_calendarFormatPattern.getText().toString();
        CalendarSettings.saveCalendarFormatPatternPref(context, appWidgetId, calendarMode.name(), calendarPattern);
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
