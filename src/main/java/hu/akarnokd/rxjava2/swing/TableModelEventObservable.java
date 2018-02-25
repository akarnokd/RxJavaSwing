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
import javax.swing.table.TableModel;

import io.reactivex.*;

final class TableModelEventObservable extends Observable<TableModelEvent> {

    final TableModel widget;

    TableModelEventObservable(TableModel widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super TableModelEvent> observer) {
        TableModel w = widget;

        TableModelEventConsumer aec = new TableModelEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addTableModelListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class TableModelEventConsumer extends AbstractEventConsumer<TableModelEvent, TableModel>
    implements TableModelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        TableModelEventConsumer(Observer<? super TableModelEvent> actual, TableModel widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(TableModel w) {
            w.removeTableModelListener(this);
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            actual.onNext(e);
        }
    }
}
