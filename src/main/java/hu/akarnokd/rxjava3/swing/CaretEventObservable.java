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

import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import io.reactivex.rxjava3.core.*;

final class CaretEventObservable extends Observable<CaretEvent> {

    final JTextComponent widget;

    CaretEventObservable(JTextComponent widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super CaretEvent> observer) {
        JTextComponent w = widget;

        CaretEventConsumer aec = new CaretEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addCaretListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class CaretEventConsumer extends AbstractEventConsumer<CaretEvent, JTextComponent>
    implements CaretListener {

        private static final long serialVersionUID = -3605206827474016488L;

        CaretEventConsumer(Observer<? super CaretEvent> actual, JTextComponent widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JTextComponent w) {
            w.removeCaretListener(this);
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            actual.onNext(e);
        }
    }
}
