/**
    Copyright (C) 2020 Forrest Guice
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class AlarmEditActivity extends AppCompatActivity implements AlarmItemAdapterListener
{
    public static final String TAG = "AlarmReceiverList";

    public static final String EXTRA_ITEM = "item";
    public static final String EXTRA_ISNEW = "isnew";

    public static final int REQUEST_RINGTONE = 10;
    public static final int REQUEST_RINGTONE1 = 12;
    public static final int REQUEST_SETTINGS = 20;
    public static final int REQUEST_ACTION0 = 40;
    public static final int REQUEST_ACTION1 = 50;
    public static final int REQUEST_ACTION2 = 60;
    public static final int REQUEST_ACTION3 = 70;

    private static final String DIALOGTAG_EVENT = "alarmevent";
    private static final String DIALOGTAG_REPEAT = "alarmrepetition";
    private static final String DIALOGTAG_LABEL = "alarmlabel";
    private static final String DIALOGTAG_NOTE = "alarmnote";
    private static final String DIALOGTAG_TIME = "alarmtime";
    private static final String DIALOGTAG_OFFSET = "alarmoffset";
    private static final String DIALOGTAG_LOCATION = "alarmlocation";
    private static final String DIALOGTAG_HELP = "alarmhelp";

    private AlarmEditDialog editor;
    private AppSettings.LocaleInfo localeInfo;

    private int colorAlarmEnabled, colorOn, colorOff, colorEnabled, colorDisabled, colorPressed;
    private int resAddIcon, resCloseIcon;

    public AlarmEditActivity()
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
        setContentView(R.layout.layout_activity_alarmedit);
        initViews(this, savedState);
        setResult(Activity.RESULT_CANCELED);
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
            case REQUEST_RINGTONE:
                onRingtoneResult(resultCode, data);
                break;

            case REQUEST_RINGTONE1:
                onRingtoneResult1(resultCode, data);
                break;

            case REQUEST_ACTION0:
                onActionResult(resultCode, data, 0);
                break;

            case REQUEST_ACTION1:
                onActionResult(resultCode, data, 1);
                break;

            case REQUEST_ACTION2:
                onActionResult(resultCode, data, 2);
                break;

            case REQUEST_ACTION3:
                onActionResult(resultCode, data, 3);
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

    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
        Log.d("DEBUG", "new intent: " + intent);
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
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState) {
        super.onRestoreInstanceState(savedState);
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context, Bundle savedState)
    {
        SuntimesUtils.initDisplayStrings(context);

        editor = (AlarmEditDialog) getSupportFragmentManager().findFragmentById(R.id.editFragment);
        editor.setOnAcceptedListener(onEditorAccepted);
        editor.setAlarmClockAdapterListener(this);
        editor.setShowDialogFrame(false);
        editor.setShowOverflow(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null && savedState == null)
        {
            AlarmClockItem item = extras.getParcelable(EXTRA_ITEM);
            boolean isNew = extras.getBoolean(EXTRA_ISNEW, false);
            editor.initFromItem(item, isNew);
        }

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            AlarmClockItem item = editor.getItem();
            actionBar.setTitle(item != null ? item.type.getDisplayString() : "");
        }
    }

    protected void updateViews(Context context) {
    }

    protected void returnItem(boolean enabled)
    {
        AlarmClockItem item = editor.getItem();
        item.enabled = enabled;
        returnItem(item);
    }

    protected void returnItem(AlarmClockItem item)
    {
        editor.saveSettings(AlarmEditActivity.this);
        Intent intent = getIntent();
        intent.putExtra(AlarmEditActivity.EXTRA_ITEM, item);
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    private final DialogInterface.OnClickListener onEditorAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            returnItem(editor.getItem());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void restoreDialogs()
    {
        FragmentManager fragments = getSupportFragmentManager();

        AlarmCreateDialog eventDialog1 = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
        if (eventDialog1 != null) {
            eventDialog1.setOnAcceptedListener(onPickEventAccepted);
            eventDialog1.setOnNeutralListener(onPickEventCanceled);
            eventDialog1.setOnCanceledListener(onPickEventCanceled);
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

        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null) {
            locationDialog.setDialogListener(onLocationChanged);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarmedit, menu);

        MenuItem optionsItem = menu.findItem(R.id.action_options);
        if (optionsItem != null) {
            SubMenu optionsMenu = optionsItem.getSubMenu();
            if (optionsMenu != null) {
                inflater.inflate(R.menu.alarmcontext2, optionsMenu);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_enable:
                returnItem(true);
                return true;

            case R.id.action_disable:
                disableAlarm();
                return true;

            case R.id.action_save:
                returnItem(editor.getItem());
                return true;

            case R.id.action_delete:
                AlarmEditDialog.confirmDeleteAlarm(AlarmEditActivity.this, editor.getItem(), onDeleteConfirmed(editor.getItem()));
                return true;

            case R.id.setAlarmType:
                editor.itemView.menu_type.performClick();
                return true;

            case R.id.setAlarmLabel:
                editor.itemView.edit_label.performClick();
                return true;

            case R.id.setAlarmNote:
                editor.itemView.edit_note.performClick();
                return true;

            case R.id.setAlarmOffset:
                editor.itemView.chip_offset.performClick();
                return true;

            case R.id.setAlarmEvent:
                editor.itemView.chip_event.performClick();
                return true;

            case R.id.setAlarmLocation:
                editor.itemView.chip_location.performClick();
                return true;

            case R.id.setAlarmRepeat:
                editor.itemView.chip_repeat.performClick();
                return true;

            case R.id.setAlarmSound:
                editor.itemView.chip_ringtone.performClick();
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);

        AlarmClockItem item = editor.getItem();
        boolean alarmEnabled = (item != null && item.enabled);

        MenuItem saveItem = menu.findItem(R.id.action_save);
        MenuItem enableItem = menu.findItem(R.id.action_enable);
        MenuItem disableItem = menu.findItem(R.id.action_disable);

        if (enableItem != null && item != null)
        {
            enableItem.setVisible(!alarmEnabled && AlarmNotifications.updateAlarmTime(this, item, Calendar.getInstance(), false));
            if (Build.VERSION.SDK_INT >= 21) {
                DrawableCompat.setTint(enableItem.getIcon().mutate(), colorAlarmEnabled);
            } else {
                enableItem.getIcon().mutate().setColorFilter(colorAlarmEnabled, PorterDuff.Mode.SRC_IN);
            }
        }
        if (disableItem != null && item != null) {
            disableItem.setVisible(alarmEnabled);
        }
        if (saveItem != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                DrawableCompat.setTint(saveItem.getIcon().mutate(), (alarmEnabled) ? colorAlarmEnabled : colorEnabled);
            } else {
                saveItem.getIcon().mutate().setColorFilter((alarmEnabled) ? colorAlarmEnabled : colorEnabled, PorterDuff.Mode.SRC_IN);
            }
        }

        return super.onPrepareOptionsPanel(view, menu);
    }

    @SuppressLint("ResourceType")
    protected void showHelp()
    {
        int iconSize = (int) getResources().getDimension(R.dimen.helpIcon_size);
        int[] iconAttrs = { R.attr.text_accentColor, R.attr.icActionBack, R.attr.icActionEnableAlarm, R.attr.icActionSave, R.attr.icActionCancel, R.attr.icActionDelete, R.attr.icActionTimeReset };
        TypedArray typedArray = obtainStyledAttributes(iconAttrs);
        int accentColor = ContextCompat.getColor(this, typedArray.getResourceId(0, R.color.text_accent_dark));
        ImageSpan iconBack = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(1, R.drawable.ic_action_discard), iconSize, iconSize, 0);
        ImageSpan iconDone = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(2, R.drawable.ic_action_doneall), iconSize, iconSize, accentColor);
        ImageSpan iconSave = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(3, R.drawable.ic_action_save), iconSize, iconSize, 0);
        ImageSpan iconCancel = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(4, R.drawable.ic_action_cancel), iconSize, iconSize, 0);
        ImageSpan iconDelete = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(5, R.drawable.ic_action_discard), iconSize, iconSize, 0);
        ImageSpan iconOffset = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(6, R.drawable.ic_action_timereset), iconSize, iconSize, 0);
        typedArray.recycle();

        SuntimesUtils.ImageSpanTag[] helpTags = {
                new SuntimesUtils.ImageSpanTag("[Icon Back]", iconBack),
                new SuntimesUtils.ImageSpanTag("[Icon Done]", iconDone),
                new SuntimesUtils.ImageSpanTag("[Icon Save]", iconSave),
                new SuntimesUtils.ImageSpanTag("[Icon Cancel]", iconCancel),
                new SuntimesUtils.ImageSpanTag("[Icon Delete]", iconDelete),
                new SuntimesUtils.ImageSpanTag("[Icon Offset]", iconOffset),
        };

        CharSequence helpText = SuntimesUtils.fromHtml(getString(R.string.help_alarms_edit));
        CharSequence helpSpan = SuntimesUtils.createSpan(this, helpText, helpTags);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpSpan);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        about.putExtra(AboutActivity.EXTRA_ICONID, R.mipmap.ic_launcher_alarms_round);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    protected DialogInterface.OnClickListener onDeleteConfirmed( final AlarmClockItem item )
    {
       return new DialogInterface.OnClickListener()
       {
           public void onClick(DialogInterface dialog, int whichButton) {
               setResult(AlarmEditActivity.RESULT_CANCELED);
               supportFinishAfterTransition();
               sendBroadcast(AlarmNotifications.getAlarmIntent(AlarmEditActivity.this, AlarmNotifications.ACTION_DELETE, item.getUri()));
           }
       };
    }

    @Override
    public void onBackPressed() {
        confirmDiscardChanges(AlarmEditActivity.this);
    }

    protected void confirmDiscardChanges(final Context context)
    {
        if (editor.isModified())
        {
            String message = context.getString(R.string.discardchanges_dialog_message);
            AlertDialog.Builder confirm = new AlertDialog.Builder(context).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.discardchanges_dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            AlarmEditActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(context.getString(R.string.discardchanges_dialog_cancel), null)
                    .setNeutralButton(context.getString(R.string.discardchanges_dialog_neutral), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onEditorAccepted.onClick(dialog, which);
                        }
                    });
            confirm.show();

        } else {
            super.onBackPressed();
        }
    }

    protected void disableAlarm()
    {
        AlarmClockItem item = editor.getItem();
        item.alarmtime = 0;
        item.enabled = false;
        item.modified = true;

        AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(this, false, false);
        task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                sendBroadcast(AlarmNotifications.getAlarmIntent(AlarmEditActivity.this, AlarmNotifications.ACTION_DISABLE, item.getUri()));
                invalidateOptionsMenu();
            }
        });
        task.execute(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void showAlarmSoundPopup(View v, @NonNull final AlarmClockItem item)
    {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.alarmsound, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_alarmsound_ringtone:
                        ringtonePicker(item);
                        return true;

                    case R.id.action_alarmsound_file:
                        audioFilePicker(item);
                        return true;

                    case R.id.action_alarmsound_none:
                        onRingtoneResult(null, false);
                        return true;
                }
                return false;
            }
        });
        SuntimesUtils.forceActionBarIcons(popup.getMenu());
        popup.show();
    }

    /**
     * pickRingtone
     * @param item apply ringtone to AlarmClockItem
     */
    protected void pickRingtone(@NonNull final AlarmClockItem item) {
        showAlarmSoundPopup(editor.itemView.chip_ringtone, item);
    }

    protected void ringtonePicker(@NonNull AlarmClockItem item)
    {
        int ringtoneType = RingtoneManager.TYPE_RINGTONE;
        if (!AlarmSettings.loadPrefAllRingtones(this)) {
            ringtoneType = (item.type == AlarmClockItem.AlarmType.ALARM ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION);
        }

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, item.type.getDisplayString());
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, AlarmSettings.getDefaultRingtoneUri(this, item.type, true));    // TODO: setDefaultRingtoneUri may block (potential ANR)...
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (item.ringtoneURI != null ? Uri.parse(item.ringtoneURI) : null));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.configAction_setAlarmSound)), REQUEST_RINGTONE);
    }

    protected void onRingtonePermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (editor != null) {
            ringtonePicker(editor.getItem());
        }
    }

    protected void onRingtoneResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && editor != null && data != null) {
            onRingtoneResult((Uri)(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)), false);
        } else {
            Log.d(TAG, "onActivityResult: bad result: " + resultCode + ", " + data);
        }
    }
    protected void onRingtoneResult(final Uri uri, boolean isAudioFile)
    {
        OnRingtoneResultTask task = new OnRingtoneResultTask(this, uri, isAudioFile);
        task.setTaskListener(new OnRingtoneResultTask.TaskListener() {
            @Override
            public void onFinished(Boolean result) {
                editor.notifyItemChanged();
            }
        });
        task.execute(editor.getItem());
    }

    /**
     * OnRingtoneResultTask
     */
    public static class OnRingtoneResultTask extends AsyncTask<AlarmClockItem, Void, Boolean>
    {
        private WeakReference<Context> contextRef;
        private boolean isAudioFile;
        private Uri uri;

        public OnRingtoneResultTask(Context context, Uri uri, boolean isAudioFile)
        {
            contextRef = new WeakReference<>(context);
            this.uri = uri;
            this.isAudioFile = isAudioFile;
        }

        @Override
        protected Boolean doInBackground(AlarmClockItem... items)
        {
            AlarmClockItem item = items[0];
            Context context = contextRef.get();
            if (context != null && uri != null)
            {
                //Log.d(TAG, "OnRingtoneResult: uri: " + uri);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                if (ringtone != null)
                {
                    ringtone.stop();
                    item.ringtoneName = AlarmSettings.getRingtoneTitle(context, uri, ringtone, isAudioFile);
                    item.ringtoneURI = uri.toString();
                    //Log.d(TAG, "OnRingtoneResult: uri: " + item.ringtoneURI + ", title: " + item.ringtoneName);

                } else {
                    item.ringtoneName = null;
                    item.ringtoneURI = null;
                    //Log.d(TAG, "OnRingtoneResult: uri: " + uri + " <null ringtone>");
                }

            } else {
                item.ringtoneName = null;
                item.ringtoneURI = null;
                //Log.d(TAG, "OnRingtoneResult: null uri");
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (listener != null) {
                listener.onFinished(result);
            }
        }

        protected TaskListener listener = null;
        public void setTaskListener( TaskListener l )
        {
            listener = l;
        }
        public static abstract class TaskListener {
            public void onFinished(Boolean result) {}
        }
    }

    protected void audioFilePicker(@NonNull AlarmClockItem item)
    {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 19)
        {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }

        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.configAction_setAlarmSound)), REQUEST_RINGTONE1);
    }

    protected void onRingtoneResult1(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && editor != null && data != null && data.getData() != null)
        {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT >= 19) {
                final int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(uri, flags);
            }
            onRingtoneResult(uri, true);
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
            if (editor != null && dialog != null)
            {
                AlarmClockItem item = editor.getItem();
                item.label = dialog.getLabel();
                editor.notifyItemChanged();
            }
        }
    };

    /**
     * pickNote
     */
    protected void pickNote(@NonNull AlarmClockItem item)
    {
        AlarmLabelDialog dialog = new AlarmLabelDialog();    // TODO: multi-line
        dialog.setDialogTitle(getString(R.string.alarmnote_dialog_title));
        dialog.setMultiLine(true);
        dialog.setAccentColor(colorAlarmEnabled);
        dialog.setLabel(item.note);
        dialog.setOnAcceptedListener(onNoteChanged);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_NOTE);
    }
    private DialogInterface.OnClickListener onNoteChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmLabelDialog dialog = (AlarmLabelDialog) fragments.findFragmentByTag(DIALOGTAG_NOTE);
            if (editor != null && dialog != null)
            {
                AlarmClockItem item = editor.getItem();
                item.note = dialog.getLabel();
                editor.notifyItemChanged();
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
            AlarmOffsetDialog offsetDialog = new AlarmOffsetDialog();
            offsetDialog.setShowDays(item.getEventItem(AlarmEditActivity.this).supportsOffsetDays());
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
            if (editor != null && offsetDialog != null)
            {
                AlarmClockItem item = editor.getItem();
                item.offset = offsetDialog.getOffset();
                AlarmNotifications.updateAlarmTime(AlarmEditActivity.this, item);
                editor.notifyItemChanged();
                editor.triggerPreviewOffset();
            }
        }
    };

    /**
     * pickSolarEvent
     */
    protected void pickSolarEvent(@NonNull AlarmClockItem item)
    {
        final AlarmCreateDialog dialog = new AlarmCreateDialog();
        dialog.loadSettings(AlarmEditActivity.this);
        dialog.setAlarmType(item.type);
        dialog.setDialogMode(item.getEvent() != null ? 0 : 1);

        dialog.setEvent(item.getEvent(), item.location);
        dialog.setAlarmTime(item.hour, item.minute, item.timezone);
        dialog.setOffset(item.offset);
        dialog.setOnAcceptedListener(onPickEventAccepted);
        dialog.setOnNeutralListener(onPickEventCanceled);
        dialog.setOnCanceledListener(onPickEventCanceled);
        dialog.show(getSupportFragmentManager(), DIALOGTAG_EVENT);
    }
    private DialogInterface.OnClickListener onPickEventAccepted = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
            if (editor != null && dialog != null)
            {
                AlarmClockItem item = editor.getItem();
                AlarmCreateDialog.updateAlarmItem(dialog, item);
                AlarmNotifications.updateAlarmTime(AlarmEditActivity.this, item);
                editor.notifyItemChanged();
                editor.triggerPreviewOffset();
                invalidateOptionsMenu();
            }
        }
    };
    private DialogInterface.OnClickListener onPickEventCanceled = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_EVENT);
            if (editor != null && dialog != null) {
                dialog.dismiss();
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
            if (editor != null)
            {
                AlarmClockItem item = editor.getItem();
                item.location = location;
                AlarmNotifications.updateAlarmTime(AlarmEditActivity.this, item);
                editor.notifyItemChanged();
                editor.triggerPreviewOffset();
                invalidateOptionsMenu();
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

            if (editor != null && repeatDialog != null)
            {
                AlarmClockItem item = editor.getItem();
                item.repeating = repeatDialog.getRepetition();
                item.repeatingDays = repeatDialog.getRepetitionDays();
                AlarmNotifications.updateAlarmTime(AlarmEditActivity.this, item);
                editor.notifyItemChanged();
            }
        }
    };

    /**
     * pickAction
     */
    protected void pickAction(@NonNull final AlarmClockItem item, final int actionNum)
    {
        Intent intent = new Intent(AlarmEditActivity.this, ActionListActivity.class);
        intent.putExtra(ActionListActivity.PARAM_NOSELECT, false);
        intent.putExtra(ActionListActivity.PARAM_SELECTED, item.getActionID(actionNum));
        startActivityForResult(intent, getActionRequestCode(actionNum));
    }
    protected void onActionResult(int resultCode, Intent data, int actionNum)
    {
        if (resultCode == RESULT_OK && editor != null && data != null)
        {
            AlarmClockItem item = editor.getItem();
            String actionID = data.getStringExtra(ActionListActivity.SELECTED_ACTIONID);
            item.setActionID(actionNum, actionID);
            editor.notifyItemChanged();

        } else {
            Log.d(TAG, "onActivityResult: bad result: " + resultCode + ", " + data);
        }
    }
    protected int getActionRequestCode(int actionNum) {
        switch (actionNum) {
            case 3: return REQUEST_ACTION3;
            case 2: return REQUEST_ACTION2;
            case 1: return REQUEST_ACTION1;
            case 0: default: return REQUEST_ACTION0;
        }
    }

    @Override
    public void onTypeChanged(AlarmClockItem forItem)
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(forItem != null ? forItem.type.getDisplayString() : "");
        }

    }

    @Override
    public void onRequestLabel(AlarmClockItem forItem) {
        pickLabel(forItem);
    }

    @Override
    public void onRequestNote(AlarmClockItem forItem) {
        pickNote(forItem);
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

}
