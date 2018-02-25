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

import static hu.akarnokd.rxjava2.swing.SwingObservable.*;
import static org.junit.Assert.*;

import java.awt.EventQueue;
import java.awt.event.*;
import java.awt.font.TextHitInfo;
import java.beans.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.junit.Test;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

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
        final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
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

                TestObserver<ActionEvent> to = actions(button)
                .test();

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
                JComboBox<String> cb = new JComboBox<String>();
                cb.addItem("a");
                cb.addItem("b");

                cb.setSelectedIndex(0);

                TestObserver<ActionEvent> to = actions(cb)
                .test();

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

                TestObserver<MouseEvent> to = mouse(cb)
                .test();

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
    public void componentMouseWheel() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserver<MouseWheelEvent> to = mouseWheel(cb)
                .test();

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
    public void componentKeyboard() {
        runEdt(new Runnable() {
            @Override
            public void run() {
                JLabel cb = new JLabel("abc");

                TestObserver<KeyEvent> to = keyboard(cb)
                .test();

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

                TestObserver<ComponentEvent> to = component(cb)
                .test();

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

                TestObserver<FocusEvent> to = focus(cb)
                .test();

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

                TestObserver<HierarchyEvent> to = hierarchyBounds(cb)
                .test();

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

                TestObserver<HierarchyEvent> to = hierarchy(cb)
                .test();

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

                TestObserver<InputMethodEvent> to = inputMethod(cb)
                .test();

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

                TestObserver<PropertyChangeEvent> to = propertyChange(cb)
                .test();

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

                TestObserver<PropertyChangeEvent> to = propertyChange(cb, "property")
                .test();

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

                TestObserver<DocumentEvent> to = document(cb)
                .test();

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

                TestObserver<UndoableEditEvent> to = undoableEdit(cb)
                .test();

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

                TestObserver<CaretEvent> to = caret(cb)
                .test();

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

                TestObserver<AncestorEvent> to = ancestor(cb)
                .test();

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

                TestObserver<VetoablePropertyChangeEvent> to = vetoableChange(cb)
                .test();

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

                TestObserver<VetoablePropertyChangeEvent> to = vetoableChange(cb)
                .doOnNext(new Consumer<VetoablePropertyChangeEvent>() {
                    @Override
                    public void accept(VetoablePropertyChangeEvent e) throws Exception {
                        e.veto();
                    }
                })
                .test();

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
}
