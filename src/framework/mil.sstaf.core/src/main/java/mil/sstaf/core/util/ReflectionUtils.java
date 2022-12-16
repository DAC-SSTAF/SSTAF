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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General-purpose utility methods for handling some reflective tasks.
 */
public class ReflectionUtils {

    private static void addFieldsForClass(Class<?> c, List<Field> l) {
        l.addAll(Arrays.asList(c.getDeclaredFields()));
    }

    public static List<Field> getAllFields(Class<?> c) {
        List<Field> fields = new ArrayList<>();
        addFieldsForClass(c, fields);
        Class<?> sc = c;
        while ((sc = sc.getSuperclass()) != null) {
            addFieldsForClass(sc, fields);
        }
        return fields;
    }

    public static <A extends Annotation> List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<A> annotationClass) {
        List<Method> methods = new ArrayList<>(2);
        for (Method m : clazz.getDeclaredMethods()) {
            final A annotation = m.getAnnotation(annotationClass);
            if (annotation != null) {
                methods.add(m);
            }
        }
        return methods;
    }

    public static <A extends Annotation> List<Method> getMethodsWithAnnotationOrThrow(Class<?> clazz, Class<A> annotationClass) {
        List<Method> methods = getMethodsWithAnnotation(clazz, annotationClass);
        if (methods.size() > 0) {
            return methods;
        } else {
            String msg = String.format("Did not find a method annotated with '%s' in class '%s'",
                    annotationClass.getName(), clazz.getName());
            throw new SSTAFException(msg);
        }

    }
}

