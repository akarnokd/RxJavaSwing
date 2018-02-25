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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import io.reactivex.*;
import io.reactivex.annotations.*;
import io.reactivex.internal.functions.ObjectHelper;

/**
 * Methods to wrap various Swing event sources.
 */
public final class SwingObservable {

    /**
     * Flag constant for {@link #mouse(Component, int)} indicating only the
     * {@link MouseListener} events should be forwarded.
     */
    public static final int MOUSE_CLICK = 1;

    /**
     * Flag constant for {@link #mouse(Component, int)} indicating only the
     * {@link MouseMotionListener} events should be forwarded.
     */
    public static final int MOUSE_MOVE = 2;

    /**
     * Flag constant for {@link #mouse(Component, int)} indicating only the
     * {@link MouseWheelListener} events should be forwarded.
     * <p>
     * Note that the {@link MouseWheelEvent} extends {@link MouseEvent}
     * and the consumer should check and cast back the stream of
     * {@code MouseEvent}.
     */
    public static final int MOUSE_WHEEL = 4;

    /**
     * The combination of the {@link #MOUSE_CLICK}, {@link #MOUSE_MOVE} and
     * {@link #MOUSE_WHEEL} flags.
     */
    public static final int MOUSE_ALL = MOUSE_CLICK | MOUSE_MOVE | MOUSE_WHEEL;

    /**
     * Flag constant for the {@link #window(Window, int)} indicating only
     * the {@link WindowListener} events should be forwarded.
     */
    public static final int WINDOW_ACTIVE = 1;

    /**
     * Flag constant for the {@link #window(Window, int)} indicating only
     * the {@link WindowFocusListener} events should be forwarded.
     */
    public static final int WINDOW_FOCUS = 2;

    /**
     * Flag constant for the {@link #window(Window, int)} indicating only
     * the {@link WindowStateListener} events should be forwarded.
     */
    public static final int WINDOW_STATE = 4;

    /**
     * The combination of {@link #WINDOW_ACTIVE}, {@link #WINDOW_FOCUS} and
     * {@link #WINDOW_STATE} flags.
     */
    public static final int WINDOW_ALL = WINDOW_ACTIVE | WINDOW_FOCUS | WINDOW_STATE;

    /** Factory class. */
    private SwingObservable() {
        throw new IllegalStateException("No instances!");
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ActionEvent> actions(@NonNull AbstractButton button) {
        ObjectHelper.requireNonNull(button, "button is null");
        return RxSwingPlugins.onAssembly(new ActionEventObservable(button));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ActionEvent> actions(@NonNull JComboBox<?> button) {
        ObjectHelper.requireNonNull(button, "button is null");
        return RxSwingPlugins.onAssembly(new ActionEventComboBoxObservable(button));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MouseEvent> mouse(@NonNull Component component) {
        return mouse(component, MOUSE_ALL);
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MouseEvent> mouse(@NonNull Component component, int flags) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MouseEventObservable(component, flags));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MouseWheelEvent> mouseWheel(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MouseWheelEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<KeyEvent> keyboard(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new KeyEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ComponentEvent> component(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ComponentEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<FocusEvent> focus(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new FocusEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<HierarchyEvent> hierarchyBounds(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new HierarchyBoundsEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<HierarchyEvent> hierarchy(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new HierarchyEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<InputMethodEvent> inputMethod(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new InputMethodEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<PropertyChangeEvent> propertyChange(@NonNull Component component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new PropertyChangeEventObservable(component, null));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<PropertyChangeEvent> propertyChange(@NonNull Component component, String propertyName) {
        ObjectHelper.requireNonNull(component, "component is null");
        ObjectHelper.requireNonNull(propertyName, "propertyName is null");
        return RxSwingPlugins.onAssembly(new PropertyChangeEventObservable(component, propertyName));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<DocumentEvent> document(@NonNull Document component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new DocumentEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<DocumentEvent> document(@NonNull JTextComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return document(component.getDocument());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<UndoableEditEvent> undoableEdit(@NonNull Document component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new UndoableEditEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<UndoableEditEvent> undoableEdit(@NonNull JTextComponent component) {
        return undoableEdit(component.getDocument());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<CaretEvent> caret(@NonNull JTextComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new CaretEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<AncestorEvent> ancestor(@NonNull JComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new AncestorEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<VetoablePropertyChangeEvent> vetoableChange(@NonNull JComponent component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new VetoableChangeEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ContainerEvent> container(@NonNull Container component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ContainerEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ListSelectionEvent> listSelection(@NonNull JList<?> component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ListSelectionEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ListSelectionEvent> listSelection(@NonNull ListSelectionModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ListSelectionEventModelObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<HyperlinkEvent> hyperlink(@NonNull JEditorPane component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new HyperlinkEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<InternalFrameEvent> internalFrame(@NonNull JInternalFrame component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new InternalFrameEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ListDataEvent> listChange(@NonNull ListModel<?> component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ListDataEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ListDataEvent> listChange(@NonNull JList<?> component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return listChange(component.getModel());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MenuDragMouseEvent> menuDrag(@NonNull JMenuItem component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MenuMouseDragEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MenuEvent> menu(@NonNull JMenu component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MenuEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MenuKeyEvent> menuKey(@NonNull JMenuItem component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MenuKeyEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<MenuKeyEvent> menuKey(@NonNull JPopupMenu component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new MenuKeyEventPopupObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<PopupMenuEvent> popupMenu(@NonNull JComboBox<?> component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new PopupMenuEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<PopupMenuEvent> popupMenu(@NonNull JPopupMenu component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new PopupMenuEventPopupObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<RowSorterEvent> rowSorter(@NonNull RowSorter<?> component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new RowSorterEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<RowSorterEvent> rowSorter(@NonNull JTable component) {
        return rowSorter(component.getRowSorter());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TableModelEvent> tableModel(@NonNull TableModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TableModelEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TableModelEvent> tableModel(@NonNull JTable component) {
        return tableModel(component.getModel());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TableColumnModelEvent> tableColumnModel(@NonNull TableColumnModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TableColumnModelEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> tableColumnMarginChange(@NonNull TableColumnModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TableColumnMarginEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ListSelectionEvent> tableColumnSelectionChange(@NonNull TableColumnModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TableColumnSelectionEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TableColumnModelEvent> tableColumnModel(@NonNull JTable component) {
        return tableColumnModel(component.getColumnModel());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeExpansionEvent> treeExpansion(@NonNull JTree component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TreeExpansionEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeModelEvent> treeModel(@NonNull TreeModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TreeModelEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeModelEvent> treeModel(@NonNull JTree component) {
        return treeModel(component.getModel());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeSelectionEvent> treeSelection(@NonNull TreeSelectionModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TreeSelectionEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeSelectionEvent> treeSelection(@NonNull JTree component) {
        return treeSelection(component.getSelectionModel());
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<TreeExpansionEvent> treeWillExpand(@NonNull JTree component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new TreeWillExpandEventObservable(component));
    }

    /**
     * Sends the oberved upstream event directly to the Event Dispatch thread individually
     * (unlike observeOn which may occupy the EDT longer with a fast emitting source).
     * <p>
     * To be used with {@link Observable#compose(ObservableTransformer)}.
     * <p>
     * This custom observeOn should allow more interleaving with other EDT-submitted
     * tasks and not occupy the EDT for too long.
     * <p>
     * Example:<pre><code>
     * Observable.range(1, 5)
     * .compose(SwingObservable.observeOnEdt())
     * .subscribe(System.out::println);
     * </code></pre>
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>The operator doesn't run on any scheduler as it directly submits work to the EDT
     *  via {@code EventQueue.invokeLater()}.</dd>
     * </dl>
     * @param <T> the value type
     * @return the new ObservableTransformer.
     * @since 0.1.1
     */
    @CheckReturnValue
    @NonNull
    public static <T> ObservableTransformer<T, T> observeOnEdt() {
        return new SwingObserveOn<T>(null);
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ItemEvent> itemSelection(@NonNull ItemSelectable component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ItemEventObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull JTabbedPane component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventTabbedPaneObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull JSlider component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventSliderObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull JSpinner component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventSpinnerObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull SpinnerModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventSpinnerModelObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull AbstractButton component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventButtonObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull ButtonModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventButtonModelObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull JViewport component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventViewportObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull ColorSelectionModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventColorObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull JProgressBar component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventProgressBarObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<ChangeEvent> change(@NonNull BoundedRangeModel component) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new ChangeEventBoundedRangeObservable(component));
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<WindowEvent> window(@NonNull Window component) {
        return window(component, WINDOW_ALL);
    }

    @CheckReturnValue
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static Observable<WindowEvent> window(@NonNull Window component, int flags) {
        ObjectHelper.requireNonNull(component, "component is null");
        return RxSwingPlugins.onAssembly(new WindowEventObservable(component, flags));
    }

}
