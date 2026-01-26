/**
    Copyright (C) 2026 Forrest Guice
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
package com.forrestguice.util.concurrent;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.Log;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ProgressCallable
 * A Callable that may `publishProgress` during execution; use with `ExecutorUtils.runTask` and ProgressListener.
 * @param <P> progress type
 * @param <T> return type
 * @see ProgressListener
 */
public abstract class ProgressCallable<P, T> implements Callable<T>, ProgressInterface<P>
{
    public void onPreExecute() {}                             // runs on UI thread, runs separately from TaskListener.onStarted
    public void onPostExecute(T result) {}                    // runs on UI thread, runs separately from TaskListener.onFinished
    public void onProgressUpdate(Collection<P> progress) {}   // runs on UI thread
    public void onCancelled(T result) {}                      // runs on UI thread (replaces onFinished when task is cancelled)

    @Override
    public void publishProgress(P progress) {
        if (progressInterface != null) {
            progressInterface.publishProgress(progress);
        } else Log.e("ProgressCallable", "publish: progressInterface is unset!");
    }

    @Override
    public void publishProgress(Collection<P> progress) {
        if (progressInterface != null) {
            progressInterface.publishProgress(progress);
        } else Log.e("ProgressCallable", "publish: progressInterface is unset!");
    }

    protected AtomicBoolean isCancelled = new AtomicBoolean();
    public void cancel() {
        isCancelled.set(true);
    }
    public boolean isCancelled() {
        return isCancelled.get();   //return Thread.currentThread().isInterrupted();
    }

    @Nullable
    protected ProgressInterface<P> progressInterface = null;
    public void setProgressInterface(@Nullable ProgressInterface<P> value) {
        progressInterface = value;
    }

    public static enum Status { PENDING, RUNNING, FINISHED }
    synchronized public Status getStatus() {
        return status;
    }
    synchronized public void setStatus(Status value) {
        status = value;
    }
    protected Status status = Status.FINISHED;
}
