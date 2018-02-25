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
import javax.swing.table.TableColumnModel;

import io.reactivex.*;

final class TableColumnSelectionEventObservable extends Observable<ListSelectionEvent> {

    final TableColumnModel widget;

    TableColumnSelectionEventObservable(TableColumnModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ListSelectionEvent> observer) {
        TableColumnModel w = widget;

        TableColumnSelectionEventConsumer aec = new TableColumnSelectionEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addColumnModelListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TableColumnSelectionEventConsumer extends AbstractEventConsumer<ListSelectionEvent, TableColumnModel>
    implements TableColumnModelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TableColumnSelectionEventConsumer(Observer<? super ListSelectionEvent> actual, TableColumnModel widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(TableColumnModel w) {
            w.removeColumnModelListener(this);
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            // ignored in this wrapper
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            // ignored in this wrapper
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            // ignored in this wrapper
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            // ignored in this wrapper
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
            actual.onNext(e);
        }
    }
}
