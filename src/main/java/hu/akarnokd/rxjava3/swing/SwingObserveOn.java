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

import java.awt.EventQueue;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;

final class SwingObserveOn<T> extends Observable<T>
implements ObservableTransformer<T, T> {

    final Observable<T> source;

    SwingObserveOn(Observable<T> source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new ObserveOnObserver<T>(observer));
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return new SwingObserveOn<T>(upstream);
    }

    static final class ObserveOnObserver<T> implements Observer<T>, Disposable, Runnable {

        final Observer<? super T> actual;

        Disposable upstream;

        volatile boolean disposed;

        ObserveOnObserver(Observer<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void dispose() {
            disposed = true;
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            EventQueue.invokeLater(new OnNextEvent<T>(actual, t, this));
        }

        @Override
        public void onError(Throwable e) {
            EventQueue.invokeLater(new OnErrorEvent<T>(actual, e, this));
        }

        @Override
        public void onComplete() {
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            if (!disposed) {
                actual.onComplete();
            }
        }

        static final class OnNextEvent<T> implements Runnable {
            final Observer<? super T> actual;

            final T event;

            final Disposable d;

            OnNextEvent(Observer<? super T> actual, T event, Disposable d) {
                this.actual = actual;
                this.event = event;
                this.d = d;
            }

            @Override
            public void run() {
                if (!d.isDisposed()) {
                    actual.onNext(event);
                }
            }
        }

        static final class OnErrorEvent<T> implements Runnable {
            final Observer<? super T> actual;

            final Throwable event;

            final Disposable d;

            OnErrorEvent(Observer<? super T> actual, Throwable event, Disposable d) {
                this.actual = actual;
                this.event = event;
                this.d = d;
            }

            @Override
            public void run() {
                if (!d.isDisposed()) {
                    actual.onError(event);
                }
            }
        }
    }
}
