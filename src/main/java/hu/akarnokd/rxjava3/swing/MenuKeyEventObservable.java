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

import javax.swing.JMenuItem;
import javax.swing.event.*;

import io.reactivex.rxjava3.core.*;

final class MenuKeyEventObservable extends Observable<MenuKeyEvent> {

    final JMenuItem widget;

    MenuKeyEventObservable(JMenuItem widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super MenuKeyEvent> observer) {
        JMenuItem w = widget;

        MenuKeyEventConsumer aec = new MenuKeyEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addMenuKeyListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class MenuKeyEventConsumer extends AbstractEventConsumer<MenuKeyEvent, JMenuItem>
    implements MenuKeyListener {

        private static final long serialVersionUID = -3605206827474016488L;

        MenuKeyEventConsumer(Observer<? super MenuKeyEvent> actual, JMenuItem widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JMenuItem w) {
            w.removeMenuKeyListener(this);
        }

        @Override
        public void menuKeyTyped(MenuKeyEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuKeyPressed(MenuKeyEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuKeyReleased(MenuKeyEvent e) {
            actual.onNext(e);
        }

    }
}
