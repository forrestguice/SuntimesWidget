/**
    Copyright (C) 2014-2021 Forrest Guice
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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialog;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.cards.CardAdapter;
import com.forrestguice.suntimeswidget.cards.CardLayoutManager;
import com.forrestguice.suntimeswidget.getfix.GetFixHelper;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.map.WorldMapDialog;
import com.forrestguice.suntimeswidget.notes.NoteChangedListener;
import com.forrestguice.suntimeswidget.notes.NoteData;
import com.forrestguice.suntimeswidget.notes.SuntimesNotes;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import android.os.Handler;

@SuppressWarnings("Convert2Diamond")
public class SuntimesActivity extends AppCompatActivity
{
    public static final String ACTION_ADD_ALARM = "suntimes.action.ALARM";

    public static final String ACTION_VIEW_SUN = "suntimes.action.VIEW_SUN";
    public static final String ACTION_VIEW_MOON = "suntimes.action.VIEW_MOON";
    public static final String ACTION_VIEW_SOLSTICE = "suntimes.action.VIEW_SOLSTICE";
    public static final String ACTION_VIEW_WORLDMAP = "suntimes.action.VIEW_WORLDMAP";

    public static final String ACTION_CARD_NEXT = "suntimes.action.CARD_NEXT";
    public static final String ACTION_CARD_PREV = "suntimes.action.CARD_PREV";
    public static final String ACTION_CARD_RESET = "suntimes.action.SWAP_CARD";
    public static final String ACTION_CARD_SHOW = "suntimes.action.SHOW_CARD";
    public static final String EXTRA_SHOW_DATE = MenuAddon.EXTRA_SHOW_DATE;

    public static final String ACTION_NOTE_NEXT = "suntimes.action.NEXT_NOTE";
    public static final String ACTION_NOTE_PREV = "suntimes.action.PREV_NOTE";
    public static final String ACTION_NOTE_RESET = "suntimes.action.RESET_NOTE";
    public static final String ACTION_NOTE_SEEK = "suntimes.action.SEEK_NOTE";
    public static final String EXTRA_SOLAREVENT = "solarevent";

    public static final String ACTION_CONFIG_LOCATION = "suntimes.action.CONFIG_LOCATION";
    public static final String ACTION_CONFIG_TIMEZONE = "suntimes.action.TIMEZONE";
    public static final String ACTION_CONFIG_DATE = "suntimes.action.CONFIG_DATE";

    public static final String SUNTIMES_ACTION_PREFIX = "suntimes.action";
    private static final String[] SUNTIMES_ACTIONS = new String[] {
            ACTION_ADD_ALARM, ACTION_VIEW_SUN, ACTION_VIEW_MOON, ACTION_VIEW_SOLSTICE, ACTION_VIEW_WORLDMAP,
            ACTION_CARD_NEXT, ACTION_CARD_PREV, ACTION_CARD_RESET, ACTION_CARD_SHOW,
            ACTION_NOTE_NEXT, ACTION_NOTE_PREV, ACTION_NOTE_RESET, ACTION_NOTE_SEEK,
            ACTION_CONFIG_LOCATION, ACTION_CONFIG_TIMEZONE, ACTION_CONFIG_DATE
    };
    private static final HashMap<String, String> SUNTIMES_ACTION_MAP = createLegacyActionMap();

    public static final String SUNTIMES_APP_UPDATE_FULL = "suntimes.SUNTIMES_APP_UPDATE_FULL";
    public static final String SUNTIMES_APP_UPDATE_PARTIAL = "suntimes.SUNTIMES_APP_UPDATE_PARTIAL";
    public static final int SUNTIMES_SETTINGS_REQUEST = 10;

    public static final String KEY_UI_NOTEINDEX = "noteIndex";
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedCard";
    public static final String KEY_UI_CARDPOSITION = "cardPosition";

    public static final String WARNINGID_DATE = "Date";
    public static final String WARNINGID_TIMEZONE = "Timezone";

    private static final String DIALOGTAG_TIMEZONE = "timezone";
    private static final String DIALOGTAG_ALARM = "alarm";
    private static final String DIALOGTAG_HELP = "help";
    private static final String DIALOGTAG_LOCATION = "location";
    private static final String DIALOGTAG_DATE = "dateselect";
    private static final String DIALOGTAG_LIGHTMAP = "lightmap";
    private static final String DIALOGTAG_WORLDMAP = "worldmap";
    private static final String DIALOGTAG_EQUINOX = "equinox";
    private static final String DIALOGTAG_MOON = "moon";

    protected static final SuntimesUtils utils = new SuntimesUtils();

    private ActionBar actionBar;
    private Menu actionBarMenu;
    private String appTheme;
    private int appThemeResID;
    private SuntimesTheme appThemeOverride = null;
    private AppSettings.LocaleInfo localeInfo;

    private GetFixHelper getFixHelper;

    private com.forrestguice.suntimeswidget.calculator.core.Location location;
    protected SuntimesNotes notes;
    protected SuntimesRiseSetDataset dataset;
    protected SuntimesEquinoxSolsticeDataset dataset_equinox;
    protected SuntimesMoonData dataset_moon;

    private int color_textTimeDelta;
    private int resID_noonIcon;
    private int resID_buttonDisabledColor;

    // clock views
    private TextView txt_time;
    private TextView txt_time_suffix;
    private TextView txt_timezone;

    // note views
    private ProgressBar note_progress;
    private ViewFlipper note_flipper;
    private Animation anim_note_inPrev, anim_note_inNext;
    private Animation anim_note_outPrev, anim_note_outNext;

    private ImageView ic_time1_note,    ic_time2_note;
    private TextView txt_time1_note1,   txt_time2_note1;
    private TextView txt_time1_note2,   txt_time2_note2;
    private TextView txt_time1_note3,   txt_time2_note3;

    // time card views
    private RecyclerView card_view;
    private CardLayoutManager card_layout;
    private LinearSmoothScroller card_scroller;
    private CardAdapter card_adapter;

    private EquinoxView card_equinoxSolstice;
    private View equinoxLayout;

    private TextView txt_datasource;
    private View layout_datasource;
    private AppCompatCheckBox check_altitude;
    private TextView txt_altitude;
    private View layout_altitude;

    private boolean isRtl = false;
    private long userSwappedCard = -1L;
    private boolean onResume_resetNoteIndex = true;

    private boolean showWarnings = false;
    private SuntimesWarning timezoneWarning;
    private SuntimesWarning dateWarning;
    private List<SuntimesWarning> warnings;

    private boolean verboseAccessibility = AppSettings.PREF_DEF_ACCESSIBILITY_VERBOSE;

    public SuntimesActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        //Log.d("DEBUG", "attachBaseContext");
        Context context = AppSettings.initLocale(newBase, localeInfo = new AppSettings.LocaleInfo());
        isRtl = AppSettings.isLocaleRtl(context);
        super.attachBaseContext(context);
    }

    /**
     * OnCreate: the Activity initially created
     * @param savedState a Bundle containing previously saved application state
     */
    @Override
    public void onCreate(Bundle savedState)
    {
        //Log.d("DEBUG", "onCreate");
        Context context = SuntimesActivity.this;
        initTheme();
        super.onCreate(savedState);
        setResult(RESULT_CANCELED);

        initLocale(this);  // must follow super.onCreate or locale is reverted
        setContentView(R.layout.layout_main);
        initViews(context);
        themeViews(context);

        initWarnings(context, savedState);

        initGetFix();
        getFixHelper.loadSettings(savedState);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent( Intent intent ) {
        Log.d("onNewIntent", intent.toString());
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        String action = intent.getAction();
        intent.setAction(null);

        if (SUNTIMES_ACTION_MAP.containsKey(action)) {
            Log.d("handleIntent", "legacy action: " + action);
            action = SUNTIMES_ACTION_MAP.get(action);
        }

        Uri data = intent.getData();
        intent.setData(null);

        Log.d("handleIntent", "action: " + action + ", data: " + data);
        if (action != null)
        {
            if (action.equals(ACTION_VIEW_SUN)) {
                showSunPositionAt(intent.getLongExtra(EXTRA_SHOW_DATE, -1));

            } else if (action.equals(ACTION_VIEW_MOON)) {
                showMoonDialog();

            } else if (action.equals(ACTION_VIEW_SOLSTICE)) {
                showEquinoxDialog();

            } else if (action.equals(ACTION_VIEW_WORLDMAP)) {
                showMapPositionAt(intent.getLongExtra(EXTRA_SHOW_DATE, -1));

            } else if (action.equals(ACTION_ADD_ALARM)) {
                scheduleAlarm(SolarEvents.valueOf(intent.getStringExtra(EXTRA_SOLAREVENT), null));

            } else if (action.equals(ACTION_CONFIG_TIMEZONE)) {
                configTimeZone();

            } else if (action.equals(ACTION_CONFIG_DATE)) {
                configDate();

            } else if (action.equals(ACTION_NOTE_SEEK)) {
                seekNextNote(SolarEvents.valueOf(intent.getStringExtra(EXTRA_SOLAREVENT), SolarEvents.SUNSET));

            } else if (action.equals(ACTION_NOTE_NEXT)) {
                setUserSwappedCard( false, "handleIntent (nextNote)" );
                notes.showNextNote();

            } else if (action.equals(ACTION_NOTE_PREV)) {
                setUserSwappedCard( false, "handleIntent (prevNote)" );
                notes.showPrevNote();

            } else if (action.equals(ACTION_NOTE_RESET)) {
                setUserSwappedCard(false, "handleIntent (resetNote)");
                notes.resetNoteIndex();
                NoteData note = notes.getNote();
                if (note != null) {
                    highlightTimeField1(note.noteMode);
                }

            } else if (action.equals(ACTION_CARD_NEXT)) {
                setUserSwappedCard( true, "handleIntent (nextCard)" );
                scrollTo(card_layout.findFirstVisibleItemPosition() + 1);

            } else if (action.equals(ACTION_CARD_PREV)) {
                setUserSwappedCard( true, "handleIntent (prevCard)" );
                scrollTo(card_layout.findFirstVisibleItemPosition() - 1);

            } else if (action.equals(ACTION_CARD_RESET)) {
                setUserSwappedCard( true, "handleIntent (resetCard)" );
                if (card_layout.findFirstVisibleItemPosition() == CardAdapter.TODAY_POSITION)
                    scrollTo(CardAdapter.TODAY_POSITION + 1);
                else scrollTo(CardAdapter.TODAY_POSITION);

            } else if (action.equals(ACTION_CARD_SHOW)) {
                final long dateMillis = intent.getLongExtra(EXTRA_SHOW_DATE, -1);
                if (dateMillis >= 0)
                {
                    txt_time.post(new Runnable() {    // must be run after onCreate or onNewIntent has finished (and data is initialized)
                        @Override
                        public void run() {
                            int position = card_adapter.findPositionForDate(SuntimesActivity.this, dateMillis);
                            if (position >= 0 && position < CardAdapter.MAX_POSITIONS) {
                                setUserSwappedCard(true, ACTION_CARD_SHOW);
                                scrollTo(position);
                            }
                        }
                    });
                }

            } else if (action.equals(ACTION_CONFIG_LOCATION)) {
                configLocation();

            } else {
                if (data != null && LocationConfigView.SCHEME_GEO.equals(data.getScheme())) {
                    configLocation(data);
                }
            }
        }
    }

    public static HashMap<String, String> createLegacyActionMap() {
        return createLegacyActionMap(SUNTIMES_ACTIONS);
    }
    public static HashMap<String, String> createLegacyActionMap(String[] actions) {
        HashMap<String, String> actionMap = new HashMap<>();
        for (String action : actions) {
            actionMap.put(action.replaceFirst(SUNTIMES_ACTION_PREFIX, BuildConfig.APPLICATION_ID), action);   // "com.forrestguice.suntimeswidget.ACTION" -> "suntimes.action.ACTION"
        }
        return actionMap;
    }

    private void initTheme()
    {
        appTheme = AppSettings.loadThemePref(this);
        appThemeResID = AppSettings.setTheme(this, appTheme);

        String themeName = AppSettings.getThemeOverride(this, appThemeResID);
        if (themeName != null)
        {
            Log.i("initTheme", "Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }

        int[] attrs = new int[] { R.attr.sunnoonIcon, R.attr.text_disabledColor };
        TypedArray a = obtainStyledAttributes(attrs);
        resID_noonIcon = a.getResourceId(0, R.drawable.ic_noon_large);
        resID_buttonDisabledColor = a.getResourceId(1, R.color.text_disabled_dark);
        a.recycle();

        GetFixUI.themeIcons(this);
    }

    private void initLocale( Context context )
    {
        WidgetSettings.initDefaults(context);        // locale specific defaults

        SuntimesUtils.initDisplayStrings(context);   // locale specific strings
        AppSettings.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);

        initColors(context);                         // locale specific colors
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        //Log.d("DEBUG", "onStart");

        calculateData(SuntimesActivity.this);

        registerReceivers(SuntimesActivity.this);
        setUpdateAlarms(SuntimesActivity.this);

        updateViews(SuntimesActivity.this);
        onResume_resetNoteIndex = true;        // reset to true (to be set false again by onRestoreInstanceState if needed)
    }

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    @Override
    public void onResume()
    {
        super.onResume();
        //Log.d("DEBUG", "onResume");

        updateActionBar(this);
        getFixHelper.onResume();

        if (onResume_resetNoteIndex) {
            notes.resetNoteIndex();
        }

        // restore open dialogs
        updateDialogs(this);

        FragmentManager fragments = getSupportFragmentManager();
        TimeZoneDialog timezoneDialog = (TimeZoneDialog) fragments.findFragmentByTag(DIALOGTAG_TIMEZONE);
        if (timezoneDialog != null)
        {
            timezoneDialog.setNow(dataset.nowThen(dataset.calendar()));
            timezoneDialog.setLongitude(dataset.location().getLongitudeAsDouble());
            timezoneDialog.setCalculator(dataset.calculator());
            timezoneDialog.setOnAcceptedListener(onConfigTimeZone);
            timezoneDialog.setOnCanceledListener(onCancelTimeZone);
            //Log.d("DEBUG", "TimeZoneDialog listeners restored.");
        }

        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null)
        {
            locationDialog.setOnAcceptedListener( onConfigLocation(locationDialog) );
            //Log.d("DEBUG", "LocationConfigDialog listeners restored.");
        }

        TimeDateDialog dateDialog = (TimeDateDialog) fragments.findFragmentByTag(DIALOGTAG_DATE);
        if (dateDialog != null)
        {
            dateDialog.setTimezone(dataset.timezone());
            dateDialog.setOnAcceptedListener(onConfigDate);
            dateDialog.setOnCanceledListener(onCancelDate);
            //Log.d("DEBUG", "TimeDateDialog listeners restored.");
        }
    }

    private void updateDialogs(Context context)
    {
        FragmentManager fragments = getSupportFragmentManager();

        AlarmCreateDialog alarmDialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_ALARM);
        if (alarmDialog != null) {
            alarmDialog.setOnAcceptedListener(onScheduleAlarm);
            alarmDialog.setOnNeutralListener(onManageAlarms);
            //Log.d("DEBUG", "AlarmCreateDialog listeners restored.");
        }

        LightMapDialog lightMapDialog = (LightMapDialog) fragments.findFragmentByTag(DIALOGTAG_LIGHTMAP);
        if (lightMapDialog != null)
        {
            lightMapDialog.themeViews(this, appThemeOverride);
            lightMapDialog.setData(context, dataset);
            lightMapDialog.setDialogListener(lightMapListener);
            lightMapDialog.updateViews();
            //Log.d("DEBUG", "LightMapDialog updated on restore.");
        }

        WorldMapDialog worldMapDialog = (WorldMapDialog) fragments.findFragmentByTag(DIALOGTAG_WORLDMAP);
        if (worldMapDialog != null)
        {
            worldMapDialog.themeViews(this, appThemeOverride);
            worldMapDialog.setData(dataset);
            worldMapDialog.setDialogListener(worldMapListener);
            worldMapDialog.updateViews();
            //Log.d("DEBUG", "WorldMapDialog updated on restore.");
        }

        EquinoxDialog equinoxDialog = (EquinoxDialog) fragments.findFragmentByTag(DIALOGTAG_EQUINOX);
        if (equinoxDialog != null)
        {
            equinoxDialog.themeViews(this, appThemeOverride);
            equinoxDialog.setDialogListener(equinoxDialogListener);
            equinoxDialog.updateViews();
            //Log.d("DEBUG", "EquinoxDialog updated on restore.");
        }

        MoonDialog moonDialog = (MoonDialog) fragments.findFragmentByTag(DIALOGTAG_MOON);
        if (moonDialog != null)
        {
            moonDialog.themeViews(this, appThemeOverride);
            moonDialog.setData((dataset_moon != null) ? dataset_moon : new SuntimesMoonData(SuntimesActivity.this, 0, "moon"));
            moonDialog.setDialogListener(moonDialogListener);
            moonDialog.updateViews();
            //Log.d("DEBUG", "MoonDialog updated on restore.");
        }
    }

    /**
     * @param context
     */
    protected void registerReceivers(Context context)
    {
        unregisterReceivers(context);
        context.registerReceiver(fullUpdateReceiver, new IntentFilter(SUNTIMES_APP_UPDATE_FULL));
        context.registerReceiver(partialUpdateReceiver, new IntentFilter(SUNTIMES_APP_UPDATE_PARTIAL));
    }
    protected void unregisterReceivers(Context context)
    {
        try {
            context.unregisterReceiver(fullUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // EMPTY
            //Log.w("UpdateAlarms", "unregisterReceiver: attempted to unregister non-registered receiver (SUNTIMES_APP_UPDATE_FULL)");
        }

        try {
            context.unregisterReceiver(partialUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // EMPTY
            //Log.w("UpdateAlarms", "unregisterReceiver: attempted to unregister non-registered receiver (SUNTIMES_APP_UPDATE_PARTIAL)");
        }
    }

    /**
     * @param context
     */
    protected void setUpdateAlarms( Context context )
    {
        setFullUpdateAlarm(context);
        setPartialUpdateAlarm(context);
    }
    protected void setFullUpdateAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            Calendar now = dataset.now();
            Calendar midnight = SuntimesRiseSetDataset.midnight(dataset.dataActual.getOtherCalendar());
            Log.d("UpdateAlarms", "setAlarm (fullUpdate): " + utils.calendarDateTimeDisplayString(context, midnight).toString());

            if (midnight.after(now))
                setUpdateAlarm(alarmManager, midnight, getFullUpdateIntent(context));
            //else Log.d("UpdateAlarms", "..skipping alarm fullUpdate (isPast)");
        }
    }
    protected void setPartialUpdateAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            Calendar now = dataset.now();
            Calendar updateTime = dataset.findNextEvent();
            if (updateTime != null && updateTime.after(now)) {
                Log.d("UpdateAlarms", "setAlarm (partialUpdate): " + utils.calendarDateTimeDisplayString(context, updateTime).toString());
                setUpdateAlarm(alarmManager, updateTime, getPartialUpdateIntent(context));
            }
            //else Log.d("UpdateAlarms", "..skipping alarm: partialUpdate (isPast)");
        }
    }
    protected void setUpdateAlarm(@NonNull AlarmManager alarmManager, Calendar updateTime, PendingIntent alarmIntent)
    {
        if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC, updateTime.getTimeInMillis(), alarmIntent);
        else alarmManager.set(AlarmManager.RTC, updateTime.getTimeInMillis(), alarmIntent);
    }

    /**
     * @param context
     */
    protected void unsetUpdateAlarms( Context context )
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            alarmManager.cancel(getFullUpdateIntent(context));
            alarmManager.cancel(getPartialUpdateIntent(context));
        }
    }

    /**
     * @param context
     * @return
     */
    protected PendingIntent getFullUpdateIntent(Context context)
    {
        return PendingIntent.getBroadcast(context, 0, new Intent(SuntimesActivity.SUNTIMES_APP_UPDATE_FULL), 0);
    }
    protected BroadcastReceiver fullUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null && action.equals(SUNTIMES_APP_UPDATE_FULL))
            {
                Log.d("UpdateAlarms", "onReceive: " + SUNTIMES_APP_UPDATE_FULL);
                invalidateData(SuntimesActivity.this);
                calculateData(SuntimesActivity.this);
                setFullUpdateAlarm(SuntimesActivity.this);
                updateDialogs(SuntimesActivity.this);
                updateViews(SuntimesActivity.this);
            }
        }
    };

    /**
     * @param context
     * @return
     */
    protected PendingIntent getPartialUpdateIntent(Context context)
    {
        return PendingIntent.getBroadcast(context, 0, new Intent(SuntimesActivity.SUNTIMES_APP_UPDATE_PARTIAL), 0);
    }
    protected BroadcastReceiver partialUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null && action.equals(SUNTIMES_APP_UPDATE_PARTIAL))
            {
                Log.d("UpdateAlarms", "onReceive: " + SUNTIMES_APP_UPDATE_PARTIAL);

                if (appTheme.equals(AppSettings.THEME_DAYNIGHT))
                {
                    boolean needsRecreate = dataset.isDay() ? (appThemeResID != R.style.AppTheme_Light)
                                                            : (appThemeResID != R.style.AppTheme_Dark);
                    if (needsRecreate)
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(recreateRunnable, 0);
                        return;
                    }
                }

                setPartialUpdateAlarm(SuntimesActivity.this);
                notes.resetNoteIndex();
                updateViews(SuntimesActivity.this);
            }
        }
    };

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    /*@Override
    public void onPause()
    {
        super.onPause();
        //Log.d("DEBUG", "onPause");
    }*/

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        //Log.d("DEBUG", "onSaveInstanceState");

        saveWarnings(outState);
        outState.putInt(KEY_UI_NOTEINDEX, notes.getNoteIndex());
        outState.putLong(KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        outState.putInt(KEY_UI_CARDPOSITION, ((card_layout.findFirstVisibleItemPosition() + card_layout.findLastVisibleItemPosition()) / 2));
        card_equinoxSolstice.saveState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        //Log.d("DEBUG", "onRestoreInstanceState");

        restoreWarnings(savedInstanceState);
        userSwappedCard = savedInstanceState.getLong(KEY_UI_USERSWAPPEDCARD, -1L);
        card_equinoxSolstice.loadState(savedInstanceState);

        int noteIndex = savedInstanceState.getInt(KEY_UI_NOTEINDEX);
        if (noteIndex >= 0 && noteIndex != notes.getNoteIndex()) {
            notes.setNoteIndex(noteIndex);
        }
        onResume_resetNoteIndex = false;

        int cardPosition = savedInstanceState.getInt(KEY_UI_CARDPOSITION, CardAdapter.TODAY_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = CardAdapter.TODAY_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap
    }

    /**
     * OnStop: the Activity no longer visible
     */
    @Override
    public void onStop()
    {
        //Log.d("DEBUG", "onStop");
        unregisterReceivers(SuntimesActivity.this);
        unsetUpdateAlarms(SuntimesActivity.this);

        stopTimeTask();
        getFixHelper.cancelGetFix();
        super.onStop();
    }

    /**
     * OnDestroy: the activity destroyed
     */
    /*@Override
    public void onDestroy()
    {
        super.onDestroy();
        //Log.d("DEBUG", "onDestroy");
    }*/

    /**
     * @param requestCode the request code that was passed to requestPermissions
     * @param permissions the requested permissions
     * @param grantResults either PERMISSION_GRANTED or PERMISSION_DENIED for each of the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        FragmentManager fragments = getSupportFragmentManager();
        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null)
        {
            locationDialog.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        WidgetSettings.LocationMode locationMode = WidgetSettings.loadLocationModePref(this, 0);
        if (locationMode == WidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            getFixHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUNTIMES_SETTINGS_REQUEST && resultCode == RESULT_OK)
        {
            boolean needsRecreate = ((!AppSettings.loadThemePref(SuntimesActivity.this).equals(appTheme))                           // theme mode changed
                    || (appThemeOverride != null && !appThemeOverride.themeName().equals(AppSettings.getThemeOverride(this, appThemeResID))) // or theme override changed
                    || (localeInfo.localeMode != AppSettings.loadLocaleModePref(SuntimesActivity.this))                             // or localeMode changed
                    || ((localeInfo.localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE                                              // or customLocale changed
                    && !AppSettings.loadLocalePref(SuntimesActivity.this).equals(localeInfo.customLocale))));

            if (needsRecreate)
            {
                Log.i("SuntimesActivity", "theme/locale was changed; calling recreate");
                Handler handler = new Handler();
                handler.postDelayed(recreateRunnable, 0);    // post to end of execution queue (onResume must be allowed to finish before calling recreate)
            }
        }
    }

    private Runnable recreateRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                recreate();

            } else {
                finish();
                startActivity(getIntent());
            }
        }
    };

    /**
     * Override the appearance of views if appThemeOverride is defined.
     * @param context Context
     */
    protected void themeViews(Context context)
    {
        if (appThemeOverride != null)
        {
            Log.i("themeViews", "Applying theme: " + appThemeOverride.themeName());
            int titleColor = appThemeOverride.getTitleColor();
            int timeColor = appThemeOverride.getTimeColor();
            int textColor = appThemeOverride.getTextColor();
            int disabledColor = ContextCompat.getColor(context, resID_buttonDisabledColor);
            int pressedColor = appThemeOverride.getActionColor();
            float suffixSizeSp = appThemeOverride.getTimeSuffixSizeSp();
            float textSizeSp = appThemeOverride.getTextSizeSp();
            float titleSizeSp = appThemeOverride.getTitleSizeSp();
            boolean titleBold = appThemeOverride.getTitleBold();

            Toolbar actionBar = (Toolbar) findViewById(R.id.app_menubar);
            actionBar.setTitleTextColor(titleColor);
            actionBar.setSubtitleTextColor(textColor);

            txt_time.setTextColor(timeColor);
            txt_time_suffix.setTextColor(timeColor);

            txt_timezone.setTextColor(SuntimesUtils.colorStateList(textColor, disabledColor, pressedColor));
            txt_timezone.setTextSize(suffixSizeSp);

            txt_time1_note1.setTextColor(timeColor);
            txt_time1_note1.setTextSize(titleSizeSp);
            txt_time1_note1.setTypeface(txt_time1_note1.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));
            txt_time2_note1.setTextColor(timeColor);
            txt_time2_note1.setTextSize(titleSizeSp);
            txt_time2_note1.setTypeface(txt_time2_note1.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));

            txt_time1_note2.setTextColor(textColor);
            txt_time1_note2.setTextSize(textSizeSp);
            txt_time2_note2.setTextColor(textColor);
            txt_time2_note2.setTextSize(textSizeSp);

            txt_time1_note3.setTextSize(titleSizeSp);
            txt_time1_note3.setTypeface(txt_time1_note3.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));
            txt_time2_note3.setTextSize(titleSizeSp);
            txt_time2_note3.setTypeface(txt_time2_note3.getTypeface(), (titleBold ? Typeface.BOLD : Typeface.NORMAL));

            txt_datasource.setTextColor(SuntimesUtils.colorStateList(textColor, disabledColor, pressedColor));
            txt_datasource.setTextSize(suffixSizeSp);

            txt_altitude.setTextColor(timeColor);
            txt_altitude.setTextSize(suffixSizeSp);

            color_textTimeDelta = appThemeOverride.getTimeColor();
            card_adapter.setThemeOverride(appThemeOverride);
            card_equinoxSolstice.themeViews(context, appThemeOverride);
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        initActionBar(context);
        initClockViews(context);
        initNoteViews(context);
        initCardViews(context);
        initEquinoxViews(context);
        initMisc(context);
    }

    /**
     * initialize warning snackbar
     */
    private void initWarnings(Context context, Bundle savedState)
    {
        timezoneWarning = new SuntimesWarning(WARNINGID_TIMEZONE);
        dateWarning = new SuntimesWarning(WARNINGID_DATE);

        warnings = new ArrayList<SuntimesWarning>();
        warnings.add(timezoneWarning);
        warnings.add(dateWarning);

        restoreWarnings(savedState);
    }
    private SuntimesWarning.SuntimesWarningListener warningListener = new SuntimesWarning.SuntimesWarningListener() {
        @Override
        public void onShowNextWarning() {
            showWarnings();
        }
    };

    /**
     * initialize the actionbar
     */
    private void initActionBar(Context context)
    {
        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
    }

    private void initMisc(final Context context)
    {
        layout_datasource = findViewById(R.id.layout_datasource);

        txt_datasource = (TextView)findViewById(R.id.txt_datasource);
        if (txt_datasource != null)
        {
            txt_datasource.setClickable(true);
            txt_datasource.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showGeneralSettings();
                }
            });
            txt_datasource.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    showGeneralSettings();
                    return true;
                }
            });
        }

        txt_altitude = (TextView) findViewById(R.id.txt_altitude);
        check_altitude = (AppCompatCheckBox) findViewById(R.id.check_altitude);

        layout_altitude = findViewById(R.id.layout_altitude);
        if (layout_altitude != null)
        {
            layout_altitude.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    boolean useAltitude = WidgetSettings.loadLocationAltitudeEnabledPref(SuntimesActivity.this, 0);
                    WidgetSettings.saveLocationAltitudeEnabledPref(SuntimesActivity.this, 0, !useAltitude);
                    CalculatorProvider.clearCachedConfig(0);
                    calculateData(SuntimesActivity.this);
                    setUpdateAlarms(SuntimesActivity.this);
                    updateViews(SuntimesActivity.this);
                }
            });
        }
    }
    
    /**
     * initialize gps helper
     */
    private void initGetFix()
    {
        getFixHelper = new GetFixHelper(this, new GetFixUI()
        {
            private MenuItem refreshItem = null;

            @Override
            public void enableUI(boolean value)
            {
                if (refreshItem != null)
                {
                    refreshItem.setEnabled(value);
                }
            }

            @Override
            public void updateUI(Location... locations)
            {
                if (locations[0] != null)
                {
                    com.forrestguice.suntimeswidget.calculator.core.Location location = new com.forrestguice.suntimeswidget.calculator.core.Location(getString(R.string.gps_lastfix_title_found), locations[0]);
                    actionBar.setSubtitle(location.toString());
                }
            }

            @Override
            public void showProgress(boolean showProgress)
            {
                note_progress.setVisibility((showProgress ? View.VISIBLE : View.GONE));
            }

            @Override
            public void onStart()
            {
                invalidateData(SuntimesActivity.this);

                refreshItem = actionBarMenu.findItem(R.id.action_location_refresh);
                if (refreshItem != null)
                {
                    actionBar.setTitle(getString(R.string.gps_lastfix_title_searching));
                    actionBar.setSubtitle("");
                    refreshItem.setIcon(GetFixUI.ICON_GPS_SEARCHING);
                }
            }

            @Override
            public void onResult(Location result, boolean wasCancelled)
            {
                if (refreshItem != null)
                {
                    refreshItem.setIcon((result != null) ? ICON_GPS_FOUND :
                            (getFixHelper.isLocationEnabled(SuntimesActivity.this) ? ICON_GPS_FOUND
                                                                                           : ICON_GPS_DISABLED));

                    if (result != null)
                    {
                        com.forrestguice.suntimeswidget.calculator.core.Location location = new com.forrestguice.suntimeswidget.calculator.core.Location(getString(R.string.gps_lastfix_title_found), result);
                        WidgetSettings.saveLocationPref(SuntimesActivity.this, 0, location);

                    } else {
                        String msg = (wasCancelled ? getString(R.string.gps_lastfix_toast_cancelled) : getString(R.string.gps_lastfix_toast_notfound));
                        Toast errorMsg = Toast.makeText(SuntimesActivity.this, msg, Toast.LENGTH_LONG);
                        errorMsg.show();
                    }
                    SuntimesActivity.this.calculateData(SuntimesActivity.this);
                    SuntimesActivity.this.setUpdateAlarms(SuntimesActivity.this);
                    SuntimesActivity.this.updateViews(SuntimesActivity.this);

                    Intent updateWidgets = new Intent();
                    updateWidgets.setAction(SuntimesWidget0.SUNTIMES_ALARM_UPDATE);
                    sendBroadcast(updateWidgets);
                }
            }
        });
    }

    /**
     * update actionbar items; shouldn't be called until after the menu is inflated.
     */
    private void updateActionBar(Context context)
    {
        if (actionBarMenu != null)
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                MenuItem mapItem = actionBarMenu.findItem(R.id.action_location_show);
                if (mapItem != null)
                {
                    boolean showMapButton = AppSettings.loadShowMapButtonPref(this);
                    int showAsAction = (showMapButton ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER);
                    mapItem.setShowAsAction(showAsAction);
                }
            }

            MenuItem refreshItem = actionBarMenu.findItem(R.id.action_location_refresh);
            if (refreshItem != null)
            {
                WidgetSettings.LocationMode mode = WidgetSettings.loadLocationModePref(context, 0);
                if (mode != WidgetSettings.LocationMode.CURRENT_LOCATION)
                {
                    refreshItem.setVisible(false);

                } else {
                    refreshItem.setIcon((getFixHelper.isLocationEnabled(SuntimesActivity.this) ? GetFixUI.ICON_GPS_FOUND : GetFixUI.ICON_GPS_DISABLED));
                    refreshItem.setVisible(true);
                }
            }
        }
    }

    /**
     * initialize the note flipper and associated views
     * @param context a context used to access resources
     */
    private void initNoteViews(Context context)
    {
        note_progress = (ProgressBar) findViewById(R.id.info_note_progress);
        if (note_progress != null)
        {
            note_progress.setVisibility(View.GONE);
        }

        note_flipper = (ViewFlipper) findViewById(R.id.info_note_flipper);
        if (note_flipper != null)
        {
            note_flipper.setOnTouchListener(noteTouchListener);
            note_flipper.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                { /* DO NOTHING HERE (but we still need this listener) */ }
            });

        } else {
            Log.w("initNoteViews", "Failed to set touchListener; note_flipper is null!");
        }

        LinearLayout note1 = (LinearLayout) findViewById(R.id.info_time_note1);
        if (note1 != null)
        {
            txt_time1_note1 = (TextView) note1.findViewById(R.id.text_timenote1);
            txt_time1_note2 = (TextView) note1.findViewById(R.id.text_timenote2);
            txt_time1_note3 = (TextView) note1.findViewById(R.id.text_timenote3);
            ic_time1_note = (ImageView) note1.findViewById(R.id.icon_timenote);
            ic_time1_note.setVisibility(View.INVISIBLE);

        } else {
            Log.w("initNoteViews", "Failed to init note layout1; was null!");
        }

        LinearLayout note2 = (LinearLayout) findViewById(R.id.info_time_note2);
        if (note2 != null)
        {
            txt_time2_note1 = (TextView) note2.findViewById(R.id.text_timenote1);
            txt_time2_note2 = (TextView) note2.findViewById(R.id.text_timenote2);
            txt_time2_note3 = (TextView) note2.findViewById(R.id.text_timenote3);
            ic_time2_note = (ImageView) note2.findViewById(R.id.icon_timenote);
            ic_time2_note.setVisibility(View.INVISIBLE);

        } else {
            Log.w("initNoteViews", "Failed to init note layout2; was null!");
        }
    }

    /**
     * initialize the solstice/equinox views
     * @param context a context used to access resources
     */
    private void initEquinoxViews(Context context)
    {
        equinoxLayout = findViewById(R.id.info_time_equinox_layout);

        card_equinoxSolstice = (EquinoxView) findViewById(R.id.info_date_solsticequinox);
        card_equinoxSolstice.setMinimized(true);
        card_equinoxSolstice.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showEquinoxDialog();
            }
        });
        card_equinoxSolstice.setOnLongClickListener( new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                showEquinoxDialog();
                return true;
            }
        });
    }

    /**
     * initialize the card flipper and associated views
     * @param context a context used to access resources
     */
    private void initCardViews(Context context)
    {
        card_adapter = new CardAdapter(context);
        card_adapter.setCardAdapterListener(cardAdapterListener);

        card_view = (RecyclerView) findViewById(R.id.info_time_flipper1);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.setLayoutManager(card_layout = new CardLayoutManager(this));
        card_view.addItemDecoration(new CardAdapter.CardViewDecorator(this));

        card_view.setAdapter(card_adapter);
        card_view.scrollToPosition(CardAdapter.TODAY_POSITION);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(card_view);
        card_scroller = new CardAdapter.CardViewScroller(context);
        card_view.setOnScrollListener(onCardScrollListener);
    }

    /**
     * initialize the clock ui
     * @param context a context used to access resources
     */
    private void initClockViews(Context context)
    {
        LinearLayout clockLayout = (LinearLayout) findViewById(R.id.layout_clock);
        if (clockLayout != null)
        {
            clockLayout.setOnClickListener(onClockClick);
        }

        txt_time = (TextView) findViewById(R.id.text_time);
        txt_time_suffix = (TextView) findViewById(R.id.text_time_suffix);

        txt_timezone = (TextView) findViewById(R.id.text_timezone);
        txt_timezone.setOnClickListener(onTimeZoneClick);

        float fontScale = getResources().getConfiguration().fontScale;
        if (fontScale > 1)
        {                                                // when using "large text"...
            float textSizePx = txt_time.getTextSize();       // revert scaling on txt_time (its already large enough / takes too much space at x1.3)
            float invFontScale = (1 / fontScale);
            float adjustedTextSizePx = textSizePx * invFontScale;
            Log.w("initClockViews", "txt_time is oversized! downsizing from " + textSizePx + "px to " + adjustedTextSizePx + "px.");
            txt_time.setTextSize(TypedValue.COMPLEX_UNIT_PX, adjustedTextSizePx);
            txt_time_suffix.setTextSize(TypedValue.COMPLEX_UNIT_PX, (txt_time_suffix.getTextSize() * invFontScale));
        }
    }

    /**
     * @param context a context used to access resources
     */
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = Color.WHITE;

        color_textTimeDelta = ContextCompat.getColor(context, typedArray.getResourceId(0, def));

        typedArray.recycle();
    }

    /**
     * Initialize note object and onChanged listener.
     */
    private void initNotes()
    {
        notes = new SuntimesNotes();
        notes.themeViews(this, appThemeOverride);
        notes.init(this, dataset, dataset_moon);
        notes.setOnChangedListener(new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition)
            {
                updateNoteUI(note, transition);
            }
        });
    }

    private View.OnClickListener onMoonriseClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showMoonDialog();
        }
    };
    private View.OnLongClickListener onMoonriseLongClick = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v) {
            showMoonDialog();
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        actionBarMenu = menu;
        updateActionBar(this);
        return true;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                showSettings();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_location_add:
                configLocation();
                return true;

            case R.id.action_location_refresh:
                refreshLocation();
                return false;

            case R.id.action_location_show:
                showMap();
                return true;

            case R.id.action_timezone:
                configTimeZone();
                return true;

            case R.id.action_date:
                configDate();
                return true;

            case R.id.action_alarm:
                scheduleAlarm();
                return true;

            case R.id.action_equinox:
                showEquinoxDialog();
                return true;

            case R.id.action_moon:
                showMoonDialog();
                return true;

            case R.id.action_sunposition:
                showLightMapDialog();
                return true;

            case R.id.action_worldmap:
                showWorldMapDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Select a date other than today.
     */
    private void configDate()
    {
        final TimeDateDialog datePicker = new TimeDateDialog();
        datePicker.setTimezone(dataset.timezone());
        datePicker.setOnAcceptedListener(onConfigDate);
        datePicker.setOnCanceledListener(onCancelDate);
        datePicker.show(getSupportFragmentManager(), DIALOGTAG_DATE);
    }
    DialogInterface.OnClickListener onConfigDate = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            afterConfigDate();
        }
    };
    DialogInterface.OnClickListener onCancelDate = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            showWarnings();
        }
    };
    private void afterConfigDate()
    {
        dateWarning.reset();
        calculateData(SuntimesActivity.this);
        setUpdateAlarms(SuntimesActivity.this);
        updateViews(SuntimesActivity.this);
        scrollTo(CardAdapter.TODAY_POSITION);
        Log.d("DEBUG", "afterConfigDate");
    }

    /**
     * Refresh location (current location mode).
     */
    protected void refreshLocation()
    {
        getFixHelper.getFix();
    }

    /**
     * Configure location.
     */
    protected void configLocation()
    {
        configLocation(null);
    }
    protected void configLocation( Uri data )
    {
        final LocationConfigDialog locationDialog = new LocationConfigDialog();
        locationDialog.setData(data);
        locationDialog.setHideTitle(true);
        locationDialog.setOnAcceptedListener(onConfigLocation(locationDialog));

        getFixHelper.cancelGetFix();
        locationDialog.show(getSupportFragmentManager(), DIALOGTAG_LOCATION);
    }
    protected DialogInterface.OnClickListener onConfigLocation( final LocationConfigDialog dialog )
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                CalculatorProvider.clearCachedConfig(0);
                calculateData(SuntimesActivity.this);
                setUpdateAlarms(SuntimesActivity.this);
                updateActionBar(SuntimesActivity.this);
                updateViews(SuntimesActivity.this);

                WidgetSettings.LocationMode locationMode = dialog.getDialogContent().getLocationMode();
                if (locationMode == WidgetSettings.LocationMode.CURRENT_LOCATION)
                {
                    getFixHelper.getFix();

                } else {
                    Intent updateWidgets = new Intent();
                    updateWidgets.setAction(SuntimesWidget0.SUNTIMES_ALARM_UPDATE);
                    sendBroadcast(updateWidgets);
                }
            }
        };
    }



    /**
     * Configure time zone.
     */
    protected void configTimeZone()
    {
        TimeZoneDialog timezoneDialog = new TimeZoneDialog();
        timezoneDialog.setNow(dataset.nowThen(dataset.calendar()));
        timezoneDialog.setLongitude(dataset.location().getLongitudeAsDouble());
        timezoneDialog.setCalculator(dataset.calculator());
        timezoneDialog.setOnAcceptedListener(onConfigTimeZone);
        timezoneDialog.setOnCanceledListener(onCancelTimeZone);
        timezoneDialog.show(getSupportFragmentManager(), DIALOGTAG_TIMEZONE);
    }
    DialogInterface.OnClickListener onConfigTimeZone = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            timezoneWarning.reset();
            CalculatorProvider.clearCachedConfig(0);
            calculateData(SuntimesActivity.this);
            setUpdateAlarms(SuntimesActivity.this);
            updateViews(SuntimesActivity.this);
        }
    };
    DialogInterface.OnClickListener onCancelTimeZone = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            showWarnings();
        }
    };

    /**
     * Show the location on a map.
     * Intent filtering code based off answer by "gumberculese";
     * http://stackoverflow.com/questions/5734678/custom-filtering-of-intent-chooser-based-on-installed-android-package-name
     */
    protected void showMap()
    {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(location.getUri());

        List<ResolveInfo> info = getPackageManager().queryIntentActivities(mapIntent, 0);
        List<Intent> geoIntents = new ArrayList<Intent>();

        if (!info.isEmpty())
        {
            for (ResolveInfo resolveInfo : info)
            {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!TextUtils.equals(packageName, BuildConfig.APPLICATION_ID))
                {
                    Intent geoIntent = new Intent(Intent.ACTION_VIEW);
                    geoIntent.setPackage(packageName);
                    geoIntent.setData(location.getUri());
                    geoIntents.add(geoIntent);
                }
            }
        }

        if (geoIntents.size() > 0)
        {
            Intent chooserIntent = Intent.createChooser(geoIntents.remove(0), getString(R.string.configAction_mapLocation_chooser));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, geoIntents.toArray(new Parcelable[0]));
            startActivity(chooserIntent);

        } else {
            Toast noAppError = Toast.makeText(this, getString(R.string.configAction_mapLocation_noapp), Toast.LENGTH_LONG);
            noAppError.show();
        }
    }

    /**
     * Show the help dialog.
     */
    protected void showHelp()
    {
        String actual = getString(R.string.help_general_actualTime);
        String twilight = getString(R.string.help_general_twilight);
        String dayLength = getString(R.string.help_general_daylength);
        String timeText = getString(R.string.help_general3, actual, twilight, dayLength);

        String goldHour = getString(R.string.help_general_goldhour);
        String blueHour = getString(R.string.help_general_bluehour);
        String blueGoldText = getString(R.string.help_general2, blueHour, goldHour);

        String moonIllum = getString(R.string.help_general_moonillum);

        String helpText = getString(R.string.help_general3, moonIllum, timeText, blueGoldText);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpText);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * Show the about dialog.
     */
    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }



    /**
     * Show application settings.
     */
    protected void showSettings()
    {
        Intent settingsIntent = new Intent(this, SuntimesSettingsActivity.class);
        startActivityForResult(settingsIntent, SUNTIMES_SETTINGS_REQUEST);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }
    protected void showGeneralSettings()
    {
        Intent settingsIntent = new Intent(this, SuntimesSettingsActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            settingsIntent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SuntimesSettingsActivity.GeneralPrefsFragment.class.getName() );
            settingsIntent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );

        } else {
            settingsIntent.setAction(SuntimesSettingsActivity.ACTION_PREFS_GENERAL);
        }
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    /**
     * Show the alarm dialog.
     */
    protected void scheduleAlarm()
    {
        scheduleAlarm(null);
    }
    protected void scheduleAlarm( SolarEvents event )
    {
        if (dataset.isCalculated())
        {
            AlarmCreateDialog dialog = new AlarmCreateDialog();
            dialog.loadSettings(SuntimesActivity.this);
            dialog.setEvent((event != null ? event.name() : dialog.getEvent()), WidgetSettings.loadLocationPref(this, 0));    // TODO: bug; dialog fails to switch tabs if already showing "by time"
            dialog.setShowAlarmListButton(true);
            dialog.setOnAcceptedListener(onScheduleAlarm);
            dialog.setOnNeutralListener(onManageAlarms);
            dialog.show(getSupportFragmentManager(), DIALOGTAG_ALARM);

        } else {
            String msg = getString(R.string.schedalarm_dialog_error2);
            Toast errorMsg = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            errorMsg.show();
        }
    }

    private DialogInterface.OnClickListener onScheduleAlarm = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            Activity context = SuntimesActivity.this;
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_ALARM);
            if (dialog != null)
            {
                AlarmClockItem.AlarmType type = dialog.getAlarmType();
                com.forrestguice.suntimeswidget.calculator.core.Location location = dialog.getLocation();
                switch (dialog.getMode())
                {
                    case 1:
                        int hour = dialog.getHour();
                        int minutes = dialog.getMinute();
                        String timezone = dialog.getTimeZone();
                        AlarmClockActivity.scheduleAlarm(context, type, "", null, location, hour, minutes, timezone);   // TODO: label
                        break;

                    case 0:
                    default:
                        String eventString = dialog.getEvent();
                        AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, getContentResolver());
                        String alarmLabel = eventString != null ? context.getString(R.string.schedalarm_labelformat2, eventItem.getTitle()) : "";
                        if (eventString != null) {
                            AlarmClockActivity.scheduleAlarm(context, type, alarmLabel, eventString, location);
                        }
                        break;
                }
            }
        }
    };

    private DialogInterface.OnClickListener onManageAlarms = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface d, int which)
        {
            FragmentManager fragments = getSupportFragmentManager();
            AlarmCreateDialog dialog = (AlarmCreateDialog) fragments.findFragmentByTag(DIALOGTAG_ALARM);
            if (dialog != null) {
                dialog.dismiss();
            }

            Context context = SuntimesActivity.this;
            Intent alarmIntent = new Intent(context, AlarmClockActivity.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);
        }
    };

    protected void scheduleAlarmFromNote()
    {
        scheduleAlarm(notes.getNote().noteMode);
    }

    protected void calculateData( Context context )
    {
        card_adapter.initData(context);
        Pair<SuntimesRiseSetDataset, SuntimesMoonData> cardData = card_adapter.initData(context, CardAdapter.TODAY_POSITION);
        dataset = cardData.first;
        dataset_moon = cardData.second;

        dataset_equinox = null;
        if (AppSettings.loadShowEquinoxPref(context)) {
            EquinoxView.EquinoxViewAdapter card_adapter1 = (card_equinoxSolstice != null ? card_equinoxSolstice.getAdapter() : null);
            dataset_equinox = (card_adapter1 != null ? card_adapter1.initData(context) : null);
        }

        initNotes();
    }

    protected void invalidateData( Context context )
    {
        if (dataset != null) {
            dataset.invalidateCalculation();
        }
        if (dataset_moon != null) {
            dataset_moon.invalidateCalculation();
        }
        if (dataset_equinox != null) {
            dataset_equinox.invalidateCalculation();
        }
        if (card_adapter != null) {
            card_adapter.invalidateData();
        }
        updateViews(context);
    }

    protected void updateViews( Context context )
    {
        stopTimeTask();

        verboseAccessibility = AppSettings.loadVerboseAccessibilityPref(this);
        showWarnings = AppSettings.loadShowWarningsPref(this);
        dateWarning.setShouldShow(false);
        timezoneWarning.setShouldShow(false);

        WidgetSettings.LocationMode locationMode = WidgetSettings.loadLocationModePref(context, 0);
        location = WidgetSettings.loadLocationPref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        String locationTitle = (locationMode == WidgetSettings.LocationMode.CURRENT_LOCATION ? getString(R.string.gps_lastfix_title_found) : location.getLabel());

        SpannableString locationSubtitle;
        String locationString = getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
        boolean supportsAltitude = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_ALTITUDE);
        boolean enabledAltitude = WidgetSettings.loadLocationAltitudeEnabledPref(SuntimesActivity.this, 0);
        String altitudeString = "";
        if (supportsAltitude && enabledAltitude && location.getAltitudeAsInteger() != 0)
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
            SuntimesUtils.TimeDisplayText altitudeText = SuntimesUtils.formatAsHeight(context, location.getAltitudeAsDouble(), units, 0,true);
            altitudeString = getString(R.string.location_format_alt, altitudeText.getValue(), altitudeText.getUnits());
            String altitudeTag = getString(R.string.location_format_alttag, altitudeString);
            String displayString = getString(R.string.location_format_latlonalt, locationString, altitudeTag);
            locationSubtitle = SuntimesUtils.createRelativeSpan(null, displayString, altitudeTag, 0.5f);
        } else {
            locationSubtitle = new SpannableString(locationString);
        }

        if (actionBar != null)
        {
            actionBar.setTitle(locationTitle);
            actionBar.setSubtitle(locationSubtitle);
        }

        //
        // equinox and solstice
        //
        boolean enableEquinox = AppSettings.loadShowEquinoxPref(this);
        showEquinoxView(enableEquinox && card_equinoxSolstice.isImplemented(context));
        card_equinoxSolstice.setTrackingMode(WidgetSettings.loadTrackingModePref(context, AppWidgetManager.INVALID_APPWIDGET_ID));
        card_equinoxSolstice.updateViews(context);
        card_equinoxSolstice.post(updateEquinoxViewColumnWidth);

        //
        // clock & date
        //
        Calendar now = dataset.now();
        Date data_date = dataset.dataActual.date();

        if (dataset.dataActual.todayIsNotToday())
        {
            WidgetSettings.DateInfo nowInfo = new WidgetSettings.DateInfo(now);
            WidgetSettings.DateInfo dataInfo = new WidgetSettings.DateInfo(dataset.dataActual.calendar());
            if (!nowInfo.equals(dataInfo))
            {
                Date time = now.getTime();
                if (data_date.after(time)) {
                    dateWarning.setShouldShow(true);

                } else if (data_date.before(time)) {
                    dateWarning.setShouldShow(true);
                }
            }
        }

        // timezone field
        TimeZone timezone = dataset.timezone();
        timezoneWarning.setShouldShow( WidgetTimezones.isProbablyNotLocal(timezone, dataset.location(), dataset.date()) );
        int iconSize = (int) getResources().getDimension(R.dimen.statusIcon_size);
        ImageSpan timezoneWarningIcon = (showWarnings && timezoneWarning.shouldShow()) ? SuntimesUtils.createWarningSpan(this, iconSize) : null;

        boolean useDST = showWarnings && (Build.VERSION.SDK_INT < 24 ? timezone.useDaylightTime()
                                                                     : timezone.observesDaylightTime());
        boolean inDST = useDST && timezone.inDaylightTime(now.getTime());
        ImageSpan dstWarningIcon = (inDST) ? SuntimesUtils.createDstSpan(this, iconSize) : null;

        SuntimesUtils.ImageSpanTag[] timezoneTags = {
                new SuntimesUtils.ImageSpanTag(SuntimesUtils.SPANTAG_WARNING, timezoneWarningIcon),
                new SuntimesUtils.ImageSpanTag(SuntimesUtils.SPANTAG_DST, dstWarningIcon)
        };

        String timezoneString = getString(R.string.timezoneField, WidgetTimezones.getTimeZoneDisplay(context, timezone));
        SpannableStringBuilder timezoneSpan = SuntimesUtils.createSpan(this, timezoneString, timezoneTags);
        txt_timezone.setText(timezoneSpan);
        txt_timezone.setContentDescription(timezoneString.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), "")
                .replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_DST), ""));

        // datasource ui
        if (txt_datasource != null)
        {
            txt_datasource.setText(dataset.dataActual.calculator().name());
        }
        if (txt_altitude != null)
        {
            txt_altitude.setText(altitudeString);
        }
        if (check_altitude != null)
        {
            check_altitude.setChecked(enabledAltitude);
        }
        if (layout_altitude != null)
        {
            layout_altitude.setVisibility(supportsAltitude ? View.VISIBLE : View.INVISIBLE);
        }
        showDatasourceUI(AppSettings.loadDatasourceUIPref(this));
        showDayLength(dataset.isCalculated());
        showNotes(dataset.isCalculated());
        showWarnings();

        startTimeTask();
    }

    private Runnable updateEquinoxViewColumnWidth = new Runnable()
    {
        @Override
        public void run()
        {
            View card = findViewById(R.id.info_time_all_today);
            if (card != null) {
                View column = card.findViewById(R.id.header_column);
                if (column != null) {
                    card_equinoxSolstice.adjustColumnWidth(column.getMeasuredWidth());
                }
            }
        }
    };

    private void updateEquinoxDialogColumnWidth(EquinoxDialog equinoxDialog)
    {
        View card = findViewById(R.id.info_time_all_today);
        if (card != null && equinoxDialog != null)
        {
            View column = card.findViewById(R.id.header_column);
            if (column != null) {
                equinoxDialog.adjustColumnWidth(column.getMeasuredWidth());
            }
        }
    }

    private void showWarnings()
    {
        if (showWarnings && timezoneWarning.shouldShow() && !timezoneWarning.wasDismissed())
        {
            timezoneWarning.initWarning(this, txt_timezone, getString(R.string.timezoneWarning));
            timezoneWarning.getSnackbar().setAction(getString(R.string.configAction_setTimeZone), new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    configTimeZone();
                }
            });
            timezoneWarning.show();
            return;
        }

        if (showWarnings && dateWarning.shouldShow() && !dateWarning.wasDismissed())
        {
            dateWarning.initWarning(this, card_view, getString(R.string.dateWarning));
            dateWarning.getSnackbar().setAction(getString(R.string.configAction_setDate), new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    configDate();
                }
            });
            dateWarning.show();
            return;
        }

        // no warnings shown; clear previous (stale) messages
        timezoneWarning.dismiss();
        dateWarning.dismiss();
    }

    /**
     * Start updates to the clock ui.
     */
    private void startTimeTask()
    {
        txt_time.post(updateTimeTask);
    }

    /**
     * Stop updates to the clock ui.
     */
    private void stopTimeTask()
    {
        txt_time.removeCallbacks(updateTimeTask);
    }

    /**
     * Clock ui update rate; once every few seconds.
     */
    public static final int UPDATE_RATE = 3000;     // primary update rate: 3s

    /**
     * Update the clock ui at regular intervals to reflect current time (and note).
     */
    private Runnable updateTimeTask = new Runnable()
    {
        @Override
        public void run()
        {
            updateTimeViews(SuntimesActivity.this);
            txt_time.postDelayed(this, UPDATE_RATE);
        }
    };

    /**
     * Update the clock ui to reflect current time.
     * @param context the Activity context
     */
    protected void updateTimeViews(Context context)
    {
        Calendar now = dataset.now();
        //Log.d("DEBUG", "" + now.getTimeZone());
        SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(this, now);
        txt_time.setText(timeText.getValue());
        txt_time_suffix.setText(timeText.getSuffix());
        notes.updateNote(context, now);

        // TODO: commented during refactor ..restore this update
        //lightmap.updateViews(false);
    }

    protected void seekNextNote(SolarEvents event)
    {
        setUserSwappedCard(false, "seekNextNote: " + event);
        notes.setNoteIndex(notes.getNoteIndex(event));
        NoteData note = notes.getNote();
        if (note != null) {
            highlightTimeField1(note.noteMode);
        }
    }

    private CardAdapter.CardAdapterListener cardAdapterListener = new CardAdapter.CardAdapterListener()
    {
        @Override
        public void onDateClick(CardAdapter adapter, int position) {
            onTapAction(AppSettings.loadDateTapActionPref(SuntimesActivity.this), "onDateClick");
        }
        @Override
        public boolean onDateLongClick(CardAdapter adapter, int position) {
            onTapAction(AppSettings.loadDateTapAction1Pref(SuntimesActivity.this), "onDateLongClick");
            return true;
        }

        @Override
        public void onSunriseHeaderClick(CardAdapter adapter, int position)
        {
            if (AppSettings.loadShowHeaderTextPref(SuntimesActivity.this) == AppSettings.HEADER_TEXT_AZIMUTH) {
                onLightmapClick(adapter, position);
                if (position == CardAdapter.TODAY_POSITION || position == (CardAdapter.TODAY_POSITION + 1) || position == (CardAdapter.TODAY_POSITION - 1)) {
                    txt_time.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            seekNextNote(SolarEvents.SUNRISE);
                        }
                    }, 500);
                }
            } else {
                seekNextNote(SolarEvents.SUNRISE);
            }
        }
        @Override
        public boolean onSunriseHeaderLongClick(CardAdapter adapter, int position) {
            seekNextNote(SolarEvents.SUNRISE);
            return true;
        }

        @Override
        public void onSunsetHeaderClick(CardAdapter adapter, int position)
        {
            if (AppSettings.loadShowHeaderTextPref(SuntimesActivity.this) == AppSettings.HEADER_TEXT_AZIMUTH) {
                onLightmapClick(adapter, position);
                if (position == CardAdapter.TODAY_POSITION || position == (CardAdapter.TODAY_POSITION + 1) || position == (CardAdapter.TODAY_POSITION - 1)) {
                    txt_time.post(new Runnable() {
                        @Override
                        public void run() {
                            txt_time.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    seekNextNote(SolarEvents.SUNSET);
                                }
                            }, 500);
                        }
                    });
                }
            } else {
                seekNextNote(SolarEvents.SUNSET);
            }
        }
        @Override
        public boolean onSunsetHeaderLongClick(CardAdapter adapter, int position) {
            seekNextNote(SolarEvents.SUNSET);
            return true;
        }

        @Override
        public void onNoonHeaderClick(CardAdapter adapter, int position) {
            onLightmapClick(adapter, position);
        }
        @Override
        public boolean onNoonHeaderLongClick(CardAdapter adapter, int position) {
            onLightmapClick(adapter, position);
            return true;
        }

        @Override
        public void onMoonHeaderClick(CardAdapter adapter, int position) {
            showMoonDialog();
        }
        @Override
        public boolean onMoonHeaderLongClick(CardAdapter adapter, int position) {
            showMoonDialog();
            return true;
        }

        @Override
        public void onLightmapClick(CardAdapter adapter, int position) {
            Pair<SuntimesRiseSetDataset, SuntimesMoonData> cardData = adapter.initData(SuntimesActivity.this, position);
            if (Math.abs(CardAdapter.TODAY_POSITION - position) > 1 && cardData != null) {
                showSunPositionAt(cardData.first.dataNoon.calendar().getTimeInMillis());
            } else showLightMapDialog();
        }
        @Override
        public boolean onLightmapLongClick(CardAdapter adapter, int position) {
            onLightmapClick(adapter, position);
            return true;
        }

        @Override
        public void onNextClick(CardAdapter adapter, int position)
        {
            int nextPosition = (position + 1);
            if (nextPosition < card_adapter.getItemCount()) {
                setUserSwappedCard(true, "onNextClick");
                card_scroller.setTargetPosition(nextPosition);
                card_layout.startSmoothScroll(card_scroller);
            }
        }
        @Override
        public void onPrevClick(CardAdapter adapter, int position)
        {
            int prevPosition = (position - 1);
            if (prevPosition >= 0) {
                setUserSwappedCard(true, "onPrevClick");
                card_scroller.setTargetPosition(prevPosition);
                card_layout.startSmoothScroll(card_scroller);
            }
        }
        @Override
        public void onCenterClick(CardAdapter adapter, int position)
        {
            setUserSwappedCard(false, "onCenterClick");
            notes.resetNoteIndex();
            NoteData note = notes.getNote();
            if (note != null) {
                highlightTimeField1(note.noteMode);
            }
        }
    };

    /**
     * viewFlipper "note" onTouchListener; swipe between available notes
     */
    private View.OnTouchListener noteTouchListener = new View.OnTouchListener()
    {
        public int MOVE_SENSITIVITY = 25;
        public int FLING_SENSITIVITY = 10;
        public float firstTouchX, secondTouchX;

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    firstTouchX = event.getX();
                    break;

                case MotionEvent.ACTION_UP:
                    secondTouchX = event.getX();
                    if ((firstTouchX - secondTouchX) >= FLING_SENSITIVITY)
                    {
                        setUserSwappedCard(false, "noteTouchListener (fling next)");
                        if (isRtl)
                            notes.showPrevNote();
                        else notes.showNextNote();    // swipe right: next

                    } else if ((secondTouchX - firstTouchX) > FLING_SENSITIVITY) {
                        setUserSwappedCard(false, "noteTouchListener (fling prev)");
                        if (isRtl)
                            notes.showNextNote();
                        else notes.showPrevNote();   // swipe left: prev

                    } else {                    // click: user defined
                        String actionID = AppSettings.loadNoteTapActionPref(SuntimesActivity.this);
                        if (WidgetActions.SuntimesAction.ALARM.name().equals(actionID)) {
                            scheduleAlarmFromNote();
                        } else if (WidgetActions.SuntimesAction.NEXT_NOTE.name().equals(actionID)) {
                            setUserSwappedCard(false, "noteTouchListener (next note)");
                            notes.showNextNote();    // call next/prev methods directly; using onTapAction (re)triggers the activity lifecycle (onResume)
                        } else if (WidgetActions.SuntimesAction.PREV_NOTE.name().equals(actionID)) {
                            setUserSwappedCard(false, "noteTouchListener (prev note)");
                            notes.showPrevNote();
                        } else {
                            onTapAction(actionID, "onNoteTouch");
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    final View currentView = note_flipper.getCurrentView();
                    int moveDeltaX = (isRtl ? (int)(firstTouchX - event.getX()) : (int)(event.getX() - firstTouchX));
                    if (Math.abs(moveDeltaX) < MOVE_SENSITIVITY)
                    {
                        currentView.layout(moveDeltaX, currentView.getTop(), currentView.getWidth(), currentView.getBottom());
                    }
                    break;
            }
            return false;
        }
    };

    View.OnClickListener onTimeZoneClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            configTimeZone();
        }
    };

    View.OnClickListener onClockClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            onTapAction(AppSettings.loadClockTapActionPref(SuntimesActivity.this), "onClockClick");
        }
    };

    private void onTapAction( String actionID, String caller )
    {
        if (actionID != null && !actionID.trim().isEmpty() && !actionID.equals(WidgetActions.SuntimesAction.NOTHING.name())) {
            Log.d("onTapAction", caller + ": " + actionID );
            WidgetActions.startIntent(SuntimesActivity.this, 0, actionID, dataset.dataActual, SuntimesActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
    }

    /**
     * Toggle day length visibility.
     * @param value true show daylength ui, false hide daylength ui
     */
    protected void showDayLength( boolean value )
    {
        // TODO
        //layout_daylength.setVisibility( (value ? View.VISIBLE : View.INVISIBLE) );
        //layout_daylength2.setVisibility( (value ? View.VISIBLE : View.INVISIBLE) );
    }

    /**
     * Toggle note flipper visibility.
     * @param value true show note ui, false hide note ui
     */
    protected void showNotes( boolean value )
    {
        note_flipper.setVisibility( (value ? View.VISIBLE : View.INVISIBLE) );
    }

    /**
     * Show the lightmap dialog.
     */
    protected LightMapDialog showLightMapDialog()
    {
        final LightMapDialog lightMapDialog = new LightMapDialog();
        lightMapDialog.themeViews(this, appThemeOverride);
        if (dataset != null) {
            lightMapDialog.setData(SuntimesActivity.this, dataset);
        } else {
            SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(SuntimesActivity.this);
            data.calculateData();
            lightMapDialog.setData(SuntimesActivity.this, data);
        }
        lightMapDialog.setDialogListener(lightMapListener);
        lightMapDialog.show(getSupportFragmentManager(), DIALOGTAG_LIGHTMAP);
        return lightMapDialog;
    }
    private LightMapDialog.LightMapDialogListener lightMapListener = new LightMapDialog.LightMapDialogListener() {
        @Override
        public void onShowMap( long suggested) {
            showMapPositionAt(suggested);
        }
        @Override
        public void onShowDate(long suggested) {
            scrollToDate(suggested);
        }
    };
    public void showSunPositionAt(@Nullable Long dateTime)
    {
        FragmentManager fragments = getSupportFragmentManager();
        LightMapDialog dialog = (LightMapDialog) fragments.findFragmentByTag(DIALOGTAG_LIGHTMAP);
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = showLightMapDialog();
        dialog.showPositionAt(dateTime);
    }

    /**
     * Show the world map dialog.
     */
    protected WorldMapDialog showWorldMapDialog()
    {
        WorldMapDialog worldMapDialog = new WorldMapDialog();
        worldMapDialog.themeViews(this, appThemeOverride);
        worldMapDialog.setData(dataset);
        worldMapDialog.setDialogListener(worldMapListener);
        worldMapDialog.show(getSupportFragmentManager(), DIALOGTAG_WORLDMAP);
        return worldMapDialog;
    }
    private WorldMapDialog.WorldMapDialogListener worldMapListener = new WorldMapDialog.WorldMapDialogListener()
    {
        @Override
        public void onShowPosition(long suggested) {
            showSunPositionAt(suggested - (60 * 1000));
        }

        @Override
        public void onShowDate(long suggested)
        {
            /**WidgetSettings.DateInfo dateInfo = new WidgetSettings.DateInfo(suggested);
            WidgetSettings.saveDateModePref(SuntimesActivity.this, 0,
                    WidgetSettings.DateInfo.isToday(dateInfo) ? WidgetSettings.DateMode.CURRENT_DATE
                                                              : WidgetSettings.DateMode.CUSTOM_DATE );
            WidgetSettings.saveDatePref(SuntimesActivity.this, 0, dateInfo);
            afterConfigDate();*/

            scrollToDate(suggested);
        }
    };
    public void showMapPositionAt(@Nullable Long dateTime)
    {
        FragmentManager fragments = getSupportFragmentManager();
        WorldMapDialog dialog = (WorldMapDialog) fragments.findFragmentByTag(DIALOGTAG_WORLDMAP);
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = showWorldMapDialog();
        dialog.showPositionAt(dateTime);
    }

    /**
     * Show the solstice and equinox view/dialog.
     */
    protected void showEquinoxView( boolean value )
    {
        equinoxLayout.setVisibility((value ? View.VISIBLE : View.GONE ));
    }

    protected void showEquinoxDialog()
    {
        final EquinoxDialog equinoxDialog = new EquinoxDialog();
        updateEquinoxDialogColumnWidth(equinoxDialog);
        equinoxDialog.themeViews(this, appThemeOverride);
        equinoxDialog.setDialogListener(equinoxDialogListener);
        equinoxDialog.show(getSupportFragmentManager(), DIALOGTAG_EQUINOX);
    }
    protected void dismissEquinoxDialog()
    {
        FragmentManager fragments = getSupportFragmentManager();
        EquinoxDialog equinoxDialog = (EquinoxDialog) fragments.findFragmentByTag(DIALOGTAG_EQUINOX);
        if (equinoxDialog != null) {
            equinoxDialog.dismiss();
        }
    }
    private EquinoxDialog.EquinoxDialogListener equinoxDialogListener = new EquinoxDialog.EquinoxDialogListener()
    {
        @Override
        public void onOptionsModified() {
            updateViews(SuntimesActivity.this);
            if (AppSettings.loadShowEquinoxPref(SuntimesActivity.this)) {
                dismissEquinoxDialog();   // dismiss the dialog if also showing the view (so any changed options are immediately visible)
            }
        }
        @Override
        public void onSetAlarm( WidgetSettings.SolsticeEquinoxMode suggestedEvent ) {
            scheduleAlarm(SolarEvents.valueOf(suggestedEvent));
        }
        @Override
        public void onShowMap( long suggestDate ) {
            showMapPositionAt(suggestDate);
        }
        @Override
        public void onShowPosition( long suggested ) {
            showSunPositionAt(suggested);
        }
        @Override
        public void onShowDate(long suggested) {
            scrollToDate(suggested);
        }
    };

    /**
     * Show the moon dialog.
     */
    protected void showMoonDialog()
    {
        MoonDialog moonDialog = new MoonDialog();
        moonDialog.themeViews(this, appThemeOverride);
        moonDialog.setData((dataset_moon != null) ? dataset_moon : new SuntimesMoonData(SuntimesActivity.this, 0, "moon"));
        moonDialog.setDialogListener(moonDialogListener);
        moonDialog.show(getSupportFragmentManager(), DIALOGTAG_MOON);
    }
    private MoonDialog.MoonDialogListener moonDialogListener = new MoonDialog.MoonDialogListener()
    {
        @Override
        public void onSetAlarm( SolarEvents suggestedEvent ) {
            scheduleAlarm(suggestedEvent);
        }
        @Override
        public void onShowMap( long suggestDate ) {
            showMapPositionAt(suggestDate);
        }
        @Override
        public void onShowPosition( long suggested ) {
            showSunPositionAt(suggested);
        }
        @Override
        public void onShowDate(long suggested) {
            scrollToDate(suggested);
        }
    };

    /**
     * Show data source labels / ui.
     */
    protected void showDatasourceUI( boolean value )
    {
        if (layout_datasource != null)
        {
            layout_datasource.setVisibility((value ? View.VISIBLE : View.GONE));
        }
    }

    public void highlightTimeField1(SolarEvents event)
    {
        int cardPosition = card_adapter.highlightField(this, event);
        if (!checkUserSwappedCard() && cardPosition != -1) {
            scrollTo(cardPosition);
        }
    }

    public static final int HIGHLIGHT_SCROLLING_ITEMS = 4;   // scroll over at most 4 items (otherwise jump straight to target)
    private void scrollTo( int position )
    {
        int firstPosition = card_layout.findFirstVisibleItemPosition();
        if (Math.abs(firstPosition - position) > HIGHLIGHT_SCROLLING_ITEMS) {
            card_view.scrollToPosition(position);        // far; jump straight to item
        } else {
            card_scroller.setTargetPosition(position);   // near; animated scroll to item
            card_layout.startSmoothScroll(card_scroller);
        }
    }
    protected void scrollToDate(long suggested)
    {
        int position = card_adapter.findPositionForDate(SuntimesActivity.this, suggested);
        if (position >= 0 && position < CardAdapter.MAX_POSITIONS) {
            setUserSwappedCard(true, "scrollToDate");
            card_view.scrollToPosition(position);
        }
    }
    private RecyclerView.OnScrollListener onCardScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState ==  RecyclerView.SCROLL_STATE_DRAGGING) {
                setUserSwappedCard(true, "onScrollStateChanged");
            }
        }
    };

    private void adjustNoteIconSize(NoteData note, ImageView icon)
    {
        Resources resources = getResources();
        int iconWidth = (int)resources.getDimension(R.dimen.sunIconLarge_width);
        int iconHeight = ((note.noteIconResource == resID_noonIcon) ? iconWidth : (int)resources.getDimension(R.dimen.sunIconLarge_height));

        ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
        iconParams.width = iconWidth;
        iconParams.height = iconHeight;
    }

    protected void updateNoteUI( NoteData note, int transition )
    {
        if (note_flipper.getDisplayedChild() == 0)
        {
            // currently using view1, ready view2
            ic_time2_note.setBackgroundResource(note.noteIconResource);
            if (appThemeOverride != null) {
                SuntimesUtils.tintDrawable(ic_time2_note.getBackground(), note.noteColor, note.noteColor2, note.noteIconStroke);
            }
            adjustNoteIconSize(note, ic_time2_note);
            ic_time2_note.setVisibility(View.VISIBLE);
            txt_time2_note1.setText(note.timeText.toString());
            txt_time2_note2.setText(note.prefixText);
            txt_time2_note2.setVisibility(note.prefixText.isEmpty() ? View.GONE : View.VISIBLE);
            txt_time2_note3.setText(note.noteText);
            txt_time2_note3.setTextColor(note.noteColor);

        } else {
            // currently using view2, ready view1
            ic_time1_note.setBackgroundResource(note.noteIconResource);
            if (appThemeOverride != null) {
                SuntimesUtils.tintDrawable(ic_time1_note.getBackground(), note.noteColor, note.noteColor2, note.noteIconStroke);
            }
            adjustNoteIconSize(note, ic_time1_note);
            ic_time1_note.setVisibility(View.VISIBLE);
            txt_time1_note1.setText(note.timeText.toString());
            txt_time1_note2.setText(note.prefixText);
            txt_time1_note2.setVisibility(note.prefixText.isEmpty() ? View.GONE : View.VISIBLE);
            txt_time1_note3.setText(note.noteText);
            txt_time1_note3.setTextColor(note.noteColor);
        }

        if (transition == NoteChangedListener.TRANSITION_NEXT)
        {
            note_flipper.setInAnimation(anim_note_inNext);
            note_flipper.setOutAnimation(anim_note_outNext);
            note_flipper.showNext();

        } else {
            note_flipper.setInAnimation(anim_note_inPrev);
            note_flipper.setOutAnimation(anim_note_outPrev);
            note_flipper.showPrevious();
        }

        if (verboseAccessibility)
        {
            String announcement = note.timeText.toString() + " " + note.prefixText + " " + note.noteText;
            SuntimesUtils.announceForAccessibility(note_flipper, announcement);
        }

        highlightTimeField1(note.noteMode);
    }

    /**
     * Save the state of warning objects to Bundle.
     * @param outState a Bundle to save state to
     */
    private void saveWarnings( Bundle outState )
    {
        for (SuntimesWarning warning : warnings)
        {
            warning.save(outState);
        }
    }

    /**
     * Restore the state of warning objects from Bundle.
     * @param savedState a Bundle containing saved state
     */
    private void restoreWarnings(Bundle savedState)
    {
        for (SuntimesWarning warning : warnings)
        {
            warning.restore(savedState);
            warning.setWarningListener(warningListener);
        }
    }

    private void setUserSwappedCard( boolean value, String tag )
    {
        userSwappedCard = (value ? SystemClock.elapsedRealtime() : -1L);
        //Log.d("DEBUG", "userSwappedCard set " + value + " (" + tag + " )");
    }

    private boolean checkUserSwappedCard()
    {
        long d = (userSwappedCard < 0) ? 0 : SystemClock.elapsedRealtime() - userSwappedCard;
        if (d < 0 || d >= RESET_USERSWAPPEDCARD_AFTER_T) {
            userSwappedCard = -1L;
            //Log.d("DEBUG", "userSwappedCard set false (checkUserSwappedCard)");
        }
        //Log.d("DEBUG", "userSwappedCard check " + (userSwappedCard >= 0));
        return (userSwappedCard >= 0);
    }
    private static final long RESET_USERSWAPPEDCARD_AFTER_T = 60 * 60 * 1000;  // 1h

    /**
     * Get the current theme's resource id (used by test verification).
     * @return the resource id of the current theme/style (or 0 if getTHemeResId failed)
     */
    public int getThemeId()
    {
        try {
            //noinspection JavaReflectionMemberAccess
            Method method = Context.class.getMethod("getThemeResId");
            method.setAccessible(true);
            return (Integer) method.invoke(this);

        } catch (Exception e) {
            Log.e("getThemeId", "Failed to get theme ID");
            e.printStackTrace();
        }
        return 0;
    }

}
