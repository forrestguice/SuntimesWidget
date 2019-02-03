/**
    Copyright (C) 2018 Forrest Guice
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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutDialog;
import com.forrestguice.suntimeswidget.AlarmDialog;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AlarmClockActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverList";

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
    private FloatingActionButton actionButton;

    private AlarmClockAdapter adapter = null;
    private Long t_selectedItem = null;
    private Location t_selectedLocation = null;

    private AlarmClockListTask updateTask = null;
    private static final SuntimesUtils utils = new SuntimesUtils();

    public AlarmClockActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
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
    private void initTheme()
    {
        appTheme = AppSettings.loadThemePref(this);
        setTheme(appThemeResID = AppSettings.themePrefToStyleId(this, appTheme, null));
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

        if (param_action != null)
        {
            if (param_action.equals(AlarmClock.ACTION_SET_ALARM))
            {
                String param_label = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
                int param_hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1);
                int param_minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1);

                ArrayList<Integer> param_days = getDefaultRepetition(this);
                boolean param_vibrate = getDefaultVibrate(this);
                Uri param_ringtoneUri = getDefaultRingtoneUri(this, AlarmClockItem.AlarmType.ALARM);
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

                Log.i(TAG, "ACTION_SET_ALARM :: " + param_label + ", " + param_hour + ", " + param_minute + ", " + param_event);
                addAlarm(AlarmClockItem.AlarmType.ALARM, param_label, param_event, param_hour, param_minute, param_vibrate, param_ringtoneUri, param_days);
            }
        }

        long selectedID = intent.getLongExtra(EXTRA_SELECTED_ALARM, -1);
        if (selectedID != -1)
        {
            Log.d(TAG, "handleIntent: selected id: " + selectedID);
            t_selectedItem = selectedID;
            setSelectedItem(t_selectedItem);
        }
    }

    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
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
            eventDialog0.setOnAcceptedListener(onActionButtonAccepted);
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

        AlarmOffsetDialog offsetDialog = (AlarmOffsetDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET);
        if (offsetDialog != null)
        {
            offsetDialog.setOnAcceptedListener(onOffsetChanged);
        }
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

        actionButton = (FloatingActionButton) findViewById(R.id.btn_addAlarm);
        actionButton.setOnClickListener(onActionButtonClick);

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

    /**
     * onActionButtonClick
     */
    private View.OnClickListener onActionButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmDialog eventDialog0 = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
            if (eventDialog0 == null)
            {
                final AlarmDialog dialog = new AlarmDialog();
                initEventDialog(dialog, null);
                dialog.setChoice(SolarEvents.SUNRISE);
                dialog.setOnAcceptedListener(onActionButtonAccepted);
                dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT_FAB);
            }
        }
    };

    private DialogInterface.OnClickListener onActionButtonAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            addAlarm(AlarmClockItem.AlarmType.ALARM);
        }
    };

    protected void addAlarm(AlarmClockItem.AlarmType type)
    {
        FragmentManager fragments = getSupportFragmentManager();
        AlarmDialog dialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT_FAB);
        addAlarm(type, "", dialog.getChoice(), -1, -1, getDefaultVibrate(this), getDefaultRingtoneUri(this, type), getDefaultRepetition(this));
    }
    protected void addAlarm(AlarmClockItem.AlarmType type, String label, SolarEvents event, int hour, int minute, boolean vibrate, Uri ringtoneUri, ArrayList<Integer> repetitionDays)
    {
        final AlarmClockItem alarm = new AlarmClockItem();
        alarm.enabled = getDefaultNewAlarmsEnabled(this);
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
                }
            }
        });
        task.execute(alarm);
    }

    public static boolean getDefaultNewAlarmsEnabled(Context context)
    {
        return true;
    }

    public static Uri getDefaultRingtoneUri(Context context, AlarmClockItem.AlarmType type)
    {
        switch (type)
        {
            case ALARM:
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
            case NOTIFICATION:
            default:
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    public static boolean getDefaultVibrate(Context context)
    {
        return false;
    }

    public static ArrayList<Integer> getDefaultRepetition(Context context)
    {
        return AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS;
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
                updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);   // TODO: reset state and reschedule on SolarEventChanged
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
        public void onRequestTime(AlarmClockItem forItem)
        {
            pickTime(forItem);
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
                updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);    // TODO: reset state and reschedule on locationChanged
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
                updateAlarmTime(AlarmClockActivity.this, item);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);          // TODO: reset state and reschedule on time changed
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
        AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
        offsetDialog.setOffset(item.offset);
        offsetDialog.setOnAcceptedListener(onOffsetChanged);
        t_selectedItem = item.rowID;
        offsetDialog.show(getSupportFragmentManager(), DIALOGTAG_OFFSET);
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
                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(AlarmClockActivity.this, false, true);
                task.setTaskListener(onUpdateItem);  // TODO: reset state and reschedule on offset changed
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
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, getDefaultRingtoneUri(this, item.type));
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
                 //       || (localeInfo.localeMode != AppSettings.loadLocaleModePref(SuntimesActivity.this))                             // or localeMode changed
                 //       || ((localeInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE                                              // or customLocale changed
                 //       && !AppSettings.loadLocalePref(SuntimesActivity.this).equals(localeInfo.customLocale))));
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
                    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                    String ringtoneName = ringtone.getTitle(this);
                    ringtone.stop();

                    item.ringtoneName = (uri != null ? ringtoneName : null);
                    item.ringtoneURI = (uri != null ? uri.toString() : null);
                    item.modified = true;
                    Log.d(TAG, "onActivityResult: uri: " + item.ringtoneURI + ", title: " + ringtoneName);

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
     * clearAlarms
     */
    protected void clearAlarms()
    {
        final Context context = this;
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.clearalarms_dialog_title))
                .setMessage(context.getString(R.string.clearalarms_dialog_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(context.getString(R.string.clearalarms_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        AlarmDatabaseAdapter.AlarmDeleteTask clearTask = new AlarmDatabaseAdapter.AlarmDeleteTask(context);
                        clearTask.setTaskListener(new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
                        {
                            @Override
                            public void onFinished(Boolean result)
                            {
                                if (result)
                                {
                                    Toast.makeText(context, context.getString(R.string.clearalarms_toast_success), Toast.LENGTH_LONG).show();
                                    updateViews(context);
                                }
                            }
                        });
                        clearTask.execute();
                    }
                })
                .setNegativeButton(context.getString(R.string.clearalarms_dialog_cancel), null);
        confirm.show();
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
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_alarmclock));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }

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
                updateAlarmTime(contextRef.get(), item);
                items.add(item);
                publishProgress(item);

                cursor.moveToNext();
            }
            db.close();

            Context context = contextRef.get();
            if (context != null)
                return new AlarmClockAdapter(context, items);
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

    /**
     * updateAlarmTime
     * @param item AlarmClockItem
     */
    protected static void updateAlarmTime(Context context, final AlarmClockItem item)
    {
        Calendar now = Calendar.getInstance();
        Calendar eventTime;
        if (item.location != null && item.event != null)
        {
            // Event Mode; set timestamp based on SolarEvent
            switch (item.event.getType())
            {
                case SolarEvents.TYPE_MOON:
                    SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
                    moonData.setLocation(item.location);
                    moonData.calculate();
                    eventTime = (item.event.isRising() ? moonData.moonriseCalendarToday() : moonData.moonsetCalendarToday());
                    if (now.after(eventTime)) {
                        eventTime = (item.event.isRising() ? moonData.moonriseCalendarTomorrow() : moonData.moonsetCalendarTomorrow());
                    }
                    break;

                case SolarEvents.TYPE_SUN:
                default:
                    SuntimesRiseSetData sunData = new SuntimesRiseSetData(context, 0);
                    sunData.setLocation(item.location);
                    WidgetSettings.TimeMode timeMode = item.event.toTimeMode();
                    sunData.setTimeMode(timeMode != null ? timeMode : WidgetSettings.TimeMode.OFFICIAL);
                    sunData.calculate();
                    eventTime = (item.event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
                    if (now.after(eventTime)) {
                        eventTime = (item.event.isRising() ? sunData.sunriseCalendarOther() : sunData.sunsetCalendarOther());
                    }
                    break;
            }
            item.hour = eventTime.get(Calendar.HOUR_OF_DAY);
            item.minute = eventTime.get(Calendar.MINUTE);

        } else {
            // Clock Mode; set timestamp from hour and minute
            eventTime = Calendar.getInstance();
            if (item.hour >= 0 && item.hour < 24) {
                eventTime.set(Calendar.HOUR_OF_DAY, item.hour);
            }
            if (item.minute >= 0 && item.minute < 60) {
                eventTime.set(Calendar.MINUTE, item.minute);
            }
            while (now.after(eventTime)) {
                eventTime.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        item.timestamp = eventTime.getTimeInMillis();
        item.modified = true;
    }

    /**
     * AlarmClockAdapter
     */
    @SuppressWarnings("Convert2Diamond")
    public static class AlarmClockAdapter extends ArrayAdapter<AlarmClockItem>
    {
        private Context context;
        private long selectedItem;
        private ArrayList<AlarmClockItem> items;
        private int iconAlarm, iconNotification, iconSoundEnabled, iconSoundDisabled;
        private Drawable alarmEnabledBG, alarmDisabledBG;
        private int alarmSelectedColor;
        private int disabledTextColor, pressedTextColor;

        public AlarmClockAdapter(Context context)
        {
            super(context, R.layout.layout_listitem_alarmclock);
            initAdapter(context);
            this.items = new ArrayList<>();
        }

        public AlarmClockAdapter(Context context, ArrayList<AlarmClockItem> items)
        {
            super(context, R.layout.layout_listitem_alarmclock, items);
            initAdapter(context);
            this.items = items;
        }

        @SuppressLint("ResourceType")
        private void initAdapter(Context context)
        {
            this.context = context;

            int[] attrs = { R.attr.alarmCardEnabled, R.attr.alarmCardDisabled,
                    R.attr.icActionAlarm, R.attr.icActionNotification, R.attr.icActionSoundEnabled, R.attr.icActionSoundDisabled,
                    R.attr.text_disabledColor, R.attr.gridItemSelected, R.attr.buttonPressColor};
            TypedArray a = context.obtainStyledAttributes(attrs);
            alarmEnabledBG = ContextCompat.getDrawable(context, a.getResourceId(0, R.drawable.card_alarmitem_enabled_dark));
            alarmDisabledBG = ContextCompat.getDrawable(context, a.getResourceId(1, R.drawable.card_alarmitem_disabled_dark));
            iconAlarm = a.getResourceId(2, R.drawable.ic_action_alarms);
            iconNotification = a.getResourceId(3, R.drawable.ic_action_notification);
            iconSoundEnabled = a.getResourceId(4, R.drawable.ic_action_soundenabled);
            iconSoundDisabled = a.getResourceId(5, R.drawable.ic_action_sounddisabled);
            disabledTextColor = ContextCompat.getColor(context, a.getResourceId(6, R.color.text_disabled_dark));
            alarmSelectedColor = ContextCompat.getColor(context, a.getResourceId(7, R.color.grid_selected_dark));
            pressedTextColor = ContextCompat.getColor(context, a.getResourceId(8, R.color.btn_tint_pressed_dark));
            a.recycle();
        }

        @Override
        public void add(AlarmClockItem item)
        {
            this.items.add(item);
            super.add(item);
        }

        @Override
        public void addAll (AlarmClockItem... items)
        {
            this.items.addAll(0, Arrays.asList(items));
            super.addAll(items);
        }

        /**
         * Retrieve an AlarmClockItem from the adapter using its rowID.
         * @param rowID the item's rowID
         * @return an AlarmClockItem or null if not found
         */
        public AlarmClockItem findItem( Long rowID )
        {
            if (rowID != null) {
                for (AlarmClockItem item : items) {
                    if (item != null && item.rowID == rowID) {
                        return item;
                    }
                }
            }
            return null;
        }

        public void setSelectedItem(long rowID)
        {
            selectedItem = rowID;
            notifyDataSetChanged();
        }

        public long getSelectedItem() {
             return selectedItem;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return itemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return itemView(position, convertView, parent);
        }

        private View itemView(int position, View convertView, @NonNull final ViewGroup parent)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.layout_listitem_alarmclock, parent, false);  // always re-inflate (ignore convertView)
            final AlarmClockItem item = ((position >= 0 && position < items.size()) ? items.get(position) : null);
            if (item == null)
            {
                Log.d(TAG, "itemView: position " + position + " is null!");
                view.setVisibility(View.GONE);
                return view;
            }
            final boolean isSelected = (item.rowID == selectedItem);

            //ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            //icon.setImageResource(item.icon);

            final View card = view.findViewById(R.id.layout_alarmcard);
            if (card != null) {
                card.setBackground(item.enabled ? alarmEnabledBG : alarmDisabledBG);
            }

            final View cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
            if (cardBackdrop != null && isSelected) {
                cardBackdrop.setBackgroundColor( alarmSelectedColor );
            }

            final ImageButton typeButton = (ImageButton) view.findViewById(R.id.type_menu);
            typeButton.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? iconAlarm : iconNotification)));
            typeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        showAlarmTypeMenu(item, typeButton, view);
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
            if (!isSelected && !item.enabled) {
                ImageViewCompat.setImageTintList(typeButton, SuntimesUtils.colorStateList(disabledTextColor, disabledTextColor, disabledTextColor));
            } else if (item.enabled) {
                ImageViewCompat.setImageTintList(typeButton, SuntimesUtils.colorStateList(pressedTextColor, disabledTextColor, disabledTextColor));
            }

            final TextView text = (TextView) view.findViewById(android.R.id.text1);
            if (text != null)
            {
                String emptyLabel = ((item.type == AlarmClockItem.AlarmType.ALARM) ? context.getString(R.string.alarmMode_alarm) : context.getString(R.string.alarmMode_notification));
                text.setText((item.label == null || item.label.isEmpty()) ? emptyLabel : item.label);
                text.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestLabel(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
                if (!isSelected && !item.enabled) {
                    text.setTextColor(disabledTextColor);
                } else if (item.enabled) {
                    text.setTextColor(pressedTextColor);
                }
            }

            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            if (text2 != null)
            {
                final String clockTime = context.getString(R.string.alarmOption_solarevent_none);
                text2.setText(item.event != null ? item.event.getLongDisplayString() : clockTime);
                text2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null)
                            {
                                adapterListener.onRequestSolarEvent(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
                if (!isSelected) {
                    text2.setTextColor(disabledTextColor);
                }
            }

            TextView text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            if (text_datetime != null)
            {
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTimeInMillis(item.timestamp);

                SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, alarmTime, false);
                if (SuntimesUtils.is24())
                {
                    text_datetime.setText(timeText.getValue());

                } else {
                    String timeString = timeText.getValue() + " " + timeText.getSuffix();
                    SpannableString timeDisplay = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
                    text_datetime.setText(timeDisplay);
                }

                if (!isSelected && !item.enabled) {
                    text_datetime.setTextColor(disabledTextColor);
                } else if (item.enabled) {
                    text_datetime.setTextColor(pressedTextColor);
                }

                text_datetime.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestTime(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
            }

            final TextView text_location = (TextView) view.findViewById(R.id.text_location_label);
            if (text_location != null)
            {
                text_location.setVisibility(item.event == null ? View.INVISIBLE : View.VISIBLE);
                AlarmDialog.updateLocationLabel(context, text_location, item.location);
                text_location.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestLocation(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
                if (!isSelected) {
                    Drawable[] d = SuntimesUtils.tintCompoundDrawables(text_location.getCompoundDrawables(), disabledTextColor);
                    text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
                    text_location.setTextColor(disabledTextColor);
                }
            }

            SwitchCompat switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);
            if (switch_enabled != null)
            {
                switch_enabled.setChecked(item.enabled);
                switch_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if (!isSelected) {
                            setSelectedItem(item.rowID);
                        }
                        enableAlarm(item, card, isChecked);
                    }
                });
            }

            final TextView text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            if (text_ringtone != null)
            {
                int iconID = item.ringtoneName != null ? iconSoundEnabled : iconSoundDisabled;
                ImageSpan icon = isSelected || item.enabled ? SuntimesUtils.createImageSpan(context, iconID, 28, 28, item.enabled ? pressedTextColor : 0)
                                                            : SuntimesUtils.createImageSpan(context, iconID, 28, 28, disabledTextColor, PorterDuff.Mode.MULTIPLY);

                final String none = context.getString(R.string.alarmOption_ringtone_none);
                String ringtoneName = isSelected ? (item.ringtoneName != null ? item.ringtoneName : none) : "";

                String ringtoneLabel = context.getString(R.string.alarmOption_ringtone_label, ringtoneName);
                SpannableStringBuilder ringtoneDisplay = SuntimesUtils.createSpan(context, ringtoneLabel, "[icon]", icon);

                text_ringtone.setText(ringtoneDisplay);
                text_ringtone.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestRingtone(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
            }

            CheckBox check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            if (check_vibrate != null)
            {
                check_vibrate.setChecked(item.vibrate);
                check_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        item.vibrate = isChecked;
                        item.modified = true;
                        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
                        task.execute(item);

                        if (isChecked) {
                            Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrate != null) {
                                vibrate.vibrate(500);
                            }
                        }
                    }
                });
                check_vibrate.setEnabled(isSelected);
                if (!isSelected) {
                    check_vibrate.setText("");
                }
            }

            TextView option_repeat = (TextView) view.findViewById(R.id.option_repeat);
            if (option_repeat != null)
            {
                boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();

                String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                        ? context.getString(R.string.alarmOption_repeat_all)
                        : noRepeat
                                ? context.getString(R.string.alarmOption_repeat_none)
                                : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
                option_repeat.setText( isSelected || !noRepeat ? repeatText : "" );
                if (!isSelected) {
                    option_repeat.setTextColor(disabledTextColor);
                }

                option_repeat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestRepetition(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
            }

            TextView option_offset = (TextView) view.findViewById(R.id.option_offset);
            if (option_offset != null)
            {
                if (item.offset == 0) {
                    option_offset.setText("at");  // TODO

                } else {
                    boolean isBefore = (item.offset <= 0);
                    String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
                    String offsetDisplay = offsetText + (isBefore ? " before" : " after");        // TODO
                    Spannable offsetSpan = SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
                    option_offset.setText(offsetSpan);
                }

                if (!isSelected && !item.enabled) {
                    option_offset.setTextColor(disabledTextColor);
                }

                option_offset.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isSelected) {
                            if (adapterListener != null) {
                                adapterListener.onRequestOffset(item);
                            }
                        } else {
                            setSelectedItem(item.rowID);
                        }
                    }
                });
            }

            ImageButton overflow = (ImageButton) view.findViewById(R.id.overflow_menu);
            if (overflow != null)
            {
                overflow.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showOverflowMenu(item, v, view);
                    }
                });
                overflow.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            }

            return view;
        }

        /**
         * @param item associated AlarmClockItem
         * @param buttonView button that triggered menu
         * @param itemView view associated with item
         */
        protected void showOverflowMenu(final AlarmClockItem item, final View buttonView, final View itemView)
        {
            PopupMenu menu = new PopupMenu(context, buttonView);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.alarmcontext, menu.getMenu());

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    switch (menuItem.getItemId())
                    {
                        case R.id.setAlarmType:
                            showAlarmTypeMenu(item, buttonView, itemView);
                            return true;

                        case R.id.setAlarmLabel:
                            if (adapterListener != null) {
                                adapterListener.onRequestLabel(item);
                            }
                            return true;

                        case R.id.setAlarmEvent:
                            if (adapterListener != null) {
                                adapterListener.onRequestSolarEvent(item);
                            }
                            return true;

                        case R.id.setAlarmOffset:
                            if (adapterListener != null) {
                                adapterListener.onRequestOffset(item);
                            }
                            return true;

                        case R.id.setAlarmLocation:
                            if (adapterListener != null) {
                                adapterListener.onRequestLocation(item);
                            }
                            return true;

                        case R.id.setAlarmRepeat:
                            if (adapterListener != null) {
                                adapterListener.onRequestRepetition(item);
                            }
                            return true;

                        case R.id.deleteAlarm:
                            deleteAlarm(item, itemView);
                            return true;

                        default:
                            return false;
                    }
                }
            });

            SuntimesUtils.forceActionBarIcons(menu.getMenu());
            menu.show();
        }

        /**
         * showAlarmTypeMenu
         * @param item AlarmClockItem
         * @param buttonView button that triggered menu
         * @param itemView view associated with item
         */
        protected void showAlarmTypeMenu(final AlarmClockItem item, final View buttonView, final View itemView)
        {
            PopupMenu menu = new PopupMenu(context, buttonView);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.alarmtype, menu.getMenu());

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    switch (menuItem.getItemId())
                    {
                        case R.id.alarmTypeNotification:
                            return changeAlarmType(item, AlarmClockItem.AlarmType.NOTIFICATION);

                        case R.id.alarmTypeAlarm:
                        default:
                            return changeAlarmType(item, AlarmClockItem.AlarmType.ALARM);
                    }
                }
            });

            SuntimesUtils.forceActionBarIcons(menu.getMenu());
            menu.show();
        }

        protected boolean changeAlarmType(AlarmClockItem item, AlarmClockItem.AlarmType type)
        {
            if (item.type != type)
            {
                Log.d(TAG, "alarmTypeMenu: alarm type is changed: " + type);
                if (item.enabled)
                {
                    Log.d(TAG, "alarmTypeMenu: alarm is enabled (reschedule required?)");
                    // item is enabled; disable it or reschedule/reenable
                    return false;

                } else {
                    Log.d(TAG, "alarmTypeMenu: alarm is disabled, changing its type..");
                    item.type = type;
                    item.setState(AlarmState.STATE_NONE);

                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                    task.execute(item);
                    notifyDataSetChanged();
                    return true;
                }
            }
            Log.w(TAG, "alarmTypeMenu: alarm type is unchanged");
            return false;
        }

        /**
         * enableAlarm
         * @param item AlarmClockItem
         * @param enabled enabled/disabled
         */
        protected void enableAlarm(final AlarmClockItem item, View itemView, final boolean enabled)
        {
            itemView.setBackground(enabled ? alarmEnabledBG : alarmDisabledBG);

            item.alarmtime = 0;
            item.enabled = enabled;
            item.modified = true;

            AlarmDatabaseAdapter.AlarmUpdateTask enableTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
            enableTask.setTaskListener(new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (result)
                    {
                        if (enabled)
                        {
                            Intent scheduleIntent = AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri());
                            context.sendBroadcast(scheduleIntent);

                        } else {
                            Intent disableIntent = AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri());
                            context.sendBroadcast(disableIntent);
                        }
                        notifyDataSetChanged();

                    } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
                }
            });
            enableTask.execute(item);   // TD
        }

        /**
         * deleteAlarm
         * @param item AlarmClockItem
         */
        protected void deleteAlarm(final AlarmClockItem item, final View itemView)
        {
            AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.deletealarm_dialog_title))
                    .setMessage(context.getString(R.string.deletealarm_dialog_message, item.rowID + ""))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.deletealarm_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            AlarmDatabaseAdapter.AlarmDeleteTask deleteTask = new AlarmDatabaseAdapter.AlarmDeleteTask(context);
                            deleteTask.setTaskListener(new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
                            {
                                @Override
                                public void onFinished(Boolean result)
                                {
                                    if (result)
                                    {
                                        final Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                                        animation.setAnimationListener(new Animation.AnimationListener()
                                        {
                                            @Override
                                            public void onAnimationStart(Animation animation) {}
                                            @Override
                                            public void onAnimationRepeat(Animation animation) {}
                                            @Override
                                            public void onAnimationEnd(Animation animation)
                                            {
                                                items.remove(item);
                                                notifyDataSetChanged();
                                                Toast.makeText(context, context.getString(R.string.deletealarm_toast_success, item.rowID + ""), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        itemView.startAnimation(animation);
                                    }
                                }
                            });
                            deleteTask.execute(item.rowID);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.deletealarm_dialog_cancel), null);
            confirm.show();
        }

        protected AlarmClockAdapterListener adapterListener;
        public void setAdapterListener(AlarmClockAdapterListener l)
        {
            adapterListener = l;
        }

        public static abstract class AlarmClockAdapterListener
        {
            public void onRequestLabel(AlarmClockItem forItem) {}
            public void onRequestRingtone(AlarmClockItem forItem) {}
            public void onRequestSolarEvent(AlarmClockItem forItem) {}
            public void onRequestLocation(AlarmClockItem forItem) {}
            public void onRequestTime(AlarmClockItem forItem) {}
            public void onRequestOffset(AlarmClockItem forItem) {}
            public void onRequestRepetition(AlarmClockItem forItem) {}
        }
    }

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
                clearAlarms();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
