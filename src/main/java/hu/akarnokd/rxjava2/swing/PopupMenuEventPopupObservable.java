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

import javax.swing.JPopupMenu;
import javax.swing.event.*;

import io.reactivex.*;

final class PopupMenuEventPopupObservable extends Observable<PopupMenuEvent> {

    final JPopupMenu widget;

    PopupMenuEventPopupObservable(JPopupMenu widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super PopupMenuEvent> observer) {
        JPopupMenu w = widget;

        PopupMenuEventConsumer aec = new PopupMenuEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addPopupMenuListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class PopupMenuEventConsumer extends AbstractEventConsumer<PopupMenuEvent, JPopupMenu>
    implements PopupMenuListener {

        private static final long serialVersionUID = -3605206827474016488L;

        PopupMenuEventConsumer(Observer<? super PopupMenuEvent> actual, JPopupMenu widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JPopupMenu w) {
            w.removePopupMenuListener(this);
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            actual.onNext(e);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            actual.onNext(e);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            actual.onNext(e);
        }

    }
}
