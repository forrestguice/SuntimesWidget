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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupLoadTask;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupRestoreTaskListener;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.concurrent.TaskListener;

public class WelcomeFirstPageView extends WelcomeView
{
    public WelcomeFirstPageView(Context context) {
        super(context, R.layout.layout_welcome_app);
    }
    public WelcomeFirstPageView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_app);
    }
    public WelcomeFirstPageView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_app);
    }
    public static WelcomeFirstPageView newInstance(AppCompatActivity activity) {
        return new WelcomeFirstPageView(activity);
    }

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);

        restoreBackupProgress = view.findViewById(R.id.progress_restore_backup);;
        restoreBackupButton = view.findViewById(R.id.button_restore_backup);
        if (restoreBackupButton != null)
        {
            onRestoreBackupClicked = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResultCompat(ExportTask.getOpenFileIntent("text/*"), IMPORT_REQUEST);
                }
            };
            restoreBackupButton.setOnClickListener(onRestoreBackupClicked);
        }

        onlineHelpButton = view.findViewById(R.id.button_online_help);
        if (onlineHelpButton != null) {
            onOnlineHelpClicked = HelpDialog.getOnlineHelpClickListener(context, HELP_PATH_ID);
            onlineHelpButton.setOnClickListener(onOnlineHelpClicked);
        }
    }

    protected Button onlineHelpButton;
    protected Button restoreBackupButton;
    protected ProgressBar restoreBackupProgress;

    private static final int HELP_PATH_ID = R.string.help_welcome_path;
    private OnClickListener onOnlineHelpClicked = null;

    public static final int IMPORT_REQUEST = 1000000;
    private OnClickListener onRestoreBackupClicked = null;

    @Override
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        restoreFromBackup(getContext(), this, uri);
                    }
                }
                break;
        }
    }

    protected void restoreFromBackup(Context context, View v, Uri uri)
    {
        Log.i("ImportSettings", "Starting import task: " + uri);
        TaskListener<SuntimesBackupLoadTask.TaskResult> taskListener = new SuntimesBackupRestoreTaskListener(context, v)
        {
            @Override
            protected void showProgress(Context context, String title, String message) {
                if (restoreBackupButton != null) {
                    restoreBackupButton.setVisibility(View.GONE);
                }
                if (onlineHelpButton != null) {
                    onlineHelpButton.setVisibility(View.GONE);
                }
                if (restoreBackupProgress != null) {
                    restoreBackupProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void dismissProgress() {
                if (restoreBackupProgress != null) {
                    restoreBackupProgress.setVisibility(View.GONE);
                }
                if (restoreBackupButton != null) {
                    restoreBackupButton.setVisibility(View.VISIBLE);
                }
                if (onlineHelpButton != null) {
                    onlineHelpButton.setVisibility(View.VISIBLE);
                }
            }
        };

        SuntimesBackupLoadTask task = new SuntimesBackupLoadTask(context, uri);
        ExecutorUtils.runTask(SuntimesBackupLoadTask.TAG, task, taskListener);
    }
}
