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
