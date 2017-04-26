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

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

abstract class AbstractEventConsumer<E, W> extends AtomicReference<W> implements Disposable {

    private static final long serialVersionUID = -7590060742382106868L;

    protected final Observer<? super E> actual;

    AbstractEventConsumer(Observer<? super E> actual, W component) {
        this.actual = actual;
        lazySet(component);
    }

    @Override
    public final boolean isDisposed() {
        return get() != null;
    }

    @Override
    public final void dispose() {
        W c = getAndSet(null);
        if (c != null) {
            onDispose(c);
        }
    }

    protected abstract void onDispose(W component);
}
