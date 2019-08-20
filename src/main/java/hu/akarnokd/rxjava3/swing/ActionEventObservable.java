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

import java.awt.event.*;

import javax.swing.AbstractButton;

import io.reactivex.rxjava3.core.*;

final class ActionEventObservable extends Observable<ActionEvent> {

    final AbstractButton widget;

    ActionEventObservable(AbstractButton widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ActionEvent> observer) {
        AbstractButton w = widget;

        ActionEventConsumer aec = new ActionEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addActionListener(aec);
        if (aec.get() == null) {
            w.removeActionListener(aec);
        }
    }

    static final class ActionEventConsumer extends AbstractEventConsumer<ActionEvent, AbstractButton>
    implements ActionListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ActionEventConsumer(Observer<? super ActionEvent> actual, AbstractButton widget) {
            super(actual, widget);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            actual.onNext(e);
        }

        @Override
        protected void onDispose(AbstractButton component) {
            component.removeActionListener(this);
        }
    }
}
