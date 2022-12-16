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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {

    @Test
    void canCreateAFullAddress() {
        EntityHandle eh = TestEntity.builder().build().getHandle();
        String handlerName = "Bob";
        Address address = Address.makeAddress(eh, handlerName);
        assertEquals(eh, address.entityHandle);
        assertEquals(handlerName, address.handlerName);
        Assertions.assertTrue(address.isExternal());
        Assertions.assertFalse(address.isInternal());
    }

    @Test
    void equalsAndHashcodeWork() {
        EntityHandle eh1 = TestEntity.builder().build().getHandle();
        String handlerName1 = "Bob";
        Address address1a = Address.makeAddress(eh1, handlerName1);
        Address address1b = Address.makeAddress(eh1, handlerName1);

        EntityHandle eh2 = TestEntity.builder().build().getHandle();
        String handlerName2 = "Jackie";
        Address address2a = Address.makeAddress(eh2, handlerName2);

        assertEquals(address1a, address1b);
        assertNotEquals(address1a, address2a);
        assertEquals(address1a.hashCode(), address1b.hashCode());
        assertNotEquals(address1a.hashCode(), address2a.hashCode());
    }

    @Test
    void canCreateAnExternalAddress() {
        EntityHandle eh = TestEntity.builder().build().getHandle();
        Address address = Address.makeExternalAddress(eh);
        assertEquals(eh, address.entityHandle);
        assertNull(address.handlerName);
        Assertions.assertTrue(address.isExternal());
        Assertions.assertFalse(address.isInternal());
    }

    @Test
    void canCreateAnInternalAddress() {
        String handlerName = "Bob";
        Address address = Address.makeInternalAddress(handlerName);
        assertNull(address.entityHandle);
        assertEquals(handlerName, address.handlerName);
        Assertions.assertFalse(address.isExternal());
        Assertions.assertTrue(address.isInternal());
    }

    @Test
    void addressComparatorWorks() {
        List<Entity> entities = List.of(TestEntity.builder().build(),
                TestEntity.builder().build(),
                TestEntity.builder().build(),
                TestEntity.builder().build());
        entities.forEach(entity -> entity.setForce(Force.BLUE));

        List<String> handlers = List.of("Handler1", "Handler2", "Handler3", "Handler4");

        Address[][] addresses = new Address[4][4];

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                addresses[i][j] = Address.makeAddress(entities.get(i).getHandle(), handlers.get(j));
            }
        }

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    for (int l = 0; l < 4; ++l) {
                        Address first = addresses[i][j];
                        Address second = addresses[k][l];

                        int result = Address.COMPARATOR.compare(first, second);

                        //logger.debug("{} {} {} {} = {} ", i,j,k,l,result);

                        if (i == k) {
                            if (j == l) {
                                Assertions.assertEquals(0, result);
                            } else if (j < l) {
                                Assertions.assertEquals(-1, result);
                            } else {
                                Assertions.assertEquals(1, result);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                Address first = addresses[i][j];
                Address external = Address.makeExternalAddress(first.entityHandle);
                Address internal = Address.makeInternalAddress(first.handlerName);
                Assertions.assertEquals(-1, Address.COMPARATOR.compare(first, external));
                Assertions.assertEquals(1, Address.COMPARATOR.compare(first, internal));
                Assertions.assertEquals(1, Address.COMPARATOR.compare(external, first));
                Assertions.assertEquals(-1, Address.COMPARATOR.compare(internal, first));
            }
        }
    }
}

