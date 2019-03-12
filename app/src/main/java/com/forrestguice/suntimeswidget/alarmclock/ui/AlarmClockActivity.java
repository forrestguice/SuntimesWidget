/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutDialog;
import com.forrestguice.suntimeswidget.AlarmDialog;
import com.forrestguice.suntimeswidget.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * AlarmClockActivity
 */
public class AlarmClockActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverList";

    public static final String ACTION_ADD_ALARM = "com.forrestguice.suntimeswidget.alarmclock.ADD_ALARM";
    public static final String ACTION_ADD_NOTIFICATION = "com.forrestguice.suntimeswidget.alarmclock.ADD_NOTIFICATION";

    public static final String EXTRA_SHOWBACK = "showBack";
    public static final String EXTRA_SOLAREVENT = "solarevent";
    public static final String EXTRA_SELECTED_ALARM = "selectedAlarm";

    public static final int REQUEST_RINGTONE = 10;
    public static final int REQUEST_SETTINGS = 20;

    private static final String DIALOGTAG_EVENT_FAB = "alarmeventfab";
    private static final String DIALOGTAG_EVENT = "alarmevent";
    private static final String DIALOGTAG_REPEAT = "alarmrepetition";
    private static final String DIALOGTAG_LABEL = "alarmlabel";
    private static final String DIALOGTAG_TIME = "alarmtime";
    private static final String DIALOGTAG_OFFSET = "alarmoffset";
    private static final String DIALOGTAG_LOCATION = "alarmlocation";
    private static final String DIALOGTAG_HELP = "help";
    private static final String DIALOGTAG_ABOUT = "about";

    private static final String KEY_SELECTED_ROWID = "selectedID";
    private static final String KEY_SELECTED_LOCATION = "selectedLocation";
    private static final String KEY_LISTVIEW_TOP = "alarmlisttop";
    private static final String KEY_LISTVIEW_INDEX = "alarmlistindex";

    private ActionBar actionBar;
    private ListView alarmList;
    private View emptyView;
    private FloatingActionButton addAlarmButton, addNotificationButton;

    private AlarmClockAdapter adapter = null;
    private Long t_selectedItem = null;
    private Location t_selectedLocation = null;

    private AlarmClockListTask updateTask = null;

    private AppSettings.LocaleInfo localeInfo;

    private int colorAlarmEnabled, colorOn, colorOff, colorEnabled, colorDisabled, colorPressed;

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

    /**
     * OnCreate: the Activity initially created
     * @param icicle a Bundle containing saved state
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        initTheme();
        super.onCreate(icicle);
        initLocale(this);
        setContentView(R.layout.layout_activity_alarmclock);
        initViews(this);
        handleIntent(getIntent());
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
        SolarEvents.initDisplayStrings(context);

        int[] attrs = { R.attr.alarmColorEnabled, android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor, android.R.attr.textColor };
        TypedArray a = context.obtainStyledAttributes(attrs);
        colorAlarmEnabled = colorOn = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled_dark));
        colorEnabled = ContextCompat.getColor(context, a.getResourceId(1, android.R.color.primary_text_dark));
        colorDisabled = ContextCompat.getColor(context, a.getResourceId(2, R.color.text_disabled_dark));
        colorPressed = ContextCompat.getColor(context, a.getResourceId(3, R.color.sunIcon_color_setting_dark));
        colorOff = ContextCompat.getColor(context, a.getResourceId(4, R.color.grey_600));
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

        FragmentManager fragments = getSupportFragmentManager();
        AlarmDialog eventDialog0 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        if (eventDialog0 != null)
        {
            initEventDialog(eventDialog0, null);
            eventDialog0.setOnAcceptedListener((eventDialog0.getType() == AlarmClockItem.AlarmType.ALARM) ? onAddAlarmAccepted : onAddNotificationAccepted);
        }

        AlarmDialog eventDialog1 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
        if (eventDialog1 != null)
        {
            initEventDialog(eventDialog1, t_selectedLocation);
            eventDialog1.setOnAcceptedListener(onSolarEventChanged);
        }

        AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) fragments.findFragmentByTag(DIALOGTAG_REPEAT);
        if (repeatDialog != null)
        {
            repeatDialog.setOnAcceptedListener(onRepetitionChanged);
        }

        AlarmLabelDialog labelDialog = (AlarmLabelDialog) fragments.findFragmentByTag(DIALOGTAG_LABEL);
        if (labelDialog != null)
        {
            labelDialog.setOnAcceptedListener(onLabelChanged);
        }

        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null)
        {
            locationDialog.setDialogListener(onLocationChanged);
        }

        AlarmTimeDialog timeDialog = (AlarmTimeDialog) fragments.findFragmentByTag(DIALOGTAG_TIME);
        if (timeDialog != null)
        {
            timeDialog.setOnAcceptedListener(onTimeChanged);
        }

        if (Build.VERSION.SDK_INT >= 11)
        {
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET);
            if (offsetDialog != null) {
                offsetDialog.setOnAcceptedListener(onOffsetChanged);
            }
        } // else // TODO
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
        saveListViewPosition(outState);

        if (adapter != null) {
            outState.putString(KEY_SELECTED_ROWID, adapter.getSelectedItem() + "");
        }

        if (t_selectedLocation != null) {
            outState.putParcelable(KEY_SELECTED_LOCATION, t_selectedLocation);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
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

        t_selectedLocation = savedState.getParcelable(KEY_SELECTED_LOCATION);
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
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            boolean showBack = getIntent().getBooleanExtra(EXTRA_SHOWBACK, false);
            if (!showBack) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
            }
        }

        addAlarmButton = (FloatingActionButton) findViewById(R.id.btn_addAlarm);
        addAlarmButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorPressed, colorDisabled, colorAlarmEnabled));
        addAlarmButton.setRippleColor(Color.TRANSPARENT);
        addAlarmButton.setOnClickListener(onAddAlarmButtonClick);

        addNotificationButton = (FloatingActionButton) findViewById(R.id.btn_addNotification);
        addNotificationButton.setBackgroundTintList(SuntimesUtils.colorStateList(colorPressed, colorDisabled, colorAlarmEnabled));
        addNotificationButton.setRippleColor(Color.TRANSPARENT);
        addNotificationButton.setOnClickListener(onAddNotificationButtonClick);

        alarmList = (ListView)findViewById(R.id.alarmList);
        alarmList.setOnItemClickListener(onAlarmItemClick);
        alarmList.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int pos = alarmList.pointToPosition((int)event.getX(), (int)event.getY());
                if ((event.getAction() == MotionEvent.ACTION_DOWN) && pos == -1) {
                    setSelectedItem(-1);
                }
                return false;
            }
        });

        emptyView = findViewById(android.R.id.empty);
        emptyView.setOnClickListener(onEmptyViewClick);
    }

    private AdapterView.OnItemClickListener onAlarmItemClick = new AdapterView.OnItemClickListener() {
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

    private View.OnClickListener onAddAlarmButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.ALARM);
        }
    };
    private View.OnClickListener onAddNotificationButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            showAddDialog(AlarmClockItem.AlarmType.NOTIFICATION);
        }
    };

    private DialogInterface.OnClickListener onAddAlarmAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            //Log.d("DEBUG", "onAddAlarmAccepted");
            addAlarm(AlarmClockItem.AlarmType.ALARM);
        }
    };
    private DialogInterface.OnClickListener onAddNotificationAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which) {
            //Log.d("DEBUG", "onAddNotificationAccepted");
            addAlarm(AlarmClockItem.AlarmType.NOTIFICATION);
        }
    };

    protected void showAddDialog(AlarmClockItem.AlarmType type)
    {
        //Log.d("DEBUG", "showAddDialog: " + type);
        FragmentManager fragments = getSupportFragmentManager();
        AlarmDialog eventDialog0 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        if (eventDialog0 == null)
        {
            final AlarmDialog dialog = new AlarmDialog();
            dialog.setDialogTitle((type == AlarmClockItem.AlarmType.NOTIFICATION) ? getString(R.string.configAction_addNotification) : getString(R.string.configAction_addAlarm));
            initEventDialog(dialog, null);
            dialog.setType(type);
            dialog.setChoice(SolarEvents.SUNRISE);
            DialogInterface.OnClickListener clickListener = (type == AlarmClockItem.AlarmType.ALARM ? onAddAlarmAccepted : onAddNotificationAccepted);
            dialog.setOnAcceptedListener(clickListener);
            dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT_FAB);
        }
    }

    protected void addAlarm(AlarmClockItem.AlarmType type)
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmDialog dialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        addAlarm(type, "", dialog.getChoice(), -1, -1, AlarmSettings.loadPrefVibrateDefault(this), AlarmSettings.getDefaultRingtoneUri(this, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }
    protected void addAlarm(AlarmClockItem.AlarmType type, String label, SolarEvents event, int hour, int minute, boolean vibrate, Uri ringtoneUri, ArrayList<Integer> repetitionDays)
    {
        //Log.d("DEBUG", "addAlarm: type is " + type.toString());
        final AlarmClockItem alarm = new AlarmClockItem();
        alarm.enabled = AlarmSettings.loadPrefAlarmAutoEnable(AlarmClockActivity.this);
        alarm.type = type;
        alarm.label = label;

        alarm.hour = hour;
        alarm.minute = minute;
        alarm.event = event;
        alarm.location = WidgetSettings.loadLocationPref(AlarmClockActivity.this, 0);

        alarm.repeating = false;

        alarm.vibrate = vibrate;
        alarm.ringtoneURI = (ringtoneUri != null ? ringtoneUri.toString() : null);
        if (alarm.ringtoneURI != null)
        {
            Ringtone ringtone = RingtoneManager.getRingtone(AlarmClockActivity.this, ringtoneUri);
            alarm.ringtoneName = ringtone.getTitle(AlarmClockActivity.this);
            ringtone.stop();
        }

        alarm.setState(alarm.enabled ? AlarmState.STATE_NONE : AlarmState.STATE_DISABLED);
        alarm.modified = true;

        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, true, true);
        task.setTaskListener(new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (result) {
                    Log.d(TAG, "onAlarmAdded: " + item.rowID);
                    t_selectedItem = item.rowID;
                    updateViews(AlarmClockActivity.this);

                    if (item.enabled) {
                        sendBroadcast( AlarmNotifications.getAlarmIntent(AlarmClockActivity.this, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                    }
                }
            }
        });
        task.execute(alarm);
    }

    /**
     * onSolarEventChanged
     */
    private DialogInterface.OnClickListener onSolarEventChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmDialog dialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && dialog != null)
            {
                item.event = dialog.getChoice();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * onEmptyViewClick
     */
    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    /**
     * onUpdateFinished
     * The update task completed creating the adapter; set a listener on the completed adapter.
     */
    private AlarmClockListTask.AlarmClockListTaskListener onUpdateFinished = new AlarmClockListTask.AlarmClockListTaskListener()
    {
        @Override
        public void onFinished(AlarmClockAdapter result)
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
    private AlarmClockAdapter.AlarmClockAdapterListener onAdapterAction = new AlarmClockAdapter.AlarmClockAdapterListener()
    {
        @Override
        public void onRequestLabel(AlarmClockItem forItem)
        {
            pickLabel(forItem);
        }

        @Override
        public void onRequestRingtone(AlarmClockItem forItem)
        {
            pickRingtone(forItem);
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
            if (forItem.event != null)
            {
                AlertDialog.Builder confirmOverride = new AlertDialog.Builder(AlarmClockActivity.this);
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
    private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onUpdateItem = new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
    {
        @Override
        public void onFinished(Boolean result, AlarmClockItem item)
        {
            if (result && adapter != null) {
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

    /**
     * pickSolarEvent
     * @param item apply selected solar event to supplied AlarmClockItem
     */
    protected void pickSolarEvent(@NonNull AlarmClockItem item)
    {
        final AlarmDialog dialog = new AlarmDialog();
        dialog.setDialogTitle((item.type == AlarmClockItem.AlarmType.NOTIFICATION) ? getString(R.string.configAction_addNotification) : getString(R.string.configAction_addAlarm));
        initEventDialog(dialog, item.location);
        dialog.setChoice(item.event);
        dialog.setOnAcceptedListener(onSolarEventChanged);

        t_selectedItem = item.rowID;
        t_selectedLocation = item.location;
        dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT);
    }

    private void initEventDialog(AlarmDialog dialog, Location forLocation)
    {
        SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(this, 0);
        SuntimesMoonData moonData = new SuntimesMoonData(this, 0);

        if (forLocation != null) {
            sunData.setLocation(forLocation);
            moonData.setLocation(forLocation);
        }

        sunData.calculateData();
        moonData.calculate();
        dialog.setData(this, sunData, moonData);
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

    private LocationConfigDialog.LocationConfigDialogListener onLocationChanged = new LocationConfigDialog.LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, WidgetSettings.LocationMode locationMode, Location location)
        {
            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null)
            {
                item.location = location;
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
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
            timeDialog.setOnAcceptedListener(onTimeChanged);
            t_selectedItem = item.rowID;
            timeDialog.show(getSupportFragmentManager(), DIALOGTAG_TIME);

        }  else {
            Toast.makeText(getApplicationContext(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
        }
    }

    private DialogInterface.OnClickListener onTimeChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmTimeDialog timeDialog = (AlarmTimeDialog) fragments.findFragmentByTag(DIALOGTAG_TIME);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && timeDialog != null)
            {
                item.event = null;
                item.hour = timeDialog.getHour();
                item.minute = timeDialog.getMinute();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }

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
            AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
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
    private DialogInterface.OnClickListener onOffsetChanged = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && offsetDialog != null)
            {
                item.offset = offsetDialog.getOffset();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
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
    private DialogInterface.OnClickListener onRepetitionChanged = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmRepeatDialog repeatDialog = (AlarmRepeatDialog) fragments.findFragmentByTag(DIALOGTAG_REPEAT);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && repeatDialog != null)
            {
                item.repeating = repeatDialog.getRepetition();
                item.repeatingDays = repeatDialog.getRepetitionDays();
                item.modified = true;
                AlarmNotifications.updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, false);
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
    private DialogInterface.OnClickListener onLabelChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmLabelDialog dialog = (AlarmLabelDialog) fragments.findFragmentByTag(DIALOGTAG_LABEL);

            AlarmClockItem item = adapter.findItem(t_selectedItem);
            t_selectedItem = null;

            if (item != null && dialog != null)
            {
                item.label = dialog.getLabel();
                item.modified = true;

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, false);
                task.setTaskListener(onUpdateItem);
                task.execute(item);
            }
        }
    };

    /**
     * pickRingtone
     * @param item apply ringtone to AlarmClockItem
     */
    protected void pickRingtone(@NonNull AlarmClockItem item)
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
        t_selectedItem = item.rowID;
        startActivityForResult(intent, REQUEST_RINGTONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        boolean recreateActivity = false;
        AlarmClockItem item = adapter.findItem(t_selectedItem);

        switch (requestCode)
        {
            case REQUEST_SETTINGS:
                recreateActivity = ((!AppSettings.loadThemePref(AlarmClockActivity.this).equals(appTheme))                           // theme mode changed
                 //       || (appThemeOverride != null && !appThemeOverride.themeName().equals(getThemeOverride()))                       // or theme override changed
                        || (localeInfo.localeMode != AppSettings.loadLocaleModePref(AlarmClockActivity.this))                             // or localeMode changed
                        || ((localeInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE                                              // or customLocale changed
                        && !AppSettings.loadLocalePref(AlarmClockActivity.this).equals(localeInfo.customLocale)))
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
                }
                break;
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
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.setIconID(R.mipmap.ic_launcher_alarms);
        aboutDialog.setAppName(R.string.app_name_alarmclock);
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmClockListTask
     */
    public static class AlarmClockListTask extends AsyncTask<String, AlarmClockItem, AlarmClockAdapter>
    {
        private AlarmDatabaseAdapter db;
        private WeakReference<Context> contextRef;
        private WeakReference<ListView> alarmListRef;
        private WeakReference<View> emptyViewRef;

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
        protected AlarmClockAdapter doInBackground(String... strings)
        {
            ArrayList<AlarmClockItem> items = new ArrayList<>();

            db.open();
            Cursor cursor = db.getAllAlarms(0, true);
            while (!cursor.isAfterLast())
            {
                ContentValues entryValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

                AlarmClockItem item = new AlarmClockItem(entryValues);
                AlarmNotifications.updateAlarmTime(contextRef.get(), item);
                items.add(item);
                publishProgress(item);

                cursor.moveToNext();
            }
            db.close();

            Context context = contextRef.get();
            if (context != null)
                return new AlarmClockAdapter(context, items, theme);
            else return null;
        }

        @Override
        protected void onProgressUpdate(AlarmClockItem... item) {}

        @Override
        protected void onPostExecute(AlarmClockAdapter result)
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
            public void onFinished(AlarmClockAdapter result) {}
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
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

}
