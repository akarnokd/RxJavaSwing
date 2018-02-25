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

import java.awt.ItemSelectable;
import java.awt.event.*;

import io.reactivex.*;

final class ItemEventObservable extends Observable<ItemEvent> {

    final ItemSelectable widget;

    ItemEventObservable(ItemSelectable widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ItemEvent> observer) {
        ItemSelectable w = widget;

        ItemEventConsumer aec = new ItemEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addItemListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class ItemEventConsumer extends AbstractEventConsumer<ItemEvent, ItemSelectable>
    implements ItemListener {

        private static final long serialVersionUID = -3605206827474016488L;

        ItemEventConsumer(Observer<? super ItemEvent> actual, ItemSelectable widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(ItemSelectable w) {
            w.removeItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            actual.onNext(e);
        }

    }
}
