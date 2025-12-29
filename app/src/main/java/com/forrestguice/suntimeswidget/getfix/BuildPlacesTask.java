/**
    Copyright (C) 2014-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class BuildPlacesTask extends AsyncTask<Object, Object, Integer>
{
    public static final long MIN_WAIT_TIME = 2000;

    private GetFixDatabaseAdapter db;
    private WeakReference<Context> contextRef;

    private boolean isPaused = false;
    public void pauseTask()
    {
        isPaused = true;
        //Log.d("DEBUG", "BuildPlacesTask paused");
    }
    public void resumeTask()
    {
        isPaused = false;
        //Log.d("DEBUG", "BuildPlacesTask resumed");
    }
    public boolean isPaused()
    {
        return isPaused;
    }

    public BuildPlacesTask(Context context)
    {
        this.contextRef = new WeakReference<Context>(context);
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
    }

    private int clearPlaces()
    {
        db.open();
        boolean result = db.clearPlaces();
        db.close();

        Log.i("BuildPlacesTask", "clearPlaces: " + result);
        return (result ? 1 : 0);
    }

    /**
     * @param context context
     * @param locations added to the given ArrayList
     */
    private void addPlacesFromRes(Context context, @NonNull ArrayList<Location> locations)
    {
        for (Locale locale : Locale.getAvailableLocales())
        {
            Location location = null;
            if (Build.VERSION.SDK_INT >= 17 && context != null)
            {
                Configuration config = new Configuration(context.getResources().getConfiguration());
                config.setLocale(locale);

                Resources resources = context.createConfigurationContext(config).getResources();
                String label = resources.getString(R.string.default_location_label);
                String lat = resources.getString(R.string.default_location_latitude);
                String lon = resources.getString(R.string.default_location_longitude);
                String alt = resources.getString(R.string.default_location_altitude);
                location = new Location(label, lat, lon, alt);
            } // else    // TODO: legacy support

            if (location != null && !locations.contains(location))
            {
                locations.add(location);
            }
        }
    }

    private void addPlacesFromGroup(Context context, @NonNull String[] groups, @NonNull ArrayList<PlaceItem> locations)
    {
        if (groups.length == 0) {
            addPlacesFromGroup(context, (String) null, locations);
        } else {
            for (String groupItem : groups)
            {
                String[] parts = groupItem.split(",");
                addPlacesFromGroup(context, parts[0], locations);
            }
        }
    }

    private void addPlacesFromGroup(Context context, @Nullable String fromGroup, @NonNull ArrayList<PlaceItem> locations)
    {
        Resources r = context.getResources();
        int groupID = (fromGroup == null) ? 0
                : r.getIdentifier(fromGroup, "array", context.getPackageName());

        if (fromGroup == null || (fromGroup.startsWith("place_group_") && groupID != 0))
        {
            String[] groups = fromGroup != null
                    ? r.getStringArray(groupID)
                    : r.getStringArray(R.array.place_groups);

            for (String groupItem : groups)
            {
                String[] parts = groupItem.split(",");
                if (parts.length > 0) {
                    addPlacesFromGroup(context, parts[0].trim(), locations);    // recursive call
                }
            }

        } else if (groupID != 0) {
            ArrayList<String> items = new ArrayList<>(Arrays.asList(r.getStringArray(groupID)));    // base case
            if (items.size() > 0)
            {
                for (String item : items)
                {
                    PlaceItem location = csvItemToPlaceItem(item);
                    if (location != null)
                    {
                        location.comment = (location.comment == null) ? getDefaultComment(fromGroup)
                                : location.comment.concat(getDefaultComment(fromGroup));
                        locations.add(location);
                    }
                }
            }
        }
    }

    private String getDefaultComment(String fromGroup) {
        return PlaceItem.TAG_DEFAULT; // + "[" + fromGroup + "]";
    }

    private void addPlacesFromUri(Context context, @NonNull Uri uri, @NonNull ArrayList<PlaceItem> locations)
    {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in != null)
            {
                BufferedInputStream input = new BufferedInputStream(in);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line = reader.readLine();
                while (line != null)
                {
                    PlaceItem location = csvItemToPlaceItem(line);
                    if (location != null && !locations.contains(location)) {
                        locations.add(location);
                    }
                    line = reader.readLine();
                }

            } else {
                Log.e("BuildPlacesTask", "Failed to import from " + uri + " (null)");
            }
        } catch (FileNotFoundException e) {
            Log.e("BuildPlacesTask", "Failed to import from " + uri + ": " + e);
        } catch (IOException e) {
            Log.e("BuildPlacesTask", "Failed to import from " + uri + ": " + e);
        }
    }

    @Nullable
    public static PlaceItem csvItemToPlaceItem(String csv_item)
    {
        if (csv_item == null) {
            return null;
        }

        String[] parts = splitCSV(csv_item, ','); // csv_item.split(",");
        if (parts.length < 3) {
            Log.e("BuildPlacesTask", "Ignoring malformed line; " + csv_item);
            return null;
        }

        String label = parts[0];
        if (label.startsWith("\"")) {
            label = label.substring(1);
        }
        if (label.endsWith("\"")) {
            label = label.substring(0, label.length()-1);
        }

        String lat, lon;
        String alt = "0";
        String comment = null;
        try {
            lat = "" + Double.parseDouble(parts[1]);
            lon = "" + Double.parseDouble(parts[2]);
            if (parts.length >= 4) {
                alt = "" + Double.parseDouble(parts[3]);
            }
            if (parts.length >= 5) {
                comment = parts[4];
            }
        } catch (NumberFormatException e) {
            Log.e("BuildPlacesTask", "Ignoring line " + csv_item + " .. " + e);
            return null;
        }

        return new PlaceItem(-1, new Location(label, lat, lon, alt), comment);
    }

    public static String[] splitCSV(String value, Character delimiter)
    {
        ArrayList<String> parts = new ArrayList<>();
        boolean quoted = false;
        int j = 0;
        for (int i=0; i<value.length(); i++)
        {
            if (value.charAt(i) == '\"') {
                quoted = !quoted;

            } else if (value.charAt(i) == delimiter) {
                if (!quoted) {
                    parts.add(value.substring(j, i));
                    j = i + 1;
                }
            }
        }
        parts.add(value.substring(j));
        return parts.toArray(new String[0]);
    }

    /**
     * Pass a URI to build from file, groups[] to build from resources, or null for both to build
     * from internal locales.
     * @param uri optional source uri; null to skip
     * @param groups optional group list; null to skip, or pass an empty list to add all
     * @return the number of items added to the database
     */
    private int buildPlaces(@Nullable Uri uri, @Nullable String[] groups)
    {
        int result = 0;
        ArrayList<PlaceItem> locations = new ArrayList<>();
        try {
            Context context = contextRef.get();
            db.open();

            if (uri != null) {
                addPlacesFromUri(context, uri, locations);
            } else if (groups != null) {
                addPlacesFromGroup(context, groups, locations);
            } else {
                ArrayList<Location> locations0 = new ArrayList<>();
                addPlacesFromRes(context, locations0);
                for (Location location : locations0) {
                    locations.add(new PlaceItem(-1, location));
                }
            }

            Collections.sort(locations, new Comparator<PlaceItem>()
            {
                @Override
                public int compare(PlaceItem o1, PlaceItem o2) {
                    return o2.location.getLabel().compareTo(o1.location.getLabel());  // descending
                }
            });

            Cursor cursor = db.getAllPlaces(0, false);
            for (int i=0; i<locations.size(); i++)
            {
                PlaceItem item = locations.get(i);
                if (item == null || item.location == null) {
                    continue;
                }
                if (item.comment == null) {
                    item.comment = PlaceItem.TAG_DEFAULT;
                } else if (!item.comment.contains(PlaceItem.TAG_DEFAULT)) {
                    item.comment = item.comment.concat(PlaceItem.TAG_DEFAULT);
                }

                int p = GetFixDatabaseAdapter.findPlaceByName(item.location.getLabel(), cursor);
                if (p < 0)    // if not found
                {                 // then add new place
                    db.addPlace(item.location, item.comment);
                    result++;
                }
            }
            cursor.close();

            Log.i("BuildPlacesTask", "buildPlaces: " + result);
            db.close();

        } catch (SQLException e) {
            Log.e("BuildPlacesTask", "Failed to access database: " + e);
            result = -1;
        }
        return result;
    }

    @Override
    protected Integer doInBackground(Object... params)
    {
        long startTime = System.currentTimeMillis();

        boolean param_clearPlaces = false;
        if (params.length > 0) {
            param_clearPlaces = (Boolean)params[0];
        }

        Uri param_source = null;
        if (params.length > 1) {
            param_source = (Uri)params[1];
        }

        String[] param_groups = null;
        if (params.length > 2) {
            param_groups = (String[])params[2];
        }

        int result = param_clearPlaces ? clearPlaces()
                                       : buildPlaces(param_source, param_groups);

        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused)
        {
            endTime = System.currentTimeMillis();
        }
        return result;
    }

    @Override
    protected void onPreExecute()
    {
        signalStarted();
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        signalFinished(result);
    }


    /**
     * Event Listener
     */
    private TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener )
    {
        taskListener = listener;
    }
    public void clearTaskListener()
    {
        taskListener = null;
    }
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( Integer result ) {}
    }

    private void signalStarted()
    {
        if (taskListener != null)
            taskListener.onStarted();
    }
    private void signalFinished( Integer result )
    {
        if (taskListener != null)
            taskListener.onFinished(result);
    }

    /**
     * OpenFileIntent
     */
    public static Intent buildPlacesOpenFileIntent() {
        return ExportTask.getOpenFileIntent("text/*");
    }

    /**
     * promptAddWorldPlaces
     */
    public static void promptAddWorldPlaces(final Context context, final BuildPlacesTask.TaskListener l)
    {
        BuildPlacesTask.chooseGroups(context, new BuildPlacesTask.ChooseGroupsDialogListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, String[] groups, boolean[] checked)
            {
                if (hasChecked(checked))
                {
                    ArrayList<String> items = new ArrayList<>();
                    for (int i=0; i<groups.length; i++) {
                        if (checked[i]) {
                            items.add(groups[i]);
                        }
                    }
                    BuildPlacesTask task = new BuildPlacesTask(context);
                    task.setTaskListener(l);
                    task.execute(false, null, items.toArray(new String[0]));
                }
            }
        });
    }

    protected static boolean hasChecked(boolean[] values) {
        for (boolean v : values) {
            if (v) {
                return true;
            }
        }
        return false;
    }

    /**
     * ChooseGroups
     */
    public static void chooseGroups(final Context context, @NonNull final ChooseGroupsDialogListener onClickListener)
    {
        String[] groups = context.getResources().getStringArray(R.array.place_groups);
        chooseGroups(context, groups, onClickListener);
    }
    public static void chooseGroups(final Context context, final String[] groups,  @NonNull final ChooseGroupsDialogListener onClickListener)
    {
        final ArrayList<Pair<Integer,CharSequence>> items = new ArrayList<>();
        final boolean[] checked = new boolean[groups.length];
        for (int i=0; i<groups.length; i++)
        {
            checked[i] = false;
            String[] itemParts = (groups[i] != null) ? groups[i].split(",") : new String[] {""};
            int labelID = (itemParts.length > 1) ? context.getResources().getIdentifier(itemParts[1].trim(), "string", context.getPackageName()) : 0;
            String label = (labelID != 0 ? context.getString(labelID) : "");
            items.add(new Pair<Integer, CharSequence>(i, label));
        }

        CharSequence[] displayStrings = new CharSequence[items.size()];
        for (int i=0; i<displayStrings.length; i++) {
            displayStrings[i] = items.get(i).second;
        }

        int[] attrs = { R.attr.icActionWorldMap };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int iconResID = a.getResourceId(0, R.drawable.ic_action_map);
        a.recycle();

        DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                int i = items.get(which).first;
                checked[i] = isChecked;
            }
        };

        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.configLabel_places_build))
                .setIcon(iconResID)
                .setMultiChoiceItems(displayStrings, Arrays.copyOf(checked, checked.length), onMultiChoiceClickListener)
                .setPositiveButton(context.getString(R.string.configAction_addPlace), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickListener.onClick(dialog, AlertDialog.BUTTON_POSITIVE, groups, checked);
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                .setNeutralButton(context.getString(R.string.configAction_checkAll), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { /* EMPTY; must be non-null */ }
                });

        final AlertDialog d = confirm.create();
        d.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                final AlertDialog d = (AlertDialog) dialog;
                final View.OnClickListener toggleListener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ListView list = d.getListView();
                        for (int i=0; i<list.getCount(); i++)
                        {
                            list.setItemChecked(i, true);
                            checked[i] = true;
                        }
                    }
                };
                d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(toggleListener);
            }
        });
        d.show();
    }

    public interface ChooseGroupsDialogListener {
        void onClick(DialogInterface dialog, int which, String[] groups, boolean[] checked);
    }

}
