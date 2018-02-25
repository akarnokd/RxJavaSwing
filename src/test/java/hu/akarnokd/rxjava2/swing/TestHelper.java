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

import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Scheduler;
import io.reactivex.exceptions.*;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.util.ExceptionHelper;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * Test helper methods.
 */
public final class TestHelper {

    /**
     * Number of times to loop a {@link #race(Runnable, Runnable)} invocation
     * by default.
     */
    public static final int RACE_DEFAULT_LOOPS = 2500;

    /**
     * Number of times to loop a {@link #race(Runnable, Runnable)} invocation
     * in tests with race conditions requiring more runs to check.
     */
    public static final int RACE_LONG_LOOPS = 10000;

    private TestHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Validates that the given class, when forcefully instantiated throws
     * an IllegalArgumentException("No instances!") exception.
     * @param clazz the class to test, not null
     */
    public static void checkUtilityClass(Class<?> clazz) {
        try {
            Constructor<?> c = clazz.getDeclaredConstructor();

            c.setAccessible(true);

            try {
                c.newInstance();
                fail("Should have thrown InvocationTargetException(IllegalStateException)");
            } catch (InvocationTargetException ex) {
                assertEquals("No instances!", ex.getCause().getMessage());
            }
        } catch (Exception ex) {
            AssertionError ae = new AssertionError(ex.toString());
            ae.initCause(ex);
            throw ae;
        }
    }

    public static List<Throwable> trackPluginErrors() {
        final List<Throwable> list = Collections.synchronizedList(new ArrayList<Throwable>());

        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable t) {
                list.add(t);
            }
        });

        return list;
    }

    public static void assertError(List<Throwable> list, int index, Class<? extends Throwable> clazz) {
        Throwable ex = list.get(index);
        if (!clazz.isInstance(ex)) {
            AssertionError err = new AssertionError(clazz + " expected but got " + list.get(index));
            err.initCause(list.get(index));
            throw err;
        }
    }

    public static void assertUndeliverable(List<Throwable> list, int index, Class<? extends Throwable> clazz) {
        Throwable ex = list.get(index);
        if (!(ex instanceof UndeliverableException)) {
            AssertionError err = new AssertionError("Outer exception UndeliverableException expected but got " + list.get(index));
            err.initCause(list.get(index));
            throw err;
        }
        ex = ex.getCause();
        if (!clazz.isInstance(ex)) {
            AssertionError err = new AssertionError("Inner exception " + clazz + " expected but got " + list.get(index));
            err.initCause(list.get(index));
            throw err;
        }
    }

    public static void assertError(List<Throwable> list, int index, Class<? extends Throwable> clazz, String message) {
        Throwable ex = list.get(index);
        if (!clazz.isInstance(ex)) {
            AssertionError err = new AssertionError("Type " + clazz + " expected but got " + ex);
            err.initCause(ex);
            throw err;
        }
        if (!ObjectHelper.equals(message, ex.getMessage())) {
            AssertionError err = new AssertionError("Message " + message + " expected but got " + ex.getMessage());
            err.initCause(ex);
            throw err;
        }
    }

    public static void assertUndeliverable(List<Throwable> list, int index, Class<? extends Throwable> clazz, String message) {
        Throwable ex = list.get(index);
        if (!(ex instanceof UndeliverableException)) {
            AssertionError err = new AssertionError("Outer exception UndeliverableException expected but got " + list.get(index));
            err.initCause(list.get(index));
            throw err;
        }
        ex = ex.getCause();
        if (!clazz.isInstance(ex)) {
            AssertionError err = new AssertionError("Inner exception " + clazz + " expected but got " + list.get(index));
            err.initCause(list.get(index));
            throw err;
        }
        if (!ObjectHelper.equals(message, ex.getMessage())) {
            AssertionError err = new AssertionError("Message " + message + " expected but got " + ex.getMessage());
            err.initCause(ex);
            throw err;
        }
    }
    /**
     * Synchronizes the execution of two runnables (as much as possible)
     * to test race conditions.
     * <p>The method blocks until both have run to completion.
     * @param r1 the first runnable
     * @param r2 the second runnable
     * @see #RACE_DEFAULT_LOOPS
     * @see #RACE_LONG_LOOPS
     */
    public static void race(final Runnable r1, final Runnable r2) {
        race(r1, r2, Schedulers.single());
    }
    /**
     * Synchronizes the execution of two runnables (as much as possible)
     * to test race conditions.
     * <p>The method blocks until both have run to completion.
     * @param r1 the first runnable
     * @param r2 the second runnable
     * @param s the scheduler to use
     * @see #RACE_DEFAULT_LOOPS
     * @see #RACE_LONG_LOOPS
     */
    public static void race(final Runnable r1, final Runnable r2, Scheduler s) {
        final AtomicInteger count = new AtomicInteger(2);
        final CountDownLatch cdl = new CountDownLatch(2);

        final Throwable[] errors = { null, null };

        s.scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (count.decrementAndGet() != 0) {
                    while (count.get() != 0) { }
                }

                try {
                    try {
                        r1.run();
                    } catch (Throwable ex) {
                        errors[0] = ex;
                    }
                } finally {
                    cdl.countDown();
                }
            }
        });

        if (count.decrementAndGet() != 0) {
            while (count.get() != 0) { }
        }

        try {
            try {
                r2.run();
            } catch (Throwable ex) {
                errors[1] = ex;
            }
        } finally {
            cdl.countDown();
        }

        try {
            if (!cdl.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("The wait timed out!");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        if (errors[0] != null && errors[1] == null) {
            throw ExceptionHelper.wrapOrThrow(errors[0]);
        }

        if (errors[0] == null && errors[1] != null) {
            throw ExceptionHelper.wrapOrThrow(errors[1]);
        }

        if (errors[0] != null && errors[1] != null) {
            throw new CompositeException(errors);
        }
    }
}
