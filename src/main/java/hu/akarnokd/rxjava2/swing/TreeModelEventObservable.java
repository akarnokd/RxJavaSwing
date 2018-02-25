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

import javax.swing.event.*;
import javax.swing.tree.TreeModel;

import io.reactivex.*;

final class TreeModelEventObservable extends Observable<TreeModelEvent> {

    final TreeModel widget;

    TreeModelEventObservable(TreeModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super TreeModelEvent> observer) {
        TreeModel w = widget;

        TreeModelEventConsumer aec = new TreeModelEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addTreeModelListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TreeModelEventConsumer extends AbstractEventConsumer<TreeModelEvent, TreeModel>
    implements TreeModelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TreeModelEventConsumer(Observer<? super TreeModelEvent> actual, TreeModel widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(TreeModel w) {
            w.removeTreeModelListener(this);
        }

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            actual.onNext(e);
        }

    }
}
