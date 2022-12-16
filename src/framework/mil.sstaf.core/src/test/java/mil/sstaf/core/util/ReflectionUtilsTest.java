/*
 * Copyright (c) 2022
 * United States Government as represented by the U.S. Army DEVCOM Analysis Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mil.sstaf.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.beans.BeanProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReflectionUtilsTest {

    @Test
    @DisplayName("Confirm that a method annotated with a specific Annotation can be found")
    void test1() {

        class Oingo {
            private final int bob = 14;

            @Deprecated
            public int getBob() {
                return bob;
            }
        }

        class Boingo {
            public Object getIt() {
                return null;
            }

            public Object getItToo() {
                return null;
            }
        }

        List<Method> methodList = ReflectionUtils.getMethodsWithAnnotation(Oingo.class, Deprecated.class);
        assertEquals(1, methodList.size());
        assertEquals("getBob", methodList.get(0).getName());

        methodList = ReflectionUtils.getMethodsWithAnnotation(Boingo.class, Deprecated.class);
        assertEquals(0, methodList.size());
    }

    @Test
    @DisplayName("Confirm that if a required annotation is not found a SSTAFException is thrown.")
    void test2() {

        class Boingo {
            @BeanProperty
            public Object getIt() {
                return null;
            }

            @BeanProperty
            public Object getItToo() {
                return null;
            }
        }

        Assertions.assertDoesNotThrow(() -> {
            List<Method> methodList = ReflectionUtils.getMethodsWithAnnotationOrThrow(Boingo.class, BeanProperty.class);
            assertEquals(2, methodList.size());
            assertTrue(methodList.stream().anyMatch(it -> it.getName().matches("getIt")));
            assertTrue(methodList.stream().anyMatch(it -> it.getName().matches("getItToo")));
        });

        SSTAFException boom = Assertions.assertThrows(SSTAFException.class, () -> {
            List<Method> methodList = ReflectionUtils.getMethodsWithAnnotationOrThrow(Boingo.class,
                    Deprecated.class);
            assertEquals(0, methodList.size());
        });

        assertTrue(boom.getMessage().contains("Deprecated"));
        assertTrue(boom.getMessage().contains("Boingo"));
    }

    @Test
    @DisplayName("Confirm that getAllFields() gets all fields in a hierarchy")
    void test3() {
        List<Field> fields = ReflectionUtils.getAllFields(C.class);
        Assertions.assertEquals(5, fields.size());
        boolean haveBob = false;
        boolean haveSam = false;
        boolean haveEmma = false;
        boolean haveEarl = false;
        boolean haveTheList = false;
        for (Field f : fields) {
            if (f.getName().equals("bob")) haveBob = true;
            if (f.getName().equals("sam")) haveSam = true;
            if (f.getName().equals("emma")) haveEmma = true;
            if (f.getName().equals("earl")) haveEarl = true;
            if (f.getName().equals("theList")) haveTheList = true;
        }
        assertTrue(haveBob);
        assertTrue(haveSam);
        assertTrue(haveEmma);
        assertTrue(haveEarl);
        assertTrue(haveTheList);
    }

    static class A {
        private Integer bob;
        private String sam;
    }

    static class B extends A {
        private Double emma;
        private Long earl;
    }

    static class C extends B {
        private List<String> theList;
    }
}

