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

import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.*;

import io.reactivex.*;

final class ChangeEventColorObservable extends Observable<ChangeEvent> {

    final ColorSelectionModel widget;

    ChangeEventColorObservable(ColorSelectionModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ChangeEvent> observer) {
        ColorSelectionModel w = widget;

        ChangeEventConsumer aec = new ChangeEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addChangeListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class ChangeEventConsumer extends AbstractEventConsumer<ChangeEvent, ColorSelectionModel>
    implements ChangeListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ChangeEventConsumer(Observer<? super ChangeEvent> actual, ColorSelectionModel widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(ColorSelectionModel w) {
            w.removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            actual.onNext(e);
        }

    }
}
