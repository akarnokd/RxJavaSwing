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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import io.reactivex.Observable;
import io.reactivex.internal.functions.ObjectHelper;

/**
 * Methods to wrap various Swing event sources.
 */
public final class SwingObservable {

    public static final int MOUSE_CLICK = 1;

    public static final int MOUSE_MOVE = 2;

    public static final int MOUSE_WHEEL = 3;

    public static final int MOUSE_ALL = MOUSE_CLICK | MOUSE_MOVE | MOUSE_WHEEL;

    /** Factory class. */
    private SwingObservable() {
        throw new IllegalStateException("No instances!");
    }

    public static Observable<ActionEvent> actions(AbstractButton button) {
        ObjectHelper.requireNonNull(button, "button is null");
        return RxSwingPlugins.onAssembly(new ActionEventObservable(button));
    }

    public static Observable<MouseEvent> mouse(Component component) {
        return mouse(component, MOUSE_ALL);
    }

    public static Observable<MouseEvent> mouse(Component component, int flags) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MouseEventObservable(component, flags));
    }

    public static Observable<MouseWheelEvent> mouseWheel(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MouseWheelEventObservable(component));
    }

    public static Observable<KeyEvent> keyboard(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new KeyEventObservable(component));
    }

    public static Observable<ComponentEvent> component(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ComponentEventObservable(component));
    }

    public static Observable<FocusEvent> focus(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new FocusEventObservable(component));
    }

    public static Observable<HierarchyEvent> hierarchyBounds(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new HierarchyBoundsEventObservable(component));
    }

    public static Observable<HierarchyEvent> hierarchy(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new HierarchyEventObservable(component));
    }

    public static Observable<InputMethodEvent> inputMethod(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new InputMethodEventObservable(component));
    }

    public static Observable<PropertyChangeEvent> propertyChange(Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new PropertyChangeEventObservable(component, null));
    }

    public static Observable<PropertyChangeEvent> propertyChange(Component component, String propertyName) {
        ObjectHelper.requireNonNull(component, "component is null");
        ObjectHelper.requireNonNull(propertyName, "propertyName is null");
        return RxSwingPlugins.onAssembly(new PropertyChangeEventObservable(component, propertyName));
    }

    public static Observable<DocumentEvent> document(Document component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new DocumentEventObservable(component));
    }

    public static Observable<DocumentEvent> document(JTextComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return document(component.getDocument());
    }

    public static Observable<UndoableEditEvent> undoableEdit(Document component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new UndoableEditEventObservable(component));
    }

    public static Observable<CaretEvent> caret(JTextComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new CaretEventObservable(component));
    }

    public static Observable<AncestorEvent> ancestor(JComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new AncestorEventObservable(component));
    }

    public static Observable<VetoablePropertyChangeEvent> vetoableChange(JComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new VetoableChangeEventObservable(component));
    }

    public static Observable<ContainerEvent> container(Container component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ContainerEventObservable(component));
    }
}
