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

import mil.sstaf.core.features.Requires;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static mil.sstaf.core.util.ReflectionUtils.getAllFields;

/**
 * Utility class for performing dependency injection. Given the very limited use-cases for DI in SSTAF,
 * this was much simpler than using Guice or Spring.
 */
public class Injector {
    private static final Logger logger = LoggerFactory.getLogger(Injector.class);

    /**
     * Injects an object into a specific field.
     *
     * @param target         the {@code Object} containing the Field.
     * @param field          the {@code Field} into which the implementation object is to be injected.
     * @param implementation the object ot be injected.
     * @param <T>            the type of the implementation
     */
    public static <T> void injectField(final Object target, final Field field, final T implementation) {
        try {
            final boolean accessible = field.canAccess(target);
            if (!accessible) {
                field.setAccessible(true);
            }
            field.set(target, implementation);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            //
            // This block should not be reachable since using setAccessible(true)
            // makes private values writeable.,
            //
            e.printStackTrace();
        }
    }

    /**
     * Injects an implementation into the specified target Object.
     *
     * @param target         the {@code Object} containing the Field.
     * @param implementation the object ot be injected.
     * @param <T>            the type of the implementation
     */
    public static <T> void inject(final Object target, final T implementation) {
        Objects.requireNonNull(target, "Target is null");
        Objects.requireNonNull(implementation, "Implementation is null");
        Class<?> c = target.getClass();

        getAllFields(c).forEach(field -> {
            if (field.getAnnotation(Injected.class) != null ||
                    field.getAnnotation(Requires.class) != null) {
                Class<?> type = field.getType();
                if (type.isAssignableFrom(implementation.getClass())) {
                    injectField(target, field, implementation);
                }
            }
        });
    }


    /**
     * Injects an implementation into the specified target Object at the field marked with specified field name.
     *
     * @param target         the {@code Object} containing the Field.
     * @param fieldName      the name/label of the injection point.
     * @param implementation the object ot be injected.
     * @param <T>            the type of the implementation.
     */
    public static <T> void inject(final Object target, final String fieldName, final T implementation) {
        Objects.requireNonNull(target, "Target is null");
        Objects.requireNonNull(fieldName, "Field name is null");
        Objects.requireNonNull(implementation, "Implementation is null");
        Class<?> c = target.getClass();
        getAllFields(c).forEach(field -> {
            Injected injected = field.getAnnotation(Injected.class);
            if (injected != null && injected.name().equals(fieldName)) {
                Class<?> type = field.getType();
                if (type.isAssignableFrom(implementation.getClass())) {
                    logger.trace("Injecting {} into {}", implementation, field.getName());
                    injectField(target, field, implementation);
                }
            }
        });
    }


    /**
     * Injects multiple objects into a single target object
     *
     * @param target          the {@code Object} containing the Field.
     * @param implementations the objects ot be injected.
     */
    public static void injectAll(final Object target, final Object... implementations) {
        Arrays.asList(implementations).forEach(implementation -> {
            logger.trace("Injecting {} into {}", implementation, target);
            if (implementation != null) inject(target, implementation);
        });
    }

}

