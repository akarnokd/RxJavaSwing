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

import javax.swing.RowSorter;
import javax.swing.event.*;

import io.reactivex.rxjava3.core.*;

final class RowSorterEventObservable extends Observable<RowSorterEvent> {

    final RowSorter<?> widget;

    RowSorterEventObservable(RowSorter<?> widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super RowSorterEvent> observer) {
        RowSorter<?> w = widget;

        RowSorterEventConsumer aec = new RowSorterEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addRowSorterListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class RowSorterEventConsumer extends AbstractEventConsumer<RowSorterEvent, RowSorter<?>>
    implements RowSorterListener {

        private static final long serialVersionUID = -3605206827474016488L;

        RowSorterEventConsumer(Observer<? super RowSorterEvent> actual, RowSorter<?> widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(RowSorter<?> w) {
            w.removeRowSorterListener(this);
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            actual.onNext(e);
        }
    }
}
