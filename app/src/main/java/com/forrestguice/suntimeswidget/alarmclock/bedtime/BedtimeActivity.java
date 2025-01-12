/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.design.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import com.forrestguice.support.design.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.navigation.SuntimesNavigation;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SettingsActivityInterface;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;

import java.util.List;

/**
 * AlarmBedtimeActivity
 */
public class BedtimeActivity extends AppCompatActivity
{
    public static final String TAG = "BedtimeActivity";

    private static final String EXTRA_SHOWBACK = AlarmClockActivity.EXTRA_SHOWBACK;

    private static final int REQUEST_SETTINGS = 20;

    private static final String DIALOGTAG_HELP = "helpDialog";

    protected Toolbar menubar;
    private BedtimeDialog list;
    private AppSettings.LocaleInfo localeInfo;
    private SuntimesNavigation navigation;

    protected int actionBar_background0, actionBar_background1;

    public BedtimeActivity() {
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
        setContentView(R.layout.layout_activity_bedtime);
        initViews(this);
        initWarnings(this, savedState);
        handleIntent(getIntent());
    }

    @Override
    public void onStart()
    {
        super.onStart();
        registerReceiver(generalUpdateBroadcastReceiver, AlarmNotifications.getUpdateBroadcastIntentFilter(false));
        registerReceiver(itemUpdateBroadcastReceiver, AlarmNotifications.getUpdateBroadcastIntentFilter());
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
        unregisterReceiver(generalUpdateBroadcastReceiver);
        unregisterReceiver(itemUpdateBroadcastReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
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
        }
    }

    protected void initWarnings(Context context, Bundle savedState) {
        // TODO
    }
    protected void checkWarnings() {
        // TODO
    }
    protected void saveWarnings(Bundle outState) {
        // TODO
    }
    protected void restoreWarnings(@NonNull Bundle savedState) {
        // TODO
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

    private final BroadcastReceiver itemUpdateBroadcastReceiver = new BroadcastReceiver()
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
                    boolean alarmDeleted = intent.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                    onAlarmItemUpdated(ContentUris.parseId(data), alarmDeleted);

                } else Log.e(TAG, "updateReceiver.onReceive: unrecognized action: " + action);
            } else Log.e(TAG, "updateReceiver.onReceive: null action!");
        }
    };

    private final BroadcastReceiver generalUpdateBroadcastReceiver = new BroadcastReceiver()
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
                    boolean alarmsCleared = intent.getBooleanExtra(AlarmNotifications.ACTION_DELETE, false);
                    onAlarmItemUpdated(null, alarmsCleared);

                } else Log.e(TAG, "updateReceiver.onReceive: unrecognized action: " + action);
            } else Log.e(TAG, "updateReceiver.onReceive: null action!");
        }
    };

    protected void onAlarmItemUpdated(@Nullable Long alarmID, boolean deleted)
    {
        BedtimeItemAdapter adapter = list.getAdapter();
        if (adapter != null)
        {
            if (alarmID != null)
            {
                Integer[] positions = adapter.findItemPositions(BedtimeActivity.this, alarmID);
                for (final int position : positions)
                {
                    BedtimeItem item = (position >= 0 ? adapter.getItem(position) : null);
                    if (item != null)
                    {
                        //Log.d("DEBUG", "onAlarmItemUpdated: " + alarmID + ", deleted? " + deleted + ", position " + position);
                        if (deleted) {
                            item.setAlarmItem(null);
                            list.notifyItemChanged(position);
                            continue;
                        }
                        item.loadAlarmItem(this, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
                        {
                            @Override
                            public void onLoadFinished(List<AlarmClockItem> result) {
                                super.onLoadFinished(result);
                                list.notifyItemChanged(position);
                            }
                        });
                    }
                }

            } else {
                list.notifyItemChanged(0);
            }
        } else {
            list.reloadAdapter();
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

        /*if (param_action != null)
        {
            if (param_action.equals(AlarmNotifications.ACTION_BEDTIME)) {
                handleIntent_bedtime(this, intent);

            } else if (param_action.equalsIgnoreCase(AlarmNotifications.ACTION_BEDTIME_DISMISS)) {
                handleIntent_bedtimeDismiss(this, intent);
            }

        } else {
            if (param_data != null) {
                // TODO
                //list.notifyAlarmUpdated(ContentUris.parseId(param_data));
            }
        }*/
    }

    /*protected void handleIntent_bedtime(Context context, Intent intent)
    {
        Toast.makeText(context, "bedtime trigger received", Toast.LENGTH_SHORT).show();
        AlarmNotifications.NotificationService.triggerBedtimeMode(context, true);
    }
    protected void handleIntent_bedtimeDismiss(Context context, Intent intent)
    {
        Toast.makeText(context, "bedtime dismiss received", Toast.LENGTH_SHORT).show();
        AlarmNotifications.NotificationService.triggerBedtimeMode(context, false);
    }*/

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
        AlarmClockItem.AlarmType.initDisplayStrings(context);
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);

        // TODO
        /*int[] attrs = { R.attr.alarmColorEnabled, android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor, android.R.attr.textColor, R.attr.icActionNew, R.attr.icActionClose };
        TypedArray a = context.obtainStyledAttributes(attrs);
        colorAlarmEnabled = colorOn = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled_dark));
        a.recycle();*/

        if (appThemeOverride != null) {
            // TODO
            /*colorAlarmEnabled = colorOn = appThemeOverride.getAccentColor();
            colorPressed = appThemeOverride.getActionColor();*/
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

        menubar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menubar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            boolean showBack = getIntent().getBooleanExtra(EXTRA_SHOWBACK, false);
            if (!showBack) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_suntimes);   // TODO: "suntimes alarms" icon
            }
        }

        navigation = new SuntimesNavigation(this, menubar, R.id.action_bedtime);

        TypedArray a = obtainStyledAttributes(new int[] { R.attr.dialogBackground, R.attr.dialogBackgroundAlt });
        actionBar_background0 = ContextCompat.getColor(this, a.getResourceId(0, R.color.dialog_bg));
        actionBar_background1 = ContextCompat.getColor(this, a.getResourceId(1, R.color.dialog_bg_alt));
        a.recycle();

        int menubarColor = BedtimeSettings.isBedtimeModeActive(getApplicationContext()) ? actionBar_background0 : actionBar_background1;
        menubar.setBackgroundColor(menubarColor);

        list = (BedtimeDialog) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        list.setDialogListener(dialogListener(menubarColor));
    }

    private BedtimeDialog.DialogListener dialogListener(final int initialColor)
    {
        return new BedtimeDialog.DialogListener()
        {
            private int backgroundColor = initialColor;

            @Override
            public void onScrolled(RecyclerView recyclerView, int lastCompletelyVisibleItemPosition)
            {
                if (menubar != null)
                {
                    int from = backgroundColor;
                    backgroundColor = ((lastCompletelyVisibleItemPosition == 0) ? actionBar_background0 : actionBar_background1);
                    if (from != backgroundColor)
                    {
                        ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), from, backgroundColor);
                        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                menubar.setBackgroundColor((Integer) animator.getAnimatedValue());
                            }
                        });

                        animation.setDuration(getResources().getInteger(R.integer.anim_fadein_duration));
                        animation.setStartDelay(0);
                        animation.start();
                    }
                }
            }

            @Override
            public void onItemClick(BedtimeViewHolder holder, BedtimeItem item) {
            }

            @Override
            public void onItemAction(BedtimeViewHolder holder, BedtimeItem item) {
            }

            @Override
            public void onItemConfigure(BedtimeViewHolder holder, BedtimeItem item) {
            }
        };
    }

    protected void updateViews(Context context) {
    }

    protected void restoreDialogs()
    {
        /*FragmentManager fragments = getSupportFragmentManager();
        AlarmCreateDialog alarmCreateDialog = (AlarmCreateDialog) fragments.findFragmentById(R.id.createAlarmFragment);
        if (alarmCreateDialog != null) {
            alarmCreateDialog.setOnAcceptedListener(onAddAlarmAccepted);
            alarmCreateDialog.setOnCanceledListener(onAddAlarmCanceled);
            alarmCreateDialog.setOnNeutralListener(onAddAlarmNeutral);
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bedtime, menu);
        SuntimesNavigation.updateMenuNavigationItems(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_permission:
                BedtimeSettings.startDoNotDisturbAccessActivity(BedtimeActivity.this);
                //AlarmNotifications.NotificationService.triggerBedtimeMode(this, true);
                return true;

            case R.id.action_settings:
                showSettings();
                return true;

            case R.id.action_help:
                showHelp(this);
                return true;

            case R.id.action_about:
                navigation.showAbout(this);
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
        if (navigation != null && navigation.isNavigationDrawerOpen()) {
            navigation.closeNavigationDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected void onHomePressed()
    {
        Intent intent = new Intent(this, AlarmClockActivity.class);
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

    protected void showSettings()
    {
        Intent settingsIntent = new Intent(this, SuntimesSettingsActivity.class);
        startActivityForResult(settingsIntent, REQUEST_SETTINGS);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    @SuppressLint("ResourceType")
    protected void showHelp(Context context)
    {
        int iconSize = (int) context.getResources().getDimension(R.dimen.helpIcon_size);
        int[] iconAttrs = { R.attr.icActionBedtime, R.attr.icActionNotification1, R.attr.icActionAlarm };
        TypedArray typedArray = context.obtainStyledAttributes(iconAttrs);
        ImageSpan bedtimeIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(0, R.drawable.ic_action_bedtime), iconSize, iconSize, 0);
        ImageSpan reminderIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(1, R.drawable.ic_action_notification1), iconSize, iconSize, 0);
        ImageSpan alarmIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(2, R.drawable.ic_action_alarms), iconSize, iconSize, 0);
        typedArray.recycle();

        SuntimesUtils.ImageSpanTag[] helpTags = {
                new SuntimesUtils.ImageSpanTag("[Icon Bedtime]", bedtimeIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Reminder]", reminderIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Alarm]", alarmIcon),
        };

        CharSequence helpString = SuntimesUtils.fromHtml(context.getString(R.string.help_alarms_bedtime));
        SpannableStringBuilder helpSpan = SuntimesUtils.createSpan(context, helpString, helpTags);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpSpan);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    protected void onSettingsResult(int resultCode, Intent data)
    {
        boolean recreateActivity = (data != null && data.getBooleanExtra(SettingsActivityInterface.RECREATE_ACTIVITY, false));
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

}
