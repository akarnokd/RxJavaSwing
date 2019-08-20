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

import java.awt.Window;
import java.awt.event.*;

import io.reactivex.rxjava3.core.*;

final class WindowEventObservable extends Observable<WindowEvent> {

    final Window widget;

    final int flags;

    WindowEventObservable(Window widget, int flags) {
        this.widget = widget;
        this.flags = flags;
    }

    @Override
    protected void subscribeActual(Observer<? super WindowEvent> observer) {
        Window w = widget;
        int f = flags;

        WindowEventConsumer aec = new WindowEventConsumer(observer, w, f);
        observer.onSubscribe(aec);

        if ((f & 1) != 0) {
            w.addWindowListener(aec);
        }
        if ((f & 2) != 0) {
            w.addWindowFocusListener(aec);
        }
        if ((f & 4) != 0) {
            w.addWindowStateListener(aec);
        }

        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class WindowEventConsumer extends AbstractEventConsumer<WindowEvent, Window>
    implements WindowListener, WindowFocusListener, WindowStateListener {

        private static final long serialVersionUID = -3605206827474016488L;

        final int flags;

        WindowEventConsumer(Observer<? super WindowEvent> actual, Window widget, int flags) {
            super(actual, widget);
            this.flags = flags;
        }

        @Override
        protected void onDispose(Window w) {
            int f = flags;
            if ((f & 1) != 0) {
                w.removeWindowListener(this);
            }
            if ((f & 2) != 0) {
                w.removeWindowFocusListener(this);
            }
            if ((f & 4) != 0) {
                w.removeWindowStateListener(this);
            }
        }

        @Override
        public void windowStateChanged(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowClosed(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowIconified(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            actual.onNext(e);
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            actual.onNext(e);
        }

    }
}
