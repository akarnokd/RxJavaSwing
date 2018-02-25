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

import javax.swing.JTree;
import javax.swing.event.*;
import javax.swing.tree.ExpandVetoException;

import io.reactivex.*;

final class TreeWillExpandEventObservable extends Observable<TreeExpansionEvent> {

    final JTree widget;

    TreeWillExpandEventObservable(JTree widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super TreeExpansionEvent> observer) {
        JTree w = widget;

        TreeWillExpandEventConsumer aec = new TreeWillExpandEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addTreeWillExpandListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TreeWillExpandEventConsumer extends AbstractEventConsumer<TreeExpansionEvent, JTree>
    implements TreeWillExpandListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TreeWillExpandEventConsumer(Observer<? super TreeExpansionEvent> actual, JTree widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JTree w) {
            w.removeTreeWillExpandListener(this);
        }

        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            actual.onNext(event);
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            actual.onNext(event);
        }

    }
}
