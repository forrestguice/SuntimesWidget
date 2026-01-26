/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemImportTask;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.settings.fragments.AlarmPrefsFragment;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.util.ExecutorUtils;

public class WelcomeAlarmsView extends WelcomeView
{
    public static final int IMPORT_REQUEST = 1200;

    public WelcomeAlarmsView(Context context) {
        super(context, R.layout.layout_welcome_alarms);
    }
    public WelcomeAlarmsView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_alarms);
    }
    public WelcomeAlarmsView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_alarms);
    }

    protected TextView autostartText;
    protected TextView batteryOptimizationText;
    protected Button importAlarmsButton;
    private ProgressBar progress_importAlarms;

    public static WelcomeAlarmsView newInstance(AppCompatActivity activity) {
        return new WelcomeAlarmsView(activity);
    }

    @Override
    public void initViews(final Context context, View view)
    {
        super.initViews(context, view);

        CheckBox launcherIconCheck = (CheckBox) view.findViewById(R.id.check_alarms_showlauncher);
        if (launcherIconCheck != null)
        {
            launcherIconCheck.setChecked(AlarmSettings.loadPrefShowLauncher(context));
            launcherIconCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AlarmSettings.savePrefShowLauncher(context, isChecked);
                }
            });
        }

        CheckBox reminderNotificationCheck = (CheckBox) view.findViewById(R.id.check_alarms_showreminders);
        if (reminderNotificationCheck != null)
        {
            long reminderMillis = AlarmSettings.loadPrefAlarmUpcoming(context);
            reminderNotificationCheck.setChecked(reminderMillis > 0);
            reminderNotificationCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    long reminderMillis = (isChecked ? AlarmSettings.PREF_DEF_ALARM_UPCOMING : 0);
                    AlarmSettings.savePrefAlarmUpcomingReminder(context, reminderMillis);
                }
            });
        }

        batteryOptimizationText = (TextView) view.findViewById(R.id.text_optWhiteList);

        Button batteryOptimizationButton = (Button) view.findViewById(R.id.button_optWhiteList);
        if (batteryOptimizationButton != null)
        {
            batteryOptimizationButton.setVisibility((Build.VERSION.SDK_INT >= 23) ? View.VISIBLE : View.GONE);
            batteryOptimizationButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmPrefsFragment.createBatteryOptimizationAlertDialog(context).show();
                }
            });
        }

        View layout_autoStart = view.findViewById(R.id.layout_autostart);
        if (layout_autoStart != null) {
            layout_autoStart.setVisibility( AlarmSettings.hasAutostartSettings(context) ? View.VISIBLE : View.GONE );
        }

        autostartText = (TextView) view.findViewById(R.id.text_autostart);
        Button autostartButton = (Button) view.findViewById(R.id.button_autostart);
        if (autostartButton != null) {
            autostartButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmSettings.openAutostartSettings(context);
                }
            });
        }

        progress_importAlarms = (ProgressBar) view.findViewById(R.id.progress_import_alarms);
        importAlarmsButton = (Button) view.findViewById(R.id.button_import_alarms);
        if (importAlarmsButton != null) {
            importAlarmsButton.setOnClickListener(onImportAlarmsClicked());
        }
    }

    @Override
    public void updateViews(Context context)
    {
        if (batteryOptimizationText != null)
        {
            batteryOptimizationText.setVisibility((Build.VERSION.SDK_INT >= 23) ? View.VISIBLE : View.GONE);
            batteryOptimizationText.setText(AlarmSettings.batteryOptimizationMessage(context));
        }

        if (autostartText != null) {
            autostartText.setText(AlarmSettings.hasAutostartSettings(context) ? AlarmSettings.autostartMessage(context) : "");
        }
    }

    protected void toggleControlsEnabled(boolean value)
    {
        if (importAlarmsButton != null) {
            importAlarmsButton.setEnabled(value);
        }
    }

    protected void toggleControlsVisible(boolean visible)
    {
        if (importAlarmsButton != null) {
            importAlarmsButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    protected void toggleProgress(boolean visible) {
        if (progress_importAlarms != null) {
            progress_importAlarms.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    //@Override
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        //super.onActivityResultCompat(requestCode, resultCode, data);
        switch (requestCode)
        {
            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importAlarms(getActivity(), uri);
                    }
                }
                break;
        }
    }

    @Nullable
    protected AlarmClockItemImportTask importTask = null;
    private OnClickListener onImportAlarmsClicked()
    {
        return new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (importTask != null) {
                    Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");
                }
                AlarmListDialog.ImportFragment fragment = new AlarmListDialog.ImportFragment() {
                    @Override
                    public void startActivityForResult(Intent intent, int request)
                    {
                        AppCompatActivity activity = getActivity();
                        if (activity != null) {
                            getActivity().startActivityForResultCompat(intent, request);
                        }
                    }
                };
                AlarmListDialog.importAlarms(fragment, getContext(), getLayoutInflater(), IMPORT_REQUEST);
            }
        };
    }

    protected void importAlarms(@Nullable final Context context, @NonNull Uri uri)
    {
        if (context == null) {
            return;
        }
        if (importTask != null) {
            Log.e("ImportAlarms", "Already busy importing/exporting! ignoring request");
        }
        importTask = new AlarmClockItemImportTask(context, uri);
        ExecutorUtils.runProgress("ImportAlarmsTask", importTask, importAlarmsListener);
    }

    private final AlarmClockItemImportTask.TaskListener importAlarmsListener = new AlarmClockItemImportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            //setRetainInstance(true);
            toggleProgress(true);
            toggleControlsEnabled(false);
            toggleControlsVisible(false);
        }

        @Override
        public void onFinished(AlarmClockItemImportTask.TaskResult result)
        {
            final Context context = getContext();
            if (result.getResult() && context != null)
            {
                final AlarmClockItem[] items = result.getItems();
                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, items, true, true);
                task.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
                {
                    @Override
                    public void onFinished(AlarmDatabaseAdapter.AlarmItemTaskResult result)
                    {
                        AlarmClockItem[] items = result.getItems();
                        //setRetainInstance(false);
                        importTask = null;
                        toggleProgress(false);
                        toggleControlsVisible(true);

                        if (result.getResult())
                        {
                            String plural = getResources().getQuantityString(R.plurals.alarmPlural, items.length, items.length);
                            importAlarmsButton.setText(getContext().getString(R.string.importalarms_toast_success, plural));

                            for (AlarmClockItem item : items) {
                                if (item.enabled) {
                                    context.sendBroadcast( AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri()) );
                                }
                            }
                        }
                    }
                });
                ExecutorUtils.runTask("AlarmUpdateTask", task, task.getTaskListener());

            } else {
                //setRetainInstance(false);
                importTask = null;
                toggleProgress(false);
                toggleControlsEnabled(true);
                if (isAdded() && context != null)
                {
                    Uri uri = result.getUri();   // import failed
                    String path = ((uri != null) ? uri.toString() : "<path>");
                    String failureMessage = context.getString(R.string.msg_import_failure, path);
                    Toast.makeText(context, failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

}
