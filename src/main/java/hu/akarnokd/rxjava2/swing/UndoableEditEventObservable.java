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

import javax.swing.event.*;
import javax.swing.text.Document;

import io.reactivex.*;

final class UndoableEditEventObservable extends Observable<UndoableEditEvent> {

    final Document widget;

    UndoableEditEventObservable(Document widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super UndoableEditEvent> observer) {
        Document w = widget;

        UndoableEditEventConsumer aec = new UndoableEditEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addUndoableEditListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class UndoableEditEventConsumer extends AbstractEventConsumer<UndoableEditEvent, Document>
    implements UndoableEditListener {

        private static final long serialVersionUID = -3605206827474016488L;

        UndoableEditEventConsumer(Observer<? super UndoableEditEvent> actual, Document widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Document w) {
            w.removeUndoableEditListener(this);
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            actual.onNext(e);
        }

    }
}
