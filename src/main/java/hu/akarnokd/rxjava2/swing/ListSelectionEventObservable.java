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

import javax.swing.JList;
import javax.swing.event.*;

import io.reactivex.*;

final class ListSelectionEventObservable extends Observable<ListSelectionEvent> {

    final JList<?> widget;

    ListSelectionEventObservable(JList<?> widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ListSelectionEvent> observer) {
        JList<?> w = widget;

        ListSelectionEventConsumer aec = new ListSelectionEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addListSelectionListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class ListSelectionEventConsumer extends AbstractEventConsumer<ListSelectionEvent, JList<?>>
    implements ListSelectionListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ListSelectionEventConsumer(Observer<? super ListSelectionEvent> actual, JList<?> widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JList<?> w) {
            w.removeListSelectionListener(this);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            actual.onNext(e);
        }

    }
}
