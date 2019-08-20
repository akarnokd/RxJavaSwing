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

import java.beans.*;

import javax.swing.JComponent;

import io.reactivex.rxjava3.core.*;

final class VetoableChangeEventObservable extends Observable<VetoablePropertyChangeEvent> {

    final JComponent widget;

    VetoableChangeEventObservable(JComponent widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super VetoablePropertyChangeEvent> observer) {
        JComponent w = widget;

        PropertyChangeEventConsumer aec = new PropertyChangeEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addVetoableChangeListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class PropertyChangeEventConsumer extends AbstractEventConsumer<VetoablePropertyChangeEvent, JComponent>
    implements VetoableChangeListener {

        private static final long serialVersionUID = -3605206827474016488L;

        PropertyChangeEventConsumer(Observer<? super VetoablePropertyChangeEvent> actual, JComponent widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JComponent w) {
            w.removeVetoableChangeListener(this);
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            VetoablePropertyChangeEvent e = new VetoablePropertyChangeEvent(
                    evt.getSource(), evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue()
            );
            e.setPropagationId(evt.getPropagationId());
            actual.onNext(e);
            if (e.isVetoed()) {
                throw new PropertyVetoException("Vetoed", evt);
            }
        }

    }
}
