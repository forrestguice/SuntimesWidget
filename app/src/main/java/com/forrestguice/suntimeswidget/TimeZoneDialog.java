/**
    Copyright (C) 2014-2023 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimes.support.app.DialogBase;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimes.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimes.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.view.ActionModeCompat;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class TimeZoneDialog extends BottomSheetDialogBase
{
    public static final String KEY_TIMEZONE_MODE = "timezoneMode";
    public static final String KEY_TIMEZONE_ID = "timezoneID";
    public static final String KEY_SOLARTIME_MODE = "solartimeMode";
    public static final String KEY_NOW = "paramNow";
    public static final String KEY_LONGITUDE = "paramLongitude";
    public static final String KEY_LONGITUDE_LABEL = "paramLongitudeLabel";
    public static final String KEY_TIMEFORMAT_MODE = "timeformatMode";

    private static final String DIALOGTAG_HELP = "timezone_help";
    public static final int HELP_PATH_ID = R.string.help_timezone_path;

    public static final String SLOT_CUSTOM0 = "custom0";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String customTimezoneID;

    private ImageButton btn_accept;
    private Spinner spinner_timezoneMode;

    private TextView preview_time;
    private TextView preview_time_suffix;
    private TextView preview_time_tz;

    private LinearLayout layout_timezone;
    private TextView label_timezone;
    private Spinner spinner_timezone;
    private ImageButton button_sort_timezones;
    private ProgressBar progress_timezone;

    private LinearLayout layout_solartime;
    private TextView label_solartime;
    private Spinner spinner_solartime;
    private Object actionMode = null;

    private View layout_timezoneExtras;
    private TextView label_tzExtras0;
    private SuntimesUtils utils;

    private ArrayAdapter<TimezoneMode> spinner_timezoneMode_adapter;
    private WidgetTimezones.TimeZoneItemAdapter spinner_timezone_adapter;
    private boolean loading = false;

    public TimeZoneDialog()
    {
        Bundle args = new Bundle();
        args.putString(KEY_TIMEFORMAT_MODE, TimeFormatMode.MODE_SYSTEM.name());
        setArguments(args);
    }

    private Calendar now = Calendar.getInstance();
    public void setNow( Calendar now )
    {
        this.now = now;
    }

    public void setLongitude( String label, double longitude )
    {
        getArgs().putDouble(KEY_LONGITUDE, longitude);
        getArgs().putString(KEY_LONGITUDE_LABEL, label);
        updatePreview(getActivity());
        onSelectionChanged();
    }
    public double getLongitude() {
        return getArgs().getDouble(KEY_LONGITUDE, 0);
    }
    public String getLongitudeLabel() {
        return getArgs().getString(KEY_LONGITUDE_LABEL);
    }

    public void setTimeFormatMode(TimeFormatMode mode) {
        getArgs().putString(KEY_TIMEFORMAT_MODE, mode.name());
        updatePreview(getActivity());
    }
    public TimeFormatMode getTimeFormatMode()
    {
        try {
            String mode = getArgs().getString(KEY_TIMEFORMAT_MODE);
            return ((mode != null) ? TimeFormatMode.valueOf(mode) : TimeFormatMode.MODE_SYSTEM);
        } catch (IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "getTimeFormatMode: " + e);
            return TimeFormatMode.MODE_SYSTEM;
        }
    }

    public TimezoneMode getTimeZoneMode() {
        return (spinner_timezoneMode != null) ? (TimezoneMode) spinner_timezoneMode.getSelectedItem() : TimezoneMode.CURRENT_TIMEZONE;
    }
    public void setTimeZoneMode(TimezoneMode value)
    {
        if (spinner_timezoneMode != null) {
            spinner_timezoneMode.setSelection(spinner_timezoneMode_adapter.getPosition(value), true);
        }
    }
    public void setCustomTimeZone(final String tzID)
    {
        if (spinner_timezoneMode != null) {
            spinner_timezoneMode.setSelection(spinner_timezoneMode_adapter.getPosition(TimezoneMode.CUSTOM_TIMEZONE), true);
        }
        spinner_timezone.post(new Runnable() {
            @Override
            public void run() {
                WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, tzID);
            }
        });
    }

    public WidgetTimezones.TimeZoneItemAdapter getTimeZoneItemAdapter() {
        return spinner_timezone_adapter;
    }

    public TimeZone getTimeZone()
    {
        String tzID;
        WidgetTimezones.TimeZoneItem item;
        TimezoneMode mode = getTimeZoneMode();
        switch (mode)
        {
            case SOLAR_TIME:
                item = (spinner_solartime != null) ? ((WidgetTimezones.TimeZoneItem) spinner_solartime.getSelectedItem()) : null;
                tzID = (item != null) ? item.getID() : TimeZone.getDefault().getID();
                return WidgetTimezones.getTimeZone(tzID, getLongitude(), calculator);

            case CUSTOM_TIMEZONE:
                item = (spinner_timezone != null) ? ((WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem()) : null;
                tzID = (item != null) ? item.getID() : TimeZone.getDefault().getID();
                return WidgetTimezones.getTimeZone(tzID, getLongitude(), calculator);

            case CURRENT_TIMEZONE:
            default: return TimeZone.getDefault();
        }
    }

    private SuntimesCalculator calculator = null;
    public void setCalculator( SuntimesCalculator calculator )
    {
        this.calculator = calculator;
    }

    private boolean hideHeader = false;
    private boolean hideFooter = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_timezone, parent, false);

        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        } else {
            loadSettings(getActivity());
        }

        View header = dialogContent.findViewById(R.id.dialog_header);
        if (header != null) {
            header.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
        }

        View footer = dialogContent.findViewById(R.id.dialog_footer);
        if (footer != null) {
            footer.setVisibility(hideFooter ? View.GONE : View.VISIBLE);
        }

        final Context myParent = getActivity();
        WidgetTimezones.TimeZoneSort sortZonesBy = AppSettings.loadTimeZoneSortPref(myParent);
        WidgetTimezones.TimeZonesLoadTask loadTask = new WidgetTimezones.TimeZonesLoadTask(myParent);
        loadTask.setListener(onTimeZonesLoaded);
        loadTask.execute(sortZonesBy);

        return dialogContent;
    }


    @SuppressWarnings({"RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState)
    {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs,R.styleable.TimeZoneConfigDialog);
        hideHeader = a.getBoolean(R.styleable.TimeZoneConfigDialog_hideHeader, hideHeader);
        hideFooter = a.getBoolean(R.styleable.TimeZoneConfigDialog_hideFooter, hideFooter);
        a.recycle();
    }

    /**
     * onSaveInstanceState
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        //Log.d("DEBUG", "TimeZoneDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews
     * @param context context
     * @param dialogContent view
     */
    protected void initViews( Context context, View dialogContent )
    {
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        utils = new SuntimesUtils();

        preview_time = (TextView) dialogContent.findViewById(R.id.text_time);
        preview_time_suffix = (TextView) dialogContent.findViewById(R.id.text_time_suffix);
        preview_time_tz = (TextView) dialogContent.findViewById(R.id.text_timezone);

        layout_timezone = (LinearLayout) dialogContent.findViewById(R.id.appwidget_timezone_custom_layout);
        label_timezone = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_custom_label);
        WidgetTimezones.TimeZoneSort.initDisplayStrings(context);

        spinner_timezoneMode_adapter = new ArrayAdapter<TimezoneMode>(context, R.layout.layout_listitem_oneline, TimezoneMode.values());
        spinner_timezoneMode_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner) dialogContent.findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneMode_adapter);
        spinner_timezoneMode.setOnItemSelectedListener(onTimeZoneModeSelected);

        View spinner_timezone_empty = dialogContent.findViewById(R.id.appwidget_timezone_custom_empty);
        label_timezone = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_custom_label);
        spinner_timezone = (Spinner) dialogContent.findViewById(R.id.appwidget_timezone_custom);

        spinner_timezone.setEmptyView(spinner_timezone_empty);
        spinner_timezone.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                return triggerTimeZoneActionMode(view);
            }
        });
        /*label_timezone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                triggerTimeZoneActionMode(view);
            }
        });
        label_timezone.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                return triggerTimeZoneActionMode(view);
            }
        });*/

        progress_timezone = (ProgressBar) dialogContent.findViewById(R.id.appwidget_timezone_progress);
        progress_timezone.setVisibility(View.GONE);

        layout_solartime = (LinearLayout) dialogContent.findViewById(R.id.appwidget_solartime_layout);
        label_solartime = (TextView) dialogContent.findViewById(R.id.appwidget_solartime_label);

        int c = 0;
        ArrayList<WidgetTimezones.TimeZoneItem> items = new ArrayList<>();
        for (SolarTimeMode value : SolarTimeMode.values()) {
            items.add(new WidgetTimezones.TimeZoneItem(value.getID(), value.getDisplayString(), c++));
        }
        WidgetTimezones.TimeZoneItemAdapter spinner_solartimeAdapter = new WidgetTimezones.TimeZoneItemAdapter(getActivity(), R.layout.layout_listitem_timezone, items, R.string.timezoneCustom_line1, R.string.timezoneCustom_line2b);

        spinner_solartime = (Spinner) dialogContent.findViewById(R.id.appwidget_solartime);
        spinner_solartime.setAdapter(spinner_solartimeAdapter);

        final ImageButton button_solartime_help = (ImageButton) dialogContent.findViewById(R.id.appwidget_solartime_help);
        button_solartime_help.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getString(R.string.help_general_solartime));
                helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
                helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
                helpDialog.show(getFragmentManager(), DIALOGTAG_HELP);
            }
        }));

        button_sort_timezones = (ImageButton) dialogContent.findViewById(R.id.sort_timezones);
        if (button_sort_timezones != null)
        {
            TooltipCompat.setTooltipText(button_sort_timezones, button_sort_timezones.getContentDescription());
            button_sort_timezones.setOnClickListener(new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //triggerTimeZoneActionMode(v);
                    showTimeZoneSortMenu(getContext(), v);
                }
            }));
        }

        layout_timezoneExtras = dialogContent.findViewById(R.id.appwidget_timezone_extrasgroup);
        label_tzExtras0 = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_extras0);

        ImageButton btn_cancel = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
        TooltipCompat.setTooltipText(btn_cancel, btn_cancel.getContentDescription());
        btn_cancel.setOnClickListener(onDialogCancelClick);
        if (AppSettings.isTelevision(context)) {
            btn_cancel.setFocusableInTouchMode(true);
        }

        btn_accept = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        TooltipCompat.setTooltipText(btn_accept, btn_accept.getContentDescription());
        btn_accept.setOnClickListener(onDialogAcceptClick);
    }

    protected void updatePreview(@Nullable Context context)
    {
        if (context != null && isAdded())
        {
            TimeZone tz = getTimeZone();
            TimeDisplayText timeDisplay = utils.calendarTimeShortDisplayString(context, Calendar.getInstance(tz), false, getTimeFormatMode());
            if (preview_time != null) {
                preview_time.setText(timeDisplay.getValue());
            }
            if (preview_time_suffix != null) {
                preview_time_suffix.setText(timeDisplay.getSuffix());
            }
            if (preview_time_tz != null) {
                preview_time_tz.setText(WidgetTimezones.getTimeZoneDisplay(context, tz));
            }
        }
    }

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (preview_time != null) {
            updateTask_isRunning = true;
            preview_time.post(updateTask);
        }
    }
    private void stopUpdateTask()
    {
        if (preview_time != null) {
            updateTask_isRunning = false;
            preview_time.removeCallbacks(updateTask);
        }
    }

    public static final int UPDATE_RATE = 3000;
    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            Activity context = getActivity();
            updatePreview(context);
            if (preview_time != null && context != null && updateTask_isRunning) {
                preview_time.postDelayed(this, UPDATE_RATE);
            }
        }
    };
    private boolean updateTask_isRunning = false;

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    private void updateExtrasLabel(@NonNull Context context, int stringResID, long offset)
    {
        int iconSize = (int) Math.ceil(context.getResources().getDimension(R.dimen.statusIcon_size));
        TimeDisplayText dstSavings = utils.timeDeltaLongDisplayString(0L, offset, false, false, true);
        ImageSpan dstIcon = SuntimesUtils.createDstSpan(context, iconSize, iconSize);
        String dstString = (dstSavings.getRawValue() < 0 ? "-" : "+") + dstSavings.getValue();
        String extrasString = getString(stringResID, dstString);

        SpannableStringBuilder extrasSpan = SuntimesUtils.createSpan(context, extrasString, SuntimesUtils.SPANTAG_DST, dstIcon);
        SpannableString boldedExtrasSpan = SuntimesUtils.createBoldSpan(SpannableString.valueOf(extrasSpan), extrasString, dstString);
        label_tzExtras0.setText(boldedExtrasSpan);
        layout_timezoneExtras.setVisibility(View.VISIBLE);
    }

    private void updateExtrasLabel(String text)
    {
        if (text == null)
        {
            layout_timezoneExtras.setVisibility(View.GONE);
            label_tzExtras0.setText("");

        } else {
            label_tzExtras0.setText(text);
            layout_timezoneExtras.setVisibility(View.VISIBLE);
        }
    }

    private void updateExtras(Context context, boolean solarTime, Object item0)
    {
        WidgetTimezones.TimeZoneItem item = (WidgetTimezones.TimeZoneItem)item0;
        if (solarTime)
        {
            if (item != null && item.getID().equals(SolarTimeMode.APPARENT_SOLAR_TIME.getID()))
            {
                int eot = TimeZones.ApparentSolarTime.equationOfTimeOffset(now.getTimeInMillis(), calculator);
                updateExtrasLabel(context, R.string.timezoneExtraApparentSolar, eot);
            } else updateExtrasLabel(null);

        } else {
            if (item != null)
            {
                TimeZone timezone = TimeZone.getTimeZone(item.getID());
                boolean usesDST = (Build.VERSION.SDK_INT < 24 ? timezone.useDaylightTime() : timezone.observesDaylightTime());
                boolean inDST = usesDST && timezone.inDaylightTime(now.getTime());
                if (inDST)
                    updateExtrasLabel(context, R.string.timezoneExtraDST, (long)timezone.getDSTSavings());
                else updateExtrasLabel(null);
            } else updateExtrasLabel(null);
        }
    }

    /**
     * onSolarTimeSelected
     */
    private final AdapterView.OnItemSelectedListener onSolarTimeSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Context context = getContext();
            if (layout_timezoneExtras != null && label_tzExtras0 != null && context != null) {
                updateExtras(context, true, parent.getItemAtPosition(position));
            }
            updatePreview(getActivity());
            onSelectionChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * onTimeZoneSelected
     */
    private final AdapterView.OnItemSelectedListener onTimeZoneSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Context context = getContext();
            if (layout_timezoneExtras != null && label_tzExtras0 != null && context != null) {
                updateExtras(context, false, parent.getItemAtPosition(position));
            }
            updatePreview(getActivity());
            onSelectionChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * onTimeZoneModeSelected
     */
    private final Spinner.OnItemSelectedListener onTimeZoneModeSelected = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            spinner_timezone.setOnItemSelectedListener(null);
            spinner_solartime.setOnItemSelectedListener(null);

            TimezoneMode timezoneMode = (TimezoneMode) parent.getSelectedItem();
            boolean useSolarTime = (timezoneMode == TimezoneMode.SOLAR_TIME);
            if (useSolarTime)
                spinner_solartime.setOnItemSelectedListener(onSolarTimeSelected);
            else spinner_timezone.setOnItemSelectedListener(onTimeZoneSelected);

            if (timezoneMode == TimezoneMode.CUSTOM_TIMEZONE) {
                customTimezoneID = WidgetSettings.loadTimezonePref(getContext(), appWidgetId, SLOT_CUSTOM0);
            }
            setUseCustomTimezone((timezoneMode == TimezoneMode.CUSTOM_TIMEZONE));
            setUseSolarTime((timezoneMode == TimezoneMode.SOLAR_TIME));

            Object item = (useSolarTime ? spinner_solartime.getSelectedItem() : spinner_timezone.getSelectedItem());
            updateExtras(getContext(), useSolarTime, item);

            SuntimesUtils.announceForAccessibility(spinner_timezoneMode, timezoneMode.getDisplayString());
            updatePreview(getActivity());
            onSelectionChanged();
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void setUseSolarTime( boolean value )
    {
        label_solartime.setEnabled(value);
        spinner_solartime.setEnabled(value);
        layout_solartime.setVisibility((value ? View.VISIBLE : View.GONE));
        layout_timezone.setVisibility((value ? View.GONE : View.VISIBLE));
    }

    private void setUseCustomTimezone( boolean value )
    {
        if (spinner_timezone_adapter != null)
        {
            String timezoneID = (value ? customTimezoneID : TimeZone.getDefault().getID());
            if (timezoneID != null)
            {
                spinner_timezone.setSelection(spinner_timezone_adapter.ordinal(timezoneID), true);
            }
        }
        label_timezone.setEnabled(value);
        spinner_timezone.setEnabled(value);
        button_sort_timezones.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    /**
     * showTimeZoneSortMenu
     */
    protected boolean showTimeZoneSortMenu(Context context, View view)
    {
        PopupMenuCompat.createMenu(context, view, R.menu.timezonesort, onTimeZoneSortMenuClick).show();
        return true;
    }
    public static void updateTimeZoneSortMenu(Context context, Menu menu)
    {
        MenuItem sortByOffset = menu.findItem(R.id.sortByOffset);
        MenuItem sortById = menu.findItem(R.id.sortById);

        switch (AppSettings.loadTimeZoneSortPref(context))
        {
            case SORT_BY_ID:
                setChecked(sortById, true);
                break;
            case SORT_BY_OFFSET:
                setChecked(sortByOffset, true);
                break;
        }
    }
    public static void setChecked(@Nullable MenuItem item, boolean value) {
        if (item != null) {
            item.setChecked(value);
        }
    }
    private final PopupMenuCompat.PopupMenuListener onTimeZoneSortMenuClick = new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            updateTimeZoneSortMenu(context, menu);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId())
            {
                case R.id.suggestTz:
                    setCustomTimeZone(timeZoneRecommendation(getLongitudeLabel(), getLongitude()));
                    return true;

                default:
                    return onSortItemClick(item);
            }
        }
        private boolean onSortItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            WidgetTimezones.TimeZoneSpinnerSortActionBase sortActionBase = new WidgetTimezones.TimeZoneSpinnerSortActionBase()
            {
                @Override
                public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
                {
                    super.onSortTimeZones(result, sortMode);
                    spinner_timezone_adapter = result;
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                    btn_accept.setEnabled(validateInput());
                    progress_timezone.setVisibility(View.GONE);
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.saveTimeZoneSortPref(context, sortMode);
                }
            };
            sortActionBase.init(context, spinner_timezone);
            return sortActionBase.onActionItemClicked(item.getItemId());
        }
    };

    /**
     * trigger the time zone ActionMode
     * @param view the view that is triggering the ActionMode
     * @return true ActionMode started, false otherwise
     */
    private boolean triggerTimeZoneActionMode(View view)
    {
        if (this.actionMode != null)
            return false;

        // ActionMode for HONEYCOMB (11) and above
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            Dialog dialog = getDialog();
            if (dialog == null)
                return false;

            Window window = dialog.getWindow();
            if (window == null)
                return false;

            View v = window.getDecorView();
            if (v == null)
                return false;

            ActionMode actionMode = v.startActionMode(new WidgetTimezones.TimeZoneSpinnerSortAction(getContext(), spinner_timezone)
            {
                @Override
                public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
                {
                    super.onSortTimeZones(result, sortMode);
                    spinner_timezone_adapter = result;
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                    btn_accept.setEnabled(validateInput());
                    progress_timezone.setVisibility(View.GONE);
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.saveTimeZoneSortPref(getContext(), sortMode);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode)
                {
                    super.onDestroyActionMode(mode);
                    TimeZoneDialog.this.actionMode = null;
                }
            });
            this.actionMode = actionMode;
            actionMode.setTitle(getString(R.string.timezone_sort_contextAction));

        } else {
            // LEGACY; ActionMode for pre HONEYCOMB
            ActionModeCompat actionMode = AppCompatActivity.startSupportActionMode(getActivity(), new WidgetTimezones.TimeZoneSpinnerSortActionCompat(getContext(), spinner_timezone)
            {
                @Override
                public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
                {
                    super.onSortTimeZones(result, sortMode);
                    spinner_timezone_adapter = result;
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                    btn_accept.setEnabled(validateInput());
                    progress_timezone.setVisibility(View.GONE);
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.saveTimeZoneSortPref(context, sortMode);
                }

                @Override
                public void onDestroyActionMode(ActionModeCompat mode)
                {
                    super.onDestroyActionMode(mode);
                    TimeZoneDialog.this.actionMode = null;
                }
            });
            if (actionMode != null)
            {
                this.actionMode = actionMode;
                actionMode.setTitle(getString(R.string.timezone_sort_contextAction));
            }
        }

        view.setSelected(true);
        return true;
    }

    /**
     * @return the appWidgetID used by this dialog when saving/loading prefs (use 0 for main app)
     */
    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
    }

    /**
     * Restore the dialog state from saved preferences currently used by the app.
     * @param context a context used to access shared prefs
     */
    protected void loadSettings(Context context)
    {
        TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        customTimezoneID = WidgetSettings.loadTimezonePref(context, appWidgetId, (timezoneMode == TimezoneMode.CUSTOM_TIMEZONE ? SLOT_CUSTOM0 : ""));
        String tzID = (getTimeZoneMode() == TimezoneMode.CURRENT_TIMEZONE ? TimeZone.getDefault().getID() : customTimezoneID);
        WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, tzID);

        SolarTimeMode solartimeMode = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
        spinner_solartime.setSelection(solartimeMode.ordinal());
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle a Bundle containing the dialog state
     */
    protected void loadSettings(Bundle bundle)
    {
        String modeString = bundle.getString(KEY_TIMEZONE_MODE);
        if (modeString != null)
        {
            TimezoneMode timezoneMode = TimezoneMode.valueOf(modeString);
            spinner_timezoneMode.setSelection(timezoneMode.ordinal());
        }

        customTimezoneID = bundle.getString(KEY_TIMEZONE_ID);
        if (customTimezoneID != null)
        {
            WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
        }

        String solarModeString = bundle.getString(KEY_SOLARTIME_MODE);
        if (solarModeString != null)
        {
            SolarTimeMode solartimeMode = SolarTimeMode.valueOf(solarModeString);
            spinner_solartime.setSelection(solartimeMode.ordinal());
        }

        long nowMillis = bundle.getLong(KEY_NOW, Calendar.getInstance().getTimeInMillis());
        now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);
    }

    /**
     * Save the dialog state to preferences to be used by the app (occurs on dialog accept).
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        final TimezoneMode[] timezoneModes = TimezoneMode.values();
        TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        String tzString;
        WidgetTimezones.TimeZoneItem tz = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        if (tz != null) {
            tzString = tz.getID();
        } else {
            tzString = TimeZone.getDefault().getID();
            Log.e("TimeZoneDialog", "Selected time zone is null; falling back to default.. " + tzString);
        }

        WidgetSettings.saveTimezonePref(context, appWidgetId, tzString);
        if (timezoneMode == TimezoneMode.CUSTOM_TIMEZONE) {
            WidgetSettings.saveTimezonePref(context, appWidgetId, tzString, SLOT_CUSTOM0);
        }

        // save: solar timemode
        SolarTimeMode[] solarTimeModes = SolarTimeMode.values();
        SolarTimeMode solarTimeMode = solarTimeModes[spinner_solartime.getSelectedItemPosition()];
        WidgetSettings.saveSolarTimeModePref(context, appWidgetId, solarTimeMode);
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle a bundle containing the dialog state
     */
    protected void saveSettings(Bundle bundle)
    {
        // save: timezone mode
        TimezoneMode[] timezoneModes = TimezoneMode.values();
        TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        bundle.putString(KEY_TIMEZONE_MODE, timezoneMode.name());

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        if (customTimezone != null)
        {
            bundle.putString(KEY_TIMEZONE_ID, customTimezone.getID());
        }

        // save: solar timemode
        SolarTimeMode[] solarTimeModes = SolarTimeMode.values();
        SolarTimeMode solarTimeMode = solarTimeModes[spinner_solartime.getSelectedItemPosition()];
        if (solarTimeMode != null)
        {
            bundle.putString(KEY_SOLARTIME_MODE, solarTimeMode.name());
        }

        // save: now
        if (now != null) {
            bundle.putLong(KEY_NOW, now.getTimeInMillis());
        }

        // save: timeformatmode
        bundle.putString(KEY_TIMEFORMAT_MODE, getTimeFormatMode().name());
    }

    /**
     * A listener that is triggered when the dialog is accepted.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * A listener that is triggered when the dialog is cancelled.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());

        HelpDialog helpDialog = (HelpDialog) getChildFragmentManager().findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        }
    }

    private final WidgetTimezones.TimeZonesLoadTaskListener onTimeZonesLoaded = new WidgetTimezones.TimeZonesLoadTaskListener()
    {
        @Override
        public void onStart()
        {
            super.onStart();
            btn_accept.setEnabled(false);
            progress_timezone.setVisibility(View.VISIBLE);
            spinner_timezone.setAdapter(new WidgetTimezones.TimeZoneItemAdapter(getActivity(), R.layout.layout_listitem_timezone));
        }

        @Override
        public void onFinished(WidgetTimezones.TimeZoneItemAdapter result)
        {
            super.onFinished(result);
            spinner_timezone_adapter = result;
            spinner_timezone.setAdapter(spinner_timezone_adapter);
            String tzID = (getTimeZoneMode() == TimezoneMode.CURRENT_TIMEZONE ? TimeZone.getDefault().getID() : customTimezoneID);
            WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, tzID);
            btn_accept.setEnabled(validateInput());
            progress_timezone.setVisibility(View.GONE);
            startUpdateTask();

            if (AppSettings.isTelevision(getActivity())) {
                btn_accept.requestFocus();
            }
        }
    };

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialog) {
            BottomSheetDialogBase.initPeekHeight(dialog, R.id.dialog_footer);
        }
    };

    private final View.OnClickListener onDialogCancelClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    });

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    private final View.OnClickListener onDialogAcceptClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (validateInput())
            {
                saveSettings(getContext());
                dismiss();
                if (onAccepted != null) {
                    onAccepted.onClick(getDialog(), 0);
                }
            }
        }
    });

    private boolean validateInput()
    {
        if (spinner_timezone.getSelectedItem() == null) {
            return false;
        }
        return true;
    }

    protected void onSelectionChanged()
    {
        if (dialogListener != null) {
            dialogListener.onSelectionChanged(getTimeZone());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        DialogBase.disableTouchOutsideBehavior(getDialog());
    }

    public String timeZoneRecommendation(String label, double longitude)
    {
        Calendar now = Calendar.getInstance();
        Log.d("DEBUG", "longitude label: " + label);

        boolean foundItem = false;
        String tzID = WidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;
        WidgetTimezones.TimeZoneItemAdapter adapter = getTimeZoneItemAdapter();
        WidgetTimezones.TimeZoneItem[] recommendations = null;
        if (adapter != null)
        {
            if (label != null)
            {
                ArrayList<String> labels = new ArrayList<>();
                String label0 = Normalizer.normalize(label, Normalizer.Form.NFD);    // isolate all accents/glyphs
                label0 = label0.replaceAll("\\p{M}", "");          // and remove them; e.g. Rīga -> Riga
                String label1 = label0.replaceAll(",", "").replaceAll("\\.", "").replaceAll(":", "").replaceAll(";", "");

                labels.add(label1.trim().replaceAll(" ", "_"));    // 0, entire label
                labels.add(label1.replaceAll("City", "")           // 1, omit "City", e.g. Panama City, New York City, Guatemala City, etc
                        .trim().replaceAll(" ", "_"));

                int i_comma = label0.lastIndexOf(",");
                if (i_comma >= 0)
                {
                    String left = label0.substring(i_comma).replaceAll(",", "").replaceAll("\\.", "").replaceAll(":", "").replaceAll(";", "")
                            .trim().replaceAll(" ", "_");
                    if (left.length() >= 4) {
                        labels.add(left);    // 2, comma (left side)
                    }

                    String right = label0.substring(0, i_comma).replaceAll(",", "").replaceAll("\\.", "").replaceAll(":", "").replaceAll(";", "")
                            .trim().replaceAll(" ", "_");
                    if (right.length() >= 4) {
                        labels.add(right);    // 3, comma (right side)
                    }
                }

                WidgetTimezones.TimeZoneItem[] items = adapter.values();
                outer_loop:
                for (int i=0; i<labels.size(); i++)
                {
                    for (WidgetTimezones.TimeZoneItem item : items)
                    {
                        if (item.getID().endsWith(labels.get(i)))
                        {
                            tzID = item.getID();
                            foundItem = true;
                            break outer_loop;
                        }
                    }
                }
            }

            if (!foundItem) {
                recommendations = adapter.findItems(longitude);
            }
        }

        if (!foundItem)
        {
            tzID = WidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;
            TimeZone tz = WidgetTimezones.getTimeZone(tzID, longitude, null);  // TODO: calculator
            if (WidgetTimezones.isProbablyNotLocal(tz, longitude, now.getTime()))
            {
                if (recommendations != null && recommendations[0] != null)
                {
                    tzID = recommendations[0].getID();
                    double offsetHr = recommendations[0].getRawOffsetHr();
                    if (offsetHr == 0) {
                        tzID = "Etc/GMT";

                    } else {
                        for (int i=0; i<recommendations.length; i++)
                        {
                            String recommendation = recommendations[i].getID();
                            if (recommendation.startsWith("Etc/GMT"))
                            {
                                tzID = recommendations[i].getID();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return tzID;
    }

    private TimeZoneDialogListener dialogListener = null;
    public void setDialogListener(TimeZoneDialogListener listener) {
        dialogListener = listener;
    }

    /**
     * TimeZoneDialogListener
     */
    public static class TimeZoneDialogListener
    {
        public void onSelectionChanged(TimeZone tz) {}
    }

}
