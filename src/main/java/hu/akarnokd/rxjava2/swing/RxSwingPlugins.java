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

import io.reactivex.*;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.ExceptionHelper;

/**
 * Static methods to override some of the RxSwing infrastructure, such as
 * the scheduler and task hook.
 */
public final class RxSwingPlugins {

    private static volatile Function<Runnable, Runnable> onSchedule;

    private static volatile Function<Scheduler, Scheduler> onEdtScheduler;

    @SuppressWarnings("rawtypes")
    private static volatile Function<Observable, Observable> onAssembly;

    /** Utility class. */
    private RxSwingPlugins() {
        throw new IllegalStateException("No instances!");
    }

    public static Runnable onSchedule(Runnable run) {
        Function<Runnable, Runnable> f = onSchedule;
        if (f == null) {
            return run;
        }
        try {
            return f.apply(run);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    public static Scheduler onEdtScheduler(Scheduler original) {
        Function<Scheduler, Scheduler> f = onEdtScheduler;
        if (f == null) {
            return original;
        }
        try {
            return f.apply(original);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Observable<T> onAssembly(Observable<T> original) {
        Function<Observable, Observable> f = onAssembly;
        if (f == null) {
            return original;
        }
        try {
            return f.apply(original);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    public static Function<Runnable, Runnable> getOnSchedule() {
        return onSchedule;
    }

    public static void setOnSchedule(Function<Runnable, Runnable> handler) {
        onSchedule = handler;
    }

    public static Function<Scheduler, Scheduler> getOnEdtScheduler() {
        return onEdtScheduler;
    }

    public static void setOnEdtScheduler(Function<Scheduler, Scheduler> handler) {
        onEdtScheduler = handler;
    }

    @SuppressWarnings("rawtypes")
    public static Function<Observable, Observable> getOnAssembly() {
        return onAssembly;
    }

    @SuppressWarnings("rawtypes")
    public static void setOnAssembly(Function<Observable, Observable> handler) {
        onAssembly = handler;
    }

    public static void reset() {
        onSchedule = null;
        onEdtScheduler = null;
        onAssembly = null;
    }
}
