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
import java.beans.*;

import io.reactivex.rxjava3.core.*;

final class PropertyChangeEventObservable extends Observable<PropertyChangeEvent> {

    final Component widget;

    final String propertyName;

    PropertyChangeEventObservable(Component widget, String propertyName) {
        this.widget = widget;
        this.propertyName = propertyName;
    }

    @Override
    protected void subscribeActual(Observer<? super PropertyChangeEvent> observer) {
        Component w = widget;
        String pn = propertyName;

        PropertyChangeEventConsumer aec = new PropertyChangeEventConsumer(observer, w, pn);
        observer.onSubscribe(aec);

        if (pn == null) {
            w.addPropertyChangeListener(aec);
        } else {
            w.addPropertyChangeListener(pn, aec);
        }
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class PropertyChangeEventConsumer extends AbstractEventConsumer<PropertyChangeEvent, Component>
    implements PropertyChangeListener {

        private static final long serialVersionUID = -3605206827474016488L;

        final String propertyName;

        PropertyChangeEventConsumer(Observer<? super PropertyChangeEvent> actual, Component widget, String propertyName) {
            super(actual, widget);
            this.propertyName = propertyName;
        }

        @Override
        protected void onDispose(Component w) {
            String pn = propertyName;

            if (pn == null) {
                w.removePropertyChangeListener(this);
            } else {
                w.removePropertyChangeListener(pn, this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            actual.onNext(evt);
        }

    }
}
