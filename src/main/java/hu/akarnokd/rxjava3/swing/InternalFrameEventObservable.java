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

import javax.swing.JInternalFrame;
import javax.swing.event.*;

import io.reactivex.rxjava3.core.*;

final class InternalFrameEventObservable extends Observable<InternalFrameEvent> {

    final JInternalFrame widget;

    InternalFrameEventObservable(JInternalFrame widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super InternalFrameEvent> observer) {
        JInternalFrame w = widget;

        InternalFrameEventConsumer aec = new InternalFrameEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addInternalFrameListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class InternalFrameEventConsumer extends AbstractEventConsumer<InternalFrameEvent, JInternalFrame>
    implements InternalFrameListener {

        private static final long serialVersionUID = -3605206827474016488L;

        InternalFrameEventConsumer(Observer<? super InternalFrameEvent> actual, JInternalFrame widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JInternalFrame w) {
            w.removeInternalFrameListener(this);
        }

        @Override
        public void internalFrameOpened(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            actual.onNext(e);
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
            actual.onNext(e);
        }

    }
}
