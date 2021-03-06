/*
 * Copyright 2017-2018 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.akarnokd.rxjava3.swing;

import java.awt.EventQueue;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * An EDT scheduler that submits runnables to the EventQueue or creates swing
 * Timers for each task submitted.
 */
final class AsyncSwingScheduler extends Scheduler {

    static final Scheduler INSTANCE = new AsyncSwingScheduler();

    @Override
    public Disposable scheduleDirect(Runnable run) {
        DirectTask dt = new DirectTask(RxSwingPlugins.onSchedule(run));
        EventQueue.invokeLater(dt);
        return dt;
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        DirectTimedTask dtt = new DirectTimedTask(
                RxSwingPlugins.onSchedule(run),
                (int)unit.toMillis(delay),
                (int)Math.max(0, unit.toMillis(delay)), false
        );
        dtt.start();
        return dtt;
    }

    @Override
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        DirectTimedTask dtt = new DirectTimedTask(
                RxSwingPlugins.onSchedule(run),
                (int)unit.toMillis(initialDelay),
                (int)Math.max(0, unit.toMillis(period)), true
        );
        dtt.start();
        return dtt;
    }

    @Override
    public Worker createWorker() {
        return new AsyncSwingWorker();
    }

    static final class AsyncSwingWorker extends Worker {

        CompositeDisposable tasks;

        AsyncSwingWorker() {
            this.tasks = new CompositeDisposable();
        }

        @Override
        public void dispose() {
            tasks.dispose();
        }

        @Override
        public boolean isDisposed() {
            return tasks.isDisposed();
        }

        void remove(Disposable d) {
            tasks.delete(d);
        }

        boolean add(Disposable d) {
            return tasks.add(d);
        }

        @Override
        public Disposable schedule(Runnable run) {
            WorkerTask wt = new WorkerTask(RxSwingPlugins.onSchedule(run));
            if (add(wt)) {
                EventQueue.invokeLater(wt);
                return wt;
            }
            return EmptyDisposable.INSTANCE;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            WorkerTimedTask wtt = new WorkerTimedTask(
                    RxSwingPlugins.onSchedule(run),
                    (int)unit.toMillis(delay),
                    (int)Math.max(0, unit.toMillis(delay)), false
            );
            if (add(wtt)) {
                wtt.start();
                return wtt;
            }
            return EmptyDisposable.INSTANCE;
        }

        @Override
        public Disposable schedulePeriodically(Runnable run, long initialDelay, long period, TimeUnit unit) {
            WorkerTimedTask wtt = new WorkerTimedTask(
                    RxSwingPlugins.onSchedule(run),
                    (int)unit.toMillis(initialDelay),
                    (int)Math.max(0, unit.toMillis(period)), true
            );
            if (add(wtt)) {
                wtt.start();
                return wtt;
            }
            return EmptyDisposable.INSTANCE;
        }

        final class WorkerTask extends AtomicReference<Runnable> implements Runnable, Disposable {

            private static final long serialVersionUID = 3954858753004137205L;

            WorkerTask(Runnable run) {
                lazySet(run);
            }

            @Override
            public void dispose() {
                if (getAndSet(null) != null) {
                    remove(this);
                }
            }

            @Override
            public boolean isDisposed() {
                return get() == null;
            }

            @Override
            public void run() {
                Runnable r = getAndSet(null);
                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        RxJavaPlugins.onError(ex);
                    }
                    remove(this);
                }
            }
        }

        final class WorkerTimedTask extends Timer implements ActionListener, Disposable {

            private static final long serialVersionUID = 1146820542834025296L;

            final boolean periodic;

            Runnable run;

            WorkerTimedTask(Runnable run, int initialDelayMillis, int periodMillis, boolean periodic) {
                super(0, null);
                this.run = run;
                this.periodic = periodic;
                setInitialDelay(initialDelayMillis);
                setDelay(periodMillis);
                addActionListener(this);
            }

            @Override
            public void dispose() {
                run = null;
                stop();
                remove(this);
            }

            @Override
            public boolean isDisposed() {
                return run == null;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Runnable r = run;
                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable ex) {
                        run = null;
                        stop();
                        remove(this);
                        Exceptions.throwIfFatal(ex);
                        RxJavaPlugins.onError(ex);
                        return;
                    }
                    if (!periodic) {
                        run = null;
                        stop();
                        remove(this);
                    }
                }
            }
        }
    }

    static final class DirectTask extends AtomicReference<Runnable> implements Runnable, Disposable {

        private static final long serialVersionUID = -4645934389976373118L;

        DirectTask(Runnable run) {
            lazySet(run);
        }

        @Override
        public void run() {
            Runnable r = getAndSet(null);
            if (r != null) {
                try {
                    r.run();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }

        @Override
        public void dispose() {
            getAndSet(null);
        }

        @Override
        public boolean isDisposed() {
            return get() == null;
        }
    }

    static final class DirectTimedTask extends Timer implements ActionListener, Disposable {

        private static final long serialVersionUID = 1146820542834025296L;

        final boolean periodic;

        Runnable run;

        DirectTimedTask(Runnable run, int initialDelayMillis, int periodMillis, boolean periodic) {
            super(0, null);
            this.run = run;
            this.periodic = periodic;
            setInitialDelay(initialDelayMillis);
            setDelay(periodMillis);
            addActionListener(this);
        }

        @Override
        public void dispose() {
            run = null;
            stop();
        }

        @Override
        public boolean isDisposed() {
            return run == null;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable r = run;
            if (r != null) {
                try {
                    r.run();
                } catch (Throwable ex) {
                    run = null;
                    stop();
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                    return;
                }
                if (!periodic) {
                    run = null;
                    stop();
                }
            }
        }
    }
}
