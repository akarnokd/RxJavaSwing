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

import java.awt.Component;
import java.awt.event.*;

import io.reactivex.rxjava3.core.*;

final class MouseEventObservable extends Observable<MouseEvent> {

    final Component widget;

    final int flags;

    MouseEventObservable(Component widget, int flags) {
        this.widget = widget;
        this.flags = flags;
    }

    @Override
    protected void subscribeActual(Observer<? super MouseEvent> observer) {
        Component w = widget;
        int f = flags;

        MouseEventConsumer aec = new MouseEventConsumer(observer, w, f);
        observer.onSubscribe(aec);
        if ((f & 1) != 0) {
            w.addMouseListener(aec);
        }
        if ((f & 2) != 0) {
            w.addMouseMotionListener(aec);
        }
        if ((f & 4) != 0) {
            w.addMouseWheelListener(aec);
        }
        if (aec.get() == null) {
            aec.onDispose(w);
        }
    }

    static final class MouseEventConsumer extends AbstractEventConsumer<MouseEvent, Component>
    implements MouseListener, MouseMotionListener, MouseWheelListener {

        private static final long serialVersionUID = -3605206827474016488L;

        final int flags;

        MouseEventConsumer(Observer<? super MouseEvent> actual, Component widget, int flags) {
            super(actual, widget);
            this.flags = flags;
        }

        @Override
        protected void onDispose(Component w) {
            int f = flags;
            if ((f & 1) != 0) {
                w.removeMouseListener(this);
            }
            if ((f & 2) != 0) {
                w.removeMouseMotionListener(this);
            }
            if ((f & 4) != 0) {
                w.removeMouseWheelListener(this);
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            actual.onNext(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            actual.onNext(e);
        }
    }
}
