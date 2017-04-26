/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.rxjava2.swing;

import java.util.concurrent.TimeUnit;

import org.junit.*;

import io.reactivex.*;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

public class RxSwingPluginsTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(RxSwingPlugins.class);
    }

    @After
    public void after() {
        RxSwingPlugins.reset();
    }

    @Test
    public void onSchedulePassthrough() {
        Assert.assertSame(Functions.EMPTY_RUNNABLE, RxSwingPlugins.onSchedule(Functions.EMPTY_RUNNABLE));
    }

    static final class RunnableWrapper implements Runnable {
        final Runnable run;

        RunnableWrapper(Runnable run) {
            this.run = run;
        }

        @Override
        public void run() {
            run.run();
        }
    }

    @Test
    public void onSchedule() {
        RxSwingPlugins.setOnSchedule(new Function<Runnable, Runnable>() {
            @Override
            public Runnable apply(final Runnable r) throws Exception {
                return new RunnableWrapper(r);
            }
        });
        Assert.assertNotNull(RxSwingPlugins.getOnSchedule());

        Runnable r = RxSwingPlugins.onSchedule(Functions.EMPTY_RUNNABLE);

        Assert.assertTrue(r.getClass().toString(), r instanceof RunnableWrapper);

        r.run();

        RxSwingPlugins.reset();

        Assert.assertNull(RxSwingPlugins.getOnSchedule());

        Assert.assertSame(Functions.EMPTY_RUNNABLE, RxSwingPlugins.onSchedule(Functions.EMPTY_RUNNABLE));
    }

    @Test
    public void onScheduleCrashes() {
        RxSwingPlugins.setOnSchedule(new Function<Runnable, Runnable>() {
            @Override
            public Runnable apply(Runnable r) throws Exception {
                throw new IllegalStateException("Failure");
            }
        });

        try {
            RxSwingPlugins.onSchedule(Functions.EMPTY_RUNNABLE);
            Assert.fail("Should have thrown!");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Failure", ex.getMessage());
        }

        RxSwingPlugins.reset();

        Assert.assertSame(Functions.EMPTY_RUNNABLE, RxSwingPlugins.onSchedule(Functions.EMPTY_RUNNABLE));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void onAssembyCrashes() {
        RxSwingPlugins.setOnAssembly(new Function<Observable, Observable>() {
            @Override
            public Observable apply(Observable r) throws Exception {
                throw new IllegalStateException("Failure");
            }
        });

        try {
            RxSwingPlugins.onAssembly(Observable.just(1));
            Assert.fail("Should have thrown!");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Failure", ex.getMessage());
        }

        RxSwingPlugins.reset();

        Observable<Integer> o = Observable.just(1);
        Assert.assertSame(o, RxSwingPlugins.onAssembly(o));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void onAssemby() {
        RxSwingPlugins.setOnAssembly(new Function<Observable, Observable>() {
            @Override
            public Observable apply(Observable r) throws Exception {
                return Observable.just(2);
            }
        });

        Assert.assertNotNull(RxSwingPlugins.getOnAssembly());

        Observable<Integer> o = RxSwingPlugins.onAssembly(Observable.just(1));

        o.test().assertResult(2);

        RxSwingPlugins.reset();

        Assert.assertNull(RxSwingPlugins.getOnAssembly());

        o = Observable.just(1);
        Assert.assertSame(o, RxSwingPlugins.onAssembly(o));
    }

    @Test
    public void onEdtSchedulerCrashes() {
        RxSwingPlugins.setOnEdtScheduler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler r) throws Exception {
                throw new IllegalStateException("Failure");
            }
        });

        try {
            RxSwingPlugins.onEdtScheduler(Schedulers.computation());
            Assert.fail("Should have thrown!");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Failure", ex.getMessage());
        }

        RxSwingPlugins.reset();

        Assert.assertSame(Schedulers.computation(), RxSwingPlugins.onEdtScheduler(Schedulers.computation()));
    }

    @Test
    public void onEdtScheduler() {
        RxSwingPlugins.setOnEdtScheduler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler r) throws Exception {
                return Schedulers.computation();
            }
        });
        Assert.assertNotNull(RxSwingPlugins.getOnEdtScheduler());

        Observable.just(1)
        .observeOn(SwingSchedulers.edt())
        .map(new Function<Integer, Object>() {
            @Override
            public Object apply(Integer v) throws Exception {
                return Thread.currentThread().getName().contains("Computation");
            }
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(true);

        RxSwingPlugins.reset();

        Assert.assertNull(RxSwingPlugins.getOnEdtScheduler());

        Observable.just(1)
        .observeOn(SwingSchedulers.edt())
        .map(new Function<Integer, Object>() {
            @Override
            public Object apply(Integer v) throws Exception {
                return Thread.currentThread().getName().contains("Computation");
            }
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(false);
    }
}
