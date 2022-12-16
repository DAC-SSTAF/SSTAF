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

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InjectorTest {

    @Test
    void canInjectArbitraryObjectsByType() {

        InjectionTarget injectionTarget = new InjectionTarget();

        Long lv = 3141592L;
        String sv = "I am the very model of a modern Major General";
        UUID uv = UUID.randomUUID();

        assertNull(injectionTarget.longValue);
        assertNull(injectionTarget.stringValue);
        assertNull(injectionTarget.uuid);

        Injector.inject(injectionTarget, lv);
        Injector.inject(injectionTarget, sv);
        Injector.inject(injectionTarget, uv);

        assertEquals(lv, injectionTarget.longValue);
        assertEquals(sv, injectionTarget.stringValue);
        assertEquals(uv, injectionTarget.uuid);
    }

    @Test
    void canInjectObjectsUsingVarargs() {

        InjectionTarget injectionTarget = new InjectionTarget();

        Long lv = 3141592L;
        String sv = "I am the very model of a modern Major General";
        UUID uv = UUID.randomUUID();


        assertNull(injectionTarget.longValue);
        assertNull(injectionTarget.stringValue);
        assertNull(injectionTarget.uuid);

        Injector.injectAll(injectionTarget, lv, sv, uv);

        assertEquals(lv, injectionTarget.longValue);
        assertEquals(sv, injectionTarget.stringValue);
        assertEquals(uv, injectionTarget.uuid);
    }

    @Test
    void canSpecifyNames() {

        NamedTarget target = new NamedTarget();

        assertNull(target.in);
        assertNull(target.out);

        Queue<Object> iv = new LinkedBlockingDeque<>();
        Queue<Object> ov = new ArrayDeque<>();

        Injector.inject(target, "inQueue", iv);
        Injector.inject(target, "outQueue", ov);

        assertEquals(iv, target.in);
        assertEquals(ov, target.out);
    }

    private static class InjectionTarget {
        @Injected
        private final Long longValue = null;
        @Injected
        private final String stringValue = null;
        @Injected
        private final UUID uuid = null;
    }

    private static class NamedTarget {
        @Injected(name = "inQueue")
        Queue<Object> in = null;
        @Injected(name = "outQueue")
        Queue<Object> out = null;
    }
}

