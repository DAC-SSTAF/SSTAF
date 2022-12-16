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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EntityRegistryTest {
    @Nested
    @DisplayName("Test the 'Happy Path'")
    class HappyTests{
        @Test
        @DisplayName("Confirm that an entity can be registered and retrieved by ID")
        void testEntityRegistry1 () {
            EntityRegistry entityRegistry = new EntityRegistry();

            DummyEntity bob = new DummyEntity("Bob", 123);
            DummyEntity fred = new DummyEntity("Fred", 456);
            entityRegistry.registerEntity(Force.BLUE, bob);
            entityRegistry.registerEntity(Force.RED, fred);
            entityRegistry.compileEntityMaps();
            assertEquals(2, entityRegistry.getAllEntities().size());
            assertTrue(entityRegistry.getEntity(123L).isPresent());
            assertTrue(entityRegistry.getEntity(456L).isPresent());
            assertFalse(entityRegistry.getEntity(314L).isPresent());
        }

        @Test
        @DisplayName("Confirm that an entity can be registered and retrieved by EntityHandle")
        void testEntityRegistry2 () {
            EntityRegistry entityRegistry = new EntityRegistry();

            DummyEntity bob = new DummyEntity("Bob", 123);
            DummyEntity fred = new DummyEntity("Fred", 456);
            entityRegistry.registerEntity(Force.BLUE, bob);
            entityRegistry.registerEntity(Force.RED, fred);
            entityRegistry.compileEntityMaps();
            assertEquals(2, entityRegistry.getAllEntities().size());

            EntityHandle eh1 = bob.getHandle();
            assertNotNull(eh1);
            EntityHandle eh2 = fred.getHandle();
            assertNotNull(eh2);

            Optional<Entity> optBob = entityRegistry.getEntityByHandle(eh1);
            Optional<Entity> optFred = entityRegistry.getEntityByHandle(eh2);

            assertTrue(optBob.isPresent());
            assertTrue(optFred.isPresent());

            assertEquals(bob, optBob.get());
            assertEquals(fred, optFred.get());
        }

        @Test
        @DisplayName("Confirm that support for the client address works")
        void testEntityRegistry3 () {
            EntityRegistry entityRegistry = new EntityRegistry();
            EntityHandle entityHandle = EntityHandle.makeDummyHandle();
            entityRegistry.setClientAddress(entityHandle);
            EntityHandle eh = entityRegistry.getClientAddress().entityHandle;
            assertEquals(entityHandle, eh);
        }

        @Test
        @DisplayName("Confirm that all entities are in the registry")
        void testEntityRegistry4 () {
            EntityRegistry entityRegistry = new EntityRegistry();

            List<DummyEntity> dummies = List.of(new DummyEntity("Bob", 123),
                    new DummyEntity("Fred", 456),
                    new DummyEntity("Wilma", 342134));

            for (BaseEntity e : dummies) {
                entityRegistry.registerEntity(Force.BLUE, e);
            }
            entityRegistry.compileEntityMaps();
            Collection<EntityHandle> allEHs = entityRegistry.getAllEntityHandles();

            Assertions.assertEquals(3, allEHs.size());
            for (Entity e : dummies) {
                Assertions.assertTrue(allEHs.contains(e.getHandle()));
            }
        }

        @Test
        @DisplayName("Confirm that all system entities are not in the simulation entities registry")
        void testEntityRegistry5 () {
            EntityRegistry entityRegistry = new EntityRegistry();

            List<DummyEntity> dummies = List.of(new DummyEntity("Bob", 123),
                    new DummyEntity("Fred", 456),
                    new DummyEntity("Wilma", 342134));

            for (BaseEntity e : dummies) {
                entityRegistry.registerEntity(Force.BLUE, e);
            }
            entityRegistry.compileEntityMaps();

            DummyEntity systemThing = new DummyEntity("Wacko", -1024);
            entityRegistry.registerEntity(Force.SYSTEM, systemThing);

            Collection<Entity> entities = entityRegistry.getSimulationEntities();

            Assertions.assertEquals(3, entities.size());
            for (Entity e : dummies) {
                Assertions.assertTrue(entities.contains(e));
            }

            Assertions.assertFalse(entities.contains(systemThing));
        }


        @Test
        @DisplayName("Confirm that getting an EntityHandle by ID works")
        void testEntityRegistry6 () {
            EntityRegistry entityRegistry = new EntityRegistry();
            DummyEntity bob = new DummyEntity("Bob", 123);
            DummyEntity fred = new DummyEntity("Fred", 456);
            entityRegistry.registerEntity(Force.BLUE, bob);
            entityRegistry.registerEntity(Force.RED, fred);
            entityRegistry.compileEntityMaps();
            Optional<EntityHandle> optBob = entityRegistry.getHandle(123L);
            Assertions.assertTrue(optBob.isPresent());
            Assertions.assertEquals(bob.getHandle(), optBob.get());

            Optional<EntityHandle> optFred = entityRegistry.getHandle(456L);
            Assertions.assertTrue(optFred.isPresent());
            Assertions.assertEquals(fred.getHandle(), optFred.get());

            Optional<EntityHandle> optNobody = entityRegistry.getHandle(314L);
            Assertions.assertFalse(optNobody.isPresent());
        }
    }

    static class DummyEntity extends BaseEntity {

        DummyEntity(String name, long id) {
            super(name, id);
        }

        @Override
        public String getPath() {
            return getName();
        }


    }
}
