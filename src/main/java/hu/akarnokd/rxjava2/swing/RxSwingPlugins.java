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

import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.ExceptionHelper;

/**
 * Static methods to override some of the RxSwing infrastructure, such as
 * the schedulers and task hook.
 */
public final class RxSwingPlugins {

    static volatile Function<Runnable, Runnable> onSchedule;

    static volatile Function<Scheduler, Scheduler> onAsyncScheduler;

    static volatile Function<Scheduler, Scheduler> onSyncScheduler;

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

    public static Scheduler onAsyncScheduler(Scheduler original) {
        Function<Scheduler, Scheduler> f = onAsyncScheduler;
        if (f == null) {
            return original;
        }
        try {
            return f.apply(original);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    public static Scheduler onSyncScheduler(Scheduler original) {
        Function<Scheduler, Scheduler> f = onSyncScheduler;
        if (f == null) {
            return original;
        }
        try {
            return f.apply(original);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }
}
