/**
    Copyright (C) 2014-2018 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class TimeZoneDialog extends DialogFragment
{
    public static final String KEY_TIMEZONE_MODE = "timezoneMode";
    public static final String KEY_TIMEZONE_ID = "timezoneID";
    public static final String KEY_SOLARTIME_MODE = "solartimeMode";
    public static final String KEY_NOW = "paramNow";
    public static final String KEY_LONGITUDE = "paramLongitude";

    private static final String DIALOGTAG_HELP = "timezone_help";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String customTimezoneID;

    private Spinner spinner_timezoneMode;

    private LinearLayout layout_timezone;
    private TextView label_timezone;
    private Spinner spinner_timezone;

    private LinearLayout layout_solartime;
    private TextView label_solartime;
    private Spinner spinner_solartime;
    private ImageButton button_solartime_help;
    private Object actionMode = null;

    private View layout_timezoneExtras;
    private TextView label_tzExtras0;
    private SuntimesUtils utils;

    private WidgetTimezones.TimeZoneItemAdapter spinner_timezone_adapter;
    private boolean loading = false;

    private Calendar now = Calendar.getInstance();
    public void setNow( Calendar now )
    {
        this.now = now;
    }

    private double longitude = 0;
    public void setLongitude( double longitude )
    {
        this.longitude = longitude;
    }

    private SuntimesCalculator calculator = null;
    public void setCalculator( SuntimesCalculator calculator )
    {
        this.calculator = calculator;
    }

    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_timezone, null);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent, 0, padding, 0, 0);
        builder.setTitle(myParent.getString(R.string.timezone_dialog_title));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.timezone_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        if (onCanceled != null)
                        {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.timezone_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        saveSettings(myParent);
                        dialog.dismiss();

                        if (onAccepted != null)
                        {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        initViews(myParent, dialogContent);
        WidgetTimezones.TimeZoneSort sortZonesBy = AppSettings.loadTimeZoneSortPref(myParent);
        WidgetTimezones.TimeZonesLoadTask loadTask = new WidgetTimezones.TimeZonesLoadTask(myParent);
        loadTask.setListener(new WidgetTimezones.TimeZonesLoadTaskListener()
        {
            @Override
            public void onStart()
            {
                super.onStart();
                spinner_timezone.setAdapter(new WidgetTimezones.TimeZoneItemAdapter(myParent, R.layout.layout_listitem_timezone));
            }

            @Override
            public void onFinished(WidgetTimezones.TimeZoneItemAdapter result)
            {
                super.onFinished(result);
                spinner_timezone_adapter = result;
                spinner_timezone.setAdapter(spinner_timezone_adapter);
                WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
            }
        });

        if (savedInstanceState != null)
        {
            // saved dialog state; restore it
            //Log.d("DEBUG", "TimeZoneDialog onCreate (restoreState)");
            loadSettings(savedInstanceState);

        } else {
            // no saved dialog state; load from preferences
            //Log.d("DEBUG", "TimeZoneDialog onCreate (newState)");
            loadSettings(myParent);
        }
        loadTask.execute(sortZonesBy);
        return dialog;
    }

    /**
     * onSaveInstanceState
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //Log.d("DEBUG", "TimeZoneDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews
     * @param context
     * @param dialogContent
     */
    protected void initViews( Context context, View dialogContent )
    {
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        utils = new SuntimesUtils();

        layout_timezone = (LinearLayout) dialogContent.findViewById(R.id.appwidget_timezone_custom_layout);
        label_timezone = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_custom_label);
        WidgetTimezones.TimeZoneSort.initDisplayStrings(context);

        ArrayAdapter<WidgetSettings.TimezoneMode> spinner_timezoneModeAdapter;
        spinner_timezoneModeAdapter = new ArrayAdapter<WidgetSettings.TimezoneMode>(context, R.layout.layout_listitem_oneline, WidgetSettings.TimezoneMode.values());
        spinner_timezoneModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_timezoneMode = (Spinner) dialogContent.findViewById(R.id.appwidget_timezone_mode);
        spinner_timezoneMode.setAdapter(spinner_timezoneModeAdapter);
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
        label_timezone.setOnClickListener(new View.OnClickListener()
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
        });

        layout_solartime = (LinearLayout) dialogContent.findViewById(R.id.appwidget_solartime_layout);
        label_solartime = (TextView) dialogContent.findViewById(R.id.appwidget_solartime_label);

        ArrayAdapter<WidgetSettings.SolarTimeMode> spinner_solartimeAdapter;
        spinner_solartimeAdapter = new ArrayAdapter<WidgetSettings.SolarTimeMode>(context, R.layout.layout_listitem_oneline, WidgetSettings.SolarTimeMode.values());
        spinner_solartimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_solartime = (Spinner) dialogContent.findViewById(R.id.appwidget_solartime);
        spinner_solartime.setAdapter(spinner_solartimeAdapter);

        button_solartime_help = (ImageButton) dialogContent.findViewById(R.id.appwidget_solartime_help);
        button_solartime_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getString(R.string.help_general_solartime));
                helpDialog.show(getFragmentManager(), DIALOGTAG_HELP);
            }
        });

        layout_timezoneExtras = dialogContent.findViewById(R.id.appwidget_timezone_extrasgroup);
        label_tzExtras0 = (TextView) dialogContent.findViewById(R.id.appwidget_timezone_extras0);
    }

    private void updateExtrasLabel(@NonNull Context context, int stringResID, long offset)
    {
        SuntimesUtils.TimeDisplayText dstSavings = utils.timeDeltaLongDisplayString(0L, offset, false, false, true);
        ImageSpan dstIcon = SuntimesUtils.createDstSpan(context, 24, 24);
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
        if (solarTime)
        {
            WidgetSettings.SolarTimeMode item = (WidgetSettings.SolarTimeMode)item0;
            if (item != null && item == WidgetSettings.SolarTimeMode.APPARENT_SOLAR_TIME)
            {
                int eot = WidgetTimezones.ApparentSolarTime.equationOfTimeOffset(now.getTimeInMillis(), calculator);
                updateExtrasLabel(getContext(), R.string.timezoneExtraApparentSolar, eot);
            } else updateExtrasLabel(null);

        } else {
            WidgetTimezones.TimeZoneItem item = (WidgetTimezones.TimeZoneItem)item0;
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
    private AdapterView.OnItemSelectedListener onSolarTimeSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Context context = getContext();
            if (layout_timezoneExtras != null && label_tzExtras0 != null && context != null) {
                updateExtras(context, true, parent.getItemAtPosition(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * onTimeZoneSelected
     */
    private AdapterView.OnItemSelectedListener onTimeZoneSelected = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Context context = getContext();
            if (layout_timezoneExtras != null && label_tzExtras0 != null && context != null) {
                updateExtras(context, false, parent.getItemAtPosition(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * onTimeZoneModeSelected
     */
    private Spinner.OnItemSelectedListener onTimeZoneModeSelected = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            spinner_timezone.setOnItemSelectedListener(null);
            spinner_solartime.setOnItemSelectedListener(null);

            final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
            WidgetSettings.TimezoneMode timezoneMode = timezoneModes[parent.getSelectedItemPosition()];

            boolean useSolarTime = (timezoneMode == WidgetSettings.TimezoneMode.SOLAR_TIME);
            if (useSolarTime)
                spinner_solartime.setOnItemSelectedListener(onSolarTimeSelected);
            else spinner_timezone.setOnItemSelectedListener(onTimeZoneSelected);

            setUseCustomTimezone((timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE));
            setUseSolarTime((timezoneMode == WidgetSettings.TimezoneMode.SOLAR_TIME));

            Object item = (useSolarTime ? spinner_solartime.getSelectedItem() : spinner_timezone.getSelectedItem());
            updateExtras(getContext(), useSolarTime, item);

            SuntimesUtils.announceForAccessibility(spinner_timezoneMode, timezoneMode.getDisplayString());
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
    }

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
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.setTimeZoneSortPref(getContext(), sortMode);
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
            AppCompatActivity activity = (AppCompatActivity)getActivity();
            android.support.v7.view.ActionMode actionMode = activity.startSupportActionMode(new WidgetTimezones.TimeZoneSpinnerSortActionCompat(getContext(), spinner_timezone)
            {
                @Override
                public void onSortTimeZones(WidgetTimezones.TimeZoneItemAdapter result, WidgetTimezones.TimeZoneSort sortMode)
                {
                    super.onSortTimeZones(result, sortMode);
                    spinner_timezone_adapter = result;
                    WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);
                }

                @Override
                public void onSaveSortMode( WidgetTimezones.TimeZoneSort sortMode )
                {
                    super.onSaveSortMode(sortMode);
                    AppSettings.setTimeZoneSortPref(context, sortMode);
                }

                @Override
                public void onDestroyActionMode(android.support.v7.view.ActionMode mode)
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
        WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        spinner_timezoneMode.setSelection(timezoneMode.ordinal());

        customTimezoneID = WidgetSettings.loadTimezonePref(context, appWidgetId);
        WidgetTimezones.selectTimeZone(spinner_timezone, spinner_timezone_adapter, customTimezoneID);

        WidgetSettings.SolarTimeMode solartimeMode = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
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
            WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.TimezoneMode.valueOf(modeString);
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
            WidgetSettings.SolarTimeMode solartimeMode = WidgetSettings.SolarTimeMode.valueOf(solarModeString);
            spinner_solartime.setSelection(solartimeMode.ordinal());
        }

        long nowMillis = bundle.getLong(KEY_NOW, Calendar.getInstance().getTimeInMillis());
        now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);

        longitude = bundle.getDouble(KEY_LONGITUDE);
    }

    /**
     * Save the dialog state to preferences to be used by the app (occurs on dialog accept).
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        final WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, timezoneMode);

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        WidgetSettings.saveTimezonePref(context, appWidgetId, customTimezone.getID());

        // save: solar timemode
        WidgetSettings.SolarTimeMode[] solarTimeModes = WidgetSettings.SolarTimeMode.values();
        WidgetSettings.SolarTimeMode solarTimeMode = solarTimeModes[spinner_solartime.getSelectedItemPosition()];
        WidgetSettings.saveSolarTimeModePref(context, appWidgetId, solarTimeMode);
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle a bundle containing the dialog state
     */
    protected void saveSettings(Bundle bundle)
    {
        // save: timezone mode
        WidgetSettings.TimezoneMode[] timezoneModes = WidgetSettings.TimezoneMode.values();
        WidgetSettings.TimezoneMode timezoneMode = timezoneModes[spinner_timezoneMode.getSelectedItemPosition()];
        bundle.putString(KEY_TIMEZONE_MODE, timezoneMode.name());

        // save: custom timezone
        WidgetTimezones.TimeZoneItem customTimezone = (WidgetTimezones.TimeZoneItem) spinner_timezone.getSelectedItem();
        if (customTimezone != null)
        {
            bundle.putString(KEY_TIMEZONE_ID, customTimezone.getID());
        }

        // save: solar timemode
        WidgetSettings.SolarTimeMode[] solarTimeModes = WidgetSettings.SolarTimeMode.values();
        WidgetSettings.SolarTimeMode solarTimeMode = solarTimeModes[spinner_solartime.getSelectedItemPosition()];
        if (solarTimeMode != null)
        {
            bundle.putString(KEY_SOLARTIME_MODE, solarTimeMode.name());
        }

        // save: now
        if (now != null) {
            bundle.putLong(KEY_NOW, now.getTimeInMillis());
        }

        // save: longitude
        bundle.putDouble(KEY_LONGITUDE, longitude);
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

}
