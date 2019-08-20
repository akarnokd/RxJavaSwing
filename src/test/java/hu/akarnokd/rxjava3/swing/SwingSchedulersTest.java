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

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.*;

import org.junit.*;

import hu.akarnokd.rxjava3.swing.AsyncSwingScheduler.*;
import hu.akarnokd.rxjava3.swing.AsyncSwingScheduler.AsyncSwingWorker.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class SwingSchedulersTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(SwingSchedulers.class);
    }

    @Test
    public void holder() {
        new SwingSchedulers.AsyncHolder();
    }

    @Test
    public void basic() {
        Observable.range(1, 5)
        .observeOn(SwingSchedulers.edt())
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void observeOnEdt() {
        Observable.range(1, 5)
        .compose(SwingObservable.<Integer>observeOnEdt())
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(1, 2, 3, 4, 5);
    }

    static final class Task extends CountDownLatch implements Runnable {

        volatile int calls;

        Task(int n) {
            super(n);
        }

        @Override
        public void run() {
            calls++;
            countDown();
        }

    }

    @Test
    public void direct() throws Exception {
        Task t = new Task(1);

        SwingSchedulers.edt().scheduleDirect(t);

        Assert.assertTrue(t.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void directDelay() throws Exception {
        Task t = new Task(1);

        SwingSchedulers.edt().scheduleDirect(t, 100, TimeUnit.MILLISECONDS);

        Assert.assertTrue(t.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void directPeriodic() throws Exception {
        Task t = new Task(3);

        Disposable d = SwingSchedulers.edt().schedulePeriodicallyDirect(t, 100, 100, TimeUnit.MILLISECONDS);

        Assert.assertTrue(t.await(5, TimeUnit.SECONDS));

        d.dispose();

        Thread.sleep(500);

        Assert.assertEquals(3, t.calls);
    }

    @Test
    public void worker() throws Exception {
        Task t = new Task(1);

        Worker w = SwingSchedulers.edt().createWorker();
        try {
            w.schedule(t);

            Assert.assertTrue(t.await(5, TimeUnit.SECONDS));
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerDelay() throws Exception {
        Task t = new Task(1);

        Worker w = SwingSchedulers.edt().createWorker();
        try {
            w.schedule(t, 100, TimeUnit.MILLISECONDS);

            Assert.assertTrue(t.await(5, TimeUnit.SECONDS));
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerPeriodic() throws Exception {
        Task t = new Task(3);

        Worker w = SwingSchedulers.edt().createWorker();
        try {
            Disposable d = w.schedulePeriodically(t, 100, 100, TimeUnit.MILLISECONDS);

            Assert.assertTrue(t.await(5, TimeUnit.SECONDS));

            d.dispose();

            Thread.sleep(500);

            Assert.assertEquals(3, t.calls);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerDispose() throws Exception {
        Task t = new Task(1);

        Worker w = SwingSchedulers.edt().createWorker();
        try {
            Disposable d = w.schedule(t, 500, TimeUnit.MILLISECONDS);

            Thread.sleep(100);

            d.dispose();

            Thread.sleep(500);

            Assert.assertEquals(0, t.calls);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void tasksAfterWorkerDispose() {
        Worker w = SwingSchedulers.edt().createWorker();
        try {
            assertFalse(w.isDisposed());

            w.dispose();

            assertTrue(w.isDisposed());

            assertSame(EmptyDisposable.INSTANCE, w.schedule(Functions.EMPTY_RUNNABLE));
            assertSame(EmptyDisposable.INSTANCE, w.schedule(Functions.EMPTY_RUNNABLE, 1, TimeUnit.MILLISECONDS));
            assertSame(EmptyDisposable.INSTANCE, w.schedulePeriodically(Functions.EMPTY_RUNNABLE, 1, 1, TimeUnit.MILLISECONDS));
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerTask() {
        AsyncSwingWorker w = (AsyncSwingWorker)SwingSchedulers.edt().createWorker();
        try {
            final int[] calls = { 0 };

            WorkerTask wt = w.new WorkerTask(new Runnable() {
                @Override
                public void run() {
                    calls[0]++;
                }
            });

            assertFalse(wt.isDisposed());

            wt.dispose();

            assertTrue(wt.isDisposed());

            wt.run();

            assertEquals(0, calls[0]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerTaskCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
            AsyncSwingWorker w = (AsyncSwingWorker)SwingSchedulers.edt().createWorker();
            try {
                final int[] calls = { 0 };

                WorkerTask wt = w.new WorkerTask(new Runnable() {
                    @Override
                    public void run() {
                        calls[0]++;
                        throw new IllegalStateException();
                    }
                });

                assertFalse(wt.isDisposed());

                wt.run();

                wt.dispose();

                assertTrue(wt.isDisposed());

                assertEquals(1, calls[0]);
            } finally {
                w.dispose();
            }

            assertEquals(1, errors.size());
            TestHelper.assertError(errors, 0, IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void workerTaskRunDisposeRace() {
        AsyncSwingWorker w = (AsyncSwingWorker)SwingSchedulers.edt().createWorker();
        try {
            final WorkerTask wt = w.new WorkerTask(Functions.EMPTY_RUNNABLE);

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    wt.dispose();
                }
            };

            TestHelper.race(wt, r2);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerTimedTask() {
        AsyncSwingWorker w = (AsyncSwingWorker)SwingSchedulers.edt().createWorker();
        try {
            final int[] calls = { 0 };

            WorkerTimedTask wt = w.new WorkerTimedTask(new Runnable() {
                @Override
                public void run() {
                    calls[0]++;
                }
            }, 1, 1, false);

            assertFalse(wt.isDisposed());

            wt.dispose();

            assertTrue(wt.isDisposed());

            wt.actionPerformed(null);

            assertEquals(0, calls[0]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void workerTimedTaskCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
            AsyncSwingWorker w = (AsyncSwingWorker)SwingSchedulers.edt().createWorker();
            try {
                final int[] calls = { 0 };

                WorkerTimedTask wt = w.new WorkerTimedTask(new Runnable() {
                    @Override
                    public void run() {
                        calls[0]++;
                        throw new IllegalStateException();
                    }
                }, 1, 1, false);

                assertFalse(wt.isDisposed());

                wt.actionPerformed(null);

                wt.dispose();

                assertTrue(wt.isDisposed());

                assertEquals(1, calls[0]);
            } finally {
                w.dispose();
            }

            assertEquals(1, errors.size());
            TestHelper.assertError(errors, 0, IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void workerDirectTask() {
        final int[] calls = { 0 };

        DirectTask wt = new DirectTask(new Runnable() {
            @Override
            public void run() {
                calls[0]++;
            }
        });

        assertFalse(wt.isDisposed());

        wt.dispose();

        assertTrue(wt.isDisposed());

        wt.run();

        assertEquals(0, calls[0]);
    }

    @Test
    public void workerDirectTaskCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final int[] calls = { 0 };

            DirectTask wt = new DirectTask(new Runnable() {
                @Override
                public void run() {
                    calls[0]++;
                    throw new IllegalStateException();
                }
            });

            assertFalse(wt.isDisposed());

            wt.run();

            wt.dispose();

            assertTrue(wt.isDisposed());

            assertEquals(1, calls[0]);

            assertEquals(1, errors.size());
            TestHelper.assertError(errors, 0, IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void workerDirectTimedTask() {
        final int[] calls = { 0 };

        DirectTimedTask wt = new DirectTimedTask(new Runnable() {
            @Override
            public void run() {
                calls[0]++;
            }
        }, 1, 1, false);

        assertFalse(wt.isDisposed());

        wt.dispose();

        assertTrue(wt.isDisposed());

        wt.actionPerformed(null);

        assertEquals(0, calls[0]);
    }

    @Test
    public void workerDirectTimedTaskCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final int[] calls = { 0 };

            DirectTimedTask wt = new DirectTimedTask(new Runnable() {
                @Override
                public void run() {
                    calls[0]++;
                    throw new IllegalStateException();
                }
            }, 1, 1, false);

            assertFalse(wt.isDisposed());

            wt.actionPerformed(null);

            wt.dispose();

            assertTrue(wt.isDisposed());

            assertEquals(1, calls[0]);

            assertEquals(1, errors.size());
            TestHelper.assertError(errors, 0, IllegalStateException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
