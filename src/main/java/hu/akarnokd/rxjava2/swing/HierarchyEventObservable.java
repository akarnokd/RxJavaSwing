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

final class HierarchyEventObservable extends Observable<HierarchyEvent> {

    final Component widget;

    HierarchyEventObservable(Component widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super HierarchyEvent> observer) {
        Component w = widget;

        HierarchyEventConsumer aec = new HierarchyEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addHierarchyListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class HierarchyEventConsumer extends AbstractEventConsumer<HierarchyEvent, Component>
    implements HierarchyListener {

        private static final long serialVersionUID = -3605206827474016488L;

        HierarchyEventConsumer(Observer<? super HierarchyEvent> actual, Component widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(Component w) {
            w.removeHierarchyListener(this);
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            actual.onNext(e);
        }

    }
}
