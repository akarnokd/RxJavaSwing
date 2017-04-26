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

/**
 * Hosts the default {@link Scheduler}s for RxSwing: {@link #edt()} and {@link #edtEager()}.
 */
public final class SwingSchedulers {

    static final class AsyncHolder {
        static final Scheduler INSTANCE = AsyncSwingScheduler.INSTANCE;
    }

    private SwingSchedulers() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Returns the default Event Dispatch Thread scheduler.
     * @return the Scheduler
     * @see #edtEager()
     */
    public static Scheduler edt() {
        return RxSwingPlugins.onAsyncScheduler(AsyncHolder.INSTANCE);
    }

    /**
     * Returns a Scheduler that keeps the event processing on
     * the Event Dispatch Thread if the sender is also on the
     * EDT.
     * @return the Scheduler
     */
    public static Scheduler edtEager() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
