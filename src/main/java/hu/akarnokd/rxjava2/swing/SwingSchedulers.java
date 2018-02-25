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

import io.reactivex.Scheduler;

/**
 * Hosts the default {@link Scheduler}s for RxSwing: {@link #edt()}.
 */
public final class SwingSchedulers {

    /**
     * Holds onto the default Scheduler instance which
     * gets only instantiated if there was an actual
     * call to the {@link SwingSchedulers#edt()}.
     */
    static final class AsyncHolder {
        static final Scheduler INSTANCE = AsyncSwingScheduler.INSTANCE;
    }

    /** Utility class. */
    private SwingSchedulers() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Returns the default Event Dispatch Thread (EDT) scheduler.
     * <p>
     * The default value can be overridden via
     * {@link RxSwingPlugins#setOnEdtScheduler(io.reactivex.functions.Function)}.
     * @return the EDT Scheduler
     */
    public static Scheduler edt() {
        return RxSwingPlugins.onEdtScheduler(AsyncHolder.INSTANCE);
    }
}
