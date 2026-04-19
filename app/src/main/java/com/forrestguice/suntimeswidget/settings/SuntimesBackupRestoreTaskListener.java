/**
    Copyright (C) 2024-2026 Forrest Guice
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

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.concurrent.TaskListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * RestoreTaskListener
 */
public abstract class SuntimesBackupRestoreTaskListener implements TaskListener<SuntimesBackupLoadTask.TaskResult>
{
    protected abstract void showProgress(Context context, String title, String message);
    protected abstract void dismissProgress();

    protected WeakReference<Context> contextRef;
    protected WeakReference<View> viewRef;

    public SuntimesBackupRestoreTaskListener(@NonNull Context context, @NonNull View view) {
        contextRef = new WeakReference<>(context);
        viewRef = new WeakReference<>(view);
    }

    @Override
    public void onStarted() {
        Context context = contextRef.get();
        if (context != null) {
            showProgress(context, context.getString(R.string.action_restoreBackup), context.getString(R.string.action_restoreBackup));
        }
    }

    @Override
    public void onFinished(SuntimesBackupLoadTask.TaskResult result)
    {
        Context context = contextRef.get();
        if (context != null && result != null && result.getResult() && result.numResults() > 0)
        {
            final Map<String, ContentValues[]> allValues = result.getItems();
            SuntimesBackupTask.chooseBackupContent(context, allValues.keySet(), true, new SuntimesBackupTask.ChooseBackupDialogListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked)
                {
                    final Set<String> includeKeys = new TreeSet<>();
                    for (int i=0; i<keys.length; i++) {
                        if (checked[i]) {
                            includeKeys.add(keys[i]);
                        }
                    }

                    final String[] keysThatWantMethods = new String[] { SuntimesBackupTask.KEY_WIDGETSETTINGS, SuntimesBackupTask.KEY_PLACEITEMS, SuntimesBackupTask.KEY_ALARMITEMS };
                    final Map<String, int[]> methodsForKeysThatWantMethods = new HashMap<>();
                    methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_ALARMITEMS, SuntimesBackupRestoreTask.IMPORT_ALARMS_METHODS);
                    methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_PLACEITEMS, SuntimesBackupRestoreTask.IMPORT_PLACES_METHODS);
                    methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_WIDGETSETTINGS, SuntimesBackupRestoreTask.IMPORT_WIDGETS_METHODS);

                    final Map<String,Integer> methods = new HashMap<>();   // choose methods for key each; import after observing all
                    final SuntimesBackupRestoreTask.BackupKeyObserver observer = new SuntimesBackupRestoreTask.BackupKeyObserver(keysThatWantMethods, new SuntimesBackupRestoreTask.BackupKeyObserver.ObserverListener()
                    {
                        @Override
                        public void onObservingItem(final SuntimesBackupRestoreTask.BackupKeyObserver observer, final String key )
                        {
                            if (includeKeys.contains(key))
                            {
                                int[] m = methodsForKeysThatWantMethods.get(key);
                                if (m != null) {
                                    SuntimesBackupRestoreTask.chooseImportMethod(context, key, m, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int importMethod) {
                                            methods.put(key, importMethod);
                                            observer.notify(key);    // trigger observeNext
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int importMethod) {
                                            dismissProgress();
                                        }
                                    });
                                }
                            } else observer.notify(key);
                        }
                        public void onObservedAll(SuntimesBackupRestoreTask.BackupKeyObserver observer) {
                            importSettings(context, includeKeys, methods, allValues);
                        }
                    });
                    observer.observeNext();
                }

                @Override
                public void onCancel(DialogInterface dialog) {
                    dismissProgress();
                }
            });

        } else {
            dismissProgress();
            View v = viewRef.get();
            if (v != null) {
                SuntimesBackupLoadTask.showIOResultSnackbar(context, v, false, 0, null);    // v = .getWindow().getDecorView()
            }
        }
    }

    protected void importSettings(final Context context, final Set<String> keys, final Map<String,Integer> methods, final Map<String, ContentValues[]> allValues)
    {
        SuntimesBackupRestoreTask task = new SuntimesBackupRestoreTask(context);
        task.setData(allValues);
        task.setKeys(keys);
        task.setMethods(methods);
        TaskListener<SuntimesBackupRestoreTask.TaskResult> taskListener = new TaskListener<SuntimesBackupRestoreTask.TaskResult>()
        {
            @Override
            public void onStarted() {
                showProgress(context, context.getString(R.string.action_import), context.getString(R.string.action_import));
            }

            @Override
            public void onFinished(SuntimesBackupRestoreTask.TaskResult result)
            {
                dismissProgress();
                View v = viewRef.get();
                if (v != null) {
                    if (result != null && result.getResult()) {
                        int c = result.getNumResults();
                        SuntimesBackupLoadTask.showIOResultSnackbar(context, v, (c > 0), c, ((c > 0) ? result.getReport() : null));

                    } else if (result != null) {
                        SuntimesBackupLoadTask.showIOResultSnackbar(context, v, false, result.getNumResults(), result.getReport());
                    }
                }
            }
        };
        ExecutorUtils.runTask(SuntimesBackupRestoreTask.TAG, task, taskListener);
    }
}