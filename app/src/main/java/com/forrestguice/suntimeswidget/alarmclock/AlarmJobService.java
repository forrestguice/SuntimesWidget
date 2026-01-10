/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

@TargetApi(24)
public class AlarmJobService extends JobService
{
    public static final String TAG = "AlarmJobService";
    public static final int JOB_AFTER_BOOT_COMPLETED = 10;

    /**
     * @param params job params
     * @return true if job is still ongoing, false if job is finished (releases wakelock)
     */
    @Override
    public boolean onStartJob(JobParameters params)
    {
        Log.d(TAG, "onJobStart: " + params.getJobId());
        switch (params.getJobId())
        {
            case JOB_AFTER_BOOT_COMPLETED:
                return onJobAfterBootCompleted();

            default:
                Log.w(TAG, "onJobStart: jobId " + params.getJobId() + " not recognized!");
                return false;    // false; job is finished
        }
    }

    /**
     * onStopJob runs if job is stopped before successful completion
     * @param params params
     * @return true if job should be retried
     */
    @Override
    public boolean onStopJob(JobParameters params)
    {
        Log.w(TAG, "onJobStop: " + params.getJobId());
        return false;   // false; no retry
    }

    protected boolean onJobAfterBootCompleted()
    {
        Context context = getApplicationContext();
        sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_AFTER_BOOT_COMPLETED, null));
        return false;
    }

    public static void scheduleJobAfterBootCompleted(Context context)
    {
        long delay = AlarmSettings.bootCompletedDelay(context);
        JobInfo.Builder job = new JobInfo.Builder(AlarmJobService.JOB_AFTER_BOOT_COMPLETED, new ComponentName(context, AlarmJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setMinimumLatency(delay)
                .setOverrideDeadline(delay + AlarmSettings.AFTER_BOOT_COMPLETED_WINDOW_MS);

        JobScheduler jobs = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobs.schedule(job.build());
    }

}
