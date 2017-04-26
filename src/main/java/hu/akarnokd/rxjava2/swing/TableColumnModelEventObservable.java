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

import javax.swing.event.*;
import javax.swing.table.TableColumnModel;

import io.reactivex.*;

final class TableColumnModelEventObservable extends Observable<TableColumnModelEvent> {

    final TableColumnModel widget;

    TableColumnModelEventObservable(TableColumnModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super TableColumnModelEvent> observer) {
        TableColumnModel w = widget;

        TableColumnModelEventConsumer aec = new TableColumnModelEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addColumnModelListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TableColumnModelEventConsumer extends AbstractEventConsumer<TableColumnModelEvent, TableColumnModel>
    implements TableColumnModelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TableColumnModelEventConsumer(Observer<? super TableColumnModelEvent> actual, TableColumnModel widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(TableColumnModel w) {
            w.removeColumnModelListener(this);
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            // ignored as the ChangeEvent is not a subtype of TableColumnModelEvent
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
            // ignored as the ChangeEvent is not a subtype of TableColumnModelEvent
        }

    }
}
