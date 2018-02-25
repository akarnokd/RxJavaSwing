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

import java.awt.Component;
import java.awt.event.*;

import io.reactivex.*;

final class KeyEventObservable extends Observable<KeyEvent> {

    final Component widget;

    KeyEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super KeyEvent> observer) {
        Component w = widget;

        KeyEventConsumer aec = new KeyEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addKeyListener(aec);
        if (aec.get() == null) {
            w.removeKeyListener(aec);
        }
    }

    static final class KeyEventConsumer extends AbstractEventConsumer<KeyEvent, Component>
    implements KeyListener {

        private static final long serialVersionUID = -3605206827474016488L;

        KeyEventConsumer(Observer<? super KeyEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component w) {
            w.removeKeyListener(this);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            actual.onNext(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            actual.onNext(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            actual.onNext(e);
        }
    }
}
