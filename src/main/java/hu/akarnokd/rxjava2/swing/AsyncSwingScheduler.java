/*
 * Copyright 2017 David Karnok
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

package hu.akarnokd.rxjava2.swing;

import java.awt.EventQueue;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;

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
                (int)Math.max(0, unit.toMillis(delay))
        );
        dtt.start();
        return dtt;
    }

    @Override
    public Disposable schedulePeriodicallyDirect(Runnable run, long initialDelay, long period, TimeUnit unit) {
        DirectTimedTask dtt = new DirectTimedTask(
                RxSwingPlugins.onSchedule(run),
                (int)unit.toMillis(initialDelay),
                (int)Math.max(0, unit.toMillis(period))
        );
        dtt.start();
        return dtt;
    }

    @Override
    public Worker createWorker() {
        return new AsyncSwingWorker();
    }

    static final class AsyncSwingWorker extends Worker {

        volatile boolean disposed;
        
        
        @Override
        public void dispose() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isDisposed() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Disposable schedule(Runnable run) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Disposable schedulePeriodically(Runnable run, long initialDelay, long period, TimeUnit unit) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    static final class DirectTask extends AtomicReference<Runnable> implements Runnable, Disposable {

        private static final long serialVersionUID = -4645934389976373118L;

        DirectTask(Runnable run) {
            lazySet(run);
        }

        @Override
        public void run() {
            Runnable r = get();
            if (r != null && compareAndSet(r, null)) {
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

        Runnable run;

        DirectTimedTask(Runnable run, int initialDelayMillis, int periodMillis) {
            super(0, null);
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
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }
    }
}
