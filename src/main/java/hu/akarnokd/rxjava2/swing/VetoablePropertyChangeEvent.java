package hu.akarnokd.rxjava2.swing;

import java.beans.PropertyChangeEvent;

/**
 * A PropertyChangeEvent that has an explicit way of indicating a vetoed
 * change via {@link #veto()}.
 */
public final class VetoablePropertyChangeEvent extends PropertyChangeEvent {

    private static final long serialVersionUID = -7246275491670353237L;

    volatile boolean vetoed;

    public VetoablePropertyChangeEvent(Object source, String propertyName,
            Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    public void veto() {
        vetoed = true;
    }

    public boolean isVetoed() {
        return vetoed;
    }
}
