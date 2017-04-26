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

final class DocumentEventObservable extends Observable<DocumentEvent> {

    final Document widget;

    DocumentEventObservable(Document widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super DocumentEvent> observer) {
        Document w = widget;

        DocumentEventConsumer aec = new DocumentEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addDocumentListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class DocumentEventConsumer extends AbstractEventConsumer<DocumentEvent, Document>
    implements DocumentListener {

        private static final long serialVersionUID = -3605206827474016488L;

        DocumentEventConsumer(Observer<? super DocumentEvent> actual, Document widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Document w) {
            w.removeDocumentListener(this);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            actual.onNext(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            actual.onNext(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            actual.onNext(e);
        }

    }
}
