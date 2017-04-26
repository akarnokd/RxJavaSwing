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

final class ComponentEventObservable extends Observable<ComponentEvent> {

    final Component widget;

    ComponentEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ComponentEvent> observer) {
        Component w = widget;

        ComponentEventConsumer aec = new ComponentEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addComponentListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class ComponentEventConsumer extends AbstractEventConsumer<ComponentEvent, Component>
    implements ComponentListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ComponentEventConsumer(Observer<? super ComponentEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component w) {
            w.removeComponentListener(this);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            actual.onNext(e);
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            actual.onNext(e);
        }

        @Override
        public void componentShown(ComponentEvent e) {
            actual.onNext(e);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            actual.onNext(e);
        }
    }
}
