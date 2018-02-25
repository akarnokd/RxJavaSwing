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

final class TableColumnMarginEventObservable extends Observable<ChangeEvent> {

    final TableColumnModel widget;

    TableColumnMarginEventObservable(TableColumnModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super ChangeEvent> observer) {
        TableColumnModel w = widget;

        TableColumnChangeEventConsumer aec = new TableColumnChangeEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addColumnModelListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TableColumnChangeEventConsumer extends AbstractEventConsumer<ChangeEvent, TableColumnModel>
    implements TableColumnModelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TableColumnChangeEventConsumer(Observer<? super ChangeEvent> actual, TableColumnModel widget) {
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
            actual.onNext(e);
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
            // ignored in this wrapper
        }
    }
}
