/**
    Copyright (C) 2019 Forrest Guice
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;

import java.util.Set;
import java.util.TreeSet;

/**
 * WidgetActions
 */
public class WidgetActions
{
    public static final String TAG = "WidgetActions";

    public static final String PREFS_WIDGET = WidgetSettings.PREFS_WIDGET;

    public static final String PREF_PREFIX_KEY = WidgetSettings.PREF_PREFIX_KEY;
    public static final String PREF_PREFIX_KEY_ACTION = "_action_";

    public static final String PREF_KEY_ACTION_LAUNCH_TITLE = "title";
    public static String PREF_DEF_ACTION_LAUNCH_TITLE = "Suntimes";

    public static final String PREF_KEY_ACTION_LAUNCH_DESC = "desc";
    public static String PREF_DEF_ACTION_LAUNCH_DESC = "";

    public static final String PREF_KEY_ACTION_LAUNCH_COLOR = "color";
    public static int PREF_DEF_ACTION_LAUNCH_COLOR = Color.WHITE;

    public static final String PREF_KEY_ACTION_LAUNCH = "launch";
    public static final String PREF_DEF_ACTION_LAUNCH = "com.forrestguice.suntimeswidget.SuntimesActivity";

    public static final String PREF_KEY_ACTION_LAUNCH_ACTION = "action";
    public static final String PREF_DEF_ACTION_LAUNCH_ACTION = "";

    public static final String PREF_KEY_ACTION_LAUNCH_EXTRAS = "extras";
    public static final String PREF_DEF_ACTION_LAUNCH_EXTRAS = "";

    public static final String PREF_KEY_ACTION_LAUNCH_DATA = "data";
    public static final String PREF_DEF_ACTION_LAUNCH_DATA = "";

    public static final String PREF_KEY_ACTION_LAUNCH_DATATYPE = "datatype";
    public static final String PREF_DEF_ACTION_LAUNCH_DATATYPE = "";

    public static final String PREF_KEY_ACTION_LAUNCH_TYPE = "type";
    public static final LaunchType PREF_DEF_ACTION_LAUNCH_TYPE = LaunchType.ACTIVITY;

    public static final String LAUNCH_TYPE_ACTIVITY = "ACTIVITY";
    public static final String LAUNCH_TYPE_BROADCAST = "BROADCAST";
    public static final String LAUNCH_TYPE_SERVICE = "SERVICE";

    public static final String PREF_KEY_ACTION_LAUNCH_LIST = "list";

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

    public static void saveActionLaunchPref(Context context, @Nullable String titleString, @Nullable String descString, @Nullable Integer color, int appWidgetId, @Nullable String id, @Nullable String launchString, @Nullable String type, @Nullable String action, @Nullable String dataString, @Nullable String mimeType, @Nullable String extrasString)
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

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";

        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH, (launchString != null ? launchString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TYPE, (type != null ? type : LAUNCH_TYPE_ACTIVITY));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_ACTION, (action != null ? action : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATA, (dataString != null ? dataString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATATYPE, (mimeType != null ? mimeType : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_EXTRAS, (extrasString != null ? extrasString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TITLE, (titleString != null ? titleString : ""));
        prefs.putString(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DESC, (descString != null ? descString : ""));
        prefs.putInt(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_COLOR, (color != null ? color : PREF_DEF_ACTION_LAUNCH_COLOR));
        prefs.apply();

        if (hasID)
        {
            Set<String> actionList = loadActionLaunchList(context, 0);
            actionList.add(id);
            prefs.putStringSet(PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST, actionList);
            prefs.apply();
        }
    }
    public static Set<String> loadActionLaunchList(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String listKey = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST;
        Set<String> actionList = prefs.getStringSet(listKey, null);
        return (actionList != null) ? actionList : new TreeSet<String>();
    }
    public static String loadActionLaunchPref(Context context, int appWidgetId, @Nullable String id, @Nullable String key)
    {
        if (id == null) {
            id = "0";
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";

        if (key == null || key.isEmpty())
        {
            return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH, PREF_DEF_ACTION_LAUNCH);

        } else {
            switch (key)
            {
                case PREF_KEY_ACTION_LAUNCH_TYPE:
                    return prefs.getString(prefs_prefix + PREF_KEY_ACTION_LAUNCH_TYPE, LAUNCH_TYPE_ACTIVITY);

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

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix0 = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH );
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_ACTION);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATA);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DATATYPE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_EXTRAS);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_TITLE);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_DESC);
        prefs.remove(prefs_prefix0 + PREF_KEY_ACTION_LAUNCH_COLOR);
        prefs.apply();

        Set<String> actionList = loadActionLaunchList(context, 0);
        actionList.remove(id);
        prefs.putStringSet(PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + PREF_KEY_ACTION_LAUNCH_LIST, actionList);
        prefs.apply();
    }
    public static boolean hasActionLaunchPref(Context context, int appWidgetId, @NonNull String id)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ACTION + PREF_KEY_ACTION_LAUNCH + "_" + id + "_";
        return prefs.contains(prefs_prefix + PREF_KEY_ACTION_LAUNCH_TITLE);
    }

    /**
     * launchIntent
     */
    public static void startIntent(@NonNull Context context, @NonNull Intent launchIntent, @Nullable String launchType)
    {
        if (launchType != null)
        {
            try {
                Log.i(TAG, "startIntent :: " + launchType + " :: " + launchIntent.toString());
                switch (launchType)
                {
                    case WidgetActions.LAUNCH_TYPE_BROADCAST:
                        context.sendBroadcast(launchIntent);
                        break;

                    case WidgetActions.LAUNCH_TYPE_SERVICE:
                        context.startService(launchIntent);
                        break;

                    case WidgetActions.LAUNCH_TYPE_ACTIVITY:
                    default:
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "startIntent: unable to start + " + launchType + " :: " + e);
            }

        } else {
            Log.i(TAG, "startIntent :: ACTIVITY :: " + launchIntent.toString());
            context.startActivity(launchIntent);
        }
    }

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
            SuntimesUtils utils = new SuntimesUtils();
            Uri dataUri = Uri.parse(Uri.decode(
                    ((data != null) ? utils.displayStringForTitlePattern(context, dataString, data) : dataString)
            ));
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
        for (String extra : extras)
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
                            intent.putExtra(key, Long.parseLong(value));  // long
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
                    }

                } else {
                    String lowerCase = value.toLowerCase();
                    if (lowerCase.equals("true") || lowerCase.equals("false")) {
                        intent.putExtra(key, lowerCase.equals("true"));  // boolean
                        Log.i(TAG, "applyExtras: applied " + extra + " (boolean)");

                    } else {
                        intent.putExtra(key, value);  // string
                        Log.i(TAG, "applyExtras: applied " + extra + " (String)");
                    }
                }

            } else {
                Log.w(TAG, "applyExtras: skipping " + extra);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

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

        if (!hasActionLaunchPref(context, 0, "def_suntimes")) {
            saveActionLaunchPref(context, WidgetActions.PREF_DEF_ACTION_LAUNCH_TITLE, WidgetActions.PREF_DEF_ACTION_LAUNCH_DESC, null, 0, "def_suntimes", WidgetActions.PREF_DEF_ACTION_LAUNCH,
                    WidgetActions.PREF_DEF_ACTION_LAUNCH_TYPE.name(), WidgetActions.PREF_DEF_ACTION_LAUNCH_ACTION, WidgetActions.PREF_DEF_ACTION_LAUNCH_DATA, WidgetActions.PREF_DEF_ACTION_LAUNCH_DATATYPE, WidgetActions.PREF_DEF_ACTION_LAUNCH_EXTRAS);
        }

        if (!hasActionLaunchPref(context, 0, "def_calendar")) {
            saveActionLaunchPref(context, "Calendar", "Open calendar", null, 0, "def_calendar", null,
                    LaunchType.ACTIVITY.name(), Intent.ACTION_VIEW, "content://com.android.calendar/time/%dm", null, null);
        }

        if (!hasActionLaunchPref(context, 0, "def_map")) {
            saveActionLaunchPref(context, "Map", "View location with map", null,  0, "def_map", null,
                    LaunchType.ACTIVITY.name(), Intent.ACTION_VIEW, "geo:%lat,%lon", null, null);
        }
    }

    public static void initDisplayStrings( Context context ) {
        LaunchType.initDisplayStrings(context);
    }
}
