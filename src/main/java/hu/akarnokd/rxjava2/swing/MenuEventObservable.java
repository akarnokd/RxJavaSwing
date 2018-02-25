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

import javax.swing.*;
import javax.swing.event.*;

import io.reactivex.*;

final class MenuEventObservable extends Observable<MenuEvent> {

    final JMenu widget;

    MenuEventObservable(JMenu widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super MenuEvent> observer) {
        JMenu w = widget;

        MenuEventConsumer aec = new MenuEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addMenuListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class MenuEventConsumer extends AbstractEventConsumer<MenuEvent, JMenu>
    implements MenuListener {

        private static final long serialVersionUID = -3605206827474016488L;

        MenuEventConsumer(Observer<? super MenuEvent> actual, JMenu widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JMenu w) {
            w.removeMenuListener(this);
        }

        @Override
        public void menuSelected(MenuEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuDeselected(MenuEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuCanceled(MenuEvent e) {
            actual.onNext(e);
        }

    }
}
