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

final class MenuMouseDragEventObservable extends Observable<MenuDragMouseEvent> {

    final JMenuItem widget;

    MenuMouseDragEventObservable(JMenuItem widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super MenuDragMouseEvent> observer) {
        JMenuItem w = widget;

        MenuDragMouseEventConsumer aec = new MenuDragMouseEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addMenuDragMouseListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class MenuDragMouseEventConsumer extends AbstractEventConsumer<MenuDragMouseEvent, JMenuItem>
    implements MenuDragMouseListener {

        private static final long serialVersionUID = -3605206827474016488L;

        MenuDragMouseEventConsumer(Observer<? super MenuDragMouseEvent> actual, JMenuItem widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JMenuItem w) {
            w.removeMenuDragMouseListener(this);
        }

        @Override
        public void menuDragMouseEntered(MenuDragMouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuDragMouseExited(MenuDragMouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuDragMouseDragged(MenuDragMouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void menuDragMouseReleased(MenuDragMouseEvent e) {
            actual.onNext(e);
        }

    }
}
