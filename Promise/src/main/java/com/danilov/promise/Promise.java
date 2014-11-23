package com.danilov.promise;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Semyon on 10.11.2014.
 */
public class Promise<Type> {

    protected static ThreadPoolExecutor defaultExecutor = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    private Type data;

    private boolean isDone = false;

    private boolean isSuccessful = false;

    private Action<Type, Void> onFinish = null;

    private Action<Exception, Void> onCatch = null;

    private boolean finishExecuted = false;

    public void finish(final Type data, final boolean success) {
        boolean shouldExecute = false;
        synchronized (this) {
            isDone = true;
            isSuccessful = success;
            this.data = data;
            if (onFinish != null && !finishExecuted) {
                shouldExecute = true;
                finishExecuted = true;
            }
        }
        if (shouldExecute) {
            onFinish.action(data, success);
        }
    }

    //TODO: what if already executed and got exception?
    public void exception(final Exception e) {
        if (onCatch != null) {
            onCatch.action(e, false);
        }
    }

    public <X> Promise<X> catchException(final Action<Exception, X> handler) {
        final Promise<X> nPromise = new Promise<X>();
        boolean shouldExecute = false;
        synchronized (this) {
            this.onCatch = new Action<Exception, Void>() {
                @Override
                public Void action(final Exception e, final boolean success) {
                    X _data = handler.action(e, isSuccessful);
                    nPromise.finish(_data, true);
                    return null;
                }
            };
            if (isDone && !finishExecuted) {
                shouldExecute = true;
                finishExecuted = true;
            }
        }
        if (shouldExecute) {
            this.onFinish.action(data, isSuccessful);
        }
        return nPromise;
    }

    public <X> Promise<X> then(final Action<Type, X> action) {
        final Promise<X> nPromise = new Promise<X>();
        boolean shouldExecute = false;
        synchronized (this) {
            this.onFinish = new Action<Type, Void>() {
                @Override
                public Void action(final Type data, final boolean success) {
                    X _data = action.action(data, isSuccessful);
                    nPromise.finish(_data, true);
                    return null;
                }
            };
            if (isDone && !finishExecuted) {
                shouldExecute = true;
                finishExecuted = true;
            }
        }
        if (shouldExecute) {
            this.onFinish.action(data, isSuccessful);
        }
        return nPromise;
    }

    public static <X> Promise<X> resolve(final X data) {
        Promise<X> promise = new Promise<X>();
        promise.finish(data, true);
        return promise;
    }

    public static Promise<Object[]> all(final Promise... promises) {
        int count = promises.length;
        final Promise<Object[]> promise = new Promise<Object[]>();
        final Counter counter = new Counter();
        counter.count = count;
        final Object[] dataArray = new Object[promises.length];
        for (int i = 0; i < count; i++) {
            final int num = i;
            promises[i].then(new Action() {
                @Override
                public Object action(final Object data, final boolean success) {
                    dataArray[num] = data;
                    boolean shouldFinish = false;
                    synchronized (counter) {
                        counter.count--;
                        if (counter.count == 0) {
                            shouldFinish = true;
                        }
                    }
                    if (shouldFinish) {
                        promise.finish(dataArray, true);
                    }
                    return null;
                }
            });
        }
        return promise;
    }

    private static class Counter {

        public int count = 0;

    }

    public static interface Action<Type, Return> {
        public Return action(final Type data, final boolean success);
    }

    public static <X> Promise<X> run(final Callable<X> callable) {
        final Promise<X> promise = new Promise<X>();
        defaultExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    X data = callable.call();
                    promise.finish(data, true);
                } catch (Exception e) {
                    promise.exception(e);
                }
            }
        });
        return promise;
    }

    public static <X> Promise<X> run(final PromiseRunnable<X> runnable, final boolean async) {
        final Promise<X> promise = new Promise<X>();
        if (async) {
            defaultExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run(promise.new Resolver());
                    } catch (Exception e) {
                        promise.exception(e);
                    }
                }
            });
        } else {
            try {
                runnable.run(promise.new Resolver());
            } catch (Exception e) {
                promise.exception(e);
            }
        }
        return promise;
    }

    public class Resolver {

        public void resolve(final Type data) {
            finish(data, true);
        }

        public void reject(final String error) {
            finish(null, false);
        }

        public void except(final Exception e) {
            exception(e);
        }

    }

    public interface PromiseRunnable<RunnableType> {

        public void run(final Promise<RunnableType>.Resolver resolver);

    }

}