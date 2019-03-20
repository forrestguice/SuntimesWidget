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
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.getfix.GetFixHelper;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.map.WorldMapDialog;
import com.forrestguice.suntimeswidget.notes.NoteChangedListener;
import com.forrestguice.suntimeswidget.notes.NoteData;
import com.forrestguice.suntimeswidget.notes.SuntimesNotes;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.lang.reflect.Method;
import java.text.DateFormat;

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
    public static final String SUNTIMES_APP_UPDATE_FULL = "suntimes.SUNTIMES_APP_UPDATE_FULL";
    public static final String SUNTIMES_APP_UPDATE_PARTIAL = "suntimes.SUNTIMES_APP_UPDATE_PARTIAL";
    public static final int SUNTIMES_SETTINGS_REQUEST = 10;

    public static final String ACTION_VIEW_SUN = "com.forrestguice.suntimeswidget.VIEW_SUN";
    public static final String ACTION_VIEW_MOON = "com.forrestguice.suntimeswidget.VIEW_MOON";
    public static final String ACTION_VIEW_SOLSTICE = "com.forrestguice.suntimeswidget.VIEW_SOLSTICE";

    public static final String KEY_UI_CARDISTOMORROW = "cardIsTomorrow";
    public static final String KEY_UI_USERSWAPPEDCARD = "userSwappedCard";

    public static final String WARNINGID_DATE = "Date";
    public static final String WARNINGID_TIMEZONE = "Timezone";

    private static final String DIALOGTAG_TIMEZONE = "timezone";
    private static final String DIALOGTAG_ALARM = "alarm";
    private static final String DIALOGTAG_ABOUT = "about";
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
    protected SuntimesEquinoxSolsticeDataset dataset2;
    protected SuntimesMoonData dataset3;

    private int color_textTimeDelta;
    private int resID_noonIcon;
    private int resID_buttonPressColor, resID_buttonDisabledColor;

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
    private ViewFlipper card_flipper;
    private Animation anim_card_inPrev, anim_card_inNext;
    private Animation anim_card_outPrev, anim_card_outNext;

    private ImageButton btn_flipperNext_today;
    private ImageButton btn_flipperPrev_today;
    private ImageButton btn_flipperNext_tomorrow;
    private ImageButton btn_flipperPrev_tomorrow;

    private View sunriseHeader, sunriseHeader2;
    private TextView header_sunrise,        header_sunrise2;
    private ImageView icon_sunrise,         icon_sunrise2;

    private View sunsetHeader, sunsetHeader2;
    private TextView header_sunset,         header_sunset2;
    private ImageView icon_sunset,          icon_sunset2;

    private TextView txt_date,              txt_date2;

    private TimeFieldRow row_astro,         row_astro2;
    private TimeFieldRow row_nautical,      row_nautical2;
    private TimeFieldRow row_civil,         row_civil2;
    private TimeFieldRow row_actual,        row_actual2;
    private TimeFieldRow row_solarnoon,     row_solarnoon2;

    private TimeFieldRow row_gold,          row_gold2;
    private TimeFieldRow row_blue8,         row_blue8_2;
    private TimeFieldRow row_blue4,         row_blue4_2;

    private LinearLayout layout_daylength,  layout_daylength2;
    private TextView txt_daylength,         txt_daylength2;
    private TextView txt_lightlength,       txt_lightlength2;

    private TextView moonlabel,             moonlabel2;
    private MoonPhaseView moonphase,        moonphase2;
    private MoonRiseSetView moonrise,       moonrise2;
    private View moonClickArea,             moonClickArea2;

    private EquinoxView card_equinoxSolstice;
    private View equinoxLayout;

    private LightMapView lightmap;
    private View lightmapLayout;

    private TextView txt_datasource;
    private View layout_datasource;
    private AppCompatCheckBox check_altitude;
    private TextView txt_altitude;
    private View layout_altitude;

    private boolean isRtl = false;
    private boolean userSwappedCard = false;
    private HashMap<SolarEvents.SolarEventField, TextView> timeFields;
    private ArrayList<TimeFieldRow> rows = new ArrayList<>();

    private boolean showWarnings = false;
    private SuntimesWarning timezoneWarning;
    private SuntimesWarning dateWarning;
    private List<SuntimesWarning> warnings;

    private boolean showSeconds = WidgetSettings.PREF_DEF_GENERAL_SHOWSECONDS;
    //private boolean showGold = AppSettings.PREF_DEF_UI_SHOWGOLDHOUR;
    //private boolean showBlue = AppSettings.PREF_DEF_UI_SHOWBLUEHOUR;
    private boolean showMoon = AppSettings.PREF_DEF_UI_SHOWMOON;
    private boolean verboseAccessibility = AppSettings.PREF_DEF_ACCESSIBILITY_VERBOSE;

    public SuntimesActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
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
        onStart_resetNoteIndex = true;

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent)
    {
        String action = intent.getAction();
        intent.setAction(null);

        Uri data = intent.getData();
        intent.setData(null);

        Log.d("handleIntent", "action: " + action + ", data: " + data);
        if (action != null)
        {
            if (action.equals(ACTION_VIEW_SUN)) {
                showLightMapDialog();

            } else if (action.equals(ACTION_VIEW_MOON)) {
                showMoonDialog();

            } else if (action.equals(ACTION_VIEW_SOLSTICE)) {
                showEquinoxDialog();

            } else {
                if (data != null) {
                    configLocation(data);
                }
            }
        }
    }

    private void initTheme()
    {
        appTheme = AppSettings.loadThemePref(this);
        setTheme(appThemeResID = AppSettings.themePrefToStyleId(this, appTheme, null));

        String themeName = AppSettings.getThemeOverride(this, appThemeResID);
        if (themeName != null)
        {
            Log.i("initTheme", "Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }

        int[] attrs = new int[] { R.attr.sunnoonIcon, R.attr.buttonPressColor, R.attr.text_disabledColor };
        TypedArray a = obtainStyledAttributes(attrs);
        resID_noonIcon = a.getResourceId(0, R.drawable.ic_noon_large);
        resID_buttonPressColor = a.getResourceId(1, R.color.btn_tint_pressed_dark);
        resID_buttonDisabledColor = a.getResourceId(2, R.color.text_disabled_dark);
        a.recycle();

        GetFixUI.themeIcons(this);
    }

    private void initLocale( Context context )
    {
        WidgetSettings.initDefaults(context);        // locale specific defaults

        SuntimesUtils.initDisplayStrings(context);   // locale specific strings
        AppSettings.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);

        initAnimations(context);                     // locale specific animations
        initColors(context);                         // locale specific colors
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        calculateData(SuntimesActivity.this);

        registerReceivers(SuntimesActivity.this);
        setUpdateAlarms(SuntimesActivity.this);

        if (onStart_resetNoteIndex)
        {
            notes.resetNoteIndex();
            onStart_resetNoteIndex = false;
        }

        updateViews(SuntimesActivity.this);
    }
    private boolean onStart_resetNoteIndex = false;

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    @Override
    public void onResume()
    {
        super.onResume();
        updateActionBar(this);
        getFixHelper.onResume();

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
            dateDialog.setOnAcceptedListener(onConfigDate);
            dateDialog.setOnCanceledListener(onCancelDate);
            //Log.d("DEBUG", "TimeDateDialog listeners restored.");
        }
    }

    private void updateDialogs(Context context)
    {
        FragmentManager fragments = getSupportFragmentManager();

        AlarmDialog alarmDialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_ALARM);
        if (alarmDialog != null)
        {
            alarmDialog.setData(this, dataset, dataset3);
            alarmDialog.setOnAcceptedListener(alarmDialog.scheduleAlarmClickListener);
            //Log.d("DEBUG", "AlarmDialog listeners restored.");
        }

        LightMapDialog lightMapDialog = (LightMapDialog) fragments.findFragmentByTag(DIALOGTAG_LIGHTMAP);
        if (lightMapDialog != null)
        {
            lightMapDialog.themeViews(this, appThemeOverride);
            lightMapDialog.setData(dataset);
            lightMapDialog.updateViews();
            //Log.d("DEBUG", "LightMapDialog updated on restore.");
        }

        WorldMapDialog worldMapDialog = (WorldMapDialog) fragments.findFragmentByTag(DIALOGTAG_WORLDMAP);
        if (worldMapDialog != null)
        {
            worldMapDialog.themeViews(this, appThemeOverride);
            worldMapDialog.setData(dataset);
            worldMapDialog.updateViews();
            //Log.d("DEBUG", "WorldMapDialog updated on restore.");
        }

        EquinoxDialog equinoxDialog = (EquinoxDialog) fragments.findFragmentByTag(DIALOGTAG_EQUINOX);
        if (equinoxDialog != null)
        {
            equinoxDialog.themeViews(this, appThemeOverride);
            equinoxDialog.setData((dataset2 != null) ? dataset2 : new SuntimesEquinoxSolsticeDataset(SuntimesActivity.this));
            equinoxDialog.updateViews();
            //Log.d("DEBUG", "EquinoxDialog updated on restore.");
        }

        MoonDialog moonDialog = (MoonDialog) fragments.findFragmentByTag(DIALOGTAG_MOON);
        if (moonDialog != null)
        {
            moonDialog.themeViews(this, appThemeOverride);
            moonDialog.setData((dataset3 != null) ? dataset3 : new SuntimesMoonData(SuntimesActivity.this, 0, "moon"));
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
    private BroadcastReceiver fullUpdateReceiver = new BroadcastReceiver()
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
    private BroadcastReceiver partialUpdateReceiver = new BroadcastReceiver()
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
                if (!userSwappedCard) {
                    notes.resetNoteIndex();
                }
                updateViews(SuntimesActivity.this);
            }
        }
    };

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveWarnings(outState);
        outState.putBoolean(KEY_UI_USERSWAPPEDCARD, userSwappedCard);
        outState.putBoolean(KEY_UI_CARDISTOMORROW, (card_flipper.getDisplayedChild() != 0));
        card_equinoxSolstice.saveState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        restoreWarnings(savedInstanceState);
        setUserSwappedCard(savedInstanceState.getBoolean(KEY_UI_USERSWAPPEDCARD, false), "onRestoreInstanceState");
        boolean cardIsTomorrow = savedInstanceState.getBoolean(KEY_UI_CARDISTOMORROW, false);
        card_flipper.setDisplayedChild((cardIsTomorrow ? 1 : 0));
        card_equinoxSolstice.loadState(savedInstanceState);
    }

    /**
     * OnStop: the Activity no longer visible
     */
    @Override
    public void onStop()
    {
        unregisterReceivers(SuntimesActivity.this);
        unsetUpdateAlarms(SuntimesActivity.this);

        stopTimeTask();
        getFixHelper.cancelGetFix();
        super.onStop();
    }

    /**
     * OnDestroy: the activity destroyed
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

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

            Toolbar actionBar = (Toolbar) findViewById(R.id.app_menubar);
            actionBar.setTitleTextColor(titleColor);
            actionBar.setSubtitleTextColor(textColor);

            txt_time.setTextColor(timeColor);
            txt_time_suffix.setTextColor(timeColor);
            txt_timezone.setTextColor(SuntimesUtils.colorStateList(textColor, disabledColor, pressedColor));

            txt_time1_note1.setTextColor(timeColor);
            txt_time1_note2.setTextColor(textColor);
            txt_time2_note1.setTextColor(timeColor);
            txt_time2_note2.setTextColor(textColor);

            txt_datasource.setTextColor(SuntimesUtils.colorStateList(textColor, disabledColor, pressedColor));
            txt_altitude.setTextColor(timeColor);

            themeCardViews(context, appThemeOverride);
            card_equinoxSolstice.themeViews(context, appThemeOverride);
            lightmap.themeViews(context, appThemeOverride);
        }
    }

    protected void themeCardViews(Context context, @NonNull SuntimesTheme theme)
    {
        color_textTimeDelta = theme.getTimeColor();

        int textColor = theme.getTextColor();
        txt_daylength.setTextColor(textColor);
        txt_daylength2.setTextColor(textColor);
        txt_lightlength.setTextColor(textColor);
        txt_lightlength2.setTextColor(textColor);

        int sunriseTextColor = theme.getSunriseTextColor();
        int sunsetTextColor = theme.getSunsetTextColor();
        for (SolarEvents.SolarEventField field : timeFields.keySet())
        {
            TextView textView = timeFields.get(field);
            if (textView != null)
            {
                switch (field.event)
                {
                    case MORNING_ASTRONOMICAL: case MORNING_NAUTICAL: case MORNING_CIVIL:
                    case MORNING_BLUE8: case MORNING_BLUE4:
                    case EVENING_GOLDEN:
                    case SUNRISE: textView.setTextColor(sunriseTextColor);
                        break;
                    case EVENING_ASTRONOMICAL: case EVENING_NAUTICAL: case EVENING_CIVIL:
                    case EVENING_BLUE8: case EVENING_BLUE4:
                    case MORNING_GOLDEN: case NOON:
                    case SUNSET: textView.setTextColor(sunsetTextColor);
                        break;
                }
            }
        }

        int labelColor = theme.getTitleColor();
        for (TimeFieldRow row : rows) {
            row.label.setTextColor(labelColor);
        }

        int disabledColor = ContextCompat.getColor(context, resID_buttonDisabledColor);
        int actionColor = theme.getActionColor();
        txt_date.setTextColor(SuntimesUtils.colorStateList(labelColor, disabledColor, actionColor));
        txt_date2.setTextColor(SuntimesUtils.colorStateList(labelColor, disabledColor, actionColor));

        int sunriseIconColor = theme.getSunriseIconColor();
        int sunriseIconColor2 = theme.getSunriseIconStrokeColor();
        int sunriseIconStrokeWidth = theme.getSunriseIconStrokePixels(this);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunrise.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunrise2.getBackground(), sunriseIconColor, sunriseIconColor2, sunriseIconStrokeWidth);
        header_sunrise.setTextColor(sunriseTextColor);
        header_sunrise2.setTextColor(sunriseTextColor);

        int sunsetIconColor = theme.getSunsetIconColor();
        int sunsetIconColor2 = theme.getSunsetIconStrokeColor();
        int sunsetIconStrokeWidth = theme.getSunsetIconStrokePixels(this);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunset.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        SuntimesUtils.tintDrawable((InsetDrawable)icon_sunset2.getBackground(), sunsetIconColor, sunsetIconColor2, sunsetIconStrokeWidth);
        header_sunset.setTextColor(sunsetTextColor);
        header_sunset2.setTextColor(sunsetTextColor);

        moonrise.themeViews(context, theme);
        moonrise2.themeViews(context, theme);

        moonphase.themeViews(context, theme);
        moonphase2.themeViews(context, theme);

        moonlabel.setTextColor(labelColor);
        moonlabel2.setTextColor(labelColor);
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
        initLightMap(context);
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

    /**
     * initialize the actionbar
     */
    private void initActionBar(Context context)
    {
        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
    }

    private void initLightMap(Context context)
    {
        lightmap = (LightMapView) findViewById(R.id.info_time_lightmap);
        lightmapLayout = findViewById(R.id.info_time_lightmap_layout);

        lightmapLayout.setClickable(true);
        lightmapLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showLightMapDialog();
            }
        });
        lightmapLayout.setOnLongClickListener( new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                showLightMapDialog();
                return true;
            }
        });
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
                com.forrestguice.suntimeswidget.calculator.core.Location location = new com.forrestguice.suntimeswidget.calculator.core.Location(getString(R.string.gps_lastfix_title_found), locations[0]);
                actionBar.setSubtitle(location.toString());
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
        timeFields = new HashMap<SolarEvents.SolarEventField, TextView>();
        card_flipper = (ViewFlipper) findViewById(R.id.info_time_flipper);
        if (card_flipper != null)
        {
            card_flipper.setOnTouchListener(timeCardTouchListener);
        } else {
            Log.w("initCardViews", "Failed to set touchListener; card_flipper was null!");
        }

        // Today's times
        View viewToday = findViewById(R.id.info_time_all_today);
        if (viewToday != null)
        {
            txt_date = (TextView) viewToday.findViewById(R.id.text_date);
            txt_date.setOnClickListener(dateTapClickListener(false));
            txt_date.setOnLongClickListener(dateTapLongClickListener(false));

            sunriseHeader = viewToday.findViewById(R.id.header_time_sunrise);
            sunriseHeader.setOnClickListener(onSunriseClick);
            header_sunrise = (TextView) viewToday.findViewById(R.id.label_time_sunrise);
            icon_sunrise = (ImageView) viewToday.findViewById(R.id.icon_time_sunrise);

            sunsetHeader = viewToday.findViewById(R.id.header_time_sunset);
            sunsetHeader.setOnClickListener(onSunsetClick);
            header_sunset = (TextView) viewToday.findViewById(R.id.label_time_sunset);
            icon_sunset = (ImageView) viewToday.findViewById(R.id.icon_time_sunset);

            rows.add(row_actual = new TimeFieldRow(viewToday, R.id.text_time_label_official, R.id.text_time_sunrise_actual, R.id.text_time_sunset_actual));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNRISE, false), row_actual.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNSET, false), row_actual.getField(1));

            rows.add(row_civil = new TimeFieldRow(viewToday, R.id.text_time_label_civil, R.id.text_time_sunrise_civil, R.id.text_time_sunset_civil));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_CIVIL, false), row_civil.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_CIVIL, false), row_civil.getField(1));

            rows.add(row_nautical = new TimeFieldRow(viewToday, R.id.text_time_label_nautical, R.id.text_time_sunrise_nautical, R.id.text_time_sunset_nautical));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_NAUTICAL, false), row_nautical.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_NAUTICAL, false), row_nautical.getField(1));

            rows.add(row_astro = new TimeFieldRow(viewToday, R.id.text_time_label_astro, R.id.text_time_sunrise_astro, R.id.text_time_sunset_astro));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_ASTRONOMICAL, false), row_astro.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_ASTRONOMICAL, false), row_astro.getField(1));

            rows.add(row_solarnoon = new TimeFieldRow(viewToday, R.id.text_time_label_noon, R.id.text_time_noon));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.NOON, false), row_solarnoon.getField(0));

            rows.add(row_gold = new TimeFieldRow(viewToday, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_GOLDEN, false), row_gold.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_GOLDEN, false), row_gold.getField(1));

            rows.add(row_blue8 = new TimeFieldRow(viewToday, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE8, false), row_blue8.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE8, false), row_blue8.getField(1));

            rows.add(row_blue4 = new TimeFieldRow(viewToday, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE4, false), row_blue4.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE4, false), row_blue4.getField(1));

            layout_daylength = (LinearLayout) viewToday.findViewById(R.id.layout_daylength);
            txt_daylength = (TextView) viewToday.findViewById(R.id.text_daylength);
            txt_lightlength = (TextView) viewToday.findViewById(R.id.text_lightlength);

            moonlabel = (TextView) viewToday.findViewById(R.id.text_time_label_moon);
            moonphase = (MoonPhaseView) viewToday.findViewById(R.id.moonphase_view);

            moonrise = (MoonRiseSetView) viewToday.findViewById(R.id.moonriseset_view);
            moonrise.setShowExtraField(false);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MOONRISE, false), null);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MOONSET, false), null);

            moonClickArea = viewToday.findViewById(R.id.moonphase_clickArea);
            moonClickArea.setOnClickListener(onMoonriseClick);
            moonClickArea.setOnLongClickListener(onMoonriseLongClick);

            btn_flipperNext_today = (ImageButton)viewToday.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_today.setOnClickListener(onNextCardClick);
            btn_flipperNext_today.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_flipperNext_today.setColorFilter((appThemeOverride != null
                                ? appThemeOverride.getActionColor()
                                : ContextCompat.getColor(SuntimesActivity.this, resID_buttonPressColor)));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        btn_flipperNext_today.setColorFilter(null);
                    }
                    return false;
                }
            });

            btn_flipperPrev_today = (ImageButton)viewToday.findViewById(R.id.info_time_prevbtn);
            btn_flipperPrev_today.setOnClickListener(onPrevCardClick);
            btn_flipperPrev_today.setVisibility(View.GONE);

        } else {
            Log.w("initCardViews", "Failed to init card layout1; was null!");
        }

        // Tomorrow's times
        View viewTomorrow = findViewById(R.id.info_time_all_tomorrow);
        if (viewTomorrow != null)
        {
            txt_date2 = (TextView) viewTomorrow.findViewById(R.id.text_date);
            txt_date2.setOnClickListener(dateTapClickListener(true));
            txt_date2.setOnLongClickListener(dateTapLongClickListener(true));

            sunriseHeader2 = viewTomorrow.findViewById(R.id.header_time_sunrise);
            sunriseHeader2.setOnClickListener(onSunriseClick);
            header_sunrise2 = (TextView) viewTomorrow.findViewById(R.id.label_time_sunrise);
            icon_sunrise2 = (ImageView) viewTomorrow.findViewById(R.id.icon_time_sunrise);

            sunsetHeader2 = viewTomorrow.findViewById(R.id.header_time_sunset);
            sunsetHeader2.setOnClickListener(onSunsetClick);
            header_sunset2 = (TextView) viewTomorrow.findViewById(R.id.label_time_sunset);
            icon_sunset2 = (ImageView) viewTomorrow.findViewById(R.id.icon_time_sunset);

            rows.add(row_actual2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_official, R.id.text_time_sunrise_actual, R.id.text_time_sunset_actual));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNRISE, true), row_actual2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNSET, true), row_actual2.getField(1));

            rows.add(row_civil2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_civil, R.id.text_time_sunrise_civil, R.id.text_time_sunset_civil));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_CIVIL, true), row_civil2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_CIVIL, true), row_civil2.getField(1));

            rows.add(row_nautical2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_nautical, R.id.text_time_sunrise_nautical, R.id.text_time_sunset_nautical));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_NAUTICAL, true), row_nautical2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_NAUTICAL, true), row_nautical2.getField(1));

            rows.add(row_astro2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_astro, R.id.text_time_sunrise_astro, R.id.text_time_sunset_astro));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_ASTRONOMICAL, true), row_astro2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_ASTRONOMICAL, true), row_astro2.getField(1));

            rows.add(row_solarnoon2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_noon, R.id.text_time_noon));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.NOON, true), row_solarnoon2.getField(0));

            rows.add(row_gold2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_GOLDEN, true), row_gold2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_GOLDEN, true), row_gold2.getField(1));

            rows.add(row_blue8_2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE8, true), row_blue8_2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE8, true), row_blue8_2.getField(1));

            rows.add(row_blue4_2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE4, true), row_blue4_2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE4, true), row_blue4_2.getField(1));

            layout_daylength2 = (LinearLayout) viewTomorrow.findViewById(R.id.layout_daylength);
            txt_daylength2 = (TextView) viewTomorrow.findViewById(R.id.text_daylength);
            txt_lightlength2 = (TextView) viewTomorrow.findViewById(R.id.text_lightlength);

            moonlabel2 = (TextView) viewTomorrow.findViewById(R.id.text_time_label_moon);

            moonphase2 = (MoonPhaseView) viewTomorrow.findViewById(R.id.moonphase_view);
            moonphase2.setTomorrowMode(true);

            moonrise2 = (MoonRiseSetView) viewTomorrow.findViewById(R.id.moonriseset_view);
            moonrise2.setShowExtraField(false);
            moonrise2.setTomorrowMode(true);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MOONRISE, true), null);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MOONSET, true), null);

            moonClickArea2 = viewTomorrow.findViewById(R.id.moonphase_clickArea);
            moonClickArea2.setOnClickListener(onMoonriseClick);
            moonClickArea2.setOnLongClickListener(onMoonriseLongClick);

            btn_flipperNext_tomorrow = (ImageButton)viewTomorrow.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_tomorrow.setOnClickListener(onNextCardClick);
            btn_flipperNext_tomorrow.setVisibility(View.GONE);

            btn_flipperPrev_tomorrow = (ImageButton)viewTomorrow.findViewById(R.id.info_time_prevbtn);
            btn_flipperPrev_tomorrow.setOnClickListener(onPrevCardClick);
            btn_flipperPrev_tomorrow.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        btn_flipperPrev_tomorrow.setColorFilter((appThemeOverride != null
                                ? appThemeOverride.getActionColor()
                                : ContextCompat.getColor(SuntimesActivity.this, resID_buttonPressColor)));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    {
                        btn_flipperPrev_tomorrow.setColorFilter(null);
                    }
                    return false;
                }
            });

            //initTimeFields();

        } else {
            Log.w("initCardViews", "Failed to init card layout2; was null!");
        }
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
     * initialize view animations
     * @param context a context used to access resources
     */
    private void initAnimations(Context context)
    {
        anim_note_inPrev = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_note_inNext = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim_note_outPrev = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        anim_note_outNext = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        //anim_note_outPrev = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        //anim_note_outNext = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        anim_card_inPrev = AnimationUtils.loadAnimation(this, (isRtl ? R.anim.slide_in_right : R.anim.slide_in_left));
        anim_card_inNext = AnimationUtils.loadAnimation(this,(isRtl ? R.anim.slide_in_left : R.anim.slide_in_right));

        anim_card_outPrev = AnimationUtils.loadAnimation(this, (isRtl ? R.anim.slide_out_left : R.anim.slide_out_right));
        anim_card_outNext = AnimationUtils.loadAnimation(this, (isRtl ? R.anim.slide_out_right : R.anim.slide_out_left));
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
        notes.init(this, dataset, dataset3);
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
        datePicker.setOnAcceptedListener(onConfigDate);
        datePicker.setOnCanceledListener(onCancelDate);
        datePicker.show(getSupportFragmentManager(), DIALOGTAG_DATE);
    }
    DialogInterface.OnClickListener onConfigDate = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            dateWarning.reset();
            calculateData(SuntimesActivity.this);
            setUpdateAlarms(SuntimesActivity.this);
            updateViews(SuntimesActivity.this);
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
                calculateData(SuntimesActivity.this);
                setUpdateAlarms(SuntimesActivity.this);
                updateActionBar(SuntimesActivity.this);
                updateViews(SuntimesActivity.this);

                WidgetSettings.LocationMode locationMode = dialog.getDialogContent().getLocationMode();
                if (locationMode == WidgetSettings.LocationMode.CURRENT_LOCATION)
                {
                    getFixHelper.getFix();
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
        //if (mapIntent.resolveActivity(getPackageManager()) != null)
        //{
        //    startActivity(mapIntent);
        //}

        String myPackage = "com.forrestguice.suntimeswidget";
        List<ResolveInfo> info = getPackageManager().queryIntentActivities(mapIntent, 0);
        List<Intent> geoIntents = new ArrayList<Intent>();

        if (!info.isEmpty())
        {
            for (ResolveInfo resolveInfo : info)
            {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!TextUtils.equals(packageName, myPackage))
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
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, geoIntents.toArray(new Parcelable[geoIntents.size()]));
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
        String timeModes = getString(R.string.help_general_timeMode);
        String dayLength = getString(R.string.help_general_daylength);
        String timeText = getString(R.string.help_general2, timeModes, dayLength);

        String goldHour = getString(R.string.help_general_goldhour);
        String blueHour = getString(R.string.help_general_bluehour);
        String blueGoldText = getString(R.string.help_general2, blueHour, goldHour);

        String moonIllum = getString(R.string.help_general_moonillum);

        String helpText = getString(R.string.help_general3, timeText, blueGoldText, moonIllum);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpText);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * Show the about dialog.
     */
    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }



    /**
     * Show application settings.
     */
    protected void showSettings()
    {
        Intent settingsIntent = new Intent(this, SuntimesSettingsActivity.class);
        startActivityForResult(settingsIntent, SUNTIMES_SETTINGS_REQUEST);
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
    }

    /**
     * Show the alarm dialog.
     */
    protected void scheduleAlarm()
    {
        scheduleAlarm(null);
    }
    protected void scheduleAlarm( SolarEvents selected )
    {
        if (dataset.isCalculated())
        {
            AlarmDialog alarmDialog = new AlarmDialog();
            alarmDialog.setData(this, dataset, dataset3);
            alarmDialog.setChoice(selected);
            alarmDialog.setOnAcceptedListener(alarmDialog.scheduleAlarmClickListener);
            alarmDialog.show(getSupportFragmentManager(), DIALOGTAG_ALARM);

        } else {
            String msg = getString(R.string.schedalarm_dialog_error2);
            Toast errorMsg = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            errorMsg.show();
        }
    }

    protected void scheduleAlarmFromNote()
    {
        scheduleAlarm(notes.getNote().noteMode);
    }

    /**
     *
     * @param context a context used to access shared prefs
     */
    private void initData( Context context )
    {
        dataset = new SuntimesRiseSetDataset(context);
        dataset2 = (AppSettings.loadShowEquinoxPref(context) ? new SuntimesEquinoxSolsticeDataset(context) : null);
        dataset3 = (AppSettings.loadShowMoonPref(context) ? new SuntimesMoonData(context, 0, "moon") : null);
    }

    protected void calculateData( Context context )
    {
        initData(context);

        if (dataset != null)
            dataset.calculateData();

        if (dataset2 != null)
            dataset2.calculateData();

        if (dataset3 != null)
            dataset3.calculate();

        initNotes();
    }

    protected void invalidateData( Context context )
    {
        if (dataset != null)
            dataset.invalidateCalculation();

        if (dataset2 != null)
            dataset2.invalidateCalculation();

        if (dataset3 != null)
            dataset3.invalidateCalculation();

        updateViews(context);
    }

    protected void updateViews( Context context )
    {
        stopTimeTask();

        verboseAccessibility = AppSettings.loadVerboseAccessibilityPref(this);
        showWarnings = AppSettings.loadShowWarningsPref(this);
        dateWarning.shouldShow = false;
        timezoneWarning.shouldShow = false;

        location = WidgetSettings.loadLocationPref(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        String locationTitle = location.getLabel();

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

        boolean[] showFields = AppSettings.loadShowFieldsPref(context);
        boolean showActual = showFields[AppSettings.FIELD_ACTUAL];
        boolean showCivil = showFields[AppSettings.FIELD_CIVIL];
        boolean showNautical = showFields[AppSettings.FIELD_NAUTICAL];
        boolean showAstro = showFields[AppSettings.FIELD_ASTRO];
        boolean showNoon = showFields[AppSettings.FIELD_NOON];
        boolean showGold = showFields[AppSettings.FIELD_GOLD];
        boolean showBlue = showFields[AppSettings.FIELD_BLUE];

        row_actual.setVisible(showActual);
        row_actual2.setVisible(showActual);
        row_civil.setVisible(showCivil);
        row_civil2.setVisible(showCivil);
        row_nautical.setVisible(showNautical);
        row_nautical2.setVisible(showNautical);
        row_astro.setVisible(showAstro);
        row_astro2.setVisible(showAstro);
        row_solarnoon.setVisible(showNoon);
        row_solarnoon2.setVisible(showNoon);

        boolean supportsGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
        showGold = showGold && supportsGoldBlue;
        showBlue = showBlue && supportsGoldBlue;
        row_blue8.setVisible(showBlue);
        row_blue8_2.setVisible(showBlue);
        row_blue4.setVisible(showBlue);
        row_blue4_2.setVisible(showBlue);
        row_gold.setVisible(showGold);
        row_gold2.setVisible(showGold);

        boolean supportsMoon = (dataset3 != null);
        showMoon = supportsMoon && AppSettings.loadShowMoonPref(context);
        showMoonrise(showMoon);

        showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

        if (dataset.isCalculated())
        {
            if (showActual)
            {
                SuntimesUtils.TimeDisplayText sunriseString_actualTime = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunriseString_actualTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunriseCalendarOther(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_actualTime = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunsetCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_actualTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunsetCalendarOther(), showSeconds);
                row_actual.updateFields(sunriseString_actualTime.toString(), sunsetString_actualTime.toString());
                row_actual2.updateFields(sunriseString_actualTime2.toString(), sunsetString_actualTime2.toString());
            }

            if (showCivil)
            {
                SuntimesUtils.TimeDisplayText sunriseString_civilTime = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunriseString_civilTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunriseCalendarOther(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_civilTime = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunsetCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_civilTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunsetCalendarOther(), showSeconds);
                row_civil.updateFields(sunriseString_civilTime.toString(), sunsetString_civilTime.toString());
                row_civil2.updateFields(sunriseString_civilTime2.toString(), sunsetString_civilTime2.toString());
            }

            if (showNautical)
            {
                SuntimesUtils.TimeDisplayText sunriseString_nauticalTime = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunriseString_nauticalTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunriseCalendarOther(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_nauticalTime = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunsetCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_nauticalTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunsetCalendarOther(), showSeconds);
                row_nautical.updateFields(sunriseString_nauticalTime.toString(), sunsetString_nauticalTime.toString());
                row_nautical2.updateFields(sunriseString_nauticalTime2.toString(), sunsetString_nauticalTime2.toString());
            }

            if (showAstro)
            {
                SuntimesUtils.TimeDisplayText sunriseString_astroTime = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunriseString_astroTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunriseCalendarOther(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_astroTime = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunsetCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText sunsetString_astroTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunsetCalendarOther(), showSeconds);
                row_astro.updateFields(sunriseString_astroTime.toString(), sunsetString_astroTime.toString());
                row_astro2.updateFields(sunriseString_astroTime2.toString(), sunsetString_astroTime2.toString());
            }

            if (showNoon)
            {
                SuntimesUtils.TimeDisplayText noonString = utils.calendarTimeShortDisplayString(context, dataset.dataNoon.sunriseCalendarToday(), showSeconds);
                SuntimesUtils.TimeDisplayText noonString2 = utils.calendarTimeShortDisplayString(context, dataset.dataNoon.sunriseCalendarOther(), showSeconds);
                row_solarnoon.updateFields(noonString.toString());
                row_solarnoon2.updateFields(noonString2.toString());
            }

            if (showBlue)
            {
                String sunriseString_blue8 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue8.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_blue8 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue8.sunsetCalendarToday(), showSeconds).toString();
                row_blue8.updateFields(sunriseString_blue8, sunsetString_blue8);

                String sunriseString_blue8_2 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue8.sunriseCalendarOther(), showSeconds).toString();
                String sunsetString_blue8_2 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue8.sunsetCalendarOther(), showSeconds).toString();
                row_blue8_2.updateFields(sunriseString_blue8_2, sunsetString_blue8_2);

                String sunriseString_blue4 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue4.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_blue4 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue4.sunsetCalendarToday(), showSeconds).toString();
                row_blue4.updateFields(sunriseString_blue4, sunsetString_blue4);

                String sunriseString_blue4_2 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue4.sunriseCalendarOther(), showSeconds).toString();
                String sunsetString_blue4_2 = utils.calendarTimeShortDisplayString(context, dataset.dataBlue4.sunsetCalendarOther(), showSeconds).toString();
                row_blue4_2.updateFields(sunriseString_blue4_2, sunsetString_blue4_2);
            }

            if (showGold)
            {
                String sunriseString_gold = utils.calendarTimeShortDisplayString(context, dataset.dataGold.sunriseCalendarToday(), showSeconds).toString();
                String sunsetString_gold = utils.calendarTimeShortDisplayString(context, dataset.dataGold.sunsetCalendarToday(), showSeconds).toString();
                row_gold.updateFields(sunriseString_gold, sunsetString_gold);

                String sunriseString_gold2 = utils.calendarTimeShortDisplayString(context, dataset.dataGold.sunriseCalendarOther(), showSeconds).toString();
                String sunsetString_gold2 = utils.calendarTimeShortDisplayString(context, dataset.dataGold.sunsetCalendarOther(), showSeconds).toString();
                row_gold2.updateFields(sunriseString_gold2, sunsetString_gold2);
            }

            updateDayLengthViews(txt_daylength, dataset.dataActual.dayLengthToday(), R.string.length_day);
            updateDayLengthViews(txt_lightlength, dataset.dataCivil.dayLengthToday(), R.string.length_light);

            updateDayLengthViews(txt_daylength2, dataset.dataActual.dayLengthOther(), R.string.length_day);
            updateDayLengthViews(txt_lightlength2, dataset.dataCivil.dayLengthOther(), R.string.length_light);

        } else {
            String notCalculated = getString(R.string.time_loading);

            row_solarnoon.updateFields(notCalculated);
            row_actual.updateFields(notCalculated, notCalculated);
            row_civil.updateFields(notCalculated, notCalculated);
            row_nautical.updateFields(notCalculated, notCalculated);
            row_astro.updateFields(notCalculated, notCalculated);

            row_gold.updateFields(notCalculated, notCalculated);
            row_blue8.updateFields(notCalculated, notCalculated);
            row_blue4.updateFields(notCalculated, notCalculated);

            row_solarnoon2.updateFields(notCalculated);
            row_actual2.updateFields(notCalculated, notCalculated);
            row_civil2.updateFields(notCalculated, notCalculated);
            row_nautical2.updateFields(notCalculated, notCalculated);
            row_astro2.updateFields(notCalculated, notCalculated);

            row_gold2.updateFields(notCalculated, notCalculated);
            row_blue8_2.updateFields(notCalculated, notCalculated);
            row_blue4_2.updateFields(notCalculated, notCalculated);
        }

        //
        // moon
        //
        sunsetHeader.measure(0, 0);      // adjust moonrise/moonset columns to match width of sunrise/sunset columns
        int sunsetHeaderWidth = sunsetHeader.getMeasuredWidth();
        moonrise.adjustColumnWidth(SuntimesActivity.this, sunsetHeaderWidth);
        moonrise2.adjustColumnWidth(SuntimesActivity.this, sunsetHeaderWidth);

        moonphase.updateViews(SuntimesActivity.this, dataset3);
        moonphase2.updateViews(SuntimesActivity.this, dataset3);
        moonrise.updateViews(SuntimesActivity.this, dataset3);
        moonrise2.updateViews(SuntimesActivity.this, dataset3);

        //
        // equinox and solstice
        //
        boolean enableEquinox = AppSettings.loadShowEquinoxPref(this);
        showEquinoxView(enableEquinox && dataset2 != null && dataset2.isImplemented());
        card_equinoxSolstice.setTrackingMode(WidgetSettings.loadTrackingModePref(context, AppWidgetManager.INVALID_APPWIDGET_ID));
        card_equinoxSolstice.updateViews(SuntimesActivity.this, dataset2);
        card_equinoxSolstice.post(updateEquinoxViewColumnWidth);

        //
        // clock & date
        //
        Calendar now = dataset.now();
        Date data_date = dataset.dataActual.date();
        Date data_date2 = dataset.dataActual.dateOther();

        //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());       // 4/11/2016
        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());   // Apr 11, 2016
        dateFormat.setTimeZone(dataset.timezone());
        //DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());   // April 11, 2016

        String thisString = getString(R.string.today);
        String otherString = getString(R.string.tomorrow);

        if (dataset.dataActual.todayIsNotToday())
        {
            WidgetSettings.DateInfo nowInfo = new WidgetSettings.DateInfo(now);
            WidgetSettings.DateInfo dataInfo = new WidgetSettings.DateInfo(dataset.dataActual.calendar());
            if (!nowInfo.equals(dataInfo))
            {
                Date time = now.getTime();
                if (data_date.after(time))
                {
                    thisString = getString(R.string.future_today);
                    otherString = getString(R.string.future_tomorrow);
                    dateWarning.shouldShow = true;

                } else if (data_date.before(time)) {
                    thisString = getString(R.string.past_today);
                    otherString = getString(R.string.past_tomorrow);
                    dateWarning.shouldShow = true;
                }
            }
        }

        // date fields
        ImageSpan dateWarningIcon = (showWarnings && dateWarning.shouldShow) ? SuntimesUtils.createWarningSpan(this, txt_date.getTextSize()) : null;
        String dateString = getString(R.string.dateField, thisString, dateFormat.format(data_date));
        SpannableStringBuilder dateSpan = SuntimesUtils.createSpan(this, dateString, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
        txt_date.setText(dateSpan);
        txt_date.setContentDescription(dateString.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), ""));

        String date2String = getString(R.string.dateField, otherString, dateFormat.format(data_date2));
        SpannableStringBuilder date2Span = SuntimesUtils.createSpan(this, date2String, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
        txt_date2.setText(date2Span);
        txt_date2.setContentDescription(date2String.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), ""));

        // timezone field
        TimeZone timezone = dataset.timezone();
        timezoneWarning.shouldShow = WidgetTimezones.isProbablyNotLocal(timezone, dataset.location(), dataset.date());
        ImageSpan timezoneWarningIcon = (showWarnings && timezoneWarning.shouldShow) ? SuntimesUtils.createWarningSpan(this, txt_timezone.getTextSize()) : null;

        boolean useDST = showWarnings && (Build.VERSION.SDK_INT < 24 ? timezone.useDaylightTime()
                                                                     : timezone.observesDaylightTime());
        boolean inDST = useDST && timezone.inDaylightTime(now.getTime());
        ImageSpan dstWarningIcon = (inDST) ? SuntimesUtils.createDstSpan(this, txt_timezone.getTextSize()) : null;

        SuntimesUtils.ImageSpanTag[] timezoneTags = {
                new SuntimesUtils.ImageSpanTag(SuntimesUtils.SPANTAG_WARNING, timezoneWarningIcon),
                new SuntimesUtils.ImageSpanTag(SuntimesUtils.SPANTAG_DST, dstWarningIcon)
        };

        String timezoneString = getString(R.string.timezoneField, timezone.getID());
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

        // "light map"
        boolean enableLightMap = AppSettings.loadShowLightmapPref(this);
        showLightMap(enableLightMap);
        lightmap.updateViews(enableLightMap ? dataset : null);

        showDayLength(dataset.isCalculated());
        showNotes(dataset.isCalculated());
        showWarnings();

        startTimeTask();
    }

    private void updateDayLengthViews(TextView textView, long dayLength, int labelID)
    {
        SuntimesUtils.TimeDisplayText dayLengthDisplay;
        if (dayLength <= 0)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 0, (showSeconds ? SuntimesUtils.strSeconds : SuntimesUtils.strMinutes)), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else if (dayLength >= SuntimesData.DAY_MILLIS)
            dayLengthDisplay = new SuntimesUtils.TimeDisplayText(String.format(SuntimesUtils.strTimeDeltaFormat, 24, SuntimesUtils.strHours), SuntimesUtils.strEmpty, SuntimesUtils.strEmpty);
        else dayLengthDisplay = utils.timeDeltaLongDisplayString(0, dayLength, showSeconds);

        dayLengthDisplay.setSuffix("");
        String dayLengthStr = dayLengthDisplay.toString();
        String dayLength_label = getString(labelID, dayLengthStr);
        textView.setText(SuntimesUtils.createBoldColorSpan(null, dayLength_label, dayLengthStr, color_textTimeDelta));
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
                    card_equinoxSolstice.adjustColumnWidth(SuntimesActivity.this, column.getMeasuredWidth());
                }
            }
        }
    };

    private void showWarnings()
    {
        if (showWarnings && timezoneWarning.shouldShow && !timezoneWarning.wasDismissed)
        {
            timezoneWarning.initWarning(this, txt_timezone, getString(R.string.timezoneWarning));
            timezoneWarning.snackbar.setAction(getString(R.string.configAction_setTimeZone), new View.OnClickListener()
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

        if (showWarnings && dateWarning.shouldShow && !dateWarning.wasDismissed)
        {
            dateWarning.initWarning(this, card_flipper, getString(R.string.dateWarning));
            dateWarning.snackbar.setAction(getString(R.string.configAction_setDate), new View.OnClickListener()
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
        lightmap.updateViews(false);
    }

    /**
     * onTouch swipe between the prev/next items in the view_flipper
     * @param event the touch MotionEvent
     * @return true continue gesture (propagate event), false end gesture (consume event)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return super.onTouchEvent(event);
    }

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
                        AppSettings.ClockTapAction action = AppSettings.loadNoteTapActionPref(SuntimesActivity.this);
                        switch (action)
                        {
                            case NOTHING:
                                break;

                            case TIMEZONE:
                                configTimeZone();
                                break;

                            case ALARM:
                                scheduleAlarmFromNote();
                                break;

                            case PREV_NOTE:
                                setUserSwappedCard(false, "noteTouchListener (tap prev)");
                                notes.showPrevNote();
                                break;

                            case NEXT_NOTE:
                            default:
                                setUserSwappedCard(false, "noteTouchListener (tap next)");
                                notes.showNextNote();
                                break;
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

    /**
     * viewFlipper "time card" onTouchListener; swipe left/right between viewflipper layouts (today/tomorrow)
     */
    private View.OnTouchListener timeCardTouchListener = new View.OnTouchListener()
    {
        public int MOVE_SENSITIVITY = 150;
        public int FLING_SENSITIVITY = 25;
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
                    if ((secondTouchX - firstTouchX) > FLING_SENSITIVITY)
                    {   // swipe right; back to previous view
                        boolean flipResult = (isRtl ? showNextCard() : showPreviousCard());
                        setUserSwappedCard(userSwappedCard || flipResult, "timeCardTouchListener (fling prev)");

                    } else if (firstTouchX - secondTouchX > FLING_SENSITIVITY) {
                        // swipe left; advance to next view
                        boolean flipResult = (isRtl ? showPreviousCard() : showNextCard());
                        setUserSwappedCard(userSwappedCard || flipResult, "timeCardTouchListener (fling next)");

                    } else {
                        // swipe cancel; reset current view
                        final View currentView = card_flipper.getCurrentView();
                        currentView.layout(0, currentView.getTop(), currentView.getWidth(), currentView.getBottom());
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    float currentTouchX = event.getX();
                    int moveDelta = (int)(currentTouchX - firstTouchX);
                    boolean isSwipeRight = (moveDelta > 0);

                    final View currentView = card_flipper.getCurrentView();
                    int currentIndex = card_flipper.getDisplayedChild();

                    int otherIndex;
                    if (isRtl)
                    {
                        otherIndex = (isSwipeRight ? currentIndex + 1 : currentIndex - 1);
                    } else {
                        otherIndex = (isSwipeRight ? currentIndex - 1 : currentIndex + 1);
                    }

                    if (otherIndex >= 0 && otherIndex < card_flipper.getChildCount())
                    {
                        // in-between child views; flip between them
                        currentView.layout( moveDelta, currentView.getTop(),
                                moveDelta + currentView.getWidth(), currentView.getBottom() );

                        // extended movement; manually trigger swipe/fling
                        if (moveDelta > MOVE_SENSITIVITY || moveDelta < MOVE_SENSITIVITY * -1)
                        {
                            event.setAction(MotionEvent.ACTION_UP);
                            return onTouch(view, event);
                        }

                    } //else {
                        // at-a-boundary (the first/last view);
                        // TODO: animate somehow to let user know there aren't additional views
                    //}
                    break;
            }
            return true;
        }
    };

    /**
     * Show the 'next' set of data displayed by the main view_flipper.
     */
    public boolean showNextCard()
    {
        if (hasNextCard())
        {
            card_flipper.setOutAnimation(anim_card_outNext);
            card_flipper.setInAnimation(anim_card_inNext);
            card_flipper.showNext();
            SuntimesUtils.announceForAccessibility(card_flipper, txt_date2.getContentDescription().toString());
            return true;
        }
        return false;
    }

    public boolean hasNextCard()
    {
        int current = card_flipper.getDisplayedChild();
        return ((current + 1) < card_flipper.getChildCount());
    }

    /**
     * Show the 'previous' set of data displayed by the main view_flipper.
     */
    public boolean showPreviousCard()
    {
        if (hasPreviousCard())
        {
            card_flipper.setOutAnimation(anim_card_outPrev);
            card_flipper.setInAnimation(anim_card_inPrev);
            card_flipper.showPrevious();
            SuntimesUtils.announceForAccessibility(card_flipper, txt_date.getContentDescription().toString());
            return true;
        }
        return false;
    }

    public boolean hasPreviousCard()
    {
        int current = card_flipper.getDisplayedChild();
        int prev = current - 1;
        return (prev >= 0);
    }

    View.OnClickListener onNextCardClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setUserSwappedCard( showNextCard(), "onNextCardClick" );
        }
    };

    View.OnClickListener onPrevCardClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setUserSwappedCard( showPreviousCard(), "onPrevCardClick" );
        }
    };

    View.OnClickListener onNextNoteClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setUserSwappedCard( false, "onNextNoteClick" );
            notes.showNextNote();
        }
    };

    View.OnClickListener onPrevNoteClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setUserSwappedCard( false, "onPrevNoteClick" );
            notes.showPrevNote();
        }
    };

    View.OnClickListener onSunriseClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setUserSwappedCard(false, "onSunriseClick");
            notes.setNoteIndex(notes.getNoteIndex(SolarEvents.SUNRISE));
        }
    };

    View.OnClickListener onSunsetClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setUserSwappedCard(false, "onSunsetClick");
            notes.setNoteIndex(notes.getNoteIndex(SolarEvents.SUNSET));
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
            AppSettings.ClockTapAction action = AppSettings.loadClockTapActionPref(SuntimesActivity.this);
            if (action == AppSettings.ClockTapAction.NOTHING)
            {
                return;
            }

            if (action == AppSettings.ClockTapAction.ALARM)
            {
                scheduleAlarm();
                return;
            }

            if (action == AppSettings.ClockTapAction.TIMEZONE)
            {
                configTimeZone();
                return;
            }

            if (action == AppSettings.ClockTapAction.NEXT_NOTE)
            {
                setUserSwappedCard( false, "onClockClick (nextNote)" );
                notes.showNextNote();
                return;
            }

            if (action == AppSettings.ClockTapAction.PREV_NOTE)
            {
                setUserSwappedCard( false, "onClockClick (prevNote)" );
                notes.showPrevNote();
                return;
            }

            Log.w("SuntimesActivity", "Unrecognized ClockTapAction (so doing nothing)" );
        }
    };

    /**
     * Toggle day length visibility.
     * @param value true show daylength ui, false hide daylength ui
     */
    protected void showDayLength( boolean value )
    {
        layout_daylength.setVisibility( (value ? View.VISIBLE : View.INVISIBLE) );
        layout_daylength2.setVisibility( (value ? View.VISIBLE : View.INVISIBLE) );
    }

    /**
     * @param value
     */
    protected void showMoonrise( boolean value )
    {
        int visibility = (value ? View.VISIBLE : View.GONE);

        moonClickArea.setVisibility(visibility);
        moonlabel.setVisibility(visibility);
        moonrise.setVisibility(visibility);
        moonphase.setVisibility(visibility);

        moonClickArea2.setVisibility(visibility);
        moonlabel2.setVisibility(visibility);
        moonrise2.setVisibility(visibility);
        moonphase2.setVisibility(visibility);
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
     * Toggle lightmap visibility.
     * @param value true show lightmap ui, false hide lightmap ui
     */
    protected void showLightMap( boolean value )
    {
        lightmapLayout.setVisibility((value ? View.VISIBLE : View.GONE));
    }

    /**
     * Show the lightmap dialog.
     */
    protected void showLightMapDialog()
    {
        final LightMapDialog lightMapDialog = new LightMapDialog();
        lightMapDialog.themeViews(this, appThemeOverride);
        lightMapDialog.setData(dataset);
        lightMapDialog.show(getSupportFragmentManager(), DIALOGTAG_LIGHTMAP);
    }

    protected void showWorldMapDialog()
    {
        WorldMapDialog worldMapDialog = new WorldMapDialog();
        worldMapDialog.themeViews(this, appThemeOverride);
        worldMapDialog.setData(dataset);
        worldMapDialog.show(getSupportFragmentManager(), DIALOGTAG_WORLDMAP);
    }

    protected void showEquinoxView( boolean value )
    {
        equinoxLayout.setVisibility((value ? View.VISIBLE : View.GONE ));
    }

    protected void showEquinoxDialog()
    {
        EquinoxDialog equinoxDialog = new EquinoxDialog();
        equinoxDialog.themeViews(this, appThemeOverride);
        equinoxDialog.setData((dataset2 != null) ? dataset2 : new SuntimesEquinoxSolsticeDataset(SuntimesActivity.this));
        equinoxDialog.show(getSupportFragmentManager(), DIALOGTAG_EQUINOX);
    }

    protected void showMoonDialog()
    {
        MoonDialog moonDialog = new MoonDialog();
        moonDialog.themeViews(this, appThemeOverride);
        moonDialog.setData((dataset3 != null) ? dataset3 : new SuntimesMoonData(SuntimesActivity.this, 0, "moon"));
        moonDialog.show(getSupportFragmentManager(), DIALOGTAG_MOON);
    }

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

    /**
     * @param tomorrow true is "tomorrow" date field, false is "today" date field
     * @return an OnClickListener for the specified date field
     */
    private View.OnClickListener dateTapClickListener( final boolean tomorrow )
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AppSettings.DateTapAction action = AppSettings.loadDateTapActionPref(SuntimesActivity.this);
                onDateTapAction(action, tomorrow);
            }
        };
    }

    /**
     * @param tomorrow true is "tomorrow" date field, false is "today" date field
     * @return an OnLongClickListener for the specified date field
     */
    private View.OnLongClickListener dateTapLongClickListener( final boolean tomorrow )
    {
        return new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                AppSettings.DateTapAction action = AppSettings.loadDateTapAction1Pref(SuntimesActivity.this);
                onDateTapAction(action, tomorrow);
                return true;
            }
        };
    }

    private void onDateTapAction( AppSettings.DateTapAction action, boolean tomorrow )
    {
        switch (action)
        {
            case NOTHING:
                break;

            case CONFIG_DATE:
                configDate();
                break;

            case SHOW_CALENDAR:
                showCalendar(tomorrow);
                break;

            case SWAP_CARD:
            default:
                if (tomorrow)
                {
                    setUserSwappedCard( showPreviousCard(), "onDateTapClick (prevCard)" );
                } else {
                    setUserSwappedCard( showNextCard(), "onDateTapClick (nextCard)" );
                }
                break;
        }
    }

    //private void initTimeFields()
    //{
        /**for (SolarEvents.SolarEventField key : timeFields.keySet())
        {
            TextView field = timeFields.get(key);
            field.setOnClickListener(createTimeFieldClickListener(key));
        }*/
    //}

    private View.OnClickListener createTimeFieldClickListener( final SolarEvents.SolarEventField event )
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Log.d("DEBUG", "TimeField clicked: " + event.toString());
                notes.showNote(event);
            }
        };
    }

    public void highlightTimeField( SolarEvents.SolarEventField highlightField )
    {
        int nextCardOffset = 0;
        int currentCard = this.card_flipper.getDisplayedChild();

        for (SolarEvents.SolarEventField field : timeFields.keySet())
        {
            boolean isMoonField = (field.event == SolarEvents.MOONRISE || field.event == SolarEvents.MOONSET);
            TextView[] txtFields;

            if (isMoonField)
            {
                MoonRiseSetView moonView = (field.tomorrow ? moonrise2 : moonrise);
                txtFields = moonView.getTimeViews(field.event);

            } else {
                txtFields = new TextView[] { timeFields.get(field) };
            }

            for (TextView txtField : txtFields)
            {
                if (txtField != null && txtField.getVisibility() == View.VISIBLE)
                {
                    if (field.equals(highlightField))
                    {
                        txtField.setTypeface(txtField.getTypeface(), Typeface.BOLD);
                        txtField.setPaintFlags(txtField.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                        if (currentCard == 0 && field.tomorrow) {
                            nextCardOffset = 1;

                        } else if (currentCard == 1 && !field.tomorrow) {
                            nextCardOffset = -1;
                        }

                    } else {
                        txtField.setTypeface(Typeface.create(txtField.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                        txtField.setPaintFlags(txtField.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    }
                }
            }
        }

        if (!userSwappedCard)
        {
            //Log.d("DEBUG", "Swapping card to show highlighted :: userSwappedCard " + userSwappedCard);
            if (nextCardOffset > 0)
            {
                showNextCard();

            } else if (nextCardOffset < 0) {
                showPreviousCard();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showCalendar(boolean tomorrow)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            long startMillis = (tomorrow ? dataset.otherCalendar().getTimeInMillis() : dataset.calendar().getTimeInMillis());
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, startMillis);
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            startActivity(intent);
        }
    }

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

        highlightTimeField(new SolarEvents.SolarEventField(note.noteMode, note.tomorrow));
    }

    /**
     * TimeFieldRow
     */
    private static class TimeFieldRow
    {
        private TextView label;
        private TextView[] fields;

        private TimeFieldRow( TextView label, TextView ...fields )
        {
            this.label = label;
            this.fields = fields;
        }

        private TimeFieldRow( View parent, int labelID, int ...fieldIDs )
        {
            if (parent != null)
            {
                this.label = (TextView) parent.findViewById(labelID);
                this.fields = new TextView[fieldIDs.length];

                for (int i=0; i<fieldIDs.length; i++)
                {
                    this.fields[i] = (TextView) parent.findViewById(fieldIDs[i]);
                }
            }
        }

        public TextView getLabel()
        {
            return label;
        }

        public TextView getField( int i )
        {
            if (i >= 0 && i < fields.length)
                return fields[i];
            else return null;
        }

        public void updateFields( String ...values )
        {
            for (int i=0; i<values.length; i++)
            {
                if (i >= fields.length)
                    break;

                if (fields[i] != null)
                {
                    fields[i].setText( values[i] );
                }
            }
        }

        public void setVisible( boolean show )
        {
            int visibility = (show ? View.VISIBLE : View.GONE);

            if (label != null)
            {
                label.setVisibility(visibility);
            }

            for (int i=0; i<fields.length; i++)
            {
                if (fields[i] != null)
                {
                    fields[i].setVisibility(visibility);
                }
            }
        }
    }

    /**
     * SuntimesWarning; wraps a Snackbar and some flags.
     */
    private class SuntimesWarning
    {
        public static final int ANNOUNCE_DELAY_MS = 500;
        public static final String KEY_WASDISMISSED = "userDismissedWarning";

        public SuntimesWarning(String id)
        {
            this.id = id;
        }
        protected String id = "";

        protected Snackbar snackbar = null;
        protected boolean shouldShow = false;
        protected boolean wasDismissed = false;

        protected String contentDescription = null;
        protected View parentView = null;

        public void initWarning(@NonNull Context context, View view, String msg)
        {
            this.parentView = view;
            ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, txt_date.getTextSize());
            SpannableStringBuilder message = SuntimesUtils.createSpan(SuntimesActivity.this, msg, SuntimesUtils.SPANTAG_WARNING, warningIcon);
            this.contentDescription = msg.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), context.getString(R.string.spanTag_warning));

            wasDismissed = false;
            snackbar = Snackbar.make(card_flipper, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.addCallback(snackbarListener);
            setContentDescription(contentDescription);
            themeWarning(context, snackbar);
        }

        @SuppressLint("ResourceType")
        private void themeWarning(@NonNull Context context, @NonNull Snackbar snackbarWarning)
        {
            int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor };
            TypedArray a = context.obtainStyledAttributes(colorAttrs);
            int textColor = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
            int accentColor = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
            int backgroundColor = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
            a.recycle();

            View snackbarView = snackbarWarning.getView();
            snackbarView.setBackgroundColor(backgroundColor);
            snackbarWarning.setActionTextColor(accentColor);

            TextView snackbarText = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            if (snackbarText != null) {
                snackbarText.setTextColor(textColor);
                snackbarText.setMaxLines(5);
            }
        }

        private Snackbar.Callback snackbarListener = new Snackbar.Callback()
        {
            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                super.onDismissed(snackbar, event);
                switch (event)
                {
                    case DISMISS_EVENT_SWIPE:
                        wasDismissed = true;
                        showNextWarning();
                        break;
                }
            }
        };

        private void showNextWarning()
        {
            showWarnings();
        }

        public boolean isShown()
        {
            return (snackbar != null && snackbar.isShown());
        }

        public void show()
        {
            if (snackbar != null)
            {
                snackbar.show();
            }
            announceWarning();
        }

        public void dismiss()
        {
            if (isShown())
            {
                snackbar.dismiss();
            }
        }

        public void reset()
        {
            wasDismissed = false;
            shouldShow = false;
        }

        public void setContentDescription( String value )
        {
            this.contentDescription = value;
            TextView snackText = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            if (snackText != null)
            {
                snackText.setContentDescription(contentDescription);
            }
        }

        public void announceWarning()
        {
            if (parentView != null && contentDescription != null)
            {
                parentView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SuntimesUtils.announceForAccessibility(parentView, contentDescription);
                    }
                }, ANNOUNCE_DELAY_MS);
            }
        }

        public void save( Bundle outState )
        {
            if (outState != null)
            {
                outState.putBoolean(KEY_WASDISMISSED + id, wasDismissed);
            }
        }

        public void restore( Bundle savedState )
        {
            if (savedState != null)
            {
                wasDismissed = savedState.getBoolean(KEY_WASDISMISSED + id, false);
            }
        }
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
        }
    }
    
    private void setUserSwappedCard( boolean value, String tag )
    {
        userSwappedCard = value;
        //Log.d("DEBUG", "userSwappedCard set " + value + " (" + tag + " )");
    }

    /**
     * Get the current theme's resource id (used by test verification).
     * @return the resource id of the current theme/style (or 0 if getTHemeResId failed)
     */
    public int getThemeId()
    {
        try {
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
