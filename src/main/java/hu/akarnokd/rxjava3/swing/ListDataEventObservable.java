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

import javax.swing.ListModel;
import javax.swing.event.*;

import io.reactivex.rxjava3.core.*;

final class ListDataEventObservable extends Observable<ListDataEvent> {

    final ListModel<?> widget;

    ListDataEventObservable(ListModel<?> widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ListDataEvent> observer) {
        ListModel<?> w = widget;

        ListDataEventConsumer aec = new ListDataEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addListDataListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class ListDataEventConsumer extends AbstractEventConsumer<ListDataEvent, ListModel<?>>
    implements ListDataListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ListDataEventConsumer(Observer<? super ListDataEvent> actual, ListModel<?> widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(ListModel<?> w) {
            w.removeListDataListener(this);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            actual.onNext(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            actual.onNext(e);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            actual.onNext(e);
        }

    }
}
