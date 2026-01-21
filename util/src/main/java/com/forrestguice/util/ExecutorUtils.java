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
import com.forrestguice.util.concurrent.ProgressCallable;
import com.forrestguice.util.concurrent.ProgressInterface;
import com.forrestguice.util.concurrent.ProgressListener;
import com.forrestguice.util.concurrent.TaskHandler;
import com.forrestguice.util.concurrent.TaskHandlerFactory;
import com.forrestguice.util.concurrent.TaskListener;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutorUtils
{
    protected static TaskHandlerFactory handler = null;
    public static void initHandler(TaskHandlerFactory value) {
        handler = value;
    }
    public static TaskHandler getHandler()
    {
        if (handler != null) {
            return handler.getHandler();
        } else {
            Log.e("ExecutorUtils", "TaskHandlerFactory is unset!");
            return null;
        }
    }

    /**
     * runTask (async)
     * @param <T> result type
     * @param <C> Callable<T>
     * @param <L> TaskListener<T>
     * @param callable Callable
     * @param listener TaskListener
     */
    public static <T, C extends Callable<T>,
            L extends TaskListener<T>> void runTask(C callable, L listener)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runTask("Task", executor, getHandler(), callable, Collections.singletonList(listener));
        executor.shutdown();
    }
    public static <T, C extends Callable<T>,
            L extends TaskListener<T>> void runTask(String tag, C callable, L listener)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runTask(tag, executor, getHandler(), callable, Collections.singletonList(listener));
        executor.shutdown();
    }
    public static <T, C extends Callable<T>,
            L extends TaskListener<T>> void runTask(String tag, @NonNull Executor executor, @Nullable TaskHandler handler, C callable, Collection<L> listeners)
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final T result;
                try {
                    postStarted(handler, listeners);
                    result = callable.call();
                    postFinished(handler, result, listeners);

                } catch (Exception e) {
                    Log.e(tag, "runTask: failed! " + e);
                }
            }
        });
    }

    /**
     * runTask (async)
     * @param <T> result type
     * @param <P> progress type
     * @param <C> ProgressCallable<T,P>
     * @param <L> ProgressListener<T,P>>
     * @param callable ProgressCallable
     * @param listener ProgressListener
     */
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(C callable, L listener)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runProgress("ProgressTask", executor, getHandler(), callable, Collections.singletonList(listener));
        executor.shutdown();
    }
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(String tag, C callable, L listener)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runProgress(tag, executor, getHandler(), callable, Collections.singletonList(listener));
        executor.shutdown();
    }
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(@NonNull Executor executor, C callable, L listener) {
        runProgress("ProgressTask", executor, getHandler(), callable, Collections.singletonList(listener));
    }
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(String tag, @NonNull Executor executor, C callable, L listener) {
        runProgress(tag, executor, getHandler(), callable, Collections.singletonList(listener));
    }
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(String tag, @NonNull Executor executor, @Nullable TaskHandler handler, C callable, L listener) {
        runProgress(tag, executor, handler, callable, Collections.singletonList(listener));
    }
    public static <T, P, C extends ProgressCallable<P,T>,
            L extends ProgressListener<T,P>> void runProgress(String tag, @NonNull Executor executor, @Nullable TaskHandler handler, C callable, Collection<L> listeners)
    {
        callable.setProgressInterface(new ProgressInterface<P>()
        {
            @Override
            public void publishProgress(P progress) {
                postProgress(handler, callable, listeners, Collections.singletonList(progress));
            }
            @Override
            public void publishProgress(Collection<P> progress) {
                postProgress(handler, callable, listeners, progress);
            }
        });

        callable.setStatus(ProgressCallable.Status.PENDING);
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final T result;
                try {
                    //Log.d("DEBUG", "runProgress: RUNNING " + callable.toString());
                    callable.setStatus(ProgressCallable.Status.RUNNING);
                    if (!callable.isCancelled())
                    {
                        postCallback(handler, new HandlerCallback() {
                            @Override
                            public void post() {
                                callable.onPreExecute();
                                for (TaskListener<T> listener : listeners) {
                                    listener.onStarted();
                                }
                            }
                        });
                    }
                    result = (callable.isCancelled() ? null : callable.call());
                    if (!callable.isCancelled())
                    {
                        postCallback(handler, new HandlerCallback()
                        {
                            @Override
                            public void post()
                            {
                                callable.onPostExecute(result);
                                for (TaskListener<T> listener : listeners) {
                                    listener.onFinished(result);
                                }
                            }
                        });
                    }
                    callable.setStatus(ProgressCallable.Status.FINISHED);
                    //Log.d("DEBUG", "runProgress: FINISHED " + callable.toString());

                } catch (Exception e) {
                    Log.e(tag, "runProgress: failed! " + e);
                }
            }
        });
    }

    private interface HandlerCallback {
        void post();
    }
    private static <T,A> void postCallback(@NonNull TaskHandler handler, @Nullable HandlerCallback callback)
    {
        if (handler != null && callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.post();
                }
            });
        }
    }
    private static <T,C extends TaskListener<T>> void postStarted(@NonNull TaskHandler handler, @Nullable Collection<C> listeners)
    {
        postCallback(handler, new HandlerCallback() {
            @Override
            public void post() {
                for (TaskListener<T> listener : listeners) {
                    listener.onStarted();
                }
            }
        });
    }
    private static <T,C extends TaskListener<T>> void postFinished(@NonNull TaskHandler handler, T result, @Nullable Collection<C> listeners)
    {
        postCallback(handler, new HandlerCallback() {
            @Override
            public void post() {
                for (TaskListener<T> listener : listeners) {
                    listener.onFinished(result);
                }
            }
        });
    }
    private static <T,P,C extends ProgressListener<T,P>> void postProgress(@NonNull TaskHandler handler, ProgressCallable<P,T> callable, @Nullable Collection<C> listeners, Collection<P> progress)
    {
        postCallback(handler, new HandlerCallback() {
            @Override
            public void post() {
                callable.onProgressUpdate(progress);
                for (ProgressListener<T,P> listener : listeners) {
                    listener.onProgressUpdate(progress);
                }
            }
        });
    }

    /**
     * runTask (async)
     * @param tag tag
     * @param r Runnable
     */
    public static void runTask(String tag, @NonNull Executor executor, @NonNull final Runnable r) {
        executor.execute(r);
    }
    public static void runTask(String tag, @NonNull final Runnable r) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runTask(tag, executor, r);
        executor.shutdown();
    }
    public static void runTask(@NonNull final Runnable r) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        runTask("Runnable", executor, r);
        executor.shutdown();
    }

    /**
     * waitForTask; synchronous
     * @param tag tag
     * @param r Callable<Boolean>
     * @param timeoutAfter will block for timeoutAfter millis
     * @return result
     */
    public static <R extends Callable<Boolean>> boolean waitForTask(String tag, @NonNull final R r, long timeoutAfter)
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
    public static <T,C extends Callable<T>> T getResult(String tag, @NonNull final C callable, long timeoutAfter)
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
