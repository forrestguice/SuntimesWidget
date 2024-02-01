/**
    Copyright (C) 2019-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWidget0;
import com.forrestguice.suntimeswidget.SuntimesWidgetListActivity;
import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.actions.SuntimesActionsContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.TAG_DEFAULT;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.TAG_SUNTIMES;
import static com.forrestguice.suntimeswidget.actions.SuntimesActionsContract.TAG_SUNTIMESALARMS;

/**
 * WidgetActions
 */
public class WidgetActions
{
    public static final String TAG = "WidgetActions";

    public static final String PREFS_WIDGETS = WidgetSettings.PREFS_WIDGET;                  // Widget related actions are stored with WidgetSettings (where the actionID is either null or 0),
    public static final String PREFS_ACTIONS = "com.forrestguice.suntimeswidget.actions";    // while the action collection is stored in separate .actions file.

    public static final String PREF_PREFIX_KEY = WidgetSettings.PREF_PREFIX_KEY;
    public static final String PREF_PREFIX_KEY_ACTION = "_action_";

    public static final String PREF_KEY_ACTION_LAUNCH_TITLE = SuntimesActionsContract.COLUMN_ACTION_TITLE;
    public static String PREF_DEF_ACTION_LAUNCH_TITLE = "Suntimes";

    public static final String PREF_KEY_ACTION_LAUNCH_DESC = SuntimesActionsContract.COLUMN_ACTION_DESC;
    public static String PREF_DEF_ACTION_LAUNCH_DESC = "";

    public static final String PREF_KEY_ACTION_LAUNCH_COLOR = SuntimesActionsContract.COLUMN_ACTION_COLOR;
    public static int PREF_DEF_ACTION_LAUNCH_COLOR = Color.WHITE;

    public static final String PREF_KEY_ACTION_LAUNCH_TAGS = SuntimesActionsContract.COLUMN_ACTION_TAGS;

    public static final String PREF_KEY_ACTION_LAUNCH = SuntimesActionsContract.COLUMN_ACTION_CLASS;
    public static final String PREF_DEF_ACTION_LAUNCH = SuntimesActivity.class.getName();

    public static final String PREF_KEY_ACTION_LAUNCH_PACKAGE = SuntimesActionsContract.COLUMN_ACTION_PACKAGE;
    public static final String PREF_DEF_ACTION_LAUNCH_PACKAGE = "";

    public static final String PREF_KEY_ACTION_LAUNCH_ACTION = SuntimesActionsContract.COLUMN_ACTION_ACTION;
    public static final String PREF_DEF_ACTION_LAUNCH_ACTION = "";

    public static final String PREF_KEY_ACTION_LAUNCH_EXTRAS = SuntimesActionsContract.COLUMN_ACTION_EXTRAS;
    public static final String PREF_DEF_ACTION_LAUNCH_EXTRAS = "";

    public static final String PREF_KEY_ACTION_LAUNCH_DATA = SuntimesActionsContract.COLUMN_ACTION_DATA;
    public static final String PREF_DEF_ACTION_LAUNCH_DATA = "";

    public static final String PREF_KEY_ACTION_LAUNCH_DATATYPE = SuntimesActionsContract.COLUMN_ACTION_MIMETYPE;
    public static final String PREF_DEF_ACTION_LAUNCH_DATATYPE = "";

    public static final String PREF_KEY_ACTION_LAUNCH_TYPE = SuntimesActionsContract.COLUMN_ACTION_TYPE;
    public static final LaunchType PREF_DEF_ACTION_LAUNCH_TYPE = LaunchType.ACTIVITY;

    public static final String PREF_KEY_ACTION_LAUNCH_LIST = "list";
    public static final String PREF_KEY_ACTION_LAUNCH_ID = "id";
    public static final String PREF_KEY_ACTION_LAUNCH_APPWIDGETID = "appWidgetId";

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[]
    {
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_TITLE,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_DESC,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_COLOR,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_TAGS,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_PACKAGE,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_ACTION,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_EXTRAS,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_DATA,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_DATATYPE,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_TYPE,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_LIST,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_ID,
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_APPWIDGETID,
    };

    public static final String[] INT_KEYS = new String[] {
            PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_0_" + PREF_KEY_ACTION_LAUNCH_COLOR,
    };

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            for (String key : INT_KEYS) {
                types.put(key, Integer.class);
            }
            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * LaunchType
     */
    public static enum LaunchType
    {
        ACTIVITY("Activity"),
        BROADCAST("Broadcast"),
        SERVICE("Service");

        private LaunchType(String displayString)
        {
            this.displayString = displayString;
        }

        private String displayString;
        public String getDisplayString()
        {
            return displayString;
        }
        public void setDisplayString(String value)
        {
            displayString = value;
        }
        public static void initDisplayStrings(Context context)
        {
            ACTIVITY.setDisplayString(context.getString(R.string.launchType_activity));
            BROADCAST.setDisplayString(context.getString(R.string.launchType_broadcast));
            SERVICE.setDisplayString(context.getString(R.string.launchType_service));
        }
        public String toString()
        {
            return displayString;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveActionLaunchPref(Context context, @Nullable String titleString, @Nullable String descString, @Nullable Integer color, @Nullable String[] tags, int appWidgetId, @Nullable String id, @Nullable String launchString, @Nullable String packageName, @Nullable String type, @Nullable String action, @Nullable String dataString, @Nullable String mimeType, @Nullable String extrasString) {
        saveActionLaunchPref(context, titleString, descString, color, tags, appWidgetId, id, launchString, packageName, type, action, dataString, mimeType, extrasString, true);
    }

    public static void saveActionLaunchPref(Context context, @Nullable String titleString, @Nullable String descString, @Nullable Integer color, @Nullable String[] tags, int appWidgetId, @Nullable String id, @Nullable String launchString, @Nullable String packageName, @Nullable String type, @Nullable String action, @Nullable String dataString, @Nullable String mimeType, @Nullable String extrasString, boolean listed)
    {
        boolean hasID = true;
        if (id == null) {
            id = "0";
            hasID = false;
        }
        if (action != null && action.trim().isEmpty()) {
            action = null;
        }
        if (dataString != null && dataString.trim().isEmpty()) {
            dataString = null;
        }
        if (mimeType != null && mimeType.trim().isEmpty()) {
            mimeType = null;
        }
        if (extrasString != null && extrasString.trim().isEmpty()) {
            extrasString = null;
        }

        SharedPreferences.Editor prefs = context.getSharedPreferences(getPrefsId(appWidgetId, id), 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";

        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH, (launchString != null ? launchString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_PACKAGE, (packageName != null ? packageName : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TYPE, (type != null ? type : SuntimesActionsContract.TYPE_ACTIVITY));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_ACTION, (action != null ? action : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATA, (dataString != null ? dataString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATATYPE, (mimeType != null ? mimeType : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_EXTRAS, (extrasString != null ? extrasString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TITLE, (titleString != null ? titleString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DESC, (descString != null ? descString : ""));
        prefs.putInt(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_COLOR, (color != null ? color : PREF_DEF_ACTION_LAUNCH_COLOR));

        Set<String> tagSet = new TreeSet<>();
        if (tags != null) {
            for (String tag : tags) {
                if (!tagSet.contains(tag)) {
                    tagSet.add(tag);
                }
            }
        }
        putStringSet(prefs, prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TAGS, tagSet);

        prefs.apply();

        if (hasID && listed)
        {
            Set<String> actionList = loadActionLaunchList(context, 0);
            actionList.add(id);
            putStringSet(prefs, PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST, actionList);
            prefs.apply();
        }
    }

    public static boolean saveActionLaunchPref(Context context, @Nullable ContentValues values, int appWidgetId)
    {
        if (values != null)
        {
            String id = values.getAsString(PREF_KEY_ACTION_LAUNCH_ID);
            if (id != null)
            {
                String tagString = values.getAsString(PREF_KEY_ACTION_LAUNCH_TAGS);
                String[] tags = (tagString != null ? tagString.split("\\|") : new String[0]);

                saveActionLaunchPref(context,
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_TITLE),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_DESC),
                        values.getAsInteger(PREF_KEY_ACTION_LAUNCH_COLOR),
                        tags, appWidgetId, id,
                        values.getAsString(PREF_KEY_ACTION_LAUNCH),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_PACKAGE),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_TYPE),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_ACTION),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_DATA),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_DATATYPE),
                        values.getAsString(PREF_KEY_ACTION_LAUNCH_EXTRAS), true);
                return true;
            }
        }
        return false;
    }

    public static Set<String> loadActionTags(Context context, int appWidgetId, @Nullable String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_ACTIONS, 0);
        String prefs_prefix0 = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";
        Set<String> tagList = getStringSet(prefs, prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TAGS, null);
        return (tagList != null) ? new TreeSet<String>(tagList) : new TreeSet<String>();
    }
    public static Set<String> loadActionLaunchList(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_ACTIONS, 0);
        String listKey = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST;
        Set<String> actionList = getStringSet(prefs, listKey, null);
        return (actionList != null) ? new TreeSet<String>(actionList) : new TreeSet<String>();
    }
    public static ContentValues loadActionLaunchPref(Context context, int appWidgetId, @Nullable String id)
    {
        ContentValues values = new ContentValues();
        values.put(PREF_KEY_ACTION_LAUNCH_ID, id);
        values.put(PREF_KEY_ACTION_LAUNCH, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH));
        values.put(PREF_KEY_ACTION_LAUNCH_PACKAGE, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_PACKAGE));
        values.put(PREF_KEY_ACTION_LAUNCH_TYPE, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_TYPE));
        values.put(PREF_KEY_ACTION_LAUNCH_ACTION, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_ACTION));
        values.put(PREF_KEY_ACTION_LAUNCH_DATA, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_DATA));
        values.put(PREF_KEY_ACTION_LAUNCH_DATATYPE, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_DATATYPE));
        values.put(PREF_KEY_ACTION_LAUNCH_EXTRAS, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_EXTRAS));
        values.put(PREF_KEY_ACTION_LAUNCH_TITLE, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_TITLE));
        values.put(PREF_KEY_ACTION_LAUNCH_DESC, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_DESC));
        values.put(PREF_KEY_ACTION_LAUNCH_COLOR, loadActionLaunchPref(context, appWidgetId, id, PREF_KEY_ACTION_LAUNCH_COLOR));
        values.put(PREF_KEY_ACTION_LAUNCH_TAGS, stringSetToString(loadActionTags(context, appWidgetId, id)));
        return values;
    }
    public static String loadActionLaunchPref(Context context, int appWidgetId, @Nullable String id, @Nullable String key)
    {
        if (id == null) {
            id = "0";
        }

        SharedPreferences prefs = context.getSharedPreferences(getPrefsId(appWidgetId, id), 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";

        if (key == null || key.isEmpty())
        {
            return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH, PREF_DEF_ACTION_LAUNCH);

        } else {
            switch (key)
            {
                case PREF_KEY_ACTION_LAUNCH_PACKAGE:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_PACKAGE, PREF_DEF_ACTION_LAUNCH_PACKAGE);

                case PREF_KEY_ACTION_LAUNCH_TYPE:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_TYPE, SuntimesActionsContract.TYPE_ACTIVITY);

                case PREF_KEY_ACTION_LAUNCH_ACTION:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_ACTION, "");

                case PREF_KEY_ACTION_LAUNCH_DATA:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_DATA, "");

                case PREF_KEY_ACTION_LAUNCH_DATATYPE:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_DATATYPE, "");

                case PREF_KEY_ACTION_LAUNCH_EXTRAS:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_EXTRAS, "");

                case PREF_KEY_ACTION_LAUNCH_TITLE:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_TITLE, PREF_DEF_ACTION_LAUNCH_TITLE);

                case PREF_KEY_ACTION_LAUNCH_DESC:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_DESC, PREF_DEF_ACTION_LAUNCH_DESC);

                case PREF_KEY_ACTION_LAUNCH_COLOR:
                    return Integer.toString(prefs.getInt(prefs_prefix + PREF_KEY_ACTION_LAUNCH_COLOR, PREF_DEF_ACTION_LAUNCH_COLOR));
            }
            return null;
        }
    }
    public static void deleteActionLaunchPref(Context context, int appWidgetId, @Nullable String id)
    {
        if (id == null) {
            id = "0";
        }

        SharedPreferences.Editor prefs = context.getSharedPreferences(getPrefsId(appWidgetId, id), 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH );
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_PACKAGE );
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_ACTION);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATA);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATATYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_EXTRAS);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TITLE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DESC);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_COLOR);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TAGS);
        prefs.apply();

        Set<String> actionList = loadActionLaunchList(context, 0);
        actionList.remove(id);
        putStringSet(prefs, PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST, actionList);
        prefs.commit();
    }
    public static boolean hasActionLaunchPref(Context context, int appWidgetId, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsId(appWidgetId, id), 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";
        return prefs.contains(prefs_prefix + PREF_KEY_ACTION_LAUNCH_TYPE);
    }

    public static String getPrefsId(int appWidgetId, String actionId)
    {
        return (((actionId == null) || actionId.equals("0")) ? PREFS_WIDGETS : PREFS_ACTIONS);
    }

    public static Set<String> getStringSet(SharedPreferences prefs, String key, @Nullable Set<String> defValues)    // TODO: needs test
    {
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getStringSet(key, defValues);

        } else {
            String s = prefs.getString(key, null);
            return (s != null) ? new TreeSet<>(Arrays.asList(s.split("\\|"))) : null;
        }
    }

    public static void putStringSet(SharedPreferences.Editor prefs, String key, @Nullable Set<String> values)  // TODO: needs test
    {
        if (Build.VERSION.SDK_INT >= 11) {
            prefs.putStringSet(key, values);
            prefs.apply();

        } else {
            prefs.putString(key, stringSetToString(values));
            prefs.apply();
        }
    }
    public static String stringSetToString(@Nullable Set<String> values)
    {
        if (values != null) {
            StringBuilder s = new StringBuilder();
            for (String v : values) {
                s.append(v).append("|");
            }
            return s.toString();
        } else {
            return null;
        }
    }

    /**
     * startIntent
     */
    public static void startIntent(@NonNull Context context, int appWidgetId, String id, @Nullable SuntimesData data, @Nullable Class fallbackLaunchClass, @Nullable Integer flags)
    {
        Intent launchIntent = WidgetActions.createIntent(context, appWidgetId, id, data, fallbackLaunchClass);
        if (launchIntent != null)
        {
            if (flags != null) {
                launchIntent.setFlags(flags);
            }
            String launchType = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TYPE);

            try {
                WidgetActions.startIntent(context, launchIntent, launchType);

            } catch (Exception e) {
                Log.e(TAG, "startIntent: unable to start + " + launchType + " :: " + e);
                Toast.makeText(context, context.getString(R.string.startaction_failed_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static void startIntent(@NonNull Context context, @NonNull Intent launchIntent, @Nullable String launchType) throws ActivityNotFoundException, SecurityException
    {
        if (launchType != null)
        {
            Log.i(TAG, "startIntent :: " + launchType + " :: " + launchIntent.toString());
            switch (launchType)
            {
                case SuntimesActionsContract.TYPE_BROADCAST:
                    context.sendBroadcast(launchIntent);
                    break;

                case SuntimesActionsContract.TYPE_SERVICE:
                    context.startService(launchIntent);
                    break;

                case SuntimesActionsContract.TYPE_ACTIVITY:
                default:
                    context.startActivity(launchIntent);
                    break;
            }

        } else {
            Log.i(TAG, "startIntent :: ACTIVITY :: " + launchIntent.toString());
            context.startActivity(launchIntent);
        }
    }

    /**
     * createIntent
     */
    @Nullable
    public static Intent createIntent(Context context, int appWidgetId, String id, @Nullable SuntimesData data, @Nullable Class fallbackLaunchClass)
    {
        Intent launchIntent;
        String launchClassName = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, null);
        String launchPackageName = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        String actionString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String dataString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String mimeType = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String extraString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);

        if (launchClassName != null && !launchClassName.trim().isEmpty())
        {
            if (launchPackageName != null && !launchPackageName.trim().isEmpty())
            {
                launchIntent = new Intent();
                launchIntent.setClassName(launchPackageName, launchClassName);

            } else {
                Class<?> launchClass;
                try {
                    launchClass = Class.forName(launchClassName);       // this only works for activities in our local class path
                    launchIntent = new Intent(context, launchClass);

                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "createIntent :: " + launchClassName + " cannot be found! " + e.toString());
                    if (fallbackLaunchClass != null)
                    {
                        launchClass = fallbackLaunchClass;
                        launchIntent = new Intent(context, launchClass);
                        launchIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);

                    } else {
                        return null;
                    }
                }
            }

        } else {
            launchIntent = new Intent();
        }

        WidgetActions.applyAction(launchIntent, actionString);
        WidgetActions.applyData(context, launchIntent, dataString, mimeType, data);
        WidgetActions.applyExtras(context, launchIntent, extraString, data);
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return launchIntent;
    }

    /**
     * applyAction
     */
    public static void applyAction(Intent intent, @Nullable String action)
    {
        if (intent == null || action == null || action.isEmpty()) {
            return;
        }
        intent.setAction(action);
    }

    /**
     * applyData
     * @param context context
     * @param intent Intent to apply data to
     * @param dataString (optional) data string (may contain %substitutions)
     * @param mimeType (optional) dataString mimeType (text/plain)
     * @param data (optional) supporting SuntimesData to be applied to %substitutions in the dataString
     */
    public static void applyData(Context context, Intent intent, @Nullable String dataString, @Nullable String mimeType, @Nullable SuntimesData data)
    {
        //Log.d(TAG, "applyData: " + dataString + " (" + mimeType + ") [" + data + "] to " + intent);
        if (intent != null && dataString != null && !dataString.trim().isEmpty())
        {
            Uri dataUri = Uri.parse(Uri.decode(displayStringForPattern(context, dataString, data)));
            if (mimeType != null && !mimeType.trim().isEmpty()) {
                intent.setDataAndType(dataUri, mimeType);
            } else intent.setData(dataUri);
        }
    }

    public static void applyExtras(Context context, Intent intent, @Nullable String extraString, @Nullable SuntimesData data)
    {
        if (intent == null || extraString == null || extraString.trim().isEmpty()) {
            return;
        }

        String[] extras = extraString.split("&");
        for (String extra : extras) {
            applyExtra(context, intent, extra, data);
        }
    }

    public static void applyExtra(Context context, Intent intent, String extra, @Nullable SuntimesData data)
    {
        String[] pair = extra.split("=");
        if (pair.length == 2)
        {
            String key = pair[0];
            String value = pair[1];

            char c = value.charAt(0);
            boolean isNumeric = (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'|| c == '8' || c == '9');
            if (isNumeric)
            {
                if (value.endsWith("L") || value.endsWith("l"))
                {
                    try {
                        intent.putExtra(key, Long.parseLong(value.substring(0, value.length()-1)));  // long
                        Log.i(TAG, "applyExtras: applied " + extra + " (long)");

                    } catch (NumberFormatException e) {
                        intent.putExtra(key, value);  // string
                        Log.w(TAG, "applyExtras: fallback " + extra + " (long)");
                    }

                } else if (value.endsWith("D") || value.endsWith("d")) {
                    try {
                        intent.putExtra(key, Double.parseDouble(value));  // double
                        Log.i(TAG, "applyExtras: applied " + extra + " (double)");

                    } catch (NumberFormatException e) {
                        intent.putExtra(key, value);  // string
                        Log.w(TAG, "applyExtras: fallback " + extra + " (double)");
                    }

                } else if (value.endsWith("F") || value.endsWith("f")) {
                    try {
                        intent.putExtra(key, Float.parseFloat(value));  // float
                        Log.i(TAG, "applyExtras: applied " + extra + " (float)");

                    } catch (NumberFormatException e) {
                        intent.putExtra(key, value);  // string
                        Log.w(TAG, "applyExtras: fallback " + extra + " (float)");
                    }

                } else {
                    try {
                        intent.putExtra(key, Integer.parseInt(value));  // int
                        Log.i(TAG, "applyExtras: applied " + extra + " (int)");

                    } catch (NumberFormatException e) {
                        intent.putExtra(key, value);  // string
                        Log.w(TAG, "applyExtras: fallback " + extra + " (int)");
                    }
                }

            } else {
                String lowerCase = value.toLowerCase(Locale.getDefault());
                if (lowerCase.equals("true") || lowerCase.equals("false")) {
                    intent.putExtra(key, lowerCase.equals("true"));  // boolean
                    Log.i(TAG, "applyExtras: applied " + extra + " (boolean)");

                } else {
                    if (value.contains("%"))
                    {
                        String v = displayStringForPattern(context, value, data);
                        if (!v.contains("%")) {
                            applyExtra(context, intent, key + "=" + v, data);    // recursive call

                        } else {
                            intent.putExtra(key, v);
                            Log.w(TAG, "applyExtras: applied " + extra + " (String) (incomplete substitution)");
                        }
                    } else {
                        intent.putExtra(key, value);
                        Log.i(TAG, "applyExtras: applied " + extra + " (String)");
                    }
                }
            }

        } else {
            Log.w(TAG, "applyExtras: skipping " + extra);
        }
    }

    public static String displayStringForPattern(Context context, String pattern, SuntimesData data)
    {
        SuntimesUtils utils = new SuntimesUtils();

        if (data instanceof SuntimesRiseSetData) {    // cast to most specific type                 // TODO: a better way to handle this..
            return utils.displayStringForTitlePattern(context, pattern, (SuntimesRiseSetData) data);

        } else if (data instanceof SuntimesClockData) {
            return utils.displayStringForTitlePattern(context, pattern, (SuntimesClockData) data);

        } else if (data instanceof SuntimesMoonData) {
            return utils.displayStringForTitlePattern(context, pattern, (SuntimesMoonData) data);

        } else if (data instanceof SuntimesEquinoxSolsticeData) {
            return utils.displayStringForTitlePattern(context, pattern, (SuntimesMoonData) data);

        } else {
            return utils.displayStringForTitlePattern(context, pattern, data);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void deletePrefs(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_ACTIONS, 0).edit();
        prefs.clear();
        prefs.apply();
    }

    public static void deletePrefs(Context context, int appWidgetId)
    {
        String[] actionList = loadActionLaunchList(context, 0).toArray(new String[0]);
        for (String action : actionList) {
            deleteActionLaunchPref(context, appWidgetId, action);
        }
        deleteActionLaunchPref(context, appWidgetId, null);
    }

    public static void initDefaults(Context context)
    {
        PREF_DEF_ACTION_LAUNCH_TITLE = context.getString(R.string.app_name);
        PREF_DEF_ACTION_LAUNCH_DESC = context.getString(R.string.app_shortdesc);

        if (!hasActionLaunchPref(context, 0, "SUNTIMES")) {
            saveActionLaunchPref(context, WidgetActions.PREF_DEF_ACTION_LAUNCH_TITLE, WidgetActions.PREF_DEF_ACTION_LAUNCH_DESC, null, new String[] {TAG_DEFAULT, TAG_SUNTIMES}, 0, "def_suntimes", WidgetActions.PREF_DEF_ACTION_LAUNCH, null,
                    WidgetActions.PREF_DEF_ACTION_LAUNCH_TYPE.name(), WidgetActions.PREF_DEF_ACTION_LAUNCH_ACTION, WidgetActions.PREF_DEF_ACTION_LAUNCH_DATA, WidgetActions.PREF_DEF_ACTION_LAUNCH_DATATYPE, WidgetActions.PREF_DEF_ACTION_LAUNCH_EXTRAS);
        }

        SuntimesAction.initDisplayStrings(context);
        SuntimesAction.initDefaults(context);
    }

    public static void initDisplayStrings( Context context ) {
        LaunchType.initDisplayStrings(context);
    }

    /**
     * Actions that can be performed when a UI element is clicked.
     */
    public static enum SuntimesAction
    {
        NOTHING("Nothing", "Do nothing", new String[] {TAG_DEFAULT}, false),

        ALARM("Suntimes", "Set alarm", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),

        CARD_NEXT("Suntimes", "Show next card", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        CARD_PREV("Suntimes", "Show previous card", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        SWAP_CARD("Suntimes", "Swap cards", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        SHOW_CARD("Suntimes", "View date", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),

        NEXT_NOTE("Suntimes", "Show next note", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        PREV_NOTE("Suntimes", "Show previous note", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        RESET_NOTE("Suntimes", "Show upcoming event", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        SEEK_NOTE_SUNRISE("Suntimes", "Show next sunrise", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),
        SEEK_NOTE_SUNSET("Suntimes", "Show next sunset", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, false),

        CONFIG_DATE("Suntimes", "Set date", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        CONFIG_LOCATION("Suntimes", "Set location", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        TIMEZONE("Suntimes", "Set time zone", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),

        SHOW_DIALOG_WORLDMAP("Suntimes", "World Map Dialog", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        SHOW_DIALOG_SOLSTICE("Suntimes", "Solstices Dialog", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        SHOW_DIALOG_MOON("Suntimes", "Moon Dialog", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        SHOW_DIALOG_SUN("Suntimes", "Sun Dialog", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),

        OPEN_ALARM_LIST("Suntimes Alarms", "Alarm List", new String[] {TAG_DEFAULT, TAG_SUNTIMESALARMS}, true),
        OPEN_THEME_LIST("Suntimes", "Theme List", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        OPEN_ACTION_LIST("Suntimes", "Action List", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        OPEN_WIDGET_LIST("Suntimes", "Widget List", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),
        OPEN_SETTINGS("Suntimes", "Settings", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true),

        SHOW_CALENDAR("Calendar", "Show calendar", new String[] {TAG_DEFAULT}, true),
        SHOW_MAP("Map", "Show map", new String[] {TAG_DEFAULT}, true),
        SNOOZE_ALARM("Suntimes Alarms", "Snooze", new String[] {TAG_DEFAULT, TAG_SUNTIMESALARMS}, true),
        DISMISS_ALARM("Suntimes Alarms", "Dismiss", new String[] {TAG_DEFAULT, TAG_SUNTIMESALARMS}, true),
        UPDATE_WIDGETS("Suntimes", "Update widgets", new String[] {TAG_DEFAULT, TAG_SUNTIMES}, true);

        private String title, desc;
        private String[] tags;
        private boolean listed;

        private SuntimesAction(String title, String desc, String[] tags, boolean listed)
        {
            this.title = title;
            this.desc = desc;
            this.tags = tags;
            this.listed = listed;
        }

        public String toString() {
            return (desc != null && !desc.trim().isEmpty()) ? desc : title;
        }

        public String desc() {
            return desc;
        }
        public void setDesc( String desc ) {
            this.desc = desc;
        }

        public String title() {
            return title;
        }
        public void setTitle( String title ) {
            this.title = title;
        }

        public String[] getTags() {
            return tags;
        }

        public boolean listed() {
            return listed;
        }

        public static void initDisplayStrings( Context context )
        {
            SuntimesAction[] actions = SuntimesAction.values();  // TODO
            String[] titles = context.getResources().getStringArray(R.array.tapActions_titles);
            String[] desc = context.getResources().getStringArray(R.array.tapActions_display);
            for (int i=0; i<desc.length; i++)
            {
                if (i < actions.length)
                {
                    actions[i].setTitle(titles[i]);
                    actions[i].setDesc(desc[i]);
                }
            }
        }

        public static void initDefaults( Context context )
        {
            Log.d("DEBUG", "initDefaults");
            for (SuntimesAction action : SuntimesAction.values())
            {
                if (!hasActionLaunchPref(context, 0, action.name()))
                {
                    LaunchType launchType = LaunchType.ACTIVITY;
                    String launchString = SuntimesActivity.class.getName();
                    String launchPackage = null, launchAction = null, launchData = null, launchMime = null, launchExtras = null;

                    switch (action)
                    {
                        case SNOOZE_ALARM:
                            launchString = null;
                            launchAction = AlarmClockActivity.ACTION_SNOOZE_ALARM;
                            launchExtras = AlarmClockActivity.EXTRA_ALARM_SNOOZE_DURATION + "=" + (AlarmSettings.PREF_DEF_ALARM_SNOOZE / (1000 * 60));
                            break;

                        case DISMISS_ALARM:
                            launchString = null;
                            launchAction = AlarmClockActivity.ACTION_DISMISS_ALARM;
                            launchExtras = AlarmClockActivity.EXTRA_ALARM_SEARCH_MODE + "=" + AlarmClockActivity.ALARM_SEARCH_MODE_NEXT;
                            break;

                        case SHOW_CALENDAR:
                            launchString = null;
                            launchAction = Intent.ACTION_VIEW;
                            launchData = "content://com.android.calendar/time/%dm";
                            break;

                        case SHOW_MAP:
                            launchString = null;
                            launchAction = Intent.ACTION_VIEW;
                            launchData = "geo:%lat,%lon";
                            break;

                        case SHOW_DIALOG_SUN: launchAction = SuntimesActivity.ACTION_VIEW_SUN; break;
                        case SHOW_DIALOG_MOON: launchAction = SuntimesActivity.ACTION_VIEW_MOON; break;
                        case SHOW_DIALOG_SOLSTICE: launchAction = SuntimesActivity.ACTION_VIEW_SOLSTICE; break;
                        case SHOW_DIALOG_WORLDMAP: launchAction = SuntimesActivity.ACTION_VIEW_WORLDMAP; break;

                        case OPEN_ACTION_LIST:
                            launchExtras = ActionListActivity.PARAM_NOSELECT + "=true";
                            launchString = ActionListActivity.class.getName(); break;

                        case OPEN_THEME_LIST:
                            launchExtras = WidgetThemeListActivity.PARAM_NOSELECT + "=true";
                            launchString = WidgetThemeListActivity.class.getName();
                            break;

                        case OPEN_ALARM_LIST: launchString = AlarmClockActivity.class.getName(); break;
                        case OPEN_WIDGET_LIST: launchString = SuntimesWidgetListActivity.class.getName(); break;
                        case OPEN_SETTINGS: launchString = SuntimesSettingsActivity.class.getName(); break;

                        case UPDATE_WIDGETS:
                            launchString = null;
                            launchAction = SuntimesWidget0.SUNTIMES_ALARM_UPDATE;
                            launchType = LaunchType.BROADCAST;
                            break;

                        case CONFIG_DATE: launchAction = SuntimesActivity.ACTION_CONFIG_DATE; break;
                        case CONFIG_LOCATION: launchAction = SuntimesActivity.ACTION_CONFIG_LOCATION; break;
                        case TIMEZONE: launchAction = SuntimesActivity.ACTION_CONFIG_TIMEZONE; break;

                        case ALARM: launchAction = SuntimesActivity.ACTION_ADD_ALARM; break;
                        case NEXT_NOTE: launchAction = SuntimesActivity.ACTION_NOTE_NEXT; break;
                        case PREV_NOTE: launchAction = SuntimesActivity.ACTION_NOTE_PREV; break;
                        case RESET_NOTE: launchAction = SuntimesActivity.ACTION_NOTE_RESET; break;
                        case SEEK_NOTE_SUNRISE: launchAction = SuntimesActivity.ACTION_NOTE_SEEK;
                            launchExtras = SuntimesActivity.EXTRA_SOLAREVENT + "=" + SolarEvents.SUNRISE.name();
                            break;
                        case SEEK_NOTE_SUNSET: launchAction = SuntimesActivity.ACTION_NOTE_SEEK;
                            launchExtras = SuntimesActivity.EXTRA_SOLAREVENT + "=" + SolarEvents.SUNSET.name();
                            break;

                        case CARD_NEXT: launchAction = SuntimesActivity.ACTION_CARD_NEXT; break;
                        case CARD_PREV: launchAction = SuntimesActivity.ACTION_CARD_PREV; break;
                        case SWAP_CARD: launchAction = SuntimesActivity.ACTION_CARD_RESET; break;

                        case SHOW_CARD:
                            launchAction = SuntimesActivity.ACTION_CARD_SHOW;
                            launchExtras = SuntimesActivity.EXTRA_SHOW_DATE + "=%dm";
                            break;

                        case NOTHING:
                            launchType = null;
                            break;
                        default:
                            launchType = null;
                            Log.w(TAG, "initDefaults: unhandled action: " + action.name());
                            break;
                    }

                    if (launchType != null) {
                        saveActionLaunchPref(context, action.title(), action.desc(), null, action.getTags(), 0, action.name(), launchString, launchPackage, launchType.name(), launchAction, launchData, launchMime, launchExtras, action.listed());
                    }
                }
            }
        }
    }

    public static String[] ANDROID_ACTION_SUGGESTIONS = new String[] {
            Intent.ACTION_VIEW, Intent.ACTION_SEARCH, Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT,
            Intent.ACTION_SEND,  Intent.ACTION_SENDTO, Intent.ACTION_SEND_MULTIPLE, "android.intent.action.PASTE",
            Intent.ACTION_EDIT, Intent.ACTION_INSERT, Intent.ACTION_INSERT_OR_EDIT, Intent.ACTION_DELETE,
            Intent.ACTION_RUN,  Intent.ACTION_SYNC, Intent.ACTION_CHOOSER,  Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_WEB_SEARCH, Intent.ACTION_SYNC,  Intent.ACTION_VOICE_COMMAND, Intent.ACTION_MAIN,
            "android.intent.action.OPEN_DOCUMENT", "android.intent.action.CREATE_DOCUMENT", "android.intent.action.ASSIST", "android.intent.action.SHOW_APP_INFO",
            "android.intent.action.MUSIC_PLAYER", "com.android.music.PLAYBACK_VIEWER", "android.intent.action.MEDIA_SEARCH",
            "android.media.action.MEDIA_PLAY_FROM_SEARCH", "android.media.action.VIDEO_PLAY_FROM_SEARCH", "android.media.action.TEXT_OPEN_FROM_SEARCH",
            Intent.ACTION_SET_WALLPAPER, "com.android.camera.action.REVIEW",
            "android.intent.action.SHOW_ALARMS", "android.intent.action.SHOW_TIMERS", "android.intent.action.SET_ALARM", "android.intent.action.SET_TIMER"
    };

    public static String[] getSuggestedActions(String launchString)
    {
        if (SuntimesActivity.class.getName().equals(launchString)) {
            return addAll(SuntimesActivity.SUNTIMES_ACTIONS, ANDROID_ACTION_SUGGESTIONS);

        } else if (AlarmClockActivity.class.getName().equals(launchString)) {
            return addAll(AlarmClockActivity.SUNTIMES_ALARMS_ACTIONS, ANDROID_ACTION_SUGGESTIONS);

        } else {
            return ANDROID_ACTION_SUGGESTIONS;
        }
    }
    private static String[] addAll(String[] a, String[] b)
    {
        String[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
