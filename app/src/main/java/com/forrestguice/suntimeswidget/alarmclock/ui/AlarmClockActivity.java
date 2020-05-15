/**
    Copyright (C) 2018-2020 Forrest Guice
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
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.AlarmDialog;
import com.forrestguice.suntimeswidget.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWarning;
import com.forrestguice.suntimeswidget.actions.LoadActionDialog;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * AlarmClockActivity
 */
public class AlarmClockActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverList";

    public static final String ACTION_ADD_ALARM = "com.forrestguice.suntimeswidget.alarmclock.ALARM";
    public static final String ACTION_ADD_NOTIFICATION = "com.forrestguice.suntimeswidget.alarmclock.ADD_NOTIFICATION";

    public static final String EXTRA_SHOWBACK = "showBack";
    public static final String EXTRA_SOLAREVENT = "solarevent";
    public static final String EXTRA_SELECTED_ALARM = "selectedAlarm";

    public static final int REQUEST_RINGTONE = 10;
    public static final int REQUEST_SETTINGS = 20;
    public static final int REQUEST_STORAGE_PERMISSION = 30;

    private static final String DIALOGTAG_ITEM = "alarmitem";
    private static final String DIALOGTAG_EVENT_FAB = "alarmeventfab";
    private static final String DIALOGTAG_EVENT = "alarmevent";
    private static final String DIALOGTAG_REPEAT = "alarmrepetition";
    private static final String DIALOGTAG_LABEL = "alarmlabel";
    private static final String DIALOGTAG_TIME = "alarmtime";
    private static final String DIALOGTAG_OFFSET = "alarmoffset";
    private static final String DIALOGTAG_LOCATION = "alarmlocation";
    private static final String DIALOGTAG_ACTION = "alarmaction";
    private static final String DIALOGTAG_HELP = "help";

    public static final String WARNINGID_NOTIFICATIONS = "NotificationsWarning";

    private AlarmListDialog list;

    private FloatingActionButton addButton, addAlarmButton, addNotificationButton;
    private View addAlarmButtonLayout, addNotificationButtonLayout;

    private SuntimesWarning notificationWarning;
    private List<SuntimesWarning> warnings;

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
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_SETTINGS:
                onSettingsResult(resultCode, data);
                break;

            case REQUEST_RINGTONE:
                onRingtoneResult(resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_STORAGE_PERMISSION:
                onRingtonePermissionResult(permissions, grantResults);
                break;
        }
    }

    private String appTheme;
    private int appThemeResID;
    private SuntimesTheme appThemeOverride = null;

    private void initTheme()
    {
        appTheme = AppSettings.loadThemePref(this);
        setTheme(appThemeResID = AppSettings.themePrefToStyleId(this, appTheme, null));

        String themeName = AppSettings.getThemeOverride(this, appThemeResID);
        if (themeName != null) {
            Log.i("initTheme", "Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }
    }



    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
        Log.d("DEBUG", "new intent: " + intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent)
    {
        String param_action = intent.getAction();
        intent.setAction(null);

        Uri param_data = intent.getData();
        intent.setData(null);

        boolean selectItem = true;

        if (param_action != null)
        {
            if (param_action.equals(AlarmClock.ACTION_SET_ALARM))
            {
                String param_label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
                int param_hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1);
                int param_minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1);

                ArrayList<Integer> param_days = AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS;
                boolean param_vibrate = AlarmSettings.loadPrefVibrateDefault(this);
                Uri param_ringtoneUri = AlarmSettings.getDefaultRingtoneUri(this, AlarmClockItem.AlarmType.ALARM);
                if (Build.VERSION.SDK_INT >= 19)
                {
                    param_vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE, param_vibrate);

                    String param_ringtoneUriString = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE);
                    if (param_ringtoneUriString != null) {
                        param_ringtoneUri = (param_ringtoneUriString.equals(AlarmClock.VALUE_RINGTONE_SILENT) ? null : Uri.parse(param_ringtoneUriString));
                    }

                    ArrayList<Integer> repeatOnDays = intent.getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS);
                    if (repeatOnDays != null) {
                        param_days = repeatOnDays;
                    }
                }

                SolarEvents param_event = SolarEvents.valueOf(intent.getStringExtra(AlarmClockActivity.EXTRA_SOLAREVENT), null);
                list.createAlarm(this, AlarmClockItem.AlarmType.ALARM, param_label, param_event, null, param_hour, param_minute, null, param_vibrate, param_ringtoneUri, param_days, true);

            } else if (param_action.equals(ACTION_ADD_ALARM)) {
                showAddDialog(AlarmClockItem.AlarmType.ALARM);

            } else if (param_action.equals(ACTION_ADD_NOTIFICATION)) {
                showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);

            } else if (param_action.equals(AlarmNotifications.ACTION_DELETE)) {
                if (param_data != null) {
                    list.notifyAlarmDeleted(ContentUris.parseId(param_data));
                } else {
                    list.notifyAlarmsCleared();
                    selectItem = false;
                }
            }
        } else {
            if (param_data != null) {
                list.notifyAlarmUpdated(ContentUris.parseId(param_data));
            }
        }

        long selectedID = intent.getLongExtra(EXTRA_SELECTED_ALARM, -1);
        if (selectItem && selectedID != -1)
        {
            Log.d(TAG, "handleIntent: selected id: " + selectedID);
            list.setSelectedRowID(selectedID);
        }
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
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreWarnings(savedState);
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

        addButton = (FloatingActionButton) findViewById(R.id.btn_add);
        addButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorAlarmEnabled, colorDisabled, colorPressed));
        addButton.setRippleColor(Color.TRANSPARENT);
        addButton.setOnClickListener(onFabMenuClick);

        addAlarmButtonLayout = findViewById(R.id.layout_btn_addAlarm);
        addAlarmButton = (FloatingActionButton) findViewById(R.id.btn_addAlarm);
        addAlarmButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorPressed, colorDisabled, colorAlarmEnabled));
        addAlarmButton.setRippleColor(Color.TRANSPARENT);
        addAlarmButton.setOnClickListener(onAddAlarmButtonClick);

        addNotificationButtonLayout = findViewById(R.id.layout_btn_addNotification);
        addNotificationButton = (FloatingActionButton) findViewById(R.id.btn_addNotification);
        addNotificationButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorPressed, colorDisabled, colorAlarmEnabled));
        addNotificationButton.setRippleColor(Color.TRANSPARENT);
        addNotificationButton.setOnClickListener(onAddNotificationButtonClick);

        list = (AlarmListDialog) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        list.setOnEmptyViewClick(onEmptyViewClick);
        list.setAdapterListener(listAdapter);

        collapseFabMenu();
    }

    private AlarmListDialog.AdapterListener listAdapter = new AlarmListDialog.AdapterListener() {
        @Override
        public void onItemClicked(AlarmClockItem item) {
            if (list.getSelectedRowID() == item.rowID) {
                showAlarmItemDialog(item, false);
            }
        }

        @Override
        public boolean onItemLongClicked(AlarmClockItem item) {
            showAlarmItemDialog(item, false);
            return true;
        }

        @Override
        public void onAlarmToggled(AlarmClockItem item, boolean enabled) {
            if (enabled) {
                AlarmNotifications.showTimeUntilToast(AlarmClockActivity.this, list.getView(), item);
            }
        }

        @Override
        public void onAlarmAdded(AlarmClockItem item) {
            //showAlarmItemDialog(item, false);
        }

        @Override
        public void onAlarmDeleted(long rowID) {}

        @Override
        public void onAlarmsCleared() {
            Toast.makeText(AlarmClockActivity.this, getString(R.string.clearalarms_toast_success), Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    private View.OnClickListener onAddAlarmButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.ALARM);
            collapseFabMenu();
        }
    };
    private View.OnClickListener onAddNotificationButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);
            collapseFabMenu();
        }
    };

    private DialogInterface.OnClickListener onAddAlarmAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
            AlarmClockItem item = createAlarm(dialog, AlarmClockItem.AlarmType.ALARM, true);
        }
    };
    private DialogInterface.OnClickListener onAddNotificationAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
            AlarmClockItem item = createAlarm(dialog, AlarmClockItem.AlarmType.NOTIFICATION, true);
        }
    };


    protected void showAlarmItemDialog(@NonNull AlarmClockItem item, boolean addItem)
    {
        AlarmEditDialog dialog = new AlarmEditDialog();
        dialog.initFromItem(item, addItem);
        dialog.setAlarmClockAdapterListener(alarmItemDialogListener);
        dialog.setOnAcceptedListener(onItemDialogAccepted);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_ITEM);
    }

    private DialogInterface.OnClickListener onItemDialogAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmEditDialog itemDialog = (AlarmEditDialog)fragments.findFragmentByTag(DIALOGTAG_ITEM);
            AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, (itemDialog.getOriginal() == null), false);
            task.setTaskListener(onUpdateItem);
            task.execute(itemDialog.getItem());
        }
    };
    private AlarmDatabaseAdapter.AlarmItemTaskListener onUpdateItem = new AlarmDatabaseAdapter.AlarmItemTaskListener()
    {
        @Override
        public void onFinished(Boolean result, AlarmClockItem item)
        {
            if (result)
            {
                if (item.enabled) {
                    sendBroadcast( AlarmNotifications.getAlarmIntent(AlarmClockActivity.this, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                }

                if (list != null) {
                    list.reloadAdapter(item.rowID);
                }
            }
        }
    };

    private AlarmItemAdapterListener alarmItemDialogListener = new AlarmItemAdapterListener()
    {
        @Override
        public void onRequestLabel(AlarmClockItem forItem) {
            pickLabel(forItem);
        }

        @Override
        public void onRequestRingtone(AlarmClockItem forItem) {
            pickRingtone(forItem);
        }

        @Override
        public void onRequestSolarEvent(AlarmClockItem forItem) {
            pickSolarEvent(forItem);
        }

        @Override
        public void onRequestLocation(AlarmClockItem forItem) {
            pickLocation(forItem);
        }

        @Override
        public void onRequestTime(AlarmClockItem forItem) {
            // TODO
        }

        @Override
        public void onRequestOffset(AlarmClockItem forItem) {
            pickOffset(forItem);
        }

        @Override
        public void onRequestRepetition(AlarmClockItem forItem) {
            pickRepetition(forItem);
        }

        @Override
        public void onRequestAction(AlarmClockItem forItem, int actionNum) {
            pickAction(forItem, actionNum);
        }

        @Override
        public void onRequestDialog(AlarmClockItem forItem) { /* EMPTY */ }
    };


    protected void showAddDialog(AlarmClockItem.AlarmType type)
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        if (dialog == null)
        {
            dialog = new AlarmCreateDialog();
            dialog.setOnAcceptedListener( type == AlarmClockItem.AlarmType.ALARM ? onAddAlarmAccepted : onAddNotificationAccepted );
            dialog.show(fragments, DIALOGTAG_EVENT_FAB);
        }
    }

    protected AlarmClockItem createAlarm(AlarmCreateDialog dialog, AlarmClockItem.AlarmType type, boolean addToDatabase)
    {
        int hour;
        int minute;
        SolarEvents event;
        String timezone;

        if (dialog.getMode() == 0)
        {
            hour = -1;
            minute = -1;
            timezone = null;
            event = dialog.getEvent();

        } else {
            hour = dialog.getHour();
            minute = dialog.getMinute();
            timezone = dialog.getTimeZone();
            event = null;
        }

        return list.createAlarm(AlarmClockActivity.this, type, "", event, dialog.getLocation(), hour, minute, timezone, AlarmSettings.loadPrefVibrateDefault(this), AlarmSettings.getDefaultRingtoneUri(this, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS, addToDatabase);
    }

    /**
     * updateViews
     * @param context context
     */
    protected void updateViews(Context context) {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void restoreDialogs()
    {
        FragmentManager fragments = getSupportFragmentManager();

        AlarmEditDialog alarmEditDialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);
        if (alarmEditDialog != null) {
            alarmEditDialog.setAlarmClockAdapterListener(alarmItemDialogListener);
            alarmEditDialog.setOnAcceptedListener(onItemDialogAccepted);
        }

        //AlarmDialog eventDialog0 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        //if (eventDialog0 != null)
        //{
            // TODO
            //initEventDialog(eventDialog0, null);
            //eventDialog0.setOnAcceptedListener((eventDialog0.getType() == AlarmClockItem.AlarmType.ALARM) ? onAddAlarmAccepted : onAddNotificationAccepted);
        //}

        AlarmDialog eventDialog1 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
        if (eventDialog1 != null)
        {
            //initEventDialog(eventDialog1, t_selectedLocation);  // TODO
            eventDialog1.setOnAcceptedListener(onSolarEventChanged);
        }

        AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) fragments.findFragmentByTag(DIALOGTAG_REPEAT);
        if (repeatDialog != null) {
            repeatDialog.setOnAcceptedListener(onRepetitionChanged);
        }

        AlarmLabelDialog labelDialog = (AlarmLabelDialog) fragments.findFragmentByTag(DIALOGTAG_LABEL);
        if (labelDialog != null)
        {
            labelDialog.setOnAcceptedListener(onLabelChanged);
        }

        for (int i=0; i<2; i++)
        {
            LoadActionDialog actionDialog = (LoadActionDialog) fragments.findFragmentByTag(DIALOGTAG_ACTION + i);
            if (actionDialog != null) {
                actionDialog.setOnAcceptedListener(onActionChanged(i));
            }
        }

        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null) {
            locationDialog.setDialogListener(onLocationChanged);
        }

        //AlarmTimeDialog timeDialog = (AlarmTimeDialog) fragments.findFragmentByTag(DIALOGTAG_TIME);
        //if (timeDialog != null)
        //{
        //timeDialog.setOnAcceptedListener(onTimeChanged);
        //}

        if (Build.VERSION.SDK_INT >= 11)
        {
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET);
            if (offsetDialog != null) {
                offsetDialog.setOnAcceptedListener(onOffsetChanged);
            }
        } // else // TODO

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initWarnings(Context context, Bundle savedState)
    {
        notificationWarning = new SuntimesWarning(WARNINGID_NOTIFICATIONS);
        warnings = new ArrayList<SuntimesWarning>();
        warnings.add(notificationWarning);
        restoreWarnings(savedState);
    }
    private SuntimesWarning.SuntimesWarningListener warningListener = new SuntimesWarning.SuntimesWarningListener() {
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
            View view = notificationWarning.getSnackbar().getView();
            float iconSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            notificationWarning.initWarning(this, view, getString(R.string.notificationsWarning), iconSize);
            notificationWarning.getSnackbar().setAction(getString(R.string.configLabel_alarms_notifications), new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    SuntimesSettingsActivity.openNotificationSettings(AlarmClockActivity.this);
                }
            });
            notificationWarning.show();
            return;
        }

        // no warnings shown; clear previous (stale) messages
        notificationWarning.dismiss();
    }
    private void checkWarnings()
    {
        notificationWarning.setShouldShow(!NotificationManagerCompat.from(this).areNotificationsEnabled());
        showWarnings();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarmclock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
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
                boolean showBack = getIntent().getBooleanExtra(EXTRA_SHOWBACK, false);
                if (showBack) {
                    onBackPressed();
                } else {
                    onHomePressed();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
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

    private boolean fabMenuExpanded = false;

    private void expandFabMenu()
    {
        addAlarmButtonLayout.setVisibility(View.VISIBLE);
        addNotificationButtonLayout.setVisibility(View.VISIBLE);
        addButton.setImageResource(resCloseIcon);
        fabMenuExpanded = true;
    }

    private void collapseFabMenu()
    {
        addAlarmButtonLayout.setVisibility(View.GONE);
        addNotificationButtonLayout.setVisibility(View.GONE);
        addButton.setImageResource(resAddIcon);
        fabMenuExpanded = false;
    }

    private void toggleFabMenu()
    {
        if (fabMenuExpanded)
            collapseFabMenu();
        else expandFabMenu();
    }

    private View.OnClickListener onFabMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFabMenu();
        }
    };

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
        about.putExtra(AboutActivity.EXTRA_ICONID, R.mipmap.ic_launcher_alarms_round);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
    private Runnable recreateRunnable = new Runnable()
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

    /**
     * pickRingtone
     * @param item apply ringtone to AlarmClockItem
     */
    protected void pickRingtone(@NonNull final AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 16)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    AlertDialog.Builder requestDialog = new AlertDialog.Builder(this);
                    requestDialog.setMessage(Html.fromHtml(getString(R.string.privacy_permission_storage1) + "<br/><br/>" + getString(R.string.privacy_permissiondialog_prompt)));
                    requestDialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //noinspection ConstantConditions
                            if (Build.VERSION.SDK_INT >= 16) {
                                ActivityCompat.requestPermissions(AlarmClockActivity.this, new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSION );
                            }
                        }
                    });
                    requestDialog.setNegativeButton(getString(R.string.privacy_permissiondialog_ignore), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ringtonePicker(item);
                        }
                    });
                    requestDialog.show();

                } else {
                    ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSION );
                }
            } else ringtonePicker(item);
        } else ringtonePicker(item);
    }

    protected void ringtonePicker(@NonNull AlarmClockItem item)
    {
        int ringtoneType = RingtoneManager.TYPE_RINGTONE;
        if (!AlarmSettings.loadPrefAllRingtones(this)) {
            ringtoneType = (item.type == AlarmClockItem.AlarmType.NOTIFICATION ? RingtoneManager.TYPE_NOTIFICATION : RingtoneManager.TYPE_ALARM);
        }

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, item.type.getDisplayString());
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, AlarmSettings.getDefaultRingtoneUri(this, item.type));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (item.ringtoneURI != null ? Uri.parse(item.ringtoneURI) : null));
        startActivityForResult(intent, REQUEST_RINGTONE);
    }

    protected void onRingtonePermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults)
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmEditDialog dialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);
        if (dialog != null) {
            ringtonePicker(dialog.getItem());
        }
    }

    protected void onRingtoneResult(int resultCode, Intent data)
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmEditDialog itemDialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);

        if (resultCode == RESULT_OK && itemDialog != null && data != null)
        {
            AlarmClockItem item = itemDialog.getItem();
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null)
            {
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                if (ringtone != null)
                {
                    String ringtoneName = ringtone.getTitle(this);
                    ringtone.stop();

                    item.ringtoneName = ringtoneName;
                    item.ringtoneURI = uri.toString();
                    Log.d(TAG, "onActivityResult: uri: " + item.ringtoneURI + ", title: " + ringtoneName);

                } else {
                    item.ringtoneName = null;
                    item.ringtoneURI = null;
                    Log.d(TAG, "onActivityResult: uri: " + uri + " <null ringtone>");
                }

            } else {
                item.ringtoneName = null;
                item.ringtoneURI = null;
                Log.d(TAG, "onActivityResult: null uri");
            }
            itemDialog.notifyItemChanged();

        } else {
            Log.d(TAG, "onActivityResult: bad result: " + resultCode + ", " + data);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * pickLabel
     */
    protected void pickLabel(@NonNull AlarmClockItem item)
    {
        AlarmLabelDialog dialog = new AlarmLabelDialog();
        dialog.setAccentColor(colorAlarmEnabled);
        dialog.setOnAcceptedListener(onLabelChanged);
        dialog.setLabel(item.label);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_LABEL);
    }
    private DialogInterface.OnClickListener onLabelChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmLabelDialog dialog = (AlarmLabelDialog) fragments.findFragmentByTag(DIALOGTAG_LABEL);
            AlarmEditDialog dialog1 = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);

            if (dialog1 != null && dialog != null)
            {
                AlarmClockItem item = dialog1.getItem();
                item.label = dialog.getLabel();
                dialog1.notifyItemChanged();
            }
        }
    };

    /**
     * pickOffset
     */
    protected void pickOffset(@NonNull AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            int eventType = item.event != null ? item.event.getType() : -1;
            AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
            offsetDialog.setShowDays(eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON);
            offsetDialog.setOffset(item.offset);
            offsetDialog.setOnAcceptedListener(onOffsetChanged);
            offsetDialog.show(getSupportFragmentManager(), DIALOGTAG_OFFSET + 1);

        }  else {
            Toast.makeText(getApplicationContext(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
        }
    }

    private DialogInterface.OnClickListener onOffsetChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET + 1);
            AlarmEditDialog itemDialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);

            if (itemDialog != null && offsetDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.offset = offsetDialog.getOffset();
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);
                itemDialog.notifyItemChanged();
            }
        }
    };

    /**
     * pickSolarEvent
     */
    protected void pickSolarEvent(@NonNull AlarmClockItem item)
    {
        final AlarmDialog dialog = new AlarmDialog();
        dialog.setDialogTitle((item.type == AlarmClockItem.AlarmType.NOTIFICATION) ? getString(R.string.configAction_addNotification) : getString(R.string.configAction_addAlarm));
        initEventDialog(dialog, item.location);
        dialog.setChoice(item.event);
        dialog.setOnAcceptedListener(onSolarEventChanged);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT);
    }
    private void initEventDialog(AlarmDialog dialog, Location forLocation)
    {
        SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(this, 0);
        SuntimesMoonData moonData = new SuntimesMoonData(this, 0);
        SuntimesEquinoxSolsticeDataset equinoxData = new SuntimesEquinoxSolsticeDataset(this, 0);

        if (forLocation != null) {
            sunData.setLocation(forLocation);
            moonData.setLocation(forLocation);
            equinoxData.setLocation(forLocation);
        }

        sunData.calculateData();
        moonData.calculate();
        equinoxData.calculateData();
        dialog.setData(this, sunData, moonData, equinoxData);
    }
    private DialogInterface.OnClickListener onSolarEventChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmDialog dialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
            AlarmEditDialog dialog1 = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);

            if (dialog1 != null && dialog != null)
            {
                AlarmClockItem item = dialog1.getItem();
                item.event = dialog.getChoice();
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);
                dialog1.notifyItemChanged();
            }
        }
    };

    /**
     * pickLocation
     */
    protected void pickLocation(@NonNull AlarmClockItem item)
    {
        final LocationConfigDialog dialog = new LocationConfigDialog();
        dialog.setHideTitle(true);
        dialog.setHideMode(true);
        dialog.setLocation(this, item.location);
        dialog.setDialogListener(onLocationChanged);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_LOCATION + 1);
    }
    private LocationConfigDialog.LocationConfigDialogListener onLocationChanged = new LocationConfigDialog.LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, WidgetSettings.LocationMode locationMode, Location location)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmEditDialog itemDialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);
            if (itemDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.location = location;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);
                itemDialog.notifyItemChanged();
                return true;
            }
            return false;
        }
    };

    /**
     * pickRepetition
     */
    protected void pickRepetition(@NonNull AlarmClockItem item)
    {
        AlarmRepeatDialog repeatDialog = new AlarmRepeatDialog();
        repeatDialog.setColorOverrides(colorOn, colorOff, colorDisabled, colorPressed);
        repeatDialog.setRepetition(item.repeating, item.repeatingDays);
        repeatDialog.setOnAcceptedListener(onRepetitionChanged);
        repeatDialog.show(getSupportFragmentManager(), DIALOGTAG_REPEAT + 1);
    }
    private DialogInterface.OnClickListener onRepetitionChanged = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) fragments.findFragmentByTag(DIALOGTAG_REPEAT + 1);
            AlarmEditDialog itemDialog = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);

            if (itemDialog != null && repeatDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.repeating = repeatDialog.getRepetition();
                item.repeatingDays = repeatDialog.getRepetitionDays();
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);
                itemDialog.notifyItemChanged();
            }
        }
    };

    /**
     * pickAction
     */
    protected void pickAction(@NonNull final AlarmClockItem item, final int actionNum)
    {
        final LoadActionDialog loadDialog = new LoadActionDialog();
        loadDialog.setOnAcceptedListener(onActionChanged(actionNum));
        loadDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                loadDialog.setSelected(item.getActionID(actionNum));
            }
        });
        loadDialog.show(getSupportFragmentManager(), DIALOGTAG_ACTION + actionNum);
    }
    private DialogInterface.OnClickListener onActionChanged(final int actionNum)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                FragmentManager fragments = getSupportFragmentManager();
                LoadActionDialog dialog = (LoadActionDialog) fragments.findFragmentByTag(DIALOGTAG_ACTION + actionNum);
                AlarmEditDialog dialog1 = (AlarmEditDialog) fragments.findFragmentByTag(DIALOGTAG_ITEM);
                if (dialog != null && dialog1 != null)
                {
                    AlarmClockItem item = dialog1.getItem();
                    item.setActionID(actionNum, dialog.getIntentID());
                    dialog1.notifyItemChanged();
                }
            }
        };
    }

}
