/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.rxjava2.swing;

import java.lang.reflect.*;

import org.junit.Test;

public class SwingObservableTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(SwingObservable.class);
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
}
