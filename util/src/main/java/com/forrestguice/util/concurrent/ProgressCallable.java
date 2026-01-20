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

import java.util.concurrent.Callable;

/**
 * ProgressCallable
 * A Callable that may `signalProgress` during execution; use with `ExecutorUtils.runTask` and ProgressListener.
 * @param <P> progress type
 * @param <T> return type
 * @see ProgressListener
 */
public abstract class ProgressCallable<P, T> implements Callable<T>, ProgressInterface<P>
{
    @Override
    public void publishProgress(P[] progress) {
        if (progressInterface != null) {
            progressInterface.publishProgress(progress);
        } else Log.e("ProgressCallable", "progressInterface is unset!");
    }

    @Nullable
    protected ProgressInterface<P> progressInterface = null;
    public void setProgressInterface(@Nullable ProgressInterface<P> value) {
        progressInterface = value;
    }
}
