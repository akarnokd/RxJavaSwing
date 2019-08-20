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

final class MouseWheelEventObservable extends Observable<MouseWheelEvent> {

    final Component widget;

    MouseWheelEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super MouseWheelEvent> observer) {
        Component w = widget;

        MouseWheelEventConsumer aec = new MouseWheelEventConsumer(observer, w);
        observer.onSubscribe(aec);
        w.addMouseWheelListener(aec);
        if (aec.get() == null) {
            w.removeMouseWheelListener(aec);
        }
    }

    static final class MouseWheelEventConsumer extends AbstractEventConsumer<MouseWheelEvent, Component>
    implements MouseWheelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        MouseWheelEventConsumer(Observer<? super MouseWheelEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component component) {
            component.removeMouseWheelListener(this);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            actual.onNext(e);
        }
    }
}
