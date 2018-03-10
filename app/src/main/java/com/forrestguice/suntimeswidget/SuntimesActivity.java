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

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import android.support.v7.widget.Toolbar;
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

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.getfix.GetFixHelper;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.notes.NoteChangedListener;
import com.forrestguice.suntimeswidget.notes.NoteData;
import com.forrestguice.suntimeswidget.notes.SuntimesNotes;
import com.forrestguice.suntimeswidget.notes.SuntimesNotes3;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.lang.reflect.Method;
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class SuntimesActivity extends AppCompatActivity
{
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
    private static final String DIALOGTAG_EQUINOX = "equinox";

    protected static final SuntimesUtils utils = new SuntimesUtils();

    private ActionBar actionBar;
    private Menu actionBarMenu;

    private GetFixHelper getFixHelper;

    private WidgetSettings.Location location;
    protected SuntimesNotes notes;
    protected SuntimesRiseSetDataset dataset;
    protected SuntimesEquinoxSolsticeDataset dataset2;

    private int color_textTimeDelta;

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

    private TextView txt_date,              txt_date2;
    private TextView txt_sunrise_actual,    txt_sunrise2_actual;
    private TextView txt_sunrise_civil,     txt_sunrise2_civil;
    private TextView txt_sunrise_nautical,  txt_sunrise2_nautical;
    private TextView txt_sunrise_astro,     txt_sunrise2_astro;
    private TextView txt_sunset_actual,     txt_sunset2_actual;
    private TextView txt_sunset_civil,      txt_sunset2_civil;
    private TextView txt_sunset_nautical,   txt_sunset2_nautical;
    private TextView txt_sunset_astro,      txt_sunset2_astro;
    private TextView txt_solarnoon,         txt_solarnoon2;

    private TimeFieldRow row_gold,          row_gold2;
    private TimeFieldRow row_blue8,         row_blue8_2;
    private TimeFieldRow row_blue4,         row_blue4_2;

    private LinearLayout layout_daylength,  layout_daylength2;
    private TextView txt_daylength,         txt_daylength2;
    private TextView txt_lightlength,       txt_lightlength2;

    private EquinoxView card_equinoxSolstice;
    private View equinoxLayout;

    private LightMapView lightmap;
    private View lightmapLayout;

    private TextView txt_datasource;
    private View layout_datasource;

    private boolean isRtl = false;
    private boolean userSwappedCard = false;
    private HashMap<SolarEvents.SolarEventField, TextView> timeFields;

    private boolean showWarnings = false;
    private SuntimesWarning timezoneWarning;
    private SuntimesWarning dateWarning;
    private List<SuntimesWarning> warnings;

    private boolean showSeconds = WidgetSettings.PREF_DEF_GENERAL_SHOWSECONDS;
    private boolean showGold = AppSettings.PREF_DEF_UI_SHOWGOLDHOUR;
    private boolean showBlue = AppSettings.PREF_DEF_UI_SHOWBLUEHOUR;
    private boolean verboseAccessibility = AppSettings.PREF_DEF_ACCESSIBILITY_VERBOSE;

    public SuntimesActivity()
    {
        super();
    }

    /**
     * OnCreate: the Activity initially created
     * @param savedState a Bundle containing previously saved application state
     */
    @Override
    public void onCreate(Bundle savedState)
    {
        Context context = SuntimesActivity.this;
        setTheme(AppSettings.loadTheme(this));
        GetFixUI.themeIcons(this);

        super.onCreate(savedState);
        setResult(RESULT_CANCELED);

        initLocale(this);  // must follow super.onCreate or locale is reverted
        setContentView(R.layout.layout_main);
        initViews(context);

        initWarnings(context, savedState);

        initGetFix();
        getFixHelper.loadSettings(savedState);
        onStart_resetNoteIndex = true;

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null)
        {
            intent.setData(null);
            configLocation(data);
        }
    }

    private void initLocale( Context context )
    {
        AppSettings.initLocale(this);
        isRtl = AppSettings.isLocaleRtl(this);

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
        FragmentManager fragments = getSupportFragmentManager();
        TimeZoneDialog timezoneDialog = (TimeZoneDialog) fragments.findFragmentByTag(DIALOGTAG_TIMEZONE);
        if (timezoneDialog != null)
        {
            timezoneDialog.setOnAcceptedListener(onConfigTimeZone);
            timezoneDialog.setOnCanceledListener(onCancelTimeZone);
            //Log.d("DEBUG", "TimeZoneDialog listeners restored.");
        }

        AlarmDialog alarmDialog = (AlarmDialog) fragments.findFragmentByTag(DIALOGTAG_ALARM);
        if (alarmDialog != null)
        {
            alarmDialog.setData(this, dataset);
            alarmDialog.setOnAcceptedListener(alarmDialog.scheduleAlarmClickListener);
            //Log.d("DEBUG", "AlarmDialog listeners restored.");
        }

        LocationConfigDialog locationDialog = (LocationConfigDialog) fragments.findFragmentByTag(DIALOGTAG_LOCATION);
        if (locationDialog != null)
        {
            locationDialog.setOnAcceptedListener( onConfigLocation(locationDialog) );
            //Log.d("DEBUG", "LocationDialog listeners restored.");
        }

        TimeDateDialogEasy dateDialog = (TimeDateDialogEasy) fragments.findFragmentByTag(DIALOGTAG_DATE);
        if (dateDialog != null)
        {
            dateDialog.setOnAcceptedListener(onConfigDate);
            dateDialog.setOnCanceledListener(onCancelDate);
            //Log.d("DEBUG", "TimeDateDialog listeners restored.");
        }

        LightMapDialog lightMapDialog = (LightMapDialog) fragments.findFragmentByTag(DIALOGTAG_LIGHTMAP);
        if (lightMapDialog != null)
        {
            lightMapDialog.setData(dataset);
            lightMapDialog.updateViews();
            //Log.d("DEBUG", "LightMapDialog updated on restore.");
        }

        EquinoxDialog equinoxDialog = (EquinoxDialog) fragments.findFragmentByTag(DIALOGTAG_EQUINOX);
        if (equinoxDialog != null)
        {
            equinoxDialog.setData(dataset2);
            equinoxDialog.updateViews(dataset2);
            //Log.d("DEBUG", "EquinoxDialog updated on restore.");
        }
    }

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

    private void initMisc(Context context)
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
                WidgetSettings.Location location = new WidgetSettings.Location(getString(R.string.gps_lastfix_title_found), locations[0]);
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
                            (getFixHelper.isLocationEnabled() ? ICON_GPS_FOUND
                                                              : ICON_GPS_DISABLED));

                    if (result != null)
                    {
                        WidgetSettings.Location location = new WidgetSettings.Location(getString(R.string.gps_lastfix_title_found), result);
                        WidgetSettings.saveLocationPref(SuntimesActivity.this, 0, location);

                    } else {
                        String msg = (wasCancelled ? getString(R.string.gps_lastfix_toast_cancelled) : getString(R.string.gps_lastfix_toast_notfound));
                        Toast errorMsg = Toast.makeText(SuntimesActivity.this, msg, Toast.LENGTH_LONG);
                        errorMsg.show();
                    }
                    SuntimesActivity.this.calculateData(SuntimesActivity.this);
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
                    refreshItem.setIcon((getFixHelper.isLocationEnabled() ? GetFixUI.ICON_GPS_FOUND : GetFixUI.ICON_GPS_DISABLED));
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

            txt_sunrise_actual = (TextView) viewToday.findViewById(R.id.text_time_sunrise_actual);
            txt_sunset_actual = (TextView) viewToday.findViewById(R.id.text_time_sunset_actual);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNRISE, false), txt_sunrise_actual);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNSET, false), txt_sunset_actual);

            txt_sunrise_civil = (TextView) viewToday.findViewById(R.id.text_time_sunrise_civil);
            txt_sunset_civil = (TextView) viewToday.findViewById(R.id.text_time_sunset_civil);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_CIVIL, false), txt_sunrise_civil);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_CIVIL, false), txt_sunset_civil);

            txt_sunrise_nautical = (TextView) viewToday.findViewById(R.id.text_time_sunrise_nautical);
            txt_sunset_nautical = (TextView) viewToday.findViewById(R.id.text_time_sunset_nautical);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_NAUTICAL, false), txt_sunrise_nautical);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_NAUTICAL, false), txt_sunset_nautical);

            txt_sunrise_astro = (TextView) viewToday.findViewById(R.id.text_time_sunrise_astro);
            txt_sunset_astro = (TextView) viewToday.findViewById(R.id.text_time_sunset_astro);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_ASTRONOMICAL, false), txt_sunrise_astro);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_ASTRONOMICAL, false), txt_sunset_astro);

            txt_solarnoon = (TextView) viewToday.findViewById(R.id.text_time_noon);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.NOON, false), txt_solarnoon);

            row_gold = new TimeFieldRow(viewToday, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_GOLDEN, false), row_gold.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_GOLDEN, false), row_gold.getField(1));

            row_blue8 = new TimeFieldRow(viewToday, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE8, false), row_blue8.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE8, false), row_blue8.getField(1));

            row_blue4 = new TimeFieldRow(viewToday, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE4, false), row_blue4.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE4, false), row_blue4.getField(1));

            layout_daylength = (LinearLayout) viewToday.findViewById(R.id.layout_daylength);
            txt_daylength = (TextView) viewToday.findViewById(R.id.text_daylength);
            txt_lightlength = (TextView) viewToday.findViewById(R.id.text_lightlength);

            btn_flipperNext_today = (ImageButton)viewToday.findViewById(R.id.info_time_nextbtn);
            btn_flipperNext_today.setOnClickListener(onNextCardClick);
            btn_flipperNext_today.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        btn_flipperNext_today.setColorFilter(ContextCompat.getColor(SuntimesActivity.this, R.color.btn_tint_pressed));
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

            txt_sunrise2_actual = (TextView) viewTomorrow.findViewById(R.id.text_time_sunrise_actual);
            txt_sunset2_actual = (TextView) viewTomorrow.findViewById(R.id.text_time_sunset_actual);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNRISE, true), txt_sunrise2_actual);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.SUNSET, true), txt_sunset2_actual);

            txt_sunrise2_civil = (TextView) viewTomorrow.findViewById(R.id.text_time_sunrise_civil);
            txt_sunset2_civil = (TextView) viewTomorrow.findViewById(R.id.text_time_sunset_civil);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_CIVIL, true), txt_sunrise2_civil);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_CIVIL, true), txt_sunset2_civil);

            txt_sunrise2_nautical = (TextView) viewTomorrow.findViewById(R.id.text_time_sunrise_nautical);
            txt_sunset2_nautical = (TextView) viewTomorrow.findViewById(R.id.text_time_sunset_nautical);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_NAUTICAL, true), txt_sunrise2_nautical);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_NAUTICAL, true), txt_sunset2_nautical);

            txt_sunrise2_astro = (TextView) viewTomorrow.findViewById(R.id.text_time_sunrise_astro);
            txt_sunset2_astro = (TextView) viewTomorrow.findViewById(R.id.text_time_sunset_astro);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_ASTRONOMICAL, true), txt_sunrise2_astro);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_ASTRONOMICAL, true), txt_sunset2_astro);

            txt_solarnoon2 = (TextView) viewTomorrow.findViewById(R.id.text_time_noon);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.NOON, true), txt_solarnoon2);

            row_gold2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_golden, R.id.text_time_golden_morning, R.id.text_time_golden_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_GOLDEN, true), row_gold2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_GOLDEN, true), row_gold2.getField(1));

            row_blue8_2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_blue8, R.id.text_time_blue8_morning, R.id.text_time_blue8_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE8, true), row_blue8_2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE8, true), row_blue8_2.getField(1));

            row_blue4_2 = new TimeFieldRow(viewTomorrow, R.id.text_time_label_blue4, R.id.text_time_blue4_morning, R.id.text_time_blue4_evening);
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.MORNING_BLUE4, true), row_blue4_2.getField(0));
            timeFields.put(new SolarEvents.SolarEventField(SolarEvents.EVENING_BLUE4, true), row_blue4_2.getField(1));

            layout_daylength2 = (LinearLayout) viewTomorrow.findViewById(R.id.layout_daylength);
            txt_daylength2 = (TextView) viewTomorrow.findViewById(R.id.text_daylength);
            txt_lightlength2 = (TextView) viewTomorrow.findViewById(R.id.text_lightlength);

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
                        btn_flipperPrev_tomorrow.setColorFilter(ContextCompat.getColor(SuntimesActivity.this, R.color.btn_tint_pressed));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    {
                        btn_flipperPrev_tomorrow.setColorFilter(null);
                    }
                    return false;
                }
            });

            initTimeFields();

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
        notes = new SuntimesNotes3();
        notes.init(this, dataset);
        notes.setOnChangedListener(new NoteChangedListener()
        {
            @Override
            public void onNoteChanged(NoteData note, int transition)
            {
                updateNoteUI(note, transition);
            }
        });
    }

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Select a date other than today.
     */
    private void configDate()
    {
        final TimeDateDialogEasy datePicker = new TimeDateDialogEasy();
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
        String topic1 = getString(R.string.help_general_timeMode);
        String topic2 = getString(R.string.help_general_daylength);
        String topic3 = getString(R.string.help_general_goldhour);
        String topic4 = getString(R.string.help_general_bluehour);
        String helpText = getString(R.string.help_general4, topic1, topic2, topic3, topic4);

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
        startActivity(settingsIntent);
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
            alarmDialog.setData(this, dataset);
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
    }

    protected void calculateData( Context context )
    {
        initData(context);

        if (dataset != null)
            dataset.calculateData();

        if (dataset2 != null)
            dataset2.calculateData();

        initNotes();
    }

    protected void invalidateData( Context context )
    {
        if (dataset != null)
            dataset.invalidateCalculation();

        if (dataset2 != null)
            dataset2.invalidateCalculation();
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
        String locationSubtitle = location.toString();

        if (actionBar != null)
        {
            actionBar.setTitle(locationTitle);
            actionBar.setSubtitle(locationSubtitle);
        }

        boolean supportsGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
        showGold = AppSettings.loadGoldHourPref(context) && supportsGoldBlue;
        showGoldTimes(showGold);

        showBlue = AppSettings.loadBlueHourPref(context) && supportsGoldBlue;
        showBlueTimes(showBlue);

        showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);

        if (dataset.isCalculated())
        {
            // today's view
            SuntimesUtils.TimeDisplayText sunriseString_actualTime = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunriseCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_civilTime = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunriseCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_nauticalTime = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunriseCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_astroTime = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunriseCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText noonString = utils.calendarTimeShortDisplayString(context, dataset.dataNoon.sunriseCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_actualTime = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunsetCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_civilTime = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunsetCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_nauticalTime = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunsetCalendarToday(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_astroTime = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunsetCalendarToday(), showSeconds);

            // tomorrow's view
            SuntimesUtils.TimeDisplayText sunriseString_actualTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunriseCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_civilTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunriseCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_nauticalTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunriseCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunriseString_astroTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunriseCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText noonString2 = utils.calendarTimeShortDisplayString(context, dataset.dataNoon.sunriseCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_actualTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataActual.sunsetCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_civilTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataCivil.sunsetCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_nauticalTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataNautical.sunsetCalendarOther(), showSeconds);
            SuntimesUtils.TimeDisplayText sunsetString_astroTime2 = utils.calendarTimeShortDisplayString(context, dataset.dataAstro.sunsetCalendarOther(), showSeconds);

            txt_sunrise_actual.setText(sunriseString_actualTime.toString());
            txt_sunrise_civil.setText(sunriseString_civilTime.toString());
            txt_sunrise_nautical.setText(sunriseString_nauticalTime.toString());
            txt_sunrise_astro.setText(sunriseString_astroTime.toString());
            txt_solarnoon.setText(noonString.toString());
            txt_sunset_actual.setText(sunsetString_actualTime.toString());
            txt_sunset_civil.setText(sunsetString_civilTime.toString());
            txt_sunset_nautical.setText(sunsetString_nauticalTime.toString());
            txt_sunset_astro.setText(sunsetString_astroTime.toString());

            txt_sunrise2_actual.setText(sunriseString_actualTime2.toString());
            txt_sunrise2_civil.setText(sunriseString_civilTime2.toString());
            txt_sunrise2_nautical.setText(sunriseString_nauticalTime2.toString());
            txt_sunrise2_astro.setText(sunriseString_astroTime2.toString());
            txt_solarnoon2.setText(noonString2.toString());
            txt_sunset2_actual.setText(sunsetString_actualTime2.toString());
            txt_sunset2_civil.setText(sunsetString_civilTime2.toString());
            txt_sunset2_nautical.setText(sunsetString_nauticalTime2.toString());
            txt_sunset2_astro.setText(sunsetString_astroTime2.toString());

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

            SuntimesUtils.TimeDisplayText dayLengthDisplay = utils.timeDeltaLongDisplayString(0, dataset.dataActual.dayLengthToday(), showSeconds);
            dayLengthDisplay.setSuffix("");
            String dayLength = dayLengthDisplay.toString();
            String dayLength_label = getString(R.string.length_day, dayLength);
            txt_daylength.setText(SuntimesUtils.createBoldColorSpan(dayLength_label, dayLength, color_textTimeDelta));

            SuntimesUtils.TimeDisplayText dayLengthDisplay2 = utils.timeDeltaLongDisplayString(0, dataset.dataActual.dayLengthOther(), showSeconds);
            dayLengthDisplay2.setSuffix("");
            String dayLength2 = dayLengthDisplay2.toString();
            String dayLength2_label = getString(R.string.length_day, dayLength2);
            txt_daylength2.setText(SuntimesUtils.createBoldColorSpan(dayLength2_label, dayLength2, color_textTimeDelta));

            SuntimesUtils.TimeDisplayText lightLengthDisplay = utils.timeDeltaLongDisplayString(0, dataset.dataCivil.dayLengthToday(), showSeconds);
            lightLengthDisplay.setSuffix("");
            String lightLength = lightLengthDisplay.toString();
            String lightLength_label = getString(R.string.length_light, lightLength);
            txt_lightlength.setText(SuntimesUtils.createBoldColorSpan(lightLength_label, lightLength, color_textTimeDelta));

            SuntimesUtils.TimeDisplayText lightLengthDisplay2 = utils.timeDeltaLongDisplayString(0, dataset.dataCivil.dayLengthOther(), showSeconds);
            lightLengthDisplay2.setSuffix("");
            String lightLength2 = lightLengthDisplay2.toString();
            String lightLength2_label = getString(R.string.length_light, lightLength2);
            txt_lightlength2.setText(SuntimesUtils.createBoldColorSpan(lightLength2_label, lightLength2, color_textTimeDelta));

        } else {
            String notCalculated = getString(R.string.time_loading);
            txt_sunrise_actual.setText(notCalculated);
            txt_sunrise_civil.setText(notCalculated);
            txt_sunrise_nautical.setText(notCalculated);
            txt_sunrise_astro.setText(notCalculated);
            txt_solarnoon.setText(notCalculated);
            txt_sunset_actual.setText(notCalculated);
            txt_sunset_civil.setText(notCalculated);
            txt_sunset_nautical.setText(notCalculated);
            txt_sunset_astro.setText(notCalculated);

            row_gold.updateFields(notCalculated, notCalculated);
            row_blue8.updateFields(notCalculated, notCalculated);
            row_blue4.updateFields(notCalculated, notCalculated);

            txt_sunrise2_actual.setText(notCalculated);
            txt_sunrise2_civil.setText(notCalculated);
            txt_sunrise2_nautical.setText(notCalculated);
            txt_sunrise2_astro.setText(notCalculated);
            txt_solarnoon2.setText(notCalculated);
            txt_sunset2_actual.setText(notCalculated);
            txt_sunset2_civil.setText(notCalculated);
            txt_sunset2_nautical.setText(notCalculated);
            txt_sunset2_astro.setText(notCalculated);

            row_gold2.updateFields(notCalculated, notCalculated);
            row_blue8_2.updateFields(notCalculated, notCalculated);
            row_blue4_2.updateFields(notCalculated, notCalculated);
        }

        //
        // equinox and solstice
        //
        boolean enableEquinox = AppSettings.loadShowEquinoxPref(this);
        showEquinoxView(enableEquinox && dataset2 != null && dataset2.isImplemented());
        card_equinoxSolstice.setTrackingMode(WidgetSettings.loadTrackingModePref(context, AppWidgetManager.INVALID_APPWIDGET_ID));
        card_equinoxSolstice.updateViews(SuntimesActivity.this, dataset2);

        //
        // clock & date
        //
        Calendar now = dataset.now();
        Date data_date = dataset.dataActual.date();
        Date data_date2 = dataset.dataActual.dateOther();

        //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());       // 4/11/2016
        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());   // Apr 11, 2016
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

        String date2String = getString(R.string.dateField, otherString, dateFormat.format(data_date2));
        SpannableStringBuilder date2Span = SuntimesUtils.createSpan(this, date2String, SuntimesUtils.SPANTAG_WARNING, dateWarningIcon);
        txt_date2.setText(date2Span);

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

        // datasource ui
        if (txt_datasource != null)
        {
            txt_datasource.setText(dataset.dataActual.calculator().name());
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

    private void showWarnings()
    {
        if (showWarnings && timezoneWarning.shouldShow && !timezoneWarning.wasDismissed)
        {
            timezoneWarning.initWarning(this, getString(R.string.timezoneWarning));
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
            dateWarning.initWarning(this, getString(R.string.dateWarning));
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
            SuntimesUtils.announceForAccessibility(card_flipper, txt_date2.getText().toString());
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
            SuntimesUtils.announceForAccessibility(card_flipper, txt_date.getText().toString());
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
        LightMapDialog lightMapDialog = new LightMapDialog();
        lightMapDialog.setData(dataset);
        lightMapDialog.show(getSupportFragmentManager(), DIALOGTAG_LIGHTMAP);
    }

    protected void showEquinoxView( boolean value )
    {
        equinoxLayout.setVisibility((value ? View.VISIBLE : View.GONE ));
    }

    protected void showEquinoxDialog()
    {
        EquinoxDialog equinoxDialog = new EquinoxDialog();
        equinoxDialog.setData(dataset2);
        equinoxDialog.show(getSupportFragmentManager(), DIALOGTAG_EQUINOX);
    }

    protected void showBlueTimes( boolean value )
    {
        row_blue8.setVisible(value);
        row_blue8_2.setVisible(value);
        row_blue4.setVisible(value);
        row_blue4_2.setVisible(value);
    }

    protected void showGoldTimes( boolean value )
    {
        row_gold.setVisible(value);
        row_gold2.setVisible(value);
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
                switch (action)
                {
                    case NOTHING:
                        break;

                    case CONFIG_DATE:
                        configDate();
                        break;

                    case SHOW_CALENDAR:
                        showCalendar();
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
        };
    }

    private void initTimeFields()
    {
        /**for (SolarEvents.SolarEventField key : timeFields.keySet())
        {
            TextView field = timeFields.get(key);
            field.setOnClickListener(createTimeFieldClickListener(key));
        }*/
    }

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
            TextView txtField = timeFields.get(field);
            if (txtField != null)
            {
                if (field.equals(highlightField))
                {
                    txtField.setTypeface(txtField.getTypeface(), Typeface.BOLD);
                    txtField.setPaintFlags(txtField.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                    if (currentCard == 0 && field.tomorrow)
                    {
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
    private void showCalendar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            long startMillis = dataset.now().getTimeInMillis();
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
        int iconHeight = ((note.noteIconResource == R.drawable.ic_noon_large) ? iconWidth : (int)resources.getDimension(R.dimen.sunIconLarge_height));

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
        public static final String KEY_WASDISMISSED = "userDismissedWarning";

        public SuntimesWarning(String id) { this.id = id; }
        protected String id = "";
        protected Snackbar snackbar = null;
        protected boolean shouldShow = false;
        protected boolean wasDismissed = false;

        public void initWarning(Context context, String msg)
        {
            ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, txt_date.getTextSize());
            SpannableStringBuilder message = SuntimesUtils.createSpan(SuntimesActivity.this, msg, SuntimesUtils.SPANTAG_WARNING, warningIcon);

            wasDismissed = false;
            snackbar = Snackbar.make(card_flipper, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.addCallback(snackbarListener);
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
