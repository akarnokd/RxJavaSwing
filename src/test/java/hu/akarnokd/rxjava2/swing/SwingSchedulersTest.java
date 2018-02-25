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

package hu.akarnokd.rxjava2.swing;

import java.util.concurrent.*;

import org.junit.*;

import io.reactivex.Observable;
import io.reactivex.Scheduler.Worker;
import io.reactivex.disposables.Disposable;

public class SwingSchedulersTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(SwingSchedulers.class);
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
}
