/**
    Copyright (C) 2022-2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.app.DialogBase;
import com.forrestguice.support.app.FragmentCompat;
import com.forrestguice.support.app.FragmentManagerCompat;

import java.io.File;

public class EventListFragment extends DialogBase
{
    public static final String ADAPTER_MODIFIED = "isModified";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_NOSELECT = "noselect";
    public static final String EXTRA_EXPANDED = "expanded";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_TYPEFILTER = "typefilter";       // filter list by event type
    public static final String EXTRA_SELECTFILTER = "selectfilter";   // allow "select and return" for given types

    private EventListHelper helper;

    public EventListFragment()
    {
        super();
        initArgs();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        View v = inflater.inflate(R.layout.layout_dialog_eventlist, parent, false);

        helper = new EventListHelper(v.getContext(), FragmentManagerCompat.from(this, true));
        helper.setLocation(getLocation());
        helper.setExpanded(getArgs().getBoolean(EXTRA_EXPANDED, false));
        helper.setDisallowSelect(getArgs().getBoolean(EXTRA_NOSELECT, false));
        helper.setTypeFilter(getArgs().getStringArray(EXTRA_TYPEFILTER));
        helper.setSelectFilter(getArgs().getStringArray(EXTRA_SELECTFILTER));
        helper.initViews(getActivity(), v, savedState);

        String preselectedEvent = getArgs().getString(EXTRA_SELECTED);
        if (preselectedEvent != null && !preselectedEvent.trim().isEmpty()) {
            helper.setSelected(preselectedEvent);
            helper.triggerActionMode();
        }

        return v;
    }

    @Override
    @NonNull
    public Bundle getArgs()
    {
        Bundle args = getArguments();
        if (args == null) {
            args = initArgs();
        }
        return args;
    }
    protected Bundle initArgs()
    {
        Bundle args = new Bundle();
        args.putString(EXTRA_SELECTED, null);
        args.putBoolean(EXTRA_NOSELECT, false);
        args.putBoolean(EXTRA_EXPANDED, false);
        args.putStringArray(EXTRA_TYPEFILTER, null);
        args.putStringArray(EXTRA_SELECTFILTER, null);
        setArguments(args);
        return args;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        helper.setFragmentManager(FragmentManagerCompat.from(this, true));
        helper.setOnItemAcceptedListener(onItemAccepted);
        helper.setExportTaskListener(exportListener);
        helper.setImportTaskListener(importListener);
        helper.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        helper.onSaveInstanceState(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case EventListHelper.REQUEST_EXPORT_URI:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        helper.exportEvents(getActivity(), uri);
                    }
                }
                break;

            case EventListHelper.REQUEST_IMPORT_URI:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        helper.importEvents(getActivity(), uri);
                    }
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.eventlist, menu);
    }

    public void showAddEventDialog(EventType type, Double angle, Double shadowLength, Double objectHeight) {
        helper.addEvent(type, angle, shadowLength, objectHeight);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        //case R.id.addEvent:
        //    helper.addEvent();
        //    return true;
        if (itemId == R.id.addEvent_sunEvent) {
            helper.addEvent(EventType.SUN_ELEVATION);
            return true;

        } else if (itemId == R.id.addEvent_shadowEvent) {
            helper.addEvent(EventType.SHADOWLENGTH);
            return true;

        } else if (itemId == R.id.addEvent_dayPercentEvent) {
            helper.addEvent(EventType.DAYPERCENT);
            return true;

        } else if (itemId == R.id.addEvent_moonIllumEvent) {
            helper.addEvent(EventType.MOONILLUM);
            return true;

        } else if (itemId == R.id.addEvent_moonEvent) {
            helper.addEvent(EventType.MOON_ELEVATION);
            return true;

        } else if (itemId == R.id.clearEvents) {
            helper.clearEvents();
            return true;

        } else if (itemId == R.id.exportEvents) {
            helper.exportEvents(FragmentCompat.from(EventListFragment.this));
            return true;

        } else if (itemId == R.id.importEvents) {
            helper.importEvents(FragmentCompat.from(EventListFragment.this));
            return true;

        } else if (itemId == R.id.helpEvents) {
            helper.showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final View.OnClickListener onItemAccepted = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemPicked(helper.getEventID(), helper.getAliasUri());
            }
        }
    };

    public boolean isModified() {
        if (helper != null) {
            return helper.isAdapterModified();
        } else return false;
    }

    public void setPreselected(String value) {
        getArgs().putString(EXTRA_SELECTED, value);
    }

    public void setExpanded(boolean value) {
        getArgs().putBoolean(EXTRA_EXPANDED, value);
        if (helper != null) {
            helper.setExpanded(value);
        }
    }
    public boolean isExpanded() {
        return getArgs().getBoolean(EXTRA_EXPANDED, false);
    }

    public void setDisallowSelect(boolean value) {
        getArgs().putBoolean(EXTRA_NOSELECT, value);
        if (helper != null) {
            helper.setDisallowSelect(value);
        }
    }
    public boolean disallowSelect() {
        return getArgs().getBoolean(EXTRA_NOSELECT, false);
    }

    public void setLocation(Location value) {
        getArgs().putSerializable(EXTRA_LOCATION, value);
    }
    public Location getLocation() {
        return (Location) getArgs().getSerializable(EXTRA_LOCATION);
    }

    public void setTypeFilter(@Nullable String[] filter) {
        getArgs().putStringArray(EXTRA_TYPEFILTER, filter);
    }
    public void setSelectFilter(@Nullable String[] filter) {
        getArgs().putStringArray(EXTRA_SELECTFILTER, filter);
    }

    /**
     * ImportListener
     */
    private final EventImportTask.TaskListener importListener = new EventImportTask.TaskListener() {
        @Override
        public void onStarted() {
            setRetainInstance(true);
        }

        @Override
        public void onFinished(EventImportTask.TaskResult result)
        {
            setRetainInstance(false);

            if (isAdded())
            {
                if (!result.getResult())
                {
                    Uri uri = result.getUri();   // import failed
                    String path = ((uri != null) ? uri.toString() : "<path>");
                    String failureMessage = getString(R.string.msg_import_failure, path);
                    Toast.makeText(getActivity(), failureMessage, Toast.LENGTH_LONG).show();

                } //else {
                  //  String successMessage = getString(R.string.msg_import_success, result.getUri().toString());
                  //  Toast.makeText(getActivity(), successMessage, Toast.LENGTH_LONG).show();
                //}
            }
        }
    };

    /**
     * ExportListener
     */
    private final ExportTask.TaskListener exportListener = new ExportTask.TaskListener()
    {
        @Override
        public void onStarted() {
            setRetainInstance(true);
        }

        @Override
        public void onFinished(ExportTask.ExportResult results)
        {
            setRetainInstance(false);

            Context context = getActivity();
            if (context != null)
            {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath() : ExportTask.getFileName(context.getContentResolver(), results.getExportUri()));

                if (results.getResult())
                {
                    if (isAdded()) {
                        String successMessage = context.getString(R.string.msg_export_success, path);
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show();
                        // TODO: use a snackbar instead; offer 'copy path' action
                    }

                    if (Build.VERSION.SDK_INT >= 19) {
                        if (results.getExportUri() == null) {
                            ExportTask.shareResult(context, results.getExportFile(), results.getMimeType());
                        }
                    } else {
                        ExportTask.shareResult(context, results.getExportFile(), results.getMimeType());
                    }
                    return;
                }

                if (isAdded()) {
                    String failureMessage = context.getString(R.string.msg_export_failure, path);
                    Toast.makeText(context, failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    /**
     * FragmentListener
     */
    public interface FragmentListener
    {
        void onItemPicked(String eventID, String eventUri);
    }

    protected FragmentListener listener;
    public void setFragmentListener(FragmentListener value) {
        listener = value;
    }

}
