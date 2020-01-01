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

import static hu.akarnokd.rxjava3.swing.SwingObservable.*;
import static org.junit.Assert.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextHitInfo;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class SwingObservableTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(SwingObservable.class);
    }

    /**
     * Run the task on the Event Dispatch Thread and rethrow any exception thrown by it
     * as AssertionError.
     * @param run the task to run
     */
    void runEdt(final Runnable run) {
        final AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        run.run();
                    } catch (Throwable ex) {
                        error.set(ex);
                    }
                }
            });
        } catch (Exception ex) {
            AssertionError ae = new AssertionError("EDT task failed: " + ex);
            ae.initCause(ex);
            throw ae;
        }

        Throwable ex = error.get();
        if (ex != null) {
            if (ex instanceof AssertionError) {
                throw (AssertionError)ex;
            }
            AssertionError ae = new AssertionError("EDT task failed: " + ex);
            ae.initCause(ex);
            throw ae;
        }
    }

    @Test
    public void nullChecks() throws Exception {
        for (Method m : SwingObservable.class.getMethods()) {
            if ((m.getModifiers() & Modifier.STATIC) != 0) {
                if (m.getParameterTypes().length == 1) {
                    try {
                        m.invoke(null, new Object[] { null });
                        throw new RuntimeException(m.toString());
                    } catch (InvocationTargetException ex) {
                        if (!(ex.getCause() instanceof NullPointerException)) {
                            throw new RuntimeException(m.toString(), ex);
                        }
                    }
                }

                if (m.getParameterTypes().length == 2) {
                    try {
                        Object o = null;

                        if (m.getParameterTypes()[1] == Integer.TYPE) {
                            o = 1;
                        }
                        if (m.getParameterTypes()[1] == String.class) {
                            o = "Str";
                        }

                        m.invoke(null, new Object[] { null, o });
                        throw new RuntimeException(m.toString());
                    } catch (InvocationTargetException ex) {
                        if (!(ex.getCause() instanceof NullPointerException)) {
                            throw new RuntimeException(m.toString(), ex);
                        }
                    } catch (Throwable ex) {
                        throw new RuntimeException(m.toString(), ex);
                    }
                }
            }
        }
    }

    @Test
    public void abstractButtonAction() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JButton button = new JButton();

                TestObserverEx<ActionEvent> to = actions(button)
                .subscribeWith(new TestObserverEx<ActionEvent>());

                button.doClick();

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                button.doClick(3);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                button.doClick();

                to.assertValueCount(2)
                .assertNotTerminated();

                actions(button).test(true);
            }
        });
    }

    @Test
    public void comboBoxAction() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JComboBox<String> cb = new JComboBox<>();
                cb.addItem("a");
                cb.addItem("b");

                cb.setSelectedIndex(0);

                TestObserverEx<ActionEvent> to = actions(cb)
                .subscribeWith(new TestObserverEx<ActionEvent>());

                cb.setSelectedIndex(1);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                cb.setSelectedIndex(0);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                cb.setSelectedIndex(1);

                to.assertValueCount(2)
                .assertNotTerminated();

                actions(cb).test(true);
            }
        });
    }

    @Test
    public void componentMouse() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<MouseEvent> to = mouse(cb)
                .subscribeWith(new TestObserverEx<MouseEvent>());

                MouseEvent evt = new MouseEvent(cb, 0, 0, 0, 100, 100, 1, false);

                for (MouseListener ml : cb.getListeners(MouseListener.class)) {
                    ml.mouseEntered(evt);
                    ml.mousePressed(evt);
                    ml.mouseReleased(evt);
                    ml.mouseClicked(evt);
                    ml.mouseExited(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(5)
                  .assertNotTerminated();

                for (MouseListener ml : cb.getListeners(MouseListener.class)) {
                    ml.mouseEntered(evt);
                    ml.mousePressed(evt);
                    ml.mouseReleased(evt);
                    ml.mouseClicked(evt);
                    ml.mouseExited(evt);
                }

                to.assertValueCount(10)
                .assertNotTerminated();

                to.dispose();

                for (MouseListener ml : cb.getListeners(MouseListener.class)) {
                    ml.mouseEntered(evt);
                    ml.mousePressed(evt);
                    ml.mouseReleased(evt);
                    ml.mouseClicked(evt);
                    ml.mouseExited(evt);
                }

                to.assertValueCount(10)
                .assertNotTerminated();

                mouse(cb).test(true);
            }
        });
    }


    @Test
    public void componentMouseNone() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<MouseEvent> to = mouse(cb, 0)
                .subscribeWith(new TestObserverEx<MouseEvent>());

                assertEquals(0, cb.getMouseListeners().length);
                assertEquals(0, cb.getMouseMotionListeners().length);
                assertEquals(0, cb.getMouseWheelListeners().length);

                to.dispose();

                assertEquals(0, cb.getMouseListeners().length);
                assertEquals(0, cb.getMouseMotionListeners().length);
                assertEquals(0, cb.getMouseWheelListeners().length);
            }
        });
    }

    @Test
    public void componentMouseMoved() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<MouseEvent> to = mouse(cb)
                .subscribeWith(new TestObserverEx<MouseEvent>());

                MouseEvent evt = new MouseEvent(cb, 0, 0, 0, 100, 100, 1, false);

                for (MouseMotionListener ml : cb.getListeners(MouseMotionListener.class)) {
                    ml.mouseMoved(evt);
                    ml.mouseDragged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (MouseMotionListener ml : cb.getListeners(MouseMotionListener.class)) {
                    ml.mouseMoved(evt);
                    ml.mouseDragged(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (MouseMotionListener ml : cb.getListeners(MouseMotionListener.class)) {
                    ml.mouseMoved(evt);
                    ml.mouseDragged(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                mouse(cb).test(true);
            }
        });
    }

    @Test
    public void componentMouseWheel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<MouseWheelEvent> to = mouseWheel(cb)
                .subscribeWith(new TestObserverEx<MouseWheelEvent>());

                MouseWheelEvent evt = new MouseWheelEvent(cb, 0, 0, 0, 0, 0, 0, false, 0, 1, 1);

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                mouseWheel(cb).test(true);
            }
        });
    }

    @Test
    public void componentMouseWheelAsMouseEvent() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<MouseEvent> to = mouse(cb)
                .subscribeWith(new TestObserverEx<MouseEvent>());

                MouseWheelEvent evt = new MouseWheelEvent(cb, 0, 0, 0, 0, 0, 0, false, 0, 1, 1);

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (MouseWheelListener ml : cb.getListeners(MouseWheelListener.class)) {
                    ml.mouseWheelMoved(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();
            }
        });
    }

    @Test
    public void componentKeyboard() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<KeyEvent> to = keyboard(cb)
                .subscribeWith(new TestObserverEx<KeyEvent>());

                KeyEvent evt = new KeyEvent(cb, 0, 0, 0, 65, 'A');

                for (KeyListener ml : cb.getListeners(KeyListener.class)) {
                    ml.keyPressed(evt);
                    ml.keyReleased(evt);
                    ml.keyTyped(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (KeyListener ml : cb.getListeners(KeyListener.class)) {
                    ml.keyPressed(evt);
                    ml.keyReleased(evt);
                    ml.keyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (KeyListener ml : cb.getListeners(KeyListener.class)) {
                    ml.keyPressed(evt);
                    ml.keyReleased(evt);
                    ml.keyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                keyboard(cb).test(true);
            }
        });
    }

    @Test
    public void componentComponent() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<ComponentEvent> to = component(cb)
                .subscribeWith(new TestObserverEx<ComponentEvent>());

                ComponentEvent evt = new ComponentEvent(cb, 1);

                for (ComponentListener ml : cb.getListeners(ComponentListener.class)) {
                    ml.componentHidden(evt);
                    ml.componentMoved(evt);
                    ml.componentResized(evt);
                    ml.componentShown(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(4)
                  .assertNotTerminated();

                for (ComponentListener ml : cb.getListeners(ComponentListener.class)) {
                    ml.componentHidden(evt);
                    ml.componentMoved(evt);
                    ml.componentResized(evt);
                    ml.componentShown(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                to.dispose();

                for (ComponentListener ml : cb.getListeners(ComponentListener.class)) {
                    ml.componentHidden(evt);
                    ml.componentMoved(evt);
                    ml.componentResized(evt);
                    ml.componentShown(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                component(cb).test(true);
            }
        });
    }

    @Test
    public void componentFocus() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<FocusEvent> to = focus(cb)
                .subscribeWith(new TestObserverEx<FocusEvent>());

                FocusEvent evt = new FocusEvent(cb, 1);

                for (FocusListener ml : cb.getListeners(FocusListener.class)) {
                    ml.focusGained(evt);
                    ml.focusLost(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (FocusListener ml : cb.getListeners(FocusListener.class)) {
                    ml.focusGained(evt);
                    ml.focusLost(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (FocusListener ml : cb.getListeners(FocusListener.class)) {
                    ml.focusGained(evt);
                    ml.focusLost(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                focus(cb).test(true);
            }
        });
    }

    @Test
    public void componentHierarchyBounds() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<HierarchyEvent> to = hierarchyBounds(cb)
                .subscribeWith(new TestObserverEx<HierarchyEvent>());

                HierarchyEvent evt = new HierarchyEvent(cb, 1, cb, new JPanel());

                for (HierarchyBoundsListener ml : cb.getListeners(HierarchyBoundsListener.class)) {
                    ml.ancestorMoved(evt);
                    ml.ancestorResized(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (HierarchyBoundsListener ml : cb.getListeners(HierarchyBoundsListener.class)) {
                    ml.ancestorMoved(evt);
                    ml.ancestorResized(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (HierarchyBoundsListener ml : cb.getListeners(HierarchyBoundsListener.class)) {
                    ml.ancestorMoved(evt);
                    ml.ancestorResized(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                hierarchyBounds(cb).test(true);
            }
        });
    }

    @Test
    public void componentHierarchy() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<HierarchyEvent> to = hierarchy(cb)
                .subscribeWith(new TestObserverEx<HierarchyEvent>());

                HierarchyEvent evt = new HierarchyEvent(cb, 1, cb, new JPanel());

                for (HierarchyListener ml : cb.getListeners(HierarchyListener.class)) {
                    ml.hierarchyChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (HierarchyListener ml : cb.getListeners(HierarchyListener.class)) {
                    ml.hierarchyChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (HierarchyListener ml : cb.getListeners(HierarchyListener.class)) {
                    ml.hierarchyChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                hierarchy(cb).test(true);
            }
        });
    }

    @Test
    public void componentInputMethod() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<InputMethodEvent> to = inputMethod(cb)
                .subscribeWith(new TestObserverEx<InputMethodEvent>());

                InputMethodEvent evt = new InputMethodEvent(cb, InputMethodEvent.INPUT_METHOD_FIRST, TextHitInfo.leading(0), TextHitInfo.leading(0));

                for (InputMethodListener ml : cb.getListeners(InputMethodListener.class)) {
                    ml.caretPositionChanged(evt);
                    ml.inputMethodTextChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (InputMethodListener ml : cb.getListeners(InputMethodListener.class)) {
                    ml.caretPositionChanged(evt);
                    ml.inputMethodTextChanged(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (InputMethodListener ml : cb.getListeners(InputMethodListener.class)) {
                    ml.caretPositionChanged(evt);
                    ml.inputMethodTextChanged(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                inputMethod(cb).test(true);
            }
        });
    }

    @Test
    public void componentPropertyChange() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<PropertyChangeEvent> to = propertyChange(cb)
                .subscribeWith(new TestObserverEx<PropertyChangeEvent>());

                PropertyChangeEvent evt = new PropertyChangeEvent(cb, "property", 0, 1);

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                propertyChange(cb).test(true);
            }
        });
    }

    @Test
    public void componentPropertyChangeSpecific() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<PropertyChangeEvent> to = propertyChange(cb, "property")
                .subscribeWith(new TestObserverEx<PropertyChangeEvent>());

                PropertyChangeEvent evt = new PropertyChangeEvent(cb, "property", 0, 1);

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (PropertyChangeListener ml : cb.getListeners(PropertyChangeListener.class)) {
                    ml.propertyChange(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                propertyChange(cb, "property").test(true);
            }
        });
    }

    @Test
    public void textDocument() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JEditorPane cb = new JEditorPane();
                cb.setText("abc");
                AbstractDocument d = (AbstractDocument) cb.getDocument();

                TestObserverEx<DocumentEvent> to = document(cb)
                .subscribeWith(new TestObserverEx<DocumentEvent>());

                DocumentEvent evt = new DocumentEvent() {

                    @Override
                    public int getOffset() {
                        return 0;
                    }

                    @Override
                    public int getLength() {
                        return 0;
                    }

                    @Override
                    public Document getDocument() {
                        return null;
                    }

                    @Override
                    public EventType getType() {
                        return null;
                    }

                    @Override
                    public ElementChange getChange(Element elem) {
                        return null;
                    }
                };

                for (DocumentListener ml : d.getDocumentListeners()) {
                    if (ml instanceof Disposable) {
                        ml.insertUpdate(evt);
                        ml.removeUpdate(evt);
                        ml.changedUpdate(evt);
                    }
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (DocumentListener ml : d.getDocumentListeners()) {
                    if (ml instanceof Disposable) {
                        ml.insertUpdate(evt);
                        ml.removeUpdate(evt);
                        ml.changedUpdate(evt);
                    }
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (DocumentListener ml : d.getDocumentListeners()) {
                    if (ml instanceof Disposable) {
                        ml.insertUpdate(evt);
                        ml.removeUpdate(evt);
                        ml.changedUpdate(evt);
                    }
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                document(cb).test(true);
            }
        });
    }

    @Test
    public void textUndoableEdit() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JEditorPane cb = new JEditorPane();
                cb.setText("abc");
                AbstractDocument d = (AbstractDocument) cb.getDocument();

                TestObserverEx<UndoableEditEvent> to = undoableEdit(cb)
                .subscribeWith(new TestObserverEx<UndoableEditEvent>());

                UndoableEditEvent evt = new UndoableEditEvent(cb, null);

                for (UndoableEditListener ml : d.getUndoableEditListeners()) {
                    if (ml instanceof Disposable) {
                        ml.undoableEditHappened(evt);
                    }
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (UndoableEditListener ml : d.getUndoableEditListeners()) {
                    if (ml instanceof Disposable) {
                        ml.undoableEditHappened(evt);
                    }
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (UndoableEditListener ml : d.getUndoableEditListeners()) {
                    if (ml instanceof Disposable) {
                        ml.undoableEditHappened(evt);
                    }
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                undoableEdit(cb).test(true);
            }
        });
    }

    @Test
    public void textCaret() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JEditorPane cb = new JEditorPane();
                cb.setText("abc");

                TestObserverEx<CaretEvent> to = caret(cb)
                .subscribeWith(new TestObserverEx<CaretEvent>());

                CaretEvent evt = new CaretEvent(cb) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public int getMark() {
                        return 0;
                    }
                    @Override
                    public int getDot() {
                        return 0;
                    }
                };

                for (CaretListener ml : cb.getCaretListeners()) {
                    ml.caretUpdate(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (CaretListener ml : cb.getCaretListeners()) {
                    ml.caretUpdate(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (CaretListener ml : cb.getCaretListeners()) {
                    ml.caretUpdate(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                caret(cb).test(true);
            }
        });
    }

    @Test
    public void componentAncestor() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<AncestorEvent> to = ancestor(cb)
                .subscribeWith(new TestObserverEx<AncestorEvent>());

                AncestorEvent evt = new AncestorEvent(cb, 1, new JPanel(), new JPanel());

                for (AncestorListener ml : cb.getListeners(AncestorListener.class)) {
                    ml.ancestorAdded(evt);
                    ml.ancestorMoved(evt);
                    ml.ancestorRemoved(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (AncestorListener ml : cb.getListeners(AncestorListener.class)) {
                    ml.ancestorAdded(evt);
                    ml.ancestorMoved(evt);
                    ml.ancestorRemoved(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (AncestorListener ml : cb.getListeners(AncestorListener.class)) {
                    ml.ancestorAdded(evt);
                    ml.ancestorMoved(evt);
                    ml.ancestorRemoved(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                ancestor(cb).test(true);
            }
        });
    }

    @Test
    public void componentVetoableChange() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<VetoablePropertyChangeEvent> to = vetoableChange(cb)
                .subscribeWith(new TestObserverEx<VetoablePropertyChangeEvent>());

                PropertyChangeEvent evt = new PropertyChangeEvent(cb, "prop", 0, 1);

                for (VetoableChangeListener ml : cb.getListeners(VetoableChangeListener.class)) {
                    try {
                        ml.vetoableChange(evt);
                    } catch (PropertyVetoException ex) {
                        AssertionError ae = new AssertionError("Vetoed?");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (VetoableChangeListener ml : cb.getListeners(VetoableChangeListener.class)) {
                    try {
                        ml.vetoableChange(evt);
                    } catch (PropertyVetoException ex) {
                        AssertionError ae = new AssertionError("Vetoed?");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (VetoableChangeListener ml : cb.getListeners(VetoableChangeListener.class)) {
                    try {
                        ml.vetoableChange(evt);
                    } catch (PropertyVetoException ex) {
                        AssertionError ae = new AssertionError("Vetoed?");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                vetoableChange(cb).test(true);
            }
        });
    }

    @Test
    public void componentVetoableChangeVetoed() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<VetoablePropertyChangeEvent> to = vetoableChange(cb)
                .doOnNext(new Consumer<VetoablePropertyChangeEvent>() {
                    @Override
                    public void accept(VetoablePropertyChangeEvent e) throws Exception {
                        e.veto();
                    }
                })
                .subscribeWith(new TestObserverEx<VetoablePropertyChangeEvent>());

                PropertyChangeEvent evt = new PropertyChangeEvent(cb, "prop", 0, 1);

                for (VetoableChangeListener ml : cb.getListeners(VetoableChangeListener.class)) {
                    try {
                        ml.vetoableChange(evt);
                        fail("Should have thrown");
                    } catch (PropertyVetoException expected) {
                        // expected
                    }
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();
            }
        });
    }

    @Test
    public void componentContainer() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserverEx<ContainerEvent> to = container(cb)
                .subscribeWith(new TestObserverEx<ContainerEvent>());

                ContainerEvent evt = new ContainerEvent(cb, 0, new JPanel());

                for (ContainerListener ml : cb.getListeners(ContainerListener.class)) {
                    ml.componentAdded(evt);
                    ml.componentRemoved(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (ContainerListener ml : cb.getListeners(ContainerListener.class)) {
                    ml.componentAdded(evt);
                    ml.componentRemoved(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (ContainerListener ml : cb.getListeners(ContainerListener.class)) {
                    ml.componentAdded(evt);
                    ml.componentRemoved(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                container(cb).test(true);
            }
        });
    }

    @Test
    public void listListSelection() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JList<String> cb = new JList<>();

                TestObserverEx<ListSelectionEvent> to = listSelection(cb)
                .subscribeWith(new TestObserverEx<ListSelectionEvent>());

                ListSelectionEvent evt = new ListSelectionEvent(cb, 0, 0, false);

                for (ListSelectionListener ml : cb.getListeners(ListSelectionListener.class)) {
                    ml.valueChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ListSelectionListener ml : cb.getListeners(ListSelectionListener.class)) {
                    ml.valueChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ListSelectionListener ml : cb.getListeners(ListSelectionListener.class)) {
                    ml.valueChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                listSelection(cb).test(true);
            }
        });
    }

    @Test
    public void listListSelectionModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JList<String> cb = new JList<>();

                DefaultListModel<String> md = new DefaultListModel<>();
                md.addElement("abc");
                cb.setModel(md);

                ListSelectionModel lsm = cb.getSelectionModel();

                TestObserverEx<ListSelectionEvent> to = listSelection(lsm)
                .subscribeWith(new TestObserverEx<ListSelectionEvent>());

                lsm.setSelectionInterval(0, 1);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                lsm.clearSelection();

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                lsm.setSelectionInterval(0, 1);

                to.assertValueCount(2)
                .assertNotTerminated();

                listSelection(cb.getSelectionModel()).test(true);
            }
        });
    }

    @Test
    public void editorHyperlink() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JEditorPane cb = new JEditorPane();

                TestObserverEx<HyperlinkEvent> to = hyperlink(cb)
                .subscribeWith(new TestObserverEx<HyperlinkEvent>());

                URL u = null;
                try {
                    new URL("localhost");
                } catch (Throwable ex) {
                    // should not fail
                }

                HyperlinkEvent evt = new HyperlinkEvent(cb, HyperlinkEvent.EventType.ACTIVATED, u);

                for (HyperlinkListener ml : cb.getListeners(HyperlinkListener.class)) {
                    ml.hyperlinkUpdate(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (HyperlinkListener ml : cb.getListeners(HyperlinkListener.class)) {
                    ml.hyperlinkUpdate(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (HyperlinkListener ml : cb.getListeners(HyperlinkListener.class)) {
                    ml.hyperlinkUpdate(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                hyperlink(cb).test(true);
            }
        });
    }

    @Test
    public void frameInternalFrame() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JInternalFrame cb = new JInternalFrame();

                TestObserverEx<InternalFrameEvent> to = internalFrame(cb)
                .subscribeWith(new TestObserverEx<InternalFrameEvent>());

                InternalFrameEvent evt = new InternalFrameEvent(cb, 0);

                for (InternalFrameListener ml : cb.getListeners(InternalFrameListener.class)) {
                    ml.internalFrameActivated(evt);
                    ml.internalFrameClosed(evt);
                    ml.internalFrameClosing(evt);
                    ml.internalFrameDeactivated(evt);
                    ml.internalFrameDeiconified(evt);
                    ml.internalFrameIconified(evt);
                    ml.internalFrameOpened(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(7)
                  .assertNotTerminated();

                for (InternalFrameListener ml : cb.getListeners(InternalFrameListener.class)) {
                    ml.internalFrameActivated(evt);
                    ml.internalFrameClosed(evt);
                    ml.internalFrameClosing(evt);
                    ml.internalFrameDeactivated(evt);
                    ml.internalFrameDeiconified(evt);
                    ml.internalFrameIconified(evt);
                    ml.internalFrameOpened(evt);
                }

                to.assertValueCount(14)
                .assertNotTerminated();

                to.dispose();

                for (InternalFrameListener ml : cb.getListeners(InternalFrameListener.class)) {
                    ml.internalFrameActivated(evt);
                    ml.internalFrameClosed(evt);
                    ml.internalFrameClosing(evt);
                    ml.internalFrameDeactivated(evt);
                    ml.internalFrameDeiconified(evt);
                    ml.internalFrameIconified(evt);
                    ml.internalFrameOpened(evt);
                }

                to.assertValueCount(14)
                .assertNotTerminated();

                internalFrame(cb).test(true);
            }
        });
    }

    @Test
    public void listListChange() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JList<String> cb = new JList<>();

                DefaultListModel<String> md = new DefaultListModel<>();
                cb.setModel(md);

                TestObserverEx<ListDataEvent> to = listChange(cb)
                .subscribeWith(new TestObserverEx<ListDataEvent>());

                md.addElement("abc");
                md.setElementAt("def", 0);
                md.removeElementAt(0);

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                md.addElement("abc");
                md.setElementAt("def", 0);
                md.removeElementAt(0);

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                md.addElement("abc");
                md.setElementAt("def", 0);
                md.removeElementAt(0);

                to.assertValueCount(6)
                .assertNotTerminated();

                listChange(cb).test(true);
            }
        });
    }

    @Test
    public void menuMenuDrag() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JMenuItem cb = new JMenuItem();

                TestObserverEx<MenuDragMouseEvent> to = menuDrag(cb)
                .subscribeWith(new TestObserverEx<MenuDragMouseEvent>());

                MenuDragMouseEvent evt = new MenuDragMouseEvent(cb, 0, 0, 0, 0, 0, 0, false, null, MenuSelectionManager.defaultManager());

                for (MenuDragMouseListener ml : cb.getListeners(MenuDragMouseListener.class)) {
                    ml.menuDragMouseDragged(evt);
                    ml.menuDragMouseEntered(evt);
                    ml.menuDragMouseExited(evt);
                    ml.menuDragMouseReleased(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(4)
                  .assertNotTerminated();

                for (MenuDragMouseListener ml : cb.getListeners(MenuDragMouseListener.class)) {
                    ml.menuDragMouseDragged(evt);
                    ml.menuDragMouseEntered(evt);
                    ml.menuDragMouseExited(evt);
                    ml.menuDragMouseReleased(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                to.dispose();

                for (MenuDragMouseListener ml : cb.getListeners(MenuDragMouseListener.class)) {
                    ml.menuDragMouseDragged(evt);
                    ml.menuDragMouseEntered(evt);
                    ml.menuDragMouseExited(evt);
                    ml.menuDragMouseReleased(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                menuDrag(cb).test(true);
            }
        });
    }

    @Test
    public void menuMenu() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JMenu cb = new JMenu();

                TestObserverEx<MenuEvent> to = menu(cb)
                .subscribeWith(new TestObserverEx<MenuEvent>());

                MenuEvent evt = new MenuEvent(cb);

                for (MenuListener ml : cb.getListeners(MenuListener.class)) {
                    ml.menuSelected(evt);
                    ml.menuDeselected(evt);
                    ml.menuCanceled(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (MenuListener ml : cb.getListeners(MenuListener.class)) {
                    ml.menuSelected(evt);
                    ml.menuDeselected(evt);
                    ml.menuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (MenuListener ml : cb.getListeners(MenuListener.class)) {
                    ml.menuSelected(evt);
                    ml.menuDeselected(evt);
                    ml.menuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                menu(cb).test(true);
            }
        });
    }

    @Test
    public void menuMenuKey() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JMenuItem cb = new JMenuItem();

                TestObserverEx<MenuKeyEvent> to = menuKey(cb)
                .subscribeWith(new TestObserverEx<MenuKeyEvent>());

                MenuKeyEvent evt = new MenuKeyEvent(cb, 0, 0, 0, 65, 'A', null, MenuSelectionManager.defaultManager());

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                menuKey(cb).test(true);
            }
        });
    }

    @Test
    public void popupMenuKey() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JPopupMenu cb = new JPopupMenu();

                TestObserverEx<MenuKeyEvent> to = menuKey(cb)
                .subscribeWith(new TestObserverEx<MenuKeyEvent>());

                MenuKeyEvent evt = new MenuKeyEvent(cb, 0, 0, 0, 65, 'A', null, MenuSelectionManager.defaultManager());

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (MenuKeyListener ml : cb.getListeners(MenuKeyListener.class)) {
                    ml.menuKeyPressed(evt);
                    ml.menuKeyReleased(evt);
                    ml.menuKeyTyped(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                menuKey(cb).test(true);
            }
        });
    }

    @Test
    public void popupPopup() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JPopupMenu cb = new JPopupMenu();

                TestObserverEx<PopupMenuEvent> to = popupMenu(cb)
                .subscribeWith(new TestObserverEx<PopupMenuEvent>());

                PopupMenuEvent evt = new PopupMenuEvent(cb);

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                popupMenu(cb).test(true);
            }
        });
    }

    @Test
    public void comboboxPopup() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JComboBox<String> cb = new JComboBox<>();

                TestObserverEx<PopupMenuEvent> to = popupMenu(cb)
                .subscribeWith(new TestObserverEx<PopupMenuEvent>());

                PopupMenuEvent evt = new PopupMenuEvent(cb);

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                for (PopupMenuListener ml : cb.getListeners(PopupMenuListener.class)) {
                    ml.popupMenuWillBecomeVisible(evt);
                    ml.popupMenuWillBecomeInvisible(evt);
                    ml.popupMenuCanceled(evt);
                }

                to.assertValueCount(6)
                .assertNotTerminated();

                popupMenu(cb).test(true);
            }
        });
    }

    @Test
    public void tableRowSorter() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTable cb = new JTable();
                DefaultTableModel tm = new DefaultTableModel();
                tm.addColumn("First");
                tm.addRow(new Object[] { "cell" });
                cb.setModel(tm);

                TableRowSorter<TableModel> rs = new TableRowSorter<>();
                rs.setModel(tm);
                cb.setRowSorter(rs);
                rs.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

                TestObserverEx<RowSorterEvent> to = rowSorter(cb)
                .subscribeWith(new TestObserverEx<RowSorterEvent>());

                rs.sort();

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                rs.sort();

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                rs.sort();

                to.assertValueCount(2)
                .assertNotTerminated();

                rowSorter(cb).test(true);
            }
        });
    }

    @Test
    public void tableTableModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTable cb = new JTable();
                DefaultTableModel tm = new DefaultTableModel();
                tm.addColumn("First");
                tm.addRow(new Object[] { "cell" });
                cb.setModel(tm);

                TestObserverEx<TableModelEvent> to = tableModel(cb)
                .subscribeWith(new TestObserverEx<TableModelEvent>());

                TableModelEvent evt = new TableModelEvent(tm, 0, 0, 1);

                for (TableModelListener ml : tm.getListeners(TableModelListener.class)) {
                    ml.tableChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (TableModelListener ml : tm.getListeners(TableModelListener.class)) {
                    ml.tableChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (TableModelListener ml : tm.getListeners(TableModelListener.class)) {
                    ml.tableChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                tableModel(cb).test(true);
            }
        });
    }

    @Test
    public void tableTableColumnModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTable cb = new JTable();
                DefaultTableModel tm = new DefaultTableModel();
                tm.addColumn("First");
                tm.addRow(new Object[] { "cell", "cell" });
                cb.setModel(tm);
                TableColumnModel cm = cb.getColumnModel();

                ListSelectionModel lsm = new DefaultListSelectionModel();
                cm.setSelectionModel(lsm);

                TestObserverEx<TableColumnModelEvent> to = tableColumnModel(cb)
                .subscribeWith(new TestObserverEx<TableColumnModelEvent>());

                TableColumn tc = new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(10);
                lsm.setSelectionInterval(0, 0);

                to
                  .assertSubscribed()
                  .assertValueCount(3)
                  .assertNotTerminated();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(5);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(6)
                .assertNotTerminated();

                to.dispose();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(1);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(6)
                .assertNotTerminated();

                tableColumnModel(cb).test(true);
            }
        });
    }

    @Test
    public void tableTableColumnMarginChange() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTable cb = new JTable();
                DefaultTableModel tm = new DefaultTableModel();
                tm.addColumn("First");
                tm.addRow(new Object[] { "cell", "cell" });
                cb.setModel(tm);
                TableColumnModel cm = cb.getColumnModel();

                ListSelectionModel lsm = new DefaultListSelectionModel();
                cm.setSelectionModel(lsm);

                TestObserverEx<ChangeEvent> to = tableColumnMarginChange(cm)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                TableColumn tc = new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(10);
                lsm.setSelectionInterval(0, 0);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(5);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(1);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(2)
                .assertNotTerminated();

                tableColumnMarginChange(cm).test(true);
            }
        });
    }

    @Test
    public void tableTableColumnSelection() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTable cb = new JTable();
                DefaultTableModel tm = new DefaultTableModel();
                tm.addColumn("First");
                tm.addRow(new Object[] { "cell", "cell" });
                cb.setModel(tm);
                TableColumnModel cm = cb.getColumnModel();

                ListSelectionModel lsm = new DefaultListSelectionModel();
                cm.setSelectionModel(lsm);
                lsm.clearSelection();

                TestObserverEx<ListSelectionEvent> to = tableColumnSelectionChange(cm)
                .subscribeWith(new TestObserverEx<ListSelectionEvent>());

                TableColumn tc = new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(10);
                lsm.setSelectionInterval(0, 0);

                to
                  .assertSubscribed()
                  .assertValueCount(2) // moveColumn fires too
                  .assertNotTerminated();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(5);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(5) // moveColumn fires too
                .assertNotTerminated();

                to.dispose();

                new TableColumn(1);
                cm.addColumn(tc);
                cm.moveColumn(1, 0);
                cm.removeColumn(tc);
                cm.setColumnMargin(1);
                lsm.setSelectionInterval(0, 0);

                to.assertValueCount(5)
                .assertNotTerminated();

                tableColumnSelectionChange(cm).test(true);
            }
        });
    }

    @Test
    public void treeTreeExpansion() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTree cb = new JTree();

                TestObserverEx<TreeExpansionEvent> to = treeExpansion(cb)
                .subscribeWith(new TestObserverEx<TreeExpansionEvent>());

                TreeExpansionEvent evt = new TreeExpansionEvent(cb, new TreePath(new DefaultMutableTreeNode()));

                for (TreeExpansionListener ml : cb.getListeners(TreeExpansionListener.class)) {
                    ml.treeCollapsed(evt);
                    ml.treeExpanded(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (TreeExpansionListener ml : cb.getListeners(TreeExpansionListener.class)) {
                    ml.treeCollapsed(evt);
                    ml.treeExpanded(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (TreeExpansionListener ml : cb.getListeners(TreeExpansionListener.class)) {
                    ml.treeCollapsed(evt);
                    ml.treeExpanded(evt);
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                treeExpansion(cb).test(true);
            }
        });
    }

    @Test
    public void treeTreeModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTree cb = new JTree();
                DefaultTreeModel tm = new DefaultTreeModel(new DefaultMutableTreeNode());
                cb.setModel(tm);

                TestObserverEx<TreeModelEvent> to = treeModel(cb)
                .subscribeWith(new TestObserverEx<TreeModelEvent>());

                TreeModelEvent evt = new TreeModelEvent(cb, new TreePath(new DefaultMutableTreeNode()));

                for (TreeModelListener ml : tm.getListeners(TreeModelListener.class)) {
                    ml.treeNodesInserted(evt);
                    ml.treeNodesRemoved(evt);
                    ml.treeNodesChanged(evt);
                    ml.treeStructureChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(4)
                  .assertNotTerminated();

                for (TreeModelListener ml : tm.getListeners(TreeModelListener.class)) {
                    ml.treeNodesInserted(evt);
                    ml.treeNodesRemoved(evt);
                    ml.treeNodesChanged(evt);
                    ml.treeStructureChanged(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                to.dispose();

                for (TreeModelListener ml : tm.getListeners(TreeModelListener.class)) {
                    ml.treeNodesInserted(evt);
                    ml.treeNodesRemoved(evt);
                    ml.treeNodesChanged(evt);
                    ml.treeStructureChanged(evt);
                }

                to.assertValueCount(8)
                .assertNotTerminated();

                treeModel(cb).test(true);
            }
        });
    }

    @Test
    public void treeTreeSelection() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTree cb = new JTree();
                TreeNode tn = new DefaultMutableTreeNode();
                DefaultTreeModel tm = new DefaultTreeModel(tn);
                cb.setModel(tm);

                TestObserverEx<TreeSelectionEvent> to = treeSelection(cb)
                .subscribeWith(new TestObserverEx<TreeSelectionEvent>());

                cb.setSelectionInterval(0, 0);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                cb.clearSelection();

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                cb.setSelectionInterval(0, 0);

                to.assertValueCount(2)
                .assertNotTerminated();

                treeSelection(cb).test(true);
            }
        });
    }

    @Test
    public void treeTreeWillExpansion() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTree cb = new JTree();

                TestObserverEx<TreeExpansionEvent> to = treeWillExpand(cb)
                .subscribeWith(new TestObserverEx<TreeExpansionEvent>());

                TreeExpansionEvent evt = new TreeExpansionEvent(cb, new TreePath(new DefaultMutableTreeNode()));

                for (TreeWillExpandListener ml : cb.getListeners(TreeWillExpandListener.class)) {
                    try {
                        ml.treeWillCollapse(evt);
                        ml.treeWillExpand(evt);
                    } catch (ExpandVetoException ex) {
                        AssertionError ae = new AssertionError("Unexpected veto");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to
                  .assertSubscribed()
                  .assertValueCount(2)
                  .assertNotTerminated();

                for (TreeWillExpandListener ml : cb.getListeners(TreeWillExpandListener.class)) {
                    try {
                        ml.treeWillCollapse(evt);
                        ml.treeWillExpand(evt);
                    } catch (ExpandVetoException ex) {
                        AssertionError ae = new AssertionError("Unexpected veto");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                to.dispose();

                for (TreeWillExpandListener ml : cb.getListeners(TreeWillExpandListener.class)) {
                    try {
                        ml.treeWillCollapse(evt);
                        ml.treeWillExpand(evt);
                    } catch (ExpandVetoException ex) {
                        AssertionError ae = new AssertionError("Unexpected veto");
                        ae.initCause(ex);
                        throw ae;
                    }
                }

                to.assertValueCount(4)
                .assertNotTerminated();

                treeWillExpand(cb).test(true);
            }
        });
    }

    @Test
    public void selectableItemSelection() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JButton cb = new JButton();

                TestObserverEx<ItemEvent> to = itemSelection(cb)
                .subscribeWith(new TestObserverEx<ItemEvent>());

                ItemEvent evt = new ItemEvent(cb, 0, "", ItemEvent.SELECTED);

                for (ItemListener ml : cb.getListeners(ItemListener.class)) {
                    ml.itemStateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ItemListener ml : cb.getListeners(ItemListener.class)) {
                    ml.itemStateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ItemListener ml : cb.getListeners(ItemListener.class)) {
                    ml.itemStateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                itemSelection(cb).test(true);
            }
        });
    }

    @Test
    public void changeTabbedPane() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JTabbedPane cb = new JTabbedPane();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeSlider() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JSlider cb = new JSlider();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeSpinner() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JSpinner cb = new JSpinner();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeSpinnerModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JSpinner cb = new JSpinner();
                SpinnerModel sm = new SpinnerNumberModel(0, 0, 100, 1);
                cb.setModel(sm);

                TestObserverEx<ChangeEvent> to = change(sm)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                sm.setValue(1);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                sm.setValue(2);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                sm.setValue(3);

                to.assertValueCount(2)
                .assertNotTerminated();

                change(sm).test(true);
            }
        });
    }

    @Test
    public void changeButton() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JButton cb = new JButton();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeButtonModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JButton cb = new JButton();
                ButtonModel sm = cb.getModel();

                TestObserverEx<ChangeEvent> to = change(sm)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                cb.doClick();

                to
                  .assertSubscribed()
                  .assertValueCount(4)
                  .assertNotTerminated();

                cb.doClick();

                to.assertValueCount(8)
                .assertNotTerminated();

                to.dispose();

                cb.doClick();

                to.assertValueCount(8)
                .assertNotTerminated();

                change(sm).test(true);
            }
        });
    }

    @Test
    public void changeViewPort() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JViewport cb = new JViewport();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeColorSelection() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                DefaultColorSelectionModel cb = new DefaultColorSelectionModel();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                cb.setSelectedColor(Color.RED);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                cb.setSelectedColor(Color.BLUE);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                cb.setSelectedColor(Color.RED);

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeProgressBar() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JProgressBar cb = new JProgressBar();

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                ChangeEvent evt = new ChangeEvent(cb);

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                for (ChangeListener ml : cb.getListeners(ChangeListener.class)) {
                    ml.stateChanged(evt);
                }

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void changeBoundedRangeModel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                BoundedRangeModel cb = new DefaultBoundedRangeModel(0, 0, 0, 100);

                TestObserverEx<ChangeEvent> to = change(cb)
                .subscribeWith(new TestObserverEx<ChangeEvent>());

                cb.setValue(1);

                to
                  .assertSubscribed()
                  .assertValueCount(1)
                  .assertNotTerminated();

                cb.setValue(2);

                to.assertValueCount(2)
                .assertNotTerminated();

                to.dispose();

                cb.setValue(3);

                to.assertValueCount(2)
                .assertNotTerminated();

                change(cb).test(true);
            }
        });
    }

    @Test
    public void windowWindow() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JFrame cb = new JFrame();

                TestObserverEx<WindowEvent> to = window(cb)
                .subscribeWith(new TestObserverEx<WindowEvent>());

                WindowEvent evt = new WindowEvent(cb, 0);

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(10)
                  .assertNotTerminated();

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to.assertValueCount(20)
                .assertNotTerminated();

                to.dispose();

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to.assertValueCount(20)
                .assertNotTerminated();

                window(cb).test(true);
            }
        });
    }

    @Test
    public void windowWindowNone() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JFrame cb = new JFrame();

                TestObserverEx<WindowEvent> to = window(cb, 0)
                .subscribeWith(new TestObserverEx<WindowEvent>());

                WindowEvent evt = new WindowEvent(cb, 0);

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to
                  .assertSubscribed()
                  .assertValueCount(0)
                  .assertNotTerminated();

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to.assertValueCount(0)
                .assertNotTerminated();

                to.dispose();

                for (WindowListener ml : cb.getListeners(WindowListener.class)) {
                    ml.windowActivated(evt);
                    ml.windowClosed(evt);
                    ml.windowClosing(evt);
                    ml.windowDeactivated(evt);
                    ml.windowDeiconified(evt);
                    ml.windowIconified(evt);
                    ml.windowOpened(evt);
                }
                for (WindowFocusListener ml : cb.getListeners(WindowFocusListener.class)) {
                    ml.windowGainedFocus(evt);
                    ml.windowLostFocus(evt);
                }
                for (WindowStateListener ml : cb.getListeners(WindowStateListener.class)) {
                    ml.windowStateChanged(evt);
                }

                to.assertValueCount(0)
                .assertNotTerminated();

                window(cb, 0).test(true);
            }
        });
    }

    @Test
    public void swingObserveOnError() {
        Observable.error(new IOException())
        .compose(SwingObservable.observeOnEdt())
        .subscribeWith(new TestObserverEx<>())
        .awaitDone(5, TimeUnit.SECONDS)
        .assertFailure(IOException.class);
    }

    @Test
    public void dispose() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new Observable<Integer>() {
                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
                }
            }
            .compose(SwingObservable.<Integer>observeOnEdt())
            .take(1)
            .subscribeWith(new TestObserverEx<Integer>());

            to.awaitDone(5, TimeUnit.SECONDS)
            .assertResult(1);

            assertEquals(1, errors.size());
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void abstractEventConsumer() {
        AbstractEventConsumer<Object, Object> aec = new AbstractEventConsumer<Object, Object>(new TestObserverEx<>(), new Object()) {
            private static final long serialVersionUID = 2275910182880030397L;

            @Override
            protected void onDispose(Object component) {
            }
        };

        assertFalse(aec.isDisposed());

        aec.dispose();

        assertTrue(aec.isDisposed());

        aec.dispose();

        assertTrue(aec.isDisposed());
    }
}
