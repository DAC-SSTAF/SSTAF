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

package mil.sstaf.core.entity;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static mil.sstaf.core.entity.Force.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityHandleTest {

    @Test
    void testToString() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        ep1.setForce(BLUE);

        String string = ep1.toString();
        String answer = "id=" + ep1.getId() + " forces=BLUE path=PATH1:PATH2:Fred name=Fred";
        assertEquals(answer, string);
    }

    @Test
    void getForce() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep2 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep3 = new EntityHandle(makeEntity("Fred"));

        ep1.setForce(BLUE);
        ep2.setForce(RED);
        ep3.setForce(GRAY);
        assertEquals(BLUE, ep1.getForce());
        assertEquals(RED, ep2.getForce());
        assertEquals(GRAY, ep3.getForce());
    }

    @Test
    void getId() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep2 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep3 = new EntityHandle(makeEntity("Fred"));

        assertTrue(ep1.getId() > MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
        assertEquals(ep1.getId() + 1, ep2.getId());
        assertEquals(ep2.getId() + 1, ep3.getId());
    }

    @Test
    void getName() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep2 = new EntityHandle(makeEntity("Wilma"));
        EntityHandle ep3 = new EntityHandle(makeEntity("Barney"));

        assertEquals("Fred", ep1.getName());
        assertEquals("Wilma", ep2.getName());
        assertEquals("Barney", ep3.getName());
    }

    @Test
    void testEquals() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep2 = new EntityHandle(makeEntity("Wilma"));
        EntityHandle ep3 = new EntityHandle(makeEntity("Barney"));

        assertEquals(ep1, ep1);
        assertEquals(ep2, ep2);
        assertEquals(ep3, ep3);
        Assertions.assertNotEquals(ep1, ep2);
        Assertions.assertNotEquals(ep1, ep3);
        Assertions.assertNotEquals(ep2, ep3);
        Assertions.assertNotEquals(ep3, ep1);
        Assertions.assertNotEquals(ep2, ep1);
        Assertions.assertNotEquals(ep3, ep2);
    }

    @Test
    void testHashCode() {
        BaseEntity fred = makeEntity("Fred");
        EntityHandle ep1 = new EntityHandle(fred);
        assertEquals(fred.hashCode(), ep1.hashCode());
    }

    @Test
    void compareTo() {
        EntityHandle ep1 = new EntityHandle(makeEntity("Fred"));
        EntityHandle ep2 = new EntityHandle(makeEntity("Wilma"));
        EntityHandle ep3 = new EntityHandle(makeEntity("Barney"));
        assertTrue(ep1.compareTo(ep2) < 0);
        assertTrue(ep2.compareTo(ep3) < 0);
        assertTrue(ep3.compareTo(ep1) > 0);
    }

    BaseEntity makeEntity(final String name) {
        var builder = FakeEntity.builder();
        builder.name(name);
        return builder.build();
    }

    @SuperBuilder
    static class FakeEntity extends BaseEntity {

        @Override
        public String getPath() {
            return "PATH1:PATH2:" + getName();
        }

    }
}
