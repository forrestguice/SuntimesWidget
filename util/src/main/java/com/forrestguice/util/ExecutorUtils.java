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

package com.forrestguice.util;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.concurrent.TaskHandler;
import com.forrestguice.util.concurrent.TaskListener;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutorUtils
{
    /**
     * runTask (async)
     * @param tag tag
     * @param handler TaskHandler
     * @param callable Callable
     * @param listener TaskListener
     * @param <T> result type
     */
    public static <T> void runTask(String tag, @Nullable TaskHandler handler, Callable<T> callable, TaskListener<T> listener)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final T result;
                try {
                    postStarted(handler, listener);
                    result = callable.call();
                    postFinished(handler, result, listener);

                } catch (Exception e) {
                    Log.e(tag, "runTask: failed! " + e);
                }
            }
        });
    }
    private static <T> void postStarted(@NonNull TaskHandler handler, TaskListener<T> listener)
    {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStarted();
                }
            });
        }
    }
    private static <T> void postFinished(@NonNull TaskHandler handler, T result, TaskListener<T> listener)
    {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFinished(result);
                }
            });
        }
    }

    /**
     * runTask (async)
     * @param tag tag
     * @param r Runnable
     */
    public static void runTask(String tag, @NonNull final Runnable r)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(r);
    }

    /**
     * waitForTask; synchronous
     * @param tag tag
     * @param r Callable<Boolean>
     * @param timeoutAfter will block for timeoutAfter millis
     * @return result
     */
    public static boolean waitForTask(String tag, @NonNull final Callable<Boolean> r, long timeoutAfter)
    {
        Boolean result = getResult(tag, r, timeoutAfter);
        return (result != null && result);
    }

    /**
     * getResult; synchronous
     * @param tag tag
     * @param callable Callable
     * @param timeoutAfter will block for timeoutAfter millis
     * @param <T> result type
     * @return result
     */
    @Nullable
    public static <T> T getResult(String tag, @NonNull final Callable<T> callable, long timeoutAfter)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> task = executor.submit(callable);
        try {
            return task.get(timeoutAfter, TimeUnit.MILLISECONDS);

        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            Log.e(tag, "getResult: failed! " + e);
            return null;

        } finally {
            task.cancel(true);
            executor.shutdownNow();
        }
    }

    // same as above, except using CompletableFuture
    /*@TargetApi(24)
    @Nullable
    public static <T> T getResult(String tag, @NonNull final Callable<T> callable, long timeoutAfter)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final CompletableFuture<T> future = new CompletableFuture<>();
        final Future<?> task = executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                if (Build.VERSION.SDK_INT >= 24)
                {
                    try {
                        future.complete(callable.call());
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                }
            }
        });

        try {
            return future.get(timeoutAfter, TimeUnit.MILLISECONDS);

        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            Log.e(tag, "getResult: failed! " + e);
            return null;

        } finally {
            task.cancel(true);
            executor.shutdownNow();
        }
    }*/
}
