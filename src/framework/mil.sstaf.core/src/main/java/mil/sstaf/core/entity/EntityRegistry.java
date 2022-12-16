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

import java.util.*;

/**
 * Registry for all Entity instances in the SSTAF environment.
 * <p>
 * Entities are divided into two categories, simulation and system. Simulation entities
 * are those Entities that represent participants in the simulation. Examples include Soldiers and Units.
 * System entities are support elements within the environment.
 */
public class EntityRegistry {

    private final Map<Force, List<EntityHandle>> forcesMap = new EnumMap<>(Force.class);
    private final Map<Long, EntityHandle> allEntityHandles = new HashMap<>();
    // 2022.04.15 : RAB : Added direct Path to Handle mapping
    private final Map<String, EntityHandle> pathToHandleMap = new HashMap<>();
    private final Map<Long, Entity> allEntities = new HashMap<>();
    private final List<Entity> simulationEntities = new ArrayList<>();
    private Address clientAddress;


    public void registerEntities(Map<Force, List<BaseEntity>> participants) {
        for (var forceEntry : participants.entrySet()) {
            for (var entity : forceEntry.getValue()) {
                registerEntity(forceEntry.getKey(), entity);
            }
        }
    }

    /**
     * Registers an {@code Entity} and assigns it to the specified {@code Force}
     *
     * @param force  the {@code Force}
     * @param entity the {@code Entity}
     */
    public void registerEntity(final Force force, final Entity entity) {
        entity.setForce(force);
        EntityHandle entityHandle = entity.getHandle();

        List<EntityHandle> forceList = forcesMap.computeIfAbsent(force, forces1 -> new ArrayList<>());
        forceList.add(entityHandle);

        if (entity instanceof Unit) {
            Unit unit = (Unit) entity;
            for (var s : unit.getDirectMembers().values()) {
                registerEntity(force, s);
            }
            for (var u : unit.getSubUnitMap().values()) {
                registerEntity(force, u);
            }
        }
    }

    /**
     * Traverse the Forces map and builds other Collections to
     * facilitate lookups.
     */
    public void compileEntityMaps() {
        for (Force force : Force.values()) {
            List<EntityHandle> entityHandleList = forcesMap.computeIfAbsent(force, force1 -> new ArrayList<>());
            for (EntityHandle entityHandle : entityHandleList) {
                Entity entity = entityHandle.getWrapped();
                long id = entity.getId();
                allEntities.put(id, entity);
                allEntityHandles.put(id, entityHandle);
                String path = force.name() + Entity.ENTITY_PATH_DELIMITER + entity.getPath();
                pathToHandleMap.put(path, entityHandle);

                if (force != Force.SYSTEM) {
                    simulationEntities.add(entity);
                }
            }
        }
    }


    /**
     * Provides the {@code Address} for the client proxy
     *
     * @return the {@code Address}
     */
    public Address getClientAddress() {
        return clientAddress;
    }

    /**
     * Creates and sets the {@code Address} for the client proxy
     *
     * @param clientHandle the {@code EntityHandle} for the client.
     */
    public void setClientAddress(EntityHandle clientHandle) {
        this.clientAddress = Address.makeExternalAddress(clientHandle);
    }

    /**
     * Provides all the entities
     *
     * @return an unmodifiable collection of all the registered entities.
     */
    public Collection<Entity> getAllEntities() {
        return Collections.unmodifiableCollection(allEntities.values());
    }

    /**
     * Provides all the simulation entities
     *
     * @return an unmodifiable
     */
    public Collection<Entity> getSimulationEntities() {
        return Collections.unmodifiableCollection(simulationEntities);
    }

    /**
     * Provides all of the {@code EntityHandle}s
     *
     * @return an unmodifiable collection of EntityHandles
     */
    public Collection<EntityHandle> getAllEntityHandles() {
        return Collections.unmodifiableCollection(allEntityHandles.values());
    }

    /**
     * Provides the Entity associated with the given handle
     *
     * @param handle the {@code EntityHandle} to dereference
     * @return an Optional that contains the Entity or is empty if the EntityHandle did not wrap an Entity.
     */
    public Optional<Entity> getEntityByHandle(EntityHandle handle) {
        Objects.requireNonNull(handle, "EntityHandle must not be null");
        return Optional.ofNullable(handle.getWrapped());
    }

    /**
     * Provides the EntityHandle associated with the given id
     *
     * @param id the id
     * @return an Optional that contains the EntityHandle or is empty if the id did not correspond to an Entity
     */
    public Optional<EntityHandle> getHandle(final Long id) {
        if (allEntityHandles.containsKey(id)) {
            return Optional.ofNullable(allEntityHandles.get(id));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Provides the EntityHandle associated with the given path string
     *
     * @param path the path to the entity
     * @return an Optional that contains the EntityHandle or is empty if the path did not correspond to an Entity
     */
    public Optional<EntityHandle> getHandle(final String path) {

        String fullPath;
        boolean hasForcePrefix = false;

        if (path.contains(":")) {
            for (Force f : Force.values()) {
                if (path.startsWith(f.name())) {
                    hasForcePrefix = true;
                    break;
                }
            }
            if (!hasForcePrefix) {
                fullPath = Force.BLUE.name() + Entity.ENTITY_PATH_DELIMITER + path;
            } else {
                fullPath = path;
            }
        } else {
            fullPath = Force.BLUE.name() + Entity.ENTITY_PATH_DELIMITER + path;
        }

        if (pathToHandleMap.containsKey(fullPath)) {
            return Optional.ofNullable(pathToHandleMap.get(fullPath));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Provides the Entity associated with the given id
     *
     * @param id the id
     * @return an Optional that contains the Entity or is empty if the id did not correspond to an Entity.
     */
    public Optional<Entity> getEntity(final Long id) {
        if (allEntities.containsKey(id)) {
            return Optional.ofNullable(allEntities.get(id));
        } else {
            return Optional.empty();
        }
    }
}

