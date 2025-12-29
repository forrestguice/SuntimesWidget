/**
    Copyright (C) 2014-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollectionPreference;
import com.forrestguice.suntimeswidget.events.DayPercentEvent;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventListActivity;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.MoonElevationEvent;
import com.forrestguice.suntimeswidget.events.ShadowLengthEvent;
import com.forrestguice.suntimeswidget.events.SunElevationEvent;
import com.forrestguice.suntimeswidget.settings.ActionButtonPreference;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.LengthPreference;
import com.forrestguice.suntimeswidget.settings.SettingsActivityInterface;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.suntimeswidget.views.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_CLOCKTAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_DATETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_DATETAPACTION1;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_NOTETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_APPEARANCE_THEME_LIGHT;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_CLOCKTAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_DATETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_DATETAPACTION1;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_NOTETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.THEME_DEFAULT;

/**
 * User Interface Prefs
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UIPrefsFragment extends PreferenceFragment
{
    public static final String LOG_TAG = "SuntimesSettings";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppSettings.initLocale(getActivity());
        Log.i(SuntimesSettingsActivity.LOG_TAG, "UIPrefsFragment: Arguments: " + getArguments());

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_userinterface, false);
        addPreferencesFromResource(R.xml.preference_userinterface);

        initPref_ui(UIPrefsFragment.this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_ui(final PreferenceFragment fragment)
    {
        boolean[] showFields = AppSettings.loadShowFieldsPref(fragment.getActivity());
        for (int i = 0; i<AppSettings.NUM_FIELDS; i++)
        {
            CheckBoxPreference field = (CheckBoxPreference)fragment.findPreference(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + i);
            if (field != null) {
                initPref_ui_field(field, fragment.getActivity(), i, showFields[i]);
            }
        }

        Activity activity = fragment.getActivity();

        final ActionButtonPreference tapAction_clock = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_CLOCKTAPACTION);
        initPref_ui_tapAction(activity, tapAction_clock, PREF_KEY_UI_CLOCKTAPACTION);
        loadPref_ui_tapAction(activity, tapAction_clock, PREF_KEY_UI_CLOCKTAPACTION, PREF_DEF_UI_CLOCKTAPACTION, SettingsActivityInterface.REQUEST_TAPACTION_CLOCK);

        final ActionButtonPreference tapAction_date0 = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_DATETAPACTION);
        initPref_ui_tapAction(activity, tapAction_date0, PREF_KEY_UI_DATETAPACTION);
        loadPref_ui_tapAction(activity, tapAction_date0, PREF_KEY_UI_DATETAPACTION, PREF_DEF_UI_DATETAPACTION, SettingsActivityInterface.REQUEST_TAPACTION_DATE0);

        final ActionButtonPreference tapAction_date1 = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_DATETAPACTION1);
        initPref_ui_tapAction(activity, tapAction_date1, PREF_KEY_UI_DATETAPACTION1);
        loadPref_ui_tapAction(activity, tapAction_date1, PREF_KEY_UI_DATETAPACTION1, PREF_DEF_UI_DATETAPACTION1, SettingsActivityInterface.REQUEST_TAPACTION_DATE1);

        final ActionButtonPreference tapAction_note = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_NOTETAPACTION);
        initPref_ui_tapAction(activity, tapAction_note,  PREF_KEY_UI_NOTETAPACTION);
        loadPref_ui_tapAction(activity, tapAction_note,  PREF_KEY_UI_NOTETAPACTION, PREF_DEF_UI_NOTETAPACTION, SettingsActivityInterface.REQUEST_TAPACTION_NOTE);

        final ActionButtonPreference overrideTheme_light = (ActionButtonPreference)fragment.findPreference(PREF_KEY_APPEARANCE_THEME_LIGHT);
        initPref_ui_themeOverride(activity, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);
        loadPref_ui_themeOverride(activity, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);

        final ActionButtonPreference overrideTheme_dark = (ActionButtonPreference)fragment.findPreference(AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        initPref_ui_themeOverride(activity, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        loadPref_ui_themeOverride(activity, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);

        String key_observerHeight = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_GENERAL + WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT;
        LengthPreference observerHeightPref = (LengthPreference) fragment.findPreference(key_observerHeight);
        if (observerHeightPref != null)
        {
            initPref_observerHeight(fragment.getActivity(), observerHeightPref);
            loadPref_observerHeight(fragment.getActivity(), observerHeightPref);
        }

        Preference manage_events = fragment.findPreference("manage_events");
        if (manage_events != null) {
            manage_events.setOnPreferenceClickListener(getOnManageEventsClickedListener(fragment.getActivity()));
            manage_events.setOrder(-91);
        }

        Preference navigation = fragment.findPreference("app_navigation_mode");
        if (navigation != null) {
            navigation.setOnPreferenceChangeListener(onNavigationChanged(fragment.getActivity(), navigation));
        }

        PreferenceCategory category = (PreferenceCategory) fragment.findPreference("custom_events");
        initPref_ui_customevents((SuntimesSettingsActivity) activity, category);

        updatePref_ui_themeOverride(AppSettings.loadThemePref(activity), overrideTheme_dark, overrideTheme_light);
    }

    public static void initPref_ui_customevents(final SuntimesSettingsActivity context, final PreferenceCategory category)
    {
        ArrayList<Preference> eventPrefs = new ArrayList<>();

        Set<String> eventIDs = EventSettings.loadVisibleEvents(AndroidEventSettings.wrap(context));
        for (final String eventID : eventIDs)
        {
            EventAlias alias = EventSettings.loadEvent(AndroidEventSettings.wrap(context), eventID);

            final CheckBoxPreference pref = new CheckBoxPreference(context);
            pref.setKey(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + eventID);
            pref.setTitle(alias.getLabel());
            pref.setSummary(alias.getSummary(context));
            pref.setPersistent(false);
            pref.setChecked(true);

            switch (alias.getType())
            {
                case MOONILLUM:
                    //MoonIllumEvent illumEvent = MoonIllumEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());
                    pref.setOrder(0);
                    break;

                case DAYPERCENT:
                    DayPercentEvent percentEvent = DayPercentEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());
                    pref.setOrder((percentEvent != null ? (int)percentEvent.getAngle() : 0));
                    break;

                case MOON_ELEVATION:
                    MoonElevationEvent moonEvent = MoonElevationEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());
                    pref.setOrder((moonEvent != null ? (int) moonEvent.getAngle() : 0));
                    break;

                case SUN_ELEVATION:
                    SunElevationEvent elevationEvent = SunElevationEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());
                    pref.setOrder((elevationEvent != null ? (int)elevationEvent.getAngle() : 0));
                    break;

                case SHADOWLENGTH:
                    ShadowLengthEvent shadowEvent = ShadowLengthEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());
                    pref.setOrder((shadowEvent != null ? 1000 + (int)shadowEvent.getLength() : 1000));
                    break;
            }

            pref.setOnPreferenceChangeListener(customEventListener(context, eventID, category, pref));
            eventPrefs.add(pref);
        }

        boolean sortByName = false;    // TODO: optional
        Collections.sort(eventPrefs, new Comparator<Preference>() {
            @Override
            public int compare(Preference o1, Preference o2) {
                return o1.getTitle().toString().compareTo(o2.getTitle().toString());
            }
        });
        for (int i=0; i<eventPrefs.size(); i++) {
            Preference p = eventPrefs.get(i);
            if (sortByName) {
                p.setOrder(i+1);
            }
            category.addPreference(p);
        }
    }

    protected static Preference.OnPreferenceChangeListener customEventListener(final SuntimesSettingsActivity context, final String eventID, final PreferenceCategory category, final CheckBoxPreference pref)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                if (!checked)
                {
                    AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setMessage(context.getString(R.string.editevent_dialog_showevent_off))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    EventSettings.setShown(AndroidEventSettings.wrap(context), eventID, false);
                                    category.removePreference(pref);
                                    context.setNeedsRecreateFlag();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pref.setChecked(true);
                                }
                            });
                    confirm.show();
                }
                return true;
            }
        };
    }

    protected static Preference.OnPreferenceChangeListener onNavigationChanged(final Context context, final Preference pref)
    {
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(context, context.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

    public static void initPref_ui_field(CheckBoxPreference field, final Context context, final int k, boolean value)
    {
        field.setChecked(value);
        field.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                if (context != null) {
                    AppSettings.saveShowFieldsPref(context, k, (Boolean) o);
                    return true;
                } else return false;
            }
        });
    }

    public static Preference.OnPreferenceClickListener getOnManageEventsClickedListener(final Activity activity)
    {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                activity.startActivityForResult(new Intent(activity, EventListActivity.class), SettingsActivityInterface.REQUEST_MANAGE_EVENTS);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                return false;
            }
        };
    }

    private static Preference.OnPreferenceChangeListener onOverrideThemeChanged(final Activity activity, final ActionButtonPreference overridePref, final int requestCode)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //overridePref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, (String)newValue, requestCode));
                Toast.makeText(activity, activity.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

    @Deprecated
    private static ActionButtonPreference.ActionButtonPreferenceListener createThemeListPreferenceListener(final Activity activity, final String selectedTheme, final int requestCode)
    {
        return new ActionButtonPreference.ActionButtonPreferenceListener()
        {
            @Override
            public void onActionButtonClicked()
            {
                Intent configThemesIntent = new Intent(activity, WidgetThemeListActivity.class);
                configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_NOSELECT, false);
                configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_SELECTED, selectedTheme);
                activity.startActivityForResult(configThemesIntent, requestCode);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
            }
        };
    }

    private static ActionButtonPreference.ActionButtonPreferenceListener createThemeListPreferenceListener(final Activity activity, final ColorValuesCollection<?> collection, final int requestCode, final int appWidgetID, final String colorTag, final CharSequence title, final CharSequence subtitle, final boolean showAlpha, @Nullable final Integer previewMode, final String[] previewKeys)
    {
        return new ActionButtonPreference.ActionButtonPreferenceListener ()
        {
            @Override
            public void onActionButtonClicked()
            {
                if (activity != null)
                {
                    Intent intent = ColorValuesCollectionPreference.createPreferenceOnClickIntent(activity, collection, appWidgetID, colorTag, title, showAlpha, previewMode, previewKeys, null);
                    activity.startActivityForResult(intent, requestCode);
                    activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                }
            }
        };
    }

    public static void initPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key) {
        initPref_ui_themeOverride(activity, listPref, key, null);
    }
    private static void initPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key, @Nullable String mustIncludeTheme)
    {
        if (listPref != null)
        {
            AppSettings.AppThemeInfo themeInfo = AppSettings.loadThemeInfo(activity);

            boolean isLightThemePref = key.equals(PREF_KEY_APPEARANCE_THEME_LIGHT);
            String[] defaultEntries = activity.getResources().getStringArray(isLightThemePref ? R.array.appThemes_light_display : R.array.appThemes_dark_display);
            String[] defaultValues = activity.getResources().getStringArray(isLightThemePref ? R.array.appThemes_light_values : R.array.appThemes_dark_values);
            HashMap<String,String> defaults = new HashMap<>();
            for (int i=0; i<defaultEntries.length; i++)
            {
                if (defaultValues[i].equals(AppSettings.THEME_DEFAULT) || themeInfo.getDefaultNightMode() == AppSettings.loadThemeInfo(defaultValues[i]).getDefaultNightMode()) {
                    defaults.put(defaultValues[i], defaultEntries[i]);
                }
            }

            /*WidgetThemes.initThemes(activity);
            List<SuntimesTheme.ThemeDescriptor> themes0 = WidgetThemes.getSortedValues(true);
            ArrayList<SuntimesTheme.ThemeDescriptor> themes = new ArrayList<>();
            for (SuntimesTheme.ThemeDescriptor theme : themes0)
            {
                //if (!theme.isDefault() || theme.name().equals(mustIncludeTheme)) {
                if (theme.name().equals(mustIncludeTheme)) {
                    themes.add(theme);    // hide default widget themes, show only user-created themes
                }                            // this is a workaround - the defaults have tiny (unreadable) font sizes, so we won't advertise their use
            }

            String[] themeEntries = new String[themes.size() + defaults.size()];
            String[] themeValues = new String[themes.size() + defaults.size()];*/

            String[] themeEntries = new String[defaults.size()];
            String[] themeValues = new String[defaults.size()];

            Set<String> keyset = defaults.keySet();
            themeValues[0] = THEME_DEFAULT;
            themeEntries[0] = defaults.get(THEME_DEFAULT);
            keyset.remove(THEME_DEFAULT);

            int j = 1;
            for (String k : keyset) {
                themeValues[j] = k;
                themeEntries[j] = defaults.get(k);
                j++;
            }
            /*for (SuntimesTheme.ThemeDescriptor theme : themes) {
                themeValues[j] = theme.name();
                themeEntries[j] = theme.displayString();
                j++;
            }*/

            listPref.setEntries(themeEntries);
            listPref.setEntryValues(themeValues);
        }
    }

    public static void loadPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key)
    {
        if (listPref != null)
        {
            boolean isLightTheme = key.equals(PREF_KEY_APPEARANCE_THEME_LIGHT);
            String themeName = ((isLightTheme ? AppSettings.loadThemeLightPref(activity) : AppSettings.loadThemeDarkPref(activity)));
            int requestCode = (isLightTheme ? SettingsActivityInterface.REQUEST_PICKCOLORS_LIGHT : SettingsActivityInterface.REQUEST_PICKCOLORS_DARK);
            AppColorValuesCollection<?> colorCollection = new AppColorValuesCollection<>(activity);
            String colorsID = colorCollection.getSelectedColorsID(activity, (isLightTheme ? 0 : 1), AppColorValues.TAG_APPCOLORS);

            int currentIndex = ((themeName != null) ? listPref.findIndexOfValue(themeName) : -1);
            if (currentIndex >= 0)
            {
                listPref.setValueIndex(currentIndex);
                listPref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, colorCollection, requestCode, (isLightTheme ? 0 : 1), AppColorValues.TAG_APPCOLORS, null, listPref.getTitle(), true, null, null));
                listPref.setOnPreferenceChangeListener(onOverrideThemeChanged(activity, listPref, requestCode));

            } else {
                if (WidgetThemes.valueOf(themeName) != null) {    // the theme exists but is missing from the list; reload the adapter
                    initPref_ui_themeOverride(activity, listPref, key, themeName);   // it mustInclude: themeName
                    loadPref_ui_themeOverride(activity, listPref, key);    // !! potential for recursive loop if initPref_ui_themeOverride fails to include themeName

                } else {
                    Log.w(LOG_TAG, "loadPref: Unable to load " + key + "... The list is missing an entry for the descriptor: " + themeName);
                    listPref.setValueIndex(0);
                    listPref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, colorCollection, requestCode, (isLightTheme ? 0 : 1), AppColorValues.TAG_APPCOLORS, null, null, true, null, null));
                    listPref.setOnPreferenceChangeListener(onOverrideThemeChanged(activity, listPref, requestCode));
                }
            }

            if (!colorCollection.isDefaultColorID(colorsID)) {
                String label = colorCollection.getColorsLabel(activity, colorsID);
                listPref.setSummary(listPref.getEntry() + "\n" + (label != null ? label : colorsID));
            }
        }
    }

    public static void updatePref_ui_themeOverride(@NonNull String mode, ListPreference darkPref, ListPreference lightPref)
    {
        AppSettings.AppThemeInfo themeInfo = AppSettings.loadThemeInfo(mode);
        String themeName = themeInfo.getThemeName();
        int themeNightMode = themeInfo.getDefaultNightMode();
        darkPref.setEnabled(themeNightMode == AppCompatDelegate.MODE_NIGHT_YES || themeNightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || themeName.equals(AppSettings.THEME_DAYNIGHT));
        lightPref.setEnabled(themeNightMode == AppCompatDelegate.MODE_NIGHT_NO || themeNightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || themeName.equals(AppSettings.THEME_DAYNIGHT));
    }

    /**
     * initPref_ui_tapAction
     */
    private static void initPref_ui_tapAction(Activity activity, ActionButtonPreference listPref, String key)
    {
        if (listPref != null)
        {
            CharSequence[] entries0 = listPref.getEntries();
            CharSequence[] values0 = listPref.getEntryValues();
            String actionID = PreferenceManager.getDefaultSharedPreferences(activity).getString(key, null);

            boolean hasValue = false;
            if (actionID != null)
            {
                for (CharSequence value : values0) {
                    if (value.equals(actionID)) {
                        hasValue = true;
                        break;
                    }
                }
            }

            if (!hasValue)
            {
                if (actionID != null && WidgetActions.hasActionLaunchPref(activity, 0, actionID))
                {
                    String title = WidgetActions.loadActionLaunchPref(activity, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
                    String desc = WidgetActions.loadActionLaunchPref(activity, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
                    String display = (desc != null && !desc.trim().isEmpty() ? desc : title);

                    CharSequence[] entries1 = new String[entries0.length + 1];
                    System.arraycopy(entries0, 0, entries1, 0, entries0.length);
                    entries1[entries0.length] = display;

                    CharSequence[] values1 = new String[values0.length + 1];
                    System.arraycopy(values0, 0, values1, 0, values0.length);
                    values1[values0.length] = actionID;

                    listPref.setEntries(entries1);
                    listPref.setEntryValues(values1);
                }
            }
        }
    }

    private static void loadPref_ui_tapAction(Activity activity, ActionButtonPreference listPref, String key, String defaultValue, final int requestCode)
    {
        if (listPref != null)
        {
            String actionID = PreferenceManager.getDefaultSharedPreferences(activity).getString(key, defaultValue);
            listPref.setActionButtonPreferenceListener(createTapActionListPreferenceListener(activity, actionID, requestCode));
            listPref.setOnPreferenceChangeListener(onTapActionChanged(activity, listPref, requestCode));

            int currentIndex = ((actionID != null) ? listPref.findIndexOfValue(actionID) : -1);
            if (currentIndex >= 0) {
                listPref.setValueIndex(currentIndex);
            } else {
                Log.w(LOG_TAG, "loadPref: Unable to load " + key + "... The list is missing an entry for the descriptor: " + actionID);
                listPref.setValueIndex(0);
            }
        }
    }

    private static ActionButtonPreference.ActionButtonPreferenceListener createTapActionListPreferenceListener(final Activity activity, final String selectedActionID, final int requestCode)
    {
        return new ActionButtonPreference.ActionButtonPreferenceListener()
        {
            @Override
            public void onActionButtonClicked()
            {
                Intent intent = new Intent(activity, ActionListActivity.class);
                intent.putExtra(ActionListActivity.PARAM_NOSELECT, false);
                intent.putExtra(ActionListActivity.PARAM_SELECTED, selectedActionID);
                activity.startActivityForResult(intent, requestCode);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
            }
        };
    }

    private static Preference.OnPreferenceChangeListener onTapActionChanged(final Activity activity, final ActionButtonPreference pref, final int requestCode)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                pref.setActionButtonPreferenceListener(createTapActionListPreferenceListener(activity, (String)newValue, requestCode));
                return true;
            }
        };
    }

    public static void initPref_observerHeight(final Activity context, final LengthPreference pref)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionShadow});
        int drawableID = a.getResourceId(0, R.drawable.ic_action_shadow);
        a.recycle();

        String title = context.getString(R.string.configLabel_general_observerheight) + " [i]";
        int iconSize = (int) context.getResources().getDimension(R.dimen.prefIcon_size);
        ImageSpan shadowIcon = SuntimesUtils.createImageSpan(context, drawableID, iconSize, iconSize, 0);
        SpannableStringBuilder titleSpan = SuntimesUtils.createSpan(context, title, "[i]", shadowIcon);
        pref.setTitle(titleSpan);

        LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
        pref.setMetric(units == LengthUnit.METRIC);
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                try {
                    double doubleValue = Double.parseDouble((String)newValue);
                    if (doubleValue > 0)
                    {
                        LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
                        preference.setSummary(formatObserverHeightSummary(preference.getContext(), doubleValue, units, false));
                        return true;

                    } else return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }
    public static void loadPref_observerHeight(Context context, final LengthPreference pref)
    {
        final LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
        double observerHeight = WidgetSettings.loadObserverHeightPref(context, 0);
        pref.setText((pref.isMetric() ? observerHeight : LengthUnit.metersToFeet(observerHeight)) + "");
        pref.setSummary(formatObserverHeightSummary(context, observerHeight, units, true));
    }
    private static CharSequence formatObserverHeightSummary(Context context, double observerHeight, LengthUnit units, boolean convert)
    {
        String observerHeightDisplay = SuntimesUtils.formatAsHeight(context, observerHeight, units, convert, 2);
        return context.getString(R.string.configLabel_general_observerheight_summary, observerHeightDisplay);
    }

}
