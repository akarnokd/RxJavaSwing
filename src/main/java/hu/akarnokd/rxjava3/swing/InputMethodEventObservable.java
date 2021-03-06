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

import java.awt.Component;
import java.awt.event.*;

import io.reactivex.rxjava3.core.*;

final class InputMethodEventObservable extends Observable<InputMethodEvent> {

    final Component widget;

    InputMethodEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super InputMethodEvent> observer) {
        Component w = widget;

        InputMethodEventConsumer aec = new InputMethodEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addInputMethodListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class InputMethodEventConsumer extends AbstractEventConsumer<InputMethodEvent, Component>
    implements InputMethodListener {

        private static final long serialVersionUID = -3605206827474016488L;

        InputMethodEventConsumer(Observer<? super InputMethodEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component w) {
            w.removeInputMethodListener(this);
        }

        @Override
        public void inputMethodTextChanged(InputMethodEvent event) {
            actual.onNext(event);
        }

        @Override
        public void caretPositionChanged(InputMethodEvent event) {
            actual.onNext(event);
        }

    }
}
