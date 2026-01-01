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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.support.app.ActivityCompat;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.NotificationManagerCompat;
import com.forrestguice.support.widget.FloatingActionButton;
import com.forrestguice.support.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.settings.fragments.AlarmPrefsFragment;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.getfix.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWarning;
import com.forrestguice.suntimeswidget.actions.LoadActionDialog;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.support.widget.Toolbar;
import com.forrestguice.util.android.AndroidResources;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * AlarmClockActivity
 */
public class AlarmClockLegacyActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverList";

    public static final String ACTION_ADD_ALARM = "suntimes.action.alarmclock.ALARM";
    public static final String ACTION_ADD_NOTIFICATION = "suntimes.action.alarmclock.ADD_NOTIFICATION";

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
    private static final String DIALOGTAG_ACTION1 = "alarmaction1";
    private static final String DIALOGTAG_HELP = "help";

    private static final String KEY_SELECTED_ROWID = "selectedID";
    private static final String KEY_SELECTED_LOCATION = "selectedLocation";
    private static final String KEY_LISTVIEW_TOP = "alarmlisttop";
    private static final String KEY_LISTVIEW_INDEX = "alarmlistindex";

    public static final String WARNINGID_NOTIFICATIONS = "NotificationsWarning";

    private ListView alarmList;
    private View emptyView;

    private FloatingActionButton addButton, addAlarmButton, addNotificationButton;
    private View addAlarmButtonLayout, addNotificationButtonLayout;

    private SuntimesWarning notificationWarning;
    private List<SuntimesWarning> warnings;

    private AlarmItemArrayAdapter adapter = null;
    private Long t_selectedItem = null;
    private Location t_selectedLocation = null;

    private AlarmClockListTask updateTask = null;

    private AppSettings.LocaleInfo localeInfo;

    private int colorAlarmEnabled, colorOn, colorOff, colorEnabled, colorDisabled, colorPressed;
    private int resAddIcon, resCloseIcon;

    public AlarmClockLegacyActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase, localeInfo = new AppSettings.LocaleInfo());
        super.attachBaseContext(context);
    }

    /**
     * OnCreate: the Activity initially created
     * @param savedState a Bundle containing saved state
     */
    @Override
    public void onCreate(Bundle savedState)
    {
        initTheme();
        super.onCreate(savedState);
        initLocale(this);
        setContentView(R.layout.layout_activity_alarmclock);
        initViews(this);
        initWarnings(this, savedState);
        handleIntent(getIntent());
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

    private void initWarnings(Context context, Bundle savedState)
    {
        notificationWarning = new SuntimesWarning(WARNINGID_NOTIFICATIONS);
        warnings = new ArrayList<SuntimesWarning>();
        warnings.add(notificationWarning);
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
            notificationWarning.setMessage(AlarmClockLegacyActivity.this, getString(R.string.notificationsWarning));
            notificationWarning.initWarning(this, alarmList, null);
            notificationWarning.getSnackbar().setAction(getString(R.string.configLabel_alarms_notifications), new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    AlarmPrefsFragment.openNotificationSettings(AlarmClockLegacyActivity.this);
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

    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
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

                String param_event = intent.getStringExtra(AlarmClockActivity.EXTRA_SOLAREVENT);

                //Log.i(TAG, "ACTION_SET_ALARM :: " + param_label + ", " + param_hour + ", " + param_minute + ", " + param_event);
                addAlarm(AlarmClockItem.AlarmType.ALARM, param_label, param_event, param_hour, param_minute, param_vibrate, param_ringtoneUri, param_days);

            } else if (param_action.equals(ACTION_ADD_ALARM)) {
                //Log.d(TAG, "handleIntent: add alarm");
                showAddDialog(AlarmClockItem.AlarmType.ALARM);

            } else if (param_action.equals(ACTION_ADD_NOTIFICATION)) {
                //Log.d(TAG, "handleIntent: add notification");
                showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);

            } else if (param_action.equals(AlarmNotifications.ACTION_DELETE)) {
                //Log.d(TAG, "handleIntent: alarm deleted");
                if (adapter != null && alarmList != null)
                {
                    if (param_data != null)
                    {
                        final AlarmClockItem item = adapter.findItem(ContentUris.parseId(param_data));
                        if (item != null) {
                            adapter.onAlarmDeleted(true, item, alarmList.getChildAt(adapter.getPosition(item)));
                            selectItem = false;
                        }
                    } else {
                        onClearAlarms(true);
                        selectItem = false;
                    }
                }
            }
        }

        long selectedID = intent.getLongExtra(EXTRA_SELECTED_ALARM, -1);
        if (selectItem && selectedID != -1)
        {
            Log.d(TAG, "handleIntent: selected id: " + selectedID);
            t_selectedItem = selectedID;
            setSelectedItem(t_selectedItem);
        }
    }

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(AndroidResources.wrap(context));
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

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateViews(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        AlarmEditDialog alarmEditDialog = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);
        if (alarmEditDialog != null) {
            alarmEditDialog.setAlarmClockAdapterListener(alarmItemDialogListener);
            alarmEditDialog.setOnAcceptedListener(onItemDialogAccepted);
        }

        AlarmEventDialog eventDialog0 = (AlarmEventDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_EVENT_FAB);
        if (eventDialog0 != null)
        {
            initEventDialog(eventDialog0, null);
            eventDialog0.setOnAcceptedListener((eventDialog0.getType() == AlarmClockItem.AlarmType.ALARM) ? onAddAlarmAccepted : onAddNotificationAccepted);
        }

        AlarmEventDialog eventDialog1 = (AlarmEventDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_EVENT);
        if (eventDialog1 != null)
        {
            initEventDialog(eventDialog1, t_selectedLocation);
            eventDialog1.setOnAcceptedListener(onSolarEventChanged);
        }

        AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_REPEAT);
        if (repeatDialog != null) {
            repeatDialog.setOnAcceptedListener(onRepetitionChanged);
        }
        AlarmRepeatDialog repeatDialog1 = (AlarmRepeatDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_REPEAT+1);
        if (repeatDialog1 != null) {
            repeatDialog1.setOnAcceptedListener(onRepetitionChanged1);
        }

        AlarmLabelDialog labelDialog = (AlarmLabelDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_LABEL);
        if (labelDialog != null)
        {
            labelDialog.setOnAcceptedListener(onLabelChanged);
        }

        for (int i=0; i<2; i++)
        {
            LoadActionDialog actionDialog = (LoadActionDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ACTION + i);
            if (actionDialog != null) {
                actionDialog.setOnAcceptedListener(onActionChanged(i));
            }
            LoadActionDialog actionDialog1 = (LoadActionDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ACTION1 + i);
            if (actionDialog1 != null) {
                actionDialog1.setOnAcceptedListener(onActionChanged1(i));
            }
        }

        LocationConfigDialog locationDialog = (LocationConfigDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null) {
            locationDialog.setDialogListener(onLocationChanged);
        }
        LocationConfigDialog locationDialog1 = (LocationConfigDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_LOCATION + 1);
        if (locationDialog1 != null) {
            locationDialog1.setDialogListener(onLocationChanged1);
        }

        AlarmTimeDialog timeDialog = (AlarmTimeDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_TIME);
        if (timeDialog != null) {
            timeDialog.setDialogListener(onTimeChanged);
        }

        if (Build.VERSION.SDK_INT >= 11)
        {
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_OFFSET);
            if (offsetDialog != null) {
                offsetDialog.setOnAcceptedListener(onOffsetChanged);
            }
            AlarmOffsetDialog offsetDialog1 = (AlarmOffsetDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_OFFSET + 1);
            if (offsetDialog1 != null) {
                offsetDialog1.setOnAcceptedListener(onOffsetChanged1);
            }
        } // else // TODO

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
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveWarnings(outState);
        saveListViewPosition(outState);

        if (adapter != null) {
            outState.putString(KEY_SELECTED_ROWID, adapter.getSelectedItem() + "");
        }

        if (t_selectedLocation != null) {
            outState.putSerializable(KEY_SELECTED_LOCATION, t_selectedLocation);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreWarnings(savedState);
        restoreListViewPosition(savedState);

        String idString = savedState.getString(KEY_SELECTED_ROWID);
        if (idString == null) {
            t_selectedItem = null;
        } else {
            try {
                t_selectedItem = Long.parseLong(idString);
                setSelectedItem(t_selectedItem);

            } catch (NumberFormatException e) {
                Log.w(TAG, "onRestoreInstanceState: KEY_SELECTED_ROWID is invalid! not a Long.. ignoring: " + idString);
                t_selectedItem = null;
            }
        }

        t_selectedLocation = (Location) savedState.getSerializable(KEY_SELECTED_LOCATION);
    }

    /**
     * ..based on stack overflow answer by ian
     * https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    private void saveListViewPosition( Bundle outState)
    {
        int i = alarmList.getFirstVisiblePosition();
        outState.putInt(KEY_LISTVIEW_INDEX, i);

        int top = 0;
        View firstItem = alarmList.getChildAt(0);
        if (firstItem != null)
        {
            top = firstItem.getTop() - alarmList.getPaddingTop();
        }
        outState.putInt(KEY_LISTVIEW_TOP, top);
    }

    private void restoreListViewPosition(@NonNull Bundle savedState )
    {
        int i = savedState.getInt(KEY_LISTVIEW_INDEX, -1);
        if (i >= 0)
        {
            int top = savedState.getInt(KEY_LISTVIEW_TOP, 0);
            alarmList.setSelectionFromTop(i, top);
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            boolean showBack = getIntent().getBooleanExtra(EXTRA_SHOWBACK, false);
            if (!showBack) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
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

        collapseFabMenu();

        alarmList = (ListView)findViewById(R.id.alarmList);
        alarmList.setOnItemClickListener(onAlarmItemClick);
        alarmList.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int pos = alarmList.pointToPosition((int)event.getX(), (int)event.getY());
                if ((event.getAction() == MotionEvent.ACTION_DOWN) && pos == -1) {
                    collapseFabMenu();
                    setSelectedItem(-1);
                }
                return false;
            }
        });

        emptyView = findViewById(android.R.id.empty);
        emptyView.setOnClickListener(onEmptyViewClick);
    }

    private final AdapterView.OnItemClickListener onAlarmItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if (adapter != null)
            {
                AlarmClockItem item = adapter.getItem(position);
                if (item != null) {
                    setSelectedItem(item.rowID);
                }
            }
        }
    };

    protected void setSelectedItem(long rowID)
    {
        t_selectedItem = rowID;
        if (adapter != null) {
            adapter.setSelectedItem(rowID);
        } else Log.d(TAG, "setSelectedItem: adapter is null");
    }

    private final View.OnClickListener onAddAlarmButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.ALARM);
            collapseFabMenu();
        }
    };
    private final View.OnClickListener onAddNotificationButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);
            collapseFabMenu();
        }
    };

    private final DialogInterface.OnClickListener onAddAlarmAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            //Log.d("DEBUG", "onAddAlarmAccepted");
            addAlarm(AlarmClockItem.AlarmType.ALARM);
        }
    };
    private final DialogInterface.OnClickListener onAddNotificationAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            //Log.d("DEBUG", "onAddNotificationAccepted");
            addAlarm(AlarmClockItem.AlarmType.NOTIFICATION);
        }
    };

    protected void showAddDialog(AlarmClockItem.AlarmType type)
    {
        //Log.d("DEBUG", "showAddDialog: " + type);
        AlarmEventDialog eventDialog0 = (AlarmEventDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_EVENT_FAB);
        if (eventDialog0 == null)
        {
            final AlarmEventDialog dialog = new AlarmEventDialog();
            dialog.setDialogTitle(getString((type == AlarmClockItem.AlarmType.ALARM) ? R.string.configAction_addAlarm : R.string.configAction_addNotification));
            initEventDialog(dialog, null);
            dialog.setType(type);
            dialog.setChoice(SolarEvents.SUNRISE.name());
            DialogInterface.OnClickListener clickListener = (type == AlarmClockItem.AlarmType.ALARM ? onAddAlarmAccepted : onAddNotificationAccepted);
            dialog.setOnAcceptedListener(clickListener);
            dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT_FAB);
        }
    }

    protected void addAlarm(AlarmClockItem.AlarmType type)
    {
        AlarmEventDialog dialog = (AlarmEventDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_EVENT_FAB);
        String choice = ((dialog != null) ? dialog.getChoice() : null);
        addAlarm(type, "", choice, -1, -1, AlarmSettings.loadPrefVibrateDefault(this), AlarmSettings.getDefaultRingtoneUri(this, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }
    protected void addAlarm(AlarmClockItem.AlarmType type, String label, String event, int hour, int minute, boolean vibrate, Uri ringtoneUri, ArrayList<Integer> repetitionDays)
    {
        //Log.d("DEBUG", "addAlarm: type is " + type.toString());
        final AlarmClockItem alarm = new AlarmClockItem();
        alarm.enabled = AlarmSettings.loadPrefAlarmAutoEnable(this);
        alarm.type = type;
        alarm.label = label;

        alarm.hour = hour;
        alarm.minute = minute;
        alarm.setEvent(event);
        alarm.location = WidgetSettings.loadLocationPref(this, 0);

        alarm.repeating = false;

        alarm.vibrate = vibrate;
        alarm.ringtoneURI = (ringtoneUri != null ? ringtoneUri.toString() : null);
        if (alarm.ringtoneURI != null)
        {
            if (alarm.ringtoneURI.equals(AlarmSettings.VALUE_RINGTONE_DEFAULT)) {
                alarm.ringtoneURI = AlarmSettings.getDefaultRingtoneUri(this, type).toString();
                alarm.ringtoneName = AlarmSettings.getDefaultRingtoneName(this, type);
            } else {
                alarm.ringtoneName = AlarmSettings.getRingtoneName(this, ringtoneUri);
            }
        }

        alarm.setState(alarm.enabled ? AlarmState.STATE_NONE : AlarmState.STATE_DISABLED);
        alarm.modified = true;

        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(this, true, true);
        task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (result) {
                    Log.d(TAG, "onAlarmAdded: " + item.rowID);
                    t_selectedItem = item.rowID;
                    updateViews(AlarmClockLegacyActivity.this);

                    if (item.enabled) {
                        sendBroadcast( AlarmNotifications.getAlarmIntent(AlarmClockLegacyActivity.this, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                    }
                }
            }
        });
        task.execute(alarm);
    }

    /**
     * onSolarEventChanged
     */
    private final DialogInterface.OnClickListener onSolarEventChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            AlarmEventDialog dialog = (AlarmEventDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_EVENT);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && dialog != null)
            {
                item.setEvent(dialog.getChoice());
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockLegacyActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * onEmptyViewClick
     */
    private final View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    /**
     * onUpdateFinished
     * The update task completed creating the adapter; set a listener on the completed adapter.
     */
    private final AlarmClockListTask.AlarmClockListTaskListener onUpdateFinished = new AlarmClockListTask.AlarmClockListTaskListener()
    {
        @Override
        public void onFinished(AlarmItemArrayAdapter result)
        {
            adapter = result;
            if (appThemeOverride != null) {
                adapter.themeAdapterViews(appThemeOverride);
            }
            if (t_selectedItem != null) {
                adapter.setSelectedItem(t_selectedItem);
            }
            adapter.setAdapterListener(onAdapterAction);
        }
    };

    /**
     * onAdapterAction
     * An action was performed on an AlarmItem managed by the adapter; respond to it.
     */
    private final AlarmItemAdapterListener onAdapterAction = new AlarmItemAdapterListener()
    {
        @Override
        public void onTypeChanged(AlarmClockItem forItem) {}

        @Override
        public void onRequestDialog(AlarmClockItem forItem) {
            showAlarmItemDialog(forItem);
        }

        @Override
        public void onRequestLabel(AlarmClockItem forItem)
        {
            pickLabel(forItem);
        }

        @Override
        public void onRequestNote(AlarmClockItem forItem) { /*pickNote(forItem)*/ }

        @Override
        public void onRequestRingtone(AlarmClockItem forItem)
        {
            pickRingtone(forItem);
        }

        @Override
        public void onRequestAction(AlarmClockItem forItem, int actionNum)
        {
            pickAction(forItem, actionNum);
        }

        @Override
        public void onRequestDismissChallenge(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestSnoozeLimit(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestSnoozeLength(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestSolarEvent(AlarmClockItem forItem)
        {
            pickSolarEvent(forItem);
        }

        @Override
        public void onRequestLocation(AlarmClockItem forItem)
        {
            pickLocation(forItem);
        }

        @Override
        public void onRequestTime(final AlarmClockItem forItem)
        {
            if (forItem.getEvent() != null)
            {
                AlertDialog.Builder confirmOverride = new AlertDialog.Builder(AlarmClockLegacyActivity.this);
                confirmOverride.setIcon(android.R.drawable.ic_dialog_alert);
                confirmOverride.setMessage(getString(R.string.alarmtime_dialog_message));
                confirmOverride.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        pickTime(forItem);
                    }
                });
                confirmOverride.setNegativeButton(getString(android.R.string.cancel), null);
                confirmOverride.show();

            } else {
                pickTime(forItem);
            }
        }

        @Override
        public void onRequestOffset(AlarmClockItem forItem)
        {
            pickOffset(forItem);
        }

        @Override
        public void onRequestRepetition(AlarmClockItem forItem)
        {
            pickRepetition(forItem);
        }
    };

    /**
     * onUpdateItem
     */
    private final AlarmDatabaseAdapter.AlarmItemTaskListener onUpdateItem = new AlarmDatabaseAdapter.AlarmItemTaskListener()
    {
        @Override
        public void onFinished(Boolean result, AlarmClockItem item)
        {
            if (result && adapter != null) {
                Log.d("DEBUG", "onUpdateItem");

                adapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * updateViews
     * @param context context
     */
    protected void updateViews(Context context)
    {
        if (updateTask != null) {
            updateTask.cancel(true);
            updateTask = null;
        }

        updateTask = new AlarmClockListTask(this, alarmList, emptyView);
        updateTask.setTaskListener(onUpdateFinished);
        updateTask.execute();
    }

    protected void showAlarmItemDialog(@NonNull AlarmClockItem item)
    {
        AlarmEditDialog dialog = new AlarmEditDialog();
        dialog.initFromItem(item, false);
        dialog.setAlarmClockAdapterListener(alarmItemDialogListener);
        dialog.setOnAcceptedListener(onItemDialogAccepted);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_ITEM);
    }
    private final DialogInterface.OnClickListener onItemDialogAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            AlarmEditDialog itemDialog = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);
            AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, true);
            task.setTaskListener(onUpdateItem);

            if (itemDialog != null) {
                ContentValues values = itemDialog.getOriginal().asContentValues(true);
                itemDialog.getOriginal().fromContentValues(AlarmClockLegacyActivity.this, values);
                task.execute(itemDialog.getItem());
            }
        }
    };

    private final AlarmItemAdapterListener alarmItemDialogListener = new AlarmItemAdapterListener()
    {
        @Override
        public void onTypeChanged(AlarmClockItem forItem) {}

        @Override
        public void onRequestLabel(AlarmClockItem forItem) { /* EMPTY */ }

        @Override
        public void onRequestNote(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestRingtone(AlarmClockItem forItem) {
            // TODO
        }

        @Override
        public void onRequestSolarEvent(AlarmClockItem forItem) {
            // TODO
        }

        @Override
        public void onRequestLocation(AlarmClockItem forItem) {
            pickLocation1(forItem);
        }

        @Override
        public void onRequestTime(AlarmClockItem forItem) {
            // TODO
        }

        @Override
        public void onRequestOffset(AlarmClockItem forItem) {
            pickOffset1(forItem);
        }

        @Override
        public void onRequestRepetition(AlarmClockItem forItem) {
            pickRepetition1(forItem);
        }

        @Override
        public void onRequestAction(AlarmClockItem forItem, int actionNum) {
            pickAction1(forItem, actionNum);
        }

        @Override
        public void onRequestDismissChallenge(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestSnoozeLimit(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestSnoozeLength(AlarmClockItem forItem) {
        }

        @Override
        public void onRequestDialog(AlarmClockItem forItem) { /* EMPTY */ }
    };

    /**
     * pickSolarEvent
     * @param item apply selected solar event to supplied AlarmClockItem
     */
    protected void pickSolarEvent(@NonNull AlarmClockItem item)
    {
        final AlarmEventDialog dialog = new AlarmEventDialog();
        dialog.setDialogTitle(getString((item.type == AlarmClockItem.AlarmType.ALARM) ? R.string.configAction_addAlarm : R.string.configAction_addNotification));
        initEventDialog(dialog, item.location);
        dialog.setChoice(item.getEvent());
        dialog.setOnAcceptedListener(onSolarEventChanged);

        t_selectedItem = item.rowID;
        t_selectedLocation = item.location;
        dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT);
    }

    private void initEventDialog(AlarmEventDialog dialog, Location forLocation)
    {
        SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(this, 0);
        SuntimesMoonData moonData = new SuntimesMoonData(this, 0);
        SuntimesEquinoxSolsticeDataset equinoxData = new SuntimesEquinoxSolsticeDataset(this, 0);

        if (forLocation != null) {
            sunData.setLocation(forLocation);
            moonData.setLocation(forLocation);
            equinoxData.setLocation(forLocation);
        }

        sunData.calculateData(this);
        moonData.calculate(this);
        equinoxData.calculateData(this);
        dialog.setData(this, sunData, moonData, equinoxData);
    }

    /**
     * pickLocation
     * @param item apply location to AlarmClockItem
     */
    protected void pickLocation(@NonNull AlarmClockItem item)
    {
        final LocationConfigDialog dialog = new LocationConfigDialog();
        dialog.setHideTitle(true);
        dialog.setHideMode(true);
        dialog.setLocation(this, item.location);
        dialog.setDialogListener(onLocationChanged);
        t_selectedItem = item.rowID;
        dialog.show(getSupportFragmentManager(), DIALOGTAG_LOCATION);
    }

    private final LocationConfigDialog.LocationConfigDialogListener onLocationChanged = new LocationConfigDialog.LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, LocationMode locationMode, Location location)
        {
            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null)
            {
                item.location = location;
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockLegacyActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
                return true;
            }
            return false;
        }
    };

    /**
     * pickTime
     * @param item apply time to AlarmClockItem
     */
    protected void pickTime(@NonNull AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.timestamp);

            int hour = item.hour;
            if (hour < 0 || hour >= 24) {
                hour = calendar.get(Calendar.HOUR_OF_DAY);
            }

            int minute = item.minute;
            if (minute < 0 || minute >= 60) {
                minute = calendar.get(Calendar.MINUTE);
            }

            AlarmTimeDialog timeDialog = new AlarmTimeDialog();
            timeDialog.setTime(hour, minute);
            timeDialog.set24Hour(SuntimesUtils.is24());
            timeDialog.setTimeZone(item.timezone);
            timeDialog.setLocation(item.location);
            timeDialog.setDialogListener(onTimeChanged);
            t_selectedItem = item.rowID;
            timeDialog.show(getSupportFragmentManager(), DIALOGTAG_TIME);

        }  else {
            Toast.makeText(getApplicationContext(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
        }
    }

    private final AlarmTimeDialog.DialogListener onTimeChanged = new AlarmTimeDialog.DialogListener()
    {
        @Override
        public void onAccepted(AlarmTimeDialog dialog)
        {
            AlarmTimeDialog timeDialog = (AlarmTimeDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_TIME);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && timeDialog != null)
            {
                item.setEvent(null);
                item.hour = timeDialog.getHour();
                item.minute = timeDialog.getMinute();
                item.timezone = timeDialog.getTimeZone();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockLegacyActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }

        @Override
        public void onCanceled(AlarmTimeDialog dialog) {}

        @Override
        public void onChanged(AlarmTimeDialog dialog) {}

        @Override
        public void onLocationClick(AlarmTimeDialog dialog) {
        }

        @Override
        public void onDateClick(AlarmTimeDialog dialog) {
        }
    };

    /**
     * pickOffset
     * @param item apply offset to AlarmClockItem
     */
    protected void pickOffset(@NonNull AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
            int eventType = event != null ? event.getType() : -1;
            AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
            offsetDialog.setShowDays(AlarmEvent.supportsOffsetDays(eventType));
            offsetDialog.setOffset(item.offset);
            offsetDialog.setOnAcceptedListener(onOffsetChanged);
            t_selectedItem = item.rowID;
            offsetDialog.show(getSupportFragmentManager(), DIALOGTAG_OFFSET);

        }  else {
            Toast.makeText(getApplicationContext(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
        }
    }

    /**
     * onOffsetChanged
     */
    private final DialogInterface.OnClickListener onOffsetChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_OFFSET);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && offsetDialog != null)
            {
                item.offset = offsetDialog.getOffset();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockLegacyActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * pickRepetition
     * @param item apply repetition to AlarmClockItem
     */
    protected void pickRepetition(@NonNull AlarmClockItem item)
    {
        AlarmRepeatDialog repeatDialog = new AlarmRepeatDialog();
        repeatDialog.setColorOverrides(colorOn, colorOff, colorDisabled, colorPressed);
        repeatDialog.setRepetition(item.repeating, item.repeatingDays);
        repeatDialog.setOnAcceptedListener(onRepetitionChanged);

        t_selectedItem = item.rowID;
        repeatDialog.show(getSupportFragmentManager(), DIALOGTAG_REPEAT);
    }

    /**
     * onRepetitionChanged
     */
    private final DialogInterface.OnClickListener onRepetitionChanged = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton)
        {
            AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_REPEAT);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && repeatDialog != null)
            {
                item.repeating = repeatDialog.getRepetition();
                item.repeatingDays = repeatDialog.getRepetitionDays();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockLegacyActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, false);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * pickLabel
     * @param item apply label to AlarmClockItem
     */
    protected void pickLabel(@NonNull AlarmClockItem item)
    {
        AlarmLabelDialog dialog = new AlarmLabelDialog();
        dialog.setAccentColor(colorAlarmEnabled);
        dialog.setOnAcceptedListener(onLabelChanged);
        dialog.setLabel(item.label);

        t_selectedItem = item.rowID;
        dialog.show(getSupportFragmentManager(), DIALOGTAG_LABEL);
    }

    /**
     * onLabelChanged
     */
    private final DialogInterface.OnClickListener onLabelChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            AlarmLabelDialog dialog = (AlarmLabelDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_LABEL);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && dialog != null)
            {
                item.label = dialog.getLabel();
                item.modified = true;

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, false);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * pickAction
     * @param item apply actionID to AlarmClockItem
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

        t_selectedItem = item.rowID;
        loadDialog.show(getSupportFragmentManager(), DIALOGTAG_ACTION + actionNum);
    }
    private DialogInterface.OnClickListener onActionChanged(final int actionNum)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                LoadActionDialog dialog = (LoadActionDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ACTION + actionNum);

                AlarmClockItem item = adapter.findItem(t_selectedItem);
                t_selectedItem = null;

                if (item != null && dialog != null)
                {
                    String actionID = dialog.getIntentID();
                    item.setActionID(actionNum, actionID);
                    item.modified = true;

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockLegacyActivity.this, false, false);
                    task.setTaskListener(onUpdateItem);
                    task.execute(item);
                }
            }
        };
    }

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
                    requestDialog.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //noinspection ConstantConditions
                            if (Build.VERSION.SDK_INT >= 16) {
                                t_selectedItem = item.rowID;
                                ActivityCompat.requestPermissions(AlarmClockLegacyActivity.this, new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSION );
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
                    t_selectedItem = item.rowID;
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
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, new AlarmSettings().setDefaultRingtone(this, item.type));   // setDefaultRingtone may block (potential ANR)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (item.ringtoneURI != null ? Uri.parse(item.ringtoneURI) : null));
        t_selectedItem = item.rowID;
        startActivityForResult(intent, REQUEST_RINGTONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_STORAGE_PERMISSION:
                AlarmClockItem item = adapter.findItem(t_selectedItem);
                if (item != null) {
                    ringtonePicker(item);
                } else Log.w(TAG, "onRequestPermissionResult: temp reference to AlarmClockItem was lost!");
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        boolean recreateActivity = false;
        AlarmClockItem item = (adapter != null) ? adapter.findItem(t_selectedItem) : null;

        switch (requestCode)
        {
            case REQUEST_SETTINGS:
                recreateActivity = ((!AppSettings.loadThemePref(AlarmClockLegacyActivity.this).equals(appTheme))                           // theme mode changed
                 //       || (appThemeOverride != null && !appThemeOverride.themeName().equals(getThemeOverride()))                       // or theme override changed
                        || (localeInfo.localeMode != AppSettings.loadLocaleModePref(AlarmClockLegacyActivity.this))                             // or localeMode changed
                        || ((localeInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE                                              // or customLocale changed
                        && !AppSettings.loadLocalePref(AlarmClockLegacyActivity.this).equals(localeInfo.customLocale)))
                );
                if (recreateActivity) {
                    Handler handler = new Handler();
                    handler.postDelayed(recreateRunnable, 0);    // post to end of execution queue (onResume must be allowed to finish before calling recreate)
                }
                break;

            case REQUEST_RINGTONE:
                if (resultCode == RESULT_OK && item != null && data != null)
                {
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
                    item.modified = true;

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(this, false, false);
                    task.setTaskListener(onUpdateItem);
                    task.execute(item);
                } else {
                    Log.d(TAG, "onActivityResult: bad result: " + resultCode + ", " + item + ", " + data);
                }
                break;
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

    /**
     * confirmClearAlarms
     */
    protected void confirmClearAlarms()
    {
        final Context context = this;
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.clearalarms_dialog_title))
                .setMessage(context.getString(R.string.clearalarms_dialog_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(context.getString(R.string.clearalarms_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        clearAlarms(context);
                    }
                })
                .setNegativeButton(context.getString(R.string.clearalarms_dialog_cancel), null);
        confirm.show();
    }

    protected void clearAlarms(final Context context)
    {
        Intent clearIntent = AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DELETE, null);
        context.sendBroadcast(clearIntent);
    }

    protected void onClearAlarms(boolean result)
    {
        if (result) {
            Toast.makeText(this, getString(R.string.clearalarms_toast_success), Toast.LENGTH_LONG).show();
            updateViews(this);
        }
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
        about.putExtra(AboutActivity.EXTRA_ICONID, R.mipmap.ic_launcher_alarms_round);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmClockListTask
     */
    public static class AlarmClockListTask extends AsyncTask<String, AlarmClockItem, AlarmItemArrayAdapter>
    {
        private final AlarmDatabaseAdapter db;
        private final WeakReference<Context> contextRef;
        private final WeakReference<ListView> alarmListRef;
        private final WeakReference<View> emptyViewRef;

        public AlarmClockListTask(Context context, ListView list, View emptyView)
        {
            contextRef = new WeakReference<>(context);
            alarmListRef = new WeakReference<>(list);
            emptyViewRef = new WeakReference<>(emptyView);
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected AlarmItemArrayAdapter doInBackground(String... strings)
        {
            ArrayList<AlarmClockItem> items = new ArrayList<>();

            db.open();
            Cursor cursor = db.getAllAlarms(0, true);
            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                AlarmClockItem item = new AlarmClockItem(contextRef.get(), entryValues);
                if (!item.enabled) {
                    AlarmNotifications.updateAlarmTime(contextRef.get(), item);
                }
                items.add(item);
                publishProgress(item);

                cursor.moveToNext();
            }
            db.close();

            Context context = contextRef.get();
            if (context != null)
                return new AlarmItemArrayAdapter(context, R.layout.layout_listitem_alarmclock, items, theme);
            else return null;
        }

        @Override
        protected void onProgressUpdate(AlarmClockItem... item) {}

        @Override
        protected void onPostExecute(AlarmItemArrayAdapter result)
        {
            if (result != null)
            {
                ListView alarmList = alarmListRef.get();
                if (alarmList != null)
                {
                    alarmList.setAdapter(result);
                    View emptyView = emptyViewRef.get();
                    if (emptyView != null) {
                        alarmList.setEmptyView(emptyView);
                    }
                }

                if (taskListener != null) {
                    taskListener.onFinished(result);
                }
            }
        }

        protected SuntimesTheme theme = null;
        public void setTheme(SuntimesTheme theme) {
            this.theme = theme;
        }

        protected AlarmClockListTaskListener taskListener;
        public void setTaskListener( AlarmClockListTaskListener l )
        {
            taskListener = l;
        }

        public static abstract class AlarmClockListTaskListener
        {
            public void onFinished(AlarmItemArrayAdapter result) {}
        }
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
            case R.id.action_clear:
                confirmClearAlarms();
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

    /**
     * onHomePressed
     */
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
        PopupMenuCompat.forceActionBarIcons(menu);
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

    private final View.OnClickListener onFabMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFabMenu();
        }
    };



    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    protected void pickOffset1(@NonNull AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
            int eventType = event != null ? event.getType() : -1;
            AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
            offsetDialog.setShowDays(AlarmEvent.supportsOffsetDays(eventType));
            offsetDialog.setOffset(item.offset);
            offsetDialog.setOnAcceptedListener(onOffsetChanged1);
            offsetDialog.show(getSupportFragmentManager(), DIALOGTAG_OFFSET + 1);

        }  else {
            Toast.makeText(getApplicationContext(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
        }
    }

    private final DialogInterface.OnClickListener onOffsetChanged1 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_OFFSET + 1);
            AlarmEditDialog itemDialog = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);

            if (itemDialog != null && offsetDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.offset = offsetDialog.getOffset();
                itemDialog.notifyItemChanged();
            }
        }
    };


    protected void pickLocation1(@NonNull AlarmClockItem item)
    {
        final LocationConfigDialog dialog = new LocationConfigDialog();
        dialog.setHideTitle(true);
        dialog.setHideMode(true);
        dialog.setLocation(this, item.location);
        dialog.setDialogListener(onLocationChanged1);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_LOCATION + 1);
    }
    private final LocationConfigDialog.LocationConfigDialogListener onLocationChanged1 = new LocationConfigDialog.LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, LocationMode locationMode, Location location)
        {
            AlarmEditDialog itemDialog = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);
            if (itemDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.location = location;
                itemDialog.notifyItemChanged();
                return true;
            }
            return false;
        }
    };

    protected void pickRepetition1(@NonNull AlarmClockItem item)
    {
        AlarmRepeatDialog repeatDialog = new AlarmRepeatDialog();
        repeatDialog.setColorOverrides(colorOn, colorOff, colorDisabled, colorPressed);
        repeatDialog.setRepetition(item.repeating, item.repeatingDays);
        repeatDialog.setOnAcceptedListener(onRepetitionChanged1);
        repeatDialog.show(getSupportFragmentManager(), DIALOGTAG_REPEAT + 1);
    }
    private final DialogInterface.OnClickListener onRepetitionChanged1 = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton)
        {
            AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_REPEAT + 1);
            AlarmEditDialog itemDialog = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);

            if (itemDialog != null && repeatDialog != null)
            {
                AlarmClockItem item = itemDialog.getItem();
                item.repeating = repeatDialog.getRepetition();
                item.repeatingDays = repeatDialog.getRepetitionDays();
                itemDialog.notifyItemChanged();
            }
        }
    };

    protected void pickAction1(@NonNull final AlarmClockItem item, final int actionNum)
    {
        final LoadActionDialog loadDialog = new LoadActionDialog();
        loadDialog.setOnAcceptedListener(onActionChanged1(actionNum));
        loadDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                loadDialog.setSelected(item.getActionID(actionNum));
            }
        });
        loadDialog.show(getSupportFragmentManager(), DIALOGTAG_ACTION1 + actionNum);
    }
    private DialogInterface.OnClickListener onActionChanged1(final int actionNum)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                LoadActionDialog dialog = (LoadActionDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ACTION1 + actionNum);
                AlarmEditDialog dialog1 = (AlarmEditDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_ITEM);
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
