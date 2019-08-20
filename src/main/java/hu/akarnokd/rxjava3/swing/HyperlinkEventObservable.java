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

import javax.swing.JEditorPane;
import javax.swing.event.*;

import io.reactivex.rxjava3.core.*;

final class HyperlinkEventObservable extends Observable<HyperlinkEvent> {

    final JEditorPane widget;

    HyperlinkEventObservable(JEditorPane widget) {
        this.widget = widget;
    }

    @Override
    protected void subscribeActual(Observer<? super HyperlinkEvent> observer) {
        JEditorPane w = widget;

        HyperlinkEventConsumer aec = new HyperlinkEventConsumer(observer, w);
        observer.onSubscribe(aec);

        w.addHyperlinkListener(aec);
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class HyperlinkEventConsumer extends AbstractEventConsumer<HyperlinkEvent, JEditorPane>
    implements HyperlinkListener {

        private static final long serialVersionUID = -3605206827474016488L;

        HyperlinkEventConsumer(Observer<? super HyperlinkEvent> actual, JEditorPane widget) {
            super(actual, widget);
        }

        @Override
        protected void onDispose(JEditorPane w) {
            w.removeHyperlinkListener(this);
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            actual.onNext(e);
        }

    }
}
