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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import mil.sstaf.core.json.EntityHandleSerializer;

import java.util.Comparator;
import java.util.Objects;

/**
 * An indirect reference to an {@code Entity} that is used for message routing.
 * <p>
 * The {@code EntityHandle} provides the ability for the {@code Session}, as well as
 * {@code Handler}s and {@code Agent}s, to create {@code Message}s for other {@code Entities}
 * without granting them direct access to the {@code Entity}. Direct access is avoided
 * to prevent uncontrolled data access across threads.
 */
@JsonSerialize(using= EntityHandleSerializer.class)
final public class EntityHandle implements Comparable<EntityHandle> {
    public static final Comparator<EntityHandle> comparator = new MyComparator();

    private final Entity wrapped;

    /**
     * Constructor
     *
     * @param entity the {@code Entity} to which the handle refers
     */
    public EntityHandle(final Entity entity) {
        this.wrapped = Objects.requireNonNull(entity, "Entity must not be null");
    }

    private EntityHandle() {
        wrapped = new BaseEntity.Dummy();
    }

    public static EntityHandle makeDummyHandle() {
        return new EntityHandle();
    }

    /**
     * Provides the wrapped {@code Entity}
     *
     * @return the Entity
     */
    Entity getWrapped() {
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "id=" + wrapped.getId()
                + " forces=" + wrapped.getForce()
                + " path=" + wrapped.getPath()
                + " name=" + wrapped.getName();
    }

    public String getForcePath() {
        return getForce().name() + Entity.ENTITY_PATH_DELIMITER + getPath();
    }

    /**
     * Provides the Forces to which the Entity belongs.
     *
     * @return the Forces enum
     */
    public Force getForce() {
        return wrapped.getForce();
    }

    /**
     * Sets the force membership for the Entity.
     *
     * @param force the {@code Forces}
     */
    public void setForce(Force force) {
        wrapped.setForce(force);
    }

    /**
     * Provides the unique ID number for the Entity.
     *
     * @return the id number
     */
    public long getId() {
        return wrapped.getId();
    }

    /**
     * Provides the next message sequence number for messages originating from the
     * wrapped {@code Entity}
     *
     * @return the sequence number
     */
    public long getMessageSequenceNumber() {
        return wrapped.generateSequenceNumber();
    }

    /**
     * Provides the current path for this Entity.
     * <p>
     * For Soldiers, the path is the unit hierarchy and position within their containing Unit. For
     * other Entities it is simply their name. Since Soldiers can be moved between Units their path
     * can change
     *
     * @return the path
     */
    public String getPath() {
        return wrapped.getPath();
    }

    /**
     * Provides the name of the Entity
     *
     * @return the name
     */
    public String getName() {
        return wrapped.getName();
    }

    /**
     * Returns a composite label that constists of the Entity path and name
     *
     * @return the composite label
     */
    public String getPathAndName() {
        String path = getPath();
        String name = getName();
        return (path == null ? "null" : path) + "[" + (name == null ? "null" : name) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityHandle that = (EntityHandle) o;
        return Objects.equals(wrapped, that.wrapped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(EntityHandle o) {
        return Long.compare(wrapped.getId(), o.wrapped.getId());
    }

    static class MyComparator implements Comparator<EntityHandle> {

        @Override
        public int compare(EntityHandle o1, EntityHandle o2) {
            if (o1 == o2) {
                return 0;
            } else if (o2 == null) {
                return -1;
            } else if (o1 == null) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }

    /**
     * Answers whether or not the wrapped Entity has a {@code Handler} that can process the content.
     *
     * @param contentClass the content to check
     * @return true if it can be handled, false otherwise.
     */
    public boolean canHandle(Class<?> contentClass) {
        return wrapped.canHandle(contentClass);
    }

}

