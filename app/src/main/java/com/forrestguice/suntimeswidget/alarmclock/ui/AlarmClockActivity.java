/**
    Copyright (C) 2018-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.ActivityNotFoundException;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.alarmclock.ui.bedtime.BedtimeActivity;
import com.forrestguice.suntimeswidget.navigation.SuntimesNavigation;
import com.forrestguice.suntimeswidget.settings.fragments.AlarmPrefsFragment;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWarning;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemUri;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * AlarmClockActivity
 */
public class AlarmClockActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverList";

    public static final String ACTION_SHOW_ALARMS = "android.intent.action.SHOW_ALARMS";            // AlarmClock.ACTION_SHOW_ALARMS (api19+)

    public static final String ACTION_SET_ALARM = AlarmClock.ACTION_SET_ALARM;
    public static final String EXTRA_MESSAGE = AlarmClock.EXTRA_MESSAGE;
    public static final String EXTRA_HOUR = AlarmClock.EXTRA_HOUR;
    public static final String EXTRA_MINUTES = AlarmClock.EXTRA_MINUTES;
    public static final String EXTRA_DAYS = "android.intent.extra.alarm.DAYS";                      // AlarmClock.EXTRA_DAYS (api19+)
    public static final String EXTRA_VIBRATE = "android.intent.extra.alarm.VIBRATE";                // AlarmClock.EXTRA_VIBRATE (api19+)
    public static final String EXTRA_SKIP_UI = "android.intent.extra.alarm.SKIP_UI";                // AlarmClock.EXTRA_SKIP_UI (api11+)
    public static final String EXTRA_RINGTONE = "android.intent.extra.alarm.RINGTONE";              // AlarmClock.EXTRA_RINGTONE (api19+)
    public static final String VALUE_RINGTONE_SILENT = "silent";                                    // AlarmClock.VALUE_RINGTONE_SILENT (api19+)

    public static final String ACTION_DISMISS_ALARM = "android.intent.action.DISMISS_ALARM";        // AlarmClock.ACTION_DISMISS_ALARM (api23+)
    public static final String EXTRA_ALARM_SEARCH_MODE = "android.intent.extra.alarm.SEARCH_MODE";  // AlarmClock.EXTRA_ALARM_SEARCH_MODE;
    public static final String ALARM_SEARCH_MODE_ALL = "android.all";
    public static final String ALARM_SEARCH_MODE_LABEL = "android.label";
    public static final String ALARM_SEARCH_MODE_NEXT = "android.next";
    public static final String ALARM_SEARCH_MODE_TIME = "android.time";
    public static final String EXTRA_IS_PM = "android.intent.extra.alarm.IS_PM";                    // AlarmClock.EXTRA_IS_PM (api23+)

    public static final String ACTION_SNOOZE_ALARM = "android.intent.action.SNOOZE_ALARM";          // AlarmClock.ACTION_SNOOZE_ALARM (api23+)
    public static final String EXTRA_ALARM_SNOOZE_DURATION = "android.intent.extra.alarm.SNOOZE_DURATION";  // minutes; AlarmClock.EXTRA_ALARM_SNOOZE_DURATION;

    public static final String ACTION_ADD_ALARM = "suntimes.action.alarmclock.ADD_ALARM";
    public static final String ACTION_ADD_NOTIFICATION = "suntimes.action.alarmclock.ADD_NOTIFICATION";
    public static final String ACTION_ADD_QNOTIFICATION = "suntimes.action.alarmclock.ADD_QNOTIFICATION";

    public static final String[] SUNTIMES_ALARMS_ACTIONS = new String[] { ACTION_SHOW_ALARMS, ACTION_ADD_ALARM, ACTION_ADD_NOTIFICATION, ACTION_ADD_QNOTIFICATION, ACTION_SET_ALARM, ACTION_SNOOZE_ALARM, ACTION_DISMISS_ALARM };    // legacy action map
    private static final HashMap<String, String> SUNTIMES_ALARMS_ACTION_MAP = SuntimesActivity.createLegacyActionMap(SUNTIMES_ALARMS_ACTIONS);

    public static final String EXTRA_SHOWBACK = "showBack";
    public static final String EXTRA_SOLAREVENT = "solarevent";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_LOCATION_LABEL = "location_label";
    public static final String EXTRA_LOCATION_LAT = "latitude";
    public static final String EXTRA_LOCATION_LON = "longitude";
    public static final String EXTRA_LOCATION_ALT = "altitude";
    public static final String EXTRA_TIMEZONE = "timezone";
    public static final String EXTRA_ALARMTYPE = "alarmtype";
    public static final String EXTRA_SELECTED_ALARM = "selectedAlarm";

    public static final int REQUEST_EDITALARM = 1;
    public static final int REQUEST_ADDALARM = 10;
    public static final int REQUEST_SETTINGS = 20;

    public static final String WARNINGID_NOTIFICATIONS = "NotificationsWarning";
    public static final String WARNINGID_NOTIFICATIONS_CHANNEL = "NotificationsChannelWarning";
    public static final String WARNINGID_BATTERY_OPTIMIZATION = "BatteryOptimizationWarning";
    public static final String WARNINGID_BATTERY_OPTIMIZATION_SONY = "BatteryOptimizationWarning_sony";
    public static final String WARNINGID_AUTOSTART= "AutostartWarning";

    private AlarmListDialog list;

    private FloatingActionButton addButton;
    private FloatingActionButton deselectButton;
    private BottomSheetBehavior sheetBehavior;
    private SuntimesNavigation navigation;

    private SuntimesWarning notificationWarning;
    private SuntimesWarning[] notificationChannelWarning = null;   // remains null for api < 26
    private SuntimesWarning batteryOptimizationWarning = null;   // remains null for api < 23
    private SuntimesWarning batteryOptimizationWarning_sony = null;   // remains null for non-sony devices
    private SuntimesWarning autostartWarning = null;    // remains null for non-xiomi devices
    private final List<SuntimesWarning> warnings = new ArrayList<SuntimesWarning>();

    private AppSettings.LocaleInfo localeInfo;

    private int colorAlarmEnabled, colorOn, colorOff, colorEnabled, colorDisabled, colorPressed;
    private int resAddIcon, resCloseIcon;

    public AlarmClockActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase, localeInfo = new AppSettings.LocaleInfo());
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        initTheme();
        super.onCreate(savedState);
        initLocale(this);
        setContentView(R.layout.layout_activity_alarmclock1);
        initViews(this);
        initWarnings(this, savedState);
        handleIntent(getIntent());
    }

    @Override
    public void onStart()
    {
        super.onStart();
        registerReceiver(updateBroadcastReceiver, AlarmNotifications.getUpdateBroadcastIntentFilter());
        registerReceiver(updateBroadcastReceiver1, AlarmNotifications.getUpdateBroadcastIntentFilter(false));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        restoreDialogs();
        checkWarnings();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(updateBroadcastReceiver);
        unregisterReceiver(updateBroadcastReceiver1);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_ADDALARM:
                onEditAlarmResult(resultCode, data, true);
                break;

            case REQUEST_EDITALARM:
                onEditAlarmResult(resultCode, data, false);
                break;

            case REQUEST_SETTINGS:
                onSettingsResult(resultCode, data);
                break;
        }
    }


    private String appTheme;
    private int appThemeResID;
    private SuntimesTheme appThemeOverride = null;

    private void initTheme()
    {
        appTheme = AppSettings.loadThemePref(this);
        appThemeResID = AppSettings.setTheme(this, appTheme);

        String themeName = AppSettings.getThemeOverride(this, appTheme);
        if (themeName != null && WidgetThemes.hasValue(themeName)) {
            Log.i("initTheme", "Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }
    }

    private final BroadcastReceiver updateBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Uri data = intent.getData();
            Log.d(TAG, "updateReceiver.onReceive: " + data + " :: " + action);

            if (action != null)
            {
                if (action.equals(AlarmNotifications.ACTION_UPDATE_UI))
                {
                    if (data != null)
                    {
                        Long alarmID;
                        try {
                            alarmID = ContentUris.parseId(data);
                        } catch (NumberFormatException e) {
                            alarmID = null;
                            Log.e(TAG, "updateReceiver.onReceive: invalid data! " + e);
                        }
                        list.reloadAdapter((alarmID != null && alarmID != -1 ? alarmID : null));
                        Log.d("DEBUG", "adapter reloaded: " + alarmID);

                        boolean wasDeleted = intent.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                        if (wasDeleted && alarmID != null) {
                            list.notifyAlarmDeleted(alarmID);
                        }

                    } else Log.e(TAG, "updateReceiver.onReceive: null data!");
                } else Log.e(TAG, "updateReceiver.onReceive: unrecognized action: " + action);
            } else Log.e(TAG, "updateReceiver.onReceive: null action!");
        }
    };

    private final BroadcastReceiver updateBroadcastReceiver1 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG, "updateReceiver.onReceive1: " + action);

            if (action != null)
            {
                if (action.equals(AlarmNotifications.ACTION_UPDATE_UI))
                {
                    boolean wasCleared = intent.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                    if (wasCleared) {
                        list.notifyAlarmsCleared();
                    }

                } else Log.e(TAG, "updateReceiver.onReceive: unrecognized action: " + action);
            } else Log.e(TAG, "updateReceiver.onReceive: null action!");
        }
    };

    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
        Log.d("DEBUG", "new intent: " + intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent)
    {
        Context context = this;

        String param_action = intent.getAction();
        intent.setAction(null);

        if (SUNTIMES_ALARMS_ACTION_MAP.containsKey(param_action)) {
            Log.d("handleIntent", "legacy action: " + param_action);
            param_action = SUNTIMES_ALARMS_ACTION_MAP.get(param_action);
        }

        Uri param_data = intent.getData();
        intent.setData(null);

        boolean selectItem = true;

        if (param_action != null)
        {
            if (param_action.equals(ACTION_SET_ALARM)) {
                handleIntent_setAlarm(context, intent);

            } else if (param_action.equals(ACTION_ADD_ALARM)) {
                showAddDialog(AlarmClockItem.AlarmType.ALARM);

            } else if (param_action.equals(ACTION_ADD_NOTIFICATION)) {
                showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);

            } else if (param_action.equals(ACTION_ADD_QNOTIFICATION)) {
                showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION1);

            } else if (param_action.equals(ACTION_DISMISS_ALARM)) {
                handleIntent_dismissAlarms(intent, param_data);

            } else if (param_action.equals(ACTION_SNOOZE_ALARM)) {
                handleIntent_snoozeAlarm(intent);

            } else if (param_action.equals(AlarmNotifications.ACTION_DELETE)) {
                if (param_data != null)
                {
                    try {
                        list.notifyAlarmDeleted(ContentUris.parseId(param_data));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "handleIntent: invalid data! " + e);
                    }
                } else {
                    list.notifyAlarmsCleared();
                    selectItem = false;
                }
            }
        } else {
            if (param_data != null)
            {
                try {
                    list.notifyAlarmUpdated(ContentUris.parseId(param_data));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "handleIntent: invalid data! " + e);
                }
            }
        }

        long selectedID = intent.getLongExtra(EXTRA_SELECTED_ALARM, -1);
        if (selectItem && selectedID != -1)
        {
            Log.d(TAG, "handleIntent: selected id: " + selectedID);
            list.setSelectedRowID(selectedID);
        }
    }

    protected void handleIntent_setAlarm(Context context, Intent intent)
    {
        Log.i(TAG, "ACTION_SET_ALARM");
        AlarmClockItem.AlarmType param_type = AlarmClockItem.AlarmType.valueOf(intent.getStringExtra(AlarmClockActivity.EXTRA_ALARMTYPE), AlarmClockItem.AlarmType.ALARM);
        String param_label = intent.getStringExtra(EXTRA_MESSAGE);
        int param_hour = intent.getIntExtra(EXTRA_HOUR, -1);
        int param_minute = intent.getIntExtra(EXTRA_MINUTES, -1);
        String param_timezone = intent.getStringExtra(AlarmClockActivity.EXTRA_TIMEZONE);

        ArrayList<Integer> param_days = AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS;
        boolean param_vibrate = AlarmSettings.loadPrefVibrateDefault(this);
        Uri param_ringtoneUri = AlarmSettings.getDefaultRingtoneUri(this, param_type);
        String param_ringtoneName = AlarmSettings.getDefaultRingtoneName(this, param_type);
        if (Build.VERSION.SDK_INT >= 19)
        {
            param_vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, param_vibrate);

            String param_ringtoneUriString = intent.getStringExtra(EXTRA_RINGTONE);
            if (param_ringtoneUriString != null) {
                param_ringtoneUri = (param_ringtoneUriString.equals(VALUE_RINGTONE_SILENT) ? null : Uri.parse(param_ringtoneUriString));
                param_ringtoneName = AlarmSettings.getRingtoneName(context, param_ringtoneUri);    // TODO: may block
            }

            ArrayList<Integer> repeatOnDays = intent.getIntegerArrayListExtra(EXTRA_DAYS);
            if (repeatOnDays != null) {
                param_days = repeatOnDays;
            }
        }

        String param_event = intent.getStringExtra(AlarmClockActivity.EXTRA_SOLAREVENT);
        if (!AlarmEvent.isValidEventID(context, param_event)) {
            Log.w(TAG, "handleIntent: ignoring invalid event " + param_event);
            param_event = null;
        }

        intent.setExtrasClassLoader(getClassLoader());
        Location param_location = locationFromIntentExtras(context, intent);

        boolean param_skipUI = false;
        if (Build.VERSION.SDK_INT >= 11) {
            param_skipUI = intent.getBooleanExtra(EXTRA_SKIP_UI, false);
        }
        if (param_skipUI) {   // TODO: support date
            list.createAlarm(context, param_type, param_label, param_event, param_location, -1L, param_hour, param_minute, param_timezone, param_vibrate, param_ringtoneUri, param_ringtoneName, param_days, true);
        } else {
            AlarmClockItem item = AlarmListDialog.createAlarm(context, param_type, param_label, param_event, param_location, -1L, param_hour, param_minute, param_timezone, param_vibrate, param_ringtoneUri, param_ringtoneName, param_days);
            AlarmNotifications.updateAlarmTime(context, item);
            showAlarmEditActivity(item, null, REQUEST_ADDALARM, true);
        }
    }

    protected void handleIntent_dismissAlarms(Intent intent, Uri param_data)
    {
        Long alarmID;
        try {
            alarmID = (param_data != null) ? ContentUris.parseId(param_data) : null;
        } catch (NumberFormatException e) {
            Log.e(TAG, "handleIntent: invalid data! " + e);
            alarmID = null;
        }

        if (alarmID != null) {
            Log.i(TAG, "ACTION_DISMISS_ALARM: " + param_data);
            sendBroadcast(AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_DISMISS, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmID)));

        } else {
            String searchMode = intent.getStringExtra(EXTRA_ALARM_SEARCH_MODE);
            if (searchMode == null) {
                searchMode = ALARM_SEARCH_MODE_NEXT;
            }
            switch (searchMode)
            {
                case ALARM_SEARCH_MODE_ALL:    // AlarmClock.ALARM_SEARCH_MODE_ALL (api23+)
                    Log.i(TAG, "ACTION_DISMISS_ALARM: All");
                    handleIntent_dismissAllAlarms();
                    break;

                case ALARM_SEARCH_MODE_LABEL:
                    Log.i(TAG, "ACTION_DISMISS_ALARM: Label");
                    handleIntent_dismissAlarmByLabel(intent);
                    break;

                case ALARM_SEARCH_MODE_TIME:
                    Log.i(TAG, "ACTION_DISMISS_ALARM: Time");
                    handleIntent_dismissAlarmByTime(intent);
                    break;

                case ALARM_SEARCH_MODE_NEXT:
                default:
                    Log.i(TAG, "ACTION_DISMISS_ALARM: Next");
                    handleIntent_dismissNextAlarm();
                    break;
            }
        }
    }

    protected void handleIntent_dismissAllAlarms()
    {
        AlarmNotifications.findEnabledAlarms(getApplicationContext(), new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
            public void onItemsLoaded(Long[] ids) {
                for (long id : ids) {
                    sendBroadcast(AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_DISMISS, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, id)));
                }
            }
        });
    }

    protected void handleIntent_dismissAlarmByTime(Intent intent)
    {
        /*int hour = intent.getIntExtra(EXTRA_HOUR, -1);
        if (hour >= 0)
        {
            int minutes = intent.getIntExtra(EXTRA_MINUTES, 0);
            boolean isPM = intent.getBooleanExtra(EXTRA_IS_PM, false);
            // TODO: search and dismiss by time; hour, minutes, isPM

        } else {*/
            Log.w(TAG, "dismissAlarmByTime: not implemented.. falling back to dismissNextAlarm.");
            handleIntent_dismissNextAlarm();
        //}
    }

    protected void handleIntent_dismissAlarmByLabel(Intent intent)
    {
        final String search = intent.getStringExtra(EXTRA_MESSAGE);
        if (search != null && !search.trim().isEmpty())
        {
            AlarmDatabaseAdapter.AlarmListTask findTask = createFindAlarmsByLabelTask(this, search);
            findTask.setParam_enabledOnly(true);
            findTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                public void onItemsLoaded(Long[] ids) {
                    for (long id : ids) {
                        sendBroadcast(AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_DISMISS, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, id)));
                    }
                }
            });
            findTask.execute();

        } else {
            Log.w(TAG, "dismissAlarmByLabel: missing search term.. falling back to dismissNextAlarm.");
            handleIntent_dismissNextAlarm();
        }
    }
    private static AlarmDatabaseAdapter.AlarmListTask createFindAlarmsByLabelTask(Context context, @NonNull final String search)
    {
        return new AlarmDatabaseAdapter.AlarmListTask(context)
        {
            @Override
            protected boolean passesFilter(Cursor cursor, long rowID) {
                int index = cursor.getColumnIndex(AlarmDatabaseAdapter.KEY_ALARM_LABEL);
                String label = (index >= 0) ? cursor.getString(index) : null;
                return (label != null && label.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)));
            }
        };
    }

    protected void handleIntent_dismissNextAlarm()
    {
        AlarmDatabaseAdapter.AlarmListTask findTask = new AlarmDatabaseAdapter.AlarmListTask(this);
        findTask.setParam_withAlarmState(AlarmState.STATE_SOUNDING, AlarmState.STATE_SNOOZING);
        findTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener()
        {
            @Override
            public void onItemsLoaded(Long[] ids) {
                if (ids.length > 0) {
                    for (long id : ids) {    // dismiss all sounding or snoozing alarms
                        sendBroadcast(AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_DISMISS, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, id)));
                    }
                } else {
                    AlarmNotifications.findUpcomingAlarm(getApplicationContext(), new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                        public void onItemsLoaded(Long[] ids) {
                            if (ids.length > 0 && ids[0] != null) {    // dismiss next upcoming alarm
                                sendBroadcast(AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_DISMISS, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, ids[0])));
                            }
                        }
                    });
                }
            }
        });
        findTask.execute();
    }

    protected void handleIntent_snoozeAlarm(final Intent intent)
    {
        final int minutes = intent.getIntExtra(EXTRA_ALARM_SNOOZE_DURATION, -1);
        Log.i(TAG, "ACTION_SNOOZE_ALARM: " + minutes + " minutes");

        AlarmNotifications.findSoundingAlarms(getApplicationContext(), new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
            @Override
            public void onItemsLoaded(Long[] ids)
            {
                for (long id : ids) {
                    Uri uri = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, id);
                    Intent snoozeIntent = AlarmNotifications.getAlarmIntent(getApplicationContext(), AlarmNotifications.ACTION_SNOOZE, uri);
                    snoozeIntent.putExtra(EXTRA_ALARM_SNOOZE_DURATION, minutes);
                    sendBroadcast(snoozeIntent);
                }
            }
        });
    }

    protected static Location locationFromIntentExtras(Context context, Intent intent)
    {
        Bundle locationBundle = intent.getBundleExtra(AlarmClockActivity.EXTRA_LOCATION);
        Location location = ((locationBundle != null) ? (Location) locationBundle.getParcelable(AlarmClockActivity.EXTRA_LOCATION) : null);
        if (location != null) {
            return location;
        }

        Location appLocation = WidgetSettings.loadLocationPref(context, 0);
        if (intent.hasExtra(AlarmClockActivity.EXTRA_LOCATION_LAT) && intent.hasExtra(AlarmClockActivity.EXTRA_LOCATION_LON))
        {
            double latitude = intent.getDoubleExtra(AlarmClockActivity.EXTRA_LOCATION_LAT, -1000);
            double longitude = intent.getDoubleExtra(AlarmClockActivity.EXTRA_LOCATION_LON, -1000);
            if ((latitude >= -90 && latitude <= 90) && (longitude >= -180 && longitude <= 180))
            {
                String label = intent.getStringExtra(AlarmClockActivity.EXTRA_LOCATION_LABEL);
                double altitude = intent.getDoubleExtra(AlarmClockActivity.EXTRA_LOCATION_ALT, 0);
                location = new Location(label, latitude + "", longitude + "", altitude + "");
                location.setUseAltitude(appLocation.useAltitude());
                return location;
            } else return appLocation;    // invalid extras
        } else return appLocation;    // no extras
    }
    
    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
        AlarmClockItem.AlarmType.initDisplayStrings(context);
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);

        int[] attrs = { R.attr.alarmColorEnabled, android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor, android.R.attr.textColor, R.attr.icActionNew, R.attr.icActionClose };
        TypedArray a = context.obtainStyledAttributes(attrs);
        colorAlarmEnabled = colorOn = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled_dark));
        colorEnabled = ContextCompat.getColor(context, a.getResourceId(1, android.R.color.primary_text_dark));
        colorDisabled = ContextCompat.getColor(context, a.getResourceId(2, R.color.text_disabled_dark));
        colorPressed = ContextCompat.getColor(context, a.getResourceId(3, R.color.sunIcon_color_setting_dark));
        colorOff = ContextCompat.getColor(context, a.getResourceId(4, R.color.grey_600));
        resAddIcon = a.getResourceId(5, R.drawable.ic_action_new);
        resCloseIcon = a.getResourceId(6, R.drawable.ic_action_close);
        a.recycle();

        if (appThemeOverride != null) {
            colorAlarmEnabled = colorOn = appThemeOverride.getAccentColor();
            colorPressed = appThemeOverride.getActionColor();
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveWarnings(outState);
        outState.putInt("bottomsheet", sheetBehavior.getState());
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreWarnings(savedState);

        int sheetState = savedState.getInt("bottomsheet", BottomSheetBehavior.STATE_HIDDEN);
        sheetBehavior.setState(sheetState);

        if (Build.VERSION.SDK_INT >= 14)
        {
            if (sheetState != BottomSheetBehavior.STATE_HIDDEN)
            {
                addButton.setScaleX(0);
                addButton.setScaleY(0);
            }
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            boolean showBack = getIntent().getBooleanExtra(EXTRA_SHOWBACK, false);
            if (!showBack) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
            }
        }

        navigation = new SuntimesNavigation(this, menuBar, R.id.action_alarms);

        addButton = (FloatingActionButton) findViewById(R.id.btn_add);

        deselectButton = (FloatingActionButton) findViewById(R.id.btn_deselect);
        deselectButton.hide();    // deselectButton.setVisibility(View.GONE);
        TooltipCompat.setTooltipText(deselectButton, deselectButton.getContentDescription());

        if (Build.VERSION.SDK_INT <= 19)    // override ripple fallback
        {
            addButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorAlarmEnabled, colorDisabled, colorPressed));
            addButton.setRippleColor(Color.TRANSPARENT);

            deselectButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorAlarmEnabled, colorDisabled, colorPressed));
            deselectButton.setRippleColor(Color.TRANSPARENT);
        }

        addButton.setOnClickListener(onFabMenuClick);
        deselectButton.setOnClickListener(onDeselectClick);

        list = (AlarmListDialog) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        if (list != null)
        {
            list.setOnEmptyViewClick(onEmptyViewClick);
            list.setAdapterListener(listAdapter);
        }

        View bottomSheet = findViewById(R.id.app_bottomsheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View view, int newState)
            {
                /* switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN: break;
                    case BottomSheetBehavior.STATE_EXPANDED: break;
                    case BottomSheetBehavior.STATE_COLLAPSED: break;
                    case BottomSheetBehavior.STATE_DRAGGING: break;
                    case BottomSheetBehavior.STATE_SETTLING: break;
                } */
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (Build.VERSION.SDK_INT >= 14) {
                    addButton.animate().scaleX(1 - v).scaleY(1 - v).setDuration(0).start();
                }
            }
        });
    }

    private boolean isAddDialogShowing() {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    private final AlarmListDialog.AdapterListener listAdapter = new AlarmListDialog.AdapterListener()
    {
        @Override
        public void onItemSelected(long rowID) {
            if (rowID != -1)
                deselectButton.show();
            else deselectButton.hide();    //deselectButton.setVisibility(rowID != -1 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onItemClicked(AlarmClockItem item, AlarmListDialog.AlarmListDialogItem view)
        {
            if (isAddDialogShowing()) {
                dismissAddDialog();

            } else if (list.getSelectedRowID() == item.rowID) {
                if (item.getState() == AlarmState.STATE_SOUNDING || item.getState() == AlarmState.STATE_SNOOZING || item.getState() == AlarmState.STATE_TIMEOUT)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM) {
                        showAlarmFullscreenActivity(item, view.text_datetime);
                    } else {
                        sendBroadcast(AlarmNotifications.getAlarmIntent(AlarmClockActivity.this, AlarmNotifications.ACTION_DISMISS, item.getUri()));
                    }
                } else {
                    showAlarmEditActivity(item, view.text_datetime, REQUEST_EDITALARM, false);
                }

            } else {
                if (item.enabled && (item.getState() == AlarmState.STATE_SCHEDULED_SOON || item.getState() == AlarmState.STATE_SCHEDULED_DISTANT)) {
                    AlarmNotifications.showTimeUntilToast(AlarmClockActivity.this, list.getView(), item);
                }
            }
        }

        @Override
        public boolean onItemLongClicked(AlarmClockItem item) {
            return true;
        }

        @Override
        public void onItemNoteClicked(final AlarmClockItem item, final AlarmListDialog.AlarmListDialogItem view)
        {
            view.triggerPreviewOffset(AlarmClockActivity.this, item);
            if (item.enabled) {
                AlarmNotifications.showTimeUntilToast(AlarmClockActivity.this, list.getView(), item);
            }
        }

        @Override
        public void onAlarmToggled(AlarmClockItem item, boolean enabled) {
            if (enabled) {
                AlarmNotifications.showTimeUntilToast(AlarmClockActivity.this, list.getView(), item);
            }
        }

        @Override
        public void onAlarmAdded(AlarmClockItem item) {
        }

        @Override
        public void onAlarmDeleted(long rowID) {}

        @Override
        public void onAlarmsCleared() {
            //Toast.makeText(AlarmClockActivity.this, getString(R.string.clearalarms_toast_success), Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    private final DialogInterface.OnClickListener onAddAlarmAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            Context context = AlarmClockActivity.this;
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentById(R.id.createAlarmFragment);
            if (dialog != null)
            {
                AlarmClockItem item = AlarmCreateDialog.createAlarm(context, dialog, dialog.getAlarmType());
                AlarmNotifications.updateAlarmTime(context, item);
                dialog.saveSettings(context);
                ViewCompat.setTransitionName(dialog.text_time, "transition_" + item.rowID);
                showAlarmEditActivity(item, dialog.text_time, REQUEST_ADDALARM, true);
            }
        }
    };
    private final DialogInterface.OnClickListener onAddAlarmNeutral = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            dismissAddDialog();
        }
    };
    private final DialogInterface.OnClickListener onAddAlarmCanceled = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            dismissAddDialog();
        }
    };

    protected boolean showAlarmFullscreenActivity(@NonNull AlarmClockItem item, @Nullable View sharedView)
    {
        if (SystemClock.elapsedRealtime() - showAlarmFullscreenctivity_last < 1000) {
            return false;  // prevent multiple successive calls (by click handlers) from triggering startActivity multiple times
        } else showAlarmFullscreenctivity_last = SystemClock.elapsedRealtime();

        startActivity(AlarmNotifications.getFullscreenIntent(AlarmClockActivity.this, item.getUri()));
        return true;
    }
    private long showAlarmFullscreenctivity_last = (SystemClock.elapsedRealtime() - 1000);

    protected boolean showAlarmEditActivity(@NonNull AlarmClockItem item, @Nullable View sharedView, int requestCode, boolean isNewAlarm)
    {
        if (SystemClock.elapsedRealtime() - showAlarmEditActivity_last < 1000) {
            return false;  // prevent multiple successive calls (by click handlers) from triggering startActivity multiple times
        } else showAlarmEditActivity_last = SystemClock.elapsedRealtime();

        Intent intent = new Intent(this, AlarmEditActivity.class);
        intent.putExtra(AlarmEditActivity.EXTRA_ITEM, item);
        intent.putExtra(AlarmEditActivity.EXTRA_ISNEW, isNewAlarm);

        if (Build.VERSION.SDK_INT >= 16 && sharedView != null)
        {
            String transitionName = ViewCompat.getTransitionName(sharedView);
            if (transitionName != null)
            {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedView, transitionName);
                startActivityForResult(intent, requestCode, options.toBundle());

            } else {
                startActivityForResult(intent, requestCode);
            }
        } else {
            startActivityForResult(intent, requestCode);
        }
        return true;
    }
    private long showAlarmEditActivity_last = (SystemClock.elapsedRealtime() - 1000);

    private final AlarmDatabaseAdapter.AlarmItemTaskListener onUpdateItem = new AlarmDatabaseAdapter.AlarmItemTaskListener()
    {
        @Override
        public void onFinished(Boolean result, AlarmClockItem item)
        {
            if (result)
            {
                if (item.enabled) {
                    sendBroadcast( AlarmNotifications.getAlarmIntent(AlarmClockActivity.this, AlarmNotifications.ACTION_RESCHEDULE, item.getUri()) );
                    listAdapter.onAlarmToggled(item, true);
                }

                if (list != null) {
                    list.reloadAdapter(item.rowID);
                    list.setSelectedRowID(item.rowID);
                }
            }
        }
    };

    protected void showAddDialog(@Nullable AlarmClockItem.AlarmType type)
    {
        list.clearSelection();

        FragmentManager fragments = getSupportFragmentManager();
        AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentById(R.id.createAlarmFragment);
        if (dialog != null) {
            dialog.loadSettings(AlarmClockActivity.this);
            if (type != null) {
                dialog.setAlarmType(type);
            }
            dialog.setOnAcceptedListener(onAddAlarmAccepted);
            dialog.setOnCanceledListener(onAddAlarmCanceled);
            dialog.setOnNeutralListener(onAddAlarmNeutral);
        }
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    protected void dismissAddDialog() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    protected void updateViews(Context context) {
    }

    protected void restoreDialogs()
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmCreateDialog alarmCreateDialog = (AlarmCreateDialog) fragments.findFragmentById(R.id.createAlarmFragment);
        if (alarmCreateDialog != null) {
            alarmCreateDialog.setOnAcceptedListener(onAddAlarmAccepted);
            alarmCreateDialog.setOnCanceledListener(onAddAlarmCanceled);
            alarmCreateDialog.setOnNeutralListener(onAddAlarmNeutral);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initWarnings(Context context, Bundle savedState)
    {
        warnings.clear();

        notificationWarning = new SuntimesWarning(WARNINGID_NOTIFICATIONS);
        warnings.add(notificationWarning);

        if (Build.VERSION.SDK_INT >= 26)
        {
            notificationChannelWarning = new SuntimesWarning[AlarmClockItem.AlarmType.values().length];
            for (int i=0; i<notificationChannelWarning.length; i++) {
                warnings.add(notificationChannelWarning[i] = new SuntimesWarning(WARNINGID_NOTIFICATIONS_CHANNEL + i));
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            batteryOptimizationWarning = new SuntimesWarning(WARNINGID_BATTERY_OPTIMIZATION);
            warnings.add(batteryOptimizationWarning);
        }

        if (AlarmSettings.isSony())
        {
            batteryOptimizationWarning_sony = new SuntimesWarning(WARNINGID_BATTERY_OPTIMIZATION_SONY);
            warnings.add(batteryOptimizationWarning_sony);
        }

        if (AlarmSettings.isXiomi())
        {
            autostartWarning = new SuntimesWarning(WARNINGID_AUTOSTART);
            warnings.add(autostartWarning);
        }

        restoreWarnings(savedState);
    }
    private final SuntimesWarning.SuntimesWarningListener warningListener = new SuntimesWarning.SuntimesWarningListener() {
        @Override
        public void onShowNextWarning() {
            showWarnings();
        }
    };
    private void saveWarnings( Bundle outState )
    {
        for (SuntimesWarning warning : warnings) {
            warning.save(outState);
        }
    }
    private void restoreWarnings(Bundle savedState)
    {
        for (SuntimesWarning warning : warnings) {
            warning.restore(savedState);
            warning.setWarningListener(warningListener);
        }
    }
    private void showWarnings()
    {
        boolean showWarnings = AppSettings.loadShowWarningsPref(this);
        if (showWarnings && notificationWarning.shouldShow() && !notificationWarning.wasDismissed())
        {
            notificationWarning.initWarning(this, addButton, getString(R.string.notificationsWarning));
            notificationWarning.getSnackbar().setAction(getString(R.string.configLabel_alarms_notifications), new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    AlarmPrefsFragment.openNotificationSettings(AlarmClockActivity.this);
                }
            });
            notificationWarning.show();
            return;
        }

        if (showWarnings && notificationChannelWarning != null)
        {
            final AlarmClockItem.AlarmType[] types = AlarmClockItem.AlarmType.values();
            for (int i=0; i<notificationChannelWarning.length; i++)
            {
                SuntimesWarning warning = notificationChannelWarning[i];
                if (warning != null && warning.shouldShow() && !warning.wasDismissed())
                {
                    final int j = ((i < types.length) ? i : 0);
                    warning.initWarning(this, addButton, getString(R.string.notificationChannelWarning));
                    warning.getSnackbar().setAction(getString(R.string.configLabel_alarms_notifications), new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view) {
                            AlarmSettings.openChannelSettings(AlarmClockActivity.this, types[j]);
                        }
                    });
                    warning.show();
                    return;
                }
            }
        }

        if (showWarnings && batteryOptimizationWarning != null
                && batteryOptimizationWarning.shouldShow() && !batteryOptimizationWarning.wasDismissed())
        {
            String message = getString(AlarmSettings.aggressiveBatteryOptimizations(this) ? R.string.configLabel_alarms_optWhiteList_unlisted_aggressive : R.string.configLabel_alarms_optWhiteList_unlisted);
            batteryOptimizationWarning.initWarning(this, addButton, "[w] " + message);
            batteryOptimizationWarning.getSnackbar().setAction(getString(R.string.configLabel_alarms_optWhiteList), new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    AlarmPrefsFragment.createBatteryOptimizationAlertDialog(AlarmClockActivity.this).show();
                }
            });
            batteryOptimizationWarning.show();
            return;
        }

        if (showWarnings && batteryOptimizationWarning_sony != null
                && batteryOptimizationWarning_sony.shouldShow() && !batteryOptimizationWarning_sony.wasDismissed())
        {
            batteryOptimizationWarning_sony.initWarning(this, addButton, getString(R.string.sonyStaminaModeWarning));
            batteryOptimizationWarning_sony.show();
            return;
        }

        if (showWarnings && autostartWarning != null
                && autostartWarning.shouldShow() && !autostartWarning.wasDismissed())
        {
            autostartWarning.initWarning(this, addButton, getString(R.string.autostartWarning));
            autostartWarning.getSnackbar().setAction(getString(R.string.configLabel_alarms_autostart), new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    AlarmSettings.openAutostartSettings(AlarmClockActivity.this);
                }
            });
            autostartWarning.show();
            return;
        }

        // no warnings shown; clear previous (stale) messages
        for (SuntimesWarning warning : warnings) {
            warning.dismiss();
        }
    }
    private void checkWarnings()
    {
        if (!AlarmSettings.bootCompletedWasRun(this))
        {
            Log.w(TAG, "checkWarnings: BOOT_COMPLETED hasn't run yet! triggering it now..");
            sendBroadcast(new Intent(AlarmNotifications.getAlarmIntent(this, AlarmNotifications.ACTION_SCHEDULE, null)));
        }

        notificationWarning.setShouldShow(!NotificationManagerCompat.from(this).areNotificationsEnabled());
        if (notificationChannelWarning != null)
        {
            AlarmClockItem.AlarmType[] types = AlarmClockItem.AlarmType.values();
            for (int i=0; i<notificationChannelWarning.length; i++)
            {
                SuntimesWarning warning = notificationChannelWarning[i];
                if (warning != null) {
                    boolean value = AlarmSettings.isChannelMuted(this, types[i]);
                    warning.setShouldShow(value);
                    //Log.d("DEBUG", "setShouldShow: " + value);
                }
            }
        }
        if (batteryOptimizationWarning != null) {
            batteryOptimizationWarning.setShouldShow(!AlarmSettings.isIgnoringBatteryOptimizations(this));
        }
        if (batteryOptimizationWarning_sony != null) {
            batteryOptimizationWarning_sony.setShouldShow(AlarmSettings.isSonyStaminaModeEnabled(this));
        }
        if (autostartWarning != null) {
            autostartWarning.setShouldShow(AlarmSettings.isAutostartDisabled(this));
        }
        showWarnings();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarmclock, menu);
        SuntimesNavigation.updateMenuNavigationItems(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_bedtime:
                showBedtime();
                return true;

            case R.id.action_settings:
                showSettings();
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                if (getIntent().getBooleanExtra(EXTRA_SHOWBACK, false))
                    onBackPressed();
                else onHomePressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (isAddDialogShowing()) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        } else if (list.getSelectedRowID() != -1) {
            list.clearSelection();

        } else {
            super.onBackPressed();
        }
    }

    protected void onHomePressed()
    {
        Intent intent = new Intent(this, SuntimesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.transition_swap_in, R.anim.transition_swap_out);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    private final View.OnClickListener onFabMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAddDialog(null);
        }
    };

    private final View.OnClickListener onDeselectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            list.clearSelection();
            deselectButton.hide();    // deselectButton.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * showBedtime
     */
    protected void showBedtime()
    {
        Intent settingsIntent = new Intent(this, BedtimeActivity.class);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    /**
     * showSettings
     */
    protected void showSettings()
    {
        Intent settingsIntent = new Intent(this, SuntimesSettingsActivity.class);
        startActivityForResult(settingsIntent, REQUEST_SETTINGS);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    /**
     * showHelp
     */
    protected void showHelp()
    {
        /**HelpDialog helpDialog = new HelpDialog();
         helpDialog.setContent(getString(R.string.help_alarmclock));
         helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);**/
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        about.putExtra(AboutActivity.EXTRA_ICONID, R.drawable.ic_suntimesalarms);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onEditAlarmResult(int resultCode, Intent data, boolean isNewAlarm)
    {
        dismissAddDialog();
        if (resultCode == RESULT_OK)
        {
            if (data != null)
            {
                AlarmClockItem item = data.getParcelableExtra(AlarmEditActivity.EXTRA_ITEM);
                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, isNewAlarm, false);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    }

    protected void onSettingsResult(int resultCode, Intent data)
    {
        boolean recreateActivity = ((!AppSettings.loadThemePref(AlarmClockActivity.this).equals(appTheme))                           // theme mode changed
                //       || (appThemeOverride != null && !appThemeOverride.themeName().equals(getThemeOverride()))                       // or theme override changed
                || (localeInfo.localeMode != AppSettings.loadLocaleModePref(AlarmClockActivity.this))                             // or localeMode changed
                || ((localeInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE                                              // or customLocale changed
                && !AppSettings.loadLocalePref(AlarmClockActivity.this).equals(localeInfo.customLocale)))
        );
        if (recreateActivity) {
            Handler handler = new Handler();
            handler.postDelayed(recreateRunnable, 0);    // post to end of execution queue (onResume must be allowed to finish before calling recreate)
        }
    }
    private final Runnable recreateRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recreate();

            } else {
                finish();
                startActivity(getIntent());
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void scheduleAlarm(Activity context, AlarmClockItem.AlarmType type, String label, @NonNull String event, @NonNull com.forrestguice.suntimeswidget.calculator.core.Location location)
    {
        AlarmClockItem item = AlarmListDialog.createAlarm(context, type, label, event, location);
        boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item);
        int hour = 6, minutes = 30;    // fallback to an arbitrary alarm time if event does not occur

        if (isSchedulable)
        {
            Calendar alarmTime = Calendar.getInstance(TimeZone.getDefault());
            alarmTime.setTimeInMillis(item.timestamp);
            hour = alarmTime.get(Calendar.HOUR_OF_DAY);
            minutes = alarmTime.get(Calendar.MINUTE);
        }
        scheduleAlarm(context, type, label, event, location, hour, minutes, null);
    }

    public static void scheduleAlarm(Activity context, AlarmClockItem.AlarmType type, String label, String event, com.forrestguice.suntimeswidget.calculator.core.Location location, int hour, int minutes, String timezone)
    {
        TimeZone tz = (timezone == null ? TimeZone.getDefault() : AlarmClockItem.AlarmTimeZone.getTimeZone(timezone, location));
        Calendar calendar0 = Calendar.getInstance(tz);
        calendar0.set(Calendar.HOUR_OF_DAY, hour);
        calendar0.set(Calendar.MINUTE, minutes);

        Calendar calendar1 = Calendar.getInstance(TimeZone.getDefault());
        calendar1.setTimeInMillis(calendar0.getTimeInMillis());

        Intent alarmIntent = new Intent(ACTION_SET_ALARM);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(EXTRA_MESSAGE, label);
        alarmIntent.putExtra(EXTRA_HOUR, ((timezone == null) ? hour : calendar1.get(Calendar.HOUR_OF_DAY)));
        alarmIntent.putExtra(EXTRA_MINUTES, ((timezone == null) ? minutes : calendar1.get(Calendar.MINUTE)));
        alarmIntent.putExtra(AlarmClockActivity.EXTRA_TIMEZONE, timezone);
        alarmIntent.putExtra(AlarmClockActivity.EXTRA_SOLAREVENT, event);
        alarmIntent.putExtra(AlarmClockActivity.EXTRA_ALARMTYPE, type.name());

        Bundle locationBundle = new Bundle();
        locationBundle.putParcelable(AlarmClockActivity.EXTRA_LOCATION, location);
        alarmIntent.putExtra(AlarmClockActivity.EXTRA_LOCATION, locationBundle);

        try {
            context.startActivity(alarmIntent);
        } catch (ActivityNotFoundException e) {
            Log.e("AlarmClockActivity", "scheduleAlarm: " + e);
        }
    }

}
