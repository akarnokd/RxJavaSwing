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

import java.awt.Component;
import java.awt.event.*;

import io.reactivex.*;

final class FocusEventObservable extends Observable<FocusEvent> {

    final Component widget;

    FocusEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super FocusEvent> observer) {
        Component w = widget;

        FocusEventConsumer aec = new FocusEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addFocusListener(aec);
        if (aec.get() == null) {
            w.removeFocusListener(aec);
        }
    }

    static final class FocusEventConsumer extends AbstractEventConsumer<FocusEvent, Component>
    implements FocusListener {

        private static final long serialVersionUID = -3605206827474016488L;

        FocusEventConsumer(Observer<? super FocusEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component w) {
            w.removeFocusListener(this);
        }

        @Override
        public void focusGained(FocusEvent e) {
            actual.onNext(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            actual.onNext(e);
        }

    }
}
